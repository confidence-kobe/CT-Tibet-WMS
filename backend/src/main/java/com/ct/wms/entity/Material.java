package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物资实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_material")
@Schema(description = "物资")
public class Material extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 物资名称
     */
    @Schema(description = "物资名称")
    private String materialName;

    /**
     * 物资编码（唯一）
     */
    @Schema(description = "物资编码")
    private String materialCode;

    /**
     * 物资类别
     */
    @Schema(description = "物资类别")
    private String category;

    /**
     * 规格型号
     */
    @Schema(description = "规格型号")
    private String spec;

    /**
     * 单位
     */
    @Schema(description = "单位")
    private String unit;

    /**
     * 单价
     */
    @Schema(description = "单价")
    private BigDecimal price;

    /**
     * 最低库存预警阈值
     */
    @Schema(description = "最低库存预警阈值")
    private BigDecimal minStock;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 图片URL
     */
    @Schema(description = "图片URL")
    private String image;

    /**
     * 状态（0-启用 1-停用）
     */
    @Schema(description = "状态")
    private Integer status;
}
