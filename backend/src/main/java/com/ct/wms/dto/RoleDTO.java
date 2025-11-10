package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 角色DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "角色DTO")
public class RoleDTO {

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码")
    private String roleCode;

    @NotNull(message = "角色级别不能为空")
    @Schema(description = "角色级别: 0-系统管理员 1-部门管理员 2-仓管员 3-普通员工")
    private Integer roleLevel;

    @Schema(description = "备注")
    private String remark;
}
