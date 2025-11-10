/**
 * 库存管理相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 库存列表
 * @param {Object} params - 查询参数
 */
export function getInventoryList(params) {
  return $uRequest({
    url: '/api/inventory',
    method: 'GET',
    data: params
  })
}

/**
 * 库存详情
 * @param {Number} warehouseId - 仓库ID
 * @param {Number} materialId - 物资ID
 */
export function getInventoryDetail(warehouseId, materialId) {
  return $uRequest({
    url: `/api/inventory/${warehouseId}/${materialId}`,
    method: 'GET'
  })
}

/**
 * 库存预警列表
 * @param {Object} params - 查询参数
 */
export function getInventoryAlerts(params) {
  return $uRequest({
    url: '/api/inventory/alerts',
    method: 'GET',
    data: params
  })
}

/**
 * 库存流水
 * @param {Object} params - 查询参数
 */
export function getInventoryLogs(params) {
  return $uRequest({
    url: '/api/inventory/logs',
    method: 'GET',
    data: params
  })
}
