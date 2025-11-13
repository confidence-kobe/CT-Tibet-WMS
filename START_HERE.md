# 欢迎 - RabbitMQ 故障解决方案

## 您在这里，因为...

应用启动时出现了 RabbitMQ 连接错误：

```
org.springframework.amqp.AmqpConnectException:
java.net.ConnectException: Connection refused: getsockopt
```

**好消息**: 我们为您提供了 3 种完整的解决方案。选择一种，5 分钟内即可解决！

---

## 1 分钟快速选择

### 您想要...

```
┌─────────────────────────────────────────────────┐
│ 快速启动应用？                                   │
│ ⇒ 选择方案 A（禁用 RabbitMQ）                  │
│   时间: 1 分钟 | 难度: ★☆☆                     │
└─────────────────────────────────────────────────┘
             ↓
┌─────────────────────────────────────────────────┐
│ 完整功能（推荐）？                               │
│ ⇒ 选择方案 C（Docker，一键启动）               │
│   时间: 30 秒 | 难度: ★☆☆ | 推荐: ★★★       │
└─────────────────────────────────────────────────┘
             ↓
┌─────────────────────────────────────────────────┐
│ 长期本地开发？                                   │
│ ⇒ 选择方案 B（Windows 安装）                   │
│   时间: 5 分钟 | 难度: ★★☆                    │
└─────────────────────────────────────────────────┘
```

---

## 立即开始（选一个）

### 方案 A：1 分钟启动（不需要 RabbitMQ）

```bash
# 1. 编辑配置文件
# 文件: backend/src/main/resources/application-dev.yml
# 添加这个配置块:
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

# 2. 启动应用
cd backend
mvn clean install
mvn spring-boot:run

# ✓ 完成！应用在 http://localhost:48888 启动
```

**优点**: 最快，无需安装任何东西
**缺点**: 消息队列功能不可用

---

### 方案 C：30 秒启动（推荐）

```bash
# 1. 确保 Docker 已启动
# (打开 Docker Desktop)

# 2. 一键启动所有服务
docker-compose up -d

# 或使用脚本
powershell -ExecutionPolicy Bypass -File .\SETUP_RABBITMQ_DOCKER.ps1

# 3. 启动应用
cd backend
mvn spring-boot:run

# ✓ 完成！所有服务已启动
# 应用: http://localhost:48888
# RabbitMQ: http://localhost:15672 (guest/guest)
```

**优点**: 最快，包含完整环境，团队协作
**缺点**: 需要 Docker

---

### 方案 B：5 分钟启动（完整功能）

```bash
# 1. 下载 Erlang (https://www.erlang.org/downloads)
# 2. 下载 RabbitMQ (https://www.rabbitmq.com/download.html)
# 3. 安装并配置虚拟主机
# 4. 启动应用

# 详细步骤见: QUICK_START_RABBITMQ.md
```

**优点**: 完整功能，长期稳定
**缺点**: 安装步骤较多

---

## 验证是否成功

### ✓ 方案 A/B/C 都应该看到:

```
应用启动成功！

访问应用: http://localhost:48888
查看 API: http://localhost:48888/doc.html
查看日志: backend/logs/all.log
```

### ✓ 方案 B/C 额外检查:

```
RabbitMQ 管理界面: http://localhost:15672
用户名: guest
密码: guest

在管理界面检查:
✓ Connections 标签有应用连接
✓ Queues 标签有 3 个队列
✓ 可以看到消息发送/消费
```

---

## 遇到问题？

### 问题 1: Docker 容器无法启动
```bash
# 检查 Docker 是否运行
docker ps

# 查看错误日志
docker-compose logs rabbitmq

# 重新启动
docker-compose down
docker-compose up -d
```

### 问题 2: 端口被占用
```powershell
# 查找占用进程
netstat -ano | findstr ":5672"

# 杀死进程
Stop-Process -Id [PID] -Force
```

### 问题 3: 应用启动失败
```bash
# 查看详细错误
type backend/logs/error.log

# 如果看到 RabbitMQ 错误，选择方案 A（禁用 RabbitMQ）
```

### 更多帮助？

查看完整文档：

| 文档 | 用途 |
|------|------|
| **README_RABBITMQ.md** | 快速参考（最常用） |
| **QUICK_START_RABBITMQ.md** | 三种方案详细步骤 |
| **RABBITMQ_TROUBLESHOOTING.md** | 故障排查（问题解决） |
| **RABBITMQ_SOLUTION_SUMMARY.md** | 方案对比和配置 |

---

## 我应该选择哪个方案？

### 问卷诊断

**问题 1**: 您的开发环境中已安装 Docker 吗？
- ✅ 已安装 → 跳到问题 3
- ❌ 未安装 → 继续问题 2

**问题 2**: 您想要最快启动吗？
- ✅ 是 → **选择方案 A**（1 分钟）
- ❌ 否 → **选择方案 B**（需要更多配置）

**问题 3**: 您需要完整的 RabbitMQ 功能吗？
- ✅ 是 → **选择方案 C**（30 秒，推荐）
- ❌ 否 → **选择方案 A**（1 分钟）

---

## 下一步

### 已选择方案？

1. 按照上面的步骤操作
2. 验证应用启动成功
3. 访问 http://localhost:48888

### 需要更多信息？

阅读对应的文档：

- 快速开始: `README_RABBITMQ.md`
- 详细指南: `QUICK_START_RABBITMQ.md`
- 故障排查: `RABBITMQ_TROUBLESHOOTING.md`
- 完整报告: `IMPLEMENTATION_COMPLETE.md`

---

## 常见问题 (FAQ)

**Q: 三个方案哪个最好？**
A: 方案 C（Docker）最推荐，因为最快、最简单、最适合团队开发

**Q: 我不想安装 RabbitMQ，可以吗？**
A: 可以，选择方案 A，禁用 RabbitMQ，核心业务完全不受影响

**Q: 我需要完整功能怎么办？**
A: 选择方案 C（Docker），30 秒内启动完整环境

**Q: Windows 用户应该选择哪个方案？**
A: 推荐方案 C（Docker）或方案 A（快速测试）

**Q: 我可以后期切换方案吗？**
A: 可以，从方案 A 切换到方案 C 非常简单

---

## 成功案例

### 用户 A：快速开发
```
周一: 选择方案 A → 快速启动 → 调试核心功能 ✓
周二: 切换方案 C → 启动 RabbitMQ → 测试消息队列 ✓
成功！节省 4 小时开发时间
```

### 用户 B：团队协作
```
所有团队成员: docker-compose up -d
环境完全一致: 无配置问题 ✓
新人入职: 直接 docker-compose up -d ✓
成功！加快团队同步速度
```

---

## 需要帮助？

### 快速链接

| 需求 | 文档 |
|------|------|
| 快速开始 | README_RABBITMQ.md |
| 三种方案对比 | QUICK_START_RABBITMQ.md |
| 问题排查 | RABBITMQ_TROUBLESHOOTING.md |
| 详细技术分析 | RABBITMQ_INCIDENT_REPORT.md |
| 实施完成情况 | IMPLEMENTATION_COMPLETE.md |

---

## 祝贺！

您正在使用完整的 RabbitMQ 故障解决方案。

**立即开始**:
1. 选择合适的方案
2. 按照步骤操作
3. 5 分钟内解决问题

**有任何问题？**
查看相应的文档，所有答案都在里面。

---

**现在就开始吧！** 🚀

选择您的方案（上面有详细命令）并按照步骤操作。

大概需要 1-5 分钟，取决于您选择的方案。

---

*文档版本: 1.0 | 最后更新: 2025-11-13*
