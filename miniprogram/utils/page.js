/**
 * 分页接口返回值转换
 * 后端分页接口返回：{ code, data: [...记录], total, pageNum, pageSize, pages }
 * 页面统一使用 { list, total }
 */
export function toPage(res) {
  const data = res && res.data
  let list = []
  if (Array.isArray(data)) {
    list = data
  } else if (data && Array.isArray(data.list)) {
    list = data.list
  } else if (data && Array.isArray(data.records)) {
    list = data.records
  }
  const total = res && res.total != null ? res.total : (data && data.total) || list.length
  return { list, total: Number(total) || 0 }
}
