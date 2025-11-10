package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 物资DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "物资DTO")
public class MaterialDTO {

    @NotBlank(message = "物资名称不能为空")
    @Schema(description = "物资名称", required = true)
    private String materialName;

    @NotBlank(message = "物资编码不能为空")
    @Schema(description = "物资编码", required = true)
    private String materialCode;

    @NotBlank(message = "物资类别不能为空")
    @Schema(description = "物资类别", required = true)
    private String category;

    @Schema(description = "规格型号")
    private String spec;

    @NotBlank(message = "单位不能为空")
    @Schema(description = "单位", required = true)
    private String unit;

    @DecimalMin(value = "0.00", message = "单价不能为负数")
    @Schema(description = "单价")
    private BigDecimal price;

    @NotNull(message = "最低库存不能为空")
    @DecimalMin(value = "0.00", message = "最低库存不能为负数")
    @Schema(description = "最低库存预警阈值", required = true)
    private BigDecimal minStock;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图片URL")
    private String image;
}
