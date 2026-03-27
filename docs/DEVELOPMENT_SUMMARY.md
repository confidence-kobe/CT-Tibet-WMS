# CT-Tibet-WMS 开发文档汇总

> 西藏电信仓库管理系统 - 项目文档索引
> 更新时间: 2026-03-12 | 版本: 1.0.0

---

## 一、项目概览

### 1.1 系统简介

**CT-Tibet-WMS** (西藏电信仓库管理系统) 是一个面向电信行业的仓库管理解决方案，支持PC端和移动端（微信小程序）双端访问。

### 1.2 技术架构

| 层级 | 技术栈 |
|------|--------|
| 后端 | Spring Boot 2.7.18 + MyBatis-Plus + MySQL + Redis + RabbitMQ |
| 前端PC | Vue 3 + Vite + Element Plus + Pinia |
| 前端移动 | uni-app (微信小程序) |
| 安全 | Spring Security + JWT |
| 文档 | Knife4j (Swagger) |

### 1.3 系统模块

| 模块 | 说明 |
|------|------|
| 用户管理 | RBAC角色权限 |
| 基础数据 | 部门、仓库、物资 |
| 入库管理 | 采购/退货/调拨入库 |
| 出库管理 | 直接出库、申请出库 |
| 申请审批 | 员工申请 + 经理审批 |
| 库存管理 | 实时库存、预警 |
| 统计分析 | 出入库统计、使用分析 |
| 消息通知 | 站内消息、微信模板 |

---

## 二、文档索引

### 2.1 需求与设计

| 文档 | 路径 | 说明 |
|------|------|------|
| 产品需求文档 | `docs/PRD-产品需求文档.md` | 完整产品需求 |
| 需求分析 | `docs/需求分析.md` | 业务需求详细分析 |
| 项目架构文档 | `docs/项目架构文档.md` | 系统架构设计 |
| 数据库设计文档 | `docs/数据库设计文档.md` | 17张表设计说明 |
| 原型设计文档 | `docs/原型设计文档.md` | UI/UX原型 |

### 2.2 开发指南

| 文档 | 路径 | 说明 |
|------|------|------|
| 开发实施指导手册 | `docs/开发实施指导手册.md` | 开发规范与流程 |
| 后端README | `backend/README.md` | 后端项目说明 |
| 前端PC README | `frontend-pc/README.md` | Vue3前端说明 |
| 小程序README | `miniprogram/README.md` | 微信小程序说明 |

### 2.3 API文档

| 文档 | 路径 | 说明 |
|------|------|------|
| API接口文档 | `docs/API接口文档.md` | 接口详细说明 |
| API快速开始 | `docs/API_QUICK_START.md` | 快速入门指南 |
| API测试指南 | `docs/API_TEST_GUIDE.md` | 接口测试方法 |
| API参考文档 | `docs/API_REFERENCE.md` | 完整API参考 |

### 2.4 部署文档

| 文档 | 路径 | 说明 |
|------|------|------|
| 部署总览 | `docs/DEPLOYMENT_README.md` | 部署入口指南 |
| Docker部署 | `docs/DEPLOYMENT_DOCKER.md` | Docker Compose部署 |
| 手动部署 | `docs/DEPLOYMENT_MANUAL.md` | 传统部署方式 |
| CI/CD部署 | `docs/DEPLOYMENT_CICD.md` | GitHub Actions自动化 |
| 部署检查清单 | `docs/DEPLOYMENT_CHECKLIST.md` | 200+项检查项 |

### 2.5 数据库脚本

| 文档 | 路径 | 说明 |
|------|------|------|
| SQL脚本说明 | `sql/README.md` | 数据库初始化 |
| 一键安装指南 | `sql/一键安装指南.md` | 快速安装 |

### 2.6 用户手册

| 文档 | 路径 | 说明 |
|------|------|------|
| 用户手册 | `docs/USER_MANUAL.md` | 终端用户指南 |
| 快速开始 | `docs/QUICK_START_USER.md` | 用户快速入门 |
| 常见问题 | `docs/FAQ.md` | FAQ解答 |

### 2.7 小程序专项

| 文档 | 路径 | 说明 |
|------|------|------|
| 小程序部署 | `miniprogram/DEPLOYMENT.md` | 微信小程序部署 |
| 小程序入门 | `miniprogram/START.md` | 快速入门指南 |
| 项目总结 | `miniprogram/PROJECT_SUMMARY.md` | 项目完成总结 |
| 交付报告 | `miniprogram/DELIVERY_REPORT.md` | 交付物清单 |

