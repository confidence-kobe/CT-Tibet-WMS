/**
 * 物资管理相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 物资列表
 * @param {Object} params - 查询参数
 */
export function getMaterialList(params) {
  return $uRequest({
    url: '/api/materials',
    method: 'GET',
    data: params
  })
}

/**
 * 物资详情
 * @param {Number} id - 物资ID
 */
export function getMaterialDetail(id) {
  return $uRequest({
    url: `/api/materials/${id}`,
    method: 'GET'
  })
}

/**
 * 物资类别列表
 */
export function getMaterialCategories() {
  return $uRequest({
    url: '/api/materials/categories',
    method: 'GET'
  })
}
