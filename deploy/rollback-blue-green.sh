set -euo pipefail

APP_DIR="/home/ubuntu/bready"
ACTIVE_FILE="$APP_DIR/active-color"
NGINX_CONF="/etc/nginx/sites-available/bready"

BLUE_PORT=8081
GREEN_PORT=8082

cd "$APP_DIR"

if [ ! -f "$ACTIVE_FILE" ]; then
  echo "[ERROR] active-color file does not exist"
  exit 1
fi

CURRENT_COLOR=$(cat "$ACTIVE_FILE")

if [ "$CURRENT_COLOR" = "blue" ]; then
  ROLLBACK_COLOR="green"
  ROLLBACK_PORT=$GREEN_PORT
else
  ROLLBACK_COLOR="blue"
  ROLLBACK_PORT=$BLUE_PORT
fi

echo "[INFO] current color   = $CURRENT_COLOR"
echo "[INFO] rollback color  = $ROLLBACK_COLOR"
echo "[INFO] rollback port   = $ROLLBACK_PORT"

sudo sed -i "s/server 127.0.0.1:[0-9]\+;/server 127.0.0.1:${ROLLBACK_PORT};/" "$NGINX_CONF"

sudo nginx -t
sudo systemctl reload nginx

echo "$ROLLBACK_COLOR" > "$ACTIVE_FILE"

echo "[SUCCESS] Rolled back traffic to $ROLLBACK_COLOR"
