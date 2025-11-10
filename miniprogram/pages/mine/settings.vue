<template>
  <view class="settings-container">
    <!-- 通知设置 -->
    <view class="settings-section">
      <view class="section-title">通知设置</view>
      <view class="settings-items">
        <view class="setting-item">
          <view class="setting-left">
            <text class="setting-icon">🔔</text>
            <text class="setting-label">接收系统通知</text>
          </view>
          <switch :checked="settings.receiveNotification" @change="onNotificationChange" color="#667eea" />
        </view>

        <view class="setting-item">
          <view class="setting-left">
            <text class="setting-icon">📱</text>
            <text class="setting-label">接收微信消息</text>
          </view>
          <switch :checked="settings.receiveWechat" @change="onWechatChange" color="#667eea" />
        </view>

        <view class="setting-item">
          <view class="setting-left">
            <text class="setting-icon">🔊</text>
            <text class="setting-label">消息提示音</text>
          </view>
          <switch :checked="settings.messageSound" @change="onSoundChange" color="#667eea" />
        </view>
      </view>
    </view>

    <!-- 数据管理 -->
    <view class="settings-section">
      <view class="section-title">数据管理</view>
      <view class="settings-items">
        <view class="setting-item clickable" @click="clearCache">
          <view class="setting-left">
            <text class="setting-icon">🗑️</text>
            <text class="setting-label">清除缓存</text>
          </view>
          <view class="setting-right">
            <text class="setting-value">{{ cacheSize }}</text>
            <text class="setting-arrow">›</text>
          </view>
        </view>

        <view class="setting-item clickable" @click="checkUpdate">
          <view class="setting-left">
            <text class="setting-icon">🔄</text>
            <text class="setting-label">检查更新</text>
          </view>
          <view class="setting-right">
            <text class="setting-value">v1.0.0</text>
            <text class="setting-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 隐私与安全 -->
    <view class="settings-section">
      <view class="section-title">隐私与安全</view>
      <view class="settings-items">
        <view class="setting-item clickable" @click="showPrivacyPolicy">
          <view class="setting-left">
            <text class="setting-icon">📋</text>
            <text class="setting-label">隐私政策</text>
          </view>
          <view class="setting-right">
            <text class="setting-arrow">›</text>
          </view>
        </view>

        <view class="setting-item clickable" @click="showUserAgreement">
          <view class="setting-left">
            <text class="setting-icon">📄</text>
            <text class="setting-label">用户协议</text>
          </view>
          <view class="setting-right">
            <text class="setting-arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 其他 -->
    <view class="settings-section">
      <view class="section-title">其他</view>
      <view class="settings-items">
        <view class="setting-item clickable" @click="contactSupport">
          <view class="setting-left">
            <text class="setting-icon">💬</text>
            <text class="setting-label">联系客服</text>
          </view>
          <view class="setting-right">
            <text class="setting-arrow">›</text>
          </view>
        </view>

        <view class="setting-item clickable" @click="showFeedback">
          <view class="setting-left">
            <text class="setting-icon">📝</text>
            <text class="setting-label">意见反馈</text>
          </view>
          <view class="setting-right">
            <text class="setting-arrow">›</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      settings: {
        receiveNotification: true,
        receiveWechat: true,
        messageSound: true
      },
      cacheSize: '0 MB'
    }
  },

  methods: {
    onNotificationChange(e) {
      this.settings.receiveNotification = e.detail.value
      this.saveSettings()
    },

    onWechatChange(e) {
      this.settings.receiveWechat = e.detail.value
      this.saveSettings()
    },

    onSoundChange(e) {
      this.settings.messageSound = e.detail.value
      this.saveSettings()
    },

    saveSettings() {
      try {
        uni.setStorageSync('app_settings', this.settings)
        uni.showToast({
          title: '设置已保存',
          icon: 'success',
          duration: 1500
        })
      } catch (err) {
        console.error('保存设置失败', err)
      }
    },

    loadSettings() {
      try {
        const savedSettings = uni.getStorageSync('app_settings')
        if (savedSettings) {
          this.settings = { ...this.settings, ...savedSettings }
        }
      } catch (err) {
        console.error('加载设置失败', err)
      }
    },

    getCacheSize() {
      try {
        const info = uni.getStorageInfoSync()
        const sizeKB = info.currentSize
        if (sizeKB < 1024) {
          this.cacheSize = `${sizeKB} KB`
        } else {
          this.cacheSize = `${(sizeKB / 1024).toFixed(2)} MB`
        }
      } catch (err) {
        console.error('获取缓存大小失败', err)
        this.cacheSize = '0 MB'
      }
    },

    clearCache() {
      uni.showModal({
        title: '清除缓存',
        content: '确定要清除所有缓存数据吗？这不会影响您的登录状态。',
        success: (res) => {
          if (res.confirm) {
            try {
              // 保留登录信息和设置
              const token = uni.getStorageSync('token')
              const userInfo = uni.getStorageSync('userInfo')
              const settings = uni.getStorageSync('app_settings')

              // 清除所有缓存
              uni.clearStorageSync()

              // 恢复重要信息
              if (token) uni.setStorageSync('token', token)
              if (userInfo) uni.setStorageSync('userInfo', userInfo)
              if (settings) uni.setStorageSync('app_settings', settings)

              this.getCacheSize()

              uni.showToast({
                title: '缓存已清除',
                icon: 'success'
              })
            } catch (err) {
              console.error('清除缓存失败', err)
              uni.showToast({
                title: '清除失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },

    checkUpdate() {
      uni.showLoading({
        title: '检查中...'
      })

      setTimeout(() => {
        uni.hideLoading()
        uni.showModal({
          title: '检查更新',
          content: '当前已是最新版本 v1.0.0',
          showCancel: false
        })
      }, 1000)
    },

    showPrivacyPolicy() {
      uni.showModal({
        title: '隐私政策',
        content: '西藏电信仓库管理系统（CT-Tibet-WMS）尊重并保护所有用户的个人隐私权。我们会采取合理措施保护您的个人信息安全。',
        showCancel: false
      })
    },

    showUserAgreement() {
      uni.showModal({
        title: '用户协议',
        content: '欢迎使用西藏电信仓库管理系统。使用本系统即表示您同意遵守相关规章制度和操作规范。',
        showCancel: false
      })
    },

    contactSupport() {
      uni.showModal({
        title: '联系客服',
        content: '客服电话：400-XXX-XXXX\n工作时间：周一至周五 9:00-18:00',
        showCancel: false
      })
    },

    showFeedback() {
      uni.showModal({
        title: '意见反馈',
        content: '请通过以下方式提交反馈：\n1. 拨打客服电话\n2. 发送邮件至 feedback@example.com',
        showCancel: false
      })
    }
  },

  onLoad() {
    this.loadSettings()
    this.getCacheSize()
  }
}
</script>

<style lang="scss" scoped>
.settings-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 24rpx 32rpx;
}

.settings-section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 24rpx;
  color: #8c8c8c;
  padding: 0 16rpx 16rpx;
}

.settings-items {
  background-color: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &.clickable:active {
    background-color: #f5f5f5;
  }
}

.setting-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.setting-icon {
  font-size: 40rpx;
  margin-right: 24rpx;
}

.setting-label {
  font-size: 28rpx;
  color: #262626;
}

.setting-right {
  display: flex;
  align-items: center;
}

.setting-value {
  font-size: 26rpx;
  color: #8c8c8c;
  margin-right: 8rpx;
}

.setting-arrow {
  font-size: 40rpx;
  color: #bfbfbf;
}
</style>
