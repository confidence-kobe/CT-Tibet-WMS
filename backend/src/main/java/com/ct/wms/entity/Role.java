package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_role")
@Schema(description = "角色")
public class Role extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色编码（唯一标识）
     */
    @Schema(description = "角色编码")
    private String roleCode;

    /**
     * 角色级别（0-系统管理员 1-部门管理员 2-仓库管理员 3-普通员工）
     */
    @Schema(description = "角色级别")
    private Integer roleLevel;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

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
}
