# CT-Tibet-WMS 小程序端

西藏电信仓库管理系统 - 微信小程序端

## 技术栈

- **框架**: uni-app
- **UI组件**: uView UI (可选)
- **状态管理**: Vuex
- **HTTP库**: uni.request 封装

## 项目结构

```
miniprogram/
├── api/                    # API接口
│   ├── auth.js            # 认证授权
│   ├── apply.js           # 申请审批
│   ├── inventory.js       # 库存管理
│   ├── inbound.js         # 入库管理
│   ├── outbound.js        # 出库管理
│   ├── material.js        # 物资管理
│   ├── warehouse.js       # 仓库管理
│   ├── message.js         # 消息通知
│   ├── stats.js           # 统计报表
│   └── user.js            # 用户管理
├── components/             # 公共组件
│   ├── status-tag/        # 状态标签
│   ├── material-item/     # 物资项
│   ├── apply-card/        # 申请卡片
│   └── empty-state/       # 空状态
├── pages/                  # 页面
│   ├── index/             # 首页
│   ├── login/             # 登录
│   ├── apply/             # 申请管理
│   │   ├── list.vue       # 申请列表
│   │   ├── create.vue     # 新建申请
│   │   └── detail.vue     # 申请详情
│   ├── inventory/         # 库存查询
│   │   ├── list.vue       # 库存列表
│   │   └── detail.vue     # 库存详情
│   ├── approval/          # 审批管理
│   │   ├── list.vue       # 审批列表
│   │   └── detail.vue     # 审批详情
│   ├── inbound/           # 入库管理
│   │   ├── list.vue       # 入库列表
│   │   └── create.vue     # 快速入库
│   ├── outbound/          # 出库管理
│   │   ├── list.vue       # 出库列表
│   │   ├── create.vue     # 快速出库
│   │   └── pending.vue    # 待领取
│   └── mine/              # 我的
│       ├── mine.vue       # 个人中心
│       ├── messages.vue   # 消息通知
│       └── settings.vue   # 设置
├── static/                 # 静态资源
│   ├── images/            # 图片
│   └── tabbar/            # TabBar图标
├── store/                  # Vuex状态管理
│   └── index.js           # Store配置
├── utils/                  # 工具函数
│   ├── request.js         # 网络请求封装
│   └── constant.js        # 常量定义
├── App.vue                 # 应用入口
├── main.js                 # 应用配置
├── manifest.json           # 应用配置清单
├── pages.json              # 页面路由配置
├── uni.scss                # 全局SCSS变量
├── package.json            # 依赖配置
└── README.md               # 项目说明

```

## 功能模块

### 普通员工功能
- [x] 首页：申请状态统计、快捷操作
- [x] 申请管理：新建申请、查看申请列表、申请详情
- [x] 库存查询：查看库存、库存详情
- [x] 个人中心：用户信息、消息通知、设置

### 仓库管理员功能
- [x] 首页：今日数据、待办事项、快捷操作
- [x] 入库管理：快速入库、入库记录
- [x] 出库管理：快速出库、出库记录、待领取出库
- [x] 审批管理：审批列表、审批详情
- [x] 个人中心：用户信息、消息通知、设置

## 开发指南

### 环境要求

- Node.js >= 12.0.0
- 微信开发者工具
- HBuilderX (推荐) 或 VSCode

### 安装依赖

```bash
npm install
```

### 开发调试

1. 使用HBuilderX打开项目
2. 运行 -> 运行到小程序模拟器 -> 微信开发者工具
3. 或使用命令行：
```bash
npm run dev:mp-weixin
```

### 构建发布

```bash
npm run build:mp-weixin
```

构建完成后，在 `dist/build/mp-weixin` 目录下会生成小程序代码。

## 配置说明

### manifest.json

- `appid`: 微信小程序AppID（需要在微信公众平台申请）
- `mp-weixin.appid`: 同上

### pages.json

- `pages`: 页面路由配置
- `tabBar`: 底部导航栏配置
- `globalStyle`: 全局样式配置

### uni.scss

全局SCSS变量，包括：
- 主题颜色
- 字体大小
- 间距
- 圆角
- 状态颜色等

## API配置

### 请求地址

在 `utils/request.js` 中配置：

```javascript
// 开发环境
const DEV_BASE_URL = 'http://localhost:8080'
// 生产环境
const PROD_BASE_URL = 'https://wms.chinatelecom.cn'
```

### Token管理

Token存储在本地Storage中，在请求拦截器中自动添加到请求头：

```javascript
'Authorization': `Bearer ${token}`
```

### 错误处理

- 401：Token过期，自动跳转登录
- 403：无权限，提示用户
- 其他错误：显示错误信息

## 注意事项

1. **登录状态**：使用微信授权登录，需要配置微信小程序AppID
2. **权限控制**：根据用户角色显示不同的TabBar和功能
3. **网络请求**：所有请求需要在微信开发者工具中配置合法域名
4. **图片资源**：TabBar图标需要放置在 `static/tabbar/` 目录下
5. **数据刷新**：列表页面支持下拉刷新

## 已完成任务

- [x] 实现登录页面（微信授权登录）
- [x] 实现首页（员工/仓管双视图）
- [x] 实现申请管理模块（创建、列表、详情）
- [x] 实现审批管理模块（列表、详情、审批操作）
- [x] 实现库存查询模块
- [x] 实现入库管理模块（创建、列表、详情）
- [x] 实现出库管理模块（创建、待领取、列表、详情）
- [x] 实现个人中心模块（用户信息、消息、设置）
- [x] 实现消息通知系统
- [x] 完善错误处理和加载状态
- [x] 优化交互体验

### 待优化任务

- [ ] 添加TabBar图标资源（需设计）
- [ ] 添加默认头像图片
- [ ] 增加扫码功能
- [ ] 支持批量操作
- [ ] 添加统计报表
- [ ] 实现消息推送（订阅消息）

## 相关文档

- [uni-app官方文档](https://uniapp.dcloud.io/)
- [微信小程序官方文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- [需求分析文档](../docs/PRD-产品需求文档.md)
- [原型设计文档](../docs/原型设计文档.md)
- [API接口文档](../docs/API接口文档.md)
