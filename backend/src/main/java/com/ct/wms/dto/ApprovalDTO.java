package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 审批DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "审批DTO")
public class ApprovalDTO {

    @NotNull(message = "申请单ID不能为空")
    @Schema(description = "申请单ID")
    private Long applyId;

    @NotNull(message = "审批结果不能为空")
    @Schema(description = "审批结果: 1-通过 2-拒绝")
    private Integer approvalResult;

    @Size(max = 500, message = "审批意见不能超过500字")
    @Schema(description = "审批意见")
    private String approvalRemark;
}
