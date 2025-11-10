package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ct.wms.enums.OutboundSource;
import com.ct.wms.enums.OutboundStatus;
import com.ct.wms.enums.OutboundType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_outbound")
@Schema(description = "出库单")
public class Outbound extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 出库单号（格式：CK_部门编码_YYYYMMDD_流水号）
     */
    @Schema(description = "出库单号")
    private String outboundNo;

    /**
     * 出库仓库ID
     */
    @Schema(description = "出库仓库ID")
    private Long warehouseId;

    /**
     * 出库类型（1-领用出库 2-报废出库 3-调拨出库 4-其他）
     */
    @Schema(description = "出库类型")
    private OutboundType outboundType;

    /**
     * 出库来源（1-直接创建 2-申请自动创建）
     */
    @Schema(description = "出库来源")
    private OutboundSource source;

    /**
     * 关联申请单ID（来源为2时）
     */
    @Schema(description = "关联申请单ID")
    private Long applyId;

    /**
     * 领用人ID
     */
    @Schema(description = "领用人ID")
    private Long receiverId;

    /**
     * 领用人姓名（冗余字段）
     */
    @Schema(description = "领用人姓名")
    private String receiverName;

    /**
     * 领用人手机号（冗余字段）
     */
    @Schema(description = "领用人手机号")
    private String receiverPhone;

    /**
     * 领用用途
     */
    @Schema(description = "领用用途")
    private String purpose;

    /**
     * 操作人ID（仓管员）
     */
    @Schema(description = "操作人ID")
    private Long operatorId;

    /**
     * 出库时间（确认领取时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "出库时间")
    private LocalDateTime outboundTime;

    /**
     * 状态（0-待领取 1-已出库 2-已取消）
     */
    @Schema(description = "状态")
    private OutboundStatus status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    // 非数据库字段
    /**
     * 仓库名称
     */
    @TableField(exist = false)
    @Schema(description = "仓库名称")
    private String warehouseName;

    /**
     * 操作人姓名
     */
    @TableField(exist = false)
    @Schema(description = "操作人姓名")
    private String operatorName;

    /**
     * 关联申请单号
     */
    @TableField(exist = false)
    @Schema(description = "关联申请单号")
    private String applyNo;

    /**
     * 出库明细列表
     */
    @TableField(exist = false)
    @Schema(description = "出库明细列表")
    private List<OutboundDetail> details;
}
