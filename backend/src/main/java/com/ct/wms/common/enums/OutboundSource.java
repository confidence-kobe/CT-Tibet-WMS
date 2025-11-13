package com.ct.wms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出库来源枚举
 */
@Getter
@AllArgsConstructor
public enum OutboundSource {
    DIRECT(1, "直接出库"),
    FROM_APPLY(2, "申请出库");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    public Integer getValue() {
        return code;
    }
}
