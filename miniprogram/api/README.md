# CT-Tibet-WMS 小程序 API 接口文档

本文档说明小程序API的使用方式、错误处理规范和示例代码。

## 目录结构

```
api/
├── index.js          # API统一入口
├── auth.js           # 认证授权接口
├── apply.js          # 申请管理接口（员工端）
├── approval.js       # 审批管理接口（仓管端）
├── inventory.js      # 库存管理接口
├── inbound.js        # 入库管理接口（仓管端）
├── outbound.js       # 出库管理接口（仓管端）
├── message.js        # 消息管理接口
├── user.js           # 用户管理接口
├── common.js         # 公共接口（物资、仓库、部门等）
└── README.md         # 本文档
```

## API调用方式

### 方式一：统一导入（推荐）

```javascript
import api from '@/api'

// 在页面或组件的methods中使用
export default {
  data() {
    return {
      list: []
    }
  },

  methods: {
    async loadData() {
      try {
        const res = await api.apply.getMyApplies({
          pageNum: 1,
          pageSize: 10
        })

        if (res.code === 200) {
          this.list = res.data.list
        }
      } catch (error) {
        console.error('加载数据失败', error)
      }
    }
  }
}
```

### 方式二：按需导入

```javascript
import { getMyApplies, createApply } from '@/api/apply.js'

export default {
  methods: {
    async loadApplies() {
      const res = await getMyApplies({ pageNum: 1, pageSize: 10 })
      // 处理响应
    },

    async submitApply(data) {
      const res = await createApply(data)
      // 处理响应
    }
  }
}
```

## 响应格式

所有API接口统一返回格式：

```javascript
{
  code: 200,           // 状态码：200成功，其他失败
  msg: "操作成功",      // 提示信息
  data: {              // 响应数据（具体结构因接口而异）
    // 列表接口
    total: 100,        // 总记录数
    list: [],          // 数据列表

    // 或单个对象
    id: 1,
    name: "xxx"
  }
}
```

## 分页参数

列表查询接口统一使用以下分页参数：

```javascript
{
  pageNum: 1,          // 页码，从1开始
  pageSize: 10,        // 每页条数
  // 其他查询条件...
}
```

## 错误处理

### 自动错误处理

`utils/request.js` 已实现自动错误处理：

- HTTP 401：自动跳转登录页
- HTTP 403：提示无权限
- 业务错误码：自动显示错误提示
- 网络错误：显示网络连接失败

### 手动错误处理

如需自定义错误处理，使用try-catch：

```javascript
async loadData() {
  try {
    const res = await api.apply.getMyApplies({ pageNum: 1, pageSize: 10 })

    if (res.code === 200) {
      this.list = res.data.list
    } else {
      // 自定义错误处理
      uni.showToast({
        title: res.msg || '加载失败',
        icon: 'none'
      })
    }
  } catch (error) {
    // 捕获网络错误或其他异常
    console.error('加载失败', error)
  }
}
```

## 常见业务错误码

```javascript
1001: 库存不足
1002: 申请已被处理
1003: 审批超时
1004: 出库单已取消
1005: 物资已停用
1006: 仓库已禁用
1007: 用户名已存在
1008: 手机号已注册
1009: 密码错误
1010: Token已过期
```

## API接口列表

### 1. 认证授权 (auth.js)

```javascript
// 用户登录
api.auth.login({ username: 'admin', password: '123456' })

// 微信小程序登录
api.auth.wechatLogin({ code, encryptedData, iv })

// 退出登录
api.auth.logout()

// 刷新Token
api.auth.refreshToken()

// 获取当前用户信息
api.auth.getCurrentUser()
```

### 2. 申请管理 (apply.js) - 员工端

```javascript
// 创建申请
api.apply.createApply({
  warehouseId: 1,
  applyReason: '生产需要',
  details: [
    { materialId: 1, quantity: 10 },
    { materialId: 2, quantity: 5 }
  ]
})

// 我的申请列表
api.apply.getMyApplies({
  pageNum: 1,
  pageSize: 10,
  status: 0  // 可选：0-待审批 1-已通过 2-已拒绝 3-已完成 4-已取消
})

// 申请详情
api.apply.getApplyDetail(1)

// 撤销申请
api.apply.cancelApply(1)

// 申请状态统计
api.apply.getApplyStats()
```

### 3. 审批管理 (approval.js) - 仓管端

