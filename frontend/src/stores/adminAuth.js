import { defineStore } from 'pinia'

export const useAdminAuthStore = defineStore('adminAuth', {
  state: () => ({
    token: window.localStorage.getItem('admin_token') || '',
    adminInfo: window.localStorage.getItem('admin_info')
      ? JSON.parse(window.localStorage.getItem('admin_info'))
      : null
  }),
  actions: {
    setLogin(payload) {
      const normalized = payload?.data ?? payload
      if (!normalized?.token) {
        throw new Error('管理员登录返回中缺少 token')
      }
      this.token = normalized.token
      this.adminInfo = normalized
      window.localStorage.setItem('admin_token', normalized.token)
      window.localStorage.setItem('admin_info', JSON.stringify(normalized))
    },
    logout() {
      this.token = ''
      this.adminInfo = null
      window.localStorage.removeItem('admin_token')
      window.localStorage.removeItem('admin_info')
    }
  }
})
