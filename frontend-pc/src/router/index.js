/**
 * Vue Router路由配置
 * 定义应用的路由规则和导航守卫
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { getToken } from '@/utils/auth'

/**
 * 静态路由配置
 * 无需权限即可访问的页面
 */
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      hidden: true // 不在菜单中显示
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '404',
      hidden: true
    }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '403',
      hidden: true
    }
  },
  {
    path: '/',
    redirect: '/dashboard',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '首页',
          icon: 'HomeFilled',
          affix: true // 固定在标签栏
        }
      }
    ]
  }
]

/**
 * 动态路由配置
 * 需要根据用户权限动态加载的页面
 */
export const asyncRoutes = [
  // 基础管理模块
  {
    path: '/basic',
    component: () => import('@/layout/index.vue'),
    redirect: '/basic/material',
    name: 'Basic',
    meta: {
      title: '基础管理',
      icon: 'Management',
      roles: ['admin', 'dept_admin', 'warehouse']
    },
    children: [
      {
        path: 'material',
        name: 'Material',
        component: () => import('@/views/basic/material/index.vue'),
        meta: {
          title: '物资管理',
          icon: 'Box',
          roles: ['admin', 'dept_admin']
        }
      },
      {
        path: 'warehouse',
        name: 'Warehouse',
        component: () => import('@/views/basic/warehouse/index.vue'),
        meta: {
          title: '仓库管理',
          icon: 'OfficeBuilding',
          roles: ['admin', 'dept_admin']
        }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('@/views/basic/dept/index.vue'),
        meta: {
          title: '部门管理',
          icon: 'Operation',
          roles: ['admin']
        }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/basic/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          roles: ['admin', 'dept_admin']
        }
      }
    ]
  },

  // 入库管理模块
  {
    path: '/inbound',
    component: () => import('@/layout/index.vue'),
    redirect: '/inbound/list',
    name: 'Inbound',
    meta: {
      title: '入库管理',
      icon: 'Download',
      roles: ['admin', 'dept_admin', 'warehouse']
    },
    children: [
      {
        path: 'list',
        name: 'InboundList',
        component: () => import('@/views/inbound/list/index.vue'),
        meta: {
          title: '入库单',
          icon: 'DocumentChecked'
        }
      },
      {
        path: 'create',
        name: 'InboundCreate',
        component: () => import('@/views/inbound/create/index.vue'),
        meta: {
          title: '新建入库单',
          icon: 'Plus',
          hidden: true // 不在菜单显示
        }
      },
      {
        path: 'detail/:id',
        name: 'InboundDetail',
        component: () => import('@/views/inbound/detail/index.vue'),
        meta: {
          title: '入库单详情',
          hidden: true
        }
      }
    ]
  },

  // 出库管理模块
  {
    path: '/outbound',
    component: () => import('@/layout/index.vue'),
    redirect: '/outbound/list',
    name: 'Outbound',
    meta: {
      title: '出库管理',
      icon: 'Upload',
      roles: ['admin', 'dept_admin', 'warehouse']
    },
    children: [
      {
        path: 'list',
        name: 'OutboundList',
        component: () => import('@/views/outbound/list/index.vue'),
        meta: {
          title: '出库单',
          icon: 'DocumentChecked'
        }
      },
      {
        path: 'create',
        name: 'OutboundCreate',
        component: () => import('@/views/outbound/create/index.vue'),
        meta: {
          title: '新建出库单',
          icon: 'Plus',
          hidden: true
        }
      },
      {
        path: 'detail/:id',
        name: 'OutboundDetail',
        component: () => import('@/views/outbound/detail/index.vue'),
        meta: {
          title: '出库单详情',
          hidden: true
        }
      },
      {
        path: 'confirm',
        name: 'OutboundConfirm',
        component: () => import('@/views/outbound/confirm/index.vue'),
        meta: {
          title: '确认领取',
          icon: 'CircleCheck'
        }
      }
    ]
  },

  // 申请管理模块（员工）
  {
    path: '/apply',
    component: () => import('@/layout/index.vue'),
    redirect: '/apply/list',
    name: 'Apply',
    meta: {
      title: '申请管理',
      icon: 'Document',
      roles: ['user', 'warehouse', 'dept_admin']
    },
    children: [
      {
        path: 'list',
        name: 'ApplyList',
        component: () => import('@/views/apply/list/index.vue'),
        meta: {
          title: '我的申请',
          icon: 'List',
          roles: ['user']
        }
      },
      {
        path: 'create',
        name: 'ApplyCreate',
        component: () => import('@/views/apply/create/index.vue'),
        meta: {
          title: '新建申请',
          icon: 'Plus',
          roles: ['user'],
          hidden: true
        }
      },
      {
        path: 'detail/:id',
        name: 'ApplyDetail',
        component: () => import('@/views/apply/detail/index.vue'),
        meta: {
          title: '申请详情',
          roles: ['user'],
          hidden: true
        }
      }
    ]
  },

  // 审批管理模块（仓管）
  {
    path: '/approval',
    component: () => import('@/layout/index.vue'),
    redirect: '/approval/pending',
    name: 'Approval',
    meta: {
      title: '审批管理',
      icon: 'CircleCheck',
      roles: ['warehouse', 'dept_admin']
    },
    children: [
      {
        path: 'pending',
        name: 'ApprovalPending',
        component: () => import('@/views/approval/pending/index.vue'),
        meta: {
          title: '待审批',
          icon: 'Clock'
        }
      },
      {
        path: 'approved',
        name: 'ApprovalApproved',
        component: () => import('@/views/approval/approved/index.vue'),
        meta: {
          title: '已审批',
          icon: 'Select'
        }
      },
      {
        path: 'detail/:id',
        name: 'ApprovalDetail',
        component: () => import('@/views/approval/detail/index.vue'),
        meta: {
          title: '审批详情',
          hidden: true
        }
      }
    ]
  },

  // 库存管理模块
  {
    path: '/inventory',
    component: () => import('@/layout/index.vue'),
    redirect: '/inventory/query',
    name: 'Inventory',
    meta: {
      title: '库存管理',
      icon: 'Goods',
      roles: ['admin', 'dept_admin', 'warehouse', 'user']
    },
    children: [
      {
        path: 'query',
        name: 'InventoryQuery',
        component: () => import('@/views/inventory/query/index.vue'),
        meta: {
          title: '库存查询',
          icon: 'Search'
        }
      },
      {
        path: 'warning',
        name: 'InventoryWarning',
        component: () => import('@/views/inventory/warning/index.vue'),
        meta: {
          title: '库存预警',
          icon: 'Warning',
          roles: ['admin', 'dept_admin', 'warehouse']
        }
      },
      {
        path: 'log',
        name: 'InventoryLog',
        component: () => import('@/views/inventory/log/index.vue'),
        meta: {
          title: '库存流水',
          icon: 'Histogram',
          roles: ['admin', 'dept_admin', 'warehouse']
        }
      }
    ]
  },

  // 统计报表模块
  {
    path: '/statistics',
    component: () => import('@/layout/index.vue'),
    redirect: '/statistics/inoutbound',
    name: 'Statistics',
    meta: {
      title: '统计报表',
      icon: 'DataAnalysis',
      roles: ['admin', 'dept_admin', 'warehouse']
    },
    children: [
      {
        path: 'inoutbound',
        name: 'StatisticsInoutbound',
        component: () => import('@/views/statistics/inoutbound/index.vue'),
        meta: {
          title: '出入库统计',
          icon: 'TrendCharts'
        }
      },
      {
        path: 'material',
        name: 'StatisticsMaterial',
        component: () => import('@/views/statistics/material/index.vue'),
        meta: {
          title: '物资统计',
          icon: 'PieChart'
        }
      },
      {
        path: 'usage',
        name: 'StatisticsUsage',
        component: () => import('@/views/statistics/usage/index.vue'),
        meta: {
          title: '使用统计',
          icon: 'DataLine'
        }
      }
    ]
  },

  // 消息中心
  {
    path: '/message',
    component: () => import('@/layout/index.vue'),
    redirect: '/message/list',
    name: 'Message',
    meta: {
      title: '消息中心',
      icon: 'Bell',
      hidden: true // 通过顶部导航访问，不在侧边栏显示
    },
    children: [
      {
        path: 'list',
        name: 'MessageList',
        component: () => import('@/views/message/list/index.vue'),
        meta: {
          title: '消息列表'
        }
      }
    ]
  },

  // 个人中心
  {
    path: '/profile',
    component: () => import('@/layout/index.vue'),
    redirect: '/profile/index',
    name: 'Profile',
    meta: {
      title: '个人中心',
      icon: 'User',
      hidden: true // 通过顶部导航访问
    },
    children: [
      {
        path: 'index',
        name: 'ProfileIndex',
        component: () => import('@/views/profile/index.vue'),
        meta: {
          title: '个人信息'
        }
      },
      {
        path: 'password',
        name: 'ProfilePassword',
        component: () => import('@/views/profile/password.vue'),
        meta: {
          title: '修改密码'
        }
      }
    ]
  },

  // 404页面必须放在最后
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
  // 路由切换时滚动到页面顶部
  scrollBehavior: () => ({ left: 0, top: 0 })
})

