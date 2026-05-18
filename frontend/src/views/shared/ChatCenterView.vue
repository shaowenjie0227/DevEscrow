<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ChatDotRound, Paperclip, RefreshRight, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import TopNav from '@/components/home/TopNav.vue'
import {
  fetchConversationMessages,
  fetchMyConversations,
  recallChatMessage,
  sendChatMessage,
  uploadChatAttachment
} from '@/api/modules/chat'
import { useChatWebSocket } from '@/composables/useChatWebSocket'
import { useAuthStore } from '@/stores/auth'

const FALLBACK_REFRESH_MS = 30000
const ATTACHMENT_TTL_MS = 24 * 60 * 60 * 1000
const PIN_STORAGE_PREFIX = 'chat:pinned:'
const ALLOWED_ATTACHMENT_EXTENSIONS = ['png', 'jpg', 'jpeg', 'webp', 'gif', 'pdf', 'txt', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'zip']
const QUICK_REPLIES = [
  '你好，我先了解一下需求范围。',
  '这个预算区间我可以接受，我们继续确认交付边界。',
  '方便先确认一下核心功能和优先级吗？',
  '我先看一下附件，稍后给你更具体的报价建议。'
]

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isWorkspaceMode = computed(() => route.path.startsWith('/client/') || route.path.startsWith('/developer/'))
const chatRoutePath = computed(() => {
  if (route.path.startsWith('/client/messages')) {
    return '/client/messages'
  }
  if (route.path.startsWith('/developer/messages')) {
    return '/developer/messages'
  }
  return '/messages'
})

const loadingConversations = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const uploadingAttachment = ref(false)
const conversations = ref([])
const messages = ref([])
const draft = ref('')
const searchKeyword = ref('')
const scrollPane = ref(null)
const attachmentInput = ref(null)
const pinnedConversationKeys = ref([])

let refreshTimer = null

const queryState = computed(() => ({
  bizType: Number(route.query.bizType || 0),
  demandId: Number(route.query.demandId || 0),
  orderId: Number(route.query.orderId || 0),
  partnerId: Number(route.query.partnerId || 0),
  partnerName: typeof route.query.partnerName === 'string' ? route.query.partnerName : '',
  demandTitle: typeof route.query.demandTitle === 'string' ? route.query.demandTitle : ''
}))

const storageKey = computed(() => `${PIN_STORAGE_PREFIX}${authStore.userInfo?.userId || 'guest'}`)

const sortedConversations = computed(() => {
  return [...conversations.value].sort((left, right) => {
    const leftPinned = isConversationPinned(left)
    const rightPinned = isConversationPinned(right)
    if (leftPinned !== rightPinned) {
      return leftPinned ? -1 : 1
    }
    const leftTime = new Date(left.lastMessageAt || 0).getTime()
    const rightTime = new Date(right.lastMessageAt || 0).getTime()
    return rightTime - leftTime
  })
})

const filteredConversations = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return sortedConversations.value
  }
  return sortedConversations.value.filter((item) =>
    [item.partnerNickname, item.demandTitle, item.demandNo, item.lastMessage]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
      .includes(keyword)
  )
})

const activeConversation = computed(() => {
  const current = conversations.value.find((item) => isSameConversation(item, queryState.value))
  if (current) {
    return current
  }
  if (!queryState.value.partnerId || !queryState.value.bizType) {
    return null
  }
  return {
    bizType: queryState.value.bizType,
    demandId: queryState.value.demandId || null,
    orderId: queryState.value.orderId || null,
    partnerId: queryState.value.partnerId,
    partnerNickname: queryState.value.partnerName || '对方',
    demandTitle: queryState.value.demandTitle || '需求沟通',
    lastMessage: '',
    unreadCount: 0,
    messageCount: 0
  }
})

const totalUnread = computed(() => conversations.value.reduce((sum, item) => sum + Number(item.unreadCount || 0), 0))

