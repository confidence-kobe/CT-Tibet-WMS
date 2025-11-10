<script>
export default {
  onLaunch: function() {
    console.log('App Launch')

    // 检查登录状态
    this.checkLogin()

    // 获取系统信息
    this.getSystemInfo()
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  },
  methods: {
    checkLogin() {
      const token = uni.getStorageSync('token')
      const userInfo = uni.getStorageSync('userInfo')

      if (token && userInfo) {
        // 已登录，更新store
        this.$store.commit('SET_TOKEN', token)
        this.$store.commit('SET_USER_INFO', userInfo)
      }
    },
    getSystemInfo() {
      uni.getSystemInfo({
        success: (res) => {
          this.$store.commit('SET_SYSTEM_INFO', {
            platform: res.platform,
            system: res.system,
            statusBarHeight: res.statusBarHeight,
            windowHeight: res.windowHeight,
            windowWidth: res.windowWidth,
            safeAreaInsets: res.safeAreaInsets
          })
        }
      })
    }
  }
}
</script>

<style lang="scss">
/* uni.scss */
@import '@/uni.scss';

/* 全局样式 */
page {
  background-color: #fafafa;
  font-size: 28rpx;
  color: #262626;
}

/* 重置样式 */
view, text, button, input, textarea {
  box-sizing: border-box;
}

/* 通用间距 */
.mt-xs { margin-top: 8rpx; }
.mt-sm { margin-top: 16rpx; }
.mt-md { margin-top: 24rpx; }
.mt-lg { margin-top: 32rpx; }
.mt-xl { margin-top: 48rpx; }

.mb-xs { margin-bottom: 8rpx; }
.mb-sm { margin-bottom: 16rpx; }
.mb-md { margin-bottom: 24rpx; }
.mb-lg { margin-bottom: 32rpx; }
.mb-xl { margin-bottom: 48rpx; }

.ml-xs { margin-left: 8rpx; }
.ml-sm { margin-left: 16rpx; }
.ml-md { margin-left: 24rpx; }
.ml-lg { margin-left: 32rpx; }

.mr-xs { margin-right: 8rpx; }
.mr-sm { margin-right: 16rpx; }
.mr-md { margin-right: 24rpx; }
.mr-lg { margin-right: 32rpx; }

.pt-xs { padding-top: 8rpx; }
.pt-sm { padding-top: 16rpx; }
.pt-md { padding-top: 24rpx; }
.pt-lg { padding-top: 32rpx; }

.pb-xs { padding-bottom: 8rpx; }
.pb-sm { padding-bottom: 16rpx; }
.pb-md { padding-bottom: 24rpx; }
.pb-lg { padding-bottom: 32rpx; }

.pl-xs { padding-left: 8rpx; }
.pl-sm { padding-left: 16rpx; }
.pl-md { padding-left: 24rpx; }
.pl-lg { padding-left: 32rpx; }

.pr-xs { padding-right: 8rpx; }
.pr-sm { padding-right: 16rpx; }
.pr-md { padding-right: 24rpx; }
.pr-lg { padding-right: 32rpx; }

.px-sm { padding-left: 16rpx; padding-right: 16rpx; }
.px-md { padding-left: 24rpx; padding-right: 24rpx; }
.px-lg { padding-left: 32rpx; padding-right: 32rpx; }

.py-sm { padding-top: 16rpx; padding-bottom: 16rpx; }
.py-md { padding-top: 24rpx; padding-bottom: 24rpx; }
.py-lg { padding-top: 32rpx; padding-bottom: 32rpx; }

/* 文本对齐 */
.text-left { text-align: left; }
.text-center { text-align: center; }
.text-right { text-align: right; }

/* 文本颜色 */
.text-primary { color: #1890ff; }
.text-success { color: #52c41a; }
.text-warning { color: #faad14; }
.text-danger { color: #f5222d; }
.text-info { color: #8c8c8c; }
.text-muted { color: #bfbfbf; }

/* Flex布局 */
.flex { display: flex; }
.flex-row { flex-direction: row; }
.flex-column { flex-direction: column; }
.flex-wrap { flex-wrap: wrap; }
.flex-center { justify-content: center; align-items: center; }
.flex-between { justify-content: space-between; }
.flex-around { justify-content: space-around; }
.flex-start { justify-content: flex-start; }
.flex-end { justify-content: flex-end; }
.align-center { align-items: center; }
.align-start { align-items: flex-start; }
.align-end { align-items: flex-end; }
.flex-1 { flex: 1; }

/* 字体大小 */
.text-xs { font-size: 20rpx; }
.text-sm { font-size: 24rpx; }
.text-md { font-size: 28rpx; }
.text-lg { font-size: 32rpx; }
.text-xl { font-size: 36rpx; }
.text-xxl { font-size: 40rpx; }

/* 字体粗细 */
.font-normal { font-weight: 400; }
.font-bold { font-weight: 500; }
.font-bolder { font-weight: 600; }

/* 通用卡片 */
.card {
  background-color: #ffffff;
  border-radius: 8rpx;
  padding: 24rpx;
  margin: 16rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.05);
}

/* 分割线 */
.divider {
  height: 1rpx;
  background-color: #f0f0f0;
  margin: 16rpx 0;
}

/* 按钮样式 */
.btn {
  border-radius: 8rpx;
  font-size: 28rpx;
  padding: 20rpx 32rpx;
  text-align: center;
}

.btn-primary {
  background-color: #1890ff;
  color: #ffffff;
}

.btn-default {
  background-color: #ffffff;
  color: #262626;
  border: 1rpx solid #d9d9d9;
}

.btn-danger {
  background-color: #f5222d;
  color: #ffffff;
}

/* 状态标签 */
.status-tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 4rpx;
  font-size: 24rpx;
}

.status-pending {
  background-color: #fff7e6;
  color: #faad14;
}

.status-approved {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background-color: #fff1f0;
  color: #f5222d;
}

.status-completed {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

.status-canceled {
  background-color: #f5f5f5;
  color: #bfbfbf;
}

/* 空状态 */
.empty-state {
  padding: 120rpx 0;
  text-align: center;
  color: #8c8c8c;
  font-size: 28rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

/* 加载更多 */
.load-more {
  padding: 32rpx;
  text-align: center;
  color: #8c8c8c;
  font-size: 24rpx;
}

/* 安全区域 */
.safe-area-inset-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
