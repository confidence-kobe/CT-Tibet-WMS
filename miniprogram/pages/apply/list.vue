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
import api from '@/api'

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

        const res = await api.apply.getMyApplies({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: status
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
              uni.showLoading({ title: '处理中...' })

              const result = await api.apply.cancelApply(item.id)

              uni.hideLoading()

              if (result.code === 200) {
                uni.showToast({
                  title: '撤销成功',
                  icon: 'success'
                })
                this.onRefresh()
              }
            } catch (err) {
              uni.hideLoading()
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
        const res = await api.apply.getApplyStats()

        if (res.code === 200) {
          const { pendingCount, approvedCount, rejectedCount } = res.data
          this.tabs[0].count = pendingCount || 0
          this.tabs[1].count = approvedCount || 0
          this.tabs[2].count = rejectedCount || 0
        }
      } catch (err) {
        console.error('加载统计失败', err)
      }
    }
  },

  onLoad(options) {
    if (options && options.status !== undefined) {
      const statusVal = parseInt(options.status)
      const tabIndex = this.tabs.findIndex(t => t.status === statusVal)
      if (tabIndex >= 0) this.activeTab = tabIndex
    }
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
@import "@/styles/design-system.scss";

.apply-list-container {
  min-height: 100vh;
  @include gradient-mesh;
  background-color: $bg-secondary;
}

.tabs {
  background: $bg-primary;
  display: flex;
  padding: $spacing-sm $spacing-lg 0;
  position: relative;
  box-shadow: $shadow-sm;

  // 渐变装饰线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4rpx;
    @include gradient-primary;
  }
}

.tab-item {
  flex: 1;
  padding: $spacing-md 0;
  text-align: center;
  position: relative;
  @include flex-center;
  @include smooth-transition(all);
  cursor: pointer;

  &:active {
    opacity: 0.7;
  }

  &.active {
    .tab-text {
      background: $primary-gradient;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      font-weight: $font-weight-semibold;
      transform: scale(1.05);
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 64rpx;
      height: 4rpx;
      @include gradient-primary;
      border-radius: $radius-full;
      box-shadow: 0 2rpx 8rpx rgba($primary, 0.3);
    }
  }
}

.tab-text {
  font-size: $font-size-base;
  color: $text-primary;
  @include smooth-transition(all);
}

.tab-badge {
  margin-left: $spacing-xs;
  padding: 4rpx 12rpx;
  @include gradient-secondary;
  color: #ffffff;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  border-radius: $radius-full;
  box-shadow: 0 2rpx 8rpx rgba($error, 0.3);
  @include pulse-animation;
}

.scroll-content {
  height: calc(100vh - 112rpx);
}

.apply-list {
  padding: $spacing-md $spacing-lg;
}

.apply-card {
  @include card-base;
  margin-bottom: $spacing-md;
  position: relative;
  overflow: hidden;
  border-left: 4rpx solid transparent;
  @include fade-in;

  // 依次出现动画
  @for $i from 1 through 10 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.05}s;
    }
  }

  // 左侧状态彩条
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4rpx;
    @include smooth-transition(width);
  }

  &:active {
    transform: scale(0.98);

    &::before {
      width: 8rpx;
    }
  }

  // 根据状态设置彩条颜色
  &:has(.status-0)::before {
    @include gradient-warning;
  }

  &:has(.status-1)::before {
    @include gradient-success;
  }

  &:has(.status-2)::before {
    background: $error;
  }

  &:has(.status-3)::before,
  &:has(.status-4)::before {
    background: $gray-300;
  }
}

