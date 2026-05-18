<template>
  <section class="dashboard-view">
    <article class="page-card hero-banner">
      <div class="hero-copy">
        <span class="hero-kicker">Client Overview</span>
        <h1 class="hero-title">把需求、订单、聊天和站内信放进同一个可执行视图里。</h1>
        <p class="hero-desc">
          这里会把你当前最需要推进的动作收拢起来。发布需求后，可以直接跟进报价、查看站内信提醒、和开发者沟通，再进入托管协作流程。
        </p>
        <div class="toolbar-actions">
          <el-button type="primary" @click="router.push('/client/demands/create')">发布新需求</el-button>
          <el-button @click="router.push('/client/orders')">查看订单进度</el-button>
        </div>
      </div>

      <div class="hero-metrics">
        <div class="hero-metric">
          <span>已发布需求</span>
          <strong>{{ stats.demands }}</strong>
        </div>
        <div class="hero-metric">
          <span>协作订单</span>
          <strong>{{ stats.orders }}</strong>
        </div>
        <div class="hero-metric">
          <span>未读站内信</span>
          <strong>{{ stats.unreadInbox }}</strong>
        </div>
        <div class="hero-metric">
          <span>未读聊天</span>
          <strong>{{ stats.unreadChats }}</strong>
        </div>
      </div>
    </article>

    <section class="message-hub">
      <button class="page-card message-portal message-portal--mail" type="button" @click="router.push('/client/inbox')">
        <span class="message-portal__icon">
          <el-icon><Message /></el-icon>
        </span>
        <div class="message-portal__copy">
          <strong>我的信箱</strong>
          <p>{{ stats.unreadInbox > 0 ? `${stats.unreadInbox} 封未读站内信，建议优先查看。` : '管理员单独发来的通知、提醒和补充说明会出现在这里。' }}</p>
        </div>
        <em v-if="stats.unreadInbox > 0" class="message-portal__badge">{{ stats.unreadInbox }}</em>
      </button>

      <button class="page-card message-portal" type="button" @click="router.push('/client/messages')">
        <span class="message-portal__icon">
          <el-icon><ChatDotRound /></el-icon>
        </span>
        <div class="message-portal__copy">
          <strong>聊天会话</strong>
          <p>{{ stats.unreadChats > 0 ? `${stats.unreadChats} 条未读聊天，适合先确认需求边界。` : '报价沟通、交付确认和历史聊天会集中保存在这里。' }}</p>
        </div>
      </button>
    </section>

    <section class="dashboard-grid-modern">
      <article class="page-card metric-card">
        <span class="metric-label">需求池状态</span>
        <strong class="metric-value">{{ stats.demands }}</strong>
        <p class="metric-note">集中跟进审核中的需求和正在报价的项目，避免线索散落。</p>
        <span class="metric-foot">建议优先完善预算边界和交付说明，提升报价质量。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">履约订单</span>
        <strong class="metric-value">{{ stats.orders }}</strong>
        <p class="metric-note">订单创建后可以直接进入托管支付、交付验收和修改反馈流程。</p>
        <span class="metric-foot">有待验收订单时，建议尽快查看详情，减少交付等待。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">消息信号</span>
        <strong class="metric-value">{{ stats.unreadInbox + stats.unreadChats }}</strong>
        <p class="metric-note">站内信更适合管理员通知，聊天更适合项目沟通，两条信息流现在都会被清晰提示。</p>
        <span class="metric-foot">先处理管理员提醒，再回到项目对话，通常能少走弯路。</span>
      </article>
    </section>

    <article class="page-card">
      <div class="toolbar">
        <h2>协作建议</h2>
      </div>
      <div class="action-list">
        <div class="action-item">
          <strong>需求说明尽量可衡量</strong>
          <p>把目标、预算、交付范围和截止时间写清楚，开发者的报价会更稳定、更容易成交。</p>
        </div>
        <div class="action-item">
          <strong>订单创建后优先完成托管</strong>
          <p>尽早进入托管支付，能让协作双方更快进入开发和验收节奏。</p>
        </div>
        <div class="action-item">
          <strong>异常情况及时留痕</strong>
          <p>如果出现延期、交付争议或沟通异常，可以直接在纠纷中心发起处理并保留记录。</p>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
import { onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fetchMyConversations } from '@/api/modules/chat'
import { useChatWebSocket } from '@/composables/useChatWebSocket'
import { useInboxUnreadCount } from '@/composables/useInboxUnreadCount'
import { fetchMyDemands } from '@/api/modules/demand'
import { fetchClientOrders } from '@/api/modules/order'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const stats = reactive({
  demands: 0,
  orders: 0,
  unreadChats: 0,
  unreadInbox: 0
})

const { unreadCount: inboxUnreadCount, refreshUnreadCount } = useInboxUnreadCount(authStore)

watch(
  inboxUnreadCount,
  (value) => {
    stats.unreadInbox = Number(value || 0)
  },
  { immediate: true }
)

const chatSocket = useChatWebSocket({
  onMessage: async (event) => {
    try {
      const payload = JSON.parse(event.data)
      if (payload?.type !== 'CHAT_MESSAGE') {
        return
      }
      await refreshChatUnreadCount()
    } catch (error) {
      // Ignore malformed websocket payloads on dashboard widgets.
    }
  }
})

onMounted(async () => {
  try {
    const [demands, orders] = await Promise.all([
      fetchMyDemands(),
      fetchClientOrders(),
      refreshChatUnreadCount(),
      refreshUnreadCount()
    ])
    stats.demands = demands.data.length
    stats.orders = orders.data.length
  } catch (error) {
    ElMessage.error(error.message || '加载控制台数据失败')
  }

  if (authStore.token) {
    chatSocket.connect(authStore.token)
  }
})

async function refreshChatUnreadCount() {
  const response = await fetchMyConversations()
  stats.unreadChats = (response.data || []).reduce((sum, item) => sum + Number(item.unreadCount || 0), 0)
}
</script>

<style scoped>
.dashboard-view {
  display: grid;
  gap: 20px;
}

.hero-banner {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(280px, 0.9fr);
  gap: 24px;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-title {
  margin: 14px 0 0;
  font-size: clamp(30px, 4vw, 44px);
  line-height: 0.98;
  letter-spacing: -0.05em;
  color: #0f172a;
}

.hero-desc {
  margin: 14px 0 0;
  color: #475569;
  line-height: 1.8;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
  flex-wrap: wrap;
}

.hero-metrics {
  display: grid;
  gap: 14px;
  align-content: start;
}

.hero-metric {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.96), rgba(239, 246, 255, 0.78));
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.hero-metric span {
  color: #64748b;
  font-size: 13px;
}

.hero-metric strong {
  display: block;
  margin-top: 10px;
  font-size: clamp(28px, 4vw, 42px);
  line-height: 1;
  color: #0f172a;
}

.message-hub {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.message-portal {
  position: relative;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 16px;
  width: 100%;
  border: 0;
  text-align: left;
  cursor: pointer;
}

.message-portal--mail {
  background:
    radial-gradient(circle at top right, rgba(249, 115, 22, 0.16), transparent 28%),
    linear-gradient(135deg, rgba(255, 250, 241, 0.96), rgba(255, 255, 255, 0.94));
}

.message-portal__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.08);
  color: #0f172a;
  font-size: 22px;
}

.message-portal__copy strong {
  display: block;
  color: #0f172a;
  font-size: 20px;
}

.message-portal__copy p {
  margin: 10px 0 0;
  color: #475569;
  line-height: 1.75;
}

.message-portal__badge {
  position: absolute;
  top: 18px;
  right: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-style: normal;
  font-size: 12px;
  font-weight: 800;
}

.dashboard-grid-modern {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.metric-card {
  display: grid;
  gap: 16px;
}

.metric-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #64748b;
}

.metric-value {
  font-size: clamp(34px, 4vw, 52px);
  line-height: 0.95;
  color: #0f172a;
}

.metric-note {
  color: #475569;
  line-height: 1.8;
}

.metric-foot {
  color: #2563eb;
  font-weight: 600;
  line-height: 1.7;
}

.toolbar h2 {
  margin: 0;
}

.action-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.action-item {
  padding: 18px;
  border-radius: 20px;
  background: rgba(248, 250, 252, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.action-item strong {
  color: #0f172a;
  font-size: 16px;
}

.action-item p {
  margin: 8px 0 0;
  line-height: 1.7;
  color: #475569;
}

@media (max-width: 960px) {
  .hero-banner,
  .message-hub,
  .dashboard-grid-modern,
  .action-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .toolbar-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
