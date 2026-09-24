<template>
  <view class="outbound-create-container">
    <!-- 表单 -->
    <view class="form">
      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <view class="form-item">
          <text class="form-label">出库仓库</text>
          <picker mode="selector" :range="warehouses" range-key="warehouseName" @change="handleWarehouseChange">
            <view class="picker-value">
              {{ selectedWarehouse ? selectedWarehouse.warehouseName : '请选择仓库' }} ▼
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">出库类型</text>
          <picker mode="selector" :range="outboundTypes" range-key="label" @change="handleTypeChange">
            <view class="picker-value">
              {{ outboundTypes[selectedTypeIndex].label }} ▼
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">领用人</text>
          <button class="select-user-btn" @click="showUserSelector">
            {{ selectedUser ? `${selectedUser.realName} (${selectedUser.phone})` : '点击选择领用人' }}
          </button>
        </view>

        <view class="form-item">
          <text class="form-label">用途说明</text>
          <textarea
            class="form-textarea"
            v-model="form.purpose"
            placeholder="请输入用途说明"
            maxlength="200"
          />
        </view>

        <view class="form-item">
          <text class="form-label">备注说明</text>
          <textarea
            class="form-textarea"
            v-model="form.remark"
            placeholder="选填"
            maxlength="200"
          />
        </view>
      </view>

      <!-- 出库物资 -->
      <view class="form-section">
        <view class="section-title">出库物资 ({{ form.details.length }})</view>

        <view v-if="form.details.length > 0" class="material-list">
          <view v-for="(item, index) in form.details" :key="index" class="material-card">
            <view class="material-info">
              <text class="material-name">📦 {{ item.materialName }}</text>
              <text class="material-spec">{{ item.spec }} | {{ item.unit }}</text>
              <text :class="['stock-info', item.quantity > item.currentStock ? 'warning' : 'normal']">
                当前库存: {{ item.currentStock }} {{ item.unit }}
              </text>
            </view>
            <view class="material-input">
              <text class="input-label">数量:</text>
              <input
                class="quantity-input"
                type="digit"
                v-model="item.quantity"
                placeholder="0"
                @blur="checkStock(index)"
              />
              <text class="input-unit">{{ item.unit }}</text>
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

      <view class="tips">
        <text class="tip-icon">💡</text>
        <text class="tip-text">提示: 直接出库将立即扣减库存，请确认物资已被领用人签收</text>
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
        提交出库
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
          <view class="search-bar">
            <input
              class="search-input"
              v-model="searchKeyword"
              placeholder="搜索物资..."
              @input="handleSearch"
            />
            <text class="search-icon">🔍</text>
          </view>

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
                  <text class="selector-spec">{{ item.spec }} | 库存: {{ item.availableQuantity }}{{ item.unit }}</text>
                </view>
                <text class="selector-check">{{ isSelected(item.materialId) ? '✓' : '' }}</text>
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

    <!-- 用户选择器弹窗 -->
    <view v-if="showUserModal" class="modal" @click="hideUserSelector">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择领用人</text>
          <text class="modal-close" @click="hideUserSelector">✕</text>
        </view>

        <view class="modal-body">
          <view class="search-bar">
            <input
              class="search-input"
              v-model="userSearchKeyword"
              placeholder="搜索用户..."
              @input="handleUserSearch"
            />
            <text class="search-icon">🔍</text>
          </view>

          <scroll-view class="user-scroll" scroll-y>
            <view v-if="filteredUsers.length > 0" class="user-list">
              <view
                v-for="user in filteredUsers"
                :key="user.id"
                class="user-item"
                @click="selectUser(user)"
              >
                <view class="user-info">
                  <text class="user-name">{{ user.realName }}</text>
                  <text class="user-dept">{{ user.deptName }} | {{ user.phone }}</text>
                </view>
                <text class="user-check">{{ selectedUser && selectedUser.id === user.id ? '✓' : '' }}</text>
              </view>
            </view>
            <view v-else class="empty-state">
              <text class="empty-icon">📭</text>
              <text class="empty-text">暂无用户</text>
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
      outboundTypes: [
        { label: '领用出库', value: 1 },
        { label: '报废出库', value: 2 },
        { label: '调拨出库', value: 3 },
        { label: '其他', value: 4 }
      ],
      selectedTypeIndex: 0,
      selectedUser: null,
      form: {
        warehouseId: null,
        outboundType: 1,
        receiverId: null,
        purpose: '',
        remark: '',
        details: []
      },
      submitting: false,
      // 物资选择器
      showSelector: false,
      materials: [],
      filteredMaterials: [],
      searchKeyword: '',
      // 用户选择器
      showUserModal: false,
      users: [],
      filteredUsers: [],
      userSearchKeyword: ''
    }
  },

  computed: {
    ...mapState(['userInfo']),

    canSubmit() {
      return (
        this.form.warehouseId &&
        this.form.receiverId &&
        this.form.purpose.trim().length > 0 &&
        this.form.details.length > 0 &&
        this.form.details.every(item => item.quantity > 0) &&
        !this.submitting
      )
    }
  },

  methods: {
    async loadWarehouses() {
      try {
        const res = await api.common.getWarehouses({ status: 0 })

        if (res.code === 200) {
          this.warehouses = res.data || []

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

      // 清空已选物资和物资列表，库存需按新仓库重新加载
      this.form.details = []
      this.materials = []
      this.filteredMaterials = []
    },

    handleTypeChange(e) {
      this.selectedTypeIndex = e.detail.value
      this.form.outboundType = this.outboundTypes[this.selectedTypeIndex].value
    },

    async loadInventory() {
      if (!this.form.warehouseId) {
        uni.showToast({
          title: '请先选择仓库',
          icon: 'none'
        })
        return
      }

      try {
        // 拉取该仓库的全部库存（后端每页最多100条，自动分页）
        const res = await api.inventory.getAll(this.form.warehouseId)

        if (res.code === 200) {
          this.materials = res.data.list
          this.filteredMaterials = this.materials
        }
      } catch (err) {
        console.error('加载库存失败', err)
      }
    },

    showMaterialSelector() {
      if (!this.form.warehouseId) {
        uni.showToast({
          title: '请先选择仓库',
          icon: 'none'
        })
        return
      }

      this.showSelector = true
      if (this.materials.length === 0) {
        this.loadInventory()
      }
    },

    hideMaterialSelector() {
      this.showSelector = false
    },

    handleSearch() {
      if (!this.searchKeyword) {
        this.filteredMaterials = this.materials
      } else {
        this.filteredMaterials = this.materials.filter(item => {
          return (
            item.materialName.includes(this.searchKeyword) ||
            item.materialCode.includes(this.searchKeyword)
          )
        })
      }
    },

    selectMaterial(material) {
      const exists = this.form.details.find(item => item.materialId === material.materialId)
      if (exists) {
        uni.showToast({
          title: '该物资已添加',
          icon: 'none'
        })
        return
      }

      this.form.details.push({
        materialId: material.materialId,
        materialName: material.materialName,
        materialCode: material.materialCode,
        spec: material.spec,
        unit: material.unit,
        quantity: '',
        unitPrice: material.price,
        currentStock: material.availableQuantity
      })

      this.hideMaterialSelector()
    },

    isSelected(materialId) {
      return this.form.details.some(item => item.materialId === materialId)
    },

    checkStock(index) {
      const item = this.form.details[index]
      const quantity = parseFloat(item.quantity) || 0

      if (quantity > item.currentStock) {
        uni.showModal({
          title: '库存不足',
          content: `${item.materialName}当前库存仅有${item.currentStock}${item.unit}，是否继续？`,
          success: (res) => {
            if (!res.confirm) {
              item.quantity = item.currentStock
            }
          }
        })
      }
    },

    deleteMaterial(index) {
      this.form.details.splice(index, 1)
    },

    async loadUsers() {
      try {
        // 本部门启用的用户（系统管理员为全部用户）
        const res = await api.common.getUsers()

        if (res.code === 200) {
          this.users = res.data || []
          this.filteredUsers = this.users
        }
      } catch (err) {
        console.error('加载用户列表失败', err)
      }
    },

    showUserSelector() {
      this.showUserModal = true
      if (this.users.length === 0) {
        this.loadUsers()
      }
    },

    hideUserSelector() {
      this.showUserModal = false
    },

    handleUserSearch() {
      if (!this.userSearchKeyword) {
        this.filteredUsers = this.users
      } else {
        this.filteredUsers = this.users.filter(user => {
          return (
            user.realName.includes(this.userSearchKeyword) ||
            user.phone.includes(this.userSearchKeyword)
          )
        })
      }
    },

    selectUser(user) {
      this.selectedUser = user
      this.form.receiverId = user.id
      this.hideUserSelector()
    },

    async handleSubmit() {
      // 验证
      if (!this.form.warehouseId) {
        uni.showToast({
          title: '请选择出库仓库',
          icon: 'none'
        })
        return
      }

      if (!this.form.receiverId) {
        uni.showToast({
          title: '请选择领用人',
          icon: 'none'
        })
        return
      }

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
        const res = await api.outbound.create({
          warehouseId: this.form.warehouseId,
          outboundType: this.form.outboundType,
          // 本地时间（ISO 格式，不带时区）
          outboundTime: formatLocalDateTime(new Date(), 'T'),
          receiverId: this.form.receiverId,
          purpose: this.form.purpose.trim(),
          remark: this.form.remark.trim(),
          details: this.form.details.map(item => ({
            materialId: item.materialId,
            quantity: parseFloat(item.quantity),
            unitPrice: item.unitPrice
          }))
        })

        if (res.code === 200) {
          uni.showToast({
            title: '出库成功',
            icon: 'success',
            duration: 2000
          })

          setTimeout(() => {
            uni.navigateBack()
          }, 2000)
        }
      } catch (err) {
        console.error('出库失败', err)
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
.outbound-create-container {
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

.select-user-btn {
  width: 100%;
  height: 72rpx;
  padding: 0 24rpx;
  background-color: #fafafa;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #262626;
  text-align: left;

  &::after {
    border: none;
  }
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
  margin-bottom: 8rpx;
}

.stock-info {
  display: block;
  font-size: 24rpx;

  &.normal {
    color: #52c41a;
  }

  &.warning {
    color: #faad14;
  }
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

.quantity-input {
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

.tips {
  background-color: #fff7e6;
  border-radius: 12rpx;
  padding: 16rpx;
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.tip-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.tip-text {
  flex: 1;
  font-size: 24rpx;
  color: #faad14;
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

.material-scroll,
.user-scroll {
  flex: 1;
}

.selector-list,
.user-list {
  display: flex;
  flex-direction: column;
}

.selector-item,
.user-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.selector-info,
.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.selector-name,
.user-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8rpx;
}

.selector-spec,
.user-dept {
  font-size: 24rpx;
  color: #8c8c8c;
}

.selector-check,
.user-check {
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
