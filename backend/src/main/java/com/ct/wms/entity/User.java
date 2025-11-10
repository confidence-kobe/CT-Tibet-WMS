package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ct.wms.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_user")
@Schema(description = "用户")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（登录名，全局唯一）
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码（加密存储）
     */
    @JsonIgnore
    @Schema(description = "密码", hidden = true)
    private String password;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 手机号（全局唯一）
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    private Long roleId;

    /**
     * 状态（0-启用 1-禁用）
     */
    @Schema(description = "状态")
    private UserStatus status;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 微信openid（小程序登录）
     */
    @Schema(description = "微信openid")
    private String wechatOpenid;

    /**
     * 微信昵称
     */
    @Schema(description = "微信昵称")
    private String wechatNickname;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP")
    private String lastLoginIp;

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
     * 角色名称
     */
    @TableField(exist = false)
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 角色编码
     */
    @TableField(exist = false)
    @Schema(description = "角色编码")
    private String roleCode;
}
