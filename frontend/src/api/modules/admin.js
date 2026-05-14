import http from '@/api/http'

export function adminLogin(data) {
  return http.post('/api/admin/auth/login', data)
}

export function fetchAdminUsers(params) {
  return http.get('/api/admin/users', { params })
}

export function fetchAdminDemands(params) {
  return http.get('/api/admin/demands', { params })
}

export function approveDemand(demandId, data) {
  return http.post(`/api/admin/demands/${demandId}/approve`, data)
}

export function rejectDemand(demandId, data) {
  return http.post(`/api/admin/demands/${demandId}/reject`, data)
}

export function fetchAdminOrders(params) {
  return http.get('/api/admin/orders', { params })
}

export function fetchAdminOrderDetail(orderId) {
  return http.get(`/api/admin/orders/${orderId}`)
}

export function fetchAdminDisputes() {
  return http.get('/api/admin/disputes')
}

export function resolveDispute(disputeId, data) {
  return http.post(`/api/admin/disputes/${disputeId}/resolve`, data)
}
