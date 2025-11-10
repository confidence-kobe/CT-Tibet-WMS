<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-image">
        <el-icon :size="200" class="error-icon">
          <Lock />
        </el-icon>
      </div>
      <h1 class="error-code">403</h1>
      <h2 class="error-title">访问被拒绝</h2>
      <p class="error-message">
        抱歉，您没有权限访问此页面
      </p>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">
          返回首页
        </el-button>
        <el-button @click="goBack">
          返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: $background-color;

  .error-content {
    text-align: center;
    padding: 40px;

    .error-image {
      margin-bottom: 24px;

      .error-icon {
        color: $error-color;
        opacity: 0.8;
      }
    }

    .error-code {
      font-size: 72px;
      font-weight: 700;
      color: $error-color;
      margin: 0 0 16px 0;
    }

    .error-title {
      font-size: 24px;
      font-weight: 500;
      color: $text-color-primary;
      margin: 0 0 12px 0;
    }

    .error-message {
      font-size: 16px;
      color: $text-color-secondary;
      margin: 0 0 32px 0;
    }

    .error-actions {
      display: flex;
      gap: 16px;
      justify-content: center;
    }
  }
}
</style>