const renderedMessages = computed(() => {
  const items = []
  let previousTime = 0

  for (const message of messages.value) {
    const currentTime = new Date(message.createdAt || '').getTime()
    const shouldInsertDivider =
      !previousTime ||
      Number.isNaN(currentTime) ||
      currentTime - previousTime > 15 * 60 * 1000

    if (shouldInsertDivider) {
      items.push({
        type: 'divider',
        id: `divider-${message.id}`,
        label: formatTimelineLabel(message.createdAt)
      })
    }

    items.push({
      type: 'message',
      id: `message-${message.id}`,
      payload: message
    })

    previousTime = currentTime
  }

  return items
})

const chatSocket = useChatWebSocket({
  onMessage: async (event) => {
    try {
      const payload = JSON.parse(event.data)
      if (payload?.type !== 'CHAT_MESSAGE') {
        return
      }
      await refreshConversations(false)
      if (matchesIncomingPayload(payload)) {
        await loadActiveMessages(false)
      }
    } catch (error) {
      // Ignore malformed websocket payloads.
    }
  }
})

watch(
  () => [queryState.value.bizType, queryState.value.demandId, queryState.value.orderId, queryState.value.partnerId].join(':'),
  async () => {
    await loadActiveMessages(true)
  }
)

watch(
  () => authStore.token,
  (token) => {
    if (!token) {
      chatSocket.disconnect()
      return
    }
    chatSocket.connect(token)
  }
)

watch(
  () => storageKey.value,
  () => {
    loadPinnedConversations()
  }
)

onMounted(async () => {
  loadPinnedConversations()
  await refreshConversations()
  await loadActiveMessages(true)
  if (authStore.token) {
    chatSocket.connect(authStore.token)
  }
  refreshTimer = window.setInterval(async () => {
    await refreshConversations(false)
    if (activeConversation.value) {
      await loadActiveMessages(false)
    }
  }, FALLBACK_REFRESH_MS)
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
    refreshTimer = null
  }
  chatSocket.disconnect()
})

async function refreshConversations(showToast = false) {
  loadingConversations.value = true
  try {
    const response = await fetchMyConversations()
    conversations.value = response.data || []
    if (showToast) {
      ElMessage.success('会话列表已刷新')
    }
  } catch (error) {
    ElMessage.error(error.message || '加载聊天会话失败')
  } finally {
    loadingConversations.value = false
  }
}

function loadPinnedConversations() {
  try {
    const raw = window.localStorage.getItem(storageKey.value)
    pinnedConversationKeys.value = raw ? JSON.parse(raw) : []
  } catch (error) {
    pinnedConversationKeys.value = []
  }
}

function persistPinnedConversations() {
  window.localStorage.setItem(storageKey.value, JSON.stringify(pinnedConversationKeys.value))
}

async function loadActiveMessages(scrollToBottom = false) {
  if (!queryState.value.partnerId || !queryState.value.bizType) {
    messages.value = []
    return
  }

  loadingMessages.value = true
  try {
    const response = await fetchConversationMessages({
      bizType: queryState.value.bizType,
      demandId: queryState.value.demandId || undefined,
      orderId: queryState.value.orderId || undefined,
      partnerId: queryState.value.partnerId
    })
    messages.value = response.data || []
    if (scrollToBottom) {
      await scrollMessagesToBottom()
    }
  } catch (error) {
    messages.value = []
    ElMessage.error(error.message || '加载聊天记录失败')
  } finally {
    loadingMessages.value = false
  }
}

function openConversation(conversation) {
  router.replace({
    path: chatRoutePath.value,
    query: {
      bizType: String(conversation.bizType),
      demandId: conversation.demandId ? String(conversation.demandId) : undefined,
      orderId: conversation.orderId ? String(conversation.orderId) : undefined,
      partnerId: String(conversation.partnerId),
      partnerName: conversation.partnerNickname || '',
      demandTitle: conversation.demandTitle || ''
    }
  })
}

function backToConversationList() {
  router.replace({ path: chatRoutePath.value })
}

function buildConversationStorageKey(conversation) {
  return [
    Number(conversation?.bizType || 0),
    Number(conversation?.demandId || 0),
    Number(conversation?.orderId || 0),
    Number(conversation?.partnerId || 0)
  ].join(':')
}

