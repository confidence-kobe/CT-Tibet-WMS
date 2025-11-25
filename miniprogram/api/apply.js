/**
 * 申请管理相关API（员工端）
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 创建物资申请
 * @param {Object} data - 申请数据
 * @param {number} data.warehouseId - 仓库ID
 * @param {string} data.applyReason - 申请理由
 * @param {Array} data.details - 申请明细列表
 * @param {number} data.details[].materialId - 物资ID
 * @param {number} data.details[].quantity - 数量
 * @returns {Promise} 返回创建结果
 */
export function createApply(data) {
  return $uRequest({
    url: '/api/applies',
    method: 'POST',
    data
  })
}

/**
 * 查询我的申请列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.warehouseId - 仓库ID（可选）
 * @param {number} params.status - 状态（可选）0-待审批 1-已通过 2-已拒绝 3-已完成 4-已取消
 * @param {string} params.startDate - 开始日期（可选）
 * @param {string} params.endDate - 结束日期（可选）
 * @param {string} params.keyword - 关键词（可选）
 * @returns {Promise} 返回分页数据
 */
export function getMyApplies(params) {
  return $uRequest({
    url: '/api/applies/my',
    method: 'GET',
    data: params
  })
}

/**
 * 查询申请详情
 * @param {number} id - 申请单ID
 * @returns {Promise} 返回申请详情（含明细）
 */
export function getApplyDetail(id) {
  return $uRequest({
    url: `/api/applies/${id}`,
    method: 'GET'
  })
}

/**
 * 撤销申请
 * @param {number} id - 申请单ID
 * @returns {Promise} 返回撤销结果
 */
export function cancelApply(id) {
  return $uRequest({
    url: `/api/applies/${id}/cancel`,
    method: 'POST'
  })
}

/**
 * 查询申请状态统计
 * @returns {Promise} 返回各状态数量统计
 */
export function getApplyStats() {
  return $uRequest({
    url: '/api/applies/stats',
    method: 'GET'
  })
}

export default {
  createApply,
  getMyApplies,
  getApplyDetail,
  cancelApply,
  getApplyStats
}
