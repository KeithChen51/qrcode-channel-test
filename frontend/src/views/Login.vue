<template>
  <main class="login-page">
    <section class="login-shell">
      <div class="brand-panel">
        <div class="brand-mark">
          <LocalIcon name="code-scan" width="32" height="32" />
        </div>
        <div>
          <h1>JCYC 活码系统</h1>
          <p>渠道二维码管理后台</p>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <h2>账号登录</h2>
        <el-form-item label="账号" prop="username">
          <el-input v-model.trim="form.username" size="large" autocomplete="username">
            <template #prefix>
              <LocalIcon name="user" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            size="large"
            type="password"
            autocomplete="current-password"
            show-password
          >
            <template #prefix>
              <LocalIcon name="lock" />
            </template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" class="login-button" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { loginAdmin } from '@/api'
import { setAuthSession } from '@/auth/session'
import LocalIcon from '@/components/LocalIcon.vue'

interface LoginResult {
  data: {
    token: string
    username: string
    mustChangePassword: boolean
  }
}

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const result = (await loginAdmin(form)) as LoginResult
    setAuthSession(result.data.token, result.data.username, result.data.mustChangePassword)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px 16px;
  background: #eef2f7;
}

.login-shell {
  width: min(920px, 100%);
  min-height: 420px;
  display: grid;
  grid-template-columns: 1fr 420px;
  background: #fff;
  border: 1px solid #d9e1ec;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.12);
}

.brand-panel {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 48px;
  background: #12243a;
  color: #fff;

  h1 {
    margin: 0 0 12px;
    font-size: 28px;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    color: rgba(255, 255, 255, 0.72);
    font-size: 15px;
  }
}

.brand-mark {
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #1d4ed8;
}

.login-form {
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  h2 {
    margin: 0 0 28px;
    color: #111827;
    font-size: 22px;
    letter-spacing: 0;
  }
}

.login-button {
  width: 100%;
  margin-top: 8px;
}

@media (max-width: 760px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 28px;
  }

  .login-form {
    padding: 28px;
  }
}
</style>
