# RabbitMQ Docker 快速设置脚本
# 用途: 使用 Docker 快速启动 RabbitMQ、MySQL 和 Redis
# 使用: powershell -ExecutionPolicy Bypass -File .\SETUP_RABBITMQ_DOCKER.ps1

param(
    [ValidateSet("start", "stop", "restart", "status", "clean", "setup")]
    [string]$Action = "setup"
)

$ErrorActionPreference = "Stop"

# 颜色定义
$Colors = @{
    Reset = "`e[0m"
    Green = "`e[32m"
    Yellow = "`e[33m"
    Red = "`e[31m"
    Blue = "`e[34m"
    Cyan = "`e[36m"
}

function Write-Log {
    param(
        [string]$Message,
        [ValidateSet("Info", "Success", "Warning", "Error")]
        [string]$Level = "Info"
    )

    $timestamp = Get-Date -Format "HH:mm:ss"

    switch ($Level) {
        "Success" { Write-Host "$($Colors.Green)[$timestamp] SUCCESS: $Message$($Colors.Reset)" }
        "Warning" { Write-Host "$($Colors.Yellow)[$timestamp] WARNING: $Message$($Colors.Reset)" }
        "Error" { Write-Host "$($Colors.Red)[$timestamp] ERROR: $Message$($Colors.Reset)" }
        default { Write-Host "$($Colors.Blue)[$timestamp] INFO: $Message$($Colors.Reset)" }
    }
}

function Test-DockerInstalled {
    try {
        $version = docker --version 2>&1
        Write-Log "Docker 已安装: $version" "Success"
        return $true
    }
    catch {
        Write-Log "Docker 未安装，请先安装 Docker Desktop" "Error"
        return $false
    }
}

function Test-DockerRunning {
    try {
        docker info >$null 2>&1
        Write-Log "Docker 守护进程运行正常" "Success"
        return $true
    }
    catch {
        Write-Log "Docker 守护进程未运行，请启动 Docker Desktop" "Error"
        return $false
    }
}

