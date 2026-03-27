# 快速修复总结 - 后端异常问题

## 问题排序

### 1. P1 (CRITICAL) - 数据库字段缺失 - 用户登录完全不可用

**错误**: `java.sql.SQLSyntaxErrorException: Unknown column 'is_deleted' in 'field list'`

**根本原因**: MyBatis-Plus逻辑删除字段配置冲突

**问题位置**: `backend/src/main/java/com/ct/wms/entity/BaseEntity.java` 第61行

**一句话修复**: 删除 `@TableField("is_deleted")` 注解，让MyBatis-Plus使用全局配置

### 2. P3 (LOW) - RabbitMQ连接失败 - 可以暂时忽略

**错误**: `org.springframework.amqp.AmqpConnectException: Connection refused: getsockopt`

**根本原因**: RabbitMQ服务未启动

**影响**: 只影响异步消息通知功能，核心业务不受影响

---

## 立即执行的修复

### 步骤1: 编辑一个文件 (5分钟)

**文件**: `H:\java\CT-Tibet-WMS\backend\src\main\java\com\ct\wms\entity\BaseEntity.java`

**修改内容** (第60-62行):

删除这一行:
```java
@TableField("is_deleted")
```

修改后应该是:
```java
@TableLogic
private Integer isDeleted;
```

### 步骤2: 重新编译 (2分钟)

```bash
cd H:\java\CT-Tibet-WMS
mvn clean compile -DskipTests
```

### 步骤3: 重新启动应用 (1分钟)

```bash
cd backend
mvn spring-boot:run
```

### 步骤4: 测试登录 (1分钟)

```bash
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin", "password":"password123", "loginType":1}'
```

**预期**: 返回JWT Token，不再出现SQLSyntaxErrorException

---

## 为什么会出现这个问题?

```
配置层面:
┌─────────────────────────────────────────┐
│ application.yml                         │
│ logic-delete-field: deleted             │
└─────────────────────────────────────────┘
                ↓ (冲突)
┌─────────────────────────────────────────┐
│ BaseEntity.java                         │
│ @TableLogic                             │
│ @TableField("is_deleted")  ← 冲突！     │
│ private Integer isDeleted;              │
└─────────────────────────────────────────┘

结果: MyBatis-Plus不知道用哪个配置，生成了错误的SQL
```

---

## 修复原理

```
修复前:
  配置说用 "deleted"，代码说用 "is_deleted" → SQL生成错误

修复后:
  配置说用 "deleted"，代码不做限制 → MyBatis-Plus自动转换
  "deleted" (驼峰) → "is_deleted" (蛇形) → SQL正确
```

---

## 修复验证 - 看到这些说明修复成功了

### 日志中应该看到:

```
✓ 2025-11-13 22:35:00 [main] INFO Tomcat started on port(s): 48888
✓ 登录请求返回200 OK，包含JWT Token
✓ 日志中没有 "SQLSyntaxErrorException"
✓ 日志中没有 "Unknown column 'is_deleted'"
```

### 日志中不应该看到:

```
✗ java.sql.SQLSyntaxErrorException
✗ Unknown column 'is_deleted' in 'field list'
✗ Field 'deleted' not found
```

---

## 如果修复失败，请检查:

1. **确认编辑的是正确的文件**
   ```bash
   cat backend/src/main/java/com/ct/wms/entity/BaseEntity.java | grep -A 3 "TableLogic"
   # 应该只显示 @TableLogic，不显示 @TableField("is_deleted")
   ```

2. **确认有执行 `mvn clean compile`**
   ```bash
   mvn clean compile -DskipTests
   # 应该显示 [INFO] BUILD SUCCESS
   ```

3. **确认运行的是最新编译的版本**
   ```bash
   rm -rf backend/target
   mvn spring-boot:run
   ```

---

## RabbitMQ问题 (P3 - 优先级低，可选修复)

### 问题现象

大量日志输出:
```
[ERROR] Failed to check/redeclare auto-delete queue(s).
org.springframework.amqp.AmqpConnectException: Connection refused
```

### 为什么可以忽略?

- RabbitMQ只用于异步消息通知
- 登录、出入库等核心功能不需要RabbitMQ
- 开发环境不需要启动RabbitMQ

### 如果想解决 (二选一):

**方案A**: 启动RabbitMQ
```bash
# Windows: 下载RabbitMQ并运行 rabbitmq-server.bat
# Linux: sudo systemctl start rabbitmq-server
# Docker: docker run -d -p 5672:5672 rabbitmq:latest
```

**方案B**: 禁用RabbitMQ自动连接 (开发环境)
```yaml
# application-dev.yml
spring:
  rabbitmq:
    listener:
      simple:
        auto-startup: false
```

---

## 完整修复清单

- [ ] 打开 `backend/src/main/java/com/ct/wms/entity/BaseEntity.java`
- [ ] 删除第61行的 `@TableField("is_deleted")`
- [ ] 保存文件
- [ ] 执行 `mvn clean compile -DskipTests`
- [ ] 执行 `mvn spring-boot:run`
- [ ] 尝试登录
- [ ] 确认没有SQLSyntaxErrorException
- [ ] 修复完成！

---

## 修复后的收获

修复后，系统会正确处理逻辑删除，这意味着:

1. 用户可以正常登录
2. 所有数据查询都会自动过滤已删除的数据
3. 不会出现"已删除"的数据在系统中显示的问题
4. 数据库的逻辑删除功能正常工作

---

## 额外资源

- **详细分析**: 见 `ANALYSIS_REPORT.md`
- **详细验证步骤**: 见 `FIX_VERIFICATION.md`
- **应用日志位置**: `backend/logs/`
- **数据库脚本**: `sql/schema.sql`

---

**最后更新**: 2025-11-13
**修复预计时间**: 10分钟
**风险等级**: 低 (只改一个注解)
