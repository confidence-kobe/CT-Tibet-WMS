<template>
  <div class="inbound-list-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">入库单列表</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建入库
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="单据编号">
          <el-input
            v-model="queryForm.code"
            placeholder="请输入单据编号"
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
        <el-form-item label="创建日期">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleQuery"
            style="width: 240px"
          />
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
        <el-table-column prop="code" label="入库单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.code }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库名称" min-width="150" />
        <el-table-column prop="itemCount" label="物资种类" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.itemCount }}种</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="入库金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createUser" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="入库单详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="入库单号">
          {{ currentRow.code }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库名称">
          {{ currentRow.warehouseName }}
        </el-descriptions-item>
        <el-descriptions-item label="创建人">
          {{ currentRow.createUser }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ currentRow.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="物资种类">
          <el-tag>{{ currentRow.itemCount }}种</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入库金额">
          <span class="amount">¥{{ currentRow.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentRow.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">入库明细</el-divider>

      <el-table :data="currentRow.details" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="spec" label="规格型号" width="120" />
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column prop="price" label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 查询表单
const queryForm = reactive({
  code: '',
  warehouseId: null,
  dateRange: null
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

// 对话框
const detailVisible = ref(false)

// 当前行数据
const currentRow = ref({
  details: []
})

// 查询数据
const handleQuery = () => {
  loading.value = true

  // TODO: 调用API获取数据
  // 模拟数据
  setTimeout(() => {
    tableData.value = [
      {
        id: 1,
        code: 'RK202511110001',
        warehouseName: '拉萨总仓',
        itemCount: 3,
        totalAmount: 11200.00,
        createUser: '张三',
        createTime: '2025-11-11 10:30:00',
        remark: '月度采购入库',
        details: [
          {
            materialCode: 'GX001',
            materialName: '光缆12芯',
            spec: '12芯单模',
            quantity: 5,
            price: 1500.00,
            amount: 7500.00
          },
          {
            materialCode: 'JHJ001',
            materialName: '交换机H3C',
            spec: 'S5130-28S',
            quantity: 1,
            price: 3700.00,
            amount: 3700.00
          }
        ]
      },
      {
        id: 2,
        code: 'RK202511100002',
        warehouseName: '日喀则分仓',
        itemCount: 2,
        totalAmount: 8300.00,
        createUser: '李四',
        createTime: '2025-11-10 14:20:00',
        remark: '紧急补货',
        details: [
          {
            materialCode: 'GX002',
            materialName: '光缆24芯',
            spec: '24芯多模',
            quantity: 3,
            price: 2100.00,
            amount: 6300.00
          },
          {
            materialCode: 'PJ001',
            materialName: '光纤连接器',
            spec: 'SC-UPC',
            quantity: 100,
            price: 20.00,
            amount: 2000.00
          }
        ]
      }
    ]
    pagination.total = 2
    loading.value = false
  }, 500)
}

// 重置查询
const handleReset = () => {
  queryForm.code = ''
  queryForm.warehouseId = null
  queryForm.dateRange = null
  pagination.page = 1
  handleQuery()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 新建
const handleCreate = () => {
  router.push('/inbound/create')
}

// 查看详情
const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除入库单"${row.code}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用删除API
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error) {
    // 用户取消
  }
}

// 初始化加载数据
handleQuery()
</script>

<style lang="scss" scoped>
.inbound-list-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }
}
</style>
