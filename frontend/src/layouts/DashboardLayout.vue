<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapsed ? '64px' : '260px'" class="sidebar">
      <div class="logo-container">
        <el-icon v-if="isCollapsed" class="logo-icon" @click="toggleSidebar">
          <LocalIcon name="hamburger-menu" />
        </el-icon>
        <template v-else>
          <el-icon class="menu-trigger" @click="toggleSidebar">
            <LocalIcon name="hamburger-menu" />
          </el-icon>
          <LocalIcon name="code-scan" class="logo-icon-main" />
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
            <LocalIcon :name="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>

      <el-dropdown trigger="click" class="user-dropdown" @command="handleUserCommand">
        <div class="user-info" :class="{ collapsed: isCollapsed }">
          <el-avatar :size="36">管</el-avatar>
          <div v-if="!isCollapsed" class="user-detail">
            <span class="user-name">{{ username }}</span>
            <span class="user-email">管理员账号</span>
          </div>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="password">
              <LocalIcon name="lock" />
              修改密码
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <LocalIcon name="log-out" />
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-aside>

    <el-main class="main-content">
      <router-view />
    </el-main>

    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="420px"
      :close-on-click-modal="!passwordForced"
      :close-on-press-escape="!passwordForced"
      :show-close="!passwordForced"
      :before-close="handlePasswordBeforeClose"
    >
      <el-alert
        v-if="passwordForced"
        class="password-alert"
        title="首次登录需要先修改初始密码"
        type="warning"
        :closable="false"
      />
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="!passwordForced" @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSubmitting" @click="submitPassword">保存并重新登录</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { changePassword, getCurrentUser, logoutAdmin } from '@/api'
import { clearAuthSession, getAuthUser, setAuthUser } from '@/auth/session'
import LocalIcon from '@/components/LocalIcon.vue'

interface CurrentUserResult {
  data: {
    username: string
    mustChangePassword: boolean
  }
}

const route = useRoute()
const router = useRouter()
const isCollapsed = ref(false)
const storedUser = getAuthUser()
const username = ref(storedUser?.username || 'admin')
const passwordDialogVisible = ref(false)
const passwordForced = ref(false)
const passwordSubmitting = ref(false)
const passwordFormRef = ref<FormInstance>()

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const menuItems = [
  { path: '/', title: '首页', icon: 'home' },
  { path: '/guide', title: '使用指南', icon: 'book' },
  { path: '/config', title: '配置管理', icon: 'settings' },
  { path: '/campaigns', title: '活动管理', icon: 'flag' },
  { path: '/qrcode', title: '二维码管理', icon: 'qr-code' },
  { path: '/scan-dashboard', title: '扫码看板', icon: 'chart' }
]

const activeMenu = computed(() => route.path)

const validateNewPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) {
    callback(new Error('新密码至少需要包含字母和数字'))
    return
  }
  callback()
}

const validateConfirmPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

const passwordRules: FormRules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 64, message: '新密码长度需要在 8 到 64 位之间', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(() => {
  void fetchCurrentUser()
})

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
}

async function fetchCurrentUser() {
  try {
    const result = (await getCurrentUser()) as CurrentUserResult
    username.value = result.data.username
    setAuthUser(result.data.username, result.data.mustChangePassword)
    if (result.data.mustChangePassword) {
      openPasswordDialog(true)
    }
  } catch {
    // The API interceptor handles expired sessions and redirects to login.
  }
}

function handleUserCommand(command: string) {
  if (command === 'password') {
    openPasswordDialog(false)
  }
  if (command === 'logout') {
    logout()
  }
}

function openPasswordDialog(force: boolean) {
  passwordForced.value = force
  passwordDialogVisible.value = true
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

function handlePasswordBeforeClose(done: () => void) {
  if (passwordForced.value) {
    return
  }
  done()
}

async function submitPassword() {
  await passwordFormRef.value?.validate()
  passwordSubmitting.value = true
  try {
    await changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码已修改，请重新登录')
    await logoutAdmin().catch(() => undefined)
    clearAuthSession()
    await router.replace('/login')
  } finally {
    passwordSubmitting.value = false
  }
}

async function logout() {
  await logoutAdmin().catch(() => undefined)
  clearAuthSession()
  void router.replace('/login')
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

.user-dropdown {
  width: 100%;
}

.user-info {
  padding: 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;

  &:hover {
    background: #f8fafc;
  }

  &.collapsed {
    justify-content: center;
  }
}

.user-detail {
  display: flex;
  flex-direction: column;
  min-width: 0;

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

.password-alert {
  margin-bottom: 18px;
}

:deep(.el-dropdown-menu__item) {
  gap: 8px;
}
</style>
