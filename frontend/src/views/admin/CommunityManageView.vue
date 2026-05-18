<template>
  <section class="page-card">
    <div class="toolbar">
      <div>
        <h2>社区交流管理</h2>
        <p class="community-admin__intro">统一处理社区帖子与回复，完成内容上线、下架和清理的运营闭环。</p>
      </div>
      <div class="toolbar-actions">
        <el-button @click="loadPosts">刷新</el-button>
      </div>
    </div>

    <div class="community-admin__filters">
      <el-input
        v-model="filters.keyword"
        clearable
        placeholder="搜索标题、摘要或正文"
        @keyup.enter="loadPosts"
      />
      <el-select v-model="filters.forumName" placeholder="选择版块" style="width: 180px">
        <el-option v-for="item in forumOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" style="width: 160px">
        <el-option label="全部状态" value="all" />
        <el-option label="启用中" :value="1" />
        <el-option label="已下架" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadPosts">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table :data="posts" v-loading="loading">
      <el-table-column prop="forumName" label="版块" width="120" />
      <el-table-column prop="title" label="标题" min-width="260" />
      <el-table-column prop="authorName" label="作者" width="130" />
      <el-table-column prop="summary" label="摘要" min-width="320" show-overflow-tooltip />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">
            {{ row.status === 1 ? '启用中' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="互动数据" width="220">
        <template #default="{ row }">
          <span>{{ row.replyCount || 0 }} 回复 / {{ row.likeCount || 0 }} 点赞 / {{ row.favoriteCount || 0 }} 收藏</span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340" fixed="right">
        <template #default="{ row }">
          <div class="table-row-actions">
            <el-button size="small" @click="openReplies(row)">查看回复</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="togglePostStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDeletePost(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawerVisible" size="760px" :title="activePost?.title || '帖子回复'">
      <div v-if="activePost" class="community-admin__detail">
        <section class="community-admin__post">
          <div class="community-admin__meta">
            <span>{{ activePost.forumName }}</span>
            <span>{{ activePost.authorName }}</span>
            <span>{{ formatDate(activePost.createdAt) }}</span>
          </div>
          <p class="community-admin__summary">{{ activePost.summary }}</p>
          <CommunityRichContent class="community-admin__content" :content="activePost.content" compact />
        </section>

        <div class="community-admin__reply-head">
          <h3>回复列表</h3>
          <el-select v-model="replyStatusFilter" style="width: 160px" @change="loadReplies">
            <el-option label="全部回复" value="all" />
            <el-option label="启用中" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </div>

        <div v-loading="replyLoading" class="community-admin__reply-list">
          <article v-for="reply in replies" :key="reply.id" class="community-admin__reply-card">
            <div class="community-admin__reply-top">
              <div>
                <strong>{{ reply.authorName }}</strong>
                <p v-if="reply.replyToAuthorName" class="community-admin__reply-target">回复 {{ reply.replyToAuthorName }}</p>
                <p>{{ formatDate(reply.createdAt) }}</p>
              </div>
              <div class="table-row-actions">
                <el-button
                  size="small"
                  :type="reply.status === 1 ? 'warning' : 'success'"
                  @click="toggleReplyStatus(reply)"
                >
                  {{ reply.status === 1 ? '下架' : '启用' }}
                </el-button>
                <el-button size="small" type="danger" @click="handleDeleteReply(reply)">删除</el-button>
              </div>
            </div>
            <p class="community-admin__reply-content">{{ reply.content }}</p>
          </article>
          <div v-if="!replyLoading && !replies.length" class="table-empty">当前帖子还没有符合筛选条件的回复。</div>
        </div>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommunityRichContent from '@/components/community/CommunityRichContent.vue'
import {
  deleteAdminCommunityPost,
  deleteAdminCommunityReply,
  fetchAdminCommunityPosts,
  fetchAdminCommunityReplies,
  updateAdminCommunityPostStatus,
  updateAdminCommunityReplyStatus
} from '@/api/modules/admin'

const forumOptions = ['全部', '接单问答', '前端开发', '后端架构', 'AI 应用', '项目协作', '运维部署']

const filters = reactive({
  keyword: '',
  forumName: '全部',
  status: 'all'
})

const loading = ref(false)
const replyLoading = ref(false)
const posts = ref([])
const replies = ref([])
const drawerVisible = ref(false)
const activePost = ref(null)
const replyStatusFilter = ref('all')

async function loadPosts() {
  loading.value = true
  try {
    const response = await fetchAdminCommunityPosts({
      keyword: filters.keyword.trim() || undefined,
      forumName: filters.forumName === '全部' ? undefined : filters.forumName,
      status: filters.status === 'all' ? undefined : filters.status
    })
    posts.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载社区帖子失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.keyword = ''
  filters.forumName = '全部'
  filters.status = 'all'
  loadPosts()
}

async function openReplies(post) {
  activePost.value = post
  replyStatusFilter.value = 'all'
  drawerVisible.value = true
  await loadReplies()
}

async function loadReplies() {
  if (!activePost.value) return
  replyLoading.value = true
  try {
    const response = await fetchAdminCommunityReplies(activePost.value.id, {
      status: replyStatusFilter.value === 'all' ? undefined : replyStatusFilter.value
    })
    replies.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载回复失败')
  } finally {
    replyLoading.value = false
  }
}

async function togglePostStatus(row) {
  try {
    await updateAdminCommunityPostStatus(row.id, { status: row.status === 1 ? 2 : 1 })
    ElMessage.success('帖子状态已更新')
    await loadPosts()
    if (activePost.value?.id === row.id) {
      activePost.value = posts.value.find((item) => item.id === row.id) || activePost.value
    }
  } catch (error) {
    ElMessage.error(error.message || '更新帖子状态失败')
  }
}

async function handleDeletePost(row) {
  try {
    await ElMessageBox.confirm(`确认删除帖子“${row.title}”吗？删除后帖子与回复会一起清理。`, '删除帖子', {
      type: 'warning'
    })
    await deleteAdminCommunityPost(row.id)
    ElMessage.success('帖子已删除')
    if (activePost.value?.id === row.id) {
      drawerVisible.value = false
      activePost.value = null
      replies.value = []
    }
    await loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除帖子失败')
    }
  }
}

async function toggleReplyStatus(reply) {
  try {
    await updateAdminCommunityReplyStatus(reply.id, { status: reply.status === 1 ? 2 : 1 })
    ElMessage.success('回复状态已更新')
    await Promise.all([loadReplies(), loadPosts()])
  } catch (error) {
    ElMessage.error(error.message || '更新回复状态失败')
  }
}

async function handleDeleteReply(reply) {
  try {
    await ElMessageBox.confirm('确认删除这条回复吗？删除后无法恢复。', '删除回复', {
      type: 'warning'
    })
    await deleteAdminCommunityReply(reply.id)
    ElMessage.success('回复已删除')
    await Promise.all([loadReplies(), loadPosts()])
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除回复失败')
    }
  }
}

function formatDate(value) {
  if (!value) return '--'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

onMounted(loadPosts)
</script>

<style scoped>
.community-admin__intro {
  margin: 8px 0 0;
  color: rgba(17, 17, 17, 0.56);
  line-height: 1.7;
}

.community-admin__filters {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px 160px auto auto;
  gap: 12px;
  margin-bottom: 18px;
}

.community-admin__detail {
  display: grid;
  gap: 18px;
}

.community-admin__post,
.community-admin__reply-card {
  padding: 18px;
  border-radius: 20px;
  background: #f8f9fc;
}

.community-admin__meta,
.community-admin__reply-top {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 12px;
}

.community-admin__meta {
  flex-wrap: wrap;
  color: rgba(17, 17, 17, 0.52);
  font-size: 12px;
}

.community-admin__summary,
.community-admin__content,
.community-admin__reply-content {
  margin: 12px 0 0;
  color: rgba(17, 17, 17, 0.72);
  line-height: 1.8;
  white-space: pre-wrap;
}

.community-admin__reply-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.community-admin__reply-head h3 {
  margin: 0;
}

.community-admin__reply-list {
  display: grid;
  gap: 12px;
  min-height: 120px;
}

.community-admin__reply-top strong {
  display: block;
}

.community-admin__reply-top p {
  margin: 6px 0 0;
  color: rgba(17, 17, 17, 0.52);
  font-size: 12px;
}

.community-admin__reply-target {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(17, 17, 17, 0.06);
}

@media (max-width: 980px) {
  .community-admin__filters {
    grid-template-columns: 1fr;
  }

  .community-admin__meta,
  .community-admin__reply-top,
  .community-admin__reply-head {
    flex-direction: column;
    align-items: start;
  }
}
</style>
