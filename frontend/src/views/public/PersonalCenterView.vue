<template>
  <section class="page-card">
    <div class="toolbar">
      <div>
        <h2>个人中心</h2>
        <p>在这里统一管理你的基础资料、开发者信息、审核状态和提交记录。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="基础资料" name="base">
        <el-form :model="baseForm" label-position="top" class="form-grid">
          <el-form-item label="昵称">
            <el-input v-model="baseForm.nickname" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="baseForm.phone" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="baseForm.email" />
          </el-form-item>
          <el-form-item label="个人简介" class="full-width">
            <el-input v-model="baseForm.intro" type="textarea" :rows="5" />
          </el-form-item>
          <div class="full-width submit-row">
            <div class="submit-note">这里只管理你的基础资料。</div>
            <el-button type="primary" :loading="savingBase" @click="saveBaseProfile">保存修改</el-button>
          </div>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="开发者资料" name="developer">
        <el-form :model="developerForm" label-position="top" class="form-grid">
          <el-form-item label="真实姓名">
            <el-input v-model="developerForm.realName" />
          </el-form-item>
          <el-form-item label="能力类型">
            <el-select v-model="developerForm.developerRoleType" style="width: 100%">
              <el-option label="程序开发" :value="1" />
              <el-option label="文档撰写" :value="2" />
              <el-option label="都能做" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="身份证正面 URL">
            <el-input v-model="developerForm.idCardFrontUrl" />
          </el-form-item>
          <el-form-item label="身份证反面 URL">
            <el-input v-model="developerForm.idCardBackUrl" />
          </el-form-item>
          <el-form-item class="full-width" label="自拍照 URL">
            <el-input v-model="developerForm.selfieUrl" />
          </el-form-item>
          <el-form-item class="full-width" label="技术栈">
            <el-select v-model="developerForm.skillTagIds" multiple filterable style="width: 100%">
              <el-option v-for="item in skillTags" :key="item.id" :label="item.tagName" :value="item.id" />
            </el-select>
          </el-form-item>
          <div class="full-width submit-row">
            <div class="submit-note">修改开发者资料会重新进入审核。</div>
            <el-button type="primary" :loading="savingDeveloper" @click="saveDeveloperProfile">提交审核</el-button>
          </div>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="审核状态" name="status">
        <div class="detail-grid">
          <p><strong>开发者审核：</strong>{{ auditInfo.developerStatus }}</p>
          <p><strong>实名审核：</strong>{{ auditInfo.idVerifyStatus }}</p>
          <p><strong>技术栈审核：</strong>{{ auditInfo.skillAuditStatus }}</p>
          <p><strong>审核备注：</strong>{{ auditInfo.skillAuditReason || '暂无' }}</p>
        </div>
      </el-tab-pane>

      <el-tab-pane label="提交记录" name="records">
        <div class="detail-grid">
          <p><strong>最近提交人：</strong>{{ baseForm.nickname || '当前账号' }}</p>
          <p><strong>资料提交状态：</strong>{{ auditInfo.skillAuditStatus }}</p>
          <p><strong>说明：</strong>后续这里可以继续扩展为完整审核时间线。</p>
        </div>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { applyDeveloperProfile, getDeveloperProfile, submitDeveloperSkillTags, updateBasicProfile } from '@/api/modules/auth'
import { fetchAdminSkillTags } from '@/api/modules/admin'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const activeTab = ref('base')
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
  developerRoleType: 1,
  idCardFrontUrl: '',
  idCardBackUrl: '',
  selfieUrl: '',
  skillTagIds: []
})

const auditInfo = reactive({
  developerStatus: '未提交',
  idVerifyStatus: '未提交',
  skillAuditStatus: '未提交',
  skillAuditReason: ''
})

async function loadSkillTags() {
  try {
    const response = await fetchAdminSkillTags()
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

  try {
    const response = await getDeveloperProfile()
    const data = response.data || {}
    baseForm.intro = data.intro || ''
    developerForm.realName = data.realName || ''
    developerForm.developerRoleType = data.developerRoleType || 1
    developerForm.idCardFrontUrl = data.idCardFrontUrl || ''
    developerForm.idCardBackUrl = data.idCardBackUrl || ''
    developerForm.selfieUrl = data.selfieUrl || ''
    developerForm.skillTagIds = data.developerSkillTagIds ? JSON.parse(data.developerSkillTagIds) : []
    auditInfo.developerStatus = formatAudit(data.developerStatus)
    auditInfo.idVerifyStatus = formatAudit(data.idVerifyStatus)
    auditInfo.skillAuditStatus = formatAudit(data.skillAuditStatus)
    auditInfo.skillAuditReason = data.skillAuditReason || ''
  } catch {
    baseForm.intro = profile.intro || ''
  }
}

function formatAudit(value) {
  if (value === 1) return '审核中'
  if (value === 2) return '已通过'
  if (value === 3) return '已拒绝'
  return '未提交'
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
      developerRoleType: developerForm.developerRoleType,
      idCardFrontUrl: developerForm.idCardFrontUrl,
      idCardBackUrl: developerForm.idCardBackUrl,
      selfieUrl: developerForm.selfieUrl
    })
    await submitDeveloperSkillTags({ skillTagIds: developerForm.skillTagIds })
    ElMessage.success('开发者资料已提交审核')
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
