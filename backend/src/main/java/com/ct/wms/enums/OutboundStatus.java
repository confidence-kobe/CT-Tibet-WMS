package com.ct.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出库单状态枚举
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Getter
@AllArgsConstructor
public enum OutboundStatus {

    /**
     * 待领取
     */
    PENDING_PICKUP(0, "待领取"),

    /**
     * 已出库
     */
    COMPLETED(1, "已出库"),

    /**
     * 已取消
     */
    CANCELED(2, "已取消");

    @EnumValue
    @JsonValue
    private final Integer value;

    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param value 枚举值
     * @return 枚举对象
     */
    public static OutboundStatus valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (OutboundStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid OutboundStatus value: " + value);
    }
}
