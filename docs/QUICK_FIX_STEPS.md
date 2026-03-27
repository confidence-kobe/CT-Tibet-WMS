# 登录API 500错误 - 快速修复步骤

## 错误症状
```
Unknown column 'deleted' in 'field list'
SQL: SELECT ... FROM tb_dept WHERE id=? AND deleted=0
```

## 根本原因（一句话）
**数据库使用 `is_deleted` 列，但Java代码和MyBatis-Plus配置期望的是 `deleted` 列**

---

## 修复步骤（立即执行）

### 第1步：停止后端应用
```bash
# 在你运行后端的终端中按 Ctrl+C 停止
# 或杀死进程
# Windows: taskkill /F /IM java.exe
# Linux: pkill java
```

### 第2步：执行数据库修复脚本
在MySQL中执行以下SQL：

```sql
-- 连接到数据库
USE ct_tibet_wms;

-- 修改所有表的列名
ALTER TABLE `tb_user` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_role` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_dept` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_menu` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_material` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0;
ALTER TABLE `tb_warehouse` CHANGE COLUMN `is_deleted` `deleted` TINYINT NOT NULL DEFAULT 0;

-- 验证修复成功
SELECT TABLE_NAME, COLUMN_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME = 'deleted'
ORDER BY TABLE_NAME;
-- 预期输出：6行，分别是 tb_user, tb_role, tb_dept, tb_menu, tb_material, tb_warehouse
```

**执行方式选择其一：**

**方式A：MySQL命令行**
```bash
mysql -u root -p ct_tibet_wms < sql/fix_deleted_column.sql
# 输入密码：root（或你的密码）
```

**方式B：MySQL Workbench或DBeaver**
- 打开SQL编辑器
- 复制上面的SQL语句粘贴
- 执行 (Ctrl+Enter 或点击 Execute)

**方式C：使用修复脚本文件**
```bash
# 脚本已在项目根目录
# Windows
mysql -u root -p -e "source sql/fix_deleted_column.sql"

# Linux/Mac
mysql -u root -p < sql/fix_deleted_column.sql
```

### 第3步：验证数据库修复
```sql
-- 验证 is_deleted 已删除
SELECT * FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME = 'is_deleted';
-- 预期：返回空（0行）

-- 验证 deleted 已创建
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ct_tibet_wms' AND COLUMN_NAME = 'deleted'
ORDER BY TABLE_NAME;
-- 预期：返回6行（tb_user, tb_role, tb_dept, tb_menu, tb_material, tb_warehouse）
```

### 第4步：清理后端缓存
```bash
cd H:\java\CT-Tibet-WMS\backend

# 清除Maven编译产物
mvn clean

# 或手动删除
# Windows: 删除 target 文件夹
# Linux/Mac: rm -rf target/
```

### 第5步：重新启动后端
```bash
cd H:\java\CT-Tibet-WMS\backend

# 方式1：Maven（推荐开发时使用）
mvn spring-boot:run

# 方式2：完整构建后运行
mvn clean install
java -jar target/ct-tibet-wms-1.0.0.jar

# 方式3：使用IDEs运行（如IntelliJ IDEA）
# - 右键 Application.java
# - 选择 Run 'Application.main()'
```

**等待输出：**
```
... Started Application in ... seconds
Tomcat started on port(s): 48888 (http) with context path '/'
```

### 第6步：测试登录API
```bash
# 使用curl测试
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 或使用Postman
# POST http://localhost:48888/api/auth/login
# Body (JSON):
# {
#   "username": "admin",
#   "password": "admin123"
# }
```

**预期响应（成功）：**
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      ...
    }
  }
}
```

**如果还是500错误：**
- 检查是否正确执行了SQL修复
- 检查是否执行了 `mvn clean`
- 检查是否正确重启了后端
- 查看后端日志输出中的错误信息
- 参考完整修复指南：`LOGIN_API_500_FIX_GUIDE.md`

---

## 完成验证清单

完成上述步骤后，检查以下项目：

- [ ] MySQL 中 `is_deleted` 列已改名为 `deleted`（6张表）
- [ ] 后端成功启动，无错误
- [ ] 登录API返回 200 OK（不再是 500）
- [ ] 登录成功返回有效的JWT token
- [ ] 用token可以访问其他API（如 `/api/user/profile`）

---

## 常见问题速查

| 问题 | 解决方案 |
|------|--------|
| SQL修复后重启仍然报错 | 确保执行了 `mvn clean`，删除了 target 目录 |
| 找不到MySQL命令 | 需要MySQL安装并添加到PATH环境变量 |
| 修改列名时报权限错误 | 确保用户（root）有修改表的权限 |
| 修改列名时说列不存在 | 说明列已经改过了，无需再改（可能之前已修复） |
| 后端启动失败 | 检查是否有多个实例运行在同一个端口，或检查数据库连接配置 |

---

## 文件清单

项目中与此修复相关的文件：

```
H:\java\CT-Tibet-WMS\
├── sql/
│   ├── fix_deleted_column.sql          # 修复脚本（主要）
│   ├── diagnose_deleted_field.sql      # 诊断脚本（验证状态）
│   └── schema.sql                      # 原始表结构（已更新）
├── LOGIN_API_500_FIX_GUIDE.md          # 详细指南（本文档）
├── QUICK_FIX_STEPS.md                  # 快速步骤（即本文件）
└── backend/
    ├── src/main/java/com/ct/wms/entity/BaseEntity.java  # 定义deleted字段
    ├── src/main/resources/application.yml               # MyBatis配置
    └── target/                                          # 需要删除（mvn clean）
```

---

## 预期时间

- 第1-3步（停止应用+执行SQL）：2-5分钟
- 第4步（清理缓存）：1分钟
- 第5步（重启应用）：10-30秒
- 第6步（测试）：1分钟

**总计：约5-10分钟**

---

## 如果需要帮助

1. 查看详细指南：`LOGIN_API_500_FIX_GUIDE.md`
2. 检查后端日志：`backend/logs/all.log` 或 `backend/logs/sql.log`
3. 运行诊断脚本：`diagnose_deleted_field.sql`
4. 检查配置文件：`backend/src/main/resources/application.yml`

---

**修复完成后，登录API应该正常工作！**
