<script setup lang="ts">
import { ArrowRight, Clock, Location, Money } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { DemandOrderItem } from '@/types/home'

const props = defineProps<{
  item: DemandOrderItem
}>()

const router = useRouter()

function goDetail() {
  router.push(`/market/demand/${props.item.id}`)
}
</script>

<template>
  <button class="demand-card" type="button" @click="goDetail">
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
          <h3 class="demand-card__title">{{ item.title }}</h3>
          <p class="demand-card__category">{{ item.category }}</p>
        </div>
        <span class="demand-card__budget">{{ item.budget }}</span>
      </div>

      <div class="demand-card__summary">{{ item.summary }}</div>

      <div class="demand-card__tags">
        <span v-for="tag in item.tags" :key="tag.label" class="demand-card__tag">{{ tag.label }}</span>
      </div>

      <div>
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

        <div class="demand-card__pills">
          <span v-for="point in item.highlights" :key="point" class="demand-card__pill">{{ point }}</span>
        </div>
      </div>

      <div class="demand-card__footer">
        <span></span>
        <span class="demand-card__cta">
          查看需求详情
          <el-icon><ArrowRight /></el-icon>
        </span>
      </div>
    </div>
  </button>
</template>
