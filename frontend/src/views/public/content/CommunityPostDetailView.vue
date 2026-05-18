<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Collection, Clock, Promotion, Star } from '@element-plus/icons-vue'
import CommunityReplyTree from '@/components/community/CommunityReplyTree.vue'
import CommunityRichContent from '@/components/community/CommunityRichContent.vue'
import PublicContentShell from '@/components/home/PublicContentShell.vue'
import {
  createCommunityReply,
  favoriteCommunityPost,
  fetchCommunityPostDetail,
  fetchCommunityReplies,
  likeCommunityPost
} from '@/api/modules/community'
import { useAuthStore } from '@/stores/auth'
import type { CommunityPostItem, CommunityReplyItem } from '@/types/community'
import { buildReplyTree } from '@/utils/community'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const replying = ref(false)
const post = ref<CommunityPostItem | null>(null)
const replies = ref<CommunityReplyItem[]>([])
const replyContent = ref('')
const replyTarget = ref<CommunityReplyItem | null>(null)
const focusedReplyId = ref<number | null>(null)

const replyTree = computed(() => buildReplyTree(replies.value))
const activeReplyId = computed(() => replyTarget.value?.id || focusedReplyId.value)

watch(
  () => [route.params.id, route.query.replyId],
  async () => {
    await loadPage()
  },
  { immediate: true }
)

async function loadPage() {
  const postId = Number(route.params.id)
  if (!postId) {
    post.value = null
    replies.value = []
    focusedReplyId.value = null
    return
  }

  loading.value = true
  try {
    const [postResponse, replyResponse] = await Promise.all([
      fetchCommunityPostDetail(postId),
      fetchCommunityReplies(postId)
    ])
    post.value = postResponse.data || null
    replies.value = replyResponse.data || []
    syncFocusedReplyId()
    await scrollToFocusedReply()
  } catch (error: any) {
    post.value = null
    replies.value = []
    focusedReplyId.value = null
    ElMessage.error(error.message || '加载帖子详情失败')
  } finally {
    loading.value = false
  }
}

function syncFocusedReplyId() {
  const rawReplyId = Array.isArray(route.query.replyId) ? route.query.replyId[0] : route.query.replyId
  const parsedReplyId = Number(rawReplyId)
  focusedReplyId.value = Number.isInteger(parsedReplyId) && parsedReplyId > 0 ? parsedReplyId : null
}

async function scrollToFocusedReply() {
  if (!focusedReplyId.value) {
    return
  }
  await nextTick()
  const target = document.getElementById(`community-reply-${focusedReplyId.value}`)
  target?.scrollIntoView({
    behavior: 'smooth',
    block: 'center'
  })
}

function ensureLogin(actionText: string) {
  if (authStore.token) return true
  ElMessage.warning(`请先登录后${actionText}`)
  return false
}

