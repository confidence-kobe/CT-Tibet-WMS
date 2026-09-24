<template>
  <view class="create-apply-container">
    <!-- 表单 -->
    <view class="form">
      <!-- 领用仓库（本部门仓库；只有一个时自动选中） -->
      <view class="form-item required">
        <view class="label">领用仓库</view>
        <picker
          v-if="warehouses.length > 1"
          mode="selector"
          :range="warehouses"
          range-key="warehouseName"
          :value="warehouseIndex"
          @change="handleWarehouseChange"
        >
          <view class="picker-value">
            <text>{{ currentWarehouse ? currentWarehouse.warehouseName : '请选择仓库' }}</text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
        <view v-else class="picker-value readonly">
          <text>{{ currentWarehouse ? currentWarehouse.warehouseName : '本部门暂无可用仓库' }}</text>
        </view>
      </view>

      <!-- 用途说明 -->
      <view class="form-item required">
        <view class="label">用途说明</view>
        <textarea
          class="textarea"
          v-model="form.purpose"
          placeholder="请详细说明物资用途，如：XX小区光缆施工"
          maxlength="500"
          :show-confirm-bar="false"
        />
        <view class="char-count">{{ form.purpose.length }}/500</view>
      </view>

      <!-- 已选物资 -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">已选物资 ({{ form.details.length }})</text>
        </view>

        <view v-if="form.details.length > 0" class="material-list">
          <view v-for="(item, index) in form.details" :key="index" class="material-card">
            <view class="material-info">
              <text class="material-icon">📦</text>
              <view class="material-detail">
                <text class="material-name">{{ item.materialName }}</text>
                <text class="material-spec">{{ item.spec }} | {{ item.unit }}</text>
                <text :class="['stock-status', item.isStockSufficient ? 'sufficient' : 'insufficient']">
                  当前库存: {{ item.currentStock }} {{ item.unit }}
                  {{ item.isStockSufficient ? '✓' : '⚠️ 库存不足' }}
                </text>
              </view>
            </view>
            <view class="material-quantity">
              <text class="quantity-label">数量:</text>
              <input
                class="quantity-input"
                type="digit"
                v-model="item.quantity"
                @blur="handleQuantityChange(index)"
              />
              <text class="quantity-unit">{{ item.unit }}</text>
            </view>
            <view class="material-actions">
              <button class="btn-text" @click="editMaterial(index)">修改</button>
              <button class="btn-text danger" @click="deleteMaterial(index)">删除</button>
            </view>
          </view>
        </view>

        <!-- 添加物资按钮 -->
        <view class="add-material-btn" @click="showMaterialSelector">
          <text class="add-icon">+</text>
          <text class="add-text">添加物资</text>
        </view>
      </view>

      <!-- 提示信息 -->
      <view class="tips">
        <text class="tip-icon">💡</text>
        <text class="tip-text">提示: 申请提交后需等待仓管审批，预计24小时内处理</text>
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
        提交申请
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
                  <text class="selector-spec">{{ item.spec }} | 库存: {{ item.stock }}{{ item.unit }}</text>
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

    <!-- 数量输入弹窗 -->
    <view v-if="showQuantityInput" class="modal" @click="hideQuantityInput">
      <view class="modal-content small" @click.stop>
        <view class="modal-header">
          <text class="modal-title">输入数量</text>
          <text class="modal-close" @click="hideQuantityInput">✕</text>
        </view>

        <view class="modal-body">
          <view class="quantity-info">
            <text class="info-row">物资: {{ selectedMaterial.materialName }}</text>
            <text class="info-row">规格: {{ selectedMaterial.spec }}</text>
            <text class="info-row">单位: {{ selectedMaterial.unit }}</text>
            <text class="info-row">当前库存: {{ selectedMaterial.stock }} {{ selectedMaterial.unit }}</text>
          </view>

          <view class="quantity-input-group">
            <text class="quantity-label">申请数量:</text>
            <input
              class="quantity-input-large"
              type="number"
              v-model="tempQuantity"
              placeholder="请输入数量"
            />
          </view>

          <view class="quick-quantity">
            <text class="quick-label">常用数量:</text>
            <view class="quick-buttons">
              <button class="quick-btn" @click="setQuickQuantity(5)">5</button>
              <button class="quick-btn" @click="setQuickQuantity(10)">10</button>
              <button class="quick-btn" @click="setQuickQuantity(20)">20</button>
              <button class="quick-btn" @click="setQuickQuantity(50)">50</button>
            </view>
          </view>

          <view class="tips small">
            <text class="tip-icon">💡</text>
            <text class="tip-text">提示: 申请数量不应超过当前库存</text>
          </view>

          <view class="modal-actions">
            <button class="btn-modal" @click="hideQuantityInput">取消</button>
            <button class="btn-modal primary" @click="confirmQuantity">确定</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '@/api'

