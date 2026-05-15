<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CollectionTag, Files, RefreshRight, Search, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import DemandCard from '@/components/home/DemandCard.vue'
import Footer from '@/components/home/Footer.vue'
import TopNav from '@/components/home/TopNav.vue'
import { fetchDemandCategories, fetchPublicMarketDemands } from '@/api/modules/demand'
import { normalizeMarketDemand } from '@/utils/market'

const COLUMN_SIZE = 10
const PAGE_SIZE = COLUMN_SIZE * 2

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const dataLoaded = ref(false)
const demands = ref<any[]>([])
const categories = ref<any[]>([])

const keyword = ref('')
const selectedCategoryIds = ref<string[]>([])
const currentPage = ref(1)

function normalizeCategoryValues(value: unknown) {
  if (Array.isArray(value)) {
    return value
      .flatMap((item) => String(item).split(','))
      .map((item) => item.trim())
      .filter(Boolean)
  }

  if (typeof value === 'string') {
    return value
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
  }

  return []
}

function readRouteState() {
  const page = Number(route.query.page || 1)

  return {
    keyword: typeof route.query.keyword === 'string' ? route.query.keyword.trim() : '',
    categories: normalizeCategoryValues(route.query.categories ?? route.query.category),
    page: Number.isInteger(page) && page > 0 ? page : 1
  }
}

function updateRouteState(nextState: { keyword?: string; categories?: string[]; page?: number }) {
  const keywordValue = (nextState.keyword ?? keyword.value).trim()
  const categoryValues = (nextState.categories ?? selectedCategoryIds.value)
    .map((item) => String(item).trim())
    .filter(Boolean)
  const pageValue = nextState.page ?? currentPage.value
  const query: Record<string, string> = {}

  if (keywordValue) query.keyword = keywordValue
  if (categoryValues.length) query.categories = categoryValues.join(',')
  if (pageValue > 1) query.page = String(pageValue)

  router.replace({
    path: '/market',
    query
  })
}

watch(
  () => route.query,
  () => {
    const state = readRouteState()
    keyword.value = state.keyword
    selectedCategoryIds.value = state.categories
    currentPage.value = state.page
  },
  { immediate: true }
)

const categoryCountMap = computed(() => {
  return demands.value.reduce((map: Record<string, number>, item) => {
    const key = String(item.categoryId || item.category)
    map[key] = (map[key] || 0) + 1
    return map
  }, {})
})

const categoryOptions = computed(() => {
  const options: Array<{ id: string; label: string; description: string; count: number }> = []
  const seen = new Set<string>()

  for (const item of categories.value) {
    const id = String(item.id)
    options.push({
      id,
      label: item.categoryName || `分类 ${id}`,
      description: item.description || '平台公开接单分类',
      count: categoryCountMap.value[id] || 0
    })
    seen.add(id)
  }

  for (const item of demands.value) {
    const id = String(item.categoryId || item.category)
    if (!id || seen.has(id)) continue

    options.push({
      id,
      label: item.category || `分类 ${id}`,
      description: '历史公开需求分类',
      count: categoryCountMap.value[id] || 0
    })
    seen.add(id)
  }

  return options
})

const activeCategoryLabel = computed(() => {
  if (!selectedCategoryIds.value.length) return '全部分类'

  const labels = categoryOptions.value
    .filter((item) => selectedCategoryIds.value.includes(item.id))
    .map((item) => item.label)

  if (!labels.length) return '全部分类'
  if (labels.length <= 2) return labels.join(' / ')
  return `${labels.slice(0, 2).join(' / ')} 等 ${labels.length} 个分类`
})

const normalizedKeyword = computed(() => keyword.value.trim().toLowerCase())

const filteredDemands = computed(() =>
  demands.value
    .filter((item) => {
      if (selectedCategoryIds.value.length) {
        const target = String(item.categoryId || item.category)
        if (!selectedCategoryIds.value.includes(target)) return false
      }

      if (!normalizedKeyword.value) return true
      return item.searchText.includes(normalizedKeyword.value)
    })
    .sort((left, right) => Number(right.id) - Number(left.id))
)

const totalPages = computed(() => Math.max(1, Math.ceil(filteredDemands.value.length / PAGE_SIZE)))
const pageStart = computed(() => (currentPage.value - 1) * PAGE_SIZE)
const pageDemands = computed(() => filteredDemands.value.slice(pageStart.value, pageStart.value + PAGE_SIZE))
const leftColumnDemands = computed(() => pageDemands.value.slice(0, COLUMN_SIZE))
const rightColumnDemands = computed(() => pageDemands.value.slice(COLUMN_SIZE, PAGE_SIZE))

