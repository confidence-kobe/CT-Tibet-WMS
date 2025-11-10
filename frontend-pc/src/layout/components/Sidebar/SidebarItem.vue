<template>
  <div v-if="!item.meta || !item.meta.hidden">
    <!-- 有子菜单 -->
    <el-sub-menu
      v-if="hasChildren"
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
        :base-path="resolvePath(child.path)"
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

// 是否有可显示的子菜单
const hasChildren = computed(() => {
  if (!props.item.children) return false

  // 过滤掉隐藏的子菜单
  const showingChildren = props.item.children.filter(child => {
    return !child.meta?.hidden
  })

  return showingChildren.length > 0
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
