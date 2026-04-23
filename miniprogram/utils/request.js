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
 * 通用请求函数
 */
export function $uRequest(options = {}) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
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
          } else if (data.code === 401 || data.code === 1010) {
            handleUnauthorized()
            reject(data)
          } else {
            handleBusinessError(data, reject)
          }
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
  $uUpload
}
