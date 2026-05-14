<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import DemandCard from './DemandCard.vue'
import type { DemandOrderItem } from '@/types/home'

const props = defineProps<{
  items: DemandOrderItem[]
}>()

const route = useRoute()

const filteredItems = computed(() => {
  const keyword = String(route.query.keyword || '').trim().toLowerCase()
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
</script>

<template>
  <section class="market-feed-card">
    <div class="market-feed-head">
      <div>
        <h2>像逛闲鱼一样逛需求订单</h2>
        <p>左边看封面，右边直接扫标题、类型和介绍，合适的再点进去看细节。</p>
      </div>
      <span class="market-feed-badge">当前共 {{ filteredItems.length }} 条需求</span>
    </div>

    <div class="market-list">
      <DemandCard v-for="item in filteredItems" :key="item.id" :item="item" />
    </div>
  </section>
</template>
