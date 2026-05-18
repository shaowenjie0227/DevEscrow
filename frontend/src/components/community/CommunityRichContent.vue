<script setup lang="ts">
withDefaults(
  defineProps<{
    content?: string
    compact?: boolean
  }>(),
  {
    content: '',
    compact: false
  }
)
</script>

<template>
  <div class="community-rich-content" :class="{ 'community-rich-content--compact': compact }" v-html="content" />
</template>

<style scoped>
.community-rich-content {
  color: rgba(17, 19, 34, 0.74);
  font-size: 15px;
  line-height: 1.9;
  word-break: break-word;
}

.community-rich-content--compact {
  font-size: 14px;
  line-height: 1.8;
}

.community-rich-content :deep(p),
.community-rich-content :deep(ul),
.community-rich-content :deep(ol),
.community-rich-content :deep(blockquote),
.community-rich-content :deep(pre),
.community-rich-content :deep(h1),
.community-rich-content :deep(h2),
.community-rich-content :deep(h3),
.community-rich-content :deep(h4) {
  margin: 0 0 14px;
}

.community-rich-content :deep(h1),
.community-rich-content :deep(h2),
.community-rich-content :deep(h3),
.community-rich-content :deep(h4) {
  color: #111322;
  font-family: var(--font-display);
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.community-rich-content :deep(h1) {
  font-size: 30px;
}

.community-rich-content :deep(h2) {
  font-size: 26px;
}

.community-rich-content :deep(h3) {
  font-size: 22px;
}

.community-rich-content :deep(ul),
.community-rich-content :deep(ol) {
  padding-left: 1.4em;
}

.community-rich-content :deep(li + li) {
  margin-top: 6px;
}

.community-rich-content :deep(blockquote) {
  padding: 12px 16px;
  border-left: 3px solid rgba(243, 190, 37, 0.68);
  border-radius: 0 18px 18px 0;
  background: rgba(243, 190, 37, 0.1);
  color: rgba(17, 19, 34, 0.7);
}

.community-rich-content :deep(pre) {
  overflow-x: auto;
  padding: 18px;
  border-radius: 20px;
  background: #111322;
  color: #f7f5ef;
  font-size: 13px;
  line-height: 1.7;
}

.community-rich-content :deep(code) {
  font-family: 'Courier New', monospace;
}

.community-rich-content :deep(:not(pre) > code) {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.08);
  color: #111322;
}

.community-rich-content :deep(a) {
  color: #2457d6;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.community-rich-content :deep(.ql-align-center) {
  text-align: center;
}

.community-rich-content :deep(.ql-align-right) {
  text-align: right;
}

.community-rich-content :deep(.ql-align-justify) {
  text-align: justify;
}

.community-rich-content :deep(.ql-size-large) {
  font-size: 1.2em;
}

.community-rich-content :deep(.ql-size-huge) {
  font-size: 1.4em;
}
</style>
