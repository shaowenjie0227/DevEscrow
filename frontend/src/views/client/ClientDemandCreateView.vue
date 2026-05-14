<template>
  <section class="page-card">
    <div class="toolbar demand-create-head">
      <div>
        <h2>发布需求</h2>
        <p>把目标、预算、素材和交付拆分整理清楚，后续报价和多阶段协作会顺畅很多。</p>
      </div>
    </div>

    <el-form :model="form" label-position="top" class="form-grid">
      <el-form-item label="需求标题">
        <el-input v-model="form.title" placeholder="例如：企业官网改版 + 后台内容管理" />
      </el-form-item>
      <el-form-item label="需求摘要">
        <el-input v-model="form.summary" placeholder="一句话说明目标、范围和核心交付" />
      </el-form-item>
      <el-form-item label="需求分类">
        <el-input v-model="form.category" placeholder="如：小程序 / 网站 / 后端开发" />
      </el-form-item>
      <el-form-item label="预计工期（天）">
        <el-input-number v-model="form.expectedDays" :min="1" style="width: 100%" />
      </el-form-item>
      <el-form-item label="预算下限">
        <el-input-number v-model="form.budgetMin" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="预算上限">
        <el-input-number v-model="form.budgetMax" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="交付类型">
        <el-select v-model="form.deliveryType" style="width: 100%">
          <el-option label="单阶段交付" :value="1" />
          <el-option label="多阶段交付" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item class="full-width" label="详细描述">
        <el-input
          v-model="form.detail"
          type="textarea"
          :rows="7"
          placeholder="写清楚目标用户、关键功能、技术边界、上线节点和验收标准。"
        />
      </el-form-item>

      <div class="full-width section-card">
        <div class="section-head">
          <div>
            <strong>需求图片</strong>
            <p>支持上传多张图片，系统默认会把第一张图作为封面展示。</p>
          </div>
          <div class="toolbar-actions">
            <input
              ref="imageInputRef"
              class="hidden-input"
              type="file"
              accept="image/*"
              multiple
              @change="handleImageSelect"
            />
            <el-button @click="imageInputRef?.click()">上传图片</el-button>
            <el-button v-if="form.images.length" text @click="clearImages">清空图片</el-button>
          </div>
        </div>

        <div v-if="form.images.length" class="image-grid">
          <article v-for="(image, index) in form.images" :key="`${image.name}-${index}`" class="image-card">
            <img :src="image.url" :alt="image.name" />
            <div class="image-card__body">
              <div>
                <strong>{{ image.name }}</strong>
                <p>{{ formatFileSize(image.size) }}</p>
              </div>
              <div class="image-card__actions">
                <span v-if="index === 0" class="cover-badge">默认封面</span>
                <el-button v-else text @click="setCover(index)">设为封面</el-button>
                <el-button text type="danger" @click="removeImage(index)">移除</el-button>
              </div>
            </div>
          </article>
        </div>
        <div v-else class="empty-tip">还没有上传图片，封面会在你上传的第一张图里自动生成。</div>
      </div>

      <div class="full-width section-card">
        <div class="section-head">
          <div>
            <strong>需求附件</strong>
            <p>可以上传文档、压缩包、原型或补充资料，方便开发者更准确理解需求。</p>
          </div>
          <div class="toolbar-actions">
            <input ref="attachmentInputRef" class="hidden-input" type="file" multiple @change="handleAttachmentSelect" />
            <el-button @click="attachmentInputRef?.click()">上传附件</el-button>
            <el-button v-if="form.attachments.length" text @click="clearAttachments">清空附件</el-button>
          </div>
        </div>

        <div v-if="form.attachments.length" class="attachment-list">
          <article
            v-for="(attachment, index) in form.attachments"
            :key="`${attachment.name}-${index}`"
            class="attachment-item"
          >
            <div>
              <strong>{{ attachment.name }}</strong>
              <p>{{ attachment.contentType || '通用文件' }} · {{ formatFileSize(attachment.size) }}</p>
            </div>
            <el-button text type="danger" @click="removeAttachment(index)">移除</el-button>
          </article>
        </div>
        <div v-else class="empty-tip">还没有上传附件，补充资料越清晰，开发者报价通常越准确。</div>
      </div>

      <div v-if="form.deliveryType === 2" class="full-width section-card">
        <div class="section-head">
          <div>
            <strong>多阶段交付规划</strong>
            <p>先设置分几段，再分别填写每阶段的任务需求和结算金额。</p>
          </div>
          <div class="stage-count">
            <span>分几段</span>
            <el-input-number v-model="stageCount" :min="2" :max="8" @change="handleStageCountChange" />
          </div>
        </div>

        <div class="stage-summary">
          阶段结算合计：<strong>{{ formatCurrency(stageAmountTotal) }}</strong>
          <span>预算区间：{{ formatCurrency(form.budgetMin) }} - {{ formatCurrency(form.budgetMax) }}</span>
        </div>

        <div class="stage-list">
          <article v-for="(stage, index) in form.stagePlans" :key="index" class="stage-card">
            <div class="stage-card__head">
              <strong>第 {{ index + 1 }} 阶段</strong>
              <span>按阶段验收和结算</span>
            </div>
            <el-form-item :label="`第 ${index + 1} 阶段任务需求`">
              <el-input
                v-model="stage.stageDesc"
                type="textarea"
                :rows="4"
                placeholder="写清楚这个阶段要完成什么、验收到什么程度。"
              />
            </el-form-item>
            <el-form-item :label="`第 ${index + 1} 阶段结算金额`">
              <el-input-number v-model="stage.stageAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </article>
        </div>
      </div>

      <div class="full-width submit-row">
        <div class="submit-note">
          图片和附件会随需求一起保存；如果选择多阶段交付，阶段规划也会同步进入后续协作流程。
        </div>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交需求</el-button>
      </div>
    </el-form>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createDemand } from '@/api/modules/demand'