function formatDate(value?: string) {
  if (!value) return '--'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

async function handlePostAction(type: 'like' | 'favorite') {
  if (!post.value) return
  if (!ensureLogin(type === 'like' ? '点赞' : '收藏')) return

  try {
    const response =
      type === 'like'
        ? await likeCommunityPost(post.value.id)
        : await favoriteCommunityPost(post.value.id)
    post.value = {
      ...post.value,
      ...(response.data || {})
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

function startReplyTo(reply: CommunityReplyItem) {
  replyTarget.value = reply
}

function clearReplyTarget() {
  replyTarget.value = null
}

async function submitReply() {
  if (!post.value) return
  if (!ensureLogin('回复')) return

  const content = replyContent.value.trim()
  if (content.length < 2) {
    ElMessage.warning('回复至少需要 2 个字')
    return
  }

  replying.value = true
  try {
    await createCommunityReply(post.value.id, {
      content,
      parentReplyId: replyTarget.value?.id || undefined
    })
    replyContent.value = ''
    clearReplyTarget()
    ElMessage.success('回复已发布')
    await loadPage()
  } catch (error: any) {
    ElMessage.error(error.message || '回复失败')
  } finally {
    replying.value = false
  }
}

function goBackToCommunity() {
  router.push('/community')
}

</script>

<template>
  <PublicContentShell back-label="返回社区广场" back-to="/community">
    <section v-if="loading" class="community-post-loading">正在加载完整帖子...</section>

    <section v-else-if="!post" class="community-post-empty">
      <h2>帖子暂时不可访问</h2>
      <p>它可能已下架、被删除，或者链接地址不完整。</p>
      <button class="market-btn market-btn--ghost" type="button" @click="goBackToCommunity">回到社区列表</button>
    </section>

    <template v-else>
      <section class="community-post-hero">
        <div class="community-post-hero__meta">
          <span class="community-post-hero__forum">{{ post.forumName }}</span>
          <span>{{ post.authorName }}</span>
          <span><el-icon><Clock /></el-icon>{{ formatDate(post.createdAt) }}</span>
        </div>
        <h1 class="community-post-hero__title">{{ post.title }}</h1>
        <p class="community-post-hero__summary">{{ post.summary }}</p>
      </section>

      <section class="community-post-layout">
        <article class="community-post-main">
          <section class="community-post-card">
            <div class="community-post-card__head">
              <h2>正文内容</h2>
              <div class="community-post-card__stats">
                <span><el-icon><ChatDotRound /></el-icon>{{ post.replyCount || 0 }} 回复</span>
                <span><el-icon><Star /></el-icon>{{ post.likeCount || 0 }} 点赞</span>
                <span><el-icon><Collection /></el-icon>{{ post.favoriteCount || 0 }} 收藏</span>
              </div>
            </div>
            <CommunityRichContent :content="post.content" />
          </section>

          <section class="community-post-card">
            <div class="community-post-card__head">
              <h2>社区回复</h2>
              <span class="community-post-card__caption">{{ replies.length }} 条参与记录</span>
            </div>

            <div v-if="!replies.length" class="community-post-empty-state">
              还没有人回复，欢迎你来补上第一条看法。
            </div>

            <div v-else class="community-post-replies">
              <CommunityReplyTree
                :replies="replyTree"
                :active-reply-id="activeReplyId"
                :format-date="formatDate"
                @reply="startReplyTo"
              />
            </div>
          </section>
        </article>

        <aside class="community-post-side">
          <section class="community-post-side__card">
            <h3>互动操作</h3>
            <div class="community-post-side__actions">
              <button
                class="community-post-side__action"
                :class="{ 'community-post-side__action--active': post.liked }"
                type="button"
                @click="handlePostAction('like')"
              >
                <el-icon><Star /></el-icon>
                {{ post.liked ? '已点赞' : '点赞帖子' }}
              </button>
              <button
                class="community-post-side__action"
                :class="{ 'community-post-side__action--active': post.favorited }"
                type="button"
                @click="handlePostAction('favorite')"
              >
                <el-icon><Collection /></el-icon>
                {{ post.favorited ? '已收藏' : '收藏帖子' }}
              </button>
              <button class="community-post-side__action community-post-side__action--dark" type="button" @click="goBackToCommunity">
                <el-icon><Promotion /></el-icon>
                返回社区列表
              </button>
            </div>
          </section>

          <section class="community-post-side__card">
            <div class="community-post-side__head">
              <h3>写回复</h3>
              <span v-if="replyTarget">回复 {{ replyTarget.authorName }}</span>
              <span v-else-if="!authStore.token">登录后即可参与</span>
            </div>

            <div v-if="replyTarget" class="community-post-side__replying">
              <span>当前目标：{{ replyTarget.authorName }}</span>
              <button type="button" @click="clearReplyTarget">取消</button>
            </div>

            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="8"
              maxlength="2000"
              show-word-limit
              :placeholder="replyTarget ? `补充你对 ${replyTarget.authorName} 的回应` : '补充你的经验、建议或协作思路'"
            />

            <div class="community-post-side__submit">
              <button class="market-btn market-btn--ghost" type="button" @click="replyContent = ''; clearReplyTarget()">清空</button>
              <button class="market-btn market-btn--primary" type="button" :disabled="replying" @click="submitReply">
                {{ replying ? '正在提交...' : '提交回复' }}
              </button>
            </div>
          </section>
        </aside>
      </section>
    </template>
  </PublicContentShell>
</template>

<style scoped>
.community-post-loading,
.community-post-empty {
  display: grid;
  gap: 12px;
  justify-items: start;
  padding: 40px 0 20px;
  color: rgba(17, 19, 34, 0.68);
}

.community-post-empty h2,
.community-post-empty p {
  margin: 0;
}

.community-post-hero {
  display: grid;
  gap: 16px;
  margin-top: 18px;
  padding: 28px 32px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 32px;
  background:
    radial-gradient(circle at top right, rgba(243, 190, 37, 0.18), transparent 24%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.92));
  box-shadow: 0 22px 42px rgba(17, 19, 34, 0.08);
}

.community-post-hero__meta,
.community-post-card__head,
.community-post-side__head,
.community-post-side__submit {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.community-post-hero__meta {
  flex-wrap: wrap;
  color: rgba(17, 19, 34, 0.54);
  font-size: 13px;
}

.community-post-hero__meta span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.community-post-hero__forum {
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(243, 190, 37, 0.18);
  color: #111322;
  font-weight: 700;
}

.community-post-hero__title {
  margin: 0;
  color: #111322;
  font: 700 42px/1.08 var(--font-display);
  letter-spacing: -0.05em;
}

.community-post-hero__summary {
  margin: 0;
  max-width: 66ch;
  color: rgba(17, 19, 34, 0.66);
  line-height: 1.85;
}

.community-post-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
  margin-top: 22px;
}

.community-post-main,
.community-post-side {
  display: grid;
  gap: 20px;
  align-content: start;
}

.community-post-card,
.community-post-side__card {
  padding: 24px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16px 32px rgba(17, 19, 34, 0.06);
}

.community-post-card {
  display: grid;
  gap: 18px;
}

.community-post-card__head {
  align-items: start;
}

.community-post-card__head h2,
.community-post-side__card h3 {
  margin: 0;
  color: #111322;
  font: 700 20px/1.2 var(--font-display);
}

.community-post-card__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: rgba(17, 19, 34, 0.56);
  font-size: 13px;
}

.community-post-card__stats span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.community-post-card__caption,
.community-post-side__head span {
  color: rgba(17, 19, 34, 0.52);
  font-size: 13px;
}

.community-post-empty-state {
  padding: 18px;
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.04);
  color: rgba(17, 19, 34, 0.58);
}

.community-post-replies {
  display: grid;
  gap: 14px;
}

.community-post-side {
  position: sticky;
  top: 96px;
}

.community-post-side__actions {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}

.community-post-side__action {
  min-height: 42px;
  padding: 0 16px;
  border: 0;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.76);
  font: 600 13px/1 var(--font-body);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
}

.community-post-side__action--active {
  background: rgba(243, 190, 37, 0.18);
  color: #111322;
}

.community-post-side__action--dark {
  background: #111322;
  color: #fff;
}

.community-post-side__replying {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 14px 0;
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(36, 87, 214, 0.08);
  color: #2457d6;
  font-size: 13px;
}

.community-post-side__replying button {
  border: 0;
  background: transparent;
  color: inherit;
  font: 600 13px/1 var(--font-body);
  cursor: pointer;
}

.community-post-side__submit {
  margin-top: 16px;
}

@media (max-width: 1080px) {
  .community-post-layout {
    grid-template-columns: 1fr;
  }

  .community-post-side {
    position: static;
  }
}

@media (max-width: 720px) {
  .community-post-hero {
    padding: 22px 20px;
  }

  .community-post-hero__title {
    font-size: 32px;
  }

  .community-post-card,
  .community-post-side__card {
    padding: 18px;
  }

  .community-post-card__head,
  .community-post-side__head,
  .community-post-side__submit {
    flex-direction: column;
    align-items: start;
  }
}
</style>
