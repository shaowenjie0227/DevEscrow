<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Clock,
  Collection,
  Plus,
  Promotion,
  Reading,
  Search,
  Star,
  TopRight
} from '@element-plus/icons-vue'
import CommunityReplyTree from '@/components/community/CommunityReplyTree.vue'
import CommunityRichContent from '@/components/community/CommunityRichContent.vue'
import CommunityRichTextEditor from '@/components/community/CommunityRichTextEditor.vue'
import PublicContentShell from '@/components/home/PublicContentShell.vue'
import {
  createCommunityPost,
  createCommunityReply,
  favoriteCommunityPost,
  fetchCommunityPostDetail,
  fetchCommunityPosts,
  fetchCommunityReplies,
  likeCommunityPost
} from '@/api/modules/community'
import { useAuthStore } from '@/stores/auth'
import type { CommunityPostItem, CommunityReplyItem } from '@/types/community'
import { buildReplyTree, htmlToPlainText } from '@/utils/community'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const forumOptions = ['全部', '接单问答', '前端开发', '后端架构', 'AI 应用', '项目协作', '运维部署']
const createForumOptions = forumOptions.filter((item) => item !== '全部')
const sortOptions = [
  { label: '最新发布', value: 'latest' },
  { label: '热门讨论', value: 'hot' },
  { label: '待回复', value: 'unanswered' }
]

const loading = ref(false)
const loadingDetail = ref(false)
const posting = ref(false)
const replying = ref(false)
const postDialogVisible = ref(false)
const detailVisible = ref(false)
const threads = ref<CommunityPostItem[]>([])
const replies = ref<CommunityReplyItem[]>([])
const activePost = ref<CommunityPostItem | null>(null)
const replyTarget = ref<CommunityReplyItem | null>(null)

const filters = reactive({
  keyword: '',
  forumName: '全部',
  sort: 'latest',
  mine: false
})

const postForm = reactive({
  forumName: '接单问答',
  title: '',
  summary: '',
  content: ''
})

const replyForm = reactive({
  content: ''
})

const titleLength = computed(() => postForm.title.trim().length)
const contentLength = computed(() => htmlToPlainText(postForm.content).length)
const titleRemaining = computed(() => Math.max(0, 4 - titleLength.value))
const contentRemaining = computed(() => Math.max(0, 10 - contentLength.value))
const canSubmitPost = computed(
  () => Boolean(authStore.token) && titleRemaining.value === 0 && contentRemaining.value === 0
)
const replyTree = computed(() => buildReplyTree(replies.value))
const postSubmitHint = computed(() => {
  if (!authStore.token) return '登录后即可发布帖子'
  if (titleRemaining.value > 0) return `标题还差 ${titleRemaining.value} 个字`
  if (contentRemaining.value > 0) return `正文还差 ${contentRemaining.value} 个字`
  return '内容已满足发布要求，可以直接发布'
})

const communityMetrics = computed(() => {
  const totalReplies = threads.value.reduce((sum, item) => sum + (item.replyCount || 0), 0)
  const unanswered = threads.value.filter((item) => !item.replyCount).length
  return [
    { label: '帖子总量', value: threads.value.length, hint: '支持按版块和关键词筛选' },
    { label: '活跃回复', value: totalReplies, hint: '回复后会实时回写到帖子热度' },
    { label: '待协作', value: unanswered, hint: '优先处理还没人接住的问题' }
  ]
})

const featuredThreads = computed(() => {
  return [...threads.value]
    .sort((left, right) => hotScore(right) - hotScore(left) || dateValue(right.createdAt) - dateValue(left.createdAt))
    .slice(0, 5)
})

const emptyCopy = computed(() => {
  if (filters.mine) return '你还没有发过帖子，发一个问题或经验分享试试。'
  if (filters.keyword.trim()) return '没有找到匹配内容，换个关键词或版块试试。'
  return '社区里还没有内容，第一篇帖子就从你开始。'
})

watch(
  () => route.query.post,
  async (postId) => {
    const numericId = Number(postId)
    if (!numericId) {
      detailVisible.value = false
      activePost.value = null
      replies.value = []
      replyForm.content = ''
      clearReplyTarget()
      return
    }
    await loadPostDetail(numericId)
  },
  { immediate: true }
)

