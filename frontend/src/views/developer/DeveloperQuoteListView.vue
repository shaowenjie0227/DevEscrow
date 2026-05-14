<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>我的报价</h2>
      <el-button @click="loadQuotes">刷新</el-button>
    </div>
    <el-table :data="quotes" v-loading="loading">
      <el-table-column prop="quoteNo" label="报价编号" min-width="180" />
      <el-table-column prop="demandId" label="需求 ID" width="120" />
      <el-table-column prop="priceTotal" label="报价金额" width="120" />
      <el-table-column prop="estimatedDays" label="工期" width="100" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getQuoteStatus(row.status).type">{{ getQuoteStatus(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deliveryDesc" label="交付说明" min-width="200" />
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMyQuotes } from '@/api/modules/quote'
import { getQuoteStatus } from '@/utils/status'

const loading = ref(false)
const quotes = ref([])

async function loadQuotes() {
  loading.value = true
  try {
    const response = await fetchMyQuotes()
    quotes.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载报价列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadQuotes)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>
