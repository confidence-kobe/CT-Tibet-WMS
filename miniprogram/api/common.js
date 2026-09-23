/**
 * 公共接口：物资、仓库、部门、用户选项、首页统计
 */

import { $uRequest, $uFetchAll } from '@/utils/request.js'

/**
 * 获取全部物资（自动分页拉取，适用于选择器）
 * @param {Object} params - 查询参数，如 { status: 0, category, keyword }
 * @returns {Promise<{ data: { list, total } }>}
 */
export function getMaterials(params = {}) {
  const { pageNum, pageSize, ...query } = params
  return $uFetchAll({
    url: '/api/materials',
    method: 'GET',
    data: query
  })
}

/**
 * 获取物资详情
 */
export function getMaterialById(id) {
  return $uRequest({
    url: `/api/materials/${id}`,
    method: 'GET'
  })
}

/**
 * 搜索启用状态的物资（最多返回100条）
 */
export function searchMaterials(keyword) {
  return $uRequest({
    url: '/api/materials/search',
    method: 'GET',
    data: {
      keyword,
      status: 0
    }
  })
}

/**
 * 获取物资类别列表
 */
export function getCategories() {
  return $uRequest({
    url: '/api/materials/categories',
    method: 'GET'
  })
}

/**
 * 获取当前用户可用的仓库（系统管理员为全部仓库，其他角色为本部门仓库）
 */
export function getWarehouses() {
  return $uRequest({
    url: '/api/warehouses/my',
    method: 'GET'
  })
}

/**
 * 获取仓库详情
 */
export function getWarehouseById(id) {
  return $uRequest({
    url: `/api/warehouses/${id}`,
    method: 'GET'
  })
}

/**
 * 获取全部部门
 */
export function getDepartments() {
  return $uRequest({
    url: '/api/depts/all',
    method: 'GET'
  })
}

/**
 * 获取可选的领用人（本部门启用用户；系统管理员为全部）
 * @param {Object} params - { keyword }
 * @returns {Promise<{ data: Array<{ id, realName, phone, deptName }> }>}
 */
export function getUsers(params = {}) {
  return $uRequest({
    url: '/api/users/options',
    method: 'GET',
    data: params.keyword ? { keyword: params.keyword } : {}
  })
}

/**
 * 获取小程序首页统计数据（按角色返回员工视图或仓管视图）
 */
export function getStatistics() {
  return $uRequest({
    url: '/api/statistics/miniprogram',
    method: 'GET'
  })
}

export default {
  getMaterials,
  getMaterialById,
  searchMaterials,
  getCategories,
  getMaterialCategories: getCategories,
  getWarehouses,
  getMyWarehouses: getWarehouses,
  getWarehouseById,
  getDepartments,
  getUsers,
  getStatistics
}
