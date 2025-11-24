<template>
  <div ref="chartRef" :style="{ width: width, height: height }" class="echart-container" />
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  // 图表配置选项
  option: {
    type: Object,
    required: true,
    default: () => ({})
  },
  // 图表宽度
  width: {
    type: String,
    default: '100%'
  },
  // 图表高度
  height: {
    type: String,
    default: '400px'
  },
  // 是否自动resize
  autoResize: {
    type: Boolean,
    default: true
  },
  // 主题
  theme: {
    type: String,
    default: ''
  }
})

const chartRef = ref(null)
let chartInstance = null
let resizeObserver = null

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  // 如果已存在实例，先销毁
  if (chartInstance) {
    chartInstance.dispose()
  }

  // 创建图表实例
  chartInstance = echarts.init(chartRef.value, props.theme)

  // 设置配置项
  if (props.option) {
    chartInstance.setOption(props.option, true)
  }

  // 自动resize
  if (props.autoResize) {
    setupResize()
  }
}

// 设置自动resize
const setupResize = () => {
  // 使用ResizeObserver监听容器大小变化
  if (window.ResizeObserver) {
    resizeObserver = new ResizeObserver(() => {
      if (chartInstance) {
        chartInstance.resize()
      }
    })
    resizeObserver.observe(chartRef.value)
  } else {
    // 降级方案：监听window resize
    window.addEventListener('resize', handleResize)
  }
}

// 处理resize
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 更新图表配置
const updateChart = () => {
  if (chartInstance && props.option) {
    chartInstance.setOption(props.option, true)
  }
}

// 监听option变化
watch(() => props.option, () => {
  nextTick(() => {
    updateChart()
  })
}, { deep: true })

// 组件挂载
onMounted(() => {
  nextTick(() => {
    initChart()
  })
})

// 组件卸载
onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }

  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  } else {
    window.removeEventListener('resize', handleResize)
  }
})

// 暴露方法供父组件调用
defineExpose({
  // 获取图表实例
  getInstance: () => chartInstance,
  // 手动resize
  resize: () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  },
  // 更新配置
  setOption: (option, notMerge = false) => {
    if (chartInstance) {
      chartInstance.setOption(option, notMerge)
    }
  },
  // 显示loading
  showLoading: (type = 'default') => {
    if (chartInstance) {
      chartInstance.showLoading(type)
    }
  },
  // 隐藏loading
  hideLoading: () => {
    if (chartInstance) {
      chartInstance.hideLoading()
    }
  },
  // 清空图表
  clear: () => {
    if (chartInstance) {
      chartInstance.clear()
    }
  }
})
</script>

<style lang="scss" scoped>
.echart-container {
  min-height: 300px;
}
</style>
