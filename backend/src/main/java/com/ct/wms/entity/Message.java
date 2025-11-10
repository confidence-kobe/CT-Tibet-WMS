package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ct.wms.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息实体类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_message")
@Schema(description = "消息")
public class Message extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 接收人ID
     */
    @Schema(description = "接收人ID")
    private Long userId;

    /**
     * 消息标题
     */
    @Schema(description = "消息标题")
    private String title;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 消息类型（1-系统通知 2-申请通知 3-审批通知 4-库存预警）
     */
    @Schema(description = "消息类型")
    private MessageType type;

    /**
     * 关联业务ID（申请单ID、出库单ID等）
     */
    @Schema(description = "关联业务ID")
    private Long relatedId;

    /**
     * 关联业务类型（1-申请单 2-出库单）
     */
    @Schema(description = "关联业务类型")
    private Integer relatedType;

    /**
     * 是否已读（0-未读 1-已读）
     */
    @Schema(description = "是否已读")
    private Integer isRead;
}
