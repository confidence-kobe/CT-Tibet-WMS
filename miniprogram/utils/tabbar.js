/**
 * 按角色调整底部第 2 个 Tab
 *
 * 微信小程序的 tabBar 页面路径是固定的（运行时不能换页面），所以第 2 个 Tab 始终是 pages/apply/list：
 * - 普通员工：显示"申请"，页面是我的申请列表
 * - 仓管员 / 部门管理员 / 系统管理员：显示"工作台"，页面是工作台（审批、待领取、出入库入口）
 *
 * 只能在 Tab 页面里调用（非 Tab 页面调用 setTabBarItem 会报错），所以在各 Tab 页的 onShow 里调用。
 */
const MANAGER_ROLES = ['admin', 'dept_admin', 'warehouse']

export function isManagerRole(userInfo) {
  const role = ((userInfo && userInfo.roleCode) || '').toLowerCase()
  return MANAGER_ROLES.includes(role)
}

export function syncTabBar(userInfo) {
  const manager = isManagerRole(userInfo)
  try {
    uni.setTabBarItem({
      index: 1,
      text: manager ? '工作台' : '申请',
      iconPath: manager ? 'static/tabbar/warehouse.png' : 'static/tabbar/apply.png',
      selectedIconPath: manager ? 'static/tabbar/warehouse-active.png' : 'static/tabbar/apply-active.png',
      fail: () => {}
    })
  } catch (e) {
    // 非 Tab 页面或平台不支持时忽略，不影响功能
  }
}