function isConversationPinned(conversation) {
  return pinnedConversationKeys.value.includes(buildConversationStorageKey(conversation))
}

function toggleConversationPin(conversation) {
  const key = buildConversationStorageKey(conversation)
  if (isConversationPinned(conversation)) {
    pinnedConversationKeys.value = pinnedConversationKeys.value.filter((item) => item !== key)
  } else {
    pinnedConversationKeys.value = [...pinnedConversationKeys.value, key]
  }
  persistPinnedConversations()
}

function formatConversationMeta(conversation) {
  const parts = []
  if (conversation.demandNo) {
    parts.push(conversation.demandNo)
  }
  if (conversation.messageCount) {
    parts.push(`${conversation.messageCount} 条记录`)
  }
  return parts.join(' · ')
}

function formatTimestamp(value) {
  if (!value) {
    return ''
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hours = `${date.getHours()}`.padStart(2, '0')
  const minutes = `${date.getMinutes()}`.padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

function formatTimelineLabel(value) {
  const text = formatTimestamp(value)
  return text || '刚刚'
}

function resolveBubbleName(message) {
  if (message.self) {
    return authStore.userInfo?.nickname || '我'
  }
  return message.senderNickname || activeConversation.value?.partnerNickname || '对方'
}

function resolveReadLabel(message) {
  if (!message?.self) {
    return ''
  }
  return Number(message.readStatus || 0) === 1 ? '已读' : '未读'
}

function isImageMessage(message) {
  return Number(message.msgType || 1) === 2
}

function isFileMessage(message) {
  return Number(message.msgType || 1) === 3
}

function isAttachmentExpired(message) {
  if (!message?.fileUrl || !message?.createdAt) {
    return false
  }
  const createdAt = new Date(message.createdAt).getTime()
  if (Number.isNaN(createdAt)) {
    return false
  }
  return Date.now() - createdAt >= ATTACHMENT_TTL_MS
}

function canRecallMessage(message) {
  return Boolean(message?.self) && Number(message?.status || 0) === 1
}

function triggerAttachmentPicker() {
  attachmentInput.value?.click()
}

async function handleAttachmentSelect(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || !activeConversation.value) {
    return
  }

  const extension = (file.name.split('.').pop() || '').toLowerCase()
  if (!ALLOWED_ATTACHMENT_EXTENSIONS.includes(extension)) {
    ElMessage.warning('仅支持 PNG、JPG、WEBP、GIF、PDF、TXT、DOC、DOCX、XLS、XLSX、PPT、PPTX、ZIP')
    return
  }

  uploadingAttachment.value = true
  try {
    const uploadResponse = await uploadChatAttachment(file)
    const data = uploadResponse.data || {}
    await sendChatMessage({
      bizType: Number(activeConversation.value.bizType || 1),
      demandId: activeConversation.value.demandId || undefined,
      orderId: activeConversation.value.orderId || undefined,
      receiverId: Number(activeConversation.value.partnerId),
      msgType: file.type?.startsWith('image/') ? 2 : 3,
      content: data.originalName || file.name,
      fileUrl: data.url
    })
    await refreshConversations(false)
    await loadActiveMessages(true)
    ElMessage.success('附件已发送，24 小时后会自动删除')
  } catch (error) {
    ElMessage.error(error.message || '附件发送失败')
  } finally {
    uploadingAttachment.value = false
  }
}

async function handleRecallMessage(messageId) {
  try {
    await recallChatMessage(messageId)
    await refreshConversations(false)
    await loadActiveMessages(false)
    ElMessage.success('消息已撤回')
  } catch (error) {
    ElMessage.error(error.message || '消息撤回失败')
  }
}

async function handleSend() {
  if (!activeConversation.value || !draft.value.trim()) {
    return
  }
  sending.value = true
  try {
    await sendChatMessage({
      bizType: Number(activeConversation.value.bizType || 1),
      demandId: activeConversation.value.demandId || undefined,
      orderId: activeConversation.value.orderId || undefined,
      receiverId: Number(activeConversation.value.partnerId),
      msgType: 1,
      content: draft.value.trim()
    })
    draft.value = ''
    await refreshConversations(false)
    await loadActiveMessages(true)
  } catch (error) {
    ElMessage.error(error.message || '发送消息失败')
  } finally {
    sending.value = false
  }
}

async function sendQuickReply(text) {
  draft.value = text
  await handleSend()
}

async function scrollMessagesToBottom() {
  await nextTick()
  const element = scrollPane.value
  if (!element) {
    return
  }
  element.scrollTop = element.scrollHeight
}

function isSameConversation(left, right) {
  return (
    Number(left?.bizType || 0) === Number(right?.bizType || 0) &&
    Number(left?.demandId || 0) === Number(right?.demandId || 0) &&
    Number(left?.orderId || 0) === Number(right?.orderId || 0) &&
    Number(left?.partnerId || 0) === Number(right?.partnerId || 0)
  )
}

function matchesIncomingPayload(payload) {
  if (!activeConversation.value) {
    return false
  }
  return (
    Number(payload.bizType || 0) === Number(activeConversation.value.bizType || 0) &&
    Number(payload.demandId || 0) === Number(activeConversation.value.demandId || 0) &&
    Number(payload.orderId || 0) === Number(activeConversation.value.orderId || 0) &&
    [Number(payload.senderId || 0), Number(payload.receiverId || 0)].includes(Number(activeConversation.value.partnerId || 0))
  )
}
</script>

<template>
  <div class="chat-page" :class="{ 'chat-page--workspace': isWorkspaceMode }">
    <TopNav v-if="!isWorkspaceMode" />

    <main class="chat-shell" :class="{ 'chat-shell--workspace': isWorkspaceMode }">
      <section class="chat-hero">
        <div>
          <span class="chat-hero__eyebrow">{{ isWorkspaceMode ? 'Workspace Chat' : 'Chat Module' }}</span>
          <h1>聊一聊，把范围和交付边界说透</h1>
          <p>这里会保留你和客户或开发者围绕具体需求的聊天记录。附件发送后 24 小时自动删除，避免敏感资料长期滞留。</p>
        </div>
        <div class="chat-hero__badge">
          <span>未读消息</span>
          <strong>{{ totalUnread }}</strong>
        </div>
      </section>

      <section class="chat-workbench">
        <aside class="chat-list" :class="{ 'chat-list--hidden': activeConversation }">
          <div class="chat-list__toolbar">
            <label class="chat-list__search">
              <el-icon><Search /></el-icon>
              <input v-model="searchKeyword" type="text" placeholder="搜索客户、需求或消息关键词" />
            </label>
            <button class="chat-list__refresh" type="button" @click="refreshConversations(true)">
              <el-icon><RefreshRight /></el-icon>
            </button>
          </div>

          <div class="chat-list__body" v-loading="loadingConversations">
            <div
              v-for="conversation in filteredConversations"
              :key="`${conversation.bizType}-${conversation.demandId || 0}-${conversation.orderId || 0}-${conversation.partnerId}`"
              class="chat-list__item"
              :class="{
                'chat-list__item--active': isSameConversation(conversation, queryState),
                'chat-list__item--pinned': isConversationPinned(conversation)
              }"
              role="button"
              tabindex="0"
              @click="openConversation(conversation)"
              @keydown.enter.prevent="openConversation(conversation)"
            >
              <div class="chat-list__avatar">
                <img v-if="conversation.partnerAvatarUrl" :src="conversation.partnerAvatarUrl" :alt="conversation.partnerNickname || '用户头像'" />
                <span v-else>{{ (conversation.partnerNickname || '聊').slice(0, 1) }}</span>
              </div>
              <div class="chat-list__copy">
                <div class="chat-list__head">
                  <strong>{{ conversation.partnerNickname || '对方' }}</strong>
                  <div class="chat-list__head-side">
                    <span>{{ formatTimestamp(conversation.lastMessageAt) }}</span>
                    <button class="chat-list__pin" type="button" @click.stop="toggleConversationPin(conversation)">
                      {{ isConversationPinned(conversation) ? '取消置顶' : '置顶' }}
                    </button>
                  </div>
                </div>
                <p class="chat-list__title">{{ conversation.demandTitle || '需求沟通' }}</p>
                <p class="chat-list__preview">{{ conversation.lastMessage || '还没有消息，发一条开启对话。' }}</p>
                <div class="chat-list__meta">
                  <span>{{ formatConversationMeta(conversation) }}</span>
                  <em v-if="conversation.unreadCount">{{ conversation.unreadCount }}</em>
                </div>
              </div>
            </div>

            <div v-if="!filteredConversations.length && !loadingConversations" class="chat-list__empty">
              <el-icon><ChatDotRound /></el-icon>
              <strong>还没有聊天会话</strong>
              <p>从需求详情页联系客户后，对话会出现在这里。</p>
            </div>
          </div>
        </aside>

        <section class="chat-thread">
          <div class="chat-thread__header" v-if="activeConversation">
            <button class="chat-thread__back" type="button" @click="backToConversationList">
              <el-icon><ArrowLeft /></el-icon>
            </button>
            <div class="chat-thread__title">
              <strong>{{ activeConversation.partnerNickname || '对方' }}</strong>
              <p>{{ activeConversation.demandTitle || '需求沟通' }}</p>
            </div>
            <span class="chat-thread__badge">{{ activeConversation.demandNo || '会话' }}</span>
          </div>

          <div v-if="activeConversation" ref="scrollPane" class="chat-thread__messages" v-loading="loadingMessages">
            <template v-for="item in renderedMessages" :key="item.id">
              <div v-if="item.type === 'divider'" class="chat-thread__divider">
                <span>{{ item.label }}</span>
              </div>

              <article v-else class="chat-bubble" :class="{ 'chat-bubble--self': item.payload.self }">
                <div class="chat-bubble__avatar">
                  <img v-if="item.payload.senderAvatarUrl" :src="item.payload.senderAvatarUrl" :alt="resolveBubbleName(item.payload)" />
                  <span v-else>{{ resolveBubbleName(item.payload).slice(0, 1) }}</span>
                </div>

                <div class="chat-bubble__body">
                  <div class="chat-bubble__meta">
                    <strong>{{ resolveBubbleName(item.payload) }}</strong>
                    <span>{{ formatTimestamp(item.payload.createdAt) }}</span>
                  </div>

                  <template v-if="Number(item.payload.status || 0) === 2">
                    <div class="chat-attachment chat-attachment--expired">
                      <strong>消息已撤回</strong>
                      <span>这条消息已被发送方撤回。</span>
                    </div>
                  </template>
                  <template v-else-if="isImageMessage(item.payload)">
                    <div v-if="!isAttachmentExpired(item.payload)" class="chat-attachment chat-attachment--image">
                      <img :src="item.payload.fileUrl" :alt="item.payload.content || '图片附件'" />
                      <a :href="item.payload.fileUrl" target="_blank" rel="noreferrer">{{ item.payload.content || '查看图片' }}</a>
                    </div>
                    <div v-else class="chat-attachment chat-attachment--expired">
                      <strong>{{ item.payload.content || '图片附件' }}</strong>
                      <span>附件已过期并自动删除</span>
                    </div>
                  </template>
                  <template v-else-if="isFileMessage(item.payload)">
                    <div v-if="!isAttachmentExpired(item.payload)" class="chat-attachment">
                      <strong>{{ item.payload.content || '附件' }}</strong>
                      <a :href="item.payload.fileUrl" target="_blank" rel="noreferrer">下载附件</a>
                      <span>24 小时后自动删除</span>
                    </div>
                    <div v-else class="chat-attachment chat-attachment--expired">
                      <strong>{{ item.payload.content || '附件' }}</strong>
                      <span>附件已过期并自动删除</span>
                    </div>
                  </template>
                  <p v-else>{{ item.payload.content }}</p>

                  <div v-if="item.payload.self" class="chat-bubble__footer">
                    <button
                      v-if="canRecallMessage(item.payload)"
                      class="chat-bubble__recall"
                      type="button"
                      @click="handleRecallMessage(item.payload.id)"
                    >
                      撤回
                    </button>
                    <span class="chat-bubble__status">{{ resolveReadLabel(item.payload) }}</span>
                  </div>
                </div>
              </article>
            </template>

            <div v-if="!messages.length && !loadingMessages" class="chat-thread__empty">
              <strong>还没有历史消息</strong>
              <p>可以先发一条消息，确认需求范围、交付方式或预算边界。</p>
            </div>
          </div>

          <div v-if="activeConversation" class="chat-thread__composer">
            <div class="chat-thread__composer-main">
              <div class="chat-thread__quick-replies">
                <button
                  v-for="reply in QUICK_REPLIES"
                  :key="reply"
                  class="chat-thread__quick-btn"
                  type="button"
                  @click="sendQuickReply(reply)"
                >
                  {{ reply }}
                </button>
              </div>

              <textarea
                v-model="draft"
                class="chat-thread__input"
                placeholder="输入消息，建议先确认需求范围、预算预期和交付节奏。"
                @keydown.enter.exact.prevent="handleSend"
              />
              <div class="chat-thread__hint">支持 PNG/JPG/WEBP/GIF/PDF/TXT/DOC/DOCX/XLS/XLSX/PPT/PPTX/ZIP，20MB 内；附件 24 小时后自动删除。</div>
            </div>

            <div class="chat-thread__actions">
              <input
                ref="attachmentInput"
                class="chat-thread__file-input"
                type="file"
                accept=".png,.jpg,.jpeg,.webp,.gif,.pdf,.txt,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip"
                @change="handleAttachmentSelect"
              />
              <button class="chat-thread__attach" type="button" :disabled="uploadingAttachment" @click="triggerAttachmentPicker">
                <el-icon><Paperclip /></el-icon>
                {{ uploadingAttachment ? '上传中...' : '附件' }}
              </button>
              <button class="chat-thread__send" type="button" :disabled="sending || !draft.trim()" @click="handleSend">
                {{ sending ? '发送中...' : '发送' }}
              </button>
            </div>
          </div>

          <div v-else class="chat-thread__blank">
            <el-icon><ChatDotRound /></el-icon>
            <strong>选择一个会话开始聊天</strong>
            <p>你可以从左侧继续历史对话，也可以在需求详情页联系客户后回到这里。</p>
          </div>
        </section>
      </section>
    </main>
  </div>
