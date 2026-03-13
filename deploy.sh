#!/bin/bash
# 单机 Ubuntu 生产环境部署脚本

set -euo pipefail

COMPOSE_FILE="docker-compose.prod.yml"
COMPOSE_CMD=(docker compose -f "$COMPOSE_FILE")

echo "======================================"
echo "地推渠道活码系统 - 生产环境部署"
echo "======================================"

if ! command -v docker >/dev/null 2>&1; then
    echo "错误: 未检测到 docker，请先安装 Docker Engine 与 Compose Plugin"
    exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
    echo "错误: 未检测到 docker compose，请先安装 Docker Compose Plugin"
    exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
    echo "错误: 未检测到 curl，请先安装 curl"
    exit 1
fi

# 检查.env文件
if [ ! -f .env ]; then
    echo "错误: .env 文件不存在!"
    echo "请复制 .env.example 为 .env 并配置环境变量"
    exit 1
fi

# 加载环境变量供脚本输出和 Compose 校验使用
set -a
. ./.env
set +a

echo ""
echo "1. 校验 Compose 配置..."
"${COMPOSE_CMD[@]}" config >/dev/null

echo ""
echo "2. 停止旧容器..."
"${COMPOSE_CMD[@]}" down || true

echo ""
echo "3. 构建并启动服务..."
"${COMPOSE_CMD[@]}" up -d --build --remove-orphans

echo ""
echo "4. 检查容器状态..."
"${COMPOSE_CMD[@]}" ps

echo ""
echo "5. 等待健康检查通过..."
for i in $(seq 1 30); do
    if curl -fsS http://127.0.0.1/api/health >/dev/null 2>&1; then
        break
    fi
    sleep 2
done

if ! curl -fsS http://127.0.0.1/api/health >/dev/null 2>&1; then
    echo "错误: 健康检查未通过，输出最近日志供排查"
    "${COMPOSE_CMD[@]}" logs --tail=100
    exit 1
fi

echo ""
echo "======================================"
echo "部署完成!"
echo ""
echo "访问地址:"
echo "  - 前端入口: http://localhost"
echo "  - 健康检查: http://localhost/api/health"
echo "  - MinIO 控制台(仅本机): http://127.0.0.1:9001"
echo ""
echo "查看日志: docker compose -f $COMPOSE_FILE logs -f"
echo "======================================"
