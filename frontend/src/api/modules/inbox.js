import http from '@/api/http'

export function fetchInboxMessages() {
  return http.get('/api/inbox/messages')
}

export function fetchInboxUnreadSummary() {
  return http.get('/api/inbox/messages/unread-summary')
}

export function markInboxMessageRead(messageId) {
  return http.post(`/api/inbox/messages/${messageId}/read`)
}

export function markAllInboxMessagesRead() {
  return http.post('/api/inbox/messages/read-all')
}
