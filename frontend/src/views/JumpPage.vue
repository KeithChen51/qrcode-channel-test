<template>
  <div class="jump-page">
    <div class="jump-container" :style="containerStyle">
      <img v-if="campaign.logoUrl" :src="campaign.logoUrl" class="logo" alt="logo" />
      <h1 class="title">{{ campaign.title || '欢迎扫码' }}</h1>
      <p v-if="campaign.subtitle" class="subtitle">{{ campaign.subtitle }}</p>
      <el-button
        type="primary"
        size="large"
        class="jump-button"
        :style="buttonStyle"
        :loading="jumping"
        :disabled="loading || !landing.canJump"
        @click="handleJump"
      >
        {{ campaign.buttonText || '立即进入小程序' }}
      </el-button>
      <p class="tip">{{ tipText }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLandingData } from '@/api'

const route = useRoute()
const loading = ref(true)
const jumping = ref(false)

const landing = ref({
  qid: 0,
  scanId: '',
  urlLink: '',
  canJump: false,
  message: ''
})

const campaign = ref({
  title: '欢迎扫码',
  subtitle: '',
  buttonText: '立即进入小程序',
  themeColor: '#1d4ed8',
  backgroundColor: '#f5f7fa',
  logoUrl: '',
  backgroundImageUrl: ''
})

const containerStyle = computed(() => ({
  backgroundColor: campaign.value.backgroundColor,
  backgroundImage: campaign.value.backgroundImageUrl ? `url(${campaign.value.backgroundImageUrl})` : 'none',
  backgroundSize: 'cover',
  backgroundPosition: 'center'
}))

const buttonStyle = computed(() => ({
  backgroundColor: campaign.value.themeColor,
  borderColor: campaign.value.themeColor
}))

const tipText = computed(() => {
  if (loading.value) {
    return '加载中，请稍候...'
  }
  if (!landing.value.canJump) {
    return landing.value.message || '当前二维码不可跳转'
  }
  return '点击按钮进入小程序'
})

const handleJump = () => {
  if (!landing.value.canJump || !landing.value.urlLink) {
    ElMessage.warning(landing.value.message || '当前二维码缺少跳转链接，请联系管理员')
    return
  }
  jumping.value = true
  window.location.href = landing.value.urlLink
}

const fetchLanding = async () => {
  const qidValue = Array.isArray(route.query.qid) ? route.query.qid[0] : route.query.qid
  const qid = Number(qidValue)
  if (!qid || Number.isNaN(qid) || qid <= 0) {
    loading.value = false
    landing.value.message = '二维码参数无效'
    return
  }

  try {
    const res = await getLandingData(qid)
    const data = res.data || {}
    landing.value = {
      qid: data.qid || qid,
      scanId: data.scanId || '',
      urlLink: data.urlLink || '',
      canJump: Boolean(data.canJump),
      message: data.message || ''
    }
    campaign.value = {
      title: data.title || '欢迎扫码',
      subtitle: data.subtitle || '',
      buttonText: data.buttonText || '立即进入小程序',
      themeColor: data.themeColor || '#1d4ed8',
      backgroundColor: data.backgroundColor || '#f5f7fa',
      logoUrl: data.logoUrl || '',
      backgroundImageUrl: data.backgroundImageUrl || ''
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLanding()
})
</script>

<style scoped lang="scss">
.jump-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.jump-container {
  text-align: center;
  padding: 48px 24px;
  max-width: 400px;
  width: 100%;
}

.logo {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  margin-bottom: 24px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px;
}

.subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0 0 32px;
}

.jump-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 12px;
}

.tip {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 24px;
}
</style>
