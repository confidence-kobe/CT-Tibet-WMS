<template>
  <div class="approval-detail-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">申请审批详情</h2>
      <div class="header-actions">
        <template v-if="detail.status === 0">
          <el-button type="success" :loading="approveLoading" @click="handleApprove">
            审批通过
          </el-button>
          <el-button type="danger" @click="showRejectPanel = !showRejectPanel">
            审批拒绝
          </el-button>
        </template>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" style="min-height: 400px;">
      <!-- 基本信息 -->
      <el-card shadow="never" class="info-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="申请单号">
            {{ detail.applyNo }}
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            {{ detail.applicantName }}
          </el-descriptions-item>
          <el-descriptions-item label="部门">
            {{ detail.deptName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="仓库名称">
            {{ detail.warehouseName }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ detail.applyTime }}
          </el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <el-tag :type="statusTagType(detail.status)" size="small">
              {{ statusText(detail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审批人">
            {{ detail.approverName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审批时间">
            {{ detail.approvalTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请金额">
            <span class="amount">¥{{ detail.totalAmount?.toFixed(2) || '0.00' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="3">
            {{ detail.applyReason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="detail.outboundNo"
            label="关联出库单"
            :span="3"
          >
            {{ detail.outboundNo }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="detail.status === 2"
            label="拒绝原因"
            :span="3"
          >
            <el-text type="danger">{{ detail.rejectReason || detail.approvalRemark || '-' }}</el-text>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 拒绝原因输入面板（内联展示） -->
      <el-card v-if="showRejectPanel && detail.status === 0" shadow="never" class="reject-card">
        <template #header>
          <div class="card-header">
            <span>填写拒绝原因</span>
          </div>
        </template>
        <el-form :model="rejectForm" label-width="100px">
          <el-form-item label="拒绝原因" required>
            <el-input
              v-model="rejectForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入拒绝原因（必填）"
              maxlength="200"
              show-word-limit
              style="max-width: 600px"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="danger"
              :loading="rejectLoading"
              @click="handleRejectSubmit"
            >
              确认拒绝
            </el-button>
            <el-button @click="showRejectPanel = false; rejectForm.reason = ''">
              取消
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 申请明细 -->
      <el-card shadow="never" style="margin-top: 16px;">
        <template #header>
          <div class="card-header">
            <span>申请明细</span>
          </div>
        </template>
        <el-table
          :data="detail.details"
          border
          stripe
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="materialCode" label="物资编码" width="120" />
          <el-table-column prop="materialName" label="物资名称" min-width="150" />
          <el-table-column prop="spec" label="规格型号" width="140" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column prop="quantity" label="申请数量" width="100" align="right" />
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
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getApplyById, approveApply } from '@/api/apply'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const approveLoading = ref(false)
const rejectLoading = ref(false)
const showRejectPanel = ref(false)

const detail = ref({
  id: null,
  applyNo: '',
  applicantName: '',
  deptName: '',
  warehouseName: '',
  applyTime: '',
  status: 0,
  approverName: '',
  approvalTime: '',
  rejectReason: '',
  approvalRemark: '',
  totalAmount: 0,
  applyReason: '',
  outboundNo: '',
  outboundId: null,
  details: []
})

const rejectForm = reactive({
  reason: ''
})

const STATUS_MAP = {
  0: { text: '待审批', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已拒绝', type: 'danger' },
  3: { text: '已完成', type: 'info' },
  4: { text: '已取消', type: '' }
}

const statusText = (status) => STATUS_MAP[status]?.text ?? '未知'
const statusTagType = (status) => STATUS_MAP[status]?.type ?? ''

const loadDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('缺少申请单ID')
    handleBack()
    return
  }

  loading.value = true
  try {
    const res = await getApplyById(id)
    detail.value = res.data || {}
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.back()
}

const handleApprove = async () => {
  approveLoading.value = true
  try {
    await approveApply(detail.value.id, 1, '')
    ElMessage.success('审批通过成功，已自动创建出库单')
    handleBack()
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error('审批操作失败')
  } finally {
    approveLoading.value = false
  }
}

const handleRejectSubmit = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  rejectLoading.value = true
  try {
    await approveApply(detail.value.id, 2, rejectForm.reason.trim())
    ElMessage.success('已拒绝该申请')
    handleBack()
  } catch (error) {
    console.error('拒绝失败:', error)
    ElMessage.error('审批操作失败')
  } finally {
    rejectLoading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.approval-detail-container {
  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;

    .page-title {
      flex: 1;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .info-card {
    margin-bottom: 16px;
  }

  .reject-card {
    margin-top: 16px;
    margin-bottom: 16px;
  }

  .card-header {
    font-size: 16px;
    font-weight: 500;
  }

  .amount {
    color: $error-color;
    font-weight: 500;
    font-size: 14px;
  }
}
</style>
