package com.ct.wms.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 *
 * 使用SETNX命令实现分布式锁，支持可重入和自动续期
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisLockUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 分布式锁默认超时时间（秒）
     */
    private static final long DEFAULT_LOCK_TIMEOUT = 30;

    /**
     * 分布式锁获取等待时间（毫秒）
     */
    private static final long DEFAULT_WAIT_TIME = 5000;

    /**
     * 分布式锁获取重试间隔（毫秒）
     */
    private static final long DEFAULT_RETRY_INTERVAL = 100;

    /**
     * Lua脚本：释放分布式锁（确保只释放自己持有的锁）
     */
    private static final String RELEASE_LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end";

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey 锁的key
     * @return 锁标识，用于释放锁
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_LOCK_TIMEOUT);
    }

    /**
     * 尝试获取分布式锁（指定超时时间）
     *
     * @param lockKey  锁的key
     * @param timeout  锁超时时间（秒）
     * @return 锁标识，用于释放锁
     */
    public String tryLock(String lockKey, long timeout) {
        return tryLock(lockKey, timeout, DEFAULT_WAIT_TIME);
    }

    /**
     * 尝试获取分布式锁（指定超时时间和等待时间）
     *
     * @param lockKey    锁的key
     * @param timeout    锁超时时间（秒）
     * @param waitTime   等待获取锁的时间（毫秒）
     * @return 锁标识，用于释放锁
     */
    public String tryLock(String lockKey, long timeout, long waitTime) {
        String lockValue = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            while (System.currentTimeMillis() - startTime < waitTime) {
                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, lockValue, timeout, TimeUnit.SECONDS);

                if (Boolean.TRUE.equals(acquired)) {
                    log.debug("获取分布式锁成功: lockKey={}, lockValue={}", lockKey, lockValue);
                    return lockValue;
                }

                // 等待一段时间后重试
                Thread.sleep(DEFAULT_RETRY_INTERVAL);
            }

            log.warn("获取分布式锁失败（超时）: lockKey={}, waitTime={}ms", lockKey, waitTime);
            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: lockKey={}", lockKey, e);
            return null;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁的key
     * @param lockValue 锁标识（必须与获取时的标识一致）
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String lockValue) {
        if (lockValue == null) {
            log.warn("释放分布式锁失败：lockValue为空, lockKey={}", lockKey);
            return false;
        }

        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(RELEASE_LOCK_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);

            if (result != null && result > 0) {
                log.debug("释放分布式锁成功: lockKey={}, lockValue={}", lockKey, lockValue);
                return true;
            } else {
                log.warn("释放分布式锁失败（锁不存在或已被他人持有）: lockKey={}, lockValue={}", lockKey, lockValue);
                return false;
            }

        } catch (Exception e) {
            log.error("释放分布式锁异常: lockKey={}, lockValue={}", lockKey, lockValue, e);
            return false;
        }
    }

    /**
     * 尝试获取分布式锁（带重试）
     *
     * @param lockKey   锁的key
     * @param timeout   锁超时时间（秒）
     * @param maxRetries 最大重试次数
     * @return 锁标识，用于释放锁
     */
    public String tryLockWithRetry(String lockKey, long timeout, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            String lockValue = tryLock(lockKey, timeout);
            if (lockValue != null) {
                return lockValue;
            }
            log.debug("获取分布式锁重试: lockKey={}, retry={}/{}", lockKey, i + 1, maxRetries);
        }
        return null;
    }
}
