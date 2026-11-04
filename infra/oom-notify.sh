#!/bin/sh

# JVM -XX:OnOutOfMemoryError 로 호출됨 (힙 덤프 저장 직후, JVM 종료 직전)
# 이 스크립트가 실패해도 JVM 종료/재시작에는 영향 없도록 항상 exit 0

[ -z "$OOM_DISCORD_WEBHOOK_URL" ] && exit 0

NOW=$(TZ=Asia/Seoul date '+%Y-%m-%d %H:%M:%S KST')

MSG="🚨**[${SPRING_PROFILES_ACTIVE}] OOM 발생**\n컨테이너: ${CONTAINER_NAME}\n시각: ${NOW}\n힙 덤프 저장 후 재시작합니다 (/home/ubuntu/heapdump/${SPRING_PROFILES_ACTIVE})"

curl -s -m 5 -o /dev/null \
    -H "Content-Type: application/json" \
    -d "{\"content\":\"${MSG}\"}" \
    "$OOM_DISCORD_WEBHOOK_URL"

exit 0