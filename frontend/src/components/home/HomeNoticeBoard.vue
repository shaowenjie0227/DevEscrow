<script setup lang="ts">
import { computed, ref } from 'vue'
import type { HomeNoticeItem } from '@/types/home'

type NoticeTabKey = 'all' | 'notice' | 'activity'

const tabs: Array<{ key: NoticeTabKey; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'notice', label: '公告' },
  { key: 'activity', label: '活动' }
]

const props = defineProps<{
  items: HomeNoticeItem[]
}>()

const emit = defineEmits<{
  action: [item: HomeNoticeItem]
}>()

const activeTab = ref<NoticeTabKey>('all')

const pinnedItem = computed(() => props.items.find((item) => Number(item.isPinned) === 1) || props.items[0] || null)

const filteredItems = computed(() => {
  if (activeTab.value === 'notice') {
    return props.items.filter((item) => item.noticeType !== 2)
  }

  if (activeTab.value === 'activity') {
    return props.items.filter((item) => item.noticeType === 2)
  }

  return props.items
})

const scrollItems = computed(() => {
  if (activeTab.value !== 'all' || !pinnedItem.value) {
    return filteredItems.value
  }

  return filteredItems.value.filter((item) => item.id !== pinnedItem.value?.id)
})

const activeCount = computed(() => String(filteredItems.value.length).padStart(2, '0'))
const listHeading = computed(() => {
  if (activeTab.value === 'notice') return '公告列表'
  if (activeTab.value === 'activity') return '活动列表'
  return pinnedItem.value ? '更多内容' : '最新内容'
})

function resolveTypeLabel(item: HomeNoticeItem) {
  return item.typeLabel || (item.noticeType === 2 ? '活动' : '公告')
}
</script>

<template>
  <section class="home-notice-board">
    <div class="home-notice-board__head">
      <div class="home-notice-board__head-copy">
        <span class="home-notice-board__eyebrow">Activity / Bulletin</span>
        <h2>活动与公告</h2>
        <p>按分类快速浏览平台最新公告、活动和阶段动态。</p>
      </div>
      <span class="home-notice-board__count">{{ activeCount }}</span>
    </div>

    <div class="home-notice-board__tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="home-notice-board__tab"
        :class="{ 'home-notice-board__tab--active': activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="!filteredItems.length" class="home-notice-board__empty">
      <strong>暂无活动与公告</strong>
      <p>管理员可在后台补充首页右侧展示内容。</p>
    </div>

    <button
      v-else-if="activeTab === 'all' && pinnedItem"
      type="button"
      class="home-notice-board__pinned"
      @click="emit('action', pinnedItem)"
    >
      <div class="home-notice-board__pinned-copy">
        <div class="home-notice-board__pinned-meta">
          <span class="home-notice-board__pin-badge">置顶</span>
          <span class="home-notice-board__tag" :data-type="pinnedItem.noticeType === 2 ? 'activity' : 'notice'">
            {{ resolveTypeLabel(pinnedItem) }}
          </span>
        </div>
        <strong>{{ pinnedItem.title }}</strong>
        <p>{{ pinnedItem.summary }}</p>
        <span class="home-notice-board__pinned-link">查看详情</span>
      </div>
      <div
        class="home-notice-board__pinned-visual"
        :style="pinnedItem.coverUrl ? { backgroundImage: `url(${pinnedItem.coverUrl})` } : {}"
      >
        <span>TOP 01</span>
      </div>
    </button>

    <div v-if="scrollItems.length" class="home-notice-board__list-shell">
      <div class="home-notice-board__list-head">
        <span>{{ listHeading }}</span>
        <small>向下滚动查看更多</small>
      </div>

      <div class="home-notice-board__list">
        <button
          v-for="(item, index) in scrollItems"
          :key="item.id"
          type="button"
          class="home-notice-board__item"
          @click="emit('action', item)"
        >
          <span
            class="home-notice-board__item-strip"
            :data-type="item.noticeType === 2 ? 'activity' : 'notice'"
          />
          <div class="home-notice-board__item-main">
            <div class="home-notice-board__item-meta">
              <span class="home-notice-board__tag" :data-type="item.noticeType === 2 ? 'activity' : 'notice'">
                {{ resolveTypeLabel(item) }}
              </span>
              <span class="home-notice-board__item-index">{{ String(index + 1).padStart(2, '0') }}</span>
            </div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.summary }}</p>
          </div>
          <span class="home-notice-board__item-arrow">→</span>
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.home-notice-board {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(243, 190, 37, 0.1), transparent 26%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(246, 248, 251, 0.96));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.8),
    0 22px 46px rgba(17, 19, 34, 0.08);
  backdrop-filter: blur(18px);
  overflow: hidden;
}

