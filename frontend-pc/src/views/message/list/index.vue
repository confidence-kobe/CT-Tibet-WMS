<template>
  <div class="message-list-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">消息中心</h2>
      <el-button type="primary" @click="handleMarkAllRead">
        <el-icon><Select /></el-icon>
        全部已读
      </el-button>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="消息类型">
          <el-select
            v-model="queryForm.type"
            placeholder="全部类型"
            clearable
            @clear="handleQuery"
            style="width: 160px"
          >
            <el-option
              v-for="(item, code) in messageTypeMap"
              :key="code"
              :label="item.text"
              :value="Number(code)"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryForm.isRead"
            placeholder="全部状态"
            clearable
            @clear="handleQuery"
            style="width: 140px"
          >
            <el-option label="未读" :value="0" />
            <el-option label="已读" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="消息总数" :value="statistics.total">
            <template #suffix>条</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card unread">
          <el-statistic title="未读消息" :value="statistics.unread">
            <template #suffix>条</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="已读消息" :value="statistics.read">
            <template #suffix>条</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 消息列表 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        @row-click="handleRowClick"
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />

        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-badge :is-dot="!row.isRead" type="danger">
              <el-icon :size="20" :color="row.isRead ? '#909399' : '#409eff'">
                <Message />
              </el-icon>
            </el-badge>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getMessageTypeTag(row.type)" size="small">
              {{ getMessageTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <span :class="{ 'unread-title': !row.isRead }">{{ row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />

        <el-table-column prop="createTime" label="时间" width="160" />

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.isRead"
              link
              type="primary"
              size="small"
              @click.stop="handleMarkRead(row)"
            >
              标记已读
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click.stop="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 16px; text-align: right;">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 消息详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="currentMessage?.title"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="消息类型">
          <el-tag :type="getMessageTypeTag(currentMessage?.type)" size="small">
            {{ getMessageTypeText(currentMessage?.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发送时间">
          {{ currentMessage?.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="消息内容">
          <div style="white-space: pre-wrap;">{{ currentMessage?.content }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="relatedLink(currentMessage)" type="primary" plain @click="goRelated(currentMessage)">
          {{ relatedLink(currentMessage).label }}
        </el-button>
        <el-button
          v-if="currentMessage && !currentMessage.isRead"
          type="primary"
          @click="handleMarkReadAndClose"
        >
          标记已读
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMessages, markAsRead, markAllAsRead, deleteMessage, notifyUnreadChanged } from '@/api/message'
import { useUserStore } from '@/store'

const router = useRouter()
const userStore = useUserStore()

// 查询表单
const queryForm = reactive({
  type: null,
  isRead: null
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 统计数据
const statistics = reactive({
  total: 0,
  unread: 0,
  read: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const currentMessage = ref(null)

// 消息类型映射（与后端 MessageType 枚举编码一致）
const messageTypeMap = {
  1: { text: '待审批', tag: 'warning' },
  2: { text: '审批提醒', tag: 'warning' },
  3: { text: '申请通过', tag: 'success' },
  4: { text: '申请未通过', tag: 'danger' },
  5: { text: '待领取', tag: 'primary' },
  6: { text: '领取提醒', tag: 'primary' },
  7: { text: '超时通知', tag: 'info' },
  8: { text: '取消通知', tag: 'info' },
  9: { text: '库存预警', tag: 'danger' },
  0: { text: '系统通知', tag: 'info' }
}

const getMessageTypeText = (type) => {
  return messageTypeMap[type]?.text || '通知'
}

const getMessageTypeTag = (type) => {
  return messageTypeMap[type]?.tag || 'info'
}

/** 关联业务类型：2-出库单 3-申请单 */
const RELATED_OUTBOUND = 2
const RELATED_APPLY = 3
const STOCK_ALERT = 9

// 消息对应的业务页面（员工看自己的申请，审批人看审批详情）
const isRole = (...codes) => codes.some(code => (userStore.roles || []).includes(code))

const relatedLink = (message) => {
  if (!message) return null
  if (message.type === STOCK_ALERT && isRole('admin', 'dept_admin', 'warehouse')) {
    return { label: '查看库存预警', path: '/inventory/warning' }
  }
  if (!message.relatedId) return null
  if (message.relatedType === RELATED_APPLY) {
    if (isRole('user')) {
      return { label: '查看申请', path: `/apply/detail/${message.relatedId}` }
    }
    if (isRole('warehouse', 'dept_admin')) {
      return { label: '查看申请', path: `/approval/detail/${message.relatedId}` }
    }
  }
  if (message.relatedType === RELATED_OUTBOUND && isRole('admin', 'dept_admin', 'warehouse')) {
    return { label: '查看出库单', path: `/outbound/detail/${message.relatedId}` }
  }
  return null
}

const goRelated = (message) => {
  const link = relatedLink(message)
  if (!link) return
  dialogVisible.value = false
  router.push(link.path)
}

// 查询数据
const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      type: queryForm.type || undefined,
      isRead: queryForm.isRead != null ? queryForm.isRead : undefined
    }

    const res = await listMessages(params)
    const data = res.data || {}
    tableData.value = data.list || []
    pagination.total = Number(data.total) || 0

    // 更新统计数据
    if (data.stats) {
      statistics.total = data.stats.total || 0
      statistics.unread = data.stats.unread || 0
      statistics.read = data.stats.read || 0
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryForm.type = null
  queryForm.isRead = null
  pagination.pageNum = 1
  handleQuery()
}

// 点击行查看详情
const handleRowClick = (row) => {
  currentMessage.value = row
  dialogVisible.value = true

  // 如果是未读消息，自动标记为已读
  if (!row.isRead) {
    markAsRead(row.id).then(() => {
      row.isRead = 1
      statistics.unread = Math.max(0, statistics.unread - 1)
      statistics.read += 1
      notifyUnreadChanged()
    }).catch(error => {
      console.error('标记已读失败:', error)
    })
  }
}

// 标记单条消息为已读
const handleMarkRead = async (row) => {
  try {
    await markAsRead(row.id)
    row.isRead = 1
    statistics.unread = Math.max(0, statistics.unread - 1)
    statistics.read += 1
    notifyUnreadChanged()
    ElMessage.success('已标记为已读')
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记失败')
  }
}

// 标记已读并关闭对话框
const handleMarkReadAndClose = async () => {
  if (currentMessage.value) {
    await handleMarkRead(currentMessage.value)
    dialogVisible.value = false
  }
}

// 全部标记为已读
const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要将所有未读消息标记为已读吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
    notifyUnreadChanged()
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 删除消息
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条消息吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteMessage(row.id)
    ElMessage.success('删除成功')
    notifyUnreadChanged()
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  handleQuery()
})
</script>

<style lang="scss" scoped>
.message-list-container {
  .stat-card {
    :deep(.el-statistic__content) {
      font-size: 24px;
      font-weight: 600;
    }

    &.unread {
      :deep(.el-statistic__content) {
        color: $primary-color;
      }
    }
  }

  .unread-title {
    font-weight: 600;
    color: $primary-color;
  }

  :deep(.el-table__row) {
    cursor: pointer;
  }
}
</style>
