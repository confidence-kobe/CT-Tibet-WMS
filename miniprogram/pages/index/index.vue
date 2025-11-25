<template>
  <view class="index-container">
    <!-- 头部信息 -->
    <view class="header">
      <view class="user-info">
        <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ userInfo.realName || '未登录' }}</text>
          <text class="dept">{{ userInfo.deptName || '' }}</text>
        </view>
      </view>
      <view class="message-icon" @click="goToMessages">
        <text class="icon">🔔</text>
        <view v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</view>
      </view>
    </view>

    <!-- 下拉刷新 -->
    <scroll-view
      scroll-y
      class="scroll-content"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 普通员工视图 -->
      <view v-if="isEmployee">
        <!-- 我的申请统计 -->
        <view class="card">
          <view class="card-header">
            <text class="card-title">📋 我的申请</text>
          </view>
          <view class="stats-grid">
            <view class="stat-item" @click="goToApplyList('pending')">
              <text class="stat-value warning">{{ myApplies.pendingCount || 0 }}</text>
              <text class="stat-label">待审批</text>
            </view>
            <view class="stat-item" @click="goToApplyList('approved')">
              <text class="stat-value success">{{ myApplies.approvedCount || 0 }}</text>
              <text class="stat-label">已通过</text>
            </view>
            <view class="stat-item" @click="goToApplyList('pickup')">
              <text class="stat-value primary">{{ myApplies.pickupCount || 0 }}</text>
              <text class="stat-label">待领取</text>
            </view>
          </view>
        </view>

        <!-- 快捷操作 -->
        <view class="card">
          <view class="card-header">
            <text class="card-title">⚡ 快捷操作</text>
          </view>
          <view class="quick-actions">
            <view class="action-item large" @click="goToCreateApply">
              <text class="action-icon">📝</text>
              <text class="action-text">新建申请</text>
            </view>
          </view>
          <view class="quick-actions">
            <view class="action-item" @click="goToMyApplies">
              <text class="action-icon">📋</text>
              <text class="action-text">我的申请</text>
            </view>
            <view class="action-item" @click="goToInventory">
              <text class="action-icon">📊</text>
              <text class="action-text">查看库存</text>
            </view>
          </view>
        </view>

        <!-- 通知消息 -->
        <view class="card">
          <view class="card-header">
            <text class="card-title">🔔 通知消息</text>
            <text class="card-more" @click="goToMessages">查看全部</text>
          </view>
          <view v-if="messages.length > 0" class="message-list">
            <view v-for="msg in messages" :key="msg.id" class="message-item" @click="handleMessageClick(msg)">
              <view class="message-content">
                <text class="message-title">{{ msg.title }}</text>
                <text class="message-text">{{ msg.content }}</text>
                <text class="message-time">{{ msg.time }}</text>
              </view>
            </view>
          </view>
          <view v-else class="empty-state">
            <text class="empty-icon">📭</text>
            <text class="empty-text">暂无消息</text>
          </view>
        </view>
      </view>

      <!-- 仓库管理员视图 -->
      <view v-if="isWarehouse">
        <!-- 今日数据 -->
        <view class="card">
          <view class="card-header">
            <text class="card-title">📊 今日数据</text>
          </view>
          <view class="stats-grid four">
            <view class="stat-item">
              <text class="stat-value primary">{{ todayData.inboundCount || 0 }}</text>
              <text class="stat-label">入库</text>
            </view>
            <view class="stat-item">
              <text class="stat-value success">{{ todayData.outboundCount || 0 }}</text>
              <text class="stat-label">出库</text>
            </view>
            <view class="stat-item">
              <text class="stat-value warning">{{ todayData.pendingApprovalCount || 0 }}</text>
              <text class="stat-label">待审批</text>
            </view>
            <view class="stat-item">
              <text class="stat-value info">{{ todayData.materialCount || 20 }}</text>
              <text class="stat-label">库存种类</text>
            </view>
          </view>
        </view>

        <!-- 待办事项 -->
        <view class="card">
          <view class="card-header">
            <text class="card-title">🔔 待办事项</text>
          </view>
          <view class="todo-list">
            <view class="todo-item" @click="goToApprovalList">
              <text class="todo-icon">⏳</text>
              <text class="todo-text">{{ pendingTasks.pendingApproval || 0 }}条申请待审批</text>
              <text class="todo-arrow">›</text>
            </view>
            <view class="todo-item" @click="goToPendingPickup">
              <text class="todo-icon">📦</text>
              <text class="todo-text">{{ pendingTasks.pendingPickup || 0 }}条出库待确认</text>
              <text class="todo-arrow">›</text>
            </view>
            <view class="todo-item" v-if="pendingTasks.lowStockAlert > 0" @click="goToLowStockAlert">
              <text class="todo-icon">⚠️</text>
              <text class="todo-text">{{ pendingTasks.lowStockAlert }}种物资库存预警</text>
              <text class="todo-arrow">›</text>
            </view>
          </view>
        </view>

        <!-- 快捷操作 -->
        <view class="card">
          <view class="card-header">
            <text class="card-title">⚡ 快捷操作</text>
          </view>
          <view class="quick-actions">
            <view class="action-item" @click="goToInboundCreate">
              <text class="action-icon">📥</text>
              <text class="action-text">快速入库</text>
            </view>
            <view class="action-item" @click="goToOutboundCreate">
              <text class="action-icon">📤</text>
              <text class="action-text">快速出库</text>
            </view>
          </view>
          <view class="quick-actions">
            <view class="action-item" @click="goToApprovalList">
              <text class="action-icon">✅</text>
              <text class="action-text">审批申请</text>
            </view>
            <view class="action-item" @click="goToInventory">
              <text class="action-icon">📊</text>
              <text class="action-text">查看库存</text>
            </view>
          </view>
        </view>

        <!-- 最近操作 -->
        <view class="card">
          <view class="card-header">
            <text class="card-title">📈 最近操作</text>
          </view>
          <view v-if="recentOperations.length > 0" class="operation-list">
            <view v-for="(op, index) in recentOperations" :key="index" class="operation-item">
              <text class="operation-time">{{ op.time }}</text>
              <text class="operation-title">{{ op.title }}</text>
            </view>
          </view>
          <view v-else class="empty-state">
            <text class="empty-icon">📭</text>
            <text class="empty-text">暂无记录</text>
          </view>
        </view>
      </view>

      <!-- 底部安全区域 -->
      <view class="safe-area-inset-bottom"></view>
    </scroll-view>
  </view>