export default {
  data() {
    return {
      form: {
        purpose: '',
        details: []
      },
      submitting: false,
      // 仓库与库存
      warehouses: [],
      warehouseIndex: 0,
      stockMap: {},
      // 物资选择器
      showSelector: false,
      materials: [],
      filteredMaterials: [],
      categories: ['全部'],
      selectedCategory: '全部',
      searchKeyword: '',
      // 数量输入
      showQuantityInput: false,
      selectedMaterial: {},
      editingIndex: -1,
      tempQuantity: ''
    }
  },

  computed: {
    currentWarehouse() {
      return this.warehouses[this.warehouseIndex] || null
    },
    canSubmit() {
      return (
        !!this.currentWarehouse &&
        this.form.purpose.trim().length > 0 &&
        this.form.details.length > 0 &&
        !this.submitting
      )
    }
  },

  methods: {
    // 加载物资列表
    async loadMaterials() {
      try {
        const res = await api.common.getMaterials({
          status: 0 // 只获取启用的物资（自动分页拉取全部）
        })

        if (res.code === 200) {
          this.materials = this.withStock(res.data.list || [])
          this.filterMaterials()

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
        uni.showToast({
          title: '加载物资列表失败',
          icon: 'none'
        })
      }
    },

    // 加载本部门仓库
    async loadWarehouses() {
      try {
        const res = await api.common.getWarehouses()
        this.warehouses = res.data || []
        if (this.warehouses.length > 0) {
          await this.loadStock()
        }
      } catch (err) {
        console.error('加载仓库失败', err)
      }
    },

    // 切换仓库：重新加载该仓库库存
    async handleWarehouseChange(e) {
      this.warehouseIndex = Number(e.detail.value)
      await this.loadStock()
    },

    // 加载当前仓库的可用库存，并刷新已选物资的库存状态
    async loadStock() {
      if (!this.currentWarehouse) return
      try {
        const res = await api.inventory.getAll(this.currentWarehouse.id)
        const stockMap = {}
        const inventories = res.data.list || []
        inventories.forEach(inventory => {
          stockMap[inventory.materialId] = Number(inventory.availableQuantity) || 0
        })
        this.stockMap = stockMap
        this.materials = this.withStock(this.materials)
        this.filterMaterials()
        this.form.details.forEach(item => {
          item.currentStock = stockMap[item.materialId] || 0
          item.isStockSufficient = Number(item.quantity) <= item.currentStock
        })
      } catch (err) {
        console.error('加载库存失败', err)
      }
    },

    // 给物资附加当前仓库的可用库存
    withStock(materials) {
      return materials.map(material => ({
        ...material,
        stock: this.stockMap[material.id] || 0
      }))
    },

    // 重新申请：带入被拒绝申请的仓库、用途和物资
    async loadReapply(applyId) {
      try {
        const res = await api.apply.getApplyDetail(applyId)
        const apply = res.data || {}
        this.form.purpose = apply.purpose || apply.applyReason || ''
        const index = this.warehouses.findIndex(warehouse => warehouse.id === apply.warehouseId)
        if (index >= 0 && index !== this.warehouseIndex) {
          this.warehouseIndex = index
          await this.loadStock()
        }
        this.form.details = (apply.details || []).map(detail => {
          const currentStock = this.stockMap[detail.materialId] || 0
          return {
            materialId: detail.materialId,
            materialName: detail.materialName,
            materialCode: detail.materialCode,
            spec: detail.spec,
            unit: detail.unit,
            quantity: Number(detail.quantity),
            currentStock,
            isStockSufficient: Number(detail.quantity) <= currentStock
          }
        })
      } catch (err) {
        console.error('加载原申请失败', err)
      }
    },

    // 显示物资选择器
    showMaterialSelector() {
      this.showSelector = true
      if (this.materials.length === 0) {
        this.loadMaterials()
      }
    },

    // 隐藏物资选择器
    hideMaterialSelector() {
      this.showSelector = false
    },

    // 搜索物资
    handleSearch() {
      this.filterMaterials()
    },

    // 类别筛选
    handleCategoryChange(e) {
      this.selectedCategory = this.categories[e.detail.value]
      this.filterMaterials()
    },

    // 过滤物资
    filterMaterials() {
      let filtered = this.materials

      // 按类别过滤
      if (this.selectedCategory && this.selectedCategory !== '全部') {
        filtered = filtered.filter(item => item.category === this.selectedCategory)
      }

      // 按关键词搜索
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

    // 选择物资
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

      this.selectedMaterial = {
        ...material,
        stock: this.stockMap[material.id] || 0
      }
      this.editingIndex = -1
      this.tempQuantity = ''
      this.showQuantityInput = true
      this.hideMaterialSelector()
    },

    // 判断是否已选择
    isSelected(materialId) {
      return this.form.details.some(item => item.materialId === materialId)
    },

    // 显示数量输入框
    hideQuantityInput() {
      this.showQuantityInput = false
    },

    // 设置快捷数量
    setQuickQuantity(value) {
      this.tempQuantity = String(value)
    },

    // 确认数量
    confirmQuantity() {
      const quantity = parseFloat(this.tempQuantity)

      if (!quantity || quantity <= 0) {
        uni.showToast({
          title: '请输入有效数量',
          icon: 'none'
        })
        return
      }

      if (quantity > this.selectedMaterial.stock) {
        uni.showModal({
          title: '提示',
          content: `当前库存仅有${this.selectedMaterial.stock}${this.selectedMaterial.unit}，是否继续？`,
          success: (res) => {
            if (res.confirm) {
              this.addMaterialToList(quantity)
            }
          }
        })
      } else {
        this.addMaterialToList(quantity)
      }
    },

    // 添加物资到列表
    addMaterialToList(quantity) {
      const detail = {
        materialId: this.selectedMaterial.id,
        materialName: this.selectedMaterial.materialName,
        materialCode: this.selectedMaterial.materialCode,
        spec: this.selectedMaterial.spec,
        unit: this.selectedMaterial.unit,
        quantity: quantity,
        currentStock: this.selectedMaterial.stock,
        isStockSufficient: quantity <= this.selectedMaterial.stock
      }

      if (this.editingIndex >= 0) {
        // 编辑模式
        this.form.details.splice(this.editingIndex, 1, detail)
      } else {
        // 新增模式
        this.form.details.push(detail)
      }

      this.hideQuantityInput()
    },

    // 编辑物资
    editMaterial(index) {
      const item = this.form.details[index]
      this.selectedMaterial = {
        id: item.materialId,
        materialName: item.materialName,
        materialCode: item.materialCode,
        spec: item.spec,
        unit: item.unit,
        stock: item.currentStock
      }
      this.editingIndex = index
      this.tempQuantity = String(item.quantity)
      this.showQuantityInput = true
    },

    // 删除物资
    deleteMaterial(index) {
      uni.showModal({
        title: '提示',
        content: '确定删除该物资吗？',
        success: (res) => {
          if (res.confirm) {
            this.form.details.splice(index, 1)
          }
        }
      })
    },

    // 数量变化
    handleQuantityChange(index) {
      const item = this.form.details[index]
      const quantity = parseFloat(item.quantity)

      if (!quantity || quantity <= 0) {
        uni.showToast({
          title: '请输入有效数量',
          icon: 'none'
        })
        item.quantity = 1
        return
      }

      item.isStockSufficient = quantity <= item.currentStock
    },

    // 提交申请
    async handleSubmit() {
      // 验证
      if (!this.form.purpose.trim()) {
        uni.showToast({
          title: '请填写用途说明',
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

      if (!this.currentWarehouse) {
        uni.showToast({
          title: '请选择领用仓库',
          icon: 'none'
        })
        return
      }

      this.submitting = true
      uni.showLoading({ title: '提交中...' })

      try {
        const res = await api.apply.createApply({
          warehouseId: this.currentWarehouse.id,
          applyReason: this.form.purpose.trim(),
          details: this.form.details.map(item => ({
            materialId: item.materialId,
            quantity: item.quantity
          }))
        })

        uni.hideLoading()

        if (res.code === 200 || res.code === 201) {
          uni.showToast({
            title: '申请提交成功',
            icon: 'success',
            duration: 2000
          })

          setTimeout(() => {
            uni.navigateBack()
          }, 2000)
        }
      } catch (err) {
        uni.hideLoading()
        console.error('提交申请失败', err)
        uni.showToast({
          title: err.message || '提交失败,请重试',
          icon: 'none'
        })
      } finally {
        this.submitting = false
      }
    }
  },

  async onLoad(options) {
    await this.loadWarehouses()
    if (options && options.reapplyId) {
      await this.loadReapply(options.reapplyId)
    }
  }
}
</script>

<style lang="scss" scoped>
.create-apply-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 160rpx;
}

.form {
  padding: 32rpx;
}

.form-item {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;

  &.required .label::before {
    content: '*';
    color: #f5222d;
    margin-right: 8rpx;
  }
}

.label {
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 16rpx;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 80rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #262626;
  background-color: #fafafa;
  border-radius: 8rpx;

  &.readonly {
    color: #595959;
  }
}

.picker-arrow {
  font-size: 22rpx;
  color: #bfbfbf;
}

.textarea {
  width: 100%;
  min-height: 200rpx;
  font-size: 28rpx;
  line-height: 1.6;
  color: #262626;
  padding: 16rpx;
  background-color: #fafafa;
  border-radius: 8rpx;
}

.char-count {
  font-size: 24rpx;
  color: #8c8c8c;
  text-align: right;
  margin-top: 8rpx;
}

.section {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
}

.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;
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
  display: flex;
  margin-bottom: 16rpx;
}

.material-icon {
  font-size: 48rpx;
  margin-right: 16rpx;
}

.material-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.material-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8rpx;
}

.material-spec {
  font-size: 24rpx;
  color: #595959;
  margin-bottom: 8rpx;
}

.stock-status {
  font-size: 24rpx;

  &.sufficient {
    color: #52c41a;
  }

  &.insufficient {
    color: #faad14;
  }
}

.material-quantity {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.quantity-label {
  font-size: 28rpx;
  color: #262626;
  margin-right: 16rpx;
}

.quantity-input {
  flex: 1;
  height: 64rpx;
  padding: 0 16rpx;
  background-color: #ffffff;
  border: 1rpx solid #d9d9d9;
  border-radius: 8rpx;
  font-size: 28rpx;
  text-align: center;
}

.quantity-unit {
  font-size: 24rpx;
  color: #8c8c8c;
  margin-left: 16rpx;
}

.material-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
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

.tips {
  background-color: #e6f7ff;
  border-radius: 8rpx;
  padding: 16rpx;
  display: flex;
  align-items: center;

  &.small {
    margin-top: 16rpx;
  }
}

.tip-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.tip-text {
  flex: 1;
  font-size: 24rpx;
  color: #1890ff;
  line-height: 1.5;
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

/* 弹窗样式 */
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

  &.small {
    max-height: 60vh;
    align-items: center;
    border-radius: 32rpx;
    margin: auto 32rpx;
  }
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
  padding: 0 48rpx 0 24rpx;
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

.quantity-info {
  background-color: #fafafa;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}

.info-row {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 8rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.quantity-input-group {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.quantity-input-large {
  flex: 1;
  height: 80rpx;
  padding: 0 24rpx;
  background-color: #fafafa;
  border: 1rpx solid #d9d9d9;
  border-radius: 8rpx;
  font-size: 32rpx;
  text-align: center;
  margin-left: 16rpx;
}

.quick-quantity {
  margin-bottom: 24rpx;
}

.quick-label {
  display: block;
  font-size: 26rpx;
  color: #262626;
  margin-bottom: 16rpx;
}

.quick-buttons {
  display: flex;
  gap: 16rpx;
}

.quick-btn {
  flex: 1;
  height: 64rpx;
  background-color: #f0f0f0;
  color: #262626;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;

  &::after {
    border: none;
  }
}

.modal-actions {
  display: flex;
  gap: 16rpx;
  padding-top: 24rpx;
}

.btn-modal {
  flex: 1;
  height: 80rpx;
  background-color: #f0f0f0;
  color: #262626;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;

  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
  }

  &::after {
    border: none;
  }
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
