# Redis缓存策略设计方案

## 概述

本文档定义了CT-Tibet-WMS系统的Redis缓存策略,旨在通过合理的缓存设计减少数据库查询压力,提升系统响应速度。

## 缓存分层策略

### L1: 热数据缓存 (TTL: 5分钟)
- **物资基本信息**: 查询频率高,变更频率低
- **仓库基本信息**: 查询频率高,变更频率低
- **部门信息**: 查询频率高,变更频率低
- **用户基本信息**: 查询频率高,变更频率低

### L2: 业务数据缓存 (TTL: 2分钟)
- **库存实时数据**: 查询频率高,变更频率中
- **待审批申请数量**: 查询频率高,变更频率中
- **低库存预警列表**: 查询频率中,变更频率低

### L3: 统计数据缓存 (TTL: 10分钟)
- **仪表盘统计数据**: 计算复杂,变更频率低
- **入库统计数据**: 计算复杂,变更频率低
- **出库统计数据**: 计算复杂,变更频率低
- **库存统计数据**: 计算复杂,变更频率低

### L4: 列表查询结果缓存 (TTL: 1分钟)
- **入库单列表**: 查询频率中,数据量大
- **出库单列表**: 查询频率中,数据量大
- **申请单列表**: 查询频率中,数据量大

## 缓存Key设计规范

### Key命名规则
```
{系统前缀}:{业务模块}:{数据类型}:{唯一标识}
```

### Key示例

#### 1. 基础数据缓存
```
wms:material:info:123                  # 物资ID=123的详细信息
wms:material:list:all                  # 所有物资列表(简化版)
wms:warehouse:info:456                 # 仓库ID=456的详细信息
wms:warehouse:list:dept:10             # 部门ID=10的仓库列表
wms:dept:info:10                       # 部门ID=10的详细信息
wms:user:info:1001                     # 用户ID=1001的详细信息
```

#### 2. 库存数据缓存
```
wms:inventory:item:w456:m123           # 仓库456中物资123的库存信息
wms:inventory:list:w456                # 仓库456的库存列表(分页)
wms:inventory:low:w456                 # 仓库456的低库存预警列表
wms:inventory:total:value              # 全局库存总值
```

#### 3. 业务单据缓存
```
wms:inbound:detail:1001                # 入库单ID=1001的详细信息
wms:inbound:list:w456:p1               # 仓库456的入库单列表第1页
wms:outbound:detail:2001               # 出库单ID=2001的详细信息
wms:outbound:pending:w456              # 仓库456的待取货出库单
wms:apply:detail:3001                  # 申请单ID=3001的详细信息
wms:apply:pending:u1001                # 用户1001的待审批申请
```

#### 4. 统计数据缓存
```
wms:stats:dashboard:global             # 全局仪表盘统计
wms:stats:dashboard:dept:10            # 部门10的仪表盘统计
wms:stats:inbound:20250101:20250131    # 1月入库统计
wms:stats:outbound:20250101:20250131   # 1月出库统计
wms:stats:inventory:snapshot           # 库存统计快照
```

## 缓存实现方案

### 1. Spring Cache注解方式 (推荐用于简单场景)

```java
@Service
@CacheConfig(cacheNames = "wms")
public class MaterialServiceImpl implements MaterialService {

    // 查询物资详情 - 自动缓存
    @Cacheable(key = "'material:info:' + #id")
    public Material getMaterialById(Long id) {
        return materialMapper.selectById(id);
    }

    // 更新物资 - 自动清除缓存
    @CacheEvict(key = "'material:info:' + #material.id")
    public void updateMaterial(Material material) {
        materialMapper.updateById(material);
    }

    // 删除物资 - 自动清除缓存
    @CacheEvict(key = "'material:info:' + #id")
    public void deleteMaterial(Long id) {
        materialMapper.deleteById(id);
    }
}
```

### 2. RedisTemplate手动缓存 (推荐用于复杂场景)

```java
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MaterialMapper materialMapper;

    private static final String CACHE_PREFIX = "wms:stats:";
    private static final long CACHE_TTL = 600; // 10分钟

    public DashboardStatsVO getDashboardStats() {
        String cacheKey = CACHE_PREFIX + "dashboard:global";

        // 尝试从缓存获取
        DashboardStatsVO cached = (DashboardStatsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取仪表盘数据: {}", cacheKey);
            return cached;
        }

        // 缓存未命中,执行查询
        log.debug("缓存未命中,查询数据库: {}", cacheKey);
        DashboardStatsVO stats = calculateDashboardStats();

        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, stats, CACHE_TTL, TimeUnit.SECONDS);
        log.debug("写入缓存: {}", cacheKey);

        return stats;
    }

    private DashboardStatsVO calculateDashboardStats() {
        // 原有的数据库查询逻辑
        // ...
    }
}
```

### 3. Cache-Aside模式 (旁路缓存)

