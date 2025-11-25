@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM CT-Tibet-WMS 开发环境启动脚本 (Windows)
REM 用途: 一键启动后端和前端开发服务器

echo ======================================
echo   CT-Tibet-WMS 开发环境启动脚本
echo ======================================
echo.

REM 检查必要的命令
echo 📋 检查环境...

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: Java 未安装
    echo 请先安装 JDK 11+
    pause
    exit /b 1
)

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: Maven 未安装
    echo 请先安装 Maven 3.8+
    pause
    exit /b 1
)

where node >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: Node.js 未安装
    echo 请先安装 Node.js 16+
    pause
    exit /b 1
)

where npm >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: npm 未安装
    echo 请先安装 Node.js (包含npm)
    pause
    exit /b 1
)

echo ✅ 环境检查通过
echo.

REM 启动后端
echo 🚀 启动后端服务...
cd backend
start "CT-WMS-Backend" cmd /c "mvn clean install -DskipTests && mvn spring-boot:run"
echo ✅ 后端服务正在启动...
echo    访问地址: http://localhost:8888
cd ..

REM 等待2秒
timeout /t 2 /nobreak >nul

REM 启动PC前端
echo.
echo 🚀 启动PC前端服务...
cd frontend-pc

REM 检查是否需要安装依赖
if not exist "node_modules" (
    echo 📦 安装前端依赖...
    call npm install
)

start "CT-WMS-Frontend" cmd /c "npm run dev"
echo ✅ PC前端服务正在启动...
echo    访问地址: http://localhost:5173
cd ..

echo.
echo ======================================
echo   ✅ 所有服务已启动
echo ======================================
echo.
echo 后端服务: http://localhost:8888
echo PC前端:   http://localhost:5173
echo API文档:  http://localhost:8888/swagger-ui.html
echo.
echo 默认账号: admin
echo 默认密码: 123456
echo.
echo 提示: 两个命令窗口已打开
echo       关闭窗口即可停止对应服务
echo.
echo 按任意键退出本窗口...
pause >nul
