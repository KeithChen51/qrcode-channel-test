<template>
  <div class="config-page">
    <div class="page-header">
      <h1>配置管理</h1>
      <p>管理微信小程序配置信息</p>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>小程序配置列表</span>
          <el-button type="primary" @click="showDialog = true">
            <Icon icon="solar:add-circle-bold" width="16" />
            新增配置
          </el-button>
        </div>
      </template>

      <el-table :data="configList" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="appId" label="AppID" />
        <el-table-column prop="pagePath" label="页面路径" />
        <el-table-column prop="defaultEnvVersion" label="环境">
          <template #default="{ row }">
            <el-tag :type="row.defaultEnvVersion === 'release' ? 'success' : 'warning'">
              {{ row.defaultEnvVersion === 'release' ? '正式版' : row.defaultEnvVersion === 'trial' ? '体验版' : '开发版' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'">
              {{ row.isActive ? '已激活' : '未激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleActivate(row)" :disabled="row.isActive">
              激活
            </el-button>
            <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="editingId ? '编辑配置' : '新增配置'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入小程序名称" />
        </el-form-item>
        <el-form-item label="AppID" required>
          <el-input v-model="form.appId" placeholder="请输入小程序AppID" />
        </el-form-item>
        <el-form-item label="AppSecret" required>
          <el-input v-model="form.appSecret" type="password" placeholder="请输入小程序AppSecret" />
        </el-form-item>
        <el-form-item label="页面路径" required>
          <el-input v-model="form.pagePath" placeholder="如：pages/index/index" />
        </el-form-item>
        <el-form-item label="环境版本">
          <el-select v-model="form.defaultEnvVersion" style="width: 100%">
            <el-option label="正式版" value="release" />
            <el-option label="体验版" value="trial" />
            <el-option label="开发版" value="develop" />
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
import { ref, onMounted, reactive } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfigList, createConfig, updateConfig, deleteConfig, activateConfig } from '@/api'

const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const editingId = ref<number | null>(null)
const configList = ref<any[]>([])

const form = reactive({
  name: '',
  appId: '',
  appSecret: '',
  pagePath: 'pages/index/index',
  defaultEnvVersion: 'release'
})

const resetForm = () => {
  form.name = ''
  form.appId = ''
  form.appSecret = ''
  form.pagePath = 'pages/index/index'
  form.defaultEnvVersion = 'release'
  editingId.value = null
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getConfigList()
    configList.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleEdit = (row: any) => {
  editingId.value = row.id
  Object.assign(form, row)
  form.appSecret = ''
  showDialog.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    if (editingId.value) {
      await updateConfig(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createConfig(form)
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
  await ElMessageBox.confirm('确定删除该配置吗？', '提示', { type: 'warning' })
  await deleteConfig(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

const handleActivate = async (row: any) => {
  await activateConfig(row.id)
  ElMessage.success('激活成功')
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.config-page {
  max-width: 1200px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
