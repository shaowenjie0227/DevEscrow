<template>
  <section class="personal-center" :class="{ 'personal-center--dialog': props.embedded }">
    <article class="page-card hero-card">
      <div class="hero-toolbar" :class="{ 'hero-toolbar--dialog': !props.showBackButton }">
        <button v-if="props.showBackButton" class="back-home" type="button" @click="goHome">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </button>
        <span class="hero-chip hero-chip--status">{{ developerStatusTag.label }}</span>
      </div>

      <div class="hero-header">
        <div class="hero-copy">
          <h1>个人中心</h1>
          <p class="hero-desc">编辑基础资料，提交开发者申请。</p>
        </div>

        <div class="hero-summary">
          <div class="hero-summary__item">
            <span>基础资料</span>
            <strong>{{ baseCompletionRate }}%</strong>
          </div>
          <div class="hero-summary__item">
            <span>开发者资料</span>
            <strong>{{ developerCompletionRate }}%</strong>
          </div>
          <div class="hero-summary__item">
            <span>当前状态</span>
            <strong>{{ developerStatusTag.label }}</strong>
          </div>
        </div>
      </div>

      <div class="hero-actions">
        <el-button type="primary" @click="goClientHome">进入用户工作台</el-button>
        <el-button v-if="isApprovedDeveloper" @click="goDeveloperHome">进入开发者工作台</el-button>
      </div>
    </article>

    <div class="dashboard-grid">
      <article class="page-card section-card">
        <div class="toolbar toolbar--section">
          <div>
            <h2>基础资料</h2>
            <p>用于展示昵称、联系方式和个人简介。</p>
          </div>
          <span class="section-chip">{{ baseCompletionRate }}% 已完善</span>
        </div>

        <el-form :model="baseForm" label-position="top" class="form-grid">
          <el-form-item label="昵称">
            <el-input v-model="baseForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="baseForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="baseForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item class="full-width" label="个人简介">
            <el-input v-model="baseForm.intro" type="textarea" :rows="5" placeholder="简单介绍一下你的背景和擅长方向" />
          </el-form-item>
          <div class="full-width submit-row">
            <span class="submit-note">保存后立即生效。</span>
            <el-button type="primary" :loading="savingBase" @click="saveBaseProfile">保存基础资料</el-button>
          </div>
        </el-form>
      </article>

      <article class="page-card section-card">
        <div class="toolbar toolbar--section">
          <div>
            <h2>开发者申请</h2>
            <p>填写实名、证件和技术栈后提交审核。</p>
          </div>
          <span class="section-chip section-chip--developer">{{ developerStatusTag.label }}</span>
        </div>

        <el-form :model="developerForm" label-position="top" class="form-grid">
          <el-form-item label="真实姓名">
            <el-input v-model="developerForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="身份证号码">
            <el-input v-model="developerForm.idCardNo" placeholder="请输入身份证号码" />
          </el-form-item>
          <el-form-item class="full-width" label="技术栈">
            <el-select v-model="developerForm.skillTagIds" multiple filterable placeholder="请选择你的技术栈" style="width: 100%">
              <el-option v-for="item in skillTags" :key="item.id" :label="item.tagName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="身份证正面">
            <div class="upload-stack">
              <UserImageUploadField v-model="developerForm.idCardFrontUrl" hint="上传身份证人像面，确保姓名和号码清晰可见。" />
            </div>
          </el-form-item>
          <el-form-item label="身份证反面">
            <div class="upload-stack">
              <UserImageUploadField v-model="developerForm.idCardBackUrl" hint="上传身份证国徽面，保证有效期信息完整。" />
            </div>
          </el-form-item>
          <el-form-item class="full-width" label="手持身份证照片">
            <div class="upload-stack">
              <UserImageUploadField v-model="developerForm.selfieUrl" hint="上传本人手持身份证照片，面部与证件信息需清晰可辨。" />
            </div>
          </el-form-item>
          <div class="full-width submit-row">
            <span class="submit-note">提交后进入审核。</span>
            <el-button type="primary" :loading="savingDeveloper" @click="saveDeveloperProfile">
              {{ developerStatusValue === 1 ? '重新提交申请' : '申请成为开发者' }}
            </el-button>
          </div>
        </el-form>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { applyDeveloperProfile, getDeveloperProfile, updateBasicProfile } from '@/api/modules/auth'