.home-notice-board__head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: start;
}

.home-notice-board__head-copy {
  display: grid;
  gap: 4px;
}

.home-notice-board__head h2 {
  margin: 0;
  font: 700 18px/1.04 var(--font-display);
  letter-spacing: -0.02em;
  color: #111322;
}

.home-notice-board__head p {
  margin: 0;
  max-width: 22ch;
  color: rgba(17, 19, 34, 0.52);
  font-size: 12px;
  line-height: 1.6;
}

.home-notice-board__eyebrow {
  color: rgba(17, 19, 34, 0.46);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.home-notice-board__count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 40px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(17, 19, 34, 0.03)),
    rgba(255, 255, 255, 0.72);
  color: rgba(17, 19, 34, 0.72);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.84),
    0 8px 18px rgba(17, 19, 34, 0.05);
}

.home-notice-board__pinned,
.home-notice-board__item {
  width: 100%;
  border: 0;
  text-align: left;
}

.home-notice-board__tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 4px;
  padding: 4px;
  border: 1px solid rgba(17, 19, 34, 0.05);
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(17, 19, 34, 0.025)),
    rgba(255, 255, 255, 0.7);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.88);
}

.home-notice-board__tab {
  min-height: 36px;
  border: 1px solid transparent;
  border-radius: 14px;
  background: transparent;
  color: rgba(17, 19, 34, 0.56);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.01em;
  transition:
    background-color 180ms ease,
    border-color 180ms ease,
    color 180ms ease,
    transform 180ms ease,
    box-shadow 180ms ease;
}

.home-notice-board__tab:hover {
  transform: translateY(-1px);
  background: rgba(17, 19, 34, 0.035);
  color: rgba(17, 19, 34, 0.78);
}

.home-notice-board__tab--active {
  border-color: rgba(17, 19, 34, 0.08);
  background:
    linear-gradient(180deg, #272b48, #171a2f),
    #171a2f;
  color: rgba(255, 255, 255, 0.94);
  box-shadow:
    0 10px 20px rgba(17, 19, 34, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.home-notice-board__pinned {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 108px;
  gap: 14px;
  padding: 16px;
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, rgba(243, 190, 37, 0.22), transparent 34%),
    linear-gradient(135deg, rgba(17, 19, 34, 0.97), rgba(34, 38, 66, 0.94));
  color: rgba(255, 255, 255, 0.96);
  box-shadow: 0 22px 42px rgba(17, 19, 34, 0.18);
  transition:
    transform 180ms ease,
    box-shadow 180ms ease;
}

.home-notice-board__pinned-copy {
  display: grid;
  gap: 10px;
}

.home-notice-board__pinned strong,
.home-notice-board__item strong {
  margin: 0;
  font-size: 18px;
  line-height: 1.35;
}

.home-notice-board__pinned p,
.home-notice-board__item p,
.home-notice-board__empty p {
  margin: 0;
  line-height: 1.7;
}

.home-notice-board__pinned p {
  color: rgba(255, 255, 255, 0.72);
}

.home-notice-board__pinned-link {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.88);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.home-notice-board__pinned-visual {
  position: relative;
  min-height: 124px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.02)),
    linear-gradient(145deg, rgba(243, 190, 37, 0.22), rgba(255, 255, 255, 0.03));
  background-position: center;
  background-size: cover;
  overflow: hidden;
}

.home-notice-board__pinned-visual::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(17, 19, 34, 0), rgba(17, 19, 34, 0.22)),
    repeating-linear-gradient(
      45deg,
      rgba(255, 255, 255, 0.06) 0,
      rgba(255, 255, 255, 0.06) 10px,
      transparent 10px,
      transparent 20px
    );
}

.home-notice-board__pinned-visual span {
  position: absolute;
  right: 12px;
  bottom: 12px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.62);
  color: rgba(255, 255, 255, 0.92);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.home-notice-board__pinned-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.home-notice-board__pin-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(243, 190, 37, 0.2);
  color: #f3be25;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.home-notice-board__list-shell {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  gap: 10px;
  padding: 10px;
  border: 1px solid rgba(17, 19, 34, 0.05);
  border-radius: 20px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(17, 19, 34, 0.02)),
    rgba(255, 255, 255, 0.68);
}

