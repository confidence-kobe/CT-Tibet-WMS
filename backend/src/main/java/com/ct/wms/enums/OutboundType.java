package com.ct.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出库类型枚举
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Getter
@AllArgsConstructor
public enum OutboundType {

    /**
     * 领用出库
     */
    RECEIVE(1, "领用出库"),

    /**
     * 报废出库
     */
    SCRAP(2, "报废出库"),

    /**
     * 调拨出库
     */
    TRANSFER(3, "调拨出库"),

    /**
     * 其他出库
     */
    OTHER(4, "其他");

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
    public static OutboundType valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (OutboundType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid OutboundType value: " + value);
    }
}
