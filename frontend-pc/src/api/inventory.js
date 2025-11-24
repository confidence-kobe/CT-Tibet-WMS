/**
 * 库存管理API接口
 * 包含库存查询、库存流水、库存预警等接口
 */

import request from './request'

/**
 * 分页查询库存列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} params.warehouseId - 仓库ID
 * @param {number} params.materialId - 物资ID
 * @param {string} params.keyword - 关键词（物资编码或名称）
 * @returns {Promise} 返回分页数据
 */
export function listInventories(params) {
  return request({
    url: '/inventories',
    method: 'get',
    params
  })
}

/**
 * 分页查询库存流水
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} params.warehouseId - 仓库ID
 * @param {number} params.materialId - 物资ID
 * @param {number} params.changeType - 变动类型 (1-入库 2-出库)
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @returns {Promise} 返回分页数据
 */
export function listInventoryLogs(params) {
  return request({
    url: '/inventories/logs',
    method: 'get',
    params
  })
}

/**
 * 查询低库存预警列表
 * @param {Object} params - 查询参数
 * @param {number} params.warehouseId - 仓库ID
 * @returns {Promise} 返回低库存预警列表
 */
export function listLowStockAlerts(params) {
  return request({
    url: '/inventories/low-stock-alerts',
    method: 'get',
    params
  })
}

/**
 * 查询库存列表（别名，用于申请创建页面）
 */
export const listInventory = listInventories
