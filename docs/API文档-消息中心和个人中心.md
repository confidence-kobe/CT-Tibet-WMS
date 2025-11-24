# 消息中心和个人中心 API 文档

## 实现概览

本文档描述了CT-Tibet-WMS系统的消息中心和个人中心模块的API接口。所有接口均已实现并通过编译测试。

---

## 一、消息中心 API

### 1. 查询消息列表（带统计信息）

**接口路径**: `GET /api/messages`

**接口描述**: 分页查询当前用户的消息列表，并返回统计信息（总数、已读、未读）

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |
| type | Integer | 否 | 消息类型（1-系统通知 2-申请通知 3-审批通知 4-库存预警） |
| isRead | Integer | 否 | 是否已读（0-未读 1-已读） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "userId": 1,
        "title": "您的申请已通过",
        "content": "申请单 AP20251114001 已通过审批",
        "type": "APPROVAL",
        "isRead": 0,
        "relatedId": 100,
        "relatedType": 1,
        "createTime": "2025-11-14 10:00:00"
      }
    ],
    "total": 100,
    "stats": {
      "total": 100,
      "unread": 25,
      "read": 75
    }
  }
}
```

---

### 2. 查询消息列表（简单版）

**接口路径**: `GET /api/messages/my`

**接口描述**: 分页查询当前用户的消息列表（不包含统计信息，向后兼容）

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |
| isRead | Integer | 否 | 是否已读（0-未读 1-已读） |

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "list": [...],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

---

### 3. 获取未读消息数量

**接口路径**: `GET /api/messages/unread-count`

**接口描述**: 获取当前用户的未读消息数量

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 25
}
```

---

### 4. 标记单条消息已读

**接口路径**: `PUT /api/messages/{id}/read`

**接口描述**: 标记指定消息为已读

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 消息ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null
}
```

**安全说明**: 只能标记当前用户的消息，否则返回403错误

---

### 5. 批量标记消息已读

**接口路径**: `PUT /api/messages/read-batch`

**接口描述**: 批量标记多条消息为已读

**请求体**:
```json
[1, 2, 3, 4, 5]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "批量标记成功",
  "data": null
}
```

---

### 6. 标记所有消息已读

**接口路径**: `PUT /api/messages/read-all`

**接口描述**: 标记当前用户的所有未读消息为已读

**响应示例**:
```json
{
  "code": 200,
  "message": "标记成功",
  "data": null
}
```

---

### 7. 删除消息

**接口路径**: `DELETE /api/messages/{id}`

**接口描述**: 删除指定消息（软删除）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 消息ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**安全说明**: 只能删除当前用户的消息，否则返回403错误

---

## 二、个人中心 API

### 1. 获取个人信息

**接口路径**: `GET /api/users/profile`

**接口描述**: 获取当前登录用户的个人信息

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "realName": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "deptId": 1,
    "deptName": "技术部",
    "roleId": 2,
    "roleName": "仓库管理员",
    "roleCode": "warehouse",
    "avatar": null,
    "lastLoginTime": "2025-11-14 09:00:00",
    "lastLoginIp": "192.168.1.100"
  }
}
```

---

### 2. 更新个人信息

**接口路径**: `PUT /api/users/profile`

**接口描述**: 更新当前用户的个人信息（真实姓名、手机号、邮箱）

**请求体**:
```json
{
  "realName": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com"
}
```

**字段验证**:
- `realName`: 不能为空
- `phone`: 必须符合手机号格式（11位，1开头）
- `email`: 必须符合邮箱格式

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

**安全说明**: 只能更新当前用户的信息，不能修改其他用户

---

### 3. 修改密码

**接口路径**: `PUT /api/users/password`

**接口描述**: 修改当前用户密码

**请求体**:
```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

**字段验证**:
- `oldPassword`: 不能为空
- `newPassword`: 不能为空，长度6-20位

**业务规则**:
1. 验证旧密码是否正确
2. 新密码不能与旧密码相同
3. 新密码使用BCrypt加密存储
4. 更新密码修改时间字段

**响应示例**:
```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "旧密码不正确",
  "data": null
}
```

---

## 三、技术实现说明

### 1. 安全性

- **权限控制**: 所有接口都验证用户身份，确保只能操作自己的数据
- **密码加密**: 使用BCryptPasswordEncoder加密存储密码
- **参数验证**: 使用javax.validation进行参数校验
- **敏感信息**: 密码字段使用@JsonIgnore避免序列化

### 2. 数据过滤

- 消息查询自动根据当前用户ID过滤
- 使用SecurityContextHolder获取当前登录用户信息
- 操作前验证数据所有权，防止越权访问

### 3. 统计信息

消息列表接口返回的统计信息包括：
- `total`: 用户所有消息总数
- `unread`: 未读消息数量
- `read`: 已读消息数量

### 4. 日志记录

所有关键操作都记录日志：
- 消息标记已读/删除
- 个人信息更新
- 密码修改

### 5. 事务管理

以下操作使用事务保护：
- 批量标记消息已读
- 更新个人信息
- 修改密码

---

## 四、新增文件清单

### DTO (数据传输对象)
- `UpdateProfileRequest.java` - 更新个人信息请求
- `ChangePasswordRequest.java` - 修改密码请求

### VO (视图对象)
- `MessageVO.java` - 消息列表响应（包含统计信息）
- `UserProfileVO.java` - 用户个人信息响应

### Service 层扩展
- `MessageService.java` - 新增 `listMyMessagesWithStats()` 方法
- `UserService.java` - 新增个人中心相关方法：
  - `getCurrentUserProfile()`
  - `updateCurrentUserProfile()`
  - `changeCurrentUserPassword()`

### Service 实现
- `MessageServiceImpl.java` - 实现带统计信息的消息查询
- `UserServiceImpl.java` - 实现个人中心业务逻辑

### Controller 层扩展
- `MessageController.java` - 新增 `GET /api/messages` 接口
- `UserController.java` - 新增个人中心3个接口

---

## 五、测试建议

### 1. 消息中心测试

1. **消息列表查询**
   - 测试分页功能
   - 测试按消息类型过滤
   - 测试按已读状态过滤
   - 验证统计信息准确性

2. **消息操作**
   - 标记单条消息已读
   - 批量标记已读
   - 全部标记已读
   - 删除消息

3. **权限测试**
   - 尝试操作其他用户的消息（应返回403）
   - 验证只能看到自己的消息

### 2. 个人中心测试

1. **获取个人信息**
   - 验证返回完整信息
   - 验证部门和角色信息正确关联

2. **更新个人信息**
   - 测试更新真实姓名、手机号、邮箱
   - 测试参数验证（手机号格式、邮箱格式）

3. **修改密码**
   - 测试旧密码验证
   - 测试新密码不能与旧密码相同
   - 测试密码长度验证
   - 验证修改后能用新密码登录

---

## 六、注意事项

1. **向后兼容**: 保留了原有的 `GET /api/messages/my` 接口，不影响现有前端代码
2. **密码安全**: 修改密码后不会自动退出登录，但建议前端提示用户重新登录
3. **手机号验证**: 仅验证格式，不验证是否真实存在
4. **邮箱验证**: 仅验证格式，不发送验证邮件
5. **头像上传**: 当前版本未实现，avatar字段可为null

---

## 七、编译状态

**状态**: ✅ 编译成功

**编译命令**: `mvn clean compile -DskipTests`

**编译结果**: BUILD SUCCESS

**编译时间**: 2025-11-16 22:44:56

所有新增代码已通过Maven编译验证，无语法错误。
