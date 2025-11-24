/**
 * 申请审批管理API接口
 * 包含物资申请、审批、取消等接口
 */

import request from './request'

/**
 * 分页查询申请单列表（管理员/仓管员）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} params.warehouseId - 仓库ID
 * @param {number} params.status - 状态 (0-待审批 1-已通过 2-已拒绝 3-已完成 4-已取消)
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {number} params.applicantId - 申请人ID
 * @param {number} params.approverId - 审批人ID
 * @param {string} params.keyword - 关键词（申请单号）
 * @returns {Promise} 返回分页数据
 */
export function listApplies(params) {
  return request({
    url: '/applies',
    method: 'get',
    params
  })
}

/**
 * 查询我的申请单列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} params.warehouseId - 仓库ID
 * @param {number} params.status - 状态
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {string} params.keyword - 关键词（申请单号）
 * @returns {Promise} 返回分页数据
 */
export function getMyApplies(params) {
  return request({
    url: '/applies/my',
    method: 'get',
    params
  })
}

/**
 * 查询待审批列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} params.warehouseId - 仓库ID
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {string} params.keyword - 关键词（申请单号）
 * @returns {Promise} 返回分页数据
 */
export function getPendingApplies(params) {
  return request({
    url: '/applies/pending',
    method: 'get',
    params
  })
}

/**
 * 查询已审批列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} params.warehouseId - 仓库ID
 * @param {number} params.approvalStatus - 审批状态（1-已通过，2-已拒绝）
 * @param {string} params.startDate - 开始日期
 * @param {string} params.endDate - 结束日期
 * @param {string} params.keyword - 关键词（申请单号）
 * @returns {Promise} 返回分页数据
 */
export function getApprovedApplies(params) {
  return request({
    url: '/applies/approved',
    method: 'get',
    params
  })
}

/**
 * 查询申请单详情
 * @param {number} id - 申请单ID
 * @returns {Promise} 返回申请单详细信息（含明细）
 */
export function getApplyById(id) {
  return request({
    url: `/applies/${id}`,
    method: 'get'
  })
}

/**
 * 创建申请单
 * @param {Object} data - 申请单数据
 * @param {number} data.warehouseId - 仓库ID
 * @param {string} data.applyReason - 申请理由
 * @param {Array} data.details - 申请明细列表
 * @param {number} data.details[].materialId - 物资ID
 * @param {number} data.details[].quantity - 数量
 * @returns {Promise} 返回创建结果（返回申请单ID）
 */
export function createApply(data) {
  return request({
    url: '/applies',
    method: 'post',
    data
  })
}

/**
 * 审批申请单
 * @param {number} id - 申请单ID
 * @param {number} approvalStatus - 审批状态 (1-通过 2-拒绝)
 * @param {string} rejectReason - 拒绝理由（拒绝时必填）
 * @returns {Promise} 返回审批结果
 */
export function approveApply(id, approvalStatus, rejectReason) {
  return request({
    url: `/applies/${id}/approve`,
    method: 'post',
    params: {
      approvalStatus,
      rejectReason
    }
  })
}

/**
 * 取消申请单
 * @param {number} id - 申请单ID
 * @returns {Promise} 返回取消结果
 */
export function cancelApply(id) {
  return request({
    url: `/applies/${id}/cancel`,
    method: 'post'
  })
}
