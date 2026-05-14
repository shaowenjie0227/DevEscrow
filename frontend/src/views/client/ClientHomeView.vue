<template>
  <section class="dashboard-view">
    <article class="page-card hero-banner">
      <div class="hero-copy">
        <span class="hero-kicker">Client Overview</span>
        <h1 class="hero-title">把需求推进到可成交、可验收、可追踪的状态。</h1>
        <p class="hero-desc">
          这里聚合了你的需求池、协作订单和下一步动作。发布需求后，可以直接跟进报价、创建订单并进入托管流程。
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
      </div>
    </article>

    <section class="dashboard-grid-modern">
      <article class="page-card metric-card">
        <span class="metric-label">需求池状态</span>
        <strong class="metric-value">{{ stats.demands }}</strong>
        <p class="metric-note">集中跟进审核中的需求和正在报价的项目，避免线索散落。</p>
        <span class="metric-foot">建议优先完善预算边界和交付说明，提高报价质量。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">履约订单</span>
        <strong class="metric-value">{{ stats.orders }}</strong>
        <p class="metric-note">订单创建后可以直接进入托管支付、交付验收和修改反馈流程。</p>
        <span class="metric-foot">有待验收订单时，建议尽快查看详情，减少交付等待。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">当前节奏</span>
        <strong class="metric-value">{{ stats.orders > 0 ? '跟进中' : '待启动' }}</strong>
        <p class="metric-note">工作台会优先帮助你把需求转成可执行订单，再推进履约闭环。</p>
        <span class="metric-foot">如果还没有订单，可以先去需求页查看最新报价。</span>
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
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchMyDemands } from '@/api/modules/demand'
import { fetchClientOrders } from '@/api/modules/order'

const router = useRouter()
const stats = reactive({
  demands: 0,
  orders: 0
})

onMounted(async () => {
  try {
    const [demands, orders] = await Promise.all([fetchMyDemands(), fetchClientOrders()])
    stats.demands = demands.data.length
    stats.orders = orders.data.length
  } catch (error) {
    ElMessage.error(error.message || '加载控制台数据失败')
  }
})
</script>
