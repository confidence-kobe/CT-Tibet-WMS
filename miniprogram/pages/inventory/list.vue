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
import api from '@/api'

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
        const filters = {
          category: this.selectedCategory === '全部' ? '' : this.selectedCategory,
          keyword: this.keyword
        }
        const res = await api.inventory.getList({
          ...filters,
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          stockStatus: this.statusOptions[this.selectedStatusIndex].value
        })

        if (res.code === 200) {
          const { list, total } = res.data

          if (this.pageNum === 1) {
            this.list = list
            // 汇总不受"状态"筛选影响，便于对比各状态数量
            this.loadSummary(filters)
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

    async loadSummary(filters) {
      try {
        const res = await api.inventory.getSummary(filters)
        if (res.code === 200) {
          this.summary = res.data || {}
        }
      } catch (err) {
        console.error('加载库存汇总失败', err)
      }
    },

    async loadCategories() {
      try {
        const res = await api.common.getCategories()

        if (res.code === 200) {
          this.categories = ['全部'].concat(res.data || [])
        }
      } catch (err) {
        console.error('加载类别失败', err)
      }
    },

    handleSearch() {
      // 不先清空列表：第1页数据返回后整体替换，避免闪现"暂无数据"
      this.pageNum = 1
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

  onLoad() {
    this.loadCategories()
  },

  // 数据统一在 onShow 加载（首次进入也会触发），避免与 onLoad 重复请求
  onShow() {
    // 从首页"库存预警"跳转时带入的筛选条件（TabBar 页面无法通过 URL 传参）
    const presetStatus = uni.getStorageSync('inventoryListStatus')
    if (presetStatus !== '' && presetStatus !== null && presetStatus !== undefined) {
      uni.removeStorageSync('inventoryListStatus')
      const index = this.statusOptions.findIndex(option => option.value === Number(presetStatus))
      this.selectedStatusIndex = index >= 0 ? index : 0
    }
    this.handleSearch()
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/design-system.scss";

.inventory-container {
  min-height: 100vh;
  @include gradient-mesh;
  background-color: $bg-secondary;
}

.search-header {
  background: $bg-primary;
  padding: $spacing-md $spacing-lg;
  box-shadow: $shadow-sm;
  position: relative;

  // 顶部渐变装饰线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4rpx;
    @include gradient-primary;
  }
}

.search-bar {
  position: relative;
  margin-bottom: $spacing-sm;
}

.search-input {
  @include input-base;
  width: 100%;
  height: 72rpx;
  padding: 0 96rpx 0 $spacing-lg;
  border-radius: $radius-full;
  font-size: $font-size-base;
  box-shadow: $shadow-sm;
}

.search-icon {
  position: absolute;
  right: $spacing-lg;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40rpx;
  @include smooth-transition(transform);

  &:active {
    transform: translateY(-50%) scale(0.9);
  }
}

.filters {
  display: flex;
  gap: $spacing-sm;
}

.filter-item {
  flex: 1;
  height: 64rpx;
  padding: 0 $spacing-md;
  background: linear-gradient(135deg, $bg-secondary 0%, $gray-100 100%);
  border-radius: $radius-lg;
  font-size: $font-size-sm;
  color: $primary;
  font-weight: $font-weight-medium;
  @include flex-center;
  border: 2rpx solid rgba($primary, 0.2);
  @include smooth-transition(all);

  &:active {
    background: rgba($primary, 0.1);
    transform: scale(0.96);
  }
}

.stats-bar {
  background: $bg-primary;
  padding: $spacing-md $spacing-lg;
  display: flex;
  justify-content: space-around;
  gap: $spacing-sm;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.stats-text {
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: $font-weight-medium;
  padding: $spacing-xs $spacing-sm;
  border-radius: $radius-md;
  background: $bg-secondary;
  @include smooth-transition(all);

  &:active {
    transform: scale(0.95);
  }

  &.success {
    color: $success;
    background: $success-light;
  }

  &.warning {
    color: $warning;
    background: $warning-light;
  }

  &.danger {
    color: $error;
    background: $error-light;
  }
}

.scroll-content {
  height: calc(100vh - 280rpx);
}

.inventory-list {
  padding: $spacing-md $spacing-lg;
}

.inventory-card {
  @include card-base;
  margin-bottom: $spacing-md;
  position: relative;
  overflow: hidden;
  @include fade-in;

  // 依次出现动画
  @for $i from 1 through 10 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.05}s;
    }
  }

  // 左侧状态彩条
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4rpx;
    @include smooth-transition(width);
  }

  &:active {
    transform: scale(0.98);

    &::before {
      width: 8rpx;
    }
  }
}

