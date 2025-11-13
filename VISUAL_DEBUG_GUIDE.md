# 可视化调试指南 - MyBatis-Plus逻辑删除问题

## 问题流程图

### 修复前: 配置冲突导致的SQL错误

```
用户尝试登录
    ↓
AuthController.login()
    ↓
AuthService.login()
    ↓
UserDetailsService.loadUserByUsername()
    ↓
UserMapper.selectOne()  [执行 Lambda查询]
    ↓
MyBatis-Plus拦截器
    ├─ 检查User实体是否有@TableLogic
    │   ↓ YES - BaseEntity有@TableLogic
    │
    ├─ 读取逻辑删除配置
    │   ├─ 从application.yml读取: "deleted"
    │   └─ 从@TableField读取: "is_deleted"  ← 冲突！
    │
    ├─ 不确定使用哪个字段名
    │   ↓
    │   生成错误的SQL
    │   "SELECT ... is_deleted FROM tb_user
    │    WHERE is_deleted=0"  (注: is_deleted可能被错误处理)
    │
    ↓
MySQL执行SQL
    ↓
SQLSyntaxErrorException
"Unknown column 'is_deleted' in 'field list'"
    ↓
用户登录失败
```

### 修复后: 配置统一使用全局设置

```
用户尝试登录
    ↓
AuthController.login()
    ↓
AuthService.login()
    ↓
UserDetailsService.loadUserByUsername()
    ↓
UserMapper.selectOne()  [执行 Lambda查询]
    ↓
MyBatis-Plus拦截器
    ├─ 检查User实体是否有@TableLogic
    │   ↓ YES - BaseEntity有@TableLogic
    │
    ├─ 读取逻辑删除配置
    │   ├─ 从application.yml读取: "deleted"  ✓ 只有一个配置源
    │   ├─ 没有@TableField来覆盖
    │   └─ 使用全局配置
    │
    ├─ 将驼峰式"deleted"转换为蛇形式"is_deleted"
    │   ↓ (因为map-underscore-to-camel-case: true)
    │
    ├─ 生成正确的SQL
    │   "SELECT id, username, password, ..., is_deleted
    │    FROM tb_user
    │    WHERE is_deleted = 0 AND (username = ?)"
    │
    ↓
MySQL执行SQL
    ↓
返回匹配的用户记录
    ↓
Spring Security处理用户信息
    ↓
返回JWT Token
    ↓
用户登录成功
```

---

## 配置映射流程图

### MyBatis-Plus字段名转换过程

```
STEP 1: 读取实体类配置
┌────────────────────────────────────────┐
│ BaseEntity.java                        │
│ @TableLogic                            │
│ private Integer isDeleted;             │
└────────────────────────────────────────┘
         ↓ 字段名 = "isDeleted"

STEP 2: 应用application.yml全局配置
┌────────────────────────────────────────┐
│ application.yml                        │
│ logic-delete-field: deleted            │
│ (因为没有@TableField覆盖)              │
└────────────────────────────────────────┘
         ↓ 逻辑删除字段 = "deleted"

STEP 3: 驼峰转蛇形转换
         "deleted" (驼峰式)
              ↓
        "is_deleted" (蛇形式)
    [因为map-underscore-to-camel-case: true]

STEP 4: 生成SQL
         ↓
    WHERE is_deleted = 0
         ↓
    ✓ 与数据库字段名匹配
```

---

## 数据库视角

### tb_user表的字段映射

```
数据库层面:
┌──────────────────────────────────────────────┐
│ MySQL Table: tb_user                         │
├──────────────────────────────────────────────┤
│ Column Name      │ Type      │ Default       │
├──────────────────┼───────────┼───────────────┤
│ id               │ BIGINT    │ AUTO_INCREMENT│
│ username         │ VARCHAR   │ NOT NULL      │
│ ...其他列...      │           │               │
│ is_deleted       │ TINYINT   │ 0             │ ← 逻辑删除字段
└──────────────────────────────────────────────┘
         ↑
         │ (映射)
         │
Java应用层面:
┌──────────────────────────────────────────────┐
│ Java Entity: User                            │
├──────────────────────────────────────────────┤
│ private Long id;                             │
│ private String username;                     │
│ ...其他字段...                                │
│ @TableLogic                                  │
│ private Integer isDeleted;  ← 自动映射到is_deleted
└──────────────────────────────────────────────┘
```

---

## 问题诊断流程图