.home-notice-board__list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 4px 0 2px;
}

.home-notice-board__list-head span {
  color: rgba(17, 19, 34, 0.8);
  font-size: 13px;
  font-weight: 700;
}

.home-notice-board__list-head small {
  color: rgba(17, 19, 34, 0.42);
  font-size: 11px;
}

.home-notice-board__tag {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.home-notice-board__tag[data-type='activity'] {
  background: rgba(243, 190, 37, 0.18);
  color: #111322;
}

.home-notice-board__tag[data-type='notice'] {
  background: rgba(17, 19, 34, 0.08);
  color: rgba(17, 19, 34, 0.82);
}

.home-notice-board__list {
  display: grid;
  flex: 1;
  gap: 12px;
  min-height: 0;
  max-height: none;
  padding-right: 2px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(17, 19, 34, 0.18) transparent;
}

.home-notice-board__list::-webkit-scrollbar {
  width: 6px;
}

.home-notice-board__list::-webkit-scrollbar-track {
  background: transparent;
}

.home-notice-board__list::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.18);
}

.home-notice-board__item {
  display: grid;
  grid-template-columns: 4px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  min-height: 108px;
  padding: 14px 16px;
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(17, 19, 34, 0.03)),
    rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(17, 19, 34, 0.06);
  transition:
    transform 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease,
    border-color 180ms ease;
}

.home-notice-board__item-strip {
  align-self: stretch;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.1);
}

.home-notice-board__item-strip[data-type='activity'] {
  background: linear-gradient(180deg, rgba(243, 190, 37, 0.88), rgba(243, 190, 37, 0.3));
}

.home-notice-board__item-strip[data-type='notice'] {
  background: linear-gradient(180deg, rgba(17, 19, 34, 0.8), rgba(17, 19, 34, 0.22));
}

.home-notice-board__item-main {
  display: grid;
  gap: 8px;
}

.home-notice-board__item:hover,
.home-notice-board__pinned:hover {
  transform: translateY(-2px);
}

.home-notice-board__item:hover {
  border-color: rgba(17, 19, 34, 0.1);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 16px 30px rgba(17, 19, 34, 0.08);
}

.home-notice-board__pinned:hover {
  box-shadow: 0 28px 48px rgba(17, 19, 34, 0.22);
}

.home-notice-board__item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.home-notice-board__item-index {
  color: rgba(17, 19, 34, 0.34);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.home-notice-board__item strong {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.home-notice-board__item-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.44);
  font-size: 15px;
  font-weight: 700;
}

.home-notice-board__item p {
  color: rgba(17, 19, 34, 0.62);
  font-size: 14px;
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.home-notice-board__empty {
  position: relative;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 0;
  padding: 28px 22px;
  border: 1px solid rgba(17, 19, 34, 0.06);
  border-radius: 24px;
  background:
    radial-gradient(circle at top, rgba(243, 190, 37, 0.08), transparent 42%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.84), rgba(247, 249, 252, 0.96));
  color: rgba(17, 19, 34, 0.62);
  text-align: center;
  overflow: hidden;
}

.home-notice-board__empty::before {
  content: '';
  width: 68px;
  height: 68px;
  border: 1px solid rgba(17, 19, 34, 0.06);
  border-radius: 22px;
  background:
    linear-gradient(145deg, rgba(243, 190, 37, 0.2), rgba(255, 255, 255, 0.92)),
    linear-gradient(180deg, rgba(17, 19, 34, 0.03), rgba(17, 19, 34, 0.02));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.94),
    0 16px 28px rgba(17, 19, 34, 0.06);
}

.home-notice-board__empty strong {
  color: #43485f;
  font: 700 15px/1.2 var(--font-display);
}

.home-notice-board__empty p {
  max-width: 18ch;
  color: rgba(17, 19, 34, 0.54);
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 720px) {
  .home-notice-board {
    padding: 18px;
  }

  .home-notice-board__head {
    align-items: stretch;
  }

  .home-notice-board__count {
    min-width: 38px;
    height: 38px;
  }

  .home-notice-board__tabs {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .home-notice-board__pinned {
    grid-template-columns: 1fr;
  }

  .home-notice-board__pinned-visual {
    min-height: 96px;
  }

  .home-notice-board__list {
    min-height: 220px;
  }

  .home-notice-board__item {
    grid-template-columns: 4px minmax(0, 1fr);
  }

  .home-notice-board__item-arrow {
    display: none;
  }
}
</style>
