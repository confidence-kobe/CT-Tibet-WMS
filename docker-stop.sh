#!/bin/bash

# CT-Tibet-WMS Docker停止脚本
# 用途: 停止所有Docker容器

echo "======================================"
echo "  CT-Tibet-WMS Docker停止脚本"
echo "======================================"
echo ""

# 检查Docker是否运行
if ! docker ps &> /dev/null; then
    echo "❌ Docker未运行或无权限访问"
    exit 1
fi

echo "🛑 停止Docker容器..."

# 尝试在当前目录停止
if [ -f "docker-compose.yml" ]; then
    docker-compose down 2>/dev/null || docker compose down 2>/dev/null
elif [ -f "deployment/docker-compose.yml" ]; then
    cd deployment
    docker-compose down 2>/dev/null || docker compose down 2>/dev/null
    cd ..
fi

# 同时尝试停止生产环境
if [ -f "docker-compose.prod.yml" ]; then
    docker-compose -f docker-compose.prod.yml down 2>/dev/null || docker compose -f docker-compose.prod.yml down 2>/dev/null
elif [ -f "deployment/docker-compose.prod.yml" ]; then
    cd deployment
    docker-compose -f docker-compose.prod.yml down 2>/dev/null || docker compose -f docker-compose.prod.yml down 2>/dev/null
    cd ..
fi

# 清理悬空镜像(可选)
read -p "是否清理未使用的Docker镜像? (y/N): " CLEAN_IMAGES
if [ "$CLEAN_IMAGES" = "y" ] || [ "$CLEAN_IMAGES" = "Y" ]; then
    echo "🧹 清理未使用的镜像..."
    docker image prune -f
    echo "✅ 清理完成"
fi

echo ""
echo "======================================"
echo "  ✅ Docker服务已停止"
echo "======================================"
