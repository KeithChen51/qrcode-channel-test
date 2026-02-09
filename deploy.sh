#!/bin/bash
# 生产环境部署脚本

set -e

echo "======================================"
echo "地推渠道活码系统 - 生产环境部署"
echo "======================================"

# 检查.env文件
if [ ! -f .env ]; then
    echo "错误: .env 文件不存在!"
    echo "请复制 .env.example 为 .env 并配置环境变量"
    exit 1
fi

# 加载环境变量
source .env

echo ""
echo "1. 停止旧容器..."
docker-compose -f docker-compose.prod.yml down || true

echo ""
echo "2. 构建镜像..."
docker-compose -f docker-compose.prod.yml build --no-cache

echo ""
echo "3. 启动服务..."
docker-compose -f docker-compose.prod.yml up -d

echo ""
echo "4. 等待服务启动..."
sleep 10

echo ""
echo "5. 检查服务状态..."
docker-compose -f docker-compose.prod.yml ps

echo ""
echo "======================================"
echo "部署完成!"
echo ""
echo "访问地址:"
echo "  - 前端: http://localhost"
echo "  - 后端API: http://localhost:8080"
echo "  - MinIO控制台: http://localhost:9001"
echo ""
echo "查看日志: docker-compose -f docker-compose.prod.yml logs -f"
echo "======================================"
