<template>
  <div class="outbound-create-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">直接出库</h2>
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
              >
                <el-option label="拉萨总仓" :value="1" />
                <el-option label="日喀则分仓" :value="2" />
                <el-option label="那曲分仓" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="领取人" prop="receiver">
              <el-input
                v-model="form.receiver"
                placeholder="请输入领取人姓名"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="备注">
              <el-input
                v-model="form.remark"
                placeholder="请输入备注信息"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 出库明细 -->
        <el-form-item label="出库明细" required>
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
              <el-table-column label="物资" width="200">
                <template #default="{ row, $index }">
                  <el-select
                    v-model="row.materialId"
                    placeholder="请选择物资"
                    @change="handleMaterialChange($index)"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="material in materials"
                      :key="material.id"
                      :label="material.name"
                      :value="material.id"
                    />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="spec" label="规格型号" min-width="120">
                <template #default="{ row }">
                  <el-input v-model="row.spec" readonly />
                </template>
              </el-table-column>
              <el-table-column prop="unit" label="单位" width="70">
                <template #default="{ row }">
                  <el-input v-model="row.unit" readonly />
                </template>
              </el-table-column>
              <el-table-column label="库存" width="90" align="right">
                <template #default="{ row }">
                  <el-tag :type="row.stock > 0 ? 'success' : 'danger'" size="small">
                    {{ row.stock || 0 }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="110">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.quantity"
                    :min="1"
                    :max="row.stock"
                    :precision="2"
                    @change="handleQuantityChange($index)"
                    style="width: 100%"
                    controls-position="right"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="price" label="单价(元)" width="90" align="right">
                <template #default="{ row }">
                  ¥{{ row.price?.toFixed(2) || '0.00' }}
                </template>
              </el-table-column>
              <el-table-column label="金额(元)" width="110" align="right">
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
              description="请添加出库物资"
              :image-size="100"
            />
          </div>
        </el-form-item>

        <!-- 库存检查提示 -->
        <el-alert
          v-if="hasStockWarning"
          title="库存不足警告"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 16px;"
        >
          <template #default>
            部分物资库存不足，请调整出库数量或联系采购补货
          </template>
        </el-alert>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="saveLoading" @click="handleSubmit">
            <el-icon><Check /></el-icon>
            提交出库
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
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 物资列表（带库存信息）
const materials = ref([
  { id: 1, code: 'GX001', name: '光缆12芯', spec: '12芯单模', unit: '条', price: 1500.00, stock: 95 },
  { id: 2, code: 'GX002', name: '光缆24芯', spec: '24芯多模', unit: '条', price: 2100.00, stock: 150 },
  { id: 3, code: 'JHJ001', name: '交换机H3C', spec: 'S5130-28S', unit: '台', price: 7800.00, stock: 25 },
  { id: 4, code: 'PJ001', name: '光纤连接器', spec: 'SC-UPC', unit: '个', price: 20.00, stock: 500 },
  { id: 5, code: 'PJ002', name: '网线', spec: '超五类', unit: '米', price: 3.50, stock: 0 } // 缺货
])

// 表单数据
const form = reactive({
  warehouseId: null,
  receiver: '',
  remark: '',
  details: []
})

// 表单引用
const formRef = ref(null)
const saveLoading = ref(false)

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  receiver: [
    { required: true, message: '请输入领取人姓名', trigger: 'blur' }
  ]
}

// 计算总金额
const totalAmount = computed(() => {
  return form.details.reduce((sum, item) => sum + item.amount, 0)
})

// 检查是否有库存警告
const hasStockWarning = computed(() => {
  return form.details.some(item => item.quantity > item.stock)
})

// 添加物资行
const handleAddMaterial = () => {
  form.details.push({
    materialId: null,
    materialCode: '',
    materialName: '',
    spec: '',
    unit: '',
    stock: 0,
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
    detail.spec = material.spec
    detail.unit = material.unit
    detail.stock = material.stock
    detail.price = material.price

    // 如果库存不足，将数量设为库存数
    if (detail.quantity > material.stock) {
      detail.quantity = material.stock
    }

    calculateAmount(index)
  }
}

// 数量变化
const handleQuantityChange = (index) => {
  const detail = form.details[index]

  // 检查库存
  if (detail.quantity > detail.stock) {
    ElMessage.warning(`库存不足！当前库存：${detail.stock}`)
    detail.quantity = detail.stock
  }

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
      ElMessage.warning('请至少添加一条出库明细')
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
      if (detail.quantity > detail.stock) {
        ElMessage.error(`第 ${i + 1} 行：出库数量超过库存！当前库存：${detail.stock}`)
        return
      }
    }

    // 确认提交
    await ElMessageBox.confirm(
      `确认提交出库单？出库金额：¥${totalAmount.value.toFixed(2)}<br/>库存将立即扣减。`,
      '提交确认',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )

    saveLoading.value = true

    // TODO: 调用API提交数据
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('出库单提交成功，库存已扣减')
    router.push('/outbound/list')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
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
      router.push('/outbound/list')
    }).catch(() => {
      // 用户选择继续填写
    })
  } else {
    router.push('/outbound/list')
  }
}
</script>

<style lang="scss" scoped>
.outbound-create-container {
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
