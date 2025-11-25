# SVG源文件说明

本目录包含所有TabBar图标的SVG源文件。

## 文件列表

- `home.svg` - 首页图标
- `apply.svg` - 申请图标
- `approval.svg` - 审批图标
- `inventory.svg` - 库存图标
- `message.svg` - 消息图标
- `mine.svg` - 我的图标
- `warehouse.svg` - 仓库图标

## SVG特性

- 尺寸: 81×81px
- 线条粗细: 3px
- 使用 `currentColor` 以便轻松修改颜色
- 风格: 线性图标（Line Icons）
- 圆角: 圆润连接

## 如何转换为PNG

### 方法1: 在线工具（推荐）

#### 使用Photopea
1. 访问 https://www.photopea.com/
2. 拖入SVG文件
3. 文件 → 导出为 → PNG
4. 修改颜色（如需要）:
   - 图像 → 调整 → 替换颜色
   - 选择黑色部分 → 改为 #999999 或 #7C3AED
5. 导出并重命名

#### 使用SVG2PNG
1. 访问 https://svgtopng.com/
2. 上传SVG文件
3. 设置尺寸: 81×81px
4. 转换并下载

### 方法2: 使用Node.js脚本

如果你安装了Node.js，可以使用提供的自动化脚本:

```bash
# 在当前目录运行
node convert-svg-to-png.js
```

### 方法3: 使用ImageMagick

如果安装了ImageMagick:

```bash
# 单个转换
magick convert home.svg -resize 81x81 home.png

# 批量转换
for file in *.svg; do magick convert "$file" -resize 81x81 "${file%.svg}.png"; done
```

## 修改颜色

### 方法1: 直接修改SVG

在文本编辑器中打开SVG文件，查找并替换:

```xml
<!-- 未选中状态 (灰色) -->
<svg ... >
  <path stroke="#999999" ... />
</svg>

<!-- 选中状态 (紫色) -->
<svg ... >
  <path stroke="#7C3AED" ... />
</svg>
```

目前SVG使用 `currentColor`，需要替换为具体颜色值。

### 方法2: 使用CSS

在网页中使用时可以通过CSS控制:

```css
.icon-gray {
  color: #999999;
}

.icon-active {
  color: #7C3AED;
}
```

### 方法3: 使用在线工具

在Photopea或其他编辑器中:
1. 导入SVG
2. 选择路径图层
3. 修改描边颜色
4. 导出PNG

## 输出文件命名

每个SVG需要生成2个PNG文件:

| SVG源文件 | PNG输出1 (灰色) | PNG输出2 (紫色) |
|-----------|----------------|----------------|
| home.svg | home.png | home-active.png |
| apply.svg | apply.png | apply-active.png |
| approval.svg | approval.png | approval-active.png |
| inventory.svg | inventory.png | inventory-active.png |
| message.svg | message.png | message-active.png |
| mine.svg | mine.png | mine-active.png |
| warehouse.svg | warehouse.png | warehouse-active.png |

颜色规范:
- 未选中: #999999
- 选中: #7C3AED

## 质量检查

转换后请检查:
- [ ] 尺寸为81×81px
- [ ] 背景透明
- [ ] 线条清晰不模糊
- [ ] 颜色正确
- [ ] 文件大小 < 10KB

## 优化建议

转换后使用TinyPNG压缩:
1. 访问 https://tinypng.com/
2. 拖入所有PNG文件
3. 下载压缩后的版本
4. 替换原文件

通常可以减少50-70%的文件大小，且视觉上无损。

---

**最后更新**: 2025-11-25
