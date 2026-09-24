package com.ct.wms.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ct.wms.service.NotificationService;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.entity.Apply;
import com.ct.wms.mapper.ApplyMapper;
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
 * 申请超时定时任务
 *
 * 说明：站内消息通过 NotificationService 直接写入，不依赖 RabbitMQ
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplyTimeoutTask {

    private final NotificationService notificationService;
    private final ApplyMapper applyMapper;

    // 可选依赖：如果 Redis 未启用（测试环境），此字段为 null
    @Autowired(required = false)
    private RedisLockUtils redisLockUtils;


    // 分布式锁KEY前缀
    private static final String LOCK_PREFIX = "wms:lock:apply_timeout_task:";

    /**
     * 定时检查并提醒超时未审批的申请
     * <p>
     * 执行时间: 每小时执行一次
     * 业务规则: 待审批状态超过24小时发送提醒
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void remindTimeoutApply() {
        // 测试环境下跳过分布式锁
        if (redisLockUtils == null) {
            executeRemindTimeoutApply();
            return;
        }

        String lockKey = LOCK_PREFIX + "remind";
        String lockValue = redisLockUtils.tryLock(lockKey, 60);
        if (lockValue == null) {
            log.info("申请超时提醒任务正在执行中，跳过本次调度");
            return;
        }

        try {
            executeRemindTimeoutApply();
        } finally {
            redisLockUtils.unlock(lockKey, lockValue);
        }
    }

    /**
     * 执行申请超时提醒任务
     */
    private void executeRemindTimeoutApply() {
        try {
            log.info("==================== 开始执行申请超时提醒任务 ====================");

            // 计算24小时前的时间
            LocalDateTime timeoutTime = LocalDateTime.now().minusHours(24);

            // 查询所有待审批且超过24小时的申请
            LambdaQueryWrapper<Apply> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Apply::getStatus, ApplyStatus.PENDING)
                    .le(Apply::getApplyTime, timeoutTime);

            List<Apply> timeoutApplies = applyMapper.selectList(queryWrapper);

            if (timeoutApplies.isEmpty()) {
                log.info("没有发现超时待审批的申请");
                return;
            }

            log.info("发现 {} 条超时待审批的申请，开始发送提醒", timeoutApplies.size());

            int successCount = 0;

            for (Apply apply : timeoutApplies) {
                try {
                    // 提醒可审批的人（仓库管理员、本部门的部门管理员）；待审批时 approverId 尚未确定
                    notificationService.notifyApplyTimeoutReminder(apply);

                    successCount++;
                    log.info("发送审批超时提醒成功: id={}, applyNo={}, approverId={}",
                            apply.getId(), apply.getApplyNo(), apply.getApproverId());

                } catch (Exception e) {
                    log.error("发送审批超时提醒失败: id={}, applyNo={}",
                            apply.getId(), apply.getApplyNo(), e);
                }
            }

            log.info("申请超时提醒任务完成: 总数={}, 成功={}", timeoutApplies.size(), successCount);

        } catch (Exception e) {
            log.error("执行申请超时提醒任务失败", e);
        }

        log.info("==================== 申请超时提醒任务结束 ====================");
    }

    /**
     * 定时检查并自动取消长期未审批的申请
     * <p>
     * 执行时间: 每天凌晨3点执行
     * 业务规则: 待审批状态超过7天自动取消
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cancelLongTimeoutApply() {
        // 测试环境下跳过分布式锁
        if (redisLockUtils == null) {
            executeCancelLongTimeoutApply();
            return;
        }

        String lockKey = LOCK_PREFIX + "cancel";
        String lockValue = redisLockUtils.tryLock(lockKey, 300);
        if (lockValue == null) {
            log.info("申请长期超时取消任务正在执行中，跳过本次调度");
            return;
        }

        try {
            executeCancelLongTimeoutApply();
        } finally {
            redisLockUtils.unlock(lockKey, lockValue);
        }
    }

    /**
     * 执行申请长期超时取消任务
     */
    private void executeCancelLongTimeoutApply() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("system", null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
        try {
            log.info("==================== 开始执行申请长期超时取消任务 ====================");

            // 计算7天前的时间
            LocalDateTime timeoutTime = LocalDateTime.now().minusDays(7);

            // 查询所有待审批且超过7天的申请
            LambdaQueryWrapper<Apply> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Apply::getStatus, ApplyStatus.PENDING)
                    .le(Apply::getApplyTime, timeoutTime);

            List<Apply> longTimeoutApplies = applyMapper.selectList(queryWrapper);

            if (longTimeoutApplies.isEmpty()) {
                log.info("没有发现长期超时的申请");
                return;
            }

            log.info("发现 {} 条长期超时的申请，开始自动取消", longTimeoutApplies.size());

            int successCount = 0;
            int failCount = 0;

            for (Apply apply : longTimeoutApplies) {
                try {
                    // 更新申请状态为已取消
                    LambdaUpdateWrapper<Apply> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(Apply::getId, apply.getId())
                            .eq(Apply::getStatus, ApplyStatus.PENDING)
                            .set(Apply::getStatus, ApplyStatus.CANCELED)
                            .set(Apply::getRemark, "系统自动取消：超过7天未审批");

                    int updated = applyMapper.update(null, updateWrapper);

                    if (updated > 0) {
                        // 通知申请人（站内消息）
                        notificationService.notifyApplyTimeoutCancelled(apply);

                        successCount++;
                        log.info("成功取消申请: id={}, applyNo={}", apply.getId(), apply.getApplyNo());
                    }

                } catch (Exception e) {
                    failCount++;
                    log.error("取消申请失败: id={}, applyNo={}", apply.getId(), apply.getApplyNo(), e);
                }
            }

            log.info("申请长期超时取消任务完成: 总数={}, 成功={}, 失败={}",
                    longTimeoutApplies.size(), successCount, failCount);

        } catch (Exception e) {
            log.error("执行申请长期超时取消任务失败", e);
        } finally {
            SecurityContextHolder.clearContext();
        }

        log.info("==================== 申请长期超时取消任务结束 ====================");
    }
}
