<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Collection, Plus, Star } from '@element-plus/icons-vue'
import PublicContentShell from '@/components/home/PublicContentShell.vue'
import {
  createCommunityPost,
  createCommunityReply,
  favoriteCommunityPost,
  fetchCommunityPosts,
  fetchCommunityReplies,
  likeCommunityPost
} from '@/api/modules/community'
import { useAuthStore } from '@/stores/auth'

type ThreadItem = {
  id: number
  title: string
  authorName: string
  forumName: string
  summary: string
  content: string
  replyCount: number
  likeCount: number
  favoriteCount: number
  createdAt?: string
}

const authStore = useAuthStore()
const tabs = ['全部', '最新', '热门']
const currentTab = ref('全部')
const loading = ref(false)
const threads = ref<ThreadItem[]>([])
const replies = ref<any[]>([])
const postDialogVisible = ref(false)
const replyDialogVisible = ref(false)
const posting = ref(false)
const replying = ref(false)
const activePost = ref<ThreadItem | null>(null)

const forums = ['前端吧', '后端吧', 'AI 吧', '接单吧', '运维吧']

const postForm = reactive({
  forumName: '接单吧',
  title: '',
  summary: '',
  content: ''
})

const replyForm = reactive({
  content: ''
})

const filteredThreads = computed(() => threads.value)
const hotReplies = computed(() => replies.value.slice(0, 3))

