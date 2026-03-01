# 登录API 500错误修复指南

## 问题描述

登录API返回500错误：
```
Unknown column 'deleted' in 'field list'
SQL: SELECT id,dept_name,dept_code,parent_id,ancestors,leader_id,phone,email,status,sort,remark,create_time,update_time,create_by,update_by,deleted FROM tb_dept WHERE id=? AND deleted=0
```

## 根本原因

存在**字段名不一致问题**（字段映射错误）：

| 位置 | 使用的字段名 | 实际数据库列名 |
|------|----------|----------|
| Java实体 (BaseEntity.java) | `deleted` | - |
| MyBatis-Plus配置 (application.yml) | `deleted` | - |
| 数据库表 (schema.sql) | - | `is_deleted` |

### 问题详解

1. **BaseEntity定义** (`backend/src/main/java/com/ct/wms/entity/BaseEntity.java`)
   - 第67行：`@TableLogic private Integer deleted;`
   - 使用字段名：`deleted`（驼峰式）

2. **MyBatis-Plus全局配置** (`backend/src/main/resources/application.yml`)
   - 第92行：`logic-delete-field: deleted`
   - 配置期望的字段名：`deleted`

3. **数据库表结构** (`sql/schema.sql`)
   - tb_user 第31行：`is_deleted` TINYINT
   - tb_role 第53行：`is_deleted` TINYINT
   - tb_dept 第75行：`is_deleted` TINYINT
   - tb_material 第138行：`is_deleted` TINYINT
   - tb_warehouse 第160行：`is_deleted` TINYINT
   - **实际列名：`is_deleted`**

### 问题的工作流程

```
用户发起登录 → Controller调用Service → Service执行query
→ MyBatis-Plus拦截 → 尝试添加逻辑删除条件 (AND deleted=0)
→ SQL中生成的列名是 deleted → MySQL数据库中找不到 deleted 列（只有 is_deleted）
→ 返回 "Unknown column 'deleted'" 错误
```

## 修复方案（两选一）

### 方案A：推荐 - 数据库列改名为 deleted（符合Java命名规范）

#### 步骤1：备份数据库
```sql
-- 可选，但强烈建议
BACKUP DATABASE ct_tibet_wms TO DISK = '/backup/before_fix.bak';
```

#### 步骤2：执行修复SQL脚本
```bash
# 在MySQL中执行
mysql -u root -p ct_tibet_wms < H:\java\CT-Tibet-WMS\sql\fix_deleted_column.sql
```

该脚本会：
- 将 tb_user 的 is_deleted 改名为 deleted
- 将 tb_role 的 is_deleted 改名为 deleted
- 将 tb_dept 的 is_deleted 改名为 deleted
- 将 tb_menu 的 is_deleted 改名为 deleted
- 将 tb_material 的 is_deleted 改名为 deleted
- 将 tb_warehouse 的 is_deleted 改名为 deleted

#### 步骤3：验证修改
```sql
-- 检查是否还有 is_deleted 列
SELECT TABLE_NAME, COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME = 'is_deleted';
-- 应该返回空结果

-- 检查 deleted 列是否存在
SELECT TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME = 'deleted'
ORDER BY TABLE_NAME;
-- 应该返回5-6行结果
```

---

### 方案B：修改Java代码以适应现有数据库（不推荐）

如果不想修改数据库，可以修改Java代码。但这需要：

1. 在每个实体中显式配置列名映射：
```java
@TableLogic
@TableField(value = "is_deleted")  // 添加这一行
private Integer deleted;
```

2. 修改MyBatis-Plus配置：
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      # 注意：global-config 中的 logic-delete-field 必须是Java字段名，不是数据库列名
      # 列名映射靠 @TableField(value = "is_deleted") 完成
```

**不推荐**的原因：
- 需要在多个实体类中重复配置
- 违反DRY原则（Don't Repeat Yourself）
- 容易遗漏某些实体导致不一致

---

## 修复后的额外步骤

### 步骤4：清理应用缓存
```bash
# 1. 清除Hikari连接池（数据源会自动重新连接）
# - 停止后端应用

