/**
 * 用户状态管理模块
 * 管理用户登录状态、用户信息、角色和权限
 */

import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken, setRefreshToken, clearAuth } from '@/utils/auth'
import { asyncRoutes, constantRoutes } from '@/router'

/**
 * 过滤异步路由
 * 根据用户角色过滤出可访问的路由
 * @param {Array} routes - 路由配置数组
 * @param {Array} roles - 用户角色数组
 * @returns {Array} 过滤后的路由数组
 */
function filterAsyncRoutes(routes, roles) {
  const res = []

  routes.forEach(route => {
    const tmp = { ...route }
    if (hasPermission(roles, tmp)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, roles)
      }
      res.push(tmp)
    }
  })

  return res
}

/**
 * 判断是否有权限访问路由
 * @param {Array} roles - 用户角色数组
 * @param {Object} route - 路由对象
 * @returns {boolean} 有权限返回true
 */
function hasPermission(roles, route) {
  if (route.meta && route.meta.roles) {
    return roles.some(role => route.meta.roles.includes(role))
  } else {
    return true
  }
}

export const useUserStore = defineStore('user', {
  // 状态
  state: () => ({
    token: getToken(), // 访问令牌
    userInfo: null, // 用户信息
    roles: [], // 用户角色列表
    permissions: [], // 用户权限列表
    routes: [] // 用户可访问的路由
  }),

  // 计算属性
  getters: {
    // 用户ID
    userId: (state) => state.userInfo?.id,
    // 用户名
    username: (state) => state.userInfo?.username,
    // 真实姓名
    realName: (state) => state.userInfo?.realName,
    // 头像
    avatar: (state) => state.userInfo?.avatar,
    // 角色编码
    roleCode: (state) => state.userInfo?.roleCode,
    // 部门名称
    deptName: (state) => state.userInfo?.deptName,
    // 手机号
    phone: (state) => state.userInfo?.phone,
    // 邮箱
    email: (state) => state.userInfo?.email,
    // 最后登录时间
    lastLoginTime: (state) => state.userInfo?.lastLoginTime,
    // 最后登录IP
    lastLoginIp: (state) => state.userInfo?.lastLoginIp,
    // 是否已登录
    isLoggedIn: (state) => !!state.token,
    // 是否是管理员（使用数据库中的角色编码）
    isAdmin: (state) => state.roles.includes('admin')
  },

  // 操作
  actions: {
    /**
     * 用户登录
     * @param {Object} loginForm - 登录表单
     * @param {string} loginForm.username - 用户名
     * @param {string} loginForm.password - 密码
     * @param {string} loginForm.loginType - 登录类型
     */
    async login(loginForm) {
      const { username, password, loginType } = loginForm
      try {
        const response = await login(username.trim(), password, loginType)
        const { accessToken, refreshToken } = response.data

        // 保存token
        this.token = accessToken
        setToken(accessToken)
        setRefreshToken(refreshToken)

        return response
      } catch (error) {
        console.error('登录失败:', error)
        throw error
      }
    },

    /**
     * 获取用户信息
     */
    async getUserInfo() {
      try {
        const response = await getUserInfo()
        const { user, roles, permissions } = response.data

        // 验证返回的数据
        if (!user) {
          throw new Error('用户信息获取失败')
        }

        if (!roles || roles.length === 0) {
          throw new Error('用户角色不能为空')
        }

        // 保存用户信息
        this.userInfo = user
        this.roles = roles
        this.permissions = permissions || []

        return response.data
      } catch (error) {
        console.error('获取用户信息失败:', error)
        throw error
      }
    },

    /**
     * 生成路由
     * 根据用户角色过滤异步路由
     * @returns {Array} 可访问的路由数组
     */
    async generateRoutes() {
      // 管理员拥有所有权限
      const accessedRoutes = this.isAdmin
        ? asyncRoutes
        : filterAsyncRoutes(asyncRoutes, this.roles)

      // 合并静态路由和动态路由
      this.routes = constantRoutes.concat(accessedRoutes)

      return accessedRoutes
    },

    /**
     * 用户登出
     */
    async logout() {
      try {
        // 调用登出接口
        await logout()
      } catch (error) {
        console.error('登出失败:', error)
      } finally {
        // 清除本地数据
        this.resetState()
      }
    },

    /**
     * 重置状态
     * 清除所有用户相关数据
     */
    resetState() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      this.permissions = []
      this.routes = []
      clearAuth()
    },

    /**
     * 更新token
     * @param {string} token - 新的访问令牌
     */
    updateToken(token) {
      this.token = token
      setToken(token)
    },

    /**
     * 检查权限
     * @param {string|Array} permission - 权限标识或权限数组
     * @returns {boolean} 有权限返回true
     */
    hasPermission(permission) {
      if (!permission) return true
      if (this.isAdmin) return true

      if (Array.isArray(permission)) {
        return permission.some(p => this.permissions.includes(p))
      }
      return this.permissions.includes(permission)
    },

    /**
     * 检查角色
     * @param {string|Array} role - 角色标识或角色数组
     * @returns {boolean} 有角色返回true
     */
    hasRole(role) {
      if (!role) return true
      if (this.isAdmin) return true

      if (Array.isArray(role)) {
        return role.some(r => this.roles.includes(r))
      }
      return this.roles.includes(role)
    }
  },

  // 持久化配置（需要安装 pinia-plugin-persistedstate 插件）
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'user',
        storage: localStorage,
        paths: ['userInfo', 'roles', 'permissions'] // 只持久化这些字段
      }
    ]
  }
})
