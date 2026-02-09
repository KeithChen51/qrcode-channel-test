<template>
  <div class="campaigns-page">
    <div class="page-header">
      <h1>活动管理</h1>
      <p>管理H5跳转页面的活动配置</p>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动列表</span>
          <el-button type="primary" @click="showDialog = true">
            <Icon icon="solar:add-circle-bold" width="16" />
            新建活动
          </el-button>
        </div>
      </template>

      <el-table :data="campaignList" v-loading="loading" stripe>
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="主题色">
          <template #default="{ row }">
            <div class="color-preview" :style="{ background: row.themeColor }"></div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗（占位） -->
    <el-dialog v-model="showDialog" :title="editingId ? '编辑活动' : '新建活动'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="H5页面标题" />
        </el-form-item>
        <el-form-item label="主题色">
          <el-color-picker v-model="form.themeColor" />
        </el-form-item>
        <el-form-item label="按钮文字">
          <el-input v-model="form.buttonText" placeholder="立即进入小程序" />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="form.status">
            <el-option label="进行中" value="active" />
            <el-option label="已结束" value="inactive" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCampaignList, createCampaign, updateCampaign, deleteCampaign } from '@/api'

const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const editingId = ref<number | null>(null)
const campaignList = ref<any[]>([])

const form = reactive({
  name: '',
  title: '欢迎扫码',
  themeColor: '#1890ff',
  buttonText: '立即进入小程序',
  status: 'active'
})

const resetForm = () => {
  form.name = ''
  form.title = '欢迎扫码'
  form.themeColor = '#1890ff'
  form.buttonText = '立即进入小程序'
  form.status = 'active'
  editingId.value = null
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getCampaignList()
    campaignList.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleEdit = (row: any) => {
  editingId.value = row.id
  Object.assign(form, row)
  showDialog.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    if (editingId.value) {
      await updateCampaign(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createCampaign(form)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    resetForm()
    fetchList()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该活动吗？', '提示', { type: 'warning' })
  await deleteCampaign(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.campaigns-page {
  max-width: 1200px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid var(--border-color);
}
</style>
