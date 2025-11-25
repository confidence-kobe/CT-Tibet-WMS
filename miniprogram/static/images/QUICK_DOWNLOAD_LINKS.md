# 图片资源快速下载链接

本文档提供所有图片资源的**直接下载链接**，无需注册，即点即得。

---

## 1. 默认头像 (avatar-default.png)

### 推荐方案A: UI Avatars（即时生成）

**直接下载链接**:
```
https://ui-avatars.com/api/?name=用户&size=200&background=7C3AED&color=fff&bold=true&font-size=0.4
```

**操作步骤**:
1. 复制上面的链接到浏览器
2. 右键点击图片 → "图片另存为"
3. 保存为: `avatar-default.png`
4. 放置到: `miniprogram/static/images/`

**自定义参数**:
```
修改背景色: background=7C3AED (品牌紫色)
修改文字: name=用户 (改为其他文字)
修改尺寸: size=200 (保持200)
修改文字颜色: color=fff (白色)
```

### 方案B: DiceBear Avatars

**不同风格的下载链接**:

1. **Initials 风格（首字母）**:
```
https://api.dicebear.com/7.x/initials/svg?seed=CT&backgroundColor=7C3AED&size=200
```

2. **Avataaars 风格（卡通头像）**:
```
https://api.dicebear.com/7.x/avataaars/svg?seed=default&backgroundColor=7C3AED&size=200
```

3. **Bottts 风格（机器人）**:
```
https://api.dicebear.com/7.x/bottts/svg?seed=warehouse&backgroundColor=7C3AED&size=200
```

**注意**: DiceBear生成的是SVG格式，需要转换为PNG:
1. 打开链接
2. 在浏览器中右键 → 检查元素 → 复制SVG代码
3. 访问 https://www.photopea.com/
4. 新建文件 → 粘贴SVG → 导出为PNG

---

## 2. 应用Logo (logo.png)

### 方案A: 使用Canva模板（推荐）

由于Canva需要登录，这里提供替代方案。

### 方案B: 简单纯色Logo（临时使用）

**在线生成器**: https://www.logomaker.com/

或使用以下HTML临时生成:

```html
<!DOCTYPE html>
<html>
<head>
  <style>
    .logo {
      width: 256px;
      height: 256px;
      background: linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 20%;
      box-shadow: 0 10px 30px rgba(124, 58, 237, 0.3);
    }
    .logo-text {
      color: white;
      font-size: 80px;
      font-weight: bold;
      font-family: Arial, sans-serif;
      letter-spacing: -2px;
    }
  </style>
</head>
<body>
  <div class="logo">
    <div class="logo-text">WMS</div>
  </div>
  <script>
    // 右键保存即可
  </script>
</body>
</html>
```

**使用步骤**:
1. 将上述代码保存为 `logo-generator.html`
2. 在浏览器中打开
3. 右键点击Logo → "图片另存为"
4. 保存为 `logo.png`

### 方案C: 使用免费Logo生成器

**Logo Maker**: https://logomakr.com/
- 完全免费
- 无需注册
- 在线编辑
- 直接下载PNG

