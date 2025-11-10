<template>
  <div class="sidebar-container">
    <!-- Logo区域 -->
    <div class="sidebar-logo">
      <el-icon v-if="!isCollapse" :size="32" class="logo-icon">
        <Box />
      </el-icon>
      <el-icon v-else :size="28" class="logo-icon-small">
        <Box />
      </el-icon>
      <h1 v-if="!isCollapse" class="logo-title">仓储管理系统</h1>
    </div>

    <!-- 菜单导航 -->
    <el-scrollbar class="sidebar-menu-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        :collapse-transition="false"
        mode="vertical"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#1890ff"
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore, useUserStore } from '@/store'
import SidebarItem from './SidebarItem.vue'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

// 是否折叠
const isCollapse = computed(() => appStore.sidebar.opened)

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 可访问的路由列表（根据权限过滤）
const routes = computed(() => {
  // 这里应该从userStore获取根据权限过滤后的路由
  // 暂时返回所有动态路由
  return userStore.routes || []
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .sidebar-logo {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 60px;
    background-color: #002140;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);

    .logo-icon,
    .logo-icon-small {
      color: #1890ff;
    }

    .logo-title {
      margin-left: 12px;
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      white-space: nowrap;
    }
  }

  .sidebar-menu-wrapper {
    flex: 1;
    overflow-x: hidden;

    :deep(.el-menu) {
      border-right: none;
    }

    :deep(.el-sub-menu__title) {
      &:hover {
        background-color: rgba(255, 255, 255, 0.05) !important;
      }
    }

    :deep(.el-menu-item) {
      &:hover {
        background-color: rgba(255, 255, 255, 0.05) !important;
      }

      &.is-active {
        background-color: #1890ff !important;
      }
    }
  }
}
</style>
