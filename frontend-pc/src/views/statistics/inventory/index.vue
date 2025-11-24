<template>
  <div class="statistics-inventory-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">库存统计</h2>
      <el-button type="primary" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出报表
      </el-button>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="仓库">
          <el-select
            v-model="queryForm.warehouseId"
            placeholder="全部仓库"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            />
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
          <el-statistic title="物资种类" :value="stats.materialCount">
            <template #suffix>种</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="库存总值" :value="stats.totalValue" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <el-statistic title="预警数量" :value="stats.warningCount">
            <template #suffix>项</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="库存周转率" :value="stats.turnoverRate" :precision="2">
            <template #suffix>次/月</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="barChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="pieChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="lineChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <!-- 数据表格 - Top 10库存占用 -->
          <div style="padding: 10px 0; font-weight: 600; text-align: center; border-bottom: 1px solid #eee;">
            Top 10 库存占用
          </div>
          <el-table :data="tableData" stripe max-height="320" style="margin-top: 0;">
            <el-table-column type="index" label="排名" width="60" align="center" />
            <el-table-column prop="materialName" label="物资名称" min-width="150" />
            <el-table-column prop="stock" label="库存" width="80" align="right" />
            <el-table-column prop="value" label="库存金额" align="right" width="120">
              <template #default="{ row }">
                ¥{{ row.value?.toFixed(2) || '0.00' }}
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
import { getInventoryStatistics } from '@/api/statistics'
import { listWarehouses } from '@/api/warehouse'

// 查询表单
const queryForm = reactive({
  warehouseId: null
})

// 统计数据
const stats = reactive({
  materialCount: 0,
  totalValue: 0,
  warningCount: 0,
  turnoverRate: 0
})

// 仓库列表
const warehouseList = ref([])

// 表格数据 - Top 10库存占用
const tableData = ref([])

// 图表实例
const barChartRef = ref(null)
const pieChartRef = ref(null)
const lineChartRef = ref(null)
let barChart = null
let pieChart = null
let lineChart = null

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
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
  }

  // 响应式调整
  window.addEventListener('resize', handleResize)
}

// 窗口大小变化
const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
  lineChart?.resize()
}

// 查询统计数据
const handleQuery = async () => {
  try {
    const params = {
      warehouseId: queryForm.warehouseId || undefined
    }

    const res = await getInventoryStatistics(params)
    const data = res.data || {}

    // 更新统计卡片
    stats.materialCount = data.materialCount || 0
    stats.totalValue = data.totalValue || 0
    stats.warningCount = data.warningCount || 0
    stats.turnoverRate = data.turnoverRate || 0

    // 更新表格数据
    tableData.value = data.topStocks || []

    // 更新图表
    updateBarChart(data.warehouseData || [])
    updatePieChart(data.categoryData || [])
    updateLineChart(data.warningTrendData || {})
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  }
}

// 更新柱状图 - 各仓库库存金额
const updateBarChart = (data) => {
  const option = {
    title: { text: '各仓库库存金额', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.name) || [],
      axisLabel: { interval: 0, rotate: 30 }
    },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      {
        name: '库存金额',
        type: 'bar',
        data: data.map(item => item.value) || [],
        itemStyle: { color: '#ee6666' },
        barWidth: '60%'
      }
    ]
  }
  barChart?.setOption(option)
}

// 更新饼图 - 物资分类占比
const updatePieChart = (data) => {
  const option = {
    title: { text: '物资分类占比', left: 'center' },
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [
      {
        name: '物资分类',
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

// 更新折线图 - 预警趋势
const updateLineChart = (data) => {
  const option = {
    title: { text: '预警趋势图', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['预警数量'], top: 30 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates || []
    },
    yAxis: {
      type: 'value',
      name: '数量'
    },
    series: [
      {
        name: '预警数量',
        type: 'line',
        smooth: true,
        data: data.counts || [],
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(250, 140, 22, 0.3)' },
            { offset: 1, color: 'rgba(250, 140, 22, 0.05)' }
          ])
        },
        itemStyle: { color: '#fa8c16' }
      }
    ]
  }
  lineChart?.setOption(option)
}

// 重置查询
const handleReset = () => {
  queryForm.warehouseId = null
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
  handleQuery()
})

// 清理
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style lang="scss" scoped>
.statistics-inventory-container {
  .stat-card {
    :deep(.el-statistic__content) {
      font-size: 24px;
      font-weight: 600;
    }

    &.warning {
      :deep(.el-statistic__content) {
        color: $warning-color;
      }
    }
  }
}
</style>
