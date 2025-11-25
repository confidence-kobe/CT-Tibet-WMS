/**
 * 审批管理相关API（仓管端）
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 查询待审批列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @param {string} params.startDate - 开始日期（可选）
 * @param {string} params.endDate - 结束日期（可选）
 * @param {string} params.keyword - 关键词（可选）
 * @returns {Promise} 返回待审批申请列表
 */
export function getPendingApproval(params) {
  return $uRequest({
    url: '/api/applies/pending',
    method: 'GET',
    data: params
  })
}

/**
 * 查询已审批列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @param {number} params.approvalStatus - 审批状态（可选）1-已通过 2-已拒绝
 * @param {string} params.startDate - 开始日期（可选）
 * @param {string} params.endDate - 结束日期（可选）
 * @param {string} params.keyword - 关键词（可选）
 * @returns {Promise} 返回已审批申请列表
 */
export function getApprovedList(params) {
  return $uRequest({
    url: '/api/applies/approved',
    method: 'GET',
    data: params
  })
}

/**
 * 审批申请
 * @param {number} id - 申请单ID
 * @param {Object} data - 审批数据
 * @param {number} data.approvalStatus - 审批状态 1-通过 2-拒绝
 * @param {string} data.rejectReason - 拒绝理由（拒绝时必填）
 * @returns {Promise} 返回审批结果
 */
export function approveApply(id, data) {
  return $uRequest({
    url: `/api/applies/${id}/approve`,
    method: 'POST',
    data: {
      approvalStatus: data.approvalStatus,
      rejectReason: data.rejectReason
    }
  })
}

/**
 * 查询待审批统计
 * @returns {Promise} 返回待审批数量统计
 */
export function getPendingStats() {
  return $uRequest({
    url: '/api/applies/pending-stats',
    method: 'GET'
  })
}

/**
 * 批量审批
 * @param {Object} data - 批量审批数据
 * @param {Array} data.ids - 申请单ID数组
 * @param {number} data.approvalStatus - 审批状态 1-通过 2-拒绝
 * @param {string} data.rejectReason - 拒绝理由（拒绝时必填）
 * @returns {Promise} 返回批量审批结果
 */
export function batchApprove(data) {
  return $uRequest({
    url: '/api/applies/batch-approve',
    method: 'POST',
    data
  })
}

export default {
  getPendingApproval,
  getApprovedList,
  approveApply,
  getPendingStats,
  batchApprove
}
