<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowRight, ChatDotRound, MagicStick, Promotion, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { resolveAiDemandDraftErrorMessage, streamAiDemandDraft } from '@/api/modules/demand'
import { useAuthStore } from '@/stores/auth'
import {
  clearAiDemandAssistantPendingPrompt,
  consumeAiDemandAssistantPendingPrompt,
  saveAiDemandAssistantPendingPrompt,
  saveAiDemandAssistantPrefill
} from '@/utils/aiDemandAssistant'
import { emitAuthExpired } from '@/utils/authEvents'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  standalone: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const quickPrompts = [
  '我想做一个企业官网，支持案例展示、文章发布和后台内容管理。',
  '我有一个小程序想做 MVP，先把核心下单、支付和订单流转跑通。',
  '现在有个老项目需要接盘续做，先帮我梳理交付范围和预算区间。'
]

const DEFAULT_THINKING_STATUS = {
  stage: '连接 AI 助手',
  message: '我先开始整理你的需求描述，并准备结构化草稿。'
}

const THINKING_STAGE_INTERVAL_MS = 1000
const TYPEWRITER_INTERVAL_MS = 16

let messageSeed = 0
let thinkingTimer = null
let typewriterTimer = null
let activeAbortController = null

const messagePane = ref(null)
const composer = ref('')
const sending = ref(false)
const draft = ref(null)
const userTurns = ref([])
const messages = ref([createWelcomeMessage()])
const activeRequestToken = ref(0)

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const topReferences = computed(() => (draft.value?.references || []).slice(0, 3))
const latestBudgetText = computed(() => formatDraftBudget(draft.value))

watch(
  () => props.modelValue,
  async (isVisible) => {
    if (!isVisible) {
      cancelActiveStream()
      stopThinkingFeedback()
      stopTypewriterEffect()
      sending.value = false
      return
    }
    await hydratePendingPrompt()
    scrollToBottom()
  },
  { immediate: true }
)

watch(
  () => authStore.token,
  async (token) => {
    if (!token || !visible.value) return
    await hydratePendingPrompt()
  }
)

watch(
  () => messages.value.length,
  () => {
    scrollToBottom()
  }
)

onBeforeUnmount(() => {
  cancelActiveStream()
  stopThinkingFeedback()
  stopTypewriterEffect()
})

function nextMessageId() {
  messageSeed += 1
  return `ai-dialog-${Date.now()}-${messageSeed}`
}

function createWelcomeMessage() {
  return {
    id: nextMessageId(),
    role: 'assistant',
    kind: 'welcome',
    text: '把一句模糊想法发给我，我会把它整理成可发布的需求草稿、预算区间和阶段拆分。'
  }
}

function createUserMessage(text) {
  return {
    id: nextMessageId(),
    role: 'user',
    kind: 'plain',
    text
  }
}

function createAssistantDraftMessage(payload) {
  return {
    id: nextMessageId(),
    role: 'assistant',
    kind: 'draft',
    text: resolveDraftLead(payload),
    draft: payload
  }
}

function createAssistantThinkingMessage() {
  return {
    id: nextMessageId(),
    role: 'assistant',
    kind: 'thinking',
    text: DEFAULT_THINKING_STATUS.message,
    stageLabel: DEFAULT_THINKING_STATUS.stage,
    elapsedLabel: '刚开始整理'
  }
}

function createAssistantErrorMessage(text) {
  return {
    id: nextMessageId(),
    role: 'assistant',
    kind: 'error',
    text
  }
}

function resolveDraftLead(payload) {
  if (!payload) {
    return '暂时还没整理出草稿，你可以再补几句目标、范围或预算倾向。'
  }
  if (payload.fallbackUsed) {
    return '我先按平台规则给你整理出一版结构化草稿，已经能拿去继续细化。'
  }
  if (payload.fromCache) {
    return '这版建议命中了最近的上下文缓存，我先把可执行草稿放在右侧。'
  }
  return '我把这段想法整理成一版可发布 brief 了，你可以继续追问，也可以直接带入正式发布页。'
}

