/**
 * 认证授权相关API
 */
import { $uRequest } from '@/utils/request.js'

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {String} data.username - 用户名
 * @param {String} data.password - 密码
 * @param {Number} data.loginType - 登录类型：1=PC端 2=小程序
 */
export function login(data) {
  return $uRequest({
    url: '/api/auth/login',
    method: 'POST',
    data
  })
}

/**
 * 微信小程序登录
 * @param {Object} data - 登录信息
 * @param {String} data.code - 微信登录凭证
 * @param {String} data.encryptedData - 加密数据
 * @param {String} data.iv - 加密算法初始向量
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
 */
export function refreshToken() {
  return $uRequest({
    url: '/api/auth/refresh-token',
    method: 'POST'
  })
}

/**
 * 退出登录
 */
export function logout() {
  return $uRequest({
    url: '/api/auth/logout',
    method: 'POST'
  })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  return $uRequest({
    url: '/api/auth/current-user',
    method: 'GET'
  })
}
