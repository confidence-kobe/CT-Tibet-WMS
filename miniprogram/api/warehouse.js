/**
 * 仓库管理相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 仓库列表
 * @param {Object} params - 查询参数
 */
export function getWarehouseList(params) {
  return $uRequest({
    url: '/api/warehouses',
    method: 'GET',
    data: params
  })
}

/**
 * 仓库详情
 * @param {Number} id - 仓库ID
 */
export function getWarehouseDetail(id) {
  return $uRequest({
    url: `/api/warehouses/${id}`,
    method: 'GET'
  })
}