function buildHistoryBlock(turns) {
  return turns.map((item, index) => `第 ${index + 1} 轮用户补充：${item}`).join('\n')
}

function buildExistingDraftBlock(payload) {
  if (!payload) return ''

  return [
    payload.title ? `当前标题：${payload.title}` : '',
    payload.summary ? `当前摘要：${payload.summary}` : '',
    payload.detail ? `当前详细说明：${payload.detail}` : ''
  ]
    .filter(Boolean)
    .join('\n')
}

function buildRequirement(prompt, turns) {
  return [
    turns.length ? `对话历史：\n${buildHistoryBlock(turns)}` : '',
    draft.value ? `当前结构化草稿：\n${buildExistingDraftBlock(draft.value)}` : '',
    `本轮用户输入：${prompt}`
  ]
    .filter(Boolean)
    .join('\n\n')
}

function formatCurrency(value) {
  return `¥${Number(value || 0).toFixed(0)}`
}

function formatDraftBudget(payload) {
  if (!payload) return '等待生成'
  return `${formatCurrency(payload.budgetMin)} - ${formatCurrency(payload.budgetMax)}`
}

function formatAiMode(payload) {
  if (payload?.fromCache) return '缓存结果'
  if (payload?.fallbackUsed) return '规则回退'
  return 'LLM 生成'
}

function formatReferenceSource(sourceType) {
  if (sourceType === 'CATEGORY') return '需求分类'
  if (sourceType === 'KNOWLEDGE') return '知识库'
  if (sourceType === 'RESOURCE') return '资源'
  return '参考'
}

function summarizeStage(stage, index) {
  return stage?.stageDesc || stage?.stageName || `第 ${index + 1} 阶段`
}

function findMessageById(messageId) {
  return messages.value.find((item) => item.id === messageId) || null
}

function createRequestToken() {
  const token = Date.now() + Math.random()
  activeRequestToken.value = token
  return token
}

function isCurrentRequestToken(token) {
  return activeRequestToken.value === token
}

function stopThinkingFeedback() {
  if (thinkingTimer) {
    window.clearInterval(thinkingTimer)
    thinkingTimer = null
  }
}

function cancelActiveStream() {
  if (!activeAbortController) {
    return
  }

  activeAbortController.abort()
  activeAbortController = null
}

function stopTypewriterEffect() {
  if (typewriterTimer) {
    window.clearInterval(typewriterTimer)
    typewriterTimer = null
  }
}

function startThinkingFeedback(messageId) {
  stopThinkingFeedback()

  const startedAt = Date.now()

  const syncElapsed = () => {
    const target = findMessageById(messageId)
    if (!target) {
      stopThinkingFeedback()
      return
    }

    const elapsedSeconds = Math.max(1, Math.floor((Date.now() - startedAt) / 1000))
    target.elapsedLabel = elapsedSeconds < 2 ? '刚开始整理' : `已思考 ${elapsedSeconds} 秒`
  }

  syncElapsed()
  thinkingTimer = window.setInterval(() => {
    syncElapsed()
  }, THINKING_STAGE_INTERVAL_MS)
}

function applyAssistantThinkingStatus(messageId, payload = {}) {
  const target = findMessageById(messageId)
  if (!target) {
    return
  }

  target.kind = 'thinking'
  if (payload.message) {
    target.text = payload.message
  }
  if (payload.stage) {
    target.stageLabel = payload.stage
  }
}

function animateAssistantDraft(messageId, payload) {
  stopThinkingFeedback()
  stopTypewriterEffect()

  return new Promise((resolve) => {
    const target = findMessageById(messageId)
    if (!target) {
      messages.value.push(createAssistantDraftMessage(payload))
      resolve()
      return
    }

    const finalText = resolveDraftLead(payload)
    let cursor = 0

    target.kind = 'draft'
    target.draft = payload
    target.text = ''
    target.stageLabel = ''
    target.elapsedLabel = ''

    typewriterTimer = window.setInterval(() => {
      cursor += 1
      target.text = finalText.slice(0, cursor)

      if (cursor >= finalText.length) {
        stopTypewriterEffect()
        target.text = finalText
        resolve()
      }
    }, TYPEWRITER_INTERVAL_MS)
  })
}

