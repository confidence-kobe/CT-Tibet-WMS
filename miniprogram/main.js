import Vue from 'vue'
import App from './App'
import store from './store'

// uni-app API Promise化
import {
  $uRequest
} from './utils/request.js'

Vue.config.productionTip = false

// 挂载全局请求方法
Vue.prototype.$request = $uRequest

// 全局方法
Vue.prototype.$toast = (title, icon = 'none', duration = 2000) => {
  uni.showToast({
    title,
    icon,
    duration
  })
}

Vue.prototype.$loading = (title = '加载中...') => {
  uni.showLoading({
    title,
    mask: true
  })
}

Vue.prototype.$hideLoading = () => {
  uni.hideLoading()
}

Vue.prototype.$modal = (content, title = '提示') => {
  return new Promise((resolve, reject) => {
    uni.showModal({
      title,
      content,
      success: (res) => {
        if (res.confirm) {
          resolve(true)
        } else {
          resolve(false)
        }
      },
      fail: () => {
        reject(false)
      }
    })
  })
}

// 格式化日期
Vue.prototype.$formatDate = (date, format = 'yyyy-MM-dd HH:mm:ss') => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('yyyy', year)
    .replace('MM', month)
    .replace('dd', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

// 格式化金额
Vue.prototype.$formatMoney = (money) => {
  if (!money && money !== 0) return '0.00'
  return Number(money).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

App.mpType = 'app'

const app = new Vue({
  store,
  ...App
})
app.$mount()
