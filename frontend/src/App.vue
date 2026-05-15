<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import LoginModal from '@/components/home/LoginModal.vue'
import AdminLoginModal from '@/components/admin/AdminLoginModal.vue'
import {
  AUTH_EXPIRED_EVENT,
  clearPendingAuthPrompt,
  consumePendingAuthPrompt
} from '@/utils/authEvents'

const userLoginVisible = ref(false)
const userLoginForced = ref(false)
const adminLoginVisible = ref(false)
const userLoginMessage = ref('')
const adminLoginMessage = ref('')
const userLoginRedirect = ref('')
const adminLoginRedirect = ref('')

function openAuthPrompt(detail = {}) {
  clearPendingAuthPrompt()

  if (detail.scope === 'admin') {
    adminLoginMessage.value = detail.message || '管理员登录状态已失效，请重新登录。'
    adminLoginRedirect.value = detail.redirectPath || ''
    adminLoginVisible.value = true
    return
  }

  userLoginForced.value = Boolean(detail.forceLogin)
  userLoginMessage.value = detail.message || '登录状态已失效，请重新登录后继续。'
  userLoginRedirect.value = detail.redirectPath || ''
  userLoginVisible.value = true
}

function handleAuthExpired(event) {
  openAuthPrompt(event.detail || {})
}

onMounted(() => {
  window.addEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)

  const pendingPrompt = consumePendingAuthPrompt()
  if (pendingPrompt) {
    openAuthPrompt(pendingPrompt)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)
})
</script>

<template>
  <router-view />

  <LoginModal
    v-model="userLoginVisible"
    :force-login="userLoginForced"
    :allow-register="true"
    :message="userLoginMessage"
    :success-redirect="userLoginRedirect"
  />

  <AdminLoginModal
    v-model="adminLoginVisible"
    :message="adminLoginMessage"
    :success-redirect="adminLoginRedirect"
  />
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Fraunces:opsz,wght@9..144,600;9..144,700&family=IBM+Plex+Sans:wght@400;500;600;700&display=swap');
</style>
