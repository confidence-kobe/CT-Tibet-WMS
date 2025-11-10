/**
 * 认证工具函数
 * 负责token的存储、获取和删除
 */

import Cookies from 'js-cookie'

// Token在Cookie中的键名
const TOKEN_KEY = 'wms_token'

// Token过期时间（天）
const TOKEN_EXPIRE_DAYS = 7

/**
 * 获取Token
 * @returns {string|undefined} 返回token字符串，如果不存在则返回undefined
 */
export function getToken() {
  return Cookies.get(TOKEN_KEY)
}

/**
 * 设置Token
 * @param {string} token - 要保存的token字符串
 * @param {number} expires - 过期天数，默认7天
 * @returns {string|undefined} 返回cookie值
 */
export function setToken(token, expires = TOKEN_EXPIRE_DAYS) {
  return Cookies.set(TOKEN_KEY, token, { expires })
}

/**
 * 移除Token
 */
export function removeToken() {
  return Cookies.remove(TOKEN_KEY)
}

/**
 * 检查是否已登录（是否存在token）
 * @returns {boolean} 存在token返回true，否则返回false
 */
export function isAuthenticated() {
  const token = getToken()
  return !!token
}

/**
 * 从localStorage获取刷新令牌
 * @returns {string|null} 返回刷新令牌
 */
export function getRefreshToken() {
  return localStorage.getItem('wms_refresh_token')
}

/**
 * 保存刷新令牌到localStorage
 * @param {string} refreshToken - 刷新令牌
 */
export function setRefreshToken(refreshToken) {
  localStorage.setItem('wms_refresh_token', refreshToken)
}

/**
 * 移除刷新令牌
 */
export function removeRefreshToken() {
  localStorage.removeItem('wms_refresh_token')
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  removeToken()
  removeRefreshToken()
}