.status-tag {
  position: absolute;
  top: $spacing-md;
  right: $spacing-md;
  padding: $spacing-xs $spacing-md;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  box-shadow: $shadow-sm;
  @include smooth-transition(transform);

  &:active {
    transform: scale(0.95);
  }

  &.status-0 {
    background: $warning-light;
    color: $warning;
    border: 2rpx solid rgba($warning, 0.2);
  }

  &.status-1 {
    background: $success-light;
    color: $success;
    border: 2rpx solid rgba($success, 0.2);
  }

  &.status-2 {
    background: $error-light;
    color: $error;
    border: 2rpx solid rgba($error, 0.2);
  }

  &.status-3 {
    background: $gray-100;
    color: $text-secondary;
    border: 2rpx solid $gray-200;
  }

  &.status-4 {
    background: $gray-100;
    color: $text-tertiary;
    border: 2rpx solid $gray-200;
  }
}

.apply-info {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  padding-right: 140rpx;
}

.apply-title {
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  @include text-ellipsis;
  margin-bottom: $spacing-xs;
}

.apply-no,
.apply-purpose,
.apply-time {
  font-size: $font-size-sm;
  color: $text-secondary;
  line-height: $line-height-relaxed;
}

.approval-info,
.rejection-info {
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  @include divider(0);
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  background: linear-gradient(135deg, $bg-secondary 0%, $gray-50 100%);
  padding: $spacing-md;
  border-radius: $radius-md;
  margin-left: -$spacing-lg;
  margin-right: -$spacing-lg;
  margin-bottom: -$spacing-lg;
}

.approval-time,
.approval-opinion,
.rejection-time,
.rejection-reason {
  font-size: $font-size-sm;
  line-height: $line-height-relaxed;
}

.approval-time,
.rejection-time {
  color: $text-tertiary;
}

.approval-opinion {
  color: $success;
  font-weight: $font-weight-medium;
}

.rejection-reason {
  color: $error;
  font-weight: $font-weight-medium;
}

.apply-actions {
  margin-top: $spacing-lg;
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}

.btn-action {
  padding: $spacing-sm $spacing-lg;
  background: $bg-secondary;
  color: $text-primary;
  border: 2rpx solid $gray-200;
  border-radius: $radius-full;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  @include smooth-transition(all);

  &:active {
    transform: scale(0.95);
    background: $gray-200;
  }

  &.primary {
    @include btn-primary;
    padding: $spacing-sm $spacing-xl;
    box-shadow: $shadow-primary;

    &:active {
      box-shadow: $shadow-sm;
    }
  }

  &::after {
    border: none;
  }
}

.empty-state {
  @include flex-center;
  flex-direction: column;
  padding: $spacing-3xl 0;
  opacity: 0.8;
}

.empty-icon {
  display: block;
  font-size: 160rpx;
  margin-bottom: $spacing-lg;
  filter: grayscale(50%);
  @include float-animation;
}

.empty-text {
  display: block;
  font-size: $font-size-lg;
  color: $text-secondary;
  margin-bottom: $spacing-2xl;
  font-weight: $font-weight-medium;
}

.btn-create {
  width: 280rpx;
  height: 88rpx;
  @include btn-primary;
  font-size: $font-size-base;
  box-shadow: $shadow-primary;

  &:active {
    transform: scale(0.95);
  }

  &::after {
    border: none;
  }
}

.load-more {
  padding: $spacing-xl;
  text-align: center;
  font-size: $font-size-sm;
  color: $text-tertiary;
}

.fab {
  position: fixed;
  right: $spacing-xl;
  bottom: 140rpx;
  width: 120rpx;
  height: 120rpx;
  @include gradient-primary;
  border-radius: $radius-full;
  @include flex-center;
  box-shadow: $shadow-primary, 0 16rpx 32rpx rgba($primary, 0.3);
  @include smooth-transition(all);
  z-index: 100;

  &::before {
    content: '';
    position: absolute;
    inset: -8rpx;
    border-radius: $radius-full;
    background: $primary-gradient;
    opacity: 0;
    @include smooth-transition(opacity);
  }

  &:active {
    transform: scale(0.9);
    box-shadow: $shadow-md;

    &::before {
      opacity: 0.3;
    }
  }
}

.fab-icon {
  font-size: 72rpx;
  color: #ffffff;
  font-weight: 300;
  position: relative;
  z-index: 1;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.2));
}
</style>
