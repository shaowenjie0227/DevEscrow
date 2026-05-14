<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuth'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()

onMounted(() => {
  if (adminAuthStore.token) {
    router.replace('/admin/dashboard')
    return
  }
  router.replace('/admin/login')
})
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-brand-canvas p-6 font-body text-black/60">
    正在进入后台管理系统...
  </div>
</template>
