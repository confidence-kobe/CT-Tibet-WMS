# TabBar 配置指南

## 目录
- [配置概述](#配置概述)
- [员工端TabBar配置](#员工端tabbar配置)
- [仓管端TabBar配置](#仓管端tabbar配置)
- [动态TabBar切换](#动态tabbar切换)
- [配置参数详解](#配置参数详解)
- [最佳实践](#最佳实践)
- [常见问题](#常见问题)

---

## 配置概述

### 什么是TabBar？

TabBar是小程序底部的导航栏，用于在不同页面之间切换。CT-Tibet-WMS小程序需要根据用户角色显示不同的TabBar:
- **员工端**: 5个Tab（首页、申请、库存、消息、我的）
- **仓管端**: 7个Tab（首页、申请、审批、库存、消息、仓库、我的）

### 配置文件位置

```
miniprogram/pages.json
```

---

## 员工端TabBar配置

### 完整配置示例

```json
{
  "pages": [
    "pages/index/index",
    "pages/apply/list",
    "pages/inventory/query",
    "pages/message/list",
    "pages/mine/index"
  ],
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
      },
      {
        "pagePath": "pages/apply/list",
        "text": "申请",
        "iconPath": "static/tabbar/apply.png",
        "selectedIconPath": "static/tabbar/apply-active.png"
      },
      {
        "pagePath": "pages/inventory/query",
        "text": "库存",
        "iconPath": "static/tabbar/inventory.png",
        "selectedIconPath": "static/tabbar/inventory-active.png"
      },
      {
        "pagePath": "pages/message/list",
        "text": "消息",
        "iconPath": "static/tabbar/message.png",
        "selectedIconPath": "static/tabbar/message-active.png"
      },
      {
        "pagePath": "pages/mine/index",
        "text": "我的",
        "iconPath": "static/tabbar/mine.png",
        "selectedIconPath": "static/tabbar/mine-active.png"
      }
    ]
  }
}
```

### 配置说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `color` | HexColor | 是 | 未选中时的文字颜色 |
| `selectedColor` | HexColor | 是 | 选中时的文字颜色 |
| `backgroundColor` | HexColor | 是 | TabBar背景色 |
| `borderStyle` | String | 否 | TabBar上边框颜色，仅支持`black`/`white` |
| `list` | Array | 是 | Tab列表，最少2个，最多5个 |

---

## 仓管端TabBar配置

### 完整配置示例

```json
{
  "pages": [
    "pages/index/index",
    "pages/apply/list",
    "pages/approval/pending",
    "pages/inventory/query",
    "pages/message/list",
    "pages/warehouse/list",
    "pages/mine/index"
  ],
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
      },
      {
        "pagePath": "pages/apply/list",
        "text": "申请",
        "iconPath": "static/tabbar/apply.png",
        "selectedIconPath": "static/tabbar/apply-active.png"
      },
      {
        "pagePath": "pages/approval/pending",
        "text": "审批",
        "iconPath": "static/tabbar/approval.png",
        "selectedIconPath": "static/tabbar/approval-active.png"
      },
      {
        "pagePath": "pages/inventory/query",
        "text": "库存",
        "iconPath": "static/tabbar/inventory.png",
        "selectedIconPath": "static/tabbar/inventory-active.png"
      },
      {
        "pagePath": "pages/message/list",
        "text": "消息",
        "iconPath": "static/tabbar/message.png",
        "selectedIconPath": "static/tabbar/message-active.png"
      },
      {
        "pagePath": "pages/warehouse/list",
        "text": "仓库",
        "iconPath": "static/tabbar/warehouse.png",
        "selectedIconPath": "static/tabbar/warehouse-active.png"
      },
      {
        "pagePath": "pages/mine/index",
        "text": "我的",
        "iconPath": "static/tabbar/mine.png",
        "selectedIconPath": "static/tabbar/mine-active.png"
      }
    ]
  }
}
```

### 与员工端的区别

仓管端相比员工端多出2个Tab:
1. **审批** (`pages/approval/pending`) - 处理员工的物资申请
2. **仓库** (`pages/warehouse/list`) - 仓库管理功能

---

## 动态TabBar切换

### 方案概述

由于微信小程序的限制，`pages.json`中的`tabBar`配置是静态的，无法在运行时动态修改。因此需要使用以下方案实现角色切换:

### 推荐方案: 自定义TabBar

#### 1. 启用自定义TabBar

```json
// pages.json
{
  "tabBar": {
    "custom": true,
    "color": "#999999",
    "selectedColor": "#7C3AED",
    "backgroundColor": "#FFFFFF",
    "list": [
      // 包含所有可能的Tab（最多7个，取员工端和仓管端的并集）
      {
        "pagePath": "pages/index/index",
        "text": "首页"
      },
      {
        "pagePath": "pages/apply/list",
        "text": "申请"
      },
      {
        "pagePath": "pages/approval/pending",
        "text": "审批"
      },
      {
        "pagePath": "pages/inventory/query",
        "text": "库存"
      },
      {
        "pagePath": "pages/message/list",
        "text": "消息"
      },
      {
        "pagePath": "pages/warehouse/list",
        "text": "仓库"
      },
      {
        "pagePath": "pages/mine/index",
        "text": "我的"
      }
    ]
  }
}
```

#### 2. 创建自定义TabBar组件

```
miniprogram/custom-tab-bar/
├── index.wxml
├── index.wxss
├── index.js
└── index.json
```

**index.js 示例**:

```javascript
Component({
  data: {
    selected: 0,
    color: "#999999",
    selectedColor: "#7C3AED",
    // 根据用户角色动态设置
    list: []
  },

  lifetimes: {
    attached() {
      this.setTabBarByRole();
    }
  },

  methods: {
    // 根据角色设置TabBar
    setTabBarByRole() {
      const userInfo = wx.getStorageSync('userInfo');
      const role = userInfo?.role || 'USER';

      let list = [];

      if (role === 'WAREHOUSE' || role === 'DEPT_ADMIN') {
        // 仓管端TabBar（7个）
        list = [
          {
            pagePath: "/pages/index/index",
            text: "首页",
            iconPath: "/static/tabbar/home.png",
            selectedIconPath: "/static/tabbar/home-active.png"
          },
          {
            pagePath: "/pages/apply/list",
            text: "申请",
            iconPath: "/static/tabbar/apply.png",
            selectedIconPath: "/static/tabbar/apply-active.png"
          },
          {
            pagePath: "/pages/approval/pending",
            text: "审批",
            iconPath: "/static/tabbar/approval.png",
            selectedIconPath: "/static/tabbar/approval-active.png"
          },
          {
            pagePath: "/pages/inventory/query",
            text: "库存",
            iconPath: "/static/tabbar/inventory.png",
            selectedIconPath: "/static/tabbar/inventory-active.png"
          },
          {
            pagePath: "/pages/message/list",
            text: "消息",
            iconPath: "/static/tabbar/message.png",
            selectedIconPath: "/static/tabbar/message-active.png"
          },
          {
            pagePath: "/pages/warehouse/list",
            text: "仓库",
            iconPath: "/static/tabbar/warehouse.png",
            selectedIconPath: "/static/tabbar/warehouse-active.png"
          },
          {
            pagePath: "/pages/mine/index",
            text: "我的",
            iconPath: "/static/tabbar/mine.png",
            selectedIconPath: "/static/tabbar/mine-active.png"
          }
        ];
      } else {
        // 员工端TabBar（5个）
        list = [
          {
            pagePath: "/pages/index/index",
            text: "首页",
            iconPath: "/static/tabbar/home.png",
            selectedIconPath: "/static/tabbar/home-active.png"
          },
          {
            pagePath: "/pages/apply/list",
            text: "申请",
            iconPath: "/static/tabbar/apply.png",
            selectedIconPath: "/static/tabbar/apply-active.png"
          },
          {
            pagePath: "/pages/inventory/query",
            text: "库存",
            iconPath: "/static/tabbar/inventory.png",
            selectedIconPath: "/static/tabbar/inventory-active.png"
          },
          {
            pagePath: "/pages/message/list",
            text: "消息",
            iconPath: "/static/tabbar/message.png",
            selectedIconPath: "/static/tabbar/message-active.png"
          },
          {
            pagePath: "/pages/mine/index",
            text: "我的",
            iconPath: "/static/tabbar/mine.png",
            selectedIconPath: "/static/tabbar/mine-active.png"
          }
        ];
      }

      this.setData({ list });
    },

    // 切换Tab
    switchTab(e) {
      const data = e.currentTarget.dataset;
      const url = data.path;
      wx.switchTab({ url });
      this.setData({
        selected: data.index
      });
    }
  }
});
```

**index.wxml 示例**:

```xml
<view class="tab-bar">
  <view
    wx:for="{{list}}"
    wx:key="index"
    class="tab-bar-item"
    data-path="{{item.pagePath}}"
    data-index="{{index}}"
    bindtap="switchTab"
  >
    <image
      class="tab-bar-icon"
      src="{{selected === index ? item.selectedIconPath : item.iconPath}}"
    />
    <text
      class="tab-bar-text"
      style="color: {{selected === index ? selectedColor : color}}"
    >
      {{item.text}}
    </text>
  </view>
</view>
```

#### 3. 在每个TabBar页面中更新选中状态

```javascript
// pages/index/index.js
Page({
  onShow() {
    // 更新TabBar选中状态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0 // 当前页面在TabBar中的索引
      });
    }
  }
});
```

---

## 配置参数详解

### tabBar字段

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `color` | HexColor | 是 | - | Tab未选中时的文字颜色 |
| `selectedColor` | HexColor | 是 | - | Tab选中时的文字颜色 |
| `backgroundColor` | HexColor | 是 | - | TabBar背景色 |
| `borderStyle` | String | 否 | `black` | TabBar上边框颜色，仅支持`black`/`white` |
| `list` | Array | 是 | - | Tab列表，最少2个，最多5个（自定义TabBar可突破限制） |
| `position` | String | 否 | `bottom` | TabBar位置，仅支持`bottom`/`top` |
| `custom` | Boolean | 否 | `false` | 是否使用自定义TabBar |

### list中每个项的字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pagePath` | String | 是 | 页面路径，必须在`pages`中预先定义 |
| `text` | String | 是 | Tab按钮上的文字 |
| `iconPath` | String | 否 | 未选中时的图标路径，不支持网络图片 |
| `selectedIconPath` | String | 否 | 选中时的图标路径，不支持网络图片 |

### 推荐配置值

```json
{
  "color": "#999999",           // 中性灰色
  "selectedColor": "#7C3AED",   // 品牌紫色
  "backgroundColor": "#FFFFFF", // 纯白背景
  "borderStyle": "black"        // 黑色边框（iOS会显示为浅灰色）
}
```

---

## 最佳实践

### 1. 图标路径规范

```javascript
// ✅ 推荐：使用相对路径
"iconPath": "static/tabbar/home.png"

// ❌ 不推荐：使用绝对路径
"iconPath": "/static/tabbar/home.png"

// ❌ 不支持：网络图片
"iconPath": "https://example.com/icon.png"
```

### 2. 页面路径顺序

```json
{
  "pages": [
    // TabBar页面建议放在前面
    "pages/index/index",
    "pages/apply/list",
    "pages/inventory/query",
    "pages/message/list",
    "pages/mine/index",

    // 非TabBar页面放在后面
    "pages/apply/create",
    "pages/apply/detail",
    // ...其他页面
  ]
}
```

**原因**:
- `pages`数组的第一项将作为小程序的首页
- TabBar页面放在前面便于管理
- 非TabBar页面可以按模块分组

### 3. 文字长度控制

```javascript
// ✅ 推荐：2-4个字符
"text": "首页"
"text": "我的"
"text": "审批"

// ⚠️ 注意：过长会被截断
"text": "消息中心" // 可能显示为 "消息..."
```

### 4. 颜色对比度

确保文字颜色与背景色有足够的对比度:

```javascript
// 对比度计算器: https://webaim.org/resources/contrastchecker/

// ✅ 良好对比度
color: "#999999"           // 未选中灰色
backgroundColor: "#FFFFFF" // 白色背景
// 对比度: 4.5:1 ✓

// ✅ 优秀对比度
selectedColor: "#7C3AED"   // 选中紫色
backgroundColor: "#FFFFFF" // 白色背景
// 对比度: 7.2:1 ✓
```

### 5. 消息角标

如果需要在TabBar上显示消息角标（如未读消息数量），使用API:

```javascript
// 显示红点
wx.showTabBarRedDot({
  index: 3 // 消息Tab的索引
});

// 显示数字
wx.setTabBarBadge({
  index: 3,
  text: '5'
});

// 移除角标
wx.removeTabBarBadge({
  index: 3
});

// 隐藏红点
wx.hideTabBarRedDot({
  index: 3
});
```

---

## 常见问题

### Q1: TabBar页面最多只能5个吗？

**A**:
- 默认TabBar: 最多5个
- 自定义TabBar: 理论上无限制，但建议不超过7个（用户体验考虑）
- 本项目仓管端需要7个Tab，必须使用自定义TabBar

### Q2: 如何隐藏/显示TabBar？

**A**: 使用以下API:

```javascript
// 隐藏TabBar（用于全屏页面）
wx.hideTabBar();

// 显示TabBar
wx.showTabBar();

// 注意：这些API仅对当前页面生效，切换页面后会恢复
```

### Q3: TabBar图标不显示怎么办？

**A**: 检查以下几点:
1. 图标路径是否正确（不要以`/`开头）
2. 图标文件是否存在
3. 图标尺寸是否符合要求（81×81px）
4. 图标格式是否为PNG
5. 图标是否有透明背景

### Q4: 如何动态修改TabBar样式？

**A**: 使用以下API:

```javascript
// 修改TabBar文字颜色
wx.setTabBarStyle({
  color: '#999999',
  selectedColor: '#7C3AED',
  backgroundColor: '#FFFFFF',
  borderStyle: 'black'
});

// 修改某个Tab的文字
wx.setTabBarItem({
  index: 0,
  text: '主页'
});

// 注意：无法动态修改图标
```

### Q5: 自定义TabBar的性能如何？

**A**:
- 自定义TabBar使用Component实现，性能接近原生TabBar
- 注意优化点:
  - 避免在TabBar中使用复杂的计算
  - 图标使用本地资源，不使用网络图片
  - 减少不必要的`setData`调用

### Q6: 如何处理iPhone X等全面屏底部适配？

**A**: 自定义TabBar需要手动处理安全区域:

```css
/* index.wxss */
.tab-bar {
  padding-bottom: env(safe-area-inset-bottom);
  padding-bottom: constant(safe-area-inset-bottom); /* 兼容iOS < 11.2 */
}
```

### Q7: 如何在非TabBar页面跳转到TabBar页面？

**A**: 必须使用`wx.switchTab`:

```javascript
// ✅ 正确
wx.switchTab({
  url: '/pages/index/index'
});

// ❌ 错误：会报错
wx.navigateTo({
  url: '/pages/index/index'
});
```

### Q8: TabBar页面可以传参吗？

**A**:
- `wx.switchTab`不支持传参
- 解决方案:
  1. 使用全局数据（`getApp().globalData`）
  2. 使用本地存储（`wx.setStorageSync`）
  3. 使用事件总线（EventBus）

```javascript
// 方案1：使用全局数据
getApp().globalData.selectedTab = 2;
wx.switchTab({ url: '/pages/index/index' });

// 在目标页面读取
const selectedTab = getApp().globalData.selectedTab;
```

---

## 测试检查清单

### 配置测试
- [ ] `pages.json`语法正确，无报错
- [ ] 所有TabBar页面已在`pages`数组中注册
- [ ] 图标路径正确，文件存在
- [ ] 颜色值格式正确（6位HexColor）

### 功能测试
- [ ] 点击TabBar可以正常切换页面
- [ ] 当前页面的Tab正确高亮显示
- [ ] 图标切换正常（选中/未选中）
- [ ] 文字颜色切换正常
- [ ] 消息角标显示正常（如果有）

### 兼容性测试
- [ ] iOS设备显示正常
- [ ] Android设备显示正常
- [ ] 微信开发者工具显示正常
- [ ] 全面屏设备底部安全区域适配正常
- [ ] 不同屏幕尺寸下显示正常

### 角色切换测试（自定义TabBar）
- [ ] 员工角色显示5个Tab
- [ ] 仓管角色显示7个Tab
- [ ] 切换角色后TabBar正确更新
- [ ] 所有Tab页面都可以正常访问

---

## 参考资源

### 官方文档
- [微信小程序TabBar配置](https://developers.weixin.qq.com/miniprogram/dev/reference/configuration/app.html#tabBar)
- [自定义TabBar](https://developers.weixin.qq.com/miniprogram/dev/framework/ability/custom-tabbar.html)
- [TabBar相关API](https://developers.weixin.qq.com/miniprogram/dev/api/ui/tab-bar/wx.showTabBar.html)

### 相关文档
- `static/tabbar/ICON_GUIDE.md` - 图标设计指南
- `DESIGN_SYSTEM.md` - UI设计规范
- `pages.json` - 小程序配置文件

---

## 更新日志

### v1.0 (2025-11-24)
- 初始版本
- 添加员工端和仓管端配置示例
- 添加自定义TabBar实现方案
- 添加常见问题解答

---

**最后更新**: 2025-11-24
**文档版本**: v1.0
**维护者**: UI/UX团队
