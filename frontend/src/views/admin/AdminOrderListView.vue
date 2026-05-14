<template>
  <section class="page-card orders-page">
    <div class="toolbar">
      <h2>订单管理</h2>
      <el-button @click="loadOrders">刷新</el-button>
    </div>
    <el-table :data="orders" v-loading="loading">
      <el-table-column prop="orderNo" label="订单编号" min-width="180" />
      <el-table-column prop="orderTitle" label="订单标题" min-width="220" />
      <el-table-column prop="amountTotal" label="金额" width="140" />
      <el-table-column label="订单状态" width="150">
        <template #default="{ row }">
          <el-tag :type="getOrderStatus(row.status).type">{{ getOrderStatus(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" width="140">
        <template #default="{ row }">
          <el-tag :type="getPayStatus(row.payStatus).type">{{ getPayStatus(row.payStatus).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="progressPercent" label="进度" width="120" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="detailVisible" size="40%" title="订单详情">
      <div v-if="currentOrder" class="detail-grid">
        <p><strong>订单编号：</strong>{{ currentOrder.orderNo }}</p>
        <p><strong>需求 ID：</strong>{{ currentOrder.demandId }}</p>
        <p><strong>用户端 ID：</strong>{{ currentOrder.clientId }}</p>
        <p><strong>乙方用户 ID：</strong>{{ currentOrder.developerId }}</p>
        <p><strong>当前阶段：</strong>{{ currentOrder.currentStageNo || 1 }} / {{ currentOrder.stageCount || 1 }}</p>
        <p><strong>订单状态：</strong>{{ getOrderStatus(currentOrder.status).label }}</p>
        <p><strong>支付状态：</strong>{{ getPayStatus(currentOrder.payStatus).label }}</p>
        <p><strong>进度：</strong>{{ currentOrder.progressPercent }}%</p>
        <p><strong>交付物：</strong>{{ currentOrder.deliverables?.join(', ') || '-' }}</p>
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminOrderDetail, fetchAdminOrders } from '@/api/modules/admin'
import { getOrderStatus, getPayStatus } from '@/utils/status'

const loading = ref(false)
const orders = ref([])
const detailVisible = ref(false)
const currentOrder = ref(null)

async function loadOrders() {
  loading.value = true
  try {
    const response = await fetchAdminOrders()
    orders.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载订单列表失败')
  } finally {
    loading.value = false
  }
}

async function showDetail(orderId) {
  try {
    const response = await fetchAdminOrderDetail(orderId)
    currentOrder.value = response.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '加载订单详情失败')
  }
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
