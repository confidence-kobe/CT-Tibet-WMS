package com.ct.wms.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.service.NotificationService;
import com.ct.wms.common.enums.OutboundSource;
import com.ct.wms.common.enums.OutboundStatus;
import com.ct.wms.entity.Outbound;
import com.ct.wms.mapper.OutboundMapper;
import com.ct.wms.service.OutboundService;
import com.ct.wms.utils.RedisLockUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 出库超时定时任务
 *
 * 说明：站内消息通过 NotificationService 直接写入，不依赖 RabbitMQ
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundTimeoutTask {

    private final NotificationService notificationService;
    private final OutboundMapper outboundMapper;
    private final OutboundService outboundService;

    // 可选依赖：如果 Redis 未启用（测试环境），此字段为 null
    @Autowired(required = false)
    private RedisLockUtils redisLockUtils;


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
        // 定时任务线程无 Security Context，注入系统账户避免下游方法调用 getCurrentUserId() 时报错
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("system", null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        try {
            log.info("==================== 开始执行出库超时检查任务 ====================");

            // 计算7天前的时间
            LocalDateTime timeoutTime = LocalDateTime.now().minusDays(7);

            // 查询所有待取货且超时的出库单
            LambdaQueryWrapper<Outbound> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Outbound::getStatus, OutboundStatus.PENDING_PICKUP)
                    .eq(Outbound::getSource, OutboundSource.FROM_APPLY)
                    // 待领取的出库单 outbound_time 为空（确认领取时才写入），按创建时间计算等待天数
                    .le(Outbound::getCreateTime, timeoutTime);

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

                    // 取消通知由 cancelOutbound 统一发送给领用人

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
        } finally {
            SecurityContextHolder.clearContext();
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
                    .eq(Outbound::getSource, OutboundSource.FROM_APPLY)
                    // 待领取的出库单 outbound_time 为空，按创建时间计算等待天数
                    .le(Outbound::getCreateTime, reminderTime)
                    .gt(Outbound::getCreateTime, timeoutTime);

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
                            outbound.getCreateTime(),
                            LocalDateTime.now()
                    ).toDays();
                    long daysRemaining = 7 - daysPassed;

                    // 发送领取提醒（站内消息）
                    notificationService.notifyPickupReminder(outbound, daysRemaining);

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
