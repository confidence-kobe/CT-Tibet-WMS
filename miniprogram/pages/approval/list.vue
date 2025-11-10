<template>
  <view class="approval-container">
    <!-- 统计信息 -->
    <view class="stats-header">
      <text class="stats-title">待审批</text>
      <text class="stats-count">{{ pendingCount }}</text>
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
      <view v-if="list.length > 0" class="approval-list">
        <view v-for="item in list" :key="item.id" class="approval-card">
          <view class="card-header">
            <view class="applicant-info">
              <text class="applicant-name">📋 {{ item.applicantName }}的申请</text>
              <text class="applicant-phone">{{ item.applicantPhone }}</text>
            </view>
            <text v-if="item.isTimeout" class="timeout-tag">⚠️ 超时</text>
          </view>

          <view class="card-body">
            <view class="info-row">
              <text class="info-label">申请单号:</text>
              <text class="info-value">{{ item.applyNo }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">申请时间:</text>
              <text class="info-value">{{ item.applyTime }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">用途:</text>
              <text class="info-value">{{ item.purpose }}</text>
            </view>

            <view class="materials-section">
              <text class="materials-title">申请物资:</text>
              <view class="materials-list">
                <view v-for="(detail, index) in item.details" :key="index" class="material-item">
                  <text class="material-name">{{ detail.materialName }} {{ detail.quantity }}{{ detail.unit }}</text>
                  <text :class="['stock-status', detail.isStockSufficient ? 'sufficient' : 'insufficient']">
                    库存: {{ detail.currentStock }}{{ detail.unit }}
                    {{ detail.isStockSufficient ? '✓' : '⚠️' }}
                  </text>
                </view>
              </view>
            </view>
          </view>

          <view class="card-footer">
            <button class="btn-action" @click="goToDetail(item.id)">查看详情</button>
            <button class="btn-action danger" @click="rejectApply(item)">拒绝</button>
            <button class="btn-action primary" @click="approveApply(item)">通过</button>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无待审批申请</text>
      </view>

      <view v-if="list.length > 0" class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>

      <view class="safe-area-inset-bottom"></view>
    </scroll-view>

    <!-- 审批弹窗 -->
    <view v-if="showApprovalModal" class="modal" @click="hideApprovalModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ approvalType === 'approve' ? '审批通过' : '拒绝申请' }}</text>
          <text class="modal-close" @click="hideApprovalModal">✕</text>
        </view>

        <view class="modal-body">
          <view class="apply-info">
            <text class="info-text">申请人: {{ currentApply.applicantName }}</text>
            <text class="info-text">申请单号: {{ currentApply.applyNo }}</text>
          </view>

          <view v-if="approvalType === 'approve'" class="stock-check">
            <text :class="['check-result', allStockSufficient ? 'success' : 'warning']">
              {{ allStockSufficient ? '✓ 库存检查: 库存充足' : '⚠️ 库存检查: 部分物资库存不足' }}
            </text>
          </view>

          <view class="form-item">
            <text class="form-label">{{ approvalType === 'approve' ? '审批意见:' : '拒绝原因:' }}</text>
            <textarea
              class="form-textarea"
              v-model="approvalForm.opinion"
              :placeholder="approvalType === 'approve' ? '请输入审批意见（选填）' : '请输入拒绝原因（必填）'"
              maxlength="200"
            />
          </view>

          <view class="modal-actions">
            <button class="btn-modal" @click="hideApprovalModal">取消</button>
            <button
              :class="['btn-modal', approvalType === 'approve' ? 'primary' : 'danger']"
              :loading="submitting"
              @click="submitApproval"
            >
              确定
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { $uRequest } from '@/utils/request.js'

export default {
  data() {
    return {
      list: [],
      pendingCount: 0,
      loading: false,
      refreshing: false,
      pageNum: 1,
      pageSize: 20,
      noMore: false,
      // 审批弹窗
      showApprovalModal: false,
      approvalType: 'approve', // approve | reject
      currentApply: {},
      approvalForm: {
        opinion: ''
      },
      submitting: false
    }
  },

  computed: {
    allStockSufficient() {
      if (!this.currentApply.details) return true
      return this.currentApply.details.every(item => item.isStockSufficient)
    }
  },

  methods: {
    async loadData() {
      if (this.loading || this.noMore) return

      this.loading = true

      try {
        const res = await $uRequest({
          url: '/api/applies',
          method: 'GET',
          data: {
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            status: 0 // 待审批
          }
        })

        if (res.code === 200) {
          const { list, total } = res.data

          if (this.pageNum === 1) {
            this.list = list
          } else {
            this.list = this.list.concat(list)
          }

          this.pendingCount = total

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

    onRefresh() {
      this.refreshing = true
      this.pageNum = 1
      this.list = []
      this.noMore = false
      this.loadData()
    },

    loadMore() {
      if (!this.loading && !this.noMore) {
        this.pageNum++
        this.loadData()
      }
    },

    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/approval/detail?id=${id}`
      })
    },

    // 审批通过
    async approveApply(item) {
      // 获取详情（包含库存检查）
      try {
        const res = await $uRequest({
          url: `/api/applies/${item.id}`,
          method: 'GET'
        })

        if (res.code === 200) {
          this.currentApply = res.data
          this.approvalType = 'approve'
          this.approvalForm.opinion = ''
          this.showApprovalModal = true
        }
      } catch (err) {
        console.error('获取详情失败', err)
      }
    },

    // 拒绝申请
    rejectApply(item) {
      this.currentApply = item
      this.approvalType = 'reject'
      this.approvalForm.opinion = ''
      this.showApprovalModal = true
    },

    hideApprovalModal() {
      this.showApprovalModal = false
    },

    // 提交审批
    async submitApproval() {
      if (this.approvalType === 'reject' && !this.approvalForm.opinion.trim()) {
        uni.showToast({
          title: '请输入拒绝原因',
          icon: 'none'
        })
        return
      }

      this.submitting = true

      try {
        const res = await $uRequest({
          url: `/api/applies/${this.currentApply.id}/approve`,
          method: 'PUT',
          data: {
            result: this.approvalType === 'approve' ? 1 : 2,
            opinion: this.approvalForm.opinion.trim(),
            rejectReason: this.approvalType === 'reject' ? this.approvalForm.opinion.trim() : ''
          }
        })

        if (res.code === 200) {
          uni.showToast({
            title: this.approvalType === 'approve' ? '审批通过' : '已拒绝',
            icon: 'success',
            duration: 2000
          })

          this.hideApprovalModal()

          // 刷新列表
          setTimeout(() => {
            this.onRefresh()
          }, 1500)
        }
      } catch (err) {
        console.error('审批失败', err)
      } finally {
        this.submitting = false
      }
    }
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
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
.approval-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.stats-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stats-title {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16rpx;
}

.stats-count {
  font-size: 96rpx;
  font-weight: 600;
  color: #ffffff;
}

.scroll-content {
  height: calc(100vh - 240rpx);
}

.approval-list {
  padding: 24rpx 32rpx;
}

.approval-card {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.applicant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.applicant-name {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8rpx;
}

.applicant-phone {
  font-size: 24rpx;
  color: #8c8c8c;
}

.timeout-tag {
  padding: 8rpx 16rpx;
  background-color: #fff7e6;
  color: #faad14;
  font-size: 24rpx;
  border-radius: 8rpx;
}

.card-body {
  margin-bottom: 24rpx;
}

.info-row {
  display: flex;
  margin-bottom: 12rpx;
}

.info-label {
  font-size: 24rpx;
  color: #8c8c8c;
  width: 120rpx;
}

.info-value {
  flex: 1;
  font-size: 24rpx;
  color: #262626;
}

.materials-section {
  margin-top: 24rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.materials-title {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 16rpx;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.material-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx;
  background-color: #fafafa;
  border-radius: 8rpx;
}

.material-name {
  font-size: 26rpx;
  color: #262626;
}

.stock-status {
  font-size: 24rpx;

  &.sufficient { color: #52c41a; }
  &.insufficient { color: #faad14; }
}

.card-footer {
  display: flex;
  gap: 16rpx;
  justify-content: flex-end;
}

.btn-action {
  padding: 16rpx 32rpx;
  border: none;
  border-radius: 8rpx;
  font-size: 26rpx;
  background-color: #f0f0f0;
  color: #262626;

  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
  }

  &.danger {
    background-color: #fff1f0;
    color: #f5222d;
  }

  &::after { border: none; }
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 85%;
  max-height: 70vh;
  background-color: #ffffff;
  border-radius: 16rpx;
}

.modal-header {
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
}

.modal-close {
  font-size: 40rpx;
  color: #8c8c8c;
}

.modal-body {
  padding: 32rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.apply-info {
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.info-text {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 8rpx;

  &:last-child { margin-bottom: 0; }
}

.stock-check {
  margin-bottom: 24rpx;
}

.check-result {
  display: block;
  padding: 16rpx;
  border-radius: 8rpx;
  font-size: 26rpx;

  &.success {
    background-color: #f6ffed;
    color: #52c41a;
  }

  &.warning {
    background-color: #fff7e6;
    color: #faad14;
  }
}

.form-item {
  margin-bottom: 24rpx;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 16rpx;
}

.form-textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 16rpx;
  background-color: #fafafa;
  border-radius: 8rpx;
  font-size: 26rpx;
  line-height: 1.6;
}

.modal-actions {
  display: flex;
  gap: 16rpx;
}

.btn-modal {
  flex: 1;
  height: 72rpx;
  border: none;
  border-radius: 36rpx;
  font-size: 28rpx;
  background-color: #f0f0f0;
  color: #262626;

  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
  }

  &.danger {
    background-color: #f5222d;
    color: #ffffff;
  }

  &::after { border: none; }
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
  font-size: 28rpx;
  color: #8c8c8c;
}

.load-more {
  padding: 32rpx;
  text-align: center;
  font-size: 24rpx;
  color: #8c8c8c;
}
</style>
