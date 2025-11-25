/**
 * 认证授权相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {number} data.loginType - 登录类型：1=PC端 2=小程序
 * @returns {Promise} 返回token和用户信息
 */
export function login(data) {
  return $uRequest({
    url: '/api/auth/login',
    method: 'POST',
    data: {
      ...data,
      loginType: 2 // 小程序固定为2
    }
  })
}

/**
 * 微信小程序登录
 * @param {Object} data - 登录信息
 * @param {string} data.code - 微信登录凭证
 * @param {string} data.encryptedData - 加密数据
 * @param {string} data.iv - 加密算法初始向量
 * @returns {Promise} 返回token和用户信息
 */
export function wechatLogin(data) {
  return $uRequest({
    url: '/api/auth/wechat-login',
    method: 'POST',
    data
  })
}

/**
 * 刷新Token
 * @returns {Promise} 返回新的token
 */
export function refreshToken() {
  return $uRequest({
    url: '/api/auth/refresh-token',
    method: 'POST'
  })
}

/**
 * 退出登录
 * @returns {Promise} 返回退出结果
 */
export function logout() {
  return $uRequest({
    url: '/api/auth/logout',
    method: 'POST'
  })
}

/**
 * 获取当前用户信息
 * @returns {Promise} 返回用户详细信息
 */
export function getCurrentUser() {
  return $uRequest({
    url: '/api/auth/current-user',
    method: 'GET'
  })
}

export default {
  login,
  wechatLogin,
  refreshToken,
  logout,
  getCurrentUser
}
