/**
 * 统计报表相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 工作台统计
 */
export function getDashboard() {
  return $uRequest({
    url: '/api/stats/dashboard',
    method: 'GET'
  })
}

/**
 * 出入库统计
 * @param {Object} params - 查询参数
 */
export function getInoutboundStats(params) {
  return $uRequest({
    url: '/api/stats/inoutbound',
    method: 'GET',
    data: params
  })
}

/**
 * 物资使用统计
 * @param {Object} params - 查询参数
 */
export function getMaterialUsageStats(params) {
  return $uRequest({
    url: '/api/stats/material-usage',
    method: 'GET',
    data: params
  })
}

/**
 * 审批统计
 * @param {Object} params - 查询参数
 */
export function getApprovalStats(params) {
  return $uRequest({
    url: '/api/stats/approval',
    method: 'GET',
    data: params
  })
}
