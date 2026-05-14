<template>
  <PortalLayout
    theme="client"
    badge="CL"
    eyebrow="User Workspace"
    title="用户端工作台"
    subtitle="把需求、报价和托管进度整合进一条清晰的协作流程。"
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
  { to: '/client/home', label: '总览', caption: '查看需求与订单概况', short: '01' },
  { to: '/client/demands', label: '需求管理', caption: '追踪审核、报价与成交', short: '02' },
  { to: '/client/demands/create', label: '发布需求', caption: '快速整理目标、预算与交付范围', short: '03' },
  { to: '/client/orders', label: '订单协作', caption: '处理托管支付、验收与修改', short: '04' },
  { to: '/client/disputes', label: '纠纷中心', caption: '保留证据并发起平台仲裁', short: '05' }
]

const accountName = computed(() => {
  return authStore.userInfo?.nickname || authStore.userInfo?.phone || authStore.userInfo?.userNo || '用户端账号'
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
