<template>
  <div class="table-card-container">
    <!-- 搜索表单区域 -->
    <el-card v-if="$slots.search" shadow="never" class="search-card">
      <slot name="search" />
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="hover" class="table-content-card">
      <!-- 表头工具栏 -->
      <template v-if="title || $slots.header || $slots.actions" #header>
        <div class="card-header">
          <span v-if="title" class="header-title">{{ title }}</span>
          <slot name="header" />
          <div class="header-actions">
            <slot name="actions" />
          </div>
        </div>
      </template>

      <!-- 表格内容 -->
      <slot />

      <!-- 分页 -->
      <el-pagination
        v-if="pagination && showPagination"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="pageSizes"
        :layout="paginationLayout"
        :background="paginationBackground"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 卡片标题
  title: {
    type: String,
    default: ''
  },
  // 是否显示分页
  showPagination: {
    type: Boolean,
    default: true
  },
  // 分页配置
  pagination: {
    type: Object,
    default: () => ({
      current: 1,
      size: 10,
      total: 0
    })
  },
  // 分页大小选项
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 分页布局
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  // 分页是否有背景色
  paginationBackground: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:pagination', 'page-change', 'size-change'])

// 当前页码
const currentPage = computed({
  get: () => props.pagination?.current || 1,
  set: (value) => {
    emit('update:pagination', { ...props.pagination, current: value })
  }
})

// 每页大小
const pageSize = computed({
  get: () => props.pagination?.size || 10,
  set: (value) => {
    emit('update:pagination', { ...props.pagination, size: value })
  }
})

// 总条数
const total = computed(() => props.pagination?.total || 0)

// 页码变化
const handleCurrentChange = (page) => {
  emit('page-change', page)
}

// 每页大小变化
const handleSizeChange = (size) => {
  emit('size-change', size)
}
</script>

<style lang="scss" scoped>
.table-card-container {
  .search-card {
    margin-bottom: 16px;

    :deep(.el-form) {
      .el-form-item:last-child {
        margin-right: 0;
      }
    }
  }

  .table-content-card {
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 16px;
      font-weight: 600;
      color: $text-color-primary;

      .header-title {
        flex: 1;
      }

      .header-actions {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    :deep(.el-pagination) {
      margin-top: 16px;
      justify-content: flex-end;
    }
  }
}
</style>
