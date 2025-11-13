# TabBar 图标生成工具

## 当前状态

✅ **小程序已可正常运行** - 当前使用 Emoji 作为临时 tabBar 图标

## 生成真实图标的方法

### 方法1: Python 脚本（推荐）

**步骤：**

```bash
# 1. 安装依赖
pip install Pillow

# 2. 运行脚本
python miniprogram/scripts/generate-tabbar-icons.py

# 3. 恢复 pages.json 中的图标配置（见下文）
```

### 方法2: Node.js 脚本

**步骤：**

```bash
# 1. 安装依赖（注意：canvas 模块可能需要系统依赖）
npm install canvas

# 2. 运行脚本
node miniprogram/scripts/generate-tabbar-icons.js

# 3. 恢复 pages.json 中的图标配置（见下文）
```

### 方法3: 在线图标资源（最简单）

从以下网站下载图标并放到 `miniprogram/static/tabbar/` 目录：

- **Iconfont**: https://www.iconfont.cn/
- **IconPark**: https://iconpark.oceanengine.com/
- **Flaticon**: https://www.flaticon.com/

**所需图标：**
- `home.png` + `home-active.png` (首页)
- `apply.png` + `apply-active.png` (申请)
- `inventory.png` + `inventory-active.png` (库存)
- `mine.png` + `mine-active.png` (我的)

**尺寸要求：** 81x81px，PNG 格式

### 方法4: 使用设计工具手动创建

使用 Figma、Sketch、Photoshop 等工具：

1. 创建 81x81px 的画布
2. 绘制简单的图标
3. 未选中状态用灰色 `#8c8c8c`
4. 选中状态用蓝色 `#1890ff`
5. 导出为 PNG 格式

## 恢复图标配置

生成图标后，在 `pages.json` 中恢复原有配置：

```json
"tabBar": {
  "color": "#8c8c8c",
  "selectedColor": "#1890ff",
  "backgroundColor": "#ffffff",
  "borderStyle": "black",
  "list": [
    {
      "pagePath": "pages/index/index",
      "text": "首页",
      "iconPath": "static/tabbar/home.png",
      "selectedIconPath": "static/tabbar/home-active.png"
    },
    {
      "pagePath": "pages/apply/list",
      "text": "申请",
      "iconPath": "static/tabbar/apply.png",
      "selectedIconPath": "static/tabbar/apply-active.png"
    },
    {
      "pagePath": "pages/inventory/list",
      "text": "库存",
      "iconPath": "static/tabbar/inventory.png",
      "selectedIconPath": "static/tabbar/inventory-active.png"
    },
    {
      "pagePath": "pages/mine/mine",
      "text": "我的",
      "iconPath": "static/tabbar/mine.png",
      "selectedIconPath": "static/tabbar/mine-active.png"
    }
  ]
}
```

## 常见问题

**Q: Python 脚本报错找不到 Emoji 字体？**
A: 脚本会自动回退到默认字体，图标会显示为简单的圆形背景。你可以手动替换为更好的图标。

**Q: Node.js canvas 模块安装失败？**
A: canvas 模块需要系统级依赖，Windows 用户建议使用 Python 方案或直接下载图标。

**Q: 不想生成图标，继续使用 Emoji 可以吗？**
A: 可以！当前配置已经可以正常运行，Emoji 作为临时图标完全够用。

## 推荐方案

🌟 **快速上手**: 保持当前 Emoji 配置，直接开始开发
🎨 **正式发布**: 使用方法3从在线图标库下载专业图标
