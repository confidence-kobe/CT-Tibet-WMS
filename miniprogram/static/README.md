# 静态资源目录

本目录包含小程序所需的所有静态资源（图标、图片、字体等）。

---

## 目录结构

```
static/
├── tabbar/                    # TabBar图标
│   ├── svg-sources/          # SVG源文件（用于生成PNG）
│   │   ├── *.svg            # 7个图标的SVG源文件
│   │   ├── README.md        # SVG使用说明
│   │   └── convert-svg-to-png.js  # 自动转换脚本
│   ├── *.png                # PNG图标（需生成）
│   ├── ICON_GUIDE.md        # 图标设计指南
│   └── PLACEHOLDER_INSTRUCTIONS.txt
│
├── images/                   # 图片资源
│   ├── *.png                # 头像、Logo、空状态图等
│   ├── IMAGE_GUIDE.md       # 图片设计指南
│   ├── QUICK_DOWNLOAD_LINKS.md  # 快速下载链接
│   └── placeholder-generator.html  # 占位图生成器
│
└── README.md                # 本文件
```

---

## 快速开始

### 方案1: 推荐方案（Iconfont + 在线工具）

**预计时间**: 30分钟

1. **获取TabBar图标** (15分钟)
   - 阅读: `ASSETS_QUICK_START.md` → 方案1
   - 访问Iconfont网站下载图标
   - 放置到 `tabbar/` 目录

2. **获取图片资源** (15分钟)
   - 阅读: `images/QUICK_DOWNLOAD_LINKS.md`
   - 点击直接下载链接
   - 放置到 `images/` 目录

3. **压缩优化** (5分钟)
   - 访问 https://tinypng.com/
   - 批量压缩所有PNG文件

### 方案2: 快速占位方案（临时开发）

**预计时间**: 5分钟

1. **生成占位图**
   - 打开 `images/placeholder-generator.html`
   - 点击下载按钮保存所有图片

2. **使用SVG源文件**
   - TabBar图标使用 `tabbar/svg-sources/` 中的SVG
   - 在线转换为PNG（见SVG目录下的README）

3. **后续替换**
   - 有时间后逐步替换为正式设计图

---

## 资源清单

### TabBar图标（必需）

| 文件名 | 尺寸 | 颜色 | 状态 |
|--------|------|------|------|
| home.png | 81×81px | #999999 | 未选中 |
| home-active.png | 81×81px | #7C3AED | 选中 |
| apply.png | 81×81px | #999999 | 未选中 |
| apply-active.png | 81×81px | #7C3AED | 选中 |
| approval.png | 81×81px | #999999 | 未选中 |
| approval-active.png | 81×81px | #7C3AED | 选中 |
| inventory.png | 81×81px | #999999 | 未选中 |
| inventory-active.png | 81×81px | #7C3AED | 选中 |
| message.png | 81×81px | #999999 | 未选中 |
| message-active.png | 81×81px | #7C3AED | 选中 |
| mine.png | 81×81px | #999999 | 未选中 |
| mine-active.png | 81×81px | #7C3AED | 选中 |
| warehouse.png | 81×81px | #999999 | 未选中 |
| warehouse-active.png | 81×81px | #7C3AED | 选中 |

**共14个文件** (7个功能 × 2个状态)

### 图片资源

#### 必需（优先级高）

| 文件名 | 尺寸 | 用途 | 优先级 |
|--------|------|------|--------|
| avatar-default.png | 200×200px | 默认头像 | ⭐⭐⭐ |
| logo.png | 256×256px | 应用Logo | ⭐⭐⭐ |
| empty-data.png | 300×200px | 无数据提示 | ⭐⭐ |

#### 可选（逐步补充）

| 文件名 | 尺寸 | 用途 | 优先级 |
|--------|------|------|--------|
| empty-search.png | 300×200px | 搜索无结果 | ⭐ |
| empty-message.png | 300×200px | 无消息 | ⭐ |
| empty-apply.png | 300×200px | 无申请 | ⭐ |
| empty-inventory.png | 300×200px | 无库存 | ⭐ |
| empty-network.png | 300×200px | 网络错误 | ⭐ |

---

## 重要文档

### 设计规范
- `../DESIGN_SYSTEM.md` - 完整的设计系统文档
- `tabbar/ICON_GUIDE.md` - TabBar图标设计指南
- `images/IMAGE_GUIDE.md` - 图片资源设计指南

### 获取指南
- `ASSETS_QUICK_START.md` - 静态资源快速获取指南（推荐阅读）
- `images/QUICK_DOWNLOAD_LINKS.md` - 图片直接下载链接

### 工具
- `tabbar/svg-sources/convert-svg-to-png.js` - SVG转PNG脚本
- `images/placeholder-generator.html` - 占位图生成器

---

## 设计规范速查

### 颜色系统

```css
/* 品牌色 */
--primary: #7C3AED;           /* 主紫色 */
--primary-light: #A78BFA;     /* 浅紫色 */
--primary-dark: #6D28D9;      /* 深紫色 */

/* 状态色 */
--success: #10B981;           /* 成功（绿） */
--warning: #F59E0B;           /* 警告（橙） */
--error: #EF4444;             /* 错误（红） */
--info: #3B82F6;              /* 信息（蓝） */

/* 中性色 */
--text-primary: #1F2937;      /* 主要文本 */
--text-secondary: #6B7280;    /* 次要文本 */
--border: #E5E7EB;            /* 边框 */
--bg-page: #F9FAFB;           /* 页面背景 */
```

