package com.ct.wms.service;

import com.ct.wms.entity.Apply;
import com.ct.wms.entity.Outbound;

/**
 * 通知服务接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface NotificationService {

    /**
     * 发送申请提交通知
     *
     * @param apply 申请单
     */
    void notifyApplySubmit(Apply apply);

    /**
     * 发送申请审批通过通知
     *
     * @param apply 申请单
     */
    void notifyApplyApproved(Apply apply);

    /**
     * 发送申请审批拒绝通知
     *
     * @param apply 申请单
     */
    void notifyApplyRejected(Apply apply);

    /**
     * 发送出库待取货通知
     *
     * @param outbound 出库单
     */
    void notifyOutboundPending(Outbound outbound);

    /**
     * 发送出库完成通知
     *
     * @param outbound 出库单
     */
    void notifyOutboundCompleted(Outbound outbound);

    /**
     * 发送库存预警通知
     *
     * @param warehouseId 仓库ID
     * @param materialId  物资ID
     * @param materialName 物资名称
     * @param currentStock 当前库存
     * @param minStock 最低库存
     */
    void notifyLowStockAlert(Long warehouseId, Long materialId, String materialName,
                             java.math.BigDecimal currentStock, java.math.BigDecimal minStock);
}
