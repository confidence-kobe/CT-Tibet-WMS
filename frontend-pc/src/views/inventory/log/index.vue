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
            placeholder="请选择仓库"
            clearable
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
        <el-form-item label="变动类型">
          <el-select
            v-model="queryForm.changeType"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="入库" :value="1" />
            <el-option label="出库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
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
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column prop="warehouseName" label="仓库" width="130" />
        <el-table-column prop="materialName" label="物资名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="changeType" label="变动类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.changeType === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ row.changeType === 1 ? '入库' : '出库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeQuantity" label="变动数量" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.changeType === 1 ? 'qty-in' : 'qty-out'">
              {{ row.changeType === 1 ? '+' : '-' }}{{ row.changeQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeQuantity" label="变动前数量" width="110" align="right" />
        <el-table-column prop="afterQuantity" label="变动后数量" width="110" align="right" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="relatedNo" label="关联单号" width="160" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.remark || '-' }}
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
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { listInventoryLogs } from '@/api/inventory'
import { listWarehouses } from '@/api/warehouse'

const route = useRoute()

// 查询表单
const queryForm = reactive({
  warehouseId: null,
  materialId: null,
  changeType: null,
  dateRange: null
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

// 加载仓库列表
const loadWarehouses = async () => {
  try {
    const res = await listWarehouses({ status: 0 })
    warehouseList.value = res.data || []
  } catch (error) {
    console.error('加载仓库列表失败:', error)
    ElMessage.error('加载仓库列表失败')
  }
}

// 查询数据
const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      warehouseId: queryForm.warehouseId || undefined,
      materialId: queryForm.materialId || undefined,
      changeType: queryForm.changeType ?? undefined,
      startDate: queryForm.dateRange?.[0] || undefined,
      endDate: queryForm.dateRange?.[1] || undefined
    }

    const res = await listInventoryLogs(params)
    tableData.value = res.data || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询库存流水失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryForm.warehouseId = null
  queryForm.changeType = null
  queryForm.dateRange = null
  pagination.pageNum = 1
  handleQuery()
}

// 初始化
onMounted(() => {
  loadWarehouses()
  if (route.query.materialId) queryForm.materialId = Number(route.query.materialId)
  if (route.query.warehouseId) queryForm.warehouseId = Number(route.query.warehouseId)
  handleQuery()
})
</script>

<style lang="scss" scoped>
.inventory-log-container {
  .page-header {
    margin-bottom: 16px;

    .page-title {
      margin: 0;
    }
  }

  .search-form {
    margin-bottom: 0;
  }

  .qty-in {
    color: var(--el-color-success);
    font-weight: 500;
  }

  .qty-out {
    color: var(--el-color-danger);
    font-weight: 500;
  }
}
</style>
