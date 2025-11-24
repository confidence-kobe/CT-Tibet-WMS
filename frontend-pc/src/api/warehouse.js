/**
 * 仓库管理API接口
 * 包含仓库的增删改查、状态更新等接口
 */

import request from './request'

/**
 * 查询仓库列表
 * @param {Object} params - 查询参数
 * @param {number} params.deptId - 部门ID
 * @param {number} params.status - 状态 (0-启用 1-禁用)
 * @returns {Promise} 返回仓库列表
 */
export function listWarehouses(params) {
  return request({
    url: '/warehouses',
    method: 'get',
    params
  })
}

/**
 * 查询仓库详情
 * @param {number} id - 仓库ID
 * @returns {Promise} 返回仓库详细信息
 */
export function getWarehouseById(id) {
  return request({
    url: `/warehouses/${id}`,
    method: 'get'
  })
}

/**
 * 创建仓库
 * @param {Object} data - 仓库数据
 * @param {string} data.warehouseName - 仓库名称
 * @param {number} data.deptId - 部门ID
 * @param {string} data.location - 仓库位置
 * @param {string} data.contact - 联系人
 * @param {string} data.phone - 联系电话
 * @param {string} data.remark - 备注
 * @returns {Promise} 返回创建结果
 */
export function createWarehouse(data) {
  return request({
    url: '/warehouses',
    method: 'post',
    data
  })
}

/**
 * 更新仓库
 * @param {number} id - 仓库ID
 * @param {Object} data - 仓库数据
 * @returns {Promise} 返回更新结果
 */
export function updateWarehouse(id, data) {
  return request({
    url: `/warehouses/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除仓库
 * @param {number} id - 仓库ID
 * @returns {Promise} 返回删除结果
 */
export function deleteWarehouse(id) {
  return request({
    url: `/warehouses/${id}`,
    method: 'delete'
  })
}

/**
 * 更新仓库状态
 * @param {number} id - 仓库ID
 * @param {number} status - 状态 (0-启用 1-禁用)
 * @returns {Promise} 返回更新结果
 */
export function updateWarehouseStatus(id, status) {
  return request({
    url: `/warehouses/${id}/status`,
    method: 'patch',
    params: { status }
  })
}
