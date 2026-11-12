/**
 * 分页接口全量加载
 * 后端每页最多返回 100 条，选择器需要完整列表时，按页循环拉取直到取完
 * 后端分页接口返回格式：{ code, data: [...], total, pageNum, pageSize }
 */

const PAGE_SIZE = 100
const MAX_PAGES = 50

/**
 * @param {Function} apiFn - 分页查询接口，如 api.common.getMaterials
 * @param {Object} params - 查询条件（不含分页参数）
 * @returns {Promise<Array>} 全部记录
 */
export async function fetchAllPages(apiFn, params = {}) {
  let all = []
  for (let pageNum = 1; pageNum <= MAX_PAGES; pageNum++) {
    const res = await apiFn({ ...params, pageNum, pageSize: PAGE_SIZE })
    const rows = Array.isArray(res.data) ? res.data : []
    all = all.concat(rows)
    if (rows.length < PAGE_SIZE || all.length >= (res.total ?? all.length)) {
      break
    }
  }
  return all
}