import { fetchCurrentSkillTags } from '@/api/modules/common'
import { useAuthStore } from '@/stores/auth'
import UserImageUploadField from '@/components/user/UserImageUploadField.vue'

const props = defineProps({
  embedded: {
    type: Boolean,
    default: false
  },
  showBackButton: {
    type: Boolean,
    default: true
  }
})

const router = useRouter()
const authStore = useAuthStore()

const savingBase = ref(false)
const savingDeveloper = ref(false)
const skillTags = ref([])

const baseForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  intro: ''
})

const developerForm = reactive({
  realName: '',
  idCardNo: '',
  idCardFrontUrl: '',
  idCardBackUrl: '',
  selfieUrl: '',
  skillTagIds: [],
  developerStatus: 0
})

const developerStatusValue = computed(() => Number(developerForm.developerStatus || 0))
const isApprovedDeveloper = computed(() => developerStatusValue.value === 2)

const developerStatusTag = computed(() => formatStatus(developerStatusValue.value))

const baseCompletionRate = computed(() => {
  const fields = [baseForm.nickname, baseForm.phone, baseForm.email, baseForm.intro]
  const completed = fields.filter((value) => String(value || '').trim()).length
  return Math.round((completed / fields.length) * 100)
})

const developerCompletionRate = computed(() => {
  const fields = [
    developerForm.realName,
    developerForm.idCardNo,
    developerForm.idCardFrontUrl,
    developerForm.idCardBackUrl,
    developerForm.selfieUrl
  ]
  const completed = fields.filter((value) => String(value || '').trim()).length
  const hasSkillTags = Array.isArray(developerForm.skillTagIds) && developerForm.skillTagIds.length > 0 ? 1 : 0
  return Math.round(((completed + hasSkillTags) / 6) * 100)
})

function formatStatus(value) {
  if (value === 1) return { label: '审核中', type: 'warning' }
  if (value === 2) return { label: '已通过', type: 'success' }
  if (value === 3) return { label: '已驳回', type: 'danger' }
  if (value === 4) return { label: '已封禁', type: 'danger' }
  return { label: '未申请', type: 'info' }
}

function goClientHome() {
  router.push('/client/home')
}

function goDeveloperHome() {
  router.push('/developer/home')
}

function goHome() {
  router.push('/')
}

async function loadSkillTags() {
  try {
    const response = await fetchCurrentSkillTags()
    skillTags.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载技术栈失败')
  }
}

async function loadProfile() {
  const profile = authStore.userInfo || {}
  baseForm.nickname = profile.nickname || ''
  baseForm.phone = profile.phone || ''
  baseForm.email = profile.email || ''
  baseForm.intro = profile.intro || ''

  try {
    const response = await getDeveloperProfile()
    const data = response.data || {}
    baseForm.nickname = data.nickname || baseForm.nickname
    baseForm.phone = data.phone || ''
    baseForm.email = data.email || ''
    baseForm.intro = data.intro || ''

    developerForm.realName = data.realName || ''
    developerForm.idCardNo = data.idCardNo || ''
    developerForm.idCardFrontUrl = data.idCardFrontUrl || ''
    developerForm.idCardBackUrl = data.idCardBackUrl || ''
    developerForm.selfieUrl = data.selfieUrl || ''
    developerForm.skillTagIds = Array.isArray(data.developerSkillTagIds) ? data.developerSkillTagIds : []
    developerForm.developerStatus = data.developerStatus ?? 0

    authStore.userInfo = {
      ...authStore.userInfo,
      nickname: data.nickname || authStore.userInfo?.nickname,
      phone: data.phone || '',
      email: data.email || '',
      intro: data.intro || '',
      developerStatus: data.developerStatus ?? authStore.userInfo?.developerStatus ?? 0,
      idVerifyStatus: data.idVerifyStatus ?? authStore.userInfo?.idVerifyStatus ?? 0,
      skillAuditStatus: data.skillAuditStatus ?? authStore.userInfo?.skillAuditStatus ?? 0
    }
    window.localStorage.setItem('user_info', JSON.stringify(authStore.userInfo))
  } catch (error) {
    ElMessage.error(error.message || '加载个人资料失败')
  }
}

