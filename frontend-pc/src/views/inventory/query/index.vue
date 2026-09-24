<template>
  <div class="inventory-query-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">库存查询</h2>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="在库物资种类" :value="statistics.materialCount">
            <template #suffix>种</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="库存总值" :value="statistics.totalValue" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="clickable" @click="router.push('/inventory/warning')">
          <el-statistic title="低库存预警" :value="statistics.warningCount">
            <template #suffix>项</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="物资">
          <el-input
            v-model="queryForm.keyword"
            placeholder="物资编号/名称"
            clearable
            @clear="handleSearch"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select
            v-model="queryForm.warehouseId"
            placeholder="全部仓库"
            clearable
            @change="handleSearch"
            style="width: 180px"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="spec" label="规格型号" width="120" />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column prop="quantity" label="当前库存" width="100" align="right">
          <template #default="{ row }">
            <span :class="stockClass[row.stockStatus]">{{ formatNumber(row.quantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="availableQuantity" label="可用库存" width="100" align="right">
          <template #default="{ row }">
            {{ formatNumber(row.availableQuantity) }}
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100" align="right">
          <template #default="{ row }">
            {{ row.minStock != null ? formatNumber(row.minStock) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            {{ row.price != null ? `¥${formatMoney(row.price)}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="stockValue" label="库存金额(元)" width="130" align="right">
          <template #default="{ row }">
            <span class="amount">{{ row.stockValue != null ? `¥${formatMoney(row.stockValue)}` : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag[row.stockStatus]?.type" size="small">
              {{ statusTag[row.stockStatus]?.text || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewLog(row)">
              流水记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 16px; text-align: right;">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listInventories } from '@/api/inventory'
import { getMyWarehouses } from '@/api/warehouse'
import { getInventoryStatistics } from '@/api/statistics'

const router = useRouter()

// 库存状态（后端 stockStatus：0-正常 1-低库存 2-缺货）
const statusTag = {
  0: { type: 'success', text: '正常' },
  1: { type: 'warning', text: '低库存' },
  2: { type: 'danger', text: '缺货' }
}
const stockClass = {
  1: 'text-warning',
  2: 'text-danger'
}

// 查询表单
const queryForm = reactive({
  keyword: '',
  warehouseId: null
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 仓库列表
const warehouseList = ref([])

// 统计数据
const statistics = reactive({
  materialCount: 0,
  totalValue: 0,
  warningCount: 0
})

const formatNumber = (value) => (value == null ? '-' : Number(value).toLocaleString())
const formatMoney = (value) => Number(value).toLocaleString(undefined, {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

// 加载仓库列表
const loadWarehouses = async () => {
  try {
    const res = await getMyWarehouses()
    warehouseList.value = res.data || []
  } catch (error) {
    console.error('加载仓库列表失败:', error)
  }
}

// 加载统计数据（与所选仓库一致）
const loadStatistics = async () => {
  try {
    const res = await getInventoryStatistics({ warehouseId: queryForm.warehouseId || undefined })
    const data = res.data || {}
    statistics.materialCount = data.materialCount || 0
    statistics.totalValue = Number(data.totalValue || 0)
    statistics.warningCount = data.warningCount || 0
  } catch (error) {
    console.error('加载库存统计失败:', error)
  }
}

// 查询数据（错误提示由请求拦截器统一处理）
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await listInventories({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: queryForm.keyword || undefined,
      warehouseId: queryForm.warehouseId || undefined
    })
    tableData.value = res.data || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

// 条件变化时回到第一页，并同步刷新统计
const handleSearch = () => {
  pagination.pageNum = 1
  handleQuery()
  loadStatistics()
}

// 重置查询
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.warehouseId = null
  handleSearch()
}

// 查看流水记录
const handleViewLog = (row) => {
  router.push({
    path: '/inventory/log',
    query: { warehouseId: row.warehouseId, materialId: row.materialId }
  })
}

// 初始化
onMounted(() => {
  loadWarehouses()
  handleSearch()
})
</script>

<style lang="scss" scoped>
.inventory-query-container {
  .clickable {
    cursor: pointer;
  }

  .amount {
    color: $error-color;
    font-weight: 500;
  }

  .text-danger {
    color: $error-color;
    font-weight: 500;
  }

  .text-warning {
    color: $warning-color;
    font-weight: 500;
  }
}
</style>
