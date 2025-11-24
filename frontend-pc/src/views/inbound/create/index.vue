<template>
  <div class="inbound-create-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">新建入库单</h2>
      <el-button @click="handleCancel">
        <el-icon><Back /></el-icon>
        返回列表
      </el-button>
    </div>

    <!-- 表单卡片 -->
    <el-card shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <!-- 基本信息 -->
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="选择仓库" prop="warehouseId">
              <el-select
                v-model="form.warehouseId"
                placeholder="请选择仓库"
                style="width: 100%"
                filterable
              >
                <el-option
                  v-for="warehouse in warehouses"
                  :key="warehouse.id"
                  :label="warehouse.warehouseName"
                  :value="warehouse.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入库类型" prop="inboundType">
              <el-select
                v-model="form.inboundType"
                placeholder="请选择入库类型"
                style="width: 100%"
              >
                <el-option label="采购入库" :value="1" />
                <el-option label="退货入库" :value="2" />
                <el-option label="调拨入库" :value="3" />
                <el-option label="其他入库" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入库时间" prop="inboundTime">
              <el-date-picker
                v-model="form.inboundTime"
                type="datetime"
                placeholder="选择入库时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input
                v-model="form.remark"
                placeholder="请输入备注信息"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 入库明细 -->
        <el-form-item label="入库明细" required>
          <div style="width: 100%;">
            <div style="margin-bottom: 12px; text-align: right;">
              <el-button type="primary" size="small" @click="handleAddMaterial">
                <el-icon><Plus /></el-icon>
                添加物资
              </el-button>
            </div>

            <el-table
              :data="form.details"
              border
              stripe
              style="width: 100%"
            >
              <el-table-column label="物资" width="250">
                <template #default="{ row, $index }">
                  <el-select
                    v-model="row.materialId"
                    placeholder="请选择物资"
                    @change="handleMaterialChange($index)"
                    style="width: 100%"
                    filterable
                  >
                    <el-option
                      v-for="material in materials"
                      :key="material.id"
                      :label="`${material.code} - ${material.name}`"
                      :value="material.id"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="spec" label="规格型号" min-width="150">
                <template #default="{ row }">
                  <el-input v-model="row.spec" readonly />
                </template>
              </el-table-column>
              <el-table-column prop="unit" label="单位" width="80">
                <template #default="{ row }">
                  <el-input v-model="row.unit" readonly />
                </template>
              </el-table-column>
              <el-table-column label="数量" width="120">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.quantity"
                    :min="1"
                    :precision="2"
                    @change="handleQuantityChange($index)"
                    style="width: 100%"
                    controls-position="right"
                  />
                </template>
              </el-table-column>
              <el-table-column label="单价(元)" width="120">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.price"
                    :min="0"
                    :precision="2"
                    :step="0.01"
                    @change="handlePriceChange($index)"
                    style="width: 100%"
                    controls-position="right"
                  />
                </template>
              </el-table-column>
              <el-table-column label="金额(元)" width="120" align="right">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.amount.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center" fixed="right">
                <template #default="{ $index }">
                  <el-button
                    link
                    type="danger"
                    size="small"
                    @click="handleRemoveMaterial($index)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 合计 -->
            <div style="margin-top: 12px; text-align: right; font-size: 16px; font-weight: 500;">
              <span>合计金额：</span>
              <span class="amount" style="font-size: 18px;">¥{{ totalAmount.toFixed(2) }}</span>
            </div>

            <!-- 空状态 -->
            <el-empty
              v-if="form.details.length === 0"
              description="请添加入库物资"
              :image-size="100"
            />
          </div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="saveLoading" @click="handleSubmit">
            <el-icon><Check /></el-icon>
            提交入库
          </el-button>
          <el-button @click="handleCancel">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createInbound } from '@/api/inbound'
import { listWarehouses } from '@/api/warehouse'
import { listMaterials } from '@/api/material'

const router = useRouter()

// 仓库列表
const warehouses = ref([])

// 物资列表
const materials = ref([])

// 表单数据
const form = reactive({
  warehouseId: null,
  inboundType: 1, // 默认采购入库
  inboundTime: null,
  remark: '',
  details: []
})

// 表单引用
const formRef = ref(null)
const saveLoading = ref(false)

