<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

type BannerItem = {
  id: number
  title: string
  subtitle?: string
  image?: string
}

const props = defineProps<{
  items: BannerItem[]
}>()

const currentIndex = ref(0)
let timer: number | undefined

const currentItem = computed(() => props.items[currentIndex.value] || null)
const hasMultiple = computed(() => props.items.length > 1)
const displayIndex = computed(() => String(currentIndex.value + 1).padStart(2, '0'))
const totalCount = computed(() => String(Math.max(props.items.length, 1)).padStart(2, '0'))

function stopAutoPlay() {
  if (timer !== undefined) {
    window.clearInterval(timer)
    timer = undefined
  }
}

function startAutoPlay() {
  stopAutoPlay()
  if (!hasMultiple.value) return
  timer = window.setInterval(() => {
    next(false)
  }, 5000)
}

function setIndex(index: number, restart = true) {
  if (!props.items.length) return
  currentIndex.value = (index + props.items.length) % props.items.length
  if (restart) startAutoPlay()
}

function next(restart = true) {
  setIndex(currentIndex.value + 1, restart)
}

function prev() {
  setIndex(currentIndex.value - 1)
}

function goTo(index: number) {
  setIndex(index)
}

function getImageStyle(item?: BannerItem) {
  if (item?.image) {
    return {
      backgroundImage: `url(${item.image})`,
      backgroundPosition: 'center center'
    }
  }

  return {
    backgroundImage:
      'linear-gradient(135deg, rgba(232, 237, 246, 0.96) 0%, rgba(202, 213, 233, 0.94) 48%, rgba(174, 190, 216, 0.92) 100%)'
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
    <div class="market-banner market-banner--carousel" @mouseenter="stopAutoPlay" @mouseleave="startAutoPlay">
      <transition name="fade-slide" mode="out-in">
        <article v-if="currentItem" :key="currentItem.id" class="market-banner__slide">
          <div class="market-banner__image" :style="getImageStyle(currentItem)">
            <div class="market-banner__shade" />

            <div class="market-banner__content">
              <div class="market-banner__meta">
                <span class="market-banner__tag">首页轮播</span>
                <span class="market-banner__count">{{ displayIndex }} / {{ totalCount }}</span>
              </div>

              <div class="market-banner__copy">
                <h1 class="market-banner__title">{{ currentItem.title }}</h1>
                <p v-if="currentItem.subtitle" class="market-banner__desc">{{ currentItem.subtitle }}</p>
              </div>
            </div>
          </div>
        </article>

        <article v-else key="empty" class="market-banner__slide">
          <div class="market-banner__image market-banner__image--empty">
            <div class="market-banner__shade" />

            <div class="market-banner__content">
              <div class="market-banner__meta">
                <span class="market-banner__tag">首页轮播</span>
                <span class="market-banner__count">00 / 00</span>
              </div>

              <div class="market-banner__copy">
                <h1 class="market-banner__title">暂无轮播内容</h1>
                <p class="market-banner__desc">后台新增轮播后，这里会按顺序展示。</p>
              </div>
            </div>
          </div>
        </article>
      </transition>

      <template v-if="hasMultiple">
        <button class="market-banner__arrow market-banner__arrow--left" type="button" aria-label="上一张" @click="prev">
          ‹
        </button>
        <button class="market-banner__arrow market-banner__arrow--right" type="button" aria-label="下一张" @click="next()">
          ›
        </button>

        <div class="market-banner__dots">
          <button
            v-for="(item, index) in props.items"
            :key="item.id"
            type="button"
            class="market-banner__dot"
            :class="{ 'market-banner__dot--active': index === currentIndex }"
            :aria-label="`切换到第 ${index + 1} 张`"
            @click="goTo(index)"
          />
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.market-banner--carousel {
  position: relative;
  height: 100%;
  overflow: hidden;
  border: 0;
  border-radius: 30px;
  background: #fff;
  box-shadow: 0 18px 40px rgba(17, 19, 34, 0.08);
}

.market-banner--carousel::before,
.market-banner--carousel::after {
  display: none;
}

.market-banner__slide {
  height: 100%;
}

.market-banner__image {
  position: relative;
  display: flex;
  align-items: flex-end;
  width: 100%;
  height: 100%;
  padding: 36px;
  background-color: #dfe6f2;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
}

.market-banner__image--empty {
  background-image: linear-gradient(135deg, #edf2fb 0%, #d6dfef 52%, #becde3 100%);
}

.market-banner__shade {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 78% 18%, rgba(255, 248, 230, 0.34) 0%, rgba(255, 248, 230, 0) 32%),
    linear-gradient(90deg, rgba(12, 18, 31, 0.44) 0%, rgba(12, 18, 31, 0.24) 34%, rgba(12, 18, 31, 0.08) 62%, rgba(12, 18, 31, 0.04) 100%),
    linear-gradient(180deg, rgba(12, 18, 31, 0.04) 0%, rgba(12, 18, 31, 0.22) 100%);
}

.market-banner__content {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 18px;
  width: min(560px, 100%);
  color: #fff;
}

.market-banner__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.market-banner__tag,
.market-banner__count {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: rgba(255, 255, 255, 0.92);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  backdrop-filter: blur(6px);
}

.market-banner__copy {
  display: grid;
  gap: 12px;
  padding: 22px 24px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(11, 16, 28, 0.48) 0%, rgba(11, 16, 28, 0.2) 100%);
  box-shadow: 0 24px 50px rgba(11, 16, 28, 0.16);
  backdrop-filter: blur(10px);
}

.market-banner__title {
  margin: 0;
  max-width: 12ch;
  color: #fff;
  font-size: clamp(34px, 4.6vw, 62px);
  line-height: 1.02;
  letter-spacing: -0.04em;
}

.market-banner__desc {
  margin: 0;
  max-width: 46ch;
  color: rgba(255, 255, 255, 0.92);
  font-size: 16px;
  line-height: 1.8;
}

.market-banner__arrow {
  position: absolute;
  top: 50%;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: #111322;
  font-size: 28px;
  line-height: 1;
  transform: translateY(-50%);
  box-shadow: 0 10px 24px rgba(17, 19, 34, 0.14);
  transition:
    background-color 180ms ease,
    transform 180ms ease;
}

.market-banner__arrow:hover {
  background: #fff;
}

.market-banner__arrow--left {
  left: 20px;
}

.market-banner__arrow--right {
  right: 20px;
}

.market-banner__dots {
  position: absolute;
  bottom: 24px;
  left: 50%;
  z-index: 2;
  display: flex;
  gap: 8px;
  transform: translateX(-50%);
}

.market-banner__dot {
  width: 10px;
  height: 10px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.42);
  transition:
    width 180ms ease,
    background-color 180ms ease;
}

.market-banner__dot--active {
  width: 28px;
  background: #fff;
}

@media (max-width: 960px) {
  .market-banner__image {
    padding: 28px;
  }

  .market-banner__content {
    width: min(100%, 520px);
  }
}

@media (max-width: 720px) {
  .market-banner--carousel,
  .market-banner__image {
    min-height: 320px;
  }

  .market-banner__image {
    padding: 22px 20px 64px;
  }

  .market-banner__meta {
    flex-wrap: wrap;
  }

  .market-banner__title {
    max-width: none;
    font-size: clamp(28px, 9vw, 40px);
  }

  .market-banner__desc {
    font-size: 14px;
    line-height: 1.7;
  }

  .market-banner__arrow {
    top: auto;
    bottom: 18px;
    transform: none;
    width: 40px;
    height: 40px;
    font-size: 24px;
  }

  .market-banner__arrow--left {
    left: 16px;
  }

  .market-banner__arrow--right {
    right: 16px;
  }

  .market-banner__dots {
    bottom: 30px;
  }
}
</style>
