package com.ct.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    /**
     * 系统通知
     */
    SYSTEM(1, "系统通知"),

    /**
     * 申请通知
     */
    APPLY(2, "申请通知"),

    /**
     * 审批通知
     */
    APPROVAL(3, "审批通知"),

    /**
     * 库存预警
     */
    INVENTORY_ALERT(4, "库存预警");

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
    public static MessageType valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (MessageType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + value);
    }
}
