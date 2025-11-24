package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.common.enums.MessageType;
import com.ct.wms.common.exception.BusinessException;
import com.ct.wms.entity.Message;
import com.ct.wms.mapper.MessageMapper;
import com.ct.wms.util.TestDataBuilder;
import com.ct.wms.vo.MessageVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MessageServiceImpl单元测试
 *
 * @author CT Development Team
 * @since 2025-11-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("消息服务测试")
class MessageServiceImplTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private static final Long TEST_USER_ID = 100L;
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        // Mock SecurityContext，模拟当前登录用户
        TestDataBuilder.mockSecurityContext(TEST_USER_ID, TEST_USERNAME, "USER");
    }

    @AfterEach
    void tearDown() {
        // 清理SecurityContext
        TestDataBuilder.clearSecurityContext();
    }

    @Test
    @DisplayName("测试查询消息列表带统计信息")
    void testListMyMessagesWithStats() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;

        List<Message> mockMessages = Arrays.asList(
            TestDataBuilder.createMessage(1L, TEST_USER_ID, MessageType.APPLY_APPROVED,
                "申请已通过", "您的申请已审批通过", 0),
            TestDataBuilder.createMessage(2L, TEST_USER_ID, MessageType.APPLY_REJECTED,
                "申请已拒绝", "您的申请已被拒绝", 1),
            TestDataBuilder.createMessage(3L, TEST_USER_ID, MessageType.SYSTEM,
                "系统通知", "系统维护通知", 0)
        );

        Page<Message> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(mockMessages);
        mockPage.setTotal(3L);

        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);
        when(messageMapper.selectCount(any(LambdaQueryWrapper.class)))
            .thenReturn(10L) // total
            .thenReturn(4L); // unread

        // When
        MessageVO result = messageService.listMyMessagesWithStats(pageNum, pageSize, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(3);
        assertThat(result.getTotal()).isEqualTo(3L);
        assertThat(result.getStats()).isNotNull();
        assertThat(result.getStats().getTotal()).isEqualTo(10L);
        assertThat(result.getStats().getUnread()).isEqualTo(4L);
        assertThat(result.getStats().getRead()).isEqualTo(6L);

        verify(messageMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(messageMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试按消息类型筛选")
    void testListMyMessagesWithStats_FilterByType() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        Integer type = MessageType.APPLY_APPROVED.getCode();

        List<Message> mockMessages = Collections.singletonList(
            TestDataBuilder.createMessage(1L, TEST_USER_ID, MessageType.APPLY_APPROVED,
                "申请已通过", "您的申请已审批通过", 0)
        );

        Page<Message> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(mockMessages);
        mockPage.setTotal(1L);

        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);
        when(messageMapper.selectCount(any(LambdaQueryWrapper.class)))
            .thenReturn(5L)
            .thenReturn(2L);

        // When
        MessageVO result = messageService.listMyMessagesWithStats(pageNum, pageSize, type, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getType()).isEqualTo(MessageType.APPLY_APPROVED);

        verify(messageMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试按已读状态筛选")
    void testListMyMessagesWithStats_FilterByReadStatus() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        Integer isRead = 0; // 未读

        List<Message> mockMessages = Arrays.asList(
            TestDataBuilder.createMessage(1L, TEST_USER_ID, MessageType.SYSTEM,
                "通知1", "内容1", 0),
            TestDataBuilder.createMessage(2L, TEST_USER_ID, MessageType.SYSTEM,
                "通知2", "内容2", 0)
        );

        Page<Message> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(mockMessages);
        mockPage.setTotal(2L);

        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);
        when(messageMapper.selectCount(any(LambdaQueryWrapper.class)))
            .thenReturn(5L)
            .thenReturn(2L);

        // When
        MessageVO result = messageService.listMyMessagesWithStats(pageNum, pageSize, null, isRead);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(2);
        assertThat(result.getList()).allMatch(msg -> msg.getIsRead() == 0);

        verify(messageMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试标记单条消息已读 - 成功")
    void testMarkAsRead_Success() {
        // Given
        Long messageId = 1L;
        Message mockMessage = TestDataBuilder.createMessage(messageId, TEST_USER_ID,
            MessageType.SYSTEM, "通知", "内容", 0);

        when(messageMapper.selectById(messageId)).thenReturn(mockMessage);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        // When
        messageService.markAsRead(messageId);

        // Then
        verify(messageMapper, times(1)).selectById(messageId);
        verify(messageMapper, times(1)).updateById(any(Message.class));
        assertThat(mockMessage.getIsRead()).isEqualTo(1);
    }

    @Test
    @DisplayName("测试标记单条消息已读 - 消息不存在")
    void testMarkAsRead_MessageNotFound() {
        // Given
        Long messageId = 999L;
        when(messageMapper.selectById(messageId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> messageService.markAsRead(messageId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("消息不存在");

        verify(messageMapper, times(1)).selectById(messageId);
        verify(messageMapper, never()).updateById(any(Message.class));
    }

    @Test
    @DisplayName("测试标记单条消息已读 - 无权操作")
    void testMarkAsRead_PermissionDenied() {
        // Given
        Long messageId = 1L;
        Long otherUserId = 200L; // 其他用户的消息
        Message mockMessage = TestDataBuilder.createMessage(messageId, otherUserId,
            MessageType.SYSTEM, "通知", "内容", 0);

        when(messageMapper.selectById(messageId)).thenReturn(mockMessage);

        // When & Then
        assertThatThrownBy(() -> messageService.markAsRead(messageId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("无权操作此消息");

        verify(messageMapper, times(1)).selectById(messageId);
        verify(messageMapper, never()).updateById(any(Message.class));
    }

    @Test
    @DisplayName("测试批量标记消息已读")
    void testBatchMarkAsRead() {
        // Given
        List<Long> messageIds = Arrays.asList(1L, 2L, 3L);

        Message message1 = TestDataBuilder.createMessage(1L, TEST_USER_ID,
            MessageType.SYSTEM, "通知1", "内容1", 0);
        Message message2 = TestDataBuilder.createMessage(2L, TEST_USER_ID,
            MessageType.SYSTEM, "通知2", "内容2", 0);
        Message message3 = TestDataBuilder.createMessage(3L, 200L, // 其他用户的消息
            MessageType.SYSTEM, "通知3", "内容3", 0);

        when(messageMapper.selectById(1L)).thenReturn(message1);
        when(messageMapper.selectById(2L)).thenReturn(message2);
        when(messageMapper.selectById(3L)).thenReturn(message3);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        // When
        messageService.batchMarkAsRead(messageIds);

        // Then
        verify(messageMapper, times(3)).selectById(any(Long.class));
        // 只会更新属于当前用户的2条消息
        verify(messageMapper, times(2)).updateById(any(Message.class));
    }

    @Test
    @DisplayName("测试标记所有消息已读")
    void testMarkAllAsRead() {
        // Given
        List<Message> unreadMessages = Arrays.asList(
            TestDataBuilder.createMessage(1L, TEST_USER_ID, MessageType.SYSTEM,
                "通知1", "内容1", 0),
            TestDataBuilder.createMessage(2L, TEST_USER_ID, MessageType.APPLY_APPROVED,
                "通知2", "内容2", 0),
            TestDataBuilder.createMessage(3L, TEST_USER_ID, MessageType.APPLY_REJECTED,
                "通知3", "内容3", 0)
        );

        when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(unreadMessages);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        // When
        messageService.markAllAsRead();

        // Then
        verify(messageMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(messageMapper, times(3)).updateById(any(Message.class));
        assertThat(unreadMessages).allMatch(msg -> msg.getIsRead() == 1);
    }

    @Test
    @DisplayName("测试标记所有消息已读 - 无未读消息")
    void testMarkAllAsRead_NoUnreadMessages() {
        // Given
        when(messageMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());

        // When
        messageService.markAllAsRead();

        // Then
        verify(messageMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(messageMapper, never()).updateById(any(Message.class));
    }

    @Test
    @DisplayName("测试获取未读消息数量")
    void testGetUnreadCount() {
        // Given
        when(messageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // When
        Long unreadCount = messageService.getUnreadCount();

        // Then
        assertThat(unreadCount).isEqualTo(5L);
        verify(messageMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试发送消息")
    void testSendMessage() {
        // Given
        Long receiverId = 200L;
        Integer messageType = MessageType.SYSTEM.getCode();
        String title = "测试消息";
        String content = "这是一条测试消息";
        Long relatedId = 123L;
        Integer relatedType = 1;

        when(messageMapper.insert(any(Message.class))).thenReturn(1);

        // When
        messageService.sendMessage(receiverId, messageType, title, content, relatedId, relatedType);

        // Then
        verify(messageMapper, times(1)).insert(any(Message.class));
    }

    @Test
    @DisplayName("测试删除消息 - 成功")
    void testDeleteMessage_Success() {
        // Given
        Long messageId = 1L;
        Message mockMessage = TestDataBuilder.createMessage(messageId, TEST_USER_ID,
            MessageType.SYSTEM, "通知", "内容", 1);

        when(messageMapper.selectById(messageId)).thenReturn(mockMessage);
        when(messageMapper.deleteById(messageId)).thenReturn(1);

        // When
        messageService.deleteMessage(messageId);

        // Then
        verify(messageMapper, times(1)).selectById(messageId);
        verify(messageMapper, times(1)).deleteById(messageId);
    }

    @Test
    @DisplayName("测试删除消息 - 消息不存在")
    void testDeleteMessage_MessageNotFound() {
        // Given
        Long messageId = 999L;
        when(messageMapper.selectById(messageId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> messageService.deleteMessage(messageId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("消息不存在");

        verify(messageMapper, times(1)).selectById(messageId);
        verify(messageMapper, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("测试删除消息 - 只能删除自己的消息")
    void testDeleteMessage_OnlyOwner() {
        // Given
        Long messageId = 1L;
        Long otherUserId = 200L;
        Message mockMessage = TestDataBuilder.createMessage(messageId, otherUserId,
            MessageType.SYSTEM, "通知", "内容", 1);

        when(messageMapper.selectById(messageId)).thenReturn(mockMessage);

        // When & Then
        assertThatThrownBy(() -> messageService.deleteMessage(messageId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("无权删除此消息");

        verify(messageMapper, times(1)).selectById(messageId);
        verify(messageMapper, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("测试查询消息列表 - 不带统计信息")
    void testListMyMessages() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        Integer isRead = null;

        List<Message> mockMessages = Arrays.asList(
            TestDataBuilder.createMessage(1L, TEST_USER_ID, MessageType.SYSTEM,
                "通知1", "内容1", 0),
            TestDataBuilder.createMessage(2L, TEST_USER_ID, MessageType.SYSTEM,
                "通知2", "内容2", 1)
        );

        Page<Message> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(mockMessages);
        mockPage.setTotal(2L);

        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        Page<Message> result = messageService.listMyMessages(pageNum, pageSize, isRead);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getTotal()).isEqualTo(2L);

        verify(messageMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }
}