async function loadPosts(options: { keepSelection?: boolean } = {}) {
  const { keepSelection = true } = options
  loading.value = true
  try {
    const response = await fetchCommunityPosts({
      sort: filters.sort,
      forumName: filters.forumName === '全部' ? undefined : filters.forumName,
      keyword: filters.keyword.trim() || undefined,
      mine: filters.mine || undefined
    })
    threads.value = sortThreadList(response.data || [])

    if (keepSelection && activePost.value) {
      const nextActive = threads.value.find((item) => item.id === activePost.value?.id)
      if (nextActive) {
        activePost.value = { ...activePost.value, ...nextActive }
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载社区帖子失败')
  } finally {
    loading.value = false
  }
}

async function loadPostDetail(postId: number) {
  loadingDetail.value = true
  detailVisible.value = true
  try {
    const [postResponse, replyResponse] = await Promise.all([
      fetchCommunityPostDetail(postId),
      fetchCommunityReplies(postId)
    ])
    activePost.value = postResponse.data || null
    replies.value = replyResponse.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载帖子详情失败')
    await closeDetail()
  } finally {
    loadingDetail.value = false
  }
}

function ensureLogin(actionText: string) {
  if (authStore.token) return true
  ElMessage.warning(`请先登录后${actionText}`)
  return false
}

function applyFilters() {
  loadPosts({ keepSelection: false })
}

function switchForum(forumName: string) {
  filters.forumName = forumName
  loadPosts({ keepSelection: false })
}

function switchSort(sortValue: string) {
  filters.sort = sortValue
  loadPosts({ keepSelection: false })
}

function toggleMineFilter() {
  if (!filters.mine && !ensureLogin('查看我的帖子')) {
    return
  }
  filters.mine = !filters.mine
  loadPosts({ keepSelection: false })
}

function resetFilters() {
  filters.keyword = ''
  filters.forumName = '全部'
  filters.sort = 'latest'
  filters.mine = false
  loadPosts({ keepSelection: false })
}

function openCreate() {
  if (!ensureLogin('发帖')) return
  postDialogVisible.value = true
}

function resetPostForm() {
  postForm.forumName = '接单问答'
  postForm.title = ''
  postForm.summary = ''
  postForm.content = ''
}

function validatePostForm() {
  const contentText = htmlToPlainText(postForm.content)
  if (!postForm.title.trim()) {
    ElMessage.warning('请先填写标题')
    return false
  }
  if (postForm.title.trim().length < 4) {
    ElMessage.warning('标题至少需要 4 个字')
    return false
  }
  if (!postForm.content.trim()) {
    ElMessage.warning('请先填写正文')
    return false
  }
  if (contentText.length < 10) {
    ElMessage.warning('正文至少需要 10 个字')
    return false
  }
  return true
}

async function submitPost() {
  if (!ensureLogin('发帖')) return
  if (!validatePostForm()) return

  posting.value = true
  try {
    const response = await createCommunityPost({
      forumName: postForm.forumName,
      title: postForm.title.trim(),
      summary: postForm.summary.trim() || undefined,
      content: postForm.content.trim()
    })
    const createdPost = response.data
    ElMessage.success('帖子已发布，快去等回复吧')
    postDialogVisible.value = false
    resetPostForm()
    await loadPosts({ keepSelection: false })
    if (createdPost?.id) {
      await openPreview(createdPost.id)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发帖失败')
  } finally {
    posting.value = false
  }
}

async function openPreview(postId: number) {
  await router.replace({
    path: route.path,
    query: {
      ...route.query,
      post: String(postId)
    }
  })
}

async function closeDetail() {
  replyForm.content = ''
  clearReplyTarget()
  const nextQuery = { ...route.query }
  delete nextQuery.post
  await router.replace({
    path: route.path,
    query: nextQuery
  })
}

function openPostPage(postId: number) {
  router.push(`/community/posts/${postId}`)
}

async function handlePostAction(type: 'like' | 'favorite', postId: number) {
  if (!ensureLogin(type === 'like' ? '点赞' : '收藏')) return
  try {
    const response =
      type === 'like' ? await likeCommunityPost(postId) : await favoriteCommunityPost(postId)
    mergeUpdatedPost(response.data)
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

function mergeUpdatedPost(updatedPost?: CommunityPostItem) {
  if (!updatedPost) return
  threads.value = sortThreadList(
    threads.value.map((item) => (item.id === updatedPost.id ? { ...item, ...updatedPost } : item))
  )
  if (activePost.value?.id === updatedPost.id) {
    activePost.value = { ...activePost.value, ...updatedPost }
  }
}

function startReplyTo(reply: CommunityReplyItem) {
  replyTarget.value = reply
}

function clearReplyTarget() {
  replyTarget.value = null
}

async function submitReply() {
  if (!activePost.value) return
  if (!ensureLogin('回复')) return
  const content = replyForm.content.trim()
  if (content.length < 2) {
    ElMessage.warning('回复至少需要 2 个字')
    return
  }

  replying.value = true
  try {
    await createCommunityReply(activePost.value.id, {
      content,
      parentReplyId: replyTarget.value?.id || undefined
    })
    replyForm.content = ''
    clearReplyTarget()
    ElMessage.success('回复已发布')
    await Promise.all([loadPosts(), loadPostDetail(activePost.value.id)])
  } catch (error: any) {
    ElMessage.error(error.message || '回复失败')
  } finally {
    replying.value = false
  }
}

function sortThreadList(list: CommunityPostItem[]) {
  const copied = [...list]
  if (filters.sort === 'hot') {
    return copied.sort(
      (left, right) => hotScore(right) - hotScore(left) || dateValue(right.createdAt) - dateValue(left.createdAt)
    )
  }
  if (filters.sort === 'unanswered') {
    return copied
      .filter((item) => !item.replyCount)
      .sort((left, right) => dateValue(right.createdAt) - dateValue(left.createdAt))
  }
  return copied.sort((left, right) => dateValue(right.createdAt) - dateValue(left.createdAt))
}

function hotScore(thread: CommunityPostItem) {
  return (thread.replyCount || 0) * 4 + (thread.favoriteCount || 0) * 3 + (thread.likeCount || 0) * 2
}

function dateValue(value?: string) {
  return value ? new Date(value).getTime() : 0
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

onMounted(() => {
  loadPosts({ keepSelection: false })
})
</script>

<template>
  <PublicContentShell back-label="返回接单大厅" back-to="/market">
    <section class="community-hero market-banner">
      <div class="market-banner__inner community-hero__inner">
        <div class="market-banner__copy">
          <span class="market-kicker">Community Loop</span>
          <h1 class="market-banner__title community-hero__title">把问题发出来，让经验和协作在这里接住你。</h1>
          <p class="market-banner__desc">
            社区不只是帖子流，而是需求前沟通、技术问答、经验沉淀和项目协作的公共入口。支持搜索、按版块筛选、发帖、回复、点赞和收藏。
          </p>

          <div class="community-hero__search">
            <el-input
              v-model="filters.keyword"
              clearable
              placeholder="搜索标题、摘要或正文关键词"
              @keyup.enter="applyFilters"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <button class="market-btn market-btn--primary" type="button" @click="applyFilters">
              立即搜索
            </button>
          </div>

          <div class="market-banner__notes">
            <span>发帖支持求助、经验复盘、项目协作</span>
            <span>回复会自动回写热度和帖子状态</span>
          </div>
        </div>

        <div class="market-banner__stats">
          <article v-for="metric in communityMetrics" :key="metric.label" class="market-metric">
            <div class="market-metric__label">{{ metric.label }}</div>
            <div class="market-metric__value">{{ metric.value }}</div>
            <div class="market-metric__hint">{{ metric.hint }}</div>
          </article>
        </div>
      </div>
    </section>

    <section class="market-layout community-layout">
      <aside class="market-sidebar">
        <article class="market-side-card">
          <div class="community-side-head">
            <h3 class="market-side-card__title">版块筛选</h3>
            <button class="community-text-btn" type="button" @click="resetFilters">重置</button>
          </div>
          <div class="market-side-list">
            <button
              v-for="forum in forumOptions"
              :key="forum"
              class="community-filter-chip"
              :class="{ 'community-filter-chip--active': filters.forumName === forum }"
              type="button"
              @click="switchForum(forum)"
            >
              {{ forum }}
            </button>
          </div>
        </article>

        <article class="market-side-card">
          <div class="community-side-head">
            <h3 class="market-side-card__title">热门话题</h3>
            <span class="community-side-caption">按互动热度排序</span>
          </div>
          <div class="community-ranked-list">
            <button
              v-for="(thread, index) in featuredThreads"
              :key="thread.id"
              class="community-ranked-item"
              type="button"
              @click="openPreview(thread.id)"
            >
              <span class="community-ranked-item__index">0{{ index + 1 }}</span>
              <span class="community-ranked-item__body">
                <strong>{{ thread.title }}</strong>
                <small>{{ thread.replyCount || 0 }} 回复 · {{ thread.likeCount || 0 }} 点赞</small>
              </span>
            </button>
            <div v-if="!featuredThreads.length" class="market-feed-empty">还没有可推荐的话题。</div>
          </div>
        </article>

        <article class="market-side-card">
          <h3 class="market-side-card__title">建议发帖方式</h3>
          <div class="community-tips">
            <p>把背景、目标和已尝试方案写清楚，回复质量会高很多。</p>
            <p>如果是项目协作帖，建议在正文里带上时间、角色和交付边界。</p>
            <p>回复后会同步更新帖子热度，管理员也可以在后台做治理。</p>
          </div>
        </article>
      </aside>

      <section class="market-feed-card">
        <div class="market-feed-head community-feed-head">
          <div>
            <h2>交流社区</h2>
            <p>面向接单前沟通、技术讨论、经验沉淀和跨角色协作的公开广场。</p>
          </div>
          <div class="community-feed-head__actions">
            <button
              class="community-filter-chip"
              :class="{ 'community-filter-chip--active': filters.mine }"
              type="button"
              @click="toggleMineFilter"
            >
              我的帖子
            </button>
            <button class="market-btn market-btn--primary" type="button" @click="openCreate">
              <el-icon><Plus /></el-icon>
              发布帖子
            </button>
          </div>
        </div>

        <div class="community-sort-row">
          <button
            v-for="tab in sortOptions"
            :key="tab.value"
            class="community-sort-pill"
            :class="{ 'community-sort-pill--active': filters.sort === tab.value }"
            type="button"
            @click="switchSort(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>

        <div v-if="loading" class="market-feed-empty">正在加载社区内容...</div>

        <div v-else-if="!threads.length" class="community-empty">
          <el-icon class="community-empty__icon"><Reading /></el-icon>
          <h3>这里暂时还没有内容</h3>
          <p>{{ emptyCopy }}</p>
          <button class="market-btn market-btn--ghost" type="button" @click="openCreate">去发第一篇帖子</button>
        </div>

        <div v-else class="market-list">
          <article
            v-for="thread in threads"
            :key="thread.id"
            class="community-card"
            @click="openPreview(thread.id)"
          >
            <div class="community-card__top">
              <div class="community-card__meta">
                <span class="community-forum-pill">{{ thread.forumName }}</span>
                <span>{{ thread.authorName }}</span>
                <span>{{ formatDate(thread.createdAt) }}</span>
              </div>
              <button class="community-text-btn" type="button" @click.stop="openPostPage(thread.id)">
                <el-icon><TopRight /></el-icon>
                查看详情
              </button>
            </div>

            <h3 class="community-card__title">{{ thread.title }}</h3>
            <p class="community-card__summary">{{ thread.summary }}</p>

            <div class="community-card__content">{{ htmlToPlainText(thread.content) }}</div>

            <div class="community-card__footer">
              <div class="community-card__stats">
                <span><el-icon><ChatDotRound /></el-icon>{{ thread.replyCount || 0 }} 回复</span>
                <span><el-icon><Star /></el-icon>{{ thread.likeCount || 0 }} 点赞</span>
                <span><el-icon><Collection /></el-icon>{{ thread.favoriteCount || 0 }} 收藏</span>
              </div>

              <div class="community-card__actions">
                <button
                  class="community-action-btn"
                  :class="{ 'community-action-btn--active': thread.liked }"
                  type="button"
                  @click.stop="handlePostAction('like', thread.id)"
                >
                  <el-icon><Star /></el-icon>
                  {{ thread.liked ? '已点赞' : '点赞' }}
                </button>
                <button
                  class="community-action-btn"
                  :class="{ 'community-action-btn--active': thread.favorited }"
                  type="button"
                  @click.stop="handlePostAction('favorite', thread.id)"
                >
                  <el-icon><Collection /></el-icon>
                  {{ thread.favorited ? '已收藏' : '收藏' }}
                </button>
                <button class="community-action-btn community-action-btn--primary" type="button" @click.stop="openPreview(thread.id)">
                  <el-icon><Promotion /></el-icon>
                  参与讨论
                </button>
              </div>
            </div>
          </article>
        </div>
      </section>
    </section>

    <el-dialog v-model="postDialogVisible" title="发布社区帖子" width="860px">
      <div class="community-dialog-hint">
        标题说清主题，正文可直接写成技术贴。支持标题层级、列表、引用、链接和代码块，适合复盘、方案说明和协作讨论。
      </div>
      <el-form :model="postForm" label-position="top">
        <el-form-item label="版块">
          <el-select v-model="postForm.forumName" style="width: 100%">
            <el-option v-for="item in createForumOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="postForm.title" maxlength="128" show-word-limit placeholder="用一句话讲清你要讨论的问题或经验" />
          <div class="community-field-meta" :class="{ 'community-field-meta--invalid': titleRemaining > 0 }">
            {{ titleRemaining > 0 ? `标题至少 4 个字，还差 ${titleRemaining} 个字` : '标题字数已达标' }}
          </div>
        </el-form-item>
        <el-form-item label="摘要（选填）">
          <el-input
            v-model="postForm.summary"
            maxlength="255"
            show-word-limit
            placeholder="选填，留空会根据正文自动生成摘要"
          />
        </el-form-item>
        <el-form-item label="正文">
          <CommunityRichTextEditor v-model="postForm.content" />
          <div class="community-field-meta" :class="{ 'community-field-meta--invalid': contentRemaining > 0 }">
            {{ contentRemaining > 0 ? `正文至少 10 个字，还差 ${contentRemaining} 个字` : `正文已达标，当前 ${contentLength} 个字` }}
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="community-dialog-status" :class="{ 'community-dialog-status--invalid': !canSubmitPost }">
          {{ postSubmitHint }}
        </span>
        <el-button @click="postDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="posting" :disabled="!canSubmitPost" @click="submitPost">发布帖子</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" size="760px" :with-header="false" @close="closeDetail">
      <div v-if="loadingDetail" class="community-detail-loading">正在加载帖子详情...</div>

      <template v-else-if="activePost">
        <section class="community-detail">
          <div class="community-detail__head">
            <div>
              <div class="community-card__meta">
                <span class="community-forum-pill">{{ activePost.forumName }}</span>
                <span>{{ activePost.authorName }}</span>
                <span><el-icon><Clock /></el-icon>{{ formatDate(activePost.createdAt) }}</span>
              </div>
              <h2 class="community-detail__title">{{ activePost.title }}</h2>
            </div>

            <div class="community-detail__chips">
              <span class="community-detail__chip">{{ activePost.replyCount || 0 }} 条回复</span>
              <span class="community-detail__chip">{{ activePost.favoriteCount || 0 }} 次收藏</span>
            </div>
          </div>

          <section class="community-detail__section">
            <h3>摘要</h3>
            <p>{{ activePost.summary }}</p>
          </section>

          <section class="community-detail__section">
            <h3>正文</h3>
            <CommunityRichContent class="community-detail__content" :content="activePost.content" />
          </section>

          <div class="community-detail__actions">
            <button
              class="community-action-btn"
              :class="{ 'community-action-btn--active': activePost.liked }"
              type="button"
              @click="handlePostAction('like', activePost.id)"
            >
              <el-icon><Star /></el-icon>
              {{ activePost.liked ? '已点赞' : '点赞' }} · {{ activePost.likeCount || 0 }}
            </button>
            <button
              class="community-action-btn"
              :class="{ 'community-action-btn--active': activePost.favorited }"
              type="button"
              @click="handlePostAction('favorite', activePost.id)"
            >
              <el-icon><Collection /></el-icon>
              {{ activePost.favorited ? '已收藏' : '收藏' }} · {{ activePost.favoriteCount || 0 }}
            </button>
            <button class="community-action-btn" type="button" @click="openPostPage(activePost.id)">
              <el-icon><TopRight /></el-icon>
              进入完整帖子
            </button>
          </div>

          <section class="community-detail__section">
            <div class="community-detail__reply-head">
              <h3>全部回复</h3>
              <span>{{ replies.length }} 条</span>
            </div>

            <div v-if="!replies.length" class="community-detail__empty">
              还没有人回复，欢迎你来抢第一个沙发。
            </div>

            <div v-else class="community-reply-list">
              <CommunityReplyTree
                :replies="replyTree"
                :active-reply-id="replyTarget?.id"
                :format-date="formatDate"
                @reply="startReplyTo"
              />
            </div>
          </section>

          <section class="community-detail__section community-detail__composer">
            <div class="community-detail__reply-head">
              <h3>写回复</h3>
              <span v-if="replyTarget">正在回复 {{ replyTarget.authorName }}</span>
              <span v-else-if="!authStore.token">登录后即可参与讨论</span>
            </div>

            <div v-if="replyTarget" class="community-replying-badge">
              <span>回复 {{ replyTarget.authorName }}</span>
              <button type="button" @click="clearReplyTarget">取消</button>
            </div>

            <el-input
              v-model="replyForm.content"
              type="textarea"
              :rows="5"
              maxlength="2000"
              show-word-limit
              :placeholder="replyTarget ? `补充你对 ${replyTarget.authorName} 的回应` : '补充你的经验、建议或协作思路'"
            />

            <div class="community-detail__composer-actions">
              <button class="market-btn market-btn--ghost" type="button" @click="replyForm.content = ''">清空</button>
              <button class="market-btn market-btn--primary" type="button" :disabled="replying" @click="submitReply">
                {{ replying ? '正在提交...' : '提交回复' }}
              </button>
            </div>
          </section>
        </section>
      </template>
    </el-drawer>
  </PublicContentShell>
</template>

<style scoped>
.community-hero {
  margin-top: 16px;
}

.community-hero__inner {
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 360px);
}

.community-hero__title {
  max-width: 11ch;
}

.community-hero__search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
}

.community-layout {
  margin-top: 20px;
}

.community-side-head,
.community-feed-head__actions,
.community-card__top,
.community-card__footer,
.community-detail__head,
.community-detail__reply-head,
.community-detail__composer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.community-side-caption {
  color: rgba(17, 19, 34, 0.52);
  font-size: 12px;
}

.community-filter-chip,
.community-sort-pill,
.community-action-btn,
.community-text-btn {
  border: 0;
  cursor: pointer;
}

.community-filter-chip,
.community-sort-pill {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.74);
  font: 600 13px/1 var(--font-body);
  transition: transform 180ms ease, background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.community-filter-chip:hover,
.community-sort-pill:hover,
.community-text-btn:hover,
.community-action-btn:hover {
  transform: translateY(-1px);
}

.community-filter-chip--active,
.community-sort-pill--active {
  background: rgba(243, 190, 37, 0.18);
  color: #111322;
  box-shadow: 0 0 0 1px rgba(243, 190, 37, 0.3) inset;
}

.community-sort-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.community-text-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  background: transparent;
  color: rgba(17, 19, 34, 0.58);
  font: 600 13px/1 var(--font-body);
}

.community-ranked-list,
.community-tips,
.community-reply-list {
  display: grid;
  gap: 12px;
}

.community-ranked-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  padding: 12px 14px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  text-align: left;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.community-ranked-item:hover {
  transform: translateY(-1px);
  border-color: rgba(243, 190, 37, 0.34);
  box-shadow: 0 14px 26px rgba(17, 19, 34, 0.06);
}

.community-ranked-item__index {
  color: rgba(243, 190, 37, 0.9);
  font: 700 18px/1 var(--font-display);
}

.community-ranked-item__body {
  display: grid;
  gap: 6px;
}

.community-ranked-item__body strong {
  color: #111322;
  font-size: 14px;
  line-height: 1.5;
}

.community-ranked-item__body small {
  color: rgba(17, 19, 34, 0.52);
  font-size: 12px;
}

.community-tips p {
  margin: 0;
  color: rgba(17, 19, 34, 0.66);
  line-height: 1.75;
}

.community-empty {
  display: grid;
  justify-items: center;
  gap: 10px;
  padding: 42px 20px;
  border: 1px dashed rgba(17, 19, 34, 0.16);
  border-radius: 28px;
  text-align: center;
  background: rgba(17, 19, 34, 0.02);
}

.community-empty__icon {
  font-size: 28px;
  color: rgba(243, 190, 37, 0.9);
}

.community-empty h3,
.community-empty p {
  margin: 0;
}

.community-empty p {
  max-width: 34ch;
  color: rgba(17, 19, 34, 0.56);
  line-height: 1.75;
}

.community-card {
  display: grid;
  gap: 16px;
  padding: 22px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(243, 190, 37, 0.12), transparent 26%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.86));
  box-shadow: 0 18px 34px rgba(17, 19, 34, 0.06);
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.community-card:hover {
  transform: translateY(-2px);
  border-color: rgba(243, 190, 37, 0.28);
  box-shadow: 0 24px 44px rgba(17, 19, 34, 0.1);
}

.community-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  color: rgba(17, 19, 34, 0.52);
  font-size: 12px;
}

.community-forum-pill,
.community-detail__chip {
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(243, 190, 37, 0.16);
  color: #111322;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.community-card__title {
  margin: 0;
  font: 700 28px/1.18 var(--font-display);
  letter-spacing: -0.04em;
}

.community-card__summary,
.community-card__content,
.community-detail__section p {
  margin: 0;
  color: rgba(17, 19, 34, 0.68);
  line-height: 1.85;
}

.community-card__summary {
  font-weight: 600;
}

.community-card__content {
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.community-card__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: rgba(17, 19, 34, 0.58);
  font-size: 13px;
}

.community-card__stats span,
.community-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.community-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.community-action-btn {
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.76);
  font: 600 13px/1 var(--font-body);
  transition: transform 180ms ease, background-color 180ms ease, color 180ms ease, box-shadow 180ms ease;
}

