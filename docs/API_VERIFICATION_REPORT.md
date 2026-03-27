# CT-Tibet-WMS 后端服务验证报告

**报告日期**: 2025-12-28
**报告人**: Claude Code Agent (Haiku 4.5)
**测试范围**: Spring Boot后端服务启动、编译验证、核心API功能测试
**测试环境**:
- 后端服务: http://localhost:9090
- 数据库: MySQL 8.0 (localhost:3306)
- 操作系统: Windows 11
- Java版本: 17.0.16
- Maven版本: 3.8.9

---

## 📊 测试总览 (2025-12-28 新增)

| 测试项 | 状态 | 说明 |
|-------|------|------|
| Maven编译 | ✅ 通过 | 68.2 MB JAR文件生成成功 |
| 服务启动 | ✅ 通过 | 5.029秒启动，运行在端口9090 |
| MySQL连接 | ✅ 通过 | 数据库正常初始化，7个部门，20个物资 |
| 登录API | ✅ 通过 | JWT认证正常工作，生成有效token |
| 仪表盘API | ✅ 通过(修复后) | 数据库Schema缺陷已修复 |
| 库存统计API | ✅ 通过 | 返回完整的库存统计数据 |
| 安全过滤 | ✅ 通过 | Spring Security正确配置，13个过滤器链 |
| 初始化数据 | ✅ 通过 | 用户、角色、部门、物资数据完整 |

---

## 🔴 发现的关键问题 (2025-12-28)

### 问题1: tb_outbound表缺少total_amount列 (严重)

**问题描述**:
- Java实体类`Outbound`声明了`totalAmount`字段 (BigDecimal类型)
- 应用代码在SQL查询中使用此列
- MySQL表`tb_outbound`中实际缺少此列
- 导致查询出库统计数据时返回500错误

**错误信息**:
```
SQLSyntaxErrorException: Unknown column 'total_amount' in 'field list'
```

**修复方案**:
```sql
ALTER TABLE ct_tibet_wms.tb_outbound
ADD COLUMN total_amount DECIMAL(10, 2) DEFAULT 0 COMMENT '总金额';
```

**验证结果**: ✅ 列添加成功，所有关联API恢复正常

**建议**:
1. 更新database/schema.sql文件，确保包含此列定义
2. 实施Flyway或Liquibase数据库版本控制
3. 在部署前添加Schema验证步骤

---

## 📊 早期测试总览 (2025-12-25)

| 测试项 | 状态 | 说明 |
|-------|------|------|
| 系统启动 | ✅ 通过 | MySQL、后端、前端全部正常运行 |
| 数据库Schema | ✅ 通过 | 修复了remark和deleted字段问题 |
| 登录认证 | ✅ 通过 | JWT认证正常工作 |
| 仓库API | ✅ 通过 | 之前的500错误已修复 |
| 部门API | ⚠️ 部分通过 | 后端正常，前端使用了错误的端点路径 |
| 物资API | ⚠️ 部分通过 | 响应格式存在双层包装问题 |
| 用户API | ⚠️ 部分通过 | 响应格式存在双层包装问题 |

---

## ✅ 成功修复的问题

### 1. 数据库Schema错误 (P0)

**问题描述**:
- tb_dept表缺少`remark`字段
- 逻辑删除字段命名不一致（数据库使用`is_deleted`，MyBatis-Plus配置使用`deleted`）

**修复措施**:
```sql
-- 添加缺失的remark字段
ALTER TABLE tb_dept ADD COLUMN remark VARCHAR(500) NULL COMMENT '备注' AFTER sort;

-- 统一逻辑删除字段名
ALTER TABLE tb_dept CHANGE COLUMN is_deleted deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除 1-已删除';
```

**验证结果**: ✅ 数据库Schema与代码定义一致，查询正常

---

### 2. 仓库列表API (P0)

**问题描述**: 用户报告的500 Internal Server Error
```
GET http://localhost:48888/api/warehouses?pageNum=1&pageSize=20&warehouseName=&manager= 500 (Internal Server Error)
```