</template>

<style scoped>
.chat-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(251, 146, 60, 0.12), transparent 30%),
    radial-gradient(circle at top right, rgba(37, 99, 235, 0.12), transparent 34%),
    linear-gradient(180deg, #fffaf4, #f8fafc 42%, #eef2ff 100%);
}

.chat-page--workspace {
  min-height: auto;
  background: transparent;
}

.chat-shell {
  width: min(1320px, calc(100% - 40px));
  margin: 0 auto;
  padding: 24px 0 36px;
}

.chat-shell--workspace {
  width: 100%;
  padding: 0;
}

.chat-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  gap: 18px;
  align-items: stretch;
  padding: 24px 26px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 24px 50px rgba(15, 23, 42, 0.08);
}

.chat-page--workspace .chat-hero {
  padding: 22px 24px;
  border-radius: 24px;
}

.chat-hero__eyebrow {
  display: inline-flex;
  min-height: 26px;
  align-items: center;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.64);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.chat-hero h1 {
  margin: 10px 0 0;
  font-size: clamp(30px, 4vw, 46px);
  line-height: 0.96;
  letter-spacing: -0.05em;
}

.chat-hero p {
  margin: 12px 0 0;
  max-width: 60ch;
  color: rgba(17, 19, 34, 0.64);
  line-height: 1.8;
}

