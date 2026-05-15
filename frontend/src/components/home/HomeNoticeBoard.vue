<script setup lang="ts">
import { computed, ref } from 'vue'

type NoticeItem = {
  id: number
  noticeType?: number
  typeLabel?: string
  title: string
  summary: string
  targetUrl?: string
  coverUrl?: string
}

type FilterKey = 'all' | 'notice' | 'activity'

const props = defineProps<{
  items: NoticeItem[]
}>()

const emit = defineEmits<{
  action: [item: NoticeItem]
}>()

const activeFilter = ref<FilterKey>('all')

const filterOptions: Array<{ key: FilterKey; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'notice', label: '公告' },
  { key: 'activity', label: '活动' }
]

const filteredItems = computed(() => {
  if (activeFilter.value === 'all') {
    return props.items
  }
  const expectedType = activeFilter.value === 'activity' ? 2 : 1
  return props.items.filter((item) => (item.noticeType || 1) === expectedType)
})

const featuredItem = computed(() => filteredItems.value[0] || null)
const streamItems = computed(() => filteredItems.value.slice(1))
const emptyMessage = computed(() => {
  if (activeFilter.value === 'activity') return '暂时还没有活动内容'
  if (activeFilter.value === 'notice') return '暂时还没有公告内容'
  return '暂时还没有首页内容'
})

function resolveTypeLabel(item: NoticeItem) {
  return item.typeLabel || (item.noticeType === 2 ? '活动' : '公告')
}

function resolveMeta(item: NoticeItem, index: number) {
  if (item.noticeType === 2) {
    return index === 0 ? '优先活动位' : '活动推荐'
  }
  return index === 0 ? '平台通知' : '最新公告'
}
</script>

<template>
  <section class="home-notice-board">
    <div class="home-notice-board__glow home-notice-board__glow--top" />
    <div class="home-notice-board__glow home-notice-board__glow--bottom" />

    <div class="home-notice-board__head">
      <div class="home-notice-board__title-row">
        <div class="home-notice-board__intro">
          <span class="home-notice-board__eyebrow">Activity / Bulletin</span>
          <h2>活动与公告</h2>
          <p>管理员可按“活动 / 公告”发布，右侧按标签筛选并滚动展示。</p>
        </div>

        <span class="home-notice-board__count">{{ String(filteredItems.length).padStart(2, '0') }}</span>
      </div>

      <div class="home-notice-board__toolbar">
        <div class="home-notice-board__filters" role="tablist" aria-label="内容筛选">
          <button
            v-for="option in filterOptions"
            :key="option.key"
            type="button"
            class="home-notice-board__filter"
            :class="{ 'home-notice-board__filter--active': activeFilter === option.key }"
            @click="activeFilter = option.key"
          >
            {{ option.label }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="featuredItem" class="home-notice-board__content">
      <button
        type="button"
        class="home-notice-board__featured"
        @click="emit('action', featuredItem)"
      >
        <div class="home-notice-board__featured-visual">
          <div
            class="home-notice-board__featured-cover"
            :style="featuredItem.coverUrl ? { backgroundImage: `url(${featuredItem.coverUrl})` } : {}"
          />

          <div class="home-notice-board__featured-overlay">
            <span class="home-notice-board__tag" :data-type="featuredItem.noticeType === 2 ? 'activity' : 'notice'">
              {{ resolveTypeLabel(featuredItem) }}
            </span>
            <span class="home-notice-board__featured-meta">{{ resolveMeta(featuredItem, 0) }}</span>
          </div>
        </div>

        <div class="home-notice-board__featured-body">
          <div class="home-notice-board__featured-copy">
            <h3>{{ featuredItem.title }}</h3>
            <p>{{ featuredItem.summary }}</p>
          </div>
          <span class="home-notice-board__featured-arrow">查看详情</span>
        </div>
      </button>

      <div class="home-notice-board__stream-wrap">
        <div class="home-notice-board__stream-head">
          <strong>滚动列表</strong>
          <span>左右区分明确，信息更适合持续上新</span>
        </div>

        <div class="home-notice-board__stream no-scrollbar">
          <button
            v-for="(item, index) in streamItems"
            :key="item.id"
            type="button"
            class="home-notice-board__item"
            @click="emit('action', item)"
          >
            <div class="home-notice-board__item-cover" :style="item.coverUrl ? { backgroundImage: `url(${item.coverUrl})` } : {}">
              <span class="home-notice-board__tag" :data-type="item.noticeType === 2 ? 'activity' : 'notice'">
                {{ resolveTypeLabel(item) }}
              </span>
            </div>

            <div class="home-notice-board__item-body">
              <div class="home-notice-board__item-meta">
                <span>{{ resolveMeta(item, index + 1) }}</span>
                <span>{{ String(index + 2).padStart(2, '0') }}</span>
              </div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.summary }}</p>
            </div>
          </button>
        </div>
      </div>
    </div>

    <div v-else class="home-notice-board__empty">
      <strong>{{ emptyMessage }}</strong>
      <p>管理员可在后台发布对应标签内容，前台会自动进入右侧滑动列表。</p>
    </div>
  </section>
