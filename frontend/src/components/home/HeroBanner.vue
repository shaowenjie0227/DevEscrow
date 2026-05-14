<script setup lang="ts">
import type { HeroMetric } from '@/types/home'

defineProps<{
  metrics: HeroMetric[]
}>()

const emit = defineEmits<{
  publish: []
  browse: []
}>()
</script>

<template>
  <section class="market-container">
    <div class="market-banner">
      <div class="market-banner__inner">
        <div class="market-banner__copy">
          <span class="market-kicker">PROGRAMMER ESCROW MVP</span>
          <div>
            <h1 class="market-banner__title">接单大厅</h1>
            <p class="market-banner__desc">
              像逛闲鱼一样浏览用户发布的技术需求。先看封面和标题，快速筛掉不合适的单，再点进详情判断是否值得报价。
            </p>
          </div>

          <div class="market-banner__cta">
            <button class="market-btn market-btn--primary" type="button" @click="emit('publish')">发布我的需求</button>
            <button class="market-btn market-btn--ghost" type="button" @click="emit('browse')">浏览全部订单</button>
          </div>

          <div class="market-banner__notes">
            <span>新需求滚动更新</span>
            <span>支持 Demo / MVP / 接盘续做</span>
          </div>
        </div>

        <div class="market-banner__stats">
          <article v-for="metric in metrics" :key="metric.label" class="market-metric">
            <p class="market-metric__label">{{ metric.label }}</p>
            <h2 class="market-metric__value">{{ metric.value }}</h2>
            <p class="market-metric__hint">{{ metric.hint }}</p>
          </article>
        </div>
      </div>
    </div>
  </section>
</template>
