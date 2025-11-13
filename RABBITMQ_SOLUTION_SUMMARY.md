# RabbitMQ 连接故障 - 完整解决方案总结

## 问题诊断

### 错误信息
```
org.springframework.amqp.AmqpConnectException: java.net.ConnectException: Connection refused: getsockopt
```

### 根本原因
RabbitMQ 服务未启动或不可用，导致应用无法连接到 localhost:5672。

---

## 解决方案（三选一）

### 方案 A: 快速修复（推荐用于快速开发）
**时间**: 1分钟 | **难度**: 最简单

禁用 RabbitMQ 自动配置，应用仍可正常运行（消息队列功能降级）。

**配置文件**: `backend/src/main/resources/application-dev.yml`
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

**优点**:
- 最快启动
- 无需安装任何依赖
- 核心业务完全不受影响

**适用场景**: 快速原型开发、调试核心功能

---

### 方案 B: Windows 本地安装
**时间**: 5分钟 | **难度**: 中等

在 Windows 上安装并配置 RabbitMQ（完整生产级安装）。

**安装步骤**:
1. 下载 Erlang OTP: https://www.erlang.org/downloads
2. 下载 RabbitMQ: https://www.rabbitmq.com/download.html
3. 运行 `SETUP_RABBITMQ_WINDOWS.bat` 脚本进行配置

**验证**:
```powershell
netstat -ano | findstr ":5672"
# 访问 http://localhost:15672 (guest/guest)
```

**适用场景**: 长期本地开发、完整功能测试

---

### 方案 C: Docker 方式（最推荐）
**时间**: 30秒 | **难度**: 最简单

使用 Docker 一键启动所有服务（RabbitMQ + MySQL + Redis）。

**启动方式**:

```powershell
# 方式1: 使用 PowerShell 脚本（推荐）
powershell -ExecutionPolicy Bypass -File .\SETUP_RABBITMQ_DOCKER.ps1

# 方式2: 使用 docker-compose 命令
docker-compose up -d
```

**验证**:
```bash
docker-compose ps
# 应看到所有容器都是 "Up" 状态

# 访问 http://localhost:15672 (guest/guest)
```

**适用场景**: 团队开发、完整环境测试、生产前验证

---

## 推荐开发流程

### 第1阶段: 快速原型（方案A）
```bash
# 1. 修改配置禁用 RabbitMQ
# 编辑: backend/src/main/resources/application-dev.yml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

# 2. 启动应用
cd backend
mvn clean install
mvn spring-boot:run
```

### 第2阶段: 完整功能（方案C）
```bash
# 1. 启动完整环境
docker-compose up -d

# 2. 移除配置中的排除项
# 恢复: backend/src/main/resources/application-dev.yml
# 移除或注释掉 exclude 部分

# 3. 启动应用
cd backend
mvn spring-boot:run

# 4. 验证消息队列功能
# 访问 http://localhost:15672
# 在 Queues 标签页查看消息
```

---

## 文件清单

### 新增文件

| 文件 | 用途 |
|------|------|
| `RABBITMQ_TROUBLESHOOTING.md` | 完整故障排查指南（最详细） |
| `QUICK_START_RABBITMQ.md` | 快速启动指南（推荐首先查看） |
| `RABBITMQ_SOLUTION_SUMMARY.md` | 本文件 - 方案总结 |
| `docker-compose.yml` | Docker 服务编排配置 |
| `SETUP_RABBITMQ_WINDOWS.bat` | Windows 设置脚本 |
| `SETUP_RABBITMQ_DOCKER.ps1` | Docker PowerShell 脚本 |

### 修改文件

| 文件 | 修改内容 |
|------|---------|
| `backend/src/main/resources/application.yml` | 添加连接超时和重试配置 |
| `backend/src/main/resources/application-dev.yml` | 支持禁用 RabbitMQ 配置 |

---

## 快速问题解决

### "Connection refused: getsockopt"
```bash
# 原因: RabbitMQ 未启动
# 解决:
docker-compose up -d           # 方案C
# 或
Start-Service RabbitMQ         # 方案B
# 或
# 禁用 RabbitMQ（方案A）
```

### 端口 5672 已被占用
```powershell
# 查找占用进程
netstat -ano | findstr ":5672"

# 杀死进程
Stop-Process -Id [PID] -Force
```

### Docker 容器无法启动
```bash
# 查看错误日志
docker-compose logs rabbitmq

# 清理重试
docker-compose down -v
docker-compose up -d
```

### 应用无法连接 RabbitMQ
```bash
# 1. 检查 RabbitMQ 是否运行
docker-compose ps
# 或
netstat -ano | findstr ":5672"

# 2. 检查应用配置
cat backend/src/main/resources/application-dev.yml | grep -A5 rabbitmq

# 3. 查看应用日志
type backend/logs/error.log | findstr "RabbitMQ"
```

---

## 配置参考

