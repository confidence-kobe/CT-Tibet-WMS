# CT-Tibet-WMS 前端项目代码解读指南

## 1. 项目概览
本项目是西藏电信仓库管理系统的 PC 端前端应用，采用现代化的前端技术栈构建。

### 技术栈
- **核心框架**: Vue 3 (Composition API)
- **构建工具**: Vite 5
- **状态管理**: Pinia (配合持久化插件)
- **UI 组件库**: Element Plus
- **图表库**: ECharts 5
- **路由**: Vue Router 4
- **网络请求**: Axios

---

## 2. 目录结构说明
```
src/
├── api/            # API 请求封装，按业务模块划分文件
├── assets/         # 静态资源（图片、全局样式）
├── components/     # 公共组件（图表封装、状态标签、表格卡片等）
├── constants/      # 全局常量定义
├── directives/     # 自定义指令（如权限控制 v-permission）
├── layout/         # 页面布局结构（侧边栏、顶栏、主内容区）
├── router/         # 路由配置及动态权限拦截
├── store/          # Pinia 状态管理模块
├── styles/         # 全局 SCSS 样式及变量
├── utils/          # 工具函数（格式化、校验、授权相关）
└── views/          # 业务页面模块
```

---

## 3. 核心机制解读

### 3.1 权限控制与动态路由 (`src/router/index.js`)
项目采用“前端路由表 + 后端角色过滤”的方案：
1. **静态路由 (`constantRoutes`)**: 包含登录页、404 等无需权限的页面。
2. **动态路由 (`asyncRoutes`)**: 包含所有业务模块，配置有 `meta.roles` 属性。
3. **路由守卫**:
   - 检查 `token` 是否存在。
   - 若存在，判断是否已获取用户信息。
   - 调用 `userStore.generateRoutes()` 根据角色过滤出可访问路由，并通过 `router.addRoute` 动态加载。

### 3.2 状态管理 (`src/store/`)
- **User Store**: 管理 Token、用户信息、角色、权限以及生成的路由表。使用了 `pinia-plugin-persistedstate` 进行本地持久化。
- **App Store**: 管理侧边栏折叠状态、设备类型（桌面/移动适配）和主题设置。

### 3.3 网络请求封装 (`src/api/request.js`)
基于 Axios 的二次封装，主要处理：
- **请求拦截**: 自动在 Header 中注入 `Authorization: Bearer <token>`。
- **响应拦截**: 
  - 统一处理 API 返回码（200 为成功）。
  - **401 处理**: 清除 Token 并跳转登录，设有防抖防止多个请求同时失败导致弹窗重叠。
  - **403 处理**: 提示无权限操作。

---

## 4. 关键业务模块

### 4.1 数据看板 (`src/views/dashboard/`)
- 整合了 ECharts 展示出入库趋势。
- **生命周期优化**: 页面销毁前自动清除定时器，释放 ECharts 实例，防止内存泄漏。

### 4.2 出入库管理 (`src/views/inbound/`, `src/views/outbound/`)
- 包含列表查询、详情查看及单据操作。
- **状态流转**: 通过 `StatusTag` 组件统一展示单据状态（待领取、已完成、已取消）。

### 4.3 统计报表 (`src/views/statistics/`)
- 高度复用了 `EChart` 和 `TableCard` 组件。
- 支持多维度筛选（日期、仓库、物资分类）并实时刷新统计数据。

---

## 5. 开发规范指南

### 5.1 组件开发
- 优先使用 `<script setup>` 语法糖。
- 公共组件应存放在 `src/components/`，并遵循 BEM 样式命名规范。
- 复杂图表使用 `src/components/Chart/EChart.vue` 进行包装，确保响应式适配。

### 5.2 样式管理
- 全局变量定义在 `src/styles/variables.scss`。
- 页面私有样式使用 `<style lang="scss" scoped>`。

### 5.3 环境配置
- `.env.development`: 开发环境，配置有 Vite Proxy 代理。
- `.env.production`: 生产环境，配置 API 基础路径。

---

## 6. 构建与部署
- **构建命令**: `npm run build`
- **产物目录**: `dist/`
- **注意事项**: 部署到子路径时，需注意 `vite.config.js` 中的 `base` 配置。
