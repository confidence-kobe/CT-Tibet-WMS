# 修复验证指南

## 快速验证清单

在应用本修复前后，请按照以下步骤验证修复效果。

### 修复前的症状确认

如果您遇到以下症状，说明确实存在问题：

- [ ] 应用启动时输出`java.sql.SQLSyntaxErrorException: Unknown column 'is_deleted' in 'field list'`
- [ ] 尝试登录时返回500错误
- [ ] SQL日志显示 `SELECT ... is_deleted FROM tb_user WHERE is_deleted=0`
- [ ] UserDetailsServiceImpl.loadUserByUsername() 方法抛出异常

### 应用修复

#### 修复步骤1: 编辑BaseEntity.java

打开文件: **`backend/src/main/java/com/ct/wms/entity/BaseEntity.java`**

**查找这一行(第61行)**:
```java
@TableField("is_deleted")
```

**删除该行**，使代码变为:
```java
@TableLogic
private Integer isDeleted;
```

完整修复后的样子应该是:
```java
/**
 * 逻辑删除标记（0-未删除 1-已删除）
 */
@TableLogic
private Integer isDeleted;
```

#### 修复步骤2: 清理并重新编译

```bash
# 进入项目根目录
cd H:\java\CT-Tibet-WMS

# 清理旧的编译输出
mvn clean

# 重新编译(跳过测试以加快速度)
mvn compile -DskipTests
```

**预期输出**:
```
[INFO] BUILD SUCCESS
```

#### 修复步骤3: 启动应用

```bash
# 方式1: 使用Maven启动
cd H:\java\CT-Tibet-WMS\backend
mvn spring-boot:run

# 方式2: 直接运行JAR(如果已构建)
java -jar backend/target/ct-tibet-wms-1.0.0.jar
```

**预期日志输出(应该NO更出现错误)**:
```
2025-11-13 22:35:00.000 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 48888
```

---

## 修复后的验证步骤

### 验证1: 检查数据库连接和字段

```sql
-- 连接到MySQL
mysql -h localhost -u root -p -e "
  USE ct_tibet_wms;
  DESC tb_user;
" | grep is_deleted

-- 预期输出:
-- is_deleted | tinyint(4) | NO | | 0 |
```

### 验证2: 查看MyBatis生成的SQL

打开日志文件: **`backend/logs/sql.log`** 或 **`backend/logs/all.log`**

查找包含这样内容的SQL语句:
```sql
SELECT id,username,password,real_name,phone,email,dept_id,role_id,status,avatar,wechat_openid,wechat_nickname,last_login_time,last_login_ip,password_update_time,is_first_login,remark,create_time,update_by,is_deleted
FROM tb_user
WHERE is_deleted=0 AND (username = ?)
```

关键点:
- `is_deleted` 字段应该出现在SELECT列表中
- WHERE子句中应该有 `is_deleted=0` 条件
- **不应该**出现 `Unknown column` 错误

### 验证3: 执行登录测试

#### 方法A: 使用curl命令

```bash
# 假设有一个username为"admin"的用户已在数据库中

curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "your_password",
    "loginType": 1
  }' \
  | jq '.'
```

**成功响应示例**:
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "deptId": 1,
      "roleId": 1
    }
  }
}
```

**失败响应示例** (修复未生效):
```json
{
  "code": 500,
  "message": "Unknown column 'is_deleted' in 'field list'",
  "data": null
}
```

#### 方法B: 使用Swagger API文档

1. 打开浏览器访问: http://localhost:48888/doc.html
2. 展开"认证授权"模块
3. 点击"用户登录"接口
4. 输入用户名和密码
5. 点击"发送请求"
6. 查看响应

### 验证4: 检查应用日志

```bash
# 查看最近100行错误日志
tail -100 backend/logs/error.log

# 查看最近100行所有日志
tail -100 backend/logs/all.log
```

**修复成功的标志**:
- 没有 `SQLSyntaxErrorException`
- 没有 `Unknown column 'is_deleted'`
- 没有任何关于字段映射的错误

**修复失败的标志**:
- 仍然出现 `SQLSyntaxErrorException`
- 日志中有 `field 'deleted' not found` 或类似错误

### 验证5: 测试其他继承BaseEntity的操作

修复应该也解决其他实体的逻辑删除问题，例如:

```bash
# 测试部门列表查询(也使用逻辑删除)
curl http://localhost:48888/api/dept/list \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试物资列表查询
curl http://localhost:48888/api/material/list \
  -H "Authorization: Bearer YOUR_TOKEN"
```

预期所有列表接口都能正常返回数据，而不会出现字段映射错误。

---

## 常见修复失败的原因及排查

### 问题1: 修复后仍然出现"Unknown column 'is_deleted'"

**可能原因**:
1. 修改的不是BaseEntity.java，而是其他文件
2. 修改后没有重新编译
3. 运行的是老版本的JAR或编译输出

**排查步骤**:
```bash
# 1. 确认修改的文件位置正确
cat backend/src/main/java/com/ct/wms/entity/BaseEntity.java | grep -A 5 "逻辑删除"

# 应该显示:
# @TableLogic
# private Integer isDeleted;

# 2. 清理并重新编译
mvn clean compile -DskipTests

# 3. 删除旧的编译输出
rm -rf backend/target

# 4. 重新启动应用
mvn spring-boot:run
```

### 问题2: 修复后出现"field 'deleted' not found"

**可能原因**:
- 数据库表中的字段名不是 `is_deleted`
- MyBatis-Plus的全局配置不正确

**排查步骤**:
```bash
# 检查数据库表结构
mysql -u root -p -e "DESC ct_tibet_wms.tb_user;" | grep deleted

