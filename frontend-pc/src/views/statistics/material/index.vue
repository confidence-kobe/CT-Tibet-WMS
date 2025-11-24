<template>
  <div class="statistics-material-container page-container">
    <div class="page-header">
      <h2 class="page-title">物资统计</h2>
    </div>

    <!-- 搜索条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="统计周期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item label="物资类别">
          <el-select v-model="searchForm.categoryId" placeholder="全部类别" clearable @change="handleSearch">
            <el-option label="全部类别" :value="null" />
            <el-option label="通信设备" :value="1" />
            <el-option label="网络设备" :value="2" />
            <el-option label="线缆配件" :value="3" />
            <el-option label="工具仪器" :value="4" />
            <el-option label="办公用品" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button :icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon category">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">物资种类</div>
              <div class="stat-value">{{ summary.totalCategories }}</div>
              <div class="stat-sub">在库物资</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon value">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">库存总价值</div>
              <div class="stat-value">¥{{ summary.totalValue.toLocaleString() }}</div>
              <div class="stat-sub">当前市值</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon hot">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">热门物资</div>
              <div class="stat-value">{{ summary.hotMaterials }}</div>
              <div class="stat-sub">高频使用</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon slow">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">滞销物资</div>
              <div class="stat-value">{{ summary.slowMoving }}</div>
              <div class="stat-sub">低周转率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <el-col :xs="24" :sm="24" :md="14">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>物资使用排行榜 (TOP 10)</span>
              <el-radio-group v-model="rankType" size="small" @change="handleRankTypeChange">
                <el-radio-button label="quantity">数量</el-radio-button>
                <el-radio-button label="value">价值</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <EChart :option="rankChartOption" height="450px" />
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="10">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>物资类别分布</span>
            </div>
          </template>
          <EChart :option="categoryChartOption" height="450px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 周转率分析 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>物资周转率分析</span>
          <el-tag type="info" size="small">周转率 = 出库次数 / 平均库存</el-tag>
        </div>
      </template>
      <EChart :option="turnoverChartOption" height="350px" />
    </el-card>

    <!-- 详细数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>物资详细统计</span>
        </div>
      </template>
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="materialName" label="物资名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="类别" width="120" />
        <el-table-column prop="specification" label="规格" width="120" show-overflow-tooltip />
        <el-table-column prop="inboundCount" label="入库次数" width="100" align="right" />
        <el-table-column prop="outboundCount" label="出库次数" width="100" align="right" />
        <el-table-column prop="currentStock" label="当前库存" width="100" align="right" />
        <el-table-column prop="turnoverRate" label="周转率" width="100" align="right">
          <template #default="{ row }">
            <el-tag :type="getTurnoverRateType(row.turnoverRate)" size="small">
              {{ row.turnoverRate }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalValue" label="库存价值" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalValue.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, Box, Money, TrendCharts, Warning } from '@element-plus/icons-vue'
import EChart from '@/components/Chart/EChart.vue'
import dayjs from 'dayjs'

// 搜索表单
const searchForm = ref({
  dateRange: [
    dayjs().subtract(30, 'day').toDate(),
    dayjs().toDate()
  ],
  categoryId: null
})

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => [dayjs().subtract(7, 'day').toDate(), dayjs().toDate()]
  },
  {
    text: '最近一个月',
    value: () => [dayjs().subtract(1, 'month').toDate(), dayjs().toDate()]
  },
  {
    text: '最近三个月',
    value: () => [dayjs().subtract(3, 'month').toDate(), dayjs().toDate()]
  },
  {
    text: '本年',
    value: () => [dayjs().startOf('year').toDate(), dayjs().toDate()]
  }
]

// 统计概览数据
const summary = ref({
  totalCategories: 156,
  totalValue: 2856000,
  hotMaterials: 38,
  slowMoving: 12
})

// 排行榜类型
const rankType = ref('quantity')

