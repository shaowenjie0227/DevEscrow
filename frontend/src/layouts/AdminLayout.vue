<template>
  <PortalLayout
    theme="admin"
    badge="AD"
    eyebrow="Operations Console"
    title="平台运营后台"
    subtitle="聚合审核、订单与纠纷处理，让关键运营动作更聚焦。"
    :nav-items="navItems"
    :account-name="accountName"
    account-meta="平台运营控制台"
    @logout="handleLogout"
  />
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import PortalLayout from '@/components/PortalLayout.vue'
import { useAdminAuthStore } from '@/stores/adminAuth'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()

const navItems = [
  { to: '/admin/dashboard', label: '总览', caption: '掌握平台核心运营指标', short: '01' },
  { to: '/admin/users', label: '用户管理', caption: '查看账户状态与角色结构', short: '02' },
  { to: '/admin/demands', label: '需求审核', caption: '处理待审核需求与备注记录', short: '03' },
  { to: '/admin/orders', label: '订单管理', caption: '跟进托管状态和履约进度', short: '04' },
  { to: '/admin/disputes', label: '纠纷处理', caption: '完成裁决与资金处理动作', short: '05' }
]

const accountName = computed(() => {
  return adminAuthStore.adminInfo?.realName || adminAuthStore.adminInfo?.username || '管理员'
})

function handleLogout() {
  adminAuthStore.logout()
  router.push('/admin/login')
}
</script>
