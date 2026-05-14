<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PublicContentShell from '@/components/home/PublicContentShell.vue'
import { fetchKnowledgeBases } from '@/api/modules/demand'

const items = ref<any[]>([])

async function loadItems() {
  const response = await fetchKnowledgeBases()
  items.value = response.data || []
}

onMounted(loadItems)
</script>

<template>
  <PublicContentShell back-label="返回接单大厅" back-to="/market">
    <section class="market-feed-card">
      <div class="market-feed-head">
        <div>
          <h2>知识库</h2>
          <p>管理员维护的技术百科，介绍每个技术适合做什么。</p>
        </div>
      </div>

      <div class="market-list">
        <article v-for="item in items" :key="item.id" class="demand-card">
          <div class="demand-card__media" style="display:grid;place-items:center;background:linear-gradient(135deg,#fff7c7,#fff);">
            <h3 style="font: 700 30px/1 var(--font-display);">{{ item.techName }}</h3>
          </div>
          <div class="demand-card__body">
            <div class="demand-card__head">
              <div>
                <h3 class="demand-card__title" style="font-size: 22px;">{{ item.title }}</h3>
                <p class="demand-card__summary">{{ item.intro }}</p>
              </div>
              <button class="market-btn market-btn--primary" type="button">查看</button>
            </div>
          </div>
        </article>
      </div>
    </section>
  </PublicContentShell>
</template>
