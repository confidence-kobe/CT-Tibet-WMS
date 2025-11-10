package com.ct.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ct.wms.dto.InboundDTO;
import com.ct.wms.entity.Inbound;

/**
 * 入库Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface InboundService {

    /**
     * 分页查询入库单列表
     *
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @param warehouseId  仓库ID（可选）
     * @param inboundType  入库类型（可选）
     * @param startDate    开始日期（可选）
     * @param endDate      结束日期（可选）
     * @param operatorId   操作人ID（可选）
     * @param keyword      关键词（可选）
     * @return 分页结果
     */
    Page<Inbound> listInbounds(Integer pageNum, Integer pageSize, Long warehouseId,
                               Integer inboundType, String startDate, String endDate,
                               Long operatorId, String keyword);

    /**
     * 根据ID获取入库单详情
     *
     * @param id 入库单ID
     * @return 入库单详情（包含明细）
     */
    Inbound getInboundById(Long id);

    /**
     * 创建入库单
     *
     * @param dto 入库单DTO
     * @return 入库单ID
     */
    Long createInbound(InboundDTO dto);
}
