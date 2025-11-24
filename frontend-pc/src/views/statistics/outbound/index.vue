<template>
  <div class="statistics-outbound-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">出库统计</h2>
      <el-button type="primary" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出报表
      </el-button>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            style="width: 260px"
          />
        </el-form-item>

        <el-form-item label="仓库">
          <el-select
            v-model="queryForm.warehouseId"
            placeholder="全部仓库"
            clearable
            style="width: 160px"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="出库类型">
          <el-select
            v-model="queryForm.outboundType"
            placeholder="全部类型"
            clearable
            style="width: 160px"
          >
            <el-option label="直接出库" :value="1" />
            <el-option label="申请出库" :value="2" />
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
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="出库总次数" :value="stats.totalCount">
            <template #suffix>次</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="出库总数量" :value="stats.totalQuantity">
            <template #suffix>件</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="出库总金额" :value="stats.totalAmount" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="日均出库" :value="stats.avgDaily" :precision="1">
            <template #suffix>件/天</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="lineChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="barChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="pieChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <!-- 数据表格 -->
          <el-table :data="tableData" stripe border max-height="350">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="count" label="出库次数" width="100" align="right" />
            <el-table-column prop="quantity" label="出库数量" width="100" align="right" />
            <el-table-column prop="amount" label="出库金额" align="right">
              <template #default="{ row }">
                ¥{{ row.amount?.toFixed(2) || '0.00' }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getOutboundStatistics } from '@/api/statistics'
import { listWarehouses } from '@/api/warehouse'

// 查询表单
const queryForm = reactive({
  dateRange: [],
  warehouseId: null,
  outboundType: null
})

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 统计数据
const stats = reactive({
  totalCount: 0,
  totalQuantity: 0,
  totalAmount: 0,
  avgDaily: 0
})

// 仓库列表
const warehouseList = ref([])

// 表格数据
const tableData = ref([])

// 图表实例
const lineChartRef = ref(null)
const barChartRef = ref(null)
const pieChartRef = ref(null)
let lineChart = null
let barChart = null
let pieChart = null

// 加载仓库列表
const loadWarehouses = async () => {
  try {
    const res = await listWarehouses({ status: 0 })
    warehouseList.value = res.data || []
  } catch (error) {
    console.error('加载仓库列表失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
  }
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }

  // 响应式调整
  window.addEventListener('resize', handleResize)
}

// 窗口大小变化
const handleResize = () => {
  lineChart?.resize()
  barChart?.resize()
  pieChart?.resize()
}

// 查询统计数据
const handleQuery = async () => {
  try {
    const params = {
      startDate: queryForm.dateRange?.[0] || undefined,
      endDate: queryForm.dateRange?.[1] || undefined,
      warehouseId: queryForm.warehouseId || undefined,
      outboundType: queryForm.outboundType || undefined
    }

    const res = await getOutboundStatistics(params)
    const data = res.data || {}

    // 更新统计卡片
    stats.totalCount = data.totalCount || 0
    stats.totalQuantity = data.totalQuantity || 0
    stats.totalAmount = data.totalAmount || 0
    stats.avgDaily = data.avgDaily || 0

    // 更新表格数据
    tableData.value = data.dailyData || []

    // 更新图表
    updateLineChart(data.trendData || {})
    updateBarChart(data.warehouseData || [])
    updatePieChart(data.typeData || [])
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  }
}

// 更新折线图
const updateLineChart = (data) => {
  const option = {
    title: { text: '出库趋势图', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['出库数量', '出库金额'], top: 30 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates || []
    },
    yAxis: [
      { type: 'value', name: '数量', position: 'left' },
      { type: 'value', name: '金额(元)', position: 'right' }
    ],
    series: [
      {
        name: '出库数量',
        type: 'line',
        smooth: true,
        data: data.quantities || [],
        yAxisIndex: 0
      },
      {
        name: '出库金额',
        type: 'line',
        smooth: true,
        data: data.amounts || [],
        yAxisIndex: 1
      }
    ]
  }
  lineChart?.setOption(option)
}

// 更新柱状图
const updateBarChart = (data) => {
  const option = {
    title: { text: '按仓库统计', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.name) || []
    },
    yAxis: { type: 'value', name: '数量' },
    series: [
      {
        name: '出库数量',
        type: 'bar',
        data: data.map(item => item.value) || [],
        itemStyle: { color: '#91cc75' },
        barWidth: '60%'
      }
    ]
  }
  barChart?.setOption(option)
}

// 更新饼图 - 按出库类型统计
const updatePieChart = (data) => {
  const option = {
    title: { text: '出库类型占比', left: 'center' },
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [
      {
        name: '出库类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 20, fontWeight: 'bold' }
        },
        labelLine: { show: false },
        data: data || []
      }
    ]
  }
  pieChart?.setOption(option)
}

// 重置查询
const handleReset = () => {
  queryForm.dateRange = []
  queryForm.warehouseId = null
  queryForm.outboundType = null
  handleQuery()
}

// 导出报表
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 初始化
onMounted(() => {
  loadWarehouses()
  initCharts()

  // 设置默认时间范围为最近一个月
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
  queryForm.dateRange = [
    start.toISOString().split('T')[0],
    end.toISOString().split('T')[0]
  ]

  handleQuery()
})

// 清理
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<style lang="scss" scoped>
.statistics-outbound-container {
  .stat-card {
    :deep(.el-statistic__content) {
      font-size: 24px;
      font-weight: 600;
    }
  }
}
</style>
