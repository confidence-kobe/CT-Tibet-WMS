package com.ct.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 入库类型枚举
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Getter
@AllArgsConstructor
public enum InboundType {

    /**
     * 采购入库
     */
    PURCHASE(1, "采购入库"),

    /**
     * 退货入库
     */
    RETURN(2, "退货入库"),

    /**
     * 调拨入库
     */
    TRANSFER(3, "调拨入库"),

    /**
     * 其他入库
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
    public static InboundType valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (InboundType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid InboundType value: " + value);
    }
}
