@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion
REM =============================================================
REM  CT-Tibet-WMS 一键启动（Windows）
REM
REM  依次完成：
REM    1. 首次运行时生成 .env.local（JWT 密钥、数据库账号），之后一直复用
REM    2. 检查 MySQL；数据库还没建时自动执行三个初始化脚本
REM    3. 检查 Redis；没在运行且装了 redis-server 时自动启动
REM    4. 编译后端，并在新窗口中启动（端口 48888）
REM    5. 在新窗口中启动 PC 前端（端口 4444，会自动打开浏览器）
REM
REM  用法：双击 start-dev.bat，或在命令行运行：
REM    start-dev.bat              启动全部
REM    start-dev.bat --no-build   后端不重新编译（代码没改时更快）
REM  停止：双击 stop-dev.bat
REM =============================================================

cd /d "%~dp0"
set "ROOT=%~dp0"
set "RUN=%ROOT%.run"
set "ENV_FILE=%ROOT%.env.local"
set "JAR=%ROOT%backend\target\ct-tibet-wms.jar"
set "DB_NAME=ct_tibet_wms"
set "BUILD=1"
if /i "%~1"=="--no-build" set "BUILD=0"
if not exist "%RUN%" mkdir "%RUN%"

echo.
echo ==== CT-Tibet-WMS 一键启动 ====

REM ---------- 1. 配置 ----------
echo.
echo [1/5] 读取配置 .env.local
if exist "%ENV_FILE%" goto env_exists
for /f "usebackq delims=" %%S in (`powershell -NoProfile -Command "[guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')"`) do set "NEW_SECRET=%%S"
if not defined NEW_SECRET goto fail_secret
> "%ENV_FILE%" echo # 本地开发配置（由 start-dev 自动生成，已被 .gitignore 忽略，不会提交到 Git）
>> "%ENV_FILE%" echo # 登录令牌签名密钥：至少 32 个字符。改了之后所有人需要重新登录。
>> "%ENV_FILE%" echo JWT_SECRET=!NEW_SECRET!
>> "%ENV_FILE%" echo # MySQL 账号密码：和你本机 MySQL 不一致时改这里
>> "%ENV_FILE%" echo DB_USER=root
>> "%ENV_FILE%" echo DB_PASSWORD=root
echo   OK 首次运行，已生成 .env.local（随机 JWT 密钥）
goto env_load
:env_exists
echo   OK 使用已有的 .env.local
:env_load
for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%ENV_FILE%") do set "%%A=%%B"
if not defined JWT_SECRET goto fail_secret_short
if "!JWT_SECRET:~31,1!"=="" goto fail_secret_short
if not defined DB_USER set "DB_USER=root"
if not defined DB_PASSWORD set "DB_PASSWORD=root"
REM 让后端使用 .env.local 里的数据库账号（覆盖 application-dev.yml 中的默认值）
set "SPRING_DATASOURCE_USERNAME=%DB_USER%"
set "SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD%"

REM ---------- 2. 检查软件 ----------
echo.
echo [2/5] 检查必需软件
for %%C in (java mvn node npm) do (
  where %%C >nul 2>&1 || (set "MISSING=%%C" & goto fail_missing)
)
echo   OK Java、Maven、Node.js 已安装

REM ---------- 3. MySQL ----------
echo.
echo [3/5] 检查 MySQL
call :port_open 3306
if errorlevel 1 goto fail_mysql_down
echo   OK MySQL 正在运行
where mysql >nul 2>&1
if errorlevel 1 (
  echo   [注意] 没有找到 mysql 命令行工具，跳过数据库检查；如果还没建库，请按 README「手动 1. 初始化数据库」执行
  goto redis
)
set "MYSQL_PWD=%DB_PASSWORD%"
set "TABLES="
for /f "usebackq delims=" %%N in (`mysql -u%DB_USER% -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='%DB_NAME%'" 2^>nul`) do set "TABLES=%%N"
if not defined TABLES goto fail_mysql_login
if not "%TABLES%"=="0" (
  echo   OK 数据库 %DB_NAME% 已存在（%TABLES% 张表）
  goto redis
)
echo   [注意] 数据库 %DB_NAME% 还没有初始化，开始建库并导入示例数据...
mysql -u%DB_USER% --default-character-set=utf8mb4 < "sql\create_database.sql" > "%RUN%\db-init.log" 2>&1 || (set "SQLFILE=create_database.sql" & goto fail_db_init)
mysql -u%DB_USER% --default-character-set=utf8mb4 %DB_NAME% < "sql\schema.sql" >> "%RUN%\db-init.log" 2>&1 || (set "SQLFILE=schema.sql" & goto fail_db_init)
mysql -u%DB_USER% --default-character-set=utf8mb4 %DB_NAME% < "sql\init_data.sql" >> "%RUN%\db-init.log" 2>&1 || (set "SQLFILE=init_data.sql" & goto fail_db_init)
echo   OK 数据库初始化完成（默认账号密码均为 123456）

