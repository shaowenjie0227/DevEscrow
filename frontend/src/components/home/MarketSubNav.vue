<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const items = [
  { label: '首页', to: '/market' },
  { label: '资源分享', to: '/resources' },
  { label: '技术文章', to: '/articles' },
  { label: '交流社区', to: '/community' },
  { label: '技术路线图', to: '/roadmap' },
  { label: '推荐课程', to: '/courses' },
  { label: '知识库', to: '/knowledge-base' }
]

const accountName = computed(() => authStore.userInfo?.nickname || authStore.userInfo?.phone || '个人中心')
</script>

<template>
  <section class="market-subnav market-subnav--line">
    <div class="market-container market-subnav__inner">
      <nav class="market-subnav__links">
        <button
          v-for="item in items"
          :key="item.label"
          class="market-subnav__link"
          type="button"
          @click="router.push(item.to)"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="market-subnav__profile">
        <button class="market-subnav__profile-btn" type="button" @click="router.push('/me')">
          <span class="market-subnav__avatar">{{ accountName.slice(0, 1) }}</span>
          <span>个人中心</span>
        </button>
      </div>
    </div>
  </section>
</template>
