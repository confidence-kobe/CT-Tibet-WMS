<template>
  <view class="detail-container">
    <scroll-view scroll-y class="scroll-content">
      <!-- 状态卡片 -->
      <view class="status-card">
        <view :class="['status-icon', `status-${detail.status}`]">
          <text>{{ getStatusIcon(detail.status) }}</text>
        </view>
        <text class="status-text">{{ statusMap[detail.status] }}</text>
        <text v-if="detail.status === 0" class="status-tips">请尽快通知领用人前来领取</text>
        <text v-if="detail.status === 1" class="status-tips">库存已减少</text>
        <text v-if="detail.status === 0 && detail.waitDays >= 5" class="status-tips warning">
          已等待 {{ detail.waitDays }} 天，即将超时
        </text>
      </view>

      <!-- 来源标签 -->
      <view :class="['source-badge', `source-${detail.source}`]">
        <text>{{ detail.source === 1 ? '直接出库' : '申请出库' }}</text>
      </view>

      <!-- 基本信息 -->
      <view class="info-card">
        <view class="card-title">基本信息</view>
        <view class="info-row">
          <text class="info-label">出库单号</text>
          <text class="info-value">{{ detail.outboundNo }}</text>
        </view>
        <view v-if="detail.source === 2 && detail.applyNo" class="info-row">
          <text class="info-label">关联申请</text>
          <text class="info-value link" @click="goToApply">{{ detail.applyNo }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">领用人</text>
          <text class="info-value">{{ detail.receiverName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">联系电话</text>
          <text class="info-value">{{ detail.receiverPhone }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">所属部门</text>
          <text class="info-value">{{ detail.deptName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">仓库名称</text>
          <text class="info-value">{{ detail.warehouseName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">创建人</text>
          <text class="info-value">{{ detail.operatorName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">创建时间</text>
          <text class="info-value">{{ detail.createTime }}</text>
        </view>
        <view v-if="detail.status === 1" class="info-row">
          <text class="info-label">完成时间</text>
          <text class="info-value">{{ detail.outboundTime }}</text>
        </view>
        <view v-if="detail.remark" class="info-row column">
          <text class="info-label">备注说明</text>
          <text class="info-value">{{ detail.remark }}</text>
        </view>
      </view>

      <!-- 出库物资 -->
      <view class="info-card">
        <view class="card-title">出库物资</view>
        <view class="material-list">
          <view v-for="(item, index) in detail.details" :key="index" class="material-item">
            <view class="material-header">
              <text class="material-name">{{ item.materialName }}</text>
              <text class="material-amount">¥{{ item.amount.toFixed(2) }}</text>
            </view>
            <view class="material-info">
              <text class="material-spec">规格: {{ item.spec }}</text>
              <text class="material-quantity">数量: {{ item.quantity }} {{ item.unit }}</text>
              <text class="material-price">单价: ¥{{ item.unitPrice.toFixed(2) }}</text>
            </view>
          </view>
        </view>

        <!-- 合计 -->
        <view class="total-summary">
          <view class="summary-row">
            <text class="summary-label">物资种类</text>
            <text class="summary-value">{{ detail.details.length }} 种</text>
          </view>
          <view class="summary-row">
            <text class="summary-label">总金额</text>
            <text class="summary-value primary">¥{{ detail.totalAmount.toFixed(2) }}</text>
          </view>
        </view>
      </view>

      <!-- 签收信息 -->
      <view v-if="detail.status === 1" class="info-card">
        <view class="card-title">签收信息</view>
        <view class="info-row">
          <text class="info-label">签收人</text>
          <text class="info-value">{{ detail.signerName || detail.receiverName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">签收时间</text>
          <text class="info-value">{{ detail.outboundTime }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">确认人</text>
          <text class="info-value">{{ detail.confirmerName }}</text>
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
      <button v-if="detail.status === 0" class="action-btn" @click="handleCancel">取消</button>
      <button v-if="detail.status === 0" class="action-btn primary" @click="handleConfirm">确认出库</button>
      <button v-if="detail.status === 1" class="action-btn" @click="handlePrint">打印单据</button>
    </view>
  </view>
</template>

<script>
import { $uRequest } from '@/utils/request.js'
import { mapState, mapGetters } from 'vuex'

export default {
  data() {
    return {
      id: null,
      detail: {
        status: 0,
        source: 1,
        details: []
      },
      statusMap: {
        0: '待领取',
        1: '已完成',
        2: '已取消'
      },
      loading: false
    }
  },

  computed: {
    ...mapState(['userInfo']),
    ...mapGetters(['isWarehouse']),

    showActions() {
      return this.isWarehouse || this.userInfo.roleCode === 'DEPT_ADMIN'
    }
  },

  methods: {
    async loadData() {
      if (this.loading) return

      this.loading = true

      try {
        const res = await $uRequest({
          url: `/api/outbounds/${this.id}`,
          method: 'GET'
        })

        if (res.code === 200) {
          this.detail = res.data

          // 设置标题
          uni.setNavigationBarTitle({
            title: '出库单详情'
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
        2: '✗'
      }
      return icons[status] || '•'
    },

    goToApply() {
      if (this.detail.applyId) {
        uni.navigateTo({
          url: `/pages/apply/detail?id=${this.detail.applyId}`
        })
      }
    },

    handleConfirm() {
      uni.showModal({
        title: '确认出库',
        content: '请确认领用人已到场并签字确认。确认后将立即扣减库存。',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({
                title: '处理中...'
              })

              const result = await $uRequest({
                url: `/api/outbounds/${this.id}/confirm`,
                method: 'PUT',
                data: {
                  confirmTime: new Date().toISOString()
                }
              })

              uni.hideLoading()

              if (result.code === 200) {
                uni.showToast({
                  title: '出库成功',
                  icon: 'success',
                  duration: 2000
                })

                setTimeout(() => {
                  this.loadData()
                }, 2000)
              }
            } catch (err) {
              uni.hideLoading()
              console.error('确认出库失败', err)
            }
          }
        }
      })
    },

    handleCancel() {
      uni.showModal({
        title: '提示',
        content: '确定要取消该出库单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({
                title: '处理中...'
              })

              const result = await $uRequest({
                url: `/api/outbounds/${this.id}/cancel`,
                method: 'PUT',
                data: {
                  cancelReason: '管理员取消'
                }
              })

              uni.hideLoading()

              if (result.code === 200) {
                uni.showToast({
                  title: '已取消',
                  icon: 'success',
                  duration: 2000
                })

                setTimeout(() => {
                  uni.navigateBack()
                }, 2000)
              }
            } catch (err) {
              uni.hideLoading()
              console.error('取消失败', err)
            }
          }
        }
      })
    },

    handlePrint() {
      uni.showToast({
        title: '打印功能开发中',
        icon: 'none'
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

.source-badge {
  display: inline-block;
  padding: 8rpx 24rpx;
  border-radius: 32rpx;
  font-size: 24rpx;
  margin-bottom: 24rpx;

  &.source-1 {
    background-color: #e6f7ff;
    color: #1890ff;
  }

  &.source-2 {
    background-color: #f9f0ff;
    color: #722ed1;
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

  &.link {
    color: #1890ff;
    text-decoration: underline;
  }

  .column & {
    text-align: left;
    margin-top: 8rpx;
  }
}

.material-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
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
  flex: 1;
}

.material-amount {
  font-size: 28rpx;
  font-weight: 500;
  color: #f5222d;
  margin-left: 16rpx;
}

.material-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.material-spec,
.material-quantity,
.material-price {
  font-size: 24rpx;
  color: #8c8c8c;
}

.total-summary {
  padding-top: 24rpx;
  border-top: 2rpx dashed #e8e8e8;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}

.summary-label {
  font-size: 26rpx;
  color: #595959;
}

.summary-value {
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;

  &.primary {
    font-size: 32rpx;
    color: #f5222d;
  }
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

.safe-area-inset-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
