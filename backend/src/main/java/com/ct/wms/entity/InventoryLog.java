package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库存流水实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_inventory_log")
@Schema(description = "库存流水")
public class InventoryLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 仓库ID
     */
    @Schema(description = "仓库ID")
    private Long warehouseId;

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
     * 变动类型（1-入库 2-出库）
     */
    @Schema(description = "变动类型")
    private Integer changeType;

    /**
     * 变动数量（正数为增加，负数为减少）
     */
    @Schema(description = "变动数量")
    private BigDecimal changeQuantity;

    /**
     * 变动前数量
     */
    @Schema(description = "变动前数量")
    private BigDecimal beforeQuantity;

    /**
     * 变动后数量
     */
    @Schema(description = "变动后数量")
    private BigDecimal afterQuantity;

    /**
     * 关联单号（入库单号或出库单号）
     */
    @Schema(description = "关联单号")
    private String relatedNo;

    /**
     * 关联单据类型（1-入库单 2-出库单）
     */
    @Schema(description = "关联单据类型")
    private Integer relatedType;

    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID")
    private Long operatorId;

    /**
     * 操作人姓名（冗余字段）
     */
    @Schema(description = "操作人姓名")
    private String operatorName;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    // 非数据库字段
    /**
     * 仓库名称（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "仓库名称")
    private String warehouseName;

    /**
     * 物资编码（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "物资编码")
    private String materialCode;

    /**
     * 规格型号（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "规格型号")
    private String spec;

    /**
     * 单位（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "单位")
    private String unit;

    /**
     * 关联单据ID（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "关联单据ID")
    private Long relatedId;

}
