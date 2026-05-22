import axios from 'axios'
import { useAdminAuthStore } from '@/stores/adminAuth'
import { useAuthStore } from '@/stores/auth'
import { emitAuthExpired } from '@/utils/authEvents'

const DEFAULT_API_BASE_URL =
  typeof window !== 'undefined'
    ? (() => {
        const { protocol, hostname, port, origin } = window.location
        const isLocalHost = hostname === 'localhost' || hostname === '127.0.0.1'
        const isFrontendDevPort = port === '5173' || port === '4173'
        if (isLocalHost && isFrontendDevPort) {
          return `${protocol}//${hostname}:8080`
        }
        return origin
      })()
    : ''

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
})

const AUTH_PROMPT_THROTTLE_MS = 800
const lastAuthPromptAt = {
  admin: 0,
  user: 0
}

function isAdminRequest(url = '') {
  return url.startsWith('/api/admin/')
}

function isLoginRequest(url = '') {
  return (
    url === '/api/auth/login' ||
    url === '/api/auth/email-code/login' ||
    url === '/api/admin/auth/login'
  )
}

function shouldHandleAuthFailure(code, url = '') {
  return code === 401 && !isLoginRequest(url)
}

function handleAuthFailure(url = '', message = '登录状态已失效，请重新登录') {
  const scope = isAdminRequest(url) ? 'admin' : 'user'

  if (scope === 'admin') {
    useAdminAuthStore().logout()
  } else {
    useAuthStore().logout()
  }

  const now = Date.now()
  if (now - lastAuthPromptAt[scope] < AUTH_PROMPT_THROTTLE_MS) {
    return
  }

  lastAuthPromptAt[scope] = now
  emitAuthExpired({ scope, message })
}

http.interceptors.request.use((config) => {
  const requestUrl = config.url || ''
  if (isLoginRequest(requestUrl)) {
    return config
  }

  const tokenKey = isAdminRequest(requestUrl) ? 'admin_token' : 'user_token'
  const token = window.localStorage.getItem(tokenKey)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const refreshToken = response.headers?.['x-refresh-token']
    if (refreshToken && !isAdminRequest(response.config?.url || '')) {
      useAuthStore().updateToken(refreshToken)
    }

    const body = response.data
    if (body && typeof body.code !== 'undefined' && body.code !== 0) {
      const requestUrl = response.config?.url || ''
      const message = body.message || body.msg || '请求失败'

      if (shouldHandleAuthFailure(body.code, requestUrl)) {
        handleAuthFailure(requestUrl, message)
      }

      return Promise.reject(new Error(message))
    }
    return body
  },
  (error) => {
    const requestUrl = error.config?.url || ''
    const responseCode = error.response?.data?.code
    const message = error.response?.data?.message || error.response?.data?.msg || error.message || '请求失败'

    if (shouldHandleAuthFailure(responseCode, requestUrl)) {
      handleAuthFailure(requestUrl, message)
    }

    return Promise.reject(new Error(message))
  }
)

export default http
