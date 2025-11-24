/**
 * Axios请求封装
 * 提供统一的HTTP请求配置和拦截器
 */

import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || 'http://localhost:48888/api',
  timeout: 15000, // 请求超时时间 15秒
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 如果存在token，在请求头中添加token
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 请求错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是200，则判定为错误
    if (res.code !== 200) {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 3000
      })

      // 401: 未授权，token过期或无效
      if (res.code === 401) {
        ElMessageBox.confirm(
          '您的登录状态已过期，请重新登录',
          '登录过期',
          {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          // 清除token并跳转到登录页
          removeToken()
          router.push('/login')
          location.reload()
        })
      }

      // 403: 无权限访问
      if (res.code === 403) {
        ElMessage({
          message: '您没有权限执行此操作',
          type: 'error',
          duration: 3000
        })
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      // 请求成功，返回数据
      return res
    }
  },
  error => {
    // 网络错误或服务器错误处理
    console.error('响应错误:', error)

    let message = '网络错误，请稍后重试'

    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          removeToken()
          router.push('/login')
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = error.response.data.message || '请求失败'
      }
    } else if (error.request) {
      message = '无法连接到服务器，请检查网络'
    }

    ElMessage({
      message: message,
      type: 'error',
      duration: 3000
    })

    return Promise.reject(error)
  }
)

// 导出axios实例
export default service
