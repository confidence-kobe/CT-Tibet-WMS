package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ct.wms.enums.ApplyStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 申请单实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_apply")
@Schema(description = "申请单")
public class Apply extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 申请单号（格式：SQ_部门编码_YYYYMMDD_流水号）
     */
    @Schema(description = "申请单号")
    private String applyNo;

    /**
     * 申请人ID
     */
    @Schema(description = "申请人ID")
    private Long applicantId;

    /**
     * 申请人部门ID
     */
    @Schema(description = "申请人部门ID")
    private Long deptId;

    /**
     * 领用用途
     */
    @Schema(description = "领用用途")
    private String purpose;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    /**
     * 状态（0-待审批 1-已通过 2-已拒绝 3-已出库 4-已取消）
     */
    @Schema(description = "状态")
    private ApplyStatus status;

    /**
     * 审批人ID
     */
    @Schema(description = "审批人ID")
    private Long approverId;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见")
    private String approvalOpinion;

    /**
     * 拒绝原因
     */
    @Schema(description = "拒绝原因")
    private String rejectReason;

    /**
     * 关联出库单ID（审批通过后自动创建）
     */
    @Schema(description = "关联出库单ID")
    private Long outboundId;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    // 非数据库字段
    /**
     * 申请人姓名
     */
    @TableField(exist = false)
    @Schema(description = "申请人姓名")
    private String applicantName;

    /**
     * 申请人手机号
     */
    @TableField(exist = false)
    @Schema(description = "申请人手机号")
    private String applicantPhone;

    /**
     * 部门名称
     */
    @TableField(exist = false)
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 审批人姓名
     */
    @TableField(exist = false)
    @Schema(description = "审批人姓名")
    private String approverName;

    /**
     * 关联出库单号
     */
    @TableField(exist = false)
    @Schema(description = "关联出库单号")
    private String outboundNo;

    /**
     * 申请明细列表
     */
    @TableField(exist = false)
    @Schema(description = "申请明细列表")
    private List<ApplyDetail> details;
}
