<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>需求市场</h2>
      <el-button @click="loadDemands">刷新</el-button>
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
      <el-table-column prop="budgetMin" label="预算下限" width="130" />
      <el-table-column prop="budgetMax" label="预算上限" width="130" />
      <el-table-column prop="expectedDays" label="工期" width="100" />
      <el-table-column label="状态" width="160">
        <template #default="{ row }">
          <el-tag :type="getDemandStatus(row.status).type">{{ getDemandStatus(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openQuoteDialog(row)">立即报价</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="quoteDialogVisible" width="640px" title="提交报价">
      <el-form :model="quoteForm" label-position="top">
        <el-form-item label="需求">
          <el-input :model-value="selectedDemand?.title || ''" disabled />
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
        <el-button type="primary" :loading="submitting" @click="handleSubmitQuote">提交报价</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMarketDemands } from '@/api/modules/demand'
import { createQuote } from '@/api/modules/quote'
import { getDemandStatus } from '@/utils/status'

const loading = ref(false)
const submitting = ref(false)
const demands = ref([])
const selectedDemand = ref(null)
const quoteDialogVisible = ref(false)
const quoteForm = reactive({
  priceTotal: 3000,
  estimatedDays: 7,
  deliveryDesc: '',
  techSolution: ''
})

async function loadDemands() {
  loading.value = true
  try {
    const response = await fetchMarketDemands()
    demands.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载需求市场失败')
  } finally {
    loading.value = false
  }
}

function openQuoteDialog(demand) {
  selectedDemand.value = demand
  quoteDialogVisible.value = true
  quoteForm.priceTotal = Number(demand.budgetMax || 3000)
  quoteForm.estimatedDays = Number(demand.expectedDays || 7)
  quoteForm.deliveryDesc = '优先交付一版可运行的 MVP 版本'
  quoteForm.techSolution = '采用 Spring Boot + Vue3 技术栈，先完成核心功能，再逐步优化细节。'
}

async function handleSubmitQuote() {
  if (!selectedDemand.value) {
    return
  }
  submitting.value = true
  try {
    await createQuote({
      demandId: selectedDemand.value.id,
      ...quoteForm
    })
    ElMessage.success('报价提交成功')
    quoteDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '报价提交失败')
  } finally {
    submitting.value = false
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

.cover-cell img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
