<script setup>
import { computed, onMounted, ref } from 'vue'
import { ChatDotRound, RefreshRight, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fetchAdminChatConversations, fetchAdminChatMessages } from '@/api/modules/chat'

const loadingConversations = ref(false)
const loadingMessages = ref(false)
const conversations = ref([])
const messages = ref([])
const activeConversation = ref(null)
const keyword = ref('')

const filteredConversations = computed(() => {
  const normalized = keyword.value.trim().toLowerCase()
  if (!normalized) {
    return conversations.value
  }
  return conversations.value.filter((item) =>
    [
      item.demandNo,
      item.demandTitle,
      item.clientNickname,
      item.developerNickname,
      item.lastMessage
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
      .includes(normalized)
  )
})

onMounted(loadConversations)

async function loadConversations() {
  loadingConversations.value = true
  try {
    const response = await fetchAdminChatConversations()
    conversations.value = response.data || []
    if (activeConversation.value) {
      const latest = conversations.value.find((item) => isSameConversation(item, activeConversation.value))
      if (latest) {
        activeConversation.value = latest
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '加载聊天会话失败')
  } finally {
    loadingConversations.value = false
  }
}

async function openConversation(conversation) {
  activeConversation.value = conversation
  loadingMessages.value = true
  try {
    const response = await fetchAdminChatMessages({
      bizType: conversation.bizType,
      demandId: conversation.demandId || undefined,
      orderId: conversation.orderId || undefined,
      clientId: conversation.clientId,
      developerId: conversation.developerId
    })
    messages.value = response.data || []
  } catch (error) {
    messages.value = []
    ElMessage.error(error.message || '加载聊天记录失败')
  } finally {
    loadingMessages.value = false
  }
}

function isSameConversation(left, right) {
  return (
    Number(left?.bizType || 0) === Number(right?.bizType || 0) &&
    Number(left?.demandId || 0) === Number(right?.demandId || 0) &&
    Number(left?.orderId || 0) === Number(right?.orderId || 0) &&
    Number(left?.clientId || 0) === Number(right?.clientId || 0) &&
    Number(left?.developerId || 0) === Number(right?.developerId || 0)
  )
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

function formatConversationLine(conversation) {
  return `${conversation.clientNickname || '客户'} ↔ ${conversation.developerNickname || '开发者'}`
}

function isAttachmentExpired(message) {
  if (!message?.fileUrl || !message?.createdAt) {
    return false
  }
  const createdAt = new Date(message.createdAt).getTime()
  if (Number.isNaN(createdAt)) {
    return false
  }
  return Date.now() - createdAt >= 24 * 60 * 60 * 1000
}

function isRecalled(message) {
  return Number(message?.status || 0) === 2
}
</script>

<template>
  <section class="page-card chat-audit">
    <div class="toolbar chat-audit__top">
      <div>
        <h2>聊天审计</h2>
        <p>按需求和参与人回看完整沟通记录，辅助分析争议、报价过程和履约异常。</p>
      </div>
      <div class="chat-audit__actions">
        <label class="chat-audit__search">
          <el-icon><Search /></el-icon>
          <input v-model="keyword" type="text" placeholder="搜索需求、客户、开发者或消息" />
        </label>
        <el-button @click="loadConversations">
          <el-icon><RefreshRight /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="chat-audit__grid">
      <aside class="chat-audit__list" v-loading="loadingConversations">
        <button
          v-for="conversation in filteredConversations"
          :key="`${conversation.bizType}-${conversation.demandId || 0}-${conversation.orderId || 0}-${conversation.clientId}-${conversation.developerId}`"
          class="chat-audit__item"
          :class="{ 'chat-audit__item--active': isSameConversation(conversation, activeConversation) }"
          type="button"
          @click="openConversation(conversation)"
        >
          <div class="chat-audit__item-head">
            <strong>{{ conversation.demandTitle || '未命名会话' }}</strong>
            <span>{{ formatTimestamp(conversation.lastMessageAt) }}</span>
          </div>
          <p class="chat-audit__item-sub">{{ conversation.demandNo || '需求沟通' }}</p>
          <p class="chat-audit__item-participants">{{ formatConversationLine(conversation) }}</p>
          <p class="chat-audit__item-preview">{{ conversation.lastMessage || '暂无消息摘要' }}</p>
          <div class="chat-audit__item-meta">
            <span>{{ conversation.messageCount || 0 }} 条消息</span>
            <span v-if="conversation.unreadCount">{{ conversation.unreadCount }} 条未读</span>
          </div>
        </button>

        <div v-if="!filteredConversations.length && !loadingConversations" class="chat-audit__empty">
          <el-icon><ChatDotRound /></el-icon>
          <strong>暂时没有聊天会话</strong>
          <p>当前平台还没有产生可供审计的聊天记录。</p>
        </div>
      </aside>

      <section class="chat-audit__thread">
        <header class="chat-audit__thread-head" v-if="activeConversation">
          <div>
            <strong>{{ activeConversation.demandTitle || '聊天记录' }}</strong>
            <p>{{ formatConversationLine(activeConversation) }}</p>
          </div>
          <span>{{ activeConversation.demandNo || '会话' }}</span>
        </header>

        <div v-if="activeConversation" class="chat-audit__messages" v-loading="loadingMessages">
          <article v-for="message in messages" :key="message.id" class="chat-audit__message">
            <div class="chat-audit__message-meta">
              <strong>{{ message.senderNickname || '未知用户' }}</strong>
              <span>{{ formatTimestamp(message.createdAt) }}</span>
            </div>
            <div v-if="isRecalled(message)" class="chat-audit__attachment chat-audit__attachment--expired">
              <strong>消息已撤回</strong>
              <p>{{ message.content }}</p>
              <a v-if="message.fileUrl && !isAttachmentExpired(message)" :href="message.fileUrl" target="_blank" rel="noreferrer">查看原始附件</a>
            </div>
            <div v-else-if="Number(message.msgType || 1) === 1">
              <p>{{ message.content }}</p>
            </div>
            <div v-else-if="!isAttachmentExpired(message)" class="chat-audit__attachment">
              <strong>{{ message.content || '附件消息' }}</strong>
              <a :href="message.fileUrl" target="_blank" rel="noreferrer">查看附件</a>
            </div>
            <div v-else class="chat-audit__attachment chat-audit__attachment--expired">
              <strong>{{ message.content || '附件消息' }}</strong>
              <span>附件已过期并自动删除</span>
            </div>
          </article>

          <div v-if="!messages.length && !loadingMessages" class="chat-audit__blank">
            <strong>这个会话还没有记录</strong>
            <p>如果是刚创建的新会话，后续有消息后会显示在这里。</p>
          </div>
        </div>

        <div v-else class="chat-audit__blank chat-audit__blank--center">
          <el-icon><ChatDotRound /></el-icon>
          <strong>选择左侧会话查看记录</strong>
          <p>管理员可以在这里完整查看每个对话的聊天记录，用于分析纠纷和履约过程。</p>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.chat-audit {
  display: grid;
  gap: 20px;
}

.chat-audit__top {
  align-items: flex-start;
}

.chat-audit__top p {
  margin: 8px 0 0;
  color: var(--text-sub);
  line-height: 1.7;
}

.chat-audit__actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.chat-audit__search {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 320px;
  min-height: 46px;
  padding: 0 14px;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.18);
  color: var(--text-soft);
}

.chat-audit__search input {
  flex: 1;
  border: 0;
  outline: 0;
  background: transparent;
}

.chat-audit__grid {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 18px;
  min-height: 720px;
}

.chat-audit__list,
.chat-audit__thread {
  border-radius: 24px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.88);
}

.chat-audit__list {
  padding: 12px;
  overflow: auto;
}

.chat-audit__item {
  width: 100%;
  margin: 0 0 10px;
  padding: 14px 16px;
  border: 0;
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.8);
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
}

