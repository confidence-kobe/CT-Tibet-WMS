<template>
  <div class="outbound-detail-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button @click="handleBack" style="margin-right: 16px;">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">出库单详情</h2>
      <div class="header-actions">
        <el-button
          v-if="detailData.status === 0"
          type="success"
          @click="handleConfirm"
        >
          <el-icon><Check /></el-icon>
          确认领取
        </el-button>
        <el-button
          v-if="detailData.status === 0"
          type="danger"
          @click="handleCancel"
        >
          <el-icon><Close /></el-icon>
          取消出库
        </el-button>
      </div>
    </div>

    <!-- 基本信息 -->
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <span class="card-header-title">基本信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="出库单号">
          {{ detailData.outboundNo }}
        </el-descriptions-item>
        <el-descriptions-item label="出库类型">
          <el-tag type="primary">
            {{ ['', '生产领用', '维修领用', '项目使用', '其他出库'][detailData.outboundType] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源">
          <el-tag :type="detailData.source === 1 ? 'success' : 'primary'">
            {{ detailData.source === 1 ? '直接出库' : '申请出库' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出库状态">
          <el-tag :type="getStatusType(detailData.status)">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="仓库名称">
          {{ detailData.warehouseName }}
        </el-descriptions-item>
        <el-descriptions-item label="领取人">
          {{ detailData.receiverName }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ detailData.receiverPhone }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ detailData.operatorName }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detailData.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="出库时间">
          {{ detailData.outboundTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="出库金额">
          <span class="amount">¥{{ detailData.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ detailData.remark || '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="detailData.applyNo"
          label="关联申请单"
          :span="2"
        >
          <el-link type="primary" @click="handleViewApply">{{ detailData.applyNo }}</el-link>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 出库明细 -->
    <el-card shadow="never" style="margin-top: 16px;" v-loading="loading">
      <template #header>
        <span class="card-header-title">出库明细</span>
      </template>
      <el-table :data="detailData.details" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="model" label="规格型号" width="120" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="unitPrice" label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.unitPrice?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 合计行 -->
      <div class="summary-row">
        <span class="summary-label">合计：</span>
        <span class="summary-value">
          物资种类 <strong>{{ detailData.details?.length || 0 }}</strong> 种，
          总数量 <strong>{{ totalQuantity }}</strong>，
          总金额 <span class="amount"><strong>¥{{ detailData.totalAmount?.toFixed(2) || '0.00' }}</strong></span>
        </span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOutboundById, confirmOutbound, cancelOutbound } from '@/api/outbound'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detailData = ref({
  outboundNo: '',
  outboundType: 1,
  source: 1,
  warehouseName: '',
  receiverName: '',
  receiverPhone: '',
  operatorName: '',
  createTime: '',
  outboundTime: '',
  status: 0,
  totalAmount: 0,
  remark: '',
  applyNo: '',
  details: []
})

// 计算总数量
const totalQuantity = computed(() => {
  return detailData.value.details.reduce((sum, item) => sum + (item.quantity || 0), 0)
})

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '待领取',
    1: '已完成',
    2: '已取消'
  }
  return textMap[status] || '未知'
}

// 加载详情数据
const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getOutboundById(id)
    detailData.value = res.data || {}
  } catch (error) {
    console.error('加载出库单详情失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 取消出库
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要取消出库单"${detailData.value.outboundNo}"吗？`,
      '取消出库',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await cancelOutbound(detailData.value.id, '手动取消')
    ElMessage.success('取消成功')
    loadDetail()
  } catch (error) {
    // 用户取消或接口错误
  }
}

// 确认领取
const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm(
      `确认已领取出库单"${detailData.value.outboundNo}"的物资吗？确认后将扣减库存。`,
      '确认领取',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await confirmOutbound(detailData.value.id)
    ElMessage.success('确认领取成功，库存已扣减')
    loadDetail()
  } catch (error) {
    // 用户取消或接口错误
  }
}

// 查看关联申请单
const handleViewApply = () => {
  if (detailData.value.applyId) {
    router.push(`/apply/detail/${detailData.value.applyId}`)
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.outbound-detail-container {
  .page-header {
    display: flex;
    align-items: center;

    .page-title {
      flex: 1;
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .card-header-title {
    font-weight: 500;
    font-size: 16px;
  }

  .amount {
    color: $error-color;
    font-weight: 500;
  }

  .summary-row {
    margin-top: 16px;
    padding: 12px 16px;
    background-color: #f5f7fa;
    border-radius: 4px;
    text-align: right;

    .summary-label {
      font-weight: 500;
      margin-right: 16px;
    }

    .summary-value {
      font-size: 14px;

      strong {
        color: $primary-color;
        font-size: 16px;
        margin: 0 4px;
      }
    }
  }
}
</style>
