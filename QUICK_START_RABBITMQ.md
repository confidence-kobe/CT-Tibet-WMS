# RabbitMQ 快速启动指南

## 三种启动方式对比

| 方式 | 难度 | 启动速度 | 推荐场景 |
|------|------|---------|---------|
| **方案A: 禁用** | 最简单 | 立即 | 快速开发，暂不需要消息队列 |
| **方案B: Windows安装** | 中等 | 5分钟 | 长期开发，需要完整环境 |
| **方案C: Docker** | 最简单 | 30秒 | **推荐** - 最快最干净 |

---

## 方案A: 快速修复 - 禁用 RabbitMQ（1分钟）

最快的方式，适合想快速启动应用的场景。

### 步骤 1: 修改配置文件

编辑 `backend\src\main\resources\application-dev.yml`，添加以下配置：

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

或者，如果想保留配置但禁用连接，修改为：

```yaml
spring:
  rabbitmq:
    host: disabled
    port: 5672
```

### 步骤 2: 启动应用

```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### 优缺点

优点:
- 最快启动
- 无需安装任何依赖
- 核心业务不受影响

缺点:
- 消息通知功能不可用
- 只适合开发调试阶段

---

## 方案C: Docker 方式（推荐，30秒）

最推荐的方式，一命令启动所有服务。

### 前置条件

- 已安装 Docker Desktop for Windows
- Docker 正常运行

### 步骤 1: 启动服务

**使用 PowerShell 脚本**（最简单）:

```powershell
# 进入项目目录
cd H:\java\CT-Tibet-WMS

# 执行启动脚本
powershell -ExecutionPolicy Bypass -File .\SETUP_RABBITMQ_DOCKER.ps1

# 或指定操作
powershell -ExecutionPolicy Bypass -File .\SETUP_RABBITMQ_DOCKER.ps1 -Action start
```

**或使用 docker-compose 命令**:

```bash
cd H:\java\CT-Tibet-WMS

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f rabbitmq
```

### 步骤 2: 验证服务

```bash
# 检查容器运行状态
docker-compose ps

# 输出应该显示 3 个容器都是 "Up" 状态
# NAME                  STATUS
# ct-wms-mysql          Up ...
# ct-wms-rabbitmq       Up ...
# ct-wms-redis          Up ...
```

### 步骤 3: 访问服务

```
RabbitMQ Web UI: http://localhost:15672
  用户名: guest
  密码: guest

应用地址: http://localhost:48888
  Swagger: http://localhost:48888/doc.html

MySQL: localhost:3306
  用户名: root
  密码: root

Redis: localhost:6379
```

### 常用 Docker 命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f [service_name]

# 进入容器
docker exec -it ct-wms-rabbitmq bash

# 清理所有数据（谨慎！）
docker-compose down -v
```

---

## 方案B: Windows 本地安装（5分钟）

适合需要长期开发的场景。

### 步骤 1: 下载依赖

**下载 Erlang OTP**:
- 网址: https://www.erlang.org/downloads
- 下载最新版本（如 `otp_win64_25.3.exe`）
- 运行安装程序，按默认选项安装

**下载 RabbitMQ**:
- 网址: https://www.rabbitmq.com/download.html
- 下载 Windows 版本（如 `rabbitmq-server-3.12.0.exe`）
- 运行安装程序

### 步骤 2: 配置虚拟主机

在 PowerShell 中执行（管理员权限）:

```powershell
# 进入 RabbitMQ sbin 目录
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"

# 创建虚拟主机
.\rabbitmqctl.bat add_vhost /wms

# 设置权限
.\rabbitmqctl.bat set_permissions -p /wms guest ".*" ".*" ".*"
```

### 步骤 3: 启动 RabbitMQ

```powershell
# 启动服务
Start-Service RabbitMQ

# 或使用脚本
H:\java\CT-Tibet-WMS\SETUP_RABBITMQ_WINDOWS.bat
```

