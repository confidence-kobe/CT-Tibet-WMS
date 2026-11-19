@echo off
chcp 65001 >nul
setlocal EnableExtensions
REM =============================================================
REM  CT-Tibet-WMS 停止开发服务（Windows）
REM  停止由 start-dev.bat 启动的后端和 PC 前端。MySQL、Redis 不会被停止。
REM =============================================================

echo 停止 CT-Tibet-WMS 开发服务...
call :kill_port 4444 "PC 前端"
call :kill_port 48888 "后端"
REM 关闭 start-dev.bat 打开的命令行窗口
taskkill /FI "WINDOWTITLE eq CT-WMS-Frontend*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq CT-WMS-Backend*" /T /F >nul 2>&1
echo 完成（MySQL、Redis 保持运行）
echo.
pause
exit /b 0

REM 结束占用指定端口的进程
:kill_port
set "FOUND="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /r /c:":%~1 .*LISTENING"') do (
  if not "%%P"=="0" (
    taskkill /PID %%P /T /F >nul 2>&1
    set "FOUND=1"
  )
)
if defined FOUND (echo   OK %~2 已停止) else (echo   - %~2 没有在运行)
exit /b 0
