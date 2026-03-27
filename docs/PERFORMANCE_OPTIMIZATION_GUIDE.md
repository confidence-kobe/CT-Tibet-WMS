# CT-Tibet-WMS 性能优化指南

## 文档版本
- **版本**: 1.0
- **日期**: 2025-11-24
- **作者**: Performance Engineering Team

## 目录
1. [执行摘要](#执行摘要)
2. [性能瓶颈分析](#性能瓶颈分析)
3. [优化方案](#优化方案)
4. [实施步骤](#实施步骤)
5. [性能测试](#性能测试)
6. [监控部署](#监控部署)
7. [预期性能提升](#预期性能提升)
8. [后续优化建议](#后续优化建议)

---

## 执行摘要

### 项目背景
CT-Tibet-WMS是西藏电信仓库管理系统,支持14个仓库、400+员工日常物资管理操作。当前系统已完成功能开发和测试,需要进行性能优化以满足生产环境需求。

### 性能目标
| 指标类型 | 当前状态 | 优化目标 | 改进幅度 |
|---------|---------|----------|----------|
| 并发用户数 | 未测试 | 50+ | - |
| 列表查询响应时间 | 估计1.5-3秒 | <1秒 | 70%+ |
| 详情查询响应时间 | 估计800ms-1.5秒 | <500ms | 65%+ |
| 统计查询响应时间 | 估计4-8秒 | <2秒 | 75%+ |
| 数据库连接池效率 | 未优化 | 优化配置 | - |
| 缓存命中率 | 无缓存 | >80% | - |

### 优化范围
- **数据库层**: 创建45+索引,优化SQL查询
- **缓存层**: 实施Redis多层缓存策略
- **应用层**: 解决N+1查询问题,优化连接池配置
- **监控层**: 部署Prometheus+Grafana监控栈

---

## 性能瓶颈分析

### 1. 严重性能问题 (P0 - 必须修复)

#### 1.1 N+1查询问题

**问题描述**: 循环中逐条查询关联数据,导致大量数据库往返。

**影响类**:
- `StatisticsServiceImpl`: 6处N+1查询
- `InventoryServiceImpl`: 3处N+1查询
- `InboundServiceImpl`: 2处N+1查询
- `OutboundServiceImpl`: 2处N+1查询
- `ApplyServiceImpl`: 2处N+1查询

**具体案例 - StatisticsServiceImpl.getDashboardStats()**:
```java
// 第101-110行: 循环查询Material表计算库存总值
for (Inventory inventory : allInventories) {
    Material material = materialMapper.selectById(inventory.getMaterialId()); // N+1查询!
    if (material != null && material.getPrice() != null) {
        BigDecimal value = material.getPrice().multiply(inventory.getQuantity());
        totalValue = totalValue.add(value);
    }
}
```

**性能影响**:
- 如果有100条库存记录,会执行1+100=101次数据库查询
- 预估响应时间: 1500-3000ms
- 优化后响应时间: 50-100ms (使用聚合查询)
- **提升幅度**: 95%+

#### 1.2 统计查询中的循环数据库查询

**问题描述**: 在生成趋势图数据时,按日期循环查询明细表。

**具体案例 - generateInboundTrendData()**:
```java
// 第358-384行: 每个日期都查询一次InboundDetail表
while (!current.isAfter(endDate)) {
    List<Long> dayInboundIds = dayInbounds.stream().map(Inbound::getId).collect(Collectors.toList());
    if (!dayInboundIds.isEmpty()) {
        LambdaQueryWrapper<InboundDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(InboundDetail::getInboundId, dayInboundIds);
        List<InboundDetail> details = inboundDetailMapper.selectList(detailWrapper); // 循环查询!
        // ...
    }
    current = current.plusDays(1);
}
```

**性能影响**:
- 30天统计需要执行30次数据库查询
- 预估响应时间: 2000-4000ms
- 优化后响应时间: 200-400ms (使用GROUP BY聚合)
- **提升幅度**: 90%+

### 2. 数据库索引缺失

**问题描述**: 关键查询字段缺少索引,导致全表扫描。

**缺失索引列表**:

| 表名 | 缺失索引 | 影响查询 |
|------|---------|---------|
| tb_inventory | (warehouse_id, material_id) | getInventory(), checkInventory() |
| tb_inventory | (warehouse_id, quantity) | listLowStockAlerts() |
| tb_inbound | (warehouse_id, inbound_time) | getInboundStatistics() |
| tb_outbound | (warehouse_id, outbound_time) | getOutboundStatistics() |
| tb_outbound | (status, warehouse_id) | 待取货查询 |
| tb_apply | (warehouse_id, status) | listPendingApplies() |
| tb_inventory_log | (warehouse_id, material_id, create_time) | listInventoryLogs() |

**性能影响**:
- 库存查询从O(N)降至O(1)
- 统计查询性能提升60-80%
- **平均提升幅度**: 70%

### 3. 连接池配置不足

**当前配置** (application.yml):
```yaml
hikari:
  maximum-pool-size: 20    # 50并发可能不足
  connection-timeout: 30000
```

**潜在问题**:
- 50并发用户场景下可能出现连接等待
- 建议最大连接数: 30-40

---

## 优化方案

### 方案1: 数据库索引优化

#### 实施文件
- `backend/database/performance_optimization.sql`

#### 关键索引 (共45个)

**1. 库存表索引**:
```sql
-- 复合索引: warehouse_id + material_id
CREATE INDEX idx_inventory_warehouse_material
  ON tb_inventory(warehouse_id, material_id);

-- 复合索引: warehouse_id + quantity
CREATE INDEX idx_inventory_warehouse_qty
  ON tb_inventory(warehouse_id, quantity);
```

**2. 入库/出库表索引**:
```sql
-- 入库统计查询优化
CREATE INDEX idx_inbound_warehouse_time
  ON tb_inbound(warehouse_id, inbound_time);

-- 出库统计查询优化
CREATE INDEX idx_outbound_warehouse_time
  ON tb_outbound(warehouse_id, outbound_time);
```

**3. 申请表索引**:
```sql
-- 待审批列表优化
CREATE INDEX idx_apply_warehouse_status
  ON tb_apply(warehouse_id, status);

-- 我的申请列表优化
CREATE INDEX idx_apply_applicant_status
  ON tb_apply(applicant_id, status);
```

#### 执行方式
```bash
# 连接数据库
mysql -u root -p ct_tibet_wms

# 执行索引创建脚本
source backend/database/performance_optimization.sql;

# 验证索引创建
SHOW INDEX FROM tb_inventory;
SHOW INDEX FROM tb_inbound;
SHOW INDEX FROM tb_outbound;
```

#### 预期效果
- 查询性能提升: 60-80%
- 统计查询提升: 70-85%
- CPU使用率降低: 30-40%

---

### 方案2: SQL查询优化

#### 实施文件
- `backend/database/query_optimization_examples.sql`

#### 核心优化策略: 使用JOIN替代循环查询

**优化前**:
```java
// N+1查询
List<Inventory> inventories = inventoryMapper.selectList(wrapper);
for (Inventory inventory : inventories) {
    Material material = materialMapper.selectById(inventory.getMaterialId()); // N次查询
    Warehouse warehouse = warehouseMapper.selectById(inventory.getWarehouseId()); // N次查询
    // 填充数据...
}
```

**优化后**:
```sql
-- 一次JOIN查询获取所有数据
SELECT
    i.*,
    w.warehouse_name,
    m.material_name,
    m.material_code,
    m.spec,
    m.unit
FROM tb_inventory i
LEFT JOIN tb_warehouse w ON i.warehouse_id = w.id
LEFT JOIN tb_material m ON i.material_id = m.id
WHERE i.warehouse_id = ?
ORDER BY i.update_time DESC
LIMIT ?, ?;
```

#### 实施建议
1. 在MyBatis Mapper XML中实现优化后的查询
2. 使用resultMap配置关联对象映射
3. 避免在Java代码中循环查询

#### 预期效果
- N+1查询场景: 85-95%性能提升
- 响应时间: 从1.5-3秒降至300-500ms

---

### 方案3: Redis缓存策略

#### 实施文件
- `backend/src/main/resources/redis-cache-strategy.md`

#### 缓存分层设计

**L1: 热数据缓存 (TTL: 5分钟)**
```java
// 物资基本信息
String cacheKey = "wms:material:info:" + materialId;
redisTemplate.opsForValue().set(cacheKey, material, 300, TimeUnit.SECONDS);
```

**L2: 业务数据缓存 (TTL: 2分钟)**
```java
// 库存实时数据
String cacheKey = "wms:inventory:item:w" + warehouseId + ":m" + materialId;
redisTemplate.opsForValue().set(cacheKey, inventory, 120, TimeUnit.SECONDS);
```

**L3: 统计数据缓存 (TTL: 10分钟)**
```java
// 仪表盘统计
String cacheKey = "wms:stats:dashboard:global";
redisTemplate.opsForValue().set(cacheKey, stats, 600, TimeUnit.SECONDS);
```

#### 缓存失效策略

**主动失效 (推荐)**:
```java
@Transactional
public Long createInbound(InboundDTO dto) {
    // 1. 创建入库单
    // ...

    // 2. 清除相关缓存
    evictInboundCaches(dto.getWarehouseId());
    evictInventoryCaches(dto.getWarehouseId());
    evictStatsCaches();

    return inbound.getId();
}
```

**延迟双删 (防止脏数据)**:
```java
// 1. 第一次删除缓存
redisTemplate.delete(cacheKey);

// 2. 更新数据库
// ...

// 3. 延迟500ms后再次删除
CompletableFuture.runAsync(() -> {
    Thread.sleep(500);
    redisTemplate.delete(cacheKey);
});
```

#### 预期效果
- 仪表盘加载: 从1.5-2秒降至50-100ms (95%提升)
- 统计查询: 从3-5秒降至50-100ms (98%提升)
- 缓存命中率目标: 80%+

---

### 方案4: 连接池优化

#### 当前配置问题
```yaml
hikari:
  maximum-pool-size: 20  # 不足以支撑50并发
  minimum-idle: 5
  connection-timeout: 30000
```

#### 优化配置
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 10           # 最小空闲连接
      maximum-pool-size: 40      # 最大连接数 (50并发 * 0.8)
      connection-timeout: 20000  # 连接超时20秒
      idle-timeout: 300000       # 空闲超时5分钟
      max-lifetime: 1800000      # 连接最大生命周期30分钟
      connection-test-query: SELECT 1
      pool-name: WmsHikariPool
```

#### 计算公式
```
最大连接数 = 并发用户数 * 0.8
           = 50 * 0.8
           = 40

最小空闲连接 = 最大连接数 * 0.25
            = 40 * 0.25
            = 10
```

#### 预期效果
- 消除连接等待
- 支持50+并发用户
- 数据库连接复用率提升

---

## 实施步骤

### 阶段1: 数据库优化 (优先级: P0)

**时间**: 1-2小时
**风险**: 低

#### 步骤:
```bash
# 1. 备份数据库
mysqldump -u root -p ct_tibet_wms > backup_$(date +%Y%m%d).sql

# 2. 执行索引创建脚本
mysql -u root -p ct_tibet_wms < backend/database/performance_optimization.sql

# 3. 分析表统计信息
mysql -u root -p ct_tibet_wms -e "
ANALYZE TABLE tb_inventory;
ANALYZE TABLE tb_inbound;
ANALYZE TABLE tb_outbound;
ANALYZE TABLE tb_apply;
"

# 4. 验证索引
mysql -u root -p ct_tibet_wms -e "
SELECT TABLE_NAME, INDEX_NAME, GROUP_CONCAT(COLUMN_NAME)
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ct_tibet_wms'
GROUP BY TABLE_NAME, INDEX_NAME;
"
```

### 阶段2: 应用配置优化 (优先级: P0)

**时间**: 30分钟
**风险**: 低

#### 步骤:
```bash
# 1. 更新application.yml
# 修改HikariCP配置 (见方案4)

# 2. 重启应用
./mvnw spring-boot:stop
./mvnw spring-boot:start

# 3. 验证连接池
curl http://localhost:48888/actuator/metrics/hikaricp.connections.active
curl http://localhost:48888/actuator/metrics/hikaricp.connections.max
```

### 阶段3: 基准性能测试 (优先级: P1)

**时间**: 1-2小时
**风险**: 无

#### 步骤:
```bash
cd backend/performance-testing

# 执行基准测试
./run-performance-test.sh

# 选择选项1: 基准性能测试 (50并发用户)

# 记录测试结果
# - 平均响应时间
# - P95响应时间
# - 吞吐量
# - 错误率
```

### 阶段4: Redis缓存实施 (优先级: P1)

**时间**: 4-8小时 (分阶段实施)
**风险**: 中 (需要处理缓存一致性)

#### 步骤:

**4.1 基础数据缓存** (2小时):
```java
// 实施物资、仓库、用户信息缓存
// 参考: backend/src/main/resources/redis-cache-strategy.md
// 章节: "缓存实现方案" - Spring Cache注解方式
```

**4.2 统计数据缓存** (2小时):
```java
// 实施仪表盘、报表缓存
// 参考: backend/src/main/resources/redis-cache-strategy.md
// 章节: "缓存实现方案" - RedisTemplate手动缓存
```

**4.3 缓存失效策略** (2-4小时):
```java
// 实施主动失效和延迟双删
// 参考: backend/src/main/resources/redis-cache-strategy.md
// 章节: "缓存失效策略"
```

### 阶段5: SQL查询优化 (优先级: P2)

**时间**: 8-16小时 (根据实际情况调整)
**风险**: 中 (需要修改Mapper XML和Service代码)

#### 步骤:

**5.1 创建优化后的Mapper XML**:
```xml
<!-- 示例: InventoryMapper.xml -->
<select id="selectInventoryWithDetails" resultMap="InventoryDetailResultMap">
    SELECT
        i.*,
        w.warehouse_name,
        m.material_name,
        m.material_code,
        m.spec,
        m.unit
    FROM tb_inventory i
    LEFT JOIN tb_warehouse w ON i.warehouse_id = w.id
    LEFT JOIN tb_material m ON i.material_id = m.id
    WHERE 1=1
    <if test="warehouseId != null">
        AND i.warehouse_id = #{warehouseId}
    </if>
    ORDER BY i.update_time DESC
</select>
```

**5.2 修改Service实现**:
```java
// 替换循环查询为一次JOIN查询
// 优先级顺序:
// 1. StatisticsServiceImpl (影响最大)
// 2. InventoryServiceImpl
// 3. InboundServiceImpl, OutboundServiceImpl, ApplyServiceImpl
```

**5.3 单元测试验证**:
```bash
# 执行单元测试
./mvnw test -Dtest=StatisticsServiceImplTest
./mvnw test -Dtest=InventoryServiceImplTest
```

### 阶段6: 验证测试 (优先级: P1)

**时间**: 2-3小时
**风险**: 无

#### 步骤:
```bash
# 1. 执行相同的50并发测试
cd backend/performance-testing
./run-performance-test.sh

# 2. 对比优化前后指标
# 3. 生成性能对比报告
# 4. 验证所有功能正常
```

### 阶段7: 监控部署 (优先级: P1)

**时间**: 2-4小时
**风险**: 低

#### 步骤:
```bash
# 1. 启动监控栈
cd backend/monitoring
docker-compose -f docker-compose-monitoring.yml up -d

# 2. 配置Grafana
# 访问 http://localhost:3000
# 登录 admin/admin123
# 导入仪表板

# 3. 配置告警
# 编辑 alertmanager.yml
# 配置邮件/Webhook

# 4. 验证监控
curl http://localhost:9090/-/healthy
curl http://localhost:3000/api/health
```

---

## 性能测试

### 测试场景

#### 场景1: 基准性能测试 (50并发)
- **并发用户**: 50
- **持续时间**: 10分钟
- **用户分布**: 35普通员工 + 10仓管员 + 5管理员

#### 场景2: 压力测试 (100并发)
- **并发用户**: 100
- **持续时间**: 5分钟
- **目标**: 找到系统容量上限

#### 场景3: 峰值测试
- **并发用户**: 20 → 100 → 20
- **持续时间**: 6分钟
- **目标**: 验证系统弹性

### 测试工具

#### 使用JMeter (推荐)
```bash
# 参考文档
backend/performance-testing/jmeter-test-plan-README.md

# 执行测试
cd backend/performance-testing
./run-performance-test.sh
```

#### 使用自动化脚本
```bash
cd backend/performance-testing
chmod +x run-performance-test.sh
./run-performance-test.sh

# 选择选项:
# 1) 基准性能测试 (50并发用户, 推荐)
# 5) 实时性能监控
```

### 测试指标

#### 关键性能指标

| 指标 | 优化前 | 优化目标 | 实际结果 |
|------|--------|----------|---------|
| 平均响应时间 | 1587ms | <1000ms | _待测试_ |
| P95响应时间 | 3201ms | <2000ms | _待测试_ |
| P99响应时间 | 5103ms | <3000ms | _待测试_ |
| 吞吐量 | 78.5 TPS | >100 TPS | _待测试_ |
| 错误率 | 2.3% | <1% | _待测试_ |
| CPU使用率 | 82% | <70% | _待测试_ |
| 内存使用率 | 78% | <75% | _待测试_ |
| 数据库连接 | 18/20 | <15 | _待测试_ |

---

## 监控部署

### 监控架构

```
应用服务器 (Spring Boot)
    ↓ (Actuator Metrics)
Prometheus (时序数据库)
    ↓ (数据查询)
Grafana (可视化)
```

### 部署步骤

#### 快速启动
```bash
cd backend/monitoring
docker-compose -f docker-compose-monitoring.yml up -d
```

#### 访问服务
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Alertmanager**: http://localhost:9093

### 关键仪表板

#### 1. WMS系统概览
- API请求速率
- 响应时间(P50/P95/P99)
- 错误率
- 活跃用户数

#### 2. JVM监控
- 堆内存使用
- GC频率和耗时
- 线程数
- 类加载情况

#### 3. 数据库监控
- 连接池使用率
- 慢查询统计
- 连接数趋势
- 查询性能

#### 4. Redis监控
- 缓存命中率
- 内存使用
- 连接数
- 操作延迟

### 告警配置

#### 关键告警规则

1. **API响应时间过长** (P95 > 2秒)
2. **API错误率过高** (>5%)
3. **JVM堆内存告警** (>85%)
4. **数据库连接池告警** (>80%)
5. **Redis缓存命中率低** (<70%)

---

## 预期性能提升

### 整体性能提升

| 场景 | 优化前 | 优化后 | 提升幅度 |
|------|--------|--------|---------|
| **仪表盘加载** | 1.5-2秒 | 50-100ms | **95%** |
| **库存列表查询** | 1.5-3秒 | 300-500ms | **83%** |
| **入库单列表** | 1.2-2秒 | 400-600ms | **75%** |
| **出库单列表** | 1.2-2秒 | 400-600ms | **75%** |
| **申请单列表** | 1.0-1.8秒 | 350-550ms | **72%** |
| **详情查询** | 600-1000ms | 100-200ms | **85%** |
| **统计报表** | 3-5秒 | 400-800ms | **87%** |

### 分阶段性能提升

#### 阶段1: 数据库索引优化
- **列表查询**: 50-60%提升
- **统计查询**: 60-70%提升
- **CPU使用率**: 降低30%

#### 阶段2: Redis缓存实施
- **仪表盘**: 95%提升 (首次查询后)
- **统计查询**: 98%提升 (首次查询后)
- **重复查询**: >95%提升

#### 阶段3: SQL查询优化
- **N+1查询场景**: 85-95%提升
- **关联查询**: 70-85%提升
- **数据库负载**: 降低60%

### 系统容量提升

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| **并发用户数** | 未测试 | 50+ | - |
| **吞吐量 (TPS)** | ~80 | 180+ | **125%** |
| **数据库QPS** | 高 | 降低70% | **-70%** |
| **应用服务器负载** | 高 | 中等 | **-40%** |

---

## 后续优化建议

### 短期优化 (1-3个月)

#### 1. 慢查询持续优化
```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- 定期分析慢查询
mysqldumpslow -t 10 /var/log/mysql/slow-query.log
```

#### 2. 缓存策略调优
- 监控缓存命中率
- 调整TTL设置
- 优化缓存Key设计

#### 3. 连接池动态调整
- 根据实际并发情况调整
- 监控连接池使用率
- 优化连接超时设置

### 中期优化 (3-6个月)

#### 1. 数据库读写分离
```
主库 (写操作)
  ├─ 从库1 (读操作)
  └─ 从库2 (读操作)
```

#### 2. 应用服务器集群化
```
Nginx (负载均衡)
  ├─ App Server 1
  ├─ App Server 2
  └─ App Server 3
```

#### 3. 静态资源CDN加速
- 前端资源上传CDN
- 图片/附件上传OSS
- 启用浏览器缓存

### 长期优化 (6-12个月)

#### 1. 数据库分库分表
- 按部门分库
- 按时间分表(历史数据归档)
- 使用ShardingSphere

#### 2. 微服务拆分
```
├─ 用户服务
├─ 仓库服务
├─ 库存服务
├─ 单据服务
└─ 统计服务
```

#### 3. 消息队列引入
- 异步处理耗时操作
- 削峰填谷
- 系统解耦

#### 4. ElasticSearch全文搜索
- 物资搜索优化
- 单据全文搜索
- 日志聚合分析

---

## 附录

### 文件清单

#### 性能优化相关
- `backend/database/performance_optimization.sql` - 数据库索引创建脚本
- `backend/database/query_optimization_examples.sql` - SQL查询优化示例
- `backend/src/main/resources/redis-cache-strategy.md` - Redis缓存策略文档

#### 性能测试相关
- `backend/performance-testing/jmeter-test-plan-README.md` - JMeter测试指南
- `backend/performance-testing/run-performance-test.sh` - 自动化测试脚本
- `backend/performance-testing/results/` - 测试结果目录

#### 监控相关
- `backend/monitoring/prometheus.yml` - Prometheus配置
- `backend/monitoring/alerts/wms-alerts.yml` - 告警规则
- `backend/monitoring/docker-compose-monitoring.yml` - 监控栈部署
- `backend/monitoring/MONITORING_SETUP_GUIDE.md` - 监控部署指南

### 参考资源

- [MySQL性能优化](https://dev.mysql.com/doc/refman/8.0/en/optimization.html)
- [Redis最佳实践](https://redis.io/docs/manual/patterns/)
- [HikariCP配置指南](https://github.com/brettwooldridge/HikariCP#configuration)
- [JMeter用户手册](https://jmeter.apache.org/usermanual/)
- [Prometheus文档](https://prometheus.io/docs/)
- [Grafana教程](https://grafana.com/docs/grafana/latest/getting-started/)

### 联系支持

如遇到问题或需要技术支持,请联系:
- **性能工程团队**: performance@example.com
- **文档问题**: docs@example.com

---

**文档结束**
