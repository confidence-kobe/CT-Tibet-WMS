<template>
  <div class="user-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建用户
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="请输入用户名或姓名"
            clearable
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select
            v-model="queryForm.deptId"
            placeholder="请选择部门"
            clearable
            @clear="handleQuery"
          >
            <el-option label="网络运维部" :value="1" />
            <el-option label="维护部" :value="2" />
            <el-option label="运维中心" :value="3" />
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
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="deptName" label="部门" min-width="150" />
        <el-table-column prop="roles" label="角色" min-width="180">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles"
              :key="role"
              size="small"
              style="margin-right: 4px"
            >
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="warning" size="small" @click="handleResetPassword(row)">
              重置密码
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（6-20位）" show-password />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%">
            <el-option label="网络运维部" :value="1" />
            <el-option label="维护部" :value="2" />
            <el-option label="运维中心" :value="3" />
            <el-option label="技术部" :value="4" />
            <el-option label="客服部" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" placeholder="请选择角色" multiple style="width: 100%">
            <el-option label="系统管理员" :value="1" />
            <el-option label="部门管理员" :value="2" />
            <el-option label="仓库管理员" :value="3" />
            <el-option label="普通员工" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
import { listUsers, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '@/api/user'

const queryForm = reactive({
  keyword: '',
  deptId: null,
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
  username: '',
  realName: '',
  password: '',
  deptId: null,
  roleIds: [],
  phone: '',
  email: '',
  status: 0
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  deptId: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  roleIds: [
    { required: true, message: '请选择角色', trigger: 'change', type: 'array', min: 1 }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新建用户')

const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    const res = await listUsers(params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.deptId = null
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
  const roleMap = { '系统管理员': 1, '部门管理员': 2, '仓库管理员': 3, '普通员工': 4 }
  Object.assign(form, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    deptId: row.deptId || 1,
    roleIds: row.roles.map(r => roleMap[r]),
    phone: row.phone,
    email: row.email,
    status: row.status
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.realName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteUser(row.id)
    ElMessage.success('删除成功')
    await handleQuery()
  } catch (error) {
    // 用户取消或API错误已在request.js中处理
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户"${row.realName}"的密码吗？重置后的密码为：123456`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await resetUserPassword(row.id)
    ElMessage.success('密码重置成功，新密码为：123456')
  } catch (error) {
    // 用户取消或API错误已在request.js中处理
  }
}

const handleToggleStatus = async (row) => {
  const action = row.status === 0 ? '停用' : '启用'
  const newStatus = row.status === 0 ? 1 : 0
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户"${row.realName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateUserStatus(row.id, newStatus)
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
      await updateUser(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createUser(form)
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
  form.username = ''
  form.realName = ''
  form.password = ''
  form.deptId = null
  form.roleIds = []
  form.phone = ''
  form.email = ''
  form.status = 0
}

handleQuery()
</script>

<style lang="scss" scoped>
.user-container {
  // 自定义样式
}
</style>
