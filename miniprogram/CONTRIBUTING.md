# 开发规范

本文档规定了 CT-Tibet-WMS 小程序的开发规范，所有开发者请遵守。

## 代码规范

### 命名规范

**文件命名：**
- 页面文件：小写字母 + 连字符（kebab-case）
  ```
  ✓ apply-list.vue
  ✓ inbound-create.vue
  ✗ ApplyList.vue
  ✗ inbound_create.vue
  ```

- 组件文件：大驼峰（PascalCase）
  ```
  ✓ MaterialItem.vue
  ✓ StatusBadge.vue
  ✗ material-item.vue
  ```

- JS 文件：小写字母 + 连字符
  ```
  ✓ request.js
  ✓ date-utils.js
  ✗ DateUtils.js
  ```

**变量命名：**
- 变量和函数：小驼峰（camelCase）
  ```javascript
  ✓ const userName = 'John'
  ✓ function getUserInfo() {}
  ✗ const user_name = 'John'
  ✗ function get_user_info() {}
  ```

- 常量：全大写 + 下划线
  ```javascript
  ✓ const API_BASE_URL = 'https://api.example.com'
  ✓ const MAX_PAGE_SIZE = 100
  ✗ const apiBaseUrl = 'https://api.example.com'
  ```

- 组件 data 属性：小驼峰
  ```javascript
  data() {
    return {
      ✓ userInfo: {},
      ✓ applyList: [],
      ✗ user_info: {},
      ✗ ApplyList: []
    }
  }
  ```

### Vue 组件规范

**组件结构顺序：**
```vue
<template>
  <!-- 模板 -->
</template>

<script>
// 1. 导入
import { mapState } from 'vuex'
import { $uRequest } from '@/utils/request.js'

// 2. 组件定义
export default {
  name: 'ComponentName',        // 组件名称
  components: {},                // 子组件
  props: {},                     // 属性
  data() { return {} },          // 数据
  computed: {},                  // 计算属性
  watch: {},                     // 监听器

  // 生命周期（按顺序）
  onLoad() {},
  onShow() {},
  onReady() {},
  onHide() {},
  onUnload() {},

  // 方法
  methods: {}
}
</script>

<style lang="scss" scoped>
/* 样式 */
</style>
```

**Props 定义：**
```javascript
// ✓ 完整定义
props: {
  title: {
    type: String,
    required: true,
    default: '默认标题'
  },
  count: {
    type: Number,
    default: 0,
    validator: (value) => value >= 0
  }
}

// ✗ 简单定义（不推荐）
props: ['title', 'count']
```

**计算属性：**
```javascript
// ✓ 使用计算属性缓存
computed: {
  fullName() {
    return `${this.firstName} ${this.lastName}`
  }
}

// ✗ 在模板中计算（不推荐）
<template>
  <text>{{ firstName + ' ' + lastName }}</text>
</template>
```

### JavaScript 规范

**使用 ES6+ 语法：**
```javascript
// ✓ 箭头函数
const add = (a, b) => a + b

// ✓ 解构赋值
const { userInfo, token } = this.$store.state

// ✓ 模板字符串
const message = `Hello, ${userName}!`

// ✓ async/await
async loadData() {
  try {
    const res = await $uRequest({ url: '/api/data' })
    this.data = res.data
  } catch (err) {
    console.error(err)
  }
}
```

**避免使用：**
```javascript
// ✗ var（使用 const 或 let）
var name = 'John'

// ✗ 回调地狱
getData(function(data) {
  getMoreData(data, function(moreData) {
    getEvenMoreData(moreData, function(evenMoreData) {
      // ...
    })
  })
})
```

**错误处理：**
```javascript
// ✓ 统一错误处理
async loadData() {
  if (this.loading) return

  this.loading = true

  try {
    const res = await $uRequest({ url: '/api/data' })

    if (res.code === 200) {
      this.data = res.data
    } else {
      throw new Error(res.message)
    }
  } catch (err) {
    console.error('加载失败', err)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    this.loading = false
  }
}
```

### 样式规范

