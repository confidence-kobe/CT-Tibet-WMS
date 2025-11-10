package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 仓库DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "仓库DTO")
public class WarehouseDTO {

    @NotBlank(message = "仓库名称不能为空")
    @Schema(description = "仓库名称", required = true)
    private String warehouseName;

    @NotBlank(message = "仓库编码不能为空")
    @Schema(description = "仓库编码", required = true)
    private String warehouseCode;

    @NotNull(message = "所属部门不能为空")
    @Schema(description = "所属部门ID", required = true)
    private Long deptId;

    @NotBlank(message = "仓库地址不能为空")
    @Schema(description = "仓库地址", required = true)
    private String address;

    @NotNull(message = "仓库管理员不能为空")
    @Schema(description = "仓库管理员ID", required = true)
    private Long managerId;

    @Schema(description = "仓库容量(平方米)")
    private BigDecimal capacity;

    @Schema(description = "备注")
    private String remark;
}
