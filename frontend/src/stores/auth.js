import { defineStore } from 'pinia'

function readUserInfo() {
  try {
    const raw = window.localStorage.getItem('user_info')
    return raw ? JSON.parse(raw) : null
  } catch (error) {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: window.localStorage.getItem('user_token') || '',
    userInfo: readUserInfo()
  }),
  actions: {
    setLogin(payload) {
      const normalized = payload?.data ?? payload
      const token = normalized?.token || normalized?.jwt
      if (!token) {
        throw new Error('登录返回中缺少 token')
      }

      const userInfo = normalized.userInfo || {
        userId: normalized.userId,
        nickname: normalized.nickname,
        avatarUrl: normalized.avatarUrl,
        phone: normalized.phone,
        email: normalized.email,
        userType: normalized.userType,
        roles: normalized.roles || [],
        developerStatus: normalized.developerStatus,
        idVerifyStatus: normalized.idVerifyStatus,
        skillAuditStatus: normalized.skillAuditStatus,
        developerRoleType: normalized.developerRoleType,
        skillTags: normalized.skillTags,
        redirectPath: normalized.redirectPath
      }

      this.token = token
      this.userInfo = userInfo
      window.localStorage.setItem('user_token', token)
      window.localStorage.setItem('user_info', JSON.stringify(userInfo))
    },
    updateToken(token) {
      if (!token) {
        return
      }
      this.token = token
      window.localStorage.setItem('user_token', token)
    },
    logout() {
      this.token = ''
      this.userInfo = null
      window.localStorage.removeItem('user_token')
      window.localStorage.removeItem('user_info')
    }
  }
})
