/**
 * 公共API接口
 * 包含物资、仓库、部门等基础数据查询
 */
import { $uRequest, $uUpload } from '@/utils/request.js'

/**
 * 查询物资列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.categoryId - 分类ID（可选）
 * @param {number} params.status - 状态（可选）0-启用 1-禁用
 * @param {string} params.keyword - 关键词（可选）
 * @returns {Promise} 返回物资列表
 */
export function getMaterials(params) {
  return $uRequest({
    url: '/api/materials',
    method: 'GET',
    data: params
  })
}

/**
 * 查询物资详情
 * @param {number} id - 物资ID
 * @returns {Promise} 返回物资详细信息
 */
export function getMaterialById(id) {
  return $uRequest({
    url: `/api/materials/${id}`,
    method: 'GET'
  })
}

/**
 * 搜索物资（用于选择器）
 * @param {string} keyword - 关键词
 * @returns {Promise} 返回物资列表
 */
export function searchMaterials(keyword) {
  return $uRequest({
    url: '/api/materials/search',
    method: 'GET',
    data: {
      keyword,
      status: 0 // 只查询启用状态的物资
    }
  })
}

/**
 * 查询仓库列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.deptId - 部门ID（可选）
 * @param {number} params.status - 状态（可选）0-启用 1-禁用
 * @param {string} params.keyword - 关键词（可选）
 * @returns {Promise} 返回仓库列表
 */
export function getWarehouses(params) {
  return $uRequest({
    url: '/api/warehouses',
    method: 'GET',
    data: params
  })
}

/**
 * 查询仓库详情
 * @param {number} id - 仓库ID
 * @returns {Promise} 返回仓库详细信息
 */
export function getWarehouseById(id) {
  return $uRequest({
    url: `/api/warehouses/${id}`,
    method: 'GET'
  })
}

/**
 * 查询我的仓库列表（当前用户有权限的仓库）
 * @returns {Promise} 返回仓库列表
 */
export function getMyWarehouses() {
  return $uRequest({
    url: '/api/warehouses/my',
    method: 'GET'
  })
}

/**
 * 查询部门列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 返回部门列表
 */
export function getDepartments(params) {
  return $uRequest({
    url: '/api/depts',
    method: 'GET',
    data: params
  })
}

/**
 * 查询部门详情
 * @param {number} id - 部门ID
 * @returns {Promise} 返回部门详细信息
 */
export function getDepartmentById(id) {
  return $uRequest({
    url: `/api/depts/${id}`,
    method: 'GET'
  })
}

/**
 * 文件上传
 * @param {string} filePath - 本地文件路径
 * @param {Object} options - 上传选项
 * @param {string} options.name - 文件字段名，默认file
 * @param {Object} options.formData - 额外的表单数据
 * @returns {Promise} 返回上传结果（文件URL）
 */
export function uploadFile(filePath, options = {}) {
  return $uUpload(filePath, {
    url: '/api/upload',
    name: options.name || 'file',
    formData: options.formData || {}
  })
}

/**
 * 查询物资分类列表
 * @returns {Promise} 返回物资分类树形列表
 */
export function getMaterialCategories() {
  return $uRequest({
    url: '/api/materials/categories',
    method: 'GET'
  })
}

/**
 * 查询数据字典
 * @param {string} dictType - 字典类型
 * @returns {Promise} 返回字典数据列表
 */
export function getDictData(dictType) {
  return $uRequest({
    url: '/api/dict/data',
    method: 'GET',
    data: {
      dictType
    }
  })
}

/**
 * 查询统计数据（首页）
 * @returns {Promise} 返回统计数据
 */
export function getStatistics() {
  return $uRequest({
    url: '/api/statistics/overview',
    method: 'GET'
  })
}

export default {
  getMaterials,
  getMaterialById,
  searchMaterials,
  getWarehouses,
  getWarehouseById,
  getMyWarehouses,
  getDepartments,
  getDepartmentById,
  uploadFile,
  getMaterialCategories,
  getDictData,
  getStatistics
}
