package com.ct.wms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申请状态枚举
 */
@Getter
@AllArgsConstructor
public enum ApplyStatus {
    PENDING(0, "待审批"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    COMPLETED(3, "已完成"),
    CANCELED(4, "已取消"),
    CANCELLED(4, "已取消");  // 别名，保持兼容性

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    public Integer getValue() {
        return code;
    }
}
