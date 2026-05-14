import http from '@/api/http'

export function createDemand(data) {
  return http.post('/api/client/demands', data)
}

export function fetchMyDemands() {
  return http.get('/api/client/demands')
}

export function fetchDemandQuotes(demandId) {
  return http.get(`/api/client/demands/${demandId}/quotes`)
}

export function fetchMarketDemands() {
  return http.get('/api/developer/demands')
}
