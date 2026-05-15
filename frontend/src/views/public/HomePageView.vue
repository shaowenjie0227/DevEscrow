<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Footer from '@/components/home/Footer.vue'
import FeedGrid from '@/components/home/FeedGrid.vue'
import HeroBanner from '@/components/home/HeroBanner.vue'
import MarketBannerCarousel from '@/components/home/MarketBannerCarousel.vue'
import StatsBar from '@/components/home/StatsBar.vue'
import TechCategory from '@/components/home/TechCategory.vue'
import TopNav from '@/components/home/TopNav.vue'
import { fetchHomeBanners, fetchPublicMarketDemands } from '@/api/modules/demand'
import { heroMetrics, statsBar, techCategories } from '@/mock/home'

const props = withDefaults(
  defineProps<{
    previewCount?: number
    showMoreAction?: boolean
    compact?: boolean
  }>(),
  {
    previewCount: Number.POSITIVE_INFINITY,
    showMoreAction: false,
    compact: false
  }
)

const router = useRouter()
const banners = ref<any[]>([])
const demands = ref<any[]>([])

const marketSignals = [
  {
    label: '高频类型',
    value: 'Vue 3 / 后台 / 小程序',
    note: '适合当天就开始沟通范围与首版交付。'
  },
  {
    label: '最稳妥',
    value: 'Demo / MVP / 接盘续做',
    note: '信息更具体，也更容易快速报价。'
  },
  {
    label: '近期涨价',
    value: 'AI 知识库 / 企业内部工具',
    note: '通常更看重方案成熟度，而不是单纯工时。'
  }
]

const tradingRules = [
  '先确认交付物，再给价格区间，避免一开始就空对空。',
  '能 mock 的环节先 mock，前两轮先解决关键流程是否跑通。',
  '信息越完整、交付节奏越清晰，越容易在当天进入报价。'
]

function formatBudget(min: unknown, max: unknown) {
  const minValue = Number(min || 0)
  const maxValue = Number(max || 0)

  if (!minValue && !maxValue) return '预算待沟通'
  if (!maxValue || minValue === maxValue) return `￥${minValue.toLocaleString()}`
  return `￥${minValue.toLocaleString()} - ￥${maxValue.toLocaleString()}`
}

async function loadBanners() {
  const response = await fetchHomeBanners()
  banners.value = (response.data || []).map((item: any) => ({
    id: item.id,
    title: item.title,
    subtitle: item.subtitle,
    buttonText: item.buttonText || '查看说明',
    image: item.imageUrl
  }))
}

async function loadMarketDemands() {
  const response = await fetchPublicMarketDemands()
  demands.value = (response.data || []).map((item: any) => ({
    id: item.id,
    title: item.title,
    category: item.category,
    summary: item.summary,
    detail: item.detail,
    coverImage:
      item.coverImage
      || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80',
    budget: formatBudget(item.budgetMin, item.budgetMax),
    deadline: `${item.expectedDays || '-'} 天内`,
    location: '线上协作',
    publisher: item.demandNo || '平台用户',
    postedAt: '刚刚更新',
    tags: [{ label: item.category || '公开需求' }],
    highlights: ['真实需求数据', '来自用户发布', '后台已审核'],
    deliverables:
      (item.stagePlans || []).map((stage: any) => stage.stageName || stage.stageDesc || '阶段交付')
      || ['可沟通拆分交付范围'],
    contactHint: '支持站内快速沟通'
  }))
}

function goPublish() {
  router.push('/publish')
}

function goMarket() {
  router.push('/market')
}

function handleBannerAction(item: any) {
  router.push(item.id ? '/publish' : '/admin')
}

onMounted(async () => {
  await Promise.all([loadBanners(), loadMarketDemands()])
})
</script>

<template>
  <div class="market-page">
    <TopNav />
    <MarketBannerCarousel :items="banners" @action="handleBannerAction" />
    <TechCategory :categories="techCategories" />
    <HeroBanner :metrics="heroMetrics" @publish="goPublish" @browse="goMarket" />
    <StatsBar :items="statsBar" />

    <section class="market-container market-layout">
      <aside class="market-sidebar">
        <article class="market-side-card market-side-card--stack">
          <div class="market-side-card__head">
            <span class="market-side-card__eyebrow">Market Signals</span>
            <h3 class="market-side-card__title">最近更容易成交的需求长什么样</h3>
            <p>不是看词大不大，而是看范围够不够清楚、交付是不是能拆。</p>
          </div>

          <div class="market-signal-list">
            <article v-for="item in marketSignals" :key="item.label" class="market-signal">
              <span class="market-signal__label">{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <p>{{ item.note }}</p>
            </article>
          </div>
        </article>

        <article class="market-side-card market-side-card--stack">
          <div class="market-side-card__head">
            <span class="market-side-card__eyebrow">How To Quote</span>
            <h3 class="market-side-card__title">更像真实合作，而不是花里胡哨的展示页</h3>
          </div>

          <ol class="market-rule-list">
            <li v-for="rule in tradingRules" :key="rule">
              <span>{{ rule }}</span>
            </li>
          </ol>
        </article>
      </aside>

      <FeedGrid
        :items="demands"
        :preview-count="previewCount"
        :show-more-action="showMoreAction"
        :compact="compact"
      />
    </section>

    <Footer />
  </div>
</template>

<style scoped>
.market-layout {
  align-items: start;
}

.market-sidebar {
  gap: 18px;
}

.market-side-card--stack {
  padding: 22px;
  border-radius: 20px;
}

.market-side-card__head {
  display: grid;
  gap: 8px;
  margin-bottom: 18px;
}

.market-side-card__eyebrow {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.56);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.market-side-card__head p {
  color: rgba(17, 19, 34, 0.6);
  line-height: 1.75;
}

.market-signal-list,
.market-rule-list {
  display: grid;
  gap: 12px;
}

.market-signal {
  padding-top: 12px;
  border-top: 1px solid rgba(17, 19, 34, 0.08);
}

.market-signal:first-child {
  padding-top: 0;
  border-top: 0;
}

.market-signal__label {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(243, 190, 37, 0.18);
  color: rgba(17, 19, 34, 0.74);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.market-signal strong {
  display: block;
  margin-top: 10px;
  font-family: var(--font-display);
  font-size: 21px;
  line-height: 1.2;
}

.market-signal p {
  margin-top: 8px;
  color: rgba(17, 19, 34, 0.58);
  font-size: 14px;
  line-height: 1.7;
}

.market-rule-list {
  margin: 0;
  padding: 0;
  list-style: none;
  counter-reset: rule;
}

.market-rule-list li {
  display: grid;
  grid-template-columns: 36px 1fr;
  gap: 12px;
  align-items: start;
  padding-top: 12px;
  border-top: 1px solid rgba(17, 19, 34, 0.08);
}

.market-rule-list li:first-child {
  padding-top: 0;
  border-top: 0;
}

.market-rule-list li::before {
  counter-increment: rule;
  content: counter(rule, decimal-leading-zero);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(17, 19, 34, 0.08);
  color: rgba(17, 19, 34, 0.74);
  font-size: 12px;
  font-weight: 700;
}

.market-rule-list span {
  color: rgba(17, 19, 34, 0.66);
  line-height: 1.8;
}

@media (max-width: 720px) {
  .market-side-card--stack {
    padding: 18px;
  }
}
</style>
