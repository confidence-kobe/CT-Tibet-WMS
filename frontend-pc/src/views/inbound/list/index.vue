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
            <el-option
              v-for="warehouse in warehouses"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
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
        <el-table-column prop="inboundNo" label="入库单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.inboundNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库名称" min-width="150">
          <template #default="{ row }">
            {{ row.warehouseName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="入库金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100">
          <template #default="{ row }">
            {{ row.operatorName || row.createBy || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="inboundTime" label="入库时间" width="160" />
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

    <!-- 注意：列表页不再使用详情对话框，直接跳转到详情页面 -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listInbounds } from '@/api/inbound'
import { listWarehouses } from '@/api/warehouse'

const router = useRouter()

// 仓库列表
const warehouses = ref([])

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

// 加载仓库列表
const loadWarehouses = async () => {
  try {
    const res = await listWarehouses({ status: 0 })
    if (res.code === 200) {
      warehouses.value = res.data || []
    }
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
      warehouseId: queryForm.warehouseId,
      keyword: queryForm.code || undefined,
      startDate: queryForm.dateRange ? queryForm.dateRange[0] : undefined,
      endDate: queryForm.dateRange ? queryForm.dateRange[1] : undefined
    }

    const res = await listInbounds(params)

    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (error) {
    console.error('查询入库单列表失败:', error)
    ElMessage.error('查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
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
  // 跳转到详情页
  router.push(`/inbound/detail/${row.id}`)
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

    // TODO: 后端暂未提供删除接口，需要后续添加
    ElMessage.warning('删除功能暂未开放')
    // 待后端添加接口后实现:
    // const res = await deleteInbound(row.id)
    // if (res.code === 200) {
    //   ElMessage.success('删除成功')
    //   handleQuery()
    // }
  } catch (error) {
    // 用户取消
  }
}

// 初始化
onMounted(() => {
  loadWarehouses()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.inbound-list-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }
}
</style>
