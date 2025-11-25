# 图片资源指南

## 目录
- [资源清单](#资源清单)
- [设计规范](#设计规范)
- [获取方式](#获取方式)
- [优化建议](#优化建议)
- [使用指南](#使用指南)
- [最佳实践](#最佳实践)

---

## 资源清单

### 1. 必需资源

#### 1.1 用户头像
```
文件名: avatar-default.png
尺寸: 200 × 200 px
用途: 用户未上传头像时的默认显示
位置: static/images/avatar-default.png
```

#### 1.2 应用Logo
```
文件名: logo.png
尺寸: 256 × 256 px (推荐)
用途: 登录页、启动页、关于页面
位置: static/images/logo.png
```

#### 1.3 空状态插图

| 文件名 | 尺寸 | 用途 | 场景 |
|--------|------|------|------|
| `empty-data.png` | 300×200px | 无数据 | 列表为空 |
| `empty-search.png` | 300×200px | 搜索无结果 | 搜索页面 |
| `empty-message.png` | 300×200px | 无消息 | 消息中心 |
| `empty-apply.png` | 300×200px | 无申请记录 | 申请列表 |
| `empty-inventory.png` | 300×200px | 无库存数据 | 库存查询 |
| `empty-network.png` | 300×200px | 网络错误 | 网络异常 |

#### 1.4 加载动画
```
文件名: loading.gif
尺寸: 100 × 100 px
用途: 数据加载中的动画
位置: static/images/loading.gif
```

#### 1.5 引导页（可选）
```
文件名: guide-1.png, guide-2.png, guide-3.png
尺寸: 750 × 1334 px (iPhone 6/7/8尺寸)
用途: 首次启动时的功能引导
位置: static/images/guide/
```

### 2. 功能图标

#### 2.1 状态图标

| 文件名 | 尺寸 | 说明 |
|--------|------|------|
| `icon-success.png` | 64×64px | 成功状态 |
| `icon-error.png` | 64×64px | 错误状态 |
| `icon-warning.png` | 64×64px | 警告状态 |
| `icon-info.png` | 64×64px | 信息提示 |

#### 2.2 功能图标

| 文件名 | 尺寸 | 说明 |
|--------|------|------|
| `icon-scan.png` | 64×64px | 扫码功能 |
| `icon-camera.png` | 64×64px | 拍照功能 |
| `icon-upload.png` | 64×64px | 上传文件 |
| `icon-download.png` | 64×64px | 下载文件 |
| `icon-share.png` | 64×64px | 分享功能 |

### 3. 装饰性图片

```
首页轮播图: banner-*.png (750×400px)
节日主题: festival-*.png (根据需要)
广告位: ad-*.png (根据位置设计)
```

---

## 设计规范

### 1. 尺寸规范

#### 设计稿基准
```
设计稿宽度: 750px (基于iPhone 6/7/8)
单位换算: 1px(设计稿) = 2rpx(小程序)
适配公式: 小程序rpx = 设计稿px × 2
```

#### 常用尺寸对照表

| 用途 | 设计稿尺寸 | 小程序rpx | 实际像素(iPhone6) |
|------|-----------|-----------|------------------|
| 头像(小) | 64×64px | 128×128rpx | 32×32px |
| 头像(中) | 100×100px | 200×200rpx | 50×50px |
| 头像(大) | 200×200px | 400×400rpx | 100×100px |
| Logo | 256×256px | 512×512rpx | 128×128px |
| 空状态图 | 300×200px | 600×400rpx | 150×100px |
| 全屏背景 | 750×1334px | 1500×2668rpx | 375×667px |
| 轮播图 | 750×400px | 1500×800rpx | 375×200px |

### 2. 文件格式

#### 推荐格式

```
PNG: 适用于Logo、图标、透明背景图
  优点: 支持透明度，无损压缩
  缺点: 文件相对较大
  适用: 图标、Logo、UI元素

JPEG/JPG: 适用于照片、背景图
  优点: 文件小，压缩率高
  缺点: 不支持透明度，有损压缩
  适用: 轮播图、背景图、照片

WebP: 现代化格式，推荐使用
  优点: 文件小，支持透明度
  缺点: 部分老设备不支持
  适用: 所有图片（优先考虑）

GIF: 适用于简单动画
  优点: 支持动画
  缺点: 颜色限制（256色）
  适用: Loading动画、简单动效
```

#### 格式选择流程
```
需要透明背景？
  ├─ 是 → PNG / WebP
  └─ 否 → 需要动画？
           ├─ 是 → GIF
           └─ 否 → JPEG / WebP
```

### 3. 颜色规范

#### 品牌色
```
主色: #7C3AED (紫色)
辅助色: #A78BFA (浅紫)
强调色: #EC4899 (粉色)
```

#### 状态色
```
成功: #10B981 (绿色)
警告: #F59E0B (橙色)
错误: #EF4444 (红色)
信息: #3B82F6 (蓝色)
```

#### 中性色
```
黑色: #1F2937
深灰: #4B5563
中灰: #9CA3AF
浅灰: #E5E7EB
白色: #FFFFFF
```

### 4. 文件大小

```
图标: < 20KB
头像: < 50KB
空状态图: < 100KB
轮播图: < 200KB
背景图: < 300KB
```

**总原则**: 单个页面所有图片总大小应控制在1MB以内

---

## 获取方式

### 1. 免费插画库

#### Undraw (推荐)
```
网址: https://undraw.co/
特点: 扁平风格，可自定义颜色
用途: 空状态、引导页
授权: MIT开源，免费商用
```

#### Storyset
```
网址: https://storyset.com/
特点: 多种风格，动画效果
用途: 空状态、404页面
授权: 免费商用（需注明来源）
```

#### Illustrations
```
网址: https://illlustrations.co/
特点: 独特风格，精美插画
用途: 装饰性图片
授权: 免费商用
```

#### DrawKit
```
网址: https://www.drawkit.io/
特点: 手绘风格
用途: 空状态、引导页
授权: 免费商用
```

### 2. 图标资源

#### Iconfont (阿里巴巴)
```
网址: https://www.iconfont.cn/
特点: 中文界面，图标丰富
用途: 所有图标需求
授权: 大部分免费商用
```

#### Flaticon
```
网址: https://www.flaticon.com/
特点: 高质量图标
用途: 功能图标、装饰图标
授权: 免费/付费（查看具体图标）
```

### 3. 头像占位图

#### UI Avatars
```
API: https://ui-avatars.com/
用法: https://ui-avatars.com/api/?name=张三&size=200&background=7C3AED&color=fff
特点: 动态生成，支持中文
```

#### DiceBear Avatars
```
网址: https://avatars.dicebear.com/
特点: 多种风格（卡通、像素等）
用法: API动态生成
```

### 4. Logo生成工具

#### Canva
```
网址: https://www.canva.com/
特点: 在线设计，模板丰富
用途: Logo、Banner设计
```

#### Logo Maker
```
网址: https://logomakr.com/
特点: 简单快捷
用途: 简单Logo设计
```

### 5. AI生成工具

#### Midjourney
```
提示词模板（空状态插图）:
"empty state illustration, no data, minimalist,
flat design, purple and white color scheme,
warehouse theme, simple, clean"

示例（库存空状态）:
"empty warehouse illustration, no inventory,
minimalist style, purple accent color, flat design"
```

#### DALL-E 3
```
提示词模板:
"Create a simple flat design illustration for
empty state, showing [scenario], use purple
and gray colors, minimalist style"
```

---

## 优化建议

### 1. 压缩工具

#### 在线工具
```
TinyPNG: https://tinypng.com/
  - PNG/JPEG压缩
  - 压缩率: 50-70%
  - 质量损失: 几乎无感知

Squoosh: https://squoosh.app/
  - 支持多种格式
  - 可对比压缩效果
  - 可转换为WebP

iLoveIMG: https://www.iloveimg.com/
  - 批量处理
  - 调整尺寸
  - 格式转换
```

#### 本地工具
```bash
# ImageMagick (命令行)
# 批量压缩PNG
magick mogrify -quality 85 -resize 200x200 *.png

# 转换为WebP
magick convert input.png -quality 85 output.webp

# 批量转换
for file in *.png; do magick convert "$file" "${file%.png}.webp"; done
```

### 2. 格式转换

#### PNG → WebP
```
优势: 文件大小减少 25-35%
工具: Squoosh, CloudConvert
注意: 保留PNG作为备用（兼容性）
```

#### JPEG → WebP
```
优势: 文件大小减少 30-50%
工具: Squoosh, cwebp命令行
```

### 3. 响应式图片

```javascript
// 根据屏幕密度加载不同图片
const dpr = wx.getSystemInfoSync().pixelRatio;
let imagePath = 'avatar.png';

if (dpr >= 3) {
  imagePath = 'avatar@3x.png'; // 高清屏
} else if (dpr >= 2) {
  imagePath = 'avatar@2x.png'; // 视网膜屏
}
```

### 4. 懒加载

```xml
<!-- 使用uni-app的lazy-load -->
<image src="{{imageUrl}}" lazy-load="true" />
```

### 5. CDN加速

```javascript
// 将图片上传到CDN
const CDN_BASE = 'https://cdn.example.com/wms/';

// 使用CDN路径
const avatarUrl = CDN_BASE + 'avatar-default.png';
```

---

## 使用指南

### 1. 引用方式

#### 静态引用（推荐）
```xml
<!-- 本地图片 -->
<image src="/static/images/logo.png" />

<!-- 相对路径 -->
<image src="../../static/images/avatar-default.png" />
```

#### 动态引用
```javascript
// JS中
data: {
  logoUrl: '/static/images/logo.png'
}

// WXML中
<image src="{{logoUrl}}" />
```

#### 网络图片
```xml
<image src="https://cdn.example.com/image.png" />
```

### 2. 空状态组件封装

```vue
<!-- components/empty-state/index.vue -->
<template>
  <view class="empty-state">
    <image
      class="empty-image"
      :src="imageMap[type]"
      mode="aspectFit"
    />
    <text class="empty-text">{{ text }}</text>
  </view>
</template>

<script>
export default {
  props: {
    type: {
      type: String,
      default: 'data' // data, search, message, network
    },
    text: {
      type: String,
      default: '暂无数据'
    }
  },
  data() {
    return {
      imageMap: {
        data: '/static/images/empty-data.png',
        search: '/static/images/empty-search.png',
        message: '/static/images/empty-message.png',
        apply: '/static/images/empty-apply.png',
        inventory: '/static/images/empty-inventory.png',
        network: '/static/images/empty-network.png'
      }
    };
  }
};
</script>
```

**使用方式**:
```xml
<empty-state type="data" text="暂无申请记录" />
```

### 3. 头像组件

```vue
<!-- components/avatar/index.vue -->
<template>
  <view class="avatar-wrapper">
    <image
      class="avatar"
      :class="sizeClass"
      :src="avatarUrl || defaultAvatar"
      @error="onError"
    />
  </view>
</template>

<script>
export default {
  props: {
    src: String,
    size: {
      type: String,
      default: 'medium' // small, medium, large
    }
  },
  data() {
    return {
      avatarUrl: this.src,
      defaultAvatar: '/static/images/avatar-default.png'
    };
  },
  computed: {
    sizeClass() {
      return `avatar-${this.size}`;
    }
  },
  methods: {
    onError() {
      this.avatarUrl = this.defaultAvatar;
    }
  }
};
</script>

<style>
.avatar {
  border-radius: 50%;
}
.avatar-small { width: 64rpx; height: 64rpx; }
.avatar-medium { width: 100rpx; height: 100rpx; }
.avatar-large { width: 160rpx; height: 160rpx; }
</style>
```

### 4. 错误处理

```javascript
// 图片加载失败处理
<image
  src="{{imageUrl}}"
  @error="handleImageError"
/>

// JS
methods: {
  handleImageError(e) {
    console.error('图片加载失败:', e);
    // 使用默认图片
    this.imageUrl = '/static/images/avatar-default.png';
  }
}
```

---

## 最佳实践

### 1. 图片命名规范

```
✅ 推荐
avatar-default.png      # 小写，连字符
empty-data.png
icon-success.png
logo@2x.png             # 高清版本

❌ 不推荐
AvatarDefault.png       # 驼峰命名
empty_data.png          # 下划线
icon success.png        # 空格
默认头像.png             # 中文
```

### 2. 目录结构

```
static/images/
├── avatar/              # 头像相关
│   ├── avatar-default.png
│   └── avatar-placeholder.png
├── empty/               # 空状态
│   ├── empty-data.png
│   ├── empty-search.png
│   └── empty-message.png
├── icons/               # 功能图标
│   ├── icon-success.png
│   ├── icon-error.png
│   └── icon-warning.png
├── guide/               # 引导页
│   ├── guide-1.png
│   ├── guide-2.png
│   └── guide-3.png
├── banner/              # 轮播图
│   └── banner-*.png
├── logo.png             # Logo
└── loading.gif          # 加载动画
```

### 3. 图片预加载

```javascript
// 预加载关键图片
onLoad() {
  const images = [
    '/static/images/logo.png',
    '/static/images/empty-data.png',
    '/static/images/loading.gif'
  ];

  images.forEach(src => {
    wx.getImageInfo({ src });
  });
}
```

### 4. 性能优化清单

```
✓ 压缩图片（TinyPNG）
✓ 使用WebP格式
✓ 开启懒加载
✓ 使用CDN
✓ 预加载关键图片
✓ 设置图片宽高（避免重排）
✓ 使用雪碧图（多个小图标）
✓ 避免使用过大图片
```

### 5. 无障碍支持

```xml
<!-- 添加alt文本（用于屏幕阅读器） -->
<image
  src="/static/images/logo.png"
  aria-label="CT-Tibet-WMS Logo"
/>

<!-- 装饰性图片标记 -->
<image
  src="/static/images/decoration.png"
  aria-hidden="true"
/>
```

---

## 检查清单

### 设计阶段
- [ ] 图片尺寸符合规范
- [ ] 使用了正确的颜色（品牌色）
- [ ] 图片风格统一
- [ ] 考虑了暗黑模式（如需要）

### 制作阶段
- [ ] 使用了合适的文件格式
- [ ] 背景透明（PNG图标）
- [ ] 图片已压缩优化
- [ ] 文件名符合命名规范
- [ ] 文件大小符合要求

### 使用阶段
- [ ] 图片路径正确
- [ ] 设置了错误处理
- [ ] 添加了懒加载（大图）
- [ ] 预加载了关键图片
- [ ] 在真机上测试显示效果

---

## 常见问题

### Q1: 图片显示模糊？
```
A: 可能原因：
   1. 图片尺寸小于显示尺寸（被放大）
   2. 使用了JPEG格式且质量过低
   3. 未提供@2x/@3x版本

   解决方案：
   - 使用至少2倍尺寸的图片
   - 提高JPEG质量或使用PNG
   - 提供高清版本
```

### Q2: 图片加载慢？
```
A: 优化方法：
   1. 压缩图片（TinyPNG）
   2. 使用WebP格式
   3. 使用CDN
   4. 开启懒加载
   5. 预加载关键图片
```

### Q3: 图片不显示？
```
A: 检查步骤：
   1. 路径是否正确（区分大小写）
   2. 文件是否存在
   3. 网络图片是否可访问
   4. 是否超过小程序包大小限制（2MB）
```

### Q4: 如何支持暗黑模式？
```
A: 两种方案：
   方案1: 提供两套图片
   - image-light.png
   - image-dark.png

   方案2: 使用透明图标 + CSS滤镜
   - 使用SVG或PNG透明图标
   - 通过CSS调整颜色
```

---

## 参考资源

### 图片资源网站
- [Undraw](https://undraw.co/) - 免费插画
- [Storyset](https://storyset.com/) - 动画插画
- [Freepik](https://www.freepik.com/) - 图片素材
- [Unsplash](https://unsplash.com/) - 高质量照片

### 优化工具
- [TinyPNG](https://tinypng.com/) - 图片压缩
- [Squoosh](https://squoosh.app/) - 图片优化
- [CloudConvert](https://cloudconvert.com/) - 格式转换

### 设计工具
- [Figma](https://www.figma.com/) - UI设计
- [Canva](https://www.canva.com/) - 快速设计
- [Photopea](https://www.photopea.com/) - 在线PS

---

**最后更新**: 2025-11-24
**文档版本**: v1.0
**维护者**: UI/UX团队
