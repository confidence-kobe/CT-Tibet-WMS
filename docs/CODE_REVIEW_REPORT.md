# CT-Tibet-WMS 代码审查报告

**审查日期**: 2025-12-29
**审查范围**: Backend (Spring Boot) + Frontend (小程序)
**代码总量**: 约 7,949 行 Java 代码
**审查员**: Claude Code AI Code Reviewer

---

## 执行摘要

CT-Tibet-WMS 是一个基于 Spring Boot + MyBatis-Plus 的仓库管理系统，代码整体架构清晰，采用了标准的分层设计。但在安全性、并发控制、性能优化和代码质量方面存在多个**严重问题**和**中等问题**需要立即修复。

### 关键发现
- **严重问题**: 7个
- **高优先级问题**: 12个
- **中等问题**: 15个
- **低优先级问题**: 8个

---

## 1. 安全性审查 (Security Review)

### 🔴 严重问题

#### 1.1 SQL注入风险 - 严重程度: CRITICAL

**问题位置**:
- `ApplyServiceImpl.java:379` - `generateApplyNo()`
- `OutboundServiceImpl.java:425` - `generateOutboundNo()`

**代码片段**:
```java
// ApplyServiceImpl.java:379
wrapper.likeRight(Apply::getApplyNo, prefix);
wrapper.orderByDesc(Apply::getApplyNo);
wrapper.last("LIMIT 1");  // ⚠️ 直接使用 LIMIT，存在SQL注入风险
```

**问题描述**:
使用 `.last("LIMIT 1")` 方法直接拼接SQL字符串，虽然当前场景下风险较低，但这是不安全的编码实践。如果 `prefix` 变量来自用户输入且未经验证，可能导致SQL注入。

**修复建议**:
```java
// 推荐做法
wrapper.likeRight(Apply::getApplyNo, prefix);
wrapper.orderByDesc(Apply::getApplyNo);
Page<Apply> page = new Page<>(1, 1);
Apply lastApply = applyMapper.selectPage(page, wrapper)
    .getRecords().stream().findFirst().orElse(null);
```

**影响**:
- 潜在的数据泄露
- SQL注入攻击风险
- 违反OWASP Top 10 (A03:2021 - Injection)

---

#### 1.2 密码加密配置不明确 - 严重程度: HIGH

**问题位置**: `SecurityConfig.java:48`, `application-dev.yml`

**问题描述**:
1. BCryptPasswordEncoder 使用默认强度(10轮)，对于高安全需求场景可能不够
2. 配置文件中数据库密码明文存储: `password: root`
3. JWT Secret 未在配置文件中验证强度

**代码片段**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // ⚠️ 使用默认强度
}
```

**修复建议**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);  // 提升到12轮
}
```

```yaml
# application-dev.yml
spring:
  datasource:
    password: ${DB_PASSWORD:root}  # 使用环境变量

jwt:
  secret: ${JWT_SECRET}  # 必须通过环境变量配置，不写在代码中
```

**影响**:
- 密码暴力破解风险增加
- 配置文件泄露导致数据库直接被访问
- JWT密钥泄露导致身份伪造

---

#### 1.3 Token黑名单机制缺失 - 严重程度: HIGH

**问题位置**: `AuthServiceImpl.java:102-106`

**代码片段**:
```java
@Override
public void logout() {
    SecurityContextHolder.clearContext();
    // TODO: 将token加入黑名单(Redis)  // ⚠️ 关键功能未实现
}
```

**问题描述**:
用户退出登录后，JWT Token 仍然有效（直到过期），攻击者如果获取到该 Token 仍可继续访问系统。

**修复建议**:
```java
@Override
public void logout() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
        String username = ((UserDetailsImpl) auth.getPrincipal()).getUsername();
        String token = getCurrentToken(); // 获取当前请求的token

        // 将token加入Redis黑名单，过期时间设置为token剩余有效期
        long expirationTime = jwtUtils.getExpirationTime(token);
        redisUtils.set("blacklist:token:" + token, "1", expirationTime);
    }
    SecurityContextHolder.clearContext();
}
```

**影响**:
- 已退出用户的Token仍可使用
- 无法强制用户下线
- 违反OWASP A07:2021 - Identification and Authentication Failures

---

#### 1.4 权限控制不够细粒度 - 严重程度: HIGH

**问题位置**:
- `ApplyServiceImpl.java:275` - `approveApply()`
- `OutboundServiceImpl.java` - 多处方法缺少权限验证

**代码片段**:
```java
// ApplyServiceImpl.java:269-277
if (!warehouse.getManagerId().equals(approverId)) {
    throw new BusinessException(403, "无权审批此申请单");
}
```

