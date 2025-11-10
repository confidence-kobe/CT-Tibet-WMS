@echo off
chcp 65001 >nul
echo =============================================
echo 西藏电信仓库管理系统 - 快速安装
echo =============================================
echo.
echo 请按以下步骤操作：
echo.
echo 1. 打开 MySQL 命令行客户端（MySQL Command Line Client）
echo 2. 输入 root 密码登录
echo 3. 复制并执行以下命令：
echo.
echo ----------------------------------------
echo SOURCE H:/java/CT-Tibet-WMS/sql/create_database.sql;
echo SOURCE H:/java/CT-Tibet-WMS/sql/schema.sql;
echo SOURCE H:/java/CT-Tibet-WMS/sql/init_data.sql;
echo ----------------------------------------
echo.
echo 或者直接执行：
echo.
echo mysql -u root -p ^< H:\java\CT-Tibet-WMS\sql\create_database.sql
echo mysql -u root -p ct_tibet_wms ^< H:\java\CT-Tibet-WMS\sql\schema.sql
echo mysql -u root -p ct_tibet_wms ^< H:\java\CT-Tibet-WMS\sql\init_data.sql
echo.
echo =============================================
echo.
echo 安装完成后的测试账号：
echo   admin / 123456 （系统管理员）
echo   wl_warehouse / 123456 （仓库管理员）
echo   wl_user1 / 123456 （普通员工）
echo.
pause
