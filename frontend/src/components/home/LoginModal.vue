<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElDialog, ElInput, ElMessage } from 'element-plus'
import { Cellphone, Close, Lock, User } from '@element-plus/icons-vue'
import { login, register } from '@/api/modules/auth'
import { useAuthStore } from '@/stores/auth'

type EntryRole = 'client' | 'developer'
type ModalTab = 'password' | 'register'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [boolean]; success: [] }>()

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref<ModalTab>('password')
const entryRole = ref<EntryRole>('client')
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
  userType: 1,
  realName: '',
  developerRoleType: 1,
  idCardFrontUrl: '',
  idCardBackUrl: '',
  selfieUrl: '',
  skillTagIds: [] as number[]
})

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

function closeDialog() {
  visible.value = false
}

function openAdminLogin() {
  closeDialog()
  router.push('/admin/login')
}

function resolveEntryTarget(roles: string[] = [], preferred: EntryRole) {
  if (preferred === 'developer' && roles.includes('DEVELOPER')) return '/developer/home'
  if (preferred === 'client' && roles.includes('CLIENT')) return '/client/home'
  if (roles.includes('CLIENT')) return '/client/home'
  if (roles.includes('DEVELOPER')) return '/developer/home'
  return '/market'
}

async function handleLogin() {
  submitting.value = true
  try {
    const response = await login(loginForm)
    const payload = response?.data ?? response
    authStore.setLogin(payload)
    closeDialog()
    emit('success')
    ElMessage.success('登录成功')
    router.push(resolveEntryTarget(payload?.roles || [], entryRole.value))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    submitting.value = false
  }
}

