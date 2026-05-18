import http from '@/api/http'

export function login(data) {
  return http.post('/api/auth/login', data)
}

export function sendEmailLoginCode(data) {
  return http.post('/api/auth/email-code/send', data)
}

export function loginByEmailCode(data) {
  return http.post('/api/auth/email-code/login', data)
}

export function register(data) {
  return http.post('/api/auth/register', data)
}

export function createQrLogin() {
  return http.post('/api/auth/qr/create')
}

export function fetchCurrentUser() {
  return http.get('/api/auth/me')
}

export function logout() {
  return http.post('/api/auth/logout')
}

export function getDeveloperProfile() {
  return http.get('/api/developer/profile')
}

export function updateBasicProfile(data) {
  return http.put('/api/developer/profile/basic', data)
}

export function applyDeveloperProfile(data) {
  return http.post('/api/developer/profile/apply', data)
}

export function uploadUserImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/api/uploads/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
