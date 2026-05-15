<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, Calendar, Link, Reading } from '@element-plus/icons-vue'
import PublicContentShell from '@/components/home/PublicContentShell.vue'
import { fetchHomeNoticeDetail } from '@/api/modules/demand'

const route = useRoute()
const router = useRouter()
const notice = ref<any>(null)
const loading = ref(false)

const detailTypeLabel = computed(() => {
  if (!notice.value) return '首页内容'
  return notice.value.typeLabel || (notice.value.noticeType === 2 ? '活动' : '公告')
})

const detailLead = computed(() => {
  if (!notice.value) return ''
  return notice.value.noticeType === 2
    ? '适合放活动招募、阶段性专题、认证激励或短期运营内容。'
    : '适合放规则更新、平台通知、流程说明或首页固定提示。'
})

const actionLabel = computed(() => {
  if (!notice.value?.targetUrl) return '返回首页'
  return notice.value.noticeType === 2 ? '进入活动页面' : '查看相关页面'
})

function openTarget() {
  if (!notice.value?.targetUrl) {
    router.push('/')
    return
  }

  if (/^https?:\/\//.test(notice.value.targetUrl)) {
    window.open(notice.value.targetUrl, '_blank', 'noopener')
    return
  }

  router.push(notice.value.targetUrl)
}

async function loadNotice() {
  loading.value = true
  try {
    const response = await fetchHomeNoticeDetail(route.params.id)
    notice.value = response.data || null
  } catch (error) {
    notice.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadNotice)
</script>

<template>
  <PublicContentShell back-label="返回首页" back-to="/">
    <section v-if="loading" class="notice-detail notice-detail--empty">
      <strong>正在加载内容...</strong>
      <p>请稍候，正在获取活动或公告详情。</p>
    </section>

    <section v-else-if="notice" class="notice-detail">
      <article class="notice-detail__hero">
        <div class="notice-detail__hero-copy">
          <div class="notice-detail__eyebrow">
            <span class="notice-detail__tag" :data-type="notice.noticeType === 2 ? 'activity' : 'notice'">
              {{ detailTypeLabel }}
            </span>
            <span class="notice-detail__serial">ID {{ String(notice.id).padStart(2, '0') }}</span>
          </div>

          <div class="notice-detail__heading">
            <h1>{{ notice.title }}</h1>
            <p>{{ notice.summary }}</p>
          </div>

          <div class="notice-detail__actions">
            <button class="market-btn market-btn--primary" type="button" @click="openTarget">
              {{ actionLabel }}
            </button>
            <button class="market-btn market-btn--ghost" type="button" @click="router.push('/')">
              返回首页
            </button>
          </div>
        </div>

        <div
          class="notice-detail__hero-cover"
          :style="notice.coverUrl ? { backgroundImage: `url(${notice.coverUrl})` } : {}"
        >
          <div class="notice-detail__cover-card">
            <span>{{ notice.noticeType === 2 ? '活动详情' : '公告详情' }}</span>
            <strong>{{ notice.targetUrl || '未设置跳转地址' }}</strong>
          </div>
        </div>
      </article>

      <section class="notice-detail__grid">
        <article class="notice-detail__panel">
          <div class="notice-detail__panel-head">
            <h2>内容说明</h2>
            <span>Overview</span>
          </div>
          <p class="notice-detail__lead">{{ detailLead }}</p>
          <div class="notice-detail__copy-block">
            <p>{{ notice.summary }}</p>
          </div>
        </article>

        <article class="notice-detail__panel notice-detail__panel--meta">
          <div class="notice-detail__meta-card">
            <p><el-icon><Bell /></el-icon> 内容类型</p>
            <strong>{{ detailTypeLabel }}</strong>
          </div>
          <div class="notice-detail__meta-card">
            <p><el-icon><Calendar /></el-icon> 展示排序</p>
            <strong>{{ notice.sortOrder ?? 0 }}</strong>
          </div>
          <div class="notice-detail__meta-card">
            <p><el-icon><Link /></el-icon> 跳转地址</p>
            <strong>{{ notice.targetUrl || '暂未配置' }}</strong>
          </div>
          <div class="notice-detail__meta-card">
            <p><el-icon><Reading /></el-icon> 展示状态</p>
            <strong>{{ notice.status === 1 ? '已启用' : '未启用' }}</strong>
          </div>
        </article>
      </section>
    </section>

    <section v-else class="notice-detail notice-detail--empty">
      <strong>内容不存在或已下线</strong>
      <p>这条活动或公告目前不可查看，可以返回首页浏览其他内容。</p>
      <button class="market-btn market-btn--primary" type="button" @click="router.push('/')">
        返回首页
      </button>
    </section>
  </PublicContentShell>
</template>

<style scoped>
.notice-detail {
  display: grid;
  gap: 22px;
}

.notice-detail--empty {
  place-items: center;
  min-height: 380px;
  padding: 30px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(248, 250, 255, 0.9));
  text-align: center;
  box-shadow: 0 22px 46px rgba(17, 19, 34, 0.06);
}