async function handleRegister() {
  submitting.value = true
  try {
    const response = await register(registerForm)
    const payload = response?.data ?? response
    authStore.setLogin(payload)
    closeDialog()
    emit('success')
    ElMessage.success('注册成功')
    router.push(registerForm.userType === 2 ? '/developer/profile' : '/client/home')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '注册失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <ElDialog v-model="visible" width="980px" :show-close="false" align-center class="login-modal-shell">
    <div class="login-modal">
      <button class="login-modal__close" type="button" @click="closeDialog" aria-label="关闭登录弹窗">
        <el-icon :size="18"><Close /></el-icon>
      </button>

      <div class="login-modal__mascot" aria-hidden="true">
        <span class="login-modal__mascot-eye login-modal__mascot-eye--left"></span>
        <span class="login-modal__mascot-eye login-modal__mascot-eye--right"></span>
        <span class="login-modal__mascot-mouth"></span>
      </div>

      <div class="login-modal__grid">
        <section class="login-modal__main">
          <div class="login-modal__header">
            <p class="login-modal__eyebrow">欢迎回来</p>
            <h2 class="login-modal__title">登录后继续逛需求</h2>
            <p class="login-modal__desc">延续闲鱼式浏览节奏，但接的是你现有系统的真实账号体系。</p>
          </div>

          <div class="login-modal__tabs">
            <button class="login-modal__tab" :class="{ 'is-active': activeTab === 'password' }" type="button" @click="activeTab = 'password'">
              密码登录
            </button>
            <button class="login-modal__tab" :class="{ 'is-active': activeTab === 'register' }" type="button" @click="activeTab = 'register'">
              注册账号
            </button>
          </div>

          <form v-if="activeTab === 'password'" class="login-modal__form" @submit.prevent="handleLogin">
            <div class="login-modal__field">
              <label>账号</label>
              <ElInput v-model="loginForm.account" size="large" class="login-modal-input" placeholder="请输入手机号 / 邮箱 / 用户编号">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__field">
              <label>密码</label>
              <ElInput v-model="loginForm.password" size="large" class="login-modal-input" type="password" show-password placeholder="请输入密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__chooser">
              <p>登录后进入</p>
              <div class="login-modal__chooser-row">
                <button class="login-modal__choice" :class="{ 'is-active': entryRole === 'client' }" type="button" @click="entryRole = 'client'">
                  用户工作台
                </button>
                <button class="login-modal__choice" :class="{ 'is-active': entryRole === 'developer' }" type="button" @click="entryRole = 'developer'">
                  开发者工作台
                </button>
              </div>
            </div>

            <button class="login-modal__submit" type="submit" :disabled="submitting">
              {{ submitting ? '登录中...' : '登录' }}
            </button>

            <p class="login-modal__agreement">登录即代表你已阅读并同意《平台服务协议》《隐私政策》。</p>
          </form>

          <form v-else class="login-modal__form login-modal__form--register" @submit.prevent="handleRegister">
            <div class="login-modal__form-grid">
              <div class="login-modal__field">
                <label>手机号</label>
                <ElInput v-model="registerForm.phone" size="large" class="login-modal-input" placeholder="请输入手机号">
                  <template #prefix>
                    <el-icon><Cellphone /></el-icon>
                  </template>
                </ElInput>
              </div>

              <div class="login-modal__field">
                <label>邮箱</label>
                <ElInput v-model="registerForm.email" size="large" class="login-modal-input" placeholder="可选" />
              </div>
            </div>

            <div class="login-modal__form-grid">
              <div class="login-modal__field">
                <label>昵称</label>
                <ElInput v-model="registerForm.nickname" size="large" class="login-modal-input" placeholder="输入平台昵称" />
              </div>

              <div class="login-modal__field">
                <label>密码</label>
                <ElInput v-model="registerForm.password" size="large" class="login-modal-input" type="password" show-password placeholder="设置登录密码">
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </ElInput>
              </div>
            </div>

            <div class="login-modal__chooser">
              <p>账号类型</p>
              <div class="login-modal__chooser-row">
                <button class="login-modal__choice" :class="{ 'is-active': registerForm.userType === 1 }" type="button" @click="registerForm.userType = 1">
                  用户端
                </button>
                <button class="login-modal__choice" :class="{ 'is-active': registerForm.userType === 2 }" type="button" @click="registerForm.userType = 2">
                  开发者
                </button>
                <button class="login-modal__choice" :class="{ 'is-active': registerForm.userType === 3 }" type="button" @click="registerForm.userType = 3">
                  双角色
                </button>
              </div>
            </div>

            <template v-if="registerForm.userType === 2 || registerForm.userType === 3">
              <div class="login-modal__form-grid">
                <div class="login-modal__field">
                  <label>真实姓名</label>
                  <ElInput v-model="registerForm.realName" size="large" class="login-modal-input" placeholder="身份证姓名" />
                </div>

                <div class="login-modal__field">
                  <label>能力类型</label>
                  <div class="login-modal__chooser-row">
                    <button class="login-modal__choice" :class="{ 'is-active': registerForm.developerRoleType === 1 }" type="button" @click="registerForm.developerRoleType = 1">
                      程序开发
                    </button>
                    <button class="login-modal__choice" :class="{ 'is-active': registerForm.developerRoleType === 2 }" type="button" @click="registerForm.developerRoleType = 2">
                      文档撰写
                    </button>
                    <button class="login-modal__choice" :class="{ 'is-active': registerForm.developerRoleType === 3 }" type="button" @click="registerForm.developerRoleType = 3">
                      都能做
                    </button>
                  </div>
                </div>
              </div>

              <div class="login-modal__form-grid">
                <div class="login-modal__field">
                  <label>身份证正面链接</label>
                  <ElInput v-model="registerForm.idCardFrontUrl" size="large" class="login-modal-input" placeholder="图片 URL" />
                </div>

                <div class="login-modal__field">
                  <label>身份证反面链接</label>
                  <ElInput v-model="registerForm.idCardBackUrl" size="large" class="login-modal-input" placeholder="图片 URL" />
                </div>
              </div>

              <div class="login-modal__field">
                <label>手持身份证自拍链接</label>
                <ElInput v-model="registerForm.selfieUrl" size="large" class="login-modal-input" placeholder="图片 URL" />
              </div>
            </template>

            <button class="login-modal__submit" type="submit" :disabled="submitting">
              {{ submitting ? '注册中...' : '注册并进入系统' }}
            </button>
          </form>
        </section>

        <aside class="login-modal__aside">
          <div class="login-modal__aside-card">
            <p class="login-modal__aside-kicker">安全登录</p>
            <h3 class="login-modal__aside-title">扫码继续</h3>
            <div class="login-modal__qr">
              <div class="login-modal__qr-grid">
                <span
                  v-for="index in 81"
                  :key="index"
                  class="login-modal__qr-cell"
                  :class="[
                    [1, 2, 3, 10, 12, 19, 20, 21, 29, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 59, 61, 63, 69, 70, 71, 79, 80, 81].includes(index)
                      ? 'is-dark'
                      : [5, 6, 7, 13, 14, 17, 23, 24, 25, 31, 33, 35, 57, 65, 67, 73, 74, 75].includes(index)
                        ? 'is-brand'
                        : ''
                  ]"
                ></span>
              </div>
            </div>
            <p class="login-modal__aside-copy">Demo 版暂未接入真实扫码能力，先用左侧账号体系登录。</p>
          </div>

          <button class="login-modal__admin" type="button" @click="openAdminLogin">进入后台管理员登录</button>
        </aside>
      </div>
    </div>
  </ElDialog>
</template>

<style scoped>
:deep(.login-modal-shell .el-dialog) {
  max-width: calc(100vw - 24px);
  border-radius: 36px;
  overflow: visible;
  background: transparent;
  box-shadow: none !important;
}

:deep(.login-modal-shell .el-dialog__header) {
  display: none;
}

:deep(.login-modal-shell .el-dialog__body) {
  padding: 0;
}

.login-modal {
  position: relative;
  overflow: visible;
  padding: 42px 28px 28px;
  border-radius: 38px;
  background:
    radial-gradient(circle at top right, rgba(255, 209, 0, 0.16), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.95));
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 36px 90px rgba(17, 17, 17, 0.22);
}

