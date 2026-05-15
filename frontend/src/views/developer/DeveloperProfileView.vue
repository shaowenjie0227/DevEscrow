<template>
  <section class="page-card bridge-card">
    <div>
      <h2>开发者资料已迁移到个人中心</h2>
      <p>
        开发者申请、实名资料、身份证照片和技术栈维护现在统一放在个人中心管理。
        审核通过后，你的开发者工作台权限会自动生效。
      </p>
    </div>
    <el-button type="primary" @click="router.push('/me')">前往个人中心</el-button>
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.bridge-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.bridge-card h2 {
  margin: 0 0 12px;
}

.bridge-card p {
  margin: 0;
  color: #64748b;
  line-height: 1.8;
}

@media (max-width: 720px) {
  .bridge-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
