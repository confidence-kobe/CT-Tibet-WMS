<template>
  <div class="material-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">物资管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建物资
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="物资名称">
          <el-input
            v-model="queryForm.materialName"
            placeholder="请输入物资名称"
            clearable
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="物资编码">
          <el-input
            v-model="queryForm.materialCode"
            placeholder="请输入物资编码"
            clearable
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="物资类别">
          <el-select
            v-model="queryForm.category"
            placeholder="请选择"
            clearable
            @clear="handleQuery"
          >
            <el-option label="光缆类" value="光缆类" />
            <el-option label="设备类" value="设备类" />
            <el-option label="配件类" value="配件类" />
            <el-option label="工具类" value="工具类" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            placeholder="请选择"
            clearable
            @clear="handleQuery"
          >
            <el-option label="启用" :value="0" />
            <el-option label="停用" :value="1" />
          </el-select>
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
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
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
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="spec" label="规格型号" width="120" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="price" label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100" align="right" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              详情
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleCloseDialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="物资名称" prop="materialName">
          <el-input v-model="form.materialName" placeholder="请输入物资名称" />
        </el-form-item>
        <el-form-item label="物资编码" prop="materialCode">
          <el-input v-model="form.materialCode" placeholder="请输入物资编码" />
        </el-form-item>
        <el-form-item label="物资类别" prop="category">
          <el-select v-model="form.category" placeholder="请选择物资类别" style="width: 100%">
            <el-option label="光缆类" value="光缆类" />
            <el-option label="设备类" value="设备类" />
            <el-option label="配件类" value="配件类" />
            <el-option label="工具类" value="工具类" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号" prop="spec">
          <el-input v-model="form.spec" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-select v-model="form.unit" placeholder="请选择单位" style="width: 100%">
            <el-option label="条" value="条" />
            <el-option label="台" value="台" />
            <el-option label="米" value="米" />
            <el-option label="个" value="个" />
            <el-option label="箱" value="箱" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最低库存" prop="minStock">
          <el-input-number
            v-model="form.minStock"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入物资描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="物资详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="物资编码">
          {{ currentRow.materialCode }}
        </el-descriptions-item>
        <el-descriptions-item label="物资名称">
          {{ currentRow.materialName }}
        </el-descriptions-item>
        <el-descriptions-item label="物资类别">
          {{ currentRow.category }}
        </el-descriptions-item>
        <el-descriptions-item label="规格型号">
          {{ currentRow.spec || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="单位">
          {{ currentRow.unit }}
        </el-descriptions-item>
        <el-descriptions-item label="单价">
          ¥{{ currentRow.price?.toFixed(2) || '0.00' }}
        </el-descriptions-item>
        <el-descriptions-item label="最低库存">
          {{ currentRow.minStock }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === 0 ? 'success' : 'danger'">
            {{ currentRow.status === 0 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ currentRow.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentRow.description || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 查询表单
const queryForm = reactive({
  materialName: '',
  materialCode: '',
  category: '',
  status: null
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
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref(null)
const saveLoading = ref(false)
const isEdit = ref(false)

// 当前行数据
const currentRow = ref({})

// 表单数据
const form = reactive({
  id: null,
  materialName: '',
  materialCode: '',
  category: '',
  spec: '',
  unit: '',
  price: 0,
  minStock: 0,
  status: 0,
  description: ''
})

// 表单验证规则
const formRules = {
  materialName: [
    { required: true, message: '请输入物资名称', trigger: 'blur' }
  ],
  materialCode: [
    { required: true, message: '请输入物资编码', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择物资类别', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请选择单位', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入单价', trigger: 'blur' }
  ],
  minStock: [
    { required: true, message: '请输入最低库存', trigger: 'blur' }
  ]
}

// 对话框标题
const dialogTitle = computed(() => isEdit.value ? '编辑物资' : '新建物资')

// 查询数据
const handleQuery = () => {
  loading.value = true

  // TODO: 调用API获取数据
  // 模拟数据
  setTimeout(() => {
    tableData.value = [
      {
        id: 1,
        materialCode: 'GX001',
        materialName: '光缆12芯',
        category: '光缆类',
        spec: '12芯单模',
        unit: '条',
        price: 1500.00,
        minStock: 100,
        status: 0,
        createTime: '2025-11-01 10:00:00',
        description: '12芯单模光缆，用于长距离传输'
      },
      {
        id: 2,
        materialCode: 'JHJ001',
        materialName: '交换机H3C',
        category: '设备类',
        spec: 'S5130-28S',
        unit: '台',
        price: 7800.00,
        minStock: 10,
        status: 0,
        createTime: '2025-11-02 14:30:00',
        description: 'H3C 24口千兆交换机'
      }
    ]
    pagination.total = 2
    loading.value = false
  }, 500)
}

// 重置查询
const handleReset = () => {
  Object.keys(queryForm).forEach(key => {
    queryForm[key] = ''
  })
  queryForm.status = null
  pagination.page = 1
  handleQuery()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 查看详情
const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除物资"${row.materialName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用删除API
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) {
    // 用户取消
  }
}

// 保存
const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saveLoading.value = true

    // TODO: 调用保存API
    await new Promise(resolve => setTimeout(resolve, 500))

    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    handleQuery()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saveLoading.value = false
  }
}

// 关闭对话框
const handleCloseDialog = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.materialName = ''
  form.materialCode = ''
  form.category = ''
  form.spec = ''
  form.unit = ''
  form.price = 0
  form.minStock = 0
  form.status = 0
  form.description = ''
}

// 初始化加载数据
handleQuery()
</script>

<style lang="scss" scoped>
.material-container {
  // 自定义样式
}
</style>
