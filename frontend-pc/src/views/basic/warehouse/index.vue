<template>
  <div class="warehouse-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">仓库管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建仓库
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="仓库名称">
          <el-input
            v-model="queryForm.warehouseName"
            placeholder="请输入仓库名称"
            clearable
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input
            v-model="queryForm.manager"
            placeholder="请输入负责人姓名"
            clearable
            @clear="handleQuery"
          />
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
        <el-table-column prop="warehouseCode" label="仓库编码" width="120" />
        <el-table-column prop="warehouseName" label="仓库名称" min-width="150" />
        <el-table-column prop="deptName" label="所属部门" min-width="150" />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
            <el-button
              link
              :type="row.status === 0 ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 0 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 16px; text-align: right;">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
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
        <el-form-item label="仓库编码" prop="warehouseCode">
          <el-input v-model="form.warehouseCode" placeholder="请输入仓库编码" />
        </el-form-item>
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%">
            <el-option label="网络运维部" :value="1" />
            <el-option label="维护部" :value="2" />
            <el-option label="运维中心" :value="3" />
            <el-option label="技术部" :value="4" />
            <el-option label="客服部" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="form.manager" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="2"
            placeholder="请输入仓库地址"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">启用</el-radio>
            <el-radio :label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSave">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWarehouses, createWarehouse, updateWarehouse, deleteWarehouse, updateWarehouseStatus } from '@/api/warehouse'

const queryForm = reactive({
  warehouseName: '',
  manager: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const saveLoading = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  warehouseCode: '',
  warehouseName: '',
  deptId: null,
  deptName: '',
  manager: '',
  phone: '',
  address: '',
  status: 0
})

const formRules = {
  warehouseCode: [
    { required: true, message: '请输入仓库编码', trigger: 'blur' }
  ],
  warehouseName: [
    { required: true, message: '请输入仓库名称', trigger: 'blur' }
  ],
  deptId: [
    { required: true, message: '请选择所属部门', trigger: 'change' }
  ],
  manager: [
    { required: true, message: '请输入负责人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑仓库' : '新建仓库')

const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    const res = await listWarehouses(params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.warehouseName = ''
  queryForm.manager = ''
  queryForm.status = null
  pagination.pageNum = 1
  handleQuery()
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除仓库"${row.warehouseName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteWarehouse(row.id)
    ElMessage.success('删除成功')
    await handleQuery()
  } catch (error) {
    // 用户取消或API错误已在request.js中处理
  }
}

const handleToggleStatus = async (row) => {
  const action = row.status === 0 ? '停用' : '启用'
  const newStatus = row.status === 0 ? 1 : 0
  try {
    await ElMessageBox.confirm(
      `确定要${action}仓库"${row.warehouseName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateWarehouseStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    await handleQuery()
  } catch (error) {
    // 用户取消或API错误已在request.js中处理
  }
}

const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saveLoading.value = true

    if (isEdit.value) {
      await updateWarehouse(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createWarehouse(form)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    await handleQuery()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saveLoading.value = false
  }
}

const handleCloseDialog = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  resetForm()
}

const resetForm = () => {
  form.id = null
  form.warehouseCode = ''
  form.warehouseName = ''
  form.deptId = null
  form.deptName = ''
  form.manager = ''
  form.phone = ''
  form.address = ''
  form.status = 0
}

handleQuery()
</script>

<style lang="scss" scoped>
.warehouse-container {
  // 自定义样式
}
</style>
