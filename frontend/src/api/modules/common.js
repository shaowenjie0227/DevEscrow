import http from '@/api/http'

export function fetchCurrentSkillTags() {
  return http.get('/api/skill-tags')
}
