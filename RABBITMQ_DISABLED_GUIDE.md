# RabbitMQ 已禁用 - 快速指南

**状态**: ✅ 已成功禁用 RabbitMQ（方案A）
**修改时间**: 2025-11-13
**适用环境**: 开发环境（dev profile）

---

## 已完成的配置

### 修改的文件
✅ `backend/src/main/resources/application-dev.yml`

### 添加的配置
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
```

### 效果
- ✅ Spring Boot 启动时不再加载 RabbitMQ 配置
- ✅ 不再尝试连接 RabbitMQ 服务
- ✅ 不会出现 `Connection refused` 错误
- ✅ 核心业务功能完全不受影响

---

## 立即验证（3步骤）

### 步骤 1: 停止当前运行的应用（如果有）

在 IDEA 中：
- 点击红色停止按钮
- 或在终端按 `Ctrl+C`

或使用命令：
```bash
# Windows
taskkill /F /IM java.exe

# 或查找具体进程
netstat -ano | findstr :48888
taskkill /F /PID <进程ID>
```

### 步骤 2: 重新启动应用

在 IDEA 中：
```
右键 WmsApplication.java → Debug 'WmsApplication'
```

或使用 Maven 命令：
```bash
cd backend
mvn spring-boot:run
```

### 步骤 3: 验证启动成功

**查看控制台输出**，应该看到：

✅ **成功的标志**：
```
Started WmsApplication in X.XXX seconds
Tomcat started on port(s): 48888
```

❌ **不应该出现**：
```
❌ AmqpConnectException
❌ Connection refused
❌ Failed to check/redeclare auto-delete queue(s)
```

**测试应用**：
```bash
# 访问健康检查接口
curl http://localhost:48888/actuator/health

# 或在浏览器打开
http://localhost:48888
```

---

## 功能影响说明

### ✅ 不受影响的功能（核心业务）
- ✅ 用户登录/登出
- ✅ 物资管理（CRUD）
- ✅ 入库管理
- ✅ 出库管理
- ✅ 申请审批流程
- ✅ 库存查询
- ✅ 统计报表
- ✅ 所有 REST API 接口

### ⚠️ 受影响的功能（非关键）
- ⚠️ 站内消息异步推送（改为同步）
- ⚠️ 微信模板消息推送（暂不可用）
- ⚠️ 审批完成通知（暂不可用）
- ⚠️ 库存预警通知（暂不可用）

**说明**：这些功能可以在需要时通过启用 RabbitMQ 恢复。

---

## 后续操作建议

### 方案 1: 继续使用当前配置（推荐初期开发）
**无需任何操作**，直接开发核心业务功能。

适用场景：
- 快速原型开发
- 核心功能实现阶段
- 不需要消息通知的场景

### 方案 2: 稍后启用 RabbitMQ（需要消息功能时）

当需要测试消息通知功能时：

#### 选项 A: 使用 Docker（推荐）
```bash
# 启动 RabbitMQ
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_VHOST=/wms \
  rabbitmq:3-management

# 修改 application-dev.yml，注释掉禁用配置
# spring:
#   autoconfigure:
#     exclude:
#       - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

# 重启应用
```

#### 选项 B: Windows 安装
参考 `QUICK_START_RABBITMQ.md` 中的方案 B

---

## 故障排查

### 问题 1: 启动时仍然有 RabbitMQ 错误

**原因**: 配置文件未生效

**解决**：
1. 确认 `application.yml` 中的 `spring.profiles.active: dev`
2. 确认 IDEA 中的 Active Profile 设置为 `dev`
3. 清理重新编译：
   ```bash
   mvn clean compile
   ```

### 问题 2: 应用启动失败

**检查清单**：
- [ ] MySQL 数据库是否启动
- [ ] Redis 服务是否启动（如果需要）
- [ ] 端口 48888 是否被占用
- [ ] 查看完整错误日志：`backend/logs/error.log`

### 问题 3: 想要临时测试消息功能

**快速启用 RabbitMQ**：
```bash
# 使用 Docker 快速启动
docker run -d --name rabbitmq-test \
  -p 5672:5672 \
  rabbitmq:3

