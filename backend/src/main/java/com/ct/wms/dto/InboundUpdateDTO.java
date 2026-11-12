package com.ct.wms.dto;

import com.ct.wms.common.enums.InboundType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 编辑入库单DTO
 * 只允许修改不影响库存的信息；物资和数量录错需作废后重新入库
 *
 * @author CT Development Team
 * @since 2026-11-12
 */
@Data
@Schema(description = "编辑入库单DTO")
public class InboundUpdateDTO {

    @NotNull(message = "入库类型不能为空")
    @Schema(description = "入库类型(1-采购 2-退货 3-调拨 4-其他)", required = true)
    private InboundType inboundType;

    @NotNull(message = "入库时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入库时间", required = true)
    private LocalDateTime inboundTime;

    @Size(max = 500, message = "备注不能超过500字")
    @Schema(description = "备注")
    private String remark;

    @Valid
    @Schema(description = "明细单价修改（只需传要修改的明细）")
    private List<DetailUpdateDTO> details;

    @Data
    @Schema(description = "入库明细修改")
    public static class DetailUpdateDTO {

        @NotNull(message = "明细ID不能为空")
        @Schema(description = "明细ID", required = true)
        private Long id;

        @DecimalMin(value = "0", message = "单价不能为负数")
        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Size(max = 200, message = "备注不能超过200字")
        @Schema(description = "备注")
        private String remark;
    }
}
