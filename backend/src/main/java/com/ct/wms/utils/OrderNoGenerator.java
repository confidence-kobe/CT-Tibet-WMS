package com.ct.wms.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 业务单号生成器
 * <p>
 * 格式: {前缀}_{部门编码}_{yyyyMMdd}_{5位流水号}
 * <p>
 * 流水号优先使用 Redis INCR（按 前缀+部门+日期 独立计数，严格递增无碰撞）；
 * Redis 不可用时降级为雪花ID取模（存在小概率碰撞，仅作兜底）。
 *
 * @author CT Development Team
 * @since 2026-08-04
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNoGenerator {

    /**
     * 流水号Redis key保留2天（跨天后不再使用，自动过期清理）
     */
    private static final long SEQ_TTL_SECONDS = 2 * 24 * 3600L;

    private final IdGenerator idGenerator;

    // 可选依赖：测试环境（@Profile("!test")）或 Redis 未启用时为 null
    @Autowired(required = false)
    private RedisUtils redisUtils;

    /**
     * 生成业务单号
     *
     * @param prefix   单号前缀，如 SQ/CK/RK
     * @param deptCode 部门编码
     * @return 单号，如 SQ_DEPT01_20260804_00012
     */
    public String generate(String prefix, String deptCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = nextSequence(prefix, deptCode, today);
        return prefix + "_" + deptCode + "_" + today + "_" + String.format("%05d", sequence);
    }

    private long nextSequence(String prefix, String deptCode, String today) {
        if (redisUtils != null) {
            try {
                String key = "wms:seq:" + prefix + ":" + deptCode + ":" + today;
                Long seq = redisUtils.incr(key, 1);
                if (seq != null) {
                    if (seq == 1L) {
                        redisUtils.expire(key, SEQ_TTL_SECONDS);
                    }
                    return seq % 100000;
                }
            } catch (Exception e) {
                log.warn("Redis 生成单号流水失败，降级为雪花ID取模: prefix={}, deptCode={}", prefix, deptCode, e);
            }
        }
        // 降级：雪花ID取模，存在小概率碰撞（单号列有唯一约束时插入会失败，由调用方重试）
        return idGenerator.nextId() % 100000;
    }
}