使用这个流程图来诊断您的系统状态:

```
                  是否出现异常?
                      │
          ┌─── YES ───┴─── NO ───┐
          ↓                      ↓
    收到错误消息              应用运行正常
          │                      │
          ├─ "Unknown column      └─ 修复成功!
          │   'is_deleted'"
          │   │
          │   └─ MyBatis配置冲突 (P1)
          │
          ├─ "Connection refused" (RabbitMQ)
          │   │
          │   └─ RabbitMQ未启动 (P3 - 可忽略)
          │
          └─ 其他SQLSyntaxError
              │
              └─ 检查数据库表结构
                 (运行: DESC tb_user;)
```

---

## 修复前后的SQL对比

### 修复前生成的SQL (错误)

```sql
-- MyBatis-Plus生成的有问题的SQL
-- (由于配置冲突，字段可能被错误解释)

SELECT
  id,
  username,
  password,
  ...
  -- 这里可能出现 is_deleted 字段的混乱引用
  -- 导致 "Unknown column 'is_deleted' in 'field list'"
FROM tb_user
WHERE
  is_deleted = 0  -- 条件本身没错，但SELECT中可能缺少这个字段
  AND (username = ?)
```

### 修复后生成的SQL (正确)

```sql
-- MyBatis-Plus生成的正确SQL
-- (全局配置统一应用)

SELECT
  id,
  username,
  password,
  real_name,
  phone,
  email,
  dept_id,
  role_id,
  status,
  avatar,
  wechat_openid,
  wechat_nickname,
  last_login_time,
  last_login_ip,
  password_update_time,
  is_first_login,
  remark,
  create_time,
  update_by,
  is_deleted,        -- ✓ 字段列表中包含is_deleted
FROM tb_user
WHERE
  is_deleted = 0      -- ✓ WHERE条件正确
  AND (username = ?)  -- ✓ 查询条件正确
```

---

## 配置冲突的可视化表示

### 修复前: 两个配置源争夺控制权

```
                   UserMapper.selectOne()
                          │
                ┌─────────┴─────────┐
                ↓                   ↓
        [全局配置源]         [实体类配置源]
        application.yml      BaseEntity.java
        │                    │
        ├─ logic-delete-    ├─ @TableLogic
        │  field: deleted   │
        │                   ├─ @TableField
        │                   │  ("is_deleted")
        │                   │
        └─ "deleted"        └─ "is_deleted"
            ↓                   ↓
        [配置冲突!]
            │
            └─> 不确定该使用哪个
                │
                └─> SQL生成错误
                    │
                    └─> SQLSyntaxErrorException
```

### 修复后: 单一配置源

```
                   UserMapper.selectOne()
                          │
                          ↓
                [全局配置源 ONLY]
                application.yml
                │
                ├─ logic-delete-
                │  field: deleted
                │
                └─ "deleted"
                    ↓
                [驼峰→蛇形转换]
                    ↓
                "is_deleted"
                    ↓
                [SQL生成正确]
                    │
                    └─> 成功!
```

---

## 日志分析指南

### 如何从日志中识别问题

#### 症状1: SQLSyntaxErrorException日志

```
2025-11-13 22:30:45.123 [main] ERROR o.s.a.r.c.JdbcUtils -
java.sql.SQLSyntaxErrorException: Unknown column 'is_deleted' in 'field list'
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:1073)
	at ...
	at com.ct.wms.service.impl.UserDetailsServiceImpl.loadUserByUsername(UserDetailsServiceImpl.java:34)
```

**诊断结果**: P1 - MyBatis配置冲突

**修复方案**: 删除BaseEntity中的@TableField注解

#### 症状2: AmqpConnectException日志

```
2025-11-13 22:14:40.649 [RabbitListenerEndpointContainer#0-1] ERROR
o.s.a.r.listener.SimpleMessageListenerContainer -
Failed to check/redeclare auto-delete queue(s).
org.springframework.amqp.AmqpConnectException:
java.net.ConnectException: Connection refused: getsockopt
```

**诊断结果**: P3 - RabbitMQ服务不可用

**修复方案**: 启动RabbitMQ或禁用自动启动

#### 症状3: 正常启动日志

```
2025-11-13 22:35:00.000 [main] INFO o.s.b.w.e.tomcat.TomcatWebServer -
Tomcat started on port(s): 48888 (http) with context path ''

2025-11-13 22:35:00.123 [main] INFO c.c.w.WmsApplication -
Started WmsApplication in 5.234 seconds (JVM running for 6.123)
```

