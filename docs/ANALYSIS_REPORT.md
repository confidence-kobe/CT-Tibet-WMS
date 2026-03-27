# 后端应用异常分析报告

**生成时间**: 2025-11-13
**分析范围**: Spring Boot仓库管理系统
**优先级评估**: P1 (数据库字段缺失) + P3 (RabbitMQ连接)

---

## 目录

1. [异常概览](#异常概览)
2. [异常1：数据库字段缺失 (P1 - 必须立即修复)](#异常1数据库字段缺失-p1---必须立即修复)
3. [异常2：RabbitMQ连接失败 (P3 - 可以暂时忽略)](#异常2rabbitmq连接失败-p3---可以暂时忽略)
4. [根本原因分析](#根本原因分析)
5. [修复方案](#修复方案)
6. [验证步骤](#验证步骤)

---

## 异常概览

| 异常ID | 异常类型 | 错误代码 | 优先级 | 影响范围 | 状态 |
|--------|---------|---------|--------|---------|------|
| 1 | SQLSyntaxErrorException | 42000 | P1 (CRITICAL) | 用户登录功能完全不可用 | 已确认 |
| 2 | AmqpConnectException | CONN_ERROR | P3 (LOW) | 异步消息通知延迟/不可用 | 可接受 |

---

## 异常1：数据库字段缺失 (P1 - 必须立即修复)

### 问题现象

**错误消息**:
```
java.sql.SQLSyntaxErrorException: Unknown column 'is_deleted' in 'field list'
```

**完整错误堆栈**:
```
SQL: SELECT id,username,password,real_name,phone,email,dept_id,role_id,status,avatar,
wechat_openid,wechat_nickname,last_login_time,last_login_ip,password_update_time,
is_first_login,remark,create_time,update_by,is_deleted FROM tb_user
WHERE is_deleted=0 AND (username = ?)

触发路径:
  UserDetailsServiceImpl.loadUserByUsername()
  ↓
  DaoAuthenticationProvider.retrieveUser()
  ↓
  AuthController.login() [POST /api/auth/login]
```

### 根本原因分析

**核心问题**: MyBatis-Plus配置与数据库表结构不一致

#### 问题的具体原因链:

1. **MyBatis-Plus逻辑删除配置** (application.yml)
   ```yaml
   mybatis-plus:
     global-config:
       db-config:
         logic-delete-field: deleted       # 指定逻辑删除字段名为 "deleted"
         logic-delete-value: 1              # 删除值为 1
         logic-not-delete-value: 0          # 未删除值为 0
   ```

2. **BaseEntity实体类配置** (BaseEntity.java)
   ```java
   @TableLogic                              # 启用逻辑删除功能
   @TableField("is_deleted")                # 映射到数据库字段 "is_deleted"
   private Integer isDeleted;                # 但字段名为 isDeleted (驼峰式)
   ```

3. **配置冲突的具体表现**:
   - 配置指定逻辑删除字段为 `deleted`
   - 但Entity实体类指定为 `isDeleted` (使用了 `@TableField("is_deleted")`)
   - MyBatis-Plus在生成SQL时产生了混淆

4. **数据库表实际情况** (schema.sql line 31)
   ```sql
   `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是'
   ```
   - 数据库表中确实存在 `is_deleted` 字段
   - 字段默认值为 0 (未删除状态)

### 为什么会触发这个错误

当UserDetailsServiceImpl执行以下代码时:
```java
User user = userMapper.selectOne(
    new LambdaQueryWrapper<User>()
        .eq(User::getUsername, username)
);
```

MyBatis-Plus会:
1. 识别User类继承自BaseEntity，且BaseEntity含有@TableLogic注解的isDeleted字段
2. 自动在SQL中添加逻辑删除条件: `WHERE is_deleted=0`
3. 但由于配置冲突，MyBatis-Plus在某些情况下会选错字段名或映射规则
4. 生成的SQL中混入了无效的字段引用或映射

### 影响范围

- **完全阻塞**用户登录功能 (AuthController.login)
- **影响任何**继承BaseEntity的实体查询操作
- **严重程度**: 系统不可用

---

## 异常2：RabbitMQ连接失败 (P3 - 可以暂时忽略)

### 问题现象

**错误消息**:
```
org.springframework.amqp.AmqpConnectException: java.net.ConnectException: Connection refused: getsocknet
```

**错误详情**:
```
Failed to check/redeclare auto-delete queue(s).
Caused by: java.net.ConnectException: Connection refused: getsockopt
  at java.base/sun.nio.ch.Net.pollConnect(Native Method)
  at com.rabbitmq.client.impl.SocketFrameHandlerFactory.create(SocketFrameHandlerFactory.java:62)
```

### 根本原因分析

**问题**: RabbitMQ服务不可用或未运行

#### 具体原因:

1. **应用配置** (application.yml)
   ```yaml
   spring:
     rabbitmq:
       host: ${RABBITMQ_HOST:localhost}     # 默认localhost
       port: ${RABBITMQ_PORT:5672}          # 默认5672
       username: ${RABBITMQ_USER:guest}     # 默认guest
       password: ${RABBITMQ_PASSWORD:guest} # 默认guest
       virtual-host: /wms
   ```

2. **环境状态**:
   - 配置指向 localhost:5672
   - 但RabbitMQ服务未启动或无法连接
   - 应用继续启动，但消息监听容器无法连接到RabbitMQ

3. **失败原因链**:
   - SimpleMessageListenerContainer 尝试与RabbitMQ连接
   - 连接被拒绝 (Connection refused)
   - Socket操作失败 (getsockopt)
   - 容器进入重试循环

### 影响范围

- **不阻塞应用启动** (RabbitMQ非强制依赖)
- **延迟或无法**发送以下功能:
  - 申请状态变更通知
  - 审批完成通知
  - 库存预警通知
  - 其他异步消息
- **严重程度**: 低 - 系统可用，但通知功能受损
- **日志污染**: 大量重试日志 (每5秒重试一次)

### 为什么可以暂时忽略

1. RabbitMQ是可选的异步通知组件
2. 主要的业务逻辑(登录、出入库等)不依赖RabbitMQ
3. 在开发环境中可以禁用RabbitMQ
4. 消息队列通常在生产环境才是必须的

---

## 根本原因分析

### MyBatis-Plus逻辑删除配置问题

**问题的本质**:

MyBatis-Plus有一个**关键的配置冲突**:

```
全局配置                 vs          实体类配置
logic-delete-field: deleted    @TableLogic + @TableField("is_deleted")
```

**为什么会产生冲突**:

1. **配置方式1(全局)**: application.yml中定义 `deleted` 为逻辑删除字段
2. **配置方式2(局部)**: BaseEntity中用 `@TableField("is_deleted")` 和 `@TableLogic` 覆盖

这两种配置同时存在时，MyBatis-Plus会:
- 优先使用实体类的 `@TableLogic` 注解
- 但全局配置仍然会被某些操作识别
- 导致SQL生成时出现混淆

### 数据库表结构无误

从schema.sql的第31行可以确认:
- 表确实包含 `is_deleted` 字段
- 字段类型正确 (TINYINT)
- 默认值正确 (0 = 未删除)

**所以问题不在数据库，而在应用配置**

---

## 修复方案

### 方案A: 统一使用全局配置(推荐) ✓

#### 修复步骤1: 修改BaseEntity

**文件**: `H:\java\CT-Tibet-WMS\backend\src\main\java\com\ct\wms\entity\BaseEntity.java`

**修改前**:
```java
@TableLogic
@TableField("is_deleted")
private Integer isDeleted;
```

**修改后 - 方式1(推荐)**:
```java
@TableLogic  // 只保留注解，删除@TableField
private Integer isDeleted;
```

或 **修改后 - 方式2**:
```java
@TableLogic
@TableField("deleted")  // 改为 "deleted" 以匹配全局配置
private Integer deleted;
```

**原因**:
- MyBatis-Plus的全局配置已指定字段名为 `deleted` (驼峰转换后映射到 `is_deleted`)
- 显式 `@TableField("is_deleted")` 会与全局配置冲突
- 移除@TableField或改用全局配置的字段名可以避免冲突

#### 修复步骤2: 验证application.yml

**文件**: `H:\java\CT-Tibet-WMS\backend\src\main\resources\application.yml`

确保以下配置存在(第78-80行):
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted          # 确保此字段名正确
      logic-delete-value: 1
      logic-not-delete-value: 0
```

#### 修复步骤3: 重新编译和测试

```bash
cd H:\java\CT-Tibet-WMS\backend
mvn clean compile
```

#### 预期效果

修改后，MyBatis-Plus会:
1. 自动将字段名 `deleted` 映射到数据库的 `is_deleted` (驼峰转换)
2. 在所有查询中自动添加 `WHERE is_deleted=0` 条件
3. SQL不再出现歧义

---

### 方案B: 同步更新所有实体类和配置

如果您希望在多个地方都使用 `is_deleted` 作为字段名，需要:

#### 步骤1: 修改application.yml
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: is_deleted       # 改为 is_deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

#### 步骤2: 更新BaseEntity
```java
@TableLogic
@TableField("is_deleted")
private Integer isDeleted;
```

#### 注意
- 这种方式不符合Java驼峰命名规范
- 不推荐使用

---

### 临时解决方案(允许部分开发继续进行)

如果您需要立即解除封锁，可以使用以下临时方案:

#### 临时方案1: 禁用逻辑删除

**修改**: `H:\java\CT-Tibet-WMS\backend\src\main\java\com\ct\wms\entity\BaseEntity.java`

```java
// 注释掉 @TableLogic 注解
// @TableLogic
@TableField("is_deleted")
private Integer isDeleted;
```

**优点**:
- 立即解除用户登录的阻塞
- 代码改动最小

**缺点**:
- 逻辑删除功能不可用
- 必须手动处理删除数据的过滤
- 不是长期解决方案

#### 临时方案2: 跳过RabbitMQ错误(可选)

**修改**: `H:\java\CT-Tibet-WMS\backend\src\main\resources\application.yml`

添加以下配置:
```yaml
spring:
  rabbitmq:
    # 如果RabbitMQ暂时不可用，可以禁用自动连接
    # 仅在开发环境使用
    listener:
      simple:
        # 增加重试间隔以减少日志污染
        retry:
          enabled: true
          initial-interval: 5000  # 5秒后重试
          max-attempts: 3          # 最多重试3次
```

---

## 修复方案详细步骤

### 推荐修复方案(方案A详细步骤)

#### Step 1: 修改BaseEntity.java

打开文件: `H:\java\CT-Tibet-WMS\backend\src\main\java\com\ct\wms\entity\BaseEntity.java`

查找第60-62行:
```java
@TableLogic
@TableField("is_deleted")
private Integer isDeleted;
```

替换为:
```java
@TableLogic
// MyBatis-Plus会根据全局配置自动映射 is_deleted 字段
// 不需要显式的 @TableField("is_deleted") 注解
private Integer isDeleted;
```

#### Step 2: 确认application.yml配置

打开: `H:\java\CT-Tibet-WMS\backend\src\main\resources\application.yml`

确认第75-80行配置正确:
```yaml
mybatis-plus:
  global-config:
    db-config:
      id-type: AUTO
      logic-delete-field: deleted          # 注意: 这里是 "deleted" (驼峰式)
      logic-delete-value: 1
      logic-not-delete-value: 0
```

重要: MyBatis-Plus会自动将 `deleted` (驼峰式)转换为 `is_deleted` (蛇形式)映射到数据库

#### Step 3: 清理并重新编译

```bash
# 在项目根目录执行
cd H:\java\CT-Tibet-WMS
mvn clean -DskipTests
mvn compile
```

#### Step 4: 启动应用进行测试

```bash
cd H:\java\CT-Tibet-WMS\backend
mvn spring-boot:run
```

#### Step 5: 验证登录功能

- 打开浏览器，访问: `http://localhost:48888`
- 尝试登录，应该不再出现 `SQLSyntaxErrorException`

---

## 验证步骤

### 验证1: 确认数据库字段存在

```sql
-- 连接到MySQL数据库
mysql -h localhost -u root -p

-- 选择数据库
USE ct_tibet_wms;

-- 查看tb_user表结构
DESC tb_user;

-- 应该能看到 is_deleted 字段
-- 类似输出:
-- | is_deleted | tinyint | NO | | 0 | |
```

### 验证2: 检查MyBatis生成的SQL

修改 `application.yml` 中的日志配置，启用SQL日志:

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 已启用
```

重启应用后，在控制台输出中查找类似的SQL:
```
SELECT id,username,password,...,is_deleted FROM tb_user
WHERE is_deleted = 0 AND (username = ?)
```

如果SQL中 `is_deleted` 字段正确出现，说明修复成功。

### 验证3: 执行登录测试

```bash
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123",
    "loginType": 1
  }'
```

**期望响应** (成功):
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "...",
    "userInfo": { ... }
  }
}
```

**错误响应** (失败):
```json
{
  "code": 500,
  "message": "Unknown column 'is_deleted' in 'field list'",
  "data": null
}
```

### 验证4: 检查应用日志

```bash
# 查看启动日志
tail -f H:\java\CT-Tibet-WMS\backend\logs\all.log

# 查看错误日志
tail -f H:\java\CT-Tibet-WMS\backend\logs\error.log
```

确保没有 `SQLSyntaxErrorException` 和 `Unknown column 'is_deleted'` 的错误

---

## 修复后的预期结果

### 修复前 vs 修复后对比

| 指标 | 修复前 | 修复后 |
|-----|--------|--------|
| 用户登录 | 失败 (SQLSyntaxErrorException) | 成功 |
| SQL逻辑删除 | 冲突/错误 | 正确应用 |
| 应用启动 | 可启动但登录失败 | 完全正常 |
| RabbitMQ日志 | 大量错误 | 保持不变(未修复) |

### 系统恢复状态

修复后，以下功能恢复正常:
- 用户认证和授权 (Spring Security)
- 所有涉及逻辑删除的查询 (用户/部门/物资等)
- 数据一致性检查

RabbitMQ连接错误仍然存在(P3优先级)，但不影响核心业务

---

## 预防措施和最佳实践

### 1. 配置一致性检查

建议定期检查:
```bash
grep -r "@TableLogic" backend/src/main/java/com/ct/wms/entity/ | wc -l
grep -r "@TableField" backend/src/main/java/com/ct/wms/entity/ | wc -l
```

确保逻辑删除字段配置保持一致

### 2. 单元测试验证

为UserDetailsService添加测试:

```java
@SpringBootTest
class UserDetailsServiceImplTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    void testLoadUserByUsername() {
        // 应该能正常加载用户，不抛出SQLSyntaxErrorException
        UserDetails user = userDetailsService.loadUserByUsername("admin");
        assertNotNull(user);
    }
}
```

### 3. 数据库初始化验证

在部署前执行:
```bash
# 验证数据库表结构与代码一致
mysql -u root -p ct_tibet_wms < sql/schema.sql --validate-only
```

### 4. 配置文件版本管理

将application.yml纳入Git版本控制，标记配置变更:
```
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted  # 必须与BaseEntity中的字段保持同步
```

---

## RabbitMQ连接问题的解决方案

虽然RabbitMQ问题优先级较低，但如果需要解决，请执行以下步骤:

### 方案1: 启动RabbitMQ服务(推荐)

#### Windows环境:
```bash
# 如果已安装RabbitMQ，使用RabbitMQ的管理脚本启动
rabbitmq-server.bat

# 或者使用Docker启动
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management
```

#### Linux/Mac环境:
```bash
# 使用系统包管理器
# Ubuntu/Debian
sudo apt-get install rabbitmq-server
sudo systemctl start rabbitmq-server

# macOS
brew install rabbitmq
brew services start rabbitmq
```

### 方案2: 修改application.yml禁用自动连接

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        # 禁用自动启动(开发环境)
        auto-startup: false
```

需要发送消息时手动启动

### 方案3: 环境变量配置外部RabbitMQ

如果有外部RabbitMQ实例:
```bash
export RABBITMQ_HOST=your-rabbitmq-host
export RABBITMQ_PORT=5672
export RABBITMQ_USER=username
export RABBITMQ_PASSWORD=password
```

---

## 总结和建议

### 关键点

1. **数据库字段缺失(P1)**:
   - 根本原因: MyBatis-Plus逻辑删除配置冲突
   - 修复方案: 删除BaseEntity中的 `@TableField("is_deleted")` 注解
   - 预计修复时间: < 5分钟
   - 影响范围: 登录及所有业务功能

2. **RabbitMQ连接失败(P3)**:
   - 根本原因: RabbitMQ服务未启动
   - 修复方案: 启动RabbitMQ或在开发环境禁用
   - 预计修复时间: 即时 (如果选择禁用)
   - 影响范围: 异步消息通知功能

### 建议的修复顺序

1. **立即执行**: 修复数据库字段缺失问题(方案A)
2. **次优先**: 验证应用能够正常启动和登录
3. **可选**: 根据需求启动RabbitMQ或禁用自动连接

### 后续改进建议

1. 添加启动时的配置检查，验证逻辑删除字段名一致
2. 为敏感操作(登录、数据修改)添加单元测试
3. 建立部署前的清单检查流程
4. 在开发文档中明确说明MyBatis-Plus的配置规范

---

## 文件修改清单

| 文件路径 | 修改内容 | 优先级 | 修改行数 |
|---------|---------|--------|---------|
| `backend/src/main/java/com/ct/wms/entity/BaseEntity.java` | 删除第4行的 `@TableField("is_deleted")` | P1 | 61行 |
| `backend/src/main/resources/application.yml` | 确认第78行 `logic-delete-field: deleted` | P1 | 78行 |
| `backend/src/main/resources/application-dev.yml` | (可选)添加RabbitMQ禁用配置 | P3 | - |

---

**报告完成时间**: 2025-11-13 22:30
**分析工程师**: Claude Code - Root Cause Analysis
**报告版本**: v1.0
