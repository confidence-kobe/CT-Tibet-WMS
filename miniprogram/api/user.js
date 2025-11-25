/**
 * 用户管理相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 获取个人信息
 * @returns {Promise} 返回用户详细信息
 */
export function getUserProfile() {
  return $uRequest({
    url: '/api/users/profile',
    method: 'GET'
  })
}

/**
 * 更新个人信息
 * @param {Object} data - 用户信息
 * @param {string} data.realName - 真实姓名（可选）
 * @param {string} data.phone - 手机号（可选）
 * @param {string} data.email - 邮箱（可选）
 * @param {string} data.avatar - 头像URL（可选）
 * @returns {Promise} 返回更新结果
 */
export function updateProfile(data) {
  return $uRequest({
    url: '/api/users/profile',
    method: 'PUT',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 * @param {string} data.confirmPassword - 确认新密码
 * @returns {Promise} 返回修改结果
 */
export function changePassword(data) {
  return $uRequest({
    url: '/api/users/change-password',
    method: 'POST',
    data
  })
}

/**
 * 查询用户列表（仓管查看部门用户）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码，默认1
 * @param {number} params.pageSize - 每页条数，默认10
 * @param {number} params.deptId - 部门ID（可选）
 * @param {number} params.roleId - 角色ID（可选）
 * @param {string} params.keyword - 关键词（可选）
 * @returns {Promise} 返回用户列表
 */
export function getUserList(params) {
  return $uRequest({
    url: '/api/users',
    method: 'GET',
    data: params
  })
}

/**
 * 查询用户详情
 * @param {number} id - 用户ID
 * @returns {Promise} 返回用户详细信息
 */
export function getUserById(id) {
  return $uRequest({
    url: `/api/users/${id}`,
    method: 'GET'
  })
}

/**
 * 绑定微信
 * @param {Object} data - 绑定信息
 * @param {string} data.code - 微信登录凭证
 * @param {string} data.encryptedData - 加密数据
 * @param {string} data.iv - 加密算法初始向量
 * @returns {Promise} 返回绑定结果
 */
export function bindWechat(data) {
  return $uRequest({
    url: '/api/users/bind-wechat',
    method: 'POST',
    data
  })
}

/**
 * 解绑微信
 * @returns {Promise} 返回解绑结果
 */
export function unbindWechat() {
  return $uRequest({
    url: '/api/users/unbind-wechat',
    method: 'POST'
  })
}

export default {
  getUserProfile,
  updateProfile,
  changePassword,
  getUserList,
  getUserById,
  bindWechat,
  unbindWechat
}
