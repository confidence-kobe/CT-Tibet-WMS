/**
 * Pinia Store入口文件
 * 统一导出所有store模块
 */

// 导入store模块
export { useUserStore } from './modules/user'
export { useAppStore } from './modules/app'

// 导出Pinia实例用于在main.js中注册
export * from 'pinia'