</template>

<script>
import { mapState, mapGetters } from 'vuex'
import api from '@/api'

export default {
  data() {
    return {
      refreshing: false,
      // 员工数据
      myApplies: {
        pendingCount: 0,
        approvedCount: 0,
        pickupCount: 0
      },
      messages: [],
      // 仓管数据
      todayData: {
        inboundCount: 0,
        outboundCount: 0,
        pendingApprovalCount: 0,
        materialCount: 0
      },
      recentOperations: []
    }
  },

  computed: {
    ...mapState(['userInfo', 'unreadCount', 'pendingTasks']),
    ...mapGetters(['isEmployee', 'isWarehouse'])
  },

  methods: {
    // 下拉刷新
    async onRefresh() {
      this.refreshing = true
      await this.loadData()
      this.refreshing = false
    },

    // 加载数据
    async loadData() {
      try {
        const res = await api.common.getStatistics()

        if (res.code === 200) {
          if (this.isEmployee) {
            // 员工数据
            this.myApplies = res.data.myApplies || {
              pendingCount: 0,
              approvedCount: 0,
              pickupCount: 0
            }
            this.messages = (res.data.messages || []).slice(0, 3) // 只显示最新3条
          } else if (this.isWarehouse) {
            // 仓管数据
            this.todayData = res.data.todayData || {
              inboundCount: 0,
              outboundCount: 0,
              pendingApprovalCount: 0,
              materialCount: 0
            }
            this.recentOperations = (res.data.recentOperations || []).slice(0, 5)

            // 更新待办事项
            this.$store.commit('SET_PENDING_TASKS', res.data.pendingTasks || {
              pendingApproval: 0,
              pendingPickup: 0,
              lowStockAlert: 0
            })
          }
        }

        // 更新未读消息数
        await this.loadUnreadCount()
      } catch (err) {
        console.error('加载数据失败', err)
      }
    },

    // 加载未读消息数
    async loadUnreadCount() {
      try {
        const res = await api.message.getUnreadCount()
        if (res.code === 200) {
          this.$store.commit('SET_UNREAD_COUNT', res.data.count || 0)
        }
      } catch (err) {
        console.error('获取未读消息数失败', err)
      }
    },

    // 跳转到消息列表
    goToMessages() {
      uni.navigateTo({
        url: '/pages/mine/messages'
      })
    },

    // 跳转到申请列表（员工）
    goToApplyList(status) {
      uni.switchTab({
        url: '/pages/apply/list'
      })
    },

    // 跳转到新建申请
    goToCreateApply() {
      uni.navigateTo({
        url: '/pages/apply/create'
      })
    },

    // 跳转到我的申请
    goToMyApplies() {
      uni.switchTab({
        url: '/pages/apply/list'
      })
    },

    // 跳转到库存查询
    goToInventory() {
      uni.switchTab({
        url: '/pages/inventory/list'
      })
    },

    // 跳转到审批列表（仓管）
    goToApprovalList() {
      uni.switchTab({
        url: '/pages/approval/list'
      })
    },

    // 跳转到待领取出库
    goToPendingPickup() {
      uni.navigateTo({
        url: '/pages/outbound/pending'
      })
    },

    // 跳转到库存预警
    goToLowStockAlert() {
      uni.navigateTo({
        url: '/pages/inventory/list?status=1'
      })
    },

    // 跳转到快速入库
    goToInboundCreate() {
      uni.navigateTo({
        url: '/pages/inbound/create'
      })
    },

    // 跳转到快速出库
    goToOutboundCreate() {
      uni.navigateTo({
        url: '/pages/outbound/create'
      })
    },

    // 处理消息点击
    handleMessageClick(msg) {
      // 根据消息类型跳转到相应页面
      if (msg.relatedType === 1) {
        // 申请单
        uni.navigateTo({
          url: `/pages/apply/detail?id=${msg.relatedId}`
        })
      } else if (msg.relatedType === 2) {
        // 出库单
        uni.navigateTo({
          url: `/pages/outbound/pending`
        })
      }
    }
  },

  onLoad() {
    // 加载数据
    this.loadData()
  },

  onShow() {
    // 页面显示时刷新数据
    this.loadData()
  },

  onPullDownRefresh() {
    this.loadData().then(() => {
      uni.stopPullDownRefresh()
    })
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/design-system.scss";

.index-container {
  min-height: 100vh;
  @include gradient-mesh;
  background-color: $bg-secondary;
}

.header {
  @include gradient-primary;
  padding: 40rpx 32rpx 56rpx;
  @include flex-between;
  position: relative;
  overflow: hidden;

  // 添加装饰性渐变球
  &::before {
    content: '';
    position: absolute;
    top: -100rpx;
    right: -100rpx;
    width: 300rpx;
    height: 300rpx;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.2) 0%, transparent 70%);
    border-radius: 50%;
  }
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: $radius-full;
  margin-right: $spacing-md;
  background: $bg-primary;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  position: relative;
  z-index: 1;
}

