<template>
  <div class="profile-container page-container">
    <div class="page-header">
      <h2 class="page-title">个人信息</h2>
    </div>

    <el-card shadow="never">
      <el-row :gutter="24">
        <!-- 左侧头像 -->
        <el-col :xs="24" :sm="8" :md="6">
          <div class="avatar-section">
            <el-avatar :size="120" :src="userStore.avatar" class="user-avatar">
              {{ userStore.realName?.charAt(0) || 'U' }}
            </el-avatar>
            <el-button type="primary" size="small" class="upload-btn" @click="handleUploadAvatar">
              更换头像
            </el-button>
          </div>
        </el-col>

        <!-- 右侧信息 -->
        <el-col :xs="24" :sm="16" :md="18">
          <div class="info-section">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户名">
                {{ userStore.username }}
              </el-descriptions-item>
              <el-descriptions-item label="真实姓名">
                {{ userStore.realName }}
              </el-descriptions-item>
              <el-descriptions-item label="手机号">
                {{ userStore.phone }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ userStore.email || '未设置' }}
              </el-descriptions-item>
              <el-descriptions-item label="所属部门">
                {{ userStore.deptName }}
              </el-descriptions-item>
              <el-descriptions-item label="角色">
                <el-tag :type="getRoleTagType(userStore.roleCode)">
                  {{ getRoleName(userStore.roleCode) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="最后登录时间">
                {{ formatTime(userStore.lastLoginTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="最后登录IP">
                {{ userStore.lastLoginIp || '-' }}
              </el-descriptions-item>
            </el-descriptions>

            <div class="action-buttons">
              <el-button type="primary" @click="handleEditProfile">
                编辑资料
              </el-button>
              <el-button @click="handleChangePassword">
                修改密码
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 编辑资料对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑资料"
      width="500px"
      @close="handleCloseEdit"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleSaveProfile">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 编辑对话框
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const saveLoading = ref(false)

// 编辑表单
const editForm = reactive({
  realName: '',
  phone: '',
  email: ''
})

// 表单验证规则
const editRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 获取角色名称
const getRoleName = (roleCode) => {
  const roleMap = {
    admin: '系统管理员',
    dept_admin: '部门管理员',
    warehouse: '仓库管理员',
    user: '普通员工'
  }
  return roleMap[roleCode] || '未知角色'
}

// 获取角色标签类型
const getRoleTagType = (roleCode) => {
  const typeMap = {
    admin: 'danger',
    dept_admin: 'warning',
    warehouse: 'success',
    user: 'info'
  }
  return typeMap[roleCode] || 'info'
}

// 上传头像
const handleUploadAvatar = () => {
  ElMessage.info('上传头像功能开发中')
}

// 编辑资料
const handleEditProfile = () => {
  editForm.realName = userStore.realName
  editForm.phone = userStore.phone
  editForm.email = userStore.email
  editDialogVisible.value = true
}

// 关闭编辑对话框
const handleCloseEdit = () => {
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

// 保存资料
const handleSaveProfile = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()
    saveLoading.value = true

    // TODO: 调用API保存用户信息
    // await userStore.updateProfile(editForm)

    ElMessage.success('保存成功')
    editDialogVisible.value = false
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saveLoading.value = false
  }
}

// 修改密码
const handleChangePassword = () => {
  router.push('/profile/password')
}
</script>

<style lang="scss" scoped>
.profile-container {
  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    padding: 24px 0;

    .user-avatar {
      background-color: $primary-color;
      font-size: 48px;
      font-weight: 600;
    }

    .upload-btn {
      width: 120px;
    }
  }

  .info-section {
    .action-buttons {
      margin-top: 24px;
      display: flex;
      gap: 12px;
    }
  }
}
</style>
