<template>
  <PortalLayout
    theme="developer"
    badge="DV"
    eyebrow="Developer Workspace"
    title="开发者工作台"
    subtitle="聚焦可报价需求、订单推进、站内信提醒、聊天沟通与交付节奏，让接单协作更清晰。"
    :nav-items="navItems"
    :account-name="accountName"
    :account-avatar="accountAvatar"
    account-meta="开发协作控制台"
    profile-to="/me"
    home-to="/"
    home-label="返回首页"
    @logout="handleLogout"
  />
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import PortalLayout from '@/components/PortalLayout.vue'
import { useChatUnreadCount } from '@/composables/useChatUnreadCount'
import { useInboxUnreadCount } from '@/composables/useInboxUnreadCount'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const { unreadCount: chatUnreadCount } = useChatUnreadCount(authStore)
const { unreadCount: inboxUnreadCount } = useInboxUnreadCount(authStore)

const navItems = computed(() => [
  { to: '/developer/home', label: '总览', caption: '查看线索池、报价量与接单状态', short: '01' },
  { to: '/developer/profile', label: '开发者资料', caption: '维护实名、技术栈与能力标签', short: '02' },
  { to: '/developer/market', label: '需求市场', caption: '筛选合适项目并快速提交方案', short: '03' },
  { to: '/developer/quotes', label: '报价记录', caption: '跟进已投方案与中标情况', short: '04' },
  { to: '/developer/orders', label: '订单执行', caption: '推进开发、提交交付与同步进度', short: '05' },
  { to: '/developer/disputes', label: '纠纷中心', caption: '处理异常协作与平台介入', short: '06' },
  {
    to: '/developer/inbox',
    label: '站内信',
    caption: '查看管理员发来的单独通知，未读时会实时提醒',
    short: '07',
    badge: inboxUnreadCount.value > 0 ? String(inboxUnreadCount.value) : ''
  },
  {
    to: '/developer/messages',
    label: '聊天会话',
    caption: '围绕具体需求和客户保持沟通',
    short: '08',
    badge: chatUnreadCount.value > 0 ? String(chatUnreadCount.value) : ''
  }
])

const accountName = computed(() => {
  return authStore.userInfo?.nickname || authStore.userInfo?.phone || authStore.userInfo?.userNo || '开发者账号'
})

const accountAvatar = computed(() => authStore.userInfo?.avatarUrl || '')

function handleLogout() {
  authStore.logout()
  router.push('/market')
}
</script>