**根本原因**: 数据库Schema问题（remark和deleted字段）

**修复结果**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "warehouseName": "网络运维部仓库",
      "warehouseCode": "WH_WL_001",
      "deptId": 2,
      "address": "西藏拉萨市城关区江苏路1号1楼",
      "managerId": 3,
      "capacity": 120.00,
      "status": 0,
      "deptName": "网络运维部",
      "managerName": "李军",
      "deleted": 0
    }
    // ... 更多仓库数据
  ],
  "timestamp": "2025-12-25T23:31:51.8515323"
}
```

**验证状态**: ✅ 通过 - API正常返回7个仓库数据

---

### 3. 登录认证API

**测试请求**:
```bash
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

**响应结果**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "phone": "13800000000",
      "deptId": 1,
      "roleId": 1,
      "roleName": "系统管理员",
      "roleCode": "admin"
    }
  }
}
```

**验证状态**: ✅ 通过 - JWT token生成正常，过期时间2小时

---

### 4. 部门列表API

**测试请求**:
```bash
curl -X GET "http://localhost:48888/api/depts/all" \
  -H "Authorization: Bearer {token}"
```

**响应结果**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "deptName": "西藏电信",
      "deptCode": "ROOT",
      "parentId": 0,  // ✅ 正确使用0表示顶级部门
      "ancestors": "0",
      "status": 0,
      "sort": 0,
      "deleted": 0
    },
    {
      "id": 2,
      "deptName": "网络运维部",
      "deptCode": "WL",
      "parentId": 1,
      "ancestors": "0,1"
    }
    // ... 更多部门数据
  ]
}
```

**验证状态**: ✅ 通过 - parentId=0修复已生效

---

## ⚠️ 发现的新问题

### 问题1: 前端使用错误的部门API端点 (P1)

**问题描述**:
- 后端端点: `/api/depts/all` 或 `/api/depts/tree`
- 前端使用: `/api/dept/list` (404 Not Found)

**影响范围**: 部门管理页面无法加载数据

**建议修复**:
修改前端API调用，将 `/api/dept/list` 改为 `/api/depts/all`

**相关文件**: `frontend-pc/src/api/dept.js` 或相关部门管理页面

---

### 问题2: 分页API响应双层包装 (P1)

**问题描述**:
物资和用户列表API返回了双层嵌套的响应结构

**物资API响应**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {                    // ⚠️ 第一层包装
    "code": 200,               // ⚠️ 内层还有code/message
    "message": "Success",
    "data": [                  // ⚠️ 实际数据在内层的data中
      {"id": 1, "materialName": "光缆12芯", ...},
      {"id": 2, "materialName": "光缆24芯", ...}
    ],
    "total": 20,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1,
    "timestamp": "2025-12-25T23:31:52.9251383"
  },
  "timestamp": "2025-12-25T23:31:52.9251383"
}
```

**用户API响应**: 同样的双层包装问题

**影响范围**:
- 物资列表页面
- 用户列表页面
- 可能还有其他使用PageResult的页面

**根本原因**:
Controller返回 `Result.success(PageResult.of(page))`，PageResult本身已经包含了code/message，Result再次包装导致双层嵌套

**后端代码位置**:
`backend/src/main/java/com/ct/wms/controller/MaterialController.java:41`
```java
@GetMapping
public Result<PageResult<Material>> listMaterials(MaterialQueryDTO queryDTO) {
    Page<Material> page = materialService.listMaterials(queryDTO);
    return Result.success(PageResult.of(page));  // ⚠️ 双层包装
}
```

**建议修复方案**:

**方案A: 修改后端Controller (推荐)**
```java
@GetMapping
public PageResult<Material> listMaterials(MaterialQueryDTO queryDTO) {
    Page<Material> page = materialService.listMaterials(queryDTO);
    return PageResult.of(page);  // 直接返回PageResult
}
```

**方案B: 修改前端访问路径**
```javascript
// 当前访问方式 (错误)
const res = await someAPI(params)
tableData.value = res.data
pagination.total = res.total

