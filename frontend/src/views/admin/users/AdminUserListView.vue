<template>
  <section class="page-card">
    <div class="toolbar">
      <h2>用户管理</h2>
      <el-button @click="loadUsers">刷新</el-button>
    </div>
    <el-table :data="users" v-loading="loading">
      <el-table-column prop="userNo" label="用户编号" min-width="180" />
      <el-table-column prop="nickname" label="昵称" min-width="180" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="220" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag>{{ formatUserType(row.userType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '已封禁' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminUsers } from '@/api/modules/admin'

const loading = ref(false)
const users = ref([])

function formatUserType(userType) {
  if (userType === 1) return '用户端'
  if (userType === 2) return '程序员'
  if (userType === 3) return '双角色'
  return String(userType ?? '-')
}

async function loadUsers() {
  loading.value = true
  try {
    const response = await fetchAdminUsers()
    users.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>
