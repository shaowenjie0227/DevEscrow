<script setup lang="ts">
import { computed } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import type { DevCardItem } from '@/types/home'

const props = defineProps<{
  item: DevCardItem
}>()

const avatarStyle = computed(() => ({
  backgroundImage: `linear-gradient(135deg, rgba(255, 209, 0, 0.92), rgba(17, 17, 17, 0.92))`
}))

const emit = defineEmits<{
  connect: [id: number]
}>()
</script>

<template>
  <article
    class="group break-inside-avoid rounded-[28px] bg-white p-5 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-xl"
  >
    <div :class="['rounded-[24px] bg-gradient-to-br p-5 text-black', item.coverGradient]">
      <div class="flex items-start justify-between gap-3">
        <div class="flex items-center gap-3">
          <div
            class="flex h-14 w-14 items-center justify-center rounded-full text-sm font-semibold text-white shadow-lg"
            :style="avatarStyle"
          >
            {{ item.avatar }}
          </div>
          <div>
            <div class="flex items-center gap-2">
              <h3 class="font-display text-lg font-semibold">{{ item.nickname }}</h3>
              <span class="flex items-center gap-1 rounded-full bg-white/75 px-2 py-1 text-[11px] font-semibold text-black/60">
                <span :class="['h-2 w-2 rounded-full', item.online ? 'bg-emerald-500' : 'bg-slate-400']"></span>
                {{ item.online ? '在线' : '稍后回复' }}
              </span>
            </div>
            <p class="mt-1 text-sm text-black/65">{{ item.city }} · 已完成 {{ item.completedProjectCount }} 单</p>
          </div>
        </div>
        <span class="rounded-full bg-black/8 px-3 py-1 text-xs font-semibold text-black/60">{{ item.replyRate }} 响应</span>
      </div>

      <div class="mt-5 rounded-[22px] bg-white/82 p-4 backdrop-blur-sm">
        <p class="text-xs font-semibold uppercase tracking-[0.22em] text-black/45">Featured Service</p>
        <h4 class="mt-2 font-display text-xl font-semibold leading-8 text-black">{{ item.serviceTitle }}</h4>
        <p class="mt-3 text-sm leading-7 text-black/65">{{ item.serviceDescription }}</p>
      </div>
    </div>

    <div class="mt-4 flex flex-wrap gap-2">
      <el-tag
        v-for="tag in item.tags"
        :key="tag.label"
        :type="tag.type || 'warning'"
        effect="plain"
        class="!border-0 !bg-black/5 !text-black/65 transition-all duration-300 group-hover:!bg-brand/20 group-hover:!text-black"
      >
        #{{ tag.label }}
      </el-tag>
    </div>

    <p class="mt-4 rounded-[20px] bg-[#f8f9fc] px-4 py-3 text-sm leading-7 text-black/65">
      {{ item.projectHighlight }}
    </p>

    <div class="mt-5 flex items-end justify-between gap-4">
      <div class="space-y-1">
        <p class="text-sm text-black/50">起步价</p>
        <p class="font-display text-2xl font-semibold text-black">￥{{ item.price.toLocaleString() }}</p>
        <p class="text-sm text-black/55">完成周期：{{ item.duration }}</p>
      </div>
      <button
        class="inline-flex h-11 translate-y-2 items-center gap-2 rounded-full bg-black px-5 text-sm font-semibold text-white opacity-0 transition-all duration-300 hover:bg-brand hover:text-black group-hover:translate-y-0 group-hover:opacity-100"
        type="button"
        @click="emit('connect', item.id)"
      >
        <el-icon><ChatDotRound /></el-icon>
        立即沟通
      </button>
    </div>
  </article>
</template>
