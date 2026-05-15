<script setup lang="ts">
const browseLinks = [
  { label: '需求大厅', to: '/market' },
  { label: '资源分享', to: '/resources' },
  { label: '交流社区', to: '/community' }
]

const knowledgeLinks = [
  { label: '技术文章', to: '/articles' },
  { label: '课程推荐', to: '/courses' },
  { label: '知识库', to: '/knowledge-base' }
]

const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="market-footer">
    <div class="market-container market-footer__grid">
      <section class="market-footer__brand">
        <span class="market-footer__eyebrow">DevEscrow</span>
        <h2>把浏览、沟通、报价、托管与交付，串成一条更可信的协作链路。</h2>
        <p>
          面向程序员接单与需求发布场景，强化需求发现、交易担保、阶段交付与纠纷处理的完整体验。
        </p>
      </section>

      <nav class="market-footer__column">
        <span class="market-footer__title">浏览入口</span>
        <router-link
          v-for="item in browseLinks"
          :key="item.to"
          :to="item.to"
          class="market-footer__link"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <nav class="market-footer__column">
        <span class="market-footer__title">内容频道</span>
        <router-link
          v-for="item in knowledgeLinks"
          :key="item.to"
          :to="item.to"
          class="market-footer__link"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <section class="market-footer__column market-footer__column--promise">
        <span class="market-footer__title">产品承诺</span>
        <p class="market-footer__promise">先明确，再报价。</p>
        <p class="market-footer__promise">先托管，再开工。</p>
        <p class="market-footer__promise">先留痕，再交付。</p>
        <p class="market-footer__copyright">© {{ currentYear }} DevEscrow</p>
      </section>
    </div>
  </footer>
</template>
