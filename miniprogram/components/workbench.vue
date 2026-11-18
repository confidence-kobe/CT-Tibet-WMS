<template>
  <view class="workbench">
    <!-- 待办 -->
    <view class="section">
      <text class="section-title">待办</text>
      <view class="todo-row">
        <view class="todo-card" @click="go('/pages/approval/list')">
          <text class="todo-count">{{ pendingTasks.pendingApproval || 0 }}</text>
          <text class="todo-label">待审批</text>
        </view>
        <view class="todo-card" @click="go('/pages/outbound/pending')">
          <text class="todo-count">{{ pendingTasks.pendingPickup || 0 }}</text>
          <text class="todo-label">待领取</text>
        </view>
      </view>
      <view v-if="pendingTasks.lowStockAlert > 0" class="alert-bar" @click="goInventory">
        <text>⚠️ {{ pendingTasks.lowStockAlert }} 种物资库存偏低，去看看 ›</text>
      </view>
    </view>

    <!-- 常用操作 -->
    <view class="section">
      <text class="section-title">常用操作</text>
      <view class="grid">
        <view v-for="item in actions" :key="item.url" class="grid-item" @click="go(item.url)">
          <text class="grid-icon">{{ item.icon }}</text>
          <text class="grid-label">{{ item.label }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'Workbench',

  data() {
    return {
      actions: [
        { icon: '📥', label: '快速入库', url: '/pages/inbound/create' },
        { icon: '📤', label: '快速出库', url: '/pages/outbound/create' },
        { icon: '📋', label: '入库记录', url: '/pages/inbound/list' },
        { icon: '🧾', label: '出库记录', url: '/pages/outbound/list' }
      ]
    }
  },

  computed: {
    ...mapState(['pendingTasks'])
  },

  methods: {
    // 父页面 onShow 时调用，刷新待办数量
    refresh() {
      this.$store.dispatch('getPendingTasks')
    },
    go(url) {
      uni.navigateTo({ url })
    },
    goInventory() {
      uni.switchTab({ url: '/pages/inventory/list' })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/design-system.scss";

.workbench {
  min-height: 100vh;
  box-sizing: border-box;
  padding: $spacing-lg;
  background: $bg-secondary;
}

.section {
  background: $bg-primary;
  border-radius: $radius-xl;
  box-shadow: $shadow-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.section-title {
  display: block;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.todo-row {
  display: flex;
}

.todo-card + .todo-card {
  margin-left: $spacing-md;
}

.todo-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-lg 0;
  border-radius: $radius-lg;
  background: $primary-gradient;
  box-shadow: $shadow-primary;

  &:active {
    opacity: 0.85;
  }
}

.todo-count {
  font-size: 56rpx;
  font-weight: $font-weight-bold;
  color: #ffffff;
  line-height: 1.2;
}

.todo-label {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.9);
  margin-top: $spacing-xs;
}

.alert-bar {
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-md;
  background: $warning-light;
  color: #92400e;
  font-size: $font-size-sm;
}

.grid {
  display: flex;
  flex-wrap: wrap;
}

.grid-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-md 0;
  border-radius: $radius-lg;

  &:active {
    background: $bg-secondary;
  }
}

.grid-icon {
  font-size: 48rpx;
}

.grid-label {
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-top: $spacing-xs;
}
</style>
