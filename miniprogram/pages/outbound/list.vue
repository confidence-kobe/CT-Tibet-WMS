<template>
  <view class="outbound-list-container">
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-row">
        <picker
          mode="date"
          :value="filters.startDate"
          @change="onStartDateChange"
          class="date-picker"
        >
          <view class="picker-content">
            <text class="picker-label">开始日期</text>
            <text class="picker-value">{{ filters.startDate || '请选择' }}</text>
          </view>
        </picker>
        <text class="date-separator">~</text>
        <picker
          mode="date"
          :value="filters.endDate"
          @change="onEndDateChange"
          class="date-picker"
        >
          <view class="picker-content">
            <text class="picker-label">结束日期</text>
            <text class="picker-value">{{ filters.endDate || '请选择' }}</text>
          </view>
        </picker>
      </view>

      <view class="filter-row">
        <picker
          :range="sourceTypes"
          range-key="label"
          :value="selectedSourceIndex"
          @change="onSourceChange"
          class="source-picker"
        >
          <view class="picker-content">
            <text class="picker-label">来源</text>
            <text class="picker-value">{{ sourceTypes[selectedSourceIndex].label }}</text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>

        <picker
          :range="statusOptions"
          range-key="label"
          :value="selectedStatusIndex"
          @change="onStatusChange"
          class="status-picker"
        >
          <view class="picker-content">
            <text class="picker-label">状态</text>
            <text class="picker-value">{{ statusOptions[selectedStatusIndex].label }}</text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
      </view>

      <view class="filter-actions">
        <button class="reset-btn" @click="resetFilters">重置</button>
        <button class="search-btn" @click="handleSearch">查询</button>
      </view>
    </view>

    <!-- 列表 -->
    <scroll-view
      scroll-y
      class="outbound-list"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="outbounds.length === 0" class="empty-state">
        <text class="empty-icon">📤</text>
        <text class="empty-text">暂无出库记录</text>
      </view>

      <view v-else class="outbound-items">
        <view
          v-for="item in outbounds"
          :key="item.id"
          class="outbound-item"
          @click="viewDetail(item)"
        >
          <!-- 头部 -->
          <view class="item-header">
            <text class="outbound-no">{{ item.outboundNo }}</text>
            <view :class="['status-badge', `status-${item.status}`]">
              <text>{{ statusMap[item.status] }}</text>
            </view>
          </view>

          <!-- 来源标签 -->
          <view :class="['source-tag', `source-${item.source}`]">
            <text>{{ item.source === 1 ? '直接出库' : '申请出库' }}</text>
          </view>

          <!-- 信息 -->
          <view class="item-info">
            <view class="info-row">
              <text class="info-label">领用人</text>
              <text class="info-value">{{ item.receiverName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">联系电话</text>
              <text class="info-value">{{ item.receiverPhone }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">仓库</text>
              <text class="info-value">{{ item.warehouseName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">创建时间</text>
              <text class="info-value">{{ item.createTime }}</text>
            </view>
            <view v-if="item.status === 1" class="info-row">
              <text class="info-label">完成时间</text>
              <text class="info-value">{{ item.outboundTime }}</text>
            </view>
          </view>

          <!-- 统计 -->
          <view class="item-summary">
            <text class="summary-text">共 {{ item.materialCount }} 种物资</text>
            <text class="summary-divider">|</text>
            <text class="summary-text">总金额 ¥{{ item.totalAmount.toFixed(2) }}</text>
          </view>

          <!-- 待领取提示 -->
          <view v-if="item.status === 0 && item.waitDays >= 5" class="warning-tip">
            <text class="warning-icon">⚠️</text>
            <text class="warning-text">已等待 {{ item.waitDays }} 天，即将超时</text>
          </view>

          <!-- 备注 -->
          <view v-if="item.remark" class="item-remark">
            <text class="remark-label">备注：</text>
            <text class="remark-text">{{ item.remark }}</text>
          </view>
        </view>
      </view>

      <view v-if="hasMore" class="loading-more">
        <text>加载中...</text>
      </view>
      <view v-else-if="outbounds.length > 0" class="no-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 悬浮按钮 -->
    <view v-if="canCreateOutbound" class="fab" @click="goToCreate">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script>
import { $uRequest } from '@/utils/request.js'
import { mapState, mapGetters } from 'vuex'

export default {
  data() {
    return {
      filters: {
        startDate: '',
        endDate: '',
        source: null,
        status: null
      },
      sourceTypes: [
        { label: '全部来源', value: null },
        { label: '直接出库', value: 1 },
        { label: '申请出库', value: 2 }
      ],
      statusOptions: [
        { label: '全部状态', value: null },
        { label: '待领取', value: 0 },
        { label: '已完成', value: 1 },
        { label: '已取消', value: 2 }
      ],
      statusMap: {
        0: '待领取',
        1: '已完成',
        2: '已取消'
      },
      selectedSourceIndex: 0,
      selectedStatusIndex: 0,
      outbounds: [],
      page: 1,
      pageSize: 20,
      hasMore: true,
      refreshing: false,
      loading: false
    }
  },

  computed: {
    ...mapState(['userInfo']),
    ...mapGetters(['isWarehouse']),

    canCreateOutbound() {
      return this.isWarehouse || this.userInfo.roleCode === 'DEPT_ADMIN'
    }
  },

  methods: {
    async loadData(isRefresh = false) {
      if (this.loading && !isRefresh) return

      if (isRefresh) {
        this.page = 1
        this.hasMore = true
        this.outbounds = []
      }

      this.loading = true

      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          startDate: this.filters.startDate || undefined,
          endDate: this.filters.endDate || undefined,
          source: this.filters.source,
          status: this.filters.status
        }

        const res = await $uRequest({
          url: '/api/outbounds',
          method: 'GET',
          data: params
        })

        if (res.code === 200) {
          const newOutbounds = res.data.records || []

          if (isRefresh) {
            this.outbounds = newOutbounds
          } else {
            this.outbounds = [...this.outbounds, ...newOutbounds]
          }

          this.hasMore = newOutbounds.length === this.pageSize
        }
      } catch (err) {
        console.error('加载出库列表失败', err)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },

    onStartDateChange(e) {
      this.filters.startDate = e.detail.value
    },

    onEndDateChange(e) {
      this.filters.endDate = e.detail.value
    },

    onSourceChange(e) {
      this.selectedSourceIndex = e.detail.value
      this.filters.source = this.sourceTypes[e.detail.value].value
    },

    onStatusChange(e) {
      this.selectedStatusIndex = e.detail.value
      this.filters.status = this.statusOptions[e.detail.value].value
    },

    resetFilters() {
      this.filters = {
        startDate: '',
        endDate: '',
        source: null,
        status: null
      }
      this.selectedSourceIndex = 0
      this.selectedStatusIndex = 0
      this.handleSearch()
    },

    handleSearch() {
      this.loadData(true)
    },

    onRefresh() {
      this.refreshing = true
      this.loadData(true)
    },

    loadMore() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadData()
    },

    viewDetail(item) {
      uni.navigateTo({
        url: `/pages/outbound/detail?id=${item.id}`
      })
    },

    goToCreate() {
      uni.navigateTo({
        url: '/pages/outbound/create'
      })
    }
  },

  onLoad() {
    this.loadData(true)
  }
}
</script>

<style lang="scss" scoped>
.outbound-list-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.filter-bar {
  background-color: #ffffff;
  padding: 24rpx 32rpx;
  margin-bottom: 24rpx;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.date-picker,
.source-picker,
.status-picker {
  flex: 1;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 16rpx 24rpx;
}

.date-separator {
  font-size: 28rpx;
  color: #8c8c8c;
  padding: 0 8rpx;
}

.picker-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.picker-label {
  font-size: 24rpx;
  color: #8c8c8c;
  margin-right: 16rpx;
}

.picker-value {
  flex: 1;
  font-size: 26rpx;
  color: #262626;
  text-align: right;
}

.picker-arrow {
  font-size: 20rpx;
  color: #bfbfbf;
  margin-left: 8rpx;
}

.filter-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

.reset-btn,
.search-btn {
  flex: 1;
  height: 72rpx;
  border-radius: 36rpx;
  font-size: 28rpx;
  border: none;

  &::after {
    border: none;
  }
}

.reset-btn {
  background-color: #f0f0f0;
  color: #595959;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
}

.outbound-list {
  height: calc(100vh - 400rpx);
  padding: 0 32rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #8c8c8c;
}

.outbound-items {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.outbound-item {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.outbound-no {
  font-size: 30rpx;
  font-weight: 500;
  color: #262626;
}

.status-badge {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;

  &.status-0 {
    background-color: #fff7e6;
    color: #faad14;
  }

  &.status-1 {
    background-color: #f6ffed;
    color: #52c41a;
  }

  &.status-2 {
    background-color: #fff1f0;
    color: #f5222d;
  }
}

.source-tag {
  display: inline-block;
  padding: 6rpx 20rpx;
  border-radius: 24rpx;
  font-size: 22rpx;
  margin-bottom: 16rpx;

  &.source-1 {
    background-color: #e6f7ff;
    color: #1890ff;
  }

  &.source-2 {
    background-color: #f9f0ff;
    color: #722ed1;
  }
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 24rpx;
  color: #8c8c8c;
}

.info-value {
  font-size: 26rpx;
  color: #262626;
}

.item-summary {
  display: flex;
  align-items: center;
  padding: 16rpx;
  background-color: #fafafa;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.summary-text {
  font-size: 24rpx;
  color: #595959;
}

.summary-divider {
  margin: 0 16rpx;
  color: #d9d9d9;
}

.warning-tip {
  display: flex;
  align-items: center;
  padding: 12rpx 16rpx;
  background-color: #fff7e6;
  border-radius: 8rpx;
  margin-bottom: 16rpx;
}

.warning-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.warning-text {
  font-size: 24rpx;
  color: #faad14;
}

.item-remark {
  display: flex;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.remark-label {
  font-size: 24rpx;
  color: #8c8c8c;
  flex-shrink: 0;
}

.remark-text {
  flex: 1;
  font-size: 24rpx;
  color: #595959;
  line-height: 1.6;
}

.loading-more,
.no-more {
  text-align: center;
  padding: 32rpx 0;
  font-size: 24rpx;
  color: #bfbfbf;
}

.fab {
  position: fixed;
  bottom: 160rpx;
  right: 48rpx;
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;

  &:active {
    transform: scale(0.95);
  }
}

.fab-icon {
  font-size: 64rpx;
  color: #ffffff;
  line-height: 1;
}
</style>