// 物资使用排行榜配置
const rankChartOption = computed(() => {
  const isValue = rankType.value === 'value'

  return {
    title: {
      text: isValue ? '物资价值排行' : '物资使用量排行',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const data = params[0]
        return `${data.name}<br/>${data.seriesName}: ${isValue ? '¥' + data.value.toLocaleString() : data.value}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: isValue ? '价值（元）' : '使用次数',
      nameTextStyle: {
        fontSize: 12
      }
    },
    yAxis: {
      type: 'category',
      data: [
        '光纤跳线',
        '网络交换机',
        '路由器',
        '网线',
        '配线架',
        '光纤收发器',
        '机柜',
        'POE交换机',
        '光缆',
        '网络模块'
      ].reverse(),
      axisLabel: {
        fontSize: 12
      }
    },
    series: [
      {
        name: isValue ? '价值' : '使用次数',
        type: 'bar',
        data: isValue
          ? [125000, 118000, 95000, 88000, 75000, 68000, 58000, 52000, 45000, 38000].reverse()
          : [285, 268, 245, 228, 195, 182, 165, 148, 125, 108].reverse(),
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#409EFF' },
              { offset: 1, color: '#67C23A' }
            ]
          },
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: true,
          position: 'right',
          formatter: (params) => {
            return isValue ? '¥' + params.value.toLocaleString() : params.value
          }
        }
      }
    ]
  }
})

// 物资类别分布配置
const categoryChartOption = computed(() => ({
  title: {
    text: '类别分布',
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
    top: 'middle',
    textStyle: {
      fontSize: 12
    }
  },
  series: [
    {
      name: '物资类别',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        position: 'outside',
        formatter: '{b}\n{d}%',
        fontSize: 12
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
        { value: 45, name: '通信设备', itemStyle: { color: '#409EFF' } },
        { value: 35, name: '网络设备', itemStyle: { color: '#67C23A' } },
        { value: 28, name: '线缆配件', itemStyle: { color: '#E6A23C' } },
        { value: 22, name: '工具仪器', itemStyle: { color: '#F56C6C' } },
        { value: 15, name: '办公用品', itemStyle: { color: '#909399' } },
        { value: 11, name: '其他物资', itemStyle: { color: '#C0C4CC' } }
      ]
    }
  ]
}))

// 周转率分析配置
const turnoverChartOption = computed(() => ({
  title: {
    text: '各类物资周转率对比',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'normal'
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {
    data: ['周转率', '平均值'],
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
    data: ['通信设备', '网络设备', '线缆配件', '工具仪器', '办公用品', '其他物资']
  },
  yAxis: {
    type: 'value',
    name: '周转率',
    nameTextStyle: {
      fontSize: 12
    }
  },
  series: [
    {
      name: '周转率',
      type: 'bar',
      data: [8.5, 7.2, 6.8, 5.5, 4.2, 3.8],
      itemStyle: {
        color: (params) => {
          const colors = ['#67C23A', '#409EFF', '#E6A23C', '#E6A23C', '#F56C6C', '#F56C6C']
          return colors[params.dataIndex]
        }
      },
      label: {
        show: true,
        position: 'top'
      }
    },
    {
      name: '平均值',
      type: 'line',
      data: [6.0, 6.0, 6.0, 6.0, 6.0, 6.0],
      itemStyle: {
        color: '#909399'
      },
      lineStyle: {
        type: 'dashed',
        width: 2
      }
    }
  ]
}))

// 表格数据
const tableData = ref([
  {
    materialName: '光纤跳线 LC-LC',
    categoryName: '线缆配件',
    specification: '3米 单模',
    inboundCount: 45,
    outboundCount: 285,
    currentStock: 1250,
    turnoverRate: 8.5,
    totalValue: 125000,
    status: '热销'
  },
  {
    materialName: '千兆网络交换机',
    categoryName: '网络设备',
    specification: '24口',
    inboundCount: 12,
    outboundCount: 68,
    currentStock: 85,
    turnoverRate: 7.2,
    totalValue: 298000,
    status: '正常'
  },
  {
    materialName: '企业级路由器',
    categoryName: '网络设备',
    specification: 'AC1900',
    inboundCount: 8,
    outboundCount: 45,
    currentStock: 42,
    turnoverRate: 6.8,
    totalValue: 185000,
    status: '正常'
  },
  {
    materialName: '六类网线',
    categoryName: '线缆配件',
    specification: '305米/箱',
    inboundCount: 28,
    outboundCount: 128,
    currentStock: 385,
    turnoverRate: 5.5,
    totalValue: 88000,
    status: '正常'
  },
  {
    materialName: '光缆测试仪',
    categoryName: '工具仪器',
    specification: 'OTDR',
    inboundCount: 2,
    outboundCount: 8,
    currentStock: 6,
    turnoverRate: 4.2,
    totalValue: 156000,
    status: '低频'
  },
  {
    materialName: '机柜理线器',
    categoryName: '配件',
    specification: '1U',
    inboundCount: 15,
    outboundCount: 35,
    currentStock: 125,
    turnoverRate: 3.8,
    totalValue: 12500,
    status: '滞销'
  }
])

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 156
})

// 查询
const handleSearch = () => {
  ElMessage.success('查询成功')
  // TODO: 调用API
}

// 重置
const handleReset = () => {
  searchForm.value = {
    dateRange: [
      dayjs().subtract(30, 'day').toDate(),
      dayjs().toDate()
    ],
    categoryId: null
  }
  handleSearch()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 排行类型变化
const handleRankTypeChange = () => {
  // 图表会自动响应computed的变化
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.value.size = size
  handleSearch()
}

const handlePageChange = (page) => {
  pagination.value.page = page
  handleSearch()
}

// 获取周转率标签类型
const getTurnoverRateType = (rate) => {
  if (rate >= 7) return 'success'
  if (rate >= 5) return ''
  if (rate >= 3) return 'warning'
  return 'danger'
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    '热销': 'success',
    '正常': '',
    '低频': 'warning',
    '滞销': 'danger'
  }
  return typeMap[status] || ''
}
</script>

<style lang="scss" scoped>
.statistics-material-container {
  .search-card {
    margin-bottom: 16px;
  }

  .stats-row {
    margin-bottom: 16px;

    .stat-card {
      margin-bottom: 16px;

      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 56px;
          height: 56px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;

          &.category {
            background-color: rgba(64, 158, 255, 0.1);
            color: #409EFF;
          }

          &.value {
            background-color: rgba(230, 162, 60, 0.1);
            color: #E6A23C;
          }

          &.hot {
            background-color: rgba(103, 194, 58, 0.1);
            color: #67C23A;
          }

          &.slow {
            background-color: rgba(245, 108, 108, 0.1);
            color: #F56C6C;
          }
        }

        .stat-info {
          flex: 1;

          .stat-label {
            font-size: 14px;
            color: $text-color-secondary;
            margin-bottom: 8px;
          }

          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: $text-color-primary;
            margin-bottom: 4px;
          }

          .stat-sub {
            font-size: 12px;
            color: $text-color-secondary;
          }
        }
      }
    }
  }

  .chart-card {
    margin-bottom: 16px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    color: $text-color-primary;
  }
}
</style>