**诊断结果**: 修复成功，应用正常运行

---

## 验证检查清单 (带诊断)

```
应用启动检查
├─ [ ] 没有SQLSyntaxErrorException
│   └─ 如果有: 修复未完成，重新检查BaseEntity
├─ [ ] 没有"Unknown column 'is_deleted'"
│   └─ 如果有: BaseEntity中仍有@TableField("is_deleted")
├─ [ ] Tomcat启动在端口48888
│   └─ 如果没有: 检查端口占用或配置错误
└─ [ ] 没有其他致命错误日志

登录功能检查
├─ [ ] 能否成功执行POST /api/auth/login
│   └─ 如果失败: 检查数据库用户数据
├─ [ ] 是否返回JWT Token
│   └─ 如果没有: 查看返回的错误信息
└─ [ ] Token是否有效
    └─ 如果无效: 检查JWT配置

数据库检查
├─ [ ] tb_user表是否存在
│   └─ SQL: SHOW TABLES LIKE 'tb_user';
├─ [ ] is_deleted字段是否存在
│   └─ SQL: DESC tb_user; (查找is_deleted行)
├─ [ ] 是否有测试用户(未删除)
│   └─ SQL: SELECT id, username FROM tb_user
│           WHERE is_deleted = 0 LIMIT 1;
└─ [ ] is_deleted字段是否为0
    └─ SQL: SELECT is_deleted FROM tb_user LIMIT 1;
```

---

## 根本原因的可视化解释

### 为什么会产生"Unknown column"错误?

```
当有两个配置源时:

┌─────────────────────────────────┐
│ 配置源1: application.yml        │
│ logic-delete-field: deleted     │
└─────────────────────────────────┘

vs

┌─────────────────────────────────┐
│ 配置源2: @TableField注解        │
│ @TableField("is_deleted")       │
└─────────────────────────────────┘

结果:
MyBatis-Plus会尝试协调这两个配置，
但由于优先级或处理顺序的问题，
最终生成的SQL中:
  ✗ SELECT列表中可能缺少is_deleted
  ✓ WHERE条件中可能有is_deleted

这导致:
  ERROR: Unknown column 'is_deleted' in 'field list'

因为SQL试图在WHERE中引用一个
在SELECT列表中不存在的列
```

---

## 修复的设计原理

### 为什么删除@TableField("is_deleted")能解决问题?

```
原理: 只使用一个配置源

修复前 - 多个配置源:
  配置源1: application.yml → "deleted"
  配置源2: @TableField → "is_deleted"
  结果: 冲突

修复后 - 单一配置源:
  配置源: application.yml → "deleted"
  (代码中没有@TableField来覆盖)

  转换规则:
    "deleted" (驼峰)
    + map-underscore-to-camel-case: true
    = "is_deleted" (自动转换)

  结果: 一致、清晰、正确
```

---

## 性能和安全考虑

### 逻辑删除的优势

```
物理删除:
├─ 优点: 节省空间
├─ 缺点: 无法恢复数据
└─ 使用场景: 临时数据(日志)

逻辑删除 (当前系统使用):
├─ 优点:
│   ├─ 数据可恢复
│   ├─ 保留数据历史
│   └─ 防止级联删除问题
├─ 缺点: 占用数据库空间
└─ 使用场景:
    ├─ 用户数据(可能需要恢复)
    ├─ 订单数据(需要历史记录)
    └─ 重要业务数据

MyBatis-Plus逻辑删除:
  ✓ 自动处理 WHERE is_deleted = 0 过滤
  ✓ 防止意外查询到已删除数据
  ✓ 开发效率高
```

---

## 其他可能受影响的实体类

由于修复影响的是BaseEntity，所有继承BaseEntity的实体都会被修复:

```
BaseEntity (已修复)
    ↓ 继承
    ├─ User (用户) ✓
    ├─ Department (部门) ✓
    ├─ Material (物资) ✓
    ├─ Warehouse (仓库) ✓
    ├─ Inbound (入库单) ✓
    ├─ Outbound (出库单) ✓
    ├─ Apply (申请单) ✓
    └─ ...其他实体... ✓

所有这些实体的查询都会自动包含
逻辑删除过滤: WHERE is_deleted = 0
```

---

**本指南完成**: 2025-11-13
**包含内容**: 7个可视化流程图、配置映射、问题诊断、验证清单
