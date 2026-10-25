# CLAUDE.md

이 파일은 이 저장소에서 작업하는 Claude Code(claude.ai/code)에게 안내를 제공합니다.

## 프로젝트 개요

EEUM(이음)은 iOS 음악 공유 커뮤니티를 위한 Spring Boot 4 백엔드입니다. 사용자가 Apple Music 트랙과 함께 사연(게시물)을 올리면, 다른 사용자가 음악이
첨부된 댓글로 반응합니다. "흔들기(Shake)" 기능은 Redis 랜덤 풀에서 O(1)로 랜덤 게시물을 반환합니다.

## 명령어

```bash
./gradlew build                                   # 빌드 (테스트 포함)
./gradlew bootRun                                 # 실행 (local 프로파일, 포트 8080)
./gradlew test                                    # 전체 테스트
./gradlew test --tests "com.eeum.SomeTest"        # 단일 테스트 클래스
```

- 프로파일: `local`, `dev`, `prod` — `SPRING_PROFILES_ACTIVE` 환경 변수로 지정 (`application.yml` 기본값은
  `local`).
- 로컬 실행에는 MySQL(`127.0.0.1:3306/eeum`)과 Redis(`localhost:6379`)만 필요합니다. 메시지 브로커는 없습니다.

## 기술 스택

- **Java 21 / Spring Boot 4.0**, Gradle
    - Jackson 3 사용: `ObjectMapper`/`JacksonException`은 `tools.jackson.*` 패키지에서 import (
      `com.fasterxml.jackson.databind` 아님).
- **MySQL** — JPA/Hibernate, TSID 기본키(`hypersistence-utils-hibernate-71`)
- **Flyway** — 스키마 마이그레이션 (`src/main/resources/db/migration/V{n}__*.sql`), `ddl-auto: validate`
- **Redis** — 캐시/카운터/분산 락/랜덤 풀 + **Pub/Sub 메시징**
- **Caffeine** — 로컬 캐시 (`CacheConfig`, 1시간 TTL, 최대 500개) — Apple Music 개발자 토큰 캐싱
- **Spring AI 2.0 + Anthropic Claude (Haiku 4.5)** — 스팸 필터 AI 분류 (`ChatClient`)
- **Apple Music API** — ES256 JWT 개발자 토큰(`AppleMusickit`), `RestClient` + 아웃바운드 rate limit/재시도
- **Firebase Admin SDK (FCM)** — 푸시 알림 (iOS는 FCM의 APNs 설정으로 전달)
- **Kakao & Apple OIDC + JWT (HS256)** — 무상태 인증
- **Spring Mail (Gmail SMTP)**, **Discord 웹훅** — 운영 알림
- **SpringDoc OpenAPI 2.3** — Swagger UI (`/swagger-ui.html`)
- **Actuator** — `/actuator/health`를 블루/그린 배포 헬스체크에 사용

> `build.gradle`에 `open-korean-text`(형태소 분석기) 의존성이 남아 있지만 코드에서는 더 이상 사용하지 않습니다.

## 아키텍처

### 요청 흐름

```
iOS → nginx(rate limit) → MdcFilter(traceId) → JWTFilter → RateLimitInterceptor
    → Controller → Service → Repository → MySQL / Redis
                       ├─ Redis Pub/Sub Publisher → Subscriber → (Claude 스팸 필터 / SMTP / Discord)
                       └─ ApplicationEvent → (같은 TX) 알림 행 PENDING 저장
                              → @TransactionalEventListener(AFTER_COMMIT) + @Async → NotificationDispatcher(claim) → FCM
```

### 패키지 구조

