<template>
  <view class="login-container">
    <!-- 顶部品牌 -->
    <view class="login-header">
      <view class="logo">
        <text class="logo-icon">📦</text>
      </view>
      <text class="app-name">西藏电信仓库管理系统</text>
      <text class="app-desc">CT-Tibet WMS</text>
    </view>

    <!-- 账号密码登录 -->
    <view class="login-card">
      <text class="card-title">账号登录</text>

      <view :class="['input-group', { focused: focusField === 'username' }]">
        <text class="input-icon">👤</text>
        <input
          class="input"
          v-model.trim="form.username"
          placeholder="请输入用户名"
          placeholder-class="input-placeholder"
          :disabled="loading"
          @focus="focusField = 'username'"
          @blur="focusField = ''"
          @confirm="focusPassword"
        />
      </view>

      <view :class="['input-group', { focused: focusField === 'password' }]">
        <text class="input-icon">🔒</text>
        <input
          class="input"
          v-model="form.password"
          :password="!showPassword"
          placeholder="请输入密码"
          placeholder-class="input-placeholder"
          :disabled="loading"
          :focus="passwordFocus"
          @focus="focusField = 'password'"
          @blur="focusField = ''; passwordFocus = false"
          @confirm="handlePasswordLogin"
        />
        <text class="toggle-password" @click="showPassword = !showPassword">
          {{ showPassword ? '隐藏' : '显示' }}
        </text>
      </view>

      <text v-if="errorMessage" class="error-text">{{ errorMessage }}</text>

      <button
        class="login-btn"
        :loading="loading"
        :disabled="loading"
        @click="handlePasswordLogin"
      >
        {{ loading ? '登录中...' : '登 录' }}
      </button>

      <text class="help-text">首次使用请向部门管理员获取账号，默认密码登录后请及时修改</text>

      <!-- 微信一键登录：后端接口完成后开启（见 utils/constant.js 中的 WECHAT_LOGIN_ENABLED） -->
      <!-- #ifdef MP-WEIXIN -->
      <view v-if="wechatLoginEnabled" class="wechat-login">
        <view class="divider">
          <view class="divider-line"></view>
          <text class="divider-text">或</text>
          <view class="divider-line"></view>
        </view>
        <button class="wechat-btn" :disabled="loading" @click="getWechatCode">微信一键登录</button>
      </view>
      <!-- #endif -->
    </view>

    <view class="login-footer">
      <text class="footer-text">西藏电信 · 物资管理</text>
    </view>
  </view>
</template>

<script>
import api from '@/api'
import { WECHAT_LOGIN_ENABLED } from '@/utils/constant.js'

export default {
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false,
      showPassword: false,
      passwordFocus: false,
      focusField: '',
      errorMessage: '',
      wechatLoginEnabled: WECHAT_LOGIN_ENABLED
    }
  },

  methods: {
    focusPassword() {
      this.passwordFocus = true
    },

    // 账号密码登录
    async handlePasswordLogin() {
      if (this.loading) return
      this.errorMessage = ''

      if (!this.form.username) {
        this.errorMessage = '请输入用户名'
        return
      }
      if (!this.form.password) {
        this.errorMessage = '请输入密码'
        return
      }

      this.loading = true
      try {
        const res = await api.auth.login({
          username: this.form.username,
          password: this.form.password
        })
        this.onLoginSuccess(res.data)
      } catch (err) {
        // 通用错误提示已由 request.js 弹出，这里在表单内保留一条提示
        this.errorMessage = (err && err.message) || '登录失败，请重试'
        this.loading = false
      }
    },

    // 保存登录状态并进入首页
    onLoginSuccess(data) {
      const { token, user } = data || {}
      this.$store.commit('SET_TOKEN', token)
      this.$store.commit('SET_USER_INFO', user)
      this.form.password = ''
      this.loading = false

      uni.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 1000
      })
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/index/index'
        })
      }, 600)
    },

    // 微信登录：获取 code 后换取登录态
    getWechatCode() {
      this.loading = true
      uni.login({
        provider: 'weixin',
        success: async (res) => {
          if (!res.code) {
            this.loading = false
            uni.showToast({ title: '获取登录凭证失败', icon: 'none' })
            return
          }
          try {
            const result = await api.auth.wechatLogin({ code: res.code })
            this.onLoginSuccess(result.data)
          } catch (err) {
            this.loading = false
          }
        },
        fail: () => {
          this.loading = false
          uni.showToast({ title: '微信登录失败', icon: 'none' })
        }
      })
    }
  },

  onLoad() {
    // 已登录则直接进入首页
    const token = uni.getStorageSync('token')
    if (token) {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  padding: 0 48rpx;
}

.login-header {
  padding-top: 140rpx;
  padding-bottom: 64rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo {
  width: 144rpx;
  height: 144rpx;
  border-radius: 36rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
}

.logo-icon {
  font-size: 80rpx;
}

.app-name {
  font-size: 44rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 12rpx;
}

.app-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  letter-spacing: 2rpx;
}

.login-card {
  background-color: #ffffff;
  border-radius: 32rpx;
  padding: 56rpx 48rpx 48rpx;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #262626;
  margin-bottom: 40rpx;
}

.input-group {
  display: flex;
  align-items: center;
  height: 96rpx;
  padding: 0 28rpx;
  margin-bottom: 28rpx;
  background-color: #f5f6fa;
  border: 2rpx solid transparent;
  border-radius: 20rpx;
  transition: border-color 0.2s;

  &.focused {
    border-color: #667eea;
    background-color: #ffffff;
  }
}

.input-icon {
  font-size: 32rpx;
  margin-right: 20rpx;
}

.input {
  flex: 1;
  height: 96rpx;
  font-size: 30rpx;
  color: #262626;
}

.input-placeholder {
  color: #bfbfbf;
}

.toggle-password {
  font-size: 26rpx;
  color: #667eea;
  padding-left: 16rpx;
}

.error-text {
  font-size: 26rpx;
  color: #f5222d;
  margin: -8rpx 0 20rpx 8rpx;
}

.login-btn {
  width: 100%;
  height: 96rpx;
  margin-top: 16rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;

  &::after {
    border: none;
  }

  &[disabled] {
    opacity: 0.7;
    color: #ffffff;
  }
}

.help-text {
  margin-top: 28rpx;
  font-size: 24rpx;
  color: #8c8c8c;
  text-align: center;
  line-height: 1.6;
}

.wechat-login {
  margin-top: 32rpx;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 28rpx;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background-color: #e8e8e8;
}

.divider-text {
  margin: 0 20rpx;
  font-size: 24rpx;
  color: #bfbfbf;
}

.wechat-btn {
  width: 100%;
  height: 88rpx;
  background-color: #07c160;
  color: #ffffff;
  border-radius: 44rpx;
  font-size: 30rpx;
  border: none;

  &::after {
    border: none;
  }
}

.login-footer {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 60rpx;
  padding-bottom: calc(60rpx + env(safe-area-inset-bottom));
}

.footer-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}
</style>
