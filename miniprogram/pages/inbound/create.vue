<template>
  <view class="inbound-create-container">
    <!-- 表单 -->
    <view class="form">
      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <view class="form-item">
          <text class="form-label">入库仓库</text>
          <picker mode="selector" :range="warehouses" range-key="warehouseName" @change="handleWarehouseChange">
            <view class="picker-value">
              {{ selectedWarehouse ? selectedWarehouse.warehouseName : '请选择仓库' }} ▼
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">入库类型</text>
          <picker mode="selector" :range="inboundTypes" range-key="label" @change="handleTypeChange">
            <view class="picker-value">
              {{ inboundTypes[selectedTypeIndex].label }} ▼
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">备注说明</text>
          <textarea
            class="form-textarea"
            v-model="form.remark"
            placeholder="选填，如：采购入库"
            maxlength="200"
          />
        </view>
      </view>

      <!-- 入库物资 -->
      <view class="form-section">
        <view class="section-title">入库物资 ({{ form.details.length }})</view>

        <view v-if="form.details.length > 0" class="material-list">
          <view v-for="(item, index) in form.details" :key="index" class="material-card">
            <view class="material-info">
              <text class="material-name">📦 {{ item.materialName }}</text>
              <text class="material-spec">{{ item.spec }} | {{ item.unit }}</text>
            </view>
            <view class="material-input">
              <text class="input-label">数量:</text>
              <input
                class="quantity-input"
                type="digit"
                v-model="item.quantity"
                placeholder="0"
              />
              <text class="input-unit">{{ item.unit }}</text>
            </view>
            <view class="material-input">
              <text class="input-label">单价:</text>
              <input
                class="price-input"
                type="digit"
                v-model="item.unitPrice"
                placeholder="选填"
              />
              <text class="input-unit">元</text>
            </view>
            <view class="material-actions">
              <button class="btn-text danger" @click="deleteMaterial(index)">删除</button>
            </view>
          </view>
        </view>

        <view class="add-material-btn" @click="showMaterialSelector">
          <text class="add-icon">+</text>
          <text class="add-text">添加物资</text>
        </view>
      </view>

      <!-- 合计 -->
      <view v-if="form.details.length > 0" class="summary-section">
        <view class="summary-row">
          <text class="summary-label">合计金额:</text>
          <text class="summary-value">¥{{ totalAmount.toFixed(2) }}</text>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-btn-container safe-area-inset-bottom">
      <button
        class="submit-btn"
        type="primary"
        :disabled="!canSubmit"
        :loading="submitting"
        @click="handleSubmit"
      >
        提交入库
      </button>
    </view>

    <!-- 物资选择器弹窗 -->
    <view v-if="showSelector" class="modal" @click="hideMaterialSelector">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择物资</text>
          <text class="modal-close" @click="hideMaterialSelector">✕</text>
        </view>

        <view class="modal-body">
          <!-- 搜索框 -->
          <view class="search-bar">
            <input
              class="search-input"
              v-model="searchKeyword"
              placeholder="搜索物资..."
              @input="handleSearch"
            />
            <text class="search-icon">🔍</text>
          </view>

          <!-- 类别筛选 -->
          <view class="filter-bar">
            <text class="filter-label">类别:</text>
            <picker mode="selector" :range="categories" @change="handleCategoryChange">
              <view class="filter-value">{{ selectedCategory || '全部' }} ▼</view>
            </picker>
          </view>

          <!-- 物资列表 -->
          <scroll-view class="material-scroll" scroll-y>
            <view v-if="filteredMaterials.length > 0" class="selector-list">
              <view
                v-for="item in filteredMaterials"
                :key="item.id"
                class="selector-item"
                @click="selectMaterial(item)"
              >
                <view class="selector-info">
                  <text class="selector-name">{{ item.materialName }}</text>
                  <text class="selector-spec">{{ item.spec }}</text>
                </view>
                <text class="selector-check">{{ isSelected(item.id) ? '✓' : '' }}</text>
              </view>
            </view>
            <view v-else class="empty-state">
              <text class="empty-icon">📭</text>
              <text class="empty-text">暂无物资</text>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '@/api'
import { formatLocalDateTime } from '@/utils/date.js'
import { mapState } from 'vuex'

