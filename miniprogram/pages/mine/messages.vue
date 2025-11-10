<template>
  <view class="messages-container">
    <!-- 头部统计 -->
    <view class="header-stats">
      <view class="stat-item">
        <text class="stat-value">{{ unreadCount }}</text>
        <text class="stat-label">未读消息</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-value">{{ totalCount }}</text>
        <text class="stat-label">全部消息</text>
      </view>
    </view>

    <!-- 操作栏 -->
    <view class="action-bar">
      <view class="filter-tabs">
        <view
          v-for="(tab, index) in tabs"
          :key="index"
          :class="['tab-item', { active: activeTab === index }]"
          @click="changeTab(index)"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <view v-if="activeTab === index" class="tab-indicator"></view>
        </view>
      </view>
      <view v-if="unreadCount > 0" class="mark-all-btn" @click="markAllRead">
        <text>全部已读</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      scroll-y
      class="message-list"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="messages.length === 0" class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无消息</text>
      </view>

      <view v-else class="message-items">
        <view
          v-for="(item, index) in messages"
          :key="item.id"
          :class="['message-item', { unread: item.isRead === 0 }]"
          @click="handleMessageClick(item)"
        >
          <view :class="['message-icon', `type-${item.type}`]">
            <text>{{ getTypeIcon(item.type) }}</text>
          </view>
          <view class="message-content">
            <view class="message-header">
              <text class="message-title">{{ item.title }}</text>
              <text class="message-time">{{ formatTime(item.createTime) }}</text>
            </view>
            <text class="message-desc">{{ item.content }}</text>
            <view v-if="item.relatedData" class="message-footer">
              <text class="related-tag">{{ getRelatedLabel(item.type) }}</text>
              <text class="related-no">{{ item.relatedData.no }}</text>
            </view>
          </view>
          <view v-if="item.isRead === 0" class="unread-dot"></view>
        </view>
      </view>

      <view v-if="hasMore" class="loading-more">
        <text>加载中...</text>
      </view>
      <view v-else-if="messages.length > 0" class="no-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { $uRequest } from '@/utils/request.js'
import { mapState } from 'vuex'

