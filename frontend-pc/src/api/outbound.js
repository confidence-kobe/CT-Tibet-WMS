/**
 * 出库管理API接口
 * 包含出库单的创建、查询、确认、取消等接口
 */

import request from './request'

/**
 * 分页查询出库单列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} params.warehouseId - 仓库ID
 * @param {number} params.outboundType - 出库类型 (1-直接出库 2-申请出库)
 * @param {number} params.status - 状态 (0-待取货 1-已完成 2-已取消)
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {number} params.operatorId - 操作人ID
 * @param {number} params.receiverId - 领用人ID
 * @param {string} params.keyword - 关键词（出库单号）
 * @returns {Promise} 返回分页数据
 */
export function listOutbounds(params) {
  return request({
    url: '/outbounds',
    method: 'get',
    params
  })
}

/**
 * 查询出库单详情
 * @param {number} id - 出库单ID
 * @returns {Promise} 返回出库单详细信息（含明细）
 */
export function getOutboundById(id) {
  return request({
    url: `/outbounds/${id}`,
    method: 'get'
  })
}

/**
 * 创建直接出库单
 * @param {Object} data - 出库单数据
 * @param {number} data.warehouseId - 仓库ID
 * @param {number} data.receiverId - 领用人ID
 * @param {string} data.outboundDate - 出库日期
 * @param {string} data.purpose - 用途
 * @param {string} data.remark - 备注
 * @param {Array} data.details - 出库明细列表
 * @param {number} data.details[].materialId - 物资ID
 * @param {number} data.details[].quantity - 数量
 * @param {string} data.details[].remark - 备注
 * @returns {Promise} 返回创建结果
 */
export function createOutboundDirect(data) {
  return request({
    url: '/outbounds/direct',
    method: 'post',
    data
  })
}

/**
 * 确认出库
 * @param {number} id - 出库单ID
 * @returns {Promise} 返回确认结果
 */
export function confirmOutbound(id) {
  return request({
    url: `/outbounds/${id}/confirm`,
    method: 'post'
  })
}

/**
 * 取消出库单
 * @param {number} id - 出库单ID
 * @param {string} reason - 取消原因
 * @returns {Promise} 返回取消结果
 */
export function cancelOutbound(id, reason) {
  return request({
    url: `/outbounds/${id}/cancel`,
    method: 'post',
    params: { reason }
  })
}
