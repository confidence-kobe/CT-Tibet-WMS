package com.ct.wms.vo;

import com.ct.wms.entity.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息列表响应VO
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息列表响应")
public class MessageVO {

    @Schema(description = "消息列表")
    private List<Message> list;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "统计信息")
    private MessageStats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "消息统计信息")
    public static class MessageStats {

        @Schema(description = "总消息数")
        private Long total;

        @Schema(description = "未读消息数")
        private Long unread;

        @Schema(description = "已读消息数")
        private Long read;
    }
}