.login-modal__close {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 4;
  width: 40px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: rgba(17, 17, 17, 0.04);
  color: rgba(17, 17, 17, 0.58);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.login-modal__close:hover {
  background: rgba(17, 17, 17, 0.08);
  color: #111;
}

.login-modal__mascot {
  position: absolute;
  left: 88px;
  top: -24px;
  width: 96px;
  height: 86px;
  border-radius: 34px 34px 28px 28px;
  background: linear-gradient(180deg, #ffe567 0%, #ffd100 100%);
  box-shadow: 0 18px 40px rgba(255, 209, 0, 0.34);
}

.login-modal__mascot::before,
.login-modal__mascot::after {
  content: '';
  position: absolute;
  top: 8px;
  width: 24px;
  height: 18px;
  border-radius: 18px 18px 0 0;
  background: #ffd100;
}

.login-modal__mascot::before {
  left: 10px;
  transform: rotate(-18deg);
}

.login-modal__mascot::after {
  right: 10px;
  transform: rotate(18deg);
}

.login-modal__mascot-eye {
  position: absolute;
  top: 28px;
  width: 16px;
  height: 22px;
  border-radius: 999px;
  background: #111;
}

.login-modal__mascot-eye--left {
  left: 28px;
}

.login-modal__mascot-eye--right {
  right: 28px;
}

.login-modal__mascot-mouth {
  position: absolute;
  left: 50%;
  bottom: 20px;
  width: 28px;
  height: 12px;
  transform: translateX(-50%);
  border: 3px solid #111;
  border-top: 0;
  border-radius: 0 0 16px 16px;
}

.login-modal__grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 26px;
  align-items: stretch;
}

.login-modal__main {
  padding: 8px 6px 0 10px;
}

.login-modal__header {
  margin-bottom: 22px;
}

.login-modal__eyebrow {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: rgba(17, 17, 17, 0.42);
}

.login-modal__title {
  margin: 10px 0 0;
  font: 700 34px/1.04 var(--font-display);
  letter-spacing: -0.04em;
  color: #111;
}

.login-modal__desc {
  margin: 10px 0 0;
  max-width: 46ch;
  font-size: 14px;
  line-height: 1.8;
  color: rgba(17, 17, 17, 0.58);
}

.login-modal__tabs {
  display: inline-flex;
  gap: 8px;
  padding: 6px;
  border-radius: 999px;
  background: #f4f6f8;
}

.login-modal__tab {
  min-height: 42px;
  padding: 0 20px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: rgba(17, 17, 17, 0.42);
  font: 700 15px/1 var(--font-body);
}

.login-modal__tab.is-active {
  background: #111;
  color: #fff;
  box-shadow: 0 14px 28px rgba(17, 17, 17, 0.18);
}

.login-modal__form {
  margin-top: 24px;
  display: grid;
  gap: 18px;
}

.login-modal__form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.login-modal__field {
  display: grid;
  gap: 8px;
}

.login-modal__field label,
.login-modal__chooser p {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: rgba(17, 17, 17, 0.62);
}

.login-modal__chooser {
  display: grid;
  gap: 10px;
}

.login-modal__chooser-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.login-modal__choice {
  min-height: 44px;
  padding: 0 16px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: #f5f7fa;
  color: rgba(17, 17, 17, 0.6);
  font: 700 14px/1 var(--font-body);
}

.login-modal__choice.is-active {
  background: rgba(255, 209, 0, 0.18);
  color: #111;
  border-color: rgba(255, 209, 0, 0.42);
  box-shadow: 0 10px 22px rgba(255, 209, 0, 0.18);
}

.login-modal__submit {
  min-height: 54px;
  width: 100%;
  border: 0;
  border-radius: 999px;
  background: #ffd100;
  color: #111;
  font: 700 18px/1 var(--font-body);
  box-shadow: 0 18px 36px rgba(255, 209, 0, 0.26);
}

.login-modal__submit:hover {
  transform: translateY(-1px);
}

.login-modal__submit:disabled {
  opacity: 0.72;
  transform: none;
}

.login-modal__agreement {
  margin: 0;
  font-size: 12px;
  line-height: 1.8;
  color: rgba(17, 17, 17, 0.42);
}

.login-modal__aside {
  display: grid;
  align-content: start;
  gap: 12px;
}

.login-modal__aside-card {
  height: 100%;
  padding: 22px 20px;
  border-radius: 30px;
  background:
    radial-gradient(circle at top right, rgba(255, 209, 0, 0.22), transparent 26%),
    linear-gradient(180deg, #fffdf4 0%, #fff8d8 100%);
  border: 1px solid rgba(255, 209, 0, 0.24);
}

.login-modal__aside-kicker {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: rgba(17, 17, 17, 0.4);
}

.login-modal__aside-title {
  margin: 10px 0 0;
  font: 700 28px/1.08 var(--font-display);
}

.login-modal__qr {
  margin-top: 20px;
  display: grid;
  place-items: center;
  border-radius: 28px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: inset 0 0 0 1px rgba(17, 17, 17, 0.04);
}

.login-modal__qr-grid {
  display: grid;
  grid-template-columns: repeat(9, 1fr);
  gap: 5px;
  width: 180px;
  height: 180px;
}

.login-modal__qr-cell {
  border-radius: 5px;
  background: rgba(17, 17, 17, 0.08);
}

.login-modal__qr-cell.is-dark {
  background: #111;
}

.login-modal__qr-cell.is-brand {
  background: #ffd100;
}

.login-modal__aside-copy {
  margin: 14px 0 0;
  font-size: 13px;
  line-height: 1.8;
  color: rgba(17, 17, 17, 0.56);
}

.login-modal__admin {
  min-height: 48px;
  border: 0;
  border-radius: 999px;
  background: #111;
  color: #fff;
  font: 700 14px/1 var(--font-body);
}

:deep(.login-modal-input .el-input__wrapper) {
  min-height: 56px;
  border-radius: 18px !important;
  background: #f5f7fa !important;
  box-shadow: inset 0 0 0 1px rgba(17, 17, 17, 0.06) !important;
}

:deep(.login-modal-input .el-input__wrapper.is-focus) {
  box-shadow:
    inset 0 0 0 1px rgba(255, 209, 0, 0.8) !important,
    0 0 0 5px rgba(255, 209, 0, 0.18) !important;
}

@media (max-width: 1023px) {
  .login-modal {
    padding: 36px 18px 18px;
  }

  .login-modal__grid {
    grid-template-columns: 1fr;
  }

  .login-modal__mascot {
    left: 50%;
    transform: translateX(-50%);
  }
}

@media (max-width: 720px) {
  .login-modal__title {
    font-size: 28px;
  }

  .login-modal__form-grid {
    grid-template-columns: 1fr;
  }

  .login-modal__main {
    padding: 6px 0 0;
  }
}
</style>
