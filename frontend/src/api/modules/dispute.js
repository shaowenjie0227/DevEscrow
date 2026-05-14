import http from '@/api/http'

export function fetchClientDisputes() {
  return http.get('/api/client/disputes')
}

export function createClientDispute(data) {
  return http.post('/api/client/disputes', data)
}

export function fetchDeveloperDisputes() {
  return http.get('/api/developer/disputes')
}

export function createDeveloperDispute(data) {
  return http.post('/api/developer/disputes', data)
}
