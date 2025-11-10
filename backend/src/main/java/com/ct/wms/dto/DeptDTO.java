package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 部门DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "部门DTO")
public class DeptDTO {

    @NotBlank(message = "部门名称不能为空")
    @Schema(description = "部门名称")
    private String deptName;

    @NotBlank(message = "部门编码不能为空")
    @Schema(description = "部门编码")
    private String deptCode;

    @NotNull(message = "上级部门ID不能为空")
    @Schema(description = "上级部门ID，0表示顶级部门")
    private Long parentId;

    @Schema(description = "负责人ID")
    private Long leaderId;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "备注")
    private String remark;
}
