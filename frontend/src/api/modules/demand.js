import { API_BASE_URL } from '@/api/http'
import http from '@/api/http'

export const AI_DEMAND_DRAFT_TIMEOUT_MS = 60000

export function createDemand(data) {
  return http.post('/api/client/demands', data)
}

export function generateDemandDraft(data) {
  return http.post('/api/client/ai/demand-draft', data, {
    timeout: AI_DEMAND_DRAFT_TIMEOUT_MS
  })
}

export async function streamAiDemandDraft(data, handlers = {}) {
  const {
    signal,
    onStatus,
    onFinal,
    onError
  } = handlers

  const token = typeof window !== 'undefined' ? window.localStorage.getItem('user_token') || '' : ''
  const response = await fetch(`${API_BASE_URL}/api/client/ai/demand-draft/stream`, {
    method: 'POST',
    headers: {
      Accept: 'text/event-stream',
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(data),
    signal
  })

  if (!response.ok) {
    const message = await resolveStreamErrorResponse(response)
    if (shouldFallbackToSyncDraft(response.status)) {
      onStatus?.({
        stage: '切回兼容模式',
        message: '当前服务端还没有启用流式接口，我先切回普通生成方式继续帮你整理草稿。',
        progress: 24
      })
      const fallbackResponse = await generateDemandDraft(data)
      const fallbackPayload = fallbackResponse?.data || null
      onFinal?.(fallbackPayload)
      return fallbackPayload
    }
    throw new Error(message)
  }

  if (!response.body) {
    throw new Error('AI 流式响应不可用，请稍后重试')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let finalPayload = null

  const dispatchEvent = (rawEventName, rawData) => {
    const eventName = rawEventName || 'message'
    let payload = null

    try {
      payload = rawData ? JSON.parse(rawData) : null
    } catch (error) {
      payload = { message: rawData }
    }

    if (eventName === 'status') {
      onStatus?.(payload || {})
      return
    }

    if (eventName === 'final') {
      finalPayload = payload
      onFinal?.(payload)
      return
    }

    if (eventName === 'error') {
      const message = payload?.message || 'AI 流式生成失败'
      onError?.(message)
      throw new Error(message)
    }
  }

  const consumeSseChunk = (chunk) => {
    const lines = chunk.split(/\r?\n/)
    let eventName = 'message'
    const dataLines = []

    for (const rawLine of lines) {
      const line = rawLine.trimEnd()
      if (!line) continue
      if (line.startsWith(':')) continue
      if (line.startsWith('event:')) {
        eventName = line.slice(6).trim()
        continue
      }
      if (line.startsWith('data:')) {
        dataLines.push(line.slice(5).trim())
      }
    }

    dispatchEvent(eventName, dataLines.join('\n'))
  }

  try {
    while (true) {
      const { value, done } = await reader.read()
      buffer += decoder.decode(value || new Uint8Array(), { stream: !done })

      const parts = buffer.split(/\r?\n\r?\n/)
      buffer = parts.pop() || ''

      for (const part of parts) {
        if (!part.trim()) continue
        consumeSseChunk(part)
      }

      if (done) {
        if (buffer.trim()) {
          consumeSseChunk(buffer)
        }
        break
      }
    }
  } finally {
    reader.releaseLock()
  }

  return finalPayload
}

export function resolveAiDemandDraftErrorMessage(error) {
  const message = error?.message || ''

  if (/timeout of \d+ms exceeded/i.test(message)) {
    return `AI 响应时间较长，已超过 ${Math.round(AI_DEMAND_DRAFT_TIMEOUT_MS / 1000)} 秒，请稍后重试，或把需求拆得更具体一些。`
  }

  return message || 'AI 初稿生成失败'
}

async function resolveStreamErrorResponse(response) {
  try {
    const contentType = response.headers.get('content-type') || ''
    if (contentType.includes('application/json')) {
      const body = await response.json()
      return body?.message || body?.msg || `请求失败（${response.status}）`
    }

    const text = await response.text()
    if (!text) {
      return `请求失败（${response.status}）`
    }

    try {
      const body = JSON.parse(text)
      return body?.message || body?.msg || text
    } catch (error) {
      return text
    }
  } catch (error) {
    return `请求失败（${response.status}）`
  }
}

function shouldFallbackToSyncDraft(status) {
  return [404, 405, 501].includes(Number(status))
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

export function fetchHomeNotices() {
  return http.get('/api/home-notices')
}

export function fetchHomeOverview() {
  return http.get('/api/home/overview')
}

export function fetchHomeNoticeDetail(noticeId) {
  return http.get(`/api/home-notices/${noticeId}`)
}

export function fetchKnowledgeBases() {
  return http.get('/api/knowledge-bases')
}

export function fetchResources() {
  return http.get('/api/resources')
}
