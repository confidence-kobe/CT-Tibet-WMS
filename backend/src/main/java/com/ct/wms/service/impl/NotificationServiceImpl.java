package com.ct.wms.service.impl;

import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Outbound;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.common.enums.MessageType;
import com.ct.wms.mapper.WarehouseMapper;
import com.ct.wms.mq.NotificationProducer;
import com.ct.wms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 通知服务实现类
 *
 * 说明：NotificationProducer 为可选依赖，如果 RabbitMQ 未启用，将降级为仅记录日志
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    // 可选依赖：如果 RabbitMQ 未启用，此字段为 null
    @Autowired(required = false)
    private NotificationProducer notificationProducer;

    private final WarehouseMapper warehouseMapper;

    @Async
    @Override
    public void notifyApplySubmit(Apply apply) {
        try {
            log.info("发送申请提交通知: applyNo={}, approverId={}", apply.getApplyNo(), apply.getApproverId());

            // 如果 RabbitMQ 未启用，降级为仅记录日志
            if (notificationProducer == null) {
                log.warn("RabbitMQ 未启用，跳过消息队列通知（降级模式）");
                return;
            }

            NotificationMessageDTO notification = NotificationMessageDTO.builder()
                    .receiverId(apply.getApproverId())
                    .messageType(MessageType.APPLY_SUBMIT.getValue())
                    .title("新的物资申请待审批")
                    .content(String.format("%s 提交了物资申请 %s，请及时审批。",
                            apply.getApplicantName(), apply.getApplyNo()))
                    .relatedId(apply.getId())
                    .relatedType(3) // 3-申请
                    .sendWechat(true)
                    .build();

            notificationProducer.sendNotification(notification);

        } catch (Exception e) {
            log.error("发送申请提交通知失败", e);
        }
    }

    @Async
    @Override
    public void notifyApplyApproved(Apply apply) {
        try {
            log.info("发送申请审批通过通知: applyNo={}, applicantId={}", apply.getApplyNo(), apply.getApplicantId());

            if (notificationProducer == null) {
                log.warn("RabbitMQ 未启用，跳过消息队列通知（降级模式）");
                return;
            }

            NotificationMessageDTO notification = NotificationMessageDTO.builder()
                    .receiverId(apply.getApplicantId())
                    .messageType(MessageType.APPLY_APPROVED.getValue())
                    .title("物资申请已通过")
                    .content(String.format("您的物资申请 %s 已审批通过，请在7天内到仓库取货。",
                            apply.getApplyNo()))
                    .relatedId(apply.getId())
                    .relatedType(3) // 3-申请
                    .sendWechat(true)
                    .build();

            notificationProducer.sendNotification(notification);

        } catch (Exception e) {
            log.error("发送申请审批通过通知失败", e);
        }
    }

    @Async
    @Override
    public void notifyApplyRejected(Apply apply) {
        try {
            log.info("发送申请审批拒绝通知: applyNo={}, applicantId={}", apply.getApplyNo(), apply.getApplicantId());

            if (notificationProducer == null) {
                log.warn("RabbitMQ 未启用，跳过消息队列通知（降级模式）");
                return;
            }

            NotificationMessageDTO notification = NotificationMessageDTO.builder()
                    .receiverId(apply.getApplicantId())
                    .messageType(MessageType.APPLY_REJECTED.getValue())
                    .title("物资申请已拒绝")
                    .content(String.format("您的物资申请 %s 未通过审批。拒绝原因: %s",
                            apply.getApplyNo(), apply.getApprovalRemark()))
                    .relatedId(apply.getId())
                    .relatedType(3) // 3-申请
                    .sendWechat(true)
                    .build();

            notificationProducer.sendNotification(notification);

        } catch (Exception e) {
            log.error("发送申请审批拒绝通知失败", e);
        }
    }

    @Async
    @Override
    public void notifyOutboundPending(Outbound outbound) {
        try {
            log.info("发送出库待取货通知: outboundNo={}, receiverId={}", outbound.getOutboundNo(), outbound.getReceiverId());

            if (notificationProducer == null) {
                log.warn("RabbitMQ 未启用，跳过消息队列通知（降级模式）");
                return;
            }

            NotificationMessageDTO notification = NotificationMessageDTO.builder()
                    .receiverId(outbound.getReceiverId())
                    .messageType(MessageType.OUTBOUND_PENDING.getValue())
                    .title("物资已备货，请及时取货")
                    .content(String.format("您的出库单 %s 已备货完成，请在7天内到仓库取货。",
                            outbound.getOutboundNo()))
                    .relatedId(outbound.getId())
                    .relatedType(2) // 2-出库
                    .sendWechat(true)
                    .build();

            notificationProducer.sendNotification(notification);

        } catch (Exception e) {
            log.error("发送出库待取货通知失败", e);
        }
    }

    @Async
    @Override
    public void notifyOutboundCompleted(Outbound outbound) {
        try {
            log.info("发送出库完成通知: outboundNo={}, receiverId={}", outbound.getOutboundNo(), outbound.getReceiverId());

            if (notificationProducer == null) {
                log.warn("RabbitMQ 未启用，跳过消息队列通知（降级模式）");
                return;
            }

            NotificationMessageDTO notification = NotificationMessageDTO.builder()
                    .receiverId(outbound.getReceiverId())
                    .messageType(MessageType.OUTBOUND_PENDING.getValue())
                    .title("物资已出库")
                    .content(String.format("您的出库单 %s 已完成，感谢您的配合。",
                            outbound.getOutboundNo()))
                    .relatedId(outbound.getId())
                    .relatedType(2) // 2-出库
                    .sendWechat(false)
                    .build();

            notificationProducer.sendNotification(notification);

        } catch (Exception e) {
            log.error("发送出库完成通知失败", e);
        }
    }

    @Async
    @Override
    public void notifyLowStockAlert(Long warehouseId, Long materialId, String materialName,
                                     BigDecimal currentStock, BigDecimal minStock) {
        try {
            log.info("发送库存预警通知: warehouseId={}, materialId={}, materialName={}",
                    warehouseId, materialId, materialName);

            if (notificationProducer == null) {
                log.warn("RabbitMQ 未启用，跳过消息队列通知（降级模式）");
                return;
            }

            // 获取仓库信息
            Warehouse warehouse = warehouseMapper.selectById(warehouseId);
            if (warehouse == null) {
                log.warn("仓库不存在: warehouseId={}", warehouseId);
                return;
            }

            // 发送给仓库管理员
            if (warehouse.getManagerId() != null) {
                NotificationMessageDTO notification = NotificationMessageDTO.builder()
                        .receiverId(warehouse.getManagerId())
                        .messageType(MessageType.STOCK_ALERT.getValue())
                        .title("库存预警通知")
                        .content(String.format("仓库 %s 的物资 %s 库存不足，当前库存: %s，最低库存: %s，请及时补货。",
                                warehouse.getWarehouseName(), materialName,
                                currentStock.stripTrailingZeros().toPlainString(),
                                minStock.stripTrailingZeros().toPlainString()))
                        .relatedId(materialId)
                        .relatedType(null)
                        .sendWechat(true)
                        .build();

                notificationProducer.sendNotification(notification);
            }

        } catch (Exception e) {
            log.error("发送库存预警通知失败", e);
        }
    }
}
