import http from '@/api/http'

export function fetchCommunityPosts(params) {
  return http.get('/api/community/posts', { params })
}

export function createCommunityPost(data) {
  return http.post('/api/community/posts', data)
}

export function likeCommunityPost(postId) {
  return http.post(`/api/community/posts/${postId}/like`)
}

export function favoriteCommunityPost(postId) {
  return http.post(`/api/community/posts/${postId}/favorite`)
}

export function fetchCommunityReplies(postId) {
  return http.get(`/api/community/posts/${postId}/replies`)
}

export function createCommunityReply(postId, data) {
  return http.post(`/api/community/posts/${postId}/replies`, data)
}