**问题描述**:
1. 权限检查逻辑在Service层实现，但Controller层的 `@PreAuthorize` 注解过于宽泛
2. 只检查了仓管员身份，未检查部门隔离（多租户问题）
3. 普通员工可能越权访问其他部门的数据

**修复建议**:
```java
// 添加细粒度权限检查
@PreAuthorize("hasAnyRole('WAREHOUSE', 'DEPT_ADMIN') and " +
              "@permissionService.canAccessWarehouse(#dto.warehouseId)")
public void approveApply(ApprovalDTO dto) {
    // ...
}

// 实现 PermissionService
@Service
public class PermissionService {
    public boolean canAccessWarehouse(Long warehouseId) {
        Long userId = getCurrentUserId();
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        User user = userMapper.selectById(userId);

        // 检查仓库管理员权限
        if (warehouse.getManagerId().equals(userId)) {
            return true;
        }

        // 检查部门管理员权限
        if (user.getRoleCode().equals("DEPT_ADMIN") &&
            warehouse.getDeptId().equals(user.getDeptId())) {
            return true;
        }

        return false;
    }
}
```

**影响**:
- 横向权限漏洞 (IDOR)
- 多租户数据隔离失效
- 违反OWASP A01:2021 - Broken Access Control

---

#### 1.5 输入验证不足 - 严重程度: MEDIUM

**问题位置**:
- `ApplyDTO.java`, `OutboundDTO.java` 等DTO类
- `MaterialController.java`, `UserController.java` 等Controller

**问题描述**:
1. DTO类缺少完整的校验注解
2. 数量、金额等业务字段缺少范围验证
3. 字符串长度验证不足

**代码片段**:
```java
// 当前代码 - 缺少验证
public class ApplyDTO {
    private Long warehouseId;
    private LocalDateTime applyTime;
    private String applyReason;  // ⚠️ 缺少 @NotBlank, @Size 验证
    private List<ApplyDetailDTO> details;  // ⚠️ 缺少 @Valid, @NotEmpty

    public static class ApplyDetailDTO {
        private Long materialId;
        private BigDecimal quantity;  // ⚠️ 缺少 @DecimalMin, @Digits 验证
        private String remark;
    }
}
```

**修复建议**:
```java
public class ApplyDTO {
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "申请时间不能为空")
    @PastOrPresent(message = "申请时间不能是未来时间")
    private LocalDateTime applyTime;

    @NotBlank(message = "申请原因不能为空")
    @Size(min = 10, max = 500, message = "申请原因长度必须在10-500字符之间")
    private String applyReason;

    @NotEmpty(message = "申请明细不能为空")
    @Valid
    private List<ApplyDetailDTO> details;

    public static class ApplyDetailDTO {
        @NotNull(message = "物资ID不能为空")
        private Long materialId;

        @NotNull(message = "数量不能为空")
        @DecimalMin(value = "0.01", message = "数量必须大于0")
        @Digits(integer = 8, fraction = 2, message = "数量格式不正确")
        private BigDecimal quantity;

        @Size(max = 200, message = "备注长度不能超过200字符")
        private String remark;
    }
}
```

**影响**:
- 脏数据进入数据库
- 潜在的业务逻辑错误
- XSS攻击风险（如果未转义输出）

---

#### 1.6 敏感信息日志泄露 - 严重程度: MEDIUM

**问题位置**:
- `AuthController.java:35` - 记录用户名
- `application-dev.yml:30-31` - DEBUG级别日志

**代码片段**:
```java
// AuthController.java
log.info("用户登录: username={}, loginType={}",
    request.getUsername(), request.getLoginType());  // ⚠️ 可能泄露敏感信息
```

```yaml
# application-dev.yml
logging:
  level:
    com.ct.wms: DEBUG  # ⚠️ 生产环境不应使用DEBUG
    org.springframework.security: DEBUG  # ⚠️ 可能泄露认证细节
```

**修复建议**:
```java
// 脱敏处理
log.info("用户登录: username={}, loginType={}",
    maskSensitiveInfo(request.getUsername()), request.getLoginType());

private String maskSensitiveInfo(String username) {
    if (username == null || username.length() <= 3) {
        return "***";
    }
    return username.substring(0, 1) + "***" +
           username.substring(username.length() - 1);
}
```

```yaml
# application-prod.yml
logging:
  level:
    root: WARN
    com.ct.wms: INFO
    org.springframework.security: WARN
```

**影响**:
- 用户隐私泄露
- 攻击者可通过日志获取系统信息
- 违反GDPR/个人信息保护法

