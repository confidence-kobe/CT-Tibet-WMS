package com.ct.wms.service;

import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Outbound;

import java.math.BigDecimal;

/**
 * 通知服务：业务事件发生时给相关人员发送站内消息
 * <p>
 * 站内消息直接写入 tb_message（不依赖 RabbitMQ），并在业务事务提交成功后才写入，
 * 避免业务回滚但消息已发出。所有方法都不会抛出异常，通知失败不影响业务。
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface NotificationService {

    /**
     * 员工提交申请：通知可审批的人（仓库管理员、本部门的部门管理员）
     */
    void notifyApplySubmit(Apply apply);

    /**
     * 申请审批通过：通知申请人去领取
     */
    void notifyApplyApproved(Apply apply);

    /**
     * 申请被拒绝：通知申请人拒绝原因
     */
    void notifyApplyRejected(Apply apply);

    /**
     * 申请超过24小时未审批：提醒可审批的人
     */
    void notifyApplyTimeoutReminder(Apply apply);

    /**
     * 申请超过7天未审批被系统取消：通知申请人
     */
    void notifyApplyTimeoutCancelled(Apply apply);

    /**
     * 出库单待领取：通知领用人
     */
    void notifyOutboundPending(Outbound outbound);

    /**
     * 出库单即将超时：提醒领用人尽快领取
     *
     * @param daysRemaining 剩余天数
     */
    void notifyPickupReminder(Outbound outbound, long daysRemaining);

    /**
     * 出库单已领取：通知领用人
     */
    void notifyOutboundCompleted(Outbound outbound);

    /**
     * 出库单被取消（仓管取消或超时自动取消）：通知领用人
     *
     * @param reason 取消原因
     */
    void notifyOutboundCancelled(Outbound outbound, String reason);

    /**
     * 库存低于最低库存：通知仓库管理员和本部门的部门管理员
     */
    void notifyLowStockAlert(Long warehouseId, Long materialId, String materialName,
                             BigDecimal currentStock, BigDecimal minStock);
}
