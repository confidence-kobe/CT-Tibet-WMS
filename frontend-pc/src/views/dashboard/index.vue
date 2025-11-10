<template>
  <div class="dashboard-container">
    <!-- 欢迎信息 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <div class="welcome-info">
          <h2>欢迎回来，{{ userStore.realName }}！</h2>
          <p class="welcome-text">
            <el-icon><Location /></el-icon>
            {{ userStore.deptName }} · {{ getRoleName(userStore.roleCode) }}
          </p>
          <p class="welcome-time">
            {{ currentTime }}
          </p>
        </div>
        <div class="welcome-avatar">
          <el-avatar :size="80" :src="userStore.avatar">
            {{ userStore.realName?.charAt(0) || 'U' }}
          </el-avatar>
        </div>
      </div>
    </el-card>

    <!-- 数据统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" class="stat-icon primary">
              <Download />
            </el-icon>
            <div class="stat-info">
              <div class="stat-label">今日入库</div>
              <div class="stat-value">{{ stats.todayInbound }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" class="stat-icon success">
              <Upload />
            </el-icon>
            <div class="stat-info">
              <div class="stat-label">今日出库</div>
              <div class="stat-value">{{ stats.todayOutbound }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" class="stat-icon warning">
              <Clock />
            </el-icon>
            <div class="stat-info">
              <div class="stat-label">待审批</div>
              <div class="stat-value">{{ stats.pendingApproval }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" class="stat-icon error">
              <WarningFilled />
            </el-icon>
            <div class="stat-info">
              <div class="stat-label">库存预警</div>
              <div class="stat-value">{{ stats.stockWarning }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card shadow="hover" class="quick-actions">
      <template #header>
        <div class="card-header">
          <span>快捷操作</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="action in quickActions" :key="action.name">
          <div class="action-item" @click="handleQuickAction(action.path)">
            <el-icon :size="32" class="action-icon">
              <component :is="action.icon" />
            </el-icon>
            <div class="action-name">{{ action.name }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据图表 -->
    <el-row :gutter="16" class="charts-row">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>出入库趋势</span>
            </div>
          </template>
          <div class="chart-placeholder">
            图表加载中...
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>库存状态分布</span>
            </div>
          </template>
          <div class="chart-placeholder">
            图表加载中...
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

// 当前时间
const currentTime = ref(dayjs().format('YYYY年MM月DD日 HH:mm:ss'))

// 统计数据
const stats = ref({
  todayInbound: 5,
  todayOutbound: 12,
  pendingApproval: 3,
  stockWarning: 2
})

// 快捷操作列表（根据角色显示不同的操作）
const quickActions = computed(() => {
  const baseActions = [
    { name: '库存查询', icon: 'Search', path: '/inventory/query' },
    { name: '库存预警', icon: 'Warning', path: '/inventory/warning' }
  ]

  if (['admin', 'dept_admin', 'warehouse'].includes(userStore.roleCode)) {
    return [
      { name: '新建入库', icon: 'Download', path: '/inbound/create' },
      { name: '新建出库', icon: 'Upload', path: '/outbound/create' },
      { name: '审批管理', icon: 'CircleCheck', path: '/approval/pending' },
      ...baseActions
    ]
  } else {
    return [
      { name: '新建申请', icon: 'Document', path: '/apply/create' },
      { name: '我的申请', icon: 'List', path: '/apply/list' },
      ...baseActions
    ]
  }
})

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

// 处理快捷操作
const handleQuickAction = (path) => {
  router.push(path)
}

// 更新时间
const updateTime = () => {
  currentTime.value = dayjs().format('YYYY年MM月DD日 HH:mm:ss')
}

onMounted(() => {
  // 每秒更新时间
  setInterval(updateTime, 1000)
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  .welcome-card {
    margin-bottom: 16px;

    .welcome-content {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .welcome-info {
        h2 {
          font-size: 24px;
          font-weight: 600;
          color: $text-color-primary;
          margin: 0 0 12px 0;
        }

        .welcome-text {
          font-size: 14px;
          color: $text-color-secondary;
          margin: 8px 0;
          display: flex;
          align-items: center;
          gap: 4px;
        }

        .welcome-time {
          font-size: 13px;
          color: $text-color-secondary;
          margin-top: 4px;
        }
      }

      .welcome-avatar {
        :deep(.el-avatar) {
          background-color: $primary-color;
          font-size: 32px;
          font-weight: 600;
        }
      }
    }
  }

  .stats-row {
    margin-bottom: 16px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          &.primary {
            color: $primary-color;
          }

          &.success {
            color: $success-color;
          }

          &.warning {
            color: $warning-color;
          }

          &.error {
            color: $error-color;
          }
        }

        .stat-info {
          flex: 1;

          .stat-label {
            font-size: 14px;
            color: $text-color-secondary;
            margin-bottom: 4px;
          }

          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: $text-color-primary;
          }
        }
      }
    }
  }

  .quick-actions {
    margin-bottom: 16px;

    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: $background-color;
        transform: translateY(-4px);

        .action-icon {
          color: $primary-color;
        }
      }

      .action-icon {
        color: $text-color-secondary;
        margin-bottom: 8px;
        transition: color 0.3s;
      }

      .action-name {
        font-size: 14px;
        color: $text-color-regular;
      }
    }
  }

  .charts-row {
    .chart-card {
      margin-bottom: 16px;

      .chart-placeholder {
        height: 300px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: $text-color-secondary;
      }
    }
  }

  .card-header {
    font-size: 16px;
    font-weight: 600;
    color: $text-color-primary;
  }
}
</style>
