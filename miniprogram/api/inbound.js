/**
 * 入库管理相关API（仓管端）
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 创建入库单
 * @param {Object} data - 入库单数据
 * @param {number} data.warehouseId - 仓库ID
 * @param {string} data.inboundDate - 入库日期
 * @param {string} data.supplier - 供应商（可选）
 * @param {string} data.remark - 备注（可选）
 * @param {Array} data.details - 入库明细列表
 * @param {number} data.details[].materialId - 物资ID
 * @param {number} data.details[].quantity - 数量
 * @param {number} data.details[].price - 单价（可选）
 * @param {string} data.details[].remark - 备注（可选）
 * @returns {Promise} 返回创建结果
 */
export function createInbound(data) {
  return $uRequest({
    url: '/api/inbounds',
    method: 'POST',
    data
  })
}

/**
 * 查询入库记录列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @param {string} params.startDate - 开始日期（可选）
 * @param {string} params.endDate - 结束日期（可选）
 * @param {string} params.keyword - 关键词（可选）
 * @returns {Promise} 返回入库记录列表
 */
export function getInboundList(params) {
  return $uRequest({
    url: '/api/inbounds',
    method: 'GET',
    data: params
  })
}

/**
 * 查询入库详情
 * @param {number} id - 入库单ID
 * @returns {Promise} 返回入库详情（含明细）
 */
export function getInboundDetail(id) {
  return $uRequest({
    url: `/api/inbounds/${id}`,
    method: 'GET'
  })
}

/**
 * 查询入库统计
 * @param {Object} params - 查询参数
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @param {string} params.startDate - 开始日期（可选）
 * @param {string} params.endDate - 结束日期（可选）
 * @returns {Promise} 返回入库统计信息
 */
export function getInboundStats(params) {
  return $uRequest({
    url: '/api/inbounds/stats',
    method: 'GET',
    data: params
  })
}

/**
 * 删除入库单
 * @param {number} id - 入库单ID
 * @returns {Promise} 返回删除结果
 */
export function deleteInbound(id) {
  return $uRequest({
    url: `/api/inbounds/${id}`,
    method: 'DELETE'
  })
}

export default {
  createInbound,
  getInboundList,
  getInboundDetail,
  getInboundStats,
  deleteInbound
}
