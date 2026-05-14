import http from '@/api/http'

export function createOrder(data) {
  return http.post('/api/client/orders', data)
}

export function payOrder(orderId) {
  return http.post(`/api/client/orders/${orderId}/pay`, { payChannel: 'SIMULATED' })
}

export function acceptOrder(orderId, remark) {
  return http.post(`/api/client/orders/${orderId}/accept`, { remark })
}

export function rejectOrder(orderId, reason) {
  return http.post(`/api/client/orders/${orderId}/reject`, { reason })
}

export function fetchClientOrders() {
  return http.get('/api/client/orders')
}

export function fetchClientOrderDetail(orderId) {
  return http.get(`/api/client/orders/${orderId}`)
}

export function fetchDeveloperOrders() {
  return http.get('/api/developer/orders')
}

export function startDeveloperOrder(orderId, remark) {
  return http.post(`/api/developer/orders/${orderId}/start`, { remark })
}

export function submitDeveloperOrder(orderId, data) {
  return http.post(`/api/developer/orders/${orderId}/submit`, data)
}

export function fetchDeveloperOrderDetail(orderId) {
  return http.get(`/api/developer/orders/${orderId}`)
}
