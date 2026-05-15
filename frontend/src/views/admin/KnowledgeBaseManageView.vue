<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>知识库管理</h2>
      <div class="toolbar-actions">
        <el-button @click="loadItems">刷新</el-button>
        <el-button type="primary" @click="openCreate">新建知识条目</el-button>
      </div>
    </div>

    <el-table :data="items" v-loading="loading">
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column prop="techName" label="技术名" width="160" />
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="intro" label="介绍" min-width="320" />
      <el-table-column label="封面" width="130">
        <template #default="{ row }">
          <img v-if="row.coverUrl" :src="row.coverUrl" alt="knowledge cover" class="table-cover" />
          <span v-else>未上传</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340" fixed="right">
        <template #default="{ row }">
          <div class="table-row-actions">
            <el-button size="small" @click="editItem(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      append-to-body
      :title="dialogMode === 'create' ? '新建知识条目' : '编辑知识条目'"
      width="720px"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="技术名">
          <el-input v-model="form.techName" placeholder="例如：Vue3" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="例如：Vue3 适合做什么" />
        </el-form-item>
        <el-form-item label="介绍">
          <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="一句话说明这个技术适合什么场景" />
        </el-form-item>
        <el-form-item label="封面图片">
          <ImageUploadField v-model="form.coverUrl" hint="知识库卡片封面建议保持统一风格，方便内容浏览。" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="详情页地址或外部文档地址" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ dialogMode === 'create' ? '创建知识条目' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUploadField from '@/components/admin/ImageUploadField.vue'
import {
  createAdminKnowledgeBase,
  deleteAdminKnowledgeBase,
  fetchAdminKnowledgeBases,
  toggleAdminKnowledgeBaseStatus,
  updateAdminKnowledgeBase
} from '@/api/modules/admin'

const loading = ref(false)
const submitting = ref(false)
const items = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const form = reactive({
  techName: '',
  title: '',
  intro: '',
  coverUrl: '',
  linkUrl: '',
  sortOrder: 0
})

async function loadItems() {
  loading.value = true
  try {
    const response = await fetchAdminKnowledgeBases()
    items.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载知识库失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.techName = ''
  form.title = ''
  form.intro = ''
  form.coverUrl = ''
  form.linkUrl = ''
  form.sortOrder = 0
  editingId.value = null
}

function openCreate() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function editItem(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.techName = row.techName
  form.title = row.title
  form.intro = row.intro
  form.coverUrl = row.coverUrl || ''
  form.linkUrl = row.linkUrl || ''
  form.sortOrder = row.sortOrder
  dialogVisible.value = true
}

async function submitForm() {
  submitting.value = true
  try {
    const payload = { ...form }
    if (dialogMode.value === 'create') {
      await createAdminKnowledgeBase(payload)
      ElMessage.success('知识条目创建成功')
    } else {
      await updateAdminKnowledgeBase(editingId.value, payload)
      ElMessage.success('知识条目保存成功')
    }
    dialogVisible.value = false
    await loadItems()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  try {
    await toggleAdminKnowledgeBaseStatus(row.id, { status: row.status === 1 ? 2 : 1 })
    ElMessage.success('状态已更新')
    await loadItems()
  } catch (error) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除知识条目“${row.title}”吗？删除后无法恢复。`, '删除知识条目', {
      type: 'warning'
    })
    await deleteAdminKnowledgeBase(row.id)
    ElMessage.success('知识条目已删除')
    await loadItems()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(loadItems)
</script>

<style scoped>
.table-cover {
  width: 92px;
  height: 56px;
  border-radius: 12px;
  object-fit: cover;
}
</style>
