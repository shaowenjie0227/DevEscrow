<script setup lang="ts">
import type { TechCategoryItem } from '@/types/home'

defineProps<{
  categories: TechCategoryItem[]
}>()
</script>

<template>
  <section class="market-subnav market-subnav--taxonomy">
    <div class="market-container market-taxonomy">
      <div class="market-taxonomy__head">
        <span>常见筛选</span>
        <p>把热门技术栈放前面，帮助用户更快扫一眼市场结构。</p>
      </div>

      <div class="market-chips market-chips--taxonomy">
        <button v-for="item in categories" :key="item.label" class="market-chip market-chip--taxonomy" type="button">
          <span>{{ item.label }}</span>
          <span class="market-chip__count">{{ item.count }}</span>
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.market-subnav--taxonomy {
  padding-top: 18px;
}

.market-taxonomy {
  display: grid;
  gap: 12px;
}

.market-taxonomy__head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.market-taxonomy__head span {
  color: rgba(17, 19, 34, 0.82);
  font-size: 15px;
  font-weight: 700;
}

.market-taxonomy__head p {
  color: rgba(17, 19, 34, 0.54);
  font-size: 13px;
  line-height: 1.7;
}

.market-chips--taxonomy {
  gap: 8px;
}

.market-chip--taxonomy {
  min-height: 38px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.72);
}

@media (max-width: 720px) {
  .market-taxonomy__head {
    align-items: start;
  }
}
</style>
