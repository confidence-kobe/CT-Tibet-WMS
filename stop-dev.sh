#!/bin/bash

# CT-Tibet-WMS 开发环境停止脚本
# 用途: 停止所有开发服务器

echo "======================================"
echo "  CT-Tibet-WMS 开发环境停止脚本"
echo "======================================"
echo ""

echo "🛑 停止服务..."

# 从PID文件读取并停止进程
if [ -f .backend.pid ]; then
    BACKEND_PID=$(cat .backend.pid)
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo "停止后端服务 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID 2>/dev/null
        echo "✅ 后端服务已停止"
    else
        echo "⚠️  后端服务未运行"
    fi
    rm -f .backend.pid
else
    # 尝试通过端口查找并停止
    BACKEND_PID=$(lsof -ti:8888 2>/dev/null)
    if [ ! -z "$BACKEND_PID" ]; then
        echo "停止后端服务 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID 2>/dev/null
        echo "✅ 后端服务已停止"
    else
        echo "⚠️  后端服务未运行"
    fi
fi

if [ -f .frontend.pid ]; then
    FRONTEND_PID=$(cat .frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo "停止PC前端服务 (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID 2>/dev/null
        echo "✅ PC前端服务已停止"
    else
        echo "⚠️  PC前端服务未运行"
    fi
    rm -f .frontend.pid
else
    # 尝试通过端口查找并停止
    FRONTEND_PID=$(lsof -ti:5173 2>/dev/null)
    if [ ! -z "$FRONTEND_PID" ]; then
        echo "停止PC前端服务 (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID 2>/dev/null
        echo "✅ PC前端服务已停止"
    else
        echo "⚠️  PC前端服务未运行"
    fi
fi

# 停止可能的Maven进程
MAVEN_PIDS=$(pgrep -f "maven" 2>/dev/null)
if [ ! -z "$MAVEN_PIDS" ]; then
    echo "停止Maven进程..."
    echo $MAVEN_PIDS | xargs kill 2>/dev/null
fi

# 停止可能的Vite进程
VITE_PIDS=$(pgrep -f "vite" 2>/dev/null)
if [ ! -z "$VITE_PIDS" ]; then
    echo "停止Vite进程..."
    echo $VITE_PIDS | xargs kill 2>/dev/null
fi

echo ""
echo "======================================"
echo "  ✅ 所有服务已停止"
echo "======================================"
