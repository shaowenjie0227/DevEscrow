<template>
  <section class="page-card">
    <div class="toolbar demand-create-head">
      <div>
        <h2>发布需求</h2>
        <p>把目标、预算和交付拆分清楚，后续沟通会更顺畅。</p>
      </div>
    </div>

    <el-form :model="form" label-position="top" class="form-grid">
      <div class="full-width section-card ai-assistant-card">
        <div class="section-head">
          <div>
            <strong>AI 需求助手</strong>
            <p>先写下你的大致想法，再让 AI 帮你生成需求初稿、预算区间和阶段拆分。</p>
          </div>
          <div class="toolbar-actions">
            <el-button type="primary" plain :loading="aiGenerating" @click="handleGenerateAiDraft">
              {{ aiGenerating ? 'AI 正在整理...' : 'AI 生成初稿' }}
            </el-button>
          </div>
        </div>

        <div class="ai-helper-note">
          AI 会结合当前知识库和资源内容给出建议，你可以继续手动调整后再提交。
        </div>

        <div v-if="aiGenerating" class="ai-stream-status">
          <div class="ai-stream-status__head">
            <strong>{{ aiStreamStatus.stage || 'AI 正在整理需求' }}</strong>
            <span>{{ Math.max(0, Math.min(100, aiStreamStatus.progress || 0)) }}%</span>
          </div>
          <p>{{ aiStreamStatus.message || '我正在结合上下文生成一版结构化需求草稿。' }}</p>
          <div class="ai-stream-status__track">
            <span class="ai-stream-status__bar" :style="{ width: `${Math.max(8, Math.min(100, aiStreamStatus.progress || 0))}%` }"></span>
          </div>
        </div>

        <div v-if="aiDraft" class="ai-draft-panel">
          <div class="ai-draft-meta">
            <span class="ai-pill">{{ formatAiMode(aiDraft) }}</span>
            <span v-if="aiDraft.categoryName">{{ aiDraft.categoryName }}</span>
            <span>{{ aiDraft.expectedDays }} 天</span>
            <span>{{ formatCurrency(aiDraft.budgetMin) }} - {{ formatCurrency(aiDraft.budgetMax) }}</span>
          </div>

          <div v-if="aiDraft.recommendedSkills?.length" class="ai-chip-row">
            <span v-for="skill in aiDraft.recommendedSkills" :key="skill" class="ai-chip">
              {{ skill }}
            </span>
          </div>

          <ul v-if="aiDraft.riskTips?.length" class="ai-risk-list">
            <li v-for="tip in aiDraft.riskTips" :key="tip">{{ tip }}</li>
          </ul>

          <div v-if="aiDraft.references?.length" class="ai-reference-block">
            <div class="ai-reference-block__head">
              <div>
                <strong>AI 参考依据</strong>
                <p>下面这些分类、知识库和资源，是 AI 生成当前初稿时参考到的上下文。</p>
              </div>
              <el-button
                v-if="hiddenAiReferenceCount > 0"
                class="ai-reference-toggle"
                :data-label="aiReferencesExpanded ? '收起依据' : `查看更多依据（+${hiddenAiReferenceCount}）`"
                text
                @click="aiReferencesExpanded = !aiReferencesExpanded"
              >
                {{ aiReferencesExpanded ? '收起依据' : `查看更多依据（+${hiddenAiReferenceCount}）` }}
                {{ aiReferencesCollapsed ? `展开依据（${aiDraft.references.length}）` : '收起依据' }}
              </el-button>
            </div>

            <div class="ai-reference-groups">
              <section
                v-for="group in groupedAiReferences"
                :key="group.sourceType"
                class="ai-reference-group"
              >
                <div class="ai-reference-group__head">
                  <strong>{{ group.title }}</strong>
                  <span>{{ group.items.length }} 条</span>
                </div>

                <div class="ai-reference-list">
                  <article
                    v-for="ref in group.items"
                    :key="`${ref.sourceType}-${ref.sourceId}`"
                    class="ai-reference-item"
                  >
                    <div>
                      <span class="ai-reference-type">{{ formatReferenceSource(ref.sourceType) }}</span>
                      <strong>{{ ref.title }}</strong>
                      <p>{{ ref.summary }}</p>
                    </div>
                    <a v-if="ref.linkUrl" :href="ref.linkUrl" target="_blank" rel="noreferrer">查看依据</a>
                  </article>
                </div>
              </section>
            </div>
          </div>
        </div>
      </div>
      <el-form-item label="需求标题">
        <el-input v-model="form.title" placeholder="例如：企业官网改版 + 后台内容管理" />
      </el-form-item>

      <el-form-item label="需求摘要">
        <el-input v-model="form.summary" placeholder="一句话说明目标、范围和核心交付" />
      </el-form-item>

      <el-form-item label="需求分类">
        <el-select v-model="form.categoryId" filterable placeholder="请选择分类" style="width: 100%">
          <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="订单类型">
        <el-radio-group v-model="form.orderType">
          <el-radio-button :label="1">开发单</el-radio-button>
          <el-radio-button :label="2">写作单</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="是否加急">
        <el-switch v-model="form.isUrgent" active-text="加急" inactive-text="普通" />
      </el-form-item>

      <el-form-item v-if="form.isUrgent" label="加急额外奖金">
        <el-input-number v-model="form.urgentBonus" :min="0" :precision="2" style="width: 100%" />
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
          placeholder="写清楚目标用户、关键功能、技术边界、上线节奏和验收标准。"
        />
      </el-form-item>

      <div class="full-width section-card">
        <div class="section-head">
          <div>
            <strong>需求图片</strong>
            <p>最多上传 8 张图片，第一张会作为封面。</p>
          </div>
          <div class="toolbar-actions">
            <input ref="imageInputRef" class="hidden-input" type="file" accept="image/*" multiple @change="handleImageSelect" />
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
            <p>可以上传文档、原型或补充材料，方便开发者更准确理解需求。</p>
          </div>
          <div class="toolbar-actions">
            <input ref="attachmentInputRef" class="hidden-input" type="file" multiple @change="handleAttachmentSelect" />
            <el-button @click="attachmentInputRef?.click()">上传附件</el-button>
            <el-button v-if="form.attachments.length" text @click="clearAttachments">清空附件</el-button>
          </div>
        </div>

        <div v-if="form.attachments.length" class="attachment-list">
          <article v-for="(attachment, index) in form.attachments" :key="`${attachment.name}-${index}`" class="attachment-item">
            <div>
              <strong>{{ attachment.name }}</strong>
              <p>{{ attachment.contentType || '通用文件' }} · {{ formatFileSize(attachment.size) }}</p>
            </div>
            <el-button text type="danger" @click="removeAttachment(index)">移除</el-button>
          </article>
        </div>
        <div v-else class="empty-tip">还没有上传附件，补充材料越清楚，开发者报价通常越准确。</div>
      </div>

      <div v-if="form.deliveryType === 2" class="full-width section-card">
        <div class="section-head">
          <div>
            <strong>多阶段交付规划</strong>
            <p>先设置几个阶段，再分别填写每阶段任务和金额。</p>
          </div>
          <div class="stage-count">
            <span>阶段数</span>
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
            <el-form-item :label="`第 ${index + 1} 阶段任务描述`">
              <el-input v-model="stage.stageDesc" type="textarea" :rows="4" placeholder="说明这一阶段要完成什么、验收什么。" />
            </el-form-item>
            <el-form-item :label="`第 ${index + 1} 阶段结算金额`">
              <el-input-number v-model="stage.stageAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </article>
        </div>
      </div>

      <div class="full-width submit-row">
        <div class="submit-note">
          图片和附件会一起保存；如果选择多阶段交付，阶段规划也会同步进入后续协作流程。
        </div>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交需求</el-button>
      </div>
    </el-form>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  createDemand,
  fetchDemandCategories,
  resolveAiDemandDraftErrorMessage,
  streamAiDemandDraft
} from '@/api/modules/demand'
import { consumeAiDemandAssistantPrefill } from '@/utils/aiDemandAssistant'

