package com.ct.wms.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ct.wms.common.enums.ApplyStatus;
import com.ct.wms.common.enums.MessageType;
import com.ct.wms.dto.NotificationMessageDTO;
import com.ct.wms.entity.Apply;
import com.ct.wms.mapper.ApplyMapper;
import com.ct.wms.mq.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 申请超时定时任务
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplyTimeoutTask {

    private final ApplyMapper applyMapper;
    private final NotificationProducer notificationProducer;

    /**
     * 定时检查并提醒超时未审批的申请
     * <p>
     * 执行时间: 每小时执行一次
     * 业务规则: 待审批状态超过24小时发送提醒
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void remindTimeoutApply() {
        log.info("==================== 开始执行申请超时提醒任务 ====================");

        try {
            // 计算24小时前的时间
            LocalDateTime timeoutTime = LocalDateTime.now().minusHours(24);

            // 查询所有待审批且超过24小时的申请
            LambdaQueryWrapper<Apply> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Apply::getStatus, ApplyStatus.PENDING.getValue())
                    .le(Apply::getApplyTime, timeoutTime)
                    .isNotNull(Apply::getApproverId);

            List<Apply> timeoutApplies = applyMapper.selectList(queryWrapper);

            if (timeoutApplies.isEmpty()) {
                log.info("没有发现超时待审批的申请");
                return;
            }

            log.info("发现 {} 条超时待审批的申请，开始发送提醒", timeoutApplies.size());

            int successCount = 0;

            for (Apply apply : timeoutApplies) {
                try {
                    // 发送提醒消息给审批人
                    NotificationMessageDTO notification = NotificationMessageDTO.builder()
                            .receiverId(apply.getApproverId())
                            .messageType(MessageType.APPLY_SUBMIT.getValue())
                            .title("申请审批超时提醒")
                            .content(String.format("申请单 %s 已提交超过24小时，请及时审批。申请人: %s",
                                    apply.getApplyNo(), apply.getApplicantName()))
                            .relatedId(apply.getId())
                            .relatedType(3) // 3-申请
                            .sendWechat(true)
                            .build();

                    notificationProducer.sendNotification(notification);

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
        log.info("==================== 开始执行申请长期超时取消任务 ====================");

        try {
            // 计算7天前的时间
            LocalDateTime timeoutTime = LocalDateTime.now().minusDays(7);

            // 查询所有待审批且超过7天的申请
            LambdaQueryWrapper<Apply> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Apply::getStatus, ApplyStatus.PENDING.getValue())
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
                            .eq(Apply::getStatus, ApplyStatus.PENDING.getValue())
                            .set(Apply::getStatus, ApplyStatus.CANCELED.getValue())
                            .set(Apply::getRemark, "系统自动取消：超过7天未审批");

                    int updated = applyMapper.update(null, updateWrapper);

                    if (updated > 0) {
                        // 发送通知给申请人
                        NotificationMessageDTO notification = NotificationMessageDTO.builder()
                                .receiverId(apply.getApplicantId())
                                .messageType(MessageType.TIMEOUT_CANCEL.getValue())
                                .title("申请超时取消通知")
                                .content(String.format("您的申请单 %s 因超过7天未审批已被系统自动取消。",
                                        apply.getApplyNo()))
                                .relatedId(apply.getId())
                                .relatedType(3) // 3-申请
                                .sendWechat(true)
                                .build();

                        notificationProducer.sendNotification(notification);

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
        }

        log.info("==================== 申请长期超时取消任务结束 ====================");
    }
}
