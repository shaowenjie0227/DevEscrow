import http from '@/api/http'

export function fetchMyConversations() {
  return http.get('/api/chat/conversations')
}

export function fetchConversationMessages(params) {
  return http.get('/api/chat/messages', { params })
}

export function sendChatMessage(data) {
  return http.post('/api/chat/messages', data)
}

export function recallChatMessage(messageId) {
  return http.post(`/api/chat/messages/${messageId}/recall`)
}

export function uploadChatAttachment(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/api/chat/uploads/attachments', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function fetchAdminChatConversations(params) {
  return http.get('/api/admin/chats/conversations', { params })
}

export function fetchAdminChatMessages(params) {
  return http.get('/api/admin/chats/messages', { params })
}
