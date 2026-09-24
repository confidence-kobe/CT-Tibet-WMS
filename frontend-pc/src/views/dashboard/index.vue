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

    <!-- 数据统计卡片（管理人员） -->
    <el-row v-if="isManager" :gutter="16" class="stats-row">
      <el-col v-for="card in statCards" :key="card.key" :xs="12" :sm="6" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card" @click="handleQuickAction(card.path)">
          <div class="stat-content">
            <el-icon :size="32" :class="['stat-icon', card.color]">
              <component :is="card.icon" />
            </el-icon>
            <div class="stat-info">
              <div class="stat-label">{{ card.label }}</div>
              <div class="stat-value">{{ stats[card.key] ?? 0 }}</div>
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

    <!-- 我的最近申请（普通员工） -->
    <el-card v-if="!isManager" shadow="hover" class="recent-applies">
      <template #header>
        <div class="card-header">
          <span>我的最近申请</span>
          <el-button link type="primary" @click="handleQuickAction('/apply/list')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentApplies" v-loading="appliesLoading" @row-click="row => handleQuickAction(`/apply/detail/${row.id}`)">
        <el-table-column prop="applyNo" label="申请单号" min-width="180" />
        <el-table-column prop="warehouseName" label="仓库" min-width="140" />
        <el-table-column prop="applyReason" label="用途" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="applyStatusTag(row.status)" size="small">{{ formatApplyStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="170" />
        <template #empty>
          <el-empty description="还没有申请记录" :image-size="80">
            <el-button type="primary" @click="handleQuickAction('/apply/create')">新建申请</el-button>
          </el-empty>
        </template>
      </el-table>
    </el-card>

    <!-- 数据图表（管理人员） -->
    <el-row v-if="isManager" :gutter="16" class="charts-row">
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import dayjs from 'dayjs'
import EChart from '@/components/Chart/EChart.vue'
import { getDashboardStats, getInboundStatistics, getOutboundStatistics } from '@/api/statistics'
import { getInventorySummary } from '@/api/inventory'
import { getMyApplies } from '@/api/apply'
import { formatApplyStatus } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

// 当前时间
const currentTime = ref(dayjs().format('YYYY年MM月DD日 HH:mm:ss'))

const isManager = computed(() => ['admin', 'dept_admin', 'warehouse'].includes(userStore.roleCode))

// 统计数据（后端 DashboardStatsVO）
const stats = ref({})
const statCards = [
  { key: 'monthInboundCount', label: '本月入库单', icon: 'Download', color: 'primary', path: '/inbound/list' },
  { key: 'monthOutboundCount', label: '本月出库单', icon: 'Upload', color: 'success', path: '/outbound/list' },
  { key: 'pendingApplies', label: '待审批申请', icon: 'Clock', color: 'warning', path: '/approval/pending' },
  { key: 'lowStockAlerts', label: '库存预警', icon: 'WarningFilled', color: 'error', path: '/inventory/warning' }
]

// 近7天出入库趋势（按单据数）
const TREND_DAYS = 7
const trend = ref({ dates: [], inbound: [], outbound: [] })
// 库存状态分布
const inventorySummary = ref({ normalCount: 0, lowStockCount: 0, outOfStockCount: 0 })

// 员工：最近申请
const recentApplies = ref([])
const appliesLoading = ref(false)
const applyStatusTag = (status) => ({ 0: 'warning', 1: 'primary', 2: 'danger', 3: 'success', 4: 'info' }[status] || 'info')

// 快捷操作列表（根据角色显示不同的操作）
const quickActions = computed(() => {
  if (isManager.value) {
    return [
      { name: '新建入库', icon: 'Download', path: '/inbound/create' },
      { name: '新建出库', icon: 'Upload', path: '/outbound/create' },
      { name: '审批管理', icon: 'CircleCheck', path: '/approval/pending' },
      { name: '库存查询', icon: 'Search', path: '/inventory/query' },
      { name: '库存预警', icon: 'Warning', path: '/inventory/warning' }
    ]
  }
  return [
    { name: '新建申请', icon: 'Document', path: '/apply/create' },
    { name: '我的申请', icon: 'List', path: '/apply/list' },
    { name: '库存查询', icon: 'Search', path: '/inventory/query' },
    { name: '消息中心', icon: 'Bell', path: '/message/list' }
  ]
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

let timer = null

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
    data: trend.value.dates
  },
  yAxis: {
    type: 'value',
    name: '单据数',
    minInterval: 1,
    nameTextStyle: {
      fontSize: 12
    }
  },
  series: [
    {
      name: '入库',
      type: 'line',
      smooth: true,
      data: trend.value.inbound,
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
      data: trend.value.outbound,
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
        { value: inventorySummary.value.normalCount || 0, name: '正常', itemStyle: { color: '#67C23A' } },
        { value: inventorySummary.value.lowStockCount || 0, name: '低库存', itemStyle: { color: '#E6A23C' } },
        { value: inventorySummary.value.outOfStockCount || 0, name: '缺货', itemStyle: { color: '#F56C6C' } }
      ]
    }
  ]
}))

// 按天取单据数，日期显示为 MM-DD
const toDailyCounts = (res) => (res?.data?.dailyData || []).map(d => Number(d.count) || 0)

// 加载统计数据（管理人员）
const loadStats = async () => {
  const endDate = dayjs().format('YYYY-MM-DD')
  const startDate = dayjs().subtract(TREND_DAYS - 1, 'day').format('YYYY-MM-DD')
  const [dashboard, inbound, outbound, summary] = await Promise.allSettled([
    getDashboardStats(),
    getInboundStatistics({ startDate, endDate }),
    getOutboundStatistics({ startDate, endDate }),
    getInventorySummary()
  ])
  if (dashboard.status === 'fulfilled') stats.value = dashboard.value.data || {}
  if (inbound.status === 'fulfilled' && outbound.status === 'fulfilled') {
    const days = inbound.value?.data?.dailyData || []
    trend.value = {
      dates: days.map(d => dayjs(d.date).format('MM-DD')),
      inbound: toDailyCounts(inbound.value),
      outbound: toDailyCounts(outbound.value)
    }
  }
  if (summary.status === 'fulfilled') inventorySummary.value = summary.value.data || {}
}

// 加载我的最近申请（普通员工）
const loadRecentApplies = async () => {
  appliesLoading.value = true
  try {
    const res = await getMyApplies({ pageNum: 1, pageSize: 5 })
    recentApplies.value = res.data || []
  } catch (error) {
    console.error('加载我的申请失败:', error)
  } finally {
    appliesLoading.value = false
  }
}

onMounted(() => {
  // 每秒更新时间
  timer = setInterval(updateTime, 1000)
  // 管理人员看统计，员工看自己的申请
  if (isManager.value) {
    loadStats()
  } else {
    loadRecentApplies()
  }
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
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
      cursor: pointer;

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

  .recent-applies {
    margin-bottom: 16px;

    :deep(.el-table__row) {
      cursor: pointer;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 16px;
    font-weight: 600;
    color: $text-color-primary;
  }
}
</style>
