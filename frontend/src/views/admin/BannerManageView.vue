<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>首页轮播 / 公告管理</h2>
      <div class="toolbar-actions">
        <el-button @click="loadBanners">刷新</el-button>
        <el-button type="primary" @click="openCreate">新建轮播</el-button>
      </div>
    </div>

    <el-table :data="banners" v-loading="loading">
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="subtitle" label="副标题" min-width="340" />
      <el-table-column label="封面" width="130">
        <template #default="{ row }">
          <img v-if="row.imageUrl" :src="row.imageUrl" alt="banner cover" class="table-cover" />
          <span v-else>未上传</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editBanner(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新建轮播' : '编辑轮播'" width="720px">
      <el-form :model="form" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="活动或公告标题" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" type="textarea" :rows="3" placeholder="说明活动内容或公告信息" />
        </el-form-item>
        <el-form-item label="按钮文案">
          <el-input v-model="form.buttonText" placeholder="例如：查看活动 / 了解公告" />
        </el-form-item>
        <el-form-item label="跳转地址">
          <el-input v-model="form.targetUrl" placeholder="/publish 或 /admin" />
        </el-form-item>
        <el-form-item label="封面图片">
          <ImageUploadField v-model="form.imageUrl" hint="轮播图建议使用横向大图，视觉效果会更完整。" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ dialogMode === 'create' ? '创建轮播' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ImageUploadField from '@/components/admin/ImageUploadField.vue'
import { createAdminBanner, fetchAdminBanners, updateAdminBanner } from '@/api/modules/admin'

const loading = ref(false)
const submitting = ref(false)
const banners = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const form = reactive({
  title: '',
  subtitle: '',
  buttonText: '',
  targetUrl: '',
  imageUrl: '',
  sortOrder: 0
})

async function loadBanners() {
  loading.value = true
  try {
    const response = await fetchAdminBanners()
    banners.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载轮播失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.title = ''
  form.subtitle = ''
  form.buttonText = ''
  form.targetUrl = ''
  form.imageUrl = ''
  form.sortOrder = 0
  editingId.value = null
}

function openCreate() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function editBanner(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.title = row.title
  form.subtitle = row.subtitle
  form.buttonText = row.buttonText || ''
  form.targetUrl = row.targetUrl || ''
  form.imageUrl = row.imageUrl || ''
  form.sortOrder = row.sortOrder
  dialogVisible.value = true
}

async function submitForm() {
  submitting.value = true
  try {
    const payload = { ...form }
    if (dialogMode.value === 'create') {
      await createAdminBanner(payload)
      ElMessage.success('轮播创建成功')
    } else {
      await updateAdminBanner(editingId.value, payload)
      ElMessage.success('轮播保存成功')
    }
    dialogVisible.value = false
    loadBanners()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  try {
    await updateAdminBanner(row.id, { ...row, status: row.status === 1 ? 2 : 1 })
    ElMessage.success('状态已更新')
    loadBanners()
  } catch (error) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

onMounted(loadBanners)
</script>

<style scoped>
.table-cover {
  width: 92px;
  height: 56px;
  border-radius: 12px;
  object-fit: cover;
}
</style>
