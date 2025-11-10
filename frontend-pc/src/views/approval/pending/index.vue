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
            v-model="queryForm.code"
            placeholder="请输入申请单号"
            clearable
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="申请人">
          <el-input
            v-model="queryForm.applicant"
            placeholder="请输入申请人姓名"
            clearable
            @clear="handleQuery"
          />
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
        <el-table-column prop="code" label="申请单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.code }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" min-width="150" />
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
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column prop="remark" label="申请原因" min-width="150" show-overflow-tooltip />
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

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalVisible"
      :title="approvalType === 'approve' ? '审批通过' : '审批拒绝'"
      width="600px"
    >
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="申请单号">
          {{ currentRow.code }}
        </el-form-item>
        <el-form-item label="申请人">
          {{ currentRow.applicant }} - {{ currentRow.deptName }}
        </el-form-item>
        <el-form-item label="申请金额">
          <span class="amount">¥{{ currentRow.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-form-item>
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
      </el-form>

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
          {{ currentRow.code }}
        </el-descriptions-item>
        <el-descriptions-item label="申请人">
          {{ currentRow.applicant }}
        </el-descriptions-item>
        <el-descriptions-item label="部门">
          {{ currentRow.deptName }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ currentRow.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="物资种类">
          <el-tag>{{ currentRow.itemCount }}种</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请金额">
          <span class="amount">¥{{ currentRow.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">
          {{ currentRow.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">申请明细</el-divider>

      <el-table :data="currentRow.details" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="spec" label="规格型号" width="120" />
        <el-table-column prop="quantity" label="申请数量" width="90" align="right" />
        <el-table-column prop="stock" label="当前库存" width="90" align="right">
          <template #default="{ row }">
            <el-tag
              :type="row.stock >= row.quantity ? 'success' : 'danger'"
              size="small"
            >
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
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
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 查询表单
const queryForm = reactive({
  code: '',
  applicant: '',
  dateRange: null
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

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
  return currentRow.value.details.some(item => item.quantity > item.stock)
})

// 查询数据
const handleQuery = () => {
  loading.value = true

  // TODO: 调用API获取数据
  // 模拟数据
  setTimeout(() => {
    tableData.value = [
      {
        id: 1,
        code: 'SQ202511110001',
        applicant: '王五',
        deptName: '网络运维部',
        itemCount: 2,
        totalAmount: 280.00,
        createTime: '2025-11-11 10:00:00',
        remark: '项目施工需要',
        details: [
          {
            materialCode: 'PJ001',
            materialName: '光纤连接器',
            spec: 'SC-UPC',
            quantity: 10,
            stock: 500,
            price: 20.00,
            amount: 200.00
          },
          {
            materialCode: 'PJ002',
            materialName: '网线',
            spec: '超五类',
            quantity: 20,
            stock: 1000,
            price: 4.00,
            amount: 80.00
          }
        ]
      },
      {
        id: 2,
        code: 'SQ202511100002',
        applicant: '赵六',
        deptName: '维护部',
        itemCount: 1,
        totalAmount: 1500.00,
        createTime: '2025-11-10 14:30:00',
        remark: '线路维修急需',
        details: [
          {
            materialCode: 'GX001',
            materialName: '光缆12芯',
            spec: '12芯单模',
            quantity: 1,
            stock: 95,
            price: 1500.00,
            amount: 1500.00
          }
        ]
      }
    ]
    pagination.total = 2
    loading.value = false
  }, 500)
}

// 重置查询
const handleReset = () => {
  queryForm.code = ''
  queryForm.applicant = ''
  queryForm.dateRange = null
  pagination.page = 1
  handleQuery()
}

// 查看详情
const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

// 审批通过
const handleApprove = (row) => {
  currentRow.value = row
  approvalType.value = 'approve'
  approvalForm.opinion = ''
  approvalVisible.value = true
}

// 审批拒绝
const handleReject = (row) => {
  currentRow.value = row
  approvalType.value = 'reject'
  approvalForm.opinion = ''
  approvalVisible.value = true
}

// 提交审批
const handleApprovalSubmit = async () => {
  // 拒绝时必须填写原因
  if (approvalType.value === 'reject' && !approvalForm.opinion) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  saveLoading.value = true

  try {
    // TODO: 调用审批API
    await new Promise(resolve => setTimeout(resolve, 1000))

    const message = approvalType.value === 'approve'
      ? '审批通过成功，已自动创建出库单'
      : '审批拒绝成功'

    ElMessage.success(message)
    approvalVisible.value = false
    detailVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('审批失败:', error)
  } finally {
    saveLoading.value = false
  }
}

// 初始化加载数据
handleQuery()
</script>

<style lang="scss" scoped>
.approval-pending-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }
}
</style>
