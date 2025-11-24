<template>
  <el-tag
    :type="tagType"
    :size="size"
    :effect="effect"
    :round="round"
    :closable="closable"
    :disable-transitions="disableTransitions"
    :hit="hit"
    @close="handleClose"
  >
    <slot>{{ label }}</slot>
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 状态值
  status: {
    type: [String, Number],
    required: true
  },
  // 状态类型：order(订单), apply(申请), inventory(库存), operation(操作)
  statusType: {
    type: String,
    default: 'order',
    validator: (value) => ['order', 'apply', 'inventory', 'operation', 'custom'].includes(value)
  },
  // 自定义状态映射（当 statusType='custom' 时使用）
  customMap: {
    type: Object,
    default: () => ({})
  },
  // 标签大小
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  },
  // 主题
  effect: {
    type: String,
    default: 'light',
    validator: (value) => ['dark', 'light', 'plain'].includes(value)
  },
  // 是否为圆形
  round: {
    type: Boolean,
    default: false
  },
  // 是否可关闭
  closable: {
    type: Boolean,
    default: false
  },
  // 是否禁用渐变动画
  disableTransitions: {
    type: Boolean,
    default: false
  },
  // 是否有边框描边
  hit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

// 状态映射配置
const statusMaps = {
  // 订单状态（入库、出库）
  order: {
    0: { label: '待审核', type: 'warning' },
    1: { label: '已审核', type: 'success' },
    2: { label: '已完成', type: 'info' },
    3: { label: '已取消', type: 'info' },
    'pending': { label: '待审核', type: 'warning' },
    'approved': { label: '已审核', type: 'success' },
    'completed': { label: '已完成', type: 'info' },
    'cancelled': { label: '已取消', type: 'info' }
  },
  // 申请状态
  apply: {
    0: { label: '待审批', type: 'warning' },
    1: { label: '已通过', type: 'success' },
    2: { label: '已拒绝', type: 'danger' },
    3: { label: '已完成', type: 'info' },
    4: { label: '已取消', type: 'info' },
    'pending': { label: '待审批', type: 'warning' },
    'approved': { label: '已通过', type: 'success' },
    'rejected': { label: '已拒绝', type: 'danger' },
    'completed': { label: '已完成', type: 'info' },
    'cancelled': { label: '已取消', type: 'info' }
  },
  // 库存状态
  inventory: {
    'sufficient': { label: '充足', type: 'success' },
    'normal': { label: '正常', type: 'primary' },
    'warning': { label: '预警', type: 'warning' },
    'urgent': { label: '紧急', type: 'danger' },
    'out_of_stock': { label: '缺货', type: 'info' }
  },
  // 操作类型
  operation: {
    'inbound': { label: '入库', type: 'primary' },
    'outbound': { label: '出库', type: 'success' },
    'apply': { label: '申请', type: 'warning' },
    'transfer': { label: '调拨', type: 'info' },
    'check': { label: '盘点', type: '' }
  }
}

// 获取状态配置
const statusConfig = computed(() => {
  if (props.statusType === 'custom') {
    return props.customMap[props.status] || { label: props.status, type: '' }
  }

  const map = statusMaps[props.statusType] || {}
  return map[props.status] || { label: String(props.status), type: '' }
})

// 标签类型
const tagType = computed(() => statusConfig.value.type)

// 标签文本
const label = computed(() => statusConfig.value.label)

// 关闭事件
const handleClose = (event) => {
  emit('close', event)
}
</script>

<style lang="scss" scoped>
// 可以在这里添加自定义样式
</style>
