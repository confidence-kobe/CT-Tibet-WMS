#!/bin/bash

# CT-Tibet-WMS Docker快速启动脚本
# 用途: 使用Docker Compose一键启动整个系统

echo "======================================"
echo "  CT-Tibet-WMS Docker启动脚本"
echo "======================================"
echo ""

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ 错误: Docker 未安装"
    echo "请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ 错误: Docker Compose 未安装"
    echo "请先安装 Docker Compose"
    exit 1
fi

echo "✅ Docker 环境检查通过"
echo ""

# 选择环境
echo "请选择启动环境:"
echo "1) 开发环境 (development)"
echo "2) 生产环境 (production)"
echo ""
read -p "请输入选项 (1 或 2, 默认为 1): " ENV_CHOICE

case $ENV_CHOICE in
    2)
        ENV="production"
        COMPOSE_FILE="docker-compose.prod.yml"
        ;;
    *)
        ENV="development"
        COMPOSE_FILE="docker-compose.yml"
        ;;
esac

echo ""
echo "🚀 启动 $ENV 环境..."
echo ""

# 进入deployment目录
if [ -f "$COMPOSE_FILE" ]; then
    COMPOSE_PATH="$COMPOSE_FILE"
elif [ -f "deployment/$COMPOSE_FILE" ]; then
    cd deployment
    COMPOSE_PATH="$COMPOSE_FILE"
else
    echo "❌ 错误: 找不到 $COMPOSE_FILE"
    exit 1
fi

# 停止并移除旧容器
echo "📋 清理旧容器..."
docker-compose -f $COMPOSE_PATH down 2>/dev/null || docker compose -f $COMPOSE_PATH down 2>/dev/null

# 启动服务
echo ""
echo "🚀 启动Docker容器..."
if docker-compose -f $COMPOSE_PATH up -d 2>/dev/null; then
    echo "✅ 使用 docker-compose 启动成功"
elif docker compose -f $COMPOSE_PATH up -d 2>/dev/null; then
    echo "✅ 使用 docker compose 启动成功"
else
    echo "❌ Docker启动失败"
    exit 1
fi

# 等待服务启动
echo ""
echo "⏳ 等待服务启动..."
sleep 5

# 检查服务状态
echo ""
echo "📋 检查服务状态..."
if docker-compose -f $COMPOSE_PATH ps 2>/dev/null; then
    docker-compose -f $COMPOSE_PATH ps
elif docker compose -f $COMPOSE_PATH ps 2>/dev/null; then
    docker compose -f $COMPOSE_PATH ps
fi

echo ""
echo "======================================"
echo "  ✅ Docker服务已启动"
echo "======================================"
echo ""

if [ "$ENV" = "development" ]; then
    echo "开发环境访问地址:"
    echo "  前端:     http://localhost:8080"
    echo "  后端API:  http://localhost:8888"
    echo "  MySQL:    localhost:3306"
    echo "  Redis:    localhost:6379 (如果启用)"
    echo ""
    echo "默认账号: admin"
    echo "默认密码: 123456"
else
    echo "生产环境访问地址:"
    echo "  系统入口: http://localhost:80"
    echo "  后端API:  http://localhost:8888"
    echo ""
    echo "请使用配置的管理员账号登录"
fi

echo ""
echo "查看日志: docker-compose -f $COMPOSE_PATH logs -f"
echo "停止服务: docker-compose -f $COMPOSE_PATH down"
echo "或运行: ./docker-stop.sh"
echo ""
