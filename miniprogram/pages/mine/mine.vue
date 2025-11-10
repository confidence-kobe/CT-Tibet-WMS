<template>
  <view class="mine-container">
    <!-- 用户信息 -->
    <view class="user-header">
      <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" />
      <view class="user-info">
        <text class="user-name">{{ userInfo.realName || '未登录' }}</text>
        <text class="user-dept">{{ userInfo.deptName || '' }} | {{ userInfo.roleName || '' }}</text>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-section">
      <view class="menu-item" @click="goToMessages">
        <view class="menu-left">
          <text class="menu-icon">🔔</text>
          <text class="menu-text">消息通知</text>
        </view>
        <view class="menu-right">
          <text v-if="unreadCount > 0" class="menu-badge">{{ unreadCount }}</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="menu-item" @click="goToSettings">
        <view class="menu-left">
          <text class="menu-icon">⚙️</text>
          <text class="menu-text">设置</text>
        </view>
        <view class="menu-right">
          <text class="menu-arrow">›</text>
        </view>
      </view>

      <view class="menu-item" @click="goToAbout">
        <view class="menu-left">
          <text class="menu-icon">ℹ️</text>
          <text class="menu-text">关于</text>
        </view>
        <view class="menu-right">
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>

    <!-- 版本信息 -->
    <view class="version-info">
      <text class="version-text">版本 v1.0.0</text>
    </view>
  </view>
</template>

<script>
import { mapState } from 'vuex'

export default {
  computed: {
    ...mapState(['userInfo', 'unreadCount'])
  },

  methods: {
    goToMessages() {
      uni.navigateTo({
        url: '/pages/mine/messages'
      })
    },

    goToSettings() {
      uni.navigateTo({
        url: '/pages/mine/settings'
      })
    },

    goToAbout() {
      uni.showModal({
        title: '关于',
        content: '西藏电信仓库管理系统\nCT-Tibet-WMS v1.0.0\n\n为西藏电信提供便捷的仓库管理服务',
        showCancel: false
      })
    },

    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            // 清除登录状态
            this.$store.commit('LOGOUT')

            // 跳转到登录页
            uni.reLaunch({
              url: '/pages/login/login'
            })

            uni.showToast({
              title: '已退出登录',
              icon: 'success'
            })
          }
        }
      })
    }
  },

  onLoad() {
    // 获取未读消息数
    this.$store.dispatch('getUnreadCount')
  },

  onShow() {
    // 刷新未读消息数
    this.$store.dispatch('getUnreadCount')
  }
}
</script>

<style lang="scss" scoped>
.mine-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.user-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 64rpx 32rpx 48rpx;
  display: flex;
  align-items: center;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  margin-right: 32rpx;
  background-color: #ffffff;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 40rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 12rpx;
}

.user-dept {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
}

.menu-section {
  margin: 24rpx 32rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: #f5f5f5;
  }
}

.menu-left {
  display: flex;
  align-items: center;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 24rpx;
}

.menu-text {
  font-size: 28rpx;
  color: #262626;
}

.menu-right {
  display: flex;
  align-items: center;
}

.menu-badge {
  background-color: #f5222d;
  color: #ffffff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  margin-right: 16rpx;
}

.menu-arrow {
  font-size: 40rpx;
  color: #bfbfbf;
}

.logout-section {
  padding: 24rpx 32rpx;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  background-color: #ffffff;
  color: #f5222d;
  border: 1rpx solid #f5222d;
  border-radius: 44rpx;
  font-size: 28rpx;

  &::after {
    border: none;
  }

  &:active {
    opacity: 0.8;
  }
}

.version-info {
  text-align: center;
  padding: 48rpx 0;
}

.version-text {
  font-size: 24rpx;
  color: #bfbfbf;
}
</style>