.community-action-btn--active {
  background: rgba(243, 190, 37, 0.18);
  color: #111322;
}

.community-action-btn--primary {
  background: #111322;
  color: #fff;
}

.community-dialog-hint {
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.04);
  color: rgba(17, 19, 34, 0.62);
  line-height: 1.75;
}

.community-field-meta {
  margin-top: 8px;
  color: rgba(20, 128, 82, 0.88);
  font-size: 12px;
  line-height: 1.6;
}

.community-field-meta--invalid,
.community-dialog-status--invalid {
  color: #c2410c;
}

.community-dialog-status {
  margin-right: auto;
  max-width: 280px;
  color: rgba(17, 19, 34, 0.56);
  font-size: 13px;
  line-height: 1.6;
}

.community-detail-loading {
  padding: 32px 12px;
  color: rgba(17, 19, 34, 0.56);
}

.community-detail {
  display: grid;
  gap: 22px;
  padding-right: 6px;
}

.community-detail__head {
  align-items: start;
}

.community-detail__title {
  margin: 14px 0 0;
  font: 700 34px/1.12 var(--font-display);
  letter-spacing: -0.05em;
}

.community-detail__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.community-detail__section {
  display: grid;
  gap: 12px;
  padding: 20px;
  border-radius: 24px;
  background: rgba(17, 19, 34, 0.035);
}

