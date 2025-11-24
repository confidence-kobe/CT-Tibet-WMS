<template>
  <div class="approval-approved-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">已审批申请</h2>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="申请单号">
          <el-input
            v-model="queryForm.keyword"
            placeholder="请输入申请单号"
            clearable
            @clear="handleQuery"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select
            v-model="queryForm.warehouseId"
            placeholder="请选择仓库"
            clearable
            @clear="handleQuery"
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
        <el-form-item label="审批结果">
          <el-select
            v-model="queryForm.approvalStatus"
            placeholder="请选择"
            clearable
            @clear="handleQuery"
            style="width: 140px"
          >
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批日期">
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
        <el-table-column prop="applyNo" label="申请单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.applyNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="itemCount" label="物资种类" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.itemCount }}种</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="申请金额(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="审批结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '已通过' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approverName" label="审批人" width="100" />
        <el-table-column prop="approvalTime" label="审批时间" width="160" />
        <el-table-column prop="rejectReason" label="拒绝理由" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.status === 2 ? row.rejectReason : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 16px; text-align: right;">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getApprovedApplies, getApplyById } from '@/api/apply'
import { listWarehouses } from '@/api/warehouse'

const router = useRouter()

// 查询表单
const queryForm = reactive({
  keyword: '',
  warehouseId: null,
  approvalStatus: null,
  dateRange: null
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 仓库列表
const warehouseList = ref([])

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
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: queryForm.keyword || undefined,
      warehouseId: queryForm.warehouseId || undefined,
      approvalStatus: queryForm.approvalStatus != null ? queryForm.approvalStatus : undefined,
      startDate: queryForm.dateRange?.[0] || undefined,
      endDate: queryForm.dateRange?.[1] || undefined
    }

    const res = await getApprovedApplies(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.warehouseId = null
  queryForm.approvalStatus = null
  queryForm.dateRange = null
  pagination.pageNum = 1
  handleQuery()
}

// 查看详情
const handleView = (row) => {
  router.push({ path: `/apply/detail/${row.id}` })
}

// 初始化
onMounted(() => {
  loadWarehouses()
  handleQuery()
})
</script>

<style lang="scss" scoped>
.approval-approved-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }

  .reject-reason {
    color: $error-color;
  }
}
</style>
