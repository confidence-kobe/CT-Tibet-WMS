<template>
  <view class="detail-container">
    <scroll-view scroll-y class="scroll-content">
      <!-- 状态卡片 -->
      <view class="status-card">
        <view :class="['status-icon', `status-${detail.status}`]">
          <text>{{ getStatusIcon(detail.status) }}</text>
        </view>
        <text class="status-text">{{ statusMap[detail.status] }}</text>
        <text v-if="detail.status === 1" class="status-tips">库存已增加</text>
      </view>

      <!-- 基本信息 -->
      <view class="info-card">
        <view class="card-title">基本信息</view>
        <view class="info-row">
          <text class="info-label">入库单号</text>
          <text class="info-value">{{ detail.inboundNo }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">入库类型</text>
          <text class="info-value">{{ getTypeLabel(detail.inboundType) }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">仓库名称</text>
          <text class="info-value">{{ detail.warehouseName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">操作人</text>
          <text class="info-value">{{ detail.operatorName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">入库时间</text>
          <text class="info-value">{{ detail.inboundTime }}</text>
        </view>
        <view v-if="detail.remark" class="info-row column">
          <text class="info-label">备注说明</text>
          <text class="info-value">{{ detail.remark }}</text>
        </view>
      </view>

      <!-- 入库物资 -->
      <view class="info-card">
        <view class="card-title">入库物资</view>
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
      <button v-if="detail.status === 1" class="action-btn" @click="handlePrint">打印单据</button>
      <button v-if="detail.status === 1 && canCancel" class="action-btn danger" @click="handleCancel">作废</button>
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
        status: 1,
        details: []
      },
      statusMap: {
        1: '已完成',
        2: '已取消'
      },
      inboundTypes: [
        { label: '采购入库', value: 1 },
        { label: '退货入库', value: 2 },
        { label: '调拨入库', value: 3 },
        { label: '其他', value: 4 }
      ],
      loading: false
    }
  },

  computed: {
    ...mapState(['userInfo']),
    ...mapGetters(['isWarehouse']),

    showActions() {
      return this.isWarehouse || this.userInfo.roleCode === 'DEPT_ADMIN'
    },

    canCancel() {
      // 只有当天的入库单可以作废
      if (!this.detail.inboundTime) return false
      const inboundDate = new Date(this.detail.inboundTime).toDateString()
      const today = new Date().toDateString()
      return inboundDate === today
    }
  },

  methods: {
    async loadData() {
      if (this.loading) return

      this.loading = true

      try {
        const res = await $uRequest({
          url: `/api/inbounds/${this.id}`,
          method: 'GET'
        })

        if (res.code === 200) {
          this.detail = res.data

          // 设置标题
          uni.setNavigationBarTitle({
            title: '入库单详情'
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
        1: '✓',
        2: '✗'
      }
      return icons[status] || '•'
    },

    getTypeLabel(type) {
      const typeItem = this.inboundTypes.find(t => t.value === type)
      return typeItem ? typeItem.label : '未知'
    },

    handlePrint() {
      uni.showToast({
        title: '打印功能开发中',
        icon: 'none'
      })
    },

    handleCancel() {
      uni.showModal({
        title: '提示',
        content: '确定要作废该入库单吗？作废后库存将相应减少。',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await $uRequest({
                url: `/api/inbounds/${this.id}/cancel`,
                method: 'PUT',
                data: {
                  cancelReason: '用户作废'
                }
              })

              if (result.code === 200) {
                uni.showToast({
                  title: '作废成功',
                  icon: 'success',
                  duration: 2000
                })

                setTimeout(() => {
                  uni.navigateBack()
                }, 2000)
              }
            } catch (err) {
              console.error('作废失败', err)
            }
          }
        }
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

  &.danger {
    background-color: #fff1f0;
    color: #f5222d;
    border: 1rpx solid #f5222d;
  }

  &::after {
    border: none;
  }
}

.safe-area-inset-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
