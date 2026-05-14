<template>
  <PortalLayout
    theme="developer"
    badge="DV"
    eyebrow="Developer Workspace"
    title="开发者工作台"
    subtitle="专注可报价需求、订单推进与交付节奏，让接单协作更清晰。"
    :nav-items="navItems"
    :account-name="accountName"
    account-meta="开发协作控制台"
    @logout="handleLogout"
  />
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import PortalLayout from '@/components/PortalLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const navItems = [
  { to: '/developer/home', label: '总览', caption: '查看线索池、报价量与接单状态', short: '01' },
  { to: '/developer/profile', label: '开发者资料', caption: '维护实名、技术栈与能力标签', short: '02' },
  { to: '/developer/market', label: '需求市场', caption: '筛选合适项目并快速提交方案', short: '03' },
  { to: '/developer/quotes', label: '报价记录', caption: '跟进已投方案与中标情况', short: '04' },
  { to: '/developer/orders', label: '订单执行', caption: '推进开发、提交交付与同步进度', short: '05' },
  { to: '/developer/disputes', label: '纠纷中心', caption: '处理异常协作与平台仲裁', short: '06' }
]

const accountName = computed(() => {
  return authStore.userInfo?.nickname || authStore.userInfo?.phone || authStore.userInfo?.userNo || '开发者账号'
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
