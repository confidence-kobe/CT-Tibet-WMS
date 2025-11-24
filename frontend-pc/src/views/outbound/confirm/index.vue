<template>
  <div class="outbound-confirm-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">确认领取</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="出库单号">
          <el-input
            v-model="queryForm.keyword"
            placeholder="请输入出库单号"
            clearable
            @clear="handleQuery"
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
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="创建日期">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleQuery"
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
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="outboundNo" label="出库单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row)">{{ row.outboundNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="applyNo" label="关联申请单号" width="180">
          <template #default="{ row }">
            <el-link v-if="row.applyNo" type="primary" @click="handleViewApply(row)">
              {{ row.applyNo }}
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="领取人" width="100" />
        <el-table-column prop="receiverPhone" label="联系电话" width="120" />
        <el-table-column prop="warehouseName" label="仓库" min-width="150" />
        <el-table-column prop="totalAmount" label="出库金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button link type="success" size="small" @click="handleConfirm(row)">
              确认领取
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 16px; text-align: right;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 确认领取对话框 -->
    <el-dialog
      v-model="confirmVisible"
      title="确认领取"
      width="800px"
    >
      <el-alert
        title="提示"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 16px;"
      >
        请确认领取人已到现场领取物资，确认后将扣减库存，此操作不可撤销！
      </el-alert>

      <el-descriptions :column="2" border style="margin-bottom: 16px;">
        <el-descriptions-item label="出库单号">
          {{ currentRow.outboundNo }}
        </el-descriptions-item>
        <el-descriptions-item label="申请单号">
          {{ currentRow.applyNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="领取人">
          {{ currentRow.receiverName }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ currentRow.receiverPhone }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库">
          {{ currentRow.warehouseName }}
        </el-descriptions-item>
        <el-descriptions-item label="出库金额">
          <span class="amount">¥{{ currentRow.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ currentRow.createTime }}
        </el-descriptions-item>
      </el-descriptions>

      <el-table :data="currentRow.details" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="model" label="规格型号" width="120" />
        <el-table-column prop="quantity" label="出库数量" width="100" align="right" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
      </el-table>

      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="success" :loading="saveLoading" @click="handleConfirmSubmit">
          确认领取
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listOutbounds, confirmOutbound } from '@/api/outbound'
import { listWarehouses } from '@/api/warehouse'

const router = useRouter()

const queryForm = reactive({
  keyword: '',
  warehouseId: null,
  dateRange: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const confirmVisible = ref(false)
const saveLoading = ref(false)

// 仓库列表
const warehouseList = ref([])

const currentRow = ref({
  details: []
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

// 查询数据
const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      status: 0, // 固定查询待领取状态
      keyword: queryForm.keyword || undefined,
      warehouseId: queryForm.warehouseId || undefined,
      startDate: queryForm.dateRange ? queryForm.dateRange[0] : undefined,
      endDate: queryForm.dateRange ? queryForm.dateRange[1] : undefined
    }

    const res = await listOutbounds(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('查询待领取出库单失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.warehouseId = null
  queryForm.dateRange = null
  pagination.page = 1
  handleQuery()
}

// 查看详情
const handleViewDetail = (row) => {
  router.push({ path: `/outbound/detail/${row.id}` })
}

// 查看关联申请单
const handleViewApply = (row) => {
  if (row.applyId) {
    router.push({ path: `/apply/detail/${row.applyId}` })
  }
}

// 确认领取
const handleConfirm = (row) => {
  currentRow.value = row
  confirmVisible.value = true
}

// 提交确认
const handleConfirmSubmit = async () => {
  saveLoading.value = true

  try {
    await confirmOutbound(currentRow.value.id)
    ElMessage.success('确认领取成功，库存已扣减')
    confirmVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('确认失败:', error)
  } finally {
    saveLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadWarehouses()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.outbound-confirm-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }
}
</style>
