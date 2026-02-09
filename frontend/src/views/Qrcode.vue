<template>
  <div class="qrcode-page">
    <div class="page-header">
      <h1>二维码管理</h1>
      <p>生成和管理地推二维码</p>
    </div>

    <el-card class="mb-4">
      <template #header>
        <span>生成二维码</span>
      </template>
      <el-form :model="form" label-width="100px" inline>
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
          <el-select v-model="form.campaignId" placeholder="选择推广活动（可选）" clearable style="width: 200px">
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

      <el-table :data="qrcodeList" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column label="二维码" width="100">
          <template #default="{ row }">
            <el-image v-if="row.qrcodeUrl" :src="row.qrcodeUrl" style="width: 60px; height: 60px" fit="contain" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="storeId" label="门店ID" />
        <el-table-column prop="storeName" label="门店名称" />
        <el-table-column prop="staffId" label="员工ID" />
        <el-table-column prop="staffName" label="员工名称" />
        <el-table-column prop="scanCount" label="扫码次数" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQrcodeList, generateQrcode, deleteQrcode, batchDeleteQrcode, getActiveCampaigns } from '@/api'

const loading = ref(false)
const generating = ref(false)
const qrcodeList = ref<any[]>([])
const selectedIds = ref<number[]>([])
const campaigns = ref<any[]>([])

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

const handleGenerate = async () => {
  if (!form.storeId || !form.staffId) {
    ElMessage.warning('请填写门店ID和员工ID')
    return
  }
  generating.value = true
  try {
    await generateQrcode(form)
    ElMessage.success('生成成功')
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    generating.value = false
  }
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该二维码吗？', '提示', { type: 'warning' })
  await deleteQrcode(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

const handleBatchDelete = async () => {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个二维码吗？`, '提示', { type: 'warning' })
  await batchDeleteQrcode(selectedIds.value)
  ElMessage.success('批量删除成功')
  fetchList()
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

onMounted(async () => {
  fetchList()
  // 加载活动列表
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
</style>
