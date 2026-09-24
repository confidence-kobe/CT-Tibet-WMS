package com.ct.wms.common.constant;

/**
 * 角色编码常量
 * <p>
 * 数据库初始化数据与前端（PC/小程序）统一使用小写角色编码。
 * 后端比较角色时必须使用 {@link #is(String, String)}，忽略大小写，
 * 避免因历史数据大小写不一致导致权限判断失效。
 *
 * @author CT Development Team
 */
public final class RoleCode {

    public static final String ADMIN = "admin";
    public static final String DEPT_ADMIN = "dept_admin";
    public static final String WAREHOUSE = "warehouse";
    public static final String USER = "user";

    private RoleCode() {
    }

    /**
     * 判断角色编码是否为指定角色（忽略大小写）
     */
    public static boolean is(String actual, String expected) {
        return actual != null && actual.trim().equalsIgnoreCase(expected);
    }

    /**
     * 规范化角色编码（去空格、转小写），用于写入数据库
     */
    public static String normalize(String roleCode) {
        return roleCode == null ? null : roleCode.trim().toLowerCase();
    }
}
