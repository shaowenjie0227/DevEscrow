<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Clock, Location, Promotion } from '@element-plus/icons-vue'
import Footer from '@/components/home/Footer.vue'
import MarketSubNav from '@/components/home/MarketSubNav.vue'
import TopNav from '@/components/home/TopNav.vue'
import { fetchMarketDemands } from '@/api/modules/demand'

const route = useRoute()
const router = useRouter()
const demand = ref<any>(null)

async function loadDemand() {
  const response = await fetchMarketDemands()
  demand.value = (response.data || []).find((item: any) => String(item.id) === String(route.params.id)) || null
}

onMounted(loadDemand)
</script>

<template>
  <div class="market-page">
    <TopNav />
    <MarketSubNav />

    <main class="market-container market-detail" v-if="demand">
      <div class="market-detail__top">
        <button class="market-back" type="button" @click="router.push('/market')">
          <el-icon><ArrowLeft /></el-icon>
          返回接单大厅
        </button>
      </div>

      <section class="market-detail__grid">
        <article class="market-detail__media">
          <img
            :src="demand.coverImage || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80'"
            :alt="demand.title"
            class="market-detail__image"
          />
          <div class="market-detail__publisher">
            <p class="market-detail__text">发布方</p>
            <h2>{{ demand.demandNo }}</h2>
            <p class="market-detail__text">线上协作 · 实时更新</p>
          </div>
        </article>

        <article class="market-detail__body">
          <div class="market-detail__heading">
            <div>
              <p class="market-detail__text">{{ demand.category }}</p>
              <h1 class="market-detail__title">{{ demand.title }}</h1>
            </div>
            <span class="market-detail__budget">￥{{ Number(demand.budgetMin || 0).toLocaleString() }} - ￥{{ Number(demand.budgetMax || 0).toLocaleString() }}</span>
          </div>

          <div class="market-detail__gridcards">
            <div class="market-detail__card">
              <p class="market-detail__card-label">
                <el-icon><Clock /></el-icon>
                期望周期
              </p>
              <p class="market-detail__card-value">{{ demand.expectedDays || '-' }} 天</p>
            </div>
            <div class="market-detail__card">
              <p class="market-detail__card-label">
                <el-icon><Location /></el-icon>
                发布地区
              </p>
              <p class="market-detail__card-value">线上协作</p>
            </div>
            <div class="market-detail__card">
              <p class="market-detail__card-label">
                <el-icon><Promotion /></el-icon>
                沟通偏好
              </p>
              <p class="market-detail__card-value">平台内快速沟通</p>
            </div>
          </div>

          <section class="market-detail__section">
            <h2>需求介绍</h2>
            <p class="market-detail__text">{{ demand.detail }}</p>
          </section>
        </article>
      </section>
    </main>

    <main class="market-container market-detail" v-else>
      <section class="market-feed-card">
        <div class="market-feed-head">
          <div>
            <h2>需求不存在或已下架</h2>
            <p>可以返回接单大厅继续浏览其他订单。</p>
          </div>
        </div>
        <button class="market-btn market-btn--primary" type="button" @click="router.push('/market')">返回接单大厅</button>
      </section>
    </main>

    <Footer />
  </div>
</template>
