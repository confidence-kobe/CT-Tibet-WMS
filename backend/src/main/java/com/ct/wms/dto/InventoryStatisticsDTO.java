package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 库存统计DTO
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "库存统计数据")
public class InventoryStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "物资种类数")
    private Integer materialCount;

    @Schema(description = "库存总值")
    private BigDecimal totalValue;

    @Schema(description = "预警物资数量")
    private Integer warningCount;

    @Schema(description = "库存周转率（次/月）")
    private BigDecimal turnoverRate;

    @Schema(description = "各仓库库存金额")
    private List<ChartData> warehouseData;

    @Schema(description = "物资分类占比")
    private List<ChartData> categoryData;

    @Schema(description = "预警趋势数据")
    private WarningTrendData warningTrendData;

    @Schema(description = "Top 10库存占用")
    private List<TopStockData> topStocks;

    /**
     * 图表数据内部类（用于饼图、柱状图等）
     */
    @Data
    @Schema(description = "图表数据")
    public static class ChartData implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "名称")
        private String name;

        @Schema(description = "数值")
        private BigDecimal value;
    }

    /**
     * 预警趋势数据内部类
     */
    @Data
    @Schema(description = "预警趋势数据")
    public static class WarningTrendData implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "日期列表")
        private List<String> dates;

        @Schema(description = "预警数量列表")
        private List<Integer> counts;
    }

    /**
     * Top库存数据内部类
     */
    @Data
    @Schema(description = "Top库存数据")
    public static class TopStockData implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "物资名称")
        private String materialName;

        @Schema(description = "库存数量")
        private BigDecimal stock;

        @Schema(description = "库存金额")
        private BigDecimal value;
    }
}
