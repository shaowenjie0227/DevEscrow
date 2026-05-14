<script setup lang="ts">
import type { TechCategoryItem } from '@/types/home'

defineProps<{
  categories: TechCategoryItem[]
}>()
</script>

<template>
  <section class="market-subnav">
    <div class="market-container">
      <div class="market-chips">
        <button v-for="item in categories" :key="item.label" class="market-chip" type="button">
          <span>{{ item.label }}</span>
          <span class="market-chip__count">{{ item.count }}</span>
        </button>
      </div>
    </div>
  </section>
</template>
