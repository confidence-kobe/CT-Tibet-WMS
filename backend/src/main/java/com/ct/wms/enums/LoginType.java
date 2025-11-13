package com.ct.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型枚举
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Getter
@AllArgsConstructor
public enum LoginType {

    /**
     * 密码登录
     */
    PASSWORD("PASSWORD", "密码登录"),

    /**
     * 手机号登录
     */
    PHONE("PHONE", "手机号登录"),

    /**
     * 微信登录
     */
    WECHAT("WECHAT", "微信登录"),

    /**
     * 企业微信登录
     */
    ENTERPRISE_WECHAT("ENTERPRISE_WECHAT", "企业微信登录");

    @EnumValue
    @JsonValue
    private final String value;

    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param value 枚举值
     * @return 枚举对象
     */
    public static LoginType fromValue(String value) {
        if (value == null) {
            return PASSWORD; // 默认使用密码登录
        }
        for (LoginType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid LoginType value: " + value);
    }
}