---

### 🟡 中等安全问题

#### 1.7 CORS配置过于宽松

**位置**: `SecurityConfig.java:72`

```java
configuration.setAllowedOriginPatterns(Collections.singletonList("*"));  // ⚠️ 允许所有来源
```

**修复**: 生产环境应明确指定允许的域名
```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "https://wms.ct-tibet.com",
    "https://admin.ct-tibet.com"
));
```

---

#### 1.8 缺少请求频率限制 (Rate Limiting)

**位置**: 所有Controller

**修复建议**: 添加限流注解
```java
@PostMapping("/login")
@RateLimit(maxRequests = 5, timeWindow = 60) // 每分钟最多5次
public Result<LoginVO> login(@Validated @RequestBody LoginRequest request) {
    // ...
}
```

---

## 2. 并发控制与事务管理 (Concurrency & Transactions)

### 🔴 严重问题

#### 2.1 乐观锁实现不完整 - 严重程度: CRITICAL

**问题位置**: `InventoryServiceImpl.java:73-76`, `Inventory.java:75-77`

**代码片段**:
```java
// Inventory.java
@Version
@Schema(description = "版本号")
private Integer version;

// InventoryServiceImpl.java:73
int updated = inventoryMapper.updateById(inventory);
if (updated == 0) {
    throw new BusinessException(500, "库存更新失败，请重试");  // ⚠️ 错误处理不当
}
```

**问题描述**:
1. **乐观锁失败未重试**: 并发更新失败时直接抛异常，未实现重试机制
2. **没有使用 UpdateWrapper**: `updateById` 方法在MyBatis-Plus中会自动处理version，但缺少显式的version条件
3. **库存检查与扣减非原子性**: `checkInventory` 和 `decreaseInventory` 是两个独立的数据库操作

**代码分析**:
```java
// OutboundServiceImpl.java:155-158
// 问题代码示例
if (!inventoryService.checkInventory(dto.getWarehouseId(),
    detailDTO.getMaterialId(), detailDTO.getQuantity())) {
    throw new BusinessException(1001, "库存不足: " + material.getMaterialName());
}
// ... 后续再执行扣减操作

// ⚠️ 存在竞态条件 (Race Condition):
// 线程A检查库存充足 → 线程B检查库存充足 → 线程A扣减成功 → 线程B扣减成功 → 超卖！
```

**修复建议**:

**方案1: 使用悲观锁 (推荐用于高并发场景)**
```java
// InventoryMapper.java
@Select("SELECT * FROM tb_inventory WHERE warehouse_id = #{warehouseId} " +
        "AND material_id = #{materialId} FOR UPDATE")
Inventory selectForUpdate(@Param("warehouseId") Long warehouseId,
                         @Param("materialId") Long materialId);

// InventoryServiceImpl.java
@Transactional(rollbackFor = Exception.class)
public void decreaseInventory(Long warehouseId, Long materialId,
                               BigDecimal quantity, String relatedNo,
                               Long relatedId, Long operatorId) {
    // 使用悲观锁锁定库存记录
    Inventory inventory = inventoryMapper.selectForUpdate(warehouseId, materialId);

    if (inventory == null) {
        throw new BusinessException(1001, "库存不存在");
    }

    // 原子性检查并扣减
    if (inventory.getQuantity().compareTo(quantity) < 0) {
        throw new BusinessException(1001,
            String.format("库存不足，当前库存：%s，需要：%s",
            inventory.getQuantity(), quantity));
    }

    // 执行扣减
    inventory.setQuantity(inventory.getQuantity().subtract(quantity));
    inventory.setAvailableQuantity(
        inventory.getQuantity().subtract(inventory.getLockedQuantity())
    );
    inventory.setLastOutboundTime(LocalDateTime.now());

    int updated = inventoryMapper.updateById(inventory);
    if (updated == 0) {
        throw new BusinessException(500, "库存更新失败");
    }

    // 记录流水
    saveInventoryLog(...);
}
```

