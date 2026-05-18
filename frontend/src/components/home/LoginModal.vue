<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElDialog, ElInput, ElMessage } from 'element-plus'
import { Cellphone, Close, Lock, Message, User } from '@element-plus/icons-vue'
import { createQrLogin, login, loginByEmailCode, register, sendEmailLoginCode } from '@/api/modules/auth'
import { useLoginWebSocket } from '@/composables/useLoginWebSocket'
import { useAuthStore } from '@/stores/auth'

type AuthPanel = 'password' | 'sms' | 'email' | 'register'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    forceLogin?: boolean
    allowRegister?: boolean
    message?: string
    successRedirect?: string
  }>(),
  {
    forceLogin: false,
    allowRegister: true,
    message: '',
    successRedirect: ''
  }
)

const emit = defineEmits<{ 'update:modelValue': [boolean]; success: [] }>()

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const DEFAULT_QR_NOTICE = '请先使用微信扫描固定公众号二维码，关注后在公众号登录页输入右侧 6 位登录码。'

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const activePanel = ref<AuthPanel>('password')
const submitting = ref(false)
const qrLoading = ref(false)
const smsCountdown = ref(0)
const emailCountdown = ref(0)
const emailSending = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const smsForm = reactive({
  phone: '',
  code: ''
})

const emailForm = reactive({
  email: '',
  code: ''
})

const registerForm = reactive({
  phone: '',
  email: '',
  nickname: '',
  password: ''
})

const loginCode = ref('')
const officialQrImageUrl = ref('')
const loginEntryUrl = ref('')
const qrNotice = ref(DEFAULT_QR_NOTICE)
const remainingSeconds = ref(300)
const expireAt = ref(0)

let refreshTimer: number | null = null
let countdownTimer: number | null = null
let smsTimer: number | null = null
let emailTimer: number | null = null

const loginCodeDisplay = computed(() => loginCode.value || '------')
const loginEntryLink = computed(() => {
  if (!loginEntryUrl.value) {
    return 'http://localhost:8080/wx/login-entry'
  }
  return loginCode.value ? `${loginEntryUrl.value}?code=${loginCode.value}` : loginEntryUrl.value
})
const showDismissAction = computed(() => !props.forceLogin && Boolean(props.message?.trim()))
const isSmsPanel = computed(() => activePanel.value === 'sms')
const isEmailPanel = computed(() => activePanel.value === 'email')
const isRegisterPanel = computed(() => activePanel.value === 'register')
const panelTitle = computed(() => {
  if (activePanel.value === 'register') return '注册新账号'
  if (activePanel.value === 'sms') return '短信验证码登录'
  if (activePanel.value === 'email') return '邮箱登录'
  return '密码登录'
})
const socketText = computed(() => {
  if (socketState.value === 'open') return '已连接'
  if (socketState.value === 'connecting') return '连接中'
  if (socketState.value === 'reconnecting') return '重连中'
  if (socketState.value === 'error') return '连接异常'
  return '未连接'
})

const { connect, disconnect, socketState } = useLoginWebSocket({
  onMessage: handleSocketMessage,
  onStateChange: handleSocketStateChange
})

watch(
  () => props.modelValue,
  (isVisible) => {
    if (isVisible) {
      activePanel.value = 'password'
      qrNotice.value = DEFAULT_QR_NOTICE
      refreshQrLogin()
      return
    }
    stopQrSession()
  },
  { immediate: true }
)

