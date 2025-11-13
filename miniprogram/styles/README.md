# 🎨 样式系统快速上手

## 📦 已完成的现代化改造

### ✅ 首页（pages/index/index.vue）

已完成现代UI升级，包括：
- 渐变头部with装饰球
- 玻璃拟态效果
- 渐变数字统计
- 水波纹按钮
- 浮动动画图标
- 卡片入场动画

### 🎯 快速应用到其他页面

只需3步，让任何页面变现代！

---

## 🚀 方法1: 快速升级现有页面

### 步骤1: 引入设计系统

在页面的 `<style>` 标签内：

```vue
<style lang="scss" scoped>
@import "@/styles/design-system.scss";
```

### 步骤2: 替换通用样式

```scss
// ❌ 旧样式
.container {
  background-color: #f5f5f5;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
}

// ✅ 新样式
.container {
  @include gradient-mesh;
  background-color: $bg-secondary;
}

.card {
  @include card-base;
  @include fade-in;
}
```

### 步骤3: 添加交互动画

```scss
// 按钮
.btn {
  @include btn-primary;
}

// 列表项
.list-item {
  @include smooth-transition(transform, box-shadow);

  &:active {
    transform: translateY(-2rpx);
    box-shadow: $shadow-md;
  }
}

// 图标
.icon {
  @include float-animation;
}
```

---

## 🎨 方法2: 常用组件速查

### 📦 卡片类

```scss
// 基础卡片
.card {
  @include card-base;
}

// 玻璃卡片
.glass-card {
  @include glass-card;
}

// 渐变卡片
.gradient-card {
  @include card-base;
  @include gradient-primary;
  color: white;
}
```

### 🔘 按钮类

```scss
// 主按钮
.btn-primary {
  @include btn-primary;
  padding: $spacing-md $spacing-xl;
}

// 次要按钮
.btn-secondary {
  @include btn-outline;
  padding: $spacing-md $spacing-xl;
}

// 圆形按钮
.btn-circle {
  @include btn-primary;
  @include flex-center;
  width: 96rpx;
  height: 96rpx;
  border-radius: $radius-full;
}
```

### 📝 列表类

```scss
// 基础列表项
.list-item {
  background: $bg-primary;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-sm;
  @include smooth-transition(all);

  &:active {
    transform: scale(0.98);
    box-shadow: $shadow-sm;
  }
}

// 带左边框的列表项
.list-item-accent {
  @extend .list-item;
  border-left: 4rpx solid $primary;
}

// 渐变背景列表项
.list-item-gradient {
  @extend .list-item;
  background: linear-gradient(135deg, $bg-secondary 0%, $gray-100 100%);
}
```

### 🎯 状态徽章

```scss
// 成功状态
.badge-success {
  @include badge($success, $success-light);
}

// 警告状态
.badge-warning {
  @include badge($warning, $warning-light);
}

// 错误状态
.badge-error {
  @include badge($error, $error-light);
}
```

### ✨ 动画效果

```scss
// 淡入
.fade-in {
  @include fade-in;
}

// 延迟淡入（列表项依次出现）
.item-1 { @include fade-in(0.1s); }
.item-2 { @include fade-in(0.2s); }
.item-3 { @include fade-in(0.3s); }

// 浮动
.float {
  @include float-animation;
}

// 脉冲（通知、徽章）
.pulse {
  @include pulse-animation;
}

// 滑入
.slide-in {
  @include slide-in-up;
}
```

---

## 📊 典型页面结构

### 列表页（如申请列表、库存列表）

```vue
<template>
  <view class="page-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" />
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item">...</view>
    </view>

    <!-- 列表 -->
    <scroll-view class="scroll-content">
      <view
        v-for="item in list"
        class="list-card"
        @click="handleClick(item)"
      >
        <!-- 卡片内容 -->
      </view>
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
@import "@/styles/design-system.scss";

.page-container {
  min-height: 100vh;
  @include gradient-mesh;
  background-color: $bg-secondary;
}

.search-bar {
  background: $bg-primary;
  padding: $spacing-md $spacing-lg;
}

.search-input {
  @include input-base;
}

.filter-bar {
  @include flex-between;
  padding: $spacing-sm $spacing-lg;
  background: $bg-primary;
}

.filter-item {
  @include badge($primary, rgba($primary, 0.1));
  padding: $spacing-xs $spacing-md;
  border-radius: $radius-full;
}

.scroll-content {
  padding: $spacing-md $spacing-lg;
}

.list-card {
  @include card-base;
  @include fade-in;
  margin-bottom: $spacing-md;

  &:nth-child(1) { animation-delay: 0.1s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.3s; }

  &:active {
    transform: scale(0.98);
  }
}
</style>
```