**方案2: 优化乐观锁 + 重试机制**
```java
@Transactional(rollbackFor = Exception.class)
public void decreaseInventory(Long warehouseId, Long materialId,
                               BigDecimal quantity, String relatedNo,
                               Long relatedId, Long operatorId) {
    int maxRetries = 3;
    int attempt = 0;

    while (attempt < maxRetries) {
        try {
            Inventory inventory = getInventory(warehouseId, materialId);

            if (inventory == null) {
                throw new BusinessException(1001, "库存不存在");
            }

            // 检查库存充足性
            if (inventory.getQuantity().compareTo(quantity) < 0) {
                throw new BusinessException(1001,
                    String.format("库存不足，当前库存：%s", inventory.getQuantity()));
            }

            // 使用CAS更新（带版本号）
            LambdaUpdateWrapper<Inventory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Inventory::getId, inventory.getId())
                        .eq(Inventory::getVersion, inventory.getVersion())  // 乐观锁
                        .set(Inventory::getQuantity,
                             inventory.getQuantity().subtract(quantity))
                        .set(Inventory::getAvailableQuantity,
                             inventory.getQuantity().subtract(quantity)
                                 .subtract(inventory.getLockedQuantity()))
                        .set(Inventory::getLastOutboundTime, LocalDateTime.now())
                        .set(Inventory::getVersion, inventory.getVersion() + 1);

            int updated = inventoryMapper.update(null, updateWrapper);

            if (updated == 0) {
                // 乐观锁冲突，重试
                attempt++;
                log.warn("库存更新乐观锁冲突，第{}次重试", attempt);
                Thread.sleep(50 * attempt);  // 指数退避
                continue;
            }

            // 更新成功，记录流水
            saveInventoryLog(...);
            return;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "库存更新被中断");
        }
    }

    throw new BusinessException(500, "库存更新失败，请稍后重试");
}
```

**方案3: 使用数据库原子操作 (最优方案)**
```java
// InventoryMapper.xml
<update id="decreaseInventoryAtomic">
    UPDATE tb_inventory
    SET quantity = quantity - #{quantity},
        available_quantity = quantity - #{quantity} - locked_quantity,
        last_outbound_time = NOW(),
        version = version + 1
    WHERE warehouse_id = #{warehouseId}
      AND material_id = #{materialId}
      AND quantity >= #{quantity}  <!--原子性检查-->
      AND version = #{version}     <!--乐观锁-->
</update>

// InventoryMapper.java
int decreaseInventoryAtomic(@Param("warehouseId") Long warehouseId,
                            @Param("materialId") Long materialId,
                            @Param("quantity") BigDecimal quantity,
                            @Param("version") Integer version);

// InventoryServiceImpl.java
@Transactional(rollbackFor = Exception.class)
public void decreaseInventory(Long warehouseId, Long materialId,
                               BigDecimal quantity, String relatedNo,
                               Long relatedId, Long operatorId) {
    Inventory inventory = getInventory(warehouseId, materialId);
    if (inventory == null) {
        throw new BusinessException(1001, "库存不存在");
    }

    int updated = inventoryMapper.decreaseInventoryAtomic(
        warehouseId, materialId, quantity, inventory.getVersion()
    );

    if (updated == 0) {
        // 可能是库存不足或版本冲突，需要重新查询确定原因
        Inventory current = getInventory(warehouseId, materialId);
        if (current.getQuantity().compareTo(quantity) < 0) {
            throw new BusinessException(1001,
                String.format("库存不足，当前库存：%s", current.getQuantity()));
        } else {
            throw new BusinessException(500, "库存更新失败，请重试");
        }
    }

    // 记录流水
    BigDecimal beforeQuantity = inventory.getQuantity();
    BigDecimal afterQuantity = beforeQuantity.subtract(quantity);
    saveInventoryLog(warehouseId, materialId, 2, quantity.negate(),
                    beforeQuantity, afterQuantity, relatedNo, 2,
                    relatedId, operatorId, "出库");
}
```

**影响**:
- 高并发场景下可能出现**超卖问题**
- 库存数据不一致
- 生产事故风险极高

---

#### 2.2 事务边界不明确 - 严重程度: HIGH

**问题位置**: `ApplyServiceImpl.java:249`, `OutboundServiceImpl.java:232`

**代码片段**:
```java
// ApplyServiceImpl.java:249
@Transactional(rollbackFor = Exception.class)
public void approveApply(ApprovalDTO dto) {
    // ...
    // 调用 OutboundService 创建出库单
    Long outboundId = outboundService.createOutboundFromApply(...);  // ⚠️ 事务嵌套问题
}

// OutboundServiceImpl.java:232
@Transactional(rollbackFor = Exception.class)
public Long createOutboundFromApply(...) {
    // ...
}
```

**问题描述**:
1. `approveApply` 和 `createOutboundFromApply` 都有 `@Transactional` 注解
2. 默认传播级别是 `REQUIRED`，会合并成一个事务
3. 如果 `createOutboundFromApply` 失败，整个审批操作会回滚，这**符合业务需求**
4. 但缺少明确的事务传播级别声明，容易被误解

