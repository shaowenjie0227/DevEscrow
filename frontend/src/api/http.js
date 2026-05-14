import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const isAdminRequest = config.url?.startsWith('/api/admin/')
  const tokenKey = isAdminRequest ? 'admin_token' : 'user_token'
  const token = window.localStorage.getItem(tokenKey)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code !== 'undefined' && body.code !== 0) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export default http
