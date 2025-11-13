package com.ct.wms.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.common.enums.MessageType;
import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Outbound;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.mq.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库超时定时任务
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundTimeoutTask {

    private final OutboundMapper outboundMapper;
    private final ApplyMapper applyMapper;
    private final NotificationProducer notificationProducer;

    /**
     * 定时检查并取消超时未取货的出库单
     * <p>
     * 执行时间: 每天凌晨2点执行
     * 业务规则: 待取货状态超过7天自动取消
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cancelTimeoutOutbound() {
        log.info("==================== 开始执行出库超时检查任务 ====================");

        try {
            // 计算7天前的时间
            LocalDateTime timeoutTime = LocalDateTime.now().minusDays(7);

            // 查询所有待取货且超时的出库单
            LambdaQueryWrapper<Outbound> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Outbound::getStatus, OutboundStatus.PENDING.getValue())
                    .eq(Outbound::getSource, OutboundSource.FROM_APPLY.getValue())
                    .le(Outbound::getOutboundTime, timeoutTime);

            List<Outbound> timeoutOutbounds = outboundMapper.selectList(queryWrapper);

            if (timeoutOutbounds.isEmpty()) {
                log.info("没有发现超时的出库单");
                return;
            }

            log.info("发现 {} 条超时的出库单，开始处理", timeoutOutbounds.size());

            int successCount = 0;
            int failCount = 0;

            for (Outbound outbound : timeoutOutbounds) {
                try {
                    // 1. 更新出库单状态为已取消
                    LambdaUpdateWrapper<Outbound> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(Outbound::getId, outbound.getId())
                            .eq(Outbound::getStatus, OutboundStatus.PENDING.getValue())
                            .set(Outbound::getStatus, OutboundStatus.CANCELED.getValue())
                            .set(Outbound::getRemark, "系统自动取消：超过7天未取货");

                    int updated = outboundMapper.update(null, updateWrapper);

                    if (updated > 0) {
                        // 2. 更新关联申请单状态为已取消
                        if (outbound.getRelatedApplyId() != null) {
                            LambdaUpdateWrapper<Apply> applyUpdateWrapper = new LambdaUpdateWrapper<>();
                            applyUpdateWrapper.eq(Apply::getId, outbound.getRelatedApplyId())
                                    .eq(Apply::getStatus, ApplyStatus.APPROVED.getValue())
                                    .set(Apply::getStatus, ApplyStatus.CANCELED.getValue())
                                    .set(Apply::getRemark, "系统自动取消：超过7天未取货");

                            applyMapper.update(null, applyUpdateWrapper);
                        }

                        // 3. 发送通知消息
                        NotificationMessageDTO notification = NotificationMessageDTO.builder()
                                .receiverId(outbound.getReceiverId())
                                .messageType(MessageType.TIMEOUT_CANCEL.getValue())
                                .title("出库单超时取消通知")
                                .content(String.format("您的出库单 %s 因超过7天未取货已被系统自动取消，请及时关注。",
                                        outbound.getOutboundNo()))
                                .relatedId(outbound.getId())
                                .relatedType(2) // 2-出库
                                .sendWechat(true)
                                .build();

                        notificationProducer.sendNotification(notification);

                        successCount++;
                        log.info("成功取消出库单: id={}, outboundNo={}", outbound.getId(), outbound.getOutboundNo());
                    }

                } catch (Exception e) {
                    failCount++;
                    log.error("取消出库单失败: id={}, outboundNo={}", outbound.getId(), outbound.getOutboundNo(), e);
                }
            }

            log.info("出库超时检查任务完成: 总数={}, 成功={}, 失败={}",
                    timeoutOutbounds.size(), successCount, failCount);

        } catch (Exception e) {
            log.error("执行出库超时检查任务失败", e);
        }

        log.info("==================== 出库超时检查任务结束 ====================");
    }

    /**
     * 定时发送出库待取货提醒
     * <p>
     * 执行时间: 每天上午10点执行
     * 业务规则: 待取货状态超过5天发送提醒
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void remindPendingOutbound() {
        log.info("==================== 开始执行出库待取货提醒任务 ====================");

        try {
            // 计算5天前的时间
            LocalDateTime reminderTime = LocalDateTime.now().minusDays(5);
            LocalDateTime timeoutTime = LocalDateTime.now().minusDays(7);

            // 查询待取货超过5天但未超过7天的出库单
            LambdaQueryWrapper<Outbound> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Outbound::getStatus, OutboundStatus.PENDING.getValue())
                    .eq(Outbound::getSource, OutboundSource.FROM_APPLY.getValue())
                    .le(Outbound::getOutboundTime, reminderTime)
                    .gt(Outbound::getOutboundTime, timeoutTime);

            List<Outbound> pendingOutbounds = outboundMapper.selectList(queryWrapper);

            if (pendingOutbounds.isEmpty()) {
                log.info("没有需要提醒的出库单");
                return;
            }

            log.info("发现 {} 条需要提醒的出库单", pendingOutbounds.size());

            int successCount = 0;

            for (Outbound outbound : pendingOutbounds) {
                try {
                    // 计算剩余天数
                    long daysPassed = java.time.Duration.between(
                            outbound.getOutboundTime(),
                            LocalDateTime.now()
                    ).toDays();
                    long daysRemaining = 7 - daysPassed;

                    // 发送提醒消息
                    NotificationMessageDTO notification = NotificationMessageDTO.builder()
                            .receiverId(outbound.getReceiverId())
                            .messageType(MessageType.OUTBOUND_PENDING.getValue())
                            .title("出库单待取货提醒")
                            .content(String.format("您的出库单 %s 已审批通过，请在 %d 天内取货，否则将被系统自动取消。",
                                    outbound.getOutboundNo(), daysRemaining))
                            .relatedId(outbound.getId())
                            .relatedType(2) // 2-出库
                            .sendWechat(true)
                            .build();

                    notificationProducer.sendNotification(notification);

                    successCount++;
                    log.info("发送出库提醒成功: id={}, outboundNo={}, daysRemaining={}",
                            outbound.getId(), outbound.getOutboundNo(), daysRemaining);

                } catch (Exception e) {
                    log.error("发送出库提醒失败: id={}, outboundNo={}",
                            outbound.getId(), outbound.getOutboundNo(), e);
                }
            }

            log.info("出库待取货提醒任务完成: 总数={}, 成功={}", pendingOutbounds.size(), successCount);

        } catch (Exception e) {
            log.error("执行出库待取货提醒任务失败", e);
        }

        log.info("==================== 出库待取货提醒任务结束 ====================");
    }
}
