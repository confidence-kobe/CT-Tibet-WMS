package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 作废入库单DTO
 *
 * @author CT Development Team
 * @since 2026-11-12
 */
@Data
@Schema(description = "作废入库单DTO")
public class InboundVoidDTO {

    @NotBlank(message = "作废原因不能为空")
    @Size(max = 100, message = "作废原因不能超过100字")
    @Schema(description = "作废原因", required = true)
    private String reason;
}
