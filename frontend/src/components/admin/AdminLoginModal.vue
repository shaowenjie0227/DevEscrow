<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElDialog, ElInput, ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { adminLogin } from '@/api/modules/admin'
import { useAdminAuthStore } from '@/stores/adminAuth'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    default: ''
  },
  successRedirect: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const route = useRoute()
const router = useRouter()
const adminAuthStore = useAdminAuthStore()
const submitting = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123456'
})

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

function closeDialog() {
  visible.value = false
}

function leaveAdmin() {
  closeDialog()
  router.push('/admin-entry')
}

function finishAfterLogin(target) {
  if (!target) {
    return
  }
  if (target === route.fullPath) {
    window.location.reload()
    return
  }
  router.push(target)
}

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  submitting.value = true
  try {
    const response = await adminLogin(form)
    adminAuthStore.setLogin(response)
    closeDialog()
    emit('success')
    ElMessage.success('管理员登录成功')
    finishAfterLogin(props.successRedirect?.trim() || '/admin/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '管理员登录失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <ElDialog
    v-model="visible"
    width="520px"
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    align-center
    class="admin-login-modal-shell"
  >
    <section class="admin-login-modal">
      <p class="admin-login-modal__eyebrow">ADMIN SESSION</p>
      <h2 class="admin-login-modal__title">后台登录已失效</h2>
      <p class="admin-login-modal__desc">
        {{ message || '为了继续处理后台内容，请重新登录管理员账号。' }}
      </p>

      <div class="admin-login-modal__panel">
        <label class="admin-login-modal__field">
          <span>用户名</span>
          <ElInput v-model="form.username" size="large" placeholder="admin">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </ElInput>
        </label>

        <label class="admin-login-modal__field">
          <span>密码</span>
          <ElInput v-model="form.password" size="large" type="password" show-password placeholder="admin123456">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </ElInput>
        </label>

        <div class="admin-login-modal__actions">
          <button class="admin-login-modal__ghost" type="button" @click="leaveAdmin">退出后台</button>
          <button class="admin-login-modal__primary" type="button" :disabled="submitting" @click="handleLogin">
            {{ submitting ? '登录中...' : '重新登录' }}
          </button>
        </div>
      </div>
    </section>
  </ElDialog>
</template>

<style scoped>
:deep(.admin-login-modal-shell .el-dialog) {
  max-width: calc(100vw - 24px);
  border-radius: 32px;
  background: transparent;
  box-shadow: none !important;
}

:deep(.admin-login-modal-shell .el-dialog__header) {
  display: none;
}

:deep(.admin-login-modal-shell .el-dialog__body) {
  padding: 0;
}

:deep(.admin-login-modal-shell .el-input__wrapper) {
  min-height: 52px;
  border-radius: 16px !important;
  background: rgba(244, 246, 248, 0.96) !important;
  box-shadow: inset 0 0 0 1px rgba(17, 17, 17, 0.08) !important;
}

:deep(.admin-login-modal-shell .el-input__wrapper.is-focus) {
  box-shadow:
    inset 0 0 0 1px rgba(49, 95, 255, 0.8) !important,
    0 0 0 5px rgba(49, 95, 255, 0.12) !important;
}

.admin-login-modal {
  padding: 30px;
  border-radius: 32px;
  background:
    radial-gradient(circle at top right, rgba(49, 95, 255, 0.18), transparent 32%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.99), rgba(245, 248, 255, 0.97));
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 32px 80px rgba(15, 23, 42, 0.2);
}

.admin-login-modal__eyebrow {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  color: rgba(17, 17, 17, 0.42);
}

.admin-login-modal__title {
  margin: 10px 0 0;
  font: 700 32px/1.05 var(--font-display);
  color: #111827;
}

.admin-login-modal__desc {
  margin: 12px 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: rgba(17, 24, 39, 0.6);
}

.admin-login-modal__panel {
  margin-top: 24px;
  display: grid;
  gap: 16px;
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.admin-login-modal__field {
  display: grid;
  gap: 8px;
}

.admin-login-modal__field span {
  font-size: 13px;
  font-weight: 700;
  color: rgba(17, 24, 39, 0.7);
}

.admin-login-modal__actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 4px;
}

.admin-login-modal__ghost,
.admin-login-modal__primary {
  min-height: 48px;
  padding: 0 22px;
  border-radius: 999px;
  font: 700 14px/1 var(--font-body);
}

.admin-login-modal__ghost {
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: rgba(255, 255, 255, 0.85);
  color: rgba(17, 24, 39, 0.68);
}

.admin-login-modal__primary {
  border: 0;
  background: linear-gradient(135deg, #315fff 0%, #1741d0 100%);
  color: #fff;
  box-shadow: 0 16px 32px rgba(49, 95, 255, 0.24);
}

.admin-login-modal__primary:disabled {
  opacity: 0.72;
  box-shadow: none;
}

@media (max-width: 640px) {
  .admin-login-modal {
    padding: 24px 18px;
  }

  .admin-login-modal__actions {
    flex-direction: column-reverse;
  }

  .admin-login-modal__ghost,
  .admin-login-modal__primary {
    width: 100%;
  }
}
</style>
