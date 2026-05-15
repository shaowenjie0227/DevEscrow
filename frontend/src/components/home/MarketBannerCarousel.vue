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
const displayIndex = computed(() => String(currentIndex.value + 1).padStart(2, '0'))
const totalCount = computed(() => String(Math.max(props.items.length, 1)).padStart(2, '0'))

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
            <div class="market-banner__copy market-banner__copy--bulletin">
              <div class="market-bulletin__meta">
                <span class="market-kicker">平台公告 / 活动</span>
                <span class="market-bulletin__counter">{{ displayIndex }} / {{ totalCount }}</span>
              </div>

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

              <div class="market-bulletin__note">
                <span>适合放活动、规则更新和精选案例，不需要夸张视觉，也能很好看。</span>
              </div>
            </div>

            <div class="market-banner__carousel-side">
              <div class="market-banner__carousel-card">
                <div
                  class="market-banner__carousel-image"
                  :style="currentItem.image ? { backgroundImage: `url(${currentItem.image})` } : {}"
                />
                <div class="market-banner__carousel-caption">
                  <strong>编辑精选</strong>
                  <span>让公告更像内容卡片，而不是轮播占位图。</span>
                </div>
              </div>
            </div>
          </div>
        </transition>

        <button class="market-banner__arrow market-banner__arrow--left" type="button" @click="prev">
          &lt;
        </button>
        <button class="market-banner__arrow market-banner__arrow--right" type="button" @click="next">
          &gt;
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.market-banner--carousel {
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(247, 249, 252, 0.96), rgba(255, 255, 255, 0.94));
  box-shadow: 0 18px 40px rgba(17, 19, 34, 0.06);
}

.market-banner__inner--carousel {
  grid-template-columns: minmax(0, 1.08fr) minmax(300px, 420px);
  gap: 28px;
  min-height: 280px;
  padding: 28px 32px;
}

.market-banner__copy--bulletin {
  gap: 20px;
}

.market-bulletin__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.market-kicker {
  min-height: auto;
  padding: 0;
  border-radius: 0;
  background: transparent;
  color: rgba(17, 19, 34, 0.56);
  letter-spacing: 0.16em;
}

.market-bulletin__counter {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.64);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.market-banner__title {
  max-width: 13ch;
  font-size: clamp(30px, 3.2vw, 48px);
  line-height: 1;
}

.market-banner__desc {
  margin-top: 14px;
  max-width: 52ch;
  line-height: 1.85;
}

.market-btn {
  min-height: 46px;
  border-radius: 12px;
}

.market-btn--primary {
  background: #111322;
}

.market-bulletin__note span {
  display: inline-flex;
  max-width: 40ch;
  align-items: center;
  min-height: 42px;
  padding: 0 14px;
  border-left: 3px solid rgba(243, 190, 37, 0.78);
  background: rgba(255, 255, 255, 0.7);
  color: rgba(17, 19, 34, 0.62);
  line-height: 1.7;
}

.market-banner__carousel-card {
  position: relative;
  min-height: 280px;
  border-radius: 18px;
  box-shadow: none;
}

.market-banner__carousel-image {
  min-height: 280px;
}

.market-banner__carousel-caption {
  position: absolute;
  right: 16px;
  bottom: 16px;
  left: 16px;
  display: grid;
  gap: 4px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(17, 19, 34, 0.78);
  color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
}

.market-banner__carousel-caption strong {
  font-size: 14px;
}

.market-banner__carousel-caption span {
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.72);
}

.market-banner__arrow {
  top: auto;
  bottom: 18px;
  transform: none;
  width: 38px;
  height: 38px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  background: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  font-weight: 700;
}

.market-banner__arrow--left {
  left: 18px;
}

.market-banner__arrow--right {
  left: 62px;
  right: auto;
}

@media (max-width: 1100px) {
  .market-banner__inner--carousel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .market-banner__inner--carousel {
    padding: 20px;
  }

  .market-banner__carousel-card,
  .market-banner__carousel-image {
    min-height: 220px;
  }
}
</style>
