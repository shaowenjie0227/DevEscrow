<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>需求分类管理</h2>
      <div class="toolbar-actions">
        <el-button @click="loadCategories">刷新</el-button>
        <el-button type="primary" @click="openCreate">新建分类</el-button>
      </div>
    </div>

    <el-table :data="categories" v-loading="loading">
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column prop="categoryName" label="分类名称" min-width="220" />
      <el-table-column prop="description" label="说明" min-width="320" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editCategory(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新建分类' : '编辑分类'" width="560px">
      <el-form :model="form" label-position="top">
        <el-form-item label="分类名称">
          <el-input v-model="form.categoryName" placeholder="例如：Java 后端" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="给用户和管理员看的分类说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          {{ dialogMode === 'create' ? '创建分类' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createAdminDemandCategory,
  fetchAdminDemandCategories,
  toggleAdminDemandCategoryStatus,
  updateAdminDemandCategory
} from '@/api/modules/admin'

const loading = ref(false)
const submitting = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const form = reactive({
  categoryName: '',
  sortOrder: 0,
  description: ''
})

async function loadCategories() {
  loading.value = true
  try {
    const response = await fetchAdminDemandCategories()
    categories.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载分类失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.categoryName = ''
  form.sortOrder = 0
  form.description = ''
  editingId.value = null
}

function openCreate() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function editCategory(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.categoryName = row.categoryName
  form.sortOrder = row.sortOrder
  form.description = row.description || ''
  dialogVisible.value = true
}

async function submitForm() {
  submitting.value = true
  try {
    const payload = {
      categoryName: form.categoryName,
      sortOrder: form.sortOrder,
      description: form.description
    }
    if (dialogMode.value === 'create') {
      await createAdminDemandCategory(payload)
      ElMessage.success('分类创建成功')
    } else {
      await updateAdminDemandCategory(editingId.value, payload)
      ElMessage.success('分类保存成功')
    }
    dialogVisible.value = false
    loadCategories()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  try {
    await toggleAdminDemandCategoryStatus(row.id, { status: row.status === 1 ? 2 : 1 })
    ElMessage.success('状态已更新')
    loadCategories()
  } catch (error) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

onMounted(loadCategories)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}
</style>