export default {
  data() {
    return {
      warehouses: [],
      selectedWarehouse: null,
      inboundTypes: [
        { label: '采购入库', value: 1 },
        { label: '退货入库', value: 2 },
        { label: '调拨入库', value: 3 },
        { label: '其他', value: 4 }
      ],
      selectedTypeIndex: 0,
      form: {
        warehouseId: null,
        inboundType: 1,
        remark: '',
        details: []
      },
      submitting: false,
      // 物资选择器
      showSelector: false,
      materials: [],
      filteredMaterials: [],
      categories: ['全部'],
      selectedCategory: '全部',
      searchKeyword: ''
    }
  },

  computed: {
    ...mapState(['userInfo']),

    canSubmit() {
      return (
        this.form.warehouseId &&
        this.form.details.length > 0 &&
        this.form.details.every(item => item.quantity > 0) &&
        !this.submitting
      )
    },

    totalAmount() {
      return this.form.details.reduce((sum, item) => {
        const quantity = parseFloat(item.quantity) || 0
        const unitPrice = parseFloat(item.unitPrice) || 0
        return sum + quantity * unitPrice
      }, 0)
    }
  },

  methods: {
    // 加载仓库列表
    async loadWarehouses() {
      try {
        const res = await api.common.getWarehouses({
          status: 0
        })

        if (res.code === 200) {
          this.warehouses = res.data || []

          // 默认选择用户所属部门的仓库
          if (this.warehouses.length > 0) {
            const userDeptWarehouse = this.warehouses.find(
              w => w.deptId === (this.userInfo ? this.userInfo.deptId : null)
            )
            if (userDeptWarehouse) {
              this.selectedWarehouse = userDeptWarehouse
              this.form.warehouseId = userDeptWarehouse.id
            } else {
              this.selectedWarehouse = this.warehouses[0]
              this.form.warehouseId = this.warehouses[0].id
            }
          }
        }
      } catch (err) {
        console.error('加载仓库列表失败', err)
      }
    },

    handleWarehouseChange(e) {
      const index = e.detail.value
      this.selectedWarehouse = this.warehouses[index]
      this.form.warehouseId = this.selectedWarehouse.id
    },

    handleTypeChange(e) {
      this.selectedTypeIndex = e.detail.value
      this.form.inboundType = this.inboundTypes[this.selectedTypeIndex].value
    },

    // 加载物资列表
    async loadMaterials() {
      try {
        const res = await api.common.getMaterials({
          status: 0
        })

        if (res.code === 200) {
          this.materials = res.data.list || []
          this.filteredMaterials = this.materials

          // 提取类别
          const categorySet = new Set(['全部'])
          this.materials.forEach(item => {
            if (item.category) {
              categorySet.add(item.category)
            }
          })
          this.categories = Array.from(categorySet)
        }
      } catch (err) {
        console.error('加载物资列表失败', err)
      }
    },

    showMaterialSelector() {
      this.showSelector = true
      if (this.materials.length === 0) {
        this.loadMaterials()
      }
    },

    hideMaterialSelector() {
      this.showSelector = false
    },

    handleSearch() {
      this.filterMaterials()
    },

    handleCategoryChange(e) {
      this.selectedCategory = this.categories[e.detail.value]
      this.filterMaterials()
    },

    filterMaterials() {
      let filtered = this.materials

      if (this.selectedCategory && this.selectedCategory !== '全部') {
        filtered = filtered.filter(item => item.category === this.selectedCategory)
      }

      if (this.searchKeyword) {
        filtered = filtered.filter(item => {
          return (
            item.materialName.includes(this.searchKeyword) ||
            item.materialCode.includes(this.searchKeyword)
          )
        })
      }

      this.filteredMaterials = filtered
    },

    selectMaterial(material) {
      // 检查是否已选择
      const exists = this.form.details.find(item => item.materialId === material.id)
      if (exists) {
        uni.showToast({
          title: '该物资已添加',
          icon: 'none'
        })
        return
      }

      // 添加到列表
      this.form.details.push({
        materialId: material.id,
        materialName: material.materialName,
        materialCode: material.materialCode,
        spec: material.spec,
        unit: material.unit,
        quantity: '',
        unitPrice: material.price || ''
      })

      this.hideMaterialSelector()
    },

    isSelected(materialId) {
      return this.form.details.some(item => item.materialId === materialId)
    },

    deleteMaterial(index) {
      this.form.details.splice(index, 1)
    },

    // 提交入库
    async handleSubmit() {
      // 验证
      if (!this.form.warehouseId) {
        uni.showToast({
          title: '请选择入库仓库',
          icon: 'none'
        })
        return
      }

      if (this.form.details.length === 0) {
        uni.showToast({
          title: '请至少添加一项物资',
          icon: 'none'
        })
        return
      }

      // 验证数量
      for (let item of this.form.details) {
        if (!item.quantity || parseFloat(item.quantity) <= 0) {
          uni.showToast({
            title: `请输入${item.materialName}的有效数量`,
            icon: 'none'
          })
          return
        }
      }

      this.submitting = true

      try {
        const res = await api.inbound.create({
          warehouseId: this.form.warehouseId,
          inboundType: this.form.inboundType,
          // 本地时间（后端格式 yyyy-MM-dd HH:mm:ss）
          inboundTime: formatLocalDateTime(),
          remark: this.form.remark.trim(),
          details: this.form.details.map(item => ({
            materialId: item.materialId,
            quantity: parseFloat(item.quantity),
            unitPrice: parseFloat(item.unitPrice) || 0
          }))
        })

        if (res.code === 200) {
          uni.showToast({
            title: '入库成功',
            icon: 'success',
            duration: 2000
          })

          setTimeout(() => {
            uni.navigateBack()
          }, 2000)
        }
      } catch (err) {
        console.error('入库失败', err)
      } finally {
        this.submitting = false
      }
    }
  },

  onLoad() {
    this.loadWarehouses()
  }
}
</script>