.name {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #ffffff;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.dept {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.9);
  font-weight: $font-weight-medium;
}

.message-icon {
  position: relative;
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: $radius-full;
  @include smooth-transition(transform, background);
  z-index: 1;

  &:active {
    transform: scale(0.9);
    background: rgba(255, 255, 255, 0.3);
  }
}

.icon {
  font-size: 40rpx;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}

.badge {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  @include gradient-secondary;
  color: #ffffff;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  padding: 4rpx 8rpx;
  border-radius: $radius-full;
  min-width: 32rpx;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(245, 87, 108, 0.4);
  @include pulse-animation;
}

.scroll-content {
  height: calc(100vh - 176rpx);
}

.card {
  @include card-base;
  margin: $spacing-md $spacing-lg;
  @include fade-in;

  &:nth-child(1) { animation-delay: 0.1s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.3s; }
  &:nth-child(4) { animation-delay: 0.4s; }
}

.card-header {
  @include flex-between;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 2rpx solid $gray-100;
}

.card-title {
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.card-more {
  font-size: $font-size-sm;
  color: $primary;
  font-weight: $font-weight-medium;
  @include smooth-transition(color);

  &:active {
    color: $primary-dark;
  }
}

.stats-grid {
  display: flex;
  justify-content: space-around;
  gap: $spacing-sm;

  &.four {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
  }
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-lg $spacing-sm;
  background: $bg-secondary;
  border-radius: $radius-lg;
  @include smooth-transition(transform, box-shadow);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4rpx;
    opacity: 0;
    @include smooth-transition(opacity);
  }

  &:active {
    transform: translateY(-4rpx);
    box-shadow: $shadow-md;
  }

  &.primary,
  &:has(.stat-value.primary) {
    &::before {
      background: $primary-gradient;
      opacity: 1;
    }
  }

  &.success,
  &:has(.stat-value.success) {
    &::before {
      @include gradient-success;
      opacity: 1;
    }
  }

  &.warning,
  &:has(.stat-value.warning) {
    &::before {
      @include gradient-warning;
      opacity: 1;
    }
  }
}

