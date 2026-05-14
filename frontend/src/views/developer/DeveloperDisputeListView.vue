<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>我的纠纷</h2>
      <div class="toolbar-actions">
        <el-button @click="loadDisputes">刷新</el-button>
        <el-button type="primary" @click="dialogVisible = true">新建纠纷</el-button>
      </div>
    </div>

    <el-table :data="disputes" v-loading="loading">
      <el-table-column prop="disputeNo" label="纠纷编号" min-width="180" />
      <el-table-column prop="orderId" label="订单 ID" width="120" />
      <el-table-column label="类型" width="130">
        <template #default="{ row }">
          <el-tag :type="getDisputeType(row.disputeType).type">{{ getDisputeType(row.disputeType).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="220" />
      <el-table-column label="状态" width="140">
        <template #default="{ row }">
          <el-tag :type="getDisputeStatus(row.status).type">{{ getDisputeStatus(row.status).label }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" width="640px" title="发起纠纷">
      <el-form :model="form" label-position="top">
        <el-form-item label="订单 ID">
          <el-input-number v-model="form.orderId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="纠纷类型">
          <el-select v-model="form.disputeType" style="width: 100%">
            <el-option label="延期" :value="1" />
            <el-option label="质量问题" :value="2" />
            <el-option label="未交付" :value="3" />
            <el-option label="付款争议" :value="4" />
            <el-option label="违规沟通" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="纠纷原因">
          <el-input v-model="form.reason" />
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input v-model="form.detail" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">提交</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createDeveloperDispute, fetchDeveloperDisputes } from '@/api/modules/dispute'
import { getDisputeStatus, getDisputeType } from '@/utils/status'

const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const disputes = ref([])
const dialogVisible = ref(false)
const form = reactive({
  orderId: route.query.orderId ? Number(route.query.orderId) : null,
  disputeType: 2,
  reason: '',
  detail: ''
})

watch(
  () => route.query.orderId,
  (orderId) => {
    if (orderId) {
      form.orderId = Number(orderId)
      dialogVisible.value = true
    }
  },
  { immediate: true }
)

async function loadDisputes() {
  loading.value = true
  try {
    const response = await fetchDeveloperDisputes()
    disputes.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载纠纷列表失败')
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  submitting.value = true
  try {
    await createDeveloperDispute(form)
    ElMessage.success('纠纷创建成功')
    dialogVisible.value = false
    form.reason = ''
    form.detail = ''
    loadDisputes()
  } catch (error) {
    ElMessage.error(error.message || '发起纠纷失败')
  } finally {
    submitting.value = false
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

.toolbar-actions {
  display: flex;
  gap: 12px;
}
</style>
