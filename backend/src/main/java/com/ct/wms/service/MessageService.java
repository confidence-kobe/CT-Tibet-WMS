package com.ct.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.entity.Message;
import com.ct.wms.vo.MessageVO;

/**
 * 消息Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface MessageService {

    /**
     * 分页查询我的消息列表（带统计信息）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param type     消息类型（可选）
     * @param isRead   是否已读（可选）
     * @return 消息列表VO（包含统计信息）
     */
    MessageVO listMyMessagesWithStats(Integer pageNum, Integer pageSize, Integer type, Integer isRead);

    /**
     * 分页查询我的消息列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param isRead   是否已读（可选）
     * @return 分页结果
     */
    Page<Message> listMyMessages(Integer pageNum, Integer pageSize, Integer isRead);

    /**
     * 标记消息为已读
     *
     * @param id 消息ID
     */
    void markAsRead(Long id);

    /**
     * 批量标记消息为已读
     *
     * @param ids 消息ID列表
     */
    void batchMarkAsRead(java.util.List<Long> ids);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead();

    /**
     * 获取未读消息数量
     *
     * @return 未读消息数量
     */
    Long getUnreadCount();

    /**
     * 发送消息
     *
     * @param receiverId   接收人ID
     * @param messageType  消息类型
     * @param title        消息标题
     * @param content      消息内容
     * @param relatedId    关联单据ID
     * @param relatedType  关联类型
     */
    void sendMessage(Long receiverId, Integer messageType, String title, String content,
                     Long relatedId, Integer relatedType);

    /**
     * 删除消息（软删除）
     *
     * @param id 消息ID
     */
    void deleteMessage(Long id);
}
