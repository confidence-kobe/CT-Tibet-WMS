#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script to update outbound pages with backend API integration
"""

import os
import sys

# Get the base directory
BASE_DIR = "H:/java/CT-Tibet-WMS/frontend-pc/src/views/outbound"

# Outbound List Page Content
OUTBOUND_LIST_CONTENT = '''<template>
  <div class="outbound-list-container page-container">
    <!-- Page header -->
    <div class="page-header">
      <h2 class="page-title">出库单列表</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        直接出库
      </el-button>
    </div>

    <!-- Search form -->
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
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
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

    <!-- Data table -->
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
            <el-link type="primary" @click="handleViewDetail(row)">{{ row.code }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseName" label="仓库名称" min-width="150" />
        <el-table-column prop="source" label="出库来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.source === 1 ? 'success' : 'primary'" size="small">
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
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
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
              @click="handleCancelOutbound(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOutbounds, confirmOutbound, cancelOutbound } from '@/api/outbound'
import { listWarehouses } from '@/api/warehouse'

const router = useRouter()

// Warehouse list
const warehouseList = ref([])

// Query form
const queryForm = reactive({
  keyword: '',
  warehouseId: null,
  outboundType: null,
  status: null,
  startDate: '',
  endDate: ''
})

// Date range
const dateRange = ref(null)

// Pagination params
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// Table data
const tableData = ref([])
const loading = ref(false)

// Load warehouses
const loadWarehouses = async () => {
  try {
    const response = await listWarehouses({})
    if (response.code === 200 && response.data) {
      warehouseList.value = response.data
    }
  } catch (error) {
    console.error('Failed to load warehouses:', error)
  }
}

// Handle date range change
const handleDateRangeChange = (value) => {
  if (value && value.length === 2) {
    queryForm.startDate = value[0]
    queryForm.endDate = value[1]
  } else {
    queryForm.startDate = ''
    queryForm.endDate = ''
  }
  handleQuery()
}

// Query data
const handleQuery = async () => {
  loading.value = true

  try {
    // Build query params
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      keyword: queryForm.keyword || undefined,
      warehouseId: queryForm.warehouseId || undefined,
      outboundType: queryForm.outboundType || undefined,
      status: queryForm.status || undefined,
      startDate: queryForm.startDate || undefined,
      endDate: queryForm.endDate || undefined
    }

    const response = await listOutbounds(params)

    if (response.code === 200 && response.data) {
      // Map backend fields to frontend
      tableData.value = (response.data.list || []).map(item => ({
        id: item.id,
        code: item.outboundNo,
        warehouseName: item.warehouseName,
        source: item.source,
        itemCount: item.details?.length || 0,
        totalAmount: item.totalAmount,
        status: item.status,
        receiver: item.receiverName,
        createTime: item.createTime,
        remark: item.remark,
        details: item.details || []
      }))

      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.msg || 'Query failed')
    }
  } catch (error) {
    console.error('Failed to query outbounds:', error)
    ElMessage.error('Query failed, please try again later')
  } finally {
    loading.value = false
  }
}

// Reset query
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.warehouseId = null
  queryForm.outboundType = null
  queryForm.status = null
  queryForm.startDate = ''
  queryForm.endDate = ''
  dateRange.value = null
  pagination.page = 1
  handleQuery()
}

// Export
const handleExport = () => {
  ElMessage.info('Export feature in development')
}

// Create direct outbound
const handleCreate = () => {
  router.push('/outbound/create')
}

// View detail
const handleViewDetail = (row) => {
  router.push(`/outbound/detail/${row.id}`)
}

// Confirm pickup
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      `Confirm that "${row.receiver}" has picked up the materials? Inventory will be deducted.`,
      'Confirm Pickup',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )

    await confirmOutbound(row.id)
    ElMessage.success('Pickup confirmed, inventory deducted')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to confirm pickup:', error)
      ElMessage.error(error.msg || 'Confirmation failed')
    }
  }
}

// Cancel outbound
const handleCancelOutbound = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `Are you sure to cancel outbound order "${row.code}"?`,
      'Cancel Outbound',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        inputPlaceholder: 'Please enter cancellation reason',
        inputValidator: (value) => {
          if (!value) {
            return 'Please enter cancellation reason'
          }
          return true
        }
      }
    )

    await cancelOutbound(row.id, reason)
    ElMessage.success('Cancellation successful')
    handleQuery()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to cancel outbound:', error)
      ElMessage.error(error.msg || 'Cancellation failed')
    }
  }
}

// Initialize
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
'''

def update_file(filepath, content):
    """Update a file with new content"""
    try:
        # Create backup
        backup_path = f"{filepath}.bak"
        if os.path.exists(filepath):
            with open(filepath, 'r', encoding='utf-8') as f:
                backup_content = f.read()
            with open(backup_path, 'w', encoding='utf-8') as f:
                f.write(backup_content)
            print(f"✓ Created backup: {backup_path}")

        # Write new content
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"✓ Updated: {filepath}")
        return True
    except Exception as e:
        print(f"✗ Failed to update {filepath}: {e}")
        return False

def main():
    print("=== Updating Outbound Pages ===\n")

    # Update List Page
    list_path = os.path.join(BASE_DIR, "list", "index.vue")
    if update_file(list_path, OUTBOUND_LIST_CONTENT):
        print("✓ Outbound List Page updated successfully\n")
    else:
        print("✗ Failed to update Outbound List Page\n")

    print("\n=== Update Complete ===")
    print("\nNote: Only the List page has been updated in this script.")
    print("The remaining 3 pages (Create, Detail, Confirm) need similar updates.")
    print("Please refer to the OUTBOUND_API_INTEGRATION.md for guidance.")

if __name__ == "__main__":
    main()
