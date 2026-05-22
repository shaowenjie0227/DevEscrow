<script setup lang="ts">
import { computed, defineAsyncComponent, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, ChatDotRound, MessageBox, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AiDemandAssistantDialog from './AiDemandAssistantDialog.vue'
import LoginModal from './LoginModal.vue'
import { logout as logoutRequest } from '@/api/modules/auth'
import { useChatUnreadCount } from '@/composables/useChatUnreadCount'
import { useInboxUnreadCount } from '@/composables/useInboxUnreadCount'
import { useAuthStore } from '@/stores/auth'
import {
  createAiAssistantClosedQuery,
  createAiAssistantOpenQuery,
  isAiAssistantQueryActive
} from '@/utils/aiDemandAssistant'
import { resolveWorkspaceChatPath, resolveWorkspaceInboxPath } from '@/utils/workspace'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const PersonalCenterView = defineAsyncComponent(() => import('@/views/public/PersonalCenterView.vue'))

const loginVisible = ref(false)
const notificationVisible = ref(false)
const notificationSnapshotReady = ref(false)
const personalCenterVisible = ref(false)

const navItems = [
  { label: '首页', to: '/' },
  { label: '接单大厅', to: '/market' },
  { label: '资源分享', to: '/resources' },
  { label: '技术文章', to: '/articles' },
  { label: '交流社区', to: '/community' },
  { label: '技术路线图', to: '/roadmap' },
  { label: '推荐课程', to: '/courses' },
  { label: '知识库', to: '/knowledge-base' }
]

const displayName = computed(() => authStore.userInfo?.nickname || authStore.userInfo?.phone || '登录')
const avatarLetter = computed(() => displayName.value.slice(0, 1).toUpperCase())

const { unreadCount: chatUnreadCount } = useChatUnreadCount(authStore)
const { unreadCount: inboxUnreadCount } = useInboxUnreadCount(authStore)

const totalUnread = computed(() => Number(chatUnreadCount.value || 0) + Number(inboxUnreadCount.value || 0))
const workspaceChatPath = computed(() => resolveWorkspaceChatPath(authStore.userInfo))
const workspaceInboxPath = computed(() => resolveWorkspaceInboxPath(authStore.userInfo))
const assistantVisible = computed({
  get: () => isAiAssistantQueryActive(route.query),
  set: (value: boolean) => {
    router.replace({
      path: route.path,
      query: value ? createAiAssistantOpenQuery(route.query) : createAiAssistantClosedQuery(route.query)
    })
  }
})

const chatNoticeText = computed(() => {
  if (chatUnreadCount.value > 0) {
    return `${chatUnreadCount.value} 条未读聊天，适合直接回工作台继续沟通。`
  }
  return '项目沟通、报价确认和交付往来会集中留在聊天会话里。'
})

const inboxNoticeText = computed(() => {
  if (inboxUnreadCount.value > 0) {
    return `${inboxUnreadCount.value} 封未读站内信，建议优先查看管理员提醒。`
  }
  return '管理员通知、审核提醒和补充说明会统一进入站内信。'
})

watch(
  () => authStore.token,
  (token) => {
    if (!token) {
      notificationVisible.value = false
      notificationSnapshotReady.value = false
      personalCenterVisible.value = false
    }
  }
)

watch([chatUnreadCount, inboxUnreadCount], ([chatCount, inboxCount], [prevChatCount, prevInboxCount]) => {
  if (!authStore.token) {
    return
  }

  if (!notificationSnapshotReady.value) {
    notificationSnapshotReady.value = true
    return
  }

  if (Number(chatCount || 0) > Number(prevChatCount || 0)) {
    ElMessage.info('你收到了新的聊天消息')
  }

  if (Number(inboxCount || 0) > Number(prevInboxCount || 0)) {
    ElMessage.info('你收到了新的站内信')
  }
})

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path === path || route.path.startsWith(`${path}/`)
}

function goTo(path: string) {
  router.push(path)
}

function openAssistant() {
  assistantVisible.value = true
}

function openLogin() {
  loginVisible.value = true
}


async function handleLogout() {
  let remoteLogoutFailed = false

  try {
    if (authStore.token) {
      await logoutRequest()
    }
  } catch {
    remoteLogoutFailed = true
  } finally {
    authStore.logout()
    await router.push('/market')
  }

  if (remoteLogoutFailed) {
    ElMessage.warning('已退出当前登录，但服务端会话注销失败')
    return
  }

  ElMessage.success('已退出登录')
}

async function handlePersonalCommand(command: string) {

  if (command === 'me') {
    if (!authStore.token) {
      openLogin()
      return
    }
    personalCenterVisible.value = true
    return
  }

  if (command === 'client') {
    if (!authStore.token) {
      openLogin()
      return
    }
    router.push('/client/home')
    return
  }

  if (command === 'developer') {
    if (!authStore.token) {
      openLogin()
      return
    }
    router.push('/developer/home')
    return
  }

  if (command === 'logout') {
    await handleLogout()
  }
}

