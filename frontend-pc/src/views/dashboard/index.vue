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
          <EChart :option="trendChartOption" height="350px" />
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>库存状态分布</span>
            </div>
          </template>
          <EChart :option="stockChartOption" height="350px" />
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
import EChart from '@/components/Chart/EChart.vue'
import { getDashboardStats } from '@/api/statistics'

const router = useRouter()
const userStore = useUserStore()

// 当前时间
const currentTime = ref(dayjs().format('YYYY年MM月DD日 HH:mm:ss'))

// 统计数据
const stats = ref({
  todayInbound: 0,
  todayOutbound: 0,
  pendingApproval: 0,
  stockWarning: 0
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

// 出入库趋势图表配置
const trendChartOption = computed(() => ({
  title: {
    text: '近7天出入库趋势',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'normal'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    data: ['入库', '出库'],
    bottom: 10
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  },
  yAxis: {
    type: 'value',
    name: '数量',
    nameTextStyle: {
      fontSize: 12
    }
  },
  series: [
    {
      name: '入库',
      type: 'line',
      smooth: true,
      data: [12, 15, 8, 20, 18, 11, 15],
      itemStyle: {
        color: '#409EFF'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0, color: 'rgba(64, 158, 255, 0.3)'
          }, {
            offset: 1, color: 'rgba(64, 158, 255, 0.05)'
          }]
        }
      }
    },
    {
      name: '出库',
      type: 'line',
      smooth: true,
      data: [10, 12, 18, 15, 22, 13, 16],
      itemStyle: {
        color: '#67C23A'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0, color: 'rgba(103, 194, 58, 0.3)'
          }, {
            offset: 1, color: 'rgba(103, 194, 58, 0.05)'
          }]
        }
      }
    }
  ]
}))

// 库存状态分布图表配置
const stockChartOption = computed(() => ({
  title: {
    text: '库存状态分布',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'normal'
    }
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'middle'
  },
  series: [
    {
      name: '库存状态',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}: {d}%'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      data: [
        { value: 245, name: '充足', itemStyle: { color: '#67C23A' } },
        { value: 89, name: '正常', itemStyle: { color: '#409EFF' } },
        { value: 34, name: '预警', itemStyle: { color: '#E6A23C' } },
        { value: 12, name: '紧急', itemStyle: { color: '#F56C6C' } }
      ]
    }
  ]
}))

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  // 每秒更新时间
  setInterval(updateTime, 1000)
  // 加载统计数据
  loadStats()
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
    }
  }

  .card-header {
    font-size: 16px;
    font-weight: 600;
    color: $text-color-primary;
  }
}
</style>
