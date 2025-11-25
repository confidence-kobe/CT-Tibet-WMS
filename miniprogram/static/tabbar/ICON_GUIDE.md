# TabBar 图标设计指南

## 图标清单

### 员工端 TabBar 图标 (5个)

| 序号 | 功能 | 未选中状态 | 选中状态 | 说明 |
|------|------|-----------|---------|------|
| 1 | 首页 | `home.png` | `home-active.png` | 房屋/主页图标 |
| 2 | 申请 | `apply.png` | `apply-active.png` | 文档/添加文件图标 |
| 3 | 库存 | `inventory.png` | `inventory-active.png` | 包裹/箱子图标 |
| 4 | 消息 | `message.png` | `message-active.png` | 铃铛/消息图标 |
| 5 | 我的 | `mine.png` | `mine-active.png` | 用户/个人图标 |

### 仓管端 TabBar 图标 (额外2个)

| 序号 | 功能 | 未选中状态 | 选中状态 | 说明 |
|------|------|-----------|---------|------|
| 6 | 审批 | `approval.png` | `approval-active.png` | 剪贴板/勾选图标 |
| 7 | 仓库 | `warehouse.png` | `warehouse-active.png` | 仓库/建筑图标 |

**共计**: 14个图标文件 (7个功能 × 2个状态)

---

## 设计规范

### 1. 尺寸规范

```
标准尺寸: 81 × 81 px
最小尺寸: 40 × 40 px (不推荐)
最大尺寸: 100 × 100 px (不推荐)
推荐尺寸: 81 × 81 px (微信小程序官方标准)
```

**注意**: 微信小程序会自动缩放图标至合适大小，但建议使用81×81px以获得最佳显示效果。

### 2. 文件格式

- **格式**: PNG (推荐)
- **背景**: 透明背景 (必须)
- **色彩模式**: RGB
- **位深度**: 32位 (支持透明度)

### 3. 颜色规范

#### 未选中状态
```
颜色值: #999999
RGB: rgb(153, 153, 153)
视觉效果: 中性灰色，不抢眼
透明度: 100%
```

#### 选中状态
```
主色调: #7C3AED (紫色)
RGB: rgb(124, 58, 237)
视觉效果: 渐变紫色，醒目
可选渐变: #7C3AED → #A78BFA (紫色渐变)
透明度: 100%
```

#### 颜色使用建议
- 未选中图标使用单一灰色填充
- 选中图标可使用纯色或渐变色
- 保持图标线条粗细一致（2-3px）
- 避免使用过多细节，确保小尺寸下可识别

### 4. 设计风格

#### 推荐风格: 线性图标（Line Icons）

**特点**:
- 简洁现代
- 视觉统一
- 适合小尺寸显示
- 符合扁平化设计趋势

**设计要素**:
```
线条粗细: 2-3px
圆角半径: 2px (可选)
间距: 保持一致
对齐: 居中对齐
内边距: 10-15px (图标与边界的距离)
```

#### 不推荐风格
- 写实风格（过于复杂）
- 3D立体效果（文件过大）
- 渐变过多（影响识别）
- 阴影效果（小尺寸下模糊）

---

## 图标设计建议

### 1. 首页图标 (home.png / home-active.png)
```
设计元素: 房屋轮廓
参考关键词: home, house, dashboard
设计要点:
  - 简单的房屋外形
  - 包含屋顶和墙体
  - 可选添加门或窗户细节
```

### 2. 申请图标 (apply.png / apply-active.png)
```
设计元素: 文档/添加符号
参考关键词: document, file-plus, add-document, form
设计要点:
  - 文档轮廓 + 加号
  - 或者笔/编辑图标
  - 表示创建新申请的动作
```

### 3. 审批图标 (approval.png / approval-active.png)
```
设计元素: 剪贴板 + 勾选
参考关键词: clipboard-check, task-check, approve
设计要点:
  - 剪贴板轮廓
  - 包含勾选标记
  - 表示审批/确认动作
```

### 4. 库存图标 (inventory.png / inventory-active.png)
```
设计元素: 包裹/箱子
参考关键词: package, box, inventory, cube
设计要点:
  - 立方体或包裹外形
  - 简洁的线条
  - 可选添加物流标签细节
```

### 5. 消息图标 (message.png / message-active.png)
```
设计元素: 铃铛/消息气泡
参考关键词: bell, notification, message, alert
设计要点:
  - 推荐使用铃铛图标
  - 或者消息气泡
  - 可选添加小红点提示位置
```

