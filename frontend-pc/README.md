# CT-Tibet-WMS 前端项目

西藏电信仓库管理系统 - PC端管理系统

## 项目介绍

本项目是西藏电信仓库管理系统的PC端管理平台，采用Vue 3 + Vite + Element Plus技术栈开发，提供完整的仓库管理功能。

### 主要功能模块

- **基础管理**：物资管理、仓库管理、部门管理、用户管理
- **入库管理**：采购入库、退货入库、调拨入库
- **出库管理**：直接出库、申请出库、确认领取
- **申请管理**：员工申请、申请审批
- **库存管理**：库存查询、库存预警、库存流水
- **统计报表**：出入库统计、物资统计、使用分析
- **消息中心**：站内消息、系统通知

## 技术栈

- **框架**：Vue 3.3+ (Composition API)
- **构建工具**：Vite 5.0+
- **UI组件库**：Element Plus 2.4+
- **状态管理**：Pinia 2.1+
- **路由管理**：Vue Router 4.2+
- **HTTP客户端**：Axios 1.6+
- **图表库**：ECharts 5.4+
- **日期处理**：Day.js 1.11+
- **工具库**：Lodash-ES 4.17+
- **CSS预处理**：Sass 1.69+

## 项目结构

```
frontend-pc/
├── public/                  # 静态资源
├── src/
│   ├── api/                # API接口
│   │   ├── auth.js        # 认证相关
│   │   └── request.js     # Axios封装
│   ├── assets/            # 资源文件
│   ├── components/        # 全局组件
│   ├── directives/        # 自定义指令
│   ├── layout/            # 布局组件
│   │   ├── components/    # 布局子组件
│   │   │   ├── AppMain/  # 主内容区
│   │   │   ├── Header/   # 顶部导航
│   │   │   └── Sidebar/  # 侧边栏菜单
│   │   └── index.vue     # 布局主文件
│   ├── router/            # 路由配置
│   │   └── index.js      # 路由入口
│   ├── store/             # 状态管理
│   │   ├── modules/      # Store模块
│   │   │   ├── user.js   # 用户状态
│   │   │   └── app.js    # 应用状态
│   │   └── index.js      # Store入口
│   ├── styles/            # 全局样式
│   │   ├── index.scss    # 样式入口
│   │   ├── variables.scss # SCSS变量
│   │   ├── reset.scss    # 样式重置
│   │   └── transition.scss # 过渡动画
│   ├── utils/             # 工具函数
│   │   ├── auth.js       # 认证工具
│   │   └── validate.js   # 表单验证
│   ├── views/             # 页面组件
│   │   ├── login/        # 登录页
│   │   ├── dashboard/    # 首页
│   │   ├── basic/        # 基础管理
│   │   │   ├── material/ # 物资管理
│   │   │   ├── warehouse/# 仓库管理
│   │   │   ├── dept/     # 部门管理
│   │   │   └── user/     # 用户管理
│   │   ├── inbound/      # 入库管理
│   │   ├── outbound/     # 出库管理
│   │   ├── apply/        # 申请管理
│   │   ├── approval/     # 审批管理
│   │   ├── inventory/    # 库存管理
│   │   ├── statistics/   # 统计报表
│   │   ├── message/      # 消息中心
│   │   ├── profile/      # 个人中心
│   │   └── error/        # 错误页面
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── .env                   # 环境变量
├── .env.development      # 开发环境变量
├── .env.production       # 生产环境变量
├── index.html            # HTML模板
├── package.json          # 依赖配置
├── vite.config.js        # Vite配置
└── README.md            # 项目说明

## 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 7.0.0 或 yarn >= 1.22.0

### 安装依赖

```bash
# 使用npm
npm install

# 或使用yarn
yarn install
```

### 开发模式

```bash
# 启动开发服务器
npm run dev

# 服务器将运行在 http://localhost:3000
```

### 构建生产

```bash
# 构建生产环境
npm run build

