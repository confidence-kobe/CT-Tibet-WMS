# 静态资源快速获取指南

## 快速概览

本文档提供小程序静态资源的**快速获取方案**，让你在30分钟内完成所有图标和图片的准备工作。

---

## 目录

- [方案选择](#方案选择)
- [方案1: Iconfont一键获取（推荐）](#方案1-iconfont一键获取推荐)
- [方案2: 使用在线工具生成](#方案2-使用在线工具生成)
- [方案3: 使用本项目提供的SVG代码](#方案3-使用本项目提供的svg代码)
- [图片资源获取](#图片资源获取)
- [验证清单](#验证清单)

---

## 方案选择

| 方案 | 时间 | 难度 | 推荐度 | 适合人群 |
|------|------|------|--------|---------|
| 方案1: Iconfont | 15分钟 | ★☆☆ | ★★★★★ | 所有人 |
| 方案2: 在线工具 | 20分钟 | ★★☆ | ★★★★☆ | 需要自定义 |
| 方案3: SVG转换 | 30分钟 | ★★★ | ★★★☆☆ | 有技术背景 |

**推荐**: 直接使用方案1（Iconfont），最快最简单。

---

## 方案1: Iconfont一键获取（推荐）

### 步骤1: 访问Iconfont

访问: https://www.iconfont.cn/

如果没有账号，使用支付宝或GitHub快速登录（免费）。

### 步骤2: 搜索并下载图标

#### 2.1 首页图标 (home)

```
搜索关键词: "主页" 或 "home"
推荐图标ID: 3600759
风格: 线性图标
操作:
  1. 点击图标
  2. 点击"下载" → "PNG"
  3. 设置尺寸: 81
  4. 设置颜色: #999999 (下载第1个)
  5. 再次下载，颜色: #7C3AED (下载第2个)
  6. 重命名:
     - 第1个 → home.png
     - 第2个 → home-active.png
```

#### 2.2 申请图标 (apply)

```
搜索关键词: "文档添加" 或 "file add"
推荐图标ID: 3600881
风格: 线性图标
操作: 同上
  - 灰色版本 → apply.png
  - 紫色版本 → apply-active.png
```

#### 2.3 审批图标 (approval)

```
搜索关键词: "审批" 或 "clipboard check"
推荐图标ID: 3600945
风格: 线性图标
操作: 同上
  - 灰色版本 → approval.png
  - 紫色版本 → approval-active.png
```

#### 2.4 库存图标 (inventory)

```
搜索关键词: "包裹" 或 "box" 或 "package"
推荐图标ID: 3600823
风格: 线性图标
操作: 同上
  - 灰色版本 → inventory.png
  - 紫色版本 → inventory-active.png
```

#### 2.5 消息图标 (message)

```
搜索关键词: "铃铛" 或 "bell" 或 "notification"
推荐图标ID: 3600712
风格: 线性图标
操作: 同上
  - 灰色版本 → message.png
  - 紫色版本 → message-active.png
```

#### 2.6 我的图标 (mine)

```
搜索关键词: "用户" 或 "user" 或 "person"
推荐图标ID: 3600678
风格: 线性图标
操作: 同上
  - 灰色版本 → mine.png
  - 紫色版本 → mine-active.png
```

#### 2.7 仓库图标 (warehouse)

```
搜索关键词: "仓库" 或 "warehouse" 或 "storage"
推荐图标ID: 3600890
风格: 线性图标
操作: 同上
  - 灰色版本 → warehouse.png
  - 紫色版本 → warehouse-active.png
```

### 步骤3: 放置文件

将下载的14个PNG文件放入:
```
miniprogram/static/tabbar/
```

### 步骤4: 验证

检查文件列表:
```
miniprogram/static/tabbar/
├── home.png
├── home-active.png
├── apply.png
├── apply-active.png
├── approval.png
├── approval-active.png
├── inventory.png
├── inventory-active.png
├── message.png
├── message-active.png
├── mine.png
├── mine-active.png
├── warehouse.png
└── warehouse-active.png
```

---

## 方案2: 使用在线工具生成

### 使用IconPark（字节跳动）

访问: https://iconpark.oceanengine.com/

#### 优势
- 风格统一
- 在线编辑颜色
- 一键导出

#### 步骤

1. **搜索图标**
   ```
   home → 选择"主页-线性"
   file-add → 选择"添加文件-线性"
   clipboard-check → 选择"审批-线性"
   box → 选择"包裹-线性"
   bell → 选择"铃铛-线性"
   user → 选择"用户-线性"
   warehouse → 选择"仓库-线性"
   ```

2. **编辑图标**
   - 点击图标进入编辑页面
   - 设置线条粗细: 3px
   - 设置尺寸: 81×81px

3. **导出灰色版本**
   - 颜色: #999999
   - 格式: PNG
   - 下载并重命名为 `{name}.png`

4. **导出紫色版本**
   - 颜色: #7C3AED
   - 格式: PNG
   - 下载并重命名为 `{name}-active.png`

---

## 方案3: 使用本项目提供的SVG代码

### SVG图标代码

#### home.svg (首页)
```svg
<svg width="81" height="81" viewBox="0 0 81 81" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M13.5 30.375L40.5 10.125L67.5 30.375V64.125C67.5 65.6168 66.9074 67.0476 65.8525 68.1025C64.7976 69.1574 63.3668 69.75 61.875 69.75H19.125C17.6332 69.75 16.2024 69.1574 15.1475 68.1025C14.0926 67.0476 13.5 65.6168 13.5 64.125V30.375Z" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M30.375 69.75V40.5H50.625V69.75" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
```

#### apply.svg (申请)
```svg
<svg width="81" height="81" viewBox="0 0 81 81" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M45.5625 13.5H19.125C17.6332 13.5 16.2024 14.0926 15.1475 15.1475C14.0926 16.2024 13.5 17.6332 13.5 19.125V61.875C13.5 63.3668 14.0926 64.7976 15.1475 65.8525C16.2024 66.9074 17.6332 67.5 19.125 67.5H61.875C63.3668 67.5 64.7976 66.9074 65.8525 65.8525C66.9074 64.7976 67.5 63.3668 67.5 61.875V35.4375" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M40.5 29.25V51.75" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M29.25 40.5H51.75" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
```

#### approval.svg (审批)
```svg
<svg width="81" height="81" viewBox="0 0 81 81" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M53.4375 13.5H19.125C17.0745 13.5 15.375 15.1995 15.375 17.25V69.75C15.375 71.8005 17.0745 73.5 19.125 73.5H61.875C63.9255 73.5 65.625 71.8005 65.625 69.75V25.3125L53.4375 13.5Z" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M53.4375 13.5V25.3125H65.625" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M30.375 45.5625L37.6875 52.875L53.4375 37.125" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
```

#### inventory.svg (库存)
```svg
<svg width="81" height="81" viewBox="0 0 81 81" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M67.5 50.625V30.375L40.5 15.375L13.5 30.375V50.625L40.5 65.625L67.5 50.625Z" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M13.5 30.375L40.5 45.5625L67.5 30.375" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M40.5 45.5625V65.625" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
```

#### message.svg (消息)
```svg
<svg width="81" height="81" viewBox="0 0 81 81" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M40.5 69.75C56.7919 69.75 69.75 56.7919 69.75 40.5C69.75 24.2081 56.7919 11.25 40.5 11.25C24.2081 11.25 11.25 24.2081 11.25 40.5C11.25 45.2812 12.4969 49.7812 14.6719 53.7188L11.25 69.75L27.2812 66.3281C31.2188 68.5031 35.7188 69.75 40.5 69.75Z" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <circle cx="40.5" cy="40.5" r="2.25" fill="#999999"/>
  <circle cx="51.75" cy="40.5" r="2.25" fill="#999999"/>
  <circle cx="29.25" cy="40.5" r="2.25" fill="#999999"/>
</svg>
```

#### mine.svg (我的)
```svg
<svg width="81" height="81" viewBox="0 0 81 81" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M40.5 46.125C50.8553 46.125 59.25 37.7303 59.25 27.375C59.25 17.0197 50.8553 8.625 40.5 8.625C30.1447 8.625 21.75 17.0197 21.75 27.375C21.75 37.7303 30.1447 46.125 40.5 46.125Z" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M10.5 72.375C10.5 65.8315 13.1027 59.5558 17.7599 54.8986C22.4171 50.2414 28.6929 47.625 35.25 47.625H45.75C52.3071 47.625 58.5829 50.2414 63.2401 54.8986C67.8973 59.5558 70.5 65.8315 70.5 72.375" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
```

#### warehouse.svg (仓库)
```svg
<svg width="81" height="81" viewBox="0 0 81 81" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M13.5 30.375L40.5 13.5L67.5 30.375V67.5H13.5V30.375Z" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M29.25 67.5V45.5625H51.75V67.5" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M29.25 51.75H51.75" stroke="#999999" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
```

### SVG转PNG步骤

#### 方法A: 使用在线工具（最简单）

1. **访问 Photopea**
   ```
   网址: https://www.photopea.com/
   ```

2. **导入SVG**
   - 复制上面的SVG代码
   - 在Photopea中: 文件 → 新建 → 粘贴SVG代码
   - 或者保存为.svg文件后拖入Photopea

3. **导出PNG**
   - 文件 → 导出为 → PNG
   - 确认尺寸为81×81px
   - 保存为对应文件名

4. **修改颜色导出第二版**
   - 选择图层
   - 使用"色相/饱和度"或直接修改SVG代码中的颜色
   - 将 `#999999` 改为 `#7C3AED`
   - 再次导出

#### 方法B: 使用SVG2PNG在线工具

1. **访问工具**
   ```
   网址1: https://svgtopng.com/
   网址2: https://cloudconvert.com/svg-to-png
   ```

2. **转换步骤**
   - 上传SVG文件或粘贴代码
   - 设置宽度和高度: 81px
   - 点击"Convert"
   - 下载PNG文件

3. **批量转换**
   - 将所有7个SVG保存为文件
   - 使用CloudConvert批量上传
   - 一次性转换所有图标

#### 方法C: 使用命令行（适合开发者）

如果你安装了Node.js，可以使用命令行工具:

```bash
# 安装svg2png-cli
npm install -g svg2png-cli

# 转换单个SVG
svg2png home.svg --width 81 --height 81 --output home.png

# 批量转换
for file in *.svg; do svg2png "$file" --width 81 --height 81 --output "${file%.svg}.png"; done
```

---

## 图片资源获取

### 1. 默认头像 (avatar-default.png)

#### 方案A: 使用在线头像生成器

```
网址: https://ui-avatars.com/

直接下载链接:
https://ui-avatars.com/api/?name=默认&size=200&background=7C3AED&color=fff&bold=true

操作:
1. 访问上述链接
2. 右键 → 另存为 → avatar-default.png
3. 放置到: miniprogram/static/images/avatar-default.png
```

#### 方案B: 使用DiceBear

```
网址: https://www.dicebear.com/

推荐风格: Initials (首字母) 或 Avataaars (卡通)

下载链接示例:
https://api.dicebear.com/7.x/initials/png?seed=CT&backgroundColor=7C3AED&size=200

操作: 同上
```

#### 方案C: 使用Figma快速设计

1. 在Figma创建200×200px画布
2. 绘制一个圆形
3. 填充紫色渐变 (#7C3AED → #A78BFA)
4. 添加白色用户图标居中
5. 导出为PNG

### 2. 应用Logo (logo.png)

#### 方案A: 简单文字Logo

使用Canva快速生成:

```
网址: https://www.canva.com/

步骤:
1. 注册/登录（免费）
2. 搜索"Logo"模板
3. 选择简洁风格的模板
4. 修改文字为"CT-WMS"或"西藏电信"
5. 调整颜色为品牌紫色 #7C3AED
6. 导出为PNG (256×256px)
```

#### 方案B: 使用IconPark组合

```
创意: 仓库图标 + 文字

步骤:
1. 在IconPark下载仓库图标（较大尺寸，如200px）
2. 使用Photopea或Canva添加文字
3. 组合为Logo
4. 导出256×256px PNG
```

#### 方案C: 临时Logo

如果时间紧张，可以先使用简单的纯色Logo:

```
1. 创建256×256px紫色背景
2. 添加白色文字 "CT" 或 "WMS"
3. 使用系统字体（加粗）
4. 居中对齐
5. 导出PNG
```

### 3. 空状态插图

#### 使用Undraw（推荐）

```
网址: https://undraw.co/illustrations

推荐插图:
1. Empty (无数据): 搜索 "empty" → 下载
2. No data (无数据): 搜索 "no data" → 下载
3. Mailbox (无消息): 搜索 "mailbox" → 下载
4. Void (空): 搜索 "void" → 下载

操作:
1. 搜索关键词
2. 修改主色调为 #7C3AED（页面右上角）
3. 下载SVG
4. 转换为PNG（300×200px）
5. 重命名为对应文件名
```

#### 使用Storyset

```
网址: https://storyset.com/

推荐场景:
- "Empty box" → empty-data.png
- "No data" → empty-search.png
- "Notification" → empty-message.png
- "Warehouse" → empty-inventory.png
- "Error" → empty-network.png

操作:
1. 搜索场景
2. 选择喜欢的插画
3. 自定义颜色为紫色系
4. 下载PNG
5. 使用在线工具调整尺寸至300×200px
```

---

## 文件放置位置

### TabBar图标
```
miniprogram/static/tabbar/
├── home.png
├── home-active.png
├── apply.png
├── apply-active.png
├── approval.png
├── approval-active.png
├── inventory.png
├── inventory-active.png
├── message.png
├── message-active.png
├── mine.png
├── mine-active.png
├── warehouse.png
└── warehouse-active.png
```

### 图片资源
```
miniprogram/static/images/
├── avatar-default.png (200×200px)
├── logo.png (256×256px)
├── empty-data.png (300×200px)
├── empty-search.png (300×200px)
├── empty-message.png (300×200px)
├── empty-apply.png (300×200px)
├── empty-inventory.png (300×200px)
└── empty-network.png (300×200px)
```

---

## 验证清单

### TabBar图标检查
- [ ] 14个图标文件全部存在（7个功能 × 2个状态）
- [ ] 文件名符合规范（小写，短横线分隔）
- [ ] 图标尺寸为81×81px
- [ ] 未选中状态为灰色 (#999999)
- [ ] 选中状态为紫色 (#7C3AED)
- [ ] 背景透明（PNG格式）
- [ ] 图标风格统一（线性图标）
- [ ] 文件大小 < 10KB

### 图片资源检查
- [ ] 默认头像存在且尺寸正确 (200×200px)
- [ ] Logo存在且尺寸正确 (256×256px)
- [ ] 空状态插图至少有3个常用的
- [ ] 所有图片已压缩优化
- [ ] 图片颜色符合品牌色系
- [ ] 图片文件大小合理

### 显示效果测试
- [ ] 在微信开发者工具中显示正常
- [ ] 图标清晰不模糊
- [ ] 颜色正确
- [ ] 选中/未选中切换正常
- [ ] 无加载错误

---

## 快速压缩优化

所有图片准备好后，使用TinyPNG批量压缩:

```
网址: https://tinypng.com/

操作:
1. 将所有PNG文件拖入页面
2. 等待压缩完成（通常减少50-70%大小）
3. 点击"Download all"下载压缩后的文件
4. 替换原文件
```

---

## 时间估算

| 任务 | 预计时间 |
|------|---------|
| TabBar图标下载 | 15分钟 |
| 默认头像生成 | 2分钟 |
| Logo设计/下载 | 5-10分钟 |
| 空状态插图下载 | 10分钟 |
| 图片压缩优化 | 5分钟 |
| 文件整理 | 3分钟 |
| **总计** | **40-45分钟** |

---

## 常见问题

### Q: 我不会设计，能否直接使用别人的图标？

A: 可以！只要确保：
1. 图标明确标注"免费商用"或使用开源协议
2. Iconfont、IconPark、Flaticon免费区的图标都可以使用
3. 如需商用，查看具体授权说明

### Q: 图标颜色怎么改？

A: 三种方法：
1. 下载时直接设置颜色（Iconfont、IconPark支持）
2. 使用Photopea在线PS修改
3. 修改SVG代码中的`stroke`或`fill`属性

### Q: 图标太大或太小怎么办？

A: 使用在线工具调整:
- Squoosh (https://squoosh.app/)
- iLoveIMG (https://www.iloveimg.com/resize-image)

### Q: 能否使用emoji作为临时图标？

A: 不推荐。微信小程序TabBar不支持emoji，必须使用图片文件。

---

## 推荐工作流

### 最快方式（15分钟）

```
1. 使用Iconfont下载所有TabBar图标
   - 搜索 → 设置颜色 → 下载 → 重命名
   - 重复7次（每个图标下载2个颜色版本）

2. 使用UI Avatars生成默认头像
   - 访问链接 → 右键保存

3. 使用Canva快速生成Logo
   - 选择模板 → 修改文字和颜色 → 导出

4. 使用Undraw下载2-3个空状态插图
   - 搜索 → 修改颜色 → 下载 → 转PNG

5. 使用TinyPNG压缩所有图片
   - 拖入文件 → 下载压缩版本

6. 放置文件到对应目录
```

### 最佳质量方式（60分钟）

```
1. 在Figma创建设计系统
   - 统一设计所有图标
   - 使用Figma插件（Iconify）导入基础图标
   - 调整为统一风格

2. 设计自定义Logo
   - 结合品牌元素
   - 专业排版

3. 设计完整的空状态插图集
   - 统一风格
   - 符合业务场景

4. 批量导出并优化
   - 使用Figma批量导出
   - 自动化压缩
```

---

## 资源链接汇总

### 图标库
- Iconfont: https://www.iconfont.cn/
- IconPark: https://iconpark.oceanengine.com/
- Flaticon: https://www.flaticon.com/
- Feather Icons: https://feathericons.com/

### 插画库
- Undraw: https://undraw.co/
- Storyset: https://storyset.com/
- DrawKit: https://www.drawkit.io/
- Illustrations: https://illlustrations.co/

### 工具
- Photopea (在线PS): https://www.photopea.com/
- TinyPNG (压缩): https://tinypng.com/
- Squoosh (优化): https://squoosh.app/
- CloudConvert (转换): https://cloudconvert.com/

### 头像/Logo
- UI Avatars: https://ui-avatars.com/
- DiceBear: https://www.dicebear.com/
- Canva: https://www.canva.com/

---

**最后更新**: 2025-11-25
**文档版本**: v1.0
**预计完成时间**: 30-60分钟
**难度等级**: ★★☆☆☆

**提示**: 建议先完成TabBar图标，这是最关键的部分。其他图片可以后续逐步完善。