# 应该看到:
# is_deleted | tinyint(4) | NO | | 0 |

# 检查application.yml的配置
grep -A 3 "logic-delete-field:" backend/src/main/resources/application.yml

# 应该看到:
# logic-delete-field: deleted
# logic-delete-value: 1
# logic-not-delete-value: 0
```

### 问题3: 修复后登录仍然失败，但错误信息不同

**可能原因**:
- 数据库用户数据为空或格式错误
- 密码加密方式不匹配
- 用户状态被禁用

**排查步骤**:
```bash
# 查看数据库中是否有用户
mysql -u root -p -e "SELECT id, username, status FROM ct_tibet_wms.tb_user LIMIT 5;"

# 查看应用日志中的具体错误
tail -50 backend/logs/error.log

# 尝试直接查询用户(不经过MyBatis-Plus逻辑删除)
mysql -u root -p -e "
  SELECT id, username, is_deleted FROM ct_tibet_wms.tb_user
  WHERE username='admin' AND is_deleted=0;
"
```

---

## 修复前后对比

### 修复前: MyBatis-Plus字段配置混乱

```
代码层面:
  BaseEntity.java:
    @TableLogic
    @TableField("is_deleted")        <-- 显式指定为 is_deleted
    private Integer isDeleted;

配置层面:
  application.yml:
    logic-delete-field: deleted      <-- 全局配置指定为 deleted

结果:
  MyBatis-Plus在生成SQL时产生冲突
  实际生成: Unknown column 'is_deleted' (因配置混乱)
```

### 修复后: 配置统一使用全局设置

```
代码层面:
  BaseEntity.java:
    @TableLogic
    // 移除了@TableField，让MyBatis-Plus使用全局配置
    private Integer isDeleted;

配置层面:
  application.yml:
    logic-delete-field: deleted      <-- 全局配置指定为 deleted

结果:
  MyBatis-Plus自动将"deleted"(驼峰式)转换为"is_deleted"(蛇形式)
  实际生成: WHERE is_deleted=0 (正确)
```

---

## 修复失败时的快速回滚

如果修复导致其他问题，可以快速回滚:

```bash
# 1. 恢复BaseEntity.java到修复前的状态
git checkout backend/src/main/java/com/ct/wms/entity/BaseEntity.java

# 2. 重新编译
mvn clean compile -DskipTests

# 3. 重新启动应用
mvn spring-boot:run
```

---

## 修复后的建议事项

1. **提交git commit**: 记录这次修复
   ```bash
   git add backend/src/main/java/com/ct/wms/entity/BaseEntity.java
   git commit -m "fix: 修复MyBatis-Plus逻辑删除字段配置冲突

   移除BaseEntity中的@TableField("is_deleted")注解，
   改由mybatis-plus全局配置自动映射is_deleted字段，
   避免配置冲突导致的SQLSyntaxErrorException"
   ```

2. **运行单元测试**: 确保没有破坏其他功能
   ```bash
   mvn test
   ```

3. **更新部署文档**: 记录此修复的细节

4. **监控应用日志**: 在生产环境部署后密切关注日志

---

## 测试数据准备

如果数据库中没有测试用户，可以使用以下SQL插入测试数据:

```sql
-- 首先确保数据库已初始化
USE ct_tibet_wms;

-- 如果没有部门，先创建测试部门
INSERT INTO tb_dept (dept_name, dept_code, parent_id, status, create_time)
VALUES ('测试部门', 'TEST', 0, 0, NOW())
ON DUPLICATE KEY UPDATE dept_name='测试部门';

-- 获取部门ID(通常是1)
SET @dept_id = LAST_INSERT_ID();

-- 如果没有角色，先创建测试角色
INSERT INTO tb_role (role_name, role_code, role_level, status, create_time)
VALUES ('普通员工', 'USER', 3, 0, NOW())
ON DUPLICATE KEY UPDATE role_name='普通员工';

SET @role_id = LAST_INSERT_ID();

-- 创建测试用户(密码需要是BCrypt加密后的值)
-- 使用密码: "password123" 的BCrypt加密值
INSERT INTO tb_user (
  username, password, real_name, phone, email,
  dept_id, role_id, status, is_first_login,
  is_deleted, create_time
) VALUES (
  'admin',
  '$2a$10$N9qo8uLOickgx2ZMRZoM2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', -- password123
  '管理员',
  '13800138000',
  'admin@example.com',
  @dept_id,
  @role_id,
  0,
  0,
  0,
  NOW()
)
ON DUPLICATE KEY UPDATE password='$2a$10$N9qo8uLOickgx2ZMRZoM2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW';
```

使用这个测试账户登录:
- 用户名: `admin`
- 密码: `password123`

---

## 修复完成标记

当您完成以下所有步骤后，修复即为成功:

- [ ] 编辑BaseEntity.java，删除@TableField("is_deleted")
- [ ] 执行 `mvn clean compile`
- [ ] 应用成功启动，没有SQLSyntaxErrorException
- [ ] 能够成功登录到系统
- [ ] 日志中没有"Unknown column 'is_deleted'"错误
- [ ] 其他查询接口(部门、物资等)正常运行
- [ ] 提交修复到git版本控制

完成这些步骤后，您的系统应该已经恢复正常。

---

**修复指南完成时间**: 2025-11-13
**状态**: 准备就绪