// 修改为访问内层数据 (临时方案)
const res = await someAPI(params)
tableData.value = res.data.data      // 访问内层的data
pagination.total = res.data.total    // 访问内层的total
```

**推荐使用方案A**，因为：
1. 保持响应格式一致性（仓库API返回的是单层Result）
2. 符合前端已实现的优化规范
3. 减少数据冗余

---

## 📋 测试详细结果

### 1. 登录API

**端点**: `POST /api/auth/login`

**请求**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应**: ✅ 200 OK
- JWT Token: 正常生成
- 过期时间: 7200秒 (2小时)
- 用户信息: 完整返回

---

### 2. 仓库列表API

**端点**: `GET /api/warehouses?pageNum=1&pageSize=20`

**响应**: ✅ 200 OK
- 返回7个仓库数据
- 数据格式: 单层Result包装，data直接是数组
- deleted字段: 正常显示

**对比之前**: ❌ 500 Internal Server Error → ✅ 200 OK

---

### 3. 部门列表API

**端点**: `GET /api/depts/all`

**响应**: ✅ 200 OK
- 返回8个部门数据
- parentId=0: 顶级部门正确标识
- deleted字段: 正常显示

**前端错误端点**: ❌ GET /api/dept/list → 404 Not Found

---

### 4. 物资列表API

**端点**: `GET /api/materials?pageNum=1&pageSize=20`

**响应**: ⚠️ 200 OK (但格式有问题)
- 返回20条物资数据
- **问题**: 双层包装 `{code, data:{code, data:[...], total}}`
- 分页信息: total=20, pageNum=1, pageSize=20, pages=1 (在内层)

---

### 5. 用户列表API

**端点**: `GET /api/users?pageNum=1&pageSize=20`

**响应**: ⚠️ 200 OK (但格式有问题)
- 返回5条用户数据
- **问题**: 双层包装 `{code, data:{code, data:[...], total}}`
- 分页信息: total=5, pageNum=1, pageSize=20, pages=1 (在内层)

---

## 🔧 前后端数据格式对比

### 正确的单层格式 (仓库API、部门API)

```json
{
  "code": 200,
  "message": "Success",
  "data": [...]  // 数据直接在data中
}
```

### 前端正确访问方式
```javascript
const res = await warehouseAPI(params)
tableData.value = res.data      // ✅ 直接访问
pagination.total = res.total    // ✅ 没有total字段(非分页接口)
```

---

### 错误的双层格式 (物资API、用户API)

```json
{
  "code": 200,
  "message": "Success",
  "data": {              // ⚠️ 外层包装
    "code": 200,
    "message": "Success",
    "data": [...],       // ⚠️ 实际数据在内层
    "total": 20
  }
}
```

### 前端当前访问方式（已优化为单层）
```javascript
const res = await materialAPI(params)
tableData.value = res.data      // ❌ 错误：访问到的是 {code, data, total} 对象
pagination.total = res.total    // ❌ 错误：外层没有total字段
```

### 前端应该的临时访问方式（如果后端不改）
```javascript
const res = await materialAPI(params)
tableData.value = res.data.data      // ⚠️ 访问内层的data数组
pagination.total = res.data.total    // ⚠️ 访问内层的total
```

---

## 📝 修复建议优先级

### P0 - 严重问题（阻塞功能）
✅ **已修复**: 数据库Schema问题
✅ **已修复**: 仓库API 500错误

### P1 - 重要问题（影响用户体验）

**问题1**: 前端使用错误的部门API端点
- **影响**: 部门管理页面无法加载
- **修复文件**: 前端API调用代码
- **修复内容**: 将 `/api/dept/list` 改为 `/api/depts/all`

**问题2**: 分页API响应双层包装
- **影响**: 物资、用户列表页面数据渲染错误
- **修复文件**:
  - `backend/src/main/java/com/ct/wms/controller/MaterialController.java`
  - `backend/src/main/java/com/ct/wms/controller/UserController.java`
  - 其他使用PageResult的Controller
- **修复方式**:
  - 选项A（推荐）: Controller直接返回PageResult，不用Result包装
  - 选项B（临时）: 前端访问内层数据 `res.data.data`

---

## 🎯 已验证的优化成果

### ✅ P0修复验证

1. **部门管理parentId修复** - ✅ 生效
   - 顶级部门使用parentId=0
   - 数据库和API响应一致

2. **分页数据渲染修复** - ⚠️ 部分生效
   - 仓库API: 单层格式正确 ✅
   - 部门API: 单层格式正确 ✅
   - 物资API: 双层格式错误 ❌
   - 用户API: 双层格式错误 ❌

### ✅ P1优化验证

1. **分页参数统一** - 需要前端配合测试
   - 后端接受pageNum/pageSize参数 ✅
   - 前端是否统一使用需要UI测试

2. **冗余代码移除** - 需要前端代码审查
   - 后端拦截器已处理错误码
   - 前端是否移除了重复检查需要代码审查

---

## 🚀 后续行动计划

### 立即行动（今天完成）

1. ✅ **修复数据库Schema** - 已完成
2. ✅ **验证核心API** - 已完成
3. ⏳ **修复部门API端点** - 待执行
4. ⏳ **修复双层包装问题** - 待执行

### 短期计划（1-2天）

1. **前端UI功能测试**
   - 测试部门管理创建功能
   - 测试物资列表分页
   - 测试用户列表分页
   - 验证所有列表页面数据正常显示

2. **补充API测试**
   - 入库列表API
   - 出库列表API
   - 申请列表API
   - 审批列表API
   - 库存查询API

3. **性能测试**
   - 页面加载时间
   - API响应时间
   - 并发请求测试

### 中期计划（1周内）

1. **代码质量审查**
   - 检查所有Controller的响应格式
   - 统一前端API调用方式
   - 添加单元测试

2. **文档更新**
   - 更新API文档
   - 更新前后端交互规范
   - 更新开发指南

---

## 📊 测试数据统计

| 指标 | 数值 |
|------|------|
| 测试API端点 | 5个 |
| 通过的API | 3个 (60%) |
| 部分通过的API | 2个 (40%) |
| 失败的API | 0个 |
| 发现的新问题 | 2个 (P1级别) |
| 修复的问题 | 2个 (P0级别) |
| 数据库Schema修复 | 2个字段 |

---

## 🔍 附录: 完整测试命令

### 测试脚本

已创建测试脚本: `test_core_apis.bat`

### 手动测试命令

```bash
# 1. 登录获取Token
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 2. 测试仓库API
curl -X GET "http://localhost:48888/api/warehouses?pageNum=1&pageSize=20" \
  -H "Authorization: Bearer {token}"

