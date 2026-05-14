<template>
  <PublicContentShell back-label="返回接单大厅" back-to="/market">
    <section class="market-feed-card">
      <div class="market-feed-head">
        <div>
          <h2>技术路线图</h2>
          <p>按阶段展示学习与成长路径。</p>
        </div>
      </div>
      <div class="market-list">
        <article class="demand-card">
          <div class="demand-card__media" style="display:grid;place-items:center;background:linear-gradient(135deg,#fff7c7,#fff);">
            <h3 class="market-banner__title" style="font-size:36px;">01</h3>
          </div>
          <div class="demand-card__body">
            <h3 class="demand-card__title" style="font-size: 22px;">入门阶段</h3>
            <p class="demand-card__summary">前端基础、后端接口、项目结构。</p>
          </div>
        </article>
        <article class="demand-card">
          <div class="demand-card__media" style="display:grid;place-items:center;background:linear-gradient(135deg,#fff7c7,#fff);">
            <h3 class="market-banner__title" style="font-size:36px;">02</h3>
          </div>
          <div class="demand-card__body">
            <h3 class="demand-card__title" style="font-size: 22px;">实战阶段</h3>
            <p class="demand-card__summary">MVP、后台、社区、资源系统。</p>
          </div>
        </article>
      </div>
    </section>
  </PublicContentShell>
</template>

<script setup lang="ts">
import PublicContentShell from '@/components/home/PublicContentShell.vue'
</script>
