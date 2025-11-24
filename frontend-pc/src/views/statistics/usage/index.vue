<template>
  <div class="statistics-usage-container page-container">
    <div class="page-header">
      <h2 class="page-title">使用统计</h2>
    </div>

    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" :inline="true" label-width="80px">
        <el-form-item label="统计周期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.deptId" placeholder="请选择部门" clearable style="width: 200px">
            <el-option label="全部" :value="null" />
            <el-option label="物流部" value="1" />
            <el-option label="技术部" value="2" />
            <el-option label="市场部" value="3" />
            <el-option label="行政部" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户">
          <el-select v-model="searchForm.userId" placeholder="请选择用户" clearable filterable style="width: 200px">
            <el-option label="全部" :value="null" />
            <el-option label="张三" value="1" />
            <el-option label="李四" value="2" />
            <el-option label="王五" value="3" />
            <el-option label="赵六" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="物资">
          <el-select v-model="searchForm.materialId" placeholder="请选择物资" clearable filterable style="width: 200px">
            <el-option label="全部" :value="null" />
            <el-option label="光纤跳线" value="1" />
            <el-option label="网络交换机" value="2" />
            <el-option label="路由器" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="16" class="stats-overview">
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" class="stat-icon primary">
              <Operation />
            </el-icon>
            <div class="stat-info">
              <div class="stat-label">总操作次数</div>
              <div class="stat-value">{{ stats.totalOperations }}</div>
              <div class="stat-trend positive">
                <el-icon><CaretTop /></el-icon>
                较上期 +12.5%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" class="stat-icon success">
              <User />
            </el-icon>
            <div class="stat-info">
              <div class="stat-label">活跃用户数</div>
              <div class="stat-value">{{ stats.activeUsers }}</div>
              <div class="stat-trend positive">
                <el-icon><CaretTop /></el-icon>
                较上期 +8.3%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" class="stat-icon warning">
              <TrendCharts />
            </el-icon>
            <div class="stat-info">
              <div class="stat-label">日均操作</div>
              <div class="stat-value">{{ stats.avgDaily }}</div>
              <div class="stat-trend negative">
                <el-icon><CaretBottom /></el-icon>
                较上期 -3.2%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="32" class="stat-icon error">
              <Box />
            </el-icon>
            <div class="stat-info">
              <div class="stat-label">活跃物资</div>
              <div class="stat-value">{{ stats.activeMaterials }}</div>
              <div class="stat-trend positive">
                <el-icon><CaretTop /></el-icon>
                较上期 +15.7%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" class="charts-section">
      <!-- 使用趋势 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>使用趋势分析</span>
              <el-radio-group v-model="trendType" size="small">
                <el-radio-button label="count">操作次数</el-radio-button>
                <el-radio-button label="users">活跃用户</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <EChart :option="trendChartOption" height="400px" />
        </el-card>
      </el-col>

      <!-- 部门使用对比 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>部门使用对比</span>
            </div>
          </template>
          <EChart :option="deptChartOption" height="350px" />
        </el-card>
      </el-col>

      <!-- 用户活跃度排行 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户活跃度排行 TOP 10</span>
            </div>
          </template>
          <EChart :option="userRankChartOption" height="350px" />
        </el-card>
      </el-col>

      <!-- 操作类型分布 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>操作类型分布</span>
            </div>
          </template>
          <EChart :option="operationTypeChartOption" height="350px" />
        </el-card>
      </el-col>

      <!-- 热门物资排行 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门物资排行 TOP 10</span>
            </div>
          </template>
          <EChart :option="materialRankChartOption" height="350px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细记录表格 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span>使用记录明细</span>
          <el-button type="primary" size="small" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="operationTime" label="操作时间" width="160" align="center" />
        <el-table-column prop="operationType" label="操作类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)" size="small">
              {{ row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="操作人" width="100" align="center" />
        <el-table-column prop="deptName" label="所属部门" width="120" align="center" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="amount" label="金额（元）" width="120" align="right">
          <template #default="{ row }">
            <span class="amount-text">{{ formatAmount(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="单据号" width="160" align="center" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import EChart from '@/components/Chart/EChart.vue'
import dayjs from 'dayjs'

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

// 搜索表单
const searchForm = ref({
  dateRange: [
    dayjs().subtract(30, 'day').toDate(),
    dayjs().toDate()
  ],
  deptId: null,
  userId: null,
  materialId: null
})

// 统计数据
const stats = ref({
  totalOperations: 1258,
  activeUsers: 89,
  avgDaily: 42,
  activeMaterials: 156
})

// 趋势类型
const trendType = ref('count')

// 分页
const pagination = ref({
  current: 1,
  size: 10,
  total: 125
})

// 表格数据（模拟）
const tableData = ref([
  {
    operationTime: '2025-11-14 14:23:15',
    operationType: '出库',
    userName: '张三',
    deptName: '物流部',
    materialName: '光纤跳线 SC-LC 单模 3米',
    quantity: 50,
    unit: '根',
    amount: 2500,
    orderNo: 'OUT202511140023',
    remark: '拉萨项目使用'
  },
  {
    operationTime: '2025-11-14 11:45:32',
    operationType: '入库',
    userName: '李四',
    deptName: '物流部',
    materialName: '网络交换机 24口千兆',
    quantity: 10,
    unit: '台',
    amount: 28000,
    orderNo: 'IN202511140015',
    remark: '新采购设备入库'
  },
  {
    operationTime: '2025-11-14 10:18:45',
    operationType: '申请',
    userName: '王五',
    deptName: '技术部',
    materialName: '光功率计',
    quantity: 2,
    unit: '台',
    amount: 8000,
    orderNo: 'APPLY202511140008',
    remark: '日喀则项目测试使用'
  },
  {
    operationTime: '2025-11-14 09:30:12',
    operationType: '出库',
    userName: '赵六',
    deptName: '市场部',
    materialName: '网线 超五类 305米/箱',
    quantity: 5,
    unit: '箱',
    amount: 1750,
    orderNo: 'OUT202511140019',
    remark: '办公室布线'
  },
  {
    operationTime: '2025-11-13 16:52:08',
    operationType: '入库',
    userName: '张三',
    deptName: '物流部',
    materialName: '光纤收发器 千兆单模',
    quantity: 20,
    unit: '对',
    amount: 6000,
    orderNo: 'IN202511130022',
    remark: '补充库存'
  },
  {
    operationTime: '2025-11-13 15:20:35',
    operationType: '申请',
    userName: '孙七',
    deptName: '技术部',
    materialName: '光纤熔接机',
    quantity: 1,
    unit: '台',
    amount: 25000,
    orderNo: 'APPLY202511130012',
    remark: '林芝项目施工'
  },
  {
    operationTime: '2025-11-13 13:45:28',
    operationType: '出库',
    userName: '周八',
    deptName: '物流部',
    materialName: '配线架 24口',
    quantity: 8,
    unit: '个',
    amount: 2400,
    orderNo: 'OUT202511130018',
    remark: '机房整理'
  },
  {
    operationTime: '2025-11-13 11:10:15',
    operationType: '入库',
    userName: '李四',
    deptName: '物流部',
    materialName: '光纤终端盒 4口',
    quantity: 100,
    unit: '个',
    amount: 3000,
    orderNo: 'IN202511130010',
    remark: '批量采购'
  },
  {
    operationTime: '2025-11-13 09:25:42',
    operationType: '出库',
    userName: '张三',
    deptName: '物流部',
    materialName: 'RJ45水晶头 8P8C',
    quantity: 500,
    unit: '个',
    amount: 250,
    orderNo: 'OUT202511130005',
    remark: '网络维护使用'
  },
  {
    operationTime: '2025-11-12 16:35:18',
    operationType: '申请',
    userName: '吴九',
    deptName: '市场部',
    materialName: '无线路由器 企业级',
    quantity: 3,
    unit: '台',
    amount: 3600,
    orderNo: 'APPLY202511120015',
    remark: '分公司办公使用'
  }
])

// 使用趋势图表配置
const trendChartOption = computed(() => {
  const isCount = trendType.value === 'count'

  return {
    title: {
      text: isCount ? '近30天操作次数趋势' : '近30天活跃用户趋势',
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
      data: isCount ? ['入库', '出库', '申请'] : ['活跃用户'],
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
      data: Array.from({ length: 30 }, (_, i) =>
        dayjs().subtract(29 - i, 'day').format('MM-DD')
      )
    },
    yAxis: {
      type: 'value',
      name: isCount ? '操作次数' : '用户数',
      nameTextStyle: {
        fontSize: 12
      }
    },
    series: isCount ? [
      {
        name: '入库',
        type: 'line',
        smooth: true,
        data: [12, 15, 18, 14, 22, 19, 25, 28, 23, 20, 24, 27, 30, 26, 22, 25, 29, 31, 28, 24, 26, 30, 33, 29, 27, 31, 34, 30, 28, 32],
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
        data: [18, 22, 25, 20, 28, 26, 32, 35, 30, 27, 31, 34, 38, 33, 29, 32, 36, 39, 35, 31, 34, 38, 41, 37, 34, 38, 42, 38, 35, 40],
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
        name: '申请',
        type: 'line',
        smooth: true,
        data: [8, 10, 12, 9, 14, 11, 15, 17, 14, 12, 15, 16, 18, 15, 13, 16, 18, 20, 17, 14, 16, 19, 21, 18, 16, 19, 22, 19, 17, 21],
        itemStyle: { color: '#E6A23C' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
              { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }
            ]
          }
        }
      }
    ] : [
      {
        name: '活跃用户',
        type: 'line',
        smooth: true,
        data: [45, 52, 58, 48, 65, 59, 72, 78, 68, 62, 70, 75, 82, 73, 65, 71, 79, 85, 76, 68, 74, 81, 88, 80, 73, 82, 90, 82, 75, 89],
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
      }
    ]
  }
})

// 部门使用对比图表配置
const deptChartOption = computed(() => ({
  title: {
    text: '各部门操作统计',
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
    data: ['入库', '出库', '申请'],
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
    data: ['物流部', '技术部', '市场部', '行政部', '财务部']
  },
  yAxis: {
    type: 'value',
    name: '操作次数',
    nameTextStyle: {
      fontSize: 12
    }
  },
  series: [
    {
      name: '入库',
      type: 'bar',
      data: [285, 156, 98, 45, 23],
      itemStyle: {
        color: '#409EFF'
      }
    },
    {
      name: '出库',
      type: 'bar',
      data: [358, 189, 125, 67, 38],
      itemStyle: {
        color: '#67C23A'
      }
    },
    {
      name: '申请',
      type: 'bar',
      data: [168, 245, 156, 89, 45],
      itemStyle: {
        color: '#E6A23C'
      }
    }
  ]
}))

// 用户活跃度排行图表配置
const userRankChartOption = computed(() => {
  const users = ['张三', '李四', '王五', '赵六', '孙七', '周八', '吴九', '郑十', '钱一', '陈二']
  const counts = [158, 142, 135, 128, 115, 108, 95, 87, 78, 65]

  return {
    title: {
      text: '用户操作次数排行',
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
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '操作次数',
      nameTextStyle: {
        fontSize: 12
      }
    },
    yAxis: {
      type: 'category',
      data: users.slice().reverse(),
      axisLabel: {
        fontSize: 12
      }
    },
    series: [
      {
        type: 'bar',
        data: counts.slice().reverse(),
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
          formatter: '{c} 次',
          fontSize: 11
        }
      }
    ]
  }
})

// 操作类型分布图表配置
const operationTypeChartOption = computed(() => ({
  title: {
    text: '操作类型分布',
    left: 'center',
    textStyle: {
      fontSize: 14,
      fontWeight: 'normal'
    }
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)次'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
    top: 'middle'
  },
  series: [
    {
      name: '操作类型',
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
        { value: 489, name: '入库', itemStyle: { color: '#409EFF' } },
        { value: 612, name: '出库', itemStyle: { color: '#67C23A' } },
        { value: 345, name: '申请', itemStyle: { color: '#E6A23C' } }
      ]
    }
  ]
}))

