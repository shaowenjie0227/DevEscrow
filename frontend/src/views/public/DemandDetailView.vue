<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ChatDotRound, Clock, Files, Lightning, Promotion } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Footer from '@/components/home/Footer.vue'
import TopNav from '@/components/home/TopNav.vue'
import { fetchPublicMarketDemandDetail } from '@/api/modules/demand'
import { createQuote } from '@/api/modules/quote'
import { fetchCurrentUser } from '@/api/modules/auth'
import { useAuthStore } from '@/stores/auth'
import { emitAuthExpired } from '@/utils/authEvents'
import { resolveWorkspaceChatPath } from '@/utils/workspace'

type DemandFileItem = {
  name?: string
  url?: string
  contentType?: string
  size?: number | string | null
}

type DemandDetail = {
  id?: number | string
  demandNo?: string
  publisherId?: number | string
  publisherNickname?: string
  publisherAvatarUrl?: string
  title?: string
  summary?: string
  detail?: string
  category?: string
  orderType?: number | string
  isUrgent?: boolean
  urgentBonus?: number | string
  budgetMin?: number | string
  budgetMax?: number | string
  expectedDays?: number | string
  deliveryType?: number | string
  coverImage?: string
  images?: DemandFileItem[]
  attachments?: DemandFileItem[]
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const demand = ref<DemandDetail | null>(null)
const loading = ref(true)
const activeImageSlide = ref(0)
const quoteDialogVisible = ref(false)
const quoteSubmitting = ref(false)
const fallbackCoverImage = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80'

const quoteForm = reactive({
  priceTotal: 0,
  estimatedDays: 7,
  deliveryDesc: '',
  techSolution: ''
})

const currentUserId = computed(() => Number(authStore.userInfo?.userId || 0))
const isOwner = computed(() => Number(demand.value?.publisherId || 0) === currentUserId.value)
const demandImages = computed<DemandFileItem[]>(() => {
  const images = Array.isArray(demand.value?.images) ? demand.value?.images : []
  if (images.length) {
    return images
  }
  if (demand.value?.coverImage) {
    return [
      {
        name: demand.value.title || '需求图片',
        url: demand.value.coverImage,
        contentType: 'image/*',
        size: null
      }
    ]
  }
  return []
})
const demandAttachments = computed<DemandFileItem[]>(() => {
  return Array.isArray(demand.value?.attachments) ? demand.value.attachments : []
})
const demandImageSlides = computed(() => {
  const slides: DemandFileItem[][] = []
  for (let index = 0; index < demandImages.value.length; index += 4) {
    slides.push(demandImages.value.slice(index, index + 4))
  }
  return slides
})
const demandImagePreviewList = computed(() => {
  return demandImages.value
    .map((image) => image?.url)
    .filter((url): url is string => Boolean(url))
})

const actionHint = computed(() => {
  if (isOwner.value) {
    return '这是你自己发布的需求，开发者联系与接单入口不会对自己显示。'
  }
  if (authStore.userInfo?.developerStatus === 2) {
    return '已通过开发者认证，可以先联系客户确认范围，再决定是否提交报价。'
  }
  return '完成开发者认证后，才可以接单或联系客户。'
})

async function loadDemand() {
  loading.value = true
  try {
    const response = await fetchPublicMarketDemandDetail(route.params.id)
    demand.value = response.data || null
    activeImageSlide.value = 0
    hydrateQuoteDefaults()
  } catch (error) {
    demand.value = null
  } finally {
    loading.value = false
  }
}

onMounted(loadDemand)

function hydrateQuoteDefaults() {
  quoteForm.priceTotal = Number(demand.value?.budgetMax || demand.value?.budgetMin || 3000)
  quoteForm.estimatedDays = Number(demand.value?.expectedDays || 7)
  quoteForm.deliveryDesc = '优先交付一版可运行的 MVP，并在核心链路稳定后继续完善细节。'
  quoteForm.techSolution = '建议先梳理范围边界、交付清单和验收节点，再按阶段推进开发与测试，减少返工成本。'
}

function formatBudgetRange(minValue?: number | string, maxValue?: number | string) {
  const min = Number(minValue || 0)
  const max = Number(maxValue || 0)
  if (!max || min === max) {
    return `￥${min.toLocaleString()}`
  }
  return `￥${min.toLocaleString()} - ￥${max.toLocaleString()}`
}

function formatFileSize(size?: number | string | null) {
  const value = Number(size || 0)
  if (!value) {
    return '大小未知'
  }
  if (value >= 1024 * 1024) {
    return `${(value / (1024 * 1024)).toFixed(2)} MB`
  }
  if (value >= 1024) {
    return `${(value / 1024).toFixed(1)} KB`
  }
  return `${value} B`
}

function formatAttachmentMeta(attachment: DemandFileItem) {
  const meta: string[] = []
  if (attachment?.contentType) {
    meta.push(attachment.contentType)
  }
  if (attachment?.size) {
    meta.push(formatFileSize(attachment.size))
  }
  return meta.join(' · ') || '通用附件'
}

function formatOrderType(orderType?: number | string) {
  return Number(orderType || 1) === 2 ? '文档单' : '开发单'
}

function formatUrgency(isUrgent?: boolean, urgentBonus?: number | string) {
  if (!isUrgent) {
    return '常规节奏'
  }
  const bonus = Number(urgentBonus || 0)
  if (bonus > 0) {
    return `加急 +${bonus.toLocaleString()}`
  }
  return '加急需求'
}

function formatDeliveryType(deliveryType?: number | string) {
  return Number(deliveryType || 1) === 2 ? '分阶段交付' : '整单交付'
}

function setActiveImageSlide(index: number) {
  activeImageSlide.value = index
}

function goToPrevImageSlide() {
  const total = demandImageSlides.value.length
  if (!total) return
  activeImageSlide.value = (activeImageSlide.value - 1 + total) % total
}

function goToNextImageSlide() {
  const total = demandImageSlides.value.length
  if (!total) return
  activeImageSlide.value = (activeImageSlide.value + 1) % total
}

async function ensureApprovedDeveloper(actionLabel: string) {
  if (!authStore.token) {
    emitAuthExpired({
      scope: 'user',
      message: `登录后才可以${actionLabel}`,
      redirectPath: route.fullPath
    })
    return false
  }

  if (isOwner.value) {
    ElMessage.info('这是你自己发布的需求。')
    return false
  }

  let developerStatus = Number(authStore.userInfo?.developerStatus || 0)
  if (developerStatus !== 2) {
    try {
      const response = await fetchCurrentUser()
      const latest = response.data || {}
      authStore.userInfo = {
        ...authStore.userInfo,
        developerStatus: latest.developerStatus ?? developerStatus,
        idVerifyStatus: latest.idVerifyStatus ?? authStore.userInfo?.idVerifyStatus ?? 0,
        skillAuditStatus: latest.skillAuditStatus ?? authStore.userInfo?.skillAuditStatus ?? 0,
        roles: latest.roles || authStore.userInfo?.roles || [],
        redirectPath: latest.redirectPath || authStore.userInfo?.redirectPath || '/me'
      }
      window.localStorage.setItem('user_info', JSON.stringify(authStore.userInfo))
      developerStatus = Number(authStore.userInfo?.developerStatus || 0)
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '无法校验开发者状态')
      return false
    }
  }

  if (developerStatus === 2) {
    return true
  }

  try {
    await ElMessageBox.confirm(
      `完成开发者认证后，才可以${actionLabel}。现在去个人中心完成认证吗？`,
      '需要开发者认证',
      {
        confirmButtonText: '去认证',
        cancelButtonText: '暂不',
        type: 'warning'
      }
    )
    router.push('/me')
  } catch (error) {
    // Ignore cancel
  }
  return false
}