async function loadPosts() {
  loading.value = true
  try {
    const sort = currentTab.value === '热门' ? 'hot' : 'latest'
    const response = await fetchCommunityPosts({ sort })
    threads.value = response.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载社区帖子失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  if (!authStore.token) {
    ElMessage.warning('请先登录后再发帖')
    return
  }
  postDialogVisible.value = true
}

async function submitPost() {
  posting.value = true
  try {
    await createCommunityPost({ ...postForm })
    ElMessage.success('发帖成功')
    postDialogVisible.value = false
    postForm.forumName = '接单吧'
    postForm.title = ''
    postForm.summary = ''
    postForm.content = ''
    await loadPosts()
  } catch (error: any) {
    ElMessage.error(error.message || '发帖失败')
  } finally {
    posting.value = false
  }
}

async function handleLike(postId: number) {
  try {
    await likeCommunityPost(postId)
    await loadPosts()
  } catch (error: any) {
    ElMessage.error(error.message || '点赞失败')
  }
}

async function handleFavorite(postId: number) {
  try {
    await favoriteCommunityPost(postId)
    await loadPosts()
  } catch (error: any) {
    ElMessage.error(error.message || '收藏失败')
  }
}

async function openReplies(post: ThreadItem) {
  activePost.value = post
  replyDialogVisible.value = true
  const response = await fetchCommunityReplies(post.id)
  replies.value = response.data || []
}

async function submitReply() {
  if (!activePost.value) return
  replying.value = true
  try {
    await createCommunityReply(activePost.value.id, { ...replyForm })
    ElMessage.success('回复成功')
    replyForm.content = ''
    await openReplies(activePost.value)
    await loadPosts()
  } catch (error: any) {
    ElMessage.error(error.message || '回复失败')
  } finally {
    replying.value = false
  }
}

onMounted(loadPosts)
</script>

<template>
  <PublicContentShell back-label="返回接单大厅" back-to="/market">
    <section class="market-feed-card">
      <div class="market-feed-head">
        <div>
          <h2>交流社区</h2>
          <p>按贴吧玩法做的社区：吧单、帖子流、楼层热评、热门切换。</p>
        </div>
        <button class="market-btn market-btn--primary" type="button" @click="openCreate">
          <el-icon><Plus /></el-icon>
          发帖求助
        </button>
      </div>

      <div class="market-side-list" style="margin-bottom: 14px;">
        <button v-for="tab in tabs" :key="tab" class="market-chip" type="button" @click="currentTab = tab; loadPosts()">{{ tab }}</button>
      </div>

      <div class="market-layout" style="grid-template-columns: 260px minmax(0, 1fr); margin-top: 0;">
        <aside class="market-sidebar" style="top: 88px;">
          <article class="market-side-card">
            <h3 class="market-side-card__title">吧单</h3>
            <div class="market-side-list">
              <span v-for="item in forums" :key="item" class="market-side-pill">{{ item }}</span>
            </div>
          </article>

          <article class="market-side-card">
            <h3 class="market-side-card__title">热评楼层</h3>
            <div class="market-side-list" style="flex-direction: column; gap: 12px;">
              <div v-for="(reply, index) in hotReplies" :key="reply.id" class="market-side-pill" style="align-items: start;">
                <b>{{ index + 1 }}楼</b>
                <span>{{ reply.content }}</span>
              </div>
              <span v-if="!hotReplies.length" class="market-side-pill">先去帖子里发出第一条回复吧。</span>
            </div>
          </article>
        </aside>

        <div class="market-list">
          <div v-if="loading" class="market-feed-empty">正在加载社区内容...</div>

          <article v-for="thread in filteredThreads" :key="thread.id" class="demand-card">
            <div class="demand-card__media" style="min-height: 180px; display: grid; place-items: center; background: linear-gradient(135deg, #fff7c7, #fff);">
              <div style="text-align: center;">
                <p style="margin: 0; color: rgba(17,17,17,.5); font-size: 12px;">{{ thread.forumName }}</p>
                <h3 style="margin: 8px 0 0; font: 700 24px/1 var(--font-display);">{{ thread.title.slice(0, 8) }}...</h3>
              </div>
            </div>
            <div class="demand-card__body">
              <div class="demand-card__head">
                <div>
                  <h3 class="demand-card__title" style="font-size: 22px;">{{ thread.title }}</h3>
                  <p class="demand-card__category">{{ thread.authorName }} · {{ thread.forumName }}</p>
                </div>
              </div>
              <div class="demand-card__summary">{{ thread.summary }}</div>
              <div class="demand-card__footer">
                <div class="demand-card__facts">
                  <button type="button" class="market-inline-action" @click="openReplies(thread)">
                    <el-icon><ChatDotRound /></el-icon> {{ thread.replyCount || 0 }} 回复
                  </button>
                  <button type="button" class="market-inline-action" @click="handleLike(thread.id)">
                    <el-icon><Star /></el-icon> {{ thread.likeCount || 0 }} 赞
                  </button>
                  <button type="button" class="market-inline-action" @click="handleFavorite(thread.id)">
                    <el-icon><Collection /></el-icon> {{ thread.favoriteCount || 0 }} 收藏
                  </button>
                </div>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>

    <el-dialog v-model="postDialogVisible" title="发帖求助" width="640px">
      <el-form :model="postForm" label-position="top">
        <el-form-item label="所在版块">
          <el-select v-model="postForm.forumName" style="width: 100%">
            <el-option v-for="item in forums" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="postForm.title" placeholder="一句话说明你的问题或经验分享" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="postForm.summary" placeholder="先给大家一个快速预览" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="postForm.content" type="textarea" :rows="6" placeholder="详细描述你的问题、背景和你已经尝试过的方案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="posting" @click="submitPost">发布帖子</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="replyDialogVisible" :title="activePost?.title || '帖子回复'" width="720px">
      <div v-if="activePost" class="market-reply-thread">
        <p class="market-reply-thread__forum">{{ activePost.forumName }} · {{ activePost.authorName }}</p>
        <p class="market-reply-thread__content">{{ activePost.content }}</p>
      </div>
      <div class="market-reply-list">
        <article v-for="reply in replies" :key="reply.id" class="market-reply-card">
          <strong>{{ reply.authorName }}</strong>
          <p>{{ reply.content }}</p>
        </article>
        <div v-if="!replies.length" class="market-feed-empty">还没有回复，来抢沙发吧。</div>
      </div>
      <el-form :model="replyForm" label-position="top" style="margin-top: 16px;">
        <el-form-item label="回复内容">
          <el-input v-model="replyForm.content" type="textarea" :rows="4" placeholder="补充你的经验、方案或建议" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="replying" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </PublicContentShell>
</template>

<style scoped>
.market-inline-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}

.market-reply-thread {
  border-radius: 18px;
  padding: 16px 18px;
  background: #f8f9fc;
}

.market-reply-thread__forum {
  margin: 0 0 8px;
  font-size: 13px;
  color: rgba(17, 17, 17, 0.56);
}

.market-reply-thread__content {
  margin: 0;
  line-height: 1.8;
}

.market-reply-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.market-reply-card {
  border-radius: 18px;
  padding: 14px 16px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.market-reply-card p {
  margin: 8px 0 0;
  color: rgba(17, 17, 17, 0.72);
  line-height: 1.7;
}
</style>
