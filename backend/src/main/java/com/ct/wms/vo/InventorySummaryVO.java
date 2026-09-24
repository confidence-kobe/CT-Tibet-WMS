package com.ct.wms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存汇总（按库存状态统计条数）
 *
 * @author CT Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存汇总")
public class InventorySummaryVO {

    @Schema(description = "库存记录总数")
    private Integer totalMaterials;

    @Schema(description = "正常")
    private Integer normalCount;

    @Schema(description = "低库存（低于最低库存）")
    private Integer lowStockCount;

    @Schema(description = "缺货（库存为0）")
    private Integer outOfStockCount;
}
