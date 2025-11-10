/**
 * 出库管理相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 出库单列表
 * @param {Object} params - 查询参数
 */
export function getOutboundList(params) {
  return $uRequest({
    url: '/api/outbounds',
    method: 'GET',
    data: params
  })
}

/**
 * 出库单详情
 * @param {Number} id - 出库单ID
 */
export function getOutboundDetail(id) {
  return $uRequest({
    url: `/api/outbounds/${id}`,
    method: 'GET'
  })
}

/**
 * 创建出库单（直接出库）
 * @param {Object} data - 出库单信息
 * @param {Number} data.warehouseId - 仓库ID
 * @param {Number} data.outboundType - 出库类型
 * @param {Number} data.receiverId - 领用人ID
 * @param {String} data.purpose - 领用用途
 * @param {String} data.remark - 备注
 * @param {Array} data.details - 出库明细
 */
export function createOutbound(data) {
  return $uRequest({
    url: '/api/outbounds',
    method: 'POST',
    data
  })
}

/**
 * 确认出库
 * @param {Number} id - 出库单ID
 * @param {Object} data - 确认信息
 * @param {String} data.remark - 备注
 */
export function confirmOutbound(id, data) {
  return $uRequest({
    url: `/api/outbounds/${id}/confirm`,
    method: 'PUT',
    data
  })
}

/**
 * 取消出库
 * @param {Number} id - 出库单ID
 * @param {Object} data - 取消信息
 * @param {String} data.cancelReason - 取消原因
 */
export function cancelOutbound(id, data) {
  return $uRequest({
    url: `/api/outbounds/${id}/cancel`,
    method: 'PUT',
    data
  })
}

/**
 * 待领取出库列表（快捷接口）
 * @param {Object} params - 查询参数
 */
export function getPendingOutbounds(params) {
  return $uRequest({
    url: '/api/outbounds',
    method: 'GET',
    data: {
      status: 0, // 待领取
      ...params
    }
  })
}
