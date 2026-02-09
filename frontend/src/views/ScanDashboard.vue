<template>
  <div class="scan-dashboard">
    <div class="page-header">
      <h1>扫码看板</h1>
      <p>查看扫码统计数据</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card shadow="hover">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">总扫码次数</span>
            <div class="icon-box blue">
              <Icon icon="solar:eye-bold-duotone" width="20" />
            </div>
          </div>
          <div class="stat-value">{{ stats.totalScans || 0 }}</div>
        </div>
      </el-card>

      <el-card shadow="hover">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">总注册人数</span>
            <div class="icon-box green">
              <Icon icon="solar:user-check-bold-duotone" width="20" />
            </div>
          </div>
          <div class="stat-value">{{ stats.totalRegisters || 0 }}</div>
        </div>
      </el-card>

      <el-card shadow="hover">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">今日扫码</span>
            <div class="icon-box purple">
              <Icon icon="solar:calendar-bold-duotone" width="20" />
            </div>
          </div>
          <div class="stat-value">{{ stats.todayScans || 0 }}</div>
        </div>
      </el-card>

      <el-card shadow="hover">
        <div class="stat-card">
          <div class="stat-header">
            <span class="stat-title">今日注册</span>
            <div class="icon-box primary">
              <Icon icon="solar:user-plus-bold-duotone" width="20" />
            </div>
          </div>
          <div class="stat-value">{{ stats.todayRegisters || 0 }}</div>
        </div>
      </el-card>
    </div>

    <!-- 扫码记录列表 -->
    <el-card>
      <template #header>
        <span>扫码记录</span>
      </template>

      <el-table :data="scanList" v-loading="loading" stripe>
        <el-table-column prop="scanId" label="扫码ID" width="200" />
        <el-table-column prop="storeName" label="门店" />
        <el-table-column prop="staffName" label="员工" />
        <el-table-column prop="campaignName" label="活动" />
        <el-table-column label="注册状态">
          <template #default="{ row }">
            <el-tag :type="row.isRegistered ? 'success' : 'info'">
              {{ row.isRegistered ? '已注册' : '未注册' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scanTime" label="扫码时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { getScanList, getScanStats } from '@/api'

const loading = ref(false)
const scanList = ref<any[]>([])
const stats = ref<any>({})

const fetchData = async () => {
  loading.value = true
  try {
    const [listRes, statsRes] = await Promise.all([
      getScanList(),
      getScanStats()
    ])
    scanList.value = listRes.data || []
    stats.value = statsRes.data || {}
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.scan-dashboard {
  max-width: 1400px;
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
    color: var(--text-secondary);
  }
  
  .stat-value {
    font-size: 32px;
    font-weight: 700;
    color: var(--text-primary);
  }
}
</style>
