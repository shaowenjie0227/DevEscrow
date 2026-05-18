<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Check, MessageBox, RefreshRight, Right } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  fetchInboxMessages,
  fetchInboxUnreadSummary,
  markAllInboxMessagesRead,
  markInboxMessageRead
} from '@/api/modules/inbox'
import { useInboxUnreadCount } from '@/composables/useInboxUnreadCount'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const loading = ref(false)
const actionLoading = ref(false)
const messages = ref([])
const summary = ref({
  unreadCount: 0,
  latestTitle: '',
  latestCreatedAt: ''
})
const activeMessageId = ref(null)

const { unreadCount, refreshUnreadCount } = useInboxUnreadCount(authStore)

const activeMessage = computed(() => {
  if (!messages.value.length || !activeMessageId.value) {
    return null
  }
  return messages.value.find((item) => item.id === activeMessageId.value) || null
})

const latestTip = computed(() => {
  if (!summary.value.latestTitle) {
    return '管理员发送的单独通知、提醒和补充说明，会统一收进这只信箱。'
  }
  return `最新未读：${summary.value.latestTitle}`
})

watch(
  unreadCount,
  (count) => {
    summary.value = {
      ...summary.value,
      unreadCount: Number(count || 0)
    }
  },
  { immediate: true }
)

onMounted(async () => {
  await Promise.all([loadMessages(), loadSummary()])
})

async function loadMessages() {
  loading.value = true
  try {
    const response = await fetchInboxMessages()
    messages.value = response.data || []
    if (!messages.value.length) {
      activeMessageId.value = null
      return
    }
    if (activeMessageId.value && !messages.value.some((item) => item.id === activeMessageId.value)) {
      activeMessageId.value = null
    }
  } catch (error) {
    ElMessage.error(error.message || '加载站内信失败')
  } finally {
    loading.value = false
  }
}

async function loadSummary() {
  try {
    const response = await fetchInboxUnreadSummary()
    summary.value = {
      unreadCount: Number(response.data?.unreadCount || 0),
      latestTitle: response.data?.latestTitle || '',
      latestCreatedAt: response.data?.latestCreatedAt || ''
    }
  } catch (error) {
    summary.value = {
      unreadCount: Number(unreadCount.value || 0),
      latestTitle: '',
      latestCreatedAt: ''
    }
  }
}

async function openMessage(message) {
  activeMessageId.value = message.id
  if (Number(message.readStatus || 0) !== 0) {
    return
  }
  try {
    const response = await markInboxMessageRead(message.id)
    messages.value = messages.value.map((item) => (item.id === message.id ? { ...item, ...(response.data || message) } : item))
    await Promise.all([loadSummary(), refreshUnreadCount()])
  } catch (error) {
    ElMessage.error(error.message || '标记已读失败')
  }
}

async function handleReadAll() {
  if (!summary.value.unreadCount) {
    return
  }
  actionLoading.value = true
  try {
    await markAllInboxMessagesRead()
    messages.value = messages.value.map((item) => ({
      ...item,
      readStatus: 1,
      readAt: item.readAt || new Date().toISOString()
    }))
    await Promise.all([loadSummary(), refreshUnreadCount()])
    ElMessage.success('全部站内信已标记为已读')
  } catch (error) {
    ElMessage.error(error.message || '批量标记失败')
  } finally {
    actionLoading.value = false
  }
}

function hasMessageAction(message) {
  return Boolean(message?.actionUrl)
}

