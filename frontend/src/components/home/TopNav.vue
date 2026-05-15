<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, Plus, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import LoginModal from './LoginModal.vue'
import { logout as logoutRequest } from '@/api/modules/auth'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loginVisible = ref(false)

const navItems = [
  { label: '首页', to: '/' },
  { label: '接单大厅', to: '/market' },
  { label: '资源分享', to: '/resources' },
  { label: '技术文章', to: '/articles' },
  { label: '交流社区', to: '/community' },
  { label: '技术路线图', to: '/roadmap' },
  { label: '推荐课程', to: '/courses' },
  { label: '知识库', to: '/knowledge-base' }
]

const displayName = computed(() => authStore.userInfo?.nickname || authStore.userInfo?.phone || '登录')
const userRoles = computed(() => authStore.userInfo?.roles || [])
const avatarLetter = computed(() => displayName.value.slice(0, 1).toUpperCase())
const actionLabel = computed(() => (authStore.token ? '进入工作台' : '立即登录'))

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path === path || route.path.startsWith(`${path}/`)
}

function goTo(path: string) {
  router.push(path)
}

function openLogin() {
  loginVisible.value = true
}

function getWorkspaceTarget() {
  if (userRoles.value.includes('DEVELOPER')) return '/developer/home'
  if (authStore.token) return '/me'
  return '/market'
}

function getOrderTarget() {
  if (userRoles.value.includes('CLIENT')) return '/client/orders'
  if (userRoles.value.includes('DEVELOPER')) return '/developer/orders'
  return '/market'
}

function handleLoginAction() {
  if (authStore.token) {
    router.push(getWorkspaceTarget())
    return
  }
  openLogin()
}

function handleOrderAction() {
  if (authStore.token) {
    router.push(getOrderTarget())
    return
  }
  openLogin()
}

async function handleLogout() {
  let remoteLogoutFailed = false

  try {
    if (authStore.token) {
      await logoutRequest()
    }
  } catch {
    remoteLogoutFailed = true
  } finally {
    authStore.logout()
    await router.push('/market')
  }

  if (remoteLogoutFailed) {
    ElMessage.warning('已退出当前登录，但服务端会话注销失败')
    return
  }

  ElMessage.success('已退出登录')
}

async function handlePersonalCommand(command: string) {
  if (command === 'admin') {
    router.push('/admin')
    return
  }

  if (command === 'me') {
    router.push('/me')
    return
  }

  if (command === 'logout') {
    await handleLogout()
  }
}
</script>

<template>
  <header class="market-nav">
    <div class="market-container">
      <div class="market-nav__inner">
        <div class="market-nav__brand-area">
          <button class="market-brand" type="button" @click="goTo('/')">
            <span class="market-brand__icon">DE</span>
            <span class="market-brand__text">
              <span class="market-brand__title">DevEscrow</span>
              <span class="market-brand__sub">程序员需求担保协作市场</span>
            </span>
          </button>

          <span class="market-nav__pill">
            <span class="market-nav__pill-dot" />
            Escrow First
          </span>
        </div>

        <nav class="market-nav__links" aria-label="主导航">
          <button
            v-for="item in navItems"
            :key="item.to"
            class="market-nav__link"
            :class="{ 'market-nav__link--active': isActive(item.to) }"
            type="button"
            @click="goTo(item.to)"
          >
            {{ item.label }}
          </button>
        </nav>

        <div class="market-nav__actions">
          <button class="market-nav__action market-nav__action--primary" type="button" @click="goTo('/publish')">
            <el-icon><Plus /></el-icon>
            发布需求
          </button>
          <button class="market-nav__action" type="button" @click="goTo('/market')">接单大厅</button>
          <button class="market-nav__action" type="button" @click="goTo('/admin')">
            <el-icon><Setting /></el-icon>
            后台管理
          </button>
        </div>

        <div class="market-nav__user">
          <button class="market-nav__user-btn" type="button" @click="handleOrderAction">
            <el-icon><Document /></el-icon>
            <span>订单</span>
          </button>

          <el-dropdown trigger="click" @command="handlePersonalCommand">
            <button class="market-nav__user-btn market-nav__user-btn--profile" type="button">
              <span class="market-nav__avatar">{{ avatarLetter }}</span>
              <span>{{ displayName }}</span>
            </button>

            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="me">个人中心</el-dropdown-item>
                <el-dropdown-item command="admin">进入后台管理</el-dropdown-item>
                <el-dropdown-item divided @click="handleLoginAction">
                  {{ actionLabel }}
                </el-dropdown-item>
                <el-dropdown-item v-if="authStore.token" command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <LoginModal v-model="loginVisible" />
  </header>
</template>
