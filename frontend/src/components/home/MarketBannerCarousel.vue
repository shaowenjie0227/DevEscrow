<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

type BannerItem = {
  id: number
  title: string
  subtitle: string
  buttonText: string
  image?: string
}

const props = defineProps<{
  items: BannerItem[]
}>()

const emit = defineEmits<{
  action: [item: BannerItem]
}>()

const currentIndex = ref(0)
let timer: number | undefined

const currentItem = computed(() => props.items[currentIndex.value] || props.items[0])

function next() {
  if (!props.items.length) return
  currentIndex.value = (currentIndex.value + 1) % props.items.length
}

function prev() {
  if (!props.items.length) return
  currentIndex.value = (currentIndex.value - 1 + props.items.length) % props.items.length
}

function startAutoPlay() {
  stopAutoPlay()
  if (props.items.length <= 1) return
  timer = window.setInterval(next, 5000)
}

function stopAutoPlay() {
  if (timer) {
    window.clearInterval(timer)
    timer = undefined
  }
}

onMounted(startAutoPlay)
onBeforeUnmount(stopAutoPlay)
</script>

<template>
  <section class="market-container">
    <div class="market-banner market-banner--carousel">
      <div class="market-banner__carousel">
        <transition name="fade-slide" mode="out-in">
          <div v-if="currentItem" :key="currentItem.id" class="market-banner__inner market-banner__inner--carousel">
            <div class="market-banner__copy">
              <span class="market-kicker">公告 / 活动</span>
              <div>
                <h1 class="market-banner__title">{{ currentItem.title }}</h1>
                <p class="market-banner__desc">{{ currentItem.subtitle }}</p>
              </div>
              <div class="market-banner__cta">
                <button class="market-btn market-btn--primary" type="button" @click="emit('action', currentItem)">
                  {{ currentItem.buttonText }}
                </button>
                <button class="market-btn market-btn--ghost" type="button" @click="next">下一条</button>
              </div>
            </div>
            <div class="market-banner__carousel-side">
              <div class="market-banner__carousel-card">
                <div class="market-banner__carousel-image" :style="currentItem.image ? { backgroundImage: `url(${currentItem.image})` } : {}"></div>
              </div>
            </div>
          </div>
        </transition>

        <button class="market-banner__arrow market-banner__arrow--left" type="button" @click="prev">‹</button>
        <button class="market-banner__arrow market-banner__arrow--right" type="button" @click="next">›</button>
      </div>
    </div>
  </section>
</template>
