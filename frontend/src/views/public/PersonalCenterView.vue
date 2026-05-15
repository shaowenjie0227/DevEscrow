<template>
  <section class="personal-center">
    <article class="page-card hero-card">
      <div>
        <p class="hero-eyebrow">Personal Center</p>
        <h1>个人首页</h1>
        <p class="hero-desc">
          先维护你的基础资料，再按需申请成为开发者。提交真实姓名、身份证号码、证件照片和技术栈后，
          管理员审核通过，你才会获得接单和报价权限。
        </p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="goClientHome">进入用户工作台</el-button>
        <el-button v-if="isApprovedDeveloper" @click="goDeveloperHome">进入开发者工作台</el-button>
      </div>
    </article>

    <div class="dashboard-grid">
      <article class="page-card">
        <div class="toolbar">
          <div>
            <h2>基础资料</h2>
            <p>这部分决定你在平台上的基础展示信息。</p>
          </div>
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
            <span class="submit-note">基础资料不会影响你的普通用户权限。</span>
            <el-button type="primary" :loading="savingBase" @click="saveBaseProfile">保存基础资料</el-button>
          </div>
        </el-form>
      </article>

      <article class="page-card">
        <div class="toolbar">
          <div>
            <h2>开发者申请</h2>
            <p>申请通过前，你仍然是普通用户；申请通过后才会开放接单权限。</p>
          </div>
          <el-tag :type="developerStatusTag.type">{{ developerStatusTag.label }}</el-tag>
        </div>

        <div class="status-panel">
          <div class="status-item">
            <span>开发者审核</span>
            <strong>{{ developerStatusTag.label }}</strong>
          </div>
          <div class="status-item">
            <span>实名审核</span>
            <strong>{{ identityStatusTag.label }}</strong>
          </div>
          <div class="status-item">
            <span>技术栈审核</span>
            <strong>{{ skillAuditStatusTag.label }}</strong>
          </div>
          <div class="status-item status-item--wide">
            <span>审核备注</span>
            <strong>{{ developerForm.skillAuditReason || '暂无' }}</strong>
          </div>
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
            <span class="submit-note">
              提交后会进入管理员审核。若被驳回，可以根据备注修改后重新提交。
            </span>
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
import { ElMessage } from 'element-plus'
import { applyDeveloperProfile, getDeveloperProfile, updateBasicProfile } from '@/api/modules/auth'
import { fetchCurrentSkillTags } from '@/api/modules/common'
import { useAuthStore } from '@/stores/auth'
import UserImageUploadField from '@/components/user/UserImageUploadField.vue'

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
  developerStatus: 0,
  idVerifyStatus: 0,
  skillAuditStatus: 0,
  skillAuditReason: ''
})

const developerStatusValue = computed(() => Number(developerForm.developerStatus || 0))
const isApprovedDeveloper = computed(() => developerStatusValue.value === 2)

const developerStatusTag = computed(() => formatStatus(developerStatusValue.value))
const identityStatusTag = computed(() => formatStatus(Number(developerForm.idVerifyStatus || 0)))
const skillAuditStatusTag = computed(() => formatStatus(Number(developerForm.skillAuditStatus || 0)))

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
    developerForm.idVerifyStatus = data.idVerifyStatus ?? 0
    developerForm.skillAuditStatus = data.skillAuditStatus ?? 0
    developerForm.skillAuditReason = data.skillAuditReason || ''

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
  gap: 20px;
}

.hero-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.hero-eyebrow {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #94a3b8;
}

.hero-card h1 {
  margin: 0;
  font-size: 30px;
  color: #0f172a;
}

.hero-desc {
  margin: 12px 0 0;
  max-width: 760px;
  line-height: 1.8;
  color: #475569;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.status-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.status-item {
  padding: 14px 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.18);
  display: grid;
  gap: 6px;
}

.status-item span {
  font-size: 13px;
  color: #64748b;
}

.status-item strong {
  color: #0f172a;
}

.status-item--wide {
  grid-column: 1 / -1;
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
}

.submit-note {
  color: #64748b;
  line-height: 1.7;
}

.upload-stack {
  width: 100%;
}

@media (max-width: 960px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .hero-card {
    flex-direction: column;
  }
}

@media (max-width: 720px) {
  .form-grid,
  .status-panel {
    grid-template-columns: 1fr;
  }

  .submit-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
