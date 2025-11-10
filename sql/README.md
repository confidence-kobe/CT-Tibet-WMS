# SQL脚本说明

## 文件列表

| 文件名 | 说明 | 执行顺序 |
|--------|------|---------|
| `create_database.sql` | 创建数据库 | 1 |
| `schema.sql` | 创建表结构（17张表） | 2 |
| `init_data.sql` | 初始化基础数据 | 3 |

## 执行步骤

### 方式一：命令行执行

```bash
# 1. 创建数据库
mysql -u root -p < create_database.sql

# 2. 创建表结构
mysql -u root -p ct_tibet_wms < schema.sql

# 3. 初始化数据
mysql -u root -p ct_tibet_wms < init_data.sql
```

### 方式二：MySQL客户端执行

```sql
-- 1. 在MySQL客户端中执行
SOURCE /path/to/create_database.sql;
SOURCE /path/to/schema.sql;
SOURCE /path/to/init_data.sql;
```

### 方式三：可视化工具执行

使用 Navicat、MySQL Workbench 等工具，依次打开并执行上述SQL文件。

## 初始账号

执行完成后，系统会创建以下测试账号：

| 用户名 | 密码 | 角色 | 所属部门 |
|--------|------|------|---------|
| admin | 123456 | 系统管理员 | 西藏电信 |
| wl_admin | 123456 | 部门管理员 | 网络运维部 |
| wl_warehouse | 123456 | 仓库管理员 | 网络运维部 |
| wl_user1 | 123456 | 普通员工 | 网络运维部 |
| wl_user2 | 123456 | 普通员工 | 网络运维部 |

**注意**：首次登录会要求修改密码（除admin外）。

## 初始数据

- **部门**：8个（根部门 + 7个业务部门）
- **角色**：4个（系统管理员、部门管理员、仓库管理员、普通员工）
- **用户**：5个测试账号
- **物资**：20个（光缆、设备、配件、工具）
- **仓库**：7个（每个业务部门一个仓库）
- **库存**：网络运维部仓库的初始库存（20种物资）
- **菜单**：系统基础菜单（7个一级菜单，多个二级菜单）

## 表结构说明

### 系统管理模块（5张表）
- `tb_user` - 用户表
- `tb_role` - 角色表
- `tb_dept` - 部门表
- `tb_menu` - 菜单表
- `tb_role_menu` - 角色菜单关联表

### 基础数据模块（2张表）
- `tb_material` - 物资表
- `tb_warehouse` - 仓库表

### 业务数据模块（8张表）
- `tb_inbound` - 入库单表
- `tb_inbound_detail` - 入库明细表
- `tb_outbound` - 出库单表
- `tb_outbound_detail` - 出库明细表
- `tb_apply` - 申请单表
- `tb_apply_detail` - 申请明细表
- `tb_inventory` - 库存表
- `tb_inventory_log` - 库存流水表

### 消息通知模块（1张表）
- `tb_message` - 消息表

### 系统日志模块（2张表）
- `tb_operation_log` - 操作日志表
- `tb_login_log` - 登录日志表

## 数据库配置建议

```ini
[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
default-time-zone = '+08:00'
default-storage-engine = InnoDB
innodb_buffer_pool_size = 2G
max_connections = 500
```

## 验证安装

执行以下SQL验证数据库是否正确创建：

```sql
-- 检查数据库
SHOW DATABASES LIKE 'ct_tibet_wms';

-- 检查表数量（应该是17张表）
USE ct_tibet_wms;
SELECT COUNT(*) AS table_count FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'ct_tibet_wms';

-- 检查数据
SELECT '部门' AS type, COUNT(*) AS count FROM tb_dept
UNION ALL SELECT '角色', COUNT(*) FROM tb_role
UNION ALL SELECT '用户', COUNT(*) FROM tb_user
UNION ALL SELECT '物资', COUNT(*) FROM tb_material
UNION ALL SELECT '仓库', COUNT(*) FROM tb_warehouse
UNION ALL SELECT '库存', COUNT(*) FROM tb_inventory;
```

预期结果：
```
部门: 8
角色: 4
用户: 5
物资: 20
仓库: 7
库存: 20
```

## 常见问题

### Q1: 执行脚本时报错 "Unknown database"
**A**: 确保先执行 `create_database.sql` 创建数据库。

### Q2: 中文显示乱码
**A**: 检查数据库字符集是否为 utf8mb4，客户端连接时也要指定 utf8mb4。

### Q3: 密码加密值如何生成？
**A**: 使用 BCrypt 算法加密，密码 "123456" 的加密值为：
```
$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
```

### Q4: 如何重置数据库？
**A**: 执行以下命令：
```sql
DROP DATABASE IF EXISTS ct_tibet_wms;
```
然后重新执行所有脚本。

## 备份建议

```bash
# 备份整个数据库
mysqldump -u root -p ct_tibet_wms > ct_tibet_wms_backup.sql

# 只备份结构（不含数据）
mysqldump -u root -p --no-data ct_tibet_wms > ct_tibet_wms_schema.sql

# 只备份数据（不含结构）
mysqldump -u root -p --no-create-info ct_tibet_wms > ct_tibet_wms_data.sql
```

## 相关文档

- [数据库设计文档](../docs/数据库设计文档.md)
- [PRD产品需求文档](../docs/PRD-产品需求文档.md)
