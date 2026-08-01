#!/bin/bash
set -e

ENV=$1
IMAGE=$2

if [ "$ENV" == "prod" ]; then
    BLUE_PORT=8080
    GREEN_PORT=8082
    PROFILE="prod"
    UPSTREAM_FILE="/etc/nginx/conf.d/upstream-prod.conf"
    UPSTREAM_NAME="prod_backend"
    LOG_OPTS="--log-driver=awslogs --log-opt awslogs-group=/eeum/prod --log-opt awslogs-region=ap-northeast-2"
elif [ "$ENV" == "dev" ]; then
    BLUE_PORT=8081
    GREEN_PORT=8083
    PROFILE="dev"
    UPSTREAM_FILE="/etc/nginx/conf.d/upstream-dev.conf"
    UPSTREAM_NAME="dev_backend"
    LOG_OPTS=""
else
    echo "Usage: $0 <prod|dev> <docker-image>"
    exit 1
fi

STATE_FILE="/home/ubuntu/blue-green/current-${ENV}"
CURRENT=$(cat "$STATE_FILE" 2>/dev/null || echo "blue")

if [ "$CURRENT" == "blue" ]; then
    NEW_ENV="green"
    NEW_PORT=$GREEN_PORT
    OLD_PORT=$BLUE_PORT
else
    NEW_ENV="blue"
    NEW_PORT=$BLUE_PORT
    OLD_PORT=$GREEN_PORT
fi

CONTAINER_NAME="eeum-${ENV}-${NEW_ENV}"
OLD_CONTAINER="eeum-${ENV}-${CURRENT}"

echo "=========================================="
echo " Blue-Green Deploy: ${ENV}"
echo " ${CURRENT}(:${OLD_PORT}) -> ${NEW_ENV}(:${NEW_PORT})"
echo "=========================================="

echo "[1/5] Starting ${NEW_ENV} container on port ${NEW_PORT}..."
docker pull ${IMAGE}
docker stop ${CONTAINER_NAME} 2>/dev/null || true
docker rm ${CONTAINER_NAME} 2>/dev/null || true
docker run -d \
    --name ${CONTAINER_NAME} \
    -p ${NEW_PORT}:8080 \
    -e SPRING_PROFILES_ACTIVE=${PROFILE} \
    --restart unless-stopped \
    ${LOG_OPTS} \
    ${IMAGE}

echo "[2/5] Health check on port ${NEW_PORT}..."
MAX_RETRIES=30
RETRY=0
while [ $RETRY -lt $MAX_RETRIES ]; do
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${NEW_PORT}/actuator/health" 2>/dev/null || echo "000")
    if [ "$HTTP_STATUS" == "200" ]; then
        echo "Health check passed!"
        break
    fi
    RETRY=$((RETRY + 1))
    echo "  Retry ${RETRY}/${MAX_RETRIES}... (status: ${HTTP_STATUS})"
    sleep 2
done

if [ $RETRY -eq $MAX_RETRIES ]; then
    echo "ERROR: Health check failed! Rolling back..."
    docker stop ${CONTAINER_NAME} 2>/dev/null || true
    docker rm ${CONTAINER_NAME} 2>/dev/null || true
    echo "Rollback complete. ${CURRENT} is still active."
    exit 1
fi

echo "[3/5] Switching Nginx upstream to port ${NEW_PORT}..."
cat > "$UPSTREAM_FILE" << EOF
upstream ${UPSTREAM_NAME} {
    server 127.0.0.1:${NEW_PORT};
}
EOF

echo "[4/5] Reloading Nginx..."
sudo nginx -t && sudo systemctl reload nginx

echo "[5/5] Stopping old container (${OLD_CONTAINER})..."
docker stop ${OLD_CONTAINER} 2>/dev/null || true
docker rm ${OLD_CONTAINER} 2>/dev/null || true
echo "$NEW_ENV" > "$STATE_FILE"

echo "=========================================="
echo " Deploy complete!"
echo " Active: ${NEW_ENV} (port ${NEW_PORT})"
echo "=========================================="
