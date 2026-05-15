<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

type BannerItem = {
  id: number
  title: string
  subtitle: string
  buttonText: string
  image?: string
  targetUrl?: string
}

const props = defineProps<{
  items: BannerItem[]
}>()

const emit = defineEmits<{
  action: [item: BannerItem]
}>()

const currentIndex = ref(0)
let timer: number | undefined

const currentItem = computed(() => props.items[currentIndex.value] || props.items[0] || null)
const displayIndex = computed(() => String(currentIndex.value + 1).padStart(2, '0'))
const totalCount = computed(() => String(Math.max(props.items.length, 1)).padStart(2, '0'))
const previewItems = computed(() => {
  if (!props.items.length) return []

  return props.items
    .map((item, index) => ({ ...item, index }))
    .filter((item) => item.index !== currentIndex.value)
    .slice(0, 2)
})

function next() {
  if (!props.items.length) return
  currentIndex.value = (currentIndex.value + 1) % props.items.length
}

function prev() {
  if (!props.items.length) return
  currentIndex.value = (currentIndex.value - 1 + props.items.length) % props.items.length
}

function goTo(index: number) {
  currentIndex.value = index
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

watch(
  () => props.items.length,
  () => {
    if (currentIndex.value >= props.items.length) {
      currentIndex.value = 0
    }
    startAutoPlay()
  }
)

onMounted(startAutoPlay)
onBeforeUnmount(stopAutoPlay)
</script>

<template>
  <section class="market-container">
    <div class="market-banner market-banner--carousel">
      <div class="market-banner__texture market-banner__texture--top" />
      <div class="market-banner__texture market-banner__texture--bottom" />

      <div class="market-banner__carousel">
        <transition name="fade-slide" mode="out-in">
          <div v-if="currentItem" :key="currentItem.id" class="market-banner__inner market-banner__inner--carousel">
            <div class="market-banner__copy market-banner__copy--bulletin">
              <div class="market-bulletin__meta">
                <span class="market-kicker">平台首页轮播</span>
                <span class="market-bulletin__counter">{{ displayIndex }} / {{ totalCount }}</span>
              </div>

              <div class="market-bulletin__body">
                <span class="market-bulletin__line">Featured Storyboard</span>
                <h1 class="market-banner__title">{{ currentItem.title }}</h1>
                <p class="market-banner__desc">{{ currentItem.subtitle }}</p>
              </div>

              <div class="market-banner__cta">
                <button class="market-btn market-btn--primary" type="button" @click="emit('action', currentItem)">
                  {{ currentItem.buttonText || '查看详情' }}
                </button>
                <button class="market-btn market-btn--ghost" type="button" @click="next">下一条</button>
              </div>

              <div class="market-bulletin__stats">
                <div class="market-bulletin__stat">
                  <strong>{{ totalCount }}</strong>
                  <span>轮播内容</span>
                </div>
                <div class="market-bulletin__stat">
                  <strong>活动 / 公告</strong>
                  <span>支持独立跳转</span>
                </div>
                <div class="market-bulletin__stat">
                  <strong>后台可配</strong>
                  <span>图片与文案均可维护</span>
                </div>
              </div>

              <div class="market-bulletin__note">
                <span>左侧做主视觉宣传，右侧承接运营内容，首屏信息层级会清晰很多。</span>
              </div>
            </div>

            <div class="market-banner__visual">
              <div
                class="market-banner__visual-image"
                :style="currentItem.image ? { backgroundImage: `url(${currentItem.image})` } : {}"
              />

              <div class="market-banner__visual-glass">
                <span>轮播主卡</span>
                <strong>{{ currentItem.buttonText || '查看详情' }}</strong>
              </div>

              <div v-if="previewItems.length" class="market-banner__preview-stack">
                <button
                  v-for="item in previewItems"
                  :key="item.id"
                  type="button"
                  class="market-banner__preview-card"
                  @click="goTo(item.index)"
                >
                  <span>{{ String(item.index + 1).padStart(2, '0') }}</span>
                  <strong>{{ item.title }}</strong>
                </button>
              </div>
            </div>
          </div>
        </transition>

        <div v-if="props.items.length > 1" class="market-banner__dots">
          <button
            v-for="(item, index) in props.items"
            :key="item.id"
            type="button"
            class="market-banner__dot"
            :class="{ 'market-banner__dot--active': index === currentIndex }"
            @click="goTo(index)"
          />
        </div>

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
  position: relative;
  overflow: hidden;
  border-radius: 30px;
  background:
    radial-gradient(circle at 12% 14%, rgba(243, 190, 37, 0.18), transparent 24%),
    radial-gradient(circle at 88% 84%, rgba(37, 99, 235, 0.1), transparent 24%),
    linear-gradient(180deg, rgba(248, 250, 255, 0.98), rgba(255, 255, 255, 0.94));
  box-shadow: 0 22px 48px rgba(17, 19, 34, 0.07);
}

.market-banner__texture {
  position: absolute;
  z-index: 0;
  border-radius: 999px;
  filter: blur(56px);
  pointer-events: none;
}