.notice-detail--empty strong {
  font-size: 28px;
}

.notice-detail--empty p {
  color: rgba(17, 19, 34, 0.58);
  line-height: 1.8;
}

.notice-detail__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.12fr) minmax(320px, 0.88fr);
  gap: 22px;
  padding: 26px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 30px;
  background:
    radial-gradient(circle at top left, rgba(243, 190, 37, 0.14), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 255, 0.92));
  box-shadow: 0 24px 52px rgba(17, 19, 34, 0.08);
}

.notice-detail__hero-copy {
  display: grid;
  align-content: center;
  gap: 18px;
}

.notice-detail__eyebrow {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.notice-detail__tag {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.notice-detail__tag[data-type='activity'] {
  background: rgba(243, 190, 37, 0.2);
  color: #111322;
}

.notice-detail__tag[data-type='notice'] {
  background: rgba(17, 19, 34, 0.08);
  color: rgba(17, 19, 34, 0.82);
}

.notice-detail__serial {
  color: rgba(17, 19, 34, 0.46);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.notice-detail__heading {
  display: grid;
  gap: 14px;
}

.notice-detail__heading h1 {
  font-size: clamp(34px, 4vw, 60px);
  line-height: 0.96;
}

.notice-detail__heading p {
  max-width: 52ch;
  color: rgba(17, 19, 34, 0.64);
  line-height: 1.9;
  font-size: 16px;
}

.notice-detail__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.notice-detail__hero-cover {
  position: relative;
  min-height: 320px;
  overflow: hidden;
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(17, 19, 34, 0.1), rgba(243, 190, 37, 0.22)),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(241, 245, 249, 0.92));
  background-position: center;
  background-size: cover;
}

.notice-detail__hero-cover::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(17, 19, 34, 0.04), rgba(17, 19, 34, 0.44)),
    linear-gradient(135deg, transparent 20%, rgba(255, 255, 255, 0.08));
}

.notice-detail__cover-card {
  position: absolute;
  right: 16px;
  bottom: 16px;
  left: 16px;
  z-index: 1;
  display: grid;
  gap: 6px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.68);
  color: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(10px);
}

.notice-detail__cover-card span {
  color: rgba(255, 255, 255, 0.64);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.notice-detail__cover-card strong {
  line-height: 1.6;
  word-break: break-all;
}

.notice-detail__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.8fr);
  gap: 20px;
}

.notice-detail__panel {
  display: grid;
  gap: 16px;
  padding: 24px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 255, 0.88));
  box-shadow: 0 18px 38px rgba(17, 19, 34, 0.05);
}

.notice-detail__panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice-detail__panel-head h2 {
  font-size: 24px;
}

.notice-detail__panel-head span {
  color: rgba(17, 19, 34, 0.46);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.notice-detail__lead {
  color: rgba(17, 19, 34, 0.68);
  line-height: 1.8;
}

.notice-detail__copy-block {
  padding: 18px;
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.04);
}

.notice-detail__copy-block p {
  line-height: 1.9;
  color: rgba(17, 19, 34, 0.66);
}

.notice-detail__panel--meta {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-content: start;
}

.notice-detail__meta-card {
  display: grid;
  gap: 8px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(17, 19, 34, 0.04);
}

.notice-detail__meta-card p {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(17, 19, 34, 0.48);
  font-size: 13px;
  font-weight: 700;
}

.notice-detail__meta-card strong {
  line-height: 1.7;
  word-break: break-word;
}

@media (max-width: 1100px) {
  .notice-detail__hero,
  .notice-detail__grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .notice-detail__hero,
  .notice-detail__panel {
    padding: 18px;
  }

  .notice-detail__heading h1 {
    font-size: 34px;
  }

  .notice-detail__panel--meta {
    grid-template-columns: 1fr;
  }
}
</style>
