package com.ct.wms.service;

import com.ct.wms.vo.DashboardStatsVO;

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
}