### 步骤 4: 验证

```powershell
# 检查端口
netstat -ano | findstr ":5672"

# 访问管理界面
# http://localhost:15672
```

---

## 故障排查

### Q1: 端口已被占用怎么办？

```powershell
# 查看占用端口的进程
netstat -ano | findstr ":5672"

# 假设输出: TCP    127.0.0.1:5672    LISTENING    1234
# 杀死进程
Stop-Process -Id 1234 -Force
```

### Q2: Docker 容器启动失败？

```bash
# 查看详细错误日志
docker-compose logs rabbitmq

# 检查 Docker 磁盘空间
docker system df

# 清理未使用的镜像
docker system prune -a
```

### Q3: RabbitMQ 管理界面无法访问？

```bash
# 进入容器检查状态
docker exec ct-wms-rabbitmq rabbitmqctl status

# 查看插件状态
docker exec ct-wms-rabbitmq rabbitmq-plugins list

# 重启容器
docker restart ct-wms-rabbitmq
```

### Q4: 应用连接失败？

检查 `backend/logs/all.log` 或 `backend/logs/error.log`：

```bash
# Windows PowerShell
Get-Content backend/logs/error.log | findstr "RabbitMQ"

# 或 bash
grep "RabbitMQ" backend/logs/error.log
```

如果看到 "Connection refused"，说明 RabbitMQ 未启动。

---

## 验证应用连接

### 方法1: 查看应用日志

启动应用后，在日志中搜索：

```
"Successfully established connection to RabbitMQ"
或
"Connected to RabbitMQ"
```

### 方法2: RabbitMQ 管理界面检查

1. 打开 http://localhost:15672
2. 点击 "Connections" 标签
3. 应该看到应用的连接（通常显示为 `spring-client` 或 `127.0.0.1:XXXXX`）

### 方法3: 查看队列

1. 打开 http://localhost:15672
2. 点击 "Queues" 标签
3. 应该看到 3 个队列：
   - `wms.notification.queue`
   - `wms.wechat.queue`
   - `wms.dlx.queue` (死信队列)

---

## 开发中的最佳实践

### 推荐流程

1. **快速原型阶段** - 使用方案A（禁用 RabbitMQ）
   ```bash
   mvn spring-boot:run
   ```

2. **需要消息队列时** - 切换到方案C（Docker）
   ```bash
   docker-compose up -d
   # 移除 application-dev.yml 中的排除配置
   mvn spring-boot:run
   ```

3. **在团队中共享** - 使用 docker-compose.yml
   ```bash
   # 团队成员只需
   docker-compose up -d
   mvn spring-boot:run
   ```

### 环境变量配置

创建 `.env` 文件（可选，覆盖默认值）:

```env
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_TIMEOUT=10000

DB_PASSWORD=root
REDIS_HOST=localhost
REDIS_PORT=6379
```

然后在应用启动时使用：

```bash
# Windows PowerShell
$env:RABBITMQ_HOST="localhost"
mvn spring-boot:run

# 或 bash
export RABBITMQ_HOST=localhost
mvn spring-boot:run
```

---

## 快速参考

### Docker 启动（推荐）
```bash
docker-compose up -d
```

### Windows 启动脚本
```powershell
powershell -ExecutionPolicy Bypass -File .\SETUP_RABBITMQ_DOCKER.ps1
```

### 禁用 RabbitMQ
```yaml
# application-dev.yml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

### 验证 RabbitMQ
```bash
# 检查端口
netstat -ano | findstr ":5672"

# 访问管理界面
# http://localhost:15672 (guest/guest)
```

---

## 下一步

- 查看完整文档: `RABBITMQ_TROUBLESHOOTING.md`
- 应用启动: `backend/README.md`
- 数据库初始化: `sql/`
- API 文档: `http://localhost:48888/doc.html`

---

**最后更新**: 2025-11-13