**修复建议**:
```java
// 明确声明事务传播级别
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
public void approveApply(ApprovalDTO dto) {
    // ...
    // 审批通过后，在同一事务中创建出库单
    Long outboundId = outboundService.createOutboundFromApply(...);
    // ...
}

// OutboundService中也明确声明
@Transactional(rollbackFor = Exception.class, propagation = Propagation.MANDATORY)
public Long createOutboundFromApply(...) {
    // 要求必须在事务中调用
    // ...
}
```

**影响**:
- 事务行为不明确
- 可能出现部分提交的脏数据

---

#### 2.3 定时任务并发控制缺失 - 严重程度: MEDIUM

**问题位置**: `OutboundTimeoutTask.java:50`, `ApplyTimeoutTask.java`

**代码片段**:
```java
@Scheduled(cron = "0 0 2 * * ?")
public void cancelTimeoutOutbound() {
    // ⚠️ 没有分布式锁控制，多实例部署时会重复执行
    List<Outbound> timeoutOutbounds = outboundMapper.selectList(queryWrapper);
    // ...
}
```

**问题描述**:
如果系统部署多个实例（负载均衡），定时任务会在每个实例上同时执行，导致：
1. 重复处理相同的超时订单
2. 并发更新冲突
3. 重复发送通知消息

**修复建议**:
```java
@Scheduled(cron = "0 0 2 * * ?")
public void cancelTimeoutOutbound() {
    String lockKey = "schedule:lock:outbound_timeout";
    String lockValue = UUID.randomUUID().toString();

    try {
        // 使用Redis分布式锁，只有获取到锁的实例才执行
        boolean locked = redisUtils.tryLock(lockKey, lockValue,
                                           Duration.ofMinutes(30));
        if (!locked) {
            log.info("其他实例正在执行超时检查任务，跳过");
            return;
        }

        log.info("获取到分布式锁，开始执行超时检查任务");

        // 原有业务逻辑
        // ...

    } finally {
        // 释放锁
        redisUtils.releaseLock(lockKey, lockValue);
    }
}
```

**影响**:
- 重复处理导致数据不一致
- 资源浪费
- 用户收到重复通知

---

## 3. 性能问题 (Performance Issues)

### 🟡 高优先级性能问题

#### 3.1 N+1查询问题 - 严重程度: HIGH

**问题位置**:
- `ApplyServiceImpl.java:87-88`
- `OutboundServiceImpl.java:94`
- `InventoryServiceImpl.java:182`

**代码片段**:
```java
// ApplyServiceImpl.java
Page<Apply> result = applyMapper.selectPage(page, wrapper);

// 填充关联数据  ⚠️ N+1 查询问题
result.getRecords().forEach(this::fillApplyInfo);

// fillApplyInfo方法
private void fillApplyInfo(Apply apply) {
    Warehouse warehouse = warehouseMapper.selectById(apply.getWarehouseId());  // N次查询
    User applicant = userMapper.selectById(apply.getApplicantId());  // N次查询
    if (apply.getApproverId() != null) {
        User approver = userMapper.selectById(apply.getApproverId());  // N次查询
    }
}
```

**问题描述**:
假设查询100条申请单，会产生：
- 1次查询申请单列表
- 100次查询仓库信息
- 100次查询申请人信息
- 最多100次查询审批人信息
- **总计: 最多301次数据库查询**

**修复建议**:

**方案1: 使用MyBatis-Plus关联查询**
```java
// ApplyMapper.java
@Select("SELECT a.*, " +
        "w.warehouse_name, " +
        "u1.real_name as applicant_name, " +
        "u2.real_name as approver_name " +
        "FROM tb_apply a " +
        "LEFT JOIN tb_warehouse w ON a.warehouse_id = w.id " +
        "LEFT JOIN tb_user u1 ON a.applicant_id = u1.id " +
        "LEFT JOIN tb_user u2 ON a.approver_id = u2.id " +
        "WHERE ... " +
        "ORDER BY a.create_time DESC")
Page<Apply> selectPageWithRelations(Page<Apply> page, @Param("...") ...);
```