.chat-audit__item:hover,
.chat-audit__item--active {
  background: rgba(255, 247, 237, 0.94);
  transform: translateY(-1px);
}

.chat-audit__item-head,
.chat-audit__item-meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.chat-audit__item-head span,
.chat-audit__item-meta span,
.chat-audit__item-sub {
  color: var(--text-soft);
  font-size: 12px;
}

.chat-audit__item-sub,
.chat-audit__item-participants,
.chat-audit__item-preview {
  margin: 6px 0 0;
}

.chat-audit__item-participants {
  color: var(--brand-primary);
  font-size: 13px;
  font-weight: 700;
}

.chat-audit__item-preview {
  color: var(--text-sub);
  line-height: 1.65;
}

.chat-audit__empty,
.chat-audit__blank {
  display: grid;
  gap: 12px;
  color: var(--text-sub);
}

.chat-audit__empty {
  justify-items: center;
  padding: 34px 20px;
  text-align: center;
}

.chat-audit__thread {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  overflow: hidden;
}

.chat-audit__thread-head {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 18px 22px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}

.chat-audit__thread-head p {
  margin: 6px 0 0;
  color: var(--text-sub);
}

.chat-audit__thread-head span {
  display: inline-flex;
  align-items: center;
  height: fit-content;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: var(--text-main);
  font-size: 12px;
  font-weight: 700;
}

.chat-audit__messages {
  overflow: auto;
  padding: 20px 22px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.62), rgba(255, 255, 255, 0.92));
}

.chat-audit__message {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.05);
}

.chat-audit__message + .chat-audit__message {
  margin-top: 12px;
}

.chat-audit__message-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.chat-audit__message-meta span {
  color: var(--text-soft);
  font-size: 12px;
}

.chat-audit__message p {
  margin: 0;
  color: var(--text-main);
  line-height: 1.75;
}

.chat-audit__attachment {
  display: grid;
  gap: 8px;
}

.chat-audit__attachment a {
  width: fit-content;
  color: var(--brand-primary);
  font-weight: 700;
}

.chat-audit__attachment--expired {
  color: var(--text-sub);
}

.chat-audit__blank--center {
  min-height: 100%;
  justify-items: center;
  align-content: center;
  padding: 34px 20px;
  text-align: center;
}

@media (max-width: 1080px) {
  .chat-audit__grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .chat-audit__actions {
    width: 100%;
  }

  .chat-audit__search {
    min-width: 0;
    width: 100%;
  }
}
</style>
