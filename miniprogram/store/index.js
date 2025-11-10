import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    // 用户信息
    userInfo: null,
    // 登录token
    token: null,
    // 系统信息
    systemInfo: {},
    // 未读消息数量
    unreadCount: 0,
    // 待办事项统计
    pendingTasks: {
      pendingApproval: 0,
      pendingPickup: 0,
      lowStockAlert: 0
    }
  },
  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      uni.setStorageSync('userInfo', userInfo)
    },
    SET_TOKEN(state, token) {
      state.token = token
      uni.setStorageSync('token', token)
    },
    SET_SYSTEM_INFO(state, systemInfo) {
      state.systemInfo = systemInfo
    },
    SET_UNREAD_COUNT(state, count) {
      state.unreadCount = count
      if (count > 0) {
        uni.setTabBarBadge({
          index: 3,
          text: count > 99 ? '99+' : String(count)
        })
      } else {
        uni.removeTabBarBadge({
          index: 3
        })
      }
    },
    SET_PENDING_TASKS(state, tasks) {
      state.pendingTasks = tasks
    },
    LOGOUT(state) {
      state.userInfo = null
      state.token = null
      state.unreadCount = 0
      state.pendingTasks = {
        pendingApproval: 0,
        pendingPickup: 0,
        lowStockAlert: 0
      }
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('token')
      uni.removeTabBarBadge({
        index: 3
      })
    }
  },
  actions: {
    // 获取未读消息数量
    async getUnreadCount({ commit }) {
      try {
        const res = await this.$request({
          url: '/api/messages/unread-count',
          method: 'GET'
        })
        if (res.code === 200) {
          commit('SET_UNREAD_COUNT', res.data.total || 0)
        }
      } catch (err) {
        console.error('获取未读消息数量失败', err)
      }
    },
    // 获取待办事项统计
    async getPendingTasks({ commit }) {
      try {
        const res = await this.$request({
          url: '/api/stats/dashboard',
          method: 'GET'
        })
        if (res.code === 200) {
          commit('SET_PENDING_TASKS', res.data.pendingTasks || {})
        }
      } catch (err) {
        console.error('获取待办事项统计失败', err)
      }
    }
  },
  getters: {
    // 是否已登录
    isLogin: state => !!state.token,
    // 用户角色代码
    roleCode: state => state.userInfo?.roleCode || '',
    // 是否是仓库管理员
    isWarehouse: state => ['admin', 'dept_admin', 'warehouse'].includes(state.userInfo?.roleCode),
    // 是否是普通员工
    isEmployee: state => state.userInfo?.roleCode === 'user',
    // TabBar列表（根据角色动态生成）
    tabBarList: (state, getters) => {
      const isWarehouse = getters.isWarehouse

      if (isWarehouse) {
        // 仓库管理员TabBar
        return [
          {
            pagePath: 'pages/index/index',
            text: '首页',
            iconPath: 'static/tabbar/home.png',
            selectedIconPath: 'static/tabbar/home-active.png'
          },
          {
            pagePath: 'pages/inbound/list',
            text: '入库',
            iconPath: 'static/tabbar/inbound.png',
            selectedIconPath: 'static/tabbar/inbound-active.png'
          },
          {
            pagePath: 'pages/outbound/list',
            text: '出库',
            iconPath: 'static/tabbar/outbound.png',
            selectedIconPath: 'static/tabbar/outbound-active.png'
          },
          {
            pagePath: 'pages/approval/list',
            text: '审批',
            iconPath: 'static/tabbar/approval.png',
            selectedIconPath: 'static/tabbar/approval-active.png'
          },
          {
            pagePath: 'pages/mine/mine',
            text: '我的',
            iconPath: 'static/tabbar/mine.png',
            selectedIconPath: 'static/tabbar/mine-active.png'
          }
        ]
      } else {
        // 普通员工TabBar
        return [
          {
            pagePath: 'pages/index/index',
            text: '首页',
            iconPath: 'static/tabbar/home.png',
            selectedIconPath: 'static/tabbar/home-active.png'
          },
          {
            pagePath: 'pages/apply/list',
            text: '申请',
            iconPath: 'static/tabbar/apply.png',
            selectedIconPath: 'static/tabbar/apply-active.png'
          },
          {
            pagePath: 'pages/inventory/list',
            text: '库存',
            iconPath: 'static/tabbar/inventory.png',
            selectedIconPath: 'static/tabbar/inventory-active.png'
          },
          {
            pagePath: 'pages/mine/mine',
            text: '我的',
            iconPath: 'static/tabbar/mine.png',
            selectedIconPath: 'static/tabbar/mine-active.png'
          }
        ]
      }
    }
  }
})

export default store
