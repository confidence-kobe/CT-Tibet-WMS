/**
 * 出库管理相关API（仓管端）
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 创建直接出库单
 * @param {Object} data - 出库单数据
 * @param {number} data.warehouseId - 仓库ID
 * @param {number} data.receiverId - 领用人ID
 * @param {string} data.outboundDate - 出库日期
 * @param {string} data.purpose - 用途
 * @param {string} data.remark - 备注（可选）
 * @param {Array} data.details - 出库明细列表
 * @param {number} data.details[].materialId - 物资ID
 * @param {number} data.details[].quantity - 数量
 * @param {string} data.details[].remark - 备注（可选）
 * @returns {Promise} 返回创建结果
 */
export function createOutbound(data) {
  return $uRequest({
    url: '/api/outbounds/direct',
    method: 'POST',
    data
  })
}

/**
 * 查询待领取列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @param {string} params.startDate - 开始日期（可选）
 * @param {string} params.endDate - 结束日期（可选）
 * @param {string} params.keyword - 关键词（可选）
 * @returns {Promise} 返回待领取出库单列表
 */
export function getPendingOutbound(params) {
  return $uRequest({
    url: '/api/outbounds/pending',
    method: 'GET',
    data: params
  })
}

/**
 * 确认领取
 * @param {number} id - 出库单ID
 * @returns {Promise} 返回确认结果
 */
export function confirmOutbound(id) {
  return $uRequest({
    url: `/api/outbounds/${id}/confirm`,
    method: 'POST'
  })
}

/**
 * 查询出库记录列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @param {number} params.outboundType - 出库类型（可选）1-直接出库 2-申请出库
 * @param {number} params.status - 状态（可选）0-待取货 1-已完成 2-已取消
 * @param {string} params.startDate - 开始日期（可选）
 * @param {string} params.endDate - 结束日期（可选）
 * @param {string} params.keyword - 关键词（可选）
 * @returns {Promise} 返回出库记录列表
 */
export function getOutboundList(params) {
  return $uRequest({
    url: '/api/outbounds',
    method: 'GET',
    data: params
  })
}

/**
 * 查询出库详情
 * @param {number} id - 出库单ID
 * @returns {Promise} 返回出库详情（含明细）
 */
export function getOutboundDetail(id) {
  return $uRequest({
    url: `/api/outbounds/${id}`,
    method: 'GET'
  })
}

/**
 * 取消出库单
 * @param {number} id - 出库单ID
 * @param {string} reason - 取消原因
 * @returns {Promise} 返回取消结果
 */
export function cancelOutbound(id, reason) {
  return $uRequest({
    url: `/api/outbounds/${id}/cancel`,
    method: 'POST',
    data: {
      reason
    }
  })
}

/**
 * 查询出库统计
 * @param {Object} params - 查询参数
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @param {string} params.startDate - 开始日期（可选）
 * @param {string} params.endDate - 结束日期（可选）
 * @returns {Promise} 返回出库统计信息
 */
export function getOutboundStats(params) {
  return $uRequest({
    url: '/api/outbounds/stats',
    method: 'GET',
    data: params
  })
}

export default {
  createOutbound,
  getPendingOutbound,
  confirmOutbound,
  getOutboundList,
  getOutboundDetail,
  cancelOutbound,
  getOutboundStats
}
