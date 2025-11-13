package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 部门实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_dept")
@Schema(description = "部门")
public class Dept extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 部门编码（用于单号生成）
     */
    @Schema(description = "部门编码")
    private String deptCode;

    /**
     * 上级部门ID（0表示顶级部门）
     */
    @Schema(description = "上级部门ID")
    private Long parentId;

    /**
     * 祖级列表（1,2,3,路径）
     */
    @Schema(description = "祖级列表")
    private String ancestors;

    /**
     * 部门负责人ID
     */
    @Schema(description = "部门负责人ID")
    private Long leaderId;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 状态（0-启用 1-禁用）
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sort;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    // 非数据库字段
    /**
     * 负责人姓名
     */
    @TableField(exist = false)
    @Schema(description = "负责人姓名")
    private String leaderName;

    /**
     * 子部门列表
     */
    @TableField(exist = false)
    @Schema(description = "子部门列表")
    private List<Dept> children;
}
