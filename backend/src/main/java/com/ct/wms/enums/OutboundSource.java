package com.ct.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出库来源枚举
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Getter
@AllArgsConstructor
public enum OutboundSource {

    /**
     * 直接创建(仓管员直接出库)
     */
    DIRECT(1, "直接创建"),

    /**
     * 申请创建(员工申请审批后自动创建)
     */
    FROM_APPLY(2, "申请创建");

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
    public static OutboundSource valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (OutboundSource source : values()) {
            if (source.value.equals(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Invalid OutboundSource value: " + value);
    }
}