```
com.eeum/
├── domain/
│   ├── posts/         # 게시물 CRUD, 흔들기(Redis 랜덤 풀), Apple Music 검색
│   │   ├── gateway/   #   MusicSearchGateway / AppleMusicSearchGateway, DeveloperTokenProvider(Caffeine 캐시)
│   │   └── batch/     #   AppleMusicTokenGenerateJob — 분기별(1·4·7·10월 1일 03시 KST) 토큰 재발급
│   ├── user/          # OIDC 로그인(카카오/애플), 게스트 로그인, JWT, 약관, 계정 삭제
│   ├── comment/       # 음악 첨부 댓글(음악 필수), 댓글 수 제한 도달 시 게시물 완료 처리
│   │   ├── event/     #   FirstCommentCreatedEvent (첫 댓글 → 푸시 알림)
│   │   └── producer/  #   CommentProducer — 게시물 완료를 Redis 채널로 발행
│   ├── block/         # 사용자 차단 (자기 자신 차단 불가), Redis 쿼리 모델 캐시
│   ├── like/          # 좋아요 토글, Redis 분산 락 + 카운터
│   ├── view/          # Redis 조회수 카운터, 10회마다 MySQL 백업
│   ├── report/        # 신고 처리(ReportFacade), Discord 웹훅 알림
│   ├── notification/
│   │   ├── publisher/  # SpamFilterPublisher, MailPublisher (Redis Pub/Sub)
│   │   ├── subscriber/ # SpamFilterSubscriber, MailSubscriber, PostsCompletedSubscriber
│   │   ├── service/    # MailService
│   │   └── fcm/        # 디바이스 토큰 등록/삭제, 알림 설정 조회/변경, 알림 이력, FCM 전송
│   │       ├── port/      # PushSender 인터페이스 / FcmPushSender 구현 (500개 단위 전송, 무효 토큰 반환)
│   │       ├── service/   # NotificationService(알림 행 저장), NotificationDispatcher(claim → FCM 전송 → 상태 변경)
│   │       ├── event/     # NotificationCreatedEvent (알림 행 생성 → 커밋 후 전송)
│   │       ├── listener/  # FirstCommentNotificationListener(동기 저장), NotificationDispatcherListener(커밋 후 @Async 전송)
│   │       └── batch/     # NotificationDispatchRetryJob — 1분마다 유실된 PENDING / lease 만료 SENDING 재발송
│   └── common/
│       ├── spamfilter/       # ForbiddenWords 검사 + Claude AI 분류
│       ├── ai/               # AI 요청/응답 DTO
│       └── webhook/discord/  # Discord 메시지 포맷 (스팸/신고/로그인)
└── global/
    ├── config/          # Redis(Pub/Sub 리스너 등록), Cache, Async(MDC 전파), Scheduling, Chat, Web, Swagger, 흔들기 워밍업
    │   └── client/      #   AppleMusicRestClientConfig
    ├── ratelimit/       # TokenBucketRateLimiter (인메모리)
    │   ├── inbound/     #   RateLimitInterceptor — /user/guest, /apple-music/search (429 + Retry-After)
    │   └── outbound/    #   Apple Music 호출 제한 + Retry-After 기반 재시도
    ├── securitycore/    # SecurityConfig, jwt/, oidc/, token/(@CurrentUser, UserPrincipal)
    ├── aop/auth/        # @RequireLogin AOP
    ├── infrastructure/  # applemusickit/(ES256 JWT), fcm/(FirebaseConfig)
    ├── dataserializer/  # DataSerializer — Redis 저장용 JSON 직렬화
    ├── logger/          # MdcFilter(traceId), LoggingFilter(요청/응답 로깅)
    └── support/         # ApiResponse<T>, ErrorCode/ErrorType, CoreApiException, ApiControllerAdvice
```

### 비동기 처리

| 채널 / 이벤트                   | 발행 위치                                | 소비자                                | 동작                                                                |
|----------------------------|--------------------------------------|------------------------------------|-------------------------------------------------------------------|
| `notification:spamfilter`  | `PostsService` (커밋 이후 `afterCommit`) | `SpamFilterSubscriber`             | 금칙어(+2) + Claude 판정(+1) → 1점: Discord 경고, 2점 이상: 게시물 삭제 + Discord |
| `notification:mail`        | `MailPublisher`                      | `MailSubscriber`                   | 메일 발송                                                             |
| `notification:playlist`    | `CommentProducer` (게시물 완료 시)         | `PostsCompletedSubscriber`         | 현재는 로그만 남김                                                        |
| `FirstCommentCreatedEvent` | `CommentService`                     | `FirstCommentNotificationListener` | 동기 `@EventListener` — 댓글 TX 안에서 알림 행을 PENDING으로 저장                   |
| `NotificationCreatedEvent` | `NotificationService`                | `NotificationDispatcherListener`   | 커밋 후 `@Async`로 `NotificationDispatcher.dispatch(id)` 호출               |
| (스케줄러, 1분 주기)              | —                                    | `NotificationDispatchRetryJob`     | 생성 1분 지난 PENDING, lease 만료 SENDING을 `dispatch(id)`로 재발송            |

