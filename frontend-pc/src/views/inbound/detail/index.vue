<template>
  <div class="inbound-detail-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button @click="handleBack" style="margin-right: 16px;">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">入库单详情</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button type="danger" @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </div>
    </div>

    <!-- 基本信息 -->
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <span class="card-header-title">基本信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="入库单号">
          {{ detailData.code }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库名称">
          {{ detailData.warehouseName }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人">
          {{ detailData.createUser }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detailData.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="物资种类">
          <el-tag>{{ detailData.itemCount }}种</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入库金额">
          <span class="amount">¥{{ detailData.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ detailData.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 入库明细 -->
    <el-card shadow="never" style="margin-top: 16px;" v-loading="loading">
      <template #header>
        <span class="card-header-title">入库明细</span>
      </template>
      <el-table :data="detailData.details" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="spec" label="规格型号" width="120" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
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

      <!-- 合计行 -->
      <div class="summary-row">
        <span class="summary-label">合计：</span>
        <span class="summary-value">
          物资种类 <strong>{{ detailData.itemCount }}</strong> 种，
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
import { getInboundById } from '@/api/inbound'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detailData = ref({
  code: '',
  warehouseName: '',
  createUser: '',
  createTime: '',
  itemCount: 0,
  totalAmount: 0,
  remark: '',
  details: []
})

// 计算总数量
const totalQuantity = computed(() => {
  return detailData.value.details.reduce((sum, item) => sum + (item.quantity || 0), 0)
})

// 加载详情数据
const loadDetail = async () => {
  loading.value = true
  const id = route.params.id

  try {
    const res = await getInboundById(id)

    if (res.code === 200) {
      const data = res.data
      detailData.value = {
        id: data.id,
        code: data.inboundNo, // 后端字段名是 inboundNo
        warehouseName: data.warehouseName,
        createUser: data.operatorName || data.createBy, // 使用操作人姓名或创建人
        createTime: data.inboundTime || data.createTime, // 优先使用入库时间
        itemCount: data.details?.length || 0,
        totalAmount: data.totalAmount || 0,
        remark: data.remark || '',
        details: (data.details || []).map(detail => ({
          materialCode: detail.materialCode,
          materialName: detail.materialName,
          spec: detail.spec,
          unit: detail.unit,
          quantity: detail.quantity,
          price: detail.unitPrice, // 后端字段名是 unitPrice
          amount: detail.amount
        }))
      }
    } else {
      ElMessage.error(res.msg || '获取详情失败')
      router.back()
    }
  } catch (error) {
    console.error('加载入库单详情失败:', error)
    ElMessage.error('加载详情失败，请稍后重试')
    router.back()
  } finally {
    loading.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  // TODO: 后端暂未提供编辑接口，编辑页面也未创建
  ElMessage.warning('编辑功能暂未开放')
  // router.push(`/inbound/edit/${route.params.id}`)
}

// 删除
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除入库单"${detailData.value.code}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 后端暂未提供删除接口，需要后续添加
    ElMessage.warning('删除功能暂未开放')
    // 待后端添加接口后实现:
    // const res = await deleteInbound(detailData.value.id)
    // if (res.code === 200) {
    //   ElMessage.success('删除成功')
    //   router.push('/inbound/list')
    // }
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.inbound-detail-container {
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