### 6. 我的图标 (mine.png / mine-active.png)
```
设计元素: 用户头像轮廓
参考关键词: user, person, profile, account
设计要点:
  - 简单的人形轮廓
  - 或者头像圆形
  - 避免性别特征
```

### 7. 仓库图标 (warehouse.png / warehouse-active.png)
```
设计元素: 仓库建筑
参考关键词: warehouse, building, storage, facility
设计要点:
  - 仓库建筑外形
  - 可包含大门或货架元素
  - 与"首页"图标区分明显
```

---

## 图标资源获取指南

### 方式1: 在线图标库 (推荐)

#### 1.1 Iconfont (阿里巴巴矢量图标库)
```
网址: https://www.iconfont.cn/
优势: 国内访问快，中文界面，图标丰富
步骤:
  1. 注册/登录账号
  2. 搜索图标关键词（如"home"、"warehouse"）
  3. 选择线性风格图标
  4. 点击"下载" → 选择PNG格式
  5. 设置尺寸为81×81px
  6. 分别下载灰色版本和紫色版本
  7. 重命名为规范名称
```

#### 1.2 IconPark (字节跳动图标库)
```
网址: https://iconpark.oceanengine.com/
优势: 风格统一，支持在线编辑颜色
步骤:
  1. 搜索所需图标
  2. 点击图标进入编辑页面
  3. 设置线条粗细（推荐3px）
  4. 设置颜色（#999999 或 #7C3AED）
  5. 下载PNG格式，81×81px
```

#### 1.3 Flaticon
```
网址: https://www.flaticon.com/
优势: 图标种类丰富，质量高
步骤:
  1. 搜索图标（英文关键词）
  2. 筛选"Line"风格
  3. 选择免费图标
  4. 下载PNG格式
  5. 使用在线工具调整颜色和尺寸
```

#### 1.4 其他优质图标库
```
Feather Icons: https://feathericons.com/ (极简线性图标)
Heroicons: https://heroicons.com/ (Tailwind官方图标)
Material Icons: https://fonts.google.com/icons (谷歌Material Design)
Remix Icon: https://remixicon.com/ (开源图标库)
```

### 方式2: 使用设计工具

#### 2.1 Figma (推荐 - 在线免费)
```
网址: https://www.figma.com/
优势: 免费，功能强大，支持协作
步骤:
  1. 创建81×81px画布
  2. 使用矢量工具绘制图标
  3. 或从Figma Community搜索免费图标包
  4. 导出为PNG格式

推荐Figma插件:
  - Iconify: 海量图标库
  - Feather Icons: 线性图标
  - Material Design Icons
```

#### 2.2 Sketch (Mac专用)
```
优势: 专业设计工具，矢量编辑
步骤:
  1. 创建Artboard (81×81px)
  2. 使用矢量工具绘制
  3. 导出为PNG @2x格式
```

#### 2.3 Adobe Illustrator / XD
```
优势: 专业级矢量设计
步骤:
  1. 创建81×81px画布
  2. 绘制矢量图标
  3. 导出为PNG格式
```

### 方式3: AI工具生成 (实验性)

#### 3.1 Midjourney / DALL-E
```
提示词模板:
"simple line icon of [subject], minimalist, monochrome,
transparent background, 81x81 pixels, flat design"

示例:
"simple line icon of warehouse building, minimalist,
monochrome, transparent background, flat design"
```

#### 3.2 Figma AI插件
```
推荐插件:
  - Magician (AI图标生成)
  - Diagram (AI设计助手)
```

---

## 图标制作流程

### 标准流程
```
1. 选择图标源
   ↓
2. 下载/生成SVG或PNG原图
   ↓
3. 调整尺寸至81×81px
   ↓
4. 修改颜色
   - 未选中: #999999
   - 选中: #7C3AED
   ↓
5. 确保背景透明
   ↓
6. 优化文件大小
   ↓
7. 重命名并保存到指定目录
   ↓
8. 测试显示效果
```

### 颜色调整工具

#### 在线工具
```
1. Photopea (在线PS): https://www.photopea.com/
   - 上传PNG → 图像 → 调整 → 色相/饱和度

2. Remove.bg (背景移除): https://www.remove.bg/zh
   - 确保背景透明

3. TinyPNG (图片压缩): https://tinypng.com/
   - 优化文件大小

4. Squoosh (图片优化): https://squoosh.app/
   - 调整尺寸和压缩
```

#### 命令行工具
```bash
# 使用ImageMagick批量调整
convert input.png -resize 81x81 -background none -flatten output.png

# 批量改变颜色
convert input.png -fuzz 20% -fill "#7C3AED" -opaque black output.png
```

---

## 快速获取示例

### 推荐方案: Iconfont 一站式获取

