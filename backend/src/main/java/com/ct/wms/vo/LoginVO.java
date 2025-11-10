package com.ct.wms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应VO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "令牌类型")
    private String tokenType;

    @Schema(description = "过期时间(秒)")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "真实姓名")
        private String realName;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "部门ID")
        private Long deptId;

        @Schema(description = "部门名称")
        private String deptName;

        @Schema(description = "角色ID")
        private Long roleId;

        @Schema(description = "角色名称")
        private String roleName;

        @Schema(description = "角色编码")
        private String roleCode;

        @Schema(description = "头像")
        private String avatar;
    }
}