.chat-hero__badge {
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 8px;
  padding: 18px;
  border-radius: 24px;
  background: linear-gradient(145deg, #111322, #303650);
  color: #fff;
}

.chat-hero__badge span {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.74);
}

.chat-hero__badge strong {
  font-size: clamp(28px, 4vw, 42px);
}

.chat-workbench {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 18px;
  margin-top: 18px;
  min-height: 720px;
}

.chat-page--workspace .chat-workbench {
  margin-top: 16px;
  min-height: 680px;
}

.chat-list,
.chat-thread {
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 24px 50px rgba(15, 23, 42, 0.08);
}

.chat-page--workspace .chat-list,
.chat-page--workspace .chat-thread {
  border-radius: 24px;
}

.chat-list {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  overflow: hidden;
}

.chat-list__toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  padding: 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

.chat-list__search {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 50px;
  padding: 0 14px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.96);
  color: rgba(17, 19, 34, 0.42);
}

.chat-list__search input {
  flex: 1;
  border: 0;
  background: transparent;
  outline: 0;
  font-size: 14px;
}

.chat-list__refresh {
  width: 50px;
  border: 0;
  border-radius: 16px;
  background: rgba(17, 19, 34, 0.06);
  color: #111322;
  cursor: pointer;
}

.chat-list__body {
  overflow: auto;
  padding: 12px;
}

