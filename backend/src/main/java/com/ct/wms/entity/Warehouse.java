package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 仓库实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_warehouse")
@Schema(description = "仓库")
public class Warehouse extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 仓库名称
     */
    @Schema(description = "仓库名称")
    private String warehouseName;

    /**
     * 仓库编码
     */
    @Schema(description = "仓库编码")
    private String warehouseCode;

    /**
     * 所属部门ID
     */
    @Schema(description = "所属部门ID")
    private Long deptId;

    /**
     * 仓库地址
     */
    @Schema(description = "仓库地址")
    private String address;

    /**
     * 仓库管理员ID
     */
    @Schema(description = "仓库管理员ID")
    private Long managerId;

    /**
     * 仓库容量（平方米）
     */
    @Schema(description = "仓库容量")
    private BigDecimal capacity;

    /**
     * 状态（0-启用 1-禁用）
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    // 非数据库字段
    /**
     * 部门名称
     */
    @TableField(exist = false)
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 管理员姓名
     */
    @TableField(exist = false)
    @Schema(description = "管理员姓名")
    private String managerName;
}
