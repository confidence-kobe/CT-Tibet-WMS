<template>
  <view class="pending-container">
    <!-- 统计信息 -->
    <view class="stats-header">
      <text class="stats-title">待领取出库</text>
      <text class="stats-count">{{ pendingCount }}</text>
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
      <view v-if="list.length > 0" class="outbound-list">
        <view
          v-for="item in list"
          :key="item.id"
          :class="['outbound-card', highlightId == item.id ? 'highlight' : '']"
        >
          <view class="card-header">
            <view class="receiver-info">
              <text class="receiver-name">📦 {{ item.receiverName }}的出库单</text>
              <text class="receiver-phone">{{ item.receiverPhone }}</text>
            </view>
            <text v-if="item.waitDays >= 5" class="warning-tag">⚠️ 已等待{{ item.waitDays }}天</text>
          </view>

          <view class="card-body">
            <view class="info-row">
              <text class="info-label">出库单号:</text>
              <text class="info-value">{{ item.outboundNo }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">创建时间:</text>
              <text class="info-value">{{ item.createTime }}</text>
            </view>
            <view v-if="item.applyNo" class="info-row">
              <text class="info-label">来源申请:</text>
              <text class="info-value">{{ item.applyNo }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">用途:</text>
              <text class="info-value">{{ item.purpose }}</text>
            </view>

            <view class="materials-section">
              <text class="materials-title">出库物资:</text>
              <view class="materials-list">
                <view v-for="(detail, index) in item.details" :key="index" class="material-item">
                  <text class="material-name">{{ detail.materialName }}</text>
                  <text class="material-quantity">{{ detail.quantity }}{{ detail.unit }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="card-footer">
            <button class="action-btn" @click="handleCancel(item)">取消出库</button>
            <button class="action-btn primary" @click="handleConfirm(item)">确认出库</button>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无待领取出库单</text>
      </view>

      <view v-if="list.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>

      <view class="safe-area-inset-bottom"></view>
    </scroll-view>

    <!-- 确认出库弹窗 -->
    <view v-if="showConfirmModal" class="modal" @click="hideConfirmModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">确认出库</text>
          <text class="modal-close" @click="hideConfirmModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="confirm-info">
            <text class="confirm-icon">📦</text>
            <text class="confirm-title">请确认员工已到场领取</text>
          </view>

          <view class="outbound-info">
            <text class="info-text">领用人: {{ currentOutbound.receiverName }}</text>
            <text class="info-text">出库单号: {{ currentOutbound.outboundNo }}</text>
          </view>

          <view class="material-summary">
            <text class="summary-title">出库物资:</text>
            <view v-for="(item, index) in currentOutbound.details" :key="index" class="summary-item">
              <text class="summary-name">• {{ item.materialName }}</text>
              <text class="summary-quantity">{{ item.quantity }}{{ item.unit }}</text>
            </view>
          </view>

          <view class="warning-box">
            <text class="warning-icon">⚠️</text>
            <text class="warning-text">确认后将立即扣减库存，请确保物资已被领用人签收</text>
          </view>

          <view class="checkbox-group">
            <checkbox-group @change="handleCheckboxChange">
              <label class="checkbox-label">
                <checkbox value="confirmed" :checked="isConfirmed" />
                <text class="checkbox-text">员工已到场签字确认</text>
              </label>
            </checkbox-group>
          </view>

          <view class="modal-actions">
            <button class="btn-modal" @click="hideConfirmModal">取消</button>
            <button
              class="btn-modal primary"
              :disabled="!isConfirmed"
              :loading="submitting"
              @click="submitConfirm"
            >
              确认出库
            </button>
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
      list: [],
      pendingCount: 0,
      loading: false,
      refreshing: false,
      pageNum: 1,
      pageSize: 20,
      noMore: false,
      highlightId: null,
      // 确认弹窗
      showConfirmModal: false,
      currentOutbound: {
        details: []
      },
      isConfirmed: false,
      submitting: false
    }
  },

  methods: {
    async loadData() {
      if (this.loading || this.noMore) return

      this.loading = true

      try {
        const res = await api.outbound.getPendingList({
          pageNum: this.pageNum,
          pageSize: this.pageSize
        })

        if (res.code === 200) {
          const list = res.data || []
          const total = res.total || 0

          // 计算等待天数
          list.forEach(item => {
            const createDate = new Date(item.createTime)
            const now = new Date()
            item.waitDays = Math.floor((now - createDate) / (1000 * 60 * 60 * 24))
          })

          if (this.pageNum === 1) {
            this.list = list
          } else {
            this.list = this.list.concat(list)
          }

          this.pendingCount = total

          if (this.list.length >= total) {
            this.noMore = true
          }
        }
      } catch (err) {
        console.error('加载数据失败', err)
        uni.showToast({ title: '加载失败，请下拉刷新', icon: 'none' })
      } finally {
        this.loading = false
        this.refreshing = false
      }
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

    handleConfirm(item) {
      this.currentOutbound = item
      this.isConfirmed = false
      this.showConfirmModal = true
    },

    hideConfirmModal() {
      this.showConfirmModal = false
    },

    handleCheckboxChange(e) {
      this.isConfirmed = e.detail.value.includes('confirmed')
    },

    async submitConfirm() {
      if (!this.isConfirmed) {
        uni.showToast({
          title: '请确认员工已到场',
          icon: 'none'
        })
        return
      }

      this.submitting = true

      try {
        const res = await api.outbound.confirmPickup(this.currentOutbound.id, {
          remark: '员工已签字确认'
        })

        if (res.code === 200) {
          uni.showToast({
            title: '出库确认成功',
            icon: 'success',
            duration: 2000
          })

          this.hideConfirmModal()

          setTimeout(() => {
            this.onRefresh()
          }, 1500)
        }
      } catch (err) {
        console.error('确认出库失败', err)
        uni.showToast({ title: '确认出库失败，请重试', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },

    handleCancel(item) {
      uni.showModal({
        title: '取消出库',
        content: '确定要取消该出库单吗？',
        editable: true,
        placeholderText: '请输入取消原因',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await api.outbound.cancel(item.id, {
                cancelReason: res.content || '仓管取消'
              })

              if (result.code === 200) {
                uni.showToast({
                  title: '已取消',
                  icon: 'success'
                })

                this.onRefresh()
              }
            } catch (err) {
              console.error('取消出库失败', err)
              uni.showToast({ title: '取消失败，请重试', icon: 'none' })
            }
          }
        }
      })
    }
  },

  onLoad(options) {
    if (options.highlightId) {
      this.highlightId = options.highlightId
    }
    this.loadData()
  },

  onShow() {
    if (this.list.length > 0) {
      this.onRefresh()
    }
  },

  onPullDownRefresh() {
    this.onRefresh()
    uni.stopPullDownRefresh()
  }
}
</script>

<style lang="scss" scoped>
.pending-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.stats-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stats-title {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16rpx;
}

.stats-count {
  font-size: 96rpx;
  font-weight: 600;
  color: #ffffff;
}

.scroll-content {
  height: calc(100vh - 240rpx);
}

.outbound-list {
  padding: 24rpx 32rpx;
}

.outbound-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);

  &.highlight {
    animation: highlight 1s ease-in-out;
  }
}