// 加载仓库列表
const loadWarehouses = async () => {
  try {
    const res = await listWarehouses({ status: 0 })
    if (res.code === 200) {
      warehouses.value = res.data || []
    }
  } catch (error) {
    console.error('加载仓库列表失败:', error)
  }
}

// 加载物资列表
const loadMaterials = async () => {
  try {
    const res = await listMaterials({
      pageNum: 1,
      pageSize: 1000,
      status: 0
    })
    if (res.code === 200) {
      materials.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载物资列表失败:', error)
  }
}

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  inboundType: [
    { required: true, message: '请选择入库类型', trigger: 'change' }
  ],
  inboundTime: [
    { required: true, message: '请选择入库时间', trigger: 'change' }
  ]
}

// 计算总金额
const totalAmount = computed(() => {
  return form.details.reduce((sum, item) => sum + item.amount, 0)
})

// 添加物资行
const handleAddMaterial = () => {
  form.details.push({
    materialId: null,
    materialCode: '',
    materialName: '',
    spec: '',
    unit: '',
    quantity: 1,
    price: 0,
    amount: 0
  })
}

// 移除物资行
const handleRemoveMaterial = (index) => {
  form.details.splice(index, 1)
}

// 物资选择变化
const handleMaterialChange = (index) => {
  const detail = form.details[index]
  const material = materials.value.find(m => m.id === detail.materialId)

  if (material) {
    detail.materialCode = material.code
    detail.materialName = material.name
    detail.spec = material.model || ''
    detail.unit = material.unit
    // 注意：后端物资表可能没有price字段，需要手动输入
    detail.price = material.price || 0
    calculateAmount(index)
  }
}

// 数量变化
const handleQuantityChange = (index) => {
  calculateAmount(index)
}

// 单价变化
const handlePriceChange = (index) => {
  calculateAmount(index)
}

// 计算行金额
const calculateAmount = (index) => {
  const detail = form.details[index]
  detail.amount = (detail.quantity || 0) * (detail.price || 0)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 验证基本信息
    await formRef.value.validate()

    // 验证明细
    if (form.details.length === 0) {
      ElMessage.warning('请至少添加一条入库明细')
      return
    }

    // 验证每一行
    for (let i = 0; i < form.details.length; i++) {
      const detail = form.details[i]
      if (!detail.materialId) {
        ElMessage.warning(`第 ${i + 1} 行：请选择物资`)
        return
      }
      if (!detail.quantity || detail.quantity <= 0) {
        ElMessage.warning(`第 ${i + 1} 行：请输入有效数量`)
        return
      }
      if (!detail.price || detail.price < 0) {
        ElMessage.warning(`第 ${i + 1} 行：请输入有效单价`)
        return
      }
    }

    // 确认提交
    await ElMessageBox.confirm(
      `确认提交入库单？入库金额：¥${totalAmount.value.toFixed(2)}`,
      '提交确认',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    saveLoading.value = true

    // 构建提交数据（符合后端InboundDTO结构）
    const submitData = {
      warehouseId: form.warehouseId,
      inboundType: form.inboundType,
      inboundTime: form.inboundTime,
      remark: form.remark || undefined,
      details: form.details.map(detail => ({
        materialId: detail.materialId,
        quantity: detail.quantity,
        unitPrice: detail.price // 后端字段名是 unitPrice
      }))
    }

    const res = await createInbound(submitData)

    if (res.code === 200) {
      ElMessage.success('入库单创建成功')
      router.push('/inbound/list')
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error('提交失败，请稍后重试')
    }
  } finally {
    saveLoading.value = false
  }
}

// 取消
const handleCancel = () => {
  if (form.details.length > 0) {
    ElMessageBox.confirm(
      '确定要取消吗？当前填写的数据将会丢失',
      '取消确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '继续填写',
        type: 'warning'
      }
    ).then(() => {
      router.push('/inbound/list')
    }).catch(() => {
      // 用户选择继续填写
    })
  } else {
    router.push('/inbound/list')
  }
}

// 格式化当前时间
const formatDateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 初始化
onMounted(() => {
  // 设置默认入库时间为当前时间
  form.inboundTime = formatDateTime()

  loadWarehouses()
  loadMaterials()
})
</script>

<style lang="scss" scoped>
.inbound-create-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }

  :deep(.el-table) {
    .el-input-number {
      .el-input__inner {
        text-align: left;
      }
    }
  }
}
</style>
