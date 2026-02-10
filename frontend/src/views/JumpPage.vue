<template>
  <div class="jump-page">
    <div class="jump-container" :style="containerStyle">
      <img v-if="campaign.logoUrl" :src="campaign.logoUrl" class="logo" alt="logo" />
      <h1 class="title">{{ campaign.title || 'Welcome' }}</h1>
      <p v-if="campaign.subtitle" class="subtitle">{{ campaign.subtitle }}</p>

      <template v-if="isWechat">
        <div
          v-if="jssdkReady && hasOpenTagData"
          ref="openTagWrapper"
          class="open-tag-wrapper"
          v-html="openTagHtml"
        ></div>

        <el-button
          v-else
          type="primary"
          size="large"
          class="jump-button"
          :style="buttonStyle"
          :loading="jssdkInitializing"
          :disabled="true"
        >
          {{ jssdkInitializing ? 'Initializing WeChat...' : 'Open Mini Program' }}
        </el-button>

        <el-button
          size="small"
          class="fallback-button"
          :disabled="loading || !landing.canJump"
          @click="handleJump"
        >
          Fallback Jump
        </el-button>
      </template>

      <template v-else>
        <el-button
          type="primary"
          size="large"
          class="jump-button"
          :style="buttonStyle"
          :loading="jumping"
          :disabled="loading || !landing.canJump"
          @click="handleJump"
        >
          {{ campaign.buttonText || 'Open Mini Program' }}
        </el-button>
      </template>

      <p v-if="jssdkError" class="tip error">{{ jssdkError }}</p>
      <p class="tip">{{ tipText }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLandingData, getWechatJssdkSignature } from '@/api'

interface LandingState {
  qid: number
  scanId: string
  urlLink: string
  canJump: boolean
  message: string
  miniProgramOriginalId: string
  miniProgramPath: string
  miniProgramEnvVersion: string
}

interface CampaignState {
  title: string
  subtitle: string
  buttonText: string
  themeColor: string
  backgroundColor: string
  logoUrl: string
  backgroundImageUrl: string
}

interface WechatSignature {
  appId: string
  timestamp: number
  nonceStr: string
  signature: string
}

const route = useRoute()

const loading = ref(true)
const jumping = ref(false)
const jssdkInitializing = ref(false)
const jssdkReady = ref(false)
const jssdkError = ref('')

const isWechat = /MicroMessenger/i.test(window.navigator.userAgent)

const landing = ref<LandingState>({
  qid: 0,
  scanId: '',
  urlLink: '',
  canJump: false,
  message: '',
  miniProgramOriginalId: '',
  miniProgramPath: '',
  miniProgramEnvVersion: 'release'
})

const campaign = ref<CampaignState>({
  title: 'Welcome',
  subtitle: '',
  buttonText: 'Open Mini Program',
  themeColor: '#1d4ed8',
  backgroundColor: '#f5f7fa',
  logoUrl: '',
  backgroundImageUrl: ''
})

const openTagWrapper = ref<HTMLElement | null>(null)
let boundOpenTag: HTMLElement | null = null
let launchListener: ((event: Event) => void) | null = null
let errorListener: ((event: Event) => void) | null = null

const hasOpenTagData = computed(() =>
  Boolean(landing.value.miniProgramOriginalId && landing.value.miniProgramPath)
)

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
    return 'Loading...'
  }
  if (!landing.value.canJump) {
    return landing.value.message || 'Current QR code can not jump.'
  }
  if (isWechat) {
    if (jssdkReady.value && hasOpenTagData.value) {
      return 'Tap the button to open mini program inside WeChat.'
    }
    if (jssdkError.value) {
      return 'Open tag is unavailable, try fallback button.'
    }
    return 'Preparing WeChat open tag...'
  }
  return 'Tap button to open mini program.'
})

const openTagHtml = computed(() => {
  if (!hasOpenTagData.value) {
    return ''
  }

  const username = escapeHtmlAttr(landing.value.miniProgramOriginalId)
  const path = escapeHtmlAttr(landing.value.miniProgramPath)
  const envVersion = escapeHtmlAttr(landing.value.miniProgramEnvVersion || 'release')
  const buttonText = escapeHtml(campaign.value.buttonText || 'Open Mini Program')
  const bgColor = escapeHtmlAttr(campaign.value.themeColor || '#1d4ed8')
  const scriptCloseTag = '</scr' + 'ipt>'

  return `
<wx-open-launch-weapp username="${username}" path="${path}" env-version="${envVersion}">
  <script type="text/wxtag-template">
    <style>
      .launch-btn {
        width: 100%;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        background: ${bgColor};
        color: #fff;
        font-size: 16px;
        font-weight: 600;
      }
    </style>
    <div class="launch-btn">${buttonText}</div>
  ${scriptCloseTag}
</wx-open-launch-weapp>`.trim()
})