function replaceAssistantMessageWithError(messageId, text) {
  stopThinkingFeedback()
  stopTypewriterEffect()

  const target = findMessageById(messageId)
  if (!target) {
    messages.value.push(createAssistantErrorMessage(text))
    return
  }

  target.kind = 'error'
  target.text = text
  target.draft = null
  target.stageLabel = ''
  target.elapsedLabel = ''
}

function scrollToBottom() {
  nextTick(() => {
    if (!messagePane.value) return
    messagePane.value.scrollTop = messagePane.value.scrollHeight
  })
}

async function hydratePendingPrompt() {
  const pending = consumeAiDemandAssistantPendingPrompt()
  if (!pending?.prompt) return

  composer.value = String(pending.prompt || '').trim()
  if (!composer.value || !authStore.token || pending.autoSend === false) {
    return
  }

  await nextTick()
  await handleSend(composer.value)
}

async function handleSend(presetText = composer.value) {
  const text = String(presetText || '').trim()
  if (!text || sending.value) return

  if (!authStore.token) {
    composer.value = text
    saveAiDemandAssistantPendingPrompt({
      prompt: text,
      autoSend: true,
      savedAt: Date.now()
    })
    emitAuthExpired({
      scope: 'user',
      message: '登录后即可继续生成 AI 需求建议。',
      redirectPath: route.fullPath
    })
    return
  }

  const nextTurns = [...userTurns.value, text]
  userTurns.value = nextTurns
  messages.value.push(createUserMessage(text))
  const assistantMessage = createAssistantThinkingMessage()
  messages.value.push(assistantMessage)
  composer.value = ''
  sending.value = true
  const requestToken = createRequestToken()
  const abortController = new AbortController()
  activeAbortController = abortController
  startThinkingFeedback(assistantMessage.id)

  try {
    const streamedDraft = await streamAiDemandDraft(
      {
        requirement: buildRequirement(text, nextTurns),
        title: draft.value?.title || '',
        summary: draft.value?.summary || '',
        detail: draft.value?.detail || ''
      },
      {
        signal: abortController.signal,
        onStatus: (payload) => {
          if (!isCurrentRequestToken(requestToken)) {
            return
          }
          applyAssistantThinkingStatus(assistantMessage.id, payload)
        }
      }
    )

    if (!isCurrentRequestToken(requestToken)) {
      return
    }

    draft.value = streamedDraft || null
    await animateAssistantDraft(assistantMessage.id, draft.value)
    clearAiDemandAssistantPendingPrompt()
    ElMessage.success(draft.value?.fallbackUsed ? '已生成结构化建议草稿' : 'AI 已整理出一版需求 brief')
  } catch (error) {
    if (error?.name === 'AbortError') {
      return
    }

    if (!isCurrentRequestToken(requestToken)) {
      return
    }

    const message = resolveAiDemandDraftErrorMessage(error)
    replaceAssistantMessageWithError(assistantMessage.id, message)
    composer.value = text
    ElMessage.error(message)
  } finally {
    if (activeAbortController === abortController) {
      activeAbortController = null
    }
    if (isCurrentRequestToken(requestToken)) {
      sending.value = false
    }
  }
}

function handleQuickPrompt(prompt) {
  composer.value = prompt
  void handleSend(prompt)
}

function handleReset() {
  activeRequestToken.value = Date.now() + Math.random()
  cancelActiveStream()
  stopThinkingFeedback()
  stopTypewriterEffect()
  sending.value = false
  draft.value = null
  userTurns.value = []
  composer.value = ''
  messages.value = [createWelcomeMessage()]
  clearAiDemandAssistantPendingPrompt()
}

