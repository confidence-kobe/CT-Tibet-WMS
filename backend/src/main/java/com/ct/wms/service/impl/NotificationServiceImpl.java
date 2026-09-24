package com.ct.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ct.wms.common.constant.RoleCode;
import com.ct.wms.common.enums.MessageType;
import com.ct.wms.common.enums.UserStatus;
import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Outbound;
import com.ct.wms.entity.Role;
import com.ct.wms.entity.User;
import com.ct.wms.entity.Warehouse;
import com.ct.wms.mapper.RoleMapper;
import com.ct.wms.mapper.UserMapper;
import com.ct.wms.mapper.WarehouseMapper;
import com.ct.wms.service.MessageService;
import com.ct.wms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知服务实现：发送站内消息
 * <p>
 * 站内消息直接写入 tb_message，不依赖 RabbitMQ（RabbitMQ 默认未启用，
 * 原先只有启用 RabbitMQ 时才会保存消息，导致消息中心始终为空）。
 * 在事务中调用时，消息在事务提交成功后才写入；任何异常只记录日志，不影响业务。
 * 微信订阅消息推送将在对接微信后另行实现。
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    /** 关联业务类型：2-出库单 3-申请单 */
    private static final int RELATED_OUTBOUND = 2;
    private static final int RELATED_APPLY = 3;

    private final MessageService messageService;
    private final WarehouseMapper warehouseMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Override
    public void notifyApplySubmit(Apply apply) {
        if (apply == null) {
            return;
        }
        send(findApprovers(apply), MessageType.APPLY_SUBMIT, "新的物资申请待审批",
                String.format("%s 提交了物资申请 %s（用途：%s），请及时审批。",
                        nvl(apply.getApplicantName(), "员工"), apply.getApplyNo(), nvl(apply.getPurpose(), "-")),
                apply.getId(), RELATED_APPLY);
    }

    @Override
    public void notifyApplyApproved(Apply apply) {
        if (apply == null) {
            return;
        }
        send(single(apply.getApplicantId()), MessageType.APPLY_APPROVED, "物资申请已通过",
                String.format("您的物资申请 %s 已审批通过，请在7天内到%s领取，逾期将自动取消。%s",
                        apply.getApplyNo(), warehouseName(apply.getWarehouseId()),
                        StringUtils.hasText(apply.getApprovalOpinion()) ? "审批意见：" + apply.getApprovalOpinion() : ""),
                apply.getId(), RELATED_APPLY);
    }

    @Override
    public void notifyApplyRejected(Apply apply) {
        if (apply == null) {
            return;
        }
        send(single(apply.getApplicantId()), MessageType.APPLY_REJECTED, "物资申请未通过",
                String.format("您的物资申请 %s 未通过审批。原因：%s",
                        apply.getApplyNo(), nvl(apply.getRejectReason(), "未填写")),
                apply.getId(), RELATED_APPLY);
    }

    @Override
    public void notifyApplyTimeoutReminder(Apply apply) {
        if (apply == null) {
            return;
        }
        send(findApprovers(apply), MessageType.APPLY_REMINDER, "申请审批超时提醒",
                String.format("%s 的物资申请 %s 已提交超过24小时仍未审批，请尽快处理。",
                        nvl(apply.getApplicantName(), "员工"), apply.getApplyNo()),
                apply.getId(), RELATED_APPLY);
    }

    @Override
    public void notifyApplyTimeoutCancelled(Apply apply) {
        if (apply == null) {
            return;
        }
        send(single(apply.getApplicantId()), MessageType.TIMEOUT_CANCEL, "申请已超时取消",
                String.format("您的物资申请 %s 超过7天未审批，已被系统自动取消，如仍需要请重新申请。", apply.getApplyNo()),
                apply.getId(), RELATED_APPLY);
    }

    @Override
    public void notifyOutboundPending(Outbound outbound) {
        if (outbound == null) {
            return;
        }
        send(single(outbound.getReceiverId()), MessageType.OUTBOUND_PENDING, "物资待领取",
                String.format("出库单 %s 的物资已备好，请在7天内到%s领取。",
                        outbound.getOutboundNo(), warehouseName(outbound.getWarehouseId())),
                relatedApplyOrOutbound(outbound), relatedTypeOf(outbound));
    }

    @Override
    public void notifyPickupReminder(Outbound outbound, long daysRemaining) {
        if (outbound == null) {
            return;
        }
        send(single(outbound.getReceiverId()), MessageType.PICKUP_REMINDER, "领取提醒",
                String.format("出库单 %s 的物资还未领取，请在 %d 天内到%s领取，逾期将自动取消。",
                        outbound.getOutboundNo(), Math.max(daysRemaining, 0), warehouseName(outbound.getWarehouseId())),
                relatedApplyOrOutbound(outbound), relatedTypeOf(outbound));
    }

    @Override
    public void notifyOutboundCompleted(Outbound outbound) {
        if (outbound == null) {
            return;
        }
        send(single(outbound.getReceiverId()), MessageType.SYSTEM, "物资已领取",
                String.format("出库单 %s 的物资已领取完成。", outbound.getOutboundNo()),
                relatedApplyOrOutbound(outbound), relatedTypeOf(outbound));
    }

    @Override
    public void notifyOutboundCancelled(Outbound outbound, String reason) {
        if (outbound == null) {
            return;
        }
        send(single(outbound.getReceiverId()), MessageType.TIMEOUT_CANCEL, "出库单已取消",
                String.format("出库单 %s 已取消，锁定的物资已释放。原因：%s",
                        outbound.getOutboundNo(), nvl(reason, "未填写")),
                relatedApplyOrOutbound(outbound), relatedTypeOf(outbound));
    }

    @Override
    public void notifyLowStockAlert(Long warehouseId, Long materialId, String materialName,
                                    BigDecimal currentStock, BigDecimal minStock) {
        try {
            Warehouse warehouse = warehouseMapper.selectById(warehouseId);
            if (warehouse == null) {
                return;
            }
            Set<Long> receivers = new LinkedHashSet<>();
            if (warehouse.getManagerId() != null) {
                receivers.add(warehouse.getManagerId());
            }
            receivers.addAll(findDeptAdmins(warehouse.getDeptId()));
            send(receivers, MessageType.STOCK_ALERT, "库存预警",
                    String.format("%s 的 %s 库存为 %s，低于最低库存 %s，请及时补货。",
                            warehouse.getWarehouseName(), nvl(materialName, "物资"),
                            plain(currentStock), plain(minStock)),
                    null, null);
        } catch (Exception e) {
            log.error("发送库存预警通知失败: warehouseId={}, materialId={}", warehouseId, materialId, e);
        }
    }

    // ============================= 辅助方法 =============================

    /**
     * 发送站内消息：在事务中时于提交成功后写入，否则立即写入；异常只记录日志
     */
    private void send(Collection<Long> receiverIds, MessageType type, String title, String content,
                      Long relatedId, Integer relatedType) {
        try {
            if (receiverIds == null || receiverIds.isEmpty()) {
                log.warn("站内消息没有接收人，已跳过: title={}", title);
                return;
            }
            List<Long> receivers = receiverIds.stream().distinct().collect(Collectors.toList());
            Runnable deliver = () -> receivers.forEach(receiverId -> {
                try {
                    messageService.sendMessage(receiverId, type.getValue(), title, content, relatedId, relatedType);
                } catch (Exception e) {
                    log.error("写入站内消息失败: receiverId={}, title={}", receiverId, title, e);
                }
            });

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        deliver.run();
                    }
                });
            } else {
                deliver.run();
            }
        } catch (Exception e) {
            log.error("发送站内消息失败: title={}", title, e);
        }
    }

    /**
     * 可审批申请的人：申请仓库的仓库管理员 + 申请人所在部门的部门管理员（与审批权限一致）
     */
    private Set<Long> findApprovers(Apply apply) {
        Set<Long> approvers = new LinkedHashSet<>();
        Warehouse warehouse = apply.getWarehouseId() != null ? warehouseMapper.selectById(apply.getWarehouseId()) : null;
        if (warehouse != null && warehouse.getManagerId() != null) {
            approvers.add(warehouse.getManagerId());
        }
        approvers.addAll(findDeptAdmins(apply.getDeptId()));
        return approvers;
    }

    /**
     * 部门内启用的部门管理员
     */
    private List<Long> findDeptAdmins(Long deptId) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        List<Long> roleIds = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                        .apply("LOWER(role_code) = {0}", RoleCode.DEPT_ADMIN))
                .stream().map(Role::getId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .select(User::getId)
                        .eq(User::getDeptId, deptId)
                        .in(User::getRoleId, roleIds)
                        .eq(User::getStatus, UserStatus.ENABLED))
                .stream().map(User::getId).collect(Collectors.toList());
    }

    private Set<Long> single(Long userId) {
        return userId == null ? Collections.emptySet() : Collections.singleton(userId);
    }

    private String warehouseName(Long warehouseId) {
        Warehouse warehouse = warehouseId != null ? warehouseMapper.selectById(warehouseId) : null;
        return warehouse != null ? warehouse.getWarehouseName() : "仓库";
    }

    /**
     * 来自申请的出库单关联到申请单（员工只能查看申请详情），直接出库关联到出库单
     */
    private Long relatedApplyOrOutbound(Outbound outbound) {
        return outbound.getApplyId() != null ? outbound.getApplyId() : outbound.getId();
    }

    private Integer relatedTypeOf(Outbound outbound) {
        return outbound.getApplyId() != null ? RELATED_APPLY : RELATED_OUTBOUND;
    }

    private static String nvl(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private static String plain(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }
}