async function handleContactClient() {
  if (!demand.value?.id || !demand.value?.publisherId) {
    return
  }
  const allowed = await ensureApprovedDeveloper('联系客户')
  if (!allowed) {
    return
  }
  router.push({
    path: resolveWorkspaceChatPath(authStore.userInfo),
    query: {
      bizType: '1',
      demandId: String(demand.value.id),
      partnerId: String(demand.value.publisherId),
      partnerName: demand.value.publisherNickname || '客户',
      demandTitle: demand.value.title || '需求沟通'
    }
  })
}

async function handleOpenQuoteDialog() {
  const allowed = await ensureApprovedDeveloper('接单')
  if (!allowed) {
    return
  }
  hydrateQuoteDefaults()
  quoteDialogVisible.value = true
  await nextTick()
}

async function handleSubmitQuote() {
  if (!demand.value?.id) {
    return
  }
  quoteSubmitting.value = true
  try {
    await createQuote({
      demandId: Number(demand.value.id),
      priceTotal: quoteForm.priceTotal,
      estimatedDays: quoteForm.estimatedDays,
      deliveryDesc: quoteForm.deliveryDesc,
      techSolution: quoteForm.techSolution
    })
    ElMessage.success('报价提交成功，接下来可以继续和客户沟通细节。')
    quoteDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '报价提交失败')
  } finally {
    quoteSubmitting.value = false
  }
}
</script>

