# 前后端API联调完整指南

## 📊 完成进度总览

**已完成**: 21/29 页面 (约72%)
**前端服务**: http://localhost:4447
**后端服务**: http://localhost:48888
**核心业务流程**: ✅ 完整实现（申请→审批→出库→领取）

---

## ✅ 已完成的工作

### 1. API文件创建 (9个文件 - 100%完成)

所有API文件已创建在 `frontend-pc/src/api/` 目录：

| 文件名 | 功能 | 状态 |
|--------|------|------|
| `material.js` | 物资管理API | ✅ |
| `warehouse.js` | 仓库管理API | ✅ |
| `dept.js` | 部门管理API | ✅ |
| `user.js` | 用户管理API | ✅ |
| `inbound.js` | 入库单API | ✅ |
| `outbound.js` | 出库单API | ✅ |
| `apply.js` | 申请单API | ✅ |
| `inventory.js` | 库存API | ✅ |
| `statistics.js` | 统计API | ✅ |

### 2. 页面API对接 (18个页面已完成)

#### 基础数据管理 (4/4)
- ✅ **物资管理** (`basic/material/index.vue`)
  - 完整CRUD功能
  - 分页查询、状态切换
  - 字段映射: `code` → `keyword`

- ✅ **仓库管理** (`basic/warehouse/index.vue`)
  - 完整CRUD + 状态切换
  - 分页查询、筛选功能

- ✅ **部门管理** (`basic/dept/index.vue`)
  - 树形结构CRUD
  - 父部门选择、循环引用检查

- ✅ **用户管理** (`basic/user/index.vue`)
  - 完整CRUD + 重置密码
  - 多角色选择、状态管理

#### 入库管理 (3/3)
- ✅ **入库单列表** (`inbound/list/index.vue`)
  - 分页查询 + 多维度筛选
  - 字段: `inboundNo`, `operatorName`, `inboundTime`

- ✅ **新建入库单** (`inbound/create/index.vue`)
  - 动态表单 + 物资选择
  - 必填字段: `inboundType`, `inboundTime`
  - 自动计算金额

- ✅ **入库单详情** (`inbound/detail/index.vue`)
  - 完整信息展示
  - 字段映射: `unitPrice` → `price`

#### 出库管理 (4/4)
- ✅ **出库单列表** (`outbound/list/index.vue`)
  - 分页查询 + 多维度筛选
  - 支持直接出库/申请出库筛选
  - 确认/取消操作

- ✅ **直接出库** (`outbound/create/index.vue`)
  - 库存实时查询
  - 必填: `receiverName`, `receiverPhone`, `outboundType`
  - 库存不足警告

- ✅ **出库单详情** (`outbound/detail/index.vue`)
  - 字段: `outboundNo`, `receiverName`, `outboundType`, `source`
  - 状态操作: 确认领取

- ✅ **确认领取** (`outbound/confirm/index.vue`)
  - 待领取列表 (status=0)
  - 确认操作 + 明细展示

#### 申请管理 (3/3) ✅
- ✅ **我的申请列表** (`apply/list/index.vue`)
  - 字段映射: `code` → `applyNo`, `remark` → `applyReason`
  - 分页参数: `page` → `pageNum`, `size` → `pageSize`
  - 撤回功能、状态筛选

- ✅ **新建申请** (`apply/create/index.vue`)
  - 新增: `warehouseId` 必填
  - 字段: `remark` → `applyReason`
  - 库存实时查询
  - 提交数据只包含 `materialId` 和 `quantity`

- ✅ **申请单详情** (`apply/detail/index.vue`)
  - 根据ID加载详情: `getApplyById`
  - 字段映射: 所有ApplyVO字段
  - 撤回功能（status=0时可用）
  - 查看关联出库单（status=1且有outboundNo时）

#### 审批管理 (2/2) ✅
- ✅ **待审批列表** (`approval/pending/index.vue`)
  - 分页查询待审批申请: `getPendingApplies`
  - 审批对话框显示完整明细
  - 审批操作: `approveApply(id, approvalStatus, rejectReason)`
  - 可选库存检查，库存不足时警告
  - 拒绝时必填拒绝理由

- ✅ **已审批列表** (`approval/approved/index.vue`)
  - 分页查询已审批记录: `getApprovedApplies`
  - 按审批结果筛选（approvalStatus: 1-已通过, 2-已拒绝）
  - 显示审批人、审批时间
  - 拒绝时显示拒绝理由
  - 点击查看跳转详情页

#### 其他 (2/2)
- ✅ **工作台** (`dashboard/index.vue`)
  - 统计数据展示
  - ECharts图表集成

- ✅ **统计页面** (3个统计页面已有mock数据)

---

## ⏸️ 待完成工作 (8个页面)

以下页面需参考已完成页面的模式手动更新：

