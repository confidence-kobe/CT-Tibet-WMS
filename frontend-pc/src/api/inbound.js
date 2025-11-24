/**
 * 入库管理API接口
 * 包含入库单的创建、查询等接口
 */

import request from './request'

/**
 * 分页查询入库单列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} params.warehouseId - 仓库ID
 * @param {number} params.inboundType - 入库类型 (1-采购入库 2-退货入库 3-调拨入库 4-其他入库)
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {number} params.operatorId - 操作人ID
 * @param {string} params.keyword - 关键词（入库单号）
 * @returns {Promise} 返回分页数据
 */
export function listInbounds(params) {
  return request({
    url: '/inbounds',
    method: 'get',
    params
  })
}

/**
 * 查询入库单详情
 * @param {number} id - 入库单ID
 * @returns {Promise} 返回入库单详细信息（含明细）
 */
export function getInboundById(id) {
  return request({
    url: `/inbounds/${id}`,
    method: 'get'
  })
}

/**
 * 创建入库单
 * @param {Object} data - 入库单数据
 * @param {number} data.warehouseId - 仓库ID
 * @param {number} data.inboundType - 入库类型
 * @param {string} data.inboundDate - 入库日期
 * @param {string} data.supplier - 供应商
 * @param {string} data.remark - 备注
 * @param {Array} data.details - 入库明细列表
 * @param {number} data.details[].materialId - 物资ID
 * @param {number} data.details[].quantity - 数量
 * @param {number} data.details[].price - 单价
 * @param {string} data.details[].remark - 备注
 * @returns {Promise} 返回创建结果
 */
export function createInbound(data) {
  return request({
    url: '/inbounds',
    method: 'post',
    data
  })
}
