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
      <div class="market-banner__carousel">
        <transition name="fade-slide" mode="out-in">
          <div v-if="currentItem" :key="currentItem.id" class="market-banner__inner market-banner__inner--carousel">
            <div class="market-banner__copy market-banner__copy--bulletin">
              <div class="market-bulletin__meta">
                <span class="market-kicker">平台首页轮播</span>
                <span class="market-bulletin__counter">{{ displayIndex }} / {{ totalCount }}</span>
              </div>

              <div>
                <h1 class="market-banner__title">{{ currentItem.title }}</h1>
                <p class="market-banner__desc">{{ currentItem.subtitle }}</p>
              </div>

              <div class="market-banner__cta">
                <button class="market-btn market-btn--primary" type="button" @click="emit('action', currentItem)">
                  {{ currentItem.buttonText || '查看详情' }}
                </button>
                <button class="market-btn market-btn--ghost" type="button" @click="next">下一条</button>
              </div>

              <div class="market-bulletin__note">
                <span>左侧适合放宣传轮播、平台活动或阶段性重点信息。</span>
              </div>
            </div>

            <div class="market-banner__visual">
              <div
                class="market-banner__visual-image"
                :style="currentItem.image ? { backgroundImage: `url(${currentItem.image})` } : {}"
              />
              <div class="market-banner__visual-caption">
                <strong>轮播预览</strong>
                <span>支持管理员上传图片、设置文案和跳转链接。</span>
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
  border-radius: 28px;
  background: linear-gradient(180deg, rgba(247, 249, 252, 0.96), rgba(255, 255, 255, 0.94));
  box-shadow: 0 18px 40px rgba(17, 19, 34, 0.06);
}

.market-banner__inner--carousel {
  grid-template-columns: minmax(0, 1.02fr) minmax(280px, 360px);
  gap: 22px;
  min-height: 320px;
  padding: 28px 32px 56px;
}

.market-banner__copy--bulletin {
  gap: 20px;
  align-content: center;
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
  max-width: 12ch;
  font-size: clamp(32px, 3.4vw, 54px);
  line-height: 0.98;
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
  max-width: 44ch;
  align-items: center;
  min-height: 42px;
  padding: 0 14px;
  border-left: 3px solid rgba(243, 190, 37, 0.78);
  background: rgba(255, 255, 255, 0.72);
  color: rgba(17, 19, 34, 0.62);
  line-height: 1.7;
}

.market-banner__visual {
  position: relative;
  min-height: 264px;
  overflow: hidden;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.76);
}

.market-banner__visual-image {
  min-height: 264px;
  height: 100%;
  background:
    linear-gradient(135deg, rgba(17, 19, 34, 0.08), rgba(243, 190, 37, 0.22)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(241, 245, 249, 0.9));
  background-position: center;
  background-size: cover;
}

.market-banner__visual-caption {
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

.market-banner__visual-caption strong {
  font-size: 14px;
}

.market-banner__visual-caption span {
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.72);
}

.market-banner__dots {
  position: absolute;
  right: 24px;
  bottom: 22px;
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
  width: 26px;
  background: #111322;
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

@media (max-width: 1180px) {
  .market-banner__inner--carousel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .market-banner__inner--carousel {
    min-height: auto;
    padding: 20px 20px 58px;
  }

  .market-banner__visual,
  .market-banner__visual-image {
    min-height: 220px;
  }

  .market-banner__dots {
    right: 20px;
  }
}
</style>
