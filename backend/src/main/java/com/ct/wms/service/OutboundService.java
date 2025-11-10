package com.ct.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.dto.OutboundDTO;
import com.ct.wms.entity.Outbound;

/**
 * 出库Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface OutboundService {

    /**
     * 分页查询出库单列表
     *
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @param warehouseId  仓库ID（可选）
     * @param outboundType 出库类型（可选）
     * @param status       状态（可选）
     * @param startDate    开始日期（可选）
     * @param endDate      结束日期（可选）
     * @param operatorId   操作人ID（可选）
     * @param receiverId   领用人ID（可选）
     * @param keyword      关键词（可选）
     * @return 分页结果
     */
    Page<Outbound> listOutbounds(Integer pageNum, Integer pageSize, Long warehouseId,
                                  Integer outboundType, Integer status, String startDate,
                                  String endDate, Long operatorId, Long receiverId, String keyword);

    /**
     * 根据ID获取出库单详情
     *
     * @param id 出库单ID
     * @return 出库单详情（包含明细）
     */
    Outbound getOutboundById(Long id);

    /**
     * 创建直接出库单（仓管员）
     * 立即扣减库存
     *
     * @param dto 出库单DTO
     * @return 出库单ID
     */
    Long createOutboundDirect(OutboundDTO dto);

    /**
     * 从申请单创建出库单（审批通过时调用）
     * 不立即扣减库存，状态为待取货
     *
     * @param applyId    申请单ID
     * @param warehouseId 仓库ID
     * @param receiverId  领用人ID
     * @param operatorId  操作人ID（审批人）
     * @return 出库单ID
     */
    Long createOutboundFromApply(Long applyId, Long warehouseId, Long receiverId, Long operatorId);

    /**
     * 确认出库（员工取货）
     * 扣减库存
     *
     * @param id 出库单ID
     */
    void confirmOutbound(Long id);

    /**
     * 取消出库单
     *
     * @param id     出库单ID
     * @param reason 取消原因
     */
    void cancelOutbound(Long id, String reason);
}
