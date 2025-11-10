/**
 * 自定义指令统一注册
 */

import permission from './permission'

/**
 * 注册所有自定义指令
 * @param {Object} app - Vue应用实例
 */
export function registerDirectives(app) {
  // 权限指令
  app.directive('permission', permission)

  // 可以继续添加其他自定义指令
  // app.directive('loading', loading)
  // app.directive('lazy', lazy)
}

// 也可以单独导出，供特定组件使用
export { permission }
