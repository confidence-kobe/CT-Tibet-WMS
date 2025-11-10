<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <el-aside
      :width="isCollapse ? '64px' : '200px'"
      class="layout-aside"
    >
      <Sidebar />
    </el-aside>

    <!-- 主容器 -->
    <el-container class="layout-container">
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <Header />
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="layout-main">
        <AppMain />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/store'
import Sidebar from './components/Sidebar/index.vue'
import Header from './components/Header/index.vue'
import AppMain from './components/AppMain/index.vue'

const appStore = useAppStore()

// 计算侧边栏是否折叠
const isCollapse = computed(() => appStore.sidebar.opened)
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  width: 100%;
  height: 100vh;

  .layout-aside {
    background-color: #001529;
    box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
    transition: width 0.28s;
  }

  .layout-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .layout-header {
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    padding: 0;
    height: 60px !important;
  }

  .layout-main {
    background-color: #f0f2f5;
    padding: 16px;
    overflow-y: auto;
  }
}
</style>
