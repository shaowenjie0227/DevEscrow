<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>纠纷处理</h2>
      <el-button @click="loadDisputes">刷新</el-button>
    </div>
    <el-table :data="disputes" v-loading="loading">
      <el-table-column prop="disputeNo" label="纠纷编号" min-width="180" />
      <el-table-column prop="orderId" label="订单 ID" width="120" />
      <el-table-column label="纠纷类型" width="130">
        <template #default="{ row }">
          <el-tag :type="getDisputeType(row.disputeType).type">{{ getDisputeType(row.disputeType).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="纠纷原因" min-width="200" />
      <el-table-column label="处理状态" width="140">
        <template #default="{ row }">
          <el-tag :type="getDisputeStatus(row.status).type">{{ getDisputeStatus(row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleResolve(row, 4, '继续履约')">继续履约</el-button>
          <el-button size="small" type="warning" @click="handleResolve(row, 1, '退款甲方')">退款甲方</el-button>
          <el-button size="small" type="success" @click="handleResolve(row, 2, '放款乙方')">放款乙方</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAdminDisputes, resolveDispute } from '@/api/modules/admin'
import { getDisputeStatus, getDisputeType } from '@/utils/status'

const loading = ref(false)
const disputes = ref([])

async function loadDisputes() {
  loading.value = true
  try {
    const response = await fetchAdminDisputes()
    disputes.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载纠纷列表失败')
  } finally {
    loading.value = false
  }
}

async function handleResolve(row, resultType, actionLabel) {
  try {
    const result = await ElMessageBox.prompt(
      `请填写纠纷 ${row.disputeNo} 的处理说明`,
      actionLabel,
      { inputValue: actionLabel }
    )
    await resolveDispute(row.id, {
      resultType,
      resolutionNote: result.value
    })
    ElMessage.success('纠纷处理完成')
    loadDisputes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '处理失败')
    }
  }
}

onMounted(loadDisputes)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>
