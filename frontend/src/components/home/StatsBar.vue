<script setup lang="ts">
import type { StatItem } from '@/types/home'

defineProps<{
  items: StatItem[]
}>()
</script>

<template>
  <section class="mx-auto max-w-7xl px-4 pt-6 sm:px-6 lg:px-8">
    <div class="grid gap-4 md:grid-cols-3">
      <article
        v-for="item in items"
        :key="item.label"
        class="rounded-[28px] bg-white p-6 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-float"
      >
        <p class="text-sm font-medium text-black/55">{{ item.label }}</p>
        <h2 class="mt-3 font-display text-3xl font-semibold text-black">{{ item.value }}</h2>
        <p class="mt-3 text-sm leading-7 text-black/60">{{ item.note }}</p>
      </article>
    </div>
  </section>
</template>
