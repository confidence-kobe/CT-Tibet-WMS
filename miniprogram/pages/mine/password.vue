<template>
  <view class="password-container">
    <view class="form-section">
      <view class="form-item">
        <text class="label">原密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="formData.oldPassword" 
          placeholder="请输入原密码" 
        />
      </view>
      
      <view class="form-item">
        <text class="label">新密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="formData.newPassword" 
          placeholder="请输入新密码(6-20位)" 
        />
      </view>

      <view class="form-item">
        <text class="label">确认新密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="formData.confirmPassword" 
          placeholder="请再次输入新密码" 
        />
      </view>
    </view>

    <view class="tips">
      密码修改成功后，需重新登录。
    </view>

    <view class="submit-section">
      <button class="submit-btn" :loading="loading" @click="handleSubmit">提交修改</button>
    </view>
  </view>
</template>

<script>
import api from '@/api'

export default {
  data() {
    return {
      formData: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      loading: false
    }
  },
  methods: {
    async handleSubmit() {
      const { oldPassword, newPassword, confirmPassword } = this.formData
      
      if (!oldPassword) return uni.showToast({ title: '请输入原密码', icon: 'none' })
      if (!newPassword || newPassword.length < 6) return uni.showToast({ title: '新密码至少6位', icon: 'none' })
      if (newPassword !== confirmPassword) return uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
      if (oldPassword === newPassword) return uni.showToast({ title: '新密码不能与原密码相同', icon: 'none' })

      this.loading = true
      try {
        const res = await api.user.changePassword({
          oldPassword,
          newPassword,
          confirmPassword
        })

        if (res.code === 200) {
          uni.showToast({ title: '密码修改成功', icon: 'success' })
          
          setTimeout(() => {
            // 清除状态并跳转登录
            this.$store.commit('LOGOUT')
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }, 1500)
        }
      } catch (err) {
        console.error('修改密码失败', err)
      } finally {
        this.loading = true
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.password-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 32rpx;
}

.form-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 0 24rpx;
  margin-bottom: 24rpx;
}

.form-item {
  padding: 32rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  display: flex;
  align-items: center;

  &:last-child {
    border-bottom: none;
  }

  .label {
    width: 180rpx;
    font-size: 28rpx;
    color: #262626;
  }

  .input {
    flex: 1;
    font-size: 28rpx;
  }
}

.tips {
  font-size: 24rpx;
  color: #8c8c8c;
  padding: 0 8rpx;
}

.submit-section {
  margin-top: 64rpx;
}

.submit-btn {
  height: 88rpx;
  line-height: 88rpx;
  background-color: #667eea;
  color: #ffffff;
  border-radius: 44rpx;
  font-size: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);

  &::after {
    border: none;
  }

  &:active {
    opacity: 0.9;
    transform: scale(0.98);
  }
}
</style>
