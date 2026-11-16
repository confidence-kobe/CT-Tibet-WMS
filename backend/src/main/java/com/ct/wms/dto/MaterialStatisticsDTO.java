package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "物资统计数据")
public class MaterialStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "物资总种类")
    private Integer totalMaterials;

    @Schema(description = "活跃物资数（有出入库记录）")
    private Integer activeMaterials;

    @Schema(description = "滞销物资数（库存>0但无出库记录）")
    private Integer slowMaterials;

    @Schema(description = "物资明细列表")
    private List<MaterialItem> items;

    @Data
    @Schema(description = "物资统计明细")
    public static class MaterialItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long materialId;
        private String materialName;
        private String materialCode;
        private String categoryName;
        private String specification;
        private Integer inboundCount;
        private BigDecimal inboundQuantity;
        private Integer outboundCount;
        private BigDecimal outboundQuantity;
        private BigDecimal currentStock;
        private BigDecimal totalValue;
        private BigDecimal turnoverRate;
        private String status;
    }
}
