# API集成快速指南

## 已完成的P0核心页面 (4个)

### 1. 登录页面 ✓
**文件**: `pages/login/login.vue`

```javascript
import api from '@/api'

// 微信登录
const res = await api.auth.wechatLogin({ code, encryptedData, iv })
```

---

### 2. 首页 ✓
**文件**: `pages/index/index.vue`

```javascript
// 获取统计数据
const res = await api.common.getStatistics()

// 获取未读消息数
const res = await api.message.getUnreadCount()
```

---

### 3. 我的申请列表 ✓
**文件**: `pages/apply/list.vue`

```javascript
// 获取申请列表
const res = await api.apply.getMyApplies({ pageNum, pageSize, status })

// 撤销申请
const res = await api.apply.cancelApply(id)

// 获取统计
const res = await api.apply.getApplyStats()
```

---

### 4. 审批管理列表 ✓
**文件**: `pages/approval/list.vue`

```javascript
// 获取待审批列表
const res = await api.approval.getPendingApproval({ pageNum, pageSize })

// 获取申请详情
const res = await api.apply.getApplyDetail(id)

// 审批操作
const res = await api.approval.approveApply(id, {
  approvalStatus: 1,  // 1通过 2拒绝
  rejectReason: ''
})
```

---

## 标准集成模式

### 模式1: 列表页面

```javascript
<script>
import api from '@/api'

export default {
  data() {
    return {
      list: [],
      pageNum: 1,
      pageSize: 20,
      loading: false,
      refreshing: false,
      noMore: false
    }
  },

  methods: {
    // 加载数据
    async loadData() {
      if (this.loading || this.noMore) return

      this.loading = true

      try {
        const res = await api.module.getList({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })

        if (res.code === 200) {
          if (this.pageNum === 1) {
            this.list = res.data.list
          } else {
            this.list = this.list.concat(res.data.list)
          }

          if (this.list.length >= res.data.total) {
            this.noMore = true
          }
        }
      } catch (err) {
        console.error('加载失败', err)
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },

    // 下拉刷新
    onRefresh() {
      this.refreshing = true
      this.pageNum = 1
      this.list = []
      this.noMore = false
      this.loadData()
    },

    // 上拉加载
    loadMore() {
      if (!this.loading && !this.noMore) {
        this.pageNum++
        this.loadData()
      }
    }
  },

  onLoad() {
    this.loadData()
  }
}
</script>
```

---

### 模式2: 表单提交

```javascript
methods: {
  async submitForm() {
    // 1. 表单验证
    if (!this.form.required) {
      uni.showToast({ title: '请填写必填项', icon: 'none' })
      return
    }

    // 2. 显示加载
    uni.showLoading({ title: '提交中...' })

    try {
      // 3. 调用API
      const res = await api.module.create(this.form)

      uni.hideLoading()

      // 4. 成功提示
      if (res.code === 200) {
        uni.showToast({ title: '提交成功', icon: 'success' })

        // 5. 返回上一页
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    } catch (err) {
      uni.hideLoading()
      console.error('提交失败', err)
    }
  }
}
```

---

### 模式3: 详情页面

```javascript
methods: {
  async loadDetail() {
    try {
      uni.showLoading({ title: '加载中...' })

      const res = await api.module.getDetail(this.id)

      uni.hideLoading()

      if (res.code === 200) {
        this.detail = res.data
      }
    } catch (err) {
      uni.hideLoading()
      console.error('加载失败', err)
    }
  }
},

onLoad(options) {
  this.id = options.id
  this.loadDetail()
}
```

---

## 待集成页面列表

### P1 优先级 (10个)
1. `pages/apply/create.vue` - 新建申请
2. `pages/apply/detail.vue` - 申请详情
3. `pages/approval/detail.vue` - 审批详情
4. `pages/inventory/list.vue` - 库存列表
5. `pages/inventory/detail.vue` - 库存详情
6. `pages/inbound/create.vue` - 快速入库
7. `pages/inbound/list.vue` - 入库记录
8. `pages/outbound/create.vue` - 快速出库
9. `pages/outbound/list.vue` - 出库记录
10. `pages/outbound/pending.vue` - 待领取

### P2 优先级 (3个)
1. `pages/mine/mine.vue` - 我的页面
2. `pages/mine/messages.vue` - 消息列表
3. `pages/mine/settings.vue` - 设置

---

## API模块速查

```javascript
// 认证
api.auth.login()
api.auth.wechatLogin()
api.auth.logout()

// 申请
api.apply.createApply()
api.apply.getMyApplies()
api.apply.getApplyDetail()
api.apply.cancelApply()

// 审批
api.approval.getPendingApproval()
api.approval.approveApply()

// 库存
api.inventory.getInventoryList()
api.inventory.getInventoryDetail()
api.inventory.searchInventory()

// 入库
api.inbound.createInbound()
api.inbound.getInboundList()

// 出库
api.outbound.createOutbound()
api.outbound.getPendingOutbound()
api.outbound.confirmOutbound()
api.outbound.getOutboundList()

// 消息
api.message.getMessages()
api.message.getUnreadCount()
api.message.markRead()

// 用户
api.user.getUserProfile()
api.user.changePassword()

// 公共
api.common.getMaterials()
api.common.getWarehouses()
api.common.getMyWarehouses()
api.common.getStatistics()
```

---

## 注意事项

1. **统一导入**: 使用 `import api from '@/api'`
2. **错误处理**: 已由 `request.js` 统一处理,页面只需 try-catch
3. **加载状态**: 使用 `uni.showLoading()` 和 `uni.hideLoading()`
4. **分页参数**: pageNum从1开始, pageSize默认20
5. **下拉刷新**: 重置 pageNum=1, list=[], noMore=false

---

## 快速参考

### 状态码
- 申请状态: 0待审批 1已通过 2已拒绝 3已完成 4已取消
- 出库状态: 0待取货 1已完成 2已取消
- 阅读状态: 0未读 1已读

### API文档
- 详细文档: `miniprogram/api/README.md`
- 总结报告: `miniprogram/API_INTEGRATION_SUMMARY.md`
