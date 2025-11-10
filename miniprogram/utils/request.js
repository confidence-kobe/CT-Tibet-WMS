/**
 * 网络请求封装
 */

// 开发环境
const DEV_BASE_URL = 'http://localhost:8080'
// 生产环境
const PROD_BASE_URL = 'https://wms.chinatelecom.cn'

// 根据环境选择baseURL
const BASE_URL = process.env.NODE_ENV === 'development' ? DEV_BASE_URL : PROD_BASE_URL

/**
 * 发起请求
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
        console.log(`[Request Success] ${options.url}`, res.data)

        const data = res.data

        // HTTP状态码检查
        if (res.statusCode === 200 || res.statusCode === 201) {
          // 业务状态码检查
          if (data.code === 200 || data.code === 201) {
            resolve(data)
          } else {
            handleBusinessError(data, reject)
          }
        } else if (res.statusCode === 401) {
          // 未授权，跳转登录
          handleUnauthorized()
          reject(data)
        } else if (res.statusCode === 403) {
          // 无权限
          uni.showToast({
            title: '无权限访问',
            icon: 'none',
            duration: 2000
          })
          reject(data)
        } else {
          // 其他HTTP错误
          uni.showToast({
            title: data.message || '请求失败',
            icon: 'none',
            duration: 2000
          })
          reject(data)
        }
      },
      fail: (err) => {
        console.error(`[Request Fail] ${options.url}`, err)

        // 网络错误处理
        let message = '网络连接失败，请检查网络设置'
        if (err.errMsg.includes('timeout')) {
          message = '请求超时，请稍后重试'
        }

        uni.showToast({
          title: message,
          icon: 'none',
          duration: 2000
        })

        reject({
          code: -1,
          message: message,
          error: err
        })
      }
    })
  })
}

/**
 * 处理业务错误
 */
function handleBusinessError(data, reject) {
  const errorMap = {
    1001: '库存不足',
    1002: '申请已被处理',
    1003: '审批超时',
    1004: '出库单已取消',
    1005: '物资已停用',
    1006: '仓库已禁用',
    1007: '用户名已存在',
    1008: '手机号已注册',
    1009: '密码错误',
    1010: 'Token已过期'
  }

  const message = errorMap[data.code] || data.message || '操作失败'

  // Token过期，跳转登录
  if (data.code === 1010) {
    handleUnauthorized()
  } else {
    uni.showToast({
      title: message,
      icon: 'none',
      duration: 2000
    })
  }

  reject(data)
}

/**
 * 处理未授权（401）
 */
function handleUnauthorized() {
  // 清除登录信息
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')

  uni.showToast({
    title: '登录已过期，请重新登录',
    icon: 'none',
    duration: 2000
  })

  // 延迟跳转，确保toast显示
  setTimeout(() => {
    uni.reLaunch({
      url: '/pages/login/login'
    })
  }, 1500)
}

/**
 * 文件上传
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
        console.log('[Upload Success]', res)
        const data = JSON.parse(res.data)

        if (res.statusCode === 200 && data.code === 200) {
          resolve(data)
        } else {
          uni.showToast({
            title: data.message || '上传失败',
            icon: 'none',
            duration: 2000
          })
          reject(data)
        }
      },
      fail: (err) => {
        console.error('[Upload Fail]', err)
        uni.showToast({
          title: '上传失败',
          icon: 'none',
          duration: 2000
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