.chat-list__item {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 14px;
  margin: 0 0 10px;
  padding: 14px;
  border-radius: 22px;
  background: transparent;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

.chat-list__item:hover,
.chat-list__item--active {
  background: rgba(255, 247, 237, 0.94);
  transform: translateY(-1px);
}

.chat-list__item--pinned {
  box-shadow: inset 0 0 0 1px rgba(251, 146, 60, 0.18);
}

.chat-list__avatar,
.chat-bubble__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.08);
  overflow: hidden;
  color: #111322;
  font-weight: 800;
}

.chat-list__avatar img,
.chat-bubble__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.chat-list__copy {
  min-width: 0;
}

.chat-list__head,
.chat-list__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.chat-list__head strong {
  font-size: 15px;
}

.chat-list__head-side {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-list__head span,
.chat-list__meta span {
  color: rgba(17, 19, 34, 0.48);
  font-size: 12px;
}

.chat-list__pin {
  min-height: 26px;
  padding: 0 10px;
  border: 0;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: #111322;
  cursor: pointer;
  font-size: 11px;
  font-weight: 700;
}

.chat-list__title {
  margin: 4px 0 0;
  color: rgba(17, 19, 34, 0.62);
  font-size: 13px;
}

.chat-list__preview {
  margin: 8px 0;
  color: rgba(17, 19, 34, 0.72);
  font-size: 13px;
  line-height: 1.6;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.chat-list__meta em {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  background: #111322;
  color: #fff;
  font-style: normal;
  font-size: 12px;
  font-weight: 700;
}

.chat-list__empty,
.chat-thread__blank,
.chat-thread__empty {
  display: grid;
  justify-items: center;
  align-content: center;
  gap: 12px;
  min-height: 100%;
  padding: 40px 24px;
  text-align: center;
  color: rgba(17, 19, 34, 0.58);
}

.chat-list__empty strong,
.chat-thread__blank strong,
.chat-thread__empty strong {
  color: #111322;
  font-size: 20px;
}

.chat-list__empty .el-icon,
.chat-thread__blank .el-icon {
  font-size: 32px;
}

.chat-thread {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  overflow: hidden;
}

.chat-thread__header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 22px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

.chat-thread__back {
  display: none;
  width: 44px;
  height: 44px;
  border: 0;
  border-radius: 14px;
  background: rgba(17, 19, 34, 0.06);
  color: #111322;
  cursor: pointer;
}

.chat-thread__title {
  min-width: 0;
}

.chat-thread__title strong {
  display: block;
  font-size: 18px;
}

.chat-thread__title p {
  margin: 5px 0 0;
  color: rgba(17, 19, 34, 0.58);
  font-size: 13px;
}

.chat-thread__badge {
  margin-left: auto;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(251, 146, 60, 0.14);
  color: rgba(17, 19, 34, 0.72);
  font-size: 12px;
  font-weight: 700;
}

.chat-thread__messages {
  overflow: auto;
  padding: 22px;
  background:
    linear-gradient(180deg, rgba(248, 250, 252, 0.72), rgba(255, 255, 255, 0.92)),
    rgba(255, 255, 255, 0.96);
}

.chat-thread__divider {
  display: flex;
  justify-content: center;
  margin: 8px 0 18px;
}

.chat-thread__divider span {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.52);
  font-size: 12px;
  font-weight: 700;
}

.chat-bubble {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  margin-bottom: 16px;
}

.chat-bubble--self {
  grid-template-columns: minmax(0, 1fr) 52px;
}

.chat-bubble--self .chat-bubble__avatar {
  order: 2;
}

.chat-bubble__body {
  max-width: min(78%, 720px);
  padding: 14px 16px;
  border-radius: 18px 18px 18px 6px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.06);
}

