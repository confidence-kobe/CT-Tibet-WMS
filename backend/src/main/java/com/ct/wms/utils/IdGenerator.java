package com.ct.wms.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

/**
 * 分布式ID生成器
 * 使用雪花算法生成唯一ID
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Component
public class IdGenerator {

    private final Snowflake snowflake;

    public IdGenerator() {
        // 使用简单雪花算法，workId和datacenterId默认为1
        this.snowflake = IdUtil.getSnowflake(1, 1);
    }

    /**
     * 生成下一个ID
     *
     * @return 唯一ID
     */
    public long nextId() {
        return snowflake.nextId();
    }

    /**
     * 生成字符串格式的ID
     *
     * @return 唯一ID字符串
     */
    public String nextIdStr() {
        return snowflake.nextIdStr();
    }

    /**
     * 生成简单UUID（无下划线）
     *
     * @return UUID字符串
     */
    public String simpleUUID() {
        return IdUtil.simpleUUID();
    }
}
