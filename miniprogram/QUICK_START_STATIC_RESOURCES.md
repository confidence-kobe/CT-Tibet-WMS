# 静态资源准备 - 快速开始

## 10分钟快速了解

本指南帮助你快速了解CT-Tibet-WMS小程序的静态资源准备工作。

---

## 当前状态

- **文档准备**: ✅ 100% 完成 (6个文档，4000+行)
- **资源准备**: ⏳ 0% 完成 (需要获取36个资源文件)
- **下一步**: 根据指南获取静态资源

---

## 核心文档 (6个)

### 1. 设计系统 📐
**文件**: `DESIGN_SYSTEM.md` (890行)
**内容**: 完整的UI设计规范
- 颜色系统 (品牌紫 #7C3AED)
- 字体系统 (基于4px网格)
- 间距、圆角、阴影规范
- 组件设计规范
- 动效和响应式设计

**适合**: 设计师、前端开发者

---

### 2. TabBar配置指南 🎯
**文件**: `TABBAR_CONFIG_GUIDE.md` (705行)
**内容**: TabBar完整配置方案
- 员工端配置 (5个Tab)
- 仓管端配置 (7个Tab)
- 动态TabBar切换实现
- 完整代码示例
- 常见问题解答

**适合**: 前端开发者

---

### 3. 图标设计指南 🎨
**文件**: `static/tabbar/ICON_GUIDE.md` (533行)
**内容**: TabBar图标完整指南
- 14个图标的设计规范
- 尺寸: 81×81px
- 颜色: #999999 / #7C3AED
- 风格: 线性图标
- 5种获取方式详解
- 在线工具推荐

**适合**: UI设计师、资源获取人员

---

### 4. 图片资源指南 🖼️
**文件**: `static/images/IMAGE_GUIDE.md` (718行)
**内容**: 所有图片资源规范
- 头像、Logo、空状态插图
- 设计规范和尺寸要求
- 获取方式和工具推荐
- 优化和压缩技巧
- 使用示例代码

**适合**: UI设计师、资源获取人员

---

### 5. 资源检查清单 ✅
**文件**: `STATIC_RESOURCES_CHECKLIST.md` (538行)
**内容**: 完整的资源清单和进度跟踪
- 36个资源的详细列表
- 优先级划分
- 完成进度跟踪
- 质量检查标准
- 时间估算

**适合**: 项目经理、执行人员

---

### 6. 总结报告 📋
**文件**: `STATIC_RESOURCES_SUMMARY.md` (633行)
**内容**: 本次任务的完整总结
- 文档清单
- 设计规范摘要
- 资源获取建议
- 下一步行动计划
- 成功标准

**适合**: 所有人员 (推荐首先阅读)

---

## 需要的资源

### 必需 (16个) - 2-3小时

#### TabBar图标 (14个)
```
✓ 员工端: 首页、申请、库存、消息、我的 (10个文件)
✓ 仓管端: +审批、+仓库 (4个文件)

规格: 81×81px, PNG, 透明
颜色: 灰色(#999999) / 紫色(#7C3AED)
```

#### 基础图片 (2个)
```
✓ 默认头像: 200×200px
✓ Logo: 256×256px
```

### 推荐 (6个) - 30分钟

#### 空状态插图
```
✓ 无数据、搜索无结果、无消息
✓ 无申请、无库存、网络错误

规格: 300×200px, PNG
```

### 可选 (14个)
- 状态图标 (4个)
- 功能图标 (4个)
- 引导页 (3个)
- 其他 (3个)

---

## 快速获取流程 (2-3小时)

### 步骤1: TabBar图标 (1-2小时)

```
工具: Iconfont (https://www.iconfont.cn/)

1. 注册/登录
2. 搜索图标:
   - home (首页)
   - file-add (申请)
   - clipboard-check (审批)
   - box (库存)
   - bell (消息)
   - warehouse (仓库)
   - user (我的)

3. 选择线性风格
4. 下载 81×81px PNG
5. 分别下载灰色和紫色版本
6. 重命名:
   home.png / home-active.png
   apply.png / apply-active.png
   ... (以此类推)

7. 保存到: miniprogram/static/tabbar/
```

### 步骤2: 基础图片 (30分钟)

**默认头像**:
```
方法1: 使用API
https://ui-avatars.com/api/?name=用户&size=200&background=7C3AED&color=fff

方法2: Figma设计
- 200×200px圆形
- 紫色背景 #7C3AED
- 白色用户图标
```

**Logo**:
```
工具: Canva (https://www.canva.com/)
1. 选择Logo模板
2. 修改为紫色主题
3. 添加文字 "CT-WMS"
4. 导出 256×256px PNG
```

### 步骤3: 空状态插图 (30分钟)

```
工具: Undraw (https://undraw.co/)

1. 设置品牌色: #7C3AED
2. 搜索: empty, search, message, inventory, error
3. 下载 PNG
4. 调整尺寸: 300×200px
5. 保存到: miniprogram/static/images/empty/
```

---

## 配置步骤 (30分钟)

### 1. 更新 pages.json

```json
{
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#7C3AED",
    "backgroundColor": "#FFFFFF",
    "borderStyle": "black",
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页",
        "iconPath": "static/tabbar/home.png",
        "selectedIconPath": "static/tabbar/home-active.png"
      }
      // ... 其他Tab
    ]
  }
}
```

### 2. 测试验证

```
1. 在微信开发者工具中打开项目
2. 检查TabBar显示
3. 测试页面切换
4. 验证图标颜色
5. 真机测试
```

---

## 核心设计规范 (速查)

### 颜色
```
品牌紫: #7C3AED
浅紫:   #A78BFA
深紫:   #6D28D9

成功: #10B981 (绿)
警告: #F59E0B (橙)
错误: #EF4444 (红)
信息: #3B82F6 (蓝)

黑色: #1F2937 (主文本)
灰色: #6B7280 (次文本)
```

### 字体
```
H1: 72rpx / 600 (页面标题)
H2: 64rpx / 600 (区块标题)
正文: 32rpx / 400 (标准文本)
辅助: 24rpx / 400 (小文本)
```

### 间距
```
基于4px网格:
sm: 16rpx (8px)
md: 24rpx (12px)
lg: 32rpx (16px)
xl: 48rpx (24px)
```

### 圆角
```
sm: 8rpx (标签)
md: 16rpx (按钮、输入框)
lg: 24rpx (卡片)
xl: 32rpx (模态框)
```

---

## 推荐工具

### 图标库
- **Iconfont**: https://www.iconfont.cn/ (推荐 ★★★★★)
- **IconPark**: https://iconpark.oceanengine.com/ (推荐 ★★★★★)

### 插画库
- **Undraw**: https://undraw.co/ (推荐 ★★★★★)
- **Storyset**: https://storyset.com/ (推荐 ★★★★★)

### 优化工具
- **TinyPNG**: https://tinypng.com/ (图片压缩)
- **Squoosh**: https://squoosh.app/ (格式转换)

### 设计工具
- **Figma**: https://www.figma.com/ (UI设计)
- **Canva**: https://www.canva.com/ (快速设计)

---

## 检查清单

### 准备阶段
- [x] 阅读 STATIC_RESOURCES_SUMMARY.md
- [x] 了解设计规范 (DESIGN_SYSTEM.md)
- [ ] 准备Iconfont账号
- [ ] 准备图片编辑工具

### 执行阶段
- [ ] 获取TabBar图标 (14个)
- [ ] 生成默认头像 (1个)
- [ ] 设计Logo (1个)
- [ ] 下载空状态插图 (6个，可选)
- [ ] 压缩优化所有图片

### 配置阶段
- [ ] 更新 pages.json
- [ ] 创建空状态组件
- [ ] 创建头像组件
- [ ] 在开发工具中测试
- [ ] 真机测试

---

## 时间估算

| 阶段 | 任务 | 时间 |
|------|------|------|
| 准备 | 阅读文档、注册账号 | 30分钟 |
| 获取 | TabBar图标 | 1-2小时 |
| 获取 | 基础图片 | 30分钟 |
| 获取 | 空状态插图(可选) | 30分钟 |
| 配置 | pages.json + 测试 | 30分钟 |
| **总计** | **最小版本** | **2-3小时** |

---

## 成功标准

### 最小可行版本 (MVP)

完成后小程序可以正常运行：

- [ ] TabBar图标 (14个) ✓
- [ ] 默认头像 (1个) ✓
- [ ] Logo (1个) ✓
- [ ] pages.json配置完成 ✓
- [ ] 在开发工具中测试通过 ✓

### 完整版本

完成后达到生产就绪状态：

- [ ] 所有必需资源 (16个) ✓
- [ ] 所有空状态插图 (6个) ✓
- [ ] 所有组件已创建 ✓
- [ ] 真机测试通过 ✓
- [ ] 性能优化完成 ✓

---

## 常见问题

### Q: 文档太多，从哪里开始？

**A**: 按以下顺序阅读：
1. **STATIC_RESOURCES_SUMMARY.md** (15分钟) - 总览
2. **ICON_GUIDE.md** (20分钟) - 图标获取
3. **TABBAR_CONFIG_GUIDE.md** (15分钟) - 配置方法

然后就可以开始获取资源了！

### Q: 没有设计基础怎么办？

**A**: 不需要设计基础！
- 使用Iconfont直接下载现成图标
- 使用Undraw下载现成插画
- 使用UI Avatars API生成头像
- 使用Canva模板快速设计Logo

### Q: 需要什么工具？

**A**: 只需要浏览器！
- 所有工具都是在线的
- 不需要安装软件
- 免费资源足够使用

### Q: 如何确保质量？

**A**: 参考检查清单：
- 尺寸: 81×81px (TabBar图标)
- 格式: PNG (透明背景)
- 颜色: #999999 / #7C3AED
- 大小: < 10KB

使用TinyPNG压缩后即可。

---

## 下一步

### 立即开始 (现在)

1. **阅读总结文档** (15分钟)
   - 打开 `STATIC_RESOURCES_SUMMARY.md`
   - 了解整体情况

2. **访问Iconfont** (马上)
   - https://www.iconfont.cn/
   - 注册/登录账号

3. **开始获取图标** (1小时)
   - 搜索图标
   - 下载并重命名
   - 保存到指定目录

### 今天完成

4. **配置TabBar** (30分钟)
5. **测试显示** (30分钟)

### 本周完成

6. **准备其他资源** (1小时)
7. **创建组件** (2小时)
8. **优化和测试** (1小时)

---

## 帮助与支持

### 详细文档

- **设计规范**: `DESIGN_SYSTEM.md`
- **图标指南**: `static/tabbar/ICON_GUIDE.md`
- **图片指南**: `static/images/IMAGE_GUIDE.md`
- **配置指南**: `TABBAR_CONFIG_GUIDE.md`
- **检查清单**: `STATIC_RESOURCES_CHECKLIST.md`
- **完整总结**: `STATIC_RESOURCES_SUMMARY.md`

### 在线工具

- Iconfont: https://www.iconfont.cn/
- Undraw: https://undraw.co/
- TinyPNG: https://tinypng.com/
- Canva: https://www.canva.com/

---

## 文档统计

- **文档总数**: 6个
- **总行数**: 4,000+ 行
- **总字数**: 约50,000字
- **文件大小**: 约75KB

---

**准备好了吗？开始获取静态资源，让小程序焕发生机！**

**第一步**: 打开 https://www.iconfont.cn/ 开始获取TabBar图标

**预计完成时间**: 2-3小时

**完成后**: 小程序将拥有完整的视觉系统！

---

**最后更新**: 2025-11-24
**文档版本**: v1.0
**状态**: 已完成 ✅
