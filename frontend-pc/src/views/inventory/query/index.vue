<template>
  <div class="inventory-query-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">库存查询</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="物资">
          <el-input
            v-model="queryForm.keyword"
            placeholder="物资编号/名称"
            clearable
            @clear="handleQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select
            v-model="queryForm.warehouseId"
            placeholder="请选择仓库"
            clearable
            @clear="handleQuery"
            style="width: 180px"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select
            v-model="queryForm.status"
            placeholder="请选择"
            clearable
            @clear="handleQuery"
            style="width: 140px"
          >
            <el-option label="充足" :value="0" />
            <el-option label="预警" :value="1" />
            <el-option label="紧急" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
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
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column prop="stock" label="当前库存" width="100" align="right">
          <template #default="{ row }">
            <span :class="getStockClass(row)">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100" align="right" />
        <el-table-column prop="price" label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalValue" label="库存金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ (row.stock * row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStockStatusType(row)" size="small">
              {{ getStockStatusText(row) }}
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

    <!-- 统计信息 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="物资总数" :value="statistics.totalItems">
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
        <el-card shadow="hover">
          <el-statistic title="预警数量" :value="statistics.warningCount">
            <template #suffix>项</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listInventories } from '@/api/inventory'
import { listWarehouses } from '@/api/warehouse'

// 查询表单
const queryForm = reactive({
  keyword: '',
  warehouseId: null,
  status: null
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
  totalItems: 0,
  totalValue: 0,
  warningCount: 0
})

// 加载仓库列表
const loadWarehouses = async () => {
  try {
    const res = await listWarehouses({ status: 0 })
    warehouseList.value = res.data || []
  } catch (error) {
    console.error('加载仓库列表失败:', error)
  }
}

// 获取库存状态类名
const getStockClass = (row) => {
  if (row.stock === 0) return 'text-danger'
  if (row.stock <= row.minStock) return 'text-warning'
  return ''
}

// 获取库存状态类型
const getStockStatusType = (row) => {
  if (row.stock === 0) return 'danger'
  if (row.stock <= row.minStock) return 'warning'
  return 'success'
}

// 获取库存状态文本
const getStockStatusText = (row) => {
  if (row.stock === 0) return '缺货'
  if (row.stock <= row.minStock) return '低库存'
  return '正常'
}

// 查询数据
const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: queryForm.keyword || undefined,
      warehouseId: queryForm.warehouseId || undefined,
      status: queryForm.status != null ? queryForm.status : undefined
    }

    const res = await listInventories(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0

    // 更新统计数据
    if (res.data.stats) {
      statistics.totalItems = res.data.stats.totalItems || 0
      statistics.totalValue = res.data.stats.totalValue || 0
      statistics.warningCount = res.data.stats.warningCount || 0
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.warehouseId = null
  queryForm.status = null
  pagination.pageNum = 1
  handleQuery()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 查看流水记录
const handleViewLog = (row) => {
  ElMessage.info(`查看"${row.materialName}"的库存流水记录`)
  // TODO: 跳转到库存流水页面或打开对话框
}

// 初始化
onMounted(() => {
  loadWarehouses()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.inventory-query-container {
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
