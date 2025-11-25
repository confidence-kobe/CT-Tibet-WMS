/**
 * 库存管理相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 查询库存列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @param {number} params.materialId - 物资ID（可选）
 * @param {string} params.keyword - 关键词（可选）
 * @param {boolean} params.lowStock - 是否只显示低库存（可选）
 * @returns {Promise} 返回库存列表
 */
export function getInventoryList(params) {
  return $uRequest({
    url: '/api/inventory',
    method: 'GET',
    data: params
  })
}

/**
 * 查询库存详情
 * @param {number} id - 库存ID
 * @returns {Promise} 返回库存详细信息
 */
export function getInventoryDetail(id) {
  return $uRequest({
    url: `/api/inventory/${id}`,
    method: 'GET'
  })
}

/**
 * 搜索库存
 * @param {string} keyword - 关键词（物资名称、编码等）
 * @param {number} warehouseId - 仓库ID（可选）
 * @returns {Promise} 返回搜索结果
 */
export function searchInventory(keyword, warehouseId) {
  return $uRequest({
    url: '/api/inventory/search',
    method: 'GET',
    data: {
      keyword,
      warehouseId
    }
  })
}

/**
 * 查询库存预警列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @returns {Promise} 返回预警库存列表
 */
export function getWarningList(params) {
  return $uRequest({
    url: '/api/inventory/warning',
    method: 'GET',
    data: params
  })
}

/**
 * 查询库存统计
 * @param {number} warehouseId - 仓库ID（可选）
 * @returns {Promise} 返回库存统计信息
 */
export function getInventoryStats(warehouseId) {
  return $uRequest({
    url: '/api/inventory/stats',
    method: 'GET',
    data: {
      warehouseId
    }
  })
}

/**
 * 查询库存变动记录
 * @param {Object} params - 查询参数
 * @param {number} params.inventoryId - 库存ID
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @returns {Promise} 返回变动记录列表
 */
export function getInventoryHistory(params) {
  return $uRequest({
    url: '/api/inventory/history',
    method: 'GET',
    data: params
  })
}

export default {
  getInventoryList,
  getInventoryDetail,
  searchInventory,
  getWarningList,
  getInventoryStats,
  getInventoryHistory
}