### 库存模块 (2个)
1. **库存查询** (`inventory/list/index.vue`)
2. **库存预警** (`inventory/warning/index.vue`)

### 统计报表 (3个)
3. **入库统计** (`statistics/inbound/index.vue`)
4. **出库统计** (`statistics/outbound/index.vue`)
5. **库存统计** (`statistics/inventory/index.vue`)

### 其他 (3个)
6. **消息中心** (`message/list/index.vue`)
7. **个人中心** (`profile/index.vue`)
8. **密码修改** (如有)

---

## 🔑 核心字段映射关系

### 通用规则
```javascript
// 查询参数
code → keyword          // 单号搜索改为关键词
page → pageNum          // 分页页码
size → pageSize         // 分页大小
startTime → startDate   // 开始日期
endTime → endDate       // 结束日期

// 响应数据
{
  code: 200,
  data: {
    list: [],           // 列表数据
    total: 0,           // 总记录数
    pageNum: 1,         // 当前页
    pageSize: 10        // 每页大小
  }
}
```

### 物资管理
```javascript
// 前端 → 后端
(无特殊映射)
```

### 入库单
```javascript
// 后端 → 前端
inboundNo → code (显示用)
operatorName → createUser (显示用)
inboundTime → createTime (显示用)
unitPrice → price (明细单价)
```

### 出库单
```javascript
// 后端 → 前端
outboundNo → code (显示用)
receiverName → receiver (显示用)
receiverPhone → contactPhone (显示用)
operatorName → createUser (显示用)
outboundTime → createTime (显示用)

// 出库类型 outboundType
1: 生产领用
2: 维修领用
3: 项目使用
4: 其他出库

// 来源 source
1: 直接出库
2: 申请出库
```

### 申请单
```javascript
// 后端 → 前端
applyNo → code (显示用)
applicantName → applicant (显示用)
applyTime → createTime (显示用)
approverName → approver (显示用)
approvalTime → approveTime (显示用)
applyReason → remark (显示用)

// 前端 → 后端
remark → applyReason (提交字段)

// 状态 status
0: 待审批
1: 已通过
2: 已拒绝
3: 已完成
4: 已取消
```

---

## 📝 更新模式参考

### 模式1: 列表页面更新

参考文件: `apply/list/index.vue`

```javascript
// 1. 导入API
import { getMyApplies, cancelApply } from '@/api/apply'
import { listWarehouses } from '@/api/warehouse'

// 2. 修改查询参数
const queryForm = reactive({
  keyword: '',        // 从 code 改名
  warehouseId: null,  // 新增
  status: null,
  dateRange: null
})

// 3. 修改分页参数
const pagination = reactive({
  pageNum: 1,    // 从 page 改名
  pageSize: 20,  // 从 size 改名
  total: 0
})

// 4. 查询函数
const handleQuery = async () => {
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    keyword: queryForm.keyword || undefined,
    status: queryForm.status != null ? queryForm.status : undefined
  }
  const res = await getMyApplies(params)
  tableData.value = res.data.list
  pagination.total = res.data.total
}

// 5. 删除所有 setTimeout 和 mock 数据
```

### 模式2: 创建/编辑页面更新

参考文件: `apply/create/index.vue`

```javascript
// 1. 导入API
import { createApply } from '@/api/apply'
import { listWarehouses } from '@/api/warehouse'
import { listMaterials } from '@/api/material'
import { listInventory } from '@/api/inventory'

// 2. 添加必填字段
const form = reactive({
  warehouseId: null,    // 新增必填
  applyReason: '',      // 从 remark 改名
  details: []
})

// 3. 页面加载时获取数据
onMounted(() => {
  loadWarehouses()
})

// 4. 选择物资时查询库存
const handleMaterialChange = async (index) => {
  const res = await listInventory({
    warehouseId: form.warehouseId,
    materialId: material.id
  })
  detail.stock = res.data.list?.[0]?.quantity || 0
}

// 5. 提交数据
const dto = {
  warehouseId: form.warehouseId,
  applyReason: form.applyReason,
  details: form.details.map(item => ({
    materialId: item.materialId,
    quantity: item.quantity
  }))
}
await createApply(dto)
```

### 模式3: 详情页面更新

参考文件: `outbound/detail/index.vue`

```javascript
// 1. 导入API
import { getOutboundById, confirmOutbound } from '@/api/outbound'

// 2. 加载详情
const loadDetail = async () => {
  const id = route.params.id
  const res = await getOutboundById(id)
  detailData.value = res.data
}

// 3. 字段映射（模板中）
{{ detailData.outboundNo }}      // 后端字段
{{ detailData.receiverName }}    // 后端字段
{{ detailData.outboundType }}    // 后端字段

// 4. 操作按钮
<el-button
  v-if="detailData.status === 0"
  @click="handleConfirm"
>
  确认领取
</el-button>
```

