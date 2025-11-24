<template>
  <div class="dept-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">部门管理</h2>
      <el-button type="primary" @click="handleAdd(null)">
        <el-icon><Plus /></el-icon>
        新增根部门
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        style="width: 100%"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="200" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAdd(row)">
              新增子部门
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
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTreeOptions"
            :props="{ label: 'deptName', value: 'id' }"
            placeholder="请选择上级部门（留空则为根部门）"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="0"
            :max="999"
            controls-position="right"
            style="width: 100%"
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
import { listDepts, listAllDepts, createDept, updateDept, deleteDept } from '@/api/dept'

const tableData = ref([])
const allDepts = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const saveLoading = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  parentId: null,
  deptName: '',
  leader: '',
  phone: '',
  sort: 0,
  status: 0
})

const formRules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ],
  leader: [
    { required: true, message: '请输入负责人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑部门' : '新增部门')

const deptTreeOptions = computed(() => {
  const filterTree = (data, excludeId) => {
    return data.filter(item => item.id !== excludeId).map(item => ({
      ...item,
      children: item.children ? filterTree(item.children, excludeId) : undefined
    }))
  }
  return form.id ? filterTree(allDepts.value, form.id) : allDepts.value
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await listDepts()
    tableData.value = res.data
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    loading.value = false
  }
}

const loadAllDepts = async () => {
  try {
    const res = await listAllDepts()
    allDepts.value = res.data
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

const handleAdd = async (row) => {
  isEdit.value = false
  resetForm()
  if (row) {
    form.parentId = row.id
  }
  await loadAllDepts()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || null,
    deptName: row.deptName,
    leader: row.leader,
    phone: row.phone,
    sort: row.sort,
    status: row.status
  })
  await loadAllDepts()
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该部门下存在子部门，无法删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除部门"${row.deptName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteDept(row.id)
    ElMessage.success('删除成功')
    await loadData()
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
      await updateDept(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createDept(form)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    await loadData()
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
  form.parentId = null
  form.deptName = ''
  form.leader = ''
  form.phone = ''
  form.sort = 0
  form.status = 0
}

loadData()
</script>

<style lang="scss" scoped>
.dept-container {
  // 自定义样式
}
</style>
