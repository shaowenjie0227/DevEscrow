<script setup lang="ts">
import { computed } from 'vue'

type NoticeItem = {
  id: number
  noticeType?: number
  typeLabel?: string
  title: string
  summary: string
  targetUrl?: string
  coverUrl?: string
}

const props = defineProps<{
  items: NoticeItem[]
}>()

const emit = defineEmits<{
  action: [item: NoticeItem]
}>()

const featuredItem = computed(() => props.items[0] || null)
const remainingItems = computed(() => props.items.slice(1, 4))

function resolveTypeLabel(item: NoticeItem) {
  return item.typeLabel || (item.noticeType === 2 ? '活动' : '公告')
}
</script>

<template>
  <section class="home-notice-board">
    <div class="home-notice-board__head">
      <div>
        <span class="home-notice-board__eyebrow">Activity / Bulletin</span>
        <h2>活动与公告</h2>
      </div>
      <span class="home-notice-board__count">{{ String(items.length).padStart(2, '0') }}</span>
    </div>

    <button
      v-if="featuredItem"
      type="button"
      class="home-notice-board__featured"
      @click="emit('action', featuredItem)"
    >
      <div
        class="home-notice-board__featured-cover"
        :style="featuredItem.coverUrl ? { backgroundImage: `url(${featuredItem.coverUrl})` } : {}"
      />
      <div class="home-notice-board__featured-content">
        <span class="home-notice-board__tag" :data-type="featuredItem.noticeType === 2 ? 'activity' : 'notice'">
          {{ resolveTypeLabel(featuredItem) }}
        </span>
        <h3>{{ featuredItem.title }}</h3>
        <p>{{ featuredItem.summary }}</p>
      </div>
    </button>

    <div v-else class="home-notice-board__empty">
      <strong>暂无活动与公告</strong>
      <p>管理员可在后台补充首页右侧展示内容。</p>
    </div>

    <div v-if="remainingItems.length" class="home-notice-board__list">
      <button
        v-for="item in remainingItems"
        :key="item.id"
        type="button"
        class="home-notice-board__item"
        @click="emit('action', item)"
      >
        <div class="home-notice-board__item-meta">
          <span class="home-notice-board__tag" :data-type="item.noticeType === 2 ? 'activity' : 'notice'">
            {{ resolveTypeLabel(item) }}
          </span>
        </div>
        <strong>{{ item.title }}</strong>
        <p>{{ item.summary }}</p>
      </button>
    </div>
  </section>
</template>

<style scoped>
.home-notice-board {
  display: grid;
  gap: 16px;
  height: 100%;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 24px 52px rgba(17, 19, 34, 0.08);
  backdrop-filter: blur(18px);
}

.home-notice-board__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.home-notice-board__head h2 {
  margin: 8px 0 0;
  font: 700 24px/1.1 var(--font-display);
}

.home-notice-board__eyebrow {
  color: rgba(17, 19, 34, 0.5);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.home-notice-board__count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
  height: 42px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.64);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.home-notice-board__featured,
.home-notice-board__item {
  width: 100%;
  border: 0;
  text-align: left;
}

.home-notice-board__featured {
  display: grid;
  gap: 14px;
  padding: 0;
  background: transparent;
}

.home-notice-board__featured-cover {
  min-height: 168px;
  border-radius: 22px;
  background:
    linear-gradient(135deg, rgba(17, 19, 34, 0.08), rgba(243, 190, 37, 0.18)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(241, 245, 249, 0.9));
  background-position: center;
  background-size: cover;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.74);
}

.home-notice-board__featured-content {
  display: grid;
  gap: 10px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(17, 19, 34, 0.95);
  color: rgba(255, 255, 255, 0.96);
}

.home-notice-board__featured-content h3,
.home-notice-board__item strong {
  margin: 0;
  font-size: 18px;
  line-height: 1.35;
}

.home-notice-board__featured-content p,
.home-notice-board__item p,
.home-notice-board__empty p {
  margin: 0;
  line-height: 1.7;
}

.home-notice-board__featured-content p {
  color: rgba(255, 255, 255, 0.72);
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

.home-notice-board__featured-content .home-notice-board__tag[data-type='notice'] {
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.92);
}

.home-notice-board__list {
  display: grid;
  gap: 12px;
}

.home-notice-board__item {
  display: grid;
  gap: 8px;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.04);
  transition:
    transform 180ms ease,
    background-color 180ms ease,
    box-shadow 180ms ease;
}

.home-notice-board__item:hover,
.home-notice-board__featured:hover {
  transform: translateY(-1px);
}

.home-notice-board__item:hover {
  background: rgba(17, 19, 34, 0.06);
  box-shadow: 0 14px 28px rgba(17, 19, 34, 0.06);
}

.home-notice-board__item-meta {
  display: flex;
  justify-content: space-between;
}

.home-notice-board__item p {
  color: rgba(17, 19, 34, 0.62);
  font-size: 14px;
}

.home-notice-board__empty {
  display: grid;
  gap: 8px;
  place-items: center;
  min-height: 260px;
  padding: 24px;
  border: 1px dashed rgba(17, 19, 34, 0.14);
  border-radius: 20px;
  color: rgba(17, 19, 34, 0.62);
  text-align: center;
}
</style>
