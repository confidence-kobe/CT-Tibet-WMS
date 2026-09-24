/**
 * 日期工具
 * 注意：不要用 toISOString() 提交时间，它是 UTC 时间，比西藏本地时间早 8 小时
 */

const pad = (n) => String(n).padStart(2, '0')

/**
 * 格式化为本地时间字符串
 * @param {Date} date - 日期，默认当前时间
 * @param {string} separator - 日期与时间之间的分隔符：' '（yyyy-MM-dd HH:mm:ss）或 'T'（ISO 格式）
 */
export function formatLocalDateTime(date = new Date(), separator = ' ') {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `${separator}${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

export default {
  formatLocalDateTime
}
