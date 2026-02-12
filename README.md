# EEUM (이음) - 백엔드 서비스

EEUM은 사용자들이 소중한 이야기를 나누고 연결될 수 있도록 돕는 커뮤니티 플랫폼의 백엔드 서비스입니다. 게시글 관리, AI 기반의 스마트 스팸 필터링, 그리고 사용자 경험을 극대화하는 랜덤 추천 시스템을 제공합니다.

---

### 🛠 기술 스택

- **Framework**: Spring Boot 3.5.0
- **Language**: Java 21
- **Database**: MySQL (JPA/Hibernate)
- **Caching & Real-time**: Redis
- **Message Broker**: RabbitMQ
- **AI Integration**: Spring AI (OpenAI GPT-4o-mini)
- **Push Notification**: APNS (Pushy)
- **Communication**: Discord Webhook (Monitoring & Alerts)
- **Documentation**: Swagger (SpringDoc OpenAPI)
- **Infrastructure**: Docker, AWS (RDS, ElastiCache, S3, Parameter Store)

---

### 🚀 주요 기능

#### 1. 스마트 게시글 관리 및 스팸 필터링
- **AI 스팸 필터링**: Spring AI와 OpenAI(GPT-4o-mini)를 활용하여 게시글 내의 부적절한 콘텐츠(비속어, 혐오 표현, 광고 등)를 자동으로 감별합니다.
- **다단계 필터링 시스템**:
    - **금칙어 필터**: `OpenKoreanText` 라이브러리를 이용해 형태소 분석 후 사전에 정의된 금칙어 포함 여부를 확인합니다.
    - **AI 심화 필터**: 문맥을 파악하여 스팸 여부를 정밀하게 판단합니다.
- **비동기 처리**: RabbitMQ를 도입하여 스팸 필터링 작업을 비동기로 처리함으로써 게시글 생성 시 응답 속도를 최적화했습니다.
- **자동 조치 및 알림**: 스팸 점수에 따라 게시글 자동 삭제 및 운영팀 Discord 채널로 실시간 알림을 전송합니다.

#### 2. 실시간 랜덤 게시글 추천 (Shake & Connect)
- **Redis 기반 랜덤 매칭**: 사용자가 기기를 흔드는 등의 액션을 취했을 때, Redis의 `Set` 자료구조와 `randomMember` 기능을 활용하여 지연 시간 없이 무작위 게시글을 추천합니다.
- **Warm-Up 시스템**: 애플리케이션 시작 시 활성 게시글 데이터를 Redis에 미리 로드하여 서비스 초기 응답성을 확보합니다.

#### 3. 사용자 및 소통 기능
- **인증/인가**: OAuth2(카카오, 애플) 및 JWT를 통한 안전한 로그인을 제공합니다.
- **댓글 및 좋아요**: 게시글에 대한 사용자 간의 소통을 지원합니다.
- **푸시 알림**: APNS를 통해 주요 이벤트에 대한 알림을 실시간으로 제공합니다.
- **이미지 업로드**: AWS S3를 연동하여 안정적인 미디어 파일 관리를 지원합니다.

#### 4. 운영 및 모니터링
- **Discord 웹훅 연동**: 시스템 에러, 스팸 감지, 리포트 발생 시 Discord 채널로 즉시 알림을 전송하여 빠른 대응이 가능하게 설계되었습니다.
- **Spring Actuator**: 서비스 상태 모니터링 및 메트릭 수집을 지원합니다.

---

### 🏗 시스템 아키텍처 (주요 흐름)

1. **Post Creation**: 사용자가 게시글을 작성하면 DB에 저장 후 RabbitMQ로 스팸 필터링 이벤트를 발행합니다.
2. **Asynchronous Filtering**: `SpamFilterConsumer`가 이벤트를 수신하여 AI 필터링을 수행합니다.
3. **Action & Alert**: 결과에 따라 게시글 상태를 업데이트하고, 필요 시 Discord 웹훅을 통해 관리자에게 알립니다.
4. **Random Discovery**: Redis에서 최신 활성 게시글 정보를 캐싱하여 초고속 랜덤 탐색 기능을 제공합니다.

---

### 📦 프로젝트 구조 (주요 도메인)

- `com.eeum.domain.user`: 회원 가입, 프로필, OAuth 인증 관리
- `com.eeum.domain.posts`: 게시글 CRUD 및 랜덤 추천 로직
- `com.eeum.domain.comment`: 댓글 및 대댓글 관리
- `com.eeum.domain.like`: 게시글/댓글 좋아요 처리
- `com.eeum.domain.notification`: 푸시 알림 및 메시지 발행/소비
- `com.eeum.domain.common.spamfilter`: AI 및 단어 기반 스팸 필터링 로직
- `com.eeum.domain.common.webhook`: 외부 알림 서비스 연동
- `com.eeum.global`: 전역 설정(Security, Redis, RabbitMQ, Swagger 등) 및 공통 예외 처리
