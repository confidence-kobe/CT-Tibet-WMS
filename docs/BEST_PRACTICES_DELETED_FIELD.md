# MyBatis-Plus 逻辑删除字段最佳实践

## 背景

项目曾因为数据库列名（`is_deleted`）与Java字段名（`deleted`）不一致导致登录API返回500错误。本文档规范未来的开发，防止类似问题。

---

## 规范定义

### 1. 字段命名规范

**所有实体的逻辑删除字段必须统一命名为 `deleted`**

| 层次 | 字段/列名 | 类型 | 值域 |
|------|---------|------|------|
| **Java代码** | `deleted` | `Integer` | 0=未删除, 1=已删除 |
| **数据库列** | `deleted` | `TINYINT` | 0=未删除, 1=已删除 |
| **配置文件** | `logic-delete-field: deleted` | - | Java字段名 |

### 2. 配置要求

在 `application.yml` 中**必须**保持以下配置：

```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted          # Java字段名（驼峰式）
      logic-delete-value: 1                 # 删除时的值
      logic-not-delete-value: 0            # 非删除时的值
```

### 3. Java实体编码规范

```java
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class MyEntity extends BaseEntity {
    // ... 其他字段 ...

    // 方式1：继承自BaseEntity（推荐）
    // BaseEntity中已定义：
    // @TableLogic
    // private Integer deleted;
    // 无需在子类中再次定义

    // 方式2：如果需要在非BaseEntity的类中定义
    @TableLogic
    @TableField(value = "deleted")  // 显式指定列名（可选，因为列名与字段名相同）
    private Integer deleted;
}
```

### 4. 数据库表创建规范

```sql
-- 正确示例
CREATE TABLE `tb_example` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50),
    -- ... 其他列 ...
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 错误示例（曾经的问题）
-- CREATE TABLE `tb_example` (
--     ...
--     `is_deleted` TINYINT NOT NULL DEFAULT 0,  -- 错误：与Java字段名不一致
--     ...
-- );
```

---

## 实现检查清单

新增实体或修改现有实体时：

### 创建新实体时
- [ ] 继承自 `BaseEntity`（已包含 `deleted` 字段）
- [ ] 不在子类中重复定义 `deleted` 字段
- [ ] 在 `@TableName` 注解中指定正确的表名

### 修改现有表结构时
- [ ] 如果没有 `deleted` 列，执行迁移脚本添加：
  ```sql
  ALTER TABLE `tb_xxx` ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0;
  ```
- [ ] 确保列名**必须是** `deleted`，不能是 `is_deleted` 或其他变体

### 执行数据库迁移时
- [ ] 在 `sql/` 目录创建迁移脚本
- [ ] 脚本文件命名：`migrate_YYYYMMDD_description.sql`
- [ ] 脚本中添加列名验证查询
- [ ] 记录在项目的迁移历史中

---

## 代码示例

### 正确的实体类
```java
package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_product")  // 正确的表名
public class Product extends BaseEntity {
    // BaseEntity已提供：
    // - id
    // - created_time / createTime
    // - updated_time / updateTime
    // - created_by / createBy
    // - updated_by / updateBy
    // - deleted （逻辑删除字段）

    private String productName;
    private String productCode;
    private BigDecimal price;

    // 不需要再定义 deleted 字段！
}
```

### 正确的数据库表
```sql
CREATE TABLE `tb_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `product_code` VARCHAR(50) NOT NULL COMMENT '产品编码',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格',

    -- 标准审计字段（BaseEntity中的字段）
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 逻辑删除字段（必须命名为 deleted）
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除 1-已删除)',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code` (`product_code`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';
```

---

## MyBatis-Plus 逻辑删除的工作原理

理解这个原理有助于避免配置错误：

```java
// 1. 定义逻辑删除字段
@Data
public class BaseEntity {
    @TableLogic  // 告诉MyBatis-Plus这是逻辑删除字段
    private Integer deleted;  // Java字段名
}

// 2. 全局配置
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted  // 指定Java字段名
      logic-delete-value: 1        // 删除时设为1
      logic-not-delete-value: 0    // 查询时过滤条件用0

// 3. 运行时行为
// 用户代码：
userMapper.delete(queryWrapper);  // 调用delete

// MyBatis-Plus自动转换为：
// UPDATE tb_user SET deleted=1 WHERE ... (逻辑删除，不真正删除行)
// 而不是：
// DELETE FROM tb_user WHERE ...   (物理删除)

// 用户代码：
userMapper.selectList(queryWrapper);  // 调用select

// MyBatis-Plus自动转换为：
// SELECT * FROM tb_user WHERE ... AND deleted=0 (自动添加条件)

// 所以 Java字段名 'deleted' 必须能够映射到数据库列名 'deleted'
// 否则会报：Unknown column 'deleted'
```

---

## 迁移现有表

如果要将现有表从 `is_deleted` 迁移到 `deleted`：

### 步骤1：创建迁移脚本
```sql
-- sql/migrate_20250101_rename_is_deleted_to_deleted.sql
ALTER TABLE `tb_example` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0;
```

### 步骤2：记录迁移
```markdown
<!-- MIGRATION_LOG.md -->
## Migration: 2025-01-01 - Rename is_deleted to deleted