const MAX_IMAGE_COUNT = 8
const MAX_IMAGE_SIZE = 2 * 1024 * 1024
const MAX_ATTACHMENT_COUNT = 5
const MAX_ATTACHMENT_SIZE = 5 * 1024 * 1024

const router = useRouter()
const submitting = ref(false)
const aiGenerating = ref(false)
const aiReferencesCollapsed = ref(true)
const aiReferencesExpanded = ref(false)
const imageInputRef = ref(null)
const attachmentInputRef = ref(null)
const stageCount = ref(2)
const categories = ref([])
const aiDraft = ref(null)
const aiStreamStatus = reactive({
  stage: '',
  message: '',
  progress: 0
})
const form = reactive({
  title: '',
  summary: '',
  detail: '',
  categoryId: null,
  orderType: 1,
  isUrgent: false,
  urgentBonus: 0,
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

const hiddenAiReferenceCount = computed(() => {
  const total = aiDraft.value?.references?.length || 0
  return Math.max(0, total - 3)
})

const visibleAiReferences = computed(() => {
  const references = aiDraft.value?.references || []
  return aiReferencesExpanded.value ? references : references.slice(0, 3)
})

const groupedAiReferences = computed(() => {
  const groups = [
    { sourceType: 'CATEGORY', title: '需求分类' },
    { sourceType: 'KNOWLEDGE', title: '知识库' },
    { sourceType: 'RESOURCE', title: '资源' }
  ]

  return groups
    .map((group) => ({
      ...group,
      items: visibleAiReferences.value.filter((item) => item.sourceType === group.sourceType)
    }))
    .filter((group) => group.items.length)
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

let aiDraftAbortController = null

async function loadCategories() {
  try {
    const response = await fetchDemandCategories()
    categories.value = response.data || []
    if (!form.categoryId && categories.value.length) {
      form.categoryId = categories.value[0].id
    }
    applyAssistantPrefill()
  } catch (error) {
    ElMessage.error(error.message || '加载需求分类失败')
  }
}

function createStage(index) {
  return {
    stageNo: index + 1,
    stageName: `第 ${index + 1} 阶段`,
    stageDesc: '',
    stageAmount: 0
  }
}

function syncStagePlans(count) {
  const nextCount = Math.max(2, Math.min(8, Number(count) || 2))
  const nextStages = Array.from({ length: nextCount }, (_, index) => {
    const current = form.stagePlans[index]
    return current ? { ...current, stageNo: index + 1, stageName: `第 ${index + 1} 阶段` } : createStage(index)
  })
  form.stagePlans = nextStages
}

function handleStageCountChange(value) {
  stageCount.value = Math.max(2, Math.min(8, Number(value) || 2))
  syncStagePlans(stageCount.value)
}

function formatFileSize(size) {
  const value = Number(size || 0)
  if (value >= 1024 * 1024) return `${(value / (1024 * 1024)).toFixed(2)} MB`
  if (value >= 1024) return `${(value / 1024).toFixed(1)} KB`
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
  if (!selected.length) return
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
  if (!validFiles.length) return
  const mapped = await mapFiles(validFiles)
  form.images.push(...mapped)
}

async function handleAttachmentSelect(event) {
  const selected = Array.from(event.target.files || [])
  event.target.value = ''
  if (!selected.length) return
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
  if (!validFiles.length) return
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

function buildAiRequirement() {
  return [
    form.title?.trim() ? `标题：${form.title.trim()}` : '',
    form.summary?.trim() ? `摘要：${form.summary.trim()}` : '',
    form.detail?.trim() ? `详细说明：${form.detail.trim()}` : ''
  ]
    .filter(Boolean)
    .join('\n')
}

function applyAiDraft(draft) {
  if (!draft) return

  if (draft.title) form.title = draft.title
  if (draft.summary) form.summary = draft.summary
  if (draft.detail) form.detail = draft.detail

  const matchedCategory = categories.value.find(
    (item) => item.id === draft.categoryId || item.categoryName === draft.categoryName
  )
  if (matchedCategory) {
    form.categoryId = matchedCategory.id
  }

  form.orderType = Number(draft.orderType) === 2 ? 2 : 1
  form.isUrgent = Boolean(draft.urgent)
  form.urgentBonus = Number(draft.urgentBonus || 0)
  form.budgetMin = Number(draft.budgetMin || form.budgetMin)
  form.budgetMax = Number(draft.budgetMax || form.budgetMax)
  form.expectedDays = Number(draft.expectedDays || form.expectedDays)

  if (Number(draft.deliveryType) === 2 && Array.isArray(draft.stagePlans) && draft.stagePlans.length >= 2) {
    form.deliveryType = 2
    stageCount.value = Math.min(8, Math.max(2, draft.stagePlans.length))
    form.stagePlans = draft.stagePlans.map((stage, index) => ({
      stageNo: index + 1,
      stageName: stage.stageName || `第${index + 1}阶段`,
      stageDesc: stage.stageDesc || '',
      stageAmount: Number(stage.stageAmount || 0)
    }))
  } else {
    form.deliveryType = 1
    form.stagePlans = []
  }
}

function applyAssistantPrefill() {
  const payload = consumeAiDemandAssistantPrefill()
  if (!payload?.draft) return

  aiDraft.value = payload.draft
  aiReferencesCollapsed.value = true
  aiReferencesExpanded.value = false
  applyAiDraft(payload.draft)
  ElMessage.success('已载入 AI 对话助手生成的需求草稿')
}

function resetAiStreamStatus() {
  aiStreamStatus.stage = ''
  aiStreamStatus.message = ''
  aiStreamStatus.progress = 0
}

function cancelAiDraftStream() {
  if (!aiDraftAbortController) {
    return
  }

  aiDraftAbortController.abort()
  aiDraftAbortController = null
}

function formatAiMode(draft) {
  if (draft?.fromCache) return '缓存结果'
  if (draft?.fallbackUsed) return '规则回退'
  return 'LLM 生成'
}

function formatReferenceSource(sourceType) {
  if (sourceType === 'CATEGORY') return '需求分类'
  if (sourceType === 'KNOWLEDGE') return '知识库'
  if (sourceType === 'RESOURCE') return '资源'
  return '参考'
}

async function handleGenerateAiDraft() {
  const requirement = buildAiRequirement()
  if (!requirement) {
    ElMessage.warning('请先填写标题、摘要或详细需求，再让 AI 帮你生成初稿')
    return
  }

  cancelAiDraftStream()
  const abortController = new AbortController()
  aiDraftAbortController = abortController
  aiGenerating.value = true
  aiStreamStatus.stage = '连接 AI 助手'
  aiStreamStatus.message = '我先读取你当前填写的标题、摘要和详细描述，再开始整理结构化草稿。'
  aiStreamStatus.progress = 6
  try {
    const draft = await streamAiDemandDraft(
      {
        requirement,
        title: form.title,
        summary: form.summary,
        detail: form.detail
      },
      {
        signal: abortController.signal,
        onStatus: (payload) => {
          aiStreamStatus.stage = payload?.stage || aiStreamStatus.stage || 'AI 正在整理需求'
          aiStreamStatus.message = payload?.message || aiStreamStatus.message || 'AI 正在生成结构化草稿。'
          aiStreamStatus.progress = Number(payload?.progress || aiStreamStatus.progress || 0)
        }
      }
    )

    aiDraft.value = draft || null
    aiReferencesCollapsed.value = true
    aiReferencesExpanded.value = false
    applyAiDraft(aiDraft.value)
    ElMessage.success(aiDraft.value?.fallbackUsed ? '已生成规则建议初稿，可继续微调' : 'AI 已生成需求初稿')
  } catch (error) {
    if (error?.name === 'AbortError') {
      return
    }
    ElMessage.error(resolveAiDemandDraftErrorMessage(error))
  } finally {
    if (aiDraftAbortController === abortController) {
      aiDraftAbortController = null
    }
    aiGenerating.value = false
    resetAiStreamStatus()
  }
}

function validateBeforeSubmit() {
  if (form.categoryId == null) {
    ElMessage.warning('请选择需求分类')
    return false
  }
  if (Number(form.budgetMax) < Number(form.budgetMin)) {
    ElMessage.warning('预算上限不能小于预算下限')
    return false
  }
  if (form.isUrgent && Number(form.urgentBonus) <= 0) {
    ElMessage.warning('加急时请填写额外奖金')
    return false
  }
  if (form.deliveryType === 2) {
    if (form.stagePlans.length < 2) {
      ElMessage.warning('多阶段交付至少需要两个阶段')
      return false
    }
    const invalidStage = form.stagePlans.find((stage) => !stage.stageDesc?.trim() || Number(stage.stageAmount) <= 0)
    if (invalidStage) {
      ElMessage.warning('请完整填写每个阶段的任务描述和结算金额')
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
  if (!validateBeforeSubmit()) return
  submitting.value = true
  try {
    const category = categories.value.find((item) => item.id === form.categoryId)
    await createDemand({
      title: form.title,
      summary: form.summary,
      detail: form.detail,
      categoryId: form.categoryId,
      category: category?.categoryName || '',
      orderType: form.orderType,
      isUrgent: form.isUrgent,
      urgentBonus: form.urgentBonus,
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
              stageName: `第 ${index + 1} 阶段`,
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

onMounted(loadCategories)

onBeforeUnmount(() => {
  cancelAiDraftStream()
})
</script>

<style scoped>
.ai-assistant-card {
  background: linear-gradient(135deg, rgba(20, 184, 166, 0.08), rgba(59, 130, 246, 0.08));
  border: 1px solid rgba(20, 184, 166, 0.18);
}

.ai-helper-note {
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
}

.ai-stream-status {
  display: grid;
  gap: 10px;
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(15, 118, 110, 0.14);
}

.ai-stream-status__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.ai-stream-status__head strong {
  color: #0f172a;
  font-size: 14px;
}

.ai-stream-status__head span {
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
}

.ai-stream-status p {
  margin: 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.7;
}

.ai-stream-status__track {
  overflow: hidden;
  height: 8px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.16);
}

.ai-stream-status__bar {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #0f766e, #1d4ed8);
  transition: width 0.32s ease;
}

.ai-draft-panel {
  margin-top: 16px;
  padding: 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.18);
  display: grid;
  gap: 14px;
}

.ai-draft-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #0f172a;
  font-size: 13px;
}

.ai-pill,
.ai-chip,
.ai-reference-type {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
}

.ai-pill {
  background: #0f766e;
  color: #fff;
}

.ai-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.ai-chip {
  background: rgba(15, 118, 110, 0.12);
  color: #0f766e;
}

.ai-risk-list {
  margin: 0;
  padding-left: 18px;
  color: #334155;
  line-height: 1.7;
}

.ai-reference-list {
  display: grid;
  gap: 12px;
}

.ai-reference-block {
  display: grid;
  gap: 12px;
}

.ai-reference-block__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.ai-reference-toggle {
  font-size: 0 !important;
  color: transparent !important;
  flex-shrink: 0;
}

.ai-reference-toggle::after {
  content: attr(data-label);
  color: #0f766e;
  font-size: 13px;
  font-weight: 600;
}

.ai-reference-block__head strong {
  display: block;
  color: #0f172a;
}

.ai-reference-block__head p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.ai-reference-groups {
  display: grid;
  gap: 16px;
}

.ai-reference-group {
  display: grid;
  gap: 10px;
}

.ai-reference-group__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 4px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

.ai-reference-group__head strong {
  color: #0f172a;
}

.ai-reference-group__head span {
  color: #64748b;
  font-size: 12px;
}

.ai-reference-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.ai-reference-item strong {
  display: block;
  margin: 8px 0 6px;
  color: #0f172a;
}

.ai-reference-item p {
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.ai-reference-item a {
  white-space: nowrap;
  color: #0f766e;
  font-weight: 600;
}

.ai-reference-type {
  background: rgba(59, 130, 246, 0.1);
  color: #1d4ed8;
}
</style>
