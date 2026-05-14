<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>需求审核</h2>
      <div class="toolbar-actions">
        <el-button @click="loadDemands">刷新</el-button>
      </div>
    </div>
    <el-table :data="demands" v-loading="loading">
      <el-table-column prop="demandNo" label="需求编号" min-width="180" />
      <el-table-column prop="title" label="需求标题" min-width="220" />
      <el-table-column prop="category" label="分类" width="180" />
      <el-table-column label="审核状态" width="160">
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
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="handleAudit(row, 'approve')">通过</el-button>
          <el-button size="small" type="danger" @click="handleAudit(row, 'reject')">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveDemand, fetchAdminDemands, rejectDemand } from '@/api/modules/admin'
import { getDemandReviewStatus, getDemandStatus } from '@/utils/status'

const loading = ref(false)
const demands = ref([])

async function loadDemands() {
  loading.value = true
  try {
    const response = await fetchAdminDemands()
    demands.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载需求列表失败')
  } finally {
    loading.value = false
  }
}

async function handleAudit(row, action) {
  try {
    const result = await ElMessageBox.prompt(
      `请填写需求 ${row.demandNo} 的审核备注`,
      action === 'approve' ? '通过需求' : '驳回需求',
      { inputValue: action === 'approve' ? '审核通过' : '审核驳回' }
    )
    const payload = { remark: result.value }
    if (action === 'approve') {
      await approveDemand(row.id, payload)
    } else {
      await rejectDemand(row.id, payload)
    }
    ElMessage.success(action === 'approve' ? '需求已审核通过' : '需求已驳回')
    loadDemands()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
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
</style>
