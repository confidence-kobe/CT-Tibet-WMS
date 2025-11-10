<template>
  <div class="outbound-list-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">出库单列表</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        直接出库
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
        <el-form-item label="出库类型">
          <el-select
            v-model="queryForm.source"
            placeholder="请选择"
            clearable
            @clear="handleQuery"
            style="width: 140px"
          >
            <el-option label="直接出库" :value="1" />
            <el-option label="申请出库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            placeholder="请选择"
            clearable
            @clear="handleQuery"
            style="width: 140px"
          >
            <el-option label="待领取" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已取消" :value="2" />
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
        <el-table-column prop="code" label="出库单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.code }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库名称" min-width="150" />
        <el-table-column prop="source" label="出库类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.source === 1 ? 'success' : 'info'" size="small">
              {{ row.source === 1 ? '直接出库' : '申请出库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="物资种类" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.itemCount }}种</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="出库金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'"
              size="small"
            >
              {{ ['待领取', '已完成', '已取消'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiver" label="领取人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 0"
              link
              type="success"
              size="small"
              @click="handleConfirm(row)"
            >
              确认领取
            </el-button>
            <el-button
              v-if="row.status === 0"
              link
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消
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
      title="出库单详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="出库单号">
          {{ currentRow.code }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库名称">
          {{ currentRow.warehouseName }}
        </el-descriptions-item>
        <el-descriptions-item label="出库类型">
          <el-tag :type="currentRow.source === 1 ? 'success' : 'info'">
            {{ currentRow.source === 1 ? '直接出库' : '申请出库' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag
            :type="currentRow.status === 0 ? 'warning' : currentRow.status === 1 ? 'success' : 'info'"
          >
            {{ ['待领取', '已完成', '已取消'][currentRow.status] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="领取人">
          {{ currentRow.receiver }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ currentRow.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="物资种类">
          <el-tag>{{ currentRow.itemCount }}种</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出库金额">
          <span class="amount">¥{{ currentRow.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentRow.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">出库明细</el-divider>

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
  source: null,
  status: null
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
        code: 'CK202511110001',
        warehouseName: '拉萨总仓',
        source: 1, // 1=直接出库, 2=申请出库
        itemCount: 2,
        totalAmount: 280.00,
        status: 0, // 0=待领取, 1=已完成, 2=已取消
        receiver: '王五',
        createTime: '2025-11-11 15:00:00',
        remark: '紧急领料',
        details: [
          {
            materialCode: 'PJ001',
            materialName: '光纤连接器',
            spec: 'SC-UPC',
            quantity: 10,
            price: 20.00,
            amount: 200.00
          },
          {
            materialCode: 'PJ002',
            materialName: '网线',
            spec: '超五类',
            quantity: 20,
            price: 4.00,
            amount: 80.00
          }
        ]
      },
      {
        id: 2,
        code: 'CK202511100002',
        warehouseName: '拉萨总仓',
        source: 2, // 申请出库
        itemCount: 1,
        totalAmount: 1500.00,
        status: 1, // 已完成
        receiver: '赵六',
        createTime: '2025-11-10 09:30:00',
        remark: '项目使用',
        details: [
          {
            materialCode: 'GX001',
            materialName: '光缆12芯',
            spec: '12芯单模',
            quantity: 1,
            price: 1500.00,
            amount: 1500.00
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
  queryForm.source = null
  queryForm.status = null
  pagination.page = 1
  handleQuery()
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 新建直接出库
const handleCreate = () => {
  router.push('/outbound/create')
}

// 查看详情
const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

// 确认领取
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认"${row.receiver}"已领取物资吗？确认后将扣减库存。`,
      '确认领取',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用确认领取API
    ElMessage.success('确认领取成功，库存已扣减')
    handleQuery()
  } catch (error) {
    // 用户取消
  }
}

// 取消出库
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消出库单"${row.code}"吗？`,
      '取消出库',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用取消API
    ElMessage.success('取消成功')
    handleQuery()
  } catch (error) {
    // 用户取消
  }
}

// 初始化加载数据
handleQuery()
</script>

<style lang="scss" scoped>
.outbound-list-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }
}
</style>
