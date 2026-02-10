<template>
  <div class="qrcode-page">
    <div class="page-header">
      <h1>二维码管理</h1>
      <p>生成并管理渠道二维码</p>
    </div>

    <el-card class="mb-4">
      <template #header>
        <span>生成二维码</span>
      </template>
      <el-form :model="form" label-width="90px" inline>
        <el-form-item label="门店ID" required>
          <el-input v-model="form.storeId" placeholder="请输入门店ID" />
        </el-form-item>
        <el-form-item label="门店名称">
          <el-input v-model="form.storeName" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="员工ID" required>
          <el-input v-model="form.staffId" placeholder="请输入员工ID" />
        </el-form-item>
        <el-form-item label="员工名称">
          <el-input v-model="form.staffName" placeholder="请输入员工名称" />
        </el-form-item>
        <el-form-item label="关联活动">
          <el-select
            v-model="form.campaignId"
            placeholder="选择活动（可选）"
            clearable
            style="width: 220px"
          >
            <el-option v-for="c in campaigns" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGenerate" :loading="generating">
            <Icon icon="solar:qr-code-bold" width="16" />
            生成二维码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>二维码列表</span>
          <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
            批量删除 ({{ selectedIds.length }})
          </el-button>
        </div>
      </template>

      <el-table
        :data="qrcodeList"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="编号" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ formatQrcodeNo(row.id) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="二维码" width="90">
          <template #default="{ row }">
            <el-image
              v-if="row.qrcodeUrl"
              :src="row.qrcodeUrl"
              :preview-src-list="[row.qrcodeUrl]"
              style="width: 56px; height: 56px; cursor: pointer"
              fit="contain"
              @click.stop="openDetail(row)"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="storeId" label="门店ID" min-width="100" />
        <el-table-column prop="storeName" label="门店名称" min-width="120" />
        <el-table-column prop="staffId" label="员工ID" min-width="100" />
        <el-table-column prop="staffName" label="员工名称" min-width="120" />
        <el-table-column prop="scanCount" label="扫码次数" width="90" />
        <el-table-column prop="registerCount" label="注册次数" width="90" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click.stop="openDetail(row)">详情</el-button>
            <el-button text type="danger" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="二维码详情" width="760px" destroy-on-close>
      <template v-if="detailRecord">
        <div class="detail-top">
          <el-tag type="info">{{ formatQrcodeNo(detailRecord.id) }}</el-tag>
          <span class="detail-id">ID: {{ detailRecord.id }}</span>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="门店ID">{{ displayValue(detailRecord.storeId) }}</el-descriptions-item>
          <el-descriptions-item label="门店名称">{{ displayValue(detailRecord.storeName) }}</el-descriptions-item>
          <el-descriptions-item label="员工ID">{{ displayValue(detailRecord.staffId) }}</el-descriptions-item>
          <el-descriptions-item label="员工名称">{{ displayValue(detailRecord.staffName) }}</el-descriptions-item>
          <el-descriptions-item label="扫码次数">{{ detailRecord.scanCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="注册次数">{{ detailRecord.registerCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ displayValue(detailRecord.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ displayValue(detailRecord.updatedAt) }}</el-descriptions-item>
        </el-descriptions>

        <div class="url-block">
          <div class="url-title">H5 跳转链接</div>
          <div class="url-row">
            <el-input :model-value="detailRecord.jumpPageUrl || ''" readonly />
            <el-button @click="copyText(detailRecord.jumpPageUrl)">复制</el-button>
            <el-button type="primary" plain @click="openExternal(detailRecord.jumpPageUrl)">打开</el-button>
          </div>
        </div>

        <div class="url-block">
          <div class="url-title">微信 URL Link</div>
          <div class="url-row">
            <el-input :model-value="detailRecord.urlLink || ''" readonly />
            <el-button @click="copyText(detailRecord.urlLink)">复制</el-button>
          </div>
        </div>

        <div class="url-block">
          <div class="url-title">二维码图片地址</div>
          <div class="url-row">
            <el-input :model-value="detailRecord.qrcodeUrl || ''" readonly />
            <el-button @click="copyText(detailRecord.qrcodeUrl)">复制</el-button>
            <el-button type="primary" plain @click="openExternal(detailRecord.qrcodeUrl)">打开</el-button>
          </div>
          <el-image
            v-if="detailRecord.qrcodeUrl"
            :src="detailRecord.qrcodeUrl"
            style="width: 110px; height: 110px; margin-top: 12px"
            fit="contain"
          />
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getQrcodeList,
  getQrcodeById,
  generateQrcode,
  deleteQrcode,
  batchDeleteQrcode,
  getActiveCampaigns
} from '@/api'

interface QrcodeRecord {
  id: number
  storeId?: string
  storeName?: string
  staffId?: string
  staffName?: string
  campaignId?: number
  urlLink?: string
  jumpPageUrl?: string
  qrcodeUrl?: string
  scanCount?: number
  registerCount?: number
  createdAt?: string
  updatedAt?: string
}

const loading = ref(false)
const generating = ref(false)
const qrcodeList = ref<QrcodeRecord[]>([])
const selectedIds = ref<number[]>([])
const campaigns = ref<any[]>([])

const detailVisible = ref(false)
const detailRecord = ref<QrcodeRecord | null>(null)

const form = reactive({
  storeId: '',
  storeName: '',
  staffId: '',
  staffName: '',
  campaignId: null as number | null
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getQrcodeList()
    qrcodeList.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const formatQrcodeNo = (id?: number) => {
  if (!id && id !== 0) {
    return '-'
  }
  return `QR-${String(id).padStart(6, '0')}`
}

const displayValue = (value: unknown) => {
  if (value === null || value === undefined) {
    return '-'
  }
  const text = String(value).trim()
  return text.length ? text : '-'
}

const openExternal = (url?: string) => {
  if (!url) {
    ElMessage.warning('链接为空')
    return
  }
  window.open(url, '_blank', 'noopener')
}

const copyText = async (text?: string) => {
  if (!text) {
    ElMessage.warning('内容为空')
    return
  }

  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    ElMessage.success('已复制')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const openDetail = async (row: QrcodeRecord) => {
  if (!row?.id) {
    return
  }

  try {
    const res = await getQrcodeById(row.id)
    detailRecord.value = (res.data || row) as QrcodeRecord
  } catch (error) {
    detailRecord.value = row
  }

  detailVisible.value = true
}

const handleRowClick = (row: QrcodeRecord, column: any) => {
  if (column?.type === 'selection') {
    return
  }
  openDetail(row)
}

const handleGenerate = async () => {
  if (!form.storeId || !form.staffId) {
    ElMessage.warning('请填写门店ID和员工ID')
    return
  }

  generating.value = true
  try {
    const res = await generateQrcode(form)
    const created = res.data as QrcodeRecord
    ElMessage.success(`生成成功：${formatQrcodeNo(created?.id)}`)
    await fetchList()
    if (created?.id) {
      await openDetail(created)
    }
  } catch (e) {
    console.error(e)
  } finally {
    generating.value = false
  }
}

const handleDelete = async (row: QrcodeRecord) => {
  await ElMessageBox.confirm(`确定删除 ${formatQrcodeNo(row.id)} 吗？`, '提示', { type: 'warning' })
  await deleteQrcode(row.id)
  ElMessage.success('删除成功')
  await fetchList()
}

const handleBatchDelete = async () => {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个二维码吗？`, '提示', {
    type: 'warning'
  })
  await batchDeleteQrcode(selectedIds.value)
  ElMessage.success('批量删除成功')
  await fetchList()
}

const handleSelectionChange = (selection: QrcodeRecord[]) => {
  selectedIds.value = selection.map((item) => item.id)
}

onMounted(async () => {
  await fetchList()
  try {
    const res = await getActiveCampaigns()
    campaigns.value = res.data || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped lang="scss">
.qrcode-page {
  max-width: 1400px;
}

.mb-4 {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.detail-id {
  color: var(--text-secondary);
  font-size: 13px;
}

.url-block {
  margin-top: 16px;
}

.url-title {
  margin-bottom: 8px;
  font-weight: 600;
}

.url-row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 8px;
}
</style>
