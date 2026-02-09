<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '260px'" class="sidebar">
      <div class="logo-container">
        <el-icon v-if="isCollapsed" class="logo-icon" @click="toggleSidebar">
          <Icon icon="solar:hamburger-menu-bold-duotone" />
        </el-icon>
        <template v-else>
          <el-icon class="menu-trigger" @click="toggleSidebar">
            <Icon icon="solar:hamburger-menu-bold-duotone" />
          </el-icon>
          <Icon icon="solar:code-scan-bold-duotone" class="logo-icon-main" />
          <span class="logo-text">JCYC 活码系统</span>
        </template>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        class="sidebar-menu"
        router
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon>
            <Icon :icon="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>

      <!-- 用户信息 -->
      <div class="user-info">
        <el-avatar :size="36">管</el-avatar>
        <div v-if="!isCollapsed" class="user-detail">
          <span class="user-name">管理员</span>
          <span class="user-email">admin@demo.com</span>
        </div>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Icon } from '@iconify/vue'

const route = useRoute()
const isCollapsed = ref(false)

const menuItems = [
  { path: '/', title: '首页', icon: 'solar:home-2-bold-duotone' },
  { path: '/guide', title: '使用指南', icon: 'solar:book-bold-duotone' },
  { path: '/config', title: '配置管理', icon: 'solar:settings-bold-duotone' },
  { path: '/campaigns', title: '活动管理', icon: 'solar:flag-bold-duotone' },
  { path: '/qrcode', title: '二维码管理', icon: 'solar:qr-code-bold-duotone' },
  { path: '/scan-dashboard', title: '扫码看板', icon: 'solar:chart-2-bold-duotone' },
]

const activeMenu = computed(() => route.path)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #fff;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  border-bottom: 1px solid var(--border-color);
}

.menu-trigger {
  cursor: pointer;
  font-size: 20px;
  color: var(--text-secondary);
  
  &:hover {
    color: var(--primary-color);
  }
}

.logo-icon {
  font-size: 24px;
  cursor: pointer;
  color: var(--text-secondary);
  margin: 0 auto;
  
  &:hover {
    color: var(--primary-color);
  }
}

.logo-icon-main {
  font-size: 24px;
  color: var(--primary-color);
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  border: none;
  padding: 8px;
  
  .el-menu-item {
    border-radius: 8px;
    margin-bottom: 4px;
    height: 44px;
    
    &.is-active {
      background: linear-gradient(135deg, rgba(29, 78, 216, 0.1), rgba(29, 78, 216, 0.05));
      color: var(--primary-color);
    }
  }
}

.user-info {
  padding: 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  
  .user-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
  }
  
  .user-email {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.main-content {
  background: var(--bg-color);
  padding: 24px;
  overflow-y: auto;
}
</style>
