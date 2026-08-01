# infra/nginx

운영 서버(`ip-172-31-35-133`)의 blue-green 배포 + nginx 설정을 버전 관리하기 위한 미러입니다.
서버가 실제 진실 소스(source of truth)이며, 이 디렉토리는 변경 이력을 남기고 리뷰를 받기 위한 용도입니다.
`main` 브랜치에서 이 디렉토리(`infra/nginx/**`)가 변경되면 `.github/workflows/deploy-nginx.yml`이 **자동으로 서버에 반영**합니다 (self-hosted 러너가 서버 위에서 직접 실행되므로 SSH 키 없이 `sudo cp` + `nginx -t && systemctl reload nginx`로 동작). `develop` 브랜치 푸시나 `upstream-*.conf.seed` 변경은 이 워크플로우를 트리거하지 않습니다 — 아래 "자동 반영 대상" 참고.

## 파일 ↔ 서버 경로 매핑

| 리포 파일 | 서버 경로 | `deploy-nginx.yml` 자동 반영 대상? |
|---|---|---|
| `deploy-blue-green.sh` | `/home/ubuntu/scripts/deploy-blue-green.sh` | ✅ (`deploy-dev.yml`, `deploy-prod.yml`가 이 경로를 직접 호출) |
| `eeum.conf` | `/etc/nginx/sites-enabled/eeum` | ✅ **심볼릭 링크가 아니라 실제 파일.** `sites-available/`에는 두지 않음 |
| `ratelimit.conf` | `/etc/nginx/conf.d/ratelimit.conf` | ✅ rate limit zone 정의 |
| `upstream-dev.conf.seed` | `/etc/nginx/conf.d/upstream-dev.conf` | ❌ **의도적으로 제외.** 배포 스크립트가 매 배포마다 덮어쓰는 런타임 상태라, 워크플로우가 이걸 seed로 덮어쓰면 현재 활성 포트가 아닌 포트로 되돌아가 서비스가 끊길 수 있음. 최초 1회 시딩 용도로만 사용 |
| `upstream-prod.conf.seed` | `/etc/nginx/conf.d/upstream-prod.conf` | ❌ 위와 동일 |
| (없음, 디렉토리만) | `/home/ubuntu/blue-green/current-dev`, `current-prod` | ❌ 현재 활성 색상(`blue`/`green`) 런타임 상태. 리포에 스냅샷을 커밋하지 않음(매 배포마다 값이 바뀌어 즉시 stale해짐) — 대신 최초 세팅 시 디렉토리/파일 존재만 보장 |

## Blue-Green 동작 방식

- dev: blue=`8081`, green=`8083` / prod: blue=`8080`, green=`8082` (컨테이너는 내부적으로 8080 리스닝, 호스트 포트만 다름)
- 현재 활성 색상은 `/home/ubuntu/blue-green/current-{dev,prod}` 파일에 `blue`/`green`으로 기록됨
- 배포 시 반대 색상 컨테이너를 새로 띄우고 `/actuator/health` 200 응답을 최대 60초(30회 × 2초) 재시도 확인
- 헬스체크 통과 시 `upstream-{env}.conf`를 새 포트로 덮어쓰고 `nginx -t && systemctl reload nginx`
- 이전 색상 컨테이너를 정지/삭제하고 상태 파일을 갱신
- 헬스체크 실패 시 새 컨테이너를 정리하고 기존 활성 컨테이너를 그대로 유지 (자동 롤백)

`eeum.conf`의 `proxy_pass`는 하드코딩된 포트가 아니라 `http://dev_backend/`, `http://prod_backend/` (upstream 이름)를 참조하므로, 실제 포트 전환은 전적으로 `upstream-*.conf` 갱신에 의해 이루어집니다.

## 변경 반영 절차 (평상시)

`eeum.conf`, `ratelimit.conf`, `deploy-blue-green.sh`를 고친 뒤 `main`에 머지되면 `.github/workflows/deploy-nginx.yml`이 자동으로 반영합니다. 별도 SSH 작업이 필요 없습니다. 워크플로우 단계:

1. 러너(=서버) 위에서 `actions/checkout`
2. `/etc/nginx` 전체를 `/home/ubuntu/nginx-backups/nginx-backup-<timestamp>.tar.gz`로 백업
3. `eeum.conf`, `ratelimit.conf`, `deploy-blue-green.sh`를 대상 경로로 `sudo cp` (+ 스크립트 실행 권한 부여)
4. `sudo nginx -t && sudo systemctl reload nginx`

`nginx -t`가 실패하면 워크플로우가 여기서 중단되고 reload가 일어나지 않으므로, 직전 백업으로 롤백하려면 `sudo tar -xzf /home/ubuntu/nginx-backups/nginx-backup-<timestamp>.tar.gz -C /`로 복원 후 reload하면 됩니다.

`develop` 브랜치에 대한 push나 `upstream-*.conf.seed` 변경은 트리거되지 않습니다. 수동으로 즉시 반영하고 싶다면 Actions 탭에서 `Nginx Config Deploy` 워크플로우를 `workflow_dispatch`로 직접 실행하세요.

## 새 서버 최초 세팅 (워크플로우가 아직 없을 때만)

```bash
# 1) 파일 복사
scp infra/nginx/deploy-blue-green.sh ubuntu@<host>:/home/ubuntu/scripts/deploy-blue-green.sh
scp infra/nginx/eeum.conf            ubuntu@<host>:/etc/nginx/sites-enabled/eeum
scp infra/nginx/ratelimit.conf       ubuntu@<host>:/etc/nginx/conf.d/ratelimit.conf
scp infra/nginx/upstream-dev.conf.seed  ubuntu@<host>:/etc/nginx/conf.d/upstream-dev.conf
scp infra/nginx/upstream-prod.conf.seed ubuntu@<host>:/etc/nginx/conf.d/upstream-prod.conf

# 2) 문법 체크 후 반영
ssh ubuntu@<host> 'sudo nginx -t && sudo systemctl reload nginx'
ssh ubuntu@<host> 'chmod +x /home/ubuntu/scripts/deploy-blue-green.sh'

# 3) 상태 디렉토리 생성
#    (없으면 첫 배포 마지막 단계 `echo "$NEW_ENV" > "$STATE_FILE"`이
#    "No such file or directory"로 실패 — 이미 컨테이너 전환/nginx reload는 끝난 뒤라 배포 자체는 성공한 것처럼 보이지만
#    상태 파일 갱신이 누락되어 다음 배포부터 blue/green 판단이 어긋남)
ssh ubuntu@<host> 'mkdir -p /home/ubuntu/blue-green && echo blue > /home/ubuntu/blue-green/current-dev && echo blue > /home/ubuntu/blue-green/current-prod'
```

`*.seed` 파일은 이 최초 세팅 시에만 쓰고, 이후에는 `deploy-blue-green.sh`가 매 배포마다 실제 활성 포트로 덮어쓰므로 손대지 마세요.

## 알려진 이슈 / 정리 필요 항목

- `/etc/nginx/sites-available/eeum`, `/etc/nginx/sites-available/eeum.save`는 실제로 로드되지 않는 죽은 복사본입니다 (`sites-enabled/eeum`이 심볼릭 링크가 아니라 별도 파일이기 때문). 혼동 방지를 위해 서버에서 삭제 권장:
  ```bash
  sudo nginx -t   # 먼저 현재 활성 설정에 이상 없는지 확인
  sudo rm /etc/nginx/sites-available/eeum
  sudo rm /etc/nginx/sites-available/eeum.save
  ```
