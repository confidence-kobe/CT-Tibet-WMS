# 仓库管理系统 API 接口文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 系统名称 | 西藏电信仓库管理系统 (CT-Tibet-WMS) |
| API版本 | v1.0 |
| 基础URL | http://localhost:8080/api |
| 文档状态 | 开发中 |
| 创建日期 | 2025-11-11 |
| 认证方式 | JWT Token |

---

## 目录

1. [接口规范](#1-接口规范)
2. [认证授权](#2-认证授权)
3. [用户管理](#3-用户管理)
4. [物资管理](#4-物资管理)
5. [仓库管理](#5-仓库管理)
6. [入库管理](#6-入库管理)
7. [出库管理](#7-出库管理)
8. [申请审批](#8-申请审批)
9. [库存管理](#9-库存管理)
10. [统计报表](#10-统计报表)
11. [消息通知](#11-消息通知)
12. [系统管理](#12-系统管理)
13. [错误码说明](#13-错误码说明)

---

## 1. 接口规范

### 1.1 请求规范

#### 基础URL
```
开发环境: http://localhost:8080/api
测试环境: http://test.example.com/api
生产环境: https://wms.chinatelecom.cn/api
```

#### HTTP方法

| 方法 | 说明 | 示例 |
|-----|------|------|
| GET | 查询资源 | GET /api/materials |
| POST | 创建资源 | POST /api/materials |
| PUT | 更新资源（全量） | PUT /api/materials/{id} |
| PATCH | 更新资源（部分） | PATCH /api/materials/{id} |
| DELETE | 删除资源 | DELETE /api/materials/{id} |

#### 请求头

```http
Content-Type: application/json
Authorization: Bearer {token}
Accept: application/json
```

#### 分页参数（统一）

```json
{
  "pageNum": 1,        // 页码，从1开始
  "pageSize": 20,      // 每页条数，默认20
  "sortField": "createTime",  // 排序字段
  "sortOrder": "desc"  // 排序方式：asc/desc
}
```

### 1.2 响应规范

#### 统一响应格式

```json
{
  "code": 200,               // 状态码
  "message": "操作成功",      // 提示信息
  "data": {},                // 业务数据
  "timestamp": 1699600000000 // 时间戳
}
```

#### 分页响应格式

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 100,           // 总记录数
    "pageNum": 1,           // 当前页码
    "pageSize": 20,         // 每页条数
    "pages": 5,             // 总页数
    "list": []              // 数据列表
  },
  "timestamp": 1699600000000
}
```

#### 成功响应示例

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "materialName": "光缆12芯"
  },
  "timestamp": 1699600000000
}
```

#### 错误响应示例

```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": {
    "errors": [
      {
        "field": "quantity",
        "message": "数量必须大于0"
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 1.3 状态码说明

| 状态码 | 说明 | 示例场景 |
|-------|------|---------|
| 200 | 成功 | 操作成功 |
| 201 | 创建成功 | 新增资源成功 |
| 400 | 请求参数错误 | 参数验证失败 |
| 401 | 未授权 | 未登录或Token过期 |
| 403 | 无权限 | 角色权限不足 |
| 404 | 资源不存在 | 数据不存在 |
| 409 | 冲突 | 数据重复 |
| 500 | 服务器错误 | 系统异常 |

### 1.4 业务错误码

| 错误码 | 说明 |
|-------|------|
| 1001 | 库存不足 |
| 1002 | 申请已被处理 |
| 1003 | 审批超时 |
| 1004 | 出库单已取消 |
| 1005 | 物资已停用 |
| 1006 | 仓库已禁用 |
| 1007 | 用户名已存在 |
| 1008 | 手机号已注册 |

---

## 2. 认证授权

### 2.1 用户登录

#### 接口说明
用户登录，返回JWT Token

#### 请求
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456",
  "loginType": 1  // 1=PC端 2=小程序
}
```

#### 响应
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,  // Token有效期（秒）
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "phone": "13800000000",
      "deptId": 1,
      "deptName": "西藏电信",
      "roleId": 1,
      "roleName": "系统管理员",
      "roleCode": "admin",
      "avatar": "https://example.com/avatar.jpg"
    }
  },
  "timestamp": 1699600000000
}
```

#### 错误响应
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": 1699600000000
}
```

### 2.2 小程序登录

#### 接口说明
微信小程序登录，使用code换取token

#### 请求
```http
POST /api/auth/wechat-login
Content-Type: application/json

{
  "code": "061xbK000YfB0P1LFs100rVjRH3xbK0x",  // 微信登录凭证
  "encryptedData": "...",  // 加密数据
  "iv": "..."              // 加密算法初始向量
}
```

#### 响应
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isNewUser": false,  // 是否新用户
    "user": {
      "id": 4,
      "username": "wl_user1",
      "realName": "张强",
      "phone": "13800000003",
      "deptId": 2,
      "deptName": "网络运维部",
      "roleId": 4,
      "roleName": "普通员工",
      "wechatNickname": "张强",
      "avatar": "https://wx.qlogo.cn/..."
    }
  },
  "timestamp": 1699600000000
}
```

### 2.3 刷新Token

#### 接口说明
使用旧Token刷新获取新Token

#### 请求
```http
POST /api/auth/refresh-token
Authorization: Bearer {old_token}
```

#### 响应
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200
  },
  "timestamp": 1699600000000
}
```

### 2.4 退出登录

#### 接口说明
退出登录，清除Token

#### 请求
```http
POST /api/auth/logout
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null,
  "timestamp": 1699600000000
}
```

### 2.5 获取当前用户信息

#### 接口说明
获取当前登录用户详细信息

#### 请求
```http
GET /api/auth/current-user
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 3,
    "username": "wl_warehouse",
    "realName": "李军",
    "phone": "13800000002",
    "email": "lijun@example.com",
    "deptId": 2,
    "deptName": "网络运维部",
    "roleId": 3,
    "roleName": "仓库管理员",
    "roleCode": "warehouse",
    "avatar": "https://example.com/avatar.jpg",
    "lastLoginTime": "2025-11-10 10:30:00",
    "lastLoginIp": "192.168.1.100",
    "permissions": [
      "inbound:create",
      "outbound:create",
      "apply:approve",
      "inventory:view"
    ],
    "menus": [
      {
        "id": 2,
        "menuName": "基础管理",
        "path": "/basic",
        "icon": "setting",
        "children": [
          {
            "id": 201,
            "menuName": "物资管理",
            "path": "/basic/material"
          }
        ]
      }
    ]
  },
  "timestamp": 1699600000000
}
```

---

## 3. 用户管理

### 3.1 用户列表

#### 接口说明
查询用户列表，支持分页和筛选

#### 请求
```http
GET /api/users?pageNum=1&pageSize=20&deptId=2&roleId=3&status=0&keyword=李军
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认20 |
| deptId | Long | 否 | 部门ID |
| roleId | Long | 否 | 角色ID |
| status | Integer | 否 | 状态：0启用 1禁用 |
| keyword | String | 否 | 关键词（用户名/姓名/手机号）|

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 3,
    "list": [
      {
        "id": 3,
        "username": "wl_warehouse",
        "realName": "李军",
        "phone": "13800000002",
        "email": "lijun@example.com",
        "deptId": 2,
        "deptName": "网络运维部",
        "roleId": 3,
        "roleName": "仓库管理员",
        "status": 0,
        "statusName": "启用",
        "avatar": "https://example.com/avatar.jpg",
        "lastLoginTime": "2025-11-10 10:30:00",
        "createTime": "2025-01-01 00:00:00"
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 3.2 用户详情

#### 请求
```http
GET /api/users/{id}
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 3,
    "username": "wl_warehouse",
    "realName": "李军",
    "phone": "13800000002",
    "email": "lijun@example.com",
    "deptId": 2,
    "deptName": "网络运维部",
    "roleId": 3,
    "roleName": "仓库管理员",
    "status": 0,
    "avatar": "https://example.com/avatar.jpg",
    "wechatNickname": "李军",
    "lastLoginTime": "2025-11-10 10:30:00",
    "lastLoginIp": "192.168.1.100",
    "createTime": "2025-01-01 00:00:00"
  },
  "timestamp": 1699600000000
}
```

### 3.3 创建用户

#### 请求
```http
POST /api/users
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "test_user",
  "password": "123456",
  "realName": "测试用户",
  "phone": "13800000099",
  "email": "test@example.com",
  "deptId": 2,
  "roleId": 4
}
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| username | String | 是 | 登录名，全局唯一 |
| password | String | 是 | 密码，长度6-20 |
| realName | String | 是 | 真实姓名 |
| phone | String | 是 | 手机号，全局唯一 |
| email | String | 否 | 邮箱 |
| deptId | Long | 是 | 部门ID |
| roleId | Long | 是 | 角色ID |

#### 响应
```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 100,
    "username": "test_user",
    "realName": "测试用户"
  },
  "timestamp": 1699600000000
}
```

### 3.4 更新用户

#### 请求
```http
PUT /api/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "realName": "测试用户2",
  "phone": "13800000099",
  "email": "test@example.com",
  "deptId": 2,
  "roleId": 4
}
```

#### 响应
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1699600000000
}
```

### 3.5 删除用户（软删除）

#### 请求
```http
DELETE /api/users/{id}
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1699600000000
}
```

### 3.6 修改密码

#### 请求
```http
PUT /api/users/change-password
Authorization: Bearer {token}
Content-Type: application/json

{
  "oldPassword": "123456",
  "newPassword": "654321",
  "confirmPassword": "654321"
}
```

#### 响应
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null,
  "timestamp": 1699600000000
}
```

### 3.7 重置密码

#### 接口说明
管理员重置用户密码为默认密码（123456）

#### 请求
```http
PUT /api/users/{id}/reset-password
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "密码已重置为：123456",
  "data": null,
  "timestamp": 1699600000000
}
```

---

## 4. 物资管理

### 4.1 物资列表

#### 请求
```http
GET /api/materials?pageNum=1&pageSize=20&category=光缆类&status=0&keyword=光缆
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| category | String | 否 | 物资类别 |
| status | Integer | 否 | 状态：0启用 1停用 |
| keyword | String | 否 | 关键词（名称/编码）|

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1,
    "list": [
      {
        "id": 1,
        "materialName": "光缆12芯",
        "materialCode": "GX001",
        "category": "光缆类",
        "spec": "12芯单模",
        "unit": "条",
        "price": 1500.00,
        "minStock": 100.00,
        "description": "12芯单模光缆",
        "image": "https://example.com/gx001.jpg",
        "status": 0,
        "statusName": "启用",
        "createTime": "2025-01-01 00:00:00"
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 4.2 物资详情

#### 请求
```http
GET /api/materials/{id}
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "materialName": "光缆12芯",
    "materialCode": "GX001",
    "category": "光缆类",
    "spec": "12芯单模",
    "unit": "条",
    "price": 1500.00,
    "minStock": 100.00,
    "description": "12芯单模光缆，用于通信线路铺设",
    "image": "https://example.com/gx001.jpg",
    "status": 0,
    "createTime": "2025-01-01 00:00:00",
    "updateTime": "2025-11-10 10:00:00",
    "totalStock": 1850.00,  // 所有仓库总库存
    "warehouses": [
      {
        "warehouseId": 1,
        "warehouseName": "网络运维部仓库",
        "quantity": 850.00
      },
      {
        "warehouseId": 2,
        "warehouseName": "市场营销部仓库",
        "quantity": 1000.00
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 4.3 创建物资

#### 请求
```http
POST /api/materials
Authorization: Bearer {token}
Content-Type: application/json

{
  "materialName": "光缆96芯",
  "materialCode": "GX005",
  "category": "光缆类",
  "spec": "96芯单模",
  "unit": "条",
  "price": 12000.00,
  "minStock": 10.00,
  "description": "96芯单模光缆",
  "image": "https://example.com/gx005.jpg"
}
```

#### 响应
```json
{
  "code": 201,
  "message": "创建成功",
  "data": {
    "id": 21,
    "materialName": "光缆96芯",
    "materialCode": "GX005"
  },
  "timestamp": 1699600000000
}
```

### 4.4 更新物资

#### 请求
```http
PUT /api/materials/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "materialName": "光缆96芯",
  "spec": "96芯单模",
  "price": 11500.00,
  "minStock": 20.00
}
```

#### 响应
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1699600000000
}
```

### 4.5 删除物资

#### 接口说明
软删除，有库存的物资不能删除

#### 请求
```http
DELETE /api/materials/{id}
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1699600000000
}
```

#### 错误响应
```json
{
  "code": 400,
  "message": "该物资有库存，无法删除",
  "data": {
    "totalStock": 850.00
  },
  "timestamp": 1699600000000
}
```

### 4.6 物资类别列表

#### 接口说明
获取所有物资类别（去重）

#### 请求
```http
GET /api/materials/categories
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    "光缆类",
    "设备类",
    "配件类",
    "工具类"
  ],
  "timestamp": 1699600000000
}
```

---

## 5. 仓库管理

### 5.1 仓库列表

#### 请求
```http
GET /api/warehouses?deptId=2&status=0
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "warehouseName": "网络运维部仓库",
      "warehouseCode": "WH_WL_001",
      "deptId": 2,
      "deptName": "网络运维部",
      "address": "西藏拉萨市城关区江苏路1号1楼",
      "managerId": 3,
      "managerName": "李军",
      "capacity": 120.00,
      "status": 0,
      "statusName": "启用",
      "materialCount": 20,  // 物资种类数
      "totalValue": 1500000.00,  // 库存总价值
      "createTime": "2025-01-01 00:00:00"
    }
  ],
  "timestamp": 1699600000000
}
```

### 5.2 仓库详情

#### 请求
```http
GET /api/warehouses/{id}
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "warehouseName": "网络运维部仓库",
    "warehouseCode": "WH_WL_001",
    "deptId": 2,
    "deptName": "网络运维部",
    "address": "西藏拉萨市城关区江苏路1号1楼",
    "managerId": 3,
    "managerName": "李军",
    "managerPhone": "13800000002",
    "capacity": 120.00,
    "status": 0,
    "remark": "主仓库",
    "materialCount": 20,
    "totalValue": 1500000.00,
    "createTime": "2025-01-01 00:00:00"
  },
  "timestamp": 1699600000000
}
```

---

## 6. 入库管理

### 6.1 入库单列表

#### 请求
```http
GET /api/inbounds?pageNum=1&pageSize=20&warehouseId=1&inboundType=1&startDate=2025-11-01&endDate=2025-11-10
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| warehouseId | Long | 否 | 仓库ID |
| inboundType | Integer | 否 | 入库类型：1采购 2退货 3调拨 4其他 |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |
| operatorId | Long | 否 | 操作人ID |
| keyword | String | 否 | 关键词（单号） |

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 3,
    "list": [
      {
        "id": 1,
        "inboundNo": "RK_WL_20251110_0001",
        "warehouseId": 1,
        "warehouseName": "网络运维部仓库",
        "inboundType": 1,
        "inboundTypeName": "采购入库",
        "operatorId": 3,
        "operatorName": "李军",
        "inboundTime": "2025-11-10 09:30:00",
        "totalAmount": 150000.00,
        "remark": "采购入库",
        "materialCount": 1,  // 物资种类数
        "totalQuantity": 100,  // 总数量
        "createTime": "2025-11-10 09:30:00"
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 6.2 入库单详情

#### 请求
```http
GET /api/inbounds/{id}
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "inboundNo": "RK_WL_20251110_0001",
    "warehouseId": 1,
    "warehouseName": "网络运维部仓库",
    "inboundType": 1,
    "inboundTypeName": "采购入库",
    "operatorId": 3,
    "operatorName": "李军",
    "inboundTime": "2025-11-10 09:30:00",
    "totalAmount": 150000.00,
    "remark": "采购入库",
    "createTime": "2025-11-10 09:30:00",
    "details": [
      {
        "id": 1,
        "materialId": 1,
        "materialName": "光缆12芯",
        "materialCode": "GX001",
        "spec": "12芯单模",
        "unit": "条",
        "quantity": 100.00,
        "unitPrice": 1500.00,
        "amount": 150000.00,
        "remark": ""
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 6.3 创建入库单

#### 请求
```http
POST /api/inbounds
Authorization: Bearer {token}
Content-Type: application/json

{
  "warehouseId": 1,
  "inboundType": 1,
  "inboundTime": "2025-11-10 09:30:00",
  "remark": "采购入库",
  "details": [
    {
      "materialId": 1,
      "quantity": 100,
      "unitPrice": 1500.00,
      "remark": ""
    }
  ]
}
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| warehouseId | Long | 是 | 仓库ID |
| inboundType | Integer | 是 | 入库类型 |
| inboundTime | String | 是 | 入库时间 |
| remark | String | 否 | 备注 |
| details | Array | 是 | 入库明细列表 |

**明细参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| materialId | Long | 是 | 物资ID |
| quantity | BigDecimal | 是 | 数量，必须>0 |
| unitPrice | BigDecimal | 否 | 单价 |
| remark | String | 否 | 备注 |

#### 响应
```json
{
  "code": 201,
  "message": "入库成功",
  "data": {
    "id": 100,
    "inboundNo": "RK_WL_20251110_0100",
    "totalAmount": 150000.00
  },
  "timestamp": 1699600000000
}
```

---

## 7. 出库管理

### 7.1 出库单列表

#### 请求
```http
GET /api/outbounds?pageNum=1&pageSize=20&warehouseId=1&status=0&source=1
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| warehouseId | Long | 否 | 仓库ID |
| status | Integer | 否 | 状态：0待领取 1已出库 2已取消 |
| source | Integer | 否 | 来源：1直接创建 2申请创建 |
| receiverId | Long | 否 | 领用人ID |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5,
    "list": [
      {
        "id": 1,
        "outboundNo": "CK_WL_20251110_0001",
        "warehouseId": 1,
        "warehouseName": "网络运维部仓库",
        "outboundType": 1,
        "outboundTypeName": "领用出库",
        "source": 2,
        "sourceName": "申请创建",
        "applyId": 1,
        "applyNo": "SQ_WL_20251110_0001",
        "receiverId": 4,
        "receiverName": "张强",
        "receiverPhone": "13800000003",
        "purpose": "XX小区光缆施工",
        "operatorId": 3,
        "operatorName": "李军",
        "outboundTime": null,
        "status": 0,
        "statusName": "待领取",
        "materialCount": 2,
        "totalQuantity": 12,
        "createTime": "2025-11-10 10:00:00",
        "waitDays": 1  // 待领取天数
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 7.2 出库单详情

#### 请求
```http
GET /api/outbounds/{id}
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "outboundNo": "CK_WL_20251110_0001",
    "warehouseId": 1,
    "warehouseName": "网络运维部仓库",
    "outboundType": 1,
    "outboundTypeName": "领用出库",
    "source": 2,
    "sourceName": "申请创建",
    "applyId": 1,
    "applyNo": "SQ_WL_20251110_0001",
    "receiverId": 4,
    "receiverName": "张强",
    "receiverPhone": "13800000003",
    "purpose": "XX小区光缆施工",
    "operatorId": 3,
    "operatorName": "李军",
    "outboundTime": null,
    "status": 0,
    "statusName": "待领取",
    "remark": "",
    "createTime": "2025-11-10 10:00:00",
    "details": [
      {
        "id": 1,
        "materialId": 1,
        "materialName": "光缆12芯",
        "materialCode": "GX001",
        "spec": "12芯单模",
        "unit": "条",
        "quantity": 10.00,
        "currentStock": 850.00,  // 当前库存
        "remark": ""
      },
      {
        "id": 2,
        "materialId": 6,
        "materialName": "H3C交换机",
        "materialCode": "SB002",
        "spec": "S5130-28S-EI",
        "unit": "台",
        "quantity": 2.00,
        "currentStock": 18.00,
        "remark": ""
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 7.3 创建出库单（直接出库）

#### 接口说明
仓管员直接创建出库单，立即扣减库存

#### 请求
```http
POST /api/outbounds
Authorization: Bearer {token}
Content-Type: application/json

{
  "warehouseId": 1,
  "outboundType": 1,
  "receiverId": 4,
  "purpose": "紧急工程维修",
  "remark": "",
  "details": [
    {
      "materialId": 1,
      "quantity": 10
    }
  ]
}
```

#### 响应
```json
{
  "code": 201,
  "message": "出库成功",
  "data": {
    "id": 200,
    "outboundNo": "CK_WL_20251110_0200"
  },
  "timestamp": 1699600000000
}
```

#### 错误响应（库存不足）
```json
{
  "code": 1001,
  "message": "库存不足",
  "data": {
    "materialName": "光缆12芯",
    "requestQuantity": 10,
    "currentStock": 5
  },
  "timestamp": 1699600000000
}
```

### 7.4 确认出库

#### 接口说明
员工到仓库领取物资，仓管员确认出库并扣减库存

#### 请求
```http
PUT /api/outbounds/{id}/confirm
Authorization: Bearer {token}
Content-Type: application/json

{
  "remark": "员工已签字确认"
}
```

#### 响应
```json
{
  "code": 200,
  "message": "出库确认成功，库存已更新",
  "data": {
    "outboundNo": "CK_WL_20251110_0001",
    "outboundTime": "2025-11-10 14:30:00"
  },
  "timestamp": 1699600000000
}
```

### 7.5 取消出库

#### 接口说明
取消待领取的出库单

#### 请求
```http
PUT /api/outbounds/{id}/cancel
Authorization: Bearer {token}
Content-Type: application/json

{
  "cancelReason": "员工已不需要"
}
```

#### 响应
```json
{
  "code": 200,
  "message": "出库单已取消",
  "data": null,
  "timestamp": 1699600000000
}
```

---

## 8. 申请审批

### 8.1 申请列表

#### 接口说明
查询申请单列表

#### 请求
```http
GET /api/applies?pageNum=1&pageSize=20&status=0&deptId=2&applicantId=4
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| status | Integer | 否 | 状态：0待审批 1已通过 2已拒绝 3已出库 4已取消 |
| deptId | Long | 否 | 部门ID |
| applicantId | Long | 否 | 申请人ID（查询我的申请）|
| approverId | Long | 否 | 审批人ID（查询我审批的）|
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 3,
    "list": [
      {
        "id": 1,
        "applyNo": "SQ_WL_20251110_0001",
        "applicantId": 4,
        "applicantName": "张强",
        "applicantPhone": "13800000003",
        "deptId": 2,
        "deptName": "网络运维部",
        "purpose": "XX小区光缆施工",
        "applyTime": "2025-11-10 09:30:00",
        "status": 0,
        "statusName": "待审批",
        "approverId": null,
        "approverName": null,
        "approvalTime": null,
        "materialCount": 2,
        "totalQuantity": 12,
        "isTimeout": false,  // 是否超时（>24小时）
        "waitHours": 5,  // 等待时长（小时）
        "createTime": "2025-11-10 09:30:00"
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 8.2 申请详情

#### 请求
```http
GET /api/applies/{id}
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "applyNo": "SQ_WL_20251110_0001",
    "applicantId": 4,
    "applicantName": "张强",
    "applicantPhone": "13800000003",
    "deptId": 2,
    "deptName": "网络运维部",
    "purpose": "XX小区光缆施工，需要铺设通信线路",
    "applyTime": "2025-11-10 09:30:00",
    "status": 0,
    "statusName": "待审批",
    "approverId": null,
    "approverName": null,
    "approvalTime": null,
    "approvalOpinion": null,
    "rejectReason": null,
    "outboundId": null,
    "outboundNo": null,
    "createTime": "2025-11-10 09:30:00",
    "details": [
      {
        "id": 1,
        "materialId": 1,
        "materialName": "光缆12芯",
        "materialCode": "GX001",
        "spec": "12芯单模",
        "unit": "条",
        "quantity": 10.00,
        "currentStock": 850.00,  // 当前库存
        "isStockSufficient": true,  // 库存是否充足
        "remark": ""
      },
      {
        "id": 2,
        "materialId": 6,
        "materialName": "H3C交换机",
        "materialCode": "SB002",
        "spec": "S5130-28S-EI",
        "unit": "台",
        "quantity": 2.00,
        "currentStock": 18.00,
        "isStockSufficient": true,
        "remark": ""
      }
    ],
    "stockCheckResult": {
      "allSufficient": true,  // 所有物资库存是否充足
      "insufficientMaterials": []  // 库存不足的物资列表
    }
  },
  "timestamp": 1699600000000
}
```

### 8.3 提交申请

#### 接口说明
普通员工提交领用申请

#### 请求
```http
POST /api/applies
Authorization: Bearer {token}
Content-Type: application/json

{
  "purpose": "XX小区光缆施工，需要铺设通信线路",
  "remark": "",
  "details": [
    {
      "materialId": 1,
      "quantity": 10
    },
    {
      "materialId": 6,
      "quantity": 2
    }
  ]
}
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| purpose | String | 是 | 领用用途，必填 |
| remark | String | 否 | 备注 |
| details | Array | 是 | 申请明细，至少1项 |

#### 响应
```json
{
  "code": 201,
  "message": "申请提交成功",
  "data": {
    "id": 100,
    "applyNo": "SQ_WL_20251110_0100",
    "status": 0,
    "statusName": "待审批"
  },
  "timestamp": 1699600000000
}
```

### 8.4 审批申请

#### 接口说明
仓管员审批员工申请，通过后自动创建出库单

#### 请求
```http
PUT /api/applies/{id}/approve
Authorization: Bearer {token}
Content-Type: application/json

{
  "result": 1,  // 1=通过 2=拒绝
  "opinion": "同意，请按时领取",
  "rejectReason": ""  // result=2时必填
}
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| result | Integer | 是 | 1=通过 2=拒绝 |
| opinion | String | 否 | 审批意见 |
| rejectReason | String | 条件 | 拒绝原因（拒绝时必填）|

#### 响应（通过）
```json
{
  "code": 200,
  "message": "审批成功",
  "data": {
    "applyNo": "SQ_WL_20251110_0001",
    "result": "通过",
    "outboundId": 200,
    "outboundNo": "CK_WL_20251110_0200"  // 自动创建的出库单号
  },
  "timestamp": 1699600000000
}
```

#### 响应（拒绝）
```json
{
  "code": 200,
  "message": "审批完成",
  "data": {
    "applyNo": "SQ_WL_20251110_0001",
    "result": "拒绝",
    "rejectReason": "库存不足，请减少数量"
  },
  "timestamp": 1699600000000
}
```

### 8.5 撤销申请

#### 接口说明
员工撤销待审批状态的申请

#### 请求
```http
PUT /api/applies/{id}/cancel
Authorization: Bearer {token}
Content-Type: application/json

{
  "cancelReason": "暂时不需要了"
}
```

#### 响应
```json
{
  "code": 200,
  "message": "申请已撤销",
  "data": null,
  "timestamp": 1699600000000
}
```

#### 错误响应（状态不允许）
```json
{
  "code": 1002,
  "message": "申请已被处理，无法撤销",
  "data": {
    "status": 1,
    "statusName": "已通过"
  },
  "timestamp": 1699600000000
}
```

### 8.6 待审批统计

#### 接口说明
获取当前用户待审批申请的统计信息

#### 请求
```http
GET /api/applies/pending-stats
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 5,  // 待审批总数
    "today": 3,  // 今日新增
    "timeout": 1,  // 超时未审批（>24小时）
    "avgApprovalTime": 3.5  // 平均审批时长（小时）
  },
  "timestamp": 1699600000000
}
```

---

## 9. 库存管理

### 9.1 库存列表

#### 请求
```http
GET /api/inventory?pageNum=1&pageSize=20&warehouseId=1&category=光缆类&status=0
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| warehouseId | Long | 否 | 仓库ID |
| category | String | 否 | 物资类别 |
| status | Integer | 否 | 库存状态：0正常 1低库存 2缺货 |
| keyword | String | 否 | 关键词（物资名称/编码）|

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 20,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1,
    "list": [
      {
        "id": 1,
        "warehouseId": 1,
        "warehouseName": "网络运维部仓库",
        "materialId": 1,
        "materialName": "光缆12芯",
        "materialCode": "GX001",
        "category": "光缆类",
        "spec": "12芯单模",
        "unit": "条",
        "quantity": 850.00,
        "lockedQuantity": 0.00,
        "availableQuantity": 850.00,
        "minStock": 100.00,
        "stockStatus": 0,  // 0=正常 1=低库存 2=缺货
        "stockStatusName": "正常",
        "stockValue": 1275000.00,  // 库存价值（quantity * price）
        "lastInboundTime": "2025-11-10 09:30:00",
        "lastOutboundTime": "2025-11-09 14:00:00",
        "updateTime": "2025-11-10 09:30:00"
      }
    ],
    "summary": {
      "totalMaterials": 20,  // 物资种类总数
      "normalCount": 17,  // 正常库存种类数
      "lowStockCount": 2,  // 低库存种类数
      "outOfStockCount": 1,  // 缺货种类数
      "totalValue": 1500000.00  // 库存总价值
    }
  },
  "timestamp": 1699600000000
}
```

### 9.2 库存详情

#### 接口说明
查询某个物资在指定仓库的库存详情和近期动态

#### 请求
```http
GET /api/inventory/{warehouseId}/{materialId}
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "warehouseId": 1,
    "warehouseName": "网络运维部仓库",
    "materialId": 1,
    "materialName": "光缆12芯",
    "materialCode": "GX001",
    "category": "光缆类",
    "spec": "12芯单模",
    "unit": "条",
    "price": 1500.00,
    "quantity": 850.00,
    "lockedQuantity": 0.00,
    "availableQuantity": 850.00,
    "minStock": 100.00,
    "stockStatus": 0,
    "stockStatusName": "正常",
    "stockValue": 1275000.00,
    "lastInboundTime": "2025-11-10 09:30:00",
    "lastOutboundTime": "2025-11-09 14:00:00",
    "recentLogs": [
      {
        "id": 100,
        "changeType": 1,  // 1=入库 2=出库
        "changeTypeName": "入库",
        "changeQuantity": 100.00,
        "beforeQuantity": 750.00,
        "afterQuantity": 850.00,
        "relatedNo": "RK_WL_20251110_0001",
        "operatorName": "李军",
        "remark": "采购入库",
        "createTime": "2025-11-10 09:30:00"
      },
      {
        "id": 99,
        "changeType": 2,
        "changeTypeName": "出库",
        "changeQuantity": -50.00,
        "beforeQuantity": 800.00,
        "afterQuantity": 750.00,
        "relatedNo": "CK_WL_20251109_0050",
        "operatorName": "李军",
        "remark": "张强领用",
        "createTime": "2025-11-09 14:00:00"
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 9.3 库存预警列表

#### 接口说明
查询所有库存预警（低库存和缺货）

#### 请求
```http
GET /api/inventory/alerts?warehouseId=1
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "lowStock": [
      {
        "warehouseId": 1,
        "warehouseName": "网络运维部仓库",
        "materialId": 10,
        "materialName": "网线超五类",
        "materialCode": "PJ001",
        "quantity": 8.00,
        "minStock": 500.00,
        "alertLevel": "低库存"
      }
    ],
    "outOfStock": [
      {
        "warehouseId": 1,
        "warehouseName": "网络运维部仓库",
        "materialId": 20,
        "materialName": "光功率计",
        "materialCode": "GJ004",
        "quantity": 0.00,
        "minStock": 5.00,
        "alertLevel": "缺货"
      }
    ],
    "summary": {
      "lowStockCount": 2,
      "outOfStockCount": 1
    }
  },
  "timestamp": 1699600000000
}
```

### 9.4 库存流水

#### 接口说明
查询库存变动流水

#### 请求
```http
GET /api/inventory/logs?pageNum=1&pageSize=20&warehouseId=1&materialId=1&changeType=1
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| warehouseId | Long | 否 | 仓库ID |
| materialId | Long | 否 | 物资ID |
| changeType | Integer | 否 | 变动类型：1入库 2出库 |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5,
    "list": [
      {
        "id": 100,
        "warehouseId": 1,
        "warehouseName": "网络运维部仓库",
        "materialId": 1,
        "materialName": "光缆12芯",
        "changeType": 1,
        "changeTypeName": "入库",
        "changeQuantity": 100.00,
        "beforeQuantity": 750.00,
        "afterQuantity": 850.00,
        "relatedNo": "RK_WL_20251110_0001",
        "relatedType": 1,  // 1=入库单 2=出库单
        "operatorId": 3,
        "operatorName": "李军",
        "remark": "采购入库",
        "createTime": "2025-11-10 09:30:00"
      }
    ]
  },
  "timestamp": 1699600000000
}
```

---

## 10. 统计报表

### 10.1 出入库统计

#### 接口说明
按日期统计出入库情况

#### 请求
```http
GET /api/stats/inoutbound?startDate=2025-11-01&endDate=2025-11-10&warehouseId=1&groupBy=day
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |
| warehouseId | Long | 否 | 仓库ID |
| groupBy | String | 否 | 分组：day/week/month |

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "chartData": [
      {
        "date": "2025-11-01",
        "inboundCount": 5,
        "outboundCount": 8,
        "inboundAmount": 50000.00,
        "outboundAmount": 0.00
      },
      {
        "date": "2025-11-02",
        "inboundCount": 3,
        "outboundCount": 12,
        "inboundAmount": 30000.00,
        "outboundAmount": 0.00
      }
    ],
    "summary": {
      "totalInboundCount": 50,
      "totalOutboundCount": 120,
      "totalInboundAmount": 500000.00,
      "avgInboundPerDay": 5.0,
      "avgOutboundPerDay": 12.0
    }
  },
  "timestamp": 1699600000000
}
```

### 10.2 物资使用统计

#### 接口说明
统计物资出库次数和数量

#### 请求
```http
GET /api/stats/material-usage?startDate=2025-11-01&endDate=2025-11-10&warehouseId=1&top=10
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |
| warehouseId | Long | 否 | 仓库ID |
| category | String | 否 | 物资类别 |
| top | Integer | 否 | Top N，默认10 |

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "materialId": 1,
      "materialName": "光缆12芯",
      "category": "光缆类",
      "unit": "条",
      "outboundCount": 25,  // 出库次数
      "totalQuantity": 500.00,  // 出库总量
      "avgQuantity": 20.00  // 平均每次出库量
    },
    {
      "materialId": 6,
      "materialName": "H3C交换机",
      "category": "设备类",
      "unit": "台",
      "outboundCount": 15,
      "totalQuantity": 50.00,
      "avgQuantity": 3.33
    }
  ],
  "timestamp": 1699600000000
}
```

### 10.3 审批统计

#### 接口说明
统计申请审批情况

#### 请求
```http
GET /api/stats/approval?startDate=2025-11-01&endDate=2025-11-10&deptId=2
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "summary": {
      "totalApplies": 100,  // 申请总数
      "pendingCount": 5,  // 待审批
      "approvedCount": 80,  // 已通过
      "rejectedCount": 10,  // 已拒绝
      "canceledCount": 5,  // 已取消
      "approvalRate": 80.0,  // 通过率(%)
      "avgApprovalTime": 3.5,  // 平均审批时长（小时）
      "timeoutCount": 2  // 超时未审批数
    },
    "chartData": [
      {
        "date": "2025-11-01",
        "totalCount": 10,
        "approvedCount": 8,
        "rejectedCount": 2
      }
    ],
    "topApprovers": [
      {
        "approverId": 3,
        "approverName": "李军",
        "approvalCount": 50,
        "avgApprovalTime": 2.5
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 10.4 工作台统计

#### 接口说明
获取当前用户工作台统计数据

#### 请求
```http
GET /api/stats/dashboard
Authorization: Bearer {token}
```

#### 响应（仓管员）
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "todayData": {
      "inboundCount": 5,
      "outboundCount": 12,
      "pendingApprovalCount": 3,
      "pendingPickupCount": 5
    },
    "pendingTasks": {
      "pendingApproval": 3,  // 待审批
      "pendingPickup": 5,  // 待领取
      "lowStockAlert": 2  // 库存预警
    },
    "recentOperations": [
      {
        "type": "inbound",
        "title": "入库 光缆12芯 100条",
        "time": "2025-11-10 09:30:00"
      },
      {
        "type": "approval",
        "title": "审批通过 张强的申请",
        "time": "2025-11-10 10:00:00"
      }
    ],
    "materialStats": {
      "totalMaterials": 20,
      "normalCount": 17,
      "lowStockCount": 2,
      "outOfStockCount": 1
    }
  },
  "timestamp": 1699600000000
}
```

#### 响应（普通员工）
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "myApplies": {
      "pendingCount": 1,
      "approvedCount": 2,
      "rejectedCount": 0,
      "pickupCount": 1  // 待领取
    },
    "recentApplies": [
      {
        "id": 100,
        "applyNo": "SQ_WL_20251110_0100",
        "status": 0,
        "statusName": "待审批",
        "applyTime": "2025-11-10 09:30:00"
      }
    ],
    "messages": [
      {
        "id": 500,
        "title": "申请审批通过",
        "content": "您的申请已通过，请到仓库领取",
        "time": "2025-11-10 10:00:00"
      }
    ]
  },
  "timestamp": 1699600000000
}
```

---

## 11. 消息通知

### 11.1 消息列表

#### 请求
```http
GET /api/messages?pageNum=1&pageSize=20&type=2&isRead=0
Authorization: Bearer {token}
```

**查询参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| type | Integer | 否 | 消息类型：1系统 2申请 3审批 4库存预警 |
| isRead | Integer | 否 | 是否已读：0未读 1已读 |

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 3,
    "unreadCount": 5,
    "list": [
      {
        "id": 100,
        "title": "申请审批通过",
        "content": "您的申请已通过，申请单号：SQ_WL_20251110_0001，请到网络运维部仓库领取",
        "type": 2,
        "typeName": "申请通知",
        "relatedId": 1,
        "relatedType": 1,  // 1=申请单 2=出库单
        "isRead": 0,
        "createTime": "2025-11-10 10:00:00"
      }
    ]
  },
  "timestamp": 1699600000000
}
```

