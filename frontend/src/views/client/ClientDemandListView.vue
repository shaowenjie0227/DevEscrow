<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>我的需求</h2>
      <div class="toolbar-actions">
        <el-button @click="loadDemands">刷新</el-button>
        <el-button type="primary" @click="$router.push('/client/demands/create')">新建需求</el-button>
      </div>
    </div>

    <el-table :data="demands" v-loading="loading">
      <el-table-column label="封面" width="108">
        <template #default="{ row }">
          <div class="cover-cell">
            <img v-if="row.coverImage" :src="row.coverImage" alt="需求封面" />
            <span v-else>无图</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="demandNo" label="需求编号" min-width="180" />
      <el-table-column prop="title" label="需求标题" min-width="220" />
      <el-table-column prop="category" label="分类" width="140" />
      <el-table-column label="审核状态" width="150">
        <template #default="{ row }">
          <el-tag :type="getDemandReviewStatus(row.reviewStatus).type">
            {{ getDemandReviewStatus(row.reviewStatus).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务状态" width="180">
        <template #default="{ row }">
          <el-tag :type="getDemandStatus(row.status).type">
            {{ getDemandStatus(row.status).label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openQuotes(row)">查看报价</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="quoteDrawerVisible" size="48%" title="需求报价列表">
      <div v-if="currentDemand" class="drawer-header">
        <img v-if="currentDemand.coverImage" :src="currentDemand.coverImage" alt="需求封面" class="drawer-cover" />
        <div>
          <strong>{{ currentDemand.title }}</strong>
          <p>{{ currentDemand.summary }}</p>
          <div class="drawer-meta">
            <span>{{ currentDemand.images?.length || 0 }} 张图片</span>
            <span>{{ currentDemand.attachments?.length || 0 }} 个附件</span>
            <span v-if="currentDemand.deliveryType === 2">{{ currentDemand.stagePlans?.length || 0 }} 个阶段</span>
          </div>
        </div>
      </div>
      <el-table :data="quotes" v-loading="quoteLoading">
        <el-table-column prop="quoteNo" label="报价编号" min-width="160" />
        <el-table-column prop="developerId" label="开发者 ID" width="120" />
        <el-table-column prop="priceTotal" label="报价金额" width="120" />
        <el-table-column prop="estimatedDays" label="工期" width="100" />
        <el-table-column label="报价状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getQuoteStatus(row.status).type">{{ getQuoteStatus(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleCreateOrder(row)">创建订单</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/modules/order'
import { fetchDemandQuotes, fetchMyDemands } from '@/api/modules/demand'
import { getDemandReviewStatus, getDemandStatus, getQuoteStatus } from '@/utils/status'

const loading = ref(false)
const quoteLoading = ref(false)
const demands = ref([])
const quotes = ref([])
const currentDemand = ref(null)
const quoteDrawerVisible = ref(false)

async function loadDemands() {
  loading.value = true
  try {
    const response = await fetchMyDemands()
    demands.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载需求列表失败')
  } finally {
    loading.value = false
  }
}

async function openQuotes(demand) {
  currentDemand.value = demand
  quoteDrawerVisible.value = true
  quoteLoading.value = true
  try {
    const response = await fetchDemandQuotes(demand.id)
    quotes.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载报价列表失败')
  } finally {
    quoteLoading.value = false
  }
}

async function handleCreateOrder(quote) {
  if (!currentDemand.value) {
    return
  }
  try {
    await createOrder({
      demandId: currentDemand.value.id,
      quoteId: quote.id
    })
    ElMessage.success('订单创建成功，请前往我的订单完成托管支付')
    quoteDrawerVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '创建订单失败')
  }
}

onMounted(loadDemands)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.drawer-header {
  display: grid;
  grid-template-columns: 112px minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.drawer-cover,
.cover-cell img {
  width: 100%;
  height: 80px;
  object-fit: cover;
  border-radius: 16px;
}

.cover-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 56px;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.92);
  color: var(--text-soft);
  overflow: hidden;
}

.drawer-header p {
  margin: 8px 0 0;
  color: var(--text-sub);
}

.drawer-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.drawer-meta span {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.08);
  color: var(--brand-primary);
  font-size: 12px;
  font-weight: 700;
}
</style>