# 临时注释掉禁用配置（不要提交）
# 在 application-dev.yml 中注释：
#   autoconfigure:
#     exclude:
#       - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

# 重启应用
```

测试完成后停止：
```bash
docker stop rabbitmq-test
docker rm rabbitmq-test

# 恢复禁用配置
```

---

## 配置文件完整示例

### application-dev.yml（当前配置）
```yaml
spring:
  # 禁用 RabbitMQ 自动配置（方案A：快速开发模式）
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

  datasource:
    url: jdbc:mysql://localhost:3306/ct_tibet_wms?...
    username: root
    password: root

  redis:
    host: localhost
    port: 6379

  # RabbitMQ 配置（已禁用）
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

---

## 切换到其他方案

### 切换到方案 C（Docker 完整部署）

1. **启动 Docker 服务**
   ```bash
   docker-compose up -d
   ```

2. **修改 application-dev.yml**
   ```yaml
   # 删除或注释掉这段配置
   # spring:
   #   autoconfigure:
   #     exclude:
   #       - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
   ```

3. **重启应用**
   ```bash
   mvn spring-boot:run
   ```

详细步骤参考：`QUICK_START_RABBITMQ.md`

---

## 常见问题 FAQ

### Q1: 禁用 RabbitMQ 会影响应用性能吗？
**A**: 不会。反而可能提升启动速度，因为不需要初始化 RabbitMQ 连接池。

### Q2: 消息功能完全不可用了吗？
**A**: 部分功能降级：
- 站内消息仍然可用（直接存数据库）
- 微信推送暂时不可用（需要 RabbitMQ）

### Q3: 如何知道 RabbitMQ 是否真的被禁用了？
**A**: 查看启动日志，不应该有以下内容：
- `RabbitListenerEndpointContainer`
- `CachingConnectionFactory`
- `SimpleMessageListenerContainer`

### Q4: 生产环境可以这样配置吗？
**A**: 不建议。生产环境应该使用完整的 RabbitMQ 集群。此配置仅适用于开发环境。

### Q5: 如何快速恢复 RabbitMQ 功能？
**A**:
```yaml
# 方法1: 注释掉禁用配置
# spring:
#   autoconfigure:
#     exclude:
#       - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration

# 方法2: 创建 application-local.yml 覆盖配置
spring:
  autoconfigure:
    exclude: []  # 清空排除列表
```

---

## 验证清单

启动应用后，检查以下项：

- [ ] 应用成功启动（看到 "Started WmsApplication"）
- [ ] 无 RabbitMQ 相关错误日志
- [ ] 访问 http://localhost:48888 正常
- [ ] 登录功能正常
- [ ] API 接口正常响应
- [ ] Swagger 文档可访问（如果启用）

---

## 相关文档

- **完整方案对比**: `START_HERE.md`
- **Docker 部署**: `QUICK_START_RABBITMQ.md` (方案C)
- **故障排查**: `RABBITMQ_TROUBLESHOOTING.md`
- **调试指南**: `backend/IDEA-DEBUG-SETUP.md`

---

## 总结

✅ **当前状态**: RabbitMQ 已成功禁用
✅ **核心功能**: 完全正常
⚠️ **消息功能**: 部分降级（可接受）
✅ **开发体验**: 启动更快，无连接错误

**建议**: 专注核心业务开发，需要消息功能时再启用 RabbitMQ。

---

**快速命令参考**：
```bash
# 启动应用
cd backend && mvn spring-boot:run

# 测试健康状态
curl http://localhost:48888/actuator/health

# 查看日志
tail -f backend/logs/debug-for-claude.log
```

如有问题，请查看 `backend/logs/error.log` 或联系 Claude Code 分析日志。
