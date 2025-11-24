package com.ct.wms.service;

import com.ct.wms.dto.InboundStatisticsDTO;
import com.ct.wms.dto.InventoryStatisticsDTO;
import com.ct.wms.dto.OutboundStatisticsDTO;
import com.ct.wms.vo.DashboardStatsVO;

import java.time.LocalDate;

/**
 * 统计Service接口
 *
 * @author CT Development Team
 * @since 2025-11-11
 */
public interface StatisticsService {

    /**
     * 获取仪表盘统计数据
     *
     * @return 仪表盘统计VO
     */
    DashboardStatsVO getDashboardStats();

    /**
     * 获取入库统计数据
     *
     * @param startDate 开始日期（可选，默认最近30天）
     * @param endDate 结束日期（可选，默认今天）
     * @param warehouseId 仓库ID（可选）
     * @return 入库统计数据
     */
    InboundStatisticsDTO getInboundStatistics(LocalDate startDate, LocalDate endDate, Long warehouseId);

    /**
     * 获取出库统计数据
     *
     * @param startDate 开始日期（可选，默认最近30天）
     * @param endDate 结束日期（可选，默认今天）
     * @param warehouseId 仓库ID（可选）
     * @param outboundType 出库类型（可选，1-直接出库，2-申请出库）
     * @return 出库统计数据
     */
    OutboundStatisticsDTO getOutboundStatistics(LocalDate startDate, LocalDate endDate, Long warehouseId, Integer outboundType);

    /**
     * 获取库存统计数据
     *
     * @param warehouseId 仓库ID（可选）
     * @return 库存统计数据
     */
    InventoryStatisticsDTO getInventoryStatistics(Long warehouseId);
}
