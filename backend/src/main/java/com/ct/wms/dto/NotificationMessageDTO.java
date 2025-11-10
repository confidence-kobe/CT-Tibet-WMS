package com.ct.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通知消息DTO
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接收人ID
     */
    private Long receiverId;

    /**
     * 消息类型: 1-申请提交 2-审批通过 3-审批拒绝 4-出库待取 5-库存预警 6-超时取消
     */
    private Integer messageType;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联单据ID
     */
    private Long relatedId;

    /**
     * 关联类型: 1-入库 2-出库 3-申请
     */
    private Integer relatedType;

    /**
     * 是否发送微信通知
     */
    private Boolean sendWechat;
}