@keyframes highlight {
  0%, 100% {
    background-color: #ffffff;
  }
  50% {
    background-color: #e6f7ff;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.receiver-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.receiver-name {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8rpx;
}

.receiver-phone {
  font-size: 24rpx;
  color: #8c8c8c;
}

.warning-tag {
  padding: 8rpx 16rpx;
  background-color: #fff7e6;
  color: #faad14;
  font-size: 24rpx;
  border-radius: 8rpx;
}

.card-body {
  margin-bottom: 24rpx;
}

.info-row {
  display: flex;
  margin-bottom: 12rpx;
}

.info-label {
  font-size: 24rpx;
  color: #8c8c8c;
  width: 120rpx;
}

.info-value {
  flex: 1;
  font-size: 24rpx;
  color: #262626;
}

.materials-section {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.materials-title {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 16rpx;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.material-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx;
  background-color: #fafafa;
  border-radius: 8rpx;
}

.material-name {
  font-size: 26rpx;
  color: #262626;
}

.material-quantity {
  font-size: 26rpx;
  color: #1890ff;
  font-weight: 500;
}

.card-footer {
  display: flex;
  gap: 16rpx;
  justify-content: flex-end;
}

.action-btn {
  padding: 16rpx 32rpx;
  border: none;
  border-radius: 8rpx;
  font-size: 26rpx;
  background-color: #f0f0f0;
  color: #262626;

  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
  }

  &::after {
    border: none;
  }
}

/* 弹窗样式 */
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
  width: 85%;
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

.confirm-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32rpx;
}

.confirm-icon {
  font-size: 96rpx;
  margin-bottom: 16rpx;
}

.confirm-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
}

.outbound-info {
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.info-text {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 8rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.material-summary {
  margin-bottom: 24rpx;
}

.summary-title {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 16rpx;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
}

.summary-name {
  font-size: 24rpx;
  color: #595959;
}

.summary-quantity {
  font-size: 24rpx;
  color: #1890ff;
  font-weight: 500;
}

.warning-box {
  background-color: #fff7e6;
  border-radius: 8rpx;
  padding: 16rpx;
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.warning-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.warning-text {
  flex: 1;
  font-size: 24rpx;
  color: #faad14;
  line-height: 1.5;
}

.checkbox-group {
  margin-bottom: 24rpx;
}

.checkbox-label {
  display: flex;
  align-items: center;
}

.checkbox-text {
  font-size: 28rpx;
  color: #262626;
  margin-left: 16rpx;
}

.modal-actions {
  display: flex;
  gap: 16rpx;
}

.btn-modal {
  flex: 1;
  height: 72rpx;
  border: none;
  border-radius: 36rpx;
  font-size: 28rpx;
  background-color: #f0f0f0;
  color: #262626;

  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;

    &:disabled {
      opacity: 0.5;
    }
  }

  &::after {
    border: none;
  }
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
