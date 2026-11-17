<template>
  <view class="login-container">
    <!-- 顶部装饰 -->
    <view class="login-header">
      <view class="logo-container">
        <image class="logo" src="/static/logo.png" mode="aspectFit" />
        <text class="app-name">西藏电信仓库管理系统</text>
        <text class="app-desc">CT-Tibet WMS</text>
      </view>
    </view>

    <!-- 主要内容 -->
    <view class="login-content">
      <view class="welcome-text">
        <text class="title">欢迎使用</text>
        <text class="subtitle">请使用系统账号登录</text>
      </view>

      <!-- 功能特性 -->
      <view class="features">
        <view class="feature-item">
          <text class="feature-icon">📦</text>
          <text class="feature-text">便捷物资申请</text>
        </view>
        <view class="feature-item">
          <text class="feature-icon">✅</text>
          <text class="feature-text">快速审批流程</text>
        </view>
        <view class="feature-item">
          <text class="feature-icon">📊</text>
          <text class="feature-text">实时库存查询</text>
        </view>
      </view>
    </view>

    <!-- 底部登录区域 -->
    <view class="login-footer">
      <!-- 账号密码登录（与 PC 端账号相同） -->
      <view class="login-form">
        <input
          v-model="form.username"
          class="form-input"
          placeholder="请输入用户名"
          placeholder-class="form-placeholder"
          :disabled="loading"
        />
        <input
          v-model="form.password"
          class="form-input"
          password
          placeholder="请输入密码"
          placeholder-class="form-placeholder"
          :disabled="loading"
          @confirm="handlePasswordLogin"
        />
      </view>

      <button
        class="login-btn"
        type="primary"
        :loading="loading"
        :disabled="loading"
        @click="handlePasswordLogin"
      >
        <text v-if="!loading">登录</text>
        <text v-else>登录中...</text>
      </button>

      <!-- 微信一键登录：后端接口尚未实现（需小程序 AppID/AppSecret），暂不显示 -->
      <button
        v-if="wechatLoginEnabled"
        class="login-btn wechat-btn"
        :disabled="loading"
        @click="handleWechatLogin"
      >
        <text>微信一键登录</text>
      </button>

      <view class="tips">
        <text class="tip-text">登录即表示同意</text>
        <text class="tip-link">《用户协议》</text>
        <text class="tip-text">和</text>
        <text class="tip-link">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script>
import api from '@/api'

