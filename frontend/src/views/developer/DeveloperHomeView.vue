<template>
  <section class="dashboard-view">
    <article class="page-card hero-banner">
      <div class="hero-copy">
        <span class="hero-kicker">Developer Overview</span>
        <h1 class="hero-title">把需求线索、报价节奏和履约进度放在同一个执行视图里。</h1>
        <p class="hero-desc">
          从筛选需求到提交报价，再到开始开发和发起交付，这里会把你当前最需要推进的动作收拢起来。
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
          <span>我的订单</span>
          <strong>{{ stats.orders }}</strong>
        </div>
      </div>
    </article>

    <section class="dashboard-grid-modern">
      <article class="page-card metric-card">
        <span class="metric-label">需求线索池</span>
        <strong class="metric-value">{{ stats.market }}</strong>
        <p class="metric-note">优先筛选预算、工期和方向合适的需求，再集中提交方案，效率会更高。</p>
        <span class="metric-foot">市场中线索越多，越适合先做一次快速过滤。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">报价记录</span>
        <strong class="metric-value">{{ stats.quotes }}</strong>
        <p class="metric-note">把已投报价和中标状态统一跟进，避免漏掉已进入沟通阶段的项目。</p>
        <span class="metric-foot">建议保持交付说明和技术方案简洁清晰，更容易建立信任。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">执行中订单</span>
        <strong class="metric-value">{{ stats.orders }}</strong>
        <p class="metric-note">订单开始后可以在这里跟进开发、提交交付和处理异常协作情况。</p>
        <span class="metric-foot">有托管中的订单时，尽量及时同步开发进展和交付说明。</span>
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
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchMarketDemands } from '@/api/modules/demand'
import { fetchMyQuotes } from '@/api/modules/quote'
import { fetchDeveloperOrders } from '@/api/modules/order'

const router = useRouter()
const stats = reactive({
  market: 0,
  quotes: 0,
  orders: 0
})

onMounted(async () => {
  try {
    const [market, quotes, orders] = await Promise.all([
      fetchMarketDemands(),
      fetchMyQuotes(),
      fetchDeveloperOrders()
    ])
    stats.market = market.data.length
    stats.quotes = quotes.data.length
    stats.orders = orders.data.length
  } catch (error) {
    ElMessage.error(error.message || '加载控制台数据失败')
  }
})
</script>
