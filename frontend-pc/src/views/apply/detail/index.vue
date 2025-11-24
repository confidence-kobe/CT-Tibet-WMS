<template>
  <div class="apply-detail-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">申请详情</h2>
      <div class="header-actions">
        <el-button
          v-if="detail.status === 0"
          type="warning"
          @click="handleCancel"
        >
          撤回申请
        </el-button>
        <el-button
          v-if="detail.status === 1 && detail.outboundNo"
          type="primary"
          @click="handleViewOutbound"
        >
          查看出库单
        </el-button>
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
          <el-descriptions-item label="仓库名称">
            {{ detail.warehouseName }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ detail.applyTime }}
          </el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <StatusTag :status="detail.status" status-type="apply" />
          </el-descriptions-item>
          <el-descriptions-item label="审批人">
            {{ detail.approverName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审批时间">
            {{ detail.approvalTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请金额" :span="2">
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
            <el-link type="primary" @click="handleViewOutbound">
              {{ detail.outboundNo }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item
            v-if="detail.status === 2"
            label="拒绝原因"
            :span="3"
          >
            <el-text type="danger">{{ detail.rejectReason || '-' }}</el-text>
          </el-descriptions-item>
        </el-descriptions>
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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplyById, cancelApply } from '@/api/apply'
import StatusTag from '@/components/StatusTag/index.vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)

const detail = ref({
  id: null,
  applyNo: '',
  applicantName: '',
  deptName: '',
  applyTime: '',
  status: 0,
  approverName: '',
  approvalTime: '',
  rejectReason: '',
  totalAmount: 0,
  applyReason: '',
  outboundNo: '',
  outboundId: null,
  details: []
})

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

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要撤回申请"${detail.value.applyNo}"吗？`,
      '撤回确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await cancelApply(detail.value.id)
    ElMessage.success('撤回成功')
    handleBack()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回失败:', error)
    }
  }
}

const handleViewOutbound = () => {
  if (detail.value.outboundId) {
    router.push(`/outbound/detail/${detail.value.outboundId}`)
  } else if (detail.value.outboundNo) {
    router.push(`/outbound/detail/${detail.value.outboundNo}`)
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.apply-detail-container {
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