function handleCarryIntoPublish() {
  if (!draft.value) {
    ElMessage.warning('先生成一版 AI 草稿，再带入正式发布页')
    return
  }

  saveAiDemandAssistantPrefill({
    draft: draft.value,
    userTurns: userTurns.value,
    savedAt: Date.now()
  })

  if (!authStore.token) {
    emitAuthExpired({
      scope: 'user',
      message: '登录后即可把 AI 草稿带入正式发布页。',
      redirectPath: '/client/demands/create'
    })
    return
  }

  router.push('/client/demands/create')
  ElMessage.success('已把 AI 草稿带入正式发布页')
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :width="standalone ? 'min(1320px, calc(100vw - 24px))' : '1320px'"
    :append-to-body="true"
    :destroy-on-close="false"
    class="ai-demand-assistant-dialog"
  >
    <div class="ai-demand-assistant">
      <section class="ai-demand-assistant__chat">
        <div class="ai-demand-assistant__hero">
          <div>
            <span class="ai-demand-assistant__eyebrow">AI Demand Concierge</span>
            <h2>需求发我，整理成能发布的 brief。</h2>
            <p>目标、范围、预算模糊也没关系。你先说业务意图，我来帮你拆成交付和报价所需的信息。</p>
          </div>

          <div class="ai-demand-assistant__hero-badges">
            <span>对话式梳理</span>
            <span>预算区间建议</span>
            <span>交付阶段拆分</span>
          </div>
        </div>

        <div ref="messagePane" class="ai-demand-assistant__messages">
          <article
            v-for="message in messages"
            :key="message.id"
            class="ai-demand-assistant__message"
            :class="{
              'ai-demand-assistant__message--user': message.role === 'user',
              'ai-demand-assistant__message--error': message.kind === 'error'
            }"
          >
            <span class="ai-demand-assistant__avatar">{{ message.role === 'user' ? '你' : 'AI' }}</span>

            <div class="ai-demand-assistant__bubble">
              <p class="ai-demand-assistant__bubble-text">{{ message.text }}</p>

              <div v-if="message.kind === 'thinking'" class="ai-demand-assistant__thinking-meta">
                <span>{{ message.stageLabel }}</span>
                <em>{{ message.elapsedLabel }}</em>
              </div>

              <template v-if="message.kind === 'draft' && message.draft">
                <div class="ai-demand-assistant__bubble-meta">
                  <span>{{ formatAiMode(message.draft) }}</span>
                  <span>{{ message.draft.categoryName || '待匹配分类' }}</span>
                  <span>{{ formatDraftBudget(message.draft) }}</span>
                  <span>{{ message.draft.expectedDays || '-' }} 天</span>
                </div>

                <div v-if="message.draft.recommendedSkills?.length" class="ai-demand-assistant__chips">
                  <span v-for="skill in message.draft.recommendedSkills" :key="skill">{{ skill }}</span>
                </div>

                <ol
                  v-if="message.draft.stagePlans?.length"
                  class="ai-demand-assistant__stage-preview"
                >
                  <li v-for="(stage, index) in message.draft.stagePlans.slice(0, 3)" :key="`${stage.stageName}-${index}`">
                    {{ summarizeStage(stage, index) }}
                  </li>
                </ol>
              </template>
            </div>
          </article>
        </div>

        <div class="ai-demand-assistant__quick-row">
          <button
            v-for="prompt in quickPrompts"
            :key="prompt"
            class="ai-demand-assistant__quick-btn"
            type="button"
            :disabled="sending"
            @click="handleQuickPrompt(prompt)"
          >
            {{ prompt }}
          </button>
        </div>

        <div class="ai-demand-assistant__composer">
          <div class="ai-demand-assistant__composer-box">
            <textarea
              v-model="composer"
              class="ai-demand-assistant__textarea"
              placeholder="例如：想做一个企业官网 + 后台管理，重点是内容维护方便、上线快，预算先控制在 1 万以内。"
            />
            <div class="ai-demand-assistant__composer-hint">
              <span>支持多轮追问。你可以继续补充“预算压低一点”“改成两阶段交付”“开发周期控制在 10 天内”。</span>
              <span>未登录时会先引导登录，再继续本次对话。</span>
            </div>
          </div>

          <div class="ai-demand-assistant__composer-actions">
            <button class="ai-demand-assistant__ghost-btn" type="button" @click="handleReset">
              <el-icon><RefreshRight /></el-icon>
              重新开始
            </button>

            <button class="ai-demand-assistant__send-btn" type="button" :disabled="sending || !composer.trim()" @click="handleSend()">
              <el-icon><Promotion /></el-icon>
              {{ sending ? 'AI 整理中' : '发送给 AI' }}
            </button>
          </div>
        </div>
      </section>

      <aside class="ai-demand-assistant__brief">
        <div class="ai-demand-assistant__brief-card ai-demand-assistant__brief-card--accent">
          <span class="ai-demand-assistant__brief-kicker">Ready To Publish</span>
          <template v-if="draft">
            <h3>{{ draft.title || 'AI 正在为你整理标题' }}</h3>
            <p>{{ draft.summary || '生成后这里会显示适合公开展示的一句话摘要。' }}</p>

            <div class="ai-demand-assistant__brief-stats">
              <article>
                <strong>{{ latestBudgetText }}</strong>
                <span>预算建议</span>
              </article>
              <article>
                <strong>{{ draft.expectedDays || '-' }} 天</strong>
                <span>预计工期</span>
              </article>
              <article>
                <strong>{{ draft.categoryName || '待匹配' }}</strong>
                <span>推荐分类</span>
              </article>
            </div>

            <div v-if="draft.detail" class="ai-demand-assistant__brief-block">
              <strong>详细说明</strong>
              <p>{{ draft.detail }}</p>
            </div>

            <div v-if="draft.stagePlans?.length" class="ai-demand-assistant__brief-block">
              <strong>阶段建议</strong>
              <div class="ai-demand-assistant__stage-list">
                <article v-for="(stage, index) in draft.stagePlans" :key="`${stage.stageName}-${index}`">
                  <span>{{ String(index + 1).padStart(2, '0') }}</span>
                  <div>
                    <h4>{{ stage.stageName || `第 ${index + 1} 阶段` }}</h4>
                    <p>{{ summarizeStage(stage, index) }}</p>
                    <em>{{ formatCurrency(stage.stageAmount) }}</em>
                  </div>
                </article>
              </div>
            </div>

            <div v-if="draft.riskTips?.length" class="ai-demand-assistant__brief-block">
              <strong>风险提醒</strong>
              <ul class="ai-demand-assistant__risk-list">
                <li v-for="tip in draft.riskTips" :key="tip">{{ tip }}</li>
              </ul>
            </div>

            <div v-if="topReferences.length" class="ai-demand-assistant__brief-block">
              <strong>参考依据</strong>
              <div class="ai-demand-assistant__reference-list">
                <article v-for="item in topReferences" :key="`${item.sourceType}-${item.sourceId}`">
                  <span>{{ formatReferenceSource(item.sourceType) }}</span>
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.summary }}</p>
                </article>
              </div>
            </div>
          </template>

          <template v-else>
            <h3>右侧会沉淀成一版可发布草稿。</h3>
            <p>先从一句业务目标开始，AI 会把它整理成更适合开发者报价和协作的结构化需求。</p>

            <div class="ai-demand-assistant__empty-steps">
              <article>
                <div>
                  <strong>一键带入正式发布页</strong>
                  <p>草稿会直接进入原有发布流程，后面你还能继续微调图片、附件和阶段金额。</p>
                </div>
              </article>
            </div>
          </template>
        </div>

        <div class="ai-demand-assistant__brief-card">
          <button class="ai-demand-assistant__carry-btn" type="button" @click="handleCarryIntoPublish">
            带入正式发布页
          </button>
        </div>
      </aside>
    </div>
  </el-dialog>
