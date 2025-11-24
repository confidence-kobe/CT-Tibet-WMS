<template>
  <div class="approval-pending-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">待审批申请</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="申请单号">
          <el-input
            v-model="queryForm.keyword"
            placeholder="请输入申请单号"
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
            style="width: 160px"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申请日期">
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
        <el-table-column prop="applyNo" label="申请单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.applyNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="itemCount" label="物资种类" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.itemCount }}种</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="申请金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column prop="applyReason" label="申请原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button link type="danger" size="small" @click="handleReject(row)">
              拒绝
            </el-button>
            <el-button link type="primary" size="small" @click="handleView(row)">
              详情
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

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalVisible"
      :title="approvalType === 'approve' ? '审批通过' : '审批拒绝'"
      width="700px"
    >
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="申请单号">
          {{ currentRow.applyNo }}
        </el-form-item>
        <el-form-item label="申请人">
          {{ currentRow.applicantName }} - {{ currentRow.deptName }}
        </el-form-item>
        <el-form-item label="仓库">
          {{ currentRow.warehouseName }}
        </el-form-item>
        <el-form-item label="物资种类">
          <el-tag>{{ currentRow.itemCount }}种</el-tag>
        </el-form-item>
        <el-form-item label="申请金额">
          <span class="amount">¥{{ currentRow.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-form-item>
        <el-form-item label="申请原因">
          {{ currentRow.applyReason || '-' }}
        </el-form-item>

        <!-- 库存检查（通过时） -->
        <el-alert
          v-if="approvalType === 'approve' && hasStockWarning"
          title="库存不足警告"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 16px;"
        >
          <template #default>
            部分物资库存不足，审批通过后可能无法正常出库
          </template>
        </el-alert>

        <el-form-item
          label="审批意见"
          :prop="approvalType === 'reject' ? 'opinion' : ''"
          :rules="approvalType === 'reject' ? [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }] : []"
        >
          <el-input
            v-model="approvalForm.opinion"
            type="textarea"
            :rows="3"
            :placeholder="approvalType === 'approve' ? '请输入审批意见(可选)' : '请输入拒绝原因'"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- 申请明细 -->
      <el-divider content-position="left">申请明细</el-divider>
      <el-table :data="currentRow.details" border stripe style="width: 100%" max-height="300">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialName" label="物资名称" min-width="120" />
        <el-table-column prop="spec" label="规格型号" width="100" />
        <el-table-column prop="quantity" label="申请数量" width="90" align="right" />
        <el-table-column prop="price" label="单价" width="90" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="approvalVisible = false">取消</el-button>
        <el-button
          :type="approvalType === 'approve' ? 'success' : 'danger'"
          :loading="saveLoading"
          @click="handleApprovalSubmit"
        >
          确认{{ approvalType === 'approve' ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="申请详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请单号">
          {{ currentRow.applyNo }}
        </el-descriptions-item>
        <el-descriptions-item label="申请人">
          {{ currentRow.applicantName }}
        </el-descriptions-item>
        <el-descriptions-item label="部门">
          {{ currentRow.deptName }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库">
          {{ currentRow.warehouseName }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ currentRow.applyTime }}
        </el-descriptions-item>
        <el-descriptions-item label="物资种类">
          <el-tag>{{ currentRow.itemCount }}种</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请金额">
          <span class="amount">¥{{ currentRow.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">
          {{ currentRow.applyReason || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">申请明细</el-divider>

      <el-table :data="currentRow.details" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="spec" label="规格型号" width="120" />
        <el-table-column prop="quantity" label="申请数量" width="90" align="right" />
        <el-table-column prop="price" label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success" @click="handleApprove(currentRow)">
          审批通过
        </el-button>
        <el-button type="danger" @click="handleReject(currentRow)">
          审批拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingApplies, getApplyById, approveApply } from '@/api/apply'
import { listWarehouses } from '@/api/warehouse'
import { listInventories } from '@/api/inventory'

// 查询表单
const queryForm = reactive({
  keyword: '',
  warehouseId: null,
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

// 对话框
const approvalVisible = ref(false)
const detailVisible = ref(false)
const saveLoading = ref(false)

// 审批类型
const approvalType = ref('approve') // approve | reject

// 审批表单
const approvalForm = reactive({
  opinion: ''
})

// 当前行数据
const currentRow = ref({
  details: []
})

// 检查是否有库存警告
const hasStockWarning = computed(() => {
  return currentRow.value.details?.some(item => item.quantity > (item.stock || 0)) || false
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
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: queryForm.keyword || undefined,
      warehouseId: queryForm.warehouseId || undefined,
      startDate: queryForm.dateRange?.[0] || undefined,
      endDate: queryForm.dateRange?.[1] || undefined
    }

    const res = await getPendingApplies(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
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
  queryForm.dateRange = null
  pagination.pageNum = 1
  handleQuery()
}

// 查看详情
const handleView = async (row) => {
  try {
    // 加载完整的申请详情（包含明细）
    const res = await getApplyById(row.id)
    currentRow.value = res.data || {}
    detailVisible.value = true
  } catch (error) {
    console.error('加载申请详情失败:', error)
    ElMessage.error('加载详情失败')
  }
}

// 审批通过
const handleApprove = async (row) => {
  try {
    // 加载完整的申请详情（包含明细）
    const res = await getApplyById(row.id)
    currentRow.value = res.data || {}

    // 重置审批表单
    approvalType.value = 'approve'
    approvalForm.opinion = ''

    approvalVisible.value = true
  } catch (error) {
    console.error('加载申请详情失败:', error)
    ElMessage.error('加载详情失败')
  }
}

// 审批拒绝
const handleReject = async (row) => {
  try {
    // 加载完整的申请详情（包含明细）
    const res = await getApplyById(row.id)
    currentRow.value = res.data || {}

    // 重置审批表单
    approvalType.value = 'reject'
    approvalForm.opinion = ''

    approvalVisible.value = true
  } catch (error) {
    console.error('加载申请详情失败:', error)
    ElMessage.error('加载详情失败')
  }
}

// 库存检查（可选）
const checkInventory = async (apply) => {
  try {
    for (const detail of apply.details || []) {
      const res = await listInventories({
        warehouseId: apply.warehouseId,
        materialId: detail.materialId,
        pageNum: 1,
        pageSize: 1
      })
      const stock = res.data.list?.[0]?.quantity || 0
      if (stock < detail.quantity) {
        return false
      }
    }
    return true
  } catch (error) {
    console.error('库存检查失败:', error)
    return true // 检查失败时不阻止审批
  }
}

// 提交审批
const handleApprovalSubmit = async () => {
  // 拒绝时必须填写原因
  if (approvalType.value === 'reject' && !approvalForm.opinion.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  // 通过时可选库存检查
  if (approvalType.value === 'approve') {
    const hasStock = await checkInventory(currentRow.value)
    if (!hasStock) {
      try {
        await ElMessageBox.confirm(
          '部分物资库存不足，确定要通过审批吗？',
          '库存警告',
          { type: 'warning' }
        )
      } catch {
        return // 用户取消
      }
    }
  }

  saveLoading.value = true
  try {
    // approvalStatus: 1-通过, 2-拒绝
    const approvalStatus = approvalType.value === 'approve' ? 1 : 2
    await approveApply(
      currentRow.value.id,
      approvalStatus,
      approvalStatus === 2 ? approvalForm.opinion : undefined
    )

    const message = approvalType.value === 'approve'
      ? '审批通过成功，已自动创建出库单'
      : '审批拒绝成功'

    ElMessage.success(message)
    approvalVisible.value = false
    detailVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error('审批失败')
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
.approval-pending-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }
}
</style>
