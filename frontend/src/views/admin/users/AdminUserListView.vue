<template>
  <section class="page-card">
    <div class="toolbar">
      <div>
        <h2>用户管理</h2>
        <p>统一查看普通用户、开发者申请资料，以及管理员审核结果。</p>
      </div>
      <el-button @click="loadUsers">刷新</el-button>
    </div>

    <el-table :data="users" v-loading="loading">
      <el-table-column prop="userNo" label="用户编号" min-width="160" />
      <el-table-column prop="nickname" label="昵称" min-width="150" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="220" />
      <el-table-column label="开发者申请" min-width="140">
        <template #default="{ row }">
          <el-tag :type="formatStatus(row.developerStatus).type">{{ formatStatus(row.developerStatus).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="实名审核" min-width="120">
        <template #default="{ row }">
          <el-tag :type="formatStatus(row.idVerifyStatus).type">{{ formatStatus(row.idVerifyStatus).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="技术栈审核" min-width="120">
        <template #default="{ row }">
          <el-tag :type="formatStatus(row.skillAuditStatus).type">{{ formatStatus(row.skillAuditStatus).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary" @click="openReview(row)">查看审核</el-button>
            <el-button v-if="row.developerStatus === 1 || row.developerStatus === 3" link type="success" @click="quickAudit(row, 1)">
              通过
            </el-button>
            <el-button v-if="row.developerStatus === 1 || row.developerStatus === 3" link type="danger" @click="openReject(row)">
              驳回
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="reviewVisible"
      append-to-body
      class="review-dialog"
      width="860px"
      title="开发者申请审核"
    >
      <template v-if="currentUser">
        <div class="review-dialog__body">
          <div class="review-grid">
            <div class="review-item">
              <span>用户编号</span>
              <strong>{{ currentUser.userNo }}</strong>
            </div>
            <div class="review-item">
              <span>昵称</span>
              <strong>{{ currentUser.nickname || '-' }}</strong>
            </div>
            <div class="review-item">
              <span>真实姓名</span>
              <strong>{{ currentUser.realName || '-' }}</strong>
            </div>
            <div class="review-item">
              <span>身份证号码</span>
              <strong>{{ currentUser.idCardNo || '-' }}</strong>
            </div>
            <div class="review-item review-item--wide">
              <span>技术栈</span>
              <div v-if="currentUserSkillTagNames.length" class="skill-tag-list">
                <el-tag v-for="tagName in currentUserSkillTagNames" :key="tagName" type="info" effect="plain">
                  {{ tagName }}
                </el-tag>
              </div>
              <strong v-else>-</strong>
            </div>
            <div class="review-item review-item--wide">
              <span>审核备注</span>
              <strong>{{ currentUser.skillAuditReason || '暂无' }}</strong>
            </div>
          </div>

          <div class="image-grid">
          <div class="image-card">
            <div class="image-card__head">
              <strong>身份证正面</strong>
              <small v-if="currentUser.idCardFrontUrl">点击放大查看</small>
            </div>
            <div class="image-card__frame">
              <el-image
                v-if="currentUser.idCardFrontUrl"
                class="review-image"
                :src="currentUser.idCardFrontUrl"
                :initial-index="getPreviewImageIndex(currentUser.idCardFrontUrl)"
                :preview-src-list="currentUserImageList"
                fit="contain"
                preview-teleported
                alt="身份证正面"
              />
              <div v-else class="image-empty">未上传</div>
            </div>
          </div>
          <div class="image-card">
            <div class="image-card__head">
              <strong>身份证反面</strong>
              <small v-if="currentUser.idCardBackUrl">点击放大查看</small>
            </div>
            <div class="image-card__frame">
              <el-image
                v-if="currentUser.idCardBackUrl"
                class="review-image"
                :src="currentUser.idCardBackUrl"
                :initial-index="getPreviewImageIndex(currentUser.idCardBackUrl)"
                :preview-src-list="currentUserImageList"
                fit="contain"
                preview-teleported
                alt="身份证反面"
              />
              <div v-else class="image-empty">未上传</div>
            </div>
          </div>
          <div class="image-card image-card--wide">
            <div class="image-card__head">
              <strong>手持身份证照片</strong>
              <small v-if="currentUser.selfieUrl">点击放大查看</small>
            </div>
            <div class="image-card__frame">
              <el-image
                v-if="currentUser.selfieUrl"
                class="review-image"
                :src="currentUser.selfieUrl"
                :initial-index="getPreviewImageIndex(currentUser.selfieUrl)"
                :preview-src-list="currentUserImageList"
                fit="contain"
                preview-teleported
                alt="手持身份证照片"
              />
              <div v-else class="image-empty">未上传</div>
            </div>
          </div>
        </div>

          <el-input
            v-model="auditRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核备注，驳回时建议说明修改要求。"
          />
        </div>
      </template>

      <template #footer>
        <div class="review-dialog__footer">
          <el-button @click="reviewVisible = false">关闭</el-button>
          <el-button class="review-dialog__reject-btn" type="danger" :loading="auditing" @click="submitAudit(0)">驳回</el-button>
          <el-button type="primary" :loading="auditing" @click="submitAudit(1)">审核通过</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditAdminDeveloper, fetchAdminSkillTags, fetchAdminUsers } from '@/api/modules/admin'

const loading = ref(false)
const auditing = ref(false)
const reviewVisible = ref(false)
const users = ref([])
const currentUser = ref(null)
const auditRemark = ref('')
const skillTags = ref([])

const skillTagNameMap = computed(() => {
  return Object.fromEntries((skillTags.value || []).map((item) => [String(item.id), item.tagName]))
})

const currentUserSkillTagNames = computed(() => {
  const ids = parseSkillTagIds(currentUser.value?.developerSkillTagIds)
  return ids.map((id) => skillTagNameMap.value[String(id)] || `ID ${id}`)
})

const currentUserImageList = computed(() => {
  return [currentUser.value?.idCardFrontUrl, currentUser.value?.idCardBackUrl, currentUser.value?.selfieUrl].filter(Boolean)
})

function formatStatus(value) {
  if (value === 1) return { label: '审核中', type: 'warning' }
  if (value === 2) return { label: '已通过', type: 'success' }
  if (value === 3) return { label: '已驳回', type: 'danger' }
  if (value === 4) return { label: '已封禁', type: 'danger' }
  return { label: '未申请', type: 'info' }
}

async function loadUsers() {
  loading.value = true
  try {
    const response = await fetchAdminUsers()
    users.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

async function loadSkillTags() {
  try {
    const response = await fetchAdminSkillTags()
    skillTags.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载技术栈失败')
  }
}

function parseSkillTagIds(value) {
  if (Array.isArray(value)) {
    return value
  }
  if (typeof value !== 'string' || !value.trim()) {
    return []
  }

  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return value
      .replace(/^\[|\]$/g, '')
      .split(',')
      .map((item) => Number(item.trim()))
      .filter((item) => Number.isFinite(item))
  }
}

function getPreviewImageIndex(url) {
  return currentUserImageList.value.findIndex((item) => item === url)
}

function openReview(row) {
  currentUser.value = row
  auditRemark.value = row.skillAuditReason || ''
  reviewVisible.value = true
}

function openReject(row) {
  openReview(row)
}

async function quickAudit(row, approve) {
  currentUser.value = row
  auditRemark.value = approve === 1 ? '资料审核通过' : ''
  await submitAudit(approve)
}

async function submitAudit(approve) {
  if (!currentUser.value?.id) {
    return
  }
  if (approve !== 1 && !auditRemark.value.trim()) {
    ElMessage.warning('驳回时请填写审核备注')
    return
  }

  const actionText = approve === 1 ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确认${actionText}该开发者申请吗？`, '审核确认', {
      type: approve === 1 ? 'success' : 'warning'
    })
  } catch {
    return
  }

  auditing.value = true
  try {
    await auditAdminDeveloper(currentUser.value.id, {
      approve,
      remark: auditRemark.value.trim()
    })
    ElMessage.success(`已${actionText}该申请`)
    reviewVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    auditing.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadUsers(), loadSkillTags()])
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.toolbar p {
  margin: 6px 0 0;
  color: #64748b;
}

.row-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-dialog__body {
  display: grid;
  gap: 20px;
  max-height: calc(100vh - 220px);
  overflow-y: auto;
  padding-right: 6px;
}

.review-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.review-dialog__reject-btn {
  color: #fff !important;
  border-color: transparent !important;
  background: linear-gradient(135deg, #fb7185, #ef4444) !important;
  box-shadow: 0 14px 28px rgba(239, 68, 68, 0.22);
}

.review-dialog__reject-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 34px rgba(239, 68, 68, 0.28);
}

.review-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.review-item {
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.18);
  display: grid;
  gap: 6px;
}

.review-item span {
  font-size: 13px;
  color: #64748b;
}

.review-item strong {
  color: #0f172a;
  word-break: break-all;
}

.skill-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.review-item--wide {
  grid-column: 1 / -1;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.image-card {
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(248, 250, 252, 0.9));
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.04);
}

.image-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px 0;
}

.image-card__head strong {
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
}

.image-card__head small {
  color: #94a3b8;
  font-size: 12px;
  white-space: nowrap;
}

.image-card__frame {
  overflow: hidden;
  margin: 12px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background:
    linear-gradient(180deg, rgba(241, 245, 249, 0.88), rgba(255, 255, 255, 0.98)),
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.08), transparent 35%);
}

.review-image,
.image-empty {
  display: grid;
  width: 100%;
  min-height: 280px;
}

.review-image {
  cursor: zoom-in;
}

:deep(.review-image .el-image__inner) {
  width: 100%;
  height: 280px;
  object-fit: contain;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.08), transparent 35%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(241, 245, 249, 0.94));
}

.image-empty {
  place-items: center;
  color: #94a3b8;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(241, 245, 249, 0.94));
}

.image-card--wide {
  grid-column: 1 / -1;
}

.image-card--wide .review-image,
.image-card--wide .image-empty {
  min-height: 340px;
}

:deep(.image-card--wide .review-image .el-image__inner) {
  height: 340px;
}

:deep(.review-dialog) {
  width: min(860px, calc(100vw - 32px)) !important;
  margin-top: 4vh !important;
}

@media (max-width: 768px) {
  .review-dialog__body {
    max-height: calc(100vh - 190px);
    padding-right: 2px;
  }

  .review-grid,
  .image-grid {
    grid-template-columns: 1fr;
  }

  .image-card__head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