```bash
# 1. 访问 Iconfont
https://www.iconfont.cn/

# 2. 搜索并下载以下图标（推荐图标ID）
首页: icon-home (搜索"主页")
申请: icon-file-add (搜索"添加文件")
审批: icon-clipboard-check (搜索"审批")
库存: icon-box (搜索"包裹")
消息: icon-bell (搜索"铃铛")
我的: icon-user (搜索"用户")
仓库: icon-warehouse (搜索"仓库")

# 3. 下载设置
格式: PNG
尺寸: 81×81px
颜色: #999999 (未选中) / #7C3AED (选中)

# 4. 重命名规则
home.png, home-active.png
apply.png, apply-active.png
... (以此类推)
```

---

## 文件命名规范

### 命名格式
```
功能名称.png              # 未选中状态
功能名称-active.png       # 选中状态
```

### 完整文件列表
```
static/tabbar/
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

## 质量检查清单

### 设计质量
- [ ] 图标风格统一（都是线性图标）
- [ ] 线条粗细一致（2-3px）
- [ ] 图标大小比例协调
- [ ] 可识别性强（小尺寸下清晰）
- [ ] 符合功能语义

### 技术规格
- [ ] 尺寸: 81×81px
- [ ] 格式: PNG
- [ ] 背景: 透明
- [ ] 颜色: #999999 / #7C3AED
- [ ] 文件大小: <10KB

### 显示效果
- [ ] 在iOS设备上清晰
- [ ] 在Android设备上清晰
- [ ] 在微信开发者工具中显示正常
- [ ] 选中/未选中状态对比明显

---

## 注意事项

### 1. 版权问题
```
✅ 推荐使用:
  - 明确标注"免费商用"的图标
  - CC0协议图标
  - MIT/Apache开源图标
  - 自己设计的图标

❌ 避免使用:
  - 未授权的商业图标
  - 标注"仅限个人使用"的图标
  - 来源不明的图标
```

### 2. 文件大小优化
```
单个图标文件大小建议:
  理想: < 5KB
  可接受: 5-10KB
  需优化: > 10KB

优化方法:
  - 使用TinyPNG压缩
  - 减少不必要的细节
  - 使用8位PNG而非24位
```

### 3. 兼容性测试
```
必须测试的设备:
  - iPhone (iOS 12+)
  - Android (Android 7+)
  - 微信开发者工具

测试要点:
  - 图标是否清晰
  - 颜色是否正确
  - 点击区域是否正常
  - 切换动画是否流畅
```

### 4. 更新维护
```
当需要更新图标时:
  1. 保持文件名不变
  2. 保持尺寸规格不变
  3. 更新所有相关状态图标
  4. 重新测试显示效果
  5. 更新版本号（如需要）
```

---

## 常见问题解答

### Q1: 图标显示模糊怎么办？
```
A: 检查以下几点：
   1. 确认图标尺寸为81×81px
   2. 确认图标是PNG格式，非JPEG
   3. 尝试导出@2x尺寸（162×162px）
   4. 检查图标本身设计是否过于复杂
```

### Q2: 图标颜色不正确？
```
A: 可能原因：
   1. PNG图标本身是彩色的，需要使用单色图标
   2. 图标使用了蒙版或滤镜效果
   3. 建议使用SVG转PNG，便于颜色控制
```

### Q3: 选中和未选中图标不一样？
```
A: 可以有两种方案：
   方案1: 使用同一图标，仅改变颜色
   方案2: 选中状态使用填充版本，未选中使用线性版本
   推荐: 方案1（更统一）
```

### Q4: 如何批量制作图标？
```
A: 推荐流程：
   1. 在Figma中创建图标组件
   2. 创建两个颜色变体（灰色/紫色）
   3. 使用Figma插件批量导出
   4. 使用脚本批量重命名
```

---

## 参考资源

### 设计指南
- [微信小程序设计指南](https://developers.weixin.qq.com/miniprogram/design/)
- [iOS Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
- [Material Design Icons](https://material.io/design/iconography)

### 图标库集合
- [Iconfont](https://www.iconfont.cn/)
- [IconPark](https://iconpark.oceanengine.com/)
- [Flaticon](https://www.flaticon.com/)
- [The Noun Project](https://thenounproject.com/)

### 工具推荐
- [Figma](https://www.figma.com/)
- [Photopea](https://www.photopea.com/)
- [TinyPNG](https://tinypng.com/)
- [Squoosh](https://squoosh.app/)

---

## 联系与反馈

如有问题或建议，请联系项目UI/UX负责人。

**最后更新**: 2025-11-24
**文档版本**: v1.0