// 白名单：不需要登录即可访问的路径
const whiteList = ['/login', '/404', '/403']

/**
 * 全局前置守卫
 * 在路由跳转前进行权限验证
 */
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - WMS仓储管理系统` : 'WMS仓储管理系统'

  // 获取token
  const hasToken = getToken()

  console.log('=== 路由守卫调试 ===')
  console.log('to.path:', to.path)
  console.log('hasToken:', hasToken)

  if (hasToken) {
    // 已登录
    if (to.path === '/login') {
      // 如果已登录，跳转到首页
      next({ path: '/' })
    } else {
      const userStore = useUserStore()
      const hasRoles = userStore.roles && userStore.roles.length > 0

      console.log('hasRoles:', hasRoles)
      console.log('userStore.roles:', userStore.roles)
      console.log('userStore.routes.length:', userStore.routes.length)
      console.log('当前所有路由:', router.getRoutes().map(r => r.path))

      if (hasRoles) {
        // 已获取用户信息，但需要检查 router 中是否已实际注册动态路由
        // 检查 router 中是否已有动态路由（而不是检查 store 中的 routes）
        const currentRoutes = router.getRoutes()
        const hasAsyncRoutes = currentRoutes.some(route =>
          route.path.startsWith('/basic') ||
          route.path.startsWith('/inbound') ||
          route.path.startsWith('/outbound') ||
          route.path.startsWith('/apply') ||
          route.path.startsWith('/approval') ||
          route.path.startsWith('/inventory') ||
          route.path.startsWith('/statistics')
        )

        console.log('检查动态路由是否已注册:', hasAsyncRoutes)

        if (!hasAsyncRoutes) {
          console.log('检测到动态路由未添加，重新生成并添加路由...')
          try {
            // 根据角色生成可访问的路由
            const accessRoutes = await userStore.generateRoutes()

            console.log('生成的路由数量:', accessRoutes.length)

            // 动态添加路由
            accessRoutes.forEach(route => {
              console.log('Adding route:', route.path)
              router.addRoute(route)
            })

            console.log('路由添加完成，当前所有路由:', router.getRoutes().map(r => r.path))

            // 使用 replace 确保导航不会留下历史记录
            next({ ...to, replace: true })
          } catch (error) {
            console.error('生成路由失败:', error)
            await userStore.logout()
            next(`/login?redirect=${to.path}`)
          }
        } else {
          // 路由已存在，直接放行
          console.log('动态路由已存在，直接放行')
          next()
        }
      } else {
        try {
          // 获取用户信息
          console.log('首次获取用户信息...')
          await userStore.getUserInfo()

          // 根据角色生成可访问的路由
          const accessRoutes = await userStore.generateRoutes()

          console.log('生成的路由数量:', accessRoutes.length)

          // 动态添加路由
          accessRoutes.forEach(route => {
            console.log('Adding route:', route.path)
            router.addRoute(route)
          })

          console.log('路由添加完成，当前所有路由:', router.getRoutes().map(r => r.path))

          // 使用 replace 确保导航不会留下历史记录
          next({ ...to, replace: true })
        } catch (error) {
          // 获取用户信息失败，清除token并跳转到登录页
          console.error('获取用户信息失败:', error)
          await userStore.logout()
          next(`/login?redirect=${to.path}`)
        }
      }
    }
  } else {
    // 未登录
    if (whiteList.indexOf(to.path) !== -1) {
      // 在白名单中，直接放行
      next()
    } else {
      // 不在白名单中，跳转到登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

/**
 * 全局后置守卫
 * 路由跳转完成后的操作
 */
router.afterEach(() => {
  // 可以在这里添加页面加载完成后的操作
  // 例如：关闭加载动画等
})

// 导出路由实例
export default router
