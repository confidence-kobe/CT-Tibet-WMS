package com.ct.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.entity.Inventory;
import com.ct.wms.entity.InventoryLog;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface InventoryService {

    /**
     * 增加库存（入库时调用）
     *
     * @param warehouseId 仓库ID
     * @param materialId  物资ID
     * @param quantity    数量
     * @param relatedNo   关联单号
     * @param relatedId   关联单据ID
     * @param operatorId  操作人ID
     */
    void increaseInventory(Long warehouseId, Long materialId, BigDecimal quantity,
                           String relatedNo, Long relatedId, Long operatorId);

    /**
     * 减少库存（出库时调用）
     *
     * @param warehouseId 仓库ID
     * @param materialId  物资ID
     * @param quantity    数量
     * @param relatedNo   关联单号
     * @param relatedId   关联单据ID
     * @param operatorId  操作人ID
     */
    void decreaseInventory(Long warehouseId, Long materialId, BigDecimal quantity,
                           String relatedNo, Long relatedId, Long operatorId);

    /**
     * 获取库存
     *
     * @param warehouseId 仓库ID
     * @param materialId  物资ID
     * @return 库存对象
     */
    Inventory getInventory(Long warehouseId, Long materialId);

    /**
     * 检查库存是否充足
     *
     * @param warehouseId 仓库ID
     * @param materialId  物资ID
     * @param quantity    需要的数量
     * @return true-充足 false-不足
     */
    boolean checkInventory(Long warehouseId, Long materialId, BigDecimal quantity);

    /**
     * 分页查询库存列表
     *
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @param warehouseId 仓库ID（可选）
     * @param materialId  物资ID（可选）
     * @param keyword     关键词（可选）
     * @return 分页结果
     */
    Page<Inventory> listInventories(Integer pageNum, Integer pageSize, Long warehouseId,
                                     Long materialId, String keyword);

    /**
     * 分页查询库存流水
     *
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @param warehouseId 仓库ID（可选）
     * @param materialId  物资ID（可选）
     * @param changeType  变动类型（可选）
     * @param startDate   开始日期（可选）
     * @param endDate     结束日期（可选）
     * @return 分页结果
     */
    Page<InventoryLog> listInventoryLogs(Integer pageNum, Integer pageSize, Long warehouseId,
                                         Long materialId, Integer changeType, String startDate, String endDate);

    /**
     * 查询低库存预警列表
     *
     * @param warehouseId 仓库ID（可选）
     * @return 低库存列表
     */
    List<Inventory> listLowStockAlerts(Long warehouseId);
}
