package com.ct.wms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    SYSTEM(0, "系统消息"),
    APPLY_SUBMIT(1, "申请提交"),
    APPLY_REMINDER(2, "申请提醒"),
    APPLY_APPROVED(3, "申请通过"),
    APPLY_REJECTED(4, "申请拒绝"),
    OUTBOUND_PENDING(5, "待取货提醒"),
    PICKUP_REMINDER(6, "取货提醒"),
    TIMEOUT_NOTICE(7, "超时通知"),
    TIMEOUT_CANCEL(8, "超时取消"),
    STOCK_ALERT(9, "库存预警");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    public Integer getValue() {
        return code;
    }
}
