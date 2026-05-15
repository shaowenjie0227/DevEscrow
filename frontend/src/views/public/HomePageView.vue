<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Footer from '@/components/home/Footer.vue'
import FeedGrid from '@/components/home/FeedGrid.vue'
import HeroBanner from '@/components/home/HeroBanner.vue'
import HomeNoticeBoard from '@/components/home/HomeNoticeBoard.vue'
import MarketBannerCarousel from '@/components/home/MarketBannerCarousel.vue'
import StatsBar from '@/components/home/StatsBar.vue'
import TechCategory from '@/components/home/TechCategory.vue'
import TopNav from '@/components/home/TopNav.vue'
import { fetchHomeBanners, fetchHomeNotices, fetchPublicMarketDemands } from '@/api/modules/demand'
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
const notices = ref<any[]>([])
const demands = ref<any[]>([])

const marketSignals = [
  {
    label: '高频类型',
    value: 'Vue 3 / 后台 / 小程序',
    note: '更适合当天就开始沟通范围、首版功能与交付节奏。'
  },
  {
    label: '更稳妥',
    value: 'Demo / MVP / 接盘续做',
    note: '信息越具体，越容易快速进入报价与排期。'
  },
  {
    label: '近期涨价',
    value: 'AI 知识库 / 企业内部工具',
    note: '通常更看重方案成熟度，而不只是单纯工时。'
  }
]

const tradingRules = [
  '先确认交付物，再给价格区间，避免一开始就空对空。',
  '能 mock 的环节先 mock，前两轮先验证关键流程是否跑通。',
  '信息越完整、交付节奏越清晰，越容易在当天进入报价。'
]

const defaultNotices = [
  {
    id: 1,
    noticeType: 1,
    typeLabel: '公告',
    title: '平台规则更新说明',
    summary: '报价前请先完善交付范围、阶段目标与验收方式，减少来回确认成本。',
    targetUrl: '/publish',
    coverUrl: ''
  },
  {
    id: 2,
    noticeType: 2,
    typeLabel: '活动',
    title: '五月需求撮合活动',
    summary: '完成实名认证和技能审核的开发者，可优先展示在活动推荐位。',
    targetUrl: '/market',
    coverUrl: ''
  },
  {
    id: 3,
    noticeType: 1,
    typeLabel: '公告',
    title: '管理员可维护首页内容',
    summary: '轮播图与右侧公告活动都支持在后台独立新增、编辑、上下线。',
    targetUrl: '/admin/banners',
    coverUrl: ''
  }
]

function formatBudget(min: unknown, max: unknown) {
  const minValue = Number(min || 0)
  const maxValue = Number(max || 0)

  if (!minValue && !maxValue) return '预算待沟通'
  if (!maxValue || minValue === maxValue) return `¥${minValue.toLocaleString()}`
  return `¥${minValue.toLocaleString()} - ¥${maxValue.toLocaleString()}`
}

async function loadBanners() {
  const response = await fetchHomeBanners()
  banners.value = (response.data || []).map((item: any) => ({
    id: item.id,
    title: item.title,
    subtitle: item.subtitle,
    buttonText: item.buttonText || '查看说明',
    image: item.imageUrl,
    targetUrl: item.targetUrl
  }))
}

async function loadNotices() {
  const response = await fetchHomeNotices()
  const remoteItems = (response.data || []).map((item: any) => ({
    id: item.id,
    noticeType: item.noticeType,
    typeLabel: item.typeLabel,
    title: item.title,
    summary: item.summary,
    targetUrl: item.targetUrl,
    coverUrl: item.coverUrl
  }))
  notices.value = remoteItems.length ? remoteItems : defaultNotices
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

function navigateByUrl(url?: string) {
  if (!url) return
  if (/^https?:\/\//.test(url)) {
    window.open(url, '_blank', 'noopener')
    return
  }
  router.push(url)
}

function goPublish() {
  router.push('/publish')
}

function goMarket() {
  router.push('/market')
}

function handleBannerAction(item: any) {
  navigateByUrl(item.targetUrl || '/publish')
}

function handleNoticeAction(item: any) {
  router.push(`/notices/${item.id}`)
}

onMounted(async () => {
  await Promise.all([loadBanners(), loadNotices(), loadMarketDemands()])
})
</script>

<template>
  <div class="market-page">
    <TopNav />

    <section class="market-container home-hero-grid">
      <div class="home-hero-grid__main">
        <MarketBannerCarousel :items="banners" @action="handleBannerAction" />
      </div>
      <div class="home-hero-grid__side">
        <HomeNoticeBoard :items="notices" @action="handleNoticeAction" />
      </div>
    </section>

    <TechCategory :categories="techCategories" />
    <HeroBanner :metrics="heroMetrics" @publish="goPublish" @browse="goMarket" />
    <StatsBar :items="statsBar" />

    <section class="market-container market-layout">
      <aside class="market-sidebar">
        <article class="market-side-card market-side-card--stack">
          <div class="market-side-card__head">
            <span class="market-side-card__eyebrow">Market Signals</span>
            <h3 class="market-side-card__title">最近更容易成交的需求长什么样</h3>
            <p>不是看词大不大，而是看范围够不够清楚、交付是否能拆、预算是不是能快速判断。</p>
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
.home-hero-grid {
  --home-hero-panel-height: 668px;
  display: grid;
  grid-template-columns: minmax(0, 1.58fr) minmax(360px, 0.82fr);
  gap: 20px;
  align-items: stretch;
}

.home-hero-grid__main,
.home-hero-grid__side {
  min-height: var(--home-hero-panel-height);
}

.home-hero-grid__main :deep(.market-container) {
  width: 100%;
  height: 100%;
}

.home-hero-grid__main :deep(.market-banner) {
  margin-top: 12px;
  height: calc(100% - 12px);
}

.home-hero-grid__side {
  padding-top: 12px;
  min-height: 100%;
}

.home-hero-grid__side :deep(.home-notice-board) {
  height: calc(100% - 12px);
  min-height: 100%;
}

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

@media (max-width: 1180px) {
  .home-hero-grid {
    grid-template-columns: 1fr;
    --home-hero-panel-height: auto;
  }

  .home-hero-grid__side {
    padding-top: 0;
  }

  .home-hero-grid__main,
  .home-hero-grid__side {
    min-height: auto;
  }

  .home-hero-grid__main :deep(.market-banner),
  .home-hero-grid__side :deep(.home-notice-board) {
    min-height: auto;
    height: auto;
  }
}

@media (max-width: 720px) {
  .market-side-card--stack {
    padding: 18px;
  }
}
</style>
