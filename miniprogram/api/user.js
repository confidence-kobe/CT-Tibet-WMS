/**
 * 用户管理相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 用户列表
 * @param {Object} params - 查询参数
 */
export function getUserList(params) {
  return $uRequest({
    url: '/api/users',
    method: 'GET',
    data: params
  })
}

/**
 * 用户详情
 * @param {Number} id - 用户ID
 */
export function getUserDetail(id) {
  return $uRequest({
    url: `/api/users/${id}`,
    method: 'GET'
  })
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @param {String} data.oldPassword - 旧密码
 * @param {String} data.newPassword - 新密码
 * @param {String} data.confirmPassword - 确认密码
 */
export function changePassword(data) {
  return $uRequest({
    url: '/api/users/change-password',
    method: 'PUT',
    data
  })
}
