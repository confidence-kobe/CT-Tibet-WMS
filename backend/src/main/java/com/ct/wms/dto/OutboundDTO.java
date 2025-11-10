package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "出库单DTO")
public class OutboundDTO {

    @NotNull(message = "仓库ID不能为空")
    @Schema(description = "仓库ID")
    private Long warehouseId;

    @NotNull(message = "出库类型不能为空")
    @Schema(description = "出库类型: 1-领用 2-报废 3-调拨 4-其他")
    private Integer outboundType;

    @NotNull(message = "出库时间不能为空")
    @Schema(description = "出库时间")
    private LocalDateTime outboundTime;

    @Schema(description = "领用人ID（仅领用时需要）")
    private Long receiverId;

    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "出库明细不能为空")
    @Valid
    @Schema(description = "出库明细列表")
    private List<OutboundDetailDTO> details;

    /**
     * 出库明细DTO
     */
    @Data
    @Schema(description = "出库明细DTO")
    public static class OutboundDetailDTO {

        @NotNull(message = "物资ID不能为空")
        @Schema(description = "物资ID")
        private Long materialId;

        @NotNull(message = "数量不能为空")
        @Schema(description = "数量")
        private BigDecimal quantity;

        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Schema(description = "备注")
        private String remark;
    }
}