function formatUnreadCount(count: number) {
  if (count > 99) {
    return '99+'
  }
  return String(count)
}

function openNoticeTarget(path: string) {
  notificationVisible.value = false
  router.push(path)
}
</script>

<template>
  <header class="market-nav">
    <div class="market-container">
      <div class="market-nav__inner">
        <div class="market-nav__brand-area">
          <button class="market-brand" type="button" @click="goTo('/')">
            <span class="market-brand__icon">DE</span>
            <span class="market-brand__text">
              <span class="market-brand__title">DevEscrow</span>
              <span class="market-brand__sub">程序员需求担保协作市场</span>
            </span>
          </button>
        </div>

        <nav class="market-nav__links" aria-label="主导航">
          <div class="market-nav__links-track">
            <button
              v-for="item in navItems"
              :key="item.to"
              class="market-nav__link"
              :class="{ 'market-nav__link--active': isActive(item.to) }"
              type="button"
              @click="goTo(item.to)"
            >
              {{ item.label }}
            </button>
          </div>
        </nav>

        <div class="market-nav__actions">
          <button class="market-nav__action market-nav__action--primary" type="button" @click="openAssistant">
            <el-icon><Plus /></el-icon>
            AI对话助手
          </button>
        </div>

        <div class="market-nav__user">
          <el-popover
            v-if="authStore.token"
            v-model:visible="notificationVisible"
            placement="bottom-end"
            :width="360"
            trigger="click"
            popper-class="market-notice-popover"
          >
            <template #reference>
              <button class="market-nav__user-btn market-nav__user-btn--notice" type="button">
                <span class="market-nav__notice-icon">
                  <el-icon><Bell /></el-icon>
                </span>
                <span>通知</span>
                <em v-if="totalUnread > 0" class="market-nav__notice-dot">{{ formatUnreadCount(totalUnread) }}</em>
              </button>
            </template>

            <div class="market-notice-panel">
              <div class="market-notice-panel__head">
                <span class="market-notice-panel__eyebrow">Notification Hub</span>
                <strong>{{ totalUnread > 0 ? `${formatUnreadCount(totalUnread)} 条新消息` : '暂时没有新消息' }}</strong>
                <p>首页右上角会实时汇总聊天和站内信，避免你错过协作进展。</p>
              </div>

              <button class="market-notice-card" type="button" @click="openNoticeTarget(workspaceInboxPath)">
                <span class="market-notice-card__icon market-notice-card__icon--mail">
                  <el-icon><MessageBox /></el-icon>
                </span>
                <span class="market-notice-card__copy">
                  <strong>站内信</strong>
                  <p>{{ inboxNoticeText }}</p>
                </span>
                <em class="market-notice-card__badge" :class="{ 'is-empty': inboxUnreadCount === 0 }">
                  {{ formatUnreadCount(inboxUnreadCount) }}
                </em>
              </button>

              <button class="market-notice-card" type="button" @click="openNoticeTarget(workspaceChatPath)">
                <span class="market-notice-card__icon market-notice-card__icon--chat">
                  <el-icon><ChatDotRound /></el-icon>
                </span>
                <span class="market-notice-card__copy">
                  <strong>聊天会话</strong>
                  <p>{{ chatNoticeText }}</p>
                </span>
                <em class="market-notice-card__badge" :class="{ 'is-empty': chatUnreadCount === 0 }">
                  {{ formatUnreadCount(chatUnreadCount) }}
                </em>
              </button>
            </div>
          </el-popover>

          <el-dropdown trigger="click" @command="handlePersonalCommand">
            <button class="market-nav__user-btn market-nav__user-btn--profile" type="button">
              <span class="market-nav__avatar">{{ avatarLetter }}</span>
              <span>{{ displayName }}</span>
            </button>

            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="me">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="client">用户工作台</el-dropdown-item>
                <el-dropdown-item command="developer">开发者工作台</el-dropdown-item>
                <el-dropdown-item v-if="authStore.token" command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <LoginModal v-model="loginVisible" />
    <el-dialog
      v-model="personalCenterVisible"
      title="个人中心"
      width="1100px"
      destroy-on-close
      class="personal-center-dialog"
    >
      <PersonalCenterView v-if="personalCenterVisible" :show-back-button="false" embedded />
    </el-dialog>

    <AiDemandAssistantDialog v-model="assistantVisible" />
  </header>
</template>

<style>
.personal-center-dialog.el-dialog {
  width: min(1100px, calc(100vw - 32px));
  border-radius: 24px;
  overflow: hidden;
}

.personal-center-dialog .el-dialog__header {
  margin-right: 0;
  padding: 20px 20px 0;
}

.personal-center-dialog .el-dialog__body {
  max-height: calc(100vh - 140px);
  padding: 20px;
  overflow-y: auto;
  background: #f8fafc;
}
</style>
