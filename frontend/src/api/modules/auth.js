import http from '@/api/http'

export function login(data) {
  return http.post('/api/auth/login', data)
}

export function register(data) {
  return http.post('/api/auth/register', data)
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

export function submitDeveloperSkillTags(data) {
  return http.post('/api/developer/profile/skill-tags', data)
}
