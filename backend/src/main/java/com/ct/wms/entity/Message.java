package com.ct.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ct.wms.common.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

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

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    /**
     * 获取接收人ID（别名方法）
     * @return 用户ID
     */
    public Long getReceiverId() {
        return this.userId;
    }

    /**
     * 设置接收人ID（别名方法）
     * @param receiverId 接收人ID
     */
    public void setReceiverId(Long receiverId) {
        this.userId = receiverId;
    }

    /**
     * 获取消息类型（别名方法）
     * @return 消息类型的整数值
     */
    public Integer getMessageType() {
        return this.type != null ? this.type.getCode() : null;
    }

    /**
     * 设置消息类型（别名方法）
     * @param messageType 消息类型整数值
     */
    public void setMessageType(Integer messageType) {
        if (messageType != null) {
            for (MessageType mt : MessageType.values()) {
                if (mt.getCode().equals(messageType)) {
                    this.type = mt;
                    break;
                }
            }
        }
    }
}