const MAX_IMAGE_COUNT = 6
const MAX_IMAGE_SIZE = 2 * 1024 * 1024
const MAX_ATTACHMENT_COUNT = 5
const MAX_ATTACHMENT_SIZE = 5 * 1024 * 1024

const router = useRouter()
const submitting = ref(false)
const imageInputRef = ref(null)
const attachmentInputRef = ref(null)
const stageCount = ref(2)
const form = reactive({
  title: '',
  summary: '',
  detail: '',
  category: '',
  budgetMin: 1000,
  budgetMax: 3000,
  expectedDays: 7,
  deliveryType: 1,
  images: [],
  attachments: [],
  stagePlans: []
})

const stageAmountTotal = computed(() => {
  return form.stagePlans.reduce((sum, stage) => sum + Number(stage.stageAmount || 0), 0)
})

watch(
  () => form.deliveryType,
  (value) => {
    if (value === 2) {
      if (stageCount.value < 2) {
        stageCount.value = 2
      }
      syncStagePlans(stageCount.value)
      return
    }
    form.stagePlans = []
  }
)

function createStage(index) {
  return {
    stageNo: index + 1,
    stageName: `第${index + 1}阶段`,
    stageDesc: '',
    stageAmount: 0
  }
}

function syncStagePlans(count) {
  const nextCount = Math.max(2, Math.min(8, Number(count) || 2))
  const nextStages = Array.from({ length: nextCount }, (_, index) => {
    const current = form.stagePlans[index]
    return current
      ? { ...current, stageNo: index + 1, stageName: `第${index + 1}阶段` }
      : createStage(index)
  })
  form.stagePlans = nextStages
}

function handleStageCountChange(value) {
  stageCount.value = Math.max(2, Math.min(8, Number(value) || 2))
  syncStagePlans(stageCount.value)
}

function formatFileSize(size) {
  const value = Number(size || 0)
  if (value >= 1024 * 1024) {
    return `${(value / (1024 * 1024)).toFixed(2)} MB`
  }
  if (value >= 1024) {
    return `${(value / 1024).toFixed(1)} KB`
  }
  return `${value} B`
}

function formatCurrency(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function readFileAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}

async function mapFiles(files) {
  return Promise.all(
    files.map(async (file) => ({
      name: file.name,
      url: await readFileAsDataUrl(file),
      contentType: file.type,
      size: file.size
    }))
  )
}

async function handleImageSelect(event) {
  const selected = Array.from(event.target.files || [])
  event.target.value = ''
  if (!selected.length) {
    return
  }
  const slots = MAX_IMAGE_COUNT - form.images.length
  if (slots <= 0) {
    ElMessage.warning(`最多上传 ${MAX_IMAGE_COUNT} 张图片`)
    return
  }
  const validFiles = selected
    .filter((file) => {
      if (!file.type.startsWith('image/')) {
        ElMessage.warning(`"${file.name}" 不是图片文件`)
        return false
      }
      if (file.size > MAX_IMAGE_SIZE) {
        ElMessage.warning(`"${file.name}" 超过 2MB，暂不支持上传`)
        return false
      }
      return true
    })
    .slice(0, slots)
  if (!validFiles.length) {
    return
  }
  const mapped = await mapFiles(validFiles)
  form.images.push(...mapped)
}

