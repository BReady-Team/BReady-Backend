set -euo pipefail

APP_DIR="/home/ubuntu/bready"
ACTIVE_FILE="$APP_DIR/active-color"
NGINX_CONF="/etc/nginx/sites-available/bready"
HEALTH_ENDPOINT="/actuator/health"

BLUE_PORT=8081
GREEN_PORT=8082

BLUE_COMPOSE="docker-compose-blue.yml"
GREEN_COMPOSE="docker-compose-green.yml"

cd "$APP_DIR"

if [ ! -f "$ACTIVE_FILE" ]; then
  echo "blue" > "$ACTIVE_FILE"
fi

CURRENT_COLOR=$(cat "$ACTIVE_FILE")

if [ "$CURRENT_COLOR" = "blue" ]; then
  TARGET_COLOR="green"
  TARGET_PORT=$GREEN_PORT
  TARGET_COMPOSE=$GREEN_COMPOSE
  OLD_COLOR="blue"
  OLD_COMPOSE=$BLUE_COMPOSE
else
  TARGET_COLOR="blue"
  TARGET_PORT=$BLUE_PORT
  TARGET_COMPOSE=$BLUE_COMPOSE
  OLD_COLOR="green"
  OLD_COMPOSE=$GREEN_COMPOSE
fi

echo "=============================="
echo "[INFO] CURRENT_COLOR = $CURRENT_COLOR"
echo "[INFO] TARGET_COLOR  = $TARGET_COLOR"
echo "[INFO] TARGET_PORT   = $TARGET_PORT"
echo "=============================="

echo "[STEP 1] Pull target image"
docker compose -p "bready-${TARGET_COLOR}" -f "$TARGET_COMPOSE" --env-file .env pull

echo "[STEP 2] Start target container"
docker compose -p "bready-${TARGET_COLOR}" -f "$TARGET_COMPOSE" --env-file .env up -d

echo "[STEP 3] Wait until target app is healthy"
for i in $(seq 1 30); do
  HEALTH_RESPONSE=$(curl -s "http://127.0.0.1:${TARGET_PORT}${HEALTH_ENDPOINT}" || true)

  if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
    echo "[INFO] Health check passed"
    break
  fi

  if [ "$i" -eq 30 ]; then
    echo "[ERROR] Health check failed after maximum retries"
    echo "[INFO] Target container logs:"
    docker compose -p "bready-${TARGET_COLOR}" -f "$TARGET_COMPOSE" --env-file .env logs --tail=100
    exit 1
  fi

  echo "[WAIT] attempt=$i, app not ready yet"
  sleep 10
done

echo "[STEP 4] Switch nginx upstream to ${TARGET_PORT}"
sudo sed -i "s/server 127.0.0.1:[0-9]\+;/server 127.0.0.1:${TARGET_PORT};/" "$NGINX_CONF"

echo "[STEP 5] Validate nginx config"
sudo nginx -t

echo "[STEP 6] Reload nginx"
sudo systemctl reload nginx

echo "$TARGET_COLOR" > "$ACTIVE_FILE"
echo "[INFO] Active color changed to $TARGET_COLOR"

echo "[STEP 7] Stop old container ($OLD_COLOR)"
docker compose -p "bready-${OLD_COLOR}" -f "$OLD_COMPOSE" --env-file .env stop || true
docker compose -p "bready-${OLD_COLOR}" -f "$OLD_COMPOSE" --env-file .env rm -f || true

echo "[STEP 8] Prune dangling images"
docker image prune -f

echo "[SUCCESS] Blue-Green deployment completed successfully"