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
import { $uRequest } from '@/utils/request.js'

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
        const res = await $uRequest({
          url: '/api/stats/dashboard',
          method: 'GET'
        })

        if (res.code === 200) {
          if (this.isEmployee) {
            // 员工数据
            this.myApplies = res.data.myApplies || {}
            this.messages = res.data.messages || []
          } else if (this.isWarehouse) {
            // 仓管数据
            this.todayData = res.data.todayData || {}
            this.recentOperations = res.data.recentOperations || []

            // 更新待办事项
            this.$store.commit('SET_PENDING_TASKS', res.data.pendingTasks || {})
          }
        }

        // 更新未读消息数
        this.$store.dispatch('getUnreadCount')
      } catch (err) {
        console.error('加载数据失败', err)
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
.index-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32rpx 32rpx 48rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  margin-right: 24rpx;
  background-color: #ffffff;
}

.info {
  display: flex;
  flex-direction: column;
}

.name {
  font-size: 36rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 8rpx;
}

.dept {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.message-icon {
  position: relative;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-size: 48rpx;
}

.badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  background-color: #f5222d;
  color: #ffffff;
  font-size: 20rpx;
  padding: 4rpx 8rpx;
  border-radius: 16rpx;
  min-width: 32rpx;
  text-align: center;
}

.scroll-content {
  height: calc(100vh - 176rpx);
}

.card {
  background-color: #ffffff;
  border-radius: 16rpx;
  margin: 24rpx 32rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #262626;
}

.card-more {
  font-size: 24rpx;
  color: #1890ff;
}

.stats-grid {
  display: flex;
  justify-content: space-around;

  &.four {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
  }
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx;
}

.stat-value {
  font-size: 48rpx;
  font-weight: 600;
  margin-bottom: 8rpx;

  &.primary {
    color: #1890ff;
  }

  &.success {
    color: #52c41a;
  }

  &.warning {
    color: #faad14;
  }

  &.info {
    color: #8c8c8c;
  }
}

.stat-label {
  font-size: 24rpx;
  color: #8c8c8c;
}

.quick-actions {
  display: flex;
  gap: 16rpx;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.action-item {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  padding: 32rpx 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  &.large {
    flex: none;
    width: 100%;
  }
}

.action-icon {
  font-size: 56rpx;
  margin-bottom: 12rpx;
}

.action-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 500;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background-color: #fafafa;
  border-radius: 12rpx;
}

.todo-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.todo-text {
  flex: 1;
  font-size: 28rpx;
  color: #262626;
}

.todo-arrow {
  font-size: 40rpx;
  color: #bfbfbf;
}

.message-list,
.operation-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.message-item {
  padding: 24rpx;
  background-color: #fafafa;
  border-radius: 12rpx;
}

.message-content {
  display: flex;
  flex-direction: column;
}

.message-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8rpx;
}

.message-text {
  font-size: 24rpx;
  color: #595959;
  margin-bottom: 8rpx;
}

.message-time {
  font-size: 20rpx;
  color: #8c8c8c;
}

.operation-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.operation-time {
  font-size: 24rpx;
  color: #8c8c8c;
}

.operation-title {
  font-size: 28rpx;
  color: #262626;
}

.empty-state {
  padding: 80rpx 0;
  text-align: center;
}

.empty-icon {
  display: block;
  font-size: 96rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #8c8c8c;
}
</style>
