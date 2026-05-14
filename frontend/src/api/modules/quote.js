import http from '@/api/http'

export function createQuote(data) {
  return http.post('/api/developer/quotes', data)
}

export function fetchMyQuotes() {
  return http.get('/api/developer/quotes/my')
}
