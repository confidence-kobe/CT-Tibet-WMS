<template>
  <div class="header-container">
    <!-- 左侧：折叠按钮 + 面包屑 -->
    <div class="header-left">
      <el-icon
        class="collapse-icon"
        :size="20"
        @click="toggleSidebar"
      >
        <Fold v-if="!appStore.sidebar.opened" />
        <Expand v-else />
      </el-icon>

      <el-breadcrumb separator="/" class="breadcrumb">
        <transition-group name="breadcrumb">
          <el-breadcrumb-item
            v-for="item in breadcrumbList"
            :key="item.path"
          >
            {{ item.meta.title }}
          </el-breadcrumb-item>
        </transition-group>
      </el-breadcrumb>
    </div>

    <!-- 右侧：消息、用户菜单 -->
    <div class="header-right">
      <!-- 消息通知 -->
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
        <el-icon :size="20" class="header-icon" @click="handleMessage">
          <Bell />
        </el-icon>
      </el-badge>

      <!-- 用户信息下拉菜单 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar
            :size="32"
            :src="userStore.avatar"
            class="user-avatar"
          >
            {{ userStore.realName?.charAt(0) || 'U' }}
          </el-avatar>
          <span class="user-name">{{ userStore.realName }}</span>
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppStore, useUserStore } from '@/store'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

// 未读消息数量
const unreadCount = ref(0)

// 面包屑列表
const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  // 如果不是首页，添加首页到面包屑
  if (matched.length > 0 && matched[0].path !== '/') {
    matched.unshift({ path: '/dashboard', meta: { title: '首页' } })
  }
  return matched
})

// 切换侧边栏
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

// 处理消息点击
const handleMessage = () => {
  router.push('/message/list')
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile/index')
      break
    case 'password':
      router.push('/profile/password')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        await userStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录')
      } catch (error) {
        // 用户取消
      }
      break
  }
}
</script>

<style lang="scss" scoped>
.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .collapse-icon {
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: $primary-color;
      }
    }

    .breadcrumb {
      font-size: 14px;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .message-badge {
      cursor: pointer;
    }

    .header-icon {
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: $primary-color;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background-color 0.3s;

      &:hover {
        background-color: #f5f5f5;
      }

      .user-avatar {
        background-color: $primary-color;
      }

      .user-name {
        font-size: 14px;
        color: #262626;
      }
    }
  }
}

// 面包屑过渡动画
.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.3s;
}

.breadcrumb-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.breadcrumb-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
