<template>
  <div class="apply-list-container page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">我的申请</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建申请
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <el-form-item label="申请单号">
          <el-input
            v-model="queryForm.code"
            placeholder="请输入申请单号"
            clearable
            @clear="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            placeholder="请选择"
            clearable
            @clear="handleQuery"
            style="width: 140px"
          >
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请日期">
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
        <el-table-column prop="code" label="申请单号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.code }}</el-link>
          </template>
        </el-table-column>
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
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column prop="approveTime" label="审批时间" width="160">
          <template #default="{ row }">
            {{ row.approveTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="approver" label="审批人" width="100">
          <template #default="{ row }">
            {{ row.approver || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="申请原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 0"
              link
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消申请
            </el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="success"
              size="small"
              @click="handlePickup(row)"
            >
              去领取
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
      title="申请详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请单号">
          {{ currentRow.code }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRow.status)">
            {{ getStatusText(currentRow.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ currentRow.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="审批时间">
          {{ currentRow.approveTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="物资种类">
          <el-tag>{{ currentRow.itemCount }}种</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请金额">
          <span class="amount">¥{{ currentRow.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="审批人">
          {{ currentRow.approver || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审批意见">
          {{ currentRow.approvalOpinion || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">
          {{ currentRow.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">申请明细</el-divider>

      <el-table :data="currentRow.details" border stripe style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="materialCode" label="物资编码" width="120" />
        <el-table-column prop="materialName" label="物资名称" min-width="150" />
        <el-table-column prop="spec" label="规格型号" width="120" />
        <el-table-column prop="quantity" label="申请数量" width="90" align="right" />
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
  status: null,
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

// 获取状态类型
const getStatusType = (status) => {
  const types = ['warning', 'success', 'danger', '', 'info']
  return types[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = ['待审批', '已通过', '已拒绝', '已完成', '已取消']
  return texts[status] || '未知'
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
        code: 'SQ202511110001',
        itemCount: 2,
        totalAmount: 280.00,
        status: 0, // 0=待审批, 1=已通过, 2=已拒绝, 3=已完成, 4=已取消
        createTime: '2025-11-11 10:00:00',
        approveTime: null,
        approver: null,
        approvalOpinion: null,
        remark: '项目施工需要',
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
        code: 'SQ202511100002',
        itemCount: 1,
        totalAmount: 1500.00,
        status: 1, // 已通过
        createTime: '2025-11-10 14:30:00',
        approveTime: '2025-11-10 15:00:00',
        approver: '张三',
        approvalOpinion: '同意',
        remark: '线路维修急需',
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
      },
      {
        id: 3,
        code: 'SQ202511090003',
        itemCount: 1,
        totalAmount: 7800.00,
        status: 2, // 已拒绝
        createTime: '2025-11-09 09:00:00',
        approveTime: '2025-11-09 10:30:00',
        approver: '张三',
        approvalOpinion: '暂无采购预算',
        remark: '办公室需要',
        details: [
          {
            materialCode: 'JHJ001',
            materialName: '交换机H3C',
            spec: 'S5130-28S',
            quantity: 1,
            price: 7800.00,
            amount: 7800.00
          }
        ]
      }
    ]
    pagination.total = 3
    loading.value = false
  }, 500)
}

// 重置查询
const handleReset = () => {
  queryForm.code = ''
  queryForm.status = null
  queryForm.dateRange = null
  pagination.page = 1
  handleQuery()
}

// 新建申请
const handleCreate = () => {
  router.push('/apply/create')
}

// 查看详情
const handleView = (row) => {
  currentRow.value = row
  detailVisible.value = true
}

// 取消申请
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消申请"${row.code}"吗？`,
      '取消确认',
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

// 去领取
const handlePickup = (row) => {
  ElMessage.info('请联系仓管员确认领取物资')
  // TODO: 可以跳转到具体的领取页面或联系信息页面
}

// 初始化加载数据
handleQuery()
</script>

<style lang="scss" scoped>
.apply-list-container {
  .amount {
    color: $error-color;
    font-weight: 500;
  }
}
</style>
