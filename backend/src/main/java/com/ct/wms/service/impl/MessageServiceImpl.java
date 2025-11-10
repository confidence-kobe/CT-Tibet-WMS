package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.entity.Message;
import com.ct.wms.mapper.MessageMapper;
import com.ct.wms.security.UserDetailsImpl;
import com.ct.wms.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 消息Service实现类
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public Page<Message> listMyMessages(Integer pageNum, Integer pageSize, Integer isRead) {
        Long userId = getCurrentUserId();

        Page<Message> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);

        if (isRead != null) {
            wrapper.eq(Message::getIsRead, isRead);
        }

        wrapper.orderByDesc(Message::getCreateTime);

        return messageMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id) {
        Long userId = getCurrentUserId();

        // 查询消息
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }

        // 检查权限：只能标记自己的消息
        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException(403, "无权操作此消息");
        }

        // 标记为已读
        message.setIsRead(1);
        messageMapper.updateById(message);

        log.info("标记消息为已读: id={}, userId={}", id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(List<Long> ids) {
        Long userId = getCurrentUserId();

        for (Long id : ids) {
            Message message = messageMapper.selectById(id);
            if (message != null && message.getReceiverId().equals(userId)) {
                message.setIsRead(1);
                messageMapper.updateById(message);
            }
        }

        log.info("批量标记消息为已读: count={}, userId={}", ids.size(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        Long userId = getCurrentUserId();

        // 查询所有未读消息
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        wrapper.eq(Message::getIsRead, 0);

        List<Message> unreadMessages = messageMapper.selectList(wrapper);

        for (Message message : unreadMessages) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }

        log.info("标记所有消息为已读: count={}, userId={}", unreadMessages.size(), userId);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        wrapper.eq(Message::getIsRead, 0);

        return messageMapper.selectCount(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Long receiverId, Integer messageType, String title, String content,
                           Long relatedId, Integer relatedType) {
        Message message = new Message();
        message.setReceiverId(receiverId);
        message.setMessageType(messageType);
        message.setTitle(title);
        message.setContent(content);
        message.setRelatedId(relatedId);
        message.setRelatedType(relatedType);
        message.setIsRead(0);

        messageMapper.insert(message);

        log.info("发送消息: receiverId={}, title={}", receiverId, title);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long id) {
        Long userId = getCurrentUserId();

        // 查询消息
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }

        // 检查权限：只能删除自己的消息
        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException(403, "无权删除此消息");
        }

        // 软删除
        messageMapper.deleteById(id);

        log.info("删除消息: id={}, userId={}", id, userId);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        }
        throw new BusinessException(401, "未登录");
    }
}