</template>

<style scoped>
.home-notice-board {
  position: relative;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 18px;
  height: 100%;
  min-height: 100%;
  padding: 24px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 30px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(248, 250, 255, 0.94)),
    linear-gradient(135deg, rgba(243, 190, 37, 0.08), transparent 38%);
  box-shadow: 0 28px 56px rgba(17, 19, 34, 0.08);
  backdrop-filter: blur(20px);
}

.home-notice-board__glow {
  position: absolute;
  z-index: 0;
  border-radius: 999px;
  filter: blur(52px);
  pointer-events: none;
}

.home-notice-board__glow--top {
  top: -36px;
  right: -28px;
  width: 180px;
  height: 180px;
  background: rgba(243, 190, 37, 0.18);
}

.home-notice-board__glow--bottom {
  bottom: -44px;
  left: -40px;
  width: 160px;
  height: 160px;
  background: rgba(37, 99, 235, 0.1);
}

.home-notice-board__head,
.home-notice-board__content,
.home-notice-board__empty {
  position: relative;
  z-index: 1;
}

.home-notice-board__head {
  display: grid;
  gap: 14px;
  padding-bottom: 2px;
}

.home-notice-board__title-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: start;
  gap: 16px;
}

.home-notice-board__intro {
  display: grid;
  gap: 8px;
  max-width: 34ch;
}

.home-notice-board__eyebrow {
  color: rgba(17, 19, 34, 0.5);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.home-notice-board__head h2 {
  font: 700 clamp(28px, 2.2vw, 34px) / 1 var(--font-display);
  letter-spacing: -0.05em;
}

.home-notice-board__intro p,
.home-notice-board__stream-head span,
.home-notice-board__empty p {
  color: rgba(17, 19, 34, 0.58);
  line-height: 1.75;
}

.home-notice-board__count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 48px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.64);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.home-notice-board__toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.home-notice-board__filters {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.78);
}

.home-notice-board__filter {
  min-height: 34px;
  padding: 0 14px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: rgba(17, 19, 34, 0.58);
  font-size: 13px;
  font-weight: 700;
  transition:
    transform 180ms ease,
    background-color 180ms ease,
    color 180ms ease,
    box-shadow 180ms ease;
}

.home-notice-board__filter:hover {
  color: rgba(17, 19, 34, 0.9);
}

.home-notice-board__filter--active {
  background: #111322;
  color: rgba(255, 255, 255, 0.96);
  box-shadow: 0 12px 26px rgba(17, 19, 34, 0.18);
}

.home-notice-board__content {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 16px;
  min-height: 0;
  flex: 1 1 auto;
}

.home-notice-board__featured,
.home-notice-board__item {
  width: 100%;
  border: 0;
  text-align: left;
}

.home-notice-board__featured {
  display: grid;
  gap: 0;
  flex: 0 0 auto;
  overflow: hidden;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 18px 36px rgba(17, 19, 34, 0.08);
  transition:
    transform 220ms ease,
    box-shadow 220ms ease;
}

.home-notice-board__featured:hover,
.home-notice-board__item:hover {
  transform: translateY(-2px);
}

.home-notice-board__featured:hover {
  box-shadow: 0 24px 40px rgba(17, 19, 34, 0.12);
}

.home-notice-board__featured-visual {
  position: relative;
  min-height: 186px;
}

.home-notice-board__featured-cover {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(135deg, rgba(17, 19, 34, 0.08), rgba(243, 190, 37, 0.24)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(241, 245, 249, 0.92));
  background-position: center;
  background-size: cover;
}