- 새 채널을 추가하면 `RedisConfig`의 `RedisMessageListenerContainer`에 리스너를 등록해야 합니다.
- Redis Pub/Sub은 영속성/재시도가 없습니다 (DLQ 없음). 구독자는 예외를 잡아 로그만 남깁니다.
- DB 커밋 이후에 발행해야 하는 작업은 `TransactionSynchronization.afterCommit` 또는
  `@TransactionalEventListener(AFTER_COMMIT)`을 사용하는 기존 패턴을 따르세요.

### 주요 패턴

- **TSID** — 모든 기본키에 시간 정렬 ID 사용 (auto-increment 미사용).
- **소프트 삭제** — 게시물/댓글 등은 Hibernate `@SoftDelete(columnName = "deleted", strategy = TIMESTAMP)`를
  사용합니다. `repository.delete()`가 자동으로 soft delete가 되고, 조회 시 삭제된 행은 자동으로 제외됩니다 (수동 `isDeleted` 필터
  불필요).
- **게시글 삭제/복구** — 직접 `postsRepository.delete()`를 호출하지 말고 `PostsDeletionService.softDelete(postId, deletedBy,
  DeleteReason)` / `restore(postId)`를 사용합니다. 조건부 UPDATE로 `deleted`·`deleted_by`·`delete_reason`(`USER/REPORT/SPAM/ADMIN`)을
  기록하고, 커밋 이후 랜덤 풀·`posts::%s` 캐시를 정리(복구 시 미완료 글만 풀에 재등록)합니다.
- **Flyway 마이그레이션** — `ddl-auto: validate`이므로 엔티티 변경 시 반드시 다음 번호의 `V{n}__설명.sql`을 추가해야 합니다 (현재
  V13까지). 기존 마이그레이션 파일은 수정하지 마세요.
- **알림 발송 (아웃박스 + claim)** — 알림 행은 원인 트랜잭션(예: 댓글 작성)에서 `PENDING`으로 함께 저장하고, 전송은 커밋 후
  `NotificationDispatcher.dispatch(id)` 한 곳에서만 합니다. 전송 전 `NotificationRepository.claim`(조건부 UPDATE
  `PENDING → SENDING`, `locked_until` lease 30초)이 1을 반환한 쪽만 전송하므로 비동기 워커와 스케줄러가 겹쳐도 중복되지 않습니다.
  `dispatch`는 트랜잭션 없이 호출되며 claim/markSent/markFailed가 각각 짧은 트랜잭션(Repository의 `@Transactional`)으로 커밋됩니다.
  새 알림 종류는 행 저장 후 `NotificationCreatedEvent(id)`만 발행하면 됩니다.
- **Redis 키** — `도메인::식별자::...` 형식 (예: `posts::%s`, `view::post::%s::view_count`,
  `lock-like::postId::%d::userId::%d`). 랜덤 풀·차단 캐시 등 일부 키는 `app.redis-key-prefix`(`local-`/dev/prod)
  로 네임스페이스를 분리합니다.
- **Redis 워밍업** — 서버 시작 시 `PostsRandomShakeWarmUp`이 활성 게시물을 랜덤 풀에 로드.
- **게시물 완료** — 댓글 수가 `commentCountLimit`에 도달하면 완료 처리. 수동 마감 게시물의 기본 제한은 100.
- **Swagger 인터페이스 분리** — 각 도메인의 `docs/*Api.java` 인터페이스에 `@Operation`을 두고 컨트롤러가 구현.
- **관리자 페이지 인증** — `/admin/**`는 `AdminSecurityConfig`의 별도 체인(`@Order(1)`)이 처리합니다. 카카오 OIDC 로그인(웹,
  `kakao-admin` 등록) + 세션 + CSRF이며, `AdminOidcUserService`가 `admin.allowed-kakao-ids`에 있는 카카오 `sub`이면서 `users`에
  가입된 계정만 `ROLE_ADMIN`(`AdminPrincipal`, `users.id` 포함)으로 인증합니다. `admin.kakao.client-id`가 비어 있으면 `/admin/**`
  전체를 거부합니다. 앱 API 체인(`SecurityConfig`, `@Order(2)`)은 그대로 JWT·무상태이고, `LoggingFilter`는 `/admin` 본문을 읽지 않습니다.
