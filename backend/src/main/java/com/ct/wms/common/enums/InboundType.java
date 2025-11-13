package com.ct.wms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 入库类型枚举
 */
@Getter
@AllArgsConstructor
public enum InboundType {
    PURCHASE(1, "采购入库"),
    RETURN(2, "退货入库"),
    TRANSFER(3, "调拨入库"),
    OTHER(4, "其他入库");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    public Integer getValue() {
        return code;
    }
}
