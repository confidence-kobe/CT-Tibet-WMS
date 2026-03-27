import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  // 部署到 /wms/ 子路径
  base: '/wms/',

  plugins: [
    vue(),
    // 自动导入Vue相关函数
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts'
    }),
    // 自动导入组件
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts'
    })
  ],

  // 路径别名
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },

  // 开发服务器配置
  server: {
    port: 4444,
    open: true,
    cors: true,
    // 代理配置 - 解决跨域问题
    proxy: {
      '/api': {
        target: 'http://localhost:48888',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },

  // 构建配置
  build: {
    outDir: 'dist',
    sourcemap: false,
    // 消除打包大小超过500kb警告
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        // 分包策略（使用函数避免循环依赖初始化问题）
        manualChunks(id) {
          if (id.includes('node_modules/echarts') || id.includes('node_modules/zrender')) {
            return 'echarts'
          }
          if (id.includes('node_modules/element-plus') || id.includes('node_modules/@element-plus')) {
            return 'element-plus'
          }
          if (id.includes('node_modules/vue') || id.includes('node_modules/@vue') || id.includes('node_modules/vue-router') || id.includes('node_modules/pinia')) {
            return 'vue-vendor'
          }
          if (id.includes('node_modules/axios') || id.includes('node_modules/dayjs') || id.includes('node_modules/lodash-es')) {
            return 'utils'
          }
        }
      }
    }
  },

  // CSS配置
  css: {
    preprocessorOptions: {
      scss: {
        // 使用现代 Sass API
        api: 'modern-compiler',
        // 使用 @use 代替 @import，并通过 as * 避免命名空间
        additionalData: `@use "@/styles/variables.scss" as *;`
      }
    }
  }
})