.home-notice-board__featured-cover::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(12, 16, 27, 0.02), rgba(12, 16, 27, 0.46)),
    linear-gradient(135deg, transparent 24%, rgba(255, 255, 255, 0.08) 100%);
}

.home-notice-board__featured-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px;
}

.home-notice-board__featured-meta {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.64);
  color: rgba(255, 255, 255, 0.88);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.home-notice-board__featured-body {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  padding: 18px 18px 20px;
  background:
    linear-gradient(180deg, rgba(17, 19, 34, 0.96), rgba(25, 29, 44, 0.98));
  color: rgba(255, 255, 255, 0.96);
}

.home-notice-board__featured-copy {
  display: grid;
  gap: 10px;
}

.home-notice-board__featured-copy h3 {
  font-size: 22px;
  line-height: 1.18;
}

.home-notice-board__featured-copy p {
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.75;
}

.home-notice-board__featured-arrow {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.82);
  font-size: 12px;
  font-weight: 700;
}

.home-notice-board__tag {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.home-notice-board__tag[data-type='activity'] {
  background: rgba(243, 190, 37, 0.9);
  color: #111322;
}

.home-notice-board__tag[data-type='notice'] {
  background: rgba(255, 255, 255, 0.86);
  color: rgba(17, 19, 34, 0.84);
}

.home-notice-board__stream-wrap {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 12px;
  min-height: 0;
  height: 100%;
  padding: 16px;
  border: 1px solid rgba(17, 19, 34, 0.07);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.64);
}

.home-notice-board__stream-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.home-notice-board__stream-head strong {
  font-size: 14px;
  letter-spacing: 0.02em;
}

.home-notice-board__stream {
  display: grid;
  gap: 12px;
  min-height: 0;
  height: 100%;
  overflow-y: auto;
  padding-right: 2px;
}

.home-notice-board__item {
  display: grid;
  grid-template-columns: 104px minmax(0, 1fr);
  gap: 14px;
  align-items: stretch;
  padding: 12px;
  border-radius: 20px;
  background: rgba(17, 19, 34, 0.04);
  transition:
    transform 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease;
}

.home-notice-board__item:hover {
  background: rgba(17, 19, 34, 0.06);
  box-shadow: 0 14px 28px rgba(17, 19, 34, 0.08);
}

.home-notice-board__item-cover {
  position: relative;
  min-height: 108px;
  overflow: hidden;
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(17, 19, 34, 0.08), rgba(243, 190, 37, 0.18)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(241, 245, 249, 0.9));
  background-position: center;
  background-size: cover;
}

.home-notice-board__item-cover::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(17, 19, 34, 0.04), rgba(17, 19, 34, 0.26));
}

.home-notice-board__item-cover .home-notice-board__tag {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1;
}

.home-notice-board__item-body {
  display: grid;
  align-content: start;
  gap: 8px;
  min-width: 0;
}

.home-notice-board__item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: rgba(17, 19, 34, 0.48);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.home-notice-board__item strong {
  font-size: 18px;
  line-height: 1.32;
}

.home-notice-board__item p {
  color: rgba(17, 19, 34, 0.6);
  line-height: 1.72;
  font-size: 14px;
}

.home-notice-board__empty {
  display: grid;
  gap: 8px;
  place-items: center;
  min-height: 320px;
  padding: 24px;
  border: 1px dashed rgba(17, 19, 34, 0.14);
  border-radius: 24px;
  text-align: center;
}

.home-notice-board__empty strong {
  font-size: 20px;
}

@media (max-width: 1280px) {
  .home-notice-board__featured-body {
    align-items: start;
    flex-direction: column;
  }
}

@media (max-width: 720px) {
  .home-notice-board {
    padding: 18px;
  }

  .home-notice-board__title-row {
    grid-template-columns: 1fr;
  }

  .home-notice-board__count {
    justify-self: start;
  }

  .home-notice-board__filters {
    max-width: 100%;
    overflow-x: auto;
  }

  .home-notice-board__item {
    grid-template-columns: 1fr;
  }

  .home-notice-board__item-cover {
    min-height: 136px;
  }

  .home-notice-board__stream {
    max-height: none;
    height: auto;
  }
}
</style>