</template>

<style>
.ai-demand-assistant-dialog.el-dialog {
  width: min(1320px, calc(100vw - 24px));
  margin-top: 4vh !important;
  overflow: hidden;
  border-radius: 30px;
  background:
    radial-gradient(circle at top left, rgba(243, 190, 37, 0.18), transparent 30%),
    linear-gradient(180deg, rgba(255, 249, 238, 0.98), rgba(255, 255, 255, 0.98));
}

.ai-demand-assistant-dialog .el-dialog__header {
  padding: 0;
}

.ai-demand-assistant-dialog .el-dialog__headerbtn {
  top: 22px;
  right: 22px;
  width: 36px;
  height: 36px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
}

.ai-demand-assistant-dialog .el-dialog__body {
  padding: 0;
}

.ai-demand-assistant {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(340px, 0.9fr);
  min-height: min(780px, calc(100vh - 110px));
}

.ai-demand-assistant__chat {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto auto;
  gap: 18px;
  padding: 28px;
  border-right: 1px solid rgba(17, 19, 34, 0.08);
}

.ai-demand-assistant__hero {
  display: grid;
  gap: 16px;
  padding: 22px 24px;
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(17, 19, 34, 0.98), rgba(31, 41, 55, 0.96)),
    linear-gradient(90deg, rgba(243, 190, 37, 0.2), transparent);
  color: #f8fafc;
}

