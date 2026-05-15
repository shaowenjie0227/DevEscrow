<script setup lang="ts">
import { computed } from 'vue'
import { ArrowRight, Clock, Location, Money } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { DemandOrderItem } from '@/types/home'

const props = withDefaults(
  defineProps<{
    item: DemandOrderItem
    compact?: boolean
  }>(),
  {
    compact: false
  }
)

const router = useRouter()

const previewDeliverables = computed(() => {
  if (props.compact) return props.item.deliverables.slice(0, 1)
  return props.item.deliverables.slice(0, 2)
})
const previewHighlights = computed(() => {
  if (props.compact) return props.item.highlights.slice(0, 2)
  return props.item.highlights
})
const serial = computed(() => `NO.${String(props.item.id).padStart(3, '0')}`)

function goDetail() {
  router.push(`/market/demand/${props.item.id}`)
}
</script>

<template>
  <button
    class="demand-card demand-card--editorial"
    :class="{ 'demand-card--compact': compact }"
    type="button"
    @click="goDetail"
  >
    <div class="demand-card__media">
      <img :src="item.coverImage" :alt="item.title" />
      <div class="demand-card__meta">
        <p class="demand-card__meta-title">{{ item.publisher }}</p>
        <p class="demand-card__meta-sub">{{ item.location }} · {{ item.postedAt }}</p>
      </div>
    </div>

    <div class="demand-card__body">
      <div class="demand-card__head">
        <div>
          <span class="demand-card__serial">{{ serial }}</span>
          <h3 class="demand-card__title">{{ item.title }}</h3>
          <p class="demand-card__category">{{ item.category }}</p>
        </div>
        <span class="demand-card__budget">{{ item.budget }}</span>
      </div>

      <div class="demand-card__summary">{{ item.summary }}</div>

      <div class="demand-card__facts">
        <span>
          <el-icon><Clock /></el-icon>
          {{ item.deadline }}
        </span>
        <span>
          <el-icon><Location /></el-icon>
          {{ item.location }}
        </span>
        <span>
          <el-icon><Money /></el-icon>
          {{ item.contactHint }}
        </span>
      </div>

      <div v-if="!compact" class="demand-card__tags">
        <span v-for="tag in item.tags" :key="tag.label" class="demand-card__tag">{{ tag.label }}</span>
      </div>

      <div v-if="previewDeliverables.length && !compact" class="demand-card__deliverables">
        <span class="demand-card__deliverables-label">交付范围</span>
        <ul>
          <li v-for="point in previewDeliverables" :key="point">{{ point }}</li>
        </ul>
      </div>

      <div v-if="previewHighlights.length" class="demand-card__pills" :class="{ 'demand-card__pills--compact': compact }">
        <span v-for="point in previewHighlights" :key="point" class="demand-card__pill">{{ point }}</span>
      </div>

      <div class="demand-card__footer">
        <span v-if="!compact" class="demand-card__footer-note">信息完整后，更容易当天进入报价。</span>
        <span class="demand-card__cta">
          查看需求详情
          <el-icon><ArrowRight /></el-icon>
        </span>
      </div>
    </div>
  </button>
</template>

<style scoped>
.demand-card--editorial {
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 20px;
  border-radius: 20px;
  border-color: rgba(17, 19, 34, 0.1);
  box-shadow: 0 18px 34px rgba(17, 19, 34, 0.05);
}

.demand-card__media {
  border-radius: 18px;
}

.demand-card__serial {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.54);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.demand-card__title {
  margin-top: 10px;
  max-width: 18ch;
}

.demand-card__budget {
  min-height: 42px;
  padding: 0 16px;
  border-radius: 12px;
  background: #111322;
  color: #fff;
}

.demand-card__facts {
  padding: 12px 0;
  border-top: 1px solid rgba(17, 19, 34, 0.08);
  border-bottom: 1px solid rgba(17, 19, 34, 0.08);
}

.demand-card__tag,
.demand-card__pill {
  min-height: 30px;
  border-radius: 10px;
}

.demand-card__deliverables {
  display: grid;
  gap: 10px;
  padding: 14px;
  border-radius: 14px;
  background: rgba(17, 19, 34, 0.04);
}

.demand-card__deliverables-label {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  min-height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(243, 190, 37, 0.16);
  color: rgba(17, 19, 34, 0.72);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.demand-card__deliverables ul {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.demand-card__deliverables li {
  position: relative;
  padding-left: 14px;
  color: rgba(17, 19, 34, 0.66);
  line-height: 1.7;
}

.demand-card__deliverables li::before {
  content: "";
  position: absolute;
  top: 10px;
  left: 0;
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: rgba(243, 190, 37, 0.88);
}

.demand-card__footer {
  align-items: center;
}

.demand-card__footer-note {
  color: rgba(17, 19, 34, 0.52);
  font-size: 13px;
  line-height: 1.6;
}

.demand-card__cta {
  border-radius: 12px;
}

.demand-card--compact {
  grid-template-columns: 148px minmax(0, 1fr);
  gap: 10px;
  padding: 10px;
  border-radius: 16px;
}

.demand-card--compact .demand-card__media {
  min-height: 96px;
  border-radius: 14px;
}

.demand-card--compact .demand-card__meta {
  padding: 8px 10px;
}

.demand-card--compact .demand-card__meta-title {
  font-size: 12px;
}

.demand-card--compact .demand-card__meta-sub {
  margin-top: 2px;
  font-size: 11px;
}

.demand-card--compact .demand-card__body {
  gap: 8px;
  padding: 0;
}

.demand-card--compact .demand-card__head {
  align-items: start;
  gap: 8px;
}

.demand-card--compact .demand-card__title {
  margin-top: 6px;
  font-size: 16px;
  line-height: 1.2;
  max-width: 12ch;
}

.demand-card--compact .demand-card__category {
  margin-top: 4px;
  font-size: 12px;
}

.demand-card--compact .demand-card__summary {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  font-size: 13px;
  line-height: 1.45;
}

.demand-card--compact .demand-card__budget {
  min-height: 32px;
  padding: 0 12px;
  font-size: 13px;
  white-space: nowrap;
}

.demand-card--compact .demand-card__facts {
  gap: 10px;
  padding: 8px 0 0;
  border-top: 0;
  border-bottom: 0;
}

.demand-card--compact .demand-card__facts span {
  gap: 6px;
  font-size: 12px;
}

.demand-card--compact .demand-card__facts span:nth-child(3) {
  display: none;
}

.demand-card__pills--compact {
  gap: 6px;
}

.demand-card--compact .demand-card__pill {
  min-height: 24px;
  padding: 0 8px;
  font-size: 11px;
}

.demand-card--compact .demand-card__footer {
  justify-content: end;
  gap: 10px;
}

.demand-card--compact .demand-card__cta {
  min-height: 32px;
  padding: 0 12px;
  font-size: 12px;
  gap: 6px;
}

@media (max-width: 720px) {
  .demand-card--editorial {
    grid-template-columns: 1fr;
  }
}
</style>
