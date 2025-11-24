<template>
  <div class="inventory-warning-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">库存预警</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="6">
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
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon urgent">
              <el-icon size="32"><CircleCloseFilled /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">紧急预警</div>
              <div class="stats-value urgent-text">{{ statistics.urgent }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon warning">
              <el-icon size="32"><WarningFilled /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">一般预警</div>
              <div class="stats-value warning-text">{{ statistics.normal }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon handled">
              <el-icon size="32"><CircleCheckFilled /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">已处理</div>
              <div class="stats-value">{{ statistics.handled }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="仓库">
          <el-select
            v-model="queryForm.warehouseId"
            placeholder="请选择仓库"
            clearable
            @clear="handleQuery"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物资">
          <el-input
            v-model="queryForm.keyword"
            placeholder="物资编号/名称"
            clearable
            @clear="handleQuery"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="预警级别">
          <el-select
            v-model="queryForm.warningLevel"
            placeholder="请选择预警级别"
            clearable
            @clear="handleQuery"
          >
            <el-option label="紧急" :value="2" />
            <el-option label="一般" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select
            v-model="queryForm.isHandled"
            placeholder="请选择处理状态"
            clearable
            @clear="handleQuery"
          >
            <el-option label="未处理" :value="false" />
            <el-option label="已处理" :value="true" />
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
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column prop="currentStock" label="当前库存" width="100" align="right">
          <template #default="{ row }">
            <span :class="getStockClass(row)">{{ row.currentStock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningThreshold" label="预警阈值" width="100" align="right" />
        <el-table-column prop="warningLevel" label="预警级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getWarningType(row)" size="small">
              {{ getWarningLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warningTime" label="预警时间" width="160" />
        <el-table-column prop="handleStatus" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.handleStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.handleStatus === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button
              v-if="row.handleStatus === 0"
              link
              type="success"
              size="small"
              @click="handleMarkHandled(row)"
            >
              标记已处理
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { listLowStockAlerts } from '@/api/inventory'
import { listWarehouses } from '@/api/warehouse'

const router = useRouter()

const queryForm = reactive({
  warehouseId: null,
  keyword: '',
  warningLevel: null,
  isHandled: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

const statistics = reactive({
  total: 0,
  urgent: 0,
  normal: 0,
  handled: 0
})

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
  }
}

const getWarningLevel = (row) => {
  // 后端返回的warningLevel: 1-一般预警, 2-紧急预警
  return row.warningLevel === 2 ? 'urgent' : 'normal'
}

const getWarningType = (row) => {
  return row.warningLevel === 2 ? 'danger' : 'warning'
}

const getWarningLabel = (row) => {
  return row.warningLevel === 2 ? '紧急' : '一般'
}

const getStockClass = (row) => {
  return row.warningLevel === 2 ? 'urgent-stock' : 'warning-stock'
}

const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      warehouseId: queryForm.warehouseId || undefined,
      keyword: queryForm.keyword || undefined,
      warningLevel: queryForm.warningLevel || undefined,
      isHandled: queryForm.isHandled != null ? queryForm.isHandled : undefined
    }

    const res = await listLowStockAlerts(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0

    // 更新统计数据
    if (res.data.stats) {
      statistics.total = res.data.stats.totalWarnings || 0
      statistics.urgent = res.data.stats.urgentWarnings || 0
      statistics.normal = res.data.stats.normalWarnings || 0
      statistics.handled = res.data.stats.handledWarnings || 0
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.warehouseId = null
  queryForm.keyword = ''
  queryForm.warningLevel = null
  queryForm.isHandled = null
  pagination.pageNum = 1
  handleQuery()
}

const handleViewDetail = (row) => {
  router.push({ path: `/inventory/detail/${row.id}` })
}

const handleMarkHandled = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要将物资"${row.materialName}"标记为已处理吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // TODO: 调用标记已处理的API
    ElMessage.success('标记成功')
    handleQuery()
  } catch (error) {
    // 用户取消
  }
}

// 初始化
onMounted(() => {
  loadWarehouses()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.inventory-warning-container {
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

        &.handled {
          background: #f6ffed;
          color: #52c41a;
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