.chat-bubble--self .chat-bubble__body {
  margin-left: auto;
  border-radius: 18px 18px 6px 18px;
  background: linear-gradient(145deg, #111322, #2e3550);
  color: #fff;
}

.chat-bubble__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.chat-bubble__meta span {
  color: rgba(17, 19, 34, 0.46);
  font-size: 12px;
}

.chat-bubble--self .chat-bubble__meta span {
  color: rgba(255, 255, 255, 0.7);
}

.chat-bubble__body p {
  margin: 0;
  line-height: 1.75;
  word-break: break-word;
}

.chat-bubble__footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.chat-bubble__recall,
.chat-bubble__status {
  font-size: 12px;
}

.chat-bubble__recall {
  min-height: 28px;
  padding: 0 10px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  cursor: pointer;
}

.chat-bubble__status {
  color: rgba(255, 255, 255, 0.72);
}

.chat-attachment {
  display: grid;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(17, 19, 34, 0.05);
}

.chat-bubble--self .chat-attachment {
  background: rgba(255, 255, 255, 0.14);
}

.chat-attachment strong,
.chat-attachment span {
  display: block;
}

.chat-attachment a {
  width: fit-content;
  color: #2563eb;
  font-weight: 700;
}

.chat-bubble--self .chat-attachment a {
  color: #fff;
}

.chat-attachment--image img {
  width: min(280px, 100%);
  border-radius: 14px;
  object-fit: cover;
}

.chat-attachment--expired {
  opacity: 0.72;
}

.chat-thread__composer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: end;
  padding: 18px 22px 22px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  background: rgba(255, 255, 255, 0.94);
}

