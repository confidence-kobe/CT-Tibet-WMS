<template>
  <view class="apply-list-container">
    <!-- 标签页 -->
    <view class="tabs">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        :class="['tab-item', { active: activeTab === index }]"
        @click="changeTab(index)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <text v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</text>
      </view>
    </view>

    <!-- 列表 -->
    <scroll-view
      scroll-y
      class="scroll-content"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view v-if="list.length > 0" class="apply-list">
        <view
          v-for="item in list"
          :key="item.id"
          class="apply-card"
          @click="goToDetail(item.id)"
        >
          <!-- 状态标签 -->
          <view :class="['status-tag', `status-${item.status}`]">
            <text>{{ statusMap[item.status] }}</text>
          </view>

          <!-- 申请信息 -->
          <view class="apply-info">
            <text class="apply-title">📋 {{ item.materialSummary || '物资申请' }}</text>
            <text class="apply-no">申请单号: {{ item.applyNo }}</text>
            <text class="apply-purpose">用途: {{ item.purpose }}</text>
            <text class="apply-time">申请时间: {{ item.applyTime }}</text>

            <view v-if="item.status === 1 || item.status === 3" class="approval-info">
              <text class="approval-time">审批时间: {{ item.approvalTime }}</text>
              <text v-if="item.approvalOpinion" class="approval-opinion">审批意见: {{ item.approvalOpinion }}</text>
            </view>

            <view v-if="item.status === 2" class="rejection-info">
              <text class="rejection-time">审批时间: {{ item.approvalTime }}</text>
              <text class="rejection-reason">拒绝原因: {{ item.rejectReason }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="apply-actions">
            <button v-if="item.status === 0" class="btn-action" @click.stop="cancelApply(item)">
              撤销申请
            </button>
            <button v-if="item.status === 1" class="btn-action primary" @click.stop="goToPickup(item)">
              去领取
            </button>
            <button v-if="item.status === 2" class="btn-action" @click.stop="reapply(item)">
              重新申请
            </button>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无申请记录</text>
        <button class="btn-create" @click="goToCreate">新建申请</button>
      </view>

      <!-- 加载更多 -->
      <view v-if="list.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>

      <!-- 底部安全区域 -->
      <view class="safe-area-inset-bottom"></view>
    </scroll-view>

    <!-- 浮动按钮 -->
    <view class="fab" @click="goToCreate">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script>
import { $uRequest } from '@/utils/request.js'

export default {
  data() {
    return {
      activeTab: 0,
      tabs: [
        { label: '待审批', status: 0, count: 0 },
        { label: '已通过', status: 1, count: 0 },
        { label: '已拒绝', status: 2, count: 0 },
        { label: '全部', status: null, count: 0 }
      ],
      statusMap: {
        0: '⏳ 待审批',
        1: '✓ 已通过',
        2: '✗ 已拒绝',
        3: '✓ 已出库',
        4: '⊘ 已取消'
      },
      list: [],
      loading: false,
      refreshing: false,
      pageNum: 1,
      pageSize: 20,
      noMore: false
    }
  },

  methods: {
    // 切换标签
    changeTab(index) {
      this.activeTab = index
      this.pageNum = 1
      this.list = []
      this.noMore = false
      this.loadData()
    },

    // 加载数据
    async loadData() {
      if (this.loading || this.noMore) return

      this.loading = true

      try {
        const status = this.tabs[this.activeTab].status

        const res = await $uRequest({
          url: '/api/applies',
          method: 'GET',
          data: {
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            status: status,
            applicantId: this.$store.state.userInfo?.id
          }
        })

        if (res.code === 200) {
          const { list, total } = res.data

          // 添加物资摘要
          list.forEach(item => {
            item.materialSummary = this.getMaterialSummary(item)
          })

          if (this.pageNum === 1) {
            this.list = list
          } else {
            this.list = this.list.concat(list)
          }

          // 判断是否还有更多
          if (this.list.length >= total) {
            this.noMore = true
          }
        }
      } catch (err) {
        console.error('加载数据失败', err)
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },

    // 获取物资摘要
    getMaterialSummary(item) {
      const count = item.materialCount || 0
      const first = item.firstMaterialName || '物资'
      return count > 1 ? `${first}等${count}项` : first
    },

    // 下拉刷新
    onRefresh() {
      this.refreshing = true
      this.pageNum = 1
      this.list = []
      this.noMore = false
      this.loadData()
    },

    // 加载更多
    loadMore() {
      if (!this.loading && !this.noMore) {
        this.pageNum++
        this.loadData()
      }
    },

    // 跳转到详情
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/apply/detail?id=${id}`
      })
    },

    // 跳转到新建
    goToCreate() {
      uni.navigateTo({
        url: '/pages/apply/create'
      })
    },

    // 撤销申请
    cancelApply(item) {
      uni.showModal({
        title: '提示',
        content: '确定要撤销该申请吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await $uRequest({
                url: `/api/applies/${item.id}/cancel`,
                method: 'PUT',
                data: {
                  cancelReason: '用户主动撤销'
                }
              })

              if (result.code === 200) {
                uni.showToast({
                  title: '撤销成功',
                  icon: 'success'
                })
                this.onRefresh()
              }
            } catch (err) {
              console.error('撤销申请失败', err)
            }
          }
        }
      })
    },

    // 去领取
    goToPickup(item) {
      // 跳转到待领取出库单列表
      uni.navigateTo({
        url: `/pages/outbound/pending?applyId=${item.id}`
      })
    },

    // 重新申请
    reapply(item) {
      uni.navigateTo({
        url: `/pages/apply/create?reapplyId=${item.id}`
      })
    },

    // 加载统计
    async loadStats() {
      try {
        const res = await $uRequest({
          url: '/api/stats/dashboard',
          method: 'GET'
        })

        if (res.code === 200 && res.data.myApplies) {
          const { pendingCount, approvedCount, rejectedCount } = res.data.myApplies
          this.tabs[0].count = pendingCount || 0
          this.tabs[1].count = approvedCount || 0
          this.tabs[2].count = rejectedCount || 0
        }
      } catch (err) {
        console.error('加载统计失败', err)
      }
    }
  },

  onLoad() {
    this.loadData()
    this.loadStats()
  },

  onShow() {
    // 从详情页返回时刷新
    if (this.list.length > 0) {
      this.onRefresh()
    }
  },

  onPullDownRefresh() {
    this.onRefresh()
    uni.stopPullDownRefresh()
  }
}
</script>

<style lang="scss" scoped>
.apply-list-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.tabs {
  background-color: #ffffff;
  display: flex;
  padding: 16rpx 32rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.tab-item {
  flex: 1;
  padding: 24rpx 0;
  text-align: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  &.active {
    .tab-text {
      color: #1890ff;
      font-weight: 500;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60rpx;
      height: 4rpx;
      background-color: #1890ff;
      border-radius: 2rpx;
    }
  }
}

.tab-text {
  font-size: 28rpx;
  color: #262626;
}

.tab-badge {
  margin-left: 8rpx;
  padding: 2rpx 12rpx;
  background-color: #f5222d;
  color: #ffffff;
  font-size: 20rpx;
  border-radius: 16rpx;
}

.scroll-content {
  height: calc(100vh - 112rpx);
}

.apply-list {
  padding: 24rpx 32rpx;
}

.apply-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
  position: relative;
}

.status-tag {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  padding: 8rpx 24rpx;
  border-radius: 8rpx;
  font-size: 24rpx;

  &.status-0 {
    background-color: #fff7e6;
    color: #faad14;
  }

  &.status-1 {
    background-color: #f6ffed;
    color: #52c41a;
  }

  &.status-2 {
    background-color: #fff1f0;
    color: #f5222d;
  }

  &.status-3 {
    background-color: #f5f5f5;
    color: #8c8c8c;
  }

  &.status-4 {
    background-color: #f5f5f5;
    color: #bfbfbf;
  }
}

.apply-info {
  display: flex;
  flex-direction: column;
  padding-right: 120rpx;
}

.apply-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 12rpx;
}

.apply-no,
.apply-purpose,
.apply-time {
  font-size: 24rpx;
  color: #8c8c8c;
  margin-bottom: 8rpx;
}

.approval-info,
.rejection-info {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  flex-direction: column;
}

.approval-time,
.approval-opinion,
.rejection-time,
.rejection-reason {
  font-size: 24rpx;
  margin-bottom: 8rpx;
}

.approval-time,
.rejection-time {
  color: #8c8c8c;
}

.approval-opinion {
  color: #52c41a;
}

.rejection-reason {
  color: #f5222d;
}

.apply-actions {
  margin-top: 24rpx;
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.btn-action {
  padding: 16rpx 32rpx;
  background-color: #f0f0f0;
  color: #262626;
  border: none;
  border-radius: 8rpx;
  font-size: 24rpx;

  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
  }

  &::after {
    border: none;
  }
}

.empty-state {
  padding: 200rpx 0;
  text-align: center;
}

.empty-icon {
  display: block;
  font-size: 160rpx;
  margin-bottom: 32rpx;
}

.empty-text {
  display: block;
  font-size: 28rpx;
  color: #8c8c8c;
  margin-bottom: 48rpx;
}

.btn-create {
  width: 240rpx;
  height: 72rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border: none;
  border-radius: 36rpx;
  font-size: 28rpx;

  &::after {
    border: none;
  }
}

.load-more {
  padding: 32rpx;
  text-align: center;
  font-size: 24rpx;
  color: #8c8c8c;
}

.fab {
  position: fixed;
  right: 48rpx;
  bottom: 120rpx;
  width: 112rpx;
  height: 112rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
}

.fab-icon {
  font-size: 64rpx;
  color: #ffffff;
  font-weight: 300;
}
</style>
