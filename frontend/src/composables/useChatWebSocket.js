import { onBeforeUnmount, ref } from 'vue'
import { API_BASE_URL } from '@/api/http'

function toWebSocketUrl(url) {
  if (url.startsWith('https://')) {
    return url.replace('https://', 'wss://')
  }
  return url.replace('http://', 'ws://')
}

export function useChatWebSocket(options = {}) {
  const socketState = ref('idle')
  let socket = null

  function disconnect() {
    if (socket) {
      const currentSocket = socket
      socket = null
      currentSocket.close()
    }
    socketState.value = 'closed'
    options.onStateChange?.(socketState.value)
  }

  function connect(token) {
    if (!token) {
      return
    }

    disconnect()
    socketState.value = 'connecting'
    options.onStateChange?.(socketState.value)

    const wsUrl = toWebSocketUrl(`${API_BASE_URL}/ws/chat?token=${encodeURIComponent(token)}`)
    socket = new WebSocket(wsUrl)

    socket.onopen = () => {
      socketState.value = 'open'
      options.onStateChange?.(socketState.value)
    }

    socket.onmessage = (event) => {
      options.onMessage?.(event)
    }

    socket.onerror = () => {
      socketState.value = 'error'
      options.onStateChange?.(socketState.value)
    }

    socket.onclose = () => {
      socket = null
      socketState.value = 'closed'
      options.onStateChange?.(socketState.value)
    }
  }

  onBeforeUnmount(() => {
    disconnect()
  })

  return {
    connect,
    disconnect,
    socketState
  }
}
