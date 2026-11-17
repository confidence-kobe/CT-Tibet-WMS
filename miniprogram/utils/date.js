/**
 * 当前本地时间，格式 yyyy-MM-dd HH:mm:ss（与后端 @JsonFormat 一致）。
 * 注意不要用 toISOString()：那是 UTC 时间，北京时间会差 8 小时。
 */
export function nowString(date = new Date()) {
  const pad = n => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