const resultRangeText = computed(() => {
  if (!filteredDemands.value.length) return '0 / 0'

  const start = pageStart.value + 1
  const end = Math.min(pageStart.value + PAGE_SIZE, filteredDemands.value.length)
  return `${start}-${end} / ${filteredDemands.value.length}`
})

const summaryCards = computed(() => [
  {
    label: '公开订单',
    value: String(demands.value.length).padStart(2, '0'),
    note: '默认按最新需求优先展示'
  },
  {
    label: '有效分类',
    value: String(categoryOptions.value.filter((item) => item.count > 0).length).padStart(2, '0'),
    note: '分类筛选已支持下拉多选'
  },
  {
    label: '当前匹配',
    value: String(filteredDemands.value.length).padStart(2, '0'),
    note: `${activeCategoryLabel.value}${normalizedKeyword.value ? ' + 关键词搜索' : ''}`
  }
])

const hasActiveFilters = computed(() => Boolean(keyword.value.trim()) || selectedCategoryIds.value.length > 0)

watch([filteredDemands, currentPage], () => {
  if (currentPage.value <= totalPages.value) return
  updateRouteState({ page: totalPages.value })
})

watch(categoryOptions, (options) => {
  if (!dataLoaded.value || !selectedCategoryIds.value.length) return

  const validIds = new Set(options.map((item) => item.id))
  const nextCategories = selectedCategoryIds.value.filter((item) => validIds.has(item))

  if (nextCategories.length !== selectedCategoryIds.value.length) {
    updateRouteState({ categories: nextCategories, page: 1 })
  }
})

async function loadMarketData() {
  loading.value = true
  dataLoaded.value = false

  try {
    const [demandResponse, categoryResponse] = await Promise.all([
      fetchPublicMarketDemands(),
      fetchDemandCategories()
    ])

    demands.value = (demandResponse.data || []).map((item: any) => normalizeMarketDemand(item))
    categories.value = categoryResponse.data || []
  } catch (error: any) {
    demands.value = []
    categories.value = []
    ElMessage.error(error?.message || '接单大厅加载失败，请稍后重试')
  } finally {
    loading.value = false
    dataLoaded.value = true
  }
}

function applyKeywordSearch() {
  updateRouteState({
    keyword: keyword.value.trim(),
    categories: selectedCategoryIds.value,
    page: 1
  })
}

function applyCategorySelection() {
  updateRouteState({
    categories: selectedCategoryIds.value,
    page: 1
  })
}

function resetFilters() {
  keyword.value = ''
  selectedCategoryIds.value = []
  updateRouteState({
    keyword: '',
    categories: [],
    page: 1
  })
}

function handlePageChange(page: number) {
  updateRouteState({ page })
}

onMounted(loadMarketData)
</script>