---

## 三、快速开始

### 3.1 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 11+ |
| Node.js | 16+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| RabbitMQ | 3.12+ (可选) |

### 3.2 启动步骤

```bash
# 1. 初始化数据库
cd sql
mysql -u root -p < create_database.sql
mysql -u root -p ct_tibet_wms < schema.sql
mysql -u root -p ct_tibet_wms < init_data.sql

# 2. 启动后端
cd backend
mvn spring-boot:run

# 3. 启动前端PC
cd frontend-pc
npm install
npm run dev

# 4. 启动小程序
cd miniprogram
# 使用微信开发者工具打开
```

### 3.3 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 系统管理员 | admin | 123456 |
| 部门管理员 | wl_admin | 123456 |
| 仓库管理员 | wl_warehouse | 123456 |
| 普通员工 | wl_user1 | 123456 |

---

## 四、项目结构

```
CT-Tibet-WMS/
├── backend/                 # Spring Boot后端
│   ├── src/main/java/
│   │   └── com/ct/wms/
│   │       ├── controller/  # 13个控制器
│   │       ├── service/    # 13个Service
│   │       ├── mapper/     # 13个Mapper
│   │       ├── entity/     # 14个实体类
│   │       ├── dto/        # 数据传输对象
│   │       ├── config/     # 配置类
│   │       └── security/   # 安全模块
│   └── src/test/          # 测试代码
│
├── frontend-pc/           # Vue 3 PC端
│   ├── src/
│   │   ├── views/         # 35+页面
│   │   ├── api/           # 12个API模块
│   │   ├── components/    # 公共组件
│   │   ├── store/         # Pinia状态
│   │   └── router/        # 路由配置
│   └── dist/              # 构建产物
│
├── miniprogram/           # 微信小程序
│   ├── pages/             # 19个页面
│   ├── api/               # 14个API模块
│   └── components/        # 公共组件
│
├── docs/                  # 完整文档
│   ├── DEPLOYMENT_*.md   # 6份部署文档
│   ├── API_*.md           # 6份API文档
│   └── *.md               # 其他文档
│
└── sql/                   # 数据库脚本
    ├── schema.sql         # 17张表
    ├── init_data.sql      # 初始数据
    └── README.md          # 使用说明
```

---

## 五、核心功能流程

### 5.1 入库流程
```
仓管员 → 填写入库单 → 提交 → 库存增加
```

### 5.2 申请出库流程
```
员工 → 提交申请 → 经理审批 → 生成出库单 → 员工领取 → 库存扣减
```

### 5.3 直接出库流程
```
仓管员 → 填写出库单 → 提交 → 库存扣减
```

---

## 六、安全配置

### 6.1 生产环境必须配置

| 配置项 | 环境变量 | 说明 |
|--------|----------|------|
| JWT密钥 | `JWT_SECRET` | 至少32位强密钥 |
| CORS域名 | `CORS_ALLOWED_ORIGINS` | 具体域名，逗号分隔 |
| MySQL密码 | `MYSQL_PASSWORD` | 强密码 |
| Redis密码 | `REDIS_PASSWORD` | 强密码 |

### 6.2 生成JWT密钥
```bash
openssl rand -base64 32
```

---

## 七、测试

### 7.1 单元测试

```bash
# 运行所有测试
cd backend
mvn test

# 运行单个测试
mvn test -Dtest=UserServiceTest
```

### 7.2 测试覆盖

- Service层: 10个测试类
- Controller层: 2个测试类
- Mapper层: 1个测试类
- 集成测试: 1个测试类

---

## 八、常见问题

详见 `docs/FAQ.md`

### 8.1 数据库连接失败
- 检查MySQL服务是否启动
- 检查防火墙是否开放3306端口

### 8.2 Redis连接失败
- 检查Redis服务是否启动
- 检查密码配置是否正确

### 8.3 启动报错找不到Mapper
- 检查@MapperScan注解
- 检查Mapper接口位置

---

## 九、版本信息

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2025-11-11 | 初始版本 |
| v1.0.1 | 2026-03-12 | 安全修复、优化 |

---

## 十、联系支持

- 技术团队: dev@chinatelecom.cn
- 项目负责人: tech-lead@chinatelecom.cn
- 项目文档: `docs/INDEX.md`

---

*本文档为项目开发文档汇总，详细说明请参阅各专项文档*
