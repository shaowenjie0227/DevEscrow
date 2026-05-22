<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AiDemandAssistantDialog from '@/components/home/AiDemandAssistantDialog.vue'

const router = useRouter()
const visible = ref(true)

watch(
  visible,
  (isVisible) => {
    if (isVisible) return
    router.push('/')
  },
  { immediate: false }
)
</script>

<template>
  <div class="ai-publish-entry">
    <div class="ai-publish-entry__mesh"></div>
    <AiDemandAssistantDialog v-model="visible" standalone />
  </div>
</template>

<style scoped>
.ai-publish-entry {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(243, 190, 37, 0.28), transparent 28%),
    radial-gradient(circle at bottom right, rgba(17, 19, 34, 0.08), transparent 32%),
    linear-gradient(180deg, #f8f4eb 0%, #fdfdfc 100%);
}

.ai-publish-entry__mesh {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.32) 1px, transparent 1px),
    linear-gradient(rgba(255, 255, 255, 0.22) 1px, transparent 1px);
  background-size: 44px 44px;
  opacity: 0.45;
  pointer-events: none;
}
</style>