async function handleAttachmentSelect(event) {
  const selected = Array.from(event.target.files || [])
  event.target.value = ''
  if (!selected.length) {
    return
  }
  const slots = MAX_ATTACHMENT_COUNT - form.attachments.length
  if (slots <= 0) {
    ElMessage.warning(`最多上传 ${MAX_ATTACHMENT_COUNT} 个附件`)
    return
  }
  const validFiles = selected
    .filter((file) => {
      if (file.size > MAX_ATTACHMENT_SIZE) {
        ElMessage.warning(`"${file.name}" 超过 5MB，暂不支持上传`)
        return false
      }
      return true
    })
    .slice(0, slots)
  if (!validFiles.length) {
    return
  }
  const mapped = await mapFiles(validFiles)
  form.attachments.push(...mapped)
}

function setCover(index) {
  const [selected] = form.images.splice(index, 1)
  form.images.unshift(selected)
}

function removeImage(index) {
  form.images.splice(index, 1)
}

function clearImages() {
  form.images.splice(0, form.images.length)
}

function removeAttachment(index) {
  form.attachments.splice(index, 1)
}

function clearAttachments() {
  form.attachments.splice(0, form.attachments.length)
}

function validateBeforeSubmit() {
  if (Number(form.budgetMax) < Number(form.budgetMin)) {
    ElMessage.warning('预算上限不能小于预算下限')
    return false
  }
  if (form.deliveryType === 2) {
    if (form.stagePlans.length < 2) {
      ElMessage.warning('多阶段交付至少需要两个阶段')
      return false
    }
    const invalidStage = form.stagePlans.find((stage) => !stage.stageDesc?.trim() || Number(stage.stageAmount) <= 0)
    if (invalidStage) {
      ElMessage.warning('请完整填写每个阶段的任务需求和结算金额')
      return false
    }
    if (stageAmountTotal.value < Number(form.budgetMin) || stageAmountTotal.value > Number(form.budgetMax)) {
      ElMessage.warning('阶段结算总额需要落在整体预算区间内')
      return false
    }
  }
  return true
}

async function handleSubmit() {
  if (!validateBeforeSubmit()) {
    return
  }
  submitting.value = true
  try {
    await createDemand({
      title: form.title,
      summary: form.summary,
      detail: form.detail,
      category: form.category,
      budgetMin: form.budgetMin,
      budgetMax: form.budgetMax,
      expectedDays: form.expectedDays,
      deliveryType: form.deliveryType,
      images: form.images,
      attachments: form.attachments,
      stagePlans:
        form.deliveryType === 2
          ? form.stagePlans.map((stage, index) => ({
              stageNo: index + 1,
              stageName: `第${index + 1}阶段`,
              stageDesc: stage.stageDesc.trim(),
              stageAmount: stage.stageAmount
            }))
          : []
    })
    ElMessage.success('需求发布成功')
    router.push('/client/demands')
  } catch (error) {
    ElMessage.error(error.message || '需求发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.demand-create-head p {
  margin-top: 10px;
  color: var(--text-sub);
  line-height: 1.7;
}

.section-card {
  padding: 22px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 24px;
  background: rgba(248, 250, 252, 0.75);
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.section-head strong {
  display: block;
  font-size: 18px;
}

.section-head p {
  margin-top: 8px;
  color: var(--text-sub);
  line-height: 1.7;
}

.hidden-input {
  display: none;
}

.empty-tip {
  padding: 18px;
  border: 1px dashed rgba(148, 163, 184, 0.4);
  border-radius: 18px;
  color: var(--text-sub);
  line-height: 1.7;
  background: rgba(255, 255, 255, 0.55);
}

.image-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.image-card {
  overflow: hidden;
  border-radius: 20px;
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: var(--shadow-soft);
}

.image-card img {
  display: block;
  width: 100%;
  height: 164px;
  object-fit: cover;
}

.image-card__body {
  display: grid;
  gap: 12px;
  padding: 14px;
}

.image-card__body strong,
.attachment-item strong,
.stage-card__head strong {
  display: block;
  font-size: 15px;
}

.image-card__body p,
.attachment-item p,
.stage-card__head span,
.submit-note,
.stage-summary {
  color: var(--text-sub);
  line-height: 1.6;
}

.image-card__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.cover-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.12);
  color: var(--brand-primary);
  font-size: 12px;
  font-weight: 700;
}

.attachment-list,
.stage-list {
  display: grid;
  gap: 14px;
}

.attachment-item,
.stage-card {
  padding: 16px 18px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.attachment-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.stage-count {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-sub);
  font-weight: 700;
}

.stage-summary {
  margin-bottom: 16px;
}

.stage-summary strong {
  color: var(--text-main);
}

.stage-summary span {
  margin-left: 12px;
}

.stage-card__head {
  margin-bottom: 10px;
}

.submit-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

@media (max-width: 720px) {
  .section-card {
    padding: 18px;
  }

  .submit-row {
    align-items: flex-start;
  }
}
</style>