- **`@CurrentUser`** — 시큐리티 컨텍스트에서 `UserPrincipalInfo` 주입.
- **`@RequireLogin`** — 메서드 실행 전 인증 강제 (게스트 차단 등).
- **`ApiResponse<T>`** — 모든 응답의 공통 JSON 래퍼. 예외는 `CoreApiException(ErrorType)`으로 던지고
  `ApiControllerAdvice`가 처리.
- **포트 & 어댑터** — 외부 연동은 인터페이스 뒤에 둡니다 (`PushSender`, `MusicSearchGateway`, `DeveloperTokenProvider`).
  테스트에서는 인터페이스를 목킹.
- **로그 추적** — 모든 요청에 8자리 `traceId`가 MDC와 `X-Trace-Id` 응답 헤더로 부여되며, `AsyncConfig`의 `TaskDecorator`가
  `@Async` 스레드로 MDC를 전파합니다.

### 테스트

- JUnit 5 + Mockito(`@ExtendWith(MockitoExtension.class)`) 기반 단위 테스트 위주입니다 (
  `src/test/java/com/eeum/...`).
- 시간 의존 로직은 `LongSupplier` 등 시계를 주입하고 테스트에서 `FakeClock`을 사용합니다 (`TokenBucketRateLimiterTest` 참고).
- `afterCommit` 등 트랜잭션 동기화를 쓰는 서비스는 테스트에서 `TransactionSynchronizationManager` 초기화가 필요합니다 (
  `PostsServiceTest` 참고).

## 환경 설정

| 파일                      | 프로파일  | 용도                                                                    |
|-------------------------|-------|-----------------------------------------------------------------------|
| `application.yml`       | (공통)  | 활성 프로파일 지정 (`local`)                                                  |
| `application-local.yml` | local | 로컬 MySQL/Redis, Anthropic, Firebase, Apple Music, rate limit, Discord |
| `application-dev.yml`   | dev   | dev 서버                                                                |
| `application-prod.yml`  | prod  | 운영 서버                                                                 |

- **모든 `application*.yml`은 `.gitignore` 대상**이며 시크릿을 포함합니다. 내용을 커밋하거나 출력·공유하지 마세요. 새 설정 키를 추가하면 사용자에게
  dev/prod yml(GitHub Secrets `APPLICATION_DEV_YML` 등)에도 추가해야 한다고 알려주세요.
- 주요 커스텀 키: `admin.kakao.client-id`, `admin.kakao.client-secret`, `admin.allowed-kakao-ids`, `rate-limit.*`, `outbound-rate-limit.*`, `gateway.max-attempts`, `apple-music.*`,
  `firebase.credentials`, `app.redis-key-prefix`, `discord.*`.

## CI/CD & 인프라

- **`develop` 푸시** → `deploy-dev.yml`: GitHub Secret으로 `application-dev.yml` 생성 →
  `./gradlew clean build` → Docker 이미지(`eeum:dev`) 푸시 → self-hosted 러너에서 블루/그린 배포.
- **`main` 푸시** → `deploy-prod.yml`: 운영 서버 블루/그린 배포.
- **`infra/nginx/**` 변경이 `main`에 푸시** → `deploy-nginx.yml`: nginx 설정·배포 스크립트 자동 반영. 상세는
  `infra/nginx/README.md`.
- 블루/그린 포트 — dev: 8081/8083, prod: 8080/8082. `/actuator/health` 통과 시 nginx upstream 전환, 실패 시 자동 롤백.
- nginx IP 기반 rate limit — prod 10r/s, dev 30r/s (`infra/nginx/ratelimit.conf`).
- PR은 `develop` → `main` 흐름. 커밋 메시지는 `feat:`, `fix:`, `test:`, `chore:`, `ci:` 접두사 + 한국어 설명.