async function viewMessageContext(message) {
  if (!hasMessageAction(message)) {
    return
  }
  await openMessage(message)
  await router.push(message.actionUrl)
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
</script>

<template>
  <section class="inbox-view">
    <article class="page-card inbox-hero">
      <div class="inbox-hero__copy">
        <span class="inbox-hero__eyebrow">Mailbox</span>
        <h1>管理员单独发给你的站内信，会在这里集中收纳。</h1>
        <p>{{ latestTip }}</p>
      </div>

      <div class="inbox-hero__panel">
        <div class="inbox-hero__count">
          <span>未读站内信</span>
          <strong>{{ summary.unreadCount }}</strong>
        </div>
        <button class="inbox-hero__action" type="button" :disabled="actionLoading || !summary.unreadCount" @click="handleReadAll">
          <el-icon><Check /></el-icon>
          {{ actionLoading ? '处理中...' : '全部设为已读' }}
        </button>
      </div>
    </article>

    <section class="inbox-board page-card" v-loading="loading">
      <aside class="inbox-list">
        <div class="inbox-list__toolbar">
          <div>
            <strong>信件列表</strong>
            <p>{{ messages.length ? `共 ${messages.length} 封消息` : '暂时还没有站内信' }}</p>
          </div>
          <button class="inbox-list__refresh" type="button" @click="Promise.all([loadMessages(), loadSummary(), refreshUnreadCount()])">
            <el-icon><RefreshRight /></el-icon>
          </button>
        </div>

        <div v-if="messages.length" class="inbox-list__body">
          <button
            v-for="message in messages"
            :key="message.id"
            class="inbox-item"
            :class="{
              'inbox-item--active': activeMessage?.id === message.id,
              'inbox-item--unread': Number(message.readStatus || 0) === 0
            }"
            type="button"
            @click="openMessage(message)"
          >
            <span class="inbox-item__dot" />
            <div class="inbox-item__copy">
              <div class="inbox-item__head">
                <strong>{{ message.title }}</strong>
                <span>{{ formatTimestamp(message.createdAt) }}</span>
              </div>
              <p>{{ message.content }}</p>
              <span v-if="hasMessageAction(message)" class="inbox-item__jump">可跳转</span>
              <small>{{ message.senderName || '平台管理员' }}</small>
            </div>
          </button>
        </div>

        <div v-else class="inbox-empty">
          <el-icon><Bell /></el-icon>
          <strong>信箱还是空的</strong>
          <p>管理员发来的单独通知、补充说明或提醒，会优先显示在这里。</p>
        </div>
      </aside>

      <article class="inbox-detail">
        <template v-if="activeMessage">
          <div class="inbox-detail__meta">
            <span class="inbox-detail__tag" :class="{ 'is-unread': Number(activeMessage.readStatus || 0) === 0 }">
              {{ Number(activeMessage.readStatus || 0) === 0 ? '未读' : '已读' }}
            </span>
            <span>{{ formatTimestamp(activeMessage.createdAt) }}</span>
          </div>
          <h2>{{ activeMessage.title }}</h2>
          <div class="inbox-detail__sender">
            <el-icon><MessageBox /></el-icon>
            <span>{{ activeMessage.senderName || '平台管理员' }}</span>
          </div>
          <div class="inbox-detail__body">{{ activeMessage.content }}</div>
          <button
            v-if="hasMessageAction(activeMessage)"
            class="inbox-detail__action"
            type="button"
            @click="viewMessageContext(activeMessage)"
          >
            <span>前往查看关联内容</span>
            <el-icon><Right /></el-icon>
          </button>
        </template>

        <div v-else class="inbox-empty inbox-empty--detail">
          <el-icon><MessageBox /></el-icon>
          <strong>选择一封站内信查看内容</strong>
          <p>左侧未读消息会优先排在前面，打开后会自动标记为已读。</p>
        </div>
      </article>
    </section>
  </section>
</template>

<style scoped>
.inbox-view {
  display: grid;
  gap: 20px;
}

.inbox-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(260px, 0.7fr);
  gap: 20px;
  background:
    radial-gradient(circle at top right, rgba(249, 115, 22, 0.12), transparent 28%),
    linear-gradient(135deg, rgba(255, 249, 240, 0.98), rgba(255, 255, 255, 0.94));
}

.inbox-hero__eyebrow {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: #8a4d10;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.inbox-hero h1 {
  margin: 14px 0 0;
  font-size: clamp(28px, 4vw, 42px);
  line-height: 1.04;
  letter-spacing: -0.04em;
  color: #111827;
}