<template>
  <div class="market-page">
    <TopNav />

    <main class="market-container market-hall">
      <section class="market-hall__hero">
        <div class="market-hall__hero-copy">
          <span class="market-hall__eyebrow">Market Hall</span>
          <h1>接单大厅</h1>
          <p>
            进入大厅后先用关键词和分类筛选，再看下面的分页需求订单。
            现在分类已经改成下拉框，并且支持多选，适合同时查看多个赛道的需求。
          </p>
        </div>

        <div class="market-hall__stats">
          <article v-for="item in summaryCards" :key="item.label" class="market-hall__stat">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <p>{{ item.note }}</p>
          </article>
        </div>
      </section>

      <section class="market-feed-card market-hall__filters">
        <div class="market-hall__filters-top">
          <div>
            <span class="market-feed-head__eyebrow">Search & Categories</span>
            <h2>先筛选，再接单</h2>
            <p>关键词搜索和分类下拉可以一起使用，分类支持多选，刷新后会保留当前筛选状态。</p>
          </div>

          <button
            class="market-btn market-btn--ghost market-hall__reset"
            type="button"
            :disabled="!hasActiveFilters"
            @click="resetFilters"
          >
            <el-icon><RefreshRight /></el-icon>
            重置筛选
          </button>
        </div>

        <div class="market-hall__search-row">
          <label class="market-hall__search-field" for="market-keyword">
            <span>关键词搜索</span>
            <el-input
              id="market-keyword"
              v-model="keyword"
              size="large"
              placeholder="搜索标题、需求编号、分类或交付方式"
              @keyup.enter="applyKeywordSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </label>

          <label class="market-hall__search-field market-hall__search-field--select">
            <span>分类筛选</span>
            <el-select
              v-model="selectedCategoryIds"
              multiple
              clearable
              collapse-tags
              collapse-tags-tooltip
              filterable
              placeholder="可选择一个或多个分类"
              @change="applyCategorySelection"
            >
              <el-option
                v-for="item in categoryOptions"
                :key="item.id"
                :label="`${item.label} (${item.count})`"
                :value="item.id"
              />
            </el-select>
          </label>

          <button class="market-btn market-btn--primary market-hall__search-btn" type="button" @click="applyKeywordSearch">
            搜索需求
          </button>
        </div>

        <p class="market-hall__filter-note">
          未选择分类时默认查看全部分类；下拉框支持多选、搜索和清空。
        </p>
      </section>

      <section class="market-feed-card market-hall__board">
        <div class="market-feed-head market-hall__board-head">
          <div class="market-feed-head__copy">
            <span class="market-feed-head__eyebrow">Demand Orders</span>
            <h2>分页需求订单</h2>
            <p>当前分类：{{ activeCategoryLabel }}。{{ normalizedKeyword ? `关键词：${keyword.trim()}` : '未设置关键词筛选。' }}</p>
          </div>

          <div class="market-hall__board-meta">
            <span class="market-feed-badge">
              <el-icon><Tickets /></el-icon>
              {{ resultRangeText }}
            </span>
            <span class="market-feed-badge">
              <el-icon><CollectionTag /></el-icon>
              {{ filteredDemands.length }} 条匹配
            </span>
            <span class="market-feed-badge">
              <el-icon><Files /></el-icon>
              第 {{ currentPage }} / {{ totalPages }} 页
            </span>
          </div>
        </div>

        <div v-if="loading" class="market-hall__skeleton">
          <article v-for="index in 3" :key="index" class="market-hall__skeleton-card">
            <el-skeleton animated>
              <template #template>
                <div class="market-hall__skeleton-grid">
                  <el-skeleton-item variant="image" class="market-hall__skeleton-media" />
                  <div class="market-hall__skeleton-copy">
                    <el-skeleton-item variant="p" style="width: 28%" />
                    <el-skeleton-item variant="h3" style="width: 58%" />
                    <el-skeleton-item variant="text" style="width: 100%" />
                    <el-skeleton-item variant="text" style="width: 92%" />
                    <el-skeleton-item variant="text" style="width: 70%" />
                  </div>
                </div>
              </template>
            </el-skeleton>
          </article>
        </div>

        <div v-else-if="pageDemands.length" class="market-hall__columns">
          <section class="market-hall__column">
            <div class="market-hall__column-head">
              <strong>左栏需求</strong>
              <span>{{ leftColumnDemands.length }} / {{ COLUMN_SIZE }}</span>
            </div>

            <div class="market-list market-hall__column-list">
              <DemandCard v-for="item in leftColumnDemands" :key="item.id" :item="item" />
            </div>
          </section>

          <section class="market-hall__column">
            <div class="market-hall__column-head">
              <strong>右栏需求</strong>
              <span>{{ rightColumnDemands.length }} / {{ COLUMN_SIZE }}</span>
            </div>

            <div v-if="rightColumnDemands.length" class="market-list market-hall__column-list">
              <DemandCard v-for="item in rightColumnDemands" :key="item.id" :item="item" />
            </div>

            <div v-else class="market-hall__column-empty">
              当前页右栏暂无更多需求
            </div>
          </section>
        </div>

        <div v-else class="market-hall__empty">
          <h3>没有匹配的需求订单</h3>
          <p>可以换个关键词，或者清空分类筛选后看看最新公开需求。</p>
          <button class="market-btn market-btn--primary" type="button" @click="resetFilters">查看全部需求</button>
        </div>

        <div v-if="filteredDemands.length > PAGE_SIZE" class="market-hall__pagination">
          <p>每页固定 20 条需求订单，左右两栏各展示 10 条，切页时保留当前搜索词和多选分类。</p>
          <el-pagination
            background
            layout="prev, pager, next"
            :current-page="currentPage"
            :page-size="PAGE_SIZE"
            :total="filteredDemands.length"
            @current-change="handlePageChange"
          />
        </div>
      </section>
    </main>

    <Footer />
  </div>
</template>

<style scoped>
.market-hall {
  display: grid;
  gap: 22px;
  padding: 20px 0 34px;
}

.market-hall__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 420px);
  gap: 22px;
  align-items: stretch;
}

.market-hall__hero-copy,
.market-hall__stat {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 30px;
  background: var(--market-card);
  box-shadow: var(--market-shadow);
  backdrop-filter: blur(18px);
}

.market-hall__hero-copy::before,
.market-hall__stat::before {
  content: '';
  position: absolute;
  inset: 0 0 auto;
  height: 1px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.82), rgba(17, 19, 34, 0.16), rgba(255, 255, 255, 0));
}

.market-hall__hero-copy {
  padding: 28px;
  background:
    radial-gradient(circle at top right, rgba(243, 190, 37, 0.22), transparent 28%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.76));
}

