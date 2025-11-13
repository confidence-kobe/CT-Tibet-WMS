@echo off
REM RabbitMQ Windows 设置脚本
REM 此脚本帮助诊断和配置 RabbitMQ 环境

setlocal enabledelayedexpansion
chcp 65001 > nul

echo.
echo ================================================================
echo RabbitMQ 环境检查和配置脚本
echo ================================================================
echo.

REM 检查管理员权限
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo 错误: 此脚本需要管理员权限运行
    echo 请右键选择"以管理员身份运行"
    pause
    exit /b 1
)

echo [1] 检查 RabbitMQ 端口占用情况...
echo.
netstat -ano | findstr ":5672"
if %errorLevel% equ 0 (
    echo 端口 5672 已被占用
) else (
    echo 端口 5672 未被占用
)
echo.

netstat -ano | findstr ":15672"
if %errorLevel% equ 0 (
    echo 端口 15672 已被占用
) else (
    echo 端口 15672 未被占用
)
echo.

REM 检查 Erlang
echo [2] 检查 Erlang 安装...
echo.
where erl >nul 2>&1
if %errorLevel% equ 0 (
    echo Erlang 已安装
    erl -version
) else (
    echo Erlang 未安装或未添加到 PATH
    echo 请访问: https://www.erlang.org/downloads
)
echo.

REM 检查 RabbitMQ 服务
echo [3] 检查 RabbitMQ Windows 服务...
echo.
sc query RabbitMQ >nul 2>&1
if %errorLevel% equ 0 (
    echo RabbitMQ 服务已安装
    sc query RabbitMQ | findstr "STATE"
) else (
    echo RabbitMQ Windows 服务未安装
    echo 请从以下网址下载: https://www.rabbitmq.com/download.html
)
echo.

REM 检查 RabbitMQ 目录
echo [4] 检查 RabbitMQ 安装目录...
echo.
if exist "C:\Program Files\RabbitMQ Server" (
    echo RabbitMQ 已安装在: C:\Program Files\RabbitMQ Server
    dir "C:\Program Files\RabbitMQ Server"
) else (
    echo RabbitMQ 未在默认位置找到
)
echo.

REM 提供启动选项
echo ================================================================
echo 可用的操作:
echo ================================================================
echo.
echo [A] 启动 RabbitMQ 服务
echo [B] 停止 RabbitMQ 服务
echo [C] 重启 RabbitMQ 服务
echo [D] 查看 RabbitMQ 服务状态
echo [E] 创建虚拟主机 /wms
echo [F] 列出虚拟主机
echo [G] 启用管理插件
echo [H] 打开 RabbitMQ 管理界面
echo [I] 检查应用连接
echo [Q] 退出
echo.

set /p choice="请选择操作 [A-I,Q]: "

if /i "%choice%"=="A" (
    echo.
    echo 正在启动 RabbitMQ 服务...
    net start RabbitMQ
    if %errorLevel% equ 0 (
        echo RabbitMQ 服务启动成功
        timeout /t 3
        cls
        goto check_status
    ) else (
        echo RabbitMQ 服务启动失败
        pause
    )
)

if /i "%choice%"=="B" (
    echo.
    echo 正在停止 RabbitMQ 服务...
    net stop RabbitMQ
    if %errorLevel% equ 0 (
        echo RabbitMQ 服务已停止
        pause
    ) else (
        echo RabbitMQ 服务停止失败
        pause
    )
)

if /i "%choice%"=="C" (
    echo.
    echo 正在重启 RabbitMQ 服务...
    net stop RabbitMQ
    timeout /t 2
    net start RabbitMQ
    echo RabbitMQ 服务已重启
    timeout /t 3
    cls
    goto check_status
)

if /i "%choice%"=="D" (
    echo.
    echo RabbitMQ 服务状态:
    sc query RabbitMQ
    pause
)

if /i "%choice%"=="E" (
    echo.
    echo 正在创建虚拟主机 /wms...
    REM 查找 RabbitMQ sbin 目录
    for /d %%i in ("C:\Program Files\RabbitMQ Server\rabbitmq_server*") do (
        set RABBITMQ_HOME=%%i
    )

    if defined RABBITMQ_HOME (
        cd /d "!RABBITMQ_HOME!\sbin"
        call rabbitmqctl.bat add_vhost /wms
        call rabbitmqctl.bat set_permissions -p /wms guest ".*" ".*" ".*"
        echo 虚拟主机创建完成
    ) else (
        echo 未找到 RabbitMQ 安装目录
    )
    pause
)

if /i "%choice%"=="F" (
    echo.
    for /d %%i in ("C:\Program Files\RabbitMQ Server\rabbitmq_server*") do (
        set RABBITMQ_HOME=%%i
    )

    if defined RABBITMQ_HOME (
        cd /d "!RABBITMQ_HOME!\sbin"
        call rabbitmqctl.bat list_vhosts
    ) else (
        echo 未找到 RabbitMQ 安装目录
    )
    pause
)

if /i "%choice%"=="G" (
    echo.
    echo 正在启用管理插件...
    for /d %%i in ("C:\Program Files\RabbitMQ Server\rabbitmq_server*") do (
        set RABBITMQ_HOME=%%i
    )

    if defined RABBITMQ_HOME (
        cd /d "!RABBITMQ_HOME!\sbin"
        call rabbitmq-plugins.bat enable rabbitmq_management
        echo 请等待 RabbitMQ 自动重启...
        timeout /t 5
        echo 管理插件启用完成
    ) else (
        echo 未找到 RabbitMQ 安装目录
    )
    pause
)

if /i "%choice%"=="H" (
    echo.
    echo 正在打开 RabbitMQ 管理界面...
    start http://localhost:15672
    echo 如果浏览器未自动打开，请访问: http://localhost:15672
    echo 默认用户名: guest
    echo 默认密码: guest
    pause
)

if /i "%choice%"=="I" (
    echo.
    echo 检查应用连接...
    echo.
    echo 请确保应用已启动（端口 48888）
    echo.
    echo 然后访问 RabbitMQ 管理界面: http://localhost:15672
    echo 在 Connections 标签页查看应用连接状态
    pause
)

if /i "%choice%"=="Q" (
    exit /b 0
)

REM 检查状态
:check_status
echo.
echo ================================================================
echo 当前 RabbitMQ 状态
echo ================================================================
echo.
netstat -ano | findstr ":5672"
if %errorLevel% equ 0 (
    echo 端口 5672: 正在监听
) else (
    echo 端口 5672: 未监听
)
echo.
pause
goto start
