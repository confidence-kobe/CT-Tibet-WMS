/**
 * 申请审批相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 申请列表
 * @param {Object} params - 查询参数
 */
export function getApplyList(params) {
  return $uRequest({
    url: '/api/applies',
    method: 'GET',
    data: params
  })
}

/**
 * 申请详情
 * @param {Number} id - 申请单ID
 */
export function getApplyDetail(id) {
  return $uRequest({
    url: `/api/applies/${id}`,
    method: 'GET'
  })
}

/**
 * 提交申请
 * @param {Object} data - 申请信息
 * @param {String} data.purpose - 领用用途
 * @param {String} data.remark - 备注
 * @param {Array} data.details - 申请明细
 */
export function createApply(data) {
  return $uRequest({
    url: '/api/applies',
    method: 'POST',
    data
  })
}

/**
 * 审批申请
 * @param {Number} id - 申请单ID
 * @param {Object} data - 审批信息
 * @param {Number} data.result - 审批结果：1=通过 2=拒绝
 * @param {String} data.opinion - 审批意见
 * @param {String} data.rejectReason - 拒绝原因（拒绝时必填）
 */
export function approveApply(id, data) {
  return $uRequest({
    url: `/api/applies/${id}/approve`,
    method: 'PUT',
    data
  })
}

/**
 * 撤销申请
 * @param {Number} id - 申请单ID
 * @param {Object} data - 撤销信息
 * @param {String} data.cancelReason - 撤销原因
 */
export function cancelApply(id, data) {
  return $uRequest({
    url: `/api/applies/${id}/cancel`,
    method: 'PUT',
    data
  })
}

/**
 * 待审批统计
 */
export function getPendingStats() {
  return $uRequest({
    url: '/api/applies/pending-stats',
    method: 'GET'
  })
}

/**
 * 我的申请列表（快捷接口）
 * @param {Object} params - 查询参数
 */
export function getMyApplies(params) {
  return $uRequest({
    url: '/api/applies',
    method: 'GET',
    data: {
      applicantId: uni.getStorageSync('userInfo')?.id,
      ...params
    }
  })
}

/**
 * 待审批列表（快捷接口）
 * @param {Object} params - 查询参数
 */
export function getPendingApplies(params) {
  return $uRequest({
    url: '/api/applies',
    method: 'GET',
    data: {
      status: 0, // 待审批
      ...params
    }
  })
}