**使用 SCSS：**
```scss
// ✓ 嵌套结构
.container {
  padding: 32rpx;

  .header {
    font-size: 32rpx;
    color: #262626;

    &.active {
      color: #667eea;
    }
  }

  .content {
    margin-top: 24rpx;
  }
}

// ✓ 使用变量
$primary-color: #667eea;
$text-color: #262626;

.button {
  background-color: $primary-color;
  color: #ffffff;
}
```

**单位使用：**
```scss
// ✓ 使用 rpx（响应式单位）
.container {
  padding: 32rpx;
  font-size: 28rpx;
}

// ✗ 使用 px（固定单位，除非必要）
.container {
  padding: 16px;  // 不推荐
}
```

**class 命名：**
```scss
// ✓ BEM 命名法
.apply-card {}
.apply-card__header {}
.apply-card__title {}
.apply-card--pending {}

// ✓ 语义化命名
.btn-primary {}
.text-secondary {}
.status-badge {}
```

## API 调用规范

### 请求封装

**使用统一的请求方法：**
```javascript
import { $uRequest } from '@/utils/request.js'

// ✓ GET 请求
const res = await $uRequest({
  url: '/api/applies',
  method: 'GET',
  data: { page: 1, pageSize: 20 }
})

// ✓ POST 请求
const res = await $uRequest({
  url: '/api/applies',
  method: 'POST',
  data: { purpose: '办公用品', details: [...] }
})

// ✓ PUT 请求
const res = await $uRequest({
  url: `/api/applies/${id}/approve`,
  method: 'PUT',
  data: { approved: true, opinion: '同意' }
})

// ✗ 直接使用 uni.request（不推荐）
uni.request({
  url: 'https://api.example.com/data',
  // ...
})
```

### 接口响应处理

**标准响应格式：**
```javascript
{
  code: 200,           // 状态码
  message: 'success',  // 消息
  data: {}            // 数据
}
```

**响应处理：**
```javascript
async loadData() {
  try {
    const res = await $uRequest({ url: '/api/data' })

    // ✓ 检查状态码
    if (res.code === 200) {
      this.data = res.data
    } else {
      uni.showToast({
        title: res.message || '操作失败',
        icon: 'none'
      })
    }
  } catch (err) {
    console.error('请求失败', err)
    uni.showToast({
      title: '网络错误',
      icon: 'none'
    })
  }
}
```

## 状态管理规范

### Vuex 使用

**读取状态：**
```javascript
import { mapState, mapGetters } from 'vuex'

export default {
  computed: {
    // ✓ 使用 mapState
    ...mapState(['userInfo', 'token']),

    // ✓ 使用 mapGetters
    ...mapGetters(['isLogin', 'isWarehouse']),

    // ✗ 直接访问（不推荐）
    userInfo() {
      return this.$store.state.userInfo
    }
  }
}
```

**修改状态：**
```javascript
// ✓ 通过 mutation
this.$store.commit('SET_USER_INFO', userInfo)

// ✓ 通过 action
this.$store.dispatch('getUnreadCount')

// ✗ 直接修改（严禁）
this.$store.state.userInfo = userInfo
```

## 注释规范

### 文件注释

```javascript
/**
 * 申请管理相关 API
 * @author Your Name
 * @date 2025-01-11
 */
```

### 函数注释

```javascript
/**
 * 加载申请列表
 * @param {Boolean} isRefresh - 是否刷新
 * @returns {Promise<void>}
 */
async loadData(isRefresh = false) {
  // 实现
}

/**
 * 计算总金额
 * @param {Array} details - 物资明细数组
 * @returns {Number} 总金额
 */
calculateTotal(details) {
  return details.reduce((sum, item) => {
    return sum + (item.quantity * item.unitPrice)
  }, 0)
}
```

### 复杂逻辑注释

```javascript
// ✓ 解释 WHY，不是 WHAT
// 审批通过后需要自动创建出库单
if (approved) {
  await this.createOutbound()
}

// ✗ 描述代码做了什么（不必要）
// 设置 loading 为 true
this.loading = true
```

