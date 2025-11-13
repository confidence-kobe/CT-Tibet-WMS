# CT-Tibet-WMS iOS App

西藏电信仓库管理系统 iOS 原生客户端

## 项目介绍

这是一个使用 **SwiftUI** 开发的现代化 iOS 应用，为西藏电信仓库管理系统提供移动端支持。

## 技术栈

- **开发语言**: Swift 5.9+
- **UI框架**: SwiftUI
- **最低支持**: iOS 16.0+
- **架构模式**: MVVM
- **网络请求**: URLSession + async/await
- **状态管理**: @StateObject, @EnvironmentObject

## 功能模块

### 1. 用户登录
- 用户名密码登录
- Token持久化存储
- 自动登录

### 2. 角色权限
根据不同角色显示不同功能：

#### 仓管员/部门管理员
- ✅ 直接出库（无需审批）
- ✅ 审批员工申领
- ✅ 查看库存
- ✅ 确认提货

#### 普通员工
- ✅ 提交物料申领
- ✅ 查看申领记录
- ✅ 查看库存
- ✅ 取消申领

### 3. 核心功能

#### 仓管员端
- **直接出库**: 创建出库单，立即扣减库存
- **审批管理**: 审批/驳回员工申领，自动生成出库单
- **提货确认**: 确认员工提货，扣减库存

#### 员工端
- **物料申领**: 选择仓库和物料，提交申领单
- **申领跟踪**: 查看申领状态（待审批/已通过/已驳回/已完成/已取消）
- **申领取消**: 取消未审批的申领

#### 通用功能
- **库存查询**: 按仓库查看实时库存
- **物料搜索**: 按名称或编码搜索物料
- **个人中心**: 查看个人信息，修改设置

## 项目结构

```
WMS/
├── WMSApp.swift              # 应用入口
├── Models/                   # 数据模型
│   ├── User.swift           # 用户模型
│   ├── Material.swift       # 物料模型
│   ├── OutboundOrder.swift  # 出库单模型
│   └── Application.swift    # 申领单模型
├── Services/                # 服务层
│   ├── APIService.swift     # API接口服务
│   └── AuthService.swift    # 认证服务
└── Views/                   # 视图层
    ├── LoginView.swift             # 登录界面
    ├── MainTabView.swift           # 主标签页
    ├── HomeView.swift              # 首页
    ├── OutboundCreateView.swift    # 创建出库单
    ├── ApplicationListView.swift   # 申领列表
    ├── ApplicationCreateView.swift # 创建申领
    ├── ApprovalListView.swift      # 审批列表
    ├── InventoryListView.swift     # 库存列表
    └── ProfileView.swift           # 个人中心
```

## 如何运行

### 前置要求
- macOS 14.0+
- Xcode 15.0+
- iOS 16.0+ 设备或模拟器

### 运行步骤

1. **克隆项目**
```bash
cd ios-app
```

2. **配置后端API地址**

编辑 `WMS/Services/APIService.swift`，修改 `baseURL`：
```swift
private let baseURL = "http://your-backend-api:8080/api"
```

3. **打开项目**
```bash
open WMS.xcodeproj
```

4. **选择目标设备**
- 在 Xcode 中选择 iPhone 模拟器或真机

5. **运行应用**
- 点击 Xcode 的运行按钮（⌘+R）

## API接口说明

### 认证相关
- `POST /api/auth/login` - 用户登录
- `GET /api/user/current` - 获取当前用户信息

### 物料与库存
- `GET /api/material/list` - 获取物料列表
- `GET /api/inventory/list?warehouseId={id}` - 获取库存列表

### 出库管理
- `POST /api/outbound/create` - 创建直接出库单
- `GET /api/outbound/list` - 获取出库单列表
- `GET /api/outbound/pending` - 获取待提货出库单
- `POST /api/outbound/confirm/{id}` - 确认提货

### 申领管理
- `POST /api/apply/create` - 创建申领单
- `GET /api/apply/my` - 获取我的申领列表
- `POST /api/apply/cancel/{id}` - 取消申领

### 审批管理
- `GET /api/apply/pending` - 获取待审批列表
- `POST /api/apply/approve/{id}` - 审批申领（通过/驳回）

## 数据模型

### 用户角色
- `SYSTEM_ADMIN` - 系统管理员
- `DEPT_ADMIN` - 部门管理员
- `WAREHOUSE` - 仓管员
- `USER` - 普通员工

### 申领状态
- `0` - 待审批
- `1` - 已通过
- `2` - 已驳回
- `3` - 已完成（已提货）
- `4` - 已取消

### 出库单状态
- `0` - 待提货
- `1` - 已完成
- `2` - 已取消

## 特性亮点

1. **SwiftUI 原生开发**: 使用最新的 SwiftUI 框架，UI流畅自然
2. **异步网络请求**: 使用 async/await 处理网络请求，代码简洁
3. **角色权限控制**: 根据用户角色动态显示功能模块
4. **响应式设计**: 支持 iPhone 和 iPad
5. **本地持久化**: Token 和用户信息本地存储
6. **下拉刷新**: 列表支持下拉刷新
7. **搜索功能**: 库存列表支持实时搜索

## 开发计划

- [x] 用户登录
- [x] 角色权限控制
- [x] 仓管员直接出库
- [x] 员工申领功能
- [x] 仓管员审批功能
- [x] 库存查询
- [x] 个人中心
- [ ] 入库功能
- [ ] 消息通知
- [ ] 统计报表
- [ ] 暗黑模式
- [ ] 国际化支持

## 注意事项

1. **开发环境**: 本项目为学习示例，后端API需要自行部署
2. **权限配置**: Info.plist 中已允许HTTP请求（用于本地开发）
3. **数据安全**: 生产环境请使用HTTPS并加强Token管理
4. **错误处理**: 部分错误处理可进一步完善

## 截图预览

（可在此添加应用截图）

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 联系方式

如有问题，请联系项目维护者。

---

**注意**: 这是一个学习示例项目，用于展示iOS原生开发在仓库管理系统中的应用。