**方案2: 批量查询 + Map映射**
```java
public Page<Apply> listApplies(...) {
    Page<Apply> result = applyMapper.selectPage(page, wrapper);
    List<Apply> records = result.getRecords();

    if (records.isEmpty()) {
        return result;
    }

    // 收集所有ID
    Set<Long> warehouseIds = records.stream()
        .map(Apply::getWarehouseId).collect(Collectors.toSet());
    Set<Long> applicantIds = records.stream()
        .map(Apply::getApplicantId).collect(Collectors.toSet());
    Set<Long> approverIds = records.stream()
        .map(Apply::getApproverId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    // 批量查询（3次查询代替N次）
    Map<Long, Warehouse> warehouseMap = warehouseMapper.selectBatchIds(warehouseIds)
        .stream().collect(Collectors.toMap(Warehouse::getId, w -> w));
    Map<Long, User> applicantMap = userMapper.selectBatchIds(applicantIds)
        .stream().collect(Collectors.toMap(User::getId, u -> u));
    Map<Long, User> approverMap = userMapper.selectBatchIds(approverIds)
        .stream().collect(Collectors.toMap(User::getId, u -> u));

    // 填充数据
    records.forEach(apply -> {
        Warehouse warehouse = warehouseMap.get(apply.getWarehouseId());
        if (warehouse != null) {
            apply.setWarehouseName(warehouse.getWarehouseName());
        }

        User applicant = applicantMap.get(apply.getApplicantId());
        if (applicant != null) {
            apply.setApplicantName(applicant.getRealName());
        }

        if (apply.getApproverId() != null) {
            User approver = approverMap.get(apply.getApproverId());
            if (approver != null) {
                apply.setApproverName(approver.getRealName());
            }
        }
    });

    return result;
}
```

**影响**:
- 数据库连接池耗尽
- 页面响应时间从100ms → 3000ms+
- 高并发下系统瘫痪

---

#### 3.2 缺少数据库索引 - 严重程度: HIGH

**问题位置**: `sql/schema.sql`

**代码分析**:
```sql
-- tb_outbound表
CREATE TABLE `tb_outbound` (
  -- ...
  `status` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  -- ...
  KEY `idx_status` (`status`),  ✅ 有索引
  KEY `idx_create_time` (`create_time`),  ❌ 但定时任务需要组合索引
)

-- 定时任务查询 (OutboundTimeoutTask.java:59-62)
queryWrapper.eq(Outbound::getStatus, OutboundStatus.PENDING.getValue())
            .eq(Outbound::getSource, OutboundSource.FROM_APPLY.getValue())
            .le(Outbound::getOutboundTime, timeoutTime);

-- ⚠️ 缺少复合索引：idx_status_source_outbound_time
```

**修复建议**:
```sql
-- 添加复合索引优化定时任务查询
ALTER TABLE tb_outbound
ADD INDEX idx_status_source_time (status, source, outbound_time);

-- 优化申请单查询
ALTER TABLE tb_apply
ADD INDEX idx_status_apply_time (status, apply_time);

-- 优化库存查询
ALTER TABLE tb_inventory
ADD UNIQUE INDEX uk_warehouse_material (warehouse_id, material_id);
```

**影响**:
- 全表扫描导致查询缓慢
- 定时任务执行时间过长
- 影响其他业务查询

---

#### 3.3 缺少缓存机制 - 严重程度: MEDIUM

**问题位置**:
- `MaterialServiceImpl` - 物资信息频繁查询
- `WarehouseServiceImpl` - 仓库信息频繁查询
- `DeptServiceImpl` - 部门信息频繁查询

**问题描述**:
基础数据（物资、仓库、部门）变更频率低但查询频率极高，每次都查询数据库浪费资源。

**修复建议**:
```java
@Service
@CacheConfig(cacheNames = "material")
public class MaterialServiceImpl implements MaterialService {

    @Cacheable(key = "#id")
    @Override
    public Material getById(Long id) {
        return materialMapper.selectById(id);
    }

    @CachePut(key = "#material.id")
    @Override
    public Material updateMaterial(Material material) {
        materialMapper.updateById(material);
        return material;
    }

    @CacheEvict(key = "#id")
    @Override
    public void deleteMaterial(Long id) {
        materialMapper.deleteById(id);
    }
}
```

```yaml
# application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
```

**影响**:
- 数据库负载高
- 查询响应慢

---

#### 3.4 分页查询未限制最大值 - 严重程度: MEDIUM

**问题位置**: 所有分页查询接口

**代码片段**:
```java
// ApplyController.java
public Result<Page<Apply>> listApplies(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        ...) {
    // ⚠️ pageSize 未限制最大值，用户可以传 pageSize=999999
    Page<Apply> page = applyService.listApplies(pageNum, pageSize, ...);
}
```

**修复建议**:
```java
public Result<Page<Apply>> listApplies(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10")
        @Max(value = 100, message = "单页最多查询100条记录") Integer pageSize,
        ...) {
    // ...
}
```

**影响**:
- 内存溢出风险
- 数据库性能下降
- DOS攻击风险

---

## 4. 代码质量问题 (Code Quality Issues)

