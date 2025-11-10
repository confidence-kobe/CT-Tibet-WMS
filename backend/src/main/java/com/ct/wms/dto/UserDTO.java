package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "用户DTO")
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码（创建时必填，更新时选填）")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    @NotNull(message = "部门ID不能为空")
    @Schema(description = "部门ID")
    private Long deptId;

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "备注")
    private String remark;
}
