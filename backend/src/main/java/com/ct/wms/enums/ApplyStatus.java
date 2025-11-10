package com.ct.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申请单状态枚举
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Getter
@AllArgsConstructor
public enum ApplyStatus {

    /**
     * 待审批
     */
    PENDING(0, "待审批"),

    /**
     * 已通过
     */
    APPROVED(1, "已通过"),

    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),

    /**
     * 已出库
     */
    COMPLETED(3, "已出库"),

    /**
     * 已取消
     */
    CANCELED(4, "已取消");

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
    public static ApplyStatus valueOf(Integer value) {
        if (value == null) {
            return null;
        }
        for (ApplyStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ApplyStatus value: " + value);
    }
}
