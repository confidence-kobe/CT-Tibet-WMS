<template>
  <view class="detail-container">
    <scroll-view scroll-y class="scroll-content">
      <!-- 状态卡片 -->
      <view class="status-card">
        <view :class="['status-icon', `status-${detail.stockStatus}`]">
          <text>{{ getStatusIcon(detail.stockStatus) }}</text>
        </view>
        <text class="status-text">{{ getStatusText(detail.stockStatus) }}</text>
        <text v-if="detail.stockStatus === 1" class="status-tips">库存已低于预警值</text>
        <text v-if="detail.stockStatus === 2" class="status-tips warning">物资已缺货，请及时补充</text>
      </view>

      <!-- 基本信息 -->
      <view class="info-card">
        <view class="card-title">基本信息</view>
        <view class="info-row">
          <text class="info-label">物资编码</text>
          <text class="info-value">{{ detail.materialCode }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">物资名称</text>
          <text class="info-value">{{ detail.materialName }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">规格型号</text>
          <text class="info-value">{{ detail.spec }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">物资分类</text>
          <text class="info-value">{{ detail.category }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">计量单位</text>
          <text class="info-value">{{ detail.unit }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">所属仓库</text>
          <text class="info-value">{{ detail.warehouseName }}</text>
        </view>
      </view>

      <!-- 库存信息 -->
      <view class="info-card">
        <view class="card-title">库存信息</view>
        <view class="stock-grid">
          <view class="stock-item primary">
            <text class="stock-number">{{ detail.quantity || 0 }}</text>
            <text class="stock-label">当前库存</text>
          </view>
          <view class="stock-item warning">
            <text class="stock-number">{{ detail.lockedQuantity || 0 }}</text>
            <text class="stock-label">锁定数量</text>
          </view>
          <view class="stock-item success">
            <text class="stock-number">{{ detail.availableQuantity || 0 }}</text>
            <text class="stock-label">可用数量</text>
          </view>
          <view class="stock-item default">
            <text class="stock-number">{{ detail.minStock || 0 }}</text>
            <text class="stock-label">预警阈值</text>
          </view>
        </view>
        <view v-if="detail.lastInboundTime || detail.lastOutboundTime" class="divider"></view>
        <view v-if="detail.lastInboundTime" class="info-row">
          <text class="info-label">最近入库</text>
          <text class="info-value">{{ detail.lastInboundTime }}</text>
        </view>
        <view v-if="detail.lastOutboundTime" class="info-row">
          <text class="info-label">最近出库</text>
          <text class="info-value">{{ detail.lastOutboundTime }}</text>
        </view>
      </view>

      <!-- 统计信息 -->
      <view v-if="detail.stats" class="info-card">
        <view class="card-title">统计信息</view>
        <view class="info-row">
          <text class="info-label">本月入库</text>
          <text class="info-value success">{{ detail.stats.monthInbound || 0 }} {{ detail.unit }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">本月出库</text>
          <text class="info-value danger">{{ detail.stats.monthOutbound || 0 }} {{ detail.unit }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">累计入库</text>
          <text class="info-value">{{ detail.stats.totalInbound || 0 }} {{ detail.unit }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">累计出库</text>
          <text class="info-value">{{ detail.stats.totalOutbound || 0 }} {{ detail.unit }}</text>
        </view>
      </view>

      <!-- 最近操作记录 -->
      <view v-if="detail.recentLogs && detail.recentLogs.length > 0" class="info-card">
        <view class="card-title">最近操作记录</view>
        <view class="timeline">
          <view v-for="(log, index) in detail.recentLogs" :key="index" class="timeline-item">
            <view :class="['timeline-dot', log.type === 'inbound' ? 'success' : 'danger']"></view>
            <view class="timeline-content">
              <text class="timeline-title">{{ log.type === 'inbound' ? '入库' : '出库' }}</text>
              <text class="timeline-desc">数量: {{ log.quantity }} {{ detail.unit }}</text>
              <text v-if="log.operator" class="timeline-desc">操作人: {{ log.operator }}</text>
              <text class="timeline-time">{{ log.time }}</text>
            </view>
          </view>
        </view>
        <view class="more-link" @click="viewAllRecords">
          <text>查看全部记录 →</text>
        </view>
      </view>

      <view class="safe-area-inset-bottom"></view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view v-if="showActions" class="action-bar safe-area-inset-bottom">
      <button class="action-btn" @click="handleInbound">快速入库</button>
      <button class="action-btn primary" @click="handleOutbound">快速出库</button>
    </view>
  </view>
</template>

<script>
import api from '@/api'

export default {
  data() {
    return {
      id: null,
      materialCode: null,
      detail: {
        stockStatus: 0,
        quantity: 0,
        lockedQuantity: 0,
        availableQuantity: 0,
        minStock: 0
      },
      loading: false
    }
  },

  computed: {
    showActions() {
      // 根据用户角色判断是否显示操作按钮
      // 这里暂时返回 false，需要根据实际权限控制
      return false
    }
  },

  methods: {
    async loadData() {
      if (this.loading) return

      this.loading = true

      try {
        const res = await api.inventory.getDetail(this.id || this.materialCode)

        if (res.code === 200) {
          this.detail = res.data

          // 设置标题
          uni.setNavigationBarTitle({
            title: this.detail.materialName || '库存详情'
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
        0: '✓',
        1: '⚠️',
        2: '⛔'
      }
      return icons[status] || '•'
    },

    getStatusText(status) {
      const texts = {
        0: '库存正常',
        1: '库存不足',
        2: '已缺货'
      }
      return texts[status] || '未知状态'
    },

    // 查看全部记录
    viewAllRecords() {
      uni.navigateTo({
        url: `/pages/inventory/records?materialCode=${this.detail.materialCode}`
      })
    },

    // 快速入库
    handleInbound() {
      uni.navigateTo({
        url: `/pages/inbound/create?materialCode=${this.detail.materialCode}`
      })
    },

    // 快速出库
    handleOutbound() {
      uni.navigateTo({
        url: `/pages/outbound/create?materialCode=${this.detail.materialCode}`
      })
    }
  },

  onLoad(options) {
    if (options.id) {
      this.id = options.id
      this.loadData()
    } else if (options.materialCode) {
      this.materialCode = options.materialCode
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
  },

  onShow() {
    // 从其他页面返回时刷新数据
    if (this.id || this.materialCode) {
      this.loadData()
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
  height: 100vh;
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

  &.status-0 { color: #52c41a; }
  &.status-1 { color: #faad14; }
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
  }

  &.danger {
    color: #f5222d;
  }
}

.stock-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.stock-item {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;

  &.primary {
    background-color: #e6f7ff;
    .stock-number { color: #1890ff; }
  }

  &.warning {
    background-color: #fff7e6;
    .stock-number { color: #faad14; }
  }

  &.success {
    background-color: #f6ffed;
    .stock-number { color: #52c41a; }
  }

  &.default {
    background-color: #fafafa;
    .stock-number { color: #8c8c8c; }
  }
}

.stock-number {
  font-size: 48rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.stock-label {
  font-size: 24rpx;
  color: #8c8c8c;
}

.divider {
  height: 1rpx;
  background-color: #f0f0f0;
  margin: 16rpx 0;
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

  &.success {
    background-color: #52c41a;
    box-shadow: 0 0 0 2rpx #f6ffed;
  }

  &.danger {
    background-color: #f5222d;
    box-shadow: 0 0 0 2rpx #fff1f0;
  }
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
  margin-bottom: 4rpx;
}

.timeline-time {
  font-size: 20rpx;
  color: #bfbfbf;
}

.more-link {
  text-align: center;
  padding-top: 16rpx;
  margin-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  font-size: 26rpx;
  color: #1890ff;
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