# 2. 清除MyBatis缓存
# - application.yml中已配置 cache-enabled: false，无需额外处理

# 3. 清除编译缓存
# - Windows: 删除 backend/target 目录
# - 或运行: mvn clean
```

### 步骤5：重新启动后端
```bash
cd H:\java\CT-Tibet-WMS\backend

# 方式1：使用Maven
mvn clean install
mvn spring-boot:run

# 方式2：直接运行编译后的jar
java -jar target/ct-tibet-wms-1.0.0.jar
```

### 步骤6：测试登录API
```bash
# 测试登录
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# 预期返回 200 OK，包含 token
```

---

## 验证清单

执行完毕后，检查以下项：

- [ ] SQL脚本执行无错误
- [ ] 数据库中 is_deleted 列已改名为 deleted（5-6张表）
- [ ] 后端应用已停止
- [ ] 执行 `mvn clean` 清除旧编译产物
- [ ] 后端应用已重启
- [ ] 登录API返回 200 OK（不再是 500）
- [ ] 登录成功返回 JWT token
- [ ] 其他API（如获取部门列表）正常工作

---

## 常见问题FAQ

### Q1: 为什么修改了表结构后还是报错？
**A:** 可能的原因：
1. SQL脚本未成功执行 → 检查数据库是否真的修改了
2. 后端缓存导致 → 确保执行了 `mvn clean` 并重新编译
3. 连接池未重置 → 检查是否重启了后端应用
4. 旧连接仍在使用 → 等待Hikari连接超时（默认30分钟）或重启MySQL

### Q2: 能否不重启后端，只修改数据库？
**A:** 否。原因：
- Hikari连接池可能缓存了表的元数据
- MyBatis可能缓存了SQL执行计划
- 必须 `mvn clean && mvn spring-boot:run` 重新启动

### Q3: 修改了列名会影响现有数据吗？
**A:** 否。`CHANGE COLUMN` 操作只改变列名，数据完全保留：
- 所有is_deleted=0的行依然是0
- 所有is_deleted=1的行依然是1
- 没有任何数据丢失

### Q4: 如何验证修复成功？
**A:** 按以下顺序测试：
1. 数据库级别：执行前面的SELECT验证查询
2. 代码级别：查看生成的SQL日志（已启用StdOutImpl）
3. API级别：调用登录API检查返回状态码
4. 功能级别：用返回的token调用其他API验证权限

### Q5: 还有其他表需要修改吗？
**A:** 检查是否有业务表也使用了 `is_deleted`：
```sql
SELECT TABLE_NAME, COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME LIKE '%delete%'
ORDER BY TABLE_NAME;
```

---

## 涉及的文件清单

### 核心代码文件
- `backend/src/main/java/com/ct/wms/entity/BaseEntity.java` - 基类定义
- `backend/src/main/resources/application.yml` - MyBatis-Plus配置
- `backend/src/main/java/com/ct/wms/config/MybatisPlusConfig.java` - 拦截器配置

### 数据库文件
- `sql/schema.sql` - 原始表结构（包含 is_deleted）
- `sql/fix_deleted_column.sql` - 修复脚本（改名为 deleted）

### 受影响的实体类
- `backend/src/main/java/com/ct/wms/entity/User.java`
- `backend/src/main/java/com/ct/wms/entity/Role.java`
- `backend/src/main/java/com/ct/wms/entity/Dept.java`
- `backend/src/main/java/com/ct/wms/entity/Material.java`
- `backend/src/main/java/com/ct/wms/entity/Warehouse.java`
- （所有继承BaseEntity的实体）

---

## 修复前后的SQL对比

### 修复前 - 报错
```sql
-- MyBatis-Plus生成的SQL（错误）
SELECT id,dept_name,dept_code,parent_id,ancestors,leader_id,phone,email,status,sort,remark,
       create_time,update_time,create_by,update_by,deleted  -- <- 问题：使用了 deleted
FROM tb_dept
WHERE id=? AND deleted=0  -- <- 问题：WHERE条件也用 deleted