.inbox-hero p {
  margin: 14px 0 0;
  color: #475569;
  line-height: 1.8;
}

.inbox-hero__panel {
  display: grid;
  gap: 14px;
  align-content: start;
}

.inbox-hero__count {
  padding: 18px 20px;
  border-radius: 22px;
  background: linear-gradient(145deg, #111827, #2b3447);
  color: #fff;
}

.inbox-hero__count span {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

.inbox-hero__count strong {
  display: block;
  margin-top: 10px;
  font-size: clamp(32px, 4vw, 46px);
  line-height: 1;
}

.inbox-hero__action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 56px;
  border: 0;
  border-radius: 22px;
  background: rgba(255, 244, 230, 0.92);
  color: #9a5311;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.inbox-hero__action:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.inbox-board {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 18px;
  min-height: 620px;
}

.inbox-list,
.inbox-detail {
  min-width: 0;
  border-radius: 24px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background: rgba(255, 255, 255, 0.82);
}

.inbox-list {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  overflow: hidden;
}

.inbox-list__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
}

.inbox-list__toolbar p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.inbox-list__refresh {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border: 0;
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.06);
  color: #0f172a;
  cursor: pointer;
}

.inbox-list__body {
  overflow: auto;
  padding: 12px;
}

.inbox-item {
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr);
  gap: 10px;
  width: 100%;
  margin: 0 0 10px;
  padding: 14px 14px 14px 12px;
  border: 1px solid transparent;
  border-radius: 20px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.inbox-item:hover,
.inbox-item--active {
  background: rgba(255, 248, 238, 0.96);
  border-color: rgba(249, 115, 22, 0.12);
}

.inbox-item__dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  margin-top: 7px;
  background: transparent;
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.34);
}

.inbox-item--unread .inbox-item__dot {
  background: #f97316;
  box-shadow: 0 0 0 6px rgba(249, 115, 22, 0.12);
}

.inbox-item__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.inbox-item__head strong {
  color: #0f172a;
  font-size: 15px;
}

.inbox-item__head span,
.inbox-item small {
  color: #64748b;
  font-size: 12px;
}

.inbox-item p {
  margin: 8px 0;
  color: #475569;
  line-height: 1.7;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.inbox-item__jump {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  margin-bottom: 8px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(36, 87, 214, 0.08);
  color: #2457d6;
  font-size: 12px;
  font-weight: 700;
}

.inbox-detail {
  padding: 28px;
}

.inbox-detail__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #64748b;
  font-size: 13px;
}

.inbox-detail__tag {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: #475569;
  font-weight: 700;
}

.inbox-detail__tag.is-unread {
  background: rgba(249, 115, 22, 0.14);
  color: #c2410c;
}

.inbox-detail h2 {
  margin: 18px 0 0;
  color: #111827;
  font-size: clamp(24px, 3vw, 34px);
  line-height: 1.1;
}

.inbox-detail__sender {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 16px;
  color: #64748b;
}

.inbox-detail__body {
  margin-top: 22px;
  padding: 20px 22px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.92), rgba(255, 255, 255, 0.98));
  border: 1px solid rgba(148, 163, 184, 0.14);
  color: #1f2937;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}

.inbox-detail__action {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  min-height: 44px;
  padding: 0 16px;
  border: 0;
  border-radius: 16px;
  background: #111827;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.inbox-empty {
  display: grid;
  justify-items: center;
  align-content: center;
  gap: 12px;
  min-height: 100%;
  padding: 32px 24px;
  text-align: center;
  color: #64748b;
}

.inbox-empty strong {
  color: #0f172a;
  font-size: 20px;
}

.inbox-empty .el-icon {
  font-size: 34px;
}

@media (max-width: 980px) {
  .inbox-hero,
  .inbox-board {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .inbox-detail {
    padding: 20px;
  }

  .inbox-item__head {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
