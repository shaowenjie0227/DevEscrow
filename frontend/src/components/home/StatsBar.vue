<script setup lang="ts">
import type { StatItem } from '@/types/home'

defineProps<{
  items: StatItem[]
}>()
</script>

<template>
  <section class="market-stats">
    <div class="market-container market-stats__grid">
      <article v-for="(item, index) in items" :key="item.label" class="market-stats__card">
        <span class="market-stats__index">{{ String(index + 1).padStart(2, '0') }}</span>
        <div>
          <p class="market-stats__label">{{ item.label }}</p>
          <h2 class="market-stats__value">{{ item.value }}</h2>
          <p class="market-stats__note">{{ item.note }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.market-stats {
  padding-top: 18px;
}

.market-stats__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.market-stats__card {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 14px;
  align-items: start;
  padding: 20px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 16px 32px rgba(17, 19, 34, 0.04);
}

.market-stats__index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(17, 19, 34, 0.08);
  color: rgba(17, 19, 34, 0.68);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.market-stats__label {
  color: rgba(17, 19, 34, 0.54);
  font-size: 13px;
  font-weight: 600;
}

.market-stats__value {
  margin-top: 8px;
  font-family: var(--font-display);
  font-size: clamp(28px, 3vw, 38px);
  line-height: 1;
}

.market-stats__note {
  margin-top: 10px;
  color: rgba(17, 19, 34, 0.62);
  line-height: 1.75;
}

@media (max-width: 1024px) {
  .market-stats__grid {
    grid-template-columns: 1fr;
  }
}
</style>
