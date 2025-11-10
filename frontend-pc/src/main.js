/**
 * Vue应用入口文件
 * 负责创建Vue应用实例并挂载到DOM
 */

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入全局样式
import './styles/index.scss'

// 导入自定义指令
import { registerDirectives } from './directives'

// 创建Vue应用实例
const app = createApp(App)

// 注册自定义指令
registerDirectives(app)

// 创建Pinia状态管理实例并注册持久化插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(pinia)
app.use(router)
app.use(ElementPlus, { size: 'default', zIndex: 3000 })

// 挂载应用到#app元素
app.mount('#app')
