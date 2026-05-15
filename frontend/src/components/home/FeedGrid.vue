<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DemandCard from './DemandCard.vue'
import type { DemandOrderItem } from '@/types/home'

const props = withDefaults(
  defineProps<{
    items: DemandOrderItem[]
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

const route = useRoute()
const router = useRouter()

const activeKeyword = computed(() => String(route.query.keyword || '').trim())

const filteredItems = computed(() => {
  const keyword = activeKeyword.value.toLowerCase()
  if (!keyword) return props.items

  return props.items.filter((item) => {
    const text = [
      item.title,
      item.category,
      item.summary,
      item.detail,
      item.location,
      item.publisher,
      item.contactHint,
      ...item.tags.map((tag) => tag.label),
      ...item.highlights,
      ...item.deliverables
    ]
      .join(' ')
      .toLowerCase()

    return text.includes(keyword)
  })
})

const sortedItems = computed(() =>
  [...filteredItems.value].sort((left, right) => Number(right.id) - Number(left.id))
)

const visibleItems = computed(() => sortedItems.value.slice(0, props.previewCount))

const badgeText = computed(() => {
  if (Number.isFinite(props.previewCount)) {
    return `展示最新 ${visibleItems.value.length} 条`
  }

  return `${visibleItems.value.length} 条在看板中`
})

function goToMarketHall() {
  router.push('/market')
}
</script>

<template>
  <section class="market-feed-card market-feed-card--board" :class="{ 'market-feed-card--compact': compact }">
    <div class="market-feed-head market-feed-head--board">
      <div class="market-feed-head__copy">
        <span class="market-feed-head__eyebrow">Latest Open Demands</span>
        <h2>最新公开需求</h2>
        <p>先看范围、预算、交付节奏，再决定是不是值得继续沟通。</p>
      </div>

      <div class="market-feed-head__meta">
        <span class="market-feed-badge">{{ badgeText }}</span>
        <span v-if="activeKeyword" class="market-feed-keyword">搜索：{{ activeKeyword }}</span>
        <button
          v-if="showMoreAction"
          class="market-btn market-btn--ghost market-feed-head__action"
          type="button"
          @click="goToMarketHall"
        >
          更多订单需求查看
        </button>
      </div>
    </div>

    <div v-if="visibleItems.length" class="market-list" :class="{ 'market-list--compact': compact }">
      <DemandCard v-for="item in visibleItems" :key="item.id" :item="item" :compact="compact" />
    </div>
    <p v-else class="market-feed-empty">当前没有匹配的订单需求。</p>
  </section>
</template>

<style scoped>
.market-feed-card--board {
  padding: 24px;
  border-radius: 22px;
}

.market-feed-head--board {
  align-items: start;
}

.market-feed-head__copy {
  display: grid;
  gap: 8px;
}

.market-feed-head__eyebrow {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.56);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.market-feed-head__meta {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.market-feed-keyword {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(243, 190, 37, 0.16);
  color: rgba(17, 19, 34, 0.72);
  font-size: 13px;
  font-weight: 600;
}

.market-feed-head__action {
  min-height: 40px;
  padding: 0 18px;
  white-space: nowrap;
}

.market-feed-card--compact {
  padding: 20px;
}

.market-feed-card--compact .market-feed-head {
  margin-bottom: 14px;
}

.market-feed-card--compact .market-feed-head__copy {
  gap: 6px;
}

.market-feed-card--compact :deep(.market-feed-head h2) {
  font-size: 24px;
}

.market-feed-card--compact :deep(.market-feed-head p) {
  font-size: 13px;
}

.market-list--compact {
  gap: 12px;
}

.market-feed-empty {
  margin: 0;
  padding: 24px 0 8px;
  color: rgba(17, 19, 34, 0.58);
  font-size: 14px;
  line-height: 1.8;
}

@media (max-width: 720px) {
  .market-feed-card--board {
    padding: 18px;
  }

  .market-feed-head__meta {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