.ai-demand-assistant__eyebrow,
.ai-demand-assistant__brief-kicker {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.ai-demand-assistant__eyebrow {
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.76);
}

.ai-demand-assistant__hero h2,
.ai-demand-assistant__brief-card h3 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(30px, 3vw, 44px);
  line-height: 0.98;
}

.ai-demand-assistant__hero p {
  margin: 0;
  max-width: 58ch;
  color: rgba(248, 250, 252, 0.78);
  line-height: 1.8;
}

.ai-demand-assistant__hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.ai-demand-assistant__hero-badges span {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #fff6d4;
  font-size: 13px;
}

.ai-demand-assistant__messages {
  display: grid;
  align-content: start;
  gap: 16px;
  min-height: 0;
  padding-right: 8px;
  overflow-y: auto;
}

.ai-demand-assistant__message {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.ai-demand-assistant__message--user {
  grid-template-columns: minmax(0, 1fr) 40px;
}

.ai-demand-assistant__message--user .ai-demand-assistant__avatar {
  order: 2;
  background: #111322;
  color: #fff;
}

.ai-demand-assistant__message--user .ai-demand-assistant__bubble {
  order: 1;
  margin-left: auto;
  background: linear-gradient(135deg, rgba(243, 190, 37, 0.24), rgba(255, 249, 238, 0.96));
}

.ai-demand-assistant__message--error .ai-demand-assistant__bubble {
  border-color: rgba(220, 38, 38, 0.18);
  background: rgba(254, 242, 242, 0.96);
}

.ai-demand-assistant__avatar {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 16px;
  background: rgba(17, 19, 34, 0.08);
  color: #111322;
  font-size: 13px;
  font-weight: 700;
}

.ai-demand-assistant__bubble {
  max-width: 100%;
  padding: 16px 18px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 18px 40px rgba(17, 19, 34, 0.06);
}

.ai-demand-assistant__bubble-text {
  margin: 0;
  color: #111322;
  line-height: 1.8;
  white-space: pre-wrap;
}

.ai-demand-assistant__bubble-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.ai-demand-assistant__thinking-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
  margin-top: 14px;
}

.ai-demand-assistant__thinking-meta span,
.ai-demand-assistant__thinking-meta em {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
}

.ai-demand-assistant__thinking-meta span {
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.78);
  font-weight: 700;
}

.ai-demand-assistant__thinking-meta em {
  color: rgba(17, 19, 34, 0.54);
  font-style: normal;
  background: rgba(243, 190, 37, 0.12);
}

.ai-demand-assistant__bubble-meta span,
.ai-demand-assistant__chips span {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.ai-demand-assistant__bubble-meta span {
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.72);
}

.ai-demand-assistant__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.ai-demand-assistant__chips span {
  background: rgba(243, 190, 37, 0.16);
  color: #8a5b00;
}