### 图标规范

```
尺寸: 81×81px (TabBar)
格式: PNG
背景: 透明
风格: 线性图标
线条: 2-3px
颜色: #999999 (未选中) / #7C3AED (选中)
```

### 图片规范

```
头像: 200×200px, PNG, 圆形
Logo: 256×256px, PNG
空状态: 300×200px, PNG
文件大小: 单个 <100KB, 页面总计 <1MB
```

---

## 验证清单

开发前请确认:

### TabBar图标
- [ ] 14个PNG文件全部存在
- [ ] 文件名正确（小写，短横线分隔）
- [ ] 尺寸为81×81px
- [ ] 背景透明
- [ ] 颜色正确（灰色/紫色）
- [ ] 图标风格统一（线性）
- [ ] 文件大小 <10KB

### 图片资源
- [ ] avatar-default.png 存在
- [ ] logo.png 存在
- [ ] 至少1个空状态图存在
- [ ] 所有图片已压缩
- [ ] 文件大小合理

### 显示测试
- [ ] 在开发者工具中显示正常
- [ ] 图标清晰不模糊
- [ ] 选中/未选中切换正常
- [ ] 无404或加载错误

---

## 常见问题

### Q1: 没有设计资源，如何快速开始？

**A**: 使用临时占位方案:
1. 打开 `images/placeholder-generator.html`
2. 下载所有占位图
3. 使用 `tabbar/svg-sources/` 中的SVG图标
4. 后续逐步替换为正式设计

### Q2: 如何获取专业设计资源？

**A**: 推荐以下免费资源:
- **图标**: Iconfont (https://www.iconfont.cn/)
- **插画**: Undraw (https://undraw.co/)
- **头像**: UI Avatars (https://ui-avatars.com/)
- **详细步骤**: 阅读 `ASSETS_QUICK_START.md`

### Q3: 图标太大/太小怎么办？

**A**: 使用在线工具调整:
- Squoosh: https://squoosh.app/
- iLoveIMG: https://www.iloveimg.com/resize-image

### Q4: 如何压缩图片？

**A**: 使用TinyPNG:
1. 访问 https://tinypng.com/
2. 拖入所有PNG文件
3. 下载压缩后的文件（减少50-70%大小）

### Q5: SVG如何转PNG？

**A**: 三种方法:
1. **在线工具** (最简单): https://www.photopea.com/
2. **命令行** (开发者): `node convert-svg-to-png.js`
3. **浏览器**: 打开SVG → 右键另存为

---

## 推荐工作流

### 初次准备（30-45分钟）

```
1. TabBar图标
   ├─ 访问Iconfont搜索下载
   ├─ 或转换svg-sources中的SVG
   └─ 放置到tabbar/目录

2. 必需图片
   ├─ 下载/生成默认头像
   ├─ 创建/下载Logo
   └─ 下载1-2个空状态图

3. 优化
   ├─ 使用TinyPNG压缩所有图片
   └─ 在开发者工具中测试

4. 开发
   └─ 可以开始使用这些资源进行开发
```

### 持续改进

```
1. 逐步替换占位图为专业设计
2. 补充更多空状态插图
3. 根据用户反馈优化图标
4. 保持文件大小优化
```

---

## 性能优化建议

### 文件大小控制

```
单个图标: <10KB
单个图片: <100KB
单页总计: <1MB
```

### 加载优化

```javascript
// 1. 预加载关键图片
wx.getImageInfo({ src: '/static/images/logo.png' });

// 2. 懒加载列表图片
<image lazy-load="true" src="{{url}}" />

// 3. 使用CDN（生产环境）
const CDN = 'https://cdn.example.com/wms/';
```

### 压缩工具

- **TinyPNG**: https://tinypng.com/ (推荐)
- **Squoosh**: https://squoosh.app/
- **ImageOptim**: https://imageoptim.com/ (Mac)

---

## 资源链接

### 免费图标库
- Iconfont: https://www.iconfont.cn/
- IconPark: https://iconpark.oceanengine.com/
- Flaticon: https://www.flaticon.com/
- Feather Icons: https://feathericons.com/

### 免费插画库
- Undraw: https://undraw.co/
- Storyset: https://storyset.com/
- DrawKit: https://www.drawkit.io/
- Illustrations: https://illlustrations.co/

### 在线工具
- Photopea (在线PS): https://www.photopea.com/
- TinyPNG (压缩): https://tinypng.com/
- Squoosh (优化): https://squoosh.app/
- CloudConvert (转换): https://cloudconvert.com/

### 设计工具
- Figma: https://www.figma.com/
- Canva: https://www.canva.com/
- Logo Maker: https://logomakr.com/

---

## 版本记录

### v1.0 (2025-11-25)
- 创建目录结构
- 提供SVG源文件
- 添加完整的获取指南
- 创建占位图生成器
- 整理设计规范文档

---

## 联系与支持

如有疑问或需要帮助:
1. 查看相关文档（ICON_GUIDE.md, IMAGE_GUIDE.md等）
2. 阅读快速开始指南（ASSETS_QUICK_START.md）
3. 联系项目UI/UX负责人

---

**最后更新**: 2025-11-25
**文档版本**: v1.0
**维护者**: UI/UX设计团队

**提示**:
- 优先完成TabBar图标，这是最关键的部分
- 图片资源可以逐步完善
- 使用占位图也可以开始开发，不必等待所有资源就绪
