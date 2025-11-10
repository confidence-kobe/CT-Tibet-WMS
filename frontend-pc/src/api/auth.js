/**
 * 认证相关API接口
 * 包含登录、登出、刷新令牌、获取用户信息等接口
 */

import request from './request'

/**
 * 用户登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @param {string} loginType - 登录类型 (PASSWORD/PHONE/WECHAT/ENTERPRISE_WECHAT)
 * @returns {Promise} 返回包含token的响应
 */
export function login(username, password, loginType = 'PASSWORD') {
  return request({
    url: '/auth/login',
    method: 'post',
    data: {
      username,
      password,
      loginType
    }
  })
}

/**
 * 用户登出
 * @returns {Promise} 返回登出结果
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 刷新访问令牌
 * @param {string} refreshToken - 刷新令牌
 * @returns {Promise} 返回新的访问令牌
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/auth/refresh-token',
    method: 'post',
    data: {
      refreshToken
    }
  })
}

/**
 * 获取当前登录用户信息
 * @returns {Promise} 返回用户详细信息
 */
export function getUserInfo() {
  return request({
    url: '/auth/user-info',
    method: 'get'
  })
}

/**
 * 修改密码
 * @param {string} oldPassword - 旧密码
 * @param {string} newPassword - 新密码
 * @returns {Promise} 返回修改结果
 */
export function changePassword(oldPassword, newPassword) {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data: {
      oldPassword,
      newPassword
    }
  })
}

/**
 * 忘记密码 - 发送验证码
 * @param {string} phone - 手机号
 * @returns {Promise} 返回发送结果
 */
export function sendResetCode(phone) {
  return request({
    url: '/auth/send-reset-code',
    method: 'post',
    data: {
      phone
    }
  })
}

/**
 * 忘记密码 - 重置密码
 * @param {string} phone - 手机号
 * @param {string} code - 验证码
 * @param {string} newPassword - 新密码
 * @returns {Promise} 返回重置结果
 */
export function resetPassword(phone, code, newPassword) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data: {
      phone,
      code,
      newPassword
    }
  })
}