async function saveBaseProfile() {
  savingBase.value = true
  try {
    const response = await updateBasicProfile({
      nickname: baseForm.nickname,
      phone: baseForm.phone,
      email: baseForm.email,
      intro: baseForm.intro
    })
    const data = response.data || {}
    authStore.userInfo = {
      ...authStore.userInfo,
      nickname: data.nickname,
      phone: data.phone,
      email: data.email,
      intro: data.intro
    }
    window.localStorage.setItem('user_info', JSON.stringify(authStore.userInfo))
    ElMessage.success('基础资料已保存')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    savingBase.value = false
  }
}

async function saveDeveloperProfile() {
  savingDeveloper.value = true
  try {
    await applyDeveloperProfile({
      realName: developerForm.realName,
      idCardNo: developerForm.idCardNo,
      idCardFrontUrl: developerForm.idCardFrontUrl,
      idCardBackUrl: developerForm.idCardBackUrl,
      selfieUrl: developerForm.selfieUrl,
      skillTagIds: developerForm.skillTagIds
    })
    ElMessage.success('开发者申请已提交，请等待管理员审核')
    await loadProfile()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    savingDeveloper.value = false
  }
}

onMounted(async () => {
  await loadSkillTags()
  await loadProfile()
})
</script>

<style scoped>
.personal-center {
  display: grid;
  gap: 22px;
  padding-bottom: 12px;
}

.personal-center--dialog {
  padding-bottom: 0;
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(300px, 0.75fr);
  gap: 24px;
  padding: 30px;
  background:
    radial-gradient(circle at top right, rgba(36, 85, 214, 0.12), transparent 24%),
    radial-gradient(circle at bottom left, rgba(15, 133, 124, 0.1), transparent 26%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.9));
}

.hero-main,
.hero-side {
  position: relative;
  z-index: 1;
}

.hero-main {
  display: grid;
  gap: 18px;
}

.hero-toolbar,
.hero-toolbar__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-toolbar--dialog {
  justify-content: flex-end;
}

.back-home {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  padding: 0 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  color: #0f172a;
  font-weight: 700;
  cursor: pointer;
  transition:
    transform 180ms ease,
    border-color 180ms ease,
    background-color 180ms ease;
}

.back-home:hover {
  transform: translateY(-1px);
  border-color: rgba(36, 85, 214, 0.24);
  background: rgba(255, 255, 255, 0.96);
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.05);
  color: #475569;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero-chip--status {
  background: rgba(36, 85, 214, 0.1);
  color: #2455d6;
}

.hero-eyebrow {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #94a3b8;
}

.hero-card h1 {
  margin: 0;
  font-size: clamp(34px, 4vw, 48px);
  line-height: 0.96;
  color: #0f172a;
}

.hero-desc {
  margin: 0;
  max-width: 760px;
  line-height: 1.8;
  color: #475569;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.hero-stat {
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 20px;
  background: rgba(248, 250, 252, 0.72);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.65);
}

