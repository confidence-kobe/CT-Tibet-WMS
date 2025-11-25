# CT-Tibet-WMS 小程序设计系统

## 设计系统概述

本设计系统为CT-Tibet-WMS小程序提供统一的视觉语言和交互规范，确保产品的一致性、可维护性和可扩展性。

### 设计原则

1. **一致性** - 统一的视觉风格和交互模式
2. **清晰性** - 信息层级分明，易于理解
3. **高效性** - 减少操作步骤，提升效率
4. **可访问性** - 照顾不同用户群体的需求
5. **现代性** - 采用现代化设计语言

---

## 目录

1. [颜色系统](#颜色系统)
2. [字体系统](#字体系统)
3. [间距系统](#间距系统)
4. [圆角规范](#圆角规范)
5. [阴影规范](#阴影规范)
6. [图标规范](#图标规范)
7. [组件规范](#组件规范)
8. [动效规范](#动效规范)
9. [响应式设计](#响应式设计)
10. [无障碍设计](#无障碍设计)

---

## 颜色系统

### 主色调 (Primary Colors)

#### 品牌紫色
```
主色: #7C3AED
RGB: rgb(124, 58, 237)
用途: 主要操作按钮、链接、选中状态、强调内容
```

#### 辅助紫色
```
浅紫: #A78BFA
RGB: rgb(167, 139, 250)
用途: 次要元素、浅色背景、辅助图标

深紫: #6D28D9
RGB: rgb(109, 40, 217)
用途: Hover状态、按钮按下状态
```

#### 渐变色
```
渐变1: linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)
用途: 头部背景、卡片装饰

渐变2: linear-gradient(135deg, #6D28D9 0%, #7C3AED 100%)
用途: 重要按钮、高亮元素
```

### 状态色 (Status Colors)

#### 成功 (Success)
```
主色: #10B981
RGB: rgb(16, 185, 129)
浅色: #D1FAE5 (背景)
深色: #059669 (强调)
用途: 成功提示、完成状态、正向反馈
```

#### 警告 (Warning)
```
主色: #F59E0B
RGB: rgb(245, 158, 11)
浅色: #FEF3C7 (背景)
深色: #D97706 (强调)
用途: 警告信息、待处理状态、提醒用户
```

#### 错误 (Error)
```
主色: #EF4444
RGB: rgb(239, 68, 68)
浅色: #FEE2E2 (背景)
深色: #DC2626 (强调)
用途: 错误提示、失败状态、拒绝操作
```

#### 信息 (Info)
```
主色: #3B82F6
RGB: rgb(59, 130, 246)
浅色: #DBEAFE (背景)
深色: #2563EB (强调)
用途: 信息提示、帮助文本、一般通知
```

### 中性色 (Neutral Colors)

```
黑色 (Black):
  #000000 - 纯黑（少用）
  #1F2937 - 主要文本
  #374151 - 次要文本

灰色 (Gray):
  #4B5563 - 深灰文本
  #6B7280 - 中灰文本
  #9CA3AF - 浅灰文本
  #D1D5DB - 边框
  #E5E7EB - 分割线
  #F3F4F6 - 浅背景
  #F9FAFB - 页面背景

白色 (White):
  #FFFFFF - 纯白
```

### 颜色使用规范

#### 文本颜色
```css
/* 主要文本 */
color: #1F2937;

/* 次要文本 */
color: #6B7280;

/* 辅助文本 */
color: #9CA3AF;

/* 禁用文本 */
color: #D1D5DB;
```

#### 背景颜色
```css
/* 页面背景 */
background: #F9FAFB;

/* 卡片背景 */
background: #FFFFFF;

/* 浅色背景 */
background: #F3F4F6;

/* 品牌背景 */
background: #7C3AED;
```

#### 边框颜色
```css
/* 普通边框 */
border-color: #E5E7EB;

/* 深色边框 */
border-color: #D1D5DB;

/* 品牌边框 */
border-color: #7C3AED;
```

### 颜色对比度

确保文本与背景的对比度符合WCAG AA标准（最低4.5:1）：

| 组合 | 对比度 | 是否通过 |
|------|--------|---------|
| #1F2937 / #FFFFFF | 14.1:1 | ✓ AAA |
| #6B7280 / #FFFFFF | 7.5:1 | ✓ AAA |
| #9CA3AF / #FFFFFF | 4.6:1 | ✓ AA |
| #7C3AED / #FFFFFF | 7.2:1 | ✓ AAA |
| #FFFFFF / #7C3AED | 7.2:1 | ✓ AAA |

---

## 字体系统

### 字体家族

```css
/* 主字体 */
font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI',
             'PingFang SC', 'Hiragino Sans GB',
             'Microsoft YaHei', 'Helvetica Neue',
             Helvetica, Arial, sans-serif;

/* 数字字体 */
font-family: 'SF Pro Display', -apple-system,
             BlinkMacSystemFont, sans-serif;

/* 等宽字体（代码、编号） */
font-family: 'SF Mono', Monaco, 'Cascadia Code',
             'Courier New', monospace;
```

### 字号规范

基于4px网格，使用偶数字号：

| 名称 | 字号 | rpx | 行高 | 用途 |
|------|------|-----|------|------|
| H1 | 36px | 72rpx | 1.3 | 页面标题 |
| H2 | 32px | 64rpx | 1.3 | 区块标题 |
| H3 | 28px | 56rpx | 1.4 | 卡片标题 |
| H4 | 24px | 48rpx | 1.4 | 小标题 |
| Body Large | 18px | 36rpx | 1.5 | 重要正文 |
| Body | 16px | 32rpx | 1.5 | 标准正文 |
| Body Small | 14px | 28rpx | 1.5 | 次要正文 |
| Caption | 12px | 24rpx | 1.4 | 辅助信息 |
| Mini | 10px | 20rpx | 1.4 | 极小文本（少用） |

### 字重规范

```
Light:    300 - 少用
Regular:  400 - 正文
Medium:   500 - 次要标题
Semibold: 600 - 重要标题
Bold:     700 - 强调文本
```

### 行高规范

```
标题: line-height: 1.2-1.4
正文: line-height: 1.5-1.6
密集: line-height: 1.2
疏松: line-height: 2.0
```

### 字体样式示例

```css
/* H1 - 页面标题 */
.h1 {
  font-size: 72rpx;
  font-weight: 600;
  line-height: 1.3;
  color: #1F2937;
}

/* H2 - 区块标题 */
.h2 {
  font-size: 64rpx;
  font-weight: 600;
  line-height: 1.3;
  color: #1F2937;
}

/* Body - 标准正文 */
.body {
  font-size: 32rpx;
  font-weight: 400;
  line-height: 1.5;
  color: #374151;
}

/* Caption - 辅助信息 */
.caption {
  font-size: 24rpx;
  font-weight: 400;
  line-height: 1.4;
  color: #9CA3AF;
}
```

---

## 间距系统

### 基准间距

基于4px网格系统：

| Token | px | rpx | 用途 |
|-------|-----|-----|------|
| xs | 4px | 8rpx | 极小间距 |
| sm | 8px | 16rpx | 小间距 |
| md | 12px | 24rpx | 中等间距 |
| lg | 16px | 32rpx | 标准间距 |
| xl | 20px | 40rpx | 大间距 |
| 2xl | 24px | 48rpx | 加大间距 |
| 3xl | 32px | 64rpx | 超大间距 |
| 4xl | 40px | 80rpx | 页面级间距 |
| 5xl | 48px | 96rpx | 区块级间距 |

### 间距使用规范

#### 内边距 (Padding)
```css
/* 卡片内边距 */
padding: 32rpx; /* lg */

/* 按钮内边距 */
padding: 16rpx 32rpx; /* sm lg */

/* 输入框内边距 */
padding: 24rpx 32rpx; /* md lg */

/* 列表项内边距 */
padding: 32rpx 32rpx; /* lg lg */
```

#### 外边距 (Margin)
```css
/* 元素间距 */
margin-bottom: 32rpx; /* lg */

/* 区块间距 */
margin-bottom: 64rpx; /* 3xl */

/* 页面边距 */
padding: 32rpx; /* lg */
```

#### 页面布局间距
```
顶部间距: 32rpx (lg)
底部间距: 32rpx (lg)
左右边距: 32rpx (lg)
区块间距: 64rpx (3xl)
卡片间距: 32rpx (lg)
列表项间距: 24rpx (md)
```

---

## 圆角规范

基于4px网格：

| Token | px | rpx | 用途 |
|-------|-----|-----|------|
| none | 0px | 0rpx | 无圆角 |
| sm | 4px | 8rpx | 小圆角（标签） |
| md | 8px | 16rpx | 中圆角（按钮、输入框） |
| lg | 12px | 24rpx | 大圆角（卡片） |
| xl | 16px | 32rpx | 超大圆角（模态框） |
| full | 50% | 50% | 全圆（头像、图标） |

### 圆角使用规范

```css
/* 按钮 */
border-radius: 16rpx; /* md */

/* 卡片 */
border-radius: 24rpx; /* lg */

/* 输入框 */
border-radius: 16rpx; /* md */

/* 标签 */
border-radius: 8rpx; /* sm */

/* 头像 */
border-radius: 50%; /* full */

/* 模态框 */
border-radius: 32rpx; /* xl */
```

---

## 阴影规范

基于Material Design阴影系统：

### 阴影层级

| 级别 | 名称 | 阴影值 | 用途 |
|------|------|--------|------|
| 0 | none | none | 无阴影 |
| 1 | xs | 0 2rpx 8rpx rgba(0,0,0,0.05) | 轻微浮起（悬停） |
| 2 | sm | 0 4rpx 16rpx rgba(0,0,0,0.08) | 卡片 |
| 3 | md | 0 8rpx 24rpx rgba(0,0,0,0.10) | 下拉菜单 |
| 4 | lg | 0 16rpx 48rpx rgba(0,0,0,0.12) | 模态框 |
| 5 | xl | 0 24rpx 64rpx rgba(0,0,0,0.15) | 抽屉、浮层 |

### 阴影使用示例

```css
/* 卡片 */
.card {
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

/* 悬浮按钮 */
.floating-button {
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.10);
}

/* 模态框 */
.modal {
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.12);
}

/* 品牌色阴影（特殊效果） */
.primary-shadow {
  box-shadow: 0 8rpx 24rpx rgba(124, 58, 237, 0.25);
}
```

---

## 图标规范

### 图标尺寸

| 名称 | px | rpx | 用途 |
|------|-----|-----|------|
| Mini | 16px | 32rpx | 内联图标 |
| Small | 20px | 40rpx | 列表图标 |
| Medium | 24px | 48rpx | 标准图标 |
| Large | 32px | 64rpx | 大图标 |
| XLarge | 48px | 96rpx | 空状态图标 |

### 图标风格

```
风格: Line Icons (线性图标)
线条粗细: 2-3px
圆角: 2px
颜色: 继承文本颜色
```

### 图标颜色

```css
/* 主要图标 */
color: #1F2937;

/* 次要图标 */
color: #6B7280;

/* 品牌图标 */
color: #7C3AED;

/* 状态图标 */
color: #10B981; /* 成功 */
color: #F59E0B; /* 警告 */
color: #EF4444; /* 错误 */
```

---

## 组件规范

### 按钮 (Button)

#### 主按钮 (Primary)
```css
background: #7C3AED;
color: #FFFFFF;
padding: 16rpx 32rpx;
border-radius: 16rpx;
font-size: 32rpx;
font-weight: 500;
min-width: 160rpx;
height: 88rpx;

/* Hover */
background: #6D28D9;

/* Pressed */
background: #5B21B6;

/* Disabled */
background: #D1D5DB;
color: #9CA3AF;
```

#### 次按钮 (Secondary)
```css
background: #F3F4F6;
color: #1F2937;
border: 2rpx solid #E5E7EB;
/* 其他样式同主按钮 */
```

#### 文字按钮 (Text)
```css
background: transparent;
color: #7C3AED;
padding: 16rpx 24rpx;
```

#### 按钮尺寸
```
Large:  height: 96rpx; padding: 24rpx 48rpx; font-size: 36rpx;
Medium: height: 88rpx; padding: 16rpx 32rpx; font-size: 32rpx;
Small:  height: 72rpx; padding: 12rpx 24rpx; font-size: 28rpx;
```

### 输入框 (Input)

```css
background: #FFFFFF;
border: 2rpx solid #E5E7EB;
border-radius: 16rpx;
padding: 24rpx 32rpx;
font-size: 32rpx;
height: 88rpx;
color: #1F2937;

/* Focus */
border-color: #7C3AED;
box-shadow: 0 0 0 4rpx rgba(124, 58, 237, 0.1);

/* Error */
border-color: #EF4444;

/* Disabled */
background: #F9FAFB;
color: #9CA3AF;
```

### 卡片 (Card)

```css
background: #FFFFFF;
border-radius: 24rpx;
padding: 32rpx;
box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);

/* Hover (可选) */
box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.10);
transform: translateY(-4rpx);
transition: all 0.3s ease;
```

### 列表项 (List Item)

```css
background: #FFFFFF;
padding: 32rpx;
border-bottom: 2rpx solid #F3F4F6;

/* 最后一项 */
border-bottom: none;

/* Hover/Active */
background: #F9FAFB;
```

### 标签 (Tag)

```css
/* 默认标签 */
background: #F3F4F6;
color: #6B7280;
padding: 8rpx 16rpx;
border-radius: 8rpx;
font-size: 24rpx;

/* 状态标签 */
/* 成功 */
background: #D1FAE5;
color: #059669;

/* 警告 */
background: #FEF3C7;
color: #D97706;

/* 错误 */
background: #FEE2E2;
color: #DC2626;

/* 品牌 */
background: #EDE9FE;
color: #7C3AED;
```

### 开关 (Switch)

```css
width: 96rpx;
height: 56rpx;
border-radius: 56rpx;

/* Off */
background: #E5E7EB;

/* On */
background: #7C3AED;
```

### 模态框 (Modal)

```css
background: #FFFFFF;
border-radius: 32rpx;
padding: 48rpx 32rpx;
max-width: 600rpx;
box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.12);
```

### 加载指示器 (Loading)

```css
/* 旋转动画 */
animation: rotate 1s linear infinite;

/* 颜色 */
color: #7C3AED;

/* 尺寸 */
width: 64rpx;
height: 64rpx;
```

---

## 动效规范

### 动画时长

```
极快: 100ms  - 微小交互（开关）
快速: 200ms  - 按钮按下
标准: 300ms  - 页面过渡
缓慢: 500ms  - 复杂动画
极慢: 800ms  - 特殊效果（少用）
```

### 缓动函数

```css
/* 默认（进出） */
ease: cubic-bezier(0.25, 0.1, 0.25, 1);

/* 进入（ease-out） */
ease-out: cubic-bezier(0, 0, 0.2, 1);

/* 退出（ease-in） */
ease-in: cubic-bezier(0.4, 0, 1, 1);

/* 标准（ease-in-out） */
ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);

/* 弹性（回弹） */
spring: cubic-bezier(0.34, 1.56, 0.64, 1);
```

### 常用动画

#### 淡入淡出
```css
.fade-in {
  animation: fadeIn 300ms ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
```

#### 滑入
```css
.slide-up {
  animation: slideUp 300ms ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(40rpx);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
```

#### 缩放
```css
.scale-in {
  animation: scaleIn 200ms ease-out;
}

@keyframes scaleIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
```

---

## 响应式设计

### 屏幕断点

```
Small:  < 375px  (iPhone SE)
Medium: 375-414px (iPhone 6/7/8/X)
Large:  414-768px (iPhone Plus, iPad)
XLarge: > 768px   (iPad Pro)
```

### 响应式字体

```css
/* 小屏幕 */
@media (max-width: 375px) {
  .h1 { font-size: 64rpx; }
  .body { font-size: 28rpx; }
}

/* 大屏幕 */
@media (min-width: 768px) {
  .h1 { font-size: 80rpx; }
  .body { font-size: 36rpx; }
}
```

### 响应式间距

```css
/* 小屏幕 */
@media (max-width: 375px) {
  .container { padding: 24rpx; }
}

/* 大屏幕 */
@media (min-width: 768px) {
  .container { padding: 48rpx; }
}
```

---

## 无障碍设计

### 颜色对比度

- 正常文本: 最低4.5:1 (WCAG AA)
- 大文本(18px+): 最低3:1
- UI组件: 最低3:1

### 触摸目标

```
最小触摸区域: 88rpx × 88rpx (44px × 44px)
推荐尺寸: 96rpx × 96rpx (48px × 48px)
```

### 文本可读性

```
最小字号: 24rpx (12px)
推荐正文: 32rpx (16px)
行长: 50-75字符
行高: 1.5-1.6
```

### 焦点状态

```css
/* 键盘焦点 */
.focusable:focus {
  outline: 4rpx solid #7C3AED;
  outline-offset: 4rpx;
}
```

---

## 设计Token

### CSS变量定义

```css
/* colors.wxss */
page {
  /* Brand Colors */
  --color-primary: #7C3AED;
  --color-primary-light: #A78BFA;
  --color-primary-dark: #6D28D9;

  /* Status Colors */
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-error: #EF4444;
  --color-info: #3B82F6;

  /* Neutral Colors */
  --color-text-primary: #1F2937;
  --color-text-secondary: #6B7280;
  --color-text-tertiary: #9CA3AF;
  --color-border: #E5E7EB;
  --color-divider: #F3F4F6;
  --color-bg-page: #F9FAFB;
  --color-bg-card: #FFFFFF;

  /* Spacing */
  --spacing-xs: 8rpx;
  --spacing-sm: 16rpx;
  --spacing-md: 24rpx;
  --spacing-lg: 32rpx;
  --spacing-xl: 40rpx;
  --spacing-2xl: 48rpx;
  --spacing-3xl: 64rpx;

  /* Border Radius */
  --radius-sm: 8rpx;
  --radius-md: 16rpx;
  --radius-lg: 24rpx;
  --radius-xl: 32rpx;
  --radius-full: 50%;

  /* Shadows */
  --shadow-sm: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  --shadow-md: 0 8rpx 24rpx rgba(0, 0, 0, 0.10);
  --shadow-lg: 0 16rpx 48rpx rgba(0, 0, 0, 0.12);
}
```

### 使用示例

```css
.card {
  background: var(--color-bg-card);
  padding: var(--spacing-lg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.button-primary {
  background: var(--color-primary);
  color: #FFFFFF;
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: var(--radius-md);
}
```

---

## 设计资源

### Figma设计文件

```
文件名: CT-Tibet-WMS Design System
包含:
  - 颜色系统
  - 字体系统
  - 组件库
  - 页面模板
  - 图标库
```

### 开发资源

```
代码库: miniprogram/styles/
  ├── variables.wxss    # CSS变量
  ├── colors.wxss       # 颜色系统
  ├── typography.wxss   # 字体系统
  ├── spacing.wxss      # 间距系统
  └── mixins.wxss       # 常用混合
```

---

## 更新日志

### v1.0.0 (2025-11-24)
- 初始版本发布
- 定义核心设计Token
- 建立组件规范
- 添加使用示例

---

**最后更新**: 2025-11-24
**文档版本**: v1.0.0
**维护者**: UI/UX设计团队
**审核者**: 项目技术负责人