export default {
  data() {
    return {
      activeTab: 0,
      tabs: [
        { label: '全部', isRead: null },
        { label: '未读', isRead: 0 },
        { label: '已读', isRead: 1 }
      ],
      messages: [],
      unreadCount: 0,
      totalCount: 0,
      page: 1,
      pageSize: 20,
      hasMore: true,
      refreshing: false,
      loading: false
    }
  },

  computed: {
    ...mapState(['userInfo'])
  },

  methods: {
    async loadData(isRefresh = false) {
      if (this.loading && !isRefresh) return

      if (isRefresh) {
        this.page = 1
        this.hasMore = true
        this.messages = []
      }

      this.loading = true

      try {
        const currentTab = this.tabs[this.activeTab]
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          isRead: currentTab.isRead
        }

        const res = await $uRequest({
          url: '/api/messages',
          method: 'GET',
          data: params
        })

        if (res.code === 200) {
          const newMessages = res.data.records || []

          if (isRefresh) {
            this.messages = newMessages
          } else {
            this.messages = [...this.messages, ...newMessages]
          }

          this.unreadCount = res.data.unreadCount || 0
          this.totalCount = res.data.total || 0
          this.hasMore = newMessages.length === this.pageSize

          // 更新 Vuex 中的未读数
          this.$store.commit('SET_UNREAD_COUNT', this.unreadCount)
        }
      } catch (err) {
        console.error('加载消息失败', err)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },

    changeTab(index) {
      if (this.activeTab === index) return
      this.activeTab = index
      this.loadData(true)
    },

    onRefresh() {
      this.refreshing = true
      this.loadData(true)
    },

    loadMore() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadData()
    },

    async handleMessageClick(item) {
      // 标记为已读
      if (item.isRead === 0) {
        try {
          await $uRequest({
            url: `/api/messages/${item.id}/read`,
            method: 'PUT'
          })

          item.isRead = 1
          this.unreadCount = Math.max(0, this.unreadCount - 1)
          this.$store.commit('SET_UNREAD_COUNT', this.unreadCount)
        } catch (err) {
          console.error('标记已读失败', err)
        }
      }

      // 跳转到相关页面
      if (item.relatedData) {
        this.navigateToRelated(item)
      }
    },

    navigateToRelated(item) {
      const { type, relatedData } = item

      switch (type) {
        case 1: // 申请审批
          if (this.userInfo.roleCode === 'WAREHOUSE' || this.userInfo.roleCode === 'DEPT_ADMIN') {
            uni.navigateTo({
              url: `/pages/approval/detail?id=${relatedData.id}`
            })
          } else {
            uni.navigateTo({
              url: `/pages/apply/detail?id=${relatedData.id}`
            })
          }
          break
        case 2: // 出库通知
          uni.navigateTo({
            url: `/pages/outbound/pending?highlightId=${relatedData.id}`
          })
          break
        case 3: // 系统通知
          // 系统通知一般不跳转
          break
        default:
          break
      }
    },

    async markAllRead() {
      if (this.unreadCount === 0) return

      uni.showModal({
        title: '提示',
        content: '确定将所有消息标记为已读吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await $uRequest({
                url: '/api/messages/read-all',
                method: 'PUT'
              })

              // 更新列表中的消息状态
              this.messages.forEach(item => {
                item.isRead = 1
              })

              this.unreadCount = 0
              this.$store.commit('SET_UNREAD_COUNT', 0)

              uni.showToast({
                title: '已全部标记为已读',
                icon: 'success'
              })
            } catch (err) {
              console.error('标记失败', err)
              uni.showToast({
                title: '操作失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },

    getTypeIcon(type) {
      const icons = {
        1: '📋', // 申请审批
        2: '📦', // 出库通知
        3: '🔔', // 系统通知
        4: '⚠️'  // 警告提醒
      }
      return icons[type] || '💬'
    },

    getRelatedLabel(type) {
      const labels = {
        1: '申请单',
        2: '出库单',
        3: '系统',
        4: '提醒'
      }
      return labels[type] || ''
    },

    formatTime(timeStr) {
      if (!timeStr) return ''

      const now = new Date()
      const time = new Date(timeStr)
      const diff = now - time

      // 1分钟内
      if (diff < 60000) {
        return '刚刚'
      }
      // 1小时内
      if (diff < 3600000) {
        return `${Math.floor(diff / 60000)}分钟前`
      }
      // 今天
      if (now.toDateString() === time.toDateString()) {
        return time.toTimeString().slice(0, 5)
      }
      // 昨天
      const yesterday = new Date(now)
      yesterday.setDate(yesterday.getDate() - 1)
      if (yesterday.toDateString() === time.toDateString()) {
        return `昨天 ${time.toTimeString().slice(0, 5)}`
      }
      // 本年
      if (now.getFullYear() === time.getFullYear()) {
        return `${time.getMonth() + 1}-${time.getDate()} ${time.toTimeString().slice(0, 5)}`
      }
      // 往年
      return timeStr.slice(0, 16)
    }
  },

  onLoad() {
    this.loadData(true)
  },

  onShow() {
    // 刷新数据
    this.loadData(true)
  }
}
</script>

<style lang="scss" scoped>
.messages-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header-stats {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48rpx 32rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 56rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}

.stat-divider {
  width: 2rpx;
  height: 80rpx;
  background-color: rgba(255, 255, 255, 0.3);
}

.action-bar {
  background-color: #ffffff;
  padding: 24rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.filter-tabs {
  display: flex;
  gap: 32rpx;
}

.tab-item {
  position: relative;
  padding-bottom: 8rpx;
}

.tab-text {
  font-size: 28rpx;
  color: #8c8c8c;
  transition: color 0.3s;
}

.tab-item.active .tab-text {
  color: #667eea;
  font-weight: 500;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2rpx;
}

.mark-all-btn {
  padding: 8rpx 24rpx;
  background-color: #f0f0f0;
  border-radius: 32rpx;
  font-size: 24rpx;
  color: #595959;

  &:active {
    opacity: 0.8;
  }
}

.message-list {
  height: calc(100vh - 340rpx);
  padding: 0 32rpx 24rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #8c8c8c;
}

.message-items {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.message-item {
  position: relative;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  gap: 24rpx;
  transition: background-color 0.3s;

  &.unread {
    background-color: #f6f8ff;
  }

  &:active {
    opacity: 0.8;
  }
}

.message-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  flex-shrink: 0;

  &.type-1 {
    background-color: #e6f7ff;
  }

  &.type-2 {
    background-color: #f6ffed;
  }

  &.type-3 {
    background-color: #fff7e6;
  }

  &.type-4 {
    background-color: #fff1f0;
  }
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;
  flex: 1;
}

.message-time {
  font-size: 22rpx;
  color: #bfbfbf;
  margin-left: 16rpx;
}

.message-desc {
  font-size: 26rpx;
  color: #595959;
  line-height: 1.6;
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 4rpx;
}

.related-tag {
  padding: 4rpx 12rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
  font-size: 20rpx;
  color: #8c8c8c;
}

.related-no {
  font-size: 22rpx;
  color: #1890ff;
}

.unread-dot {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background-color: #f5222d;
}

.loading-more,
.no-more {
  text-align: center;
  padding: 32rpx 0;
  font-size: 24rpx;
  color: #bfbfbf;
}
</style>
