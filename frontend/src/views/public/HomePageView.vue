<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Footer from '@/components/home/Footer.vue'
import FeedGrid from '@/components/home/FeedGrid.vue'
import HeroBanner from '@/components/home/HeroBanner.vue'
import HomeNoticeBoard from '@/components/home/HomeNoticeBoard.vue'
import MarketBannerCarousel from '@/components/home/MarketBannerCarousel.vue'
import StatsBar from '@/components/home/StatsBar.vue'
import TechCategory from '@/components/home/TechCategory.vue'
import TopNav from '@/components/home/TopNav.vue'
import {
  fetchHomeBanners,
  fetchHomeNotices,
  fetchHomeOverview,
  fetchPublicMarketDemands
} from '@/api/modules/demand'
import { techCategories } from '@/mock/home'
import type {
  HeroMetric,
  HomeAdminContact,
  HomeInsightItem,
  HomeNoticeItem,
  HomeOverviewPayload,
  StatItem
} from '@/types/home'

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

const defaultAdminContact: HomeAdminContact = {
  title: '联系平台管理员',
  message: '如需入驻答疑、活动合作、问题反馈或人工协助，可通过下方二维码联系平台管理员。',
  qrImageUrl: '/qr-code.jpg'
}

const defaultHeroMetrics: HeroMetric[] = [
  {
    label: '公开需求',
    value: '--',
    hint: '已审核并在市场公开展示的需求数，适合快速浏览与报价。'
  },
  {
    label: '进行中协作',
    value: '--',
    hint: '统计当前处于托管、开发中与待验收阶段的真实协作订单。'
  },
  {
    label: '认证开发者',
    value: '--',
    hint: '仅统计已通过开发者审核、可直接参与沟通与接单的账号。'
  }
]

const defaultStatsBar: StatItem[] = [
  {
    label: '注册用户',
    value: '--',
    note: '实时统计当前可正常使用的平台账号规模，用于展示平台活跃基础。'
  },
  {
    label: '完结协作',
    value: '--',
    note: '累计完成验收的协作订单数量，可直接反映平台交付沉淀。'
  },
  {
    label: '公告与活动',
    value: '--',
    note: '由现有首页公告后台维护，上下线后会在首页右侧实时展示。'
  }
]

const router = useRouter()
const banners = ref<any[]>([])
const notices = ref<HomeNoticeItem[]>([])
const demands = ref<any[]>([])
const adminContact = ref<HomeAdminContact>({ ...defaultAdminContact })
const heroMetrics = ref<HeroMetric[]>(defaultHeroMetrics.map((item) => ({ ...item })))
const statsBar = ref<StatItem[]>(defaultStatsBar.map((item) => ({ ...item })))
const marketSignals = ref<HomeInsightItem[]>([])
const tradingRules = ref<string[]>([])
const hasMarketSidebar = computed(() => marketSignals.value.length > 0 || tradingRules.value.length > 0)

function formatBudget(min: unknown, max: unknown) {
  const minValue = Number(min || 0)
  const maxValue = Number(max || 0)

  if (!minValue && !maxValue) return '预算待沟通'
  if (!maxValue || minValue === maxValue) return `￥${minValue.toLocaleString()}`
  return `￥${minValue.toLocaleString()} - ￥${maxValue.toLocaleString()}`
}

function buildDemandDeliverables(stagePlans: any[] = []) {
  const deliverables = stagePlans
    .map((stage: any) => stage.stageName || stage.stageDesc || '')
    .filter(Boolean)

  return deliverables.length ? deliverables : ['可沟通拆分交付范围']
}