```javascript
// 待审批列表
api.approval.getPendingApproval({
  pageNum: 1,
  pageSize: 10
})

// 已审批列表
api.approval.getApprovedList({
  pageNum: 1,
  pageSize: 10,
  approvalStatus: 1  // 可选：1-已通过 2-已拒绝
})

// 审批操作
api.approval.approveApply(1, {
  approvalStatus: 1,      // 1-通过 2-拒绝
  rejectReason: '理由'    // 拒绝时必填
})

// 批量审批
api.approval.batchApprove({
  ids: [1, 2, 3],
  approvalStatus: 1
})

// 待审批统计
api.approval.getPendingStats()
```

### 4. 库存管理 (inventory.js)

```javascript
// 库存列表
api.inventory.getInventoryList({
  pageNum: 1,
  pageSize: 10,
  warehouseId: 1,  // 可选
  keyword: '电缆'  // 可选
})

// 库存详情
api.inventory.getInventoryDetail(1)

// 搜索库存
api.inventory.searchInventory('电缆', 1)

// 库存预警列表
api.inventory.getWarningList({ pageNum: 1, pageSize: 10 })

// 库存统计
api.inventory.getInventoryStats(1)

// 库存变动记录
api.inventory.getInventoryHistory({ inventoryId: 1 })
```

### 5. 入库管理 (inbound.js) - 仓管端

```javascript
// 创建入库单
api.inbound.createInbound({
  warehouseId: 1,
  inboundDate: '2025-11-24',
  supplier: '供应商名称',
  remark: '备注',
  details: [
    { materialId: 1, quantity: 100, price: 50 }
  ]
})

// 入库记录列表
api.inbound.getInboundList({
  pageNum: 1,
  pageSize: 10,
  warehouseId: 1
})

// 入库详情
api.inbound.getInboundDetail(1)

// 入库统计
api.inbound.getInboundStats({ warehouseId: 1 })

// 删除入库单
api.inbound.deleteInbound(1)
```

### 6. 出库管理 (outbound.js) - 仓管端

```javascript
// 创建直接出库单
api.outbound.createOutbound({
  warehouseId: 1,
  receiverId: 10,
  outboundDate: '2025-11-24',
  purpose: '生产使用',
  details: [
    { materialId: 1, quantity: 10 }
  ]
})

// 待领取列表
api.outbound.getPendingOutbound({
  pageNum: 1,
  pageSize: 10
})

// 确认领取
api.outbound.confirmOutbound(1)

// 出库记录列表
api.outbound.getOutboundList({
  pageNum: 1,
  pageSize: 10,
  outboundType: 1,  // 可选：1-直接出库 2-申请出库
  status: 0         // 可选：0-待取货 1-已完成 2-已取消
})

// 出库详情
api.outbound.getOutboundDetail(1)

// 取消出库单
api.outbound.cancelOutbound(1, '取消原因')

// 出库统计
api.outbound.getOutboundStats({ warehouseId: 1 })
```

### 7. 消息管理 (message.js)

```javascript
// 消息列表
api.message.getMessages({
  pageNum: 1,
  pageSize: 10,
  messageType: 2,   // 可选：1-系统通知 2-审批通知 3-出库通知
  readStatus: 0     // 可选：0-未读 1-已读
})

// 未读消息数量
api.message.getUnreadCount()

// 标记已读
api.message.markRead(1)

// 标记全部已读
api.message.markAllRead()

// 删除消息
api.message.deleteMessage(1)

// 批量删除消息
api.message.batchDeleteMessages([1, 2, 3])

// 消息详情
api.message.getMessageDetail(1)
```

### 8. 用户管理 (user.js)

```javascript
// 获取个人信息
api.user.getUserProfile()

// 更新个人信息
api.user.updateProfile({
  realName: '张三',
  phone: '13800138000',
  email: 'zhangsan@example.com',
  avatar: 'https://example.com/avatar.jpg'
})

// 修改密码
api.user.changePassword({
  oldPassword: '123456',
  newPassword: 'abc123',
  confirmPassword: 'abc123'
})

// 用户列表（仓管查看部门用户）
api.user.getUserList({ pageNum: 1, pageSize: 10 })

// 用户详情
api.user.getUserById(1)

// 绑定微信
api.user.bindWechat({ code, encryptedData, iv })

// 解绑微信
api.user.unbindWechat()
```

### 9. 公共接口 (common.js)

