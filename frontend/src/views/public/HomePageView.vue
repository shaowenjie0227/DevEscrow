<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Footer from '@/components/home/Footer.vue'
import FeedGrid from '@/components/home/FeedGrid.vue'
import HeroBanner from '@/components/home/HeroBanner.vue'
import MarketBannerCarousel from '@/components/home/MarketBannerCarousel.vue'
import MarketSubNav from '@/components/home/MarketSubNav.vue'
import TopNav from '@/components/home/TopNav.vue'
import TechCategory from '@/components/home/TechCategory.vue'
import { fetchHomeBanners, fetchMarketDemands } from '@/api/modules/demand'
import { heroMetrics, statsBar, techCategories } from '@/mock/home'

const router = useRouter()
const banners = ref<any[]>([])
const demands = ref<any[]>([])

async function loadBanners() {
  const response = await fetchHomeBanners()
  banners.value = (response.data || []).map((item: any) => ({
    id: item.id,
    title: item.title,
    subtitle: item.subtitle,
    buttonText: item.buttonText || '查看详情',
    image: item.imageUrl
  }))
}

async function loadMarketDemands() {
  const response = await fetchMarketDemands()
  demands.value = (response.data || []).map((item: any) => ({
    id: item.id,
    title: item.title,
    category: item.category,
    summary: item.summary,
    detail: item.detail,
    coverImage: item.coverImage || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80',
    budget: `￥${Number(item.budgetMin || 0).toLocaleString()} - ￥${Number(item.budgetMax || 0).toLocaleString()}`,
    deadline: `${item.expectedDays || '-'}天内`,
    location: '线上协作',
    publisher: item.demandNo || '平台用户',
    postedAt: '刚刚更新',
    tags: [{ label: item.category || '需求' }],
    highlights: ['真实需求数据', '来自用户发布', '后台已审核'],
    deliverables: (item.stagePlans || []).map((stage: any) => stage.stageName || stage.stageDesc || '阶段交付'),
    contactHint: '平台内快速沟通'
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
    <MarketSubNav />
    <MarketBannerCarousel :items="banners" @action="handleBannerAction" />
    <TechCategory :categories="techCategories" />
    <HeroBanner :metrics="heroMetrics" @publish="goPublish" @browse="goMarket" />

    <section class="market-container market-layout">
      <aside class="market-sidebar">
        <article class="market-side-card">
          <h3 class="market-side-card__title">需求偏好</h3>
          <div class="market-side-list">
            <span class="market-side-pill"><b>高频</b> Vue3 后台</span>
            <span class="market-side-pill"><b>热门</b> Demo 急单</span>
            <span class="market-side-pill"><b>稳定</b> 小程序 MVP</span>
            <span class="market-side-pill"><b>高价</b> AI 知识库</span>
          </div>
        </article>

        <article class="market-side-card">
          <h3 class="market-side-card__title">大厅快照</h3>
          <div class="market-side-list">
            <span class="market-side-pill"><b>{{ statsBar[0].value }}</b> {{ statsBar[0].label }}</span>
            <span class="market-side-pill"><b>{{ statsBar[1].value }}</b> {{ statsBar[1].label }}</span>
            <span class="market-side-pill"><b>{{ statsBar[2].value }}</b> {{ statsBar[2].label }}</span>
          </div>
        </article>
      </aside>

      <FeedGrid :items="demands" />
    </section>

    <Footer />
  </div>
</template>
