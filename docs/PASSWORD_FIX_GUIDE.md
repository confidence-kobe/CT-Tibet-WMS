# 密码问题修复指南

**问题**: 使用默认密码 `123456` 无法登录系统
**原因**: 数据库中的BCrypt密码hash值不正确
**状态**: ✅ 已修复

---

## 问题分析

### 原因
BCrypt是一种单向加密算法，原SQL脚本中使用的hash值可能不是通过标准BCrypt生成的，导致Spring Security验证失败。

### BCrypt特点
- **单向加密**: 无法解密，只能验证
- **每次不同**: 相同密码每次生成的hash都不同
- **自动验证**: Spring Security会自动使用BCrypt验证

### 问题症状
```
输入: admin / 123456
错误: 密码错误
日志: Authentication failed
```

---

## 解决方案

### 方案1: 快速修复 - 运行修复脚本 ⭐推荐

#### Windows系统
```bash
cd sql
fix_password.bat
```

#### Linux/Mac系统
```bash
cd sql
chmod +x fix_password.sh
./fix_password.sh
```

#### 脚本功能
1. 连接到MySQL数据库
2. 将所有用户密码重置为正确的hash值
3. 验证更新结果
4. 显示可用账号

---

### 方案2: 手动执行SQL

#### 步骤1: 连接数据库
```bash
mysql -u root -p
```

#### 步骤2: 选择数据库
```sql
USE ct_tibet_wms;
```

#### 步骤3: 执行修复SQL
```sql
-- 正确的BCrypt密码hash (密码: 123456)
UPDATE tb_user
SET password = '$2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e';
```

#### 步骤4: 验证结果
```sql
SELECT id, username, real_name, LEFT(password, 20) AS password_preview
FROM tb_user;
```

---

### 方案3: 重新初始化数据库

如果数据库是全新的，可以重新执行初始化：

```bash
cd sql
# Windows
install.bat

# Linux/Mac
chmod +x install.sh
./install.sh
```

**注意**: 这会清空所有数据！

---

## 验证修复

### 1. 检查密码hash
```sql
SELECT username, password
FROM tb_user
WHERE username = 'admin';
```

**期望输出**:
```
username | password
---------|----------
admin    | $2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e
```

### 2. 尝试登录

#### PC端登录
```
URL: http://localhost:5173
账号: admin
密码: 123456
```

#### API测试
```bash
curl -X POST http://localhost:8888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

**成功响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "username": "admin",
      "realName": "系统管理员"
    }
  }
}
```

---

## 可用账号

修复后，以下账号均可使用密码 `123456` 登录：

| 账号 | 密码 | 角色 | 说明 |
|------|------|------|------|
| admin | 123456 | 系统管理员 | 最高权限 |
| wl_admin | 123456 | 部门管理员 | 网运部管理员 |
| wl_warehouse | 123456 | 仓库管理员 | 李军 |
| wl_user1 | 123456 | 普通员工 | 张强 |
| wl_user2 | 123456 | 普通员工 | 王小明 |

---

## 技术细节

### BCrypt密码生成

使用 `PasswordHashGenerator` 工具生成：

```bash
cd backend
mvn test-compile exec:java \
  -Dexec.mainClass="com.ct.wms.util.PasswordHashGenerator" \
  -Dexec.classpathScope=test
```

**工具位置**: `backend/src/test/java/com/ct/wms/util/PasswordHashGenerator.java`

### 密码验证流程

1. 用户输入: `admin / 123456`
2. Spring Security获取数据库中的hash
3. BCryptPasswordEncoder.matches("123456", hash)
4. 验证通过 → 生成JWT Token
5. 验证失败 → 返回 "密码错误"

### 正确的Hash值

```
密码: 123456
Hash: $2a$10$j7sCAk4BYfqNqmnW.7QK7eyNjtt01JOcehbxpjg5T7ImShI0751.e
验证: ✓ 通过
```

---

## 常见问题

### Q1: 为什么之前的hash不能用？
A: 可能是手动编写的假hash值，或者使用了不兼容的BCrypt实现。

### Q2: 每次生成的hash都不一样，正常吗？
A: 正常！BCrypt使用随机salt，相同密码每次生成的hash都不同，但都能正确验证。

### Q3: 如何为新用户生成密码？
A: 运行 `PasswordHashGenerator` 工具，或在代码中使用：
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("新密码");
```

### Q4: 修复后还是无法登录？
检查以下几点：
1. 后端服务是否正常运行
2. 数据库连接是否正常
3. 浏览器控制台是否有错误
4. 后端日志中的详细错误信息

---

## 文件清单

### 新增文件
```
sql/
├── fix_password.sql        # 密码修复SQL脚本
├── fix_password.bat        # Windows修复脚本
└── fix_password.sh         # Linux/Mac修复脚本

backend/src/test/java/com/ct/wms/util/
└── PasswordHashGenerator.java  # 密码hash生成工具

docs/
└── PASSWORD_FIX_GUIDE.md   # 本文档
```

### 修改文件
```
sql/
└── init_data.sql           # 已更新为正确的密码hash
```

---

## 预防措施

### 开发环境
- 使用 `PasswordHashGenerator` 生成所有测试密码
- 在SQL脚本中注释密码对应的明文
- 定期验证测试账号可用性

### 生产环境
- 部署前强制修改所有默认密码
- 使用强密码策略
- 启用首次登录强制修改密码
- 定期审计账号权限

---

## 总结

✅ **问题已修复**
- SQL脚本已更新
- 提供快速修复工具
- 所有账号密码为 `123456`

✅ **工具已添加**
- PasswordHashGenerator 生成工具
- 自动化修复脚本

✅ **文档已完善**
- 问题原因分析
- 多种解决方案
- 常见问题解答

---

**最后更新**: 2025-12-12
**文档版本**: v1.0
**问题状态**: 已解决 ✅
