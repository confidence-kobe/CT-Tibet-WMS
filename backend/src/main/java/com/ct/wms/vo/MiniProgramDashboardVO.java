package com.ct.wms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 小程序首页统计VO
 *
 * @author CT Development Team
 * @since 2025-12-26
 */
@Data
@Schema(description = "小程序首页统计VO")
public class MiniProgramDashboardVO {

    // ========== 员工视图数据 ==========
    @Schema(description = "我的申请统计")
    private MyApplyStats myApplies;

    @Schema(description = "最近消息列表")
    private List<MessageItem> messages;

    // ========== 仓管视图数据 ==========
    @Schema(description = "今日数据统计")
    private TodayStats todayData;

    @Schema(description = "待办事项统计")
    private PendingTasks pendingTasks;

    @Schema(description = "最近操作记录")
    private List<OperationItem> recentOperations;

    /**
     * 我的申请统计
     */
    @Data
    public static class MyApplyStats {
        @Schema(description = "待审批数量")
        private Integer pendingCount = 0;

        @Schema(description = "已通过数量")
        private Integer approvedCount = 0;

        @Schema(description = "待领取数量")
        private Integer pickupCount = 0;
    }

    /**
     * 今日数据统计
     */
    @Data
    public static class TodayStats {
        @Schema(description = "今日入库数量")
        private Integer inboundCount = 0;

        @Schema(description = "今日出库数量")
        private Integer outboundCount = 0;

        @Schema(description = "待审批申请数量")
        private Integer pendingApprovalCount = 0;

        @Schema(description = "库存物资种类数")
        private Integer materialCount = 0;
    }

    /**
     * 待办事项统计
     */
    @Data
    public static class PendingTasks {
        @Schema(description = "待审批数量")
        private Integer pendingApproval = 0;

        @Schema(description = "待领取出库数量")
        private Integer pendingPickup = 0;

        @Schema(description = "低库存预警数量")
        private Integer lowStockAlert = 0;
    }

    /**
     * 消息项
     */
    @Data
    public static class MessageItem {
        @Schema(description = "消息ID")
        private Long id;

        @Schema(description = "消息标题")
        private String title;

        @Schema(description = "消息内容")
        private String content;

        @Schema(description = "消息时间")
        private String time;

        @Schema(description = "关联类型：1-申请单 2-出库单")
        private Integer relatedType;

        @Schema(description = "关联ID")
        private Long relatedId;
    }

    /**
     * 操作记录项
     */
    @Data
    public static class OperationItem {
        @Schema(description = "操作时间")
        private String time;

        @Schema(description = "操作标题")
        private String title;
    }
}
