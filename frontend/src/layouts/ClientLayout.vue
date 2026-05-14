<template>
  <PortalLayout
    theme="client"
    badge="CL"
    eyebrow="User Workspace"
    title="用户工作台"
    subtitle="集中管理需求、报价、订单与纠纷，清楚掌握每一笔合作进度。"
    :nav-items="navItems"
    :account-name="accountName"
    account-meta="用户协作控制台"
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
  { to: '/client/home', label: '总览', caption: '查看需求、报价与订单整体情况', short: '01' },
  { to: '/client/demands', label: '我的需求', caption: '跟进审核、报价与成交状态', short: '02' },
  { to: '/client/demands/create', label: '发布需求', caption: '快速整理目标、预算与交付范围', short: '03' },
  { to: '/client/orders', label: '订单协作', caption: '处理托管支付、验收与阶段变更', short: '04' },
  { to: '/client/disputes', label: '纠纷中心', caption: '提交证据并发起平台仲裁处理', short: '05' }
]

const accountName = computed(() => {
  return authStore.userInfo?.nickname || authStore.userInfo?.phone || authStore.userInfo?.userNo || '用户账号'
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
