### 结论

**修掉高严重度问题后可以合并。**发现 4 项：第 3 项是新增回归；第 1、2、4 项是本次修复仍遗漏的既有问题。

已完整阅读四个指定文件并核对调用链、测试。现有 Surefire 报告为 **252 个测试通过**；当前环境只读，未重新运行 Maven 测试。数量精度问题已用内存 H2 按代码计算顺序复现。

### 发现

| # | 级别 | 位置 | 问题及代码证据 | 触发场景（输入/状态 → 错误结果） | 建议 |
|---|---|---|---|---|---|
| 1 | **高** | [OutboundDTO.java:61](H:/java/CT-Tibet-WMS/backend/src/main/java/com/ct/wms/dto/OutboundDTO.java:61)、[InventoryServiceImpl.java:487](H:/java/CT-Tibet-WMS/backend/src/main/java/com/ct/wms/service/impl/InventoryServiceImpl.java:487) | **数量校验未限制精度，直接出库能产生残留锁定和账实差异。** DTO 只有 `@DecimalMin("0.01")`；提交仍用原始数量计算 `inventory.getQuantity().subtract(quantity)`，而[库存列](H:/java/CT-Tibet-WMS/sql/schema.sql:326)是 `DECIMAL(10,2)`。 | 初始总量/锁定/可用为 `100/0/100`，直接出库传 `0.015`。锁定落库成为 `0.02`；提交后变成 **`99.99/0.01/99.98`**，出库明细却记录 `0.02`。已完成单留下无对应待取货单的锁定量，总库存也只扣了 `0.01`。 | DTO 增加小数位约束；服务层统一拒绝不能精确表示为两位小数的数量。所有计算、明细及流水使用同一精度。 |
| 2 | **高** | [ApplyServiceImpl.java:76](H:/java/CT-Tibet-WMS/backend/src/main/java/com/ct/wms/service/impl/ApplyServiceImpl.java:76)、[OutboundServiceImpl.java:71](H:/java/CT-Tibet-WMS/backend/src/main/java/com/ct/wms/service/impl/OutboundServiceImpl.java:71) | **详情被保护，列表仍泄露跨部门数据。** 申请列表只处理 `"DEPT_ADMIN".equals(...)`；出库列表的范围完全来自 `if (warehouseId != null) wrapper.eq(...)`，随后直接 `selectPage`。 | 仓管员调用 `GET /api/applies`，可读取其他仓库申请，包括持久化的申请人手机号；部门管理员调用 `GET /api/outbounds` 或 `/pending`，可读取其他部门的领用记录。省略筛选条件即可触发，详情返回 403 不影响列表泄露。 | 在分页查询前强制加入当前用户允许访问的部门/仓库条件，与客户端筛选取交集；无权限范围应返回空集。 |
| 3 | **高** | [DataScopeGuard.java:83](H:/java/CT-Tibet-WMS/backend/src/main/java/com/ct/wms/security/DataScopeGuard.java:83) | **新增守卫不兼容仓库自带的小写角色数据。** 使用 `DEPT_ADMIN.equals(a.roleCode)`，读取数据库角色后未归一化。 | 按 [init_data.sql:27](H:/java/CT-Tibet-WMS/sql/init_data.sql:27) 初始化后，部门管理员角色是 `dept_admin`。认证层通过 `roleCode.toUpperCase()` 生成权限，因此能通过 Controller；但新守卫拒绝这个未兼任仓管员的管理员，导致其查看本部门申请、直接出库、确认及取消出库均失败。 | 数据库角色读取后统一按 `Locale.ROOT` 转大写；同步处理审批和列表中的角色比较，并提供存量数据迁移。 |
| 4 | **中** | [InboundServiceImpl.java:168](H:/java/CT-Tibet-WMS/backend/src/main/java/com/ct/wms/service/impl/InboundServiceImpl.java:168)、[InventoryServiceImpl.java:78](H:/java/CT-Tibet-WMS/backend/src/main/java/com/ct/wms/service/impl/InventoryServiceImpl.java:78) | **库存加锁排序遗漏入库入口，仍能与审批形成死锁。** 入库执行 `for (... : dto.getDetails())`，逐条调用最终执行 `updateById(inventory)` 的库存增加方法；UPDATE 同样持有行锁。 | 同仓库物资均已有库存：入库按 `[2,1]` 先更新并锁住物资 2；审批按 `[1,2]` 锁住物资 1。随后入库等待 1、审批等待 2，形成死锁，其中一个业务事务被回滚。乐观锁版本条件不能消除这条等待环。 | 入库也按统一的物资顺序操作库存；一并补齐 [cancelApply 的兜底解锁循环](H:/java/CT-Tibet-WMS/backend/src/main/java/com/ct/wms/service/impl/ApplyServiceImpl.java:484)的排序。 |

第 1 项的内存复现结果与 MySQL 对 `DECIMAL` 列采用的舍入规则一致；尚未执行完整 MySQL 服务链路复现。[MySQL 舍入规则](https://dev.mysql.com/doc/refman/8.0/en/precision-math-rounding.html)

### 查过、没发现问题的方面

- 负数、零及 `null` 已被三个库存方法的 `requirePositive` 拦截；出库 DTO 存在级联校验。
- 审批抢占、出库确认和取消的主状态更新均使用旧状态条件，并检查抢占结果；相关库存操作处于同一事务。
- 正常取消已审批申请时，由关联出库单释放库存，成功后不会再次执行申请侧解锁。
- 直接出库第二轮虽按原始明细顺序提交库存，但第一轮已按顺序取得全部库存行锁，本身不会重新形成反向加锁。
- 已检查的查询使用参数化条件；`FOR UPDATE` 为常量。超时取消任务注入的 `ROLE_ADMIN` 能通过新守卫，并在 `finally` 清理上下文。

### 建议补的测试

- **数量精度**：初始库存 `100/0/100`，直接出库传 `0.015`，期望校验失败且库存、单据、流水均不变。现有 [InventoryQuantityGuardTest.java:130](H:/java/CT-Tibet-WMS/backend/src/test/java/com/ct/wms/service/impl/InventoryQuantityGuardTest.java:130)只测试负数、零和 `0.5`，未覆盖超精度数量。
- **列表隔离**：创建两个部门、两个仓库的数据，分别以部门管理员和仓管员查询普通列表、待取货列表及已审批列表，断言记录和总数均不包含越权数据。现有列表测试仅断言非空和 `total >= 0`。
- **角色兼容**：数据库角色分别使用 `dept_admin`、`DEPT_ADMIN`，验证本部门允许、跨部门拒绝。现有 `DataScopeGuardTest` 的种子角色全部大写。
- **跨入口并发**：在 MySQL 上用两个独立事务交错执行逆序入库和审批，验证无死锁且最终库存正确。[LockOrderAndAuditTest.java:60](H:/java/CT-Tibet-WMS/backend/src/test/java/com/ct/wms/service/impl/LockOrderAndAuditTest.java:60)目前只检查单线程流水顺序。
- **确认与取消竞态**：同一待取货单并发确认和取消，只允许一个成功，并检查申请状态及三项库存。现有“完整流程”测试中，[确认出库及最终库存断言仍是注释](H:/java/CT-Tibet-WMS/backend/src/test/java/com/ct/wms/integration/ApplyOutboundFlowTest.java:181)。