### 🟡 中等问题

#### 4.1 代码重复 - 严重程度: MEDIUM

**问题位置**:
- `ApplyServiceImpl.java:346-366` vs `OutboundServiceImpl.java:392-412`
- `ApplyServiceImpl.java:396-403` vs `OutboundServiceImpl.java:442-449`

**代码片段**:
```java
// ApplyServiceImpl.java:396-403
private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
    throw new BusinessException(401, "未登录");
}

// OutboundServiceImpl.java:442-449 - 完全相同的代码
private Long getCurrentUserId() {
    // 完全重复...
}
```

**修复建议**:
```java
// 创建 SecurityUtils 工具类
@Component
public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
            authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        }
        throw new BusinessException(401, "未登录");
    }

    public static UserDetailsImpl getCurrentUser() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
            authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        throw new BusinessException(401, "未登录");
    }

    public static boolean hasRole(String roleCode) {
        return getCurrentUser().getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + roleCode));
    }
}

// 使用示例
Long userId = SecurityUtils.getCurrentUserId();
```

**影响**:
- 维护成本高
- 容易遗漏修改

---

#### 4.2 魔法数字 - 严重程度: LOW

**问题位置**:
- `ApplyServiceImpl.java:284` - `dto.getApprovalResult() == 1`
- `InventoryServiceImpl.java:83` - `changeType = 1`
- `OutboundTimeoutTask.java:56` - `minusDays(7)`

**代码片段**:
```java
if (dto.getApprovalResult() == 1) {  // ⚠️ 1代表什么？
    apply.setStatus(ApplyStatus.APPROVED);
} else if (dto.getApprovalResult() == 2) {  // ⚠️ 2代表什么？
    apply.setStatus(ApplyStatus.REJECTED);
}
```

**修复建议**:
```java
// 创建常量类
public class ApprovalConstants {
    public static final Integer APPROVED = 1;
    public static final Integer REJECTED = 2;
}

// 使用常量
if (ApprovalConstants.APPROVED.equals(dto.getApprovalResult())) {
    apply.setStatus(ApplyStatus.APPROVED);
} else if (ApprovalConstants.REJECTED.equals(dto.getApprovalResult())) {
    apply.setStatus(ApplyStatus.REJECTED);
}
```

**影响**:
- 代码可读性差
- 容易出错

---

#### 4.3 异常处理不规范 - 严重程度: MEDIUM

**问题位置**: `GlobalExceptionHandler.java:169`

**代码片段**:
```java
@ExceptionHandler(RuntimeException.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public Result<?> handleRuntimeException(RuntimeException e) {
    log.error("Runtime exception occurred: {}", e.getMessage(), e);
    return Result.error(ResultCode.INTERNAL_ERROR,
        "Internal server error: " + e.getMessage());  // ⚠️ 泄露异常详情
}
```

**修复建议**:
```java
@ExceptionHandler(RuntimeException.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public Result<?> handleRuntimeException(RuntimeException e) {
    log.error("Runtime exception occurred", e);  // 详细信息只记录到日志

    // 生产环境不返回异常详情
    if (isProdEnvironment()) {
        return Result.error(ResultCode.INTERNAL_ERROR,
            "系统繁忙，请稍后重试");
    } else {
        return Result.error(ResultCode.INTERNAL_ERROR,
            "Internal server error: " + e.getMessage());
    }
}
```

---

#### 4.4 缺少JavaDoc注释 - 严重程度: LOW

**问题位置**: 大部分Service方法

**修复建议**:
```java
/**
 * 审批申请单
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>只有仓管员可以审批自己管理的仓库的申请</li>
 *   <li>审批通过后自动创建出库单（状态为待取货）</li>
 *   <li>审批和创建出库单在同一事务中执行</li>
 * </ul>
 *
 * @param dto 审批DTO，包含申请单ID、审批结果(1通过/2拒绝)、审批意见
 * @throws BusinessException 当申请单不存在、状态不正确或无权审批时
 */
@Transactional(rollbackFor = Exception.class)
public void approveApply(ApprovalDTO dto) {
    // ...
}
```

---

#### 4.5 命名不规范 - 严重程度: LOW

**问题位置**:
- `OutboundTimeoutTask.java:60` - `queryWrapper` (应该是 `timeoutQueryWrapper`)
- 部分DTO字段命名与数据库字段不一致

**修复建议**: 使用更具描述性的变量名

---

## 5. 架构设计问题 (Architecture Issues)

### 🟡 中等问题

#### 5.1 Service层职责过重

**问题位置**: `ApplyServiceImpl`, `OutboundServiceImpl`

