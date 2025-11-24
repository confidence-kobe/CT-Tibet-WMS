/**
 * 部门管理API接口
 * 包含部门的增删改查等接口
 */

import request from './request'

/**
 * 查询部门树
 * @returns {Promise} 返回树形结构的部门列表
 */
export function listDeptTree() {
  return request({
    url: '/depts/tree',
    method: 'get'
  })
}

/**
 * 查询所有部门
 * @returns {Promise} 返回平铺列表的所有部门
 */
export function listAllDepts() {
  return request({
    url: '/depts/all',
    method: 'get'
  })
}

/**
 * 查询部门详情
 * @param {number} id - 部门ID
 * @returns {Promise} 返回部门详细信息
 */
export function getDeptById(id) {
  return request({
    url: `/depts/${id}`,
    method: 'get'
  })
}

/**
 * 创建部门
 * @param {Object} data - 部门数据
 * @param {string} data.deptName - 部门名称
 * @param {number} data.parentId - 父部门ID (0表示顶级部门)
 * @param {number} data.orderNum - 排序号
 * @param {string} data.remark - 备注
 * @returns {Promise} 返回创建结果
 */
export function createDept(data) {
  return request({
    url: '/depts',
    method: 'post',
    data
  })
}

/**
 * 更新部门
 * @param {number} id - 部门ID
 * @param {Object} data - 部门数据
 * @returns {Promise} 返回更新结果
 */
export function updateDept(id, data) {
  return request({
    url: `/depts/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除部门
 * @param {number} id - 部门ID
 * @returns {Promise} 返回删除结果
 */
export function deleteDept(id) {
  return request({
    url: `/depts/${id}`,
    method: 'delete'
  })
}
