<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Collection, Link, Share, Star } from '@element-plus/icons-vue'
import PublicContentShell from '@/components/home/PublicContentShell.vue'
import { fetchResources } from '@/api/modules/demand'

type ResourceItem = {
  id: number
  title: string
  intro: string
  coverUrl?: string
  linkUrl?: string
  likeCount?: number
  favoriteCount?: number
  shareCount?: number
}

const router = useRouter()
const loading = ref(false)
const items = ref<ResourceItem[]>([])
const tags = ['工具模板', '面试资料', 'MVP 方案', 'AI 工具', '运营公告']

async function loadItems() {
  loading.value = true
  try {
    const response = await fetchResources()
    items.value = response.data || []
  } finally {
    loading.value = false
  }
}

function openItem(item: ResourceItem) {
  if (item.linkUrl) {
    window.open(item.linkUrl, '_blank', 'noreferrer')
  } else {
    router.push('/admin')
  }
}

onMounted(loadItems)
</script>

<template>
  <PublicContentShell back-label="返回接单大厅" back-to="/market">
    <section class="market-feed-card">
      <div class="market-feed-head">
        <div>
          <h2>资源分享</h2>
          <p>管理员发布的资源、模板和公告，用户可以点赞、收藏、分享。</p>
        </div>
      </div>

      <div class="market-side-list" style="margin-bottom: 16px;">
        <span v-for="tag in tags" :key="tag" class="market-side-pill">{{ tag }}</span>
      </div>

      <div v-if="loading" class="market-feed-empty">正在加载资源...</div>

      <div v-else class="market-list">
        <article v-for="item in items" :key="item.id" class="demand-card">
          <div class="demand-card__media">
            <img
              :src="item.coverUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80'"
              :alt="item.title"
            />
          </div>
          <div class="demand-card__body">
            <div class="demand-card__head">
              <div>
                <h3 class="demand-card__title" style="font-size: 22px;">{{ item.title }}</h3>
                <p class="demand-card__summary">{{ item.intro }}</p>
              </div>
              <button class="demand-card__cta" type="button" @click="openItem(item)">
                <el-icon><Link /></el-icon>
                打开资源
              </button>
            </div>
            <div class="demand-card__footer">
              <div class="demand-card__facts">
                <span><el-icon><Star /></el-icon> {{ item.likeCount || 0 }} 点赞</span>
                <span><el-icon><Collection /></el-icon> {{ item.favoriteCount || 0 }} 收藏</span>
                <span><el-icon><Share /></el-icon> {{ item.shareCount || 0 }} 分享</span>
              </div>
            </div>
          </div>
        </article>
      </div>
    </section>
  </PublicContentShell>
</template>
