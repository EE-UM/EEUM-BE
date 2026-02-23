# EEUM (이음)

> 음악으로 이야기를 잇다 — 사용자들이 음악과 함께 소중한 이야기를 나누고, 우연한 연결을 경험할 수 있는 커뮤니티 플랫폼

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Language / Framework** | Java 21, Spring Boot 3.5.0 |
| **Database** | MySQL (JPA/Hibernate), Redis |
| **Messaging** | RabbitMQ (비동기 이벤트 처리) |
| **AI** | Spring AI + OpenAI GPT-4o-mini, OpenKoreanText |
| **Authentication** | OAuth2 (Kakao, Apple OIDC) + JWT (HS256) |
| **Push Notification** | APNS (Pushy) |
| **Music** | Apple Music API (ES256 JWT 토큰) |
| **Monitoring** | Discord Webhook, Spring Actuator |
| **Infrastructure** | Docker, AWS (RDS, ElastiCache, S3, Parameter Store) |
| **Documentation** | SpringDoc OpenAPI (Swagger UI) |

---

## 주요 기능

### Shake & Connect — 랜덤 이야기 발견
기기를 흔들면 다른 사용자의 이야기를 우연히 만나볼 수 있는 핵심 기능입니다.

- Redis `Set` + `randomMember()`를 활용한 **O(1) 랜덤 추천**
- 애플리케이션 시작 시 활성 게시글을 Redis에 미리 로드하는 **Warm-Up 시스템**
- dev/prod 환경 간 Redis 키 네임스페이스 분리

### AI 기반 다단계 스팸 필터링
게시글 작성 시 비동기로 콘텐츠를 검증하여 건전한 커뮤니티를 유지합니다.

- **1단계 — 금칙어 필터**: OpenKoreanText 형태소 분석 후 금칙어 사전 매칭
- **2단계 — AI 심화 필터**: GPT-4o-mini가 문맥을 파악하여 스팸 여부 판단
- **비동기 처리**: RabbitMQ를 통해 게시글 생성 응답 속도에 영향 없이 처리
- **자동 조치**: 스팸 점수에 따라 경고(Discord 알림) 또는 자동 삭제

### 음악과 함께하는 이야기
Apple Music API와 연동하여 게시글에 음악 메타데이터를 첨부할 수 있습니다.

- 앨범명, 곡명, 아티스트, 아트워크, Apple Music 링크 포함
- 분기별 배치 작업으로 API 토큰 자동 갱신 (6개월 유효)

### 사용자 인증 및 소통
- **OAuth2 로그인**: 카카오, 애플 OIDC 기반 인증 + 게스트 로그인 지원
- **JWT 인증**: 무상태(Stateless) API 인증
- **댓글 & 좋아요**: 이야기에 대한 소통 기능
- **푸시 알림**: APNS를 통한 실시간 알림
- **신고 시스템**: 부적절한 콘텐츠 신고 → 이메일 + Discord 알림

### 운영 모니터링
- **Discord Webhook**: 스팸 감지, 신고, 시스템 에러 발생 시 채널별 실시간 알림
- **Spring Actuator**: 서비스 헬스체크 및 메트릭 수집

---

## 시스템 아키텍처

```
Client (iOS)
    │
    ▼
┌─────────────────────────────────────────────┐
│              Spring Boot API                 │
│  ┌─────────┐  ┌──────────┐  ┌────────────┐  │
│  │  Posts   │  │  Comment  │  │   Like     │  │
│  │ Service  │  │  Service  │  │  Service   │  │
│  └────┬─────┘  └────┬─────┘  └─────┬──────┘  │
│       │              │              │          │
│  ┌────▼──────────────▼──────────────▼──────┐  │
│  │              MySQL (JPA)                 │  │
│  └─────────────────────────────────────────┘  │
│       │                                       │
│  ┌────▼─────────┐    ┌──────────────────┐     │
│  │    Redis      │    │    RabbitMQ      │     │
│  │ • 랜덤 추천   │    │ • 스팸 필터링    │     │
│  │ • 조회수 카운트│    │ • 이메일 알림    │     │
│  │ • 분산 락     │    │ • 푸시 알림      │     │
│  └──────────────┘    └───────┬──────────┘     │
│                              │                │
│                     ┌────────▼─────────┐      │
│                     │    Consumers      │      │
│                     │ • SpamFilter      │      │
│                     │ • ReportMail      │      │
│                     │ • PostsCompleted  │      │
│                     └──────────────────┘      │
└─────────────────────────────────────────────┘
         │                    │
    ┌────▼────┐         ┌────▼────┐
    │  APNS   │         │ Discord │
    │  (Push) │         │ Webhook │
    └─────────┘         └─────────┘
```