.market-hall__eyebrow {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(17, 19, 34, 0.06);
  color: rgba(17, 19, 34, 0.58);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.market-hall__hero-copy h1 {
  margin-top: 18px;
  font: 700 clamp(34px, 4vw, 54px) / 0.96 var(--font-display);
  letter-spacing: -0.05em;
}

.market-hall__hero-copy p {
  max-width: 56ch;
  margin-top: 16px;
  color: rgba(17, 19, 34, 0.66);
  font-size: 15px;
  line-height: 1.9;
}

.market-hall__stats {
  display: grid;
  gap: 14px;
}

.market-hall__stat {
  padding: 20px 22px;
  background: rgba(255, 255, 255, 0.86);
}

.market-hall__stat span {
  color: rgba(17, 19, 34, 0.52);
  font-size: 13px;
  font-weight: 600;
}

.market-hall__stat strong {
  display: block;
  margin-top: 10px;
  font: 700 34px / 1 var(--font-display);
  letter-spacing: -0.04em;
}

.market-hall__stat p {
  margin-top: 10px;
  color: rgba(17, 19, 34, 0.62);
  font-size: 14px;
  line-height: 1.7;
}

.market-hall__filters,
.market-hall__board {
  padding: 24px;
  border-radius: 30px;
}

.market-hall__filters-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.market-hall__filters-top h2 {
  margin-top: 10px;
  font: 700 30px / 1.04 var(--font-display);
}

.market-hall__filters-top p {
  margin-top: 10px;
  color: rgba(17, 19, 34, 0.6);
  line-height: 1.8;
}

.market-hall__reset:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.market-hall__search-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px) auto;
  gap: 14px;
  align-items: end;
  margin-top: 20px;
}

.market-hall__search-field {
  display: grid;
  gap: 10px;
}

.market-hall__search-field span {
  color: rgba(17, 19, 34, 0.58);
  font-size: 13px;
  font-weight: 700;
}

.market-hall__search-field :deep(.el-input__wrapper),
.market-hall__search-field :deep(.el-select__wrapper) {
  min-height: 56px;
  border-radius: 18px !important;
}

.market-hall__search-field--select :deep(.el-select) {
  width: 100%;
}

.market-hall__search-btn {
  min-height: 56px;
  padding-inline: 24px;
}

.market-hall__filter-note {
  margin-top: 14px;
  color: rgba(17, 19, 34, 0.56);
  font-size: 13px;
  line-height: 1.8;
}

.market-hall__board-head {
  align-items: flex-start;
}

.market-hall__board-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.market-feed-badge {
  gap: 8px;
}

.market-hall__skeleton {
  display: grid;
  gap: 16px;
}

.market-hall__skeleton-card {
  padding: 18px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.88);
}

.market-hall__skeleton-grid {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
  align-items: stretch;
}

.market-hall__skeleton-media {
  width: 100%;
  height: 220px;
  border-radius: 22px;
}

.market-hall__skeleton-copy {
  display: grid;
  gap: 14px;
  align-content: center;
}

.market-hall__columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.market-hall__column {
  display: grid;
  gap: 14px;
  align-content: start;
}

.market-hall__column-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid rgba(17, 19, 34, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.84);
}

.market-hall__column-head strong {
  font-size: 16px;
}

.market-hall__column-head span {
  color: rgba(17, 19, 34, 0.56);
  font-size: 13px;
  font-weight: 700;
}

.market-hall__column-list {
  align-content: start;
}

.market-hall__column-empty {
  padding: 24px 18px;
  border: 1px dashed rgba(17, 19, 34, 0.14);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  color: rgba(17, 19, 34, 0.56);
  line-height: 1.8;
}

.market-hall__empty {
  display: grid;
  justify-items: start;
  gap: 12px;
  padding: 24px 6px 6px;
}

.market-hall__empty h3 {
  font-size: 26px;
}

.market-hall__empty p,
.market-hall__pagination p {
  color: rgba(17, 19, 34, 0.58);
  line-height: 1.8;
}

.market-hall__pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 22px;
  padding-top: 22px;
  border-top: 1px solid rgba(17, 19, 34, 0.08);
}

@media (max-width: 1180px) {
  .market-hall__hero,
  .market-hall__columns {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 920px) {
  .market-hall__search-row {
    grid-template-columns: 1fr;
  }

  .market-hall__search-btn {
    width: 100%;
  }
}

@media (max-width: 820px) {
  .market-hall__filters-top,
  .market-hall__pagination {
    flex-direction: column;
    align-items: stretch;
  }

  .market-hall__board-meta {
    justify-content: flex-start;
  }
}

@media (max-width: 720px) {
  .market-hall {
    padding: 16px 0 28px;
  }

  .market-hall__hero-copy,
  .market-hall__filters,
  .market-hall__board {
    padding: 18px;
  }

  .market-hall__skeleton-grid {
    grid-template-columns: 1fr;
  }

  .market-hall__skeleton-media {
    height: 180px;
  }
}
</style>