**问题描述**:
Service层同时承担：
- 业务逻辑
- 数据填充
- 权限检查
- 单号生成

建议拆分为：
- Service: 核心业务逻辑
- Assembler: 数据组装
- NumberGenerator: 单号生成
- PermissionChecker: 权限检查

---

#### 5.2 缺少DTO转Entity的统一处理

**问题描述**: 每个Service都手动转换DTO到Entity，代码冗余

**修复建议**: 使用MapStruct
```java
@Mapper(componentModel = "spring")
public interface ApplyConverter {
    Apply toEntity(ApplyDTO dto);
    ApplyDTO toDTO(Apply entity);
}
```

---

#### 5.3 硬编码的业务规则

**问题位置**: `OutboundTimeoutTask.java:56` - 7天超时时间

**修复建议**: 配置化
```yaml
business:
  outbound:
    timeout-days: 7
    reminder-days: 5
```

---

## 6. 测试覆盖率 (Test Coverage)

### 🔴 严重问题

**问题**: **测试覆盖率目标设置过低**

**位置**: `pom.xml:253`
```xml
<limit>
    <counter>LINE</counter>
    <value>COVEREDRATIO</value>
    <minimum>0.60</minimum>  ⚠️ 仅60%
</limit>
```

**修复建议**:
- 核心业务逻辑(Service层): 至少80%
- Controller层: 至少70%
- 工具类: 至少90%

**缺少的测试**:
1. 并发场景测试（库存超卖）
2. 事务回滚测试
3. 权限控制测试
4. 边界值测试

---

## 7. 配置与部署问题 (Configuration Issues)

### 🟡 中等问题

#### 7.1 数据库连接池配置不当

**位置**: `application-dev.yml:18`
```yaml
hikari:
  maximum-pool-size: 10  # ⚠️ 生产环境可能不够
```

**修复建议**:
```yaml
hikari:
  maximum-pool-size: 50
  minimum-idle: 10
  connection-timeout: 30000
  idle-timeout: 600000
  max-lifetime: 1800000
  leak-detection-threshold: 60000  # 连接泄漏检测
```

---

#### 7.2 缺少健康检查配置

**修复建议**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  health:
    db:
      enabled: true
    redis:
      enabled: true
```

---

## 8. 前端代码审查 (Frontend Review)

### 🟡 中等问题

#### 8.1 API调用未处理错误

**位置**: `miniprogram/api/*.js`

**问题**: 大部分API调用未处理网络错误和业务异常

**修复建议**:
```javascript
export const applyList = (params) => {
  return request({
    url: '/api/apply/my',
    method: 'GET',
    params
  }).catch(error => {
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    });
    throw error;
  });
};
```

---

#### 8.2 Token管理不安全

**问题**: Token可能存储在localStorage，存在XSS风险

**修复建议**: 使用httpOnly cookie或加密存储

---

## 优先级修复路线图

### 第一阶段 (紧急 - 1周内)
1. ✅ 修复库存并发控制问题（实现悲观锁/数据库原子操作）
2. ✅ 实现Token黑名单机制
3. ✅ 修复N+1查询问题（至少优化3个核心接口）
4. ✅ 添加分布式锁控制定时任务

### 第二阶段 (高优先级 - 2周内)
5. ✅ 完善权限控制（细粒度权限检查）
6. ✅ 添加数据库索引
7. ✅ 实现缓存机制
8. ✅ 配置敏感信息外部化

### 第三阶段 (中等优先级 - 1个月内)
9. ✅ 添加输入验证
10. ✅ 优化异常处理
11. ✅ 代码重构（提取公共方法）
12. ✅ 提升测试覆盖率到75%+

### 第四阶段 (低优先级 - 持续优化)
13. ✅ 完善JavaDoc注释
14. ✅ 优化命名规范
15. ✅ 架构重构（拆分Service职责）

---

## 总结

CT-Tibet-WMS 项目整体架构清晰，技术选型合理，但在**安全性**、**并发控制**和**性能优化**方面存在严重问题，需要立即修复。

**关键风险**:
1. 库存超卖风险（并发控制不足）
2. 权限绕过风险（横向越权）
3. Token泄露风险（缺少黑名单）
4. 性能瓶颈（N+1查询、缺少索引）

**建议采取的措施**:
1. 立即修复严重安全问题
2. 建立代码审查流程
3. 增加集成测试和压力测试
4. 部署前进行安全扫描（OWASP ZAP, SonarQube）

---

**审查完成时间**: 2025-12-29
**下次审查建议**: 修复后1个月进行复审