**步骤**:
1. 访问网站
2. 搜索"warehouse"或"box"图标
3. 添加文字"CT-WMS"或"西藏电信"
4. 修改颜色为紫色 (#7C3AED)
5. 导出256×256px PNG

---

## 3. 空状态插图

### 使用Undraw（推荐 - 完全免费）

访问: https://undraw.co/illustrations

**直接搜索和下载**:

#### 3.1 Empty Data (无数据)
```
搜索关键词: "empty"
推荐插图: "Void" 或 "Empty"
直接链接: https://undraw.co/search?q=empty
```

#### 3.2 No Search Results (搜索无结果)
```
搜索关键词: "search"
推荐插图: "Searching" 或 "Not found"
直接链接: https://undraw.co/search?q=search
```

#### 3.3 No Messages (无消息)
```
搜索关键词: "message" 或 "mailbox"
推荐插图: "Mailbox" 或 "No messages"
直接链接: https://undraw.co/search?q=mailbox
```

#### 3.4 No Applications (无申请)
```
搜索关键词: "document" 或 "file"
推荐插图: "Add files" 或 "Documents"
直接链接: https://undraw.co/search?q=files
```

#### 3.5 No Inventory (无库存)
```
搜索关键词: "box" 或 "package"
推荐插图: "Package" 或 "Delivery"
直接链接: https://undraw.co/search?q=package
```

#### 3.6 Network Error (网络错误)
```
搜索关键词: "error" 或 "warning"
推荐插图: "Server down" 或 "Warning"
直接链接: https://undraw.co/search?q=error
```

**下载步骤**:
1. 点击上面的链接
2. 选择喜欢的插图
3. 点击右上角颜色选择器
4. 输入品牌色: `#7C3AED`
5. 点击"Download"下载SVG
6. 转换为PNG（见下方方法）

**SVG转PNG方法**:
1. 访问 https://cloudconvert.com/svg-to-png
2. 上传SVG文件
3. 设置宽度: 300px，高度: 200px
4. 点击"Convert"
5. 下载PNG文件

---

## 4. 加载动画 (loading.gif)

### 方案A: 使用在线生成器

**Loading.io** (推荐):
```
网址: https://loading.io/
```

**步骤**:
1. 访问网站
2. 选择"Spinner"类型
3. 选择喜欢的动画（推荐"Dual Ring"或"Spinner"）
4. 自定义:
   - 颜色: #7C3AED
   - 尺寸: 100px
   - 背景: 透明
5. 下载GIF格式

**直接生成链接**:
```
Spinner样式:
https://loading.io/spinner/

Dots样式:
https://loading.io/spinner/double-ring/
```

### 方案B: 使用CSS Loading动画

如果不想使用GIF，可以使用纯CSS动画（更推荐）:

```vue
<!-- components/loading/index.vue -->
<template>
  <view class="loading-spinner">
    <view class="spinner"></view>
  </view>
</template>

<style>
.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx;
}

.spinner {
  width: 64rpx;
  height: 64rpx;
  border: 4rpx solid #E5E7EB;
  border-top-color: #7C3AED;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
```

**优势**:
- 无需加载图片
- 文件更小
- 可自定义颜色
- 性能更好

---

## 5. 备用图标资源

### 状态图标

使用Iconfont快速获取:

**成功图标**:
```
搜索: "success" 或 "勾选"
下载: PNG, 64×64px, 颜色 #10B981
保存为: icon-success.png
```

**错误图标**:
```
搜索: "error" 或 "错误"
下载: PNG, 64×64px, 颜色 #EF4444
保存为: icon-error.png
```

**警告图标**:
```
搜索: "warning" 或 "警告"
下载: PNG, 64×64px, 颜色 #F59E0B
保存为: icon-warning.png
```

**信息图标**:
```
搜索: "info" 或 "信息"
下载: PNG, 64×64px, 颜色 #3B82F6
保存为: icon-info.png
```

---

## 6. 图片压缩（必做）

所有图片下载后，务必使用TinyPNG压缩:

**TinyPNG**:
```
网址: https://tinypng.com/
```

**步骤**:
1. 将所有PNG文件拖入页面
2. 等待压缩完成（通常2-3秒）
3. 点击"Download all"下载压缩后的文件
4. 替换原文件

**效果**:
- 文件大小减少50-70%
- 视觉质量无损
- 加载速度更快

---

## 文件清单

### 必需文件（优先级高）

```
miniprogram/static/images/
├── avatar-default.png     (200×200px, <50KB)
├── logo.png              (256×256px, <100KB)
└── empty-data.png        (300×200px, <100KB)
```

### 可选文件（后续补充）

```
miniprogram/static/images/
├── empty-search.png      (300×200px)
├── empty-message.png     (300×200px)
├── empty-apply.png       (300×200px)
├── empty-inventory.png   (300×200px)
├── empty-network.png     (300×200px)
├── icon-success.png      (64×64px)
├── icon-error.png        (64×64px)
├── icon-warning.png      (64×64px)
└── icon-info.png         (64×64px)
```

---

## 验证清单

下载完成后，检查:

- [ ] avatar-default.png 存在且尺寸为200×200px
- [ ] logo.png 存在且尺寸为256×256px
- [ ] 至少有1个空状态插图（empty-data.png）
- [ ] 所有图片背景透明（PNG格式）
- [ ] 所有图片已用TinyPNG压缩
- [ ] 文件命名符合规范（小写，短横线）
- [ ] 图片颜色符合品牌色系

---

## 时间估算

| 任务 | 预计时间 |
|------|---------|
| 下载默认头像 | 1分钟 |
| 生成Logo | 5分钟 |
| 下载空状态插图 | 10分钟 |
| 压缩优化 | 3分钟 |
| **总计** | **19分钟** |

---

## 常见问题

### Q: 链接打不开怎么办？

A: 某些网站可能需要科学上网。替代方案:
1. UI Avatars: 国内可直接访问
2. Undraw: 国内可访问，但有时较慢
3. 如果都不行，使用纯色占位图临时替代

### Q: SVG转PNG太麻烦？

A: 最简单方法:
1. 在浏览器中打开SVG链接
2. 右键 → "图片另存为"（某些浏览器自动转为PNG）
3. 或使用截图工具直接截取

### Q: 图片尺寸不对怎么办？

A: 使用在线工具调整:
- Squoosh: https://squoosh.app/
- iLoveIMG: https://www.iloveimg.com/resize-image

### Q: 能否使用占位图临时开发？

A: 可以！使用纯色背景占位:
```
200×200px 紫色方块 → avatar-default.png
256×256px 紫色方块 + "LOGO"文字 → logo.png
300×200px 灰色方块 + "暂无数据"文字 → empty-data.png
```

---

**最后更新**: 2025-11-25
**预计完成时间**: 20分钟
**难度等级**: ★☆☆☆☆

**提示**:
1. 优先完成头像和Logo，这两个最常用
2. 空状态插图可以逐步补充
3. 所有图片下载后务必使用TinyPNG压缩