export default {
  data() {
    return {
      loading: false,
      // 微信登录待后端接入后开启
      wechatLoginEnabled: false,
      form: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    // 账号密码登录
    async handlePasswordLogin() {
      const username = this.form.username.trim()
      if (!username || !this.form.password) {
        uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
        return
      }

      this.loading = true
      try {
        const res = await api.auth.login({ username, password: this.form.password })
        const { token, user } = res.data
        this.$store.commit('SET_TOKEN', token)
        this.$store.commit('SET_USER_INFO', user)

        uni.showToast({ title: '登录成功', icon: 'success', duration: 1000 })
        setTimeout(() => {
          this.loading = false
          uni.switchTab({ url: '/pages/index/index' })
        }, 1000)
      } catch (err) {
        // 错误提示（如用户名或密码错误）已由 request.js 展示
        this.loading = false
        console.error('登录失败', err)
      }
    },

    // 微信登录
    handleWechatLogin() {
      // 检查是否在微信环境
      // #ifdef MP-WEIXIN
      this.getWechatCode()
      // #endif

      // #ifndef MP-WEIXIN
      uni.showToast({
        title: '请在微信小程序中使用',
        icon: 'none'
      })
      // #endif
    },

    // 获取微信code
    getWechatCode() {
      this.loading = true

      uni.login({
        provider: 'weixin',
        success: (res) => {
          console.log('获取code成功', res.code)
          if (res.code) {
            // 获取用户信息授权
            this.getUserProfile(res.code)
          } else {
            this.loading = false
            uni.showToast({
              title: '获取登录凭证失败',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          this.loading = false
          console.error('获取code失败', err)
          uni.showToast({
            title: '微信登录失败',
            icon: 'none'
          })
        }
      })
    },

    // 获取用户信息
    getUserProfile(code) {
      // uni-app获取用户信息
      uni.getUserProfile({
        desc: '用于完善用户资料',
        success: (res) => {
          console.log('获取用户信息成功', res.userInfo)

          const { encryptedData, iv } = res

          // 调用后端登录接口
          this.loginToBackend(code, encryptedData, iv)
        },
        fail: (err) => {
          this.loading = false
          console.error('获取用户信息失败', err)
          uni.showToast({
            title: '需要授权才能使用',
            icon: 'none'
          })
        }
      })
    },

    // 调用后端登录接口
    async loginToBackend(code, encryptedData, iv) {
      try {
        uni.showLoading({ title: '登录中...' })

        const res = await api.auth.wechatLogin({
          code: code,
          encryptedData: encryptedData,
          iv: iv
        })

        uni.hideLoading()
        console.log('登录成功', res)

        if (res.code === 200) {
          const { token, user, isNewUser } = res.data

          // 保存token和用户信息
          this.$store.commit('SET_TOKEN', token)
          this.$store.commit('SET_USER_INFO', user)

          uni.showToast({
            title: isNewUser ? '欢迎新用户' : '登录成功',
            icon: 'success',
            duration: 1500
          })

          // 延迟跳转到首页
          setTimeout(() => {
            this.loading = false
            uni.switchTab({
              url: '/pages/index/index'
            })
          }, 1500)
        }
      } catch (err) {
        this.loading = false
        uni.hideLoading()
        console.error('登录失败', err)
        // 错误已由request.js自动处理，这里只需记录日志
      }
    }
  },

  onLoad() {
    // 检查是否已登录
    const token = uni.getStorageSync('token')
    if (token) {
      // 已登录，直接跳转到首页
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
  justify-content: space-between;
  padding: 0 48rpx;
}

.login-header {
  padding-top: 120rpx;
  display: flex;
  justify-content: center;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  background-color: #ffffff;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  margin-bottom: 32rpx;
}

.app-name {
  font-size: 48rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 16rpx;
}

.app-desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  letter-spacing: 2rpx;
}

.login-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 80rpx 0;
}

.welcome-text {
  text-align: center;
  margin-bottom: 120rpx;
}

.title {
  display: block;
  font-size: 56rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 24rpx;
}

.subtitle {
  display: block;
  font-size: 32rpx;
  color: rgba(255, 255, 255, 0.9);
}

.features {
  display: flex;
  justify-content: space-around;
  padding: 0 32rpx;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.feature-icon {
  font-size: 64rpx;
  margin-bottom: 16rpx;
}

.feature-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
}

.login-footer {
  padding-bottom: 80rpx;
  padding-bottom: calc(80rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(80rpx + env(safe-area-inset-bottom));
}

.login-btn {
  width: 100%;
  height: 96rpx;
  background-color: #ffffff;
  color: #667eea;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  border: none;

  &::after {
    border: none;
  }

  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
}

.login-form {
  width: 100%;
  margin-bottom: 32rpx;
}

.form-input {
  width: 100%;
  height: 96rpx;
  margin-bottom: 24rpx;
  padding: 0 40rpx;
  box-sizing: border-box;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 48rpx;
  font-size: 30rpx;
  color: #333333;
}

.form-placeholder {
  color: #999999;
}

.wechat-btn {
  margin-top: 24rpx;
}

.tips {
  margin-top: 32rpx;
  text-align: center;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}

.tip-text {
  color: rgba(255, 255, 255, 0.7);
}

.tip-link {
  color: #ffffff;
  text-decoration: underline;
}
</style>
