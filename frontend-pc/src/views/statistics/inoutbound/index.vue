<template>
  <div class="statistics-inoutbound-container page-container">
    <div class="page-header">
      <h2 class="page-title">出入库统计</h2>
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
        <el-form-item label="仓库">
          <el-select v-model="searchForm.warehouseId" placeholder="全部仓库" clearable @change="handleSearch">
            <el-option label="全部仓库" :value="null" />
            <el-option label="拉萨仓库" :value="1" />
            <el-option label="日喀则仓库" :value="2" />
            <el-option label="昌都仓库" :value="3" />
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
            <div class="stat-icon inbound">
              <el-icon><Download /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">入库总数</div>
              <div class="stat-value">{{ summary.totalInbound }}</div>
              <div class="stat-trend up">
                <el-icon><CaretTop /></el-icon>
                {{ summary.inboundGrowth }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon outbound">
              <el-icon><Upload /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">出库总数</div>
              <div class="stat-value">{{ summary.totalOutbound }}</div>
              <div class="stat-trend down">
                <el-icon><CaretBottom /></el-icon>
                {{ summary.outboundGrowth }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon amount">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">入库总额</div>
              <div class="stat-value">¥{{ summary.inboundAmount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon amount">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">出库总额</div>
              <div class="stat-value">¥{{ summary.outboundAmount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>出入库趋势分析</span>
          <el-radio-group v-model="trendType" size="small" @change="handleTrendTypeChange">
            <el-radio-button label="count">数量</el-radio-button>
            <el-radio-button label="amount">金额</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <EChart :option="trendChartOption" height="400px" />
    </el-card>

    <!-- 对比图表 -->
    <el-row :gutter="16">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>仓库出入库对比</span>
            </div>
          </template>
          <EChart :option="warehouseCompareOption" height="350px" />
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>物资类别分布</span>
            </div>
          </template>
          <EChart :option="categoryDistributionOption" height="350px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>详细数据</span>
        </div>
      </template>
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="inboundCount" label="入库数量" width="100" align="right" />
        <el-table-column prop="inboundAmount" label="入库金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.inboundAmount.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="outboundCount" label="出库数量" width="100" align="right" />
        <el-table-column prop="outboundAmount" label="出库金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.outboundAmount.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="netChange" label="净变化" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.netChange >= 0 ? 'text-success' : 'text-error'">
              {{ row.netChange > 0 ? '+' : '' }}{{ row.netChange }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
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
import { Search, Refresh, Download, Upload, Money, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import EChart from '@/components/Chart/EChart.vue'
import dayjs from 'dayjs'

// 搜索表单
const searchForm = ref({
  dateRange: [
    dayjs().subtract(30, 'day').toDate(),
    dayjs().toDate()
  ],
  warehouseId: null
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
  totalInbound: 1285,
  totalOutbound: 1098,
  inboundAmount: 285600,
  outboundAmount: 198300,
  inboundGrowth: 12.5,
  outboundGrowth: 8.3
})

// 趋势类型
const trendType = ref('count')

// 趋势图表配置
const trendChartOption = computed(() => {
  const isAmount = trendType.value === 'amount'

  return {
    title: {
      text: isAmount ? '出入库金额趋势' : '出入库数量趋势',
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
      data: ['入库', '出库', '净变化'],
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
      data: ['1/1', '1/5', '1/10', '1/15', '1/20', '1/25', '1/30']
    },
    yAxis: {
      type: 'value',
      name: isAmount ? '金额（元）' : '数量',
      nameTextStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '入库',
        type: 'line',
        smooth: true,
        data: isAmount ? [28000, 35000, 22000, 41000, 38000, 28000, 35000] : [45, 58, 38, 72, 65, 48, 58],
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ]
          }
        }
      },
      {
        name: '出库',
        type: 'line',
        smooth: true,
        data: isAmount ? [24000, 30000, 38000, 35000, 45000, 32000, 38000] : [38, 48, 62, 58, 75, 52, 62],
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
            ]
          }
        }
      },
      {
        name: '净变化',
        type: 'line',
        smooth: true,
        data: isAmount ? [4000, 5000, -16000, 6000, -7000, -4000, -3000] : [7, 10, -24, 14, -10, -4, -4],
        itemStyle: { color: '#E6A23C' }
      }
    ]
  }
})

// 仓库对比图表配置
const warehouseCompareOption = computed(() => ({
  title: {
    text: '各仓库出入库对比',
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
    data: ['拉萨仓库', '日喀则仓库', '昌都仓库', '林芝仓库', '那曲仓库']
  },
  yAxis: {
    type: 'value',
    name: '数量'
  },
  series: [
    {
      name: '入库',
      type: 'bar',
      data: [485, 285, 195, 155, 165],
      itemStyle: { color: '#409EFF' }
    },
    {
      name: '出库',
      type: 'bar',
      data: [412, 248, 175, 128, 135],
      itemStyle: { color: '#67C23A' }
    }
  ]
}))

// 物资类别分布图表配置
const categoryDistributionOption = computed(() => ({
  title: {
    text: '物资类别分布',
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
      name: '物资类别',
      type: 'pie',
      radius: '60%',
      center: ['60%', '50%'],
      data: [
        { value: 335, name: '通信设备', itemStyle: { color: '#409EFF' } },
        { value: 280, name: '网络设备', itemStyle: { color: '#67C23A' } },
        { value: 210, name: '线缆配件', itemStyle: { color: '#E6A23C' } },
        { value: 180, name: '工具仪器', itemStyle: { color: '#F56C6C' } },
        { value: 145, name: '办公用品', itemStyle: { color: '#909399' } },
        { value: 135, name: '其他物资', itemStyle: { color: '#C0C4CC' } }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      label: {
        show: true,
        formatter: '{b}: {d}%'
      }
    }
  ]
}))

// 表格数据
const tableData = ref([
  {
    date: '2025-01-13',
    warehouseName: '拉萨仓库',
    inboundCount: 45,
    inboundAmount: 28500,
    outboundCount: 38,
    outboundAmount: 24200,
    netChange: 7,
    remark: '正常业务'
  },
  {
    date: '2025-01-12',
    warehouseName: '拉萨仓库',
    inboundCount: 58,
    inboundAmount: 35200,
    outboundCount: 48,
    outboundAmount: 30100,
    netChange: 10,
    remark: '月度盘点'
  },
  {
    date: '2025-01-11',
    warehouseName: '日喀则仓库',
    inboundCount: 38,
    inboundAmount: 22800,
    outboundCount: 62,
    outboundAmount: 38500,
    netChange: -24,
    remark: '大批量出库'
  },
  {
    date: '2025-01-10',
    warehouseName: '拉萨仓库',
    inboundCount: 72,
    inboundAmount: 41200,
    outboundCount: 58,
    outboundAmount: 35800,
    netChange: 14,
    remark: '新品入库'
  },
  {
    date: '2025-01-09',
    warehouseName: '昌都仓库',
    inboundCount: 65,
    inboundAmount: 38500,
    outboundCount: 75,
    outboundAmount: 45200,
    netChange: -10,
    remark: '正常业务'
  }
])

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 150
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
    warehouseId: null
  }
  handleSearch()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 趋势类型变化
const handleTrendTypeChange = () => {
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
</script>

<style lang="scss" scoped>
.statistics-inoutbound-container {
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

          &.inbound {
            background-color: rgba(64, 158, 255, 0.1);
            color: #409EFF;
          }

          &.outbound {
            background-color: rgba(103, 194, 58, 0.1);
            color: #67C23A;
          }

          &.amount {
            background-color: rgba(230, 162, 60, 0.1);
            color: #E6A23C;
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

          .stat-trend {
            font-size: 12px;
            display: flex;
            align-items: center;
            gap: 2px;

            &.up {
              color: #67C23A;
            }

            &.down {
              color: #F56C6C;
            }
          }
        }
      }
    }
  }

  .chart-card {
    margin-bottom: 16px;
  }

  .table-card {
    .text-success {
      color: #67C23A;
    }

    .text-error {
      color: #F56C6C;
    }
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
