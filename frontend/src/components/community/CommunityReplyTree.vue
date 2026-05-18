<script setup lang="ts">
import type { CommunityReplyNode } from '@/types/community'

defineOptions({
  name: 'CommunityReplyTree'
})

const props = withDefaults(
  defineProps<{
    replies: CommunityReplyNode[]
    activeReplyId?: number | null
    formatDate: (value?: string) => string
  }>(),
  {
    activeReplyId: null
  }
)

defineEmits<{
  reply: [reply: CommunityReplyNode]
}>()
</script>

<template>
  <div class="community-reply-tree">
    <article
      v-for="reply in props.replies"
      :key="reply.id"
      :id="`community-reply-${reply.id}`"
      class="community-reply-tree__card"
      :class="{ 'community-reply-tree__card--active': props.activeReplyId === reply.id }"
      tabindex="-1"
    >
      <div class="community-reply-tree__top">
        <div class="community-reply-tree__meta">
          <strong>{{ reply.authorName }}</strong>
          <span v-if="reply.replyToAuthorName" class="community-reply-tree__target">回复 {{ reply.replyToAuthorName }}</span>
        </div>
        <span class="community-reply-tree__time">{{ props.formatDate(reply.createdAt) }}</span>
      </div>

      <p class="community-reply-tree__content">{{ reply.content }}</p>

      <div class="community-reply-tree__actions">
        <button class="community-reply-tree__reply-btn" type="button" @click="$emit('reply', reply)">回复这条评论</button>
      </div>

      <div v-if="reply.children.length" class="community-reply-tree__children">
        <CommunityReplyTree
          :replies="reply.children"
          :active-reply-id="props.activeReplyId"
          :format-date="props.formatDate"
          @reply="$emit('reply', $event)"
        />
      </div>
    </article>
  </div>
</template>

<style scoped>
.community-reply-tree {
  display: grid;
  gap: 14px;
}

.community-reply-tree__card {
  scroll-margin-top: 120px;
  padding: 16px 18px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 22px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.88));
  box-shadow: 0 12px 24px rgba(17, 19, 34, 0.05);
  outline: none;
}

.community-reply-tree__card--active {
  border-color: rgba(36, 87, 214, 0.34);
  box-shadow: 0 16px 28px rgba(36, 87, 214, 0.12);
}

.community-reply-tree__top,
.community-reply-tree__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.community-reply-tree__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.community-reply-tree__meta strong {
  color: #111322;
  font-size: 14px;
}

.community-reply-tree__target,
.community-reply-tree__time {
  color: rgba(17, 19, 34, 0.5);
  font-size: 12px;
}

.community-reply-tree__target {
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.05);
}

.community-reply-tree__content {
  margin: 12px 0 0;
  color: rgba(17, 19, 34, 0.72);
  line-height: 1.8;
  white-space: pre-wrap;
}

.community-reply-tree__actions {
  margin-top: 14px;
  justify-content: flex-end;
}

.community-reply-tree__reply-btn {
  border: 0;
  background: transparent;
  color: #2457d6;
  font: 600 13px/1 var(--font-body);
  cursor: pointer;
}

.community-reply-tree__children {
  margin-top: 14px;
  padding-left: 18px;
  border-left: 1px dashed rgba(17, 19, 34, 0.12);
}

@media (max-width: 720px) {
  .community-reply-tree__top {
    flex-direction: column;
    align-items: start;
  }

  .community-reply-tree__children {
    padding-left: 12px;
  }
}
</style>
