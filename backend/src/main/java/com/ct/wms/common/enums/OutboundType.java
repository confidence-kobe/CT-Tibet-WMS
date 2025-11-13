package com.ct.wms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出库类型枚举
 */
@Getter
@AllArgsConstructor
public enum OutboundType {
    RECEIVE(1, "领用出库"),
    SCRAP(2, "报废出库"),
    TRANSFER(3, "调拨出库"),
    OTHER(4, "其他出库");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    public Integer getValue() {
        return code;
    }
}
