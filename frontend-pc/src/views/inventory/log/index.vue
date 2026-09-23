<template>
  <div class="inventory-log-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">库存流水</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
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
        <el-form-item label="物资">
          <el-select
            v-model="queryForm.materialId"
            placeholder="全部物资"
            clearable
            filterable
            @change="handleSearch"
            style="width: 220px"
          >
            <el-option
              v-for="material in materialList"
              :key="material.id"
              :label="`${material.materialCode} - ${material.materialName}`"
              :value="material.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="变动类型">
          <el-select
            v-model="queryForm.changeType"
            placeholder="全部"
            clearable
            @change="handleSearch"
            style="width: 120px"
          >
            <el-option
              v-for="(item, value) in changeTypeMap"
              :key="value"
              :label="item.text"
              :value="Number(value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
            style="width: 260px"
          />
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
        <el-table-column prop="createTime" label="时间" width="165" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column prop="materialCode" label="物资编码" width="110" />
        <el-table-column prop="materialName" label="物资名称" min-width="140" />
        <el-table-column prop="spec" label="规格" width="110" show-overflow-tooltip />
        <el-table-column label="变动类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="changeTypeMap[row.changeType]?.type" size="small">
              {{ changeTypeMap[row.changeType]?.text || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动数量" width="100" align="right">
          <template #default="{ row }">
            <span :class="quantityClass(row.changeQuantity)">
              {{ formatChange(row.changeQuantity) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动前" width="90" align="right">
          <template #default="{ row }">{{ formatNumber(row.beforeQuantity) }}</template>
        </el-table-column>
        <el-table-column label="变动后" width="90" align="right">
          <template #default="{ row }">{{ formatNumber(row.afterQuantity) }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="60" align="center" />
        <el-table-column prop="relatedNo" label="关联单号" min-width="170" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="90" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />

        <template #empty>
          <el-empty description="暂无库存流水" :image-size="80" />
        </template>
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listInventoryLogs } from '@/api/inventory'
import { getMyWarehouses } from '@/api/warehouse'
import { listAllMaterials } from '@/api/material'

const route = useRoute()

// 变动类型（后端 changeType：1-入库 2-出库 3-锁定 4-解锁）
const changeTypeMap = {
  1: { type: 'success', text: '入库' },
  2: { type: 'danger', text: '出库' },
  3: { type: 'warning', text: '锁定' },
  4: { type: 'info', text: '解锁' }
}

// 支持从库存查询页带参数跳转：/inventory/log?warehouseId=1&materialId=2
const toId = (value) => (value ? Number(value) : null)

const queryForm = reactive({
  warehouseId: toId(route.query.warehouseId),
  materialId: toId(route.query.materialId),
  changeType: null,
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const warehouseList = ref([])
const materialList = ref([])

const formatNumber = (value) => (value == null ? '-' : Number(value).toLocaleString())
const formatChange = (value) => {
  if (value == null) return '-'
  const num = Number(value)
  return num > 0 ? `+${num.toLocaleString()}` : num.toLocaleString()
}
const quantityClass = (value) => {
  const num = Number(value)
  if (num > 0) return 'text-success'
  if (num < 0) return 'text-danger'
  return ''
}

const loadOptions = async () => {
  try {
    const [warehouseRes, materials] = await Promise.all([
      getMyWarehouses(),
      listAllMaterials()
    ])
    warehouseList.value = warehouseRes.data || []
    materialList.value = materials
  } catch (error) {
    console.error('加载筛选项失败:', error)
  }
}

// 查询数据（错误提示由请求拦截器统一处理）
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await listInventoryLogs({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      warehouseId: queryForm.warehouseId || undefined,
      materialId: queryForm.materialId || undefined,
      changeType: queryForm.changeType || undefined,
      startDate: queryForm.dateRange?.[0] || undefined,
      endDate: queryForm.dateRange?.[1] || undefined
    })
    tableData.value = res.data || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('查询库存流水失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  handleQuery()
}

const handleReset = () => {
  queryForm.warehouseId = null
  queryForm.materialId = null
  queryForm.changeType = null
  queryForm.dateRange = []
  handleSearch()
}

onMounted(() => {
  loadOptions()
  handleQuery()
})

// 页面被 keep-alive 缓存，从库存查询页再次跳转时根据新的参数刷新
watch(
  () => route.query,
  (query) => {
    if (route.path !== '/inventory/log') return
    queryForm.warehouseId = toId(query.warehouseId)
    queryForm.materialId = toId(query.materialId)
    handleSearch()
  }
)
</script>

<style lang="scss" scoped>
.inventory-log-container {
  .text-success {
    color: $success-color;
    font-weight: 500;
  }

  .text-danger {
    color: $error-color;
    font-weight: 500;
  }
}
</style>
