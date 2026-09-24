package com.ct.wms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户选项（用于选择领用人等下拉场景，只包含必要字段）
 *
 * @author CT Development Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户选项")
public class UserOptionVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "部门名称")
    private String deptName;
}
