package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @Size(max = 50, message = "领用人姓名不能超过50字")
    @Schema(description = "领用人姓名（领用人不是系统用户时填写，如外部施工人员）")
    private String receiverName;

    @Size(max = 20, message = "领用人电话不能超过20位")
    @Schema(description = "领用人电话")
    private String receiverPhone;

    @Size(max = 500, message = "用途说明不能超过500字")
    @Schema(description = "用途说明")
    private String purpose;

    @Size(max = 500, message = "备注不能超过500字")
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

        @Size(max = 200, message = "备注不能超过200字")
        @Schema(description = "备注")
        private String remark;
    }
}
