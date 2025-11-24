# 共享组件使用文档

本目录包含项目中所有可复用的共享组件。

## 组件列表

- [EChart](#echart) - ECharts 图表封装组件
- [StatusTag](#statustag) - 状态标签组件
- [TableCard](#tablecard) - 表格卡片容器组件

---

## EChart

ECharts 图表的封装组件，提供响应式、自动 resize、配置更新等功能。

### 基本用法

```vue
<template>
  <EChart :option="chartOption" height="400px" />
</template>

<script setup>
import { computed } from 'vue'
import { EChart } from '@/components'

const chartOption = computed(() => ({
  title: { text: '销售趋势' },
  xAxis: { type: 'category', data: ['周一', '周二', '周三'] },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    data: [120, 200, 150]
  }]
}))
</script>
```

### Props

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| option | ECharts 配置选项 | Object | - (必填) |
| width | 图表宽度 | String | '100%' |
| height | 图表高度 | String | '400px' |
| autoResize | 是否自动 resize | Boolean | true |
| theme | 主题名称 | String | '' |

### 暴露方法

```vue
<template>
  <EChart ref="chartRef" :option="chartOption" />
</template>

<script setup>
import { ref } from 'vue'
const chartRef = ref()

// 获取 ECharts 实例
const instance = chartRef.value?.getInstance()

// 手动 resize
chartRef.value?.resize()

// 更新配置
chartRef.value?.setOption(newOption, true)

// 显示/隐藏 loading
chartRef.value?.showLoading()
chartRef.value?.hideLoading()

// 清空图表
chartRef.value?.clear()
</script>
```

### 示例：折线图

```vue
<EChart :option="{
  title: { text: '出入库趋势' },
  tooltip: { trigger: 'axis' },
  legend: { data: ['入库', '出库'] },
  xAxis: { type: 'category', data: ['周一', '周二', '周三'] },
  yAxis: { type: 'value' },
  series: [
    { name: '入库', type: 'line', data: [12, 15, 18] },
    { name: '出库', type: 'line', data: [10, 12, 16] }
  ]
}" height="350px" />
```

### 示例：饼图

```vue
<EChart :option="{
  title: { text: '库存分布' },
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: [
      { value: 245, name: '充足' },
      { value: 89, name: '正常' },
      { value: 34, name: '预警' }
    ]
  }]
}" height="350px" />
```

---

## StatusTag

统一的状态标签组件，支持多种预定义状态类型。

### 基本用法

```vue
<template>
  <!-- 订单状态 -->
  <StatusTag status-type="order" :status="0" />  <!-- 待审核 -->
  <StatusTag status-type="order" :status="1" />  <!-- 已审核 -->

  <!-- 申请状态 -->
  <StatusTag status-type="apply" :status="0" />  <!-- 待审批 -->
  <StatusTag status-type="apply" :status="1" />  <!-- 已通过 -->

  <!-- 库存状态 -->
  <StatusTag status-type="inventory" status="warning" />  <!-- 预警 -->

  <!-- 操作类型 -->
  <StatusTag status-type="operation" status="inbound" />  <!-- 入库 -->
</template>

<script setup>
import { StatusTag } from '@/components'
</script>
```

### Props

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| status | 状态值 | String/Number | - (必填) |
| statusType | 状态类型 | String | 'order' |
| customMap | 自定义状态映射 | Object | {} |
| size | 标签大小 | String | 'default' |
| effect | 主题 | String | 'light' |
| round | 是否圆形 | Boolean | false |
| closable | 是否可关闭 | Boolean | false |

### 支持的状态类型

#### 1. order (订单状态)

| status | 显示 | 颜色 |
|--------|------|------|
| 0 / 'pending' | 待审核 | warning |
| 1 / 'approved' | 已审核 | success |
| 2 / 'completed' | 已完成 | info |
| 3 / 'cancelled' | 已取消 | info |

#### 2. apply (申请状态)

| status | 显示 | 颜色 |
|--------|------|------|
| 0 / 'pending' | 待审批 | warning |
| 1 / 'approved' | 已通过 | success |
| 2 / 'rejected' | 已拒绝 | danger |
| 3 / 'completed' | 已完成 | info |
| 4 / 'cancelled' | 已取消 | info |

#### 3. inventory (库存状态)

| status | 显示 | 颜色 |
|--------|------|------|
| 'sufficient' | 充足 | success |
| 'normal' | 正常 | primary |
| 'warning' | 预警 | warning |
| 'urgent' | 紧急 | danger |
| 'out_of_stock' | 缺货 | info |

#### 4. operation (操作类型)

| status | 显示 | 颜色 |
|--------|------|------|
| 'inbound' | 入库 | primary |
| 'outbound' | 出库 | success |
| 'apply' | 申请 | warning |
| 'transfer' | 调拨 | info |
| 'check' | 盘点 | - |

### 自定义状态

```vue
<StatusTag
  status-type="custom"
  :custom-map="{
    'active': { label: '活跃', type: 'success' },
    'inactive': { label: '停用', type: 'info' }
  }"
  status="active"
/>
```

### 在表格中使用

```vue
<el-table-column prop="status" label="状态" width="100" align="center">
  <template #default="{ row }">
    <StatusTag status-type="order" :status="row.status" size="small" />
  </template>
</el-table-column>
```

---

## TableCard

表格容器组件，提供统一的搜索、表格、分页布局。

### 基本用法

```vue
<template>
  <TableCard
    title="用户列表"
    :pagination="pagination"
    @page-change="handlePageChange"
    @size-change="handleSizeChange"
  >
    <!-- 搜索表单插槽 -->
    <template #search>
      <el-form :inline="true">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </template>

    <!-- 表头操作按钮插槽 -->
    <template #actions>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon>
        导出
      </el-button>
    </template>

    <!-- 表格内容 -->
    <el-table :data="tableData" stripe border>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <StatusTag status-type="custom" :status="row.status" />
        </template>
      </el-table-column>
    </el-table>
  </TableCard>
</template>

<script setup>
import { ref } from 'vue'
import { TableCard, StatusTag } from '@/components'

const searchForm = ref({ username: '' })
const pagination = ref({
  current: 1,
  size: 10,
  total: 100
})
const tableData = ref([])

const handleSearch = () => {
  // 搜索逻辑
}

const handlePageChange = (page) => {
  pagination.value.current = page
  // 重新加载数据
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  // 重新加载数据
}

const handleAdd = () => {
  // 新增逻辑
}

const handleExport = () => {
  // 导出逻辑
}
</script>
```

### Props

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| title | 卡片标题 | String | '' |
| showPagination | 是否显示分页 | Boolean | true |
| pagination | 分页配置对象 | Object | { current: 1, size: 10, total: 0 } |
| pageSizes | 分页大小选项 | Array | [10, 20, 50, 100] |
| paginationLayout | 分页布局 | String | 'total, sizes, prev, pager, next, jumper' |
| paginationBackground | 分页背景色 | Boolean | false |

### 插槽

| 名称 | 说明 |
|------|------|
| search | 搜索表单区域 |
| header | 表头左侧自定义内容 |
| actions | 表头右侧操作按钮 |
| default | 表格内容 |

### Events

| 事件名 | 说明 | 参数 |
|--------|------|------|
| update:pagination | 分页数据更新 | pagination 对象 |
| page-change | 页码变化 | 当前页码 |
| size-change | 每页大小变化 | 每页大小 |

### 完整示例：入库单列表

```vue
<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">入库管理</h2>
    </div>

    <TableCard
      title="入库单列表"
      :pagination="pagination"
      @page-change="fetchData"
      @size-change="fetchData"
    >
      <!-- 搜索表单 -->
      <template #search>
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="单据号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入单据号" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="全部" :value="null" />
              <el-option label="待审核" :value="0" />
              <el-option label="已审核" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </template>

      <!-- 操作按钮 -->
      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建入库
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </template>

      <!-- 表格 -->
      <el-table :data="tableData" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="orderNo" label="单据号" width="180" />
        <el-table-column prop="warehouseName" label="仓库" />
        <el-table-column prop="totalAmount" label="总金额" align="right">
          <template #default="{ row }">
            {{ formatAmount(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <StatusTag status-type="order" :status="row.status" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </TableCard>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { TableCard, StatusTag } from '@/components'

const router = useRouter()

const searchForm = ref({
  orderNo: '',
  status: null
})

const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const fetchData = async () => {
  // 调用 API 获取数据
  // const res = await getInboundList({
  //   ...searchForm.value,
  //   page: pagination.value.current,
  //   size: pagination.value.size
  // })
  // tableData.value = res.data.records
  // pagination.value.total = res.data.total
}

const handleSearch = () => {
  pagination.value.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.value = { orderNo: '', status: null }
  handleSearch()
}

const handleCreate = () => {
  router.push('/inbound/create')
}

const handleView = (row) => {
  router.push(`/inbound/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/inbound/edit/${row.id}`)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该入库单吗?', '提示', {
    type: 'warning'
  }).then(async () => {
    // await deleteInbound(row.id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const formatAmount = (amount) => {
  return amount?.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

onMounted(() => {
  fetchData()
})
</script>
```

---

## 最佳实践

### 1. 统一导入

推荐在需要使用组件的文件中统一从 `@/components` 导入：

```javascript
import { EChart, StatusTag, TableCard } from '@/components'
```

### 2. 响应式设计

所有组件都支持响应式布局，使用 Element Plus 的栅格系统配合使用效果更佳。

### 3. 主题一致性

使用 StatusTag 组件可以确保整个应用的状态显示风格统一，避免重复定义状态映射逻辑。

### 4. 性能优化

- EChart 组件已内置自动 resize 和配置深度监听，无需手动管理
- TableCard 组件支持 v-model:pagination 双向绑定，简化分页逻辑

### 5. 代码复用

通过使用这些共享组件，可以大幅减少重复代码，提高开发效率和代码可维护性。

---

## 开发规范

### 新增共享组件

1. 在 `src/components` 下创建组件目录
2. 组件目录结构：
   ```
   ComponentName/
   ├── index.vue      # 组件实现
   └── index.js       # 导出文件
   ```
3. 在 `src/components/index.js` 中添加导出
4. 更新本 README 文档

### 组件命名规范

- 组件文件名：PascalCase（如 StatusTag.vue）
- 组件导入名：PascalCase（如 import { StatusTag } from '@/components'）
- 组件使用：kebab-case 或 PascalCase（如 <status-tag> 或 <StatusTag>）

---

## 更新日志

### v1.0.0 (2025-11-14)

- 新增 EChart 图表组件
- 新增 StatusTag 状态标签组件
- 新增 TableCard 表格容器组件
- 创建组件使用文档
