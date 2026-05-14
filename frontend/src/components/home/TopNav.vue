<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElDropdown, ElDropdownItem, ElDropdownMenu, ElInput } from 'element-plus'
import { Document, Plus, Search, Setting } from '@element-plus/icons-vue'
import LoginModal from './LoginModal.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const searchValue = ref('')
const loginVisible = ref(false)

const displayName = computed(() => authStore.userInfo?.nickname || authStore.userInfo?.phone || '登录')
const userRoles = computed(() => authStore.userInfo?.roles || [])
const avatarLetter = computed(() => displayName.value.slice(0, 1).toUpperCase())

function goTo(path: string) {
  router.push(path)
}

function openLogin() {
  loginVisible.value = true
}

function handleSearch() {
  router.push({
    path: '/market',
    query: searchValue.value ? { keyword: searchValue.value } : undefined
  })
}

function getWorkspaceTarget() {
  if (userRoles.value.includes('CLIENT')) return '/client/home'
  if (userRoles.value.includes('DEVELOPER')) return '/developer/home'
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

function handlePersonalCommand(command: string) {
  if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'me') {
    router.push('/me')
  }
}
</script>

<template>
  <header class="market-nav">
    <div class="market-container">
      <div class="market-nav__inner">
        <button class="market-brand" type="button" @click="goTo('/market')">
          <span class="market-brand__icon">码</span>
          <span class="market-brand__text">
            <span class="market-brand__title">DevEscrow</span>
            <span class="market-brand__sub">程序员接单大厅</span>
          </span>
        </button>

        <div class="market-search">
          <ElInput
            v-model="searchValue"
            size="large"
            placeholder="搜索需求标题、类型、技术栈、预算"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </ElInput>
        </div>

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
              <span>个人中心</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="me">个人中心</el-dropdown-item>
                <el-dropdown-item command="admin">进入后台管理</el-dropdown-item>
                <el-dropdown-item divided @click="handleLoginAction">
                  {{ authStore.token ? '进入工作台' : '立即登录' }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <LoginModal v-model="loginVisible" />
  </header>
</template>
