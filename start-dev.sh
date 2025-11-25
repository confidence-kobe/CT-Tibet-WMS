#!/bin/bash

# CT-Tibet-WMS 开发环境启动脚本
# 用途: 一键启动后端和前端开发服务器

echo "======================================"
echo "  CT-Tibet-WMS 开发环境启动脚本"
echo "======================================"
echo ""

# 检查必要的命令
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo "❌ 错误: $1 未安装"
        echo "请先安装 $1"
        exit 1
    fi
}

echo "📋 检查环境..."
check_command java
check_command mvn
check_command node
check_command npm

echo "✅ 环境检查通过"
echo ""

# 启动后端
echo "🚀 启动后端服务..."
cd backend
mvn clean install -DskipTests > /dev/null 2>&1 &
BACKEND_PID=$!
echo "后端构建中... (PID: $BACKEND_PID)"

# 等待构建完成
wait $BACKEND_PID
if [ $? -eq 0 ]; then
    echo "✅ 后端构建完成"
    mvn spring-boot:run &
    BACKEND_RUN_PID=$!
    echo "✅ 后端服务已启动 (PID: $BACKEND_RUN_PID)"
    echo "   访问地址: http://localhost:8888"
else
    echo "❌ 后端构建失败"
    exit 1
fi

cd ..

# 启动PC前端
echo ""
echo "🚀 启动PC前端服务..."
cd frontend-pc

# 检查是否需要安装依赖
if [ ! -d "node_modules" ]; then
    echo "📦 安装前端依赖..."
    npm install
fi

npm run dev &
FRONTEND_PID=$!
echo "✅ PC前端服务已启动 (PID: $FRONTEND_PID)"
echo "   访问地址: http://localhost:5173"

cd ..

echo ""
echo "======================================"
echo "  ✅ 所有服务已启动"
echo "======================================"
echo ""
echo "后端服务: http://localhost:8888"
echo "PC前端:   http://localhost:5173"
echo "API文档:  http://localhost:8888/swagger-ui.html"
echo ""
echo "默认账号: admin"
echo "默认密码: 123456"
echo ""
echo "按 Ctrl+C 停止所有服务"
echo ""

# 保存PID到文件
echo $BACKEND_RUN_PID > .backend.pid
echo $FRONTEND_PID > .frontend.pid

# 等待用户中断
trap "echo ''; echo '🛑 停止所有服务...'; kill $BACKEND_RUN_PID $FRONTEND_PID 2>/dev/null; rm -f .backend.pid .frontend.pid; echo '✅ 服务已停止'; exit" INT TERM

# 保持脚本运行
wait