---

## 프로젝트 구조

```
com.eeum
├── domain
│   ├── user/             # OAuth2 인증, 프로필 관리, 게스트 로그인
│   ├── posts/            # 게시글 CRUD, 랜덤 추천, Apple Music 연동
│   ├── comment/          # 댓글 관리, RabbitMQ 알림 발행
│   ├── like/             # 좋아요 처리, Redis 분산 락 기반 카운팅
│   ├── notification/     # APNS 푸시, 이메일, RabbitMQ Consumer/Producer
│   ├── report/           # 게시글/댓글 신고, Discord + 이메일 알림
│   ├── view/             # Redis 조회수 카운트, DB 배치 백업
│   └── common
│       ├── spamfilter/   # AI + 형태소 분석 스팸 필터링
│       ├── webhook/      # Discord 웹훅 연동
│       └── ai/           # Spring AI ChatClient 설정
└── global
    ├── config/           # Redis, RabbitMQ, APNS, Swagger, Warm-Up 설정
    ├── securitycore/     # JWT, OIDC Provider, Security Filter Chain
    ├── infrastructure/   # Apple Music Kit (ES256 토큰 생성)
    ├── aop/              # @RequireLogin 인증 AOP
    ├── logger/           # HTTP 요청/응답 로깅
    └── support/          # 공통 ApiResponse, 에러 핸들링
```

---

## API 엔드포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/user/login` | OAuth2 로그인 |
| `POST` | `/user/guest` | 게스트 로그인 |
| `GET/PATCH` | `/user/profile` | 프로필 조회/수정 |
| `POST` | `/posts` | 게시글 작성 |
| `GET` | `/posts/random` | 랜덤 이야기 추천 (Shake) |
| `GET` | `/posts/ing/infinite-scroll` | 진행 중 게시글 무한 스크롤 |
| `GET` | `/posts/done/infinite-scroll` | 완료된 게시글 무한 스크롤 |
| `PATCH` | `/posts/{postId}/complete` | 게시글 완료 처리 |
| `GET/POST/DELETE` | `/comments/*` | 댓글 CRUD |
| `POST/DELETE` | `/like/posts/{postId}` | 좋아요 토글 |
| `POST` | `/report/posts` | 게시글 신고 |
| `POST` | `/report/comment` | 댓글 신고 |
| `GET` | `/apple-music/search` | 음악 검색 |

> Swagger UI에서 전체 API 명세를 확인할 수 있습니다.

---

## 실행 방법

### 사전 요구사항
- Java 21
- MySQL 8.x
- Redis
- RabbitMQ

### 로컬 실행

```bash
# 빌드
./gradlew build

# 실행 (local 프로필)
./gradlew bootRun
```

### Docker 실행

```bash
# 개발 환경
docker-compose -f docker-compose-dev.yml up -d

# 운영 환경
docker-compose -f docker-compose-prod.yml up -d
```

### 환경별 설정

| 환경 | 프로필 | 포트 | Docker 이미지 |
|------|--------|------|---------------|
| Local | `local` | 8080 | - |
| Dev | `dev` | 8081 | `rookie97/eeum:dev` |
| Prod | `prod` | 8080 | `rookie97/eeum:prod` |

---

## 메시지 큐 구조

| Exchange | Queue | 용도 |
|----------|-------|------|
| `spamFilter.exchange` | `spamFilter.queue` | 게시글 스팸 필터링 |
| `email.exchange` | `email.queue` | 신고 이메일 발송 |
| `playlist_exchange` | `playlist_completed_queue` | 플레이리스트 완료 알림 |

모든 큐는 Dead Letter Queue(DLQ)를 갖추고 있으며, 최대 3회 재시도 후 DLQ로 이동합니다.

---

## 주요 설계 결정

- **TSID**: 분산 환경에서도 충돌 없는 시간 정렬 가능한 ID 생성
- **Soft Delete**: `isDeleted` 플래그를 통한 논리적 삭제로 데이터 이력 보존
- **이벤트 드리븐**: RabbitMQ 기반 비동기 처리로 서비스 간 결합도 최소화
- **Redis 분산 락**: 조회수 카운팅 시 동시성 제어 (3초 TTL)
- **조회수 배치 백업**: Redis에서 실시간 카운팅, 10회마다 DB에 영구 저장
