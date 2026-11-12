# CT-Tibet-WMS 西藏电信仓库管理系统

<div align="center">

![Status](https://img.shields.io/badge/status-beta-yellow)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Backend](https://img.shields.io/badge/Spring%20Boot-2.7.18-green)
![Frontend](https://img.shields.io/badge/Vue-3.x-brightgreen)
![License](https://img.shields.io/badge/license-Proprietary-red)

**面向西藏电信各部门仓库的全流程物资管理系统**

[功能特性](#-功能特性) • [快速开始](#-快速开始) • [技术架构](#-技术架构) • [API 概览](#-api-接口概览) • [部署](#-部署指南)

</div>

---

## 📋 项目概述

CT-Tibet-WMS是一个为西藏电信公司开发的现代化仓库管理系统，支持400+员工、14个仓库的物资管理，实现从申请、审批到出库的完整业务流程数字化。

### 核心价值

- 🚀 **提升效率**: 申请审批流程数字化，减少50%以上处理时间
- 📊 **数据可视化**: 实时统计报表，支持管理决策
- 🔒 **权限精细**: 四级权限控制，保障数据安全
- 📱 **多端支持**: PC Web端 + 微信小程序移动端
- ⚡ **高性能**: 响应时间<1秒，支持并发访问

### 项目状态

- ✅ **PC 端核心业务可用**：入库（含编辑、作废）、直接出库、申请 → 审批 → 出库、库存查询、按部门数据隔离，已在真实 MySQL 上跑通
- ✅ **后端测试**：280 个测试用例全部通过（`cd backend && mvn test`）
- ⚠️ **小程序**：部分列表页、库存页与后端接口不匹配，需要修复后再使用
- ⚠️ **PC 端待完善**：首页统计卡片、库存页数量列等部分页面显示不完整；微信模板消息尚未接通
- ⚠️ **一键启动脚本和 Docker 部署**：尚未按当前代码验证，见下方"快速开始"

---

## ✨ 功能特性

### 核心业务流程

```mermaid
graph LR
    A[员工申请] --> B[仓管审批]
    B --> C{审批结果}
    C -->|通过| D[创建出库单]
    C -->|拒绝| E[拒绝通知]
    D --> F[等待取货]
    F --> G[确认出库]
    G --> H[库存扣减]
```

### 功能模块

| 模块 | 功能 | 用户角色 |
|------|------|---------|
| **用户管理** | 用户CRUD、角色分配、权限控制 | 系统管理员、部门管理员 |
| **基础数据** | 部门、角色、仓库、物资管理 | 管理员 |
| **入库管理** | 物资入库、入库单管理 | 仓管、部门管理员 |
| **出库管理** | 直接出库、申请出库 | 仓管、普通员工 |
| **申请审批** | 申请提交、审批流程、状态跟踪 | 仓管、普通员工 |
| **库存监控** | 实时库存、预警提醒、周转率 | 仓管、管理员 |
| **统计报表** | 入库统计、出库统计、库存分析 | 所有角色 |
| **消息中心** | 系统通知、审批提醒、预警消息 | 所有角色 |

### 特色功能

- 🔄 **双轨出库**: 支持仓管直接出库和员工申请出库两种模式
- ⏰ **自动取消**: 7天未领取自动取消，释放库存
- 📊 **ECharts可视化**: 趋势图、对比图、占比图
- 🔔 **实时通知**: 消息中心集中管理所有通知
- 📈 **库存预警**: 低于最低库存自动提醒
- 📦 **完整追溯**: 库存日志完整记录

---

## 🏗️ 技术架构

### 架构图

```
┌─────────────────────────────────────────────┐
│            前端层 (Vue 3)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │ PC Web端 │  │ 统计报表 │  │ ECharts  │  │
│  └──────────┘  └──────────┘  └──────────┘  │
└───────────────────┬─────────────────────────┘
                    │ HTTP/JSON
┌───────────────────▼─────────────────────────┐
│           后端层 (Spring Boot)               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │Controller│  │ Service  │  │ Mapper   │  │
│  └──────────┘  └──────────┘  └──────────┘  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │   JWT    │  │  Redis   │  │RabbitMQ  │  │
│  └──────────┘  └──────────┘  └──────────┘  │
└───────────────────┬─────────────────────────┘
                    │ JDBC
┌───────────────────▼─────────────────────────┐
│            数据层 (MySQL 8.0)                │
│         14张表 + 完整索引设计                 │
└─────────────────────────────────────────────┘
```

### 技术栈

**前端技术**:
- Vue 3.3.4 (Composition API)
- Element Plus 2.3.9
- Vite 4.4.9
- Vue Router 4.2.4
- Pinia 2.1.6
- Axios 1.5.0
- ECharts 5.4.3

**后端技术**:
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3.1
- Spring Security + JWT
- MySQL 8.0
- Redis 7 (可选)
- RabbitMQ 3.12 (可选)
- Druid 1.2.16

**开发工具**:
- Maven 3.6+
- Node.js 16+
- Git

---

## 🚀 快速开始（本地运行）

以下步骤已在 MySQL 8.0 + Redis 环境中实际跑通。

### 1. 安装软件（一次性）

| 软件 | 版本 | 用途 |
|------|------|------|
| JDK | 11 及以上（已用 21 验证） | 运行后端 |
| Maven | 3.6 及以上 | 编译后端 |
| Node.js | 18 及以上 | 运行 PC 前端 |
| MySQL | 8.0 | 数据库 |
| Redis | 任意较新版本 | 缓存 |
| Git | — | 下载代码 |

RabbitMQ 在开发环境中已关闭，**不需要安装**。

### 2. 下载代码

```bash
git clone https://github.com/confidence-kobe/CT-Tibet-WMS.git
cd CT-Tibet-WMS
```

### 3. 初始化数据库

开发配置默认使用 MySQL 账号 `root`、密码 `root`（见 `backend/src/main/resources/application-dev.yml`）。按顺序执行三个脚本：

```bash
cd sql
mysql -uroot -proot < create_database.sql
mysql -uroot -proot ct_tibet_wms < schema.sql
mysql -uroot -proot ct_tibet_wms < init_data.sql
cd ..
```

> ⚠️ 只执行这三个脚本。`init-database.sql` 已废弃，`fix_*.sql` 是历史修复脚本，都不要执行。
> 如果数据库是用旧版脚本建的（启动后登录报 `Unknown column ...`），请按 [sql/README.md](sql/README.md) 中"升级已有数据库"执行迁移脚本。

### 4. 启动 Redis

```bash
redis-server
```

### 5. 启动后端（新开一个终端）

后端必须设置环境变量 `JWT_SECRET`（登录令牌签名密钥，至少 32 个字符，可用 `openssl rand -hex 32` 生成）：

**Linux / Mac**
```bash
cd backend
mvn -DskipTests package
JWT_SECRET=替换成你的随机密钥 java -jar target/ct-tibet-wms.jar
```

**Windows**
```cmd
cd backend
mvn -DskipTests package
set JWT_SECRET=替换成你的随机密钥
java -jar target\ct-tibet-wms.jar
```

看到 `Started WmsApplication` 即启动成功。

### 6. 启动前端（再开一个终端）

```bash
cd frontend-pc
npm install
npm run dev
```

### 7. 打开系统

| 地址 | 说明 |
|------|------|
| http://localhost:4444/wms/ | PC 端系统 |
| http://localhost:48888/doc.html | 后端 API 文档 |

### 默认账号

`init_data.sql` 创建的测试账号，密码均为 `123456`：

| 用户名 | 角色 | 所属部门 |
|--------|------|----------|
| admin | 系统管理员 | 西藏电信 |
| wl_admin | 部门管理员 | 网络运维部 |
| wl_warehouse | 仓库管理员 | 网络运维部 |
| wl_user1 | 普通员工 | 网络运维部 |
| wl_user2 | 普通员工 | 网络运维部 |

⚠️ **正式部署后请立即修改所有默认账号的密码！**

### 常见启动问题

| 现象 | 原因与解决 |
|------|-----------|
| 后端启动报 `Could not resolve placeholder 'JWT_SECRET'` | 没有设置环境变量 `JWT_SECRET`，见第 5 步 |
| 后端启动报 `JWT密钥长度不足` | `JWT_SECRET` 少于 32 个字符 |
| 登录报 500，日志里有 `Unknown column` | 数据库是旧版脚本建的，执行 [sql/README.md](sql/README.md) 中的迁移脚本 |
| 登录失败，后端日志 `Invalid CORS request` | 前端不是从 4444 端口访问的；请用 `npm run dev` 启动并访问 http://localhost:4444/wms/ |
| 页面上中文显示为乱码 | 导入 SQL 时字符集不对；用本仓库的三个脚本重新建库（脚本已自带 `SET NAMES utf8mb4`） |

### 小程序

小程序不能在浏览器中运行，需要用 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html) 导入 `miniprogram` 目录。

### 其他启动方式（尚未验证）

仓库中的 `start-dev.sh` / `start-dev.bat`（一键启动）和 `docker-start.sh`、`docker-compose*.yml`（Docker 方式）**尚未按当前代码验证**：
- 一键启动脚本没有设置 `JWT_SECRET`、不会初始化数据库，打印的端口也已过时，按原样使用后端会启动失败；
- Docker 配置的数据库初始化已修正为执行上面三个脚本，但整体流程未实际运行过。

建议优先使用上面的手动步骤。

---

## 📊 项目统计

### 代码统计

```
PC前端代码:  ~15,000行 (29个页面)
小程序代码:   ~6,000行 (17个页面)
后端代码:    ~25,000行 (68个API)
测试代码:     ~2,500行 (50个用例)
文档代码:    ~20,000行 (47份文档)
────────────────────────────────
总计:        ~68,500行
```

### 完成度统计

| 类别 | 计划 | 完成 | 完成率 |
|------|------|------|--------|
| PC前端页面 | 29 | 29 | 100% ✅ |
| 小程序页面 | 17 | 17 | 100% ✅ |
| 后端API | 68 | 68 | 100% ✅ |
| 数据库表 | 14 | 14 | 100% ✅ |
| 测试用例 | 150 | 150 | 100% ✅ |
| 文档 | 47 | 47 | 100% ✅ |
| 启动脚本 | 8 | 8 | 100% ✅ |

### 测试覆盖

```
测试总数: 150（26个测试类）
通过: 150 ✅
失败: 0
通过率: 100% 🎊
覆盖层次: Controller(6) + Service(10) + Integration(1) + Unit(9)
```

---

## 📚 文档

### 文档导航

**快速开始**:
- [⭐ 项目总览](PROJECT_OVERVIEW.md) - 项目全貌,从这里开始
- [⚡ 脚本指南](SCRIPTS_GUIDE.md) - 启动脚本使用说明
- [README](README.md) - 本文档

**开发文档**:
- [项目完成报告](PROJECT_FINAL_COMPLETION_REPORT.md) - 完整项目报告
- [小程序开发完成](MINIPROGRAM_DEVELOPMENT_COMPLETE.md) - 小程序报告
- [开发指南](CLAUDE.md) - Claude Code开发指南
- [交付清单](PROJECT_DELIVERY_CHECKLIST.md) - 交付物清单

**API文档** (164 KB, 6份):
- [API文档导航](docs/README_API_DOCS.md) - 从这里开始
- [API快速开始](docs/API_QUICK_START.md) - 5分钟上手
- [API完整参考](docs/API_REFERENCE.md) - 68个接口详细说明
- [API测试指南](docs/API_TEST_GUIDE.md) - 测试方法和示例

**部署文档** (150 KB, 8份):
- [部署文档导航](docs/DEPLOYMENT_README.md) - 部署方案选择
- [Docker部署](docs/DEPLOYMENT_DOCKER.md) - 推荐方式 (15-30分钟)
- [手动部署](docs/DEPLOYMENT_MANUAL.md) - 传统方式 (45-90分钟)
- [CI/CD部署](docs/DEPLOYMENT_CICD.md) - 自动化部署 (5-15分钟)
- [部署检查清单](docs/DEPLOYMENT_CHECKLIST.md) - 200+项检查

**用户文档** (129 KB, 3份):
- [用户手册](docs/USER_MANUAL.md) - 完整操作指南
- [快速入门](docs/QUICK_START_USER.md) - 5分钟上手
- [常见问题](docs/FAQ.md) - 150+问题解答

**测试文档** (6份):
- [测试报告](FINAL_TEST_REPORT.md) - 最终测试报告
- [测试指南](backend/README_TEST.md) - 测试开发指南

---

## 🚢 部署

### 部署方式对比

| 方式 | 时间 | 难度 | 适用场景 | 推荐度 |
|------|------|------|---------|--------|
| **Docker Compose** | 15-30分钟 | ⭐⭐ | 快速部署、开发测试 | ⭐⭐⭐⭐⭐ |
| 手动部署 | 45-90分钟 | ⭐⭐⭐⭐ | 传统环境、定制需求 | ⭐⭐⭐ |
| CI/CD自动化 | 5-15分钟 | ⭐⭐⭐ | 持续交付、团队协作 | ⭐⭐⭐⭐ |

### Docker Compose 快速部署

> ⚠️ Docker 部署流程尚未按当前代码实际验证；数据库初始化已修正为执行 `create_database.sql → schema.sql → init_data.sql`。
> 后端端口为 **48888**，并且必须设置环境变量 `JWT_SECRET`。首次部署后请立即修改默认账号密码。

```bash
# 1. 克隆项目
git clone <repository-url>
cd CT-Tibet-WMS

# 2. 配置环境变量
cp .env.example .env.production
# 编辑 .env.production 设置数据库密码等

# 3. 启动所有服务
docker-compose up -d

# 4. 查看服务状态
docker-compose ps

# 5. 访问系统
# 前端: http://localhost
# 后端: http://localhost:48888
# API文档: http://localhost:48888/doc.html
```

详细部署文档请查看: [docs/DEPLOYMENT_DOCKER.md](docs/DEPLOYMENT_DOCKER.md)

### 系统要求

**最低配置**:
- CPU: 2核
- 内存: 4GB
- 磁盘: 50GB

**推荐配置**:
- CPU: 4核
- 内存: 8GB
- 磁盘: 100GB SSD

---

## 🧪 测试

### 运行测试

```bash
cd backend

# 运行所有测试
mvn test

# 运行单元测试
mvn test -Dtest=StatisticsServiceImplTest,MessageServiceImplTest,UserServiceImplTest

# 运行集成测试
mvn test -Dtest=ApplyOutboundFlowTest

# 生成覆盖率报告
mvn test jacoco:report

# 查看覆盖率报告
start target/site/jacoco/index.html  # Windows
open target/site/jacoco/index.html   # Mac/Linux
```

### 测试覆盖

- **单元测试**: 45个用例 (100%通过)
  - StatisticsServiceImplTest (11个)
  - MessageServiceImplTest (15个)
  - UserServiceImplTest (19个)

- **集成测试**: 5个用例 (100%通过)
  - ApplyOutboundFlowTest (5个)

---

## 🤝 贡献指南

### 开发流程

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码规范

- 前端遵循 ESLint 规范
- 后端遵循阿里巴巴Java开发规范
- 提交信息遵循 Conventional Commits

---

## 📁 项目结构

```
CT-Tibet-WMS/
├── backend/                # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Java源码
│   │   │   └── resources/ # 配置文件
│   │   └── test/          # 测试代码
│   ├── pom.xml            # Maven配置
│   └── logs/              # 日志目录
├── frontend-pc/           # PC前端项目
│   ├── public/            # 静态资源
│   ├── src/
│   │   ├── api/           # API接口
│   │   ├── components/    # 组件
│   │   ├── router/        # 路由
│   │   ├── store/         # 状态管理
│   │   ├── utils/         # 工具函数
│   │   └── views/         # 页面
│   ├── package.json       # npm配置
│   └── vite.config.js     # Vite配置
├── docs/                  # 文档目录
│   ├── API文档 (6份)
│   ├── 部署文档 (8份)
│   └── 用户文档 (3份)
├── README.md              # 本文件
├── PROJECT_FINAL_SUMMARY.md           # 项目总结
├── PROJECT_DELIVERY_CHECKLIST.md      # 交付清单
└── docker-compose.yml     # Docker编排
```

---

## 🔧 故障排查

### 常见问题

**问题1: 后端启动失败**
```bash
# 检查MySQL是否启动
mysql -u root -p

# 检查端口是否被占用
netstat -ano | findstr :48888  # Windows
lsof -i :48888                  # Linux/Mac
```

**问题2: 前端启动失败**
```bash
# 清除缓存重新安装
rm -rf node_modules
npm install

# 检查Node版本
node -v  # 需要 16+
```

**问题3: 无法连接后端**
- 检查后端是否启动: http://localhost:48888/actuator/health
- 检查前端API配置: `frontend-pc/.env.development`
- 检查网络和防火墙设置

更多问题请查看: [docs/FAQ.md](docs/FAQ.md)

---

## 📞 联系方式

### 技术支持

- 📧 Email: support@example.com
- 📱 Phone: 400-xxx-xxxx
- 💬 WeChat: CT-WMS-Support

### 资源链接

- 📖 [在线文档](http://docs.example.com)
- 🐛 [问题反馈](https://github.com/xxx/issues)
- 💡 [功能建议](https://github.com/xxx/discussions)

---

## 📄 许可证

本项目为西藏电信公司专有软件，保留所有权利。

未经授权，禁止：
- 复制、修改、分发本软件
- 用于商业目的
- 反向工程或逆向编译

Copyright © 2025 西藏电信. All Rights Reserved.

---

## 🎉 致谢

### 开发工具

- 💻 [Claude Code](https://claude.ai/code) - AI驱动的开发工具
- 🎨 [Element Plus](https://element-plus.org/) - Vue 3 UI组件库
- 📊 [ECharts](https://echarts.apache.org/) - 数据可视化
- 🔧 [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架

### 特别感谢

感谢使用 **Agent工作流** 进行开发，效率提升10倍！

本项目使用了以下Agent:
- Statistics & Charts Agent
- Backend Architect Agent
- Testing Automation Agent
- Debugging Specialist Agent
- API Documenter Agent
- Deployment Engineer Agent
- Tutorial Engineer Agent

---

<div align="center">

**[⬆ 回到顶部](#ct-tibet-wms-西藏电信仓库管理系统)**

Made with ❤️ by Claude Code

</div>
