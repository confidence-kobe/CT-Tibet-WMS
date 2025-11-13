package com.ct.wms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出库状态枚举
 */
@Getter
@AllArgsConstructor
public enum OutboundStatus {
    PENDING(0, "待取货"),
    PENDING_PICKUP(0, "待取货"),  // 别名，保持兼容性
    COMPLETED(1, "已完成"),
    CANCELED(2, "已取消"),
    CANCELLED(2, "已取消");  // 别名，保持兼容性

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    public Integer getValue() {
        return code;
    }
}
