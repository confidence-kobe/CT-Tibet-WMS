package com.ct.wms.dto;

import com.ct.wms.enums.InboundType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "入库单DTO")
public class InboundDTO {

    @NotNull(message = "仓库ID不能为空")
    @Schema(description = "仓库ID", required = true)
    private Long warehouseId;

    @NotNull(message = "入库类型不能为空")
    @Schema(description = "入库类型(1-采购 2-退货 3-调拨 4-其他)", required = true)
    private InboundType inboundType;

    @NotNull(message = "入库时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入库时间", required = true)
    private LocalDateTime inboundTime;

    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "入库明细不能为空")
    @Valid
    @Schema(description = "入库明细列表", required = true)
    private List<InboundDetailDTO> details;

    @Data
    @Schema(description = "入库明细")
    public static class InboundDetailDTO {

        @NotNull(message = "物资ID不能为空")
        @Schema(description = "物资ID", required = true)
        private Long materialId;

        @NotNull(message = "数量不能为空")
        @Schema(description = "入库数量", required = true)
        private BigDecimal quantity;

        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Schema(description = "备注")
        private String remark;
    }
}
