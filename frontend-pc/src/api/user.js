/**
 * 用户管理API接口
 * 包含用户的增删改查、状态更新、密码重置等接口
 */

import request from './request'

/**
 * 分页查询用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页条数
 * @param {number} params.deptId - 部门ID
 * @param {number} params.roleId - 角色ID
 * @param {number} params.status - 状态 (0-启用 1-禁用)
 * @param {string} params.keyword - 关键词（用户名或真实姓名）
 * @returns {Promise} 返回分页数据
 */
export function listUsers(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

/**
 * 查询用户详情
 * @param {number} id - 用户ID
 * @returns {Promise} 返回用户详细信息
 */
export function getUserById(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 * @param {Object} data - 用户数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.realName - 真实姓名
 * @param {string} data.phone - 手机号
 * @param {string} data.email - 邮箱
 * @param {number} data.deptId - 部门ID
 * @param {number} data.roleId - 角色ID
 * @param {string} data.remark - 备注
 * @returns {Promise} 返回创建结果
 */
export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 * @param {number} id - 用户ID
 * @param {Object} data - 用户数据
 * @returns {Promise} 返回更新结果
 */
export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 * @returns {Promise} 返回删除结果
 */
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

/**
 * 更新用户状态
 * @param {number} id - 用户ID
 * @param {number} status - 状态 (0-启用 1-禁用)
 * @returns {Promise} 返回更新结果
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 重置用户密码
 * @param {number} id - 用户ID
 * @param {string} newPassword - 新密码
 * @returns {Promise} 返回重置结果
 */
export function resetUserPassword(id, newPassword) {
  return request({
    url: `/users/${id}/reset-password`,
    method: 'put',
    params: { newPassword }
  })
}