const handleJump = () => {
  if (!landing.value.canJump || !landing.value.urlLink) {
    ElMessage.warning(landing.value.message || 'Missing jump link.')
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
    landing.value.message = 'Invalid qid parameter.'
    return
  }

  try {
    const res = await getLandingData(qid)
    const data = (res as any).data || {}

    landing.value = {
      qid: data.qid || qid,
      scanId: data.scanId || '',
      urlLink: data.urlLink || '',
      canJump: Boolean(data.canJump),
      message: data.message || '',
      miniProgramOriginalId: data.miniProgramOriginalId || '',
      miniProgramPath: data.miniProgramPath || '',
      miniProgramEnvVersion: data.miniProgramEnvVersion || 'release'
    }

    campaign.value = {
      title: data.title || 'Welcome',
      subtitle: data.subtitle || '',
      buttonText: data.buttonText || 'Open Mini Program',
      themeColor: data.themeColor || '#1d4ed8',
      backgroundColor: data.backgroundColor || '#f5f7fa',
      logoUrl: data.logoUrl || '',
      backgroundImageUrl: data.backgroundImageUrl || ''
    }

    if (isWechat && landing.value.canJump) {
      if (hasOpenTagData.value) {
        await initWechatJssdk()
      } else {
        jssdkError.value = 'Missing mini program originalId/pagePath, fallback jump only.'
      }
    }
  } finally {
    loading.value = false
  }
}

const initWechatJssdk = async () => {
  if (!isWechat || !hasOpenTagData.value || jssdkReady.value || jssdkInitializing.value) {
    return
  }

  jssdkInitializing.value = true
  jssdkError.value = ''

  try {
    await loadWechatJssdkScript()

    const currentUrl = window.location.href.split('#')[0]
    const res = await getWechatJssdkSignature(currentUrl)
    const signature = (res as any).data as WechatSignature

    const wx = (window as any).wx
    if (!wx || typeof wx.config !== 'function') {
      throw new Error('WeChat JS-SDK not found.')
    }

    wx.config({
      debug: false,
      appId: signature.appId,
      timestamp: signature.timestamp,
      nonceStr: signature.nonceStr,
      signature: signature.signature,
      jsApiList: ['checkJsApi'],
      openTagList: ['wx-open-launch-weapp']
    })

    wx.ready(async () => {
      jssdkReady.value = true
      jssdkInitializing.value = false
      await bindOpenTagEvents()
    })

    wx.error((err: any) => {
      jssdkReady.value = false
      jssdkInitializing.value = false
      const errMsg = err?.errMsg || 'unknown error'
      jssdkError.value = `WeChat SDK init failed: ${errMsg}`
    })
  } catch (error: any) {
    jssdkReady.value = false
    jssdkInitializing.value = false
    jssdkError.value = error?.message || 'WeChat SDK initialization failed.'
  }
}

const loadWechatJssdkScript = () => {
  return new Promise<void>((resolve, reject) => {
    if ((window as any).wx) {
      resolve()
      return
    }

    const scriptId = 'wechat-jssdk-script'
    const existingScript = document.getElementById(scriptId) as HTMLScriptElement | null

    if (existingScript) {
      existingScript.addEventListener('load', () => resolve(), { once: true })
      existingScript.addEventListener('error', () => reject(new Error('Failed to load WeChat JS-SDK script.')), {
        once: true
      })
      return
    }

    const script = document.createElement('script')
    script.id = scriptId
    script.src = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js'
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Failed to load WeChat JS-SDK script.'))
    document.head.appendChild(script)
  })
}

const bindOpenTagEvents = async () => {
  await nextTick()

  if (!openTagWrapper.value || !jssdkReady.value) {
    return
  }

  const tag = openTagWrapper.value.querySelector('wx-open-launch-weapp') as HTMLElement | null
  if (!tag || tag === boundOpenTag) {
    return
  }

  unbindOpenTagEvents()

  launchListener = () => {
    jssdkError.value = ''
  }

  errorListener = (event: Event) => {
    const detail = (event as any)?.detail
    jssdkError.value = detail?.errMsg
      ? `WeChat launch failed: ${detail.errMsg}`
      : 'WeChat launch failed, try fallback jump.'
  }

  tag.addEventListener('launch', launchListener)
  tag.addEventListener('error', errorListener)
  boundOpenTag = tag
}

const unbindOpenTagEvents = () => {
  if (!boundOpenTag) {
    return
  }

  if (launchListener) {
    boundOpenTag.removeEventListener('launch', launchListener)
  }
  if (errorListener) {
    boundOpenTag.removeEventListener('error', errorListener)
  }

  boundOpenTag = null
  launchListener = null
  errorListener = null
}

const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#39;')

const escapeHtmlAttr = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#39;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

watch(
  () => [openTagHtml.value, jssdkReady.value],
  async () => {
    if (isWechat) {
      await bindOpenTagEvents()
    }
  }
)

onMounted(async () => {
  await fetchLanding()
})

onBeforeUnmount(() => {
  unbindOpenTagEvents()
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

.open-tag-wrapper {
  width: 100%;
}

.fallback-button {
  margin-top: 12px;
}

.tip {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 24px;
}

.tip.error {
  color: #ef4444;
}
</style>
