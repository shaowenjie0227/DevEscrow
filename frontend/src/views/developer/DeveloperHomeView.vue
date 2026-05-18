<template>
  <section class="dashboard-view">
    <article class="page-card hero-banner">
      <div class="hero-copy">
        <span class="hero-kicker">Developer Overview</span>
        <h1 class="hero-title">把线索、报价、履约和站内信提醒放在同一张执行面板里。</h1>
        <p class="hero-desc">
          从筛选需求到提交报价，再到交付推进和验收沟通，这里会把你当下最该处理的事情集中显示出来。
        </p>
        <div class="toolbar-actions">
          <el-button type="primary" @click="router.push('/developer/market')">浏览需求市场</el-button>
          <el-button @click="router.push('/developer/orders')">进入订单执行</el-button>
        </div>
      </div>

      <div class="hero-metrics">
        <div class="hero-metric">
          <span>可报价需求</span>
          <strong>{{ stats.market }}</strong>
        </div>
        <div class="hero-metric">
          <span>我的报价</span>
          <strong>{{ stats.quotes }}</strong>
        </div>
        <div class="hero-metric">
          <span>执行中订单</span>
          <strong>{{ stats.orders }}</strong>
        </div>
        <div class="hero-metric">
          <span>未读站内信</span>
          <strong>{{ stats.unreadInbox }}</strong>
        </div>
      </div>
    </article>

    <section class="message-hub">
      <button class="page-card message-portal message-portal--mail" type="button" @click="router.push('/developer/inbox')">
        <span class="message-portal__icon">
          <el-icon><Message /></el-icon>
        </span>
        <div class="message-portal__copy">
          <strong>开发者信箱</strong>
          <p>{{ stats.unreadInbox > 0 ? `${stats.unreadInbox} 封未读站内信待处理。` : '管理员发来的通知、审核补充和运营提醒会优先进入这里。' }}</p>
        </div>
        <em v-if="stats.unreadInbox > 0" class="message-portal__badge">{{ stats.unreadInbox }}</em>
      </button>

      <button class="page-card message-portal" type="button" @click="router.push('/developer/messages')">
        <span class="message-portal__icon">
          <el-icon><ChatDotRound /></el-icon>
        </span>
        <div class="message-portal__copy">
          <strong>聊天会话</strong>
          <p>需求边界、交付确认和附件往来都可以在这里继续跟进，不会和管理员通知混在一起。</p>
        </div>
      </button>
    </section>

    <section class="dashboard-grid-modern">
      <article class="page-card metric-card">
        <span class="metric-label">需求线索池</span>
        <strong class="metric-value">{{ stats.market }}</strong>
        <p class="metric-note">优先筛选预算、工期和方向合适的需求，再集中提交方案，效率会更高。</p>
        <span class="metric-foot">市场线索越多，越适合先做一次快速过滤。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">报价记录</span>
        <strong class="metric-value">{{ stats.quotes }}</strong>
        <p class="metric-note">把已投报价和中标状态统一跟进，避免漏掉已进入沟通阶段的项目。</p>
        <span class="metric-foot">保持交付说明和技术方案简洁清晰，更容易建立信任。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">履约信号</span>
        <strong class="metric-value">{{ stats.orders + stats.unreadInbox }}</strong>
        <p class="metric-note">订单推进与管理员提醒一起看，更容易及时发现需要处理的补件、通知或异常协作。</p>
        <span class="metric-foot">站内信有变化时，建议先看通知，再决定是否调整报价或交付节奏。</span>
      </article>
    </section>

    <article class="page-card">
      <div class="toolbar">
        <h2>开发者工作建议</h2>
      </div>
      <div class="mini-list">
        <div class="mini-item">
          <strong>先用一句话判断项目是否适合接</strong>
          <p>预算合理、周期可控、需求边界清晰的项目，往往更值得优先报价。</p>
        </div>
        <div class="mini-item">
          <strong>报价说明聚焦可交付结果</strong>
          <p>比起堆功能清单，更重要的是说明你会交付什么、何时交付、如何保障上线。</p>
        </div>
        <div class="mini-item">
          <strong>交付节点尽量留痕</strong>
          <p>当开发进度、提交说明和附件都沉淀在平台里，后续验收和争议处理会轻松很多。</p>
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
import { useInboxUnreadCount } from '@/composables/useInboxUnreadCount'
import { fetchMarketDemands } from '@/api/modules/demand'
import { fetchMyQuotes } from '@/api/modules/quote'
import { fetchDeveloperOrders } from '@/api/modules/order'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const stats = reactive({
  market: 0,
  quotes: 0,
  orders: 0,
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

onMounted(async () => {
  try {
    const [market, quotes, orders] = await Promise.all([
      fetchMarketDemands(),
      fetchMyQuotes(),
      fetchDeveloperOrders(),
      refreshUnreadCount()
    ])
    stats.market = market.data.length
    stats.quotes = quotes.data.length
    stats.orders = orders.data.length
  } catch (error) {
    ElMessage.error(error.message || '加载控制台数据失败')
  }
})
</script>

<style scoped>
.dashboard-view {
  display: grid;
  gap: 20px;
}

.hero-banner {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.85fr);
  gap: 24px;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(15, 155, 142, 0.12);
  color: #0f766e;
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
  background: linear-gradient(145deg, rgba(248, 250, 252, 0.96), rgba(240, 253, 250, 0.78));
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
    radial-gradient(circle at top right, rgba(13, 148, 136, 0.14), transparent 28%),
    linear-gradient(135deg, rgba(240, 253, 250, 0.96), rgba(255, 255, 255, 0.94));
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
  color: #0f9b8e;
  font-weight: 600;
  line-height: 1.7;
}

.toolbar h2 {
  margin: 0;
}

.mini-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.mini-item {
  padding: 18px;
  border-radius: 20px;
  background: rgba(248, 250, 252, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.mini-item strong {
  color: #0f172a;
  font-size: 16px;
}

.mini-item p {
  margin: 8px 0 0;
  line-height: 1.7;
  color: #475569;
}

@media (max-width: 960px) {
  .hero-banner,
  .message-hub,
  .dashboard-grid-modern,
  .mini-list {
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
