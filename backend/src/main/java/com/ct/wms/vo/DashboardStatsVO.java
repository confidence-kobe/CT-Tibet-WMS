package com.ct.wms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 仪表盘统计VO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "仪表盘统计VO")
public class DashboardStatsVO {

    @Schema(description = "总物资种类数")
    private Long totalMaterials;

    @Schema(description = "总仓库数")
    private Long totalWarehouses;

    @Schema(description = "低库存预警数")
    private Long lowStockAlerts;

    @Schema(description = "待审批申请数")
    private Long pendingApplies;

    @Schema(description = "本月入库单数")
    private Long monthInboundCount;

    @Schema(description = "本月入库总额")
    private BigDecimal monthInboundAmount;

    @Schema(description = "本月出库单数")
    private Long monthOutboundCount;

    @Schema(description = "本月出库总额")
    private BigDecimal monthOutboundAmount;

    @Schema(description = "库存总价值")
    private BigDecimal totalInventoryValue;
}
