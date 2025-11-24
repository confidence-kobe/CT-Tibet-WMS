package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 入库统计DTO
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "入库统计数据")
public class InboundStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "入库总次数")
    private Integer totalCount;

    @Schema(description = "入库总数量")
    private BigDecimal totalQuantity;

    @Schema(description = "入库总金额")
    private BigDecimal totalAmount;

    @Schema(description = "日均入库数量")
    private BigDecimal avgDaily;

    @Schema(description = "趋势图数据")
    private TrendData trendData;

    @Schema(description = "按仓库统计")
    private List<ChartData> warehouseData;

    @Schema(description = "物资分类占比")
    private List<ChartData> categoryData;

    @Schema(description = "每日明细表格")
    private List<DailyData> dailyData;

    /**
     * 趋势图数据内部类
     */
    @Data
    @Schema(description = "趋势图数据")
    public static class TrendData implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "日期列表")
        private List<String> dates;

        @Schema(description = "数量列表")
        private List<BigDecimal> quantities;

        @Schema(description = "金额列表")
        private List<BigDecimal> amounts;
    }

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
     * 每日明细数据内部类
     */
    @Data
    @Schema(description = "每日明细数据")
    public static class DailyData implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "日期")
        private String date;

        @Schema(description = "入库次数")
        private Integer count;

        @Schema(description = "入库数量")
        private BigDecimal quantity;

        @Schema(description = "入库金额")
        private BigDecimal amount;
    }
}
