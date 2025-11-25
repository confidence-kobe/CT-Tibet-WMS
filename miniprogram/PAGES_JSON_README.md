# pages.json 配置说明

## TabBar 配置

当前 TabBar 已配置好颜色和页面路径,但图标文件路径已注释,等待图标文件准备好后取消注释即可使用。

### 当前配置

```json
{
  "tabBar": {
    "color": "#999999",           // 未选中时的文字颜色(灰色)
    "selectedColor": "#7C3AED",   // 选中时的文字颜色(品牌紫色)
    "backgroundColor": "#ffffff",  // TabBar背景色(白色)
    "borderStyle": "black",        // TabBar上边框颜色
    "list": [...]                  // Tab列表(4个)
  }
}
```

### 需要的图标文件

当图标文件准备好后,需要在 pages.json 的 tabBar.list 中为每个tab添加以下字段:

#### 员工端 TabBar (4个)

1. **首页**
   ```
   "iconPath": "static/tabbar/home.png"
   "selectedIconPath": "static/tabbar/home-active.png"
   ```

2. **申请**
   ```
   "iconPath": "static/tabbar/apply.png"
   "selectedIconPath": "static/tabbar/apply-active.png"
   ```

3. **库存**
   ```
   "iconPath": "static/tabbar/inventory.png"
   "selectedIconPath": "static/tabbar/inventory-active.png"
   ```

4. **我的**
   ```
   "iconPath": "static/tabbar/mine.png"
   "selectedIconPath": "static/tabbar/mine-active.png"
   ```

#### 仓管端扩展 (需添加审批tab时)

如果需要为仓管端添加"审批"tab,可在 tabBar.list 中插入:

```json
{
  "pagePath": "pages/approval/list",
  "text": "审批",
  "iconPath": "static/tabbar/approval.png",
  "selectedIconPath": "static/tabbar/approval-active.png"
}
```

### 图标规格要求

- **尺寸**: 81×81 px
- **格式**: PNG (透明背景)
- **颜色**:
  - iconPath (未选中): #999999 灰色
  - selectedIconPath (选中): #7C3AED 紫色
- **文件大小**: <10KB/个

### 如何获取图标

参考以下文档获取图标:

1. **快速方案**: `static/ASSETS_QUICK_START.md` (30分钟完成)
2. **图标指南**: `static/tabbar/ICON_GUIDE.md` (详细规范)
3. **SVG源文件**: `static/tabbar/svg-sources/` (可转PNG)
4. **在线工具**: `static/tabbar/svg-sources/convert-svg-to-png.js` (自动转换)

### 动态TabBar切换

如需根据用户角色动态切换TabBar,参考:
- `TABBAR_CONFIG_GUIDE.md` - TabBar动态切换实现方案

## 页面路由配置

### 已配置的17个页面

1. **认证**: pages/login/login
2. **首页**: pages/index/index
3. **申请**: pages/apply/list, pages/apply/create, pages/apply/detail
4. **审批**: pages/approval/list, pages/approval/detail
5. **库存**: pages/inventory/list, pages/inventory/detail
6. **入库**: pages/inbound/create, pages/inbound/list
7. **出库**: pages/outbound/create, pages/outbound/list, pages/outbound/pending
8. **个人中心**: pages/mine/mine, pages/mine/messages, pages/mine/settings

### 下拉刷新配置

已为以下列表页面启用下拉刷新:
- pages/index/index
- pages/apply/list
- pages/inventory/list
- pages/approval/list
- pages/inbound/list
- pages/outbound/list
- pages/outbound/pending
- pages/mine/messages

### 自定义导航栏

登录页面使用自定义导航栏:
```json
{
  "path": "pages/login/login",
  "style": {
    "navigationStyle": "custom"
  }
}
```

## 全局样式配置

```json
{
  "globalStyle": {
    "navigationBarTextStyle": "black",
    "navigationBarTitleText": "仓库管理系统",
    "navigationBarBackgroundColor": "#ffffff",
    "backgroundColor": "#fafafa"
  }
}
```

## 调试配置

condition 配置用于快速跳转到指定页面进行调试:

```json
{
  "condition": {
    "current": 0,
    "list": [
      { "name": "登录页", "path": "pages/login/login" },
      { "name": "新建申请", "path": "pages/apply/create" },
      { "name": "审批管理", "path": "pages/approval/list" }
    ]
  }
}
```

可根据需要添加更多调试页面。

## 注意事项

1. **JSON格式**: pages.json 是标准JSON文件,不支持注释
2. **图标路径**: 相对于项目根目录,不需要前缀 `/`
3. **TabBar限制**: 最少2个,最多5个tab
4. **页面顺序**: pages数组的第一项为小程序启动页面
5. **动态切换**: 如需根据角色切换TabBar,需使用uni.setTabBarItem等API

## 参考文档

- [uni-app pages.json官方文档](https://uniapp.dcloud.net.cn/collocation/pages.html)
- [TabBar配置指南](./TABBAR_CONFIG_GUIDE.md)
- [静态资源获取指南](./static/ASSETS_QUICK_START.md)
- [设计系统规范](./DESIGN_SYSTEM.md)