### 完整 RabbitMQ 配置（application.yml）

```yaml
spring:
  rabbitmq:
    # 基本配置
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: /wms

    # 连接配置
    connection-timeout: ${RABBITMQ_TIMEOUT:10000}
    cache:
      connection:
        timeout: ${RABBITMQ_TIMEOUT:10000}

    # 消息确认
    publisher-confirm-type: correlated
    publisher-returns: true

    # 监听器配置
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 1
        retry:
          enabled: true
          initial-interval: 1000
          max-interval: 10000
          multiplier: 1.0
          max-attempts: 3
```

### 环境变量配置（.env）

```env
# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_TIMEOUT=10000

# MySQL
DB_PASSWORD=root

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## 验证清单

启动应用前，确认以下检查点：

- [ ] 选择了合适的方案（A/B/C）
- [ ] 修改了相应的配置文件
- [ ] RabbitMQ 服务已启动（方案B或C）
- [ ] 检查了 5672 端口状态
- [ ] 应用可以成功启动
- [ ] 查看了应用日志确认无 RabbitMQ 错误
- [ ] （可选）验证了 RabbitMQ 管理界面可访问

---

## 获取帮助

### 文档导航

```
├─ QUICK_START_RABBITMQ.md         ← 首先读这个
│  └─ 快速启动指南，包含三种方案对比
│
├─ RABBITMQ_TROUBLESHOOTING.md     ← 详细问题排查
│  └─ 完整故障排查指南，包含所有常见问题
│
└─ RABBITMQ_SOLUTION_SUMMARY.md    ← 你在这里
   └─ 方案总结和快速参考
```

### 常见问题快速链接

- **应用启动失败?** → RABBITMQ_TROUBLESHOOTING.md 的"验证步骤"章节
- **想快速启动?** → QUICK_START_RABBITMQ.md 的"方案A"
- **想完整安装?** → RABBITMQ_TROUBLESHOOTING.md 的"方案B"
- **想用 Docker?** → QUICK_START_RABBITMQ.md 的"方案C"

---

## 技术支持

### 收集日志信息

如需进一步帮助，请收集以下信息：

```bash
# 1. 应用错误日志
cat backend/logs/error.log

# 2. RabbitMQ 日志
docker logs ct-wms-rabbitmq

# 3. Docker 容器状态
docker-compose ps

# 4. 端口占用情况
netstat -ano | findstr ":5672"

# 5. 应用配置
cat backend/src/main/resources/application-dev.yml
```

---

## 成功标志

### 方案A（禁用 RabbitMQ）
```
应用成功启动，打印日志如下：
  INFO ... Spring Boot Application started successfully
  INFO ... Server running at http://localhost:48888
```

### 方案B/C（使用 RabbitMQ）
```
应用成功启动，日志中看到：
  INFO ... Successfully established connection to RabbitMQ
  INFO ... RabbitMQ listeners initialized
  INFO ... Server running at http://localhost:48888

RabbitMQ 管理界面可访问：
  http://localhost:15672 (guest/guest)
  Connections 标签页可见应用连接
  Queues 标签页可见 3 个队列
```

---

## 性能优化建议

### 开发环境
```yaml
spring:
  rabbitmq:
    connection-timeout: 5000      # 快速失败
    listener:
      simple:
        max-concurrent-consumers: 1
        concurrent-consumers: 1
        prefetch: 1
```

### 测试环境
```yaml
spring:
  rabbitmq:
    connection-timeout: 10000
    listener:
      simple:
        max-concurrent-consumers: 5
        concurrent-consumers: 3
        prefetch: 5
```

### 生产环境
```yaml
spring:
  rabbitmq:
    connection-timeout: 30000
    listener:
      simple:
        max-concurrent-consumers: 20
        concurrent-consumers: 10
        prefetch: 10
    # 启用连接池
    cache:
      channel:
        size: 25
      connection:
        size: 3
        timeout: 30000
```

---

## 常见错误代码表

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| `Connection refused: getsockopt` | RabbitMQ 未启动 | 启动 RabbitMQ（方案B或C） |
| `Connection refused: connect` | RabbitMQ 端口不正确 | 检查 application.yml 配置 |
| `Authentication failed` | 用户名密码错误 | 确认使用 guest/guest |
| `Virtual host does not exist` | /wms 虚拟主机不存在 | 运行虚拟主机配置脚本 |
| `Channel/queue closed` | 连接丢失 | 检查网络，重启 RabbitMQ |

---

## 总结

1. **快速开发**: 使用方案A（禁用 RabbitMQ）
2. **完整功能**: 使用方案C（Docker，推荐）
3. **生产环境**: 使用方案B（Windows 安装）或 Kubernetes（容器编排）

选择合适的方案，按照相应指南操作即可快速解决问题。

---

**创建日期**: 2025-11-13
**文档版本**: 1.0
**维护者**: CT-WMS Development Team