```java
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final InventoryMapper inventoryMapper;

    private static final String CACHE_PREFIX = "wms:inventory:";
    private static final long CACHE_TTL = 120; // 2分钟

    /**
     * 查询库存 - Cache-Aside模式
     */
    public Inventory getInventory(Long warehouseId, Long materialId) {
        String cacheKey = CACHE_PREFIX + "item:w" + warehouseId + ":m" + materialId;

        // 1. 先查缓存
        Inventory cached = (Inventory) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 缓存未命中,查数据库
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getWarehouseId, warehouseId);
        wrapper.eq(Inventory::getMaterialId, materialId);
        Inventory inventory = inventoryMapper.selectOne(wrapper);

        // 3. 写入缓存
        if (inventory != null) {
            redisTemplate.opsForValue().set(cacheKey, inventory, CACHE_TTL, TimeUnit.SECONDS);
        }

        return inventory;
    }

    /**
     * 增加库存 - 先更新数据库,再删除缓存
     */
    @Transactional(rollbackFor = Exception.class)
    public void increaseInventory(Long warehouseId, Long materialId, BigDecimal quantity,
                                  String relatedNo, Long relatedId, Long operatorId) {
        // 1. 更新数据库
        // ... 原有逻辑 ...

        // 2. 删除缓存 (确保缓存一致性)
        String cacheKey = CACHE_PREFIX + "item:w" + warehouseId + ":m" + materialId;
        redisTemplate.delete(cacheKey);

        // 3. 删除相关列表缓存
        String listKey = CACHE_PREFIX + "list:w" + warehouseId;
        redisTemplate.delete(listKey);

        // 4. 删除统计缓存
        deleteStatsCache();
    }

    /**
     * 删除统计相关缓存
     */
    private void deleteStatsCache() {
        Set<String> keys = redisTemplate.keys("wms:stats:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

## 缓存失效策略

### 1. 主动失效 (推荐)

#### 数据变更时立即失效
```java
@Transactional(rollbackFor = Exception.class)
public Long createInbound(InboundDTO dto) {
    // 1. 创建入库单
    // ...原有逻辑...

    // 2. 清除相关缓存
    evictInboundCaches(dto.getWarehouseId());
    evictInventoryCaches(dto.getWarehouseId());
    evictStatsCaches();

    return inbound.getId();
}

private void evictInboundCaches(Long warehouseId) {
    // 清除入库单列表缓存
    Set<String> keys = redisTemplate.keys("wms:inbound:list:w" + warehouseId + ":*");
    if (keys != null && !keys.isEmpty()) {
        redisTemplate.delete(keys);
    }
}

private void evictInventoryCaches(Long warehouseId) {
    // 清除库存缓存
    Set<String> keys = redisTemplate.keys("wms:inventory:*:w" + warehouseId + ":*");
    if (keys != null && !keys.isEmpty()) {
        redisTemplate.delete(keys);
    }
}