.hero-stat span,
.signal-card__head span,
.progress-card__head span {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-stat strong {
  display: block;
  margin-top: 12px;
  font-size: 28px;
  line-height: 1;
  color: #0f172a;
}

.hero-stat p {
  margin-top: 10px;
  color: #64748b;
  line-height: 1.7;
}

.hero-side {
  display: grid;
  align-content: start;
  gap: 14px;
}

.signal-card,
.progress-card,
.step-list {
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 22px;
  background: rgba(248, 250, 252, 0.84);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.signal-card {
  background:
    linear-gradient(135deg, rgba(36, 85, 214, 0.12), rgba(255, 255, 255, 0.9) 42%),
    rgba(248, 250, 252, 0.86);
}

.signal-card__head,
.progress-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.signal-card__head strong,
.progress-card__head strong {
  color: #0f172a;
  font-size: 16px;
}

.signal-card p {
  margin-top: 12px;
  color: #475569;
  line-height: 1.8;
}

.progress-card--accent {
  background:
    linear-gradient(135deg, rgba(15, 133, 124, 0.1), rgba(255, 255, 255, 0.88) 40%),
    rgba(248, 250, 252, 0.84);
}

.progress-track {
  position: relative;
  height: 10px;
  margin-top: 14px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.16);
}

.progress-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2455d6, #6f8df1);
}

.progress-card--accent .progress-track span {
  background: linear-gradient(90deg, #0f857c, #38b2a6);
}

.step-list {
  display: grid;
  gap: 14px;
}

.step-item {
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr);
  gap: 12px;
  align-items: flex-start;
}

.step-indicator {
  width: 14px;
  height: 14px;
  margin-top: 4px;
  border: 2px solid rgba(148, 163, 184, 0.42);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 1);
}

.step-item.is-done .step-indicator {
  border-color: rgba(16, 185, 129, 0.3);
  background: #10b981;
}

.step-item strong {
  display: block;
  color: #0f172a;
  font-size: 15px;
}

.step-item p {
  margin-top: 6px;
  color: #64748b;
  line-height: 1.65;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.04fr) minmax(0, 0.96fr);
  gap: 20px;
}

.section-card {
  padding: 30px;
}

.toolbar--section {
  align-items: center;
}

.section-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(36, 85, 214, 0.1);
  color: #2455d6;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.section-chip {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(36, 85, 214, 0.08);
  color: #2455d6;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.section-chip--developer {
  background: rgba(15, 133, 124, 0.1);
  color: #0f857c;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.full-width {
  grid-column: 1 / -1;
}

.submit-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.75);
}

.submit-note {
  color: #64748b;
  line-height: 1.7;
}

.upload-stack {
  width: 100%;
}

.personal-center :deep(.el-form-item__label) {
  margin-bottom: 8px;
  color: #334155;
  font-weight: 600;
}

.personal-center :deep(.el-input__wrapper),
.personal-center :deep(.el-select__wrapper) {
  min-height: 48px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.14);
  transition: box-shadow 180ms ease, background-color 180ms ease;
}

.personal-center :deep(.el-input__wrapper:hover),
.personal-center :deep(.el-select__wrapper:hover) {
  box-shadow: inset 0 0 0 1px rgba(36, 85, 214, 0.22);
}

.personal-center :deep(.el-input__wrapper.is-focus),
.personal-center :deep(.el-select__wrapper.is-focused) {
  background: #ffffff;
  box-shadow:
    inset 0 0 0 1px rgba(36, 85, 214, 0.56),
    0 0 0 4px rgba(36, 85, 214, 0.08);
}

.personal-center :deep(.el-textarea__inner) {
  min-height: 146px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.14);
}

.personal-center :deep(.el-textarea__inner:focus) {
  background: #ffffff;
  box-shadow:
    inset 0 0 0 1px rgba(36, 85, 214, 0.56),
    0 0 0 4px rgba(36, 85, 214, 0.08);
}

@media (max-width: 960px) {
  .hero-card,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .hero-card,
  .section-card {
    padding: 22px;
  }

  .hero-toolbar {
    align-items: stretch;
  }

  .back-home {
    width: 100%;
    justify-content: center;
  }

  .submit-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
