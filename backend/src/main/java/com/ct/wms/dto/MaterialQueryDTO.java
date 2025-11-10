package com.ct.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物资查询DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Schema(description = "物资查询条件")
public class MaterialQueryDTO {

    @Schema(description = "物资类别")
    private String category;

    @Schema(description = "状态(0-启用 1-停用)")
    private Integer status;

    @Schema(description = "关键词(名称/编码)")
    private String keyword;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer pageSize = 20;
}