.stat-value {
  font-size: $font-size-3xl;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-xs;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;

  &.primary {
    background: $primary-gradient;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  &.success {
    @include gradient-success;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  &.warning {
    @include gradient-warning;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  &.info {
    color: $text-secondary;
  }
}

.stat-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  font-weight: $font-weight-medium;
}

.quick-actions {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;

  &:last-child {
    margin-bottom: 0;
  }
}

.action-item {
  flex: 1;
  @include gradient-primary;
  border-radius: $radius-xl;
  padding: $spacing-xl $spacing-md;
  @include flex-center;
  flex-direction: column;
  box-shadow: $shadow-primary;
  @include smooth-transition(transform, box-shadow);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    transform: translate(-50%, -50%);
    transition: width 0.6s, height 0.6s;
  }

  &:active {
    transform: scale(0.96);
    box-shadow: $shadow-md;

    &::before {
      width: 300rpx;
      height: 300rpx;
    }
  }

  &.large {
    flex: none;
    width: 100%;
    flex-direction: row;
    justify-content: flex-start;
    padding: $spacing-xl;

    .action-icon {
      font-size: 80rpx;
      margin-bottom: 0;
      margin-right: $spacing-md;
    }

    .action-text {
      font-size: $font-size-lg;
    }
  }
}

.action-icon {
  font-size: 64rpx;
  margin-bottom: $spacing-sm;
  filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.15));
  position: relative;
  z-index: 1;
}

.action-text {
  font-size: $font-size-base;
  color: #ffffff;
  font-weight: $font-weight-semibold;
  position: relative;
  z-index: 1;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: $spacing-lg;
  background: $bg-secondary;
  border-radius: $radius-lg;
  border-left: 4rpx solid $primary;
  @include smooth-transition(all);

  &:active {
    transform: translateX(4rpx);
    background: $gray-100;
  }
}

.todo-icon {
  font-size: 48rpx;
  margin-right: $spacing-md;
  @include float-animation;
}

.todo-text {
  flex: 1;
  font-size: $font-size-base;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

.todo-arrow {
  font-size: 48rpx;
  color: $text-tertiary;
  @include smooth-transition(transform);

  .todo-item:active & {
    transform: translateX(4rpx);
  }
}

.message-list,
.operation-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.message-item {
  padding: $spacing-lg;
  background: linear-gradient(135deg, $bg-secondary 0%, $gray-100 100%);
  border-radius: $radius-lg;
  border-left: 4rpx solid $info;
  @include smooth-transition(transform, box-shadow);

  &:active {
    transform: translateY(-2rpx);
    box-shadow: $shadow-md;
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.message-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  @include text-ellipsis;
}

.message-text {
  font-size: $font-size-sm;
  color: $text-secondary;
  @include text-clamp(2);
}

.message-time {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

.operation-item {
  @include flex-between;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $gray-200;
  @include smooth-transition(padding);

  &:last-child {
    border-bottom: none;
  }

  &:active {
    padding-left: $spacing-sm;
  }
}

.operation-time {
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.operation-title {
  flex: 1;
  font-size: $font-size-base;
  color: $text-primary;
  margin-left: $spacing-md;
  @include text-ellipsis;
}

.empty-state {
  @include flex-center;
  flex-direction: column;
  padding: $spacing-3xl 0;
  opacity: 0.6;
}

.empty-icon {
  display: block;
  font-size: 128rpx;
  margin-bottom: $spacing-md;
  filter: grayscale(100%);
  opacity: 0.5;
}

.empty-text {
  font-size: $font-size-base;
  color: $text-tertiary;
  font-weight: $font-weight-medium;
}
</style>
