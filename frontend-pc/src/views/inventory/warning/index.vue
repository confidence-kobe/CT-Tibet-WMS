<template>
  <div class="inventory-warning-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">库存预警</h2>
      <span class="page-tip">当前库存低于物资最低库存即产生预警，补货入库后自动解除</span>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="8">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon total">
              <el-icon size="32"><WarningFilled /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">预警总数</div>
              <div class="stats-value">{{ statistics.total }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon urgent">
              <el-icon size="32"><CircleCloseFilled /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">缺货（库存为0）</div>
              <div class="stats-value urgent-text">{{ statistics.outOfStock }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon warning">
              <el-icon size="32"><WarningFilled /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">低库存</div>
              <div class="stats-value warning-text">{{ statistics.lowStock }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true" @submit.prevent>
        <el-form-item label="仓库">
          <el-select
            v-model="queryForm.warehouseId"
            placeholder="全部仓库"
            clearable
            @change="handleQuery"
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
        <el-form-item label="物资">
          <el-input
            v-model="queryForm.keyword"
            placeholder="物资编号/名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="预警类型">
          <el-select
            v-model="queryForm.stockStatus"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option label="缺货" :value="STATUS_OUT_OF_STOCK" />
            <el-option label="低库存" :value="STATUS_LOW_STOCK" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><RefreshRight /></el-icon>
            刷新
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table
        :data="pagedData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" :index="rowIndex" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="spec" label="规格" width="120" show-overflow-tooltip />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column label="当前库存" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.stockStatus === STATUS_OUT_OF_STOCK ? 'urgent-stock' : 'warning-stock'">
              {{ formatNumber(row.quantity) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="最低库存" width="100" align="right">
          <template #default="{ row }">{{ formatNumber(row.minStock) }}</template>
        </el-table-column>
        <el-table-column label="缺口" width="100" align="right">
          <template #default="{ row }">
            <span class="urgent-stock">{{ formatNumber(shortage(row)) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column label="预警类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.stockStatus === STATUS_OUT_OF_STOCK ? 'danger' : 'warning'" size="small">
              {{ row.stockStatus === STATUS_OUT_OF_STOCK ? '缺货' : '低库存' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastOutboundTime" label="最近出库" width="160">
          <template #default="{ row }">{{ row.lastOutboundTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleRestock(row)">
              入库补货
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewLog(row)">
              流水记录
            </el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无库存预警，库存状况良好" :image-size="80" />
        </template>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 16px; text-align: right;">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="filteredData.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listLowStockAlerts } from '@/api/inventory'
import { getMyWarehouses } from '@/api/warehouse'

const router = useRouter()

// 与后端 stockStatus 保持一致：1-低库存 2-缺货
const STATUS_LOW_STOCK = 1
const STATUS_OUT_OF_STOCK = 2

const queryForm = reactive({
  warehouseId: null,
  keyword: '',
  stockStatus: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20
})

// 全部预警数据（后端一次返回，前端筛选分页）
const allData = ref([])
const loading = ref(false)
const warehouseList = ref([])

const statistics = computed(() => {
  const outOfStock = allData.value.filter(item => item.stockStatus === STATUS_OUT_OF_STOCK).length
  return {
    total: allData.value.length,
    outOfStock,
    lowStock: allData.value.length - outOfStock
  }
})

const filteredData = computed(() => {
  const keyword = queryForm.keyword.trim().toLowerCase()
  return allData.value
    .filter(item => queryForm.stockStatus == null || item.stockStatus === queryForm.stockStatus)
    .filter(item => !keyword ||
      (item.materialName || '').toLowerCase().includes(keyword) ||
      (item.materialCode || '').toLowerCase().includes(keyword))
    // 缺货优先，其次按缺口从大到小
    .sort((a, b) => (b.stockStatus - a.stockStatus) || (shortage(b) - shortage(a)))
})

const pagedData = computed(() => {
  const start = (pagination.pageNum - 1) * pagination.pageSize
  return filteredData.value.slice(start, start + pagination.pageSize)
})

// 本地筛选条件变化时回到第一页
watch(() => [queryForm.keyword, queryForm.stockStatus], () => {
  pagination.pageNum = 1
})

const rowIndex = (index) => (pagination.pageNum - 1) * pagination.pageSize + index + 1
const shortage = (row) => Math.max(0, Number(row.minStock || 0) - Number(row.quantity || 0))
const formatNumber = (value) => (value == null ? '-' : Number(value).toLocaleString())

const loadWarehouses = async () => {
  try {
    const res = await getMyWarehouses()
    warehouseList.value = res.data || []
  } catch (error) {
    console.error('加载仓库列表失败:', error)
  }
}

// 查询预警（错误提示由请求拦截器统一处理）
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await listLowStockAlerts({ warehouseId: queryForm.warehouseId || undefined })
    allData.value = res.data || []
    pagination.pageNum = 1
  } catch (error) {
    console.error('查询库存预警失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.warehouseId = null
  queryForm.keyword = ''
  queryForm.stockStatus = null
  handleQuery()
}

// 跳转入库页面，自动带上仓库、物资和建议补货数量
const handleRestock = (row) => {
  router.push({
    path: '/inbound/create',
    query: {
      warehouseId: row.warehouseId,
      materialId: row.materialId,
      quantity: shortage(row) || undefined
    }
  })
}

const handleViewLog = (row) => {
  router.push({
    path: '/inventory/log',
    query: { warehouseId: row.warehouseId, materialId: row.materialId }
  })
}

onMounted(() => {
  loadWarehouses()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.inventory-warning-container {
  .page-tip {
    font-size: 13px;
    color: #999;
  }

  .stats-card {
    .stats-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .stats-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;

        &.total {
          background: #e6f7ff;
          color: #1890ff;
        }

        &.urgent {
          background: #fff1f0;
          color: #f5222d;
        }

        &.warning {
          background: #fff7e6;
          color: #fa8c16;
        }
      }

      .stats-info {
        flex: 1;

        .stats-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 4px;
        }

        .stats-value {
          font-size: 24px;
          font-weight: 600;
          color: #333;

          &.urgent-text {
            color: #f5222d;
          }

          &.warning-text {
            color: #fa8c16;
          }
        }
      }
    }
  }

  .urgent-stock {
    color: #f5222d;
    font-weight: 600;
  }

  .warning-stock {
    color: #fa8c16;
    font-weight: 600;
  }
}
</style>
