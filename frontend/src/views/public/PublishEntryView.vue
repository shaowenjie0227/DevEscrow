<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { emitAuthExpired } from '@/utils/authEvents'

const router = useRouter()
const authStore = useAuthStore()
const publishTarget = '/client/demands/create'

const targetLabel = computed(() => (authStore.token ? '进入需求发布页' : '登录后发布需求'))

function handleEntry() {
  if (authStore.token) {
    router.push(publishTarget)
    return
  }

  emitAuthExpired({
    scope: 'user',
    message: '登录后即可发布需求。',
    redirectPath: publishTarget
  })
}
</script>

<template>
  <div class="min-h-screen bg-brand-canvas px-4 py-16 font-body sm:px-6 lg:px-8">
    <div class="mx-auto max-w-3xl rounded-[32px] bg-white p-8 shadow-float sm:p-12">
      <p class="text-sm font-semibold uppercase tracking-[0.24em] text-black/45">Publish Demand</p>
      <h1 class="mt-4 font-display text-4xl font-semibold text-black">把需求快速送进你现有的后台系统</h1>
      <p class="mt-4 text-base leading-8 text-black/65">
        当前首页的“立即发布需求”入口已经接到这个过渡页。这里继续复用现有的需求创建与登录逻辑，
        会根据你的登录状态跳转到对应页面。
      </p>

      <div class="mt-8 grid gap-4 rounded-[28px] bg-[#f8f9fc] p-6 text-sm leading-7 text-black/65">
        <p>未登录：直接唤起全局登录弹窗，登录后继续发布流程。</p>
        <p>已登录：直接进入 `/client/demands/create`，复用后台已有发布能力。</p>
        <p>后续如果你想做消费级发布弹窗，我们可以再把这里替换成真正的前台发布表单。</p>
      </div>

      <div class="mt-8 flex flex-wrap gap-3">
        <button
          class="inline-flex h-12 items-center justify-center rounded-full bg-brand px-6 text-sm font-semibold text-brand-ink transition-all duration-300 hover:-translate-y-1 hover:shadow-glow"
          type="button"
          @click="handleEntry"
        >
          {{ targetLabel }}
        </button>
        <button
          class="inline-flex h-12 items-center justify-center rounded-full border border-black/10 bg-white px-6 text-sm font-semibold text-black transition-all duration-300 hover:-translate-y-1 hover:shadow-float"
          type="button"
          @click="router.push('/')"
        >
          返回首页
        </button>
      </div>
    </div>
  </div>
</template>
