package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 出库明细实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_outbound_detail")
@Schema(description = "出库明细")
public class OutboundDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 出库单ID
     */
    @Schema(description = "出库单ID")
    private Long outboundId;

    /**
     * 物资ID
     */
    @Schema(description = "物资ID")
    private Long materialId;

    /**
     * 物资名称（冗余字段）
     */
    @Schema(description = "物资名称")
    private String materialName;

    /**
     * 物资编码（冗余字段）
     */
    @Schema(description = "物资编码")
    private String materialCode;

    /**
     * 规格型号（冗余字段）
     */
    @Schema(description = "规格型号")
    private String spec;

    /**
     * 单位（冗余字段）
     */
    @Schema(description = "单位")
    private String unit;

    /**
     * 出库数量
     */
    @Schema(description = "出库数量")
    private BigDecimal quantity;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    // 非数据库字段
    /**
     * 单价（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "单价")
    private BigDecimal unitPrice;

    /**
     * 金额（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "金额")
    private BigDecimal amount;

    /**
     * 当前库存
     */
    @TableField(exist = false)
    @Schema(description = "当前库存")
    private BigDecimal currentStock;
}
