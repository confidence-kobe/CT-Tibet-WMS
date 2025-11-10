/**
 * 权限指令
 * 用于控制按钮、元素的显示/隐藏
 *
 * 使用方式：
 * <el-button v-permission="['admin']">仅管理员可见</el-button>
 * <el-button v-permission:any="['admin', 'warehouse']">管理员或仓管可见</el-button>
 */

import { useUserStore } from '@/store'

/**
 * 检查权限
 * @param {Array|String} value - 权限值（角色或权限标识）
 * @param {String} modifier - 修饰符（any表示满足任一权限即可，默认需要全部满足）
 * @returns {boolean} 是否有权限
 */
function checkPermission(value, modifier = 'all') {
  const userStore = useUserStore()

  // 如果是管理员，拥有所有权限
  if (userStore.isAdmin) return true

  // 处理权限值
  const permissions = Array.isArray(value) ? value : [value]
  if (permissions.length === 0) return true

  // 获取用户角色和权限
  const { roles, permissions: userPermissions } = userStore

  // 检查角色权限
  const hasRole = modifier === 'any'
    ? permissions.some(permission => roles.includes(permission))
    : permissions.every(permission => roles.includes(permission))

  // 检查功能权限
  const hasPermission = modifier === 'any'
    ? permissions.some(permission => userPermissions.includes(permission))
    : permissions.every(permission => userPermissions.includes(permission))

  return hasRole || hasPermission
}

export default {
  mounted(el, binding) {
    const { value, modifiers } = binding

    // 如果没有传入权限值，默认显示
    if (!value) return

    // 获取修饰符（any 或 all）
    const modifier = modifiers.any ? 'any' : 'all'

    // 检查权限
    const hasPermission = checkPermission(value, modifier)

    // 如果没有权限，移除元素
    if (!hasPermission) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
