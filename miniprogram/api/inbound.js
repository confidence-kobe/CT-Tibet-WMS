/**
 * 入库管理相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 入库单列表
 * @param {Object} params - 查询参数
 */
export function getInboundList(params) {
  return $uRequest({
    url: '/api/inbounds',
    method: 'GET',
    data: params
  })
}

/**
 * 入库单详情
 * @param {Number} id - 入库单ID
 */
export function getInboundDetail(id) {
  return $uRequest({
    url: `/api/inbounds/${id}`,
    method: 'GET'
  })
}

/**
 * 创建入库单
 * @param {Object} data - 入库单信息
 * @param {Number} data.warehouseId - 仓库ID
 * @param {Number} data.inboundType - 入库类型
 * @param {String} data.inboundTime - 入库时间
 * @param {String} data.remark - 备注
 * @param {Array} data.details - 入库明细
 */
export function createInbound(data) {
  return $uRequest({
    url: '/api/inbounds',
    method: 'POST',
    data
  })
}
