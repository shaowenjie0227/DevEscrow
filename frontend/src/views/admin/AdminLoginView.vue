<template>
  <section class="page-shell auth-shell">
    <div class="auth-frame">
      <section class="auth-intro">
        <div>
          <span class="auth-badge">Operations Console</span>
          <h1 class="auth-title">在一个运营视图里处理审核、订单与纠纷。</h1>
          <p class="auth-desc">
            这是平台管理后台入口，用来集中处理用户状态、需求审核、订单跟踪和纠纷裁决等关键运营动作。
          </p>
        </div>

        <div class="auth-highlights">
          <article>
            <strong>01</strong>
            <span>需求审核、订单进度和纠纷处理放在同一条运营链路里，信息更集中。</span>
          </article>
          <article>
            <strong>02</strong>
            <span>当前环境支持默认管理员账号本地登录，便于你快速验证后台流程和视觉效果。</span>
          </article>
          <article>
            <strong>03</strong>
            <span>后台入口保持和用户端一致的视觉系统，但会更突出操作效率和状态识别。</span>
          </article>
        </div>
      </section>

      <section class="page-card auth-panel">
        <div class="auth-panel-head">
          <div>
            <h2>管理员登录</h2>
            <p>首次本地启动可直接使用默认账号进入后台，后续也方便替换成正式权限体系。</p>
          </div>
          <router-link class="auth-link" to="/market">返回用户端</router-link>
        </div>

        <el-form :model="form" label-position="top" @submit.prevent="handleLogin">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="admin" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password placeholder="admin123456" />
          </el-form-item>
          <el-button class="auth-submit" type="primary" :loading="submitting" @click="handleLogin">登录后台</el-button>
          <p class="auth-form-note">默认演示账号为 `admin / admin123456`，方便你直接检查后台页面和流程。</p>
        </el-form>
      </section>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/modules/admin'
import { useAdminAuthStore } from '@/stores/adminAuth'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()
const submitting = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123456'
})

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  submitting.value = true
  try {
    const response = await adminLogin(form)
    adminAuthStore.setLogin(response)
    ElMessage.success('管理员登录成功')
    router.push('/admin/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '管理员登录失败')
  } finally {
    submitting.value = false
  }
}
</script>