### 表单页（如新建申请、快速入库）

```vue
<template>
  <view class="form-container">
    <view class="form-section">
      <view class="section-title">基本信息</view>

      <view class="form-item">
        <text class="form-label">标题</text>
        <input class="form-input" />
      </view>
    </view>

    <view class="submit-btn-container">
      <button class="submit-btn">提交</button>
    </view>
  </view>
</template>

<style lang="scss" scoped>
@import "@/styles/design-system.scss";

.form-container {
  min-height: 100vh;
  @include gradient-mesh;
  background-color: $bg-secondary;
}

.form-section {
  @include card-base;
  margin: $spacing-md $spacing-lg;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: $spacing-md;
  padding-bottom: $spacing-sm;
  border-bottom: 2rpx solid $gray-100;
}

.form-item {
  margin-bottom: $spacing-md;
}

.form-label {
  display: block;
  font-size: $font-size-sm;
  color: $text-secondary;
  margin-bottom: $spacing-xs;
  font-weight: $font-weight-medium;
}

.form-input {
  @include input-base;
  width: 100%;
}

.submit-btn-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-md $spacing-lg;
  background: $bg-primary;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  @include safe-area-bottom;
}

.submit-btn {
  @include btn-primary;
  width: 100%;
  height: 88rpx;
  font-size: $font-size-lg;
}
</style>
```

---

## 🎨 常用色彩组合

### 主题色组合
```scss
// 主按钮
background: $primary-gradient;
color: #ffffff;
box-shadow: $shadow-primary;

// 成功提示
background: $success-light;
color: $success;
border-left: 4rpx solid $success;

// 警告提示
background: $warning-light;
color: $warning;
border-left: 4rpx solid $warning;

// 错误提示
background: $error-light;
color: $error;
border-left: 4rpx solid $error;
```

---

## 💡 设计技巧

### 1. 层次感营造

```scss
// 使用不同的背景色
.layer-1 { background: $bg-primary; }   // 最上层 - 白色
.layer-2 { background: $bg-secondary; } // 中间层 - 浅灰
.layer-3 { background: $bg-tertiary; }  // 底层 - 深灰

// 使用不同的阴影
.elevation-1 { box-shadow: $shadow-sm; }
.elevation-2 { box-shadow: $shadow-md; }
.elevation-3 { box-shadow: $shadow-lg; }
```

### 2. 视觉焦点

```scss
// 使用渐变吸引注意力
.highlight {
  @include gradient-primary;
  color: white;
  box-shadow: $shadow-primary;
}

// 使用动画引导用户
.attention {
  @include pulse-animation;
}
```

### 3. 状态反馈

```scss
// 点击反馈
&:active {
  transform: scale(0.96);
  opacity: 0.8;
}

// 加载状态
&.loading {
  @include skeleton;
}

// 禁用状态
&:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
```

---

## 🐛 常见问题

### Q: 渐变文字不显示？

A: 需要使用 webkit 前缀：

```scss
background: $primary-gradient;
-webkit-background-clip: text;
-webkit-text-fill-color: transparent;
background-clip: text;
```

### Q: 动画不流畅？

A: 使用 GPU 加速属性：

```scss
@include smooth-transition(transform, opacity);
// 避免使用 width, height, left, right 等
```

### Q: 毛玻璃效果不生效？

A: 确保有半透明背景：

```scss
background: rgba(255, 255, 255, 0.8); // 必须有透明度
backdrop-filter: blur(20rpx);
```

---

## 📚 参考资源

- 完整设计系统: `/styles/design-system.scss`
- 设计指南: `/DESIGN_GUIDE.md`
- 示例页面: `/pages/index/index.vue`

---

**Happy Coding! 🎉**
