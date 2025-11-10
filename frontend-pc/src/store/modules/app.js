/**
 * 应用状态管理模块
 * 管理应用的全局状态，如侧边栏、设备类型等
 */

import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  // 状态
  state: () => ({
    // 侧边栏状态
    sidebar: {
      opened: true, // 是否打开
      withoutAnimation: false // 是否禁用动画
    },
    // 设备类型
    device: 'desktop', // desktop/mobile
    // 主题
    theme: 'light', // light/dark
    // 语言
    language: 'zh-CN', // zh-CN/en-US
    // 布局大小
    size: 'default' // default/large/small
  }),

  // 计算属性
  getters: {
    // 侧边栏是否打开
    sidebarOpened: (state) => state.sidebar.opened,
    // 是否是移动设备
    isMobile: (state) => state.device === 'mobile',
    // 是否是桌面设备
    isDesktop: (state) => state.device === 'desktop',
    // 是否是暗色主题
    isDark: (state) => state.theme === 'dark'
  },

  // 操作
  actions: {
    /**
     * 切换侧边栏状态
     */
    toggleSidebar() {
      this.sidebar.opened = !this.sidebar.opened
      this.sidebar.withoutAnimation = false
    },

    /**
     * 关闭侧边栏
     */
    closeSidebar(withoutAnimation = false) {
      this.sidebar.opened = false
      this.sidebar.withoutAnimation = withoutAnimation
    },

    /**
     * 打开侧边栏
     */
    openSidebar(withoutAnimation = false) {
      this.sidebar.opened = true
      this.sidebar.withoutAnimation = withoutAnimation
    },

    /**
     * 设置设备类型
     * @param {string} device - 设备类型 desktop/mobile
     */
    setDevice(device) {
      this.device = device

      // 移动设备自动关闭侧边栏
      if (device === 'mobile') {
        this.closeSidebar(true)
      } else {
        this.openSidebar(true)
      }
    },

    /**
     * 切换主题
     * @param {string} theme - 主题 light/dark
     */
    toggleTheme(theme) {
      if (theme) {
        this.theme = theme
      } else {
        this.theme = this.theme === 'light' ? 'dark' : 'light'
      }

      // 添加或移除暗色主题类
      if (this.theme === 'dark') {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    },

    /**
     * 设置语言
     * @param {string} language - 语言代码
     */
    setLanguage(language) {
      this.language = language
      // 可以在这里切换i18n语言
    },

    /**
     * 设置组件大小
     * @param {string} size - 组件大小 default/large/small
     */
    setSize(size) {
      this.size = size
    },

    /**
     * 初始化应用设置
     * 从localStorage读取用户的个性化设置
     */
    initSettings() {
      // 读取主题设置
      const savedTheme = localStorage.getItem('app_theme')
      if (savedTheme) {
        this.toggleTheme(savedTheme)
      }

      // 读取语言设置
      const savedLanguage = localStorage.getItem('app_language')
      if (savedLanguage) {
        this.setLanguage(savedLanguage)
      }

      // 读取组件大小设置
      const savedSize = localStorage.getItem('app_size')
      if (savedSize) {
        this.setSize(savedSize)
      }

      // 读取侧边栏状态
      const savedSidebarState = localStorage.getItem('app_sidebar_opened')
      if (savedSidebarState !== null) {
        this.sidebar.opened = savedSidebarState === 'true'
      }

      // 检测设备类型
      this.detectDevice()
    },

    /**
     * 检测设备类型
     */
    detectDevice() {
      const rect = document.body.getBoundingClientRect()
      const isMobile = rect.width - 1 < 768
      this.setDevice(isMobile ? 'mobile' : 'desktop')
    }
  },

  // 持久化配置
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'app',
        storage: localStorage,
        paths: ['sidebar', 'theme', 'language', 'size'] // 持久化这些字段
      }
    ]
  }
})
