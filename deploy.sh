#!/bin/bash

set -euo pipefail

COMPOSE_FILE="docker-compose.prod.yml"
COMPOSE_CMD=(docker compose -f "$COMPOSE_FILE")
BACKEND_PORT="${BACKEND_PORT:-8080}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

source "$SCRIPT_DIR/deploy/env-secrets.sh"

echo "======================================"
echo "QR code channel system - backend deploy"
echo "======================================"

if ! command -v docker >/dev/null 2>&1; then
    echo "ERROR: docker is not installed."
    exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
    echo "ERROR: docker compose plugin is not installed."
    exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
    echo "ERROR: curl is not installed."
    exit 1
fi

if [ ! -f .env ]; then
    echo "ERROR: .env does not exist."
    echo "Copy .env.example to .env and fill in production values first."
    exit 1
fi

set -a
. ./.env
set +a

GENERATED_AUTH_TOKEN_SECRET="$(ensure_env_secret .env AUTH_TOKEN_SECRET 48)"
GENERATED_ADMIN_INITIAL_PASSWORD="$(ensure_env_secret .env ADMIN_INITIAL_PASSWORD 24)"

if [ -n "$GENERATED_AUTH_TOKEN_SECRET" ] || [ -n "$GENERATED_ADMIN_INITIAL_PASSWORD" ]; then
    set -a
    . ./.env
    set +a
fi

BACKEND_PORT="${BACKEND_PORT:-8080}"
DB_HOST="${DB_HOST:-mysql}"

if [ "${#AUTH_TOKEN_SECRET}" -lt 32 ]; then
    echo "ERROR: AUTH_TOKEN_SECRET must be at least 32 characters."
    exit 1
fi

if [ -n "$GENERATED_AUTH_TOKEN_SECRET" ]; then
    echo "Generated AUTH_TOKEN_SECRET and wrote it to .env."
fi

if [ -n "$GENERATED_ADMIN_INITIAL_PASSWORD" ]; then
    echo "Generated ADMIN_INITIAL_PASSWORD and wrote it to .env."
    echo "Initial admin username: ${ADMIN_USERNAME:-admin}"
    echo "Initial admin password was stored in .env and will not be printed to deployment logs."
    echo "Use the .env value for the first login, then change it in the system."
fi

echo ""
echo "1. Validate compose config..."
"${COMPOSE_CMD[@]}" config >/dev/null

if [ "$DB_HOST" = "mysql" ]; then
    echo ""
    echo "2. Start bundled MySQL..."
    "${COMPOSE_CMD[@]}" up -d mysql

    echo ""
    echo "3. Wait for MySQL health check..."
    MYSQL_CONTAINER="$("${COMPOSE_CMD[@]}" ps -q mysql)"
    for _ in $(seq 1 30); do
        MYSQL_STATUS="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$MYSQL_CONTAINER" 2>/dev/null || true)"
        if [ "$MYSQL_STATUS" = "healthy" ] || [ "$MYSQL_STATUS" = "running" ]; then
            break
        fi
        sleep 2
    done

    if [ "$MYSQL_STATUS" != "healthy" ] && [ "$MYSQL_STATUS" != "running" ]; then
        echo "ERROR: bundled MySQL did not become healthy. Current status: ${MYSQL_STATUS:-unknown}"
        "${COMPOSE_CMD[@]}" logs --tail=100 mysql
        exit 1
    fi
else
    echo ""
    echo "2. Skip bundled MySQL because DB_HOST=$DB_HOST."
fi

echo ""
echo "4. Build and start backend..."
"${COMPOSE_CMD[@]}" up -d --build backend

echo ""
echo "5. Check backend health on localhost:${BACKEND_PORT}..."
for _ in $(seq 1 30); do
    if curl -fsS "http://127.0.0.1:${BACKEND_PORT}/api/health" >/dev/null 2>&1; then
        break
    fi
    sleep 2
done

if ! curl -fsS "http://127.0.0.1:${BACKEND_PORT}/api/health" >/dev/null 2>&1; then
    echo "ERROR: backend health check failed. Recent logs:"
    "${COMPOSE_CMD[@]}" logs --tail=100 backend
    exit 1
fi

echo ""
echo "======================================"
echo "Backend deploy complete."
echo ""
echo "Company Nginx should serve frontend/dist and proxy /api/ to:"
echo "  http://127.0.0.1:${BACKEND_PORT}"
echo ""
echo "Reference config:"
echo "  deploy/nginx/qrcode-channel.conf"
echo ""
echo "Health check:"
echo "  http://127.0.0.1:${BACKEND_PORT}/api/health"
echo ""
echo "Logs:"
echo "  docker compose -f $COMPOSE_FILE logs -f backend"
echo "======================================"