### 11.2 标记已读

#### 请求
```http
PUT /api/messages/{id}/read
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1699600000000
}
```

### 11.3 全部标记已读

#### 请求
```http
PUT /api/messages/read-all
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "全部已读",
  "data": {
    "count": 5  // 标记数量
  },
  "timestamp": 1699600000000
}
```

### 11.4 未读数量

#### 请求
```http
GET /api/messages/unread-count
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 5,
    "byType": {
      "1": 1,  // 系统通知
      "2": 2,  // 申请通知
      "3": 1,  // 审批通知
      "4": 1   // 库存预警
    }
  },
  "timestamp": 1699600000000
}
```

---

## 12. 系统管理

### 12.1 部门列表

#### 请求
```http
GET /api/depts
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "deptName": "西藏电信",
      "deptCode": "ROOT",
      "parentId": 0,
      "sort": 0,
      "status": 0,
      "children": [
        {
          "id": 2,
          "deptName": "网络运维部",
          "deptCode": "WL",
          "parentId": 1,
          "leaderId": 2,
          "leaderName": "网运部管理员",
          "phone": "13800000001",
          "sort": 1,
          "status": 0,
          "userCount": 10,  // 人员数量
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1699600000000
}
```

### 12.2 角色列表

#### 请求
```http
GET /api/roles
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "roleName": "系统管理员",
      "roleCode": "admin",
      "roleLevel": 0,
      "description": "系统最高权限，管理所有功能",
      "status": 0,
      "userCount": 1,
      "createTime": "2025-01-01 00:00:00"
    }
  ],
  "timestamp": 1699600000000
}
```