private void evictStatsCaches() {
    // 清除所有统计缓存
    Set<String> keys = redisTemplate.keys("wms:stats:*");
    if (keys != null && !keys.isEmpty()) {
        redisTemplate.delete(keys);
    }
}
```

### 2. TTL自动过期 (兜底策略)

不同类型数据设置不同的TTL:
- **基础数据**: 300秒 (5分钟)
- **业务数据**: 120秒 (2分钟)
- **统计数据**: 600秒 (10分钟)
- **列表查询**: 60秒 (1分钟)

### 3. 定时刷新 (可选)

对于统计数据,可以使用定时任务预热缓存:

```java
@Component
@RequiredArgsConstructor
public class CacheWarmupTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StatisticsService statisticsService;

    /**
     * 每10分钟预热仪表盘统计缓存
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void warmupDashboardStats() {
        log.info("开始预热仪表盘统计缓存");

        // 提前查询并缓存
        DashboardStatsVO stats = statisticsService.getDashboardStats();

        String cacheKey = "wms:stats:dashboard:global";
        redisTemplate.opsForValue().set(cacheKey, stats, 600, TimeUnit.SECONDS);

        log.info("仪表盘统计缓存预热完成");
    }
}
```

## 缓存一致性方案

### 方案选择: Cache-Aside + 延迟双删

#### 读操作
```
1. 查询缓存
2. 如果命中,直接返回
3. 如果未命中,查询数据库
4. 将结果写入缓存
5. 返回结果
```

#### 写操作
```
1. 删除缓存 (第一次删除)
2. 更新数据库
3. 延迟500ms
4. 再次删除缓存 (第二次删除,防止读操作在更新期间写入旧数据)
```

#### 实现示例
```java
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final InventoryMapper inventoryMapper;

    @Transactional(rollbackFor = Exception.class)
    public void decreaseInventory(Long warehouseId, Long materialId, BigDecimal quantity,
                                  String relatedNo, Long relatedId, Long operatorId) {
        String cacheKey = "wms:inventory:item:w" + warehouseId + ":m" + materialId;

        // 1. 第一次删除缓存
        redisTemplate.delete(cacheKey);

        // 2. 更新数据库
        // ... 原有逻辑 ...

        // 3. 延迟双删 (异步执行,不影响主流程)
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500); // 延迟500ms
                redisTemplate.delete(cacheKey); // 第二次删除
                log.debug("延迟双删完成: {}", cacheKey);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("延迟双删失败", e);
            }
        });
    }
}
```

## 缓存穿透/击穿/雪崩防护

### 1. 缓存穿透防护 (查询不存在的数据)

#### 方案: 缓存空对象
```java
public Material getMaterialById(Long id) {
    String cacheKey = "wms:material:info:" + id;

    // 查缓存
    Material cached = (Material) redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        // 检查是否是空对象标记
        if (cached.getId() == null) {
            return null; // 之前查询过,数据不存在
        }
        return cached;
    }

    // 查数据库
    Material material = materialMapper.selectById(id);

    if (material != null) {
        // 缓存实际数据
        redisTemplate.opsForValue().set(cacheKey, material, 300, TimeUnit.SECONDS);
    } else {
        // 缓存空对象,防止穿透 (设置较短的TTL)
        Material emptyObj = new Material();
        redisTemplate.opsForValue().set(cacheKey, emptyObj, 60, TimeUnit.SECONDS);
    }

    return material;
}
```

### 2. 缓存击穿防护 (热点数据过期)

#### 方案: 互斥锁 + 永不过期
```java
public DashboardStatsVO getDashboardStats() {
    String cacheKey = "wms:stats:dashboard:global";
    String lockKey = cacheKey + ":lock";

    // 查缓存
    DashboardStatsVO cached = (DashboardStatsVO) redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return cached;
    }

    // 尝试获取锁 (使用Redis SETNX实现)
    Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(
        lockKey, "locked", 10, TimeUnit.SECONDS
    );

    try {
        if (Boolean.TRUE.equals(lockAcquired)) {
            // 获取锁成功,查询数据库
            DashboardStatsVO stats = calculateDashboardStats();

            // 写入缓存 (热点数据设置较长TTL)
            redisTemplate.opsForValue().set(cacheKey, stats, 600, TimeUnit.SECONDS);

            return stats;
        } else {
            // 获取锁失败,等待后重试
            Thread.sleep(100);
            return getDashboardStats(); // 递归重试
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new BusinessException(500, "获取统计数据失败");
    } finally {
        // 释放锁
        if (Boolean.TRUE.equals(lockAcquired)) {
            redisTemplate.delete(lockKey);
        }
    }
}
```

### 3. 缓存雪崩防护 (大量缓存同时过期)

#### 方案: 随机TTL + 缓存预热
```java
private long getRandomTTL(long baseTTL) {
    // 在基础TTL上增加随机时间 (±20%)
    long randomOffset = (long) (baseTTL * 0.2 * Math.random());
    return baseTTL + randomOffset;
}

public void cacheData(String key, Object value, long baseTTL) {
    long randomTTL = getRandomTTL(baseTTL);
    redisTemplate.opsForValue().set(key, value, randomTTL, TimeUnit.SECONDS);
}
```

## 性能监控指标

### 1. 缓存命中率
```java
@Component
@RequiredArgsConstructor
public class CacheMetrics {

    private final MeterRegistry meterRegistry;

    public void recordCacheHit(String cacheName) {
        meterRegistry.counter("wms.cache.hit", "cache", cacheName).increment();
    }

    public void recordCacheMiss(String cacheName) {
        meterRegistry.counter("wms.cache.miss", "cache", cacheName).increment();
    }
}
```

### 2. 监控建议
- **目标命中率**: 80%以上
- **统计缓存**: 85%以上 (计算复杂,应高命中)
- **基础数据**: 90%以上 (变更少,应高命中)
- **业务数据**: 70%以上 (变更频繁,命中率相对较低)

## 预期性能提升

### 场景1: 仪表盘加载
- **优化前**: 执行6-8次数据库查询,响应时间1.5-2秒
- **优化后**: 1次缓存读取,响应时间50-100ms
- **提升幅度**: 95%

### 场景2: 库存列表查询
- **优化前**: 执行1+2N次数据库查询 (N为记录数)
- **优化后**: 1次缓存读取 (首次查询后)
- **提升幅度**: 90%

### 场景3: 统计报表查询
- **优化前**: 复杂聚合查询,响应时间3-5秒
- **优化后**: 缓存命中时50-100ms
- **提升幅度**: 98%

## 实施步骤

1. **阶段1**: 实施基础数据缓存 (物资、仓库、部门、用户)
2. **阶段2**: 实施统计数据缓存 (仪表盘、报表)
3. **阶段3**: 实施业务数据缓存 (库存、单据列表)
4. **阶段4**: 优化缓存一致性和防护机制
5. **阶段5**: 监控和调优

## 注意事项

1. **缓存数据大小**: 单个缓存对象不超过1MB
2. **缓存键数量**: 控制在10万以内
3. **内存使用**: Redis内存使用率不超过80%
4. **序列化方式**: 使用Jackson或Kryo,避免使用JDK序列化
5. **缓存雪崩**: 设置随机TTL,避免大量缓存同时过期
