/**
 * 物资管理API接口
 * 包含物资的增删改查、状态更新、类别查询等接口
 */

import request from './request'

/**
 * 分页查询物资列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {string} params.category - 物资类别
 * @param {number} params.status - 状态 (0-启用 1-停用)
 * @param {string} params.keyword - 关键词（物资编码或名称）
 * @returns {Promise} 返回分页数据
 */
export function listMaterials(params) {
  return request({
    url: '/materials',
    method: 'get',
    params
  })
}

/** 后端分页接口允许的最大每页条数 */
const MAX_PAGE_SIZE = 100

/**
 * 查询全部物资（用于下拉选择器）
 * 后端每页最多返回100条，这里自动逐页拉取直到取完
 * @param {Object} params - 查询参数（同 listMaterials，无需分页参数）
 * @returns {Promise<Array>} 返回物资数组
 */
export async function listAllMaterials(params = {}) {
  const all = []
  let pageNum = 1
  while (true) {
    const res = await listMaterials({ ...params, pageNum, pageSize: MAX_PAGE_SIZE })
    const records = res.data || []
    all.push(...records)
    if (records.length < MAX_PAGE_SIZE || all.length >= (res.total || 0)) {
      return all
    }
    pageNum++
  }
}

/**
 * 查询物资详情
 * @param {number} id - 物资ID
 * @returns {Promise} 返回物资详细信息
 */
export function getMaterialById(id) {
  return request({
    url: `/materials/${id}`,
    method: 'get'
  })
}

/**
 * 创建物资
 * @param {Object} data - 物资数据
 * @param {string} data.code - 物资编码
 * @param {string} data.name - 物资名称
 * @param {string} data.category - 物资类别
 * @param {string} data.model - 规格型号
 * @param {string} data.unit - 单位
 * @param {number} data.minStock - 最低库存
 * @param {number} data.maxStock - 最高库存
 * @param {string} data.remark - 备注
 * @returns {Promise} 返回创建结果
 */
export function createMaterial(data) {
  return request({
    url: '/materials',
    method: 'post',
    data
  })
}

/**
 * 更新物资
 * @param {number} id - 物资ID
 * @param {Object} data - 物资数据
 * @returns {Promise} 返回更新结果
 */
export function updateMaterial(id, data) {
  return request({
    url: `/materials/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除物资
 * @param {number} id - 物资ID
 * @returns {Promise} 返回删除结果
 */
export function deleteMaterial(id) {
  return request({
    url: `/materials/${id}`,
    method: 'delete'
  })
}

/**
 * 更新物资状态
 * @param {number} id - 物资ID
 * @param {number} status - 状态 (0-启用 1-停用)
 * @returns {Promise} 返回更新结果
 */
export function updateMaterialStatus(id, status) {
  return request({
    url: `/materials/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

/**
 * 获取物资类别列表
 * @returns {Promise} 返回所有物资类别（去重）
 */
export function getMaterialCategories() {
  return request({
    url: '/materials/categories',
    method: 'get'
  })
}
