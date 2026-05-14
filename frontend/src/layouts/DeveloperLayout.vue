<template>
  <PortalLayout
    theme="developer"
    badge="DV"
    eyebrow="Developer Workspace"
    title="开发者工作台"
    subtitle="聚焦可报价需求、订单推进与可交付节点，让接单更顺手。"
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
  { to: '/developer/home', label: '总览', caption: '查看线索池、报价和订单体量', short: '01' },
  { to: '/developer/market', label: '需求市场', caption: '筛选合适项目并快速提交方案', short: '02' },
  { to: '/developer/quotes', label: '报价记录', caption: '跟进已投方案与中标状态', short: '03' },
  { to: '/developer/orders', label: '订单执行', caption: '开始开发、提交交付与同步进度', short: '04' },
  { to: '/developer/disputes', label: '纠纷中心', caption: '针对异常协作及时留痕处理', short: '05' }
]

const accountName = computed(() => {
  return authStore.userInfo?.nickname || authStore.userInfo?.phone || authStore.userInfo?.userNo || '开发者账号'
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
