<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>我的订单</h2>
      <el-button @click="loadOrders">刷新</el-button>
    </div>
    <el-table :data="orders" v-loading="loading">
      <el-table-column prop="orderNo" label="订单编号" min-width="180" />
      <el-table-column prop="orderTitle" label="订单标题" min-width="220" />
      <el-table-column prop="amountTotal" label="金额" width="130" />
      <el-table-column label="状态" width="150">
        <template #default="{ row }">
          <el-tag :type="getOrderStatus(row.status).type">{{ getOrderStatus(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="progressPercent" label="进度" width="110" />
      <el-table-column label="操作" width="440" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row.id)">详情</el-button>
          <el-button v-if="row.status === 2" size="small" type="primary" @click="handleStart(row.id)">开始开发</el-button>
          <el-button v-if="row.status === 3" size="small" type="success" @click="handleSubmit(row.id)">提交交付</el-button>
          <el-button v-if="[2, 3, 4].includes(row.status)" size="small" type="warning" @click="openDispute(row.id)">
            发起纠纷
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="detailVisible" size="40%" title="订单详情">
      <div v-if="currentOrder" class="detail-grid">
        <p><strong>订单编号：</strong>{{ currentOrder.orderNo }}</p>
        <p><strong>需求 ID：</strong>{{ currentOrder.demandId }}</p>
        <p><strong>用户端 ID：</strong>{{ currentOrder.clientId }}</p>
        <p><strong>当前阶段：</strong>{{ currentOrder.currentStageNo || 1 }} / {{ currentOrder.stageCount || 1 }}</p>
        <p><strong>订单状态：</strong>{{ getOrderStatus(currentOrder.status).label }}</p>
        <p><strong>进度：</strong>{{ currentOrder.progressPercent }}%</p>
        <p><strong>交付物：</strong>{{ currentOrder.deliverables?.join(', ') || '-' }}</p>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchDeveloperOrderDetail,
  fetchDeveloperOrders,
  startDeveloperOrder,
  submitDeveloperOrder
} from '@/api/modules/order'
import { getOrderStatus } from '@/utils/status'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const detailVisible = ref(false)
const currentOrder = ref(null)

async function loadOrders() {
  loading.value = true
  try {
    const response = await fetchDeveloperOrders()
    orders.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载订单列表失败')
  } finally {
    loading.value = false
  }
}

async function showDetail(orderId) {
  try {
    const response = await fetchDeveloperOrderDetail(orderId)
    currentOrder.value = response.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '加载订单详情失败')
  }
}

async function handleStart(orderId) {
  try {
    await startDeveloperOrder(orderId, '开发者已开始开发')
    ElMessage.success('订单已开始开发')
    loadOrders()
  } catch (error) {
    ElMessage.error(error.message || '开始开发失败')
  }
}

async function handleSubmit(orderId) {
  try {
    const result = await ElMessageBox.prompt('请填写本次交付说明', '提交交付', {
      inputValue: '代码与部署包已准备完成'
    })
    await submitDeveloperOrder(orderId, {
      submitContent: result.value,
      deliverables: ['https://example.com/release.zip']
    })
    ElMessage.success('交付提交成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '交付提交失败')
    }
  }
}

function openDispute(orderId) {
  router.push(`/developer/disputes?orderId=${orderId}`)
}

onMounted(loadOrders)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.detail-grid {
  display: grid;
  gap: 10px;
}

.detail-grid p {
  margin: 0;
}
</style>
