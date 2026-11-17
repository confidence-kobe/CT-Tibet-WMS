<template>
  <div v-if="!item.meta || !item.meta.hidden">
    <!-- 没有标题、只有一个子菜单的分组（如首页）：直接显示子菜单，不显示空的分组 -->
    <sidebar-item
      v-if="onlyChild"
      :item="onlyChild"
      :base-path="resolvePath(item.path)"
    />

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

// 分组本身没有标题且只有一个子菜单时，直接显示该子菜单
const onlyChild = computed(() => {
  if (props.item.meta?.title) return null
  return showingChildren.value.length === 1 ? showingChildren.value[0] : null
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
