<template>
  <section class="home-manage">
    <article class="page-card">
      <div class="toolbar">
        <div>
          <h2>首页轮播管理</h2>
          <p class="toolbar-subtitle">维护首页左侧轮播图，支持图片、标题、按钮文案与跳转地址。</p>
        </div>
        <div class="toolbar-actions">
          <el-button @click="loadBanners">刷新</el-button>
          <el-button type="primary" @click="openCreateBanner">新增轮播</el-button>
        </div>
      </div>

      <el-table :data="banners" v-loading="bannerLoading">
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
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <div class="table-row-actions">
              <el-button size="small" @click="editBanner(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleBannerStatus(row)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteBanner(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </article>

    <article class="page-card">
      <div class="toolbar">
        <div>
          <h2>活动 / 公告管理</h2>
          <p class="toolbar-subtitle">维护首页右侧公告卡片，可配置为“公告”或“活动”。</p>
        </div>
        <div class="toolbar-actions">
          <el-button @click="loadNotices">刷新</el-button>
          <el-button type="primary" @click="openCreateNotice">新增内容</el-button>
        </div>
      </div>

      <el-table :data="notices" v-loading="noticeLoading">
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.noticeType === 2 ? 'warning' : 'primary'">{{ row.typeLabel || (row.noticeType === 2 ? '活动' : '公告') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="summary" label="摘要" min-width="340" />
        <el-table-column label="封面" width="130">
          <template #default="{ row }">
            <img v-if="row.coverUrl" :src="row.coverUrl" alt="notice cover" class="table-cover" />
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
              <el-button size="small" @click="editNotice(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleNoticeStatus(row)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteNotice(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </article>

    <el-dialog
      v-model="bannerDialogVisible"
      append-to-body
      :title="bannerDialogMode === 'create' ? '新增轮播' : '编辑轮播'"
      width="720px"
    >
      <el-form :model="bannerForm" label-position="top">
        <el-form-item label="标题">
          <el-input v-model="bannerForm.title" placeholder="活动或公告标题" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="bannerForm.subtitle" type="textarea" :rows="3" placeholder="说明轮播内容与引导文案" />
        </el-form-item>
        <el-form-item label="按钮文案">
          <el-input v-model="bannerForm.buttonText" placeholder="例如：查看详情 / 立即参与" />
        </el-form-item>
        <el-form-item label="跳转地址">
          <el-input v-model="bannerForm.targetUrl" placeholder="/publish 或 /market" />
        </el-form-item>
        <el-form-item label="封面图片">
          <ImageUploadField v-model="bannerForm.imageUrl" hint="建议上传横向大图，首页左侧轮播展示会更完整。" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="bannerForm.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bannerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bannerSubmitting" @click="submitBannerForm">
          {{ bannerDialogMode === 'create' ? '创建轮播' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="noticeDialogVisible"
      append-to-body
      :title="noticeDialogMode === 'create' ? '新增活动 / 公告' : '编辑活动 / 公告'"
      width="720px"
    >
      <el-form :model="noticeForm" label-position="top">
        <el-form-item label="内容类型">
          <el-select v-model="noticeForm.noticeType" style="width: 100%">
            <el-option label="公告" :value="1" />
            <el-option label="活动" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="noticeForm.title" placeholder="首页右侧展示标题" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="noticeForm.summary" type="textarea" :rows="3" placeholder="一句话说明活动或公告内容" />
        </el-form-item>
        <el-form-item label="跳转地址">
          <el-input v-model="noticeForm.targetUrl" placeholder="/market 或活动详情链接" />
        </el-form-item>
        <el-form-item label="封面图片">
          <ImageUploadField v-model="noticeForm.coverUrl" hint="可选。上传后会在右侧卡片顶部显示封面图。" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="noticeForm.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="noticeSubmitting" @click="submitNoticeForm">
          {{ noticeDialogMode === 'create' ? '创建内容' : '保存修改' }}
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
  createAdminBanner,
  createAdminHomeNotice,
  deleteAdminBanner,
  deleteAdminHomeNotice,
  fetchAdminBanners,
  fetchAdminHomeNotices,
  toggleAdminBannerStatus,
  toggleAdminHomeNoticeStatus,
  updateAdminBanner,
  updateAdminHomeNotice
} from '@/api/modules/admin'

const bannerLoading = ref(false)
const bannerSubmitting = ref(false)
const banners = ref([])
const bannerDialogVisible = ref(false)
const bannerDialogMode = ref('create')
const editingBannerId = ref(null)
const bannerForm = reactive({
  title: '',
  subtitle: '',
  buttonText: '',
  targetUrl: '',
  imageUrl: '',
  sortOrder: 0
})

const noticeLoading = ref(false)
const noticeSubmitting = ref(false)
const notices = ref([])
const noticeDialogVisible = ref(false)
const noticeDialogMode = ref('create')
const editingNoticeId = ref(null)
const noticeForm = reactive({
  noticeType: 1,
  title: '',
  summary: '',
  targetUrl: '',
  coverUrl: '',
  sortOrder: 0
})

async function loadBanners() {
  bannerLoading.value = true
  try {
    const response = await fetchAdminBanners()
    banners.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载轮播失败')
  } finally {
    bannerLoading.value = false
  }
}

async function loadNotices() {
  noticeLoading.value = true
  try {
    const response = await fetchAdminHomeNotices()
    notices.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载首页内容失败')
  } finally {
    noticeLoading.value = false
  }
}

function resetBannerForm() {
  bannerForm.title = ''
  bannerForm.subtitle = ''
  bannerForm.buttonText = ''
  bannerForm.targetUrl = ''
  bannerForm.imageUrl = ''
  bannerForm.sortOrder = 0
  editingBannerId.value = null
}

function resetNoticeForm() {
  noticeForm.noticeType = 1
  noticeForm.title = ''
  noticeForm.summary = ''
  noticeForm.targetUrl = ''
  noticeForm.coverUrl = ''
  noticeForm.sortOrder = 0
  editingNoticeId.value = null
}

function openCreateBanner() {
  bannerDialogMode.value = 'create'
  resetBannerForm()
  bannerDialogVisible.value = true
}

function editBanner(row) {
  bannerDialogMode.value = 'edit'
  editingBannerId.value = row.id
  bannerForm.title = row.title
  bannerForm.subtitle = row.subtitle
  bannerForm.buttonText = row.buttonText || ''
  bannerForm.targetUrl = row.targetUrl || ''
  bannerForm.imageUrl = row.imageUrl || ''
  bannerForm.sortOrder = row.sortOrder
  bannerDialogVisible.value = true
}

function openCreateNotice() {
  noticeDialogMode.value = 'create'
  resetNoticeForm()
  noticeDialogVisible.value = true
}

function editNotice(row) {
  noticeDialogMode.value = 'edit'
  editingNoticeId.value = row.id
  noticeForm.noticeType = row.noticeType || 1
  noticeForm.title = row.title
  noticeForm.summary = row.summary
  noticeForm.targetUrl = row.targetUrl || ''
  noticeForm.coverUrl = row.coverUrl || ''
  noticeForm.sortOrder = row.sortOrder
  noticeDialogVisible.value = true
}

async function submitBannerForm() {
  bannerSubmitting.value = true
  try {
    const payload = { ...bannerForm }
    if (bannerDialogMode.value === 'create') {
      await createAdminBanner(payload)
      ElMessage.success('轮播创建成功')
    } else {
      await updateAdminBanner(editingBannerId.value, payload)
      ElMessage.success('轮播保存成功')
    }
    bannerDialogVisible.value = false
    await loadBanners()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    bannerSubmitting.value = false
  }
}

async function submitNoticeForm() {
  noticeSubmitting.value = true
  try {
    const payload = { ...noticeForm }
    if (noticeDialogMode.value === 'create') {
      await createAdminHomeNotice(payload)
      ElMessage.success('首页内容创建成功')
    } else {
      await updateAdminHomeNotice(editingNoticeId.value, payload)
      ElMessage.success('首页内容保存成功')
    }
    noticeDialogVisible.value = false
    await loadNotices()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    noticeSubmitting.value = false
  }
}

async function toggleBannerStatus(row) {
  try {
    await toggleAdminBannerStatus(row.id, { status: row.status === 1 ? 2 : 1 })
    ElMessage.success('轮播状态已更新')
    await loadBanners()
  } catch (error) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

async function toggleNoticeStatus(row) {
  try {
    await toggleAdminHomeNoticeStatus(row.id, { status: row.status === 1 ? 2 : 1 })
    ElMessage.success('首页内容状态已更新')
    await loadNotices()
  } catch (error) {
    ElMessage.error(error.message || '更新状态失败')
  }
}

async function handleDeleteBanner(row) {
  try {
    await ElMessageBox.confirm(`确认删除轮播“${row.title}”吗？删除后无法恢复。`, '删除轮播', {
      type: 'warning'
    })
    await deleteAdminBanner(row.id)
    ElMessage.success('轮播已删除')
    await loadBanners()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function handleDeleteNotice(row) {
  try {
    await ElMessageBox.confirm(`确认删除首页内容“${row.title}”吗？删除后无法恢复。`, '删除首页内容', {
      type: 'warning'
    })
    await deleteAdminHomeNotice(row.id)
    ElMessage.success('首页内容已删除')
    await loadNotices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadBanners(), loadNotices()])
})
</script>

<style scoped>
.home-manage {
  display: grid;
  gap: 20px;
}

.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.toolbar h2 {
  margin: 0;
}

.toolbar-subtitle {
  margin: 8px 0 0;
  color: rgba(71, 85, 105, 0.9);
  line-height: 1.6;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.table-row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.table-cover {
  width: 92px;
  height: 56px;
  border-radius: 12px;
  object-fit: cover;
}

@media (max-width: 900px) {
  .toolbar {
    flex-direction: column;
  }
}
</style>
