import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { fetchInboxUnreadSummary } from '@/api/modules/inbox'
import { useChatWebSocket } from '@/composables/useChatWebSocket'

const sharedUnreadCount = ref(0)

export function useInboxUnreadCount(authStore) {
  const unreadCount = sharedUnreadCount

  const socket = useChatWebSocket({
    onMessage: async (event) => {
      try {
        const payload = JSON.parse(event.data)
        if (payload?.type !== 'SITE_MESSAGE') {
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
      const response = await fetchInboxUnreadSummary()
      unreadCount.value = Number(response.data?.unreadCount || 0)
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
