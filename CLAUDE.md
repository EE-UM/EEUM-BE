# CLAUDE.md

이 파일은 이 저장소에서 작업하는 Claude Code(claude.ai/code)에게 안내를 제공합니다.

## 프로젝트 개요

EEUM(이음)은 iOS 음악 공유 커뮤니티를 위한 Spring Boot 3.5 백엔드입니다. 사용자가 Apple Music 트랙과 함께 스토리를 올리면, 다른 사용자가 음악이 첨부된 댓글로 반응합니다. "흔들기(Shake)" 기능은 Redis O(1) 조회를 통해 랜덤 게시물을 반환합니다.

## 명령어

```bash
# 빌드
./gradlew build

# 실행 (local 프로파일 — 포트 8080)
./gradlew bootRun

# 전체 테스트 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.eeum.SomeTest"
```

Spring 프로파일은 `SPRING_PROFILES_ACTIVE` 환경 변수로 지정합니다. 프로파일: `local`, `dev`, `prod`.

## 기술 스택

- **Java 21 / Spring Boot 3.5**, Gradle 8
- **MySQL** (JPA/Hibernate, TSID 기본키)
- **Redis** — 랜덤 게시물 풀(Set), 조회수/좋아요 카운터, 분산 락(3초 TTL)
- **RabbitMQ (AMQP)** — 3개 exchange: `spamFilter`, `email`, `playlist_exchange`
- **Spring AI + OpenAI GPT-4o-mini** — 2단계 스팸 필터 (키워드 → AI)
- **Apple Music API** — 메타데이터 조회용 ES256 JWT (`AppleMusicKit`)
- **Pushy (APNS)** — 푸시 알림
- **Kakao & Apple OIDC + JWT (HS256)** — 무상태 인증
- **SpringDoc OpenAPI 2.3** — Swagger UI (`/swagger-ui.html`)

## 아키텍처

### 요청 흐름

```
iOS → Controller → Service → Repository → MySQL / Redis
                         ↘ RabbitMQ Producer → Consumer → (OpenAI / APNS / SMTP / Discord)
```

### 패키지 구조

```
com.eeum/
├── domain/
│   ├── posts/        # 게시물 CRUD, Apple Music 메타데이터, 흔들기(Redis 랜덤 풀)
│   ├── user/         # OAuth2 OIDC 로그인(카카오/애플), 게스트 로그인, JWT
│   ├── comment/      # 음악 첨부 댓글, RabbitMQ → 스팸 필터
│   ├── like/         # 좋아요 토글, Redis 분산 락으로 중복 방지
│   ├── view/         # Redis 조회수 카운터, MySQL 배치 백업
│   ├── notification/ # APNS 푸시, 이메일, 스팸 필터 컨슈머/프로듀서
│   ├── report/       # 신고 처리, Discord 웹훅 알림
│   └── common/
│       ├── spamfilter/       # ForbiddenWords 검사 + GPT-4o-mini 분류
│       └── webhook/discord/  # Discord 알림 메시지 포맷
└── global/
    ├── config/         # RabbitMQ, Redis, Spring AI, APNS, Swagger, 흔들기 워밍업
    ├── securitycore/   # SecurityConfig, JWTFilter, OIDC 프로바이더, @CurrentUser
    ├── aop/auth/       # @RequireLogin AOP 어드바이스
    ├── infrastructure/applemusickit/ # Apple Music용 ES256 JWT 생성
    ├── logger/         # HTTP 요청/응답 로깅 필터
    └── support/        # ApiResponse<T> 래퍼, ErrorCode enum, 전역 @ExceptionHandler
```

### 주요 패턴

- **TSID** — 모든 기본키에 시간 정렬 ID 사용 (auto-increment 미사용).
- **소프트 삭제** — 엔티티에 `isDeleted` 플래그를 유지하며, 쿼리에서 필터링.
- **분산 락** — `RedisLockRepository`(3초 TTL)로 좋아요·조회수 경쟁 상태 방지.
- **Redis 워밍업** — 서버 시작 시 `PostsRandomShakeWarmUp`이 활성 게시물 풀을 Redis에 로드. dev/prod 키 네임스페이스 분리.
- **Swagger 인터페이스 분리** — 각 도메인에 `docs/*Api.java` 인터페이스(`@Operation` 포함)가 있고 컨트롤러가 이를 구현.
- **`@CurrentUser`** — 시큐리티 컨텍스트에서 `UserPrincipalInfo`를 주입하는 커스텀 어노테이션.
- **`@RequireLogin`** — 메서드 실행 전 인증을 강제하는 AOP 어노테이션.
- **`ApiResponse<T>`** — 모든 응답에 사용하는 공통 JSON 래퍼.
- **RabbitMQ DLQ** — 3개 큐 모두 데드 레터 exchange 설정 포함.

### 환경 설정

| 파일 | 프로파일 | 용도 |
|------|---------|------|
| `application.yml` | (공통) | 공유 기본값, 활성 프로파일 지정 |
| `application-local.yml` | local | 로컬 MySQL/Redis/RabbitMQ |
| `application-dev.yml` | dev | AWS RDS/ElastiCache (포트 8081) |
| `application-prod.yml` | prod | AWS 운영 환경 (포트 8080) |

`dev`/`prod` 환경의 시크릿은 시작 시 AWS Parameter Store에서 가져옵니다.

### CI/CD

- **`develop` 브랜치** → `deploy-dev.yml` → Docker 이미지 빌드 → dev 서버 배포 (블루/그린).
- **`main` 브랜치** → `deploy-prod.yml` → 운영 서버 배포.