.ai-demand-assistant__stage-preview {
  margin: 14px 0 0;
  padding-left: 18px;
  color: rgba(17, 19, 34, 0.74);
  line-height: 1.7;
}

.ai-demand-assistant__bubble--loading {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.ai-demand-assistant__bubble--loading span {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.28);
  animation: ai-demand-dot 1.2s ease-in-out infinite;
}

.ai-demand-assistant__bubble--loading span:nth-child(2) {
  animation-delay: 0.15s;
}

.ai-demand-assistant__bubble--loading span:nth-child(3) {
  animation-delay: 0.3s;
}

.ai-demand-assistant__quick-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.ai-demand-assistant__quick-btn,
.ai-demand-assistant__ghost-btn,
.ai-demand-assistant__send-btn,
.ai-demand-assistant__carry-btn {
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.ai-demand-assistant__quick-btn {
  min-height: 42px;
  padding: 0 14px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  color: rgba(17, 19, 34, 0.74);
  font-size: 13px;
  text-align: left;
}

.ai-demand-assistant__quick-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.ai-demand-assistant__quick-btn:hover,
.ai-demand-assistant__ghost-btn:hover,
.ai-demand-assistant__send-btn:hover,
.ai-demand-assistant__carry-btn:hover {
  transform: translateY(-1px);
}

.ai-demand-assistant__composer {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(17, 19, 34, 0.08);
}

.ai-demand-assistant__composer-box {
  display: grid;
  gap: 10px;
}

.ai-demand-assistant__textarea {
  width: 100%;
  min-height: 120px;
  padding: 16px 18px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 20px;
  background: #fff;
  color: #111322;
  font: inherit;
  line-height: 1.8;
  resize: vertical;
}

.ai-demand-assistant__composer-hint {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 18px;
  color: rgba(17, 19, 34, 0.54);
  font-size: 12px;
  line-height: 1.7;
}

.ai-demand-assistant__composer-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.ai-demand-assistant__ghost-btn,
.ai-demand-assistant__send-btn,
.ai-demand-assistant__carry-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 48px;
  padding: 0 18px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 700;
}

.ai-demand-assistant__ghost-btn {
  border: 1px solid rgba(17, 19, 34, 0.08);
  background: rgba(255, 255, 255, 0.9);
  color: rgba(17, 19, 34, 0.78);
}

.ai-demand-assistant__send-btn,
.ai-demand-assistant__carry-btn {
  border: none;
  background: #111322;
  color: #fff7dc;
  box-shadow: 0 18px 32px rgba(17, 19, 34, 0.16);
}

.ai-demand-assistant__send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.ai-demand-assistant__brief {
  display: grid;
  align-content: start;
  gap: 18px;
  padding: 28px;
  overflow-y: auto;
}

.ai-demand-assistant__brief-card {
  display: grid;
  gap: 18px;
  padding: 24px;
  border-radius: 26px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 38px rgba(17, 19, 34, 0.06);
}

.ai-demand-assistant__brief-card--accent {
  background:
    radial-gradient(circle at top right, rgba(243, 190, 37, 0.18), transparent 32%),
    rgba(255, 255, 255, 0.9);
}

.ai-demand-assistant__brief-kicker {
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.64);
}

.ai-demand-assistant__brief-card p {
  margin: 0;
  color: rgba(17, 19, 34, 0.68);
  line-height: 1.8;
}

.ai-demand-assistant__brief-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.ai-demand-assistant__brief-stats article,
.ai-demand-assistant__empty-steps article {
  padding: 16px;
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.04);
}

.ai-demand-assistant__brief-stats strong {
  display: block;
  color: #111322;
  font-size: 16px;
}

.ai-demand-assistant__brief-stats span {
  display: block;
  margin-top: 8px;
  color: rgba(17, 19, 34, 0.54);
  font-size: 12px;
}

.ai-demand-assistant__brief-block {
  display: grid;
  gap: 12px;
}

