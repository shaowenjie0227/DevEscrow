<template>
  <PublicContentShell back-label="返回接单大厅" back-to="/market">
    <section class="market-feed-card">
      <div class="market-feed-head">
        <div>
          <h2>技术文章</h2>
          <p>管理员发布的技术总结、案例拆解和经验沉淀。</p>
        </div>
      </div>
      <div class="market-list">
        <article class="demand-card">
          <div class="demand-card__media"></div>
          <div class="demand-card__body">
            <h3 class="demand-card__title" style="font-size: 22px;">如何设计一个高响应率的接单大厅</h3>
            <p class="demand-card__summary">从首页、搜索、分类到详情页的转化路径设计。</p>
          </div>
        </article>
      </div>
    </section>
  </PublicContentShell>
</template>

<script setup lang="ts">
import PublicContentShell from '@/components/home/PublicContentShell.vue'
</script>
