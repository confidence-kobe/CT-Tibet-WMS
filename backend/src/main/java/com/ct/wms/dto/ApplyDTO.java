package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 申请单DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "申请单DTO")
public class ApplyDTO {

    @NotNull(message = "仓库ID不能为空")
    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "申请理由")
    private String applyReason;

    @NotEmpty(message = "申请明细不能为空")
    @Valid
    @Schema(description = "申请明细列表")
    private List<ApplyDetailDTO> details;

    /**
     * 申请明细DTO
     */
    @Data
    @Schema(description = "申请明细DTO")
    public static class ApplyDetailDTO {

        @NotNull(message = "物资ID不能为空")
        @Schema(description = "物资ID")
        private Long materialId;

        @NotNull(message = "数量不能为空")
        @DecimalMin(value = "0.01", message = "数量必须大于0")
        @Schema(description = "数量")
        private BigDecimal quantity;

        @Schema(description = "用途说明")
        private String remark;
    }
}