# 3. 测试部门API
curl -X GET "http://localhost:48888/api/depts/all" \
  -H "Authorization: Bearer {token}"

# 4. 测试物资API
curl -X GET "http://localhost:48888/api/materials?pageNum=1&pageSize=20" \
  -H "Authorization: Bearer {token}"

# 5. 测试用户API
curl -X GET "http://localhost:48888/api/users?pageNum=1&pageSize=20" \
  -H "Authorization: Bearer {token}"
```

---

## ✅ 结论

### 成功修复
1. ✅ 数据库Schema问题 - remark和deleted字段
2. ✅ 仓库API 500错误 - 现已正常工作
3. ✅ 登录认证 - JWT正常生成
4. ✅ 部门parentId - 正确使用0表示顶级

### 待修复问题
1. ⚠️ 前端部门API端点路径错误 (P1)
2. ⚠️ 分页API响应双层包装 (P1)

### 系统状态
**后端**: ✅ 正常运行 (http://localhost:48888)
**前端**: ✅ 正常运行 (http://localhost:4446)
**数据库**: ✅ 正常运行，Schema已修复

**总体评估**: 🟡 基本可用，存在2个P1级别问题需要修复

---

**报告生成时间**: 2025-12-25 23:35
**测试执行人员**: Claude Sonnet 4.5
**下一步**: 修复部门API端点和双层包装问题