.chat-thread__composer-main {
  display: grid;
  gap: 10px;
}

.chat-thread__quick-replies {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.chat-thread__quick-btn {
  min-height: 34px;
  padding: 0 12px;
  border: 0;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: #111322;
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
}

.chat-thread__input {
  min-height: 104px;
  padding: 16px 18px;
  border-radius: 20px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(248, 250, 252, 0.92);
  resize: vertical;
  outline: 0;
  font: inherit;
  line-height: 1.7;
}

.chat-thread__hint {
  color: rgba(17, 19, 34, 0.52);
  font-size: 12px;
}

.chat-thread__actions {
  display: grid;
  gap: 10px;
}

.chat-thread__file-input {
  display: none;
}

.chat-thread__attach,
.chat-thread__send {
  min-width: 116px;
  min-height: 52px;
  padding: 0 20px;
  border: 0;
  border-radius: 16px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
}

.chat-thread__attach {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: rgba(17, 19, 34, 0.08);
  color: #111322;
}

.chat-thread__send {
  background: linear-gradient(145deg, #111322, #2f3650);
  color: #fff;
}

.chat-thread__attach:disabled,
.chat-thread__send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 980px) {
  .chat-shell {
    width: min(100% - 20px, 1320px);
  }

  .chat-shell--workspace {
    width: 100%;
  }

  .chat-hero {
    grid-template-columns: 1fr;
  }

  .chat-workbench {
    grid-template-columns: 1fr;
  }

  .chat-list--hidden {
    display: none;
  }

  .chat-thread__back {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }
}

@media (max-width: 720px) {
  .chat-shell {
    padding: 16px 0 24px;
  }

  .chat-hero,
  .chat-list,
  .chat-thread {
    border-radius: 24px;
  }

  .chat-thread__composer {
    grid-template-columns: 1fr;
  }

  .chat-thread__actions {
    grid-template-columns: 1fr 1fr;
  }

  .chat-bubble__body {
    max-width: 100%;
  }
}
</style>
