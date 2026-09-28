# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**CT-Tibet-WMS**（西藏电信仓库管理系统）— warehouse management system for a telecom company in Tibet.

**Status**: 三端开发完成（后端 + PC 前端 + 微信小程序），处于收尾/部署阶段。后端 236 个测试全部通过。

## Technology Stack (Actual)

- **Backend**: Spring Boot 2.7.18, Java 11, Spring Security + JWT, MyBatis-Plus 3.5.5, Maven — port **48888**
- **Database**: MySQL 8 (`ct_tibet_wms`)；测试用 H2（schema 在 `backend/src/test/resources/schema.sql`）
- **Cache/MQ**: Redis + RabbitMQ — 均为可选依赖（`@Profile("!test")` / `@ConditionalOnBean(RabbitTemplate)`），不可用时业务自动降级
- **PC Frontend**: Vue 3 + Vite + Element Plus (`frontend-pc/`, dev port **4444**, proxy → 127.0.0.1:48888)
- **Mini Program**: 微信小程序原生 (`miniprogram/`)；另有 uni-app (`frontend-mobile/`) 与 iOS (`ios-app/`) 两端
- **Deploy**: Docker Compose（`docker-compose.prod.yml`）

## Commands

```bash
# 后端（在 backend/ 目录）
mvn spring-boot:run          # 启动，profile 见 application-dev.yml
mvn test                     # 全量测试（H2，无需 MySQL/Redis/RabbitMQ）
mvn test -Dtest=ApplyServiceImplTest   # 单个测试类

# PC 前端（在 frontend-pc/ 目录）
npm run dev                  # Vite dev server (4444)
npm run build

# 一键启动开发环境（仓库根目录）
start-dev.bat                # Windows
```

## Architecture

标准三层：Controller (`controller/`) → Service (`service/` + `service/impl/`) → Mapper (`mapper/`, MyBatis-Plus)。

关键包：
- `event/` — 业务通知事件（`NotificationEvent` + `@TransactionalEventListener(AFTER_COMMIT)` 监听器，事务提交后发通知，RabbitMQ 不可用时降级直接写站内信）
- `mq/` — RabbitMQ 生产者/消费者（站内信 + 微信模板消息）
- `schedule/` — 超时定时任务（审批 24h 提醒/7 天取消、取货 5 天提醒/7 天取消），Redis 分布式锁防重复执行
- `utils/OrderNoGenerator` — 业务单号（SQ/CK/RK 前缀），Redis INCR 流水号，降级雪花取模

## Core Business Rules

**两轨出库**：
1. 直接出库（仓管员/部门管理员）：无需审批，锁定库存后立即扣减（lockInventory → commitInventory）
2. 申请出库（普通员工）：提交申请 → 仓管员审批通过（**锁定库存** + 自动创建待取货出库单，同一事务）→ 员工取货、仓管员确认（**锁定转扣减**）→ 完成。7 天未取货定时任务自动取消并释放锁定

**库存三字段**：`quantity`（总量）/ `lockedQuantity`（锁定）/ `availableQuantity`（可用=总-锁定），乐观锁 version + FOR UPDATE 行锁双保险。审批只锁定不扣减，取货确认才扣减。

**状态机**（所有状态变更必须用条件更新 `where status=旧状态` 并校验影响行数，禁止裸 `updateById` 改状态——防并发竞态）：
- 申请单：0 待审批 → 1 已通过 → 3 已完成；0→2 已拒绝；0/1→4 已取消
- 出库单：0 待取货 → 1 已完成；0→2 已取消（直接出库直接为 1）

**角色**（`tb_role.role_code`）：ADMIN / DEPT_ADMIN / WAREHOUSE / USER。数据隔离：部门管理员只能看本部门，仓管员只能审批自己管理仓库的申请。

## Gotchas（踩过的坑）

- **实体虚拟字段**：`Apply.approvalRemark`、`Apply.applyReason` 等标了 `@TableField(exist = false)`，写它们不会持久化。真实列是 `approvalOpinion` / `purpose` / `rejectReason`。改实体前先确认字段是否有 `exist = false`
- **取消申请的库存释放**：已审批申请的锁定库存由"取消关联出库单"(`cancelOutboundByApplyId`) 统一释放，**不要**在申请侧再解锁一遍（会双重释放）
- **锁顺序**：涉及申请单+出库单双行更新时，先抢出库单行、后抢申请单行（confirmOutbound / cancelApply 已统一），避免死锁
- **出库单 outboundTime**：申请类出库单该字段是审批通过时刻（取货 7 天超时的起算点），不是申请时间
- 定时任务线程无 SecurityContext，调用业务方法前需注入系统账户（见 `OutboundTimeoutTask`）
- **库存数量必须为正**：`lockInventory` / `unlockInventory` / `commitInventory` 开头有 `requirePositive`，DTO 也有 `@DecimalMin("0.01")`。负数会让"锁定"变"释放"、"出库"变"入库"（2026-09-29 发现：直接出库传 -100 能凭空加 100 库存）。新增任何改库存的入口都要保留这两层
- **权限 = 角色 + 归属**：`@PreAuthorize` 只管角色，"是不是我管的仓库/部门"必须在服务层再查。统一用 `security/DataScopeGuard`（`assertCanOperateWarehouse` / `assertCanViewApply`）。**新增对外接口时必须调用它**；内部调用（如取消申请时连带取消出库单）不要加，否则员工取消不了自己的申请。定时任务注入的 system 账户带 ROLE_ADMIN，会被放行
- **多行加锁按 materialId 排序**：审批、直接出库、确认取货、取消出库逐条 `FOR UPDATE` 锁库存，已统一排序（评审 #5）。**新增多行改库存的逻辑也要先排序**，否则明细顺序相反的并发请求会死锁。入库（`createInbound`）和取消申请的兜底解锁同样已排序
- **角色编码大小写不统一**：`sql/init_data.sql` 是小写（`dept_admin`），测试种子是大写（`DEPT_ADMIN`）。代码里比较角色一律 `equalsIgnoreCase` 或先 `toUpperCase(Locale.ROOT)`，**不要写 `"DEPT_ADMIN".equals(...)`**。测试全绿不代表生产正确——2026-09-29 就因此引入过一次回归，被 Codex 异源审查发现
- **数量最多两位小数**：库存列是 `DECIMAL(10,2)`。`requirePositive` 会拒绝多余小数位，DTO 有 `@Digits(integer = 8, fraction = 2)`。多余精度会让锁定/解锁对不上，留下永远释放不掉的锁定量

## 评审记录
- `docs/reviews/`：每次链路级审查的报告（发现、触发场景、状态）；`docs/reviews/codex/` 是异源审查（Codex）的原始输出。改完关键链路后用 `python H:\ai-dotfiles\bin\xreview.py` 再跑一次异源审查

## Key Directories

- `backend/` — Spring Boot 后端（130+ Java 文件，测试 37 类 256 个）
- `frontend-pc/src/views/` — PC 页面（apply/approval/inbound/outbound/inventory/statistics/...）
- `miniprogram/pages/` — 小程序页面
- `sql/` — 生产库脚本（`schema.sql` 为准）
- `docs/` — 需求分析、API 文档、部署手册等（历史过程报告较多，以 `需求分析.md`、`API_REFERENCE.md`、`数据库设计文档.md` 为主要参考）

## Pending / External Blockers

- 微信模板消息需真实 AppID/AppSecret（`WechatConsumer` 中 TODO）
- 小程序生产域名为占位符，需替换（`miniprogram/utils/request.js`）
- 生产部署需服务器环境（`docker-compose.prod.yml` 已就绪）
