<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import Footer from './Footer.vue'
import MarketSubNav from './MarketSubNav.vue'
import TopNav from './TopNav.vue'

const props = defineProps<{
  backTo?: string
  backLabel?: string
}>()

const router = useRouter()

function goBack() {
  router.push(props.backTo || '/market')
}
</script>

<template>
  <div class="market-page">
    <TopNav />
    <MarketSubNav />

    <main class="market-container market-content-page">
      <div class="market-detail__top">
        <button class="market-back" type="button" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          {{ backLabel || '返回接单大厅' }}
        </button>
      </div>

      <slot />
    </main>

    <Footer />
  </div>
</template>