// 热门物资排行图表配置
const materialRankChartOption = computed(() => {
  const materials = [
    '光纤跳线',
    '网络交换机',
    '网线',
    '光纤收发器',
    '配线架',
    '光功率计',
    '光纤终端盒',
    '无线路由器',
    'RJ45水晶头',
    '光纤熔接机'
  ]
  const counts = [285, 268, 245, 198, 176, 158, 142, 125, 108, 89]

  return {
    title: {
      text: '热门物资使用次数',
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
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '使用次数',
      nameTextStyle: {
        fontSize: 12
      }
    },
    yAxis: {
      type: 'category',
      data: materials.slice().reverse(),
      axisLabel: {
        fontSize: 12
      }
    },
    series: [
      {
        type: 'bar',
        data: counts.slice().reverse(),
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#F56C6C' },
              { offset: 1, color: '#E6A23C' }
            ]
          },
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c} 次',
          fontSize: 11
        }
      }
    ]
  }
})

// 操作类型标签
const getOperationTypeTag = (type) => {
  const typeMap = {
    '入库': 'primary',
    '出库': 'success',
    '申请': 'warning'
  }
  return typeMap[type] || 'info'
}

// 格式化金额
const formatAmount = (amount) => {
  return amount?.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 搜索
const handleSearch = () => {
  ElMessage.success('查询成功')
  // 实际应调用API获取数据
}

// 重置
const handleReset = () => {
  searchForm.value = {
    dateRange: [
      dayjs().subtract(30, 'day').toDate(),
      dayjs().toDate()
    ],
    deptId: null,
    userId: null,
    materialId: null
  }
  ElMessage.info('已重置搜索条件')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 分页
const handleSizeChange = (size) => {
  pagination.value.size = size
  // 实际应调用API获取数据
}

const handleCurrentChange = (page) => {
  pagination.value.current = page
  // 实际应调用API获取数据
}
</script>

<style lang="scss" scoped>
.statistics-usage-container {
  .search-card {
    margin-bottom: 16px;
  }

  .stats-overview {
    margin-bottom: 16px;

    .stat-card {
      margin-bottom: 16px;

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
            margin-bottom: 4px;
          }

          .stat-trend {
            font-size: 12px;
            display: flex;
            align-items: center;
            gap: 2px;

            &.positive {
              color: $success-color;
            }

            &.negative {
              color: $error-color;
            }
          }
        }
      }
    }
  }

  .charts-section {
    margin-bottom: 16px;

    .chart-card {
      margin-bottom: 16px;
    }
  }

  .table-card {
    .amount-text {
      color: $primary-color;
      font-weight: 500;
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

  :deep(.el-pagination) {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