async function loadHomeOverview() {
  const response = await fetchHomeOverview()
  const data = (response.data || {}) as HomeOverviewPayload

  adminContact.value = {
    ...defaultAdminContact,
    ...(data.adminContact || {})
  }
  heroMetrics.value = data.heroMetrics?.length ? data.heroMetrics : defaultHeroMetrics.map((item) => ({ ...item }))
  statsBar.value = data.stats?.length ? data.stats : defaultStatsBar.map((item) => ({ ...item }))
  marketSignals.value = data.marketSignals?.length ? data.marketSignals : []
  tradingRules.value = data.tradingRules?.length ? data.tradingRules.filter(Boolean) : []
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
  notices.value = (response.data || []).map((item: any) => ({
    id: item.id,
    noticeType: item.noticeType,
    typeLabel: item.typeLabel,
    title: item.title,
    summary: item.summary,
    targetUrl: item.targetUrl,
    coverUrl: item.coverUrl,
    isPinned: Number(item.isPinned) === 1 ? 1 : 0
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
    deliverables: buildDemandDeliverables(item.stagePlans || []),
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
  navigateByUrl(item.targetUrl || `/notices/${item.id}`)
}

onMounted(async () => {
  await Promise.allSettled([loadHomeOverview(), loadBanners(), loadNotices(), loadMarketDemands()])
})
</script>

<template>
  <div class="market-page">
    <TopNav />

    <section class="market-container home-hero-grid">
      <div class="home-hero-grid__main">
        <MarketBannerCarousel :items="banners" @action="handleBannerAction" />

        <article class="admin-contact-card">
          <div class="admin-contact-card__copy">
            <span class="admin-contact-card__eyebrow">Contact Admin</span>
            <h2>{{ adminContact.title }}</h2>
            <p>{{ adminContact.message }}</p>
          </div>

          <div class="admin-contact-card__qr-slot">
            <img
              v-if="adminContact.qrImageUrl"
              class="admin-contact-card__qr-image"
              :src="adminContact.qrImageUrl"
              :alt="`${adminContact.title}二维码`"
            />
            <p v-else class="admin-contact-card__qr-placeholder">请在后端配置管理员联系二维码</p>
          </div>
        </article>
      </div>
      <div class="home-hero-grid__side">
        <HomeNoticeBoard :items="notices" @action="handleNoticeAction" />
      </div>
    </section>

    <TechCategory :categories="techCategories" />
    <HeroBanner :metrics="heroMetrics" @publish="goPublish" @browse="goMarket" />
    <StatsBar :items="statsBar" />

    <section class="market-container market-layout" :class="{ 'market-layout--full': !hasMarketSidebar }">
      <aside v-if="hasMarketSidebar" class="market-sidebar">
        <article v-if="marketSignals.length" class="market-side-card market-side-card--stack">
          <div class="market-side-card__head">
            <span class="market-side-card__eyebrow">Market Signals</span>
            <h3 class="market-side-card__title">最近更容易成交的需求长什么样</h3>
            <p>这里不强调夸张的包装，而是优先展示更接近真实业务沟通的判断信息和交付信号。</p>
          </div>

          <div class="market-signal-list">
            <article v-for="item in marketSignals" :key="item.label" class="market-signal">
              <span class="market-signal__label">{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <p>{{ item.note }}</p>
            </article>
          </div>
        </article>

        <article v-if="tradingRules.length" class="market-side-card market-side-card--stack">
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
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(320px, 0.78fr);
  gap: 18px;
  align-items: stretch;
}

.home-hero-grid__main :deep(.market-container) {
  width: 100%;
}

.home-hero-grid__main {
  display: grid;
  gap: 18px;
  align-content: start;
}

.home-hero-grid__main :deep(.market-banner) {
  margin-top: 14px;
}

.home-hero-grid__side {
  padding-top: 14px;
}

.admin-contact-card {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) 220px;
  gap: 20px;
  align-items: center;
  padding: 22px 24px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(243, 190, 37, 0.16), transparent 42%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.88));
  box-shadow: 0 24px 52px rgba(17, 19, 34, 0.08);
  backdrop-filter: blur(18px);
}

.admin-contact-card__copy {
  display: grid;
  gap: 10px;
}

.admin-contact-card__eyebrow {
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

.admin-contact-card h2 {
  margin: 0;
  font: 700 26px/1.08 var(--font-display);
  color: #111322;
}

.admin-contact-card p {
  margin: 0;
  color: rgba(17, 19, 34, 0.64);
  line-height: 1.8;
}

.admin-contact-card__qr-slot {
  display: grid;
  place-items: center;
  min-height: 188px;
  padding: 12px;
  border: 1px solid rgba(17, 19, 34, 0.1);
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(243, 244, 246, 0.78)),
    rgba(255, 255, 255, 0.9);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.82),
    0 16px 28px rgba(17, 19, 34, 0.06);
}

.admin-contact-card__qr-image {
  display: block;
  width: 100%;
  max-width: 176px;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 24px rgba(17, 19, 34, 0.1);
}

.admin-contact-card__qr-placeholder {
  max-width: 14ch;
  color: rgba(17, 19, 34, 0.5);
  font-size: 13px;
  line-height: 1.8;
  text-align: center;
}

.market-layout {
  align-items: start;
}

.market-layout--full {
  grid-template-columns: minmax(0, 1fr);
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
  }

  .home-hero-grid__side {
    padding-top: 0;
  }

  .admin-contact-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .admin-contact-card {
    padding: 18px;
  }

  .admin-contact-card__qr-slot {
    min-height: 164px;
  }

  .market-side-card--stack {
    padding: 18px;
  }
}
</style>
