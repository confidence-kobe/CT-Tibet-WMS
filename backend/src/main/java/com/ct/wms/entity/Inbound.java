package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ct.wms.enums.InboundType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_inbound")
@Schema(description = "入库单")
public class Inbound extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 入库单号（格式：RK_部门编码_YYYYMMDD_流水号）
     */
    @Schema(description = "入库单号")
    private String inboundNo;

    /**
     * 入库仓库ID
     */
    @Schema(description = "入库仓库ID")
    private Long warehouseId;

    /**
     * 入库类型（1-采购入库 2-退货入库 3-调拨入库 4-其他）
     */
    @Schema(description = "入库类型")
    private InboundType inboundType;

    /**
     * 操作人ID（仓管员）
     */
    @Schema(description = "操作人ID")
    private Long operatorId;

    /**
     * 入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "入库时间")
    private LocalDateTime inboundTime;

    /**
     * 入库总金额
     */
    @Schema(description = "入库总金额")
    private BigDecimal totalAmount;

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
     * 入库明细列表
     */
    @TableField(exist = false)
    @Schema(description = "入库明细列表")
    private List<InboundDetail> details;
}
