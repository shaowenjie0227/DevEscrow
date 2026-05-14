<template>
  <div class="page-shell auth-shell">
    <div class="auth-frame">
      <section class="auth-intro">
        <div>
          <span class="auth-badge">Programmer Escrow</span>
          <h1 class="auth-title">把需求、报价、托管和交付放进同一条可信交易链路。</h1>
          <p class="auth-desc">
            面向用户端与开发者的担保协作入口。注册后即可进入对应工作台，从需求发布到验收放款都能在一个界面内完成。
          </p>
        </div>

        <div class="auth-highlights">
          <article>
            <strong>01</strong>
            <span>用统一流程管理需求审核、报价筛选、订单托管和验收交付，减少来回切换。</span>
          </article>
          <article>
            <strong>02</strong>
            <span>用户端和开发者共享同一条交易链路，关键节点更透明，协作节奏更容易对齐。</span>
          </article>
          <article>
            <strong>03</strong>
            <span>一旦出现异常，纠纷记录和平台仲裁入口都已经预留好，不需要再补搭流程。</span>
          </article>
        </div>
      </section>

      <section class="page-card auth-panel">
        <div class="auth-panel-head">
          <div>
            <h2>开始使用</h2>
            <p>先登录已有账户，或者直接注册并进入你的协作工作台。</p>
          </div>
          <router-link class="auth-link" to="/admin/login">管理后台</router-link>
        </div>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="用户登录" name="login">
            <el-form :model="loginForm" label-position="top" @submit.prevent="handleLogin">
              <el-form-item label="账号">
                <el-input v-model="loginForm.account" placeholder="手机号 / 邮箱 / 用户编号" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="loginForm.password" type="password" show-password />
              </el-form-item>
              <el-form-item label="进入身份">
                <el-radio-group v-model="entryRole">
                  <el-radio-button label="client">用户端</el-radio-button>
                  <el-radio-button label="developer">程序员</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-button class="auth-submit" type="primary" :loading="submitting" @click="handleLogin">登录并进入</el-button>
              <p class="auth-form-note">登录成功后会直接进入对应角色的工作台，继续当前交易流程。</p>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册账号" name="register">
            <el-form :model="registerForm" label-position="top" @submit.prevent="handleRegister">
              <el-form-item label="手机号">
                <el-input v-model="registerForm.phone" placeholder="13800000000" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="registerForm.email" placeholder="可选，用于辅助登录" />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="registerForm.nickname" placeholder="给自己起一个容易识别的名字" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="registerForm.password" type="password" show-password />
              </el-form-item>
              <el-form-item label="用户类型">
                <el-select v-model="registerForm.userType" style="width: 100%">
                  <el-option label="用户端" :value="1" />
                  <el-option label="程序员" :value="2" />
                  <el-option label="双角色" :value="3" />
                </el-select>
              </el-form-item>
              <el-button class="auth-submit" type="primary" :loading="submitting" @click="handleRegister">
                注册并进入工作台
              </el-button>
              <p class="auth-form-note">如果选择双角色，注册完成后会先进入用户端工作台，也可以后续切换身份使用。</p>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/modules/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('login')
const entryRole = ref('client')
const submitting = ref(false)
const loginForm = reactive({
  account: '',
  password: ''
})
const registerForm = reactive({
  phone: '',
  email: '',
  nickname: '',
  password: '',
  userType: 1
})

async function handleLogin() {
  submitting.value = true
  try {
    const response = await login(loginForm)
    authStore.setLogin(response)
    ElMessage.success('登录成功')
    router.push(entryRole.value === 'developer' ? '/developer/home' : '/client/home')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    submitting.value = false
  }
}

async function handleRegister() {
  submitting.value = true
  try {
    const response = await register(registerForm)
    authStore.setLogin(response)
    ElMessage.success('注册成功')
    const target = registerForm.userType === 2 ? '/developer/home' : '/client/home'
    router.push(target)
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    submitting.value = false
  }
}
</script>
