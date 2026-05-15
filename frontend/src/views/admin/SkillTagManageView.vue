<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>技术栈管理</h2>
      <div class="toolbar-actions">
        <el-button @click="loadTags">刷新</el-button>
        <el-button type="primary" @click="openCreate">新建技术栈</el-button>
      </div>
    </div>

    <el-table :data="tags" v-loading="loading">
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column prop="tagName" label="技术栈名称" min-width="220" />
      <el-table-column prop="tagType" label="类型" width="160" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340" fixed="right">
        <template #default="{ row }">
          <div class="table-row-actions">
            <el-button size="small" @click="editTag(row)">编辑</el-button>
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
      :title="dialogMode === 'create' ? '新建技术栈' : '编辑技术栈'"
      width="560px"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="技术栈名称">
          <el-input v-model="form.tagName" placeholder="例如：SpringBoot" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.tagType" style="width: 100%">
            <el-option label="程序开发" value="developer" />
            <el-option label="文档撰写" value="document" />
            <el-option label="设计" value="design" />
            <el-option label="运维" value="ops" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ dialogMode === 'create' ? '创建技术栈' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminSkillTag,
  deleteAdminSkillTag,
  fetchAdminSkillTags,
  toggleAdminSkillTagStatus,
  updateAdminSkillTag
} from '@/api/modules/admin'

const loading = ref(false)
const submitting = ref(false)
const tags = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const form = reactive({
  tagName: '',
  tagType: 'developer',
  sortOrder: 0
})

async function loadTags() {
  loading.value = true
  try {
    const response = await fetchAdminSkillTags()
    tags.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载技术栈失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.tagName = ''
  form.tagType = 'developer'
  form.sortOrder = 0
  editingId.value = null
}

function openCreate() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function editTag(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.tagName = row.tagName
  form.tagType = row.tagType
  form.sortOrder = row.sortOrder
  dialogVisible.value = true
}

async function submitForm() {
  submitting.value = true
  try {
    const payload = { ...form }
    if (dialogMode.value === 'create') {
      await createAdminSkillTag(payload)
      ElMessage.success('技术栈创建成功')
    } else {
      await updateAdminSkillTag(editingId.value, payload)
      ElMessage.success('技术栈保存成功')
    }
    dialogVisible.value = false
    await loadTags()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  try {
    await toggleAdminSkillTagStatus(row.id, { status: row.status === 1 ? 2 : 1 })
    ElMessage.success('状态已更新')
    await loadTags()
  } catch (error) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除技术栈“${row.tagName}”吗？删除后无法恢复。`, '删除技术栈', {
      type: 'warning'
    })
    await deleteAdminSkillTag(row.id)
    ElMessage.success('技术栈已删除')
    await loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(loadTags)
</script>
