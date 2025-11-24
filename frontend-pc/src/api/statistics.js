/**
 * 统计报表API接口
 * 包含数据统计、报表接口
 */

import request from './request'

/**
 * 获取仪表盘统计数据
 * @returns {Promise} 返回首页仪表盘的各项统计数据
 */
export function getDashboardStats() {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  })
}

/**
 * 获取入库统计数据
 * @param {Object} params - 查询参数 { startDate, endDate, warehouseId }
 * @returns {Promise} 返回入库统计数据
 */
export function getInboundStatistics(params) {
  return request({
    url: '/statistics/inbound',
    method: 'get',
    params
  })
}

/**
 * 获取出库统计数据
 * @param {Object} params - 查询参数 { startDate, endDate, warehouseId, outboundType }
 * @returns {Promise} 返回出库统计数据
 */
export function getOutboundStatistics(params) {
  return request({
    url: '/statistics/outbound',
    method: 'get',
    params
  })
}

/**
 * 获取库存统计数据
 * @param {Object} params - 查询参数 { warehouseId }
 * @returns {Promise} 返回库存统计数据
 */
export function getInventoryStatistics(params) {
  return request({
    url: '/statistics/inventory',
    method: 'get',
    params
  })
}
