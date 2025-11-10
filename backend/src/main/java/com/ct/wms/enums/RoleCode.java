package com.ct.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色编码枚举
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Getter
@AllArgsConstructor
public enum RoleCode {

    /**
     * 系统管理员
     */
    ADMIN("admin", "系统管理员"),

    /**
     * 部门管理员
     */
    DEPT_ADMIN("dept_admin", "部门管理员"),

    /**
     * 仓库管理员
     */
    WAREHOUSE("warehouse", "仓库管理员"),

    /**
     * 普通员工
     */
    USER("user", "普通员工");

    private final String code;
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 角色编码
     * @return 枚举对象
     */
    public static RoleCode fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (RoleCode roleCode : values()) {
            if (roleCode.code.equals(code)) {
                return roleCode;
            }
        }
        throw new IllegalArgumentException("Invalid RoleCode: " + code);
    }
}