.ai-demand-assistant__brief-block strong,
.ai-demand-assistant__brief-head h3,
.ai-demand-assistant__empty-steps strong,
.ai-demand-assistant__flow strong,
.ai-demand-assistant__stage-list h4,
.ai-demand-assistant__reference-list h4 {
  color: #111322;
}

.ai-demand-assistant__stage-list,
.ai-demand-assistant__reference-list,
.ai-demand-assistant__flow,
.ai-demand-assistant__empty-steps {
  display: grid;
  gap: 12px;
}

.ai-demand-assistant__stage-list article,
.ai-demand-assistant__reference-list article,
.ai-demand-assistant__flow article,
.ai-demand-assistant__empty-steps article {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.ai-demand-assistant__stage-list article {
  padding: 14px;
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.04);
}

.ai-demand-assistant__stage-list span,
.ai-demand-assistant__empty-steps span {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 14px;
  background: #111322;
  color: #fff7dc;
  font-size: 12px;
  font-weight: 700;
}

.ai-demand-assistant__stage-list h4,
.ai-demand-assistant__reference-list h4 {
  margin: 0 0 6px;
}

.ai-demand-assistant__stage-list em {
  display: inline-block;
  margin-top: 8px;
  color: #8a5b00;
  font-style: normal;
  font-weight: 700;
}

.ai-demand-assistant__risk-list {
  margin: 0;
  padding-left: 18px;
  color: rgba(17, 19, 34, 0.72);
  line-height: 1.8;
}

.ai-demand-assistant__reference-list article {
  padding: 14px;
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.04);
}

.ai-demand-assistant__reference-list span {
  display: inline-flex;
  width: fit-content;
  min-height: 28px;
  align-items: center;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(243, 190, 37, 0.18);
  color: #8a5b00;
  font-size: 12px;
  font-weight: 700;
}

.ai-demand-assistant__brief-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.ai-demand-assistant__flow article .el-icon {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 14px;
  background: rgba(17, 19, 34, 0.08);
  color: #111322;
}

.ai-demand-assistant__carry-btn {
  width: 100%;
}

@keyframes ai-demand-dot {
  0%,
  80%,
  100% {
    transform: scale(0.8);
    opacity: 0.5;
  }

  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@media (max-width: 1120px) {
  .ai-demand-assistant {
    grid-template-columns: 1fr;
  }

  .ai-demand-assistant__chat {
    border-right: none;
    border-bottom: 1px solid rgba(17, 19, 34, 0.08);
  }

  .ai-demand-assistant__brief {
    max-height: none;
  }
}

@media (max-width: 720px) {
  .ai-demand-assistant-dialog.el-dialog {
    width: calc(100vw - 12px);
    margin-top: 1.5vh !important;
    border-radius: 24px;
  }

  .ai-demand-assistant__chat,
  .ai-demand-assistant__brief {
    padding: 18px;
  }

  .ai-demand-assistant__hero,
  .ai-demand-assistant__composer,
  .ai-demand-assistant__brief-card {
    border-radius: 20px;
  }

  .ai-demand-assistant__hero h2,
  .ai-demand-assistant__brief-card h3 {
    font-size: 28px;
  }

  .ai-demand-assistant__brief-stats {
    grid-template-columns: 1fr;
  }

  .ai-demand-assistant__composer-actions {
    flex-direction: column;
  }

  .ai-demand-assistant__ghost-btn,
  .ai-demand-assistant__send-btn,
  .ai-demand-assistant__carry-btn {
    width: 100%;
  }

  .ai-demand-assistant__message,
  .ai-demand-assistant__message--user,
  .ai-demand-assistant__stage-list article,
  .ai-demand-assistant__reference-list article,
  .ai-demand-assistant__flow article,
  .ai-demand-assistant__empty-steps article {
    grid-template-columns: 1fr;
  }

  .ai-demand-assistant__message--user .ai-demand-assistant__avatar,
  .ai-demand-assistant__avatar {
    order: 0;
  }

  .ai-demand-assistant__message--user .ai-demand-assistant__bubble {
    order: 0;
  }
}
</style>