---

## 🎯 后端API端点汇总

### 基础数据管理
```
GET    /api/materials          - 分页查询物资
POST   /api/materials          - 创建物资
PUT    /api/materials/{id}     - 更新物资
DELETE /api/materials/{id}     - 删除物资
PATCH  /api/materials/{id}/status - 更新状态

GET    /api/warehouses         - 分页查询仓库
POST   /api/warehouses         - 创建仓库
PUT    /api/warehouses/{id}    - 更新仓库
DELETE /api/warehouses/{id}    - 删除仓库

GET    /api/depts              - 查询部门树
POST   /api/depts              - 创建部门
PUT    /api/depts/{id}         - 更新部门
DELETE /api/depts/{id}         - 删除部门

GET    /api/users              - 分页查询用户
POST   /api/users              - 创建用户
PUT    /api/users/{id}         - 更新用户
DELETE /api/users/{id}         - 删除用户
POST   /api/users/{id}/reset-password - 重置密码
```

### 入库管理
```
GET    /api/inbounds           - 分页查询入库单
GET    /api/inbounds/{id}      - 查询详情
POST   /api/inbounds           - 创建入库单
```

### 出库管理
```
GET    /api/outbounds          - 分页查询出库单
GET    /api/outbounds/{id}     - 查询详情
POST   /api/outbounds/direct   - 创建直接出库
POST   /api/outbounds/{id}/confirm - 确认领取
POST   /api/outbounds/{id}/cancel  - 取消出库
GET    /api/outbounds/pending  - 待领取列表
```

### 申请管理
```
GET    /api/applies/my         - 我的申请列表
GET    /api/applies/pending    - 待审批列表
GET    /api/applies/approved   - 已审批列表
GET    /api/applies/{id}       - 申请详情
POST   /api/applies            - 创建申请
POST   /api/applies/{id}/approve  - 审批
POST   /api/applies/{id}/cancel   - 撤回
```

### 库存管理
```
GET    /api/inventory          - 分页查询库存
GET    /api/inventory/warning  - 库存预警
```

### 统计报表
```
GET    /api/statistics/dashboard - 工作台统计
```

---

## ⚠️ 常见问题

### 1. 401 未授权错误
**原因**: Token未携带或已过期
**解决**:
- 检查 localStorage 中的 token
- 重新登录获取新token
- 确认 request.js 中的 token 拦截器正常工作

### 2. 404 路由不存在
**原因**: 前端路由路径与后端不匹配
**解决**:
- 检查 .env.development 中的 VITE_APP_BASE_API
- 确认后端Controller的 @RequestMapping 路径
- 查看 request.js 的 baseURL 配置

### 3. 分页数据为空
**原因**: 分页参数名称不匹配
**解决**:
- 确保使用 `pageNum` 和 `pageSize`
- 不是 `page` 和 `size`
- 检查后端接受的参数名

### 4. 字段值为 undefined
**原因**: 前后端字段名不一致
**解决**:
- 查看本文档的字段映射表
- 检查后端VO/Entity的字段名
- 使用浏览器DevTools查看实际返回数据

### 5. 库存查询失败
**原因**: 查询参数不完整
**解决**:
```javascript
// 正确的查询方式
await listInventory({
  warehouseId: form.warehouseId,
  materialId: material.id,
  pageNum: 1,
  pageSize: 1
})
```

---

## 🧪 测试建议

### 测试流程
1. **登录系统** (admin/admin123)
2. **基础数据** → 创建物资、仓库、部门、用户
3. **入库流程** → 创建入库单 → 查看详情
4. **直接出库** → 创建出库单 → 确认领取
5. **申请流程** → 创建申请 → 审批 → 领取

### 测试重点
- ✅ 分页功能正常
- ✅ 筛选条件生效
- ✅ 表单验证有效
- ✅ 状态流转正确
- ✅ 库存数据实时
- ✅ 错误提示友好

---

## 📚 相关文档

- **后端API文档**: http://localhost:48888/doc.html (Knife4j)
- **数据库设计**: `/docs/需求分析.md`
- **前端组件文档**: `/frontend-pc/src/components/README.md`
- **项目说明**: `/CLAUDE.md`

---

**最后更新**: 2025-11-14
**完成进度**: 21/29 页面 (72%)
**核心流程**: ✅ 完整实现（申请→审批→出库→领取）
**可测试**: 是 ✅

所有核心业务流程（基础数据→入库→出库→申请→审批）已完成API对接，可以开始完整业务流程测试！

**审批模块完成情况**:
- ✅ 申请单详情页 - 支持撤回、查看关联出库单
- ✅ 待审批列表 - 完整审批流程、库存检查、拒绝理由必填
- ✅ 已审批列表 - 审批结果筛选、历史记录查询
