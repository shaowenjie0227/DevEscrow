import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { fetchMyConversations } from '@/api/modules/chat'
import { useChatWebSocket } from '@/composables/useChatWebSocket'

export function useChatUnreadCount(authStore) {
  const unreadCount = ref(0)

  const socket = useChatWebSocket({
    onMessage: async (event) => {
      try {
        const payload = JSON.parse(event.data)
        if (payload?.type !== 'CHAT_MESSAGE') {
          return
        }
        await refreshUnreadCount()
      } catch (error) {
        // Ignore malformed websocket payloads.
      }
    }
  })

  async function refreshUnreadCount() {
    if (!authStore?.token) {
      unreadCount.value = 0
      return
    }
    try {
      const response = await fetchMyConversations()
      unreadCount.value = (response.data || []).reduce((sum, item) => sum + Number(item.unreadCount || 0), 0)
    } catch (error) {
      unreadCount.value = 0
    }
  }

  watch(
    () => authStore?.token,
    (token) => {
      if (!token) {
        unreadCount.value = 0
        socket.disconnect()
        return
      }
      socket.connect(token)
      refreshUnreadCount()
    }
  )

  onMounted(async () => {
    if (authStore?.token) {
      socket.connect(authStore.token)
      await refreshUnreadCount()
    }
  })

  onBeforeUnmount(() => {
    socket.disconnect()
  })

  return {
    unreadCount,
    refreshUnreadCount
  }
}
