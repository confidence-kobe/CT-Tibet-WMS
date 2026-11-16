<template>
  <!-- 注意：不要用 div 等元素包裹菜单项，Element Plus 折叠态样式依赖 el-menu 的直接子代选择器 -->
  <template v-if="visible">
    <!-- 父路由自身无标题（如根路由 '/'）时直接渲染其子路由，避免出现无文字的空白菜单项 -->
    <template v-if="isTitleless">
      <sidebar-item
        v-for="child in showingChildren"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
      />
    </template>

    <!-- 有子菜单 -->
    <el-sub-menu
      v-else-if="showingChildren.length > 0"
      :index="resolvePath(item.path)"
      teleported
    >
      <template #title>
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>

      <sidebar-item
        v-for="child in showingChildren"
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
      <el-icon v-if="item.meta?.icon">
        <component :is="item.meta.icon" />
      </el-icon>
      <template #title>
        <span>{{ item.meta?.title }}</span>
      </template>
    </el-menu-item>
  </template>
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

// 是否显示本菜单项
const visible = computed(() => !props.item.meta?.hidden)

// 可显示的子路由
const showingChildren = computed(() =>
  (props.item.children || []).filter(child => !child.meta?.hidden)
)

// 自身无标题且有子路由：作为纯分组容器，只渲染子项
const isTitleless = computed(() =>
  !props.item.meta?.title && showingChildren.value.length > 0
)

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