```javascript
// 物资列表
api.common.getMaterials({ pageNum: 1, pageSize: 10 })

// 物资详情
api.common.getMaterialById(1)

// 搜索物资（用于选择器）
api.common.searchMaterials('电缆')

// 仓库列表
api.common.getWarehouses({ pageNum: 1, pageSize: 10 })

// 仓库详情
api.common.getWarehouseById(1)

// 我的仓库列表
api.common.getMyWarehouses()

// 部门列表
api.common.getDepartments()

// 部门详情
api.common.getDepartmentById(1)

// 文件上传
api.common.uploadFile(filePath, {
  name: 'file',
  formData: { type: 'image' }
})

// 物资分类列表
api.common.getMaterialCategories()

// 数据字典
api.common.getDictData('material_type')

// 首页统计
api.common.getStatistics()
```

## 完整使用示例

### 示例1：申请单列表页面

```javascript
<template>
  <view class="container">
    <view v-for="item in list" :key="item.id">
      {{ item.applyNo }} - {{ item.statusText }}
    </view>
  </view>
</template>

<script>
import api from '@/api'

export default {
  data() {
    return {
      list: [],
      pageNum: 1,
      pageSize: 10,
      total: 0
    }
  },

  onLoad() {
    this.loadData()
  },

  methods: {
    async loadData() {
      uni.showLoading({ title: '加载中...' })

      try {
        const res = await api.apply.getMyApplies({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })

        uni.hideLoading()

        if (res.code === 200) {
          this.list = res.data.list
          this.total = res.data.total
        }
      } catch (error) {
        uni.hideLoading()
        console.error('加载失败', error)
      }
    }
  }
}
</script>
```

### 示例2：创建申请单

```javascript
methods: {
  async submitApply() {
    // 表单验证
    if (!this.form.warehouseId) {
      uni.showToast({
        title: '请选择仓库',
        icon: 'none'
      })
      return
    }

    if (this.form.details.length === 0) {
      uni.showToast({
        title: '请添加物资',
        icon: 'none'
      })
      return
    }

    uni.showLoading({ title: '提交中...' })

    try {
      const res = await api.apply.createApply(this.form)

      uni.hideLoading()

      if (res.code === 200) {
        uni.showToast({
          title: '提交成功',
          icon: 'success'
        })

        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    } catch (error) {
      uni.hideLoading()
      // 错误已在request.js中自动处理
    }
  }
}
```

### 示例3：文件上传

```javascript
methods: {
  async uploadImage() {
    const [error, res] = await uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera']
    })

    if (error) {
      console.error('选择图片失败', error)
      return
    }

    const filePath = res.tempFilePaths[0]

    uni.showLoading({ title: '上传中...' })

    try {
      const uploadRes = await api.common.uploadFile(filePath)

      uni.hideLoading()

      if (uploadRes.code === 200) {
        this.imageUrl = uploadRes.data.url

        uni.showToast({
          title: '上传成功',
          icon: 'success'
        })
      }
    } catch (error) {
      uni.hideLoading()
      console.error('上传失败', error)
    }
  }
}
```

## 注意事项

1. **Token管理**：Token在登录后自动保存，request.js会自动在请求头中添加

2. **请求方法**：
   - GET：查询操作
   - POST：创建操作、需要body的操作
   - PUT：更新操作
   - DELETE：删除操作

3. **日期格式**：统一使用 `YYYY-MM-DD` 或 `YYYY-MM-DD HH:mm:ss`

4. **状态码**：
   - code: 200 表示成功
   - code: 其他值表示业务错误
   - HTTP状态码401/403会自动处理

5. **分页**：所有列表接口都支持分页，pageNum从1开始

6. **权限控制**：后端已做权限控制，前端调用时无需额外处理权限

7. **错误提示**：大部分错误会自动显示toast提示，特殊情况可使用try-catch自定义处理

## 字段映射说明

### 申请单状态 (status)
- 0: 待审批
- 1: 已通过
- 2: 已拒绝
- 3: 已完成
- 4: 已取消

### 出库单状态 (status)
- 0: 待取货
- 1: 已完成
- 2: 已取消

### 出库类型 (outboundType)
- 1: 直接出库
- 2: 申请出库

### 消息类型 (messageType)
- 1: 系统通知
- 2: 审批通知
- 3: 出库通知

### 阅读状态 (readStatus)
- 0: 未读
- 1: 已读

## 更新日志

- 2025-11-24: 完成9个API模块，共计68个接口
  - auth.js: 5个接口
  - apply.js: 5个接口
  - approval.js: 5个接口
  - inventory.js: 6个接口
  - inbound.js: 5个接口
  - outbound.js: 7个接口
  - message.js: 7个接口
  - user.js: 7个接口
  - common.js: 13个接口

## 技术支持

如有问题，请联系开发团队或查看后端API文档：`docs/API_REFERENCE.md`