## Git 提交规范

### Commit Message 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Type 类型：**
- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例：**
```
feat(apply): 添加物资申请批量选择功能

- 支持一次选择多个物资
- 添加全选和取消全选按钮
- 优化物资选择器 UI

Closes #123
```

```
fix(outbound): 修复待领取列表数据重复问题

修复下拉刷新时数据未清空导致重复显示的 Bug
```

### 分支管理

**分支命名：**
- `main` - 主分支（生产环境）
- `develop` - 开发分支
- `feature/xxx` - 功能分支
- `fix/xxx` - 修复分支
- `hotfix/xxx` - 紧急修复分支

**工作流程：**
1. 从 `develop` 创建功能分支
2. 完成功能开发并测试
3. 提交 Pull Request 到 `develop`
4. Code Review 后合并
5. 定期从 `develop` 合并到 `main` 发布

## 性能优化规范

### 避免频繁 setData

```javascript
// ✗ 多次 setData
this.name = 'John'
this.age = 25
this.email = 'john@example.com'

// ✓ 批量更新
Object.assign(this, {
  name: 'John',
  age: 25,
  email: 'john@example.com'
})
```

### 列表渲染优化

```vue
<!-- ✓ 使用 key -->
<view v-for="item in list" :key="item.id">
  {{ item.name }}
</view>

<!-- ✗ 使用 index 作为 key（数据会变化时）-->
<view v-for="(item, index) in list" :key="index">
  {{ item.name }}
</view>
```

### 图片优化

```vue
<!-- ✓ 指定图片尺寸 -->
<image
  src="/static/logo.png"
  mode="aspectFit"
  style="width: 200rpx; height: 200rpx"
/>

<!-- ✓ 懒加载 -->
<image
  src="/static/large-image.png"
  lazy-load
/>
```

## 测试规范

### 功能测试

**测试清单：**
- [ ] 页面正常加载
- [ ] 数据正确显示
- [ ] 按钮点击响应
- [ ] 表单验证正常
- [ ] 错误提示正确
- [ ] 边界情况处理
- [ ] 网络异常处理

### 兼容性测试

**测试设备：**
- iOS（iPhone）
- Android（各主流品牌）
- 不同屏幕尺寸
- 不同微信版本

**测试场景：**
- 网络良好
- 网络较差
- 无网络连接
- 弱网环境

## 安全规范

### 数据安全

```javascript
// ✗ 在代码中硬编码密钥（严禁）
const API_KEY = 'sk_1234567890abcdef'

// ✓ 使用环境变量
const API_KEY = process.env.API_KEY

// ✓ 敏感信息不打印
console.log(userInfo)  // 开发环境可以
// 生产环境应移除或使用条件编译
```

### 权限控制

```javascript
// ✓ 前端权限校验
computed: {
  canApprove() {
    return this.isWarehouse || this.userInfo.roleCode === 'DEPT_ADMIN'
  }
}

// ✓ 后端必须验证权限
// 前端权限校验仅用于 UI 展示，不能替代后端验证
```

## Code Review 检查清单

**代码质量：**
- [ ] 符合命名规范
- [ ] 代码结构清晰
- [ ] 无重复代码
- [ ] 注释完整清晰
- [ ] 无 console.log 遗留

**功能实现：**
- [ ] 功能符合需求
- [ ] 边界情况处理
- [ ] 错误处理完善
- [ ] 用户体验良好

**性能：**
- [ ] 无性能瓶颈
- [ ] 资源加载优化
- [ ] 内存使用合理

**安全：**
- [ ] 无安全隐患
- [ ] 数据验证完整
- [ ] 权限控制正确

## 参考资源

**官方文档：**
- [uni-app 官方文档](https://uniapp.dcloud.io/)
- [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- [Vue.js 风格指南](https://cn.vuejs.org/v2/style-guide/)

**编码规范：**
- [JavaScript Standard Style](https://standardjs.com/)
- [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)
- [BEM 命名规范](http://getbem.com/)

---

遵守以上规范，保持代码质量和团队协作效率。
