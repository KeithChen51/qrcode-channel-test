<template>
  <div class="home-page">
    <div class="page-header">
      <h1>欢迎使用 JCYC 活码系统</h1>
      <p>这是您的地推数据概览</p>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-grid">
      <el-card shadow="hover">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">小程序配置</span>
            <div class="icon-box primary">
              <Icon icon="solar:settings-bold-duotone" width="20" />
            </div>
          </div>
          <div class="stat-value" :class="configStatus.class">
            {{ configStatus.text }}
          </div>
          <p class="stat-desc">{{ configStatus.desc }}</p>
        </div>
      </el-card>

      <el-card shadow="hover">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">二维码数量</span>
            <div class="icon-box blue">
              <Icon icon="solar:qr-code-bold-duotone" width="20" />
            </div>
          </div>
          <div class="stat-value">{{ stats.qrcodeCount }}</div>
          <p class="stat-desc">已生成的推广码</p>
        </div>
      </el-card>

      <el-card shadow="hover">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">总扫码次数</span>
            <div class="icon-box green">
              <Icon icon="solar:eye-bold-duotone" width="20" />
            </div>
          </div>
          <div class="stat-value">{{ stats.scanCount }}</div>
          <p class="stat-desc">累计扫码次数</p>
        </div>
      </el-card>
    </div>

    <!-- 快捷操作卡片 -->
    <div class="action-grid">
      <el-card shadow="hover" class="hover-scale">
        <div class="action-card">
          <div class="action-header">
            <div class="icon-box primary">
              <Icon icon="solar:settings-bold-duotone" width="24" />
            </div>
            <span class="action-title">配置管理</span>
          </div>
          <p class="action-desc">配置小程序AppID和密钥</p>
          <el-button type="primary" @click="$router.push('/config')">
            <Icon icon="solar:arrow-right-linear" width="16" />
            查看配置
          </el-button>
        </div>
      </el-card>

      <el-card shadow="hover" class="hover-scale">
        <div class="action-card">
          <div class="action-header">
            <div class="icon-box blue">
              <Icon icon="solar:qr-code-bold-duotone" width="24" />
            </div>
            <span class="action-title">生成二维码</span>
          </div>
          <p class="action-desc">为门店和服务人员生成专属推广码</p>
          <el-button type="primary" @click="$router.push('/qrcode')">
            <Icon icon="solar:arrow-right-linear" width="16" />
            生成二维码
          </el-button>
        </div>
      </el-card>

      <el-card shadow="hover" class="hover-scale">
        <div class="action-card">
          <div class="action-header">
            <div class="icon-box purple">
              <Icon icon="solar:book-bold-duotone" width="24" />
            </div>
            <span class="action-title">使用指南</span>
          </div>
          <p class="action-desc">查看系统使用说明和常见问题</p>
          <el-button type="primary" @click="$router.push('/guide')">
            <Icon icon="solar:arrow-right-linear" width="16" />
            查看指南
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { getActiveConfig, getQrcodeList } from '@/api'

const loading = ref(false)
const activeConfig = ref<any>(null)
const qrcodes = ref<any[]>([])

const configStatus = computed(() => {
  if (activeConfig.value) {
    return {
      text: '已配置',
      class: 'text-success',
      desc: activeConfig.value.name || activeConfig.value.appId
    }
  }
  return {
    text: '未配置',
    class: 'text-warning',
    desc: '请先配置小程序信息'
  }
})

const stats = computed(() => ({
  qrcodeCount: qrcodes.value.length,
  scanCount: qrcodes.value.reduce((sum, q) => sum + (q.scanCount || 0), 0)
}))

onMounted(async () => {
  loading.value = true
  try {
    const [configRes, qrcodeRes] = await Promise.all([
      getActiveConfig(),
      getQrcodeList()
    ])
    activeConfig.value = configRes.data
    qrcodes.value = qrcodeRes.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.home-page {
  max-width: 1200px;
}

.stat-card {
  .stat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }
  
  .stat-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-secondary);
  }
  
  .stat-value {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    
    &.text-success {
      color: var(--success-color);
    }
    
    &.text-warning {
      color: var(--warning-color);
    }
  }
  
  .stat-desc {
    font-size: 13px;
    color: var(--text-muted);
    margin: 8px 0 0;
  }
}

.action-card {
  .action-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }
  
  .action-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
  }
  
  .action-desc {
    font-size: 14px;
    color: var(--text-secondary);
    margin: 0 0 16px;
  }
  
  .el-button {
    width: 100%;
  }
}
</style>