<style lang="scss" scoped>
.inbound-create-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 160rpx;
}

.form {
  padding: 24rpx 32rpx;
}

.form-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 24rpx;
}

.form-item {
  margin-bottom: 24rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 16rpx;
}

.picker-value {
  height: 72rpx;
  padding: 0 24rpx;
  background-color: #fafafa;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #262626;
  display: flex;
  align-items: center;
}

.form-textarea {
  width: 100%;
  min-height: 120rpx;
  padding: 16rpx;
  background-color: #fafafa;
  border-radius: 8rpx;
  font-size: 28rpx;
  line-height: 1.6;
}

.material-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.material-card {
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 24rpx;
}

.material-info {
  margin-bottom: 16rpx;
}

.material-name {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8rpx;
}

.material-spec {
  display: block;
  font-size: 24rpx;
  color: #8c8c8c;
}

.material-input {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.input-label {
  font-size: 26rpx;
  color: #262626;
  width: 80rpx;
}

.quantity-input,
.price-input {
  flex: 1;
  height: 64rpx;
  padding: 0 16rpx;
  background-color: #ffffff;
  border: 1rpx solid #d9d9d9;
  border-radius: 8rpx;
  font-size: 28rpx;
  margin-right: 16rpx;
}

.input-unit {
  font-size: 24rpx;
  color: #8c8c8c;
  width: 60rpx;
}

.material-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 12rpx;
  border-top: 1rpx solid #f0f0f0;
}

.btn-text {
  padding: 8rpx 24rpx;
  font-size: 24rpx;
  color: #1890ff;
  background: none;
  border: none;

  &.danger {
    color: #f5222d;
  }

  &::after {
    border: none;
  }
}

.add-material-btn {
  height: 96rpx;
  border: 2rpx dashed #d9d9d9;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.add-icon {
  font-size: 40rpx;
  color: #1890ff;
  margin-right: 16rpx;
}

.add-text {
  font-size: 28rpx;
  color: #1890ff;
}

.summary-section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  font-size: 32rpx;
  color: #262626;
}

.summary-value {
  font-size: 40rpx;
  font-weight: 600;
  color: #f5222d;
}

.submit-btn-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx;
  background-color: #ffffff;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 500;
  border: none;

  &::after {
    border: none;
  }

  &:disabled {
    opacity: 0.5;
  }
}

/* 弹窗样式（复用申请页面的样式）*/
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  max-height: 80vh;
  background-color: #ffffff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #262626;
}

.modal-close {
  font-size: 40rpx;
  color: #8c8c8c;
}

.modal-body {
  flex: 1;
  padding: 32rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.search-bar {
  position: relative;
  margin-bottom: 24rpx;
}

.search-input {
  width: 100%;
  height: 64rpx;
  padding: 0 80rpx 0 24rpx;
  background-color: #f5f5f5;
  border-radius: 32rpx;
  font-size: 28rpx;
}

.search-icon {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 32rpx;
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.filter-label {
  font-size: 28rpx;
  color: #262626;
  margin-right: 16rpx;
}

.filter-value {
  font-size: 28rpx;
  color: #1890ff;
  padding: 8rpx 16rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
}

.material-scroll {
  flex: 1;
}

.selector-list {
  display: flex;
  flex-direction: column;
}

.selector-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.selector-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.selector-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8rpx;
}

.selector-spec {
  font-size: 24rpx;
  color: #8c8c8c;
}

.selector-check {
  font-size: 40rpx;
  color: #52c41a;
  margin-left: 16rpx;
}

.empty-state {
  padding: 120rpx 0;
  text-align: center;
}

.empty-icon {
  display: block;
  font-size: 96rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #8c8c8c;
}
</style>