**Reason**: 统一逻辑删除字段名，解决MyBatis-Plus配置不一致问题

**Affected Tables**:
- tb_user
- tb_role
- tb_dept
- ...

**Execution**:
mysql -u root -p ct_tibet_wms < sql/migrate_20250101_rename_is_deleted_to_deleted.sql

**Rollback** (if needed):
ALTER TABLE `tb_example` CHANGE COLUMN `deleted` `is_deleted` TINYINT NOT NULL DEFAULT 0;
```

### 步骤3：更新Java代码
- 更新对应的实体类（继承BaseEntity，不需要修改字段）
- 如果有显式的 `@TableField("is_deleted")` 注解，改为 `@TableField("deleted")` 或移除

---

## 诊断和验证

### 项目启动时的检查清单

```bash
# 1. 检查MyBatis-Plus配置
grep -n "logic-delete-field" backend/src/main/resources/application.yml
# 预期输出：logic-delete-field: deleted

# 2. 检查BaseEntity
grep -n "@TableLogic" backend/src/main/java/com/ct/wms/entity/BaseEntity.java
# 预期输出：@TableLogic 注解在某处

# 3. 检查所有实体是否继承BaseEntity
grep -r "extends BaseEntity" backend/src/main/java/com/ct/wms/entity/
# 预期输出：所有业务实体都继承BaseEntity

# 4. 检查是否有其他地方定义了deleted字段
grep -r "private Integer deleted" backend/src/main/java/com/ct/wms/entity/
# 预期输出：只在BaseEntity中定义，子类中不应出现
```

### 运行时的SQL日志检查

```sql
-- 启用SQL日志输出（application.yml中已配置）
log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

-- 查看生成的SQL中是否包含 deleted 列（不是 is_deleted）
-- 示例（正确）：
-- SELECT ... WHERE ... AND deleted=0

-- 示例（错误）：
-- SELECT ... WHERE ... AND is_deleted=0  （会报错）
```

---

## 常见错误及解决方案

### 错误1：在子类中重复定义deleted字段
```java
// 错误做法
@Data
public class User extends BaseEntity {
    private String username;

    @TableLogic  // 错误：BaseEntity中已定义
    private Integer deleted;
}

// 正确做法
@Data
public class User extends BaseEntity {
    private String username;
    // deleted 继承自 BaseEntity，无需定义
}
```

### 错误2：数据库列名与Java字段名不一致
```java
// Java代码
@TableLogic
@TableField(value = "is_deleted")  // 指向数据库列名
private Integer deleted;            // Java字段名

// 数据库表（当前问题的根源）
CREATE TABLE (
    `deleted` TINYINT,  // 列名是 deleted
    // 而Java期望的 is_deleted 不存在
    // 导致 Unknown column 错误
);

// 解决方案：保持一致
// 方案A（推荐）：改数据库列为 deleted
ALTER TABLE `tb_xxx` CHANGE COLUMN `is_deleted` `deleted` TINYINT ...;

// 方案B：改Java代码为 is_deleted（不推荐，违反驼峰式规范）
@TableLogic
@TableField(value = "is_deleted")
private Integer isDeleted;  // 改为驼峰式
```

### 错误3：全局配置指向错误的字段名
```yaml
# 错误
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: is_deleted  # 应该是Java字段名，不是数据库列名

# 正确
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted  # Java字段名（驼峰式）
```

---

## 相关文件位置

| 文件 | 用途 | 备注 |
|------|------|------|
| `backend/src/main/java/com/ct/wms/entity/BaseEntity.java` | 定义deleted字段 | 所有实体的基类 |
| `backend/src/main/resources/application.yml` | MyBatis-Plus配置 | 全局逻辑删除设置 |
| `sql/schema.sql` | 数据库表结构 | 原始建表脚本 |
| `sql/fix_deleted_column.sql` | 修复脚本 | 修正字段名 |
| `sql/migrate_*.sql` | 迁移脚本 | 版本更新脚本 |

---

## 团队遵循承诺

新增实体或修改表结构时，开发者需要：

1. [ ] **使用BaseEntity**作为所有实体的基类
2. [ ] **不重复定义**逻辑删除字段
3. [ ] **数据库列名统一为** `deleted`
4. [ ] **验证配置**：运行诊断脚本确保一致
5. [ ] **记录迁移**：新增表结构变更时在迁移日志中记录

---

## 参考资源

- [MyBatis-Plus 官方文档 - 逻辑删除](https://baomidou.com/pages/6b03c5/)
- [本项目修复指南](./LOGIN_API_500_FIX_GUIDE.md)
- [快速修复步骤](./QUICK_FIX_STEPS.md)

---

**遵循本规范，可以完全避免字段名不一致导致的问题。**
