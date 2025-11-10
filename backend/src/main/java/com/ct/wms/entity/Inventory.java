package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_inventory")
@Schema(description = "库存")
public class Inventory extends BaseEntity {

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
     * 当前库存数量
     */
    @Schema(description = "当前库存数量")
    private BigDecimal quantity;

    /**
     * 锁定数量（预留功能）
     */
    @Schema(description = "锁定数量")
    private BigDecimal lockedQuantity;

    /**
     * 可用数量（quantity - lockedQuantity）
     */
    @Schema(description = "可用数量")
    private BigDecimal availableQuantity;

    /**
     * 最后入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后入库时间")
    private LocalDateTime lastInboundTime;

    /**
     * 最后出库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后出库时间")
    private LocalDateTime lastOutboundTime;

    /**
     * 乐观锁版本号（用于并发控制）
     */
    @Version
    @Schema(description = "版本号")
    private Integer version;

    // 非数据库字段
    /**
     * 仓库名称
     */
    @TableField(exist = false)
    @Schema(description = "仓库名称")
    private String warehouseName;

    /**
     * 物资名称
     */
    @TableField(exist = false)
    @Schema(description = "物资名称")
    private String materialName;

    /**
     * 物资编码
     */
    @TableField(exist = false)
    @Schema(description = "物资编码")
    private String materialCode;

    /**
     * 物资类别
     */
    @TableField(exist = false)
    @Schema(description = "物资类别")
    private String category;

    /**
     * 规格型号
     */
    @TableField(exist = false)
    @Schema(description = "规格型号")
    private String spec;

    /**
     * 单位
     */
    @TableField(exist = false)
    @Schema(description = "单位")
    private String unit;

    /**
     * 单价
     */
    @TableField(exist = false)
    @Schema(description = "单价")
    private BigDecimal price;

    /**
     * 最低库存预警阈值
     */
    @TableField(exist = false)
    @Schema(description = "最低库存预警阈值")
    private BigDecimal minStock;

    /**
     * 库存状态（0-正常 1-低库存 2-缺货）
     */
    @TableField(exist = false)
    @Schema(description = "库存状态")
    private Integer stockStatus;

    /**
     * 库存价值（quantity * price）
     */
    @TableField(exist = false)
    @Schema(description = "库存价值")
    private BigDecimal stockValue;
}