watch(
  () => props.allowRegister,
  (allowRegister) => {
    if (!allowRegister && activePanel.value === 'register') {
      activePanel.value = 'password'
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  stopQrSession()
  clearSmsTimer()
  clearEmailTimer()
})

function clearTimers() {
  if (refreshTimer) {
    window.clearTimeout(refreshTimer)
    refreshTimer = null
  }
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function clearSmsTimer() {
  if (smsTimer) {
    window.clearInterval(smsTimer)
    smsTimer = null
  }
  smsCountdown.value = 0
}

function clearEmailTimer() {
  if (emailTimer) {
    window.clearInterval(emailTimer)
    emailTimer = null
  }
  emailCountdown.value = 0
}

function stopQrSession(nextNotice = DEFAULT_QR_NOTICE) {
  clearTimers()
  disconnect()
  loginCode.value = ''
  officialQrImageUrl.value = ''
  loginEntryUrl.value = ''
  remainingSeconds.value = 300
  expireAt.value = 0
  qrNotice.value = nextNotice
}

function closeDialog() {
  if (props.forceLogin) {
    return
  }
  visible.value = false
}

function clearForms() {
  loginForm.account = ''
  loginForm.password = ''
  smsForm.phone = ''
  smsForm.code = ''
  emailForm.email = ''
  emailForm.code = ''
  registerForm.phone = ''
  registerForm.email = ''
  registerForm.nickname = ''
  registerForm.password = ''
}

function openAdminLogin() {
  visible.value = false
  stopQrSession()
  router.push('/admin/login')
}

function resolveRedirectPath(payload: any) {
  if (payload?.redirectPath) {
    return payload.redirectPath
  }
  return '/me'
}

function finishAfterLogin(target: string) {
  if (!target) {
    return
  }
  if (target === route.fullPath) {
    window.location.reload()
    return
  }
  router.push(target)
}

function startExpiryTimer(targetExpireAt: number) {
  clearTimers()
  expireAt.value = targetExpireAt

  const tick = () => {
    remainingSeconds.value = Math.max(0, Math.ceil((expireAt.value - Date.now()) / 1000))
  }

  tick()
  countdownTimer = window.setInterval(tick, 1000)

  const delay = Math.max(0, expireAt.value - Date.now() - 1000)
  refreshTimer = window.setTimeout(() => {
    qrNotice.value = '登录码已过期，正在自动刷新…'
    refreshQrLogin()
  }, delay)
}

async function refreshQrLogin() {
  if (!props.modelValue) {
    return
  }

  qrLoading.value = true
  qrNotice.value = '正在生成当前电脑的临时登录码…'
  clearTimers()
  disconnect()

  try {
    const response = await createQrLogin()
    const payload = response?.data ?? response
    loginCode.value = payload.loginCode || ''
    officialQrImageUrl.value = payload.officialAccountQrImageUrl || ''
    loginEntryUrl.value = payload.loginEntryUrl || ''

    if (payload.expireAt) {
      startExpiryTimer(new Date(payload.expireAt).getTime())
    }

    if (payload.token) {
      connect(payload.token)
    }

    qrNotice.value = '请扫码关注公众号，然后在手机端输入当前 6 位登录码完成本机授权。'
  } catch (error) {
    qrNotice.value = '登录码生成失败，请稍后重试。'
    ElMessage.error(error instanceof Error ? error.message : '登录码生成失败')
  } finally {
    qrLoading.value = false
  }
}

function handleSocketStateChange(state: string) {
  if (state === 'open') {
    qrNotice.value = '连接成功，等待公众号端输入登录码并完成授权。'
  } else if (state === 'reconnecting') {
    qrNotice.value = '连接断开，正在自动重连…'
  } else if (state === 'error') {
    qrNotice.value = '连接异常，请刷新登录码后重试。'
  }
}

function handleSocketMessage(event: MessageEvent) {
  let payload: any = {}

  try {
    payload = JSON.parse(event.data || '{}')
  } catch (error) {
    return
  }

  if (payload.type === 'PONG') {
    return
  }

  if (payload.type === 'LOGIN_SUCCESS') {
    authStore.setLogin({
      token: payload.jwt,
      userId: payload.userId,
      nickname: payload.nickname,
      avatarUrl: payload.avatarUrl,
      userType: payload.userType,
      roles: payload.roles,
      redirectPath: payload.redirectPath
    })
    visible.value = false
    stopQrSession()
    clearForms()
    clearSmsTimer()
    clearEmailTimer()
    emit('success')
    ElMessage.success('扫码登录成功')
    finishAfterLogin(props.successRedirect?.trim() || resolveRedirectPath(payload))
  }
}

async function performLogin(payload: { account: string; password: string }, successMessage = '登录成功') {
  submitting.value = true
  try {
    const response = await login(payload)
    finishLogin(response?.data ?? response, successMessage)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    submitting.value = false
  }
}

function finishLogin(payload: any, successMessage: string) {
  authStore.setLogin(payload)
  visible.value = false
  clearForms()
  clearSmsTimer()
  clearEmailTimer()
  emit('success')
  ElMessage.success(successMessage)
  finishAfterLogin(props.successRedirect?.trim() || resolveRedirectPath(payload))
}

async function handlePasswordLogin() {
  await performLogin(
    {
      account: loginForm.account.trim(),
      password: loginForm.password
    },
    '登录成功'
  )
}

async function handleEmailLogin() {
  const email = emailForm.email.trim()
  if (!email) {
    ElMessage.warning('请输入邮箱')
    return
  }

  if (!EMAIL_PATTERN.test(email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  const code = emailForm.code.trim()
  if (!code) {
    ElMessage.warning('请输入验证码')
    return
  }

  submitting.value = true
  try {
    const response = await loginByEmailCode({
      email,
      code
    })
    finishLogin(response?.data ?? response, '邮箱登录成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '邮箱登录失败')
  } finally {
    submitting.value = false
  }
}

async function handleRegister() {
  submitting.value = true
  try {
    const response = await register(registerForm)
    finishLogin(response?.data ?? response, '注册成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '注册失败')
  } finally {
    submitting.value = false
  }
}

function handleSmsLogin() {
  ElMessage.warning('短信登录接口暂未接入，当前请先使用密码登录、邮箱登录或扫码登录。')
}

function handleSendSms() {
  if (!smsForm.phone.trim()) {
    ElMessage.warning('请先输入手机号')
    return
  }

  if (smsCountdown.value > 0) {
    return
  }

  ElMessage.info('演示环境暂未对接短信服务，这里仅展示登录样式。')
  clearSmsTimer()
  smsCountdown.value = 60
  smsTimer = window.setInterval(() => {
    smsCountdown.value -= 1
    if (smsCountdown.value <= 0) {
      clearSmsTimer()
    }
  }, 1000)
}

async function handleSendEmailCode() {
  const email = emailForm.email.trim()
  if (!email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  if (!EMAIL_PATTERN.test(email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  if (emailCountdown.value > 0 || emailSending.value) {
    return
  }

  emailSending.value = true
  try {
    await sendEmailLoginCode({ email })
    ElMessage.success('验证码已发送，请查收邮箱')
    clearEmailTimer()
    emailCountdown.value = 60
    emailTimer = window.setInterval(() => {
      emailCountdown.value -= 1
      if (emailCountdown.value <= 0) {
        clearEmailTimer()
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '验证码发送失败')
  } finally {
    emailSending.value = false
  }
}

function handleForgotPassword() {
  ElMessage.info('找回密码流程暂未接入，请联系管理员或先使用扫码登录。')
}
</script>

<template>
  <ElDialog
    v-model="visible"
    width="860px"
    :show-close="false"
    :close-on-click-modal="!forceLogin"
    :close-on-press-escape="!forceLogin"
    align-center
    class="login-modal-shell"
  >
    <div class="login-modal">
      <button
        v-if="!forceLogin"
        class="login-modal__close"
        type="button"
        aria-label="关闭登录弹窗"
        @click="closeDialog"
      >
        <el-icon :size="20"><Close /></el-icon>
      </button>

      <div class="login-modal__body">
        <section class="login-modal__scan">
          <div class="login-modal__scan-header">
            <h3>扫描二维码登录</h3>
            <p>请使用微信扫描固定公众号二维码</p>
          </div>

          <div class="login-modal__qr-frame">
            <img
              v-if="officialQrImageUrl"
              class="login-modal__qr-image"
              :src="officialQrImageUrl"
              alt="微信公众号二维码"
            />
            <div v-else class="login-modal__qr-placeholder">公众号二维码</div>
          </div>

          <div class="login-modal__scan-copy">
            <p>扫码关注公众号后，在手机端输入当前电脑登录码。</p>
            <strong class="login-modal__code">{{ loginCodeDisplay }}</strong>
          </div>

          <div class="login-modal__scan-footer">
            <span>请使用微信公众号登录页输入登录码</span>
            <el-link type="primary" :href="loginEntryLink" target="_blank">打开手机登录入口</el-link>
          </div>

          <div class="login-modal__scan-status">
            <span>剩余 {{ remainingSeconds }}s</span>
            <span>{{ socketText }}</span>
          </div>

          <p class="login-modal__notice">{{ qrNotice }}</p>

          <button class="login-modal__text-action" type="button" :disabled="qrLoading" @click="refreshQrLogin">
            {{ qrLoading ? '刷新中…' : '刷新登录码' }}
          </button>
        </section>

        <div class="login-modal__divider" />

        <section class="login-modal__auth">
          <div class="login-modal__auth-top">
            <div class="login-modal__tabs" v-if="!isRegisterPanel">
              <button
                class="login-modal__tab"
                :class="{ 'is-active': activePanel === 'password' }"
                type="button"
                @click="activePanel = 'password'"
              >
                密码登录
              </button>
              <button
                class="login-modal__tab"
                :class="{ 'is-active': activePanel === 'sms' }"
                type="button"
                @click="activePanel = 'sms'"
              >
                短信登录
              </button>
              <button
                class="login-modal__tab"
                :class="{ 'is-active': activePanel === 'email' }"
                type="button"
                @click="activePanel = 'email'"
              >
                邮箱登录
              </button>
            </div>
            <button
              v-if="isRegisterPanel"
              class="login-modal__back-link"
              type="button"
              @click="activePanel = 'password'"
            >
              返回登录
            </button>
          </div>

          <div class="login-modal__auth-title">
            <h3>{{ panelTitle }}</h3>
            <p v-if="message">{{ message }}</p>
            <p v-else-if="isRegisterPanel">注册后先进入个人中心，后续可申请成为开发者并等待管理员审核。</p>
            <p v-else-if="isSmsPanel">短信界面已做好，短信服务接口暂未接入。</p>
            <p v-else-if="isEmailPanel">请输入注册邮箱，查收验证码后完成邮箱登录。</p>
            <p v-else>请输入账号密码继续访问当前页面。</p>
          </div>

          <button
            v-if="showDismissAction"
            class="login-modal__soft-link"
            type="button"
            @click="closeDialog"
          >
            暂不登录
          </button>

          <form v-if="activePanel === 'password'" class="login-modal__form" @submit.prevent="handlePasswordLogin">
            <div class="login-modal__field-row">
              <label>账号</label>
              <ElInput v-model="loginForm.account" class="login-modal-input" placeholder="请输入账号">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__field-row">
              <label>密码</label>
              <ElInput v-model="loginForm.password" class="login-modal-input" type="password" show-password placeholder="请输入密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__helper-row">
              <span>支持手机号、邮箱或用户编号登录</span>
              <button class="login-modal__link-btn" type="button" @click="handleForgotPassword">
                忘记密码？
              </button>
            </div>

            <div class="login-modal__action-row">
              <button
                v-if="allowRegister"
                class="login-modal__ghost-btn"
                type="button"
                @click="activePanel = 'register'"
              >
                注册
              </button>
              <button class="login-modal__primary-btn" type="submit" :disabled="submitting">
                {{ submitting ? '登录中…' : '登录' }}
              </button>
            </div>
          </form>

          <form v-else-if="activePanel === 'sms'" class="login-modal__form" @submit.prevent="handleSmsLogin">
            <div class="login-modal__field-row">
              <label>手机</label>
              <ElInput v-model="smsForm.phone" class="login-modal-input" placeholder="请输入手机号">
                <template #prefix>
                  <el-icon><Cellphone /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__field-row">
              <label>验证码</label>
              <ElInput v-model="smsForm.code" class="login-modal-input" placeholder="请输入验证码">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
                <template #append>
                  <button class="login-modal__sms-btn" type="button" @click="handleSendSms">
                    {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                  </button>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__helper-row">
              <span>当前只完成了页面样式与交互预留</span>
              <span>后端短信接口待接入</span>
            </div>

            <div class="login-modal__action-row">
              <button
                v-if="allowRegister"
                class="login-modal__ghost-btn"
                type="button"
                @click="activePanel = 'register'"
              >
                注册
              </button>
              <button class="login-modal__primary-btn" type="submit">
                登录
              </button>
            </div>
          </form>

          <form v-else-if="activePanel === 'email'" class="login-modal__form" @submit.prevent="handleEmailLogin">
            <div class="login-modal__field-row">
              <label>邮箱</label>
              <ElInput v-model="emailForm.email" class="login-modal-input" placeholder="请输入邮箱">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__field-row">
              <label>验证码</label>
              <ElInput v-model="emailForm.code" class="login-modal-input" placeholder="请输入验证码">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
                <template #append>
                  <button
                    class="login-modal__sms-btn"
                    type="button"
                    :disabled="emailSending || emailCountdown > 0"
                    @click="handleSendEmailCode"
                  >
                    {{ emailSending ? '发送中…' : emailCountdown > 0 ? `${emailCountdown}s` : '获取验证码' }}
                  </button>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__helper-row">
              <span>验证码会发送到已注册邮箱，5 分钟内有效</span>
              <span>60 秒内不可重复发送</span>
            </div>

            <div class="login-modal__action-row">
              <button
                v-if="allowRegister"
                class="login-modal__ghost-btn"
                type="button"
                @click="activePanel = 'register'"
              >
                注册
              </button>
              <button class="login-modal__primary-btn" type="submit" :disabled="submitting">
                {{ submitting ? '登录中…' : '登录' }}
              </button>
            </div>
          </form>

          <form v-else class="login-modal__form login-modal__form--register" @submit.prevent="handleRegister">
            <div class="login-modal__field-row">
              <label>昵称</label>
              <ElInput v-model="registerForm.nickname" class="login-modal-input" placeholder="请输入昵称">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__field-row">
              <label>手机</label>
              <ElInput v-model="registerForm.phone" class="login-modal-input" placeholder="请输入手机号">
                <template #prefix>
                  <el-icon><Cellphone /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__field-row">
              <label>邮箱</label>
              <ElInput v-model="registerForm.email" class="login-modal-input" placeholder="可选，用于接收通知">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__field-row">
              <label>密码</label>
              <ElInput v-model="registerForm.password" class="login-modal-input" type="password" show-password placeholder="请设置登录密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </ElInput>
            </div>

            <div class="login-modal__action-row">
              <button class="login-modal__ghost-btn" type="button" @click="activePanel = 'password'">
                返回
              </button>
              <button class="login-modal__primary-btn" type="submit" :disabled="submitting">
                {{ submitting ? '注册中…' : '注册' }}
              </button>
            </div>
          </form>

          <div class="login-modal__extra">
            <span>其他方式登录</span>
            <div class="login-modal__extra-list">
              <button class="login-modal__extra-item is-active" type="button" @click="refreshQrLogin">
                公众号扫码
              </button>
              <button class="login-modal__extra-item" type="button" @click="activePanel = 'sms'">
                短信验证码
              </button>
              <button class="login-modal__extra-item" type="button" @click="openAdminLogin">
                后台登录
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  </ElDialog>
</template>

<style scoped>
:deep(.login-modal-shell .el-dialog) {
  max-width: calc(100vw - 24px);
  border-radius: 18px;
  overflow: hidden;
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
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 28px 64px rgba(15, 23, 42, 0.18);
}

.login-modal__close {
  position: absolute;
  top: 18px;
  right: 18px;
  z-index: 4;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #374151;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.login-modal__close:hover {
  background: #f3f4f6;
}

.login-modal__body {
  display: grid;
  grid-template-columns: 270px 1px minmax(0, 1fr);
  align-items: stretch;
}

.login-modal__divider {
  background: linear-gradient(180deg, transparent 0%, #e5e7eb 8%, #e5e7eb 92%, transparent 100%);
}

.login-modal__scan {
  padding: 44px 28px 26px;
  text-align: center;
  background: linear-gradient(180deg, #ffffff 0%, #fafcff 100%);
}

.login-modal__scan-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.login-modal__scan-header p {
  margin: 10px 0 0;
  font-size: 13px;
  color: #909399;
}

.login-modal__qr-frame {
  width: 176px;
  height: 176px;
  margin: 26px auto 18px;
  padding: 10px;
  border: 1px solid #dfe4ea;
  border-radius: 10px;
  background: #fff;
}

.login-modal__qr-image,
.login-modal__qr-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 4px;
}

.login-modal__qr-image {
  object-fit: cover;
}

.login-modal__qr-placeholder {
  display: grid;
  place-items: center;
  background:
    linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  color: #3b82f6;
  font-size: 14px;
  font-weight: 600;
}

.login-modal__scan-copy p {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #606266;
}

.login-modal__code {
  display: block;
  margin-top: 12px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 0.26em;
  color: #111827;
}

.login-modal__scan-footer {
  margin-top: 14px;
  display: grid;
  gap: 6px;
  justify-items: center;
  font-size: 12px;
  color: #909399;
}

.login-modal__scan-status {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
  font-size: 12px;
}

.login-modal__scan-status span + span::before {
  content: '';
  display: inline-block;
  width: 1px;
  height: 10px;
  margin-right: 12px;
  vertical-align: -1px;
  background: #d1d5db;
}

.login-modal__notice {
  margin: 16px 0 0;
  font-size: 12px;
  line-height: 1.75;
  color: #606266;
}

.login-modal__text-action {
  margin-top: 16px;
  border: 0;
  background: transparent;
  color: #409eff;
  font-size: 13px;
  cursor: pointer;
}

.login-modal__text-action:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.login-modal__auth {
  padding: 34px 38px 30px;
}

.login-modal__auth-top {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  min-height: 32px;
}

.login-modal__tabs {
  display: inline-flex;
  align-items: center;
  gap: 18px;
}

.login-modal__tab {
  border: 0;
  background: transparent;
  padding: 0;
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  position: relative;
}

.login-modal__tab + .login-modal__tab::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 50%;
  width: 1px;
  height: 14px;
  transform: translateY(-50%);
  background: #dcdfe6;
}

.login-modal__tab.is-active {
  color: #409eff;
  font-weight: 600;
}

.login-modal__back-link {
  border: 0;
  background: transparent;
  color: #409eff;
  font-size: 14px;
  cursor: pointer;
}

.login-modal__auth-title {
  margin-top: 18px;
}

.login-modal__auth-title h3 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}

.login-modal__auth-title p {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: #909399;
}

.login-modal__soft-link {
  margin-top: 10px;
  border: 0;
  background: transparent;
  padding: 0;
  color: #409eff;
  font-size: 13px;
  cursor: pointer;
}

.login-modal__form {
  margin-top: 24px;
  display: grid;
  gap: 14px;
}

.login-modal__field-row {
  display: grid;
  grid-template-columns: 62px minmax(0, 1fr);
  align-items: center;
  min-height: 56px;
  border: 1px solid #dfe4ea;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.login-modal__field-row label {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  border-right: 1px solid #ebeef5;
  font-size: 14px;
  color: #303133;
  background: #fff;
}

.login-modal__helper-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.login-modal__link-btn {
  border: 0;
  background: transparent;
  color: #409eff;
  font-size: 13px;
  cursor: pointer;
}

.login-modal__action-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 8px;
}

.login-modal__ghost-btn,
.login-modal__primary-btn {
  min-height: 40px;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
}

.login-modal__ghost-btn {
  border: 1px solid #dfe4ea;
  background: #fff;
  color: #303133;
}

.login-modal__primary-btn {
  border: 0;
  background: linear-gradient(90deg, #73c8f5 0%, #66c4ef 100%);
  color: #fff;
}

.login-modal__primary-btn:disabled,
.login-modal__ghost-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.login-modal__role-row {
  display: grid;
  gap: 10px;
}

.login-modal__role-row span {
  font-size: 13px;
  color: #909399;
}

.login-modal__role-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.login-modal__role-btn {
  min-height: 36px;
  padding: 0 16px;
  border: 1px solid #dfe4ea;
  border-radius: 999px;
  background: #fff;
  color: #606266;
  cursor: pointer;
}

.login-modal__role-btn.is-active {
  border-color: #7ccdf3;
  background: #f0fbff;
  color: #2b90c9;
}

.login-modal__extra {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid #f0f2f5;
}

.login-modal__extra > span {
  display: block;
  text-align: center;
  font-size: 13px;
  color: #909399;
}

.login-modal__extra-list {
  margin-top: 14px;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
}

.login-modal__extra-item {
  min-height: 34px;
  padding: 0 14px;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  background: #fff;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
}

.login-modal__extra-item.is-active {
  border-color: #b8e6fb;
  background: #f0fbff;
  color: #2b90c9;
}

:deep(.login-modal-input .el-input__wrapper) {
  box-shadow: none !important;
  border-radius: 0 !important;
  min-height: 54px;
  background: transparent !important;
}

:deep(.login-modal-input .el-input-group__append) {
  border: 0;
  background: transparent;
  padding-right: 12px;
}

.login-modal__sms-btn {
  border: 0;
  background: transparent;
  color: #409eff;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}

.login-modal__sms-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

@media (max-width: 900px) {
  .login-modal__body {
    grid-template-columns: 1fr;
  }

  .login-modal__divider {
    display: none;
  }

  .login-modal__scan,
  .login-modal__auth {
    padding-left: 24px;
    padding-right: 24px;
  }

  .login-modal__scan {
    border-bottom: 1px solid #eef2f6;
  }
}

@media (max-width: 640px) {
  .login-modal__auth {
    padding-top: 26px;
  }

  .login-modal__field-row {
    grid-template-columns: 56px minmax(0, 1fr);
  }

  .login-modal__action-row {
    grid-template-columns: 1fr;
  }

  .login-modal__helper-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