REM ---------- 4. Redis ----------
:redis
set "MYSQL_PWD="
echo.
echo [4/5] 检查 Redis
call :port_open 6379
if not errorlevel 1 (
  echo   OK Redis 正在运行
  goto backend
)
where redis-server >nul 2>&1
if errorlevel 1 goto fail_redis
start "CT-WMS-Redis" /min redis-server
timeout /t 3 /nobreak >nul
call :port_open 6379
if errorlevel 1 goto fail_redis
echo   OK 已在新窗口中启动 Redis（已最小化）

REM ---------- 5. 后端 ----------
:backend
echo.
echo [5/5] 启动后端和 PC 前端
call :port_open 48888
if not errorlevel 1 (
  echo   [注意] 端口 48888 已被占用，后端可能已经在运行，跳过。需要重启请先运行 stop-dev.bat
  goto frontend
)
if "%BUILD%"=="0" if exist "%JAR%" goto backend_run
echo   编译后端中（第一次需要下载依赖，可能要几分钟）...
pushd backend
call mvn -q -B -DskipTests package > "%RUN%\build.log" 2>&1
set "BUILD_RESULT=%errorlevel%"
popd
if not "%BUILD_RESULT%"=="0" goto fail_build
echo   OK 编译完成
:backend_run
start "CT-WMS-Backend" cmd /k java -Dfile.encoding=UTF-8 -jar "%JAR%"
echo   等待后端启动（通常 20~60 秒，日志在标题为 CT-WMS-Backend 的窗口里）...
set /a WAITED=0
:wait_backend
call :port_open 48888
if not errorlevel 1 goto backend_ok
set /a WAITED+=3
if %WAITED% geq 180 goto fail_backend
timeout /t 3 /nobreak >nul
goto wait_backend
:backend_ok
echo   OK 后端已启动

REM ---------- 6. PC 前端 ----------
:frontend
call :port_open 4444
if not errorlevel 1 (
  echo   [注意] 端口 4444 已被占用，前端可能已经在运行，跳过
  goto done
)
if exist "frontend-pc\node_modules" goto frontend_run
echo   首次运行，安装前端依赖（可能要几分钟）...
pushd frontend-pc
call npm install > "%RUN%\npm-install.log" 2>&1
set "NPM_RESULT=%errorlevel%"
popd
if not "%NPM_RESULT%"=="0" goto fail_npm
:frontend_run
start "CT-WMS-Frontend" /D "%ROOT%frontend-pc" cmd /k npm run dev
set /a WAITED=0
:wait_frontend
call :port_open 4444
if not errorlevel 1 goto frontend_ok
set /a WAITED+=3
if %WAITED% geq 90 goto fail_frontend
timeout /t 3 /nobreak >nul
goto wait_frontend
:frontend_ok
echo   OK 前端已启动

:done
echo.
echo ==== 全部启动完成 ====
echo   PC 端：     http://localhost:4444/wms/
echo   API 文档：  http://localhost:48888/doc.html
echo   测试账号：  admin / wl_warehouse / wl_user1，密码均为 123456
echo   小程序：    用 HBuilderX 运行 miniprogram 目录（后端地址 http://localhost:48888）
echo   停止服务：  双击 stop-dev.bat
echo.
pause
exit /b 0

REM ---------- 工具函数 ----------
:port_open
REM 用 netstat 判断端口是否在监听：同时覆盖 IPv4 和 IPv6（新版 Node 的 Vite 可能只监听 ::1）
netstat -ano | findstr /r /c:":%1 .*LISTENING" >nul 2>&1
exit /b %errorlevel%

REM ---------- 错误处理 ----------
:fail_secret
echo   X 生成 JWT 密钥失败（需要 PowerShell）。请手动创建 .env.local，内容参考 README
goto fail
:fail_secret_short
echo   X .env.local 中 JWT_SECRET 少于 32 个字符，请修改或删除该文件后重新运行
goto fail
:fail_missing
echo   X 没有找到 %MISSING%，请先安装（见 README「安装软件」），安装后重新打开命令行窗口
goto fail
:fail_mysql_down
echo   X MySQL 没有运行（3306 端口无响应）。请先启动 MySQL 服务（服务名通常是 MySQL80）
goto fail
:fail_mysql_login
echo   X 无法用账号 %DB_USER% 登录 MySQL。请在 .env.local 中改成正确的 DB_USER / DB_PASSWORD
goto fail
:fail_db_init
echo   X 执行 sql\%SQLFILE% 失败，详情见 .run\db-init.log
goto fail
:fail_redis
echo   X Redis 没有运行，也没能自动启动。请先安装并启动 Redis（Windows 可用 Memurai 或 Redis for Windows）
goto fail
:fail_build
echo   X 后端编译失败，详情见 .run\build.log
goto fail
:fail_backend
echo   X 后端 3 分钟内没有启动成功，请查看标题为 CT-WMS-Backend 的窗口里的报错（常见原因见 README「常见启动问题」）
goto fail
:fail_npm
echo   X npm install 失败，详情见 .run\npm-install.log
goto fail
:fail_frontend
echo   X 前端没有启动成功，请查看标题为 CT-WMS-Frontend 的窗口里的报错
goto fail
:fail
echo.
pause
exit /b 1