function Start-Services {
    Write-Log "启动 Docker 容器服务..." "Info"

    if ((Test-Path "docker-compose.yml") -eq $false) {
        Write-Log "未找到 docker-compose.yml 文件" "Error"
        return
    }

    try {
        docker-compose up -d
        Write-Log "Docker 容器启动中..." "Info"

        # 等待服务启动
        Start-Sleep -Seconds 5

        # 检查服务状态
        Write-Log "检查服务状态..." "Info"
        docker-compose ps

        # 等待 RabbitMQ 完全启动
        Write-Log "等待 RabbitMQ 完全启动..." "Info"
        $maxAttempts = 30
        $attempt = 0
        while ($attempt -lt $maxAttempts) {
            try {
                $response = Invoke-WebRequest -Uri "http://localhost:15672/api/whoami" `
                    -Authentication Basic `
                    -Credential (New-Object System.Management.Automation.PSCredential("guest", (ConvertTo-SecureString "guest" -AsPlainText -Force))) `
                    -ErrorAction SilentlyContinue

                if ($response.StatusCode -eq 200) {
                    Write-Log "RabbitMQ 管理界面已就绪" "Success"
                    break
                }
            }
            catch {
                $attempt++
                if ($attempt -lt $maxAttempts) {
                    Write-Host "." -NoNewline
                    Start-Sleep -Seconds 1
                }
            }
        }

        if ($attempt -eq $maxAttempts) {
            Write-Log "RabbitMQ 启动超时，请检查日志" "Warning"
        }

        Write-Log "服务启动完成！" "Success"
        Show-ServiceInfo

    }
    catch {
        Write-Log "启动服务失败: $_" "Error"
    }
}

function Stop-Services {
    Write-Log "停止 Docker 容器..." "Info"

    try {
        docker-compose down
        Write-Log "容器已停止" "Success"
    }
    catch {
        Write-Log "停止容器失败: $_" "Error"
    }
}

function Restart-Services {
    Stop-Services
    Start-Sleep -Seconds 2
    Start-Services
}

function Show-Status {
    Write-Log "Docker 容器状态:" "Info"
    docker-compose ps
}

function Clean-Services {
    Write-Log "清理 Docker 容器和数据卷..." "Warning"

    $confirm = Read-Host "确定要删除所有数据吗? (yes/no)"
    if ($confirm -eq "yes") {
        docker-compose down -v
        Write-Log "清理完成" "Success"
    }
    else {
        Write-Log "操作已取消" "Info"
    }
}

function Setup-Services {
    Write-Log "设置 RabbitMQ Docker 环境..." "Info"

    # 检查前置条件
    if (-not (Test-DockerInstalled)) {
        exit 1
    }

    if (-not (Test-DockerRunning)) {
        Write-Log "请先启动 Docker Desktop" "Error"
        exit 1
    }

    # 启动服务
    Start-Services

    # 配置虚拟主机
    Write-Log "配置 RabbitMQ 虚拟主机..." "Info"
    try {
        docker exec ct-wms-rabbitmq rabbitmqctl add_vhost /wms 2>$null
        docker exec ct-wms-rabbitmq rabbitmqctl set_permissions -p /wms guest ".*" ".*" ".*" 2>$null
        Write-Log "虚拟主机配置完成" "Success"
    }
    catch {
        Write-Log "虚拟主机配置失败或已存在: $_" "Warning"
    }

    # 启用管理插件
    Write-Log "启用 RabbitMQ 管理插件..." "Info"
    docker exec ct-wms-rabbitmq rabbitmq-plugins enable rabbitmq_management 2>$null
    Write-Log "管理插件启用完成" "Success"

    Show-ServiceInfo
}

function Show-ServiceInfo {
    Write-Log "================================================" "Info"
    Write-Log "服务信息" "Info"
    Write-Log "================================================" "Info"
    Write-Host ""

    Write-Host "$($Colors.Cyan)RabbitMQ:$($Colors.Reset)"
    Write-Host "  Web UI: http://localhost:15672"
    Write-Host "  AMQP: localhost:5672"
    Write-Host "  用户名: guest"
    Write-Host "  密码: guest"
    Write-Host ""

    Write-Host "$($Colors.Cyan)MySQL:$($Colors.Reset)"
    Write-Host "  主机: localhost:3306"
    Write-Host "  用户名: root"
    Write-Host "  密码: root"
    Write-Host "  数据库: ct_tibet_wms"
    Write-Host ""

    Write-Host "$($Colors.Cyan)Redis:$($Colors.Reset)"
    Write-Host "  主机: localhost:6379"
    Write-Host "  密码: (无)"
    Write-Host ""

    Write-Host "$($Colors.Cyan)应用配置:$($Colors.Reset)"
    Write-Host "  应用地址: http://localhost:48888"
    Write-Host "  Swagger UI: http://localhost:48888/doc.html"
    Write-Host ""

    Write-Host "$($Colors.Cyan)有用的命令:$($Colors.Reset)"
    Write-Host "  查看日志: docker-compose logs -f rabbitmq"
    Write-Host "  进入容器: docker exec -it ct-wms-rabbitmq bash"
    Write-Host "  停止服务: docker-compose down"
    Write-Host "  查看状态: docker-compose ps"
    Write-Host ""
}

function Show-Help {
    Write-Host @"
RabbitMQ Docker 快速设置脚本

使用方法:
  powershell -ExecutionPolicy Bypass -File .\SETUP_RABBITMQ_DOCKER.ps1 [选项]

选项:
  start   - 启动容器服务
  stop    - 停止容器服务
  restart - 重启容器服务
  status  - 显示容器状态
  clean   - 清理容器和数据卷
  setup   - 完整设置（默认）

示例:
  .\SETUP_RABBITMQ_DOCKER.ps1                    # 完整设置
  .\SETUP_RABBITMQ_DOCKER.ps1 -Action start      # 启动服务
  .\SETUP_RABBITMQ_DOCKER.ps1 -Action stop       # 停止服务
  .\SETUP_RABBITMQ_DOCKER.ps1 -Action status     # 显示状态

"@
}

# 主程序
Write-Host ""
Write-Log "RabbitMQ Docker 快速设置工具" "Blue"
Write-Host ""

switch ($Action.ToLower()) {
    "setup" { Setup-Services }
    "start" { Start-Services }
    "stop" { Stop-Services }
    "restart" { Restart-Services }
    "status" { Show-Status }
    "clean" { Clean-Services }
    default {
        Write-Log "未知操作: $Action" "Error"
        Show-Help
        exit 1
    }
}

Write-Host ""
