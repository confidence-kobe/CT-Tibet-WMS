package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修改密码请求DTO
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间")
    @Schema(description = "新密码")
    private String newPassword;
}
