<template>
  <div class="inventory-query-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">库存查询</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="物资名称">
          <el-input
            v-model="queryForm.materialName"
            placeholder="请输入物资名称"
            clearable
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="物资编码">
          <el-input
            v-model="queryForm.materialCode"
            placeholder="请输入物资编码"
            clearable
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select
            v-model="queryForm.warehouseId"
            placeholder="请选择仓库"
            clearable
            @clear="handleQuery"
            style="width: 180px"
          >
            <el-option label="拉萨总仓" :value="1" />
            <el-option label="日喀则分仓" :value="2" />
            <el-option label="那曲分仓" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select
            v-model="queryForm.stockStatus"
            placeholder="请选择"
            clearable
            @clear="handleQuery"
            style="width: 140px"
          >
            <el-option label="正常" value="normal" />
            <el-option label="低库存" value="low" />
            <el-option label="缺货" value="empty" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="spec" label="规格型号" width="120" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column prop="stock" label="当前库存" width="100" align="right">
          <template #default="{ row }">
            <span :class="getStockClass(row)">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100" align="right" />
        <el-table-column prop="price" label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalValue" label="库存金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ (row.stock * row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStockStatusType(row)" size="small">
              {{ getStockStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewLog(row)">
              流水记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 16px; text-align: right;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="物资总数" :value="statistics.totalItems">
            <template #suffix>种</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="库存总值" :value="statistics.totalValue" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="预警数量" :value="statistics.warningCount">
            <template #suffix>项</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 查询表单
const queryForm = reactive({
  materialName: '',
  materialCode: '',
  warehouseId: null,
  stockStatus: null
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 统计数据
const statistics = computed(() => {
  return {
    totalItems: tableData.value.length,
    totalValue: tableData.value.reduce((sum, item) => sum + (item.stock * item.price), 0),
    warningCount: tableData.value.filter(item => item.stock <= item.minStock).length
  }
})

// 获取库存状态类名
const getStockClass = (row) => {
  if (row.stock === 0) return 'text-danger'
  if (row.stock <= row.minStock) return 'text-warning'
  return ''
}

// 获取库存状态类型
const getStockStatusType = (row) => {
  if (row.stock === 0) return 'danger'
  if (row.stock <= row.minStock) return 'warning'
  return 'success'
}

// 获取库存状态文本
const getStockStatusText = (row) => {
  if (row.stock === 0) return '缺货'
  if (row.stock <= row.minStock) return '低库存'
  return '正常'
}

// 查询数据
const handleQuery = () => {
  loading.value = true

  // TODO: 调用API获取数据
  // 模拟数据
  setTimeout(() => {
    tableData.value = [
      {
        id: 1,
        materialCode: 'GX001',
        materialName: '光缆12芯',
        category: '光缆类',
        spec: '12芯单模',
        unit: '条',
        warehouseName: '拉萨总仓',
        stock: 95,
        minStock: 100,
        price: 1500.00,
        updateTime: '2025-11-11 15:00:00'
      },
      {
        id: 2,
        materialCode: 'GX002',
        materialName: '光缆24芯',
        category: '光缆类',
        spec: '24芯多模',
        unit: '条',
        warehouseName: '拉萨总仓',
        stock: 150,
        minStock: 50,
        price: 2100.00,
        updateTime: '2025-11-11 10:30:00'
      },
      {
        id: 3,
        materialCode: 'JHJ001',
        materialName: '交换机H3C',
        category: '设备类',
        spec: 'S5130-28S',
        unit: '台',
        warehouseName: '拉萨总仓',
        stock: 25,
        minStock: 10,
        price: 7800.00,
        updateTime: '2025-11-10 16:20:00'
      },
      {
        id: 4,
        materialCode: 'PJ001',
        materialName: '光纤连接器',
        category: '配件类',
        spec: 'SC-UPC',
        unit: '个',
        warehouseName: '拉萨总仓',
        stock: 500,
        minStock: 200,
        price: 20.00,
        updateTime: '2025-11-11 15:00:00'
      },
      {
        id: 5,
        materialCode: 'PJ002',
        materialName: '网线',
        category: '配件类',
        spec: '超五类',
        unit: '米',
        warehouseName: '拉萨总仓',
        stock: 0,
        minStock: 500,
        price: 3.50,
        updateTime: '2025-11-05 10:00:00'
      }
    ]
    pagination.total = 5
    loading.value = false
  }, 500)
}

// 重置查询
const handleReset = () => {
  queryForm.materialName = ''
  queryForm.materialCode = ''
  queryForm.warehouseId = null
  queryForm.stockStatus = null
  pagination.page = 1
  handleQuery()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 查看流水记录
const handleViewLog = (row) => {
  ElMessage.info(`查看"${row.materialName}"的库存流水记录`)
  // TODO: 跳转到库存流水页面或打开对话框
}

// 初始化加载数据
handleQuery()
</script>

<style lang="scss" scoped>
.inventory-query-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }

  .text-danger {
    color: $error-color;
    font-weight: 500;
  }

  .text-warning {
    color: $warning-color;
    font-weight: 500;
  }
}
</style>
