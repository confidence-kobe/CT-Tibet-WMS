package com.ct.wms.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.enums.MessageType;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.entity.Outbound;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.mq.NotificationProducer;
import com.ct.wms.service.OutboundService;
import com.ct.wms.utils.RedisLockUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库超时定时任务
 *
 * 说明：NotificationProducer 为可选依赖，如果 RabbitMQ 未启用，将跳过消息通知
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundTimeoutTask {

    private final OutboundMapper outboundMapper;
    private final OutboundService outboundService;

    // 可选依赖：如果 Redis 未启用（测试环境），此字段为 null
    @Autowired(required = false)
    private RedisLockUtils redisLockUtils;

    // 可选依赖：如果 RabbitMQ 未启用，此字段为 null
    @Autowired(required = false)
    private NotificationProducer notificationProducer;

    // 分布式锁KEY前缀
    private static final String LOCK_PREFIX = "wms:lock:outbound_timeout_task:";

    /**
     * 定时检查并取消超时未取货的出库单
     * <p>
     * 执行时间: 每天凌晨2点执行
     * 业务规则: 待取货状态超过7天自动取消
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cancelTimeoutOutbound() {
        // 测试环境下跳过分布式锁
        if (redisLockUtils == null) {
            executeCancelTimeoutOutbound();
            return;
        }

        String lockKey = LOCK_PREFIX + "cancel";
        String lockValue = redisLockUtils.tryLock(lockKey, 300);
        if (lockValue == null) {
            log.info("出库超时检查任务正在执行中，跳过本次调度");
            return;
        }

        try {
            executeCancelTimeoutOutbound();
        } finally {
            redisLockUtils.unlock(lockKey, lockValue);
        }
    }

    /**
     * 执行出库超时检查任务
     */
    private void executeCancelTimeoutOutbound() {
        try {
            log.info("==================== 开始执行出库超时检查任务 ====================");

            // 计算7天前的时间
            LocalDateTime timeoutTime = LocalDateTime.now().minusDays(7);

            // 查询所有待取货且超时的出库单
            LambdaQueryWrapper<Outbound> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Outbound::getStatus, OutboundStatus.PENDING_PICKUP)
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
                    // 在同一事务中取消出库单（释放锁定库存 + 更新出库单状态 + 更新关联申请单状态）
                    outboundService.cancelOutbound(outbound.getId(), "系统自动取消：超过7天未取货");

                    // 发送通知消息（如果 RabbitMQ 可用）
                    if (notificationProducer != null) {
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
                    }

                    successCount++;
                    log.info("成功取消出库单: id={}, outboundNo={}", outbound.getId(), outbound.getOutboundNo());

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
        // 测试环境下跳过分布式锁
        if (redisLockUtils == null) {
            executeRemindPendingOutbound();
            return;
        }

        String lockKey = LOCK_PREFIX + "remind";
        String lockValue = redisLockUtils.tryLock(lockKey, 60);
        if (lockValue == null) {
            log.info("出库待取货提醒任务正在执行中，跳过本次调度");
            return;
        }

        try {
            executeRemindPendingOutbound();
        } finally {
            redisLockUtils.unlock(lockKey, lockValue);
        }
    }

    /**
     * 执行出库待取货提醒任务
     */
    private void executeRemindPendingOutbound() {
        try {
            log.info("==================== 开始执行出库待取货提醒任务 ====================");

            // 计算5天前的时间
            LocalDateTime reminderTime = LocalDateTime.now().minusDays(5);
            LocalDateTime timeoutTime = LocalDateTime.now().minusDays(7);

            // 查询待取货超过5天但未超过7天的出库单
            LambdaQueryWrapper<Outbound> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Outbound::getStatus, OutboundStatus.PENDING_PICKUP)
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

            // 如果 RabbitMQ 未启用，跳过消息通知
            if (notificationProducer == null) {
                log.warn("RabbitMQ 未启用，跳过消息队列通知（降级模式）");
                return;
            }

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
