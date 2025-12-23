@echo off
chcp 65001 >nul
echo ======================================
echo 快速密码修复
echo ======================================
echo.
echo 正在修复数据库密码...
echo.

REM 创建临时SQL文件
echo UPDATE ct_tibet_wms.tb_user SET password = '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e'; > temp_fix.sql
echo SELECT 'Password updated successfully!' AS result; >> temp_fix.sql

REM 执行SQL (尝试多种常见密码)
echo 尝试使用密码: root
mysql -u root -proot < temp_fix.sql 2>nul

if %errorlevel% == 0 (
    echo.
    echo ✓ 密码修复成功！
    echo.
    echo 现在可以使用以下账号登录:
    echo   账号: admin
    echo   密码: 123456
    echo.
    del temp_fix.sql
    pause
    exit /b 0
)

echo 密码root失败，尝试空密码...
mysql -u root < temp_fix.sql 2>nul

if %errorlevel% == 0 (
    echo.
    echo ✓ 密码修复成功！
    echo.
    echo 现在可以使用以下账号登录:
    echo   账号: admin
    echo   密码: 123456
    echo.
    del temp_fix.sql
    pause
    exit /b 0
)

echo.
echo ✗ 自动修复失败
echo.
echo 请手动执行以下步骤:
echo.
echo 1. 打开命令行
echo 2. 执行: mysql -u root -p
echo 3. 输入MySQL root密码
echo 4. 执行以下SQL:
echo.
echo    USE ct_tibet_wms;
echo    UPDATE tb_user SET password = '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e';
echo.

del temp_fix.sql
pause
