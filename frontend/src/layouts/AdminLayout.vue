<template>
  <PortalLayout
    theme="admin"
    badge="AD"
    eyebrow="Operations Console"
    title="平台运营后台"
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
  { to: '/admin/dashboard', label: '总览', caption: '查看平台核心经营与审核指标', short: '01' },
  { to: '/admin/banners', label: '首页轮播', caption: '管理活动宣传位与首页公告', short: '02' },
  { to: '/admin/resources', label: '资源分享', caption: '发布模板、资料、工具与公告资源', short: '03' },
  { to: '/admin/skill-tags', label: '技术栈', caption: '管理开发者可选择的技术栈标签', short: '04' },
  { to: '/admin/categories', label: '需求分类', caption: '管理用户发布需求时可选分类', short: '05' },
  { to: '/admin/knowledge-bases', label: '知识库', caption: '维护技术介绍与科普内容', short: '06' },
  { to: '/admin/users', label: '用户管理', caption: '查看账号、身份与开发者审核状态', short: '07' },
  { to: '/admin/demands', label: '需求审核', caption: '处理用户发布需求与风控备注', short: '08' },
  { to: '/admin/orders', label: '订单管理', caption: '跟进托管、履约与阶段交付进度', short: '09' },
  { to: '/admin/disputes', label: '纠纷处理', caption: '处理争议、裁决与异常协作情况', short: '10' }
]

const accountName = computed(() => {
  return adminAuthStore.adminInfo?.realName || adminAuthStore.adminInfo?.username || '管理员'
})

function handleLogout() {
  adminAuthStore.logout()
  router.push('/admin/login')
}
</script>
