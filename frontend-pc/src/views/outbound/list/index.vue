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
            v-model="queryForm.keyword"
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
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="出库类型">
          <el-select
            v-model="queryForm.outboundType"
            placeholder="请选择"
            clearable
            @clear="handleQuery"
            style="width: 140px"
          >
            <el-option label="生产领用" :value="1" />
            <el-option label="维修领用" :value="2" />
            <el-option label="项目使用" :value="3" />
            <el-option label="其他出库" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
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
        <el-table-column prop="outboundNo" label="出库单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.outboundNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库名称" min-width="150" />
        <el-table-column prop="outboundType" label="出库类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">
              {{ ['', '生产领用', '维修领用', '项目使用', '其他出库'][row.outboundType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.source === 1 ? 'success' : 'info'" size="small">
              {{ row.source === 1 ? '直接出库' : '申请出库' }}
            </el-tag>
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
        <el-table-column prop="receiverName" label="领取人" width="100" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="outboundTime" label="出库时间" width="160" />
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

    <!-- 详情对话框（简化版，完整详情请跳转详情页） -->
    <el-dialog
      v-model="detailVisible"
      title="出库单详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="出库单号">
          {{ currentRow.outboundNo }}
        </el-descriptions-item>
        <el-descriptions-item label="仓库名称">
          {{ currentRow.warehouseName }}
        </el-descriptions-item>
        <el-descriptions-item label="来源">
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
          {{ currentRow.receiverName }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ currentRow.operatorName }}
        </el-descriptions-item>
        <el-descriptions-item label="出库时间">
          {{ currentRow.outboundTime }}
        </el-descriptions-item>
        <el-descriptions-item label="出库金额">
          <span class="amount">¥{{ currentRow.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentRow.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleView(currentRow)">查看完整详情</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOutbounds, confirmOutbound, cancelOutbound } from '@/api/outbound'
import { listWarehouses } from '@/api/warehouse'

const router = useRouter()

// 查询表单
const queryForm = reactive({
  keyword: '',
  warehouseId: null,
  outboundType: null,
  source: null,
  status: null,
  startDate: '',
  endDate: ''
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

// 仓库列表
const warehouseList = ref([])

// 对话框
const detailVisible = ref(false)

// 当前行数据
const currentRow = ref({
  details: []
})

// 加载仓库列表
const loadWarehouses = async () => {
  try {
    const res = await listWarehouses({ status: 0 })
    warehouseList.value = res.data || []
  } catch (error) {
    console.error('加载仓库列表失败:', error)
  }
}

// 查询数据
const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      keyword: queryForm.keyword || undefined,
      warehouseId: queryForm.warehouseId || undefined,
      outboundType: queryForm.outboundType || undefined,
      source: queryForm.source || undefined,
      status: queryForm.status || undefined,
      startDate: queryForm.startDate || undefined,
      endDate: queryForm.endDate || undefined
    }

    const res = await listOutbounds(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('查询出库单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.warehouseId = null
  queryForm.outboundType = null
  queryForm.source = null
  queryForm.status = null
  queryForm.startDate = ''
  queryForm.endDate = ''
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
  router.push(`/outbound/detail/${row.id}`)
}

// 确认领取
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认"${row.receiverName}"已领取物资吗？确认后将扣减库存。`,
      '确认领取',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await confirmOutbound(row.id)
    ElMessage.success('确认领取成功，库存已扣减')
    handleQuery()
  } catch (error) {
    // 用户取消或接口错误
  }
}

// 取消出库
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消出库单"${row.outboundNo}"吗？`,
      '取消出库',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await cancelOutbound(row.id, '手动取消')
    ElMessage.success('取消成功')
    handleQuery()
  } catch (error) {
    // 用户取消或接口错误
  }
}

// 初始化
onMounted(() => {
  loadWarehouses()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.outbound-list-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }
}
</style>
