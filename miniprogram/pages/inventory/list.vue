<template>
  <view class="inventory-container">
    <!-- 搜索栏 -->
    <view class="search-header">
      <view class="search-bar">
        <input class="search-input" v-model="keyword" placeholder="搜索物资..." @confirm="handleSearch" />
        <text class="search-icon" @click="handleSearch">🔍</text>
      </view>
      <view class="filters">
        <picker mode="selector" :range="categories" @change="handleCategoryChange">
          <view class="filter-item">类别: {{ selectedCategory || '全部' }} ▼</view>
        </picker>
        <picker mode="selector" :range="statusOptions" :range-key="'label'" @change="handleStatusChange">
          <view class="filter-item">状态: {{ statusOptions[selectedStatusIndex].label }} ▼</view>
        </picker>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-bar">
      <text class="stats-text">总数 {{ summary.totalMaterials || 0 }}</text>
      <text class="stats-text success">正常 {{ summary.normalCount || 0 }}</text>
      <text class="stats-text warning">⚠️ 低库存 {{ summary.lowStockCount || 0 }}</text>
      <text class="stats-text danger">⛔ 缺货 {{ summary.outOfStockCount || 0 }}</text>
    </view>

    <!-- 列表 -->
    <scroll-view
      scroll-y
      class="scroll-content"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="list.length > 0" class="inventory-list">
        <view v-for="item in list" :key="item.id" class="inventory-card" @click="showDetail(item)">
          <view class="card-header">
            <text :class="['status-icon', `status-${item.stockStatus}`]">
              {{ item.stockStatus === 0 ? '✓' : item.stockStatus === 1 ? '⚠️' : '⛔' }}
            </text>
            <view class="material-info">
              <text class="material-name">{{ item.materialName }}</text>
              <text class="material-spec">{{ item.spec }}</text>
            </view>
          </view>
          <view class="card-body">
            <view class="stock-item">
              <text class="stock-label">库存:</text>
              <text class="stock-value">{{ item.quantity }} {{ item.unit }}</text>
            </view>
            <view class="stock-item">
              <text class="stock-label">锁定:</text>
              <text class="stock-value">{{ item.lockedQuantity }} {{ item.unit }}</text>
            </view>
            <view class="stock-item">
              <text class="stock-label">可用:</text>
              <text class="stock-value primary">{{ item.availableQuantity }} {{ item.unit }}</text>
            </view>
            <view class="stock-item">
              <text class="stock-label">预警值:</text>
              <text class="stock-value">{{ item.minStock }} {{ item.unit }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无库存数据</text>
      </view>

      <view v-if="list.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>

      <view class="safe-area-inset-bottom"></view>
    </scroll-view>

    <!-- 详情弹窗 -->
    <view v-if="showDetailModal" class="modal" @click="hideDetail">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">📦 {{ detailData.materialName }}</text>
          <text class="modal-close" @click="hideDetail">✕</text>
        </view>
        <view class="modal-body">
          <view class="detail-section">
            <text class="detail-label">物资编码:</text>
            <text class="detail-value">{{ detailData.materialCode }}</text>
          </view>
          <view class="detail-section">
            <text class="detail-label">规格型号:</text>
            <text class="detail-value">{{ detailData.spec }}</text>
          </view>
          <view class="detail-section">
            <text class="detail-label">单位:</text>
            <text class="detail-value">{{ detailData.unit }}</text>
          </view>
          <view class="divider"></view>
          <view class="detail-section">
            <text class="detail-label">当前库存:</text>
            <text class="detail-value bold">{{ detailData.quantity }} {{ detailData.unit }}</text>
          </view>
          <view class="detail-section">
            <text class="detail-label">锁定数量:</text>
            <text class="detail-value">{{ detailData.lockedQuantity }} {{ detailData.unit }}</text>
          </view>
          <view class="detail-section">
            <text class="detail-label">可用数量:</text>
            <text class="detail-value primary">{{ detailData.availableQuantity }} {{ detailData.unit }}</text>
          </view>
          <view class="detail-section">
            <text class="detail-label">预警阈值:</text>
            <text class="detail-value">{{ detailData.minStock }} {{ detailData.unit }}</text>
          </view>
          <view class="detail-section">
            <text class="detail-label">库存状态:</text>
            <text :class="['detail-value', getStatusClass(detailData.stockStatus)]">
              {{ getStatusText(detailData.stockStatus) }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { $uRequest } from '@/utils/request.js'

export default {
  data() {
    return {
      keyword: '',
      categories: ['全部'],
      selectedCategory: '全部',
      statusOptions: [
        { label: '全部', value: null },
        { label: '正常', value: 0 },
        { label: '低库存', value: 1 },
        { label: '缺货', value: 2 }
      ],
      selectedStatusIndex: 0,
      list: [],
      summary: {},
      loading: false,
      refreshing: false,
      pageNum: 1,
      pageSize: 20,
      noMore: false,
      showDetailModal: false,
      detailData: {}
    }
  },

  methods: {
    async loadData() {
      if (this.loading || this.noMore) return

      this.loading = true

      try {
        const res = await $uRequest({
          url: '/api/inventory',
          method: 'GET',
          data: {
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            category: this.selectedCategory === '全部' ? '' : this.selectedCategory,
            status: this.statusOptions[this.selectedStatusIndex].value,
            keyword: this.keyword
          }
        })

        if (res.code === 200) {
          const { list, summary, total } = res.data

          if (this.pageNum === 1) {
            this.list = list
            this.summary = summary || {}
          } else {
            this.list = this.list.concat(list)
          }

          if (this.list.length >= total) {
            this.noMore = true
          }
        }
      } catch (err) {
        console.error('加载数据失败', err)
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },

    async loadCategories() {
      try {
        const res = await $uRequest({
          url: '/api/materials/categories',
          method: 'GET'
        })

        if (res.code === 200) {
          this.categories = ['全部'].concat(res.data || [])
        }
      } catch (err) {
        console.error('加载类别失败', err)
      }
    },

    handleSearch() {
      this.pageNum = 1
      this.list = []
      this.noMore = false
      this.loadData()
    },

    handleCategoryChange(e) {
      this.selectedCategory = this.categories[e.detail.value]
      this.handleSearch()
    },

    handleStatusChange(e) {
      this.selectedStatusIndex = e.detail.value
      this.handleSearch()
    },

    onRefresh() {
      this.refreshing = true
      this.pageNum = 1
      this.list = []
      this.noMore = false
      this.loadData()
    },

    loadMore() {
      if (!this.loading && !this.noMore) {
        this.pageNum++
        this.loadData()
      }
    },

    showDetail(item) {
      this.detailData = item
      this.showDetailModal = true
    },

    hideDetail() {
      this.showDetailModal = false
    },

    getStatusClass(status) {
      return status === 0 ? 'success' : status === 1 ? 'warning' : 'danger'
    },

    getStatusText(status) {
      return status === 0 ? '✓ 正常' : status === 1 ? '⚠️ 低库存' : '⛔ 缺货'
    }
  },

  onLoad(options) {
    if (options.status) {
      this.selectedStatusIndex = parseInt(options.status) + 1
    }
    this.loadCategories()
    this.loadData()
  },

  onShow() {
    if (this.list.length > 0) {
      this.onRefresh()
    }
  }
}
</script>

<style lang="scss" scoped>
.inventory-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.search-header {
  background-color: #ffffff;
  padding: 24rpx 32rpx;
}

.search-bar {
  position: relative;
  margin-bottom: 16rpx;
}

.search-input {
  width: 100%;
  height: 64rpx;
  padding: 0 80rpx 0 24rpx;
  background-color: #f5f5f5;
  border-radius: 32rpx;
  font-size: 28rpx;
}

.search-icon {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 32rpx;
}

.filters {
  display: flex;
  gap: 16rpx;
}

.filter-item {
  flex: 1;
  height: 56rpx;
  padding: 0 24rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stats-bar {
  background-color: #ffffff;
  padding: 16rpx 32rpx;
  display: flex;
  justify-content: space-around;
  border-top: 1rpx solid #f0f0f0;
}

.stats-text {
  font-size: 24rpx;
  color: #262626;

  &.success { color: #52c41a; }
  &.warning { color: #faad14; }
  &.danger { color: #f5222d; }
}

.scroll-content {
  height: calc(100vh - 280rpx);
}

.inventory-list {
  padding: 24rpx 32rpx;
}

.inventory-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  margin-bottom: 24rpx;
}

.status-icon {
  font-size: 48rpx;
  margin-right: 16rpx;

  &.status-0 { color: #52c41a; }
  &.status-1 { color: #faad14; }
  &.status-2 { color: #f5222d; }
}

.material-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.material-name {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8rpx;
}

.material-spec {
  font-size: 24rpx;
  color: #8c8c8c;
}

.card-body {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}

.stock-item {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
}

.stock-label {
  font-size: 24rpx;
  color: #8c8c8c;
}

.stock-value {
  font-size: 24rpx;
  color: #262626;

  &.primary {
    color: #1890ff;
    font-weight: 500;
  }
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 80%;
  max-height: 70vh;
  background-color: #ffffff;
  border-radius: 16rpx;
}

.modal-header {
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
}

.modal-close {
  font-size: 40rpx;
  color: #8c8c8c;
}

.modal-body {
  padding: 32rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
}

.detail-label {
  font-size: 26rpx;
  color: #8c8c8c;
}

.detail-value {
  font-size: 26rpx;
  color: #262626;

  &.bold { font-weight: 500; }
  &.primary { color: #1890ff; }
  &.success { color: #52c41a; }
  &.warning { color: #faad14; }
  &.danger { color: #f5222d; }
}

.divider {
  height: 1rpx;
  background-color: #f0f0f0;
  margin: 16rpx 0;
}

.empty-state {
  padding: 200rpx 0;
  text-align: center;
}

.empty-icon {
  display: block;
  font-size: 160rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #8c8c8c;
}

.load-more {
  padding: 32rpx;
  text-align: center;
  font-size: 24rpx;
  color: #8c8c8c;
}
</style>
