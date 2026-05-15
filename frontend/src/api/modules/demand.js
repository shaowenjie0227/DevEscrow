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

export function fetchPublicMarketDemands() {
  return http.get('/api/market/demands')
}

export function fetchPublicMarketDemandDetail(demandId) {
  return http.get(`/api/market/demands/${demandId}`)
}

export function fetchDemandCategories() {
  return http.get('/api/demand-categories')
}

export function fetchHomeBanners() {
  return http.get('/api/home-banners')
}

export function fetchKnowledgeBases() {
  return http.get('/api/knowledge-bases')
}

export function fetchResources() {
  return http.get('/api/resources')
}
