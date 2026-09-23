/**
 * 网络请求封装
 */

// 开发环境配置
// 注意：真机调试时请将 localhost 修改为电脑的局域网 IP
const DEV_BASE_URL = 'http://localhost:48888'
// 生产环境配置
const PROD_BASE_URL = 'https://wms.chinatelecom.cn'

// 根据环境选择 baseURL
const BASE_URL = process.env.NODE_ENV === 'development' ? DEV_BASE_URL : PROD_BASE_URL

// 防止重复弹窗
let isReloginShow = false

/**
 * 是否为登录请求（登录失败返回的 401 不应当作"登录过期"处理）
 */
function isLoginRequest(url = '') {
  return url.indexOf('/api/auth/login') === 0 || url.indexOf('/api/auth/wechat-login') === 0
}

/**
 * 去掉值为 null / undefined / 空字符串 的参数
 */
function cleanParams(params = {}) {
  const result = {}
  Object.keys(params || {}).forEach(key => {
    const value = params[key]
    if (value !== null && value !== undefined && value !== '') {
      result[key] = value
    }
  })
  return result
}

/**
 * 通用请求函数
 */
export function $uRequest(options = {}) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    const method = options.method || 'GET'

    uni.request({
      url: BASE_URL + options.url,
      method,
      // GET 请求去掉值为 null/undefined 的参数，避免拼出 "status=null" 这类无效查询条件
      data: method === 'GET' ? cleanParams(options.data) : (options.data || {}),
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      timeout: options.timeout || 30000,
      success: (res) => {
        const data = res.data

        // HTTP 状态码处理
        if (res.statusCode === 200 || res.statusCode === 201) {
          // 业务状态码处理
          if (data.code === 200 || data.code === 201) {
            resolve(data)
          } else if ((data.code === 401 || data.code === 1010) && !isLoginRequest(options.url)) {
            handleUnauthorized()
            reject(data)
          } else {
            handleBusinessError(data, reject)
          }
        } else if (res.statusCode === 401 && isLoginRequest(options.url)) {
          // 登录接口返回 401 表示账号或密码错误，不是登录过期
          uni.showToast({
            title: (data && data.message) || '用户名或密码错误',
            icon: 'none'
          })
          reject(data)
        } else if (res.statusCode === 401) {
          handleUnauthorized()
          reject(data)
        } else if (res.statusCode === 403) {
          uni.showToast({
            title: '拒绝访问：权限不足',
            icon: 'none'
          })
          reject(data)
        } else {
          uni.showToast({
            title: data.message || '请求失败',
            icon: 'none'
          })
          reject(data)
        }
      },
      fail: (err) => {
        console.error(`[Request Fail] ${options.url}`, err)
        let message = '网络连接失败，请检查网络设置'
        if (err.errMsg.includes('timeout')) {
          message = '请求超时，请重试'
        }
        uni.showToast({
          title: message,
          icon: 'none'
        })
        reject({ code: -1, message })
      }
    })
  })
}

/**
 * 分页请求：把后端分页返回 { data: [...], total, pageNum, pageSize }
 * 统一转换为 { data: { list, total, pageNum, pageSize, hasMore } }，页面只需读取 res.data.list
 */
export async function $uPageRequest(options = {}) {
  const res = await $uRequest(options)
  const list = Array.isArray(res.data) ? res.data : []
  const pageNum = res.pageNum || (options.data && options.data.pageNum) || 1
  const pageSize = res.pageSize || (options.data && options.data.pageSize) || list.length
  const total = typeof res.total === 'number' ? res.total : list.length
  return {
    ...res,
    data: {
      list,
      total,
      pageNum,
      pageSize,
      hasMore: pageNum * pageSize < total
    }
  }
}

/** 后端分页接口允许的最大每页条数 */
export const MAX_PAGE_SIZE = 100

/**
 * 拉取全部数据（用于下拉选择器）：按每页100条逐页请求直到取完
 * 返回格式与 $uPageRequest 一致
 */
export async function $uFetchAll(options = {}) {
  const all = []
  let pageNum = 1
  let res
  while (true) {
    res = await $uPageRequest({
      ...options,
      data: { ...(options.data || {}), pageNum, pageSize: MAX_PAGE_SIZE }
    })
    all.push(...res.data.list)
    if (!res.data.hasMore) break
    pageNum++
  }
  return { ...res, data: { list: all, total: all.length, pageNum: 1, pageSize: all.length, hasMore: false } }
}

/**
 * 业务错误处理
 */
function handleBusinessError(data, reject) {
  const errorMap = {
    1001: '库存不足',
    1002: '申请单状态错误',
    1003: '审批流程异常',
    1004: '操作已取消',
    1005: '参数校验失败',
    1006: '系统繁忙',
    1007: '用户名或密码错误',
    1008: '手机号已存在',
    1009: '原密码错误'
  }

  const message = errorMap[data.code] || data.message || '未知业务错误'
  
  uni.showToast({
    title: message,
    icon: 'none'
  })

  reject(data)
}

/**
 * 登录失效处理
 */
function handleUnauthorized() {
  if (isReloginShow) return
  isReloginShow = true

  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')

  uni.showModal({
    title: '登录过期',
    content: '您的登录状态已过期，请重新登录',
    showCancel: false,
    success: (res) => {
      if (res.confirm) {
        isReloginShow = false
        uni.reLaunch({
          url: '/pages/login/login'
        })
      }
    }
  })
}

/**
 * 文件上传函数
 */
export function $uUpload(filePath, options = {}) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    uni.uploadFile({
      url: BASE_URL + (options.url || '/api/upload'),
      filePath: filePath,
      name: options.name || 'file',
      formData: options.formData || {},
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (res.statusCode === 200 && data.code === 200) {
          resolve(data)
        } else {
          uni.showToast({
            title: data.message || '上传失败',
            icon: 'none'
          })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default {
  $uRequest,
  $uPageRequest,
  $uFetchAll,
  $uUpload
}
