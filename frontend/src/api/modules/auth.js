import http from '@/api/http'

export function login(data) {
  return http.post('/api/auth/login', data)
}

export function register(data) {
  return http.post('/api/auth/register', data)
}
