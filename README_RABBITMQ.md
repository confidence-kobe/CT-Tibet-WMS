# RabbitMQ 连接故障 - 完整解决方案

欢迎使用 CT-Tibet-WMS RabbitMQ 故障解决指南。本文档为您提供快速解决 RabbitMQ 连接问题的三种方案。

---

## 快速开始（选一种）

### 方案 A: 1 分钟快速启动（推荐初学者）

最快的方式，不需要安装 RabbitMQ。

```bash
# 1. 修改配置文件：backend/src/main/resources/application-dev.yml
# 添加以下内容：
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

# 2. 启动应用
cd backend
mvn clean install
mvn spring-boot:run

# 3. 完成！应用在 http://localhost:48888 启动
```

**优点**: 最快启动，零依赖
**缺点**: 消息队列功能不可用
**适用**: 快速原型开发

---

### 方案 C: 30 秒 Docker 启动（推荐，最简单）

使用 Docker 一键启动所有服务（RabbitMQ + MySQL + Redis）。

```powershell
# 1. 确保 Docker Desktop 已启动

# 2. 运行启动脚本
powershell -ExecutionPolicy Bypass -File .\SETUP_RABBITMQ_DOCKER.ps1

# 或手动启动
docker-compose up -d

# 3. 完成！所有服务已启动
# RabbitMQ: http://localhost:15672 (guest/guest)
# 应用: http://localhost:48888
```

**优点**: 最快启动，完整环境，团队协作
**缺点**: 需要 Docker 已安装
**适用**: 团队开发、完整功能测试

---

### 方案 B: 5 分钟 Windows 安装（推荐长期开发）

在 Windows 上安装完整的 RabbitMQ。

```powershell
# 1. 下载 Erlang: https://www.erlang.org/downloads
# 2. 下载 RabbitMQ: https://www.rabbitmq.com/download.html
# 3. 运行安装程序

# 4. 配置虚拟主机
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"
.\rabbitmqctl.bat add_vhost /wms
.\rabbitmqctl.bat set_permissions -p /wms guest ".*" ".*" ".*"

# 5. 启动服务
Start-Service RabbitMQ

# 6. 启动应用
cd backend
mvn spring-boot:run
```

**优点**: 完整功能，长期稳定
**缺点**: 安装步骤较多
**适用**: 长期本地开发

---

## 方案对比表

```
┌────────┬──────────┬──────────┬────────────┬──────────────┐
│ 方案   │ 启动时间 │ 难度     │ 适用场景   │ 消息队列     │
├────────┼──────────┼──────────┼────────────┼──────────────┤
│ A      │ 1 分钟   │ 最简单   │ 快速开发   │ ✗ 不可用     │
│ B      │ 5 分钟   │ 中等     │ 长期开发   │ ✓ 完整功能   │
│ C      │ 30 秒    │ 最简单   │ 团队协作   │ ✓ 完整功能   │
│        │          │          │ (推荐)     │ (推荐)       │
└────────┴──────────┴──────────┴────────────┴──────────────┘
```

---

## 验证您已成功启动

### 方案 A 验证
```
✓ 应用启动成功（日志中无 RabbitMQ 错误）
✓ 可以访问 http://localhost:48888
✓ Swagger UI 可访问 http://localhost:48888/doc.html
```

### 方案 B 或 C 验证
```
✓ 应用启动成功
✓ 可以访问 http://localhost:48888
✓ 可以访问 RabbitMQ 管理界面 http://localhost:15672
✓ 在管理界面的 Connections 标签页看到应用连接
✓ 在 Queues 标签页看到 3 个队列
```

---

## 常见问题快速解决

### Q: 应用启动时显示 "Connection refused"

**答**: RabbitMQ 未启动或未安装。选择方案B或C启动RabbitMQ。

```bash
# 检查 RabbitMQ 是否运行
netstat -ano | findstr ":5672"

# 如果显示 LISTENING 表示已启动
# 如果无输出表示未启动，请选择方案B或C启动
```

### Q: Docker 容器无法启动

**答**: 检查 Docker 是否运行，并查看详细日志。

```bash
# 启动 Docker Desktop
# 然后重试
docker-compose up -d

# 查看错误日志
docker-compose logs rabbitmq
```

### Q: 端口 5672 被占用

**答**: 更改 docker-compose.yml 中的端口映射或杀死占用进程。

```powershell
# 查看占用进程
netstat -ano | findstr ":5672"

# 杀死进程（PID 假设为 1234）
Stop-Process -Id 1234 -Force
```

### Q: 无法访问 RabbitMQ 管理界面

**答**: 确认 RabbitMQ 已启动，并检查防火墙设置。

```bash
# 检查 15672 端口
netstat -ano | findstr ":15672"

# 访问 http://localhost:15672
# 用户名: guest
# 密码: guest
```

---

## 推荐的开发流程

### 快速原型阶段
```
选择方案 A → 快速启动应用 → 验证核心功能
     (1 分钟)         (5 分钟)
```

### 完整功能阶段
```
选择方案 C → 启动完整环境 → 测试消息队列 → 验证所有功能
     (30 秒)      (1 分钟)        (5 分钟)
```

### 团队协作阶段
```
所有团队成员使用 docker-compose.yml → 环境完全一致 → 无配置问题
```

---

