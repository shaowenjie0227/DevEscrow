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
    <div class="market-banner market-banner--hero">
      <div class="market-banner__inner market-banner__inner--hero">
        <div class="market-banner__copy market-banner__copy--hero">
          <span class="market-kicker">Curated Demand Board</span>

          <div class="market-hero__heading">
            <span class="market-hero__serial">Issue 05</span>
            <h1 class="market-banner__title">先看交付范围，再决定要不要报价。</h1>
          </div>

          <p class="market-banner__desc">
            这里不是把需求做成一张张好看的卡片，而是尽量让你更快判断：这单值不值得聊、范围是否清楚、能不能当天进入报价。
          </p>

          <div class="market-banner__cta">
            <button class="market-btn market-btn--primary" type="button" @click="emit('publish')">发布我的需求</button>
            <button class="market-btn market-btn--ghost" type="button" @click="emit('browse')">浏览最新需求</button>
          </div>

          <div class="market-banner__notes market-banner__notes--hero">
            <span>公开需求滚动更新</span>
            <span>支持 Demo / MVP / 接盘续做</span>
            <span>优先展示信息完整的单子</span>
          </div>
        </div>

        <div class="market-banner__stats market-banner__stats--hero">
          <article v-for="(metric, index) in metrics" :key="metric.label" class="market-metric">
            <span class="market-metric__index">{{ String(index + 1).padStart(2, '0') }}</span>
            <p class="market-metric__label">{{ metric.label }}</p>
            <h2 class="market-metric__value">{{ metric.value }}</h2>
            <p class="market-metric__hint">{{ metric.hint }}</p>
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.market-banner--hero {
  border-radius: 22px;
  border-color: rgba(17, 19, 34, 0.1);
  background:
    linear-gradient(180deg, rgba(254, 251, 241, 0.96), rgba(255, 255, 255, 0.94));
  box-shadow: 0 24px 54px rgba(17, 19, 34, 0.08);
}

.market-banner--hero::before,
.market-banner--hero::after {
  opacity: 0.1;
}

.market-banner__inner--hero {
  grid-template-columns: minmax(0, 1.35fr) minmax(300px, 380px);
  gap: 28px;
  padding: 36px;
}

.market-banner__copy--hero {
  gap: 22px;
}

.market-kicker {
  min-height: auto;
  padding: 0;
  border-radius: 0;
  background: transparent;
  color: rgba(17, 19, 34, 0.54);
  letter-spacing: 0.2em;
}

.market-hero__heading {
  display: grid;
  gap: 10px;
}

.market-hero__serial {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.68);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.market-banner__title {
  max-width: 11ch;
  font-size: clamp(38px, 4.6vw, 72px);
  line-height: 0.96;
}

.market-banner__desc {
  max-width: 50ch;
  font-size: 16px;
  line-height: 1.9;
}

.market-btn {
  min-height: 48px;
  border-radius: 12px;
}

.market-btn--primary {
  background: #111322;
}

.market-btn--ghost {
  background: rgba(255, 255, 255, 0.82);
}

.market-banner__notes--hero {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.market-banner__notes--hero span {
  min-height: auto;
  padding: 14px 14px 12px;
  border-radius: 14px;
  border-top: 2px solid rgba(243, 190, 37, 0.8);
  background: rgba(255, 255, 255, 0.74);
  line-height: 1.6;
}

.market-banner__stats--hero {
  gap: 12px;
}

.market-metric {
  border-radius: 18px;
  border-color: rgba(17, 19, 34, 0.08);
  box-shadow: none;
}

.market-metric::before {
  display: none;
}

.market-metric__index {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.52);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

@media (max-width: 1080px) {
  .market-banner__inner--hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .market-banner__inner--hero {
    padding: 22px;
  }

  .market-banner__notes--hero {
    grid-template-columns: 1fr;
  }
}
</style>