.community-detail__section h3 {
  margin: 0;
  font: 700 18px/1.2 var(--font-display);
}

.community-detail__content {
  white-space: pre-wrap;
}

.community-detail__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.community-detail__reply-head span {
  color: rgba(17, 19, 34, 0.52);
  font-size: 13px;
}

.community-detail__empty {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  color: rgba(17, 19, 34, 0.58);
}

.community-reply-card {
  padding: 16px 18px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
}

.community-reply-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: rgba(17, 19, 34, 0.52);
  font-size: 12px;
}

.community-reply-card__meta strong {
  color: #111322;
  font-size: 14px;
}

.community-reply-card p {
  margin: 12px 0 0;
  color: rgba(17, 19, 34, 0.7);
  line-height: 1.8;
  white-space: pre-wrap;
}

.community-detail__composer {
  gap: 16px;
}

.community-replying-badge {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(36, 87, 214, 0.08);
  color: #2457d6;
  font-size: 13px;
}

.community-replying-badge button {
  border: 0;
  background: transparent;
  color: inherit;
  font: 600 13px/1 var(--font-body);
  cursor: pointer;
}

@media (max-width: 980px) {
  .community-hero__inner {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .community-hero__search {
    grid-template-columns: 1fr;
  }

  .community-feed-head__actions,
  .community-card__top,
  .community-card__footer,
  .community-detail__head,
  .community-detail__reply-head,
  .community-detail__composer-actions {
    flex-direction: column;
    align-items: start;
  }

  .community-card {
    padding: 18px;
  }

  .community-card__title {
    font-size: 24px;
  }

  .community-detail__title {
    font-size: 28px;
  }
}
</style>