.card-header {
  display: flex;
  margin-bottom: $spacing-md;
  align-items: flex-start;
}

.status-icon {
  font-size: 56rpx;
  margin-right: $spacing-md;
  @include float-animation;

  &.status-0 {
    color: $success;
    filter: drop-shadow(0 2rpx 4rpx rgba($success, 0.3));
  }

  &.status-1 {
    color: $warning;
    filter: drop-shadow(0 2rpx 4rpx rgba($warning, 0.3));
  }

  &.status-2 {
    color: $error;
    filter: drop-shadow(0 2rpx 4rpx rgba($error, 0.3));
  }
}

.material-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.material-name {
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  @include text-ellipsis;
}

.material-spec {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: $line-height-normal;
}

// 根据状态设置卡片左侧彩条
.inventory-card:has(.status-0)::before {
  @include gradient-success;
}

.inventory-card:has(.status-1)::before {
  @include gradient-warning;
}

.inventory-card:has(.status-2)::before {
  background: $error;
}

.card-body {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-sm;
  background: linear-gradient(135deg, $bg-secondary 0%, $gray-50 100%);
  padding: $spacing-md;
  border-radius: $radius-md;
  margin: 0 (-$spacing-lg);
  margin-bottom: -$spacing-lg;
}

.stock-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm 0;
}

.stock-label {
  font-size: $font-size-sm;
  color: $text-tertiary;
  font-weight: $font-weight-medium;
}

.stock-value {
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: $font-weight-semibold;

  &.primary {
    background: $primary-gradient;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    font-size: $font-size-lg;
  }
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8rpx);
  -webkit-backdrop-filter: blur(8rpx);
  @include flex-center;
  z-index: 1000;
  @include fade-in;
}

.modal-content {
  width: 85%;
  max-height: 75vh;
  @include glass-card;
  @include scale-in;
  box-shadow: $shadow-2xl;
}

.modal-header {
  padding: $spacing-xl;
  @include flex-between;
  border-bottom: 2rpx solid $gray-100;
  background: linear-gradient(135deg, rgba($primary, 0.05) 0%, transparent 100%);
}

.modal-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.modal-close {
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  font-size: 48rpx;
  color: $text-tertiary;
  border-radius: $radius-full;
  @include smooth-transition(all);

  &:active {
    background: $gray-100;
    color: $text-primary;
    transform: rotate(90deg);
  }
}

.modal-body {
  padding: $spacing-xl;
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  @include flex-between;
  padding: $spacing-md 0;
  @include smooth-transition(padding);

  &:active {
    padding-left: $spacing-sm;
  }
}

.detail-label {
  font-size: $font-size-sm;
  color: $text-tertiary;
  font-weight: $font-weight-medium;
}

.detail-value {
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: $font-weight-medium;

  &.bold {
    font-weight: $font-weight-bold;
    font-size: $font-size-base;
  }

  &.primary {
    background: $primary-gradient;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  &.success { color: $success; }
  &.warning { color: $warning; }
  &.danger { color: $error; }
}

.divider {
  @include divider($spacing-sm);
}

.empty-state {
  @include flex-center;
  flex-direction: column;
  padding: $spacing-3xl 0;
  opacity: 0.8;
}

.empty-icon {
  display: block;
  font-size: 160rpx;
  margin-bottom: $spacing-lg;
  filter: grayscale(50%);
  @include float-animation;
}

.empty-text {
  font-size: $font-size-lg;
  color: $text-secondary;
  font-weight: $font-weight-medium;
}

.load-more {
  padding: $spacing-xl;
  text-align: center;
  font-size: $font-size-sm;
  color: $text-tertiary;
}
</style>
