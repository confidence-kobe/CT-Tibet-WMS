package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 申请明细实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_apply_detail")
@Schema(description = "申请明细")
public class ApplyDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 申请单ID
     */
    @Schema(description = "申请单ID")
    private Long applyId;

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
     * 申请数量
     */
    @Schema(description = "申请数量")
    private BigDecimal quantity;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    // 非数据库字段
    /**
     * 当前库存
     */
    @TableField(exist = false)
    @Schema(description = "当前库存")
    private BigDecimal currentStock;

    /**
     * 库存是否充足
     */
    @TableField(exist = false)
    @Schema(description = "库存是否充足")
    private Boolean isStockSufficient;
}
