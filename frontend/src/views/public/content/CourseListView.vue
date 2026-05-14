<template>
  <PublicContentShell back-label="返回接单大厅" back-to="/market">
    <section class="market-feed-card">
      <div class="market-feed-head">
        <div>
          <h2>推荐课程</h2>
          <p>管理员精选的课程和学习路线。</p>
        </div>
      </div>
      <div class="market-list">
        <article class="demand-card">
          <div class="demand-card__media"></div>
          <div class="demand-card__body">
            <h3 class="demand-card__title" style="font-size: 22px;">Vue3 + SpringBoot 实战课</h3>
            <p class="demand-card__summary">适合接单开发者快速上手。</p>
          </div>
        </article>
      </div>
    </section>
  </PublicContentShell>
</template>

<script setup lang="ts">
import PublicContentShell from '@/components/home/PublicContentShell.vue'
</script>
