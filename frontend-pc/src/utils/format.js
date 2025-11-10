/**
 * 格式化工具函数
 * 用于数据展示的格式化处理
 */

import dayjs from 'dayjs'
import {
  ROLE_NAME_MAP,
  APPLY_STATUS_NAME,
  OUTBOUND_STATUS_NAME,
  INBOUND_TYPE_NAME,
  OUTBOUND_TYPE_NAME,
  MESSAGE_TYPE,
  STOCK_STATUS_NAME,
  DATE_FORMAT
} from '@/constants'

/**
 * 格式化日期时间
 * @param {string|Date} date - 日期
 * @param {string} format - 格式模板
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = DATE_FORMAT.DATETIME) {
  if (!date) return '-'
  return dayjs(date).format(format)
}

/**
 * 格式化角色名称
 * @param {string} roleCode - 角色编码
 * @returns {string} 角色名称
 */
export function formatRoleName(roleCode) {
  return ROLE_NAME_MAP[roleCode] || '未知角色'
}

/**
 * 格式化申请单状态
 * @param {number} status - 状态值
 * @returns {string} 状态名称
 */
export function formatApplyStatus(status) {
  return APPLY_STATUS_NAME[status] || '未知状态'
}

/**
 * 格式化出库单状态
 * @param {number} status - 状态值
 * @returns {string} 状态名称
 */
export function formatOutboundStatus(status) {
  return OUTBOUND_STATUS_NAME[status] || '未知状态'
}

/**
 * 格式化入库类型
 * @param {number} type - 类型值
 * @returns {string} 类型名称
 */
export function formatInboundType(type) {
  return INBOUND_TYPE_NAME[type] || '未知类型'
}

/**
 * 格式化出库类型
 * @param {number} type - 类型值
 * @returns {string} 类型名称
 */
export function formatOutboundType(type) {
  return OUTBOUND_TYPE_NAME[type] || '未知类型'
}

/**
 * 格式化库存状态
 * @param {string} status - 状态值
 * @returns {string} 状态名称
 */
export function formatStockStatus(status) {
  return STOCK_STATUS_NAME[status] || '未知状态'
}

/**
 * 格式化金额（保留两位小数）
 * @param {number} amount - 金额
 * @returns {string} 格式化后的金额字符串
 */
export function formatMoney(amount) {
  if (amount === null || amount === undefined) return '¥0.00'
  return `¥${Number(amount).toFixed(2)}`
}

/**
 * 格式化数量（保留两位小数）
 * @param {number} quantity - 数量
 * @param {string} unit - 单位
 * @returns {string} 格式化后的数量字符串
 */
export function formatQuantity(quantity, unit = '') {
  if (quantity === null || quantity === undefined) return '0'
  const formatted = Number(quantity).toFixed(2)
  return unit ? `${formatted} ${unit}` : formatted
}

/**
 * 格式化手机号（隐藏中间4位）
 * @param {string} phone - 手机号
 * @returns {string} 格式化后的手机号
 */
export function formatPhone(phone) {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 格式化相对时间（多久之前）
 * @param {string|Date} date - 日期
 * @returns {string} 相对时间描述
 */
export function formatRelativeTime(date) {
  if (!date) return '-'

  const now = dayjs()
  const target = dayjs(date)
  const diff = now.diff(target, 'second')

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  if (diff < 2592000) return `${Math.floor(diff / 86400)}天前`
  if (diff < 31536000) return `${Math.floor(diff / 2592000)}个月前`
  return `${Math.floor(diff / 31536000)}年前`
}

/**
 * 格式化百分比
 * @param {number} value - 数值
 * @param {number} total - 总数
 * @param {number} precision - 精度（小数位数）
 * @returns {string} 百分比字符串
 */
export function formatPercent(value, total, precision = 2) {
  if (!total || total === 0) return '0%'
  const percent = (value / total) * 100
  return `${percent.toFixed(precision)}%`
}

/**
 * 截断文本（添加省略号）
 * @param {string} text - 文本
 * @param {number} length - 最大长度
 * @returns {string} 截断后的文本
 */
export function truncate(text, length = 20) {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}

/**
 * 高亮关键词
 * @param {string} text - 原始文本
 * @param {string} keyword - 关键词
 * @returns {string} HTML字符串
 */
export function highlightKeyword(text, keyword) {
  if (!text || !keyword) return text
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

/**
 * 数字千分位格式化
 * @param {number} num - 数字
 * @returns {string} 格式化后的数字字符串
 */
export function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 生成随机颜色
 * @returns {string} 十六进制颜色值
 */
export function randomColor() {
  return '#' + Math.floor(Math.random() * 16777215).toString(16)
}

/**
 * 首字母大写
 * @param {string} str - 字符串
 * @returns {string} 首字母大写的字符串
 */
export function capitalize(str) {
  if (!str) return ''
  return str.charAt(0).toUpperCase() + str.slice(1)
}
