<template>
  <div class="apply-create-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">新建物资申请</h2>
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
            <el-form-item label="申请部门">
              <el-input v-model="userInfo.deptName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="申请人">
              <el-input v-model="userInfo.userName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="目标仓库" prop="warehouseId">
              <el-select
                v-model="form.warehouseId"
                placeholder="请选择仓库"
                @change="handleWarehouseChange"
                style="width: 100%"
              >
                <el-option
                  v-for="warehouse in warehouseList"
                  :key="warehouse.id"
                  :label="warehouse.name"
                  :value="warehouse.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="申请原因" prop="applyReason">
              <el-input
                v-model="form.applyReason"
                type="textarea"
                :rows="3"
                placeholder="请输入申请原因"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 申请明细 -->
        <el-form-item label="申请明细" required>
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
              <el-table-column label="申请数量" width="110">
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
              <el-table-column label="用途说明" min-width="150">
                <template #default="{ row }">
                  <el-input
                    v-model="row.purpose"
                    placeholder="请输入用途"
                    maxlength="50"
                  />
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
              description="请添加申请物资"
              :image-size="100"
            />
          </div>
        </el-form-item>

        <!-- 库存检查提示 -->
        <el-alert
          v-if="hasStockWarning"
          title="库存不足提示"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 16px;"
        >
          <template #default>
            部分物资当前库存不足，审批通过后可能需要等待补货
          </template>
        </el-alert>

        <!-- 申请说明 -->
        <el-alert
          title="申请说明"
          type="info"
          :closable="false"
          style="margin-bottom: 16px;"
        >
          <template #default>
            <ul style="margin: 0; padding-left: 20px;">
              <li>提交申请后，将由仓管员审批</li>
              <li>审批通过后，请在7天内到仓库领取物资，逾期自动取消</li>
              <li>领取物资时需出示申请单号或员工证件</li>
            </ul>
          </template>
        </el-alert>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="saveLoading" @click="handleSubmit">
            <el-icon><Check /></el-icon>
            提交申请
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
import { createApply } from '@/api/apply'
import { listWarehouses } from '@/api/warehouse'
import { listMaterials } from '@/api/material'
import { listInventory } from '@/api/inventory'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

// 用户信息（从store获取）
const userInfo = reactive({
  deptName: userStore.deptName || '-',
  userName: userStore.realName || userStore.username || '-'
})

// 仓库列表
const warehouseList = ref([])

// 物资列表
const materials = ref([])

// 表单数据
const form = reactive({
  warehouseId: null,
  applyReason: '',
  details: []
})

// 表单引用
const formRef = ref(null)
const saveLoading = ref(false)

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择目标仓库', trigger: 'change' }
  ],
  applyReason: [
    { required: true, message: '请输入申请原因', trigger: 'blur' }
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
    amount: 0,
    purpose: ''
  })
}

// 移除物资行
const handleRemoveMaterial = (index) => {
  form.details.splice(index, 1)
}

// 加载仓库列表
const loadWarehouses = async () => {
  try {
    const res = await listWarehouses({ status: 0 })
    warehouseList.value = res.data || []
  } catch (error) {
    console.error('加载仓库列表失败:', error)
  }
}

// 加载物资列表
const loadMaterials = async () => {
  if (!form.warehouseId) return

  try {
    const res = await listMaterials({ pageNum: 1, pageSize: 1000, status: 0 })
    materials.value = res.data.list || []
  } catch (error) {
    console.error('加载物资列表失败:', error)
    ElMessage.error('加载物资列表失败')
  }
}

// 仓库变化
const handleWarehouseChange = () => {
  // 清空明细
  form.details = []
  // 重新加载物资
  loadMaterials()
}

// 物资选择变化
const handleMaterialChange = async (index) => {
  const detail = form.details[index]
  const material = materials.value.find(m => m.id === detail.materialId)

  if (material) {
    detail.materialCode = material.code
    detail.materialName = material.name
    detail.spec = material.spec
    detail.unit = material.unit
    detail.price = material.price

    // 查询库存
    try {
      const res = await listInventory({
        warehouseId: form.warehouseId,
        materialId: material.id,
        pageNum: 1,
        pageSize: 1
      })
      const inventory = res.data.list?.[0]
      detail.stock = inventory?.quantity || 0
    } catch (error) {
      console.error('查询库存失败:', error)
      detail.stock = 0
    }

    calculateAmount(index)
  }
}

// 数量变化
const handleQuantityChange = (index) => {
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
      ElMessage.warning('请至少添加一条申请明细')
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
    }

    // 确认提交
    await ElMessageBox.confirm(
      `确认提交申请？申请金额：¥${totalAmount.value.toFixed(2)}<br/>提交后将发送给仓管员审批`,
      '提交确认',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '取消',
        type: 'info',
        dangerouslyUseHTMLString: true
      }
    )

    saveLoading.value = true

    // 构建提交数据（后端只需要 materialId 和 quantity）
    const dto = {
      warehouseId: form.warehouseId,
      applyReason: form.applyReason,
      details: form.details.map(item => ({
        materialId: item.materialId,
        quantity: item.quantity
      }))
    }

    await createApply(dto)
    ElMessage.success('申请提交成功，请等待仓管员审批')
    router.push('/apply/list')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
    }
  } finally {
    saveLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadWarehouses()
})

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
      router.push('/apply/list')
    }).catch(() => {
      // 用户选择继续填写
    })
  } else {
    router.push('/apply/list')
  }
}
</script>

<style lang="scss" scoped>
.apply-create-container {
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