### 12.3 菜单树

#### 请求
```http
GET /api/menus/tree
Authorization: Bearer {token}
```

#### 响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "id": 1,
      "menuName": "系统管理",
      "menuCode": "system",
      "path": "/system",
      "icon": "system",
      "menuType": 1,  // 1=目录 2=菜单 3=按钮
      "sort": 1,
      "children": [
        {
          "id": 101,
          "menuName": "用户管理",
          "menuCode": "user_manage",
          "path": "/system/user",
          "menuType": 2,
          "permission": "user:view",
          "sort": 1,
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1699600000000
}
```

---

## 13. 错误码说明

### 13.1 HTTP状态码

| 状态码 | 说明 |
|-------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或Token过期）|
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 冲突（数据重复等）|
| 500 | 服务器内部错误 |

### 13.2 业务错误码

| 错误码 | 说明 | 处理建议 |
|-------|------|---------|
| 1001 | 库存不足 | 提示用户减少数量或等待补货 |
| 1002 | 申请已被处理 | 提示用户无法操作 |
| 1003 | 审批超时 | 发送催办通知 |
| 1004 | 出库单已取消 | 提示用户重新申请 |
| 1005 | 物资已停用 | 选择其他物资 |
| 1006 | 仓库已禁用 | 选择其他仓库 |
| 1007 | 用户名已存在 | 更换用户名 |
| 1008 | 手机号已注册 | 更换手机号或找回账号 |
| 1009 | 密码错误 | 重新输入或重置密码 |
| 1010 | Token已过期 | 重新登录 |

### 13.3 错误响应示例

```json
{
  "code": 1001,
  "message": "库存不足",
  "data": {
    "materialName": "光缆12芯",
    "requestQuantity": 100,
    "currentStock": 50,
    "shortfall": 50
  },
  "timestamp": 1699600000000
}
```

---

## 附录

### A. 数据类型说明

| 类型 | 说明 | 示例 |
|-----|------|------|
| String | 字符串 | "admin" |
| Integer | 整数 | 1 |
| Long | 长整数 | 1234567890 |
| BigDecimal | 高精度数字 | 1500.00 |
| Boolean | 布尔值 | true/false |
| Date | 日期 | "2025-11-10" |
| DateTime | 日期时间 | "2025-11-10 09:30:00" |
| Array | 数组 | [] |
| Object | 对象 | {} |

### B. 时间格式

| 格式 | 说明 | 示例 |
|-----|------|------|
| yyyy-MM-dd | 日期 | 2025-11-10 |
| yyyy-MM-dd HH:mm:ss | 日期时间 | 2025-11-10 09:30:00 |
| timestamp | 时间戳（毫秒） | 1699600000000 |

### C. 常用枚举值

**用户状态**：
- 0: 启用
- 1: 禁用

**申请单状态**：
- 0: 待审批
- 1: 已通过
- 2: 已拒绝
- 3: 已出库
- 4: 已取消

**出库单状态**：
- 0: 待领取
- 1: 已出库
- 2: 已取消

**入库类型**：
- 1: 采购入库
- 2: 退货入库
- 3: 调拨入库
- 4: 其他

**出库类型**：
- 1: 领用出库
- 2: 报废出库
- 3: 调拨出库
- 4: 其他

**出库来源**：
- 1: 直接创建
- 2: 申请自动创建

**库存状态**：
- 0: 正常
- 1: 低库存
- 2: 缺货

**消息类型**：
- 1: 系统通知
- 2: 申请通知
- 3: 审批通知
- 4: 库存预警

### D. Postman导入

可以使用以下格式导入到Postman：

```json
{
  "info": {
    "name": "仓库管理系统API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "认证授权",
      "item": [
        {
          "name": "用户登录",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"admin\",\n  \"password\": \"123456\",\n  \"loginType\": 1\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/auth/login",
              "host": ["{{baseUrl}}"],
              "path": ["api", "auth", "login"]
            }
          }
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    }
  ]
}
```

---

**文档版本：v1.0**
**最后更新：2025-11-11**
**维护团队：技术开发组**
