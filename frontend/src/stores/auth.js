import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: window.localStorage.getItem('user_token') || '',
    userInfo: window.localStorage.getItem('user_info')
      ? JSON.parse(window.localStorage.getItem('user_info'))
      : null
  }),
  actions: {
    setLogin(payload) {
      const normalized = payload?.data ?? payload
      if (!normalized?.token) {
        throw new Error('用户登录返回中缺少 token')
      }
      this.token = normalized.token
      this.userInfo = normalized.userInfo || normalized
      window.localStorage.setItem('user_token', normalized.token)
      window.localStorage.setItem('user_info', JSON.stringify(this.userInfo))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      window.localStorage.removeItem('user_token')
      window.localStorage.removeItem('user_info')
    }
  }
})
