import { onBeforeUnmount, ref } from 'vue'
import { API_BASE_URL } from '@/api/http'

function toWebSocketUrl(url) {
  if (url.startsWith('https://')) {
    return url.replace('https://', 'wss://')
  }
  return url.replace('http://', 'ws://')
}

export function useLoginWebSocket(options = {}) {
  const socketState = ref('idle')
  const reconnectAttempts = ref(0)

  let socket = null
  let reconnectTimer = null
  let currentToken = ''
  let manuallyClosed = false

  function clearReconnectTimer() {
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  function closeSocket() {
    if (socket) {
      const currentSocket = socket
      socket = null
      currentSocket.close()
    }
  }

  function scheduleReconnect() {
    clearReconnectTimer()
    const delay = Math.min(1000 * 2 ** reconnectAttempts.value, 8000)
    reconnectTimer = window.setTimeout(() => {
      reconnectAttempts.value += 1
      connect(currentToken)
    }, delay)
  }

  function connect(token) {
    if (!token) {
      return
    }

    currentToken = token
    manuallyClosed = false
    clearReconnectTimer()
    closeSocket()

    socketState.value = 'connecting'
    options.onStateChange?.(socketState.value)

    const wsUrl = toWebSocketUrl(`${API_BASE_URL}/ws/login/${encodeURIComponent(token)}`)
    socket = new WebSocket(wsUrl)

    socket.onopen = () => {
      reconnectAttempts.value = 0
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
      socketState.value = manuallyClosed ? 'closed' : 'reconnecting'
      options.onStateChange?.(socketState.value)

      if (!manuallyClosed && currentToken) {
        scheduleReconnect()
      }
    }
  }

  function disconnect() {
    manuallyClosed = true
    currentToken = ''
    clearReconnectTimer()
    closeSocket()
    socketState.value = 'closed'
    options.onStateChange?.(socketState.value)
  }

  onBeforeUnmount(() => {
    disconnect()
  })

  return {
    connect,
    disconnect,
    reconnectAttempts,
    socketState
  }
}
