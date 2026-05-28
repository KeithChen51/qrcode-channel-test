<template>
  <div class="not-found">
    <div class="content">
      <LocalIcon name="solar:ghost-bold-duotone" width="120" class="icon" />
      <h1>404</h1>
      <p>页面未找到</p>
      <el-button type="primary" @click="$router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import LocalIcon from '@/components/LocalIcon.vue'
</script>

<style scoped lang="scss">
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-color);
}

.content {
  text-align: center;
  
  .icon {
    color: var(--text-muted);
    margin-bottom: 24px;
  }
  
  h1 {
    font-size: 72px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0;
  }
  
  p {
    font-size: 18px;
    color: var(--text-secondary);
    margin: 16px 0 32px;
  }
}
</style>