# 预览构建结果
npm run preview
```

### 代码检查

```bash
# ESLint检查
npm run lint
```

## 开发规范

### 命名规范

- **文件命名**：使用小写字母和连字符（kebab-case）
  - 组件文件：`user-list.vue`
  - 工具文件：`format-date.js`

- **组件命名**：使用大驼峰（PascalCase）
  ```vue
  <template>
    <UserList />
  </template>
  ```

- **变量命名**：使用小驼峰（camelCase）
  ```javascript
  const userName = 'admin'
  const handleSubmit = () => {}
  ```

### 目录结构

- **页面组件**：放在 `src/views/` 目录下，按模块分类
- **全局组件**：放在 `src/components/` 目录下
- **API接口**：放在 `src/api/` 目录下，按模块划分文件
- **工具函数**：放在 `src/utils/` 目录下

### 路由配置

路由分为两类：

1. **静态路由**（constantRoutes）：无需权限即可访问
   - 登录页
   - 错误页（404、403）

2. **动态路由**（asyncRoutes）：需要根据权限动态加载
   - 业务功能页面

### 权限控制

基于角色的权限控制（RBAC）：

```javascript
// 路由配置中的权限设置
{
  path: '/basic/material',
  meta: {
    title: '物资管理',
    roles: ['admin', 'dept_admin'] // 只有这些角色可访问
  }
}
```

角色说明：
- `admin`：系统管理员
- `dept_admin`：部门管理员
- `warehouse`：仓库管理员
- `user`：普通员工

### API调用

使用统一的axios实例：

```javascript
import request from '@/api/request'

// GET请求
export function getMaterialList(params) {
  return request({
    url: '/api/material/list',
    method: 'get',
    params
  })
}

// POST请求
export function createMaterial(data) {
  return request({
    url: '/api/material/create',
    method: 'post',
    data
  })
}
```

### 状态管理

使用Pinia进行状态管理：

```javascript
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: {}
  }),
  actions: {
    async login(loginForm) {
      // 登录逻辑
    }
  }
})
```

### 样式规范

- 使用SCSS作为CSS预处理器
- 全局变量定义在 `src/styles/variables.scss`
- 组件样式使用scoped避免污染
- 使用设计规范中的颜色、间距等变量

```vue
<style lang="scss" scoped>
.container {
  padding: $spacing-md;
  background-color: $white-color;

  .title {
    color: $text-color-primary;
    font-size: $font-size-lg;
  }
}
</style>
```

## 页面开发模板

### 标准CRUD页面

参考 `src/views/basic/material/index.vue` 示例，包含：

1. **页面头部**：标题 + 操作按钮
2. **搜索表单**：查询条件 + 搜索/重置按钮
3. **数据表格**：数据展示 + 分页
4. **表单对话框**：新增/编辑表单
5. **详情对话框**：数据详情展示

### 页面模板结构

```vue
<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">页面标题</h2>
      <el-button type="primary">操作按钮</el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-form">
      <el-form :model="queryForm" :inline="true">
        <!-- 查询表单项 -->
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading">
        <!-- 表格列 -->
      </el-table>

      <!-- 分页 -->
      <el-pagination />
    </el-card>
  </div>
</template>

<script setup>
// 组件逻辑
</script>

<style lang="scss" scoped>
// 组件样式
</style>
```

## 环境配置

### 开发环境 (.env.development)

```env
VITE_APP_TITLE=仓库管理系统
VITE_APP_BASE_API=/api
VITE_APP_PORT=3000
```

### 生产环境 (.env.production)

```env
VITE_APP_TITLE=仓库管理系统
VITE_APP_BASE_API=https://api.example.com
```

## 部署说明

### 构建

```bash
npm run build
```

构建产物在 `dist/` 目录下。

### Nginx配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://backend-server:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 常见问题

### 1. 端口被占用

修改 `vite.config.js` 中的端口配置：

```javascript
server: {
  port: 3001  // 修改为其他端口
}
```

### 2. 跨域问题

在 `vite.config.js` 中配置代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://your-backend-url',
      changeOrigin: true
    }
  }
}
```

### 3. 路由刷新404

确保服务器配置了SPA的重写规则，将所有请求指向 `index.html`。

## 待开发页面

基于当前架构，以下页面待完善（可参考物资管理页面模板）：

- [ ] 仓库管理页面
- [ ] 部门管理页面
- [ ] 用户管理页面
- [ ] 入库单列表页面
- [ ] 新建入库单页面
- [ ] 出库单列表页面
- [ ] 新建出库单页面
- [ ] 确认领取页面
- [ ] 申请单列表页面
- [ ] 新建申请页面
- [ ] 审批管理页面
- [ ] 库存查询页面
- [ ] 库存预警页面
- [ ] 库存流水页面
- [ ] 出入库统计页面
- [ ] 物资统计页面
- [ ] 消息列表页面

## 相关文档

- [PRD产品需求文档](../docs/PRD-产品需求文档.md)
- [API接口文档](../docs/API接口文档.md)
- [数据库设计文档](../docs/数据库设计文档.md)
- [原型设计文档](../docs/原型设计文档.md)

## 联系方式

如有问题，请联系项目负责人或技术团队。

---

© 2025 西藏电信 · 技术团队
