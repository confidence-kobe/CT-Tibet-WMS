@echo off
chcp 65001 >nul
echo ======================================
echo 密码修复脚本
echo ======================================
echo.
echo 此脚本将重置所有用户密码为: 123456
echo.
set /p confirm=确认执行? (y/n):

if /i not "%confirm%"=="y" (
    echo 操作已取消
    pause
    exit /b
)

echo.
echo 请输入MySQL连接信息:
set /p MYSQL_HOST=MySQL主机 (默认: localhost):
if "%MYSQL_HOST%"=="" set MYSQL_HOST=localhost

set /p MYSQL_PORT=MySQL端口 (默认: 3306):
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306

set /p MYSQL_USER=MySQL用户 (默认: root):
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASSWORD=MySQL密码:

echo.
echo 正在执行密码修复...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% ct_tibet_wms < fix_password.sql

if errorlevel 1 (
    echo.
    echo ✗ 密码修复失败！
    echo 请检查:
    echo   1. MySQL服务是否运行
    echo   2. 连接信息是否正确
    echo   3. 数据库 ct_tibet_wms 是否存在
) else (
    echo.
    echo ✓ 密码修复成功！
    echo.
    echo 现在可以使用以下账号登录:
    echo   admin / 123456
    echo   wl_warehouse / 123456
    echo   wl_user1 / 123456
)

echo.
pause
