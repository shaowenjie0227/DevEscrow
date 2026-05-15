import http from '@/api/http'

export function adminLogin(data) {
  return http.post('/api/admin/auth/login', data)
}

export function fetchAdminUsers(params) {
  return http.get('/api/admin/users', { params })
}

export function auditAdminDeveloper(userId, data) {
  return http.post(`/api/admin/users/${userId}/developer-audit`, data)
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

export function fetchAdminDemandCategories() {
  return http.get('/api/admin/demand-categories')
}

export function createAdminDemandCategory(data) {
  return http.post('/api/admin/demand-categories', data)
}

export function updateAdminDemandCategory(categoryId, data) {
  return http.put(`/api/admin/demand-categories/${categoryId}`, data)
}

export function toggleAdminDemandCategoryStatus(categoryId, data) {
  return http.post(`/api/admin/demand-categories/${categoryId}/status`, data)
}

export function deleteAdminDemandCategory(categoryId) {
  return http.delete(`/api/admin/demand-categories/${categoryId}`)
}

export function fetchAdminBanners(params) {
  return http.get('/api/admin/banners', { params })
}

export function createAdminBanner(data) {
  return http.post('/api/admin/banners', data)
}

export function updateAdminBanner(bannerId, data) {
  return http.put(`/api/admin/banners/${bannerId}`, data)
}

export function toggleAdminBannerStatus(bannerId, data) {
  return http.post(`/api/admin/banners/${bannerId}/status`, data)
}

export function deleteAdminBanner(bannerId) {
  return http.delete(`/api/admin/banners/${bannerId}`)
}

export function fetchAdminSkillTags() {
  return http.get('/api/admin/skill-tags')
}

export function createAdminSkillTag(data) {
  return http.post('/api/admin/skill-tags', data)
}

export function updateAdminSkillTag(tagId, data) {
  return http.put(`/api/admin/skill-tags/${tagId}`, data)
}

export function toggleAdminSkillTagStatus(tagId, data) {
  return http.post(`/api/admin/skill-tags/${tagId}/status`, data)
}

export function deleteAdminSkillTag(tagId) {
  return http.delete(`/api/admin/skill-tags/${tagId}`)
}

export function fetchAdminKnowledgeBases() {
  return http.get('/api/admin/knowledge-bases')
}

export function createAdminKnowledgeBase(data) {
  return http.post('/api/admin/knowledge-bases', data)
}

export function updateAdminKnowledgeBase(id, data) {
  return http.put(`/api/admin/knowledge-bases/${id}`, data)
}

export function toggleAdminKnowledgeBaseStatus(id, data) {
  return http.post(`/api/admin/knowledge-bases/${id}/status`, data)
}

export function deleteAdminKnowledgeBase(id) {
  return http.delete(`/api/admin/knowledge-bases/${id}`)
}

export function fetchAdminResources(params) {
  return http.get('/api/admin/resources', { params })
}

export function createAdminResource(data) {
  return http.post('/api/admin/resources', data)
}

export function updateAdminResource(id, data) {
  return http.put(`/api/admin/resources/${id}`, data)
}

export function toggleAdminResourceStatus(id, data) {
  return http.post(`/api/admin/resources/${id}/status`, data)
}

export function deleteAdminResource(id) {
  return http.delete(`/api/admin/resources/${id}`)
}

export function uploadAdminImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/api/admin/uploads/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