<template>
  <div class="market-page">
    <TopNav />

    <main class="market-container market-detail" v-loading="loading" v-if="demand">
      <div class="market-detail__top">
        <button class="market-back" type="button" @click="router.push('/market')">
          <el-icon><ArrowLeft /></el-icon>
          返回接单大厅
        </button>
      </div>

      <section class="market-detail__grid">
        <article class="market-detail__media">
          <img
            :src="demand.coverImage || fallbackCoverImage"
            :alt="demand.title || '需求封面'"
            class="market-detail__image"
          />
          <div class="market-detail__publisher">
            <p class="market-detail__text">需求编号</p>
            <h2>{{ demand.demandNo || '-' }}</h2>
            <p class="market-detail__text">
              {{ demand.publisherNickname || '客户' }} · 线上协作
            </p>
          </div>
        </article>

        <article class="market-detail__body">
          <div class="market-detail__heading">
            <div>
              <p class="market-detail__text">{{ demand.category || '未分类' }}</p>
              <h1 class="market-detail__title">{{ demand.title || '未命名需求' }}</h1>
              <p class="market-detail__summary">{{ demand.summary || '暂无摘要说明' }}</p>
            </div>
            <span class="market-detail__budget">
              {{ formatBudgetRange(demand.budgetMin, demand.budgetMax) }}
            </span>
          </div>

          <div class="market-detail__gridcards">
            <div class="market-detail__card">
              <p class="market-detail__card-label">
                <el-icon><Clock /></el-icon>
                预期周期
              </p>
              <p class="market-detail__card-value">{{ demand.expectedDays || '-' }} 天</p>
            </div>
            <div class="market-detail__card">
              <p class="market-detail__card-label">
                <el-icon><Files /></el-icon>
                需求类型
              </p>
              <p class="market-detail__card-value">{{ formatOrderType(demand.orderType) }}</p>
            </div>
            <div class="market-detail__card">
              <p class="market-detail__card-label">
                <el-icon><Lightning /></el-icon>
                加急状态
              </p>
              <p class="market-detail__card-value">{{ formatUrgency(demand.isUrgent, demand.urgentBonus) }}</p>
            </div>
            <div class="market-detail__card">
              <p class="market-detail__card-label">
                <el-icon><Promotion /></el-icon>
                交付方式
              </p>
              <p class="market-detail__card-value">{{ formatDeliveryType(demand.deliveryType) }}</p>
            </div>
          </div>

          <section class="market-detail__action-shell" v-if="!isOwner">
            <div class="market-detail__action-copy">
              <span class="market-detail__action-eyebrow">Developer Actions</span>
              <h2>先聊清范围，再决定是否接这单</h2>
              <p>{{ actionHint }}</p>
            </div>
            <div class="market-detail__action-buttons">
              <button class="market-detail__action-btn market-detail__action-btn--ghost" type="button" @click="handleContactClient">
                <el-icon><ChatDotRound /></el-icon>
                联系客户
              </button>
              <button class="market-detail__action-btn market-detail__action-btn--primary" type="button" @click="handleOpenQuoteDialog">
                <el-icon><Promotion /></el-icon>
                接单并报价
              </button>
            </div>
          </section>

          <section class="market-detail__section">
            <h2>需求介绍</h2>
            <p class="market-detail__text">{{ demand.detail || '暂无详细说明' }}</p>
          </section>
        </article>
      </section>

      <section class="market-detail__resources">
        <article class="market-detail__resource-card">
          <div class="market-detail__resource-head">
            <div>
              <p class="market-detail__text">补充素材</p>
              <h2>图片展示</h2>
            </div>
            <span class="market-detail__resource-count">{{ demandImages.length }} 张</span>
          </div>

          <div v-if="demandImages.length > 0 && demandImages.length <= 4" class="market-detail__gallery">
            <article
              v-for="(image, index) in demandImages"
              :key="`${image.url}-${index}`"
              class="market-detail__gallery-item"
            >
              <el-image
                :src="image.url"
                :alt="image.name || `需求图片 ${index + 1}`"
                :initial-index="index"
                :preview-src-list="demandImagePreviewList"
                class="market-detail__gallery-image"
                fit="cover"
                preview-teleported
                show-progress
              >
                <template #progress="{ activeIndex, total }">
                  <span class="market-detail__viewer-progress">({{ activeIndex + 1 }}/{{ total }})</span>
                </template>
              </el-image>
            </article>
          </div>

          <div v-else-if="demandImages.length" class="market-detail__gallery-carousel">
            <div class="market-detail__carousel-shell">
              <button
                aria-label="上一组图片"
                class="market-detail__carousel-arrow"
                type="button"
                @click="goToPrevImageSlide"
              >
                ‹
              </button>

              <div class="market-detail__gallery market-detail__gallery--carousel">
                <article
                  v-for="(image, index) in demandImageSlides[activeImageSlide]"
                  :key="`${image.url}-${activeImageSlide}-${index}`"
                  class="market-detail__gallery-item"
                >
                  <el-image
                    :src="image.url"
                    :alt="image.name || `需求图片 ${activeImageSlide * 4 + index + 1}`"
                    :initial-index="activeImageSlide * 4 + index"
                    :preview-src-list="demandImagePreviewList"
                    class="market-detail__gallery-image"
                    fit="cover"
                    preview-teleported
                    show-progress
                  >
                    <template #progress="{ activeIndex, total }">
                      <span class="market-detail__viewer-progress">({{ activeIndex + 1 }}/{{ total }})</span>
                    </template>
                  </el-image>
                </article>
              </div>

              <button
                aria-label="下一组图片"
                class="market-detail__carousel-arrow"
                type="button"
                @click="goToNextImageSlide"
              >
                ›
              </button>
            </div>

            <div class="market-detail__carousel-dots">
              <button
                v-for="(_, slideIndex) in demandImageSlides"
                :key="slideIndex"
                :aria-label="`切换到第 ${slideIndex + 1} 组图片`"
                :class="[
                  'market-detail__carousel-dot',
                  { 'market-detail__carousel-dot--active': slideIndex === activeImageSlide }
                ]"
                type="button"
                @click="setActiveImageSlide(slideIndex)"
              />
            </div>
          </div>

          <div v-else class="market-detail__resource-empty">暂未上传图片资料</div>
        </article>

        <article class="market-detail__resource-card">
          <div class="market-detail__resource-head">
            <div>
              <p class="market-detail__text">补充素材</p>
              <h2>附件展示</h2>
            </div>
            <span class="market-detail__resource-count">{{ demandAttachments.length }} 个</span>
          </div>

          <div v-if="demandAttachments.length" class="market-detail__attachment-list">
            <a
              v-for="(attachment, index) in demandAttachments"
              :key="`${attachment.url}-${index}`"
              :href="attachment.url"
              class="market-detail__attachment-item"
              rel="noreferrer"
              target="_blank"
            >
              <div class="market-detail__attachment-copy">
                <strong>{{ attachment.name || `附件 ${index + 1}` }}</strong>
                <p>{{ formatAttachmentMeta(attachment) }}</p>
              </div>
              <span class="market-detail__attachment-link">查看附件</span>
            </a>
          </div>

          <div v-else class="market-detail__resource-empty">暂未上传附件资料</div>
        </article>
      </section>
    </main>

    <main class="market-container market-detail" v-else-if="!loading">
      <section class="market-feed-card">
        <div class="market-feed-head">
          <div>
            <h2>需求不存在或已下架</h2>
            <p>可以返回接单大厅继续浏览其它公开需求。</p>
          </div>
        </div>
        <button class="market-btn market-btn--primary" type="button" @click="router.push('/market')">返回接单大厅</button>
      </section>
    </main>

    <el-dialog v-model="quoteDialogVisible" width="640px" title="提交报价">
      <el-form :model="quoteForm" label-position="top">
        <el-form-item label="需求">
          <el-input :model-value="demand?.title || ''" disabled />
        </el-form-item>
        <el-form-item label="总报价">
          <el-input-number v-model="quoteForm.priceTotal" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预计工期（天）">
          <el-input-number v-model="quoteForm.estimatedDays" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="交付说明">
          <el-input v-model="quoteForm.deliveryDesc" />
        </el-form-item>
        <el-form-item label="技术方案">
          <el-input v-model="quoteForm.techSolution" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quoteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="quoteSubmitting" @click="handleSubmitQuote">提交报价</el-button>
      </template>
    </el-dialog>

    <Footer />
  </div>
