<template>
  <div class="inbound-detail-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button @click="handleBack" style="margin-right: 16px;">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">入库单详情</h2>
      <div v-if="detailData.id && !isVoid" class="header-actions">
        <el-button type="primary" @click="handleEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button type="danger" @click="handleVoid">
          <el-icon><Delete /></el-icon>
          作废
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="isVoid"
      type="info"
      :closable="false"
      show-icon
      title="此入库单已作废，增加的库存已扣回"
      style="margin-bottom: 16px;"
    />

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
        <el-descriptions-item label="入库类型">
          {{ getInboundTypeName(detailData.inboundType) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="isVoid" type="info">已作废</el-tag>
          <el-tag v-else type="success">已入库</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ detailData.createUser }}
        </el-descriptions-item>
        <el-descriptions-item label="入库时间">
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

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑入库单" width="720px">
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="物资和数量不能修改。如果数量录错，请作废此单后重新入库。"
        style="margin-bottom: 16px;"
      />
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入库类型" prop="inboundType">
              <el-select v-model="editForm.inboundType" style="width: 100%">
                <el-option
                  v-for="(name, value) in INBOUND_TYPES"
                  :key="value"
                  :label="name"
                  :value="Number(value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库时间" prop="inboundTime">
              <el-date-picker
                v-model="editForm.inboundTime"
                type="datetime"
                placeholder="选择入库时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>

      <el-table :data="editForm.details" border size="small">
        <el-table-column prop="materialName" label="物资名称" min-width="140" />
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column label="单价(元)" width="170" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.unitPrice"
              :min="0"
              :precision="2"
              :step="1"
              controls-position="right"
              size="small"
              style="width: 140px"
            />
          </template>
        </el-table-column>
        <el-table-column label="金额(元)" width="110" align="right">
          <template #default="{ row }">
            ¥{{ ((row.unitPrice || 0) * (row.quantity || 0)).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
      <div class="edit-total">
        总金额 <span class="amount">¥{{ editTotalAmount.toFixed(2) }}</span>
      </div>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getInboundById, updateInbound } from '@/api/inbound'
import { confirmVoidInbound, INBOUND_STATUS_VOID } from '../voidInbound'

// 入库类型
const INBOUND_TYPES = {
  1: '采购入库',
  2: '退货入库',
  3: '调拨入库',
  4: '其他入库'
}

const getInboundTypeName = (type) => INBOUND_TYPES[type] || '-'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detailData = ref({
  code: '',
  inboundType: null,
  inboundTime: '',
  status: null,
  warehouseName: '',
  createUser: '',
  createTime: '',
  itemCount: 0,
  totalAmount: 0,
  remark: '',
  details: []
})

const isVoid = computed(() => detailData.value.status === INBOUND_STATUS_VOID)

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
    const data = res.data
    detailData.value = {
      id: data.id,
      code: data.inboundNo, // 后端字段名是 inboundNo
      inboundType: data.inboundType,
      inboundTime: data.inboundTime,
      status: data.status,
      warehouseName: data.warehouseName,
      createUser: data.operatorName || data.createBy, // 使用操作人姓名或创建人
      createTime: data.inboundTime || data.createTime, // 优先使用入库时间
      itemCount: data.details?.length || 0,
      totalAmount: data.totalAmount || 0,
      remark: data.remark || '',
      details: (data.details || []).map(detail => ({
        id: detail.id,
        materialCode: detail.materialCode,
        materialName: detail.materialName,
        spec: detail.spec,
        unit: detail.unit,
        quantity: detail.quantity,
        price: detail.unitPrice, // 后端字段名是 unitPrice
        amount: detail.amount
      }))
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

// 编辑对话框
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const saveLoading = ref(false)
const editForm = reactive({
  inboundType: null,
  inboundTime: '',
  remark: '',
  details: []
})
const editRules = {
  inboundType: [{ required: true, message: '请选择入库类型', trigger: 'change' }],
  inboundTime: [{ required: true, message: '请选择入库时间', trigger: 'change' }]
}

const editTotalAmount = computed(() => {
  return editForm.details.reduce((sum, item) => sum + (item.unitPrice || 0) * (item.quantity || 0), 0)
})

// 编辑
const handleEdit = () => {
  editForm.inboundType = detailData.value.inboundType
  editForm.inboundTime = detailData.value.inboundTime
  editForm.remark = detailData.value.remark
  editForm.details = detailData.value.details.map(item => ({
    id: item.id,
    materialName: item.materialName,
    quantity: item.quantity,
    unitPrice: item.price ?? 0
  }))
  editDialogVisible.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  try {
    await editFormRef.value.validate()
  } catch {
    return
  }

  saveLoading.value = true
  try {
    await updateInbound(detailData.value.id, {
      inboundType: editForm.inboundType,
      inboundTime: editForm.inboundTime,
      remark: editForm.remark,
      details: editForm.details.map(item => ({ id: item.id, unitPrice: item.unitPrice ?? 0 }))
    })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadDetail()
  } catch (error) {
    // 错误提示已由请求拦截器展示
    console.error('保存入库单失败:', error)
  } finally {
    saveLoading.value = false
  }
}

// 作废
const handleVoid = async () => {
  const voided = await confirmVoidInbound({ id: detailData.value.id, inboundNo: detailData.value.code })
  if (voided) {
    loadDetail()
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

  .edit-total {
    margin-top: 12px;
    text-align: right;
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
