package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 更新个人信息请求DTO
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "更新个人信息请求")
public class UpdateProfileRequest {

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;
}