## 文档导航

### 📖 详细文档（按推荐阅读顺序）

1. **QUICK_START_RABBITMQ.md**
   - 快速启动指南
   - 三种方案的详细步骤
   - 首先读这个！

2. **RABBITMQ_TROUBLESHOOTING.md**
   - 完整故障排查指南
   - 所有常见问题的解决方案
   - 详细的技术参考

3. **RABBITMQ_SOLUTION_SUMMARY.md**
   - 方案总结
   - 快速参考
   - 配置示例

4. **RABBITMQ_INCIDENT_REPORT.md**
   - 事件报告
   - 根本原因分析
   - 详细的技术分析

---

## 支持的服务和端口

### 方案 A（禁用 RabbitMQ）
```
应用服务:
  - Web: http://localhost:48888
  - API Docs: http://localhost:48888/doc.html

数据库:
  - MySQL: 需自行启动或使用其他方案

缓存:
  - Redis: 需自行启动或使用其他方案

消息队列:
  - RabbitMQ: ✗ 禁用
```

### 方案 B 或 C（完整环境）
```
应用服务:
  - Web: http://localhost:48888
  - API Docs: http://localhost:48888/doc.html

消息队列:
  - RabbitMQ AMQP: localhost:5672
  - RabbitMQ 管理界面: http://localhost:15672
  - 用户: guest / 密码: guest

数据库:
  - MySQL: localhost:3306
  - 用户: root / 密码: root
  - 数据库: ct_tibet_wms

缓存:
  - Redis: localhost:6379
  - 密码: (无)
```

---

## 有用的命令

### Docker 相关
```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看服务状态
docker-compose ps

# 查看 RabbitMQ 日志
docker-compose logs -f rabbitmq

# 进入 RabbitMQ 容器
docker exec -it ct-wms-rabbitmq bash

# 清理所有数据（谨慎使用！）
docker-compose down -v
```

### RabbitMQ 管理
```bash
# 创建虚拟主机（如果未创建）
docker exec ct-wms-rabbitmq rabbitmqctl add_vhost /wms

# 设置权限
docker exec ct-wms-rabbitmq rabbitmqctl set_permissions -p /wms guest ".*" ".*" ".*"

# 列出虚拟主机
docker exec ct-wms-rabbitmq rabbitmqctl list_vhosts

# 列出用户
docker exec ct-wms-rabbitmq rabbitmqctl list_users
```

### 应用相关
```bash
# 启动应用
cd backend
mvn spring-boot:run

# 清理构建
mvn clean

# 重新构建
mvn clean install

# 运行单元测试
mvn test
```

---

## 故障排查树

```
应用无法启动?
├─ 看到 "Connection refused" 错误?
│  ├─ 使用方案 A?
│  │  └─ 移除 AutoConfiguration exclude 配置
│  ├─ 使用方案 B 或 C?
│  │  └─ 启动 RabbitMQ
│  │     ├─ docker-compose up -d (方案C)
│  │     └─ Start-Service RabbitMQ (方案B)
│
├─ 看到其他错误?
│  └─ 查看详细日志
│     └─ type backend/logs/error.log
│
└─ 应用启动成功?
   └─ 验证所有服务运行正常
      ├─ 访问 http://localhost:48888
      ├─ 检查 RabbitMQ http://localhost:15672
      └─ 查看应用日志确认无警告
```

---

## 快速参考卡

### 方案选择
```
【快速开发】→ 方案 A （1 分钟）
【完整功能】→ 方案 C （30 秒，推荐）
【生产环境】→ 方案 B （5 分钟）
```

### 启动应用
```
# 禁用 RabbitMQ
mvn spring-boot:run

# 使用 RabbitMQ
docker-compose up -d
mvn spring-boot:run
```

### 验证
```
# 应用运行
http://localhost:48888

# RabbitMQ 管理
http://localhost:15672 (guest/guest)

# API 文档
http://localhost:48888/doc.html
```

### 常用端口
```
应用: 48888
RabbitMQ AMQP: 5672
RabbitMQ 管理: 15672
MySQL: 3306
Redis: 6379
```

---

## 需要帮助？

### 文档查找
- 快速启动问题? → 查看 **QUICK_START_RABBITMQ.md**
- 详细故障排查? → 查看 **RABBITMQ_TROUBLESHOOTING.md**
- 方案对比? → 查看 **RABBITMQ_SOLUTION_SUMMARY.md**
- 事件分析? → 查看 **RABBITMQ_INCIDENT_REPORT.md**

### 问题反馈
1. 收集日志信息
2. 查看相应的文档章节
3. 按照故障排查步骤操作

### 获取更多信息
- RabbitMQ 官方文档: https://www.rabbitmq.com/documentation.html
- Spring AMQP 文档: https://spring.io/projects/spring-amqp
- Docker 文档: https://docs.docker.com/

---

## 总结

| 场景 | 推荐方案 | 时间 |
|------|---------|------|
| 快速原型 | 方案 A | 1 分钟 |
| 完整开发 | 方案 C | 30 秒 |
| 生产环境 | 方案 B | 5 分钟 |

**立即开始**: 选择适合您的方案，按照步骤操作即可！

---

**文档版本**: 1.0
**最后更新**: 2025-11-13
**维护者**: CT-WMS Development Team

祝开发愉快！