.market-banner__texture--top {
  top: -40px;
  right: 18%;
  width: 220px;
  height: 220px;
  background: rgba(243, 190, 37, 0.14);
}

.market-banner__texture--bottom {
  bottom: -70px;
  left: 52%;
  width: 220px;
  height: 220px;
  background: rgba(37, 99, 235, 0.1);
}

.market-banner__carousel {
  position: relative;
  z-index: 1;
  height: 100%;
}

.market-banner__inner--carousel {
  grid-template-columns: minmax(0, 1.04fr) minmax(320px, 380px);
  gap: 24px;
  min-height: 100%;
  height: 100%;
  padding: 30px 34px 58px;
}

.market-banner__copy--bulletin {
  min-height: 100%;
  gap: 22px;
  align-content: start;
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
  letter-spacing: 0.18em;
}

.market-bulletin__counter {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.64);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.market-bulletin__body {
  display: grid;
  gap: 14px;
}

.market-bulletin__line {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(243, 190, 37, 0.16);
  color: rgba(17, 19, 34, 0.72);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.market-banner__title {
  max-width: 10ch;
  font-size: clamp(38px, 4vw, 64px);
  line-height: 0.94;
}

.market-banner__desc {
  max-width: 54ch;
  color: rgba(17, 19, 34, 0.66);
  line-height: 1.9;
  font-size: 15px;
}

.market-btn {
  min-height: 48px;
  border-radius: 14px;
}

.market-btn--primary {
  background: #111322;
}

.market-bulletin__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.market-bulletin__stat {
  display: grid;
  gap: 6px;
  padding: 14px 14px 12px;
  border: 1px solid rgba(17, 19, 34, 0.06);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.62);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.76);
}

.market-bulletin__stat strong {
  font-family: var(--font-display);
  font-size: 18px;
  line-height: 1.1;
}

.market-bulletin__stat span {
  color: rgba(17, 19, 34, 0.56);
  font-size: 13px;
  line-height: 1.5;
}

.market-bulletin__note span {
  display: inline-flex;
  max-width: 50ch;
  align-items: center;
  min-height: 44px;
  padding: 0 14px;
  border-left: 3px solid rgba(243, 190, 37, 0.78);
  background: rgba(255, 255, 255, 0.72);
  color: rgba(17, 19, 34, 0.62);
  line-height: 1.75;
}

.market-banner__visual {
  position: relative;
  min-height: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.76),
    0 20px 32px rgba(17, 19, 34, 0.08);
}

.market-banner__visual-image {
  min-height: 100%;
  height: 100%;
  background:
    linear-gradient(135deg, rgba(17, 19, 34, 0.1), rgba(243, 190, 37, 0.24)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(241, 245, 249, 0.9));
  background-position: center;
  background-size: cover;
  transform: scale(1.01);
}

.market-banner__visual-image::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(17, 19, 34, 0.02), rgba(17, 19, 34, 0.3)),
    linear-gradient(135deg, transparent 18%, rgba(255, 255, 255, 0.1));
}

.market-banner__visual-glass {
  position: absolute;
  top: 16px;
  left: 16px;
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  background: rgba(17, 19, 34, 0.5);
  color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
}

.market-banner__visual-glass span {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.66);
}

.market-banner__visual-glass strong {
  font-size: 15px;
}

.market-banner__preview-stack {
  position: absolute;
  right: 16px;
  bottom: 16px;
  left: 16px;
  display: grid;
  gap: 10px;
}

.market-banner__preview-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  background: rgba(17, 19, 34, 0.68);
  color: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  transition:
    transform 180ms ease,
    background-color 180ms ease;
}

.market-banner__preview-card:hover {
  transform: translateY(-1px);
  background: rgba(17, 19, 34, 0.8);
}

.market-banner__preview-card span {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  height: 34px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.1em;
}

.market-banner__preview-card strong {
  flex: 1 1 auto;
  min-width: 0;
  text-align: left;
  font-size: 14px;
  line-height: 1.45;
}

.market-banner__dots {
  position: absolute;
  right: 28px;
  bottom: 24px;
  display: flex;
  gap: 8px;
}

.market-banner__dot {
  width: 9px;
  height: 9px;
  border: 0;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.18);
}

.market-banner__dot--active {
  width: 28px;
  background: #111322;
}

.market-banner__arrow {
  top: auto;
  bottom: 18px;
  transform: none;
  width: 40px;
  height: 40px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  background: rgba(255, 255, 255, 0.92);
  font-size: 16px;
  font-weight: 700;
}

.market-banner__arrow--left {
  left: 18px;
}

.market-banner__arrow--right {
  left: 64px;
  right: auto;
}

@media (max-width: 1180px) {
  .market-banner__inner--carousel {
    grid-template-columns: 1fr;
    min-height: auto;
    height: auto;
  }
}

@media (max-width: 720px) {
  .market-banner__inner--carousel {
    min-height: auto;
    padding: 20px 20px 58px;
  }

  .market-bulletin__stats {
    grid-template-columns: 1fr;
  }

  .market-banner__visual,
  .market-banner__visual-image {
    min-height: 240px;
  }

  .market-banner__dots {
    right: 20px;
  }
}
</style>