</template>

<style scoped>
.market-detail__summary {
  margin: 10px 0 0;
  max-width: 58ch;
  color: rgba(17, 19, 34, 0.62);
  line-height: 1.8;
}

.market-detail__gridcards {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.market-detail__action-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  margin: 22px 0 0;
  padding: 20px 22px;
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(255, 247, 237, 0.96), rgba(255, 255, 255, 0.92)),
    rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(251, 146, 60, 0.16);
  box-shadow: 0 20px 42px rgba(251, 146, 60, 0.08);
}

.market-detail__action-copy h2 {
  margin: 8px 0 0;
  font-size: clamp(22px, 3vw, 28px);
  letter-spacing: -0.04em;
}

.market-detail__action-copy p {
  margin: 10px 0 0;
  color: rgba(17, 19, 34, 0.62);
  line-height: 1.8;
}

.market-detail__action-eyebrow {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(251, 146, 60, 0.14);
  color: rgba(17, 19, 34, 0.7);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.market-detail__action-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.market-detail__action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 54px;
  padding: 0 22px;
  border-radius: 16px;
  border: 0;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
  font-size: 15px;
  font-weight: 700;
}

.market-detail__action-btn:hover {
  transform: translateY(-1px);
}

.market-detail__action-btn--ghost {
  background: rgba(255, 255, 255, 0.86);
  color: #111322;
  box-shadow: inset 0 0 0 1px rgba(17, 19, 34, 0.08);
}

.market-detail__action-btn--primary {
  background: linear-gradient(135deg, #111322, #2f364f);
  color: #fff;
  box-shadow: 0 18px 34px rgba(17, 19, 34, 0.18);
}

@media (max-width: 1100px) {
  .market-detail__gridcards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .market-detail__action-shell {
    grid-template-columns: 1fr;
  }

  .market-detail__action-buttons {
    justify-content: stretch;
  }

  .market-detail__action-btn {
    width: 100%;
  }
}

@media (max-width: 720px) {
  .market-detail__gridcards {
    grid-template-columns: 1fr;
  }
}
</style>
