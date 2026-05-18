<template>
  <PortalLayout
    theme="client"
    eyebrow="User Workspace"
    title="用户工作台"
    
    :nav-items="navItems"
    :account-name="accountName"
    :account-avatar="accountAvatar"
    account-meta="用户协作控制台"
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
  { to: '/client/home', label: '总览', caption: '查看需求、报价与订单整体情况', short: '01' },
  { to: '/client/demands', label: '我的需求', caption: '跟进审核、报价与成交状态', short: '02' },
  { to: '/client/demands/create', label: '发布需求', caption: '快速整理目标、预算与交付范围', short: '03' },
  { to: '/client/orders', label: '订单协作', caption: '处理托管支付、验收与阶段变更', short: '04' },
  { to: '/client/disputes', label: '纠纷中心', caption: '提交证据并发起平台介入处理', short: '05' },
  {
    to: '/client/inbox',
    label: '站内信',
    caption: '查看管理员单独发送的提醒、通知与补充说明',
    short: '06',
    badge: inboxUnreadCount.value > 0 ? String(inboxUnreadCount.value) : ''
  },
  {
    to: '/client/messages',
    label: '聊天会话',
    caption: '查看客户与开发者之间的沟通记录',
    short: '07',
    badge: chatUnreadCount.value > 0 ? String(chatUnreadCount.value) : ''
  }
])

const accountName = computed(() => {
  return authStore.userInfo?.nickname || authStore.userInfo?.phone || authStore.userInfo?.userNo || '用户账号'
})

const accountAvatar = computed(() => authStore.userInfo?.avatarUrl || '')

function handleLogout() {
  authStore.logout()
  router.push('/market')
}
</script>
