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
     * 申请提交
     */
    APPLY_SUBMIT(1, "申请提交"),

    /**
     * 审批通过
     */
    APPLY_APPROVED(2, "审批通过"),

    /**
     * 审批拒绝
     */
    APPLY_REJECTED(3, "审批拒绝"),

    /**
     * 出库待取
     */
    OUTBOUND_PENDING(4, "出库待取"),

    /**
     * 库存预警
     */
    STOCK_ALERT(5, "库存预警"),

    /**
     * 超时取消
     */
    TIMEOUT_CANCEL(6, "超时取消");

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
