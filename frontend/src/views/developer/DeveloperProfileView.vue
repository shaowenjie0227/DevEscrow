<template>
  <section class="page-card">
    <div class="toolbar">
      <div>
        <h2>开发者资料</h2>
        <p>先提交实名材料，再提交可接单技术栈，审核通过后即可进入接单状态。</p>
      </div>
    </div>

    <el-form :model="form" label-position="top" class="form-grid">
      <el-form-item label="真实姓名">
        <el-input v-model="form.realName" placeholder="身份证姓名" />
      </el-form-item>

      <el-form-item label="能力类型">
        <el-select v-model="form.developerRoleType" style="width: 100%">
          <el-option label="程序开发" :value="1" />
          <el-option label="文档撰写" :value="2" />
          <el-option label="都能做" :value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="身份证正面图片 URL">
        <el-input v-model="form.idCardFrontUrl" placeholder="身份证正面图片链接" />
      </el-form-item>

      <el-form-item label="身份证反面图片 URL">
        <el-input v-model="form.idCardBackUrl" placeholder="身份证反面图片链接" />
      </el-form-item>

      <el-form-item class="full-width" label="手持身份证自拍 URL">
        <el-input v-model="form.selfieUrl" placeholder="手持身份证自拍图片链接" />
      </el-form-item>

      <el-form-item class="full-width" label="技术栈（管理员管理）">
        <el-select v-model="form.skillTagIds" multiple filterable placeholder="请选择你会的技术栈" style="width: 100%">
          <el-option v-for="item in skillTags" :key="item.id" :label="item.tagName" :value="item.id" />
        </el-select>
      </el-form-item>

      <div class="full-width submit-row">
        <div class="submit-note">提交后进入审核，审核结果会通过邮箱通知你。</div>
        <div class="toolbar-actions">
          <el-button @click="loadSkillTags">刷新技术栈</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交审核</el-button>
        </div>
      </div>
    </el-form>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { applyDeveloperProfile, getDeveloperProfile, submitDeveloperSkillTags } from '@/api/modules/auth'
import { fetchAdminSkillTags } from '@/api/modules/admin'

const submitting = ref(false)
const skillTags = ref([])
const form = reactive({
  realName: '',
  developerRoleType: 1,
  idCardFrontUrl: '',
  idCardBackUrl: '',
  selfieUrl: '',
  skillTagIds: []
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
  try {
    const response = await getDeveloperProfile()
    const data = response.data || {}
    form.realName = data.realName || ''
    form.developerRoleType = data.developerRoleType || 1
    form.idCardFrontUrl = data.idCardFrontUrl || ''
    form.idCardBackUrl = data.idCardBackUrl || ''
    form.selfieUrl = data.selfieUrl || ''
    form.skillTagIds = data.developerSkillTagIds ? JSON.parse(data.developerSkillTagIds) : []
  } catch (error) {
    ElMessage.error(error.message || '加载资料失败')
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    await applyDeveloperProfile({
      realName: form.realName,
      developerRoleType: form.developerRoleType,
      idCardFrontUrl: form.idCardFrontUrl,
      idCardBackUrl: form.idCardBackUrl,
      selfieUrl: form.selfieUrl
    })
    await submitDeveloperSkillTags({ skillTagIds: form.skillTagIds })
    ElMessage.success('资料已提交审核')
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadSkillTags()
  await loadProfile()
})
</script>
