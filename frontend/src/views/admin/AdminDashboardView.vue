<template>
  <section class="dashboard-view">
    <article class="page-card hero-banner">
      <div class="hero-copy">
        <span class="hero-kicker">Operations Overview</span>
        <h1 class="hero-title">把平台审核、履约和纠纷处理汇总到一条清晰的运营面板里。</h1>
        <p class="hero-desc">
          这里帮助你快速判断平台当前最需要处理的动作，尤其是待审核需求、活跃订单和异常协作问题。
        </p>
        <div class="toolbar-actions">
          <el-button type="primary" @click="router.push('/admin/demands')">处理需求审核</el-button>
          <el-button @click="router.push('/admin/disputes')">查看纠纷中心</el-button>
        </div>
      </div>

      <div class="hero-metrics">
        <div class="hero-metric">
          <span>平台用户数</span>
          <strong>{{ stats.users }}</strong>
        </div>
        <div class="hero-metric">
          <span>待审核需求</span>
          <strong>{{ stats.pendingDemands }}</strong>
        </div>
      </div>
    </article>

    <section class="dashboard-grid-modern">
      <article class="page-card metric-card">
        <span class="metric-label">用户体量</span>
        <strong class="metric-value">{{ stats.users }}</strong>
        <p class="metric-note">用户规模越大，越适合把账户状态、角色结构和异常行为放到固定节奏里复查。</p>
        <span class="metric-foot">优先关注被封禁和高频协作用户的变化。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">审核压力</span>
        <strong class="metric-value">{{ stats.pendingDemands }}</strong>
        <p class="metric-note">待审核需求越多，越需要尽快处理，避免需求流转和报价节奏被卡住。</p>
        <span class="metric-foot">建议先看描述边界清晰、可快速放行的需求。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">订单全景</span>
        <strong class="metric-value">{{ stats.orders }}</strong>
        <p class="metric-note">订单侧可以帮助你快速确认托管、履约和验收是否处在健康状态。</p>
        <span class="metric-foot">异常进度的订单值得优先打开详情查看。</span>
      </article>

      <article class="page-card metric-card">
        <span class="metric-label">纠纷处理</span>
        <strong class="metric-value">{{ stats.disputes }}</strong>
        <p class="metric-note">纠纷数量是平台协作风险的重要信号，处理效率会直接影响交易体验。</p>
        <span class="metric-foot">需要结合订单状态和资金处理动作一起判断。</span>
      </article>
    </section>

    <article class="page-card">
      <div class="toolbar">
        <h2>运营优先级</h2>
      </div>
      <div class="action-list">
        <div class="action-item">
          <strong>优先清理待审核需求</strong>
          <p>审核通过越及时，用户端和开发者越容易进入有效报价和成交阶段。</p>
        </div>
        <div class="action-item">
          <strong>关注履约节点异常</strong>
          <p>如果订单长时间停滞在托管或待验收阶段，通常意味着后续容易出现争议。</p>
        </div>
        <div class="action-item">
          <strong>让纠纷处理动作可追踪</strong>
          <p>继续履约、退款甲方和放款乙方这类动作都建议带上清晰说明，方便后续复盘。</p>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchAdminUsers, fetchAdminDemands, fetchAdminOrders, fetchAdminDisputes } from '@/api/modules/admin'

const router = useRouter()
const stats = reactive({
  users: 0,
  pendingDemands: 0,
  orders: 0,
  disputes: 0
})

onMounted(async () => {
  try {
    const [users, demands, orders, disputes] = await Promise.all([
      fetchAdminUsers(),
      fetchAdminDemands({ reviewStatus: 0 }),
      fetchAdminOrders(),
      fetchAdminDisputes()
    ])
    stats.users = users.data.length
    stats.pendingDemands = demands.data.length
    stats.orders = orders.data.length
    stats.disputes = disputes.data.length
  } catch (error) {
    ElMessage.error(error.message || '加载控制台数据失败')
  }
})
</script>