-- 数据库实际表结构
-- ... is_deleted TINYINT ...  -- <- 实际列名是 is_deleted
```

### 修复后 - 正常
```sql
-- 修改数据库后，MySQL会自动识别正确的列名
ALTER TABLE tb_dept CHANGE COLUMN is_deleted deleted TINYINT ...

-- MyBatis-Plus生成的SQL（现在正确）
SELECT id,dept_name,dept_code,parent_id,ancestors,leader_id,phone,email,status,sort,remark,
       create_time,update_time,create_by,update_by,deleted  -- <- 现在正确对应
FROM tb_dept
WHERE id=? AND deleted=0  -- <- 现在正确对应

-- 数据库表结构
-- ... deleted TINYINT ...  -- <- 列名已改正
```

---

## 性能影响

修复操作对性能的影响：
- **表修改成本**：O(n)，需要重写整个表，但数据量小（仅几千条）
- **业务中断时间**：< 5秒（表锁持续时间短）
- **修复后性能**：零影响（列名改变不影响查询性能）

推荐：
- 在非业务高峰期执行（如晚上）
- 若有数据备份方案，先完整备份

---

## 回滚计划（如遇到问题）

如果修复出现问题，可以回滚：

```sql
-- 恢复原列名（如果修改失败）
ALTER TABLE `tb_user` CHANGE COLUMN `deleted` `is_deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_role` CHANGE COLUMN `deleted` `is_deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_dept` CHANGE COLUMN `deleted` `is_deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_menu` CHANGE COLUMN `deleted` `is_deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_material` CHANGE COLUMN `deleted` `is_deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_warehouse` CHANGE COLUMN `deleted` `is_deleted` TINYINT NOT NULL DEFAULT 0;

-- 然后重新部署旧版本代码
```

---

## 后续建议

1. **更新schema.sql**：修改原始脚本中的所有 `is_deleted` → `deleted`
2. **代码审查**：确保所有新增实体都遵循 `deleted` 命名规范
3. **文档更新**：在开发指南中明确规定逻辑删除字段必须命名为 `deleted`
4. **自动化测试**：添加单元测试验证逻辑删除功能

---

## 技术细节补充

### MyBatis-Plus逻辑删除工作原理

```java
// 在BaseEntity中
@TableLogic
@TableField(value = "deleted", exist = true)
private Integer deleted;

// MyBatis-Plus会：
// 1. 从application.yml读取 logic-delete-field: deleted
// 2. 从application.yml读取 logic-delete-value: 1 和 logic-not-delete-value: 0
// 3. 在执行SELECT/UPDATE/DELETE时自动添加条件 AND deleted=0
// 4. 在执行DELETE时改为 UPDATE tb_dept SET deleted=1 WHERE ...

// 问题所在：
// MyBatis-Plus使用Java字段名 'deleted' 生成SQL
// 但没有找到对应的数据库列 'deleted'（只有 'is_deleted'）
// 导致SQL语法错误
```

### 列名映射的两种方式

方式1（推荐）：数据库列名与Java字段名相同
```java
// BaseEntity.java
@TableLogic
private Integer deleted;  // Java字段名
```
```sql
-- schema.sql
`deleted` TINYINT  -- 数据库列名相同
```

方式2：使用@TableField显式映射
```java
// 某实体.java
@TableLogic
@TableField(value = "is_deleted")  // 显式指定数据库列名
private Integer deleted;  // Java字段名不同
```
```sql
-- schema.sql
`is_deleted` TINYINT  -- 数据库列名不同
```

本项目使用方式1，所以必须修改数据库列名为 `deleted`。

---

## 相关日志位置

修复后，查看以下日志验证：

```
后端日志输出：
- 显示的SQL应该包含 `deleted` 列（不是 `is_deleted`）
- SELECT ... WHERE ... AND deleted=0
- UPDATE ... SET deleted=1 WHERE ...

日志文件位置：
- H:\java\CT-Tibet-WMS\backend\logs\sql.log - SQL语句日志
- H:\java\CT-Tibet-WMS\backend\logs\all.log - 全部日志
- H:\java\CT-Tibet-WMS\backend\logs\debug-for-claude.log - 调试日志
```

---

**修复完成后，整个系统应该可以正常工作。若还有问题，请检查上述清单。**
