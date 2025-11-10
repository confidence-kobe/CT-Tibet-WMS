<template>
  <view class="detail-container">
    <scroll-view scroll-y class="scroll-content">
      <!-- 状态卡片 -->
      <view class="status-card">
        <view :class="['status-icon', `status-${detail.status}`]">
          <text>{{ getStatusIcon(detail.status) }}</text>
        </view>
        <text class="status-text">{{ statusMap[detail.status] }}</text>
        <text v-if="detail.status === 1" class="status-tips">请到仓库领取物资</text>
        <text v-if="detail.status === 0 && detail.isTimeout" class="status-tips warning">审批超时，请及时处理</text>
      </view>

      <!-- 基本信息 -->
      <view class="info-card">
        <view class="card-title">基本信息</view>
        <view class="info-row">
          <text class="info-label">申请单号</text>
          <text class="info-value">{{ detail.applyNo }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">申请人</text>
          <text class="info-value">{{ detail.applicantName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">联系电话</text>
          <text class="info-value">{{ detail.applicantPhone }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">所属部门</text>
          <text class="info-value">{{ detail.deptName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">申请时间</text>
          <text class="info-value">{{ detail.applyTime }}</text>
        </view>
        <view class="info-row column">
          <text class="info-label">用途说明</text>
          <text class="info-value">{{ detail.purpose }}</text>
        </view>
      </view>

      <!-- 申请物资 -->
      <view class="info-card">
        <view class="card-title">申请物资</view>
        <view class="material-list">
          <view v-for="(item, index) in detail.details" :key="index" class="material-item">
            <view class="material-header">
              <text class="material-name">{{ item.materialName }}</text>
              <text :class="['stock-badge', item.isStockSufficient ? 'success' : 'warning']">
                {{ item.isStockSufficient ? '库存充足' : '库存不足' }}
              </text>
            </view>
            <view class="material-info">
              <text class="material-spec">规格: {{ item.spec }}</text>
              <text class="material-quantity">数量: {{ item.quantity }} {{ item.unit }}</text>
              <text class="material-stock">当前库存: {{ item.currentStock }} {{ item.unit }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 审批信息 -->
      <view v-if="detail.status !== 0" class="info-card">
        <view class="card-title">审批信息</view>
        <view class="info-row">
          <text class="info-label">审批人</text>
          <text class="info-value">{{ detail.approverName || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">审批时间</text>
          <text class="info-value">{{ detail.approvalTime || '-' }}</text>
        </view>
        <view v-if="detail.status === 1 && detail.approvalOpinion" class="info-row column">
          <text class="info-label">审批意见</text>
          <text class="info-value success">{{ detail.approvalOpinion }}</text>
        </view>
        <view v-if="detail.status === 2" class="info-row column">
          <text class="info-label">拒绝原因</text>
          <text class="info-value danger">{{ detail.rejectReason }}</text>
        </view>
      </view>

      <!-- 出库信息 -->
      <view v-if="detail.status === 1 || detail.status === 3" class="info-card">
        <view class="card-title">出库信息</view>
        <view class="info-row">
          <text class="info-label">出库单号</text>
          <text class="info-value">{{ detail.outboundNo || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">出库状态</text>
          <text class="info-value">{{ detail.status === 3 ? '已出库' : '待领取' }}</text>
        </view>
        <view v-if="detail.status === 3" class="info-row">
          <text class="info-label">出库时间</text>
          <text class="info-value">{{ detail.outboundTime || '-' }}</text>
        </view>
      </view>

      <!-- 操作记录 -->
      <view v-if="detail.logs && detail.logs.length > 0" class="info-card">
        <view class="card-title">操作记录</view>
        <view class="timeline">
          <view v-for="(log, index) in detail.logs" :key="index" class="timeline-item">
            <view class="timeline-dot"></view>
            <view class="timeline-content">
              <text class="timeline-title">{{ log.action }}</text>
              <text class="timeline-desc">{{ log.operator }} {{ log.description }}</text>
              <text class="timeline-time">{{ log.time }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="safe-area-inset-bottom"></view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view v-if="showActions" class="action-bar safe-area-inset-bottom">
      <button v-if="detail.status === 0" class="action-btn" @click="handleCancel">撤销申请</button>
      <button v-if="detail.status === 1" class="action-btn primary" @click="handlePickup">去领取</button>
      <button v-if="detail.status === 2" class="action-btn" @click="handleReapply">重新申请</button>
    </view>
  </view>
</template>

<script>
import { $uRequest } from '@/utils/request.js'

export default {
  data() {
    return {
      id: null,
      detail: {
        status: 0,
        details: []
      },
      statusMap: {
        0: '待审批',
        1: '已通过',
        2: '已拒绝',
        3: '已出库',
        4: '已取消'
      },
      loading: false
    }
  },

  computed: {
    showActions() {
      return [0, 1, 2].includes(this.detail.status)
    }
  },

  methods: {
    async loadData() {
      if (this.loading) return

      this.loading = true

      try {
        const res = await $uRequest({
          url: `/api/applies/${this.id}`,
          method: 'GET'
        })

        if (res.code === 200) {
          this.detail = res.data

          // 设置标题
          uni.setNavigationBarTitle({
            title: this.statusMap[this.detail.status]
          })
        }
      } catch (err) {
        console.error('加载详情失败', err)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    getStatusIcon(status) {
      const icons = {
        0: '⏳',
        1: '✓',
        2: '✗',
        3: '✓',
        4: '⊘'
      }
      return icons[status] || '•'
    },

    // 撤销申请
    handleCancel() {
      uni.showModal({
        title: '提示',
        content: '确定要撤销该申请吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await $uRequest({
                url: `/api/applies/${this.id}/cancel`,
                method: 'PUT',
                data: {
                  cancelReason: '用户主动撤销'
                }
              })

              if (result.code === 200) {
                uni.showToast({
                  title: '撤销成功',
                  icon: 'success',
                  duration: 2000
                })

                setTimeout(() => {
                  uni.navigateBack()
                }, 2000)
              }
            } catch (err) {
              console.error('撤销失败', err)
            }
          }
        }
      })
    },

    // 去领取
    handlePickup() {
      if (this.detail.outboundId) {
        uni.navigateTo({
          url: `/pages/outbound/pending?highlightId=${this.detail.outboundId}`
        })
      } else {
        uni.navigateTo({
          url: '/pages/outbound/pending'
        })
      }
    },

    // 重新申请
    handleReapply() {
      uni.navigateTo({
        url: `/pages/apply/create?reapplyId=${this.id}`
      })
    }
  },

  onLoad(options) {
    if (options.id) {
      this.id = options.id
      this.loadData()
    } else {
      uni.showToast({
        title: '参数错误',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.scroll-content {
  height: calc(100vh - 160rpx);
  padding: 24rpx 32rpx;
}

.status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  padding: 48rpx 32rpx;
  margin-bottom: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.status-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
  margin-bottom: 24rpx;
  background-color: rgba(255, 255, 255, 0.2);

  &.status-0 { color: #faad14; }
  &.status-1 { color: #52c41a; }
  &.status-2 { color: #f5222d; }
  &.status-3 { color: #8c8c8c; }
  &.status-4 { color: #bfbfbf; }
}

.status-text {
  font-size: 40rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 16rpx;
}

.status-tips {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);

  &.warning {
    color: #faad14;
  }
}

.info-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #fafafa;

  &:last-child {
    border-bottom: none;
  }

  &.column {
    flex-direction: column;
  }
}

.info-label {
  font-size: 26rpx;
  color: #8c8c8c;
  width: 160rpx;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  font-size: 26rpx;
  color: #262626;
  text-align: right;

  &.success {
    color: #52c41a;
    text-align: left;
    margin-top: 8rpx;
  }

  &.danger {
    color: #f5222d;
    text-align: left;
    margin-top: 8rpx;
  }
}

.material-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.material-item {
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 24rpx;
}

.material-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.material-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;
}

.stock-badge {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 20rpx;

  &.success {
    background-color: #f6ffed;
    color: #52c41a;
  }

  &.warning {
    background-color: #fff7e6;
    color: #faad14;
  }
}

.material-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.material-spec,
.material-quantity,
.material-stock {
  font-size: 24rpx;
  color: #8c8c8c;
}

.timeline {
  position: relative;
  padding-left: 40rpx;
}

.timeline-item {
  position: relative;
  padding-bottom: 32rpx;

  &:last-child {
    padding-bottom: 0;

    &::after {
      display: none;
    }
  }

  &::after {
    content: '';
    position: absolute;
    left: -32rpx;
    top: 32rpx;
    bottom: 0;
    width: 2rpx;
    background-color: #e8e8e8;
  }
}

.timeline-dot {
  position: absolute;
  left: -38rpx;
  top: 8rpx;
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: #1890ff;
  border: 2rpx solid #ffffff;
  box-shadow: 0 0 0 2rpx #e6f7ff;
}

.timeline-content {
  display: flex;
  flex-direction: column;
}

.timeline-title {
  font-size: 26rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8rpx;
}

.timeline-desc {
  font-size: 24rpx;
  color: #8c8c8c;
  margin-bottom: 8rpx;
}

.timeline-time {
  font-size: 20rpx;
  color: #bfbfbf;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  background-color: #ffffff;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  border: none;
  border-radius: 44rpx;
  font-size: 28rpx;
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
</style>
