<template>
  <div v-if="!item.meta || !item.meta.hidden">
    <!-- 无标题且只有一个可见子菜单的父路由（如首页）：直接显示子菜单 -->
    <el-menu-item
      v-if="onlyChild"
      :index="resolvePath(onlyChild.path)"
      @click="handleMenuClick(resolvePath(onlyChild.path))"
    >
      <el-icon v-if="onlyChild.meta && onlyChild.meta.icon">
        <component :is="onlyChild.meta.icon" />
      </el-icon>
      <template #title>
        <span>{{ onlyChild.meta?.title }}</span>
      </template>
    </el-menu-item>

    <!-- 有子菜单 -->
    <el-sub-menu
      v-else-if="hasChildren"
      :index="resolvePath(item.path)"
      :popper-append-to-body="true"
    >
      <template #title>
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>

      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
      />
    </el-sub-menu>

    <!-- 无子菜单 -->
    <el-menu-item
      v-else
      :index="resolvePath(item.path)"
      @click="handleMenuClick(resolvePath(item.path))"
    >
      <el-icon v-if="item.meta && item.meta.icon">
        <component :is="item.meta.icon" />
      </el-icon>
      <template #title>
        <span>{{ item.meta?.title }}</span>
      </template>
    </el-menu-item>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import path from 'path-browserify'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

const router = useRouter()

// 可显示的子菜单（过滤掉隐藏的）
const showingChildren = computed(() => {
  return (props.item.children || []).filter(child => !child.meta?.hidden)
})

// 是否有可显示的子菜单
const hasChildren = computed(() => showingChildren.value.length > 0)

// 父路由没有标题且只有一个可见子菜单时，直接展示该子菜单
const onlyChild = computed(() => {
  if (props.item.meta?.title || showingChildren.value.length !== 1) return null
  return showingChildren.value[0]
})

// 解析完整路径
const resolvePath = (routePath) => {
  if (routePath.startsWith('/')) {
    return routePath
  }
  return path.resolve(props.basePath, routePath)
}

// 处理菜单点击
const handleMenuClick = (path) => {
  router.push(path)
}
</script>
