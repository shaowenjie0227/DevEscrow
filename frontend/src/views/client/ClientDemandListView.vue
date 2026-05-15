<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>我的需求</h2>
      <div class="toolbar-actions">
        <el-button @click="loadDemands">刷新</el-button>
        <el-button type="primary" @click="$router.push('/client/demands/create')">新建需求</el-button>
      </div>
    </div>

    <el-table :data="demands" v-loading="loading">
      <el-table-column label="封面" width="108">
        <template #default="{ row }">
          <div class="cover-cell">
            <img v-if="row.coverImage" :src="row.coverImage" alt="需求封面" />
            <span v-else>无图</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="demandNo" label="需求编号" min-width="180" />
      <el-table-column prop="title" label="需求标题" min-width="220" />
      <el-table-column prop="category" label="分类" width="140" />
      <el-table-column label="审核状态" width="150">
        <template #default="{ row }">
          <el-tag :type="getDemandReviewStatus(row.reviewStatus).type">
            {{ getDemandReviewStatus(row.reviewStatus).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务状态" width="180">
        <template #default="{ row }">
          <el-tag :type="getDemandStatus(row.status).type">
            {{ getDemandStatus(row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="报价数" width="100" align="center">
        <template #default="{ row }">
          <strong class="quote-count">{{ row.quoteCount ?? 0 }}</strong>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openQuotes(row)">查看报价</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="quoteDrawerVisible" size="48%" title="需求报价列表">
      <div v-if="currentDemand" class="drawer-panel">
        <div class="drawer-header">
          <img v-if="currentDemand.coverImage" :src="currentDemand.coverImage" alt="需求封面" class="drawer-cover" />
          <div class="drawer-copy">
            <span class="drawer-eyebrow">当前需求单</span>
            <strong>{{ currentDemand.title }}</strong>
            <p>{{ currentDemand.summary || '已发布需求，等待进一步协作。' }}</p>
          </div>
        </div>

        <div class="drawer-summary-grid">
          <article class="summary-item">
            <span class="summary-label">需求编号</span>
            <strong class="summary-value">{{ currentDemand.demandNo }}</strong>
          </article>
          <article class="summary-item">
            <span class="summary-label">预算区间</span>
            <strong class="summary-value">{{ formatBudget(currentDemand) }}</strong>
          </article>
          <article class="summary-item">
            <span class="summary-label">预计工期</span>
            <strong class="summary-value">{{ formatExpectedDays(currentDemand.expectedDays) }}</strong>
          </article>
          <article class="summary-item">
            <span class="summary-label">交付方式</span>
            <strong class="summary-value">{{ getDeliveryTypeLabel(currentDemand.deliveryType) }}</strong>
          </article>
          <article class="summary-item">
            <span class="summary-label">审核状态</span>
            <el-tag :type="getDemandReviewStatus(currentDemand.reviewStatus).type">
              {{ getDemandReviewStatus(currentDemand.reviewStatus).label }}
            </el-tag>
          </article>
          <article class="summary-item">
            <span class="summary-label">业务状态</span>
            <el-tag :type="getDemandStatus(currentDemand.status).type">
              {{ getDemandStatus(currentDemand.status).label }}
            </el-tag>
          </article>
        </div>

        <div class="drawer-meta">
          <span>{{ currentDemand.images?.length || 0 }} 张图片</span>
          <span>{{ currentDemand.attachments?.length || 0 }} 个附件</span>
          <span>{{ currentDemand.deliveryType === 2 ? currentDemand.stagePlans?.length || 0 : 1 }} 个交付阶段</span>
          <span>{{ currentDemand.quoteCount ?? quotes.length }} 份报价</span>
        </div>
      </div>

      <div class="quote-section">
        <div class="section-heading">
          <div>
            <h3>开发者报价</h3>
            <p>这里会展示围绕这张需求单收到的全部报价方案。</p>
          </div>
          <span class="section-badge">{{ quotes.length }} 份</span>
        </div>

        <el-table v-if="quotes.length" :data="quotes" v-loading="quoteLoading">
          <el-table-column prop="quoteNo" label="报价编号" min-width="160" />
          <el-table-column prop="developerId" label="开发者 ID" width="120" />
          <el-table-column prop="priceTotal" label="报价金额" width="120" />
          <el-table-column prop="estimatedDays" label="工期" width="100" />
          <el-table-column label="报价状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getQuoteStatus(row.status).type">{{ getQuoteStatus(row.status).label }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="handleCreateOrder(row)">创建订单</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty
          v-else
          :description="emptyQuoteDescription"
          class="quote-empty"
        />
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/modules/order'
import { fetchDemandQuotes, fetchMyDemands } from '@/api/modules/demand'
import { getDemandReviewStatus, getDemandStatus, getQuoteStatus } from '@/utils/status'

const loading = ref(false)
const quoteLoading = ref(false)
const demands = ref([])
const quotes = ref([])
const currentDemand = ref(null)
const quoteDrawerVisible = ref(false)

const emptyQuoteDescription = computed(() => {
  if (!currentDemand.value) {
    return '暂无需求数据'
  }
  if (currentDemand.value.reviewStatus === 0) {
    return '需求单已提交，正在等待平台审核，审核通过后才会进入开发者报价。'
  }
  if (currentDemand.value.reviewStatus === 2) {
    return '需求单审核未通过，修改后重新提交，开发者才能看到并报价。'
  }
  if (currentDemand.value.status === 4) {
    return '你已经选定开发者，当前需求单正在进入订单流程。'
  }
  return '你的需求单已经发布成功，当前还没有开发者报价，请继续等待或补充更明确的需求说明。'
})

async function loadDemands() {
  loading.value = true
  try {
    const response = await fetchMyDemands()
    demands.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载需求列表失败')
  } finally {
    loading.value = false
  }
}

async function openQuotes(demand) {
  currentDemand.value = demand
  quotes.value = []
  quoteDrawerVisible.value = true
  quoteLoading.value = true
  try {
    const response = await fetchDemandQuotes(demand.id)
    quotes.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载报价列表失败')
  } finally {
    quoteLoading.value = false
  }
}

async function handleCreateOrder(quote) {
  if (!currentDemand.value) {
    return
  }
  try {
    await createOrder({
      demandId: currentDemand.value.id,
      quoteId: quote.id
    })
    ElMessage.success('订单创建成功，请前往我的订单完成托管支付')
    quoteDrawerVisible.value = false
    await loadDemands()
  } catch (error) {
    ElMessage.error(error.message || '创建订单失败')
  }
}

function formatBudget(demand) {
  const minValue = Number(demand?.budgetMin || 0)
  const maxValue = Number(demand?.budgetMax || 0)
  if (!maxValue || minValue === maxValue) {
    return `￥${minValue.toLocaleString()}`
  }
  return `￥${minValue.toLocaleString()} - ￥${maxValue.toLocaleString()}`
}

function formatExpectedDays(days) {
  if (!days) {
    return '待补充'
  }
  return `${days} 天`
}

function getDeliveryTypeLabel(type) {
  return type === 2 ? '分阶段交付' : '一次性交付'
}

onMounted(loadDemands)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.quote-count {
  color: var(--brand-primary);
  font-weight: 800;
}

.drawer-panel {
  padding: 4px 4px 18px;
  border-radius: 24px;
  background:
    linear-gradient(145deg, rgba(248, 250, 252, 0.96), rgba(239, 246, 255, 0.78)),
    rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
  margin-bottom: 20px;
}

.drawer-header {
  display: grid;
  grid-template-columns: 112px minmax(0, 1fr);
  gap: 16px;
  padding: 18px;
}

.drawer-cover,
.cover-cell img {
  width: 100%;
  height: 80px;
  object-fit: cover;
  border-radius: 16px;
}

.drawer-copy {
  min-width: 0;
}

.drawer-copy strong {
  display: block;
  font-size: 18px;
  color: var(--text-main);
}

.drawer-eyebrow {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  margin-bottom: 10px;
  background: rgba(37, 99, 235, 0.12);
  color: var(--brand-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.cover-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 56px;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.92);
  color: var(--text-soft);
  overflow: hidden;
}

.drawer-copy p {
  margin: 8px 0 0;
  color: var(--text-sub);
  line-height: 1.7;
}

.drawer-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 0 18px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.summary-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--text-soft);
}

.summary-value {
  color: var(--text-main);
  font-size: 15px;
  line-height: 1.5;
  word-break: break-all;
}

.drawer-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 16px;
  padding: 0 18px;
}

.drawer-meta span {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.08);
  color: var(--brand-primary);
  font-size: 12px;
  font-weight: 700;
}

.quote-section {
  padding-top: 4px;
}

.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.section-heading h3 {
  margin: 0;
  font-size: 16px;
  color: var(--text-main);
}

.section-heading p {
  margin: 6px 0 0;
  color: var(--text-sub);
  line-height: 1.6;
}

.section-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  color: var(--text-main);
  font-weight: 800;
}

.quote-empty {
  padding: 24px 0 8px;
  border-radius: 22px;
  background: rgba(248, 250, 252, 0.78);
}

@media (max-width: 960px) {
  .drawer-header,
  .drawer-summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
