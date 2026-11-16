package com.ct.wms.event;

import com.ct.wms.dto.NotificationMessageDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务通知事件
 * <p>
 * 业务服务在事务内发布此事件，由 {@link NotificationEventListener} 在事务提交后发送通知，
 * 避免事务回滚后通知已发出的不一致问题。
 *
 * @author CT Development Team
 * @since 2026-08-04
 */
@Getter
@AllArgsConstructor
public class NotificationEvent {

    /**
     * 通知消息内容
     */
    private final NotificationMessageDTO message;
}
