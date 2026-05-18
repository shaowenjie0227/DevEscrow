<template>
  <section class="page-card">
    <div class="toolbar">
      <div>
        <h2>用户管理</h2>
        <p>这里统一处理开发者审核、封禁时长管理、站内信与外部邮箱通知。</p>
      </div>
      <el-button @click="loadUsers">刷新</el-button>
    </div>

    <el-table :data="users" v-loading="loading">
      <el-table-column prop="userNo" label="用户编号" min-width="160" />
      <el-table-column prop="nickname" label="昵称" min-width="150" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="220" />
      <el-table-column label="账号状态" min-width="120">
        <template #default="{ row }">
          <el-tag :type="formatAccountStatus(row.status).type">{{ formatAccountStatus(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="封禁到期" min-width="180">
        <template #default="{ row }">
          <span class="ban-expire-text">
            {{ Number(row.status || 1) === 2 ? formatBanExpiry(row.banExpiresAt) : '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="开发者申请" min-width="140">
        <template #default="{ row }">
          <el-tag :type="formatAuditStatus(row.developerStatus).type">{{ formatAuditStatus(row.developerStatus).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary" @click="openReview(row)">查看审核</el-button>
            <el-button link type="warning" @click="openMessageDialog(row)">发通知</el-button>
            <el-button
              link
              :type="Number(row.status || 1) === 2 ? 'success' : 'danger'"
              @click="handleToggleStatus(row)"
            >
              {{ Number(row.status || 1) === 2 ? '解除封禁' : '封禁用户' }}
            </el-button>
            <el-button v-if="canAudit(row)" link type="success" @click="quickAudit(row, 1)">通过</el-button>
            <el-button v-if="canAudit(row)" link type="danger" @click="openReject(row)">驳回</el-button>
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
              <span>身份证号</span>
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
        <div class="dialog-footer">
          <el-button @click="reviewVisible = false">关闭</el-button>
          <el-button class="review-dialog__reject-btn" type="danger" :loading="auditing" @click="submitAudit(0)">驳回</el-button>
          <el-button type="primary" :loading="auditing" @click="submitAudit(1)">审核通过</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="messageVisible"
      append-to-body
      width="680px"
      title="发送通知"
    >
      <template v-if="messageTarget">
        <div class="message-dialog__summary">
          <div>
            <strong>{{ messageTarget.nickname || '未命名用户' }}</strong>
            <span>{{ messageTarget.userNo }}</span>
          </div>
          <em>{{ messageTarget.email ? '会同步发送到外部邮箱' : '未绑定邮箱，仅发送站内信' }}</em>
        </div>
      </template>
      <el-input v-model="messageForm.title" maxlength="120" placeholder="请输入通知标题" />
      <el-input
        v-model="messageForm.content"
        class="message-dialog__textarea"
        type="textarea"
        :rows="8"
        maxlength="2000"
        show-word-limit
        placeholder="请输入要单独发给该用户的通知内容。"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="messageVisible = false">取消</el-button>
          <el-button type="primary" :loading="sendingMessage" @click="submitMessage">发送通知</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="banVisible"
      append-to-body
      class="ban-dialog"
      width="720px"
      :show-close="false"
    >
      <template v-if="banTarget">
        <section class="ban-shell">
          <div class="ban-shell__hero">
            <div>
              <p class="ban-shell__eyebrow">Access Control</p>
              <h3>封禁用户</h3>
              <p class="ban-shell__desc">
                封禁后会立即限制账号访问，同时同步站内信；如果用户绑定了邮箱，还会发送一封外部通知邮件。
              </p>
            </div>
            <button class="ban-shell__close" type="button" @click="banVisible = false">关闭</button>
          </div>

          <div class="ban-shell__grid">
            <article class="ban-card">
              <span class="ban-card__label">目标用户</span>
              <strong>{{ banTarget.nickname || '未命名用户' }}</strong>
              <p>{{ banTarget.userNo }}</p>
              <small>{{ banTarget.email || '未绑定邮箱' }}</small>
            </article>

            <article class="ban-card ban-card--warn">
              <span class="ban-card__label">执行后会发生什么</span>
              <ul class="ban-list">
                <li>账号立即失去访问权限</li>
                <li>写入站内信留痕</li>
                <li>如已绑定邮箱，同步发送外部邮件</li>
                <li>临时封禁到期后自动解封</li>
              </ul>
            </article>
          </div>

          <div class="ban-block">
            <div class="ban-block__head">
              <strong>封禁时长</strong>
              <span>{{ banDurationText }}</span>
            </div>
            <div class="ban-duration">
              <button
                v-for="option in BAN_DURATION_OPTIONS"
                :key="option.value"
                class="ban-duration__option"
                :class="{ 'is-active': banForm.mode === option.value }"
                type="button"
                @click="banForm.mode = option.value"
              >
                <strong>{{ option.label }}</strong>
                <small>{{ option.desc }}</small>
              </button>
            </div>
            <div v-if="banForm.mode === 'custom'" class="ban-custom">
              <span>自定义封禁天数</span>
              <el-input-number v-model="banForm.customDays" :min="1" :max="3650" />
            </div>
            <div class="ban-preview">
              <strong>{{ banExpirePreviewLabel }}</strong>
              <span>{{ banExpirePreviewValue }}</span>
            </div>
          </div>

          <div class="ban-block">
            <div class="ban-block__head">
              <strong>封禁原因</strong>
              <span>用户将会在站内信和邮件里看到这里的说明</span>
            </div>
            <div class="ban-reasons">
              <button
                v-for="preset in BAN_REASON_PRESETS"
                :key="preset"
                class="ban-reason-chip"
                type="button"
                @click="applyBanReasonPreset(preset)"
              >
                {{ preset }}
              </button>
            </div>
            <el-input
              v-model="banForm.reason"
              class="ban-textarea"
              type="textarea"
              :rows="5"
              maxlength="255"
              show-word-limit
              placeholder="请输入具体封禁原因，例如：多次违规骚扰、恶意刷单、资料造假。"
            />
          </div>
        </section>
      </template>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="banVisible = false">取消</el-button>
          <el-button type="danger" :loading="banSubmitting" @click="submitBan">确认封禁</el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  auditAdminDeveloper,
  fetchAdminSkillTags,
  fetchAdminUsers,
  sendAdminUserMessage,
  updateAdminUserStatus
} from '@/api/modules/admin'

const BAN_DURATION_OPTIONS = [
  { value: '1d', label: '1 天', desc: '短期冷却' },
  { value: '3d', label: '3 天', desc: '轻度处罚' },
  { value: '7d', label: '7 天', desc: '标准封禁' },
  { value: '30d', label: '30 天', desc: '重度处罚' },
  { value: 'custom', label: '自定义', desc: '按天设定' },
  { value: 'permanent', label: '永久', desc: '不自动解封' }
]

const BAN_REASON_PRESETS = [
  '多次违规骚扰用户',
  '恶意刷单或扰乱交易秩序',
  '资料造假或身份信息异常',
  '站内沟通存在辱骂威胁行为'
]

const loading = ref(false)
const auditing = ref(false)
const sendingMessage = ref(false)
const banSubmitting = ref(false)
const reviewVisible = ref(false)
const messageVisible = ref(false)
const banVisible = ref(false)
const users = ref([])
const currentUser = ref(null)
const messageTarget = ref(null)
const banTarget = ref(null)
const auditRemark = ref('')
const skillTags = ref([])
const messageForm = reactive({
  title: '',
  content: ''
})
const banForm = reactive({
  mode: '7d',
  customDays: 14,
  reason: ''
})

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

const selectedBanDays = computed(() => {
  if (banForm.mode === 'permanent') {
    return 0
  }
  if (banForm.mode === 'custom') {
    return Number(banForm.customDays || 0)
  }
  return Number(String(banForm.mode).replace('d', ''))
})

const banDurationText = computed(() => {
  if (banForm.mode === 'permanent') {
    return '永久封禁'
  }
  return `${selectedBanDays.value} 天后自动解封`
})

const banExpirePreviewLabel = computed(() => {
  return banForm.mode === 'permanent' ? '封禁到期' : '预计自动解封'
})

const banExpirePreviewValue = computed(() => {
  if (banForm.mode === 'permanent') {
    return '不会自动解封，需要管理员手动解除'
  }
  const date = new Date()
  date.setDate(date.getDate() + selectedBanDays.value)
  return formatDateTime(date)
})

function canAudit(row) {
  return Number(row?.developerStatus || 0) === 1 || Number(row?.developerStatus || 0) === 3
}

function formatAccountStatus(value) {
  if (Number(value || 0) === 2) return { label: '已封禁', type: 'danger' }
  if (Number(value || 0) === 3) return { label: '已注销', type: 'info' }
  return { label: '正常', type: 'success' }
}

function formatAuditStatus(value) {
  if (value === 1) return { label: '审核中', type: 'warning' }
  if (value === 2) return { label: '已通过', type: 'success' }
  if (value === 3) return { label: '已驳回', type: 'danger' }
  if (value === 4) return { label: '已封禁', type: 'danger' }
  return { label: '未申请', type: 'info' }
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '-'
  }
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hours = `${date.getHours()}`.padStart(2, '0')
  const minutes = `${date.getMinutes()}`.padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

function formatBanExpiry(value) {
  return value ? formatDateTime(value) : '永久封禁'
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

function openMessageDialog(row) {
  messageTarget.value = row
  messageForm.title = ''
  messageForm.content = ''
  messageVisible.value = true
}

function openBanDialog(row) {
  banTarget.value = row
  banForm.mode = '7d'
  banForm.customDays = 14
  banForm.reason = row.banReason || ''
  banVisible.value = true
}

function applyBanReasonPreset(reason) {
  banForm.reason = reason
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

async function handleToggleStatus(row) {
  if (!row?.id) {
    return
  }

  if (Number(row.status || 1) === 2) {
    try {
      await ElMessageBox.confirm(`确认解除对 ${row.nickname || row.userNo} 的封禁吗？系统会同步发送站内信和邮箱通知。`, '解除封禁', {
        type: 'warning'
      })
    } catch {
      return
    }

    try {
      await updateAdminUserStatus(row.id, {
        status: 1,
        reason: '管理员解除封禁'
      })
      ElMessage.success('用户已解除封禁')
      await loadUsers()
    } catch (error) {
      ElMessage.error(error.message || '解除封禁失败')
    }
    return
  }

  openBanDialog(row)
}

async function submitBan() {
  if (!banTarget.value?.id) {
    return
  }
  if (!banForm.reason.trim()) {
    ElMessage.warning('请输入封禁原因')
    return
  }
  if (banForm.mode === 'custom' && selectedBanDays.value < 1) {
    ElMessage.warning('自定义封禁天数至少为 1 天')
    return
  }

  banSubmitting.value = true
  try {
    await updateAdminUserStatus(banTarget.value.id, {
      status: 2,
      reason: banForm.reason.trim(),
      days: selectedBanDays.value
    })
    ElMessage.success('封禁已生效，通知已同步发送')
    banVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '封禁用户失败')
  } finally {
    banSubmitting.value = false
  }
}

async function submitMessage() {
  if (!messageTarget.value?.id) {
    return
  }
  if (!messageForm.title.trim() || !messageForm.content.trim()) {
    ElMessage.warning('请先填写通知标题和内容')
    return
  }

  sendingMessage.value = true
  try {
    await sendAdminUserMessage(messageTarget.value.id, {
      title: messageForm.title.trim(),
      content: messageForm.content.trim()
    })
    ElMessage.success('通知已发送，已同步站内信和邮箱')
    messageVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '发送通知失败')
  } finally {
    sendingMessage.value = false
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

.ban-expire-text {
  color: #475569;
  font-size: 13px;
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

.dialog-footer {
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
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(241, 245, 249, 0.94));
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

.message-dialog__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
  padding: 16px 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.92), rgba(255, 255, 255, 0.96));
  border: 1px solid rgba(59, 130, 246, 0.12);
}

.message-dialog__summary strong {
  display: block;
  color: #0f172a;
}

.message-dialog__summary span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.message-dialog__summary em {
  color: #2563eb;
  font-style: normal;
  font-size: 13px;
  font-weight: 600;
}

.message-dialog__textarea {
  margin-top: 14px;
}

.ban-shell {
  display: grid;
  gap: 18px;
}

.ban-shell__hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(251, 146, 60, 0.18), transparent 28%),
    linear-gradient(145deg, rgba(255, 247, 237, 0.98), rgba(255, 255, 255, 0.96));
  border: 1px solid rgba(249, 115, 22, 0.12);
}

.ban-shell__eyebrow {
  margin: 0;
  color: #c2410c;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.ban-shell__hero h3 {
  margin: 12px 0 0;
  color: #111827;
  font-size: 28px;
  line-height: 1.08;
}

.ban-shell__desc {
  margin: 12px 0 0;
  color: #475569;
  line-height: 1.75;
}

.ban-shell__close {
  align-self: flex-start;
  min-height: 40px;
  padding: 0 14px;
  border: 0;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: #334155;
  cursor: pointer;
}

.ban-shell__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.ban-card {
  display: grid;
  gap: 8px;
  padding: 18px 18px 20px;
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.ban-card--warn {
  background: linear-gradient(180deg, rgba(255, 251, 235, 0.9), rgba(255, 255, 255, 0.96));
}

.ban-card__label {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.ban-card strong {
  color: #0f172a;
  font-size: 18px;
}

.ban-card p,
.ban-card small {
  margin: 0;
  color: #475569;
}

.ban-list {
  margin: 0;
  padding-left: 18px;
  color: #475569;
  line-height: 1.8;
}

.ban-block {
  padding: 18px;
  border-radius: 22px;
  background: #ffffff;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.ban-block__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.ban-block__head strong {
  color: #0f172a;
  font-size: 16px;
}

.ban-block__head span {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  text-align: right;
}

.ban-duration {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.ban-duration__option {
  display: grid;
  gap: 6px;
  padding: 14px 14px 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.68);
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.ban-duration__option strong {
  color: #0f172a;
  font-size: 15px;
}

.ban-duration__option small {
  color: #64748b;
  line-height: 1.5;
}

.ban-duration__option:hover,
.ban-duration__option.is-active {
  transform: translateY(-1px);
  border-color: rgba(249, 115, 22, 0.22);
  box-shadow: 0 16px 32px rgba(249, 115, 22, 0.12);
}

.ban-duration__option.is-active {
  background: linear-gradient(145deg, rgba(255, 247, 237, 0.96), rgba(255, 255, 255, 0.98));
}

.ban-custom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.9);
}

.ban-custom span {
  color: #334155;
  font-weight: 600;
}

.ban-preview {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.94), rgba(31, 41, 55, 0.92));
  color: #ffffff;
}

.ban-preview strong {
  display: block;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.ban-preview span {
  display: block;
  margin-top: 8px;
  font-size: 16px;
  line-height: 1.7;
}

.ban-reasons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.ban-reason-chip {
  min-height: 34px;
  padding: 0 12px;
  border: 0;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: #334155;
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
}

.ban-textarea {
  margin-top: 14px;
}

:deep(.ban-dialog .el-dialog) {
  border-radius: 28px;
  overflow: hidden;
}

:deep(.ban-dialog .el-dialog__header) {
  display: none;
}

:deep(.ban-dialog .el-dialog__body) {
  padding: 22px 22px 6px;
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
  .image-grid,
  .ban-shell__grid,
  .ban-duration {
    grid-template-columns: 1fr;
  }

  .image-card__head,
  .message-dialog__summary,
  .ban-block__head,
  .ban-custom,
  .ban-shell__hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .ban-shell__close {
    align-self: stretch;
  }
}
</style>
