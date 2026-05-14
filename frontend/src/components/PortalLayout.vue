<template>
  <div class="workspace-shell" :class="`theme-${theme}`">
    <button
      v-if="mobileMenuVisible"
      type="button"
      class="workspace-mask"
      aria-label="关闭导航菜单"
      @click="closeMobileMenu"
    />

    <aside class="workspace-sidebar" :class="{ 'is-open': mobileMenuVisible }">
      <button type="button" class="sidebar-close" aria-label="关闭导航" @click="closeMobileMenu">
        关闭
      </button>

      <div class="brand-panel">
        <div class="brand-mark">{{ badge }}</div>
        <div class="brand-copy">
          <span>{{ eyebrow }}</span>
          <h1>{{ title }}</h1>
          <p>{{ subtitle }}</p>
        </div>
      </div>

      <nav class="workspace-nav">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          @click="closeMobileMenu"
        >
          <span class="nav-chip">{{ item.short }}</span>
          <span class="nav-copy">
            <strong>{{ item.label }}</strong>
            <small>{{ item.caption }}</small>
          </span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="identity-card">
          <span class="identity-avatar">{{ accountInitial }}</span>
          <div>
            <strong>{{ accountName || '当前账号' }}</strong>
            <small>{{ accountMeta }}</small>
          </div>
        </div>
        <button type="button" class="logout-link" @click="emit('logout')">退出登录</button>
      </div>
    </aside>

    <div class="workspace-main">
      <header class="workspace-topbar">
        <div class="topbar-copy">
          <button type="button" class="nav-toggle" aria-label="打开导航菜单" @click="openMobileMenu">
            导航
          </button>
          <span class="topbar-kicker">{{ currentSection?.caption || subtitle }}</span>
          <h2>{{ currentSection?.label || title }}</h2>
        </div>

        <div class="topbar-profile">
          <span class="presence-dot" />
          <div>
            <strong>{{ accountName || title }}</strong>
            <p>{{ accountMeta }}</p>
          </div>
        </div>
      </header>

      <main class="workspace-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps({
  theme: {
    type: String,
    default: 'client'
  },
  badge: {
    type: String,
    default: 'PE'
  },
  eyebrow: {
    type: String,
    default: 'Workspace'
  },
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  navItems: {
    type: Array,
    default: () => []
  },
  accountName: {
    type: String,
    default: ''
  },
  accountMeta: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['logout'])
const route = useRoute()
const mobileMenuVisible = ref(false)

const currentSection = computed(() => {
  const sorted = [...props.navItems].sort((left, right) => right.to.length - left.to.length)
  return sorted.find((item) => route.path.startsWith(item.to)) || props.navItems[0] || null
})

const accountInitial = computed(() => {
  const source = (props.accountName || props.title || 'P').trim()
  return source.slice(0, 1).toUpperCase()
})

function openMobileMenu() {
  mobileMenuVisible.value = true
}

function closeMobileMenu() {
  mobileMenuVisible.value = false
}
</script>

<style scoped>
.workspace-shell {
  position: relative;
  min-height: 100vh;
  display: grid;
  grid-template-columns: 308px minmax(0, 1fr);
  gap: 24px;
  padding: 24px;
}

.workspace-shell.theme-client {
  --theme-primary: #2563eb;
  --theme-accent: rgba(59, 130, 246, 0.14);
  --theme-glow: rgba(96, 165, 250, 0.28);
}

.workspace-shell.theme-developer {
  --theme-primary: #0f766e;
  --theme-accent: rgba(13, 148, 136, 0.14);
  --theme-glow: rgba(45, 212, 191, 0.22);
}

.workspace-shell.theme-admin {
  --theme-primary: #1f2937;
  --theme-accent: rgba(51, 65, 85, 0.14);
  --theme-glow: rgba(148, 163, 184, 0.28);
}

.workspace-mask {
  position: fixed;
  inset: 0;
  z-index: 20;
  border: 0;
  background: rgba(15, 23, 42, 0.48);
  backdrop-filter: blur(6px);
}

.workspace-sidebar {
  position: sticky;
  top: 24px;
  z-index: 30;
  display: flex;
  min-height: calc(100vh - 48px);
  flex-direction: column;
  gap: 22px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 30px;
  padding: 24px;
  background:
    radial-gradient(circle at top right, var(--theme-glow), transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, rgba(245, 247, 251, 0.92) 100%);
  box-shadow: 0 28px 50px rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(18px);
}

.sidebar-close,
.nav-toggle,
.logout-link {
  border: 0;
  font: inherit;
  cursor: pointer;
}

.sidebar-close {
  display: none;
  align-self: flex-end;
  padding: 0;
  color: rgba(71, 85, 105, 0.88);
  background: transparent;
}

.brand-panel {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 60px;
  height: 60px;
  border-radius: 20px;
  background: linear-gradient(135deg, var(--theme-primary), color-mix(in srgb, var(--theme-primary) 40%, white));
  color: #fff;
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.08em;
  box-shadow: 0 18px 30px color-mix(in srgb, var(--theme-primary) 26%, transparent);
}

.brand-copy span,
.topbar-kicker {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.brand-copy h1,
.topbar-copy h2 {
  margin: 0;
  font-size: clamp(24px, 2vw, 32px);
  line-height: 1.1;
}

.brand-copy p,
.topbar-profile p {
  margin: 10px 0 0;
  color: rgba(71, 85, 105, 0.92);
  line-height: 1.6;
}

.workspace-nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border: 1px solid transparent;
  border-radius: 20px;
  color: inherit;
  transition:
    transform 180ms ease,
    background-color 180ms ease,
    border-color 180ms ease,
    box-shadow 180ms ease;
}

.nav-link:hover {
  transform: translateX(2px);
  background: rgba(255, 255, 255, 0.68);
  border-color: rgba(148, 163, 184, 0.2);
}

.nav-link.router-link-active {
  background: linear-gradient(135deg, var(--theme-accent), rgba(255, 255, 255, 0.86));
  border-color: color-mix(in srgb, var(--theme-primary) 18%, white);
  box-shadow: 0 16px 28px color-mix(in srgb, var(--theme-primary) 10%, transparent);
}

.nav-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 46px;
  height: 46px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.nav-copy {
  display: grid;
  gap: 4px;
}

.nav-copy strong {
  font-size: 15px;
  font-weight: 700;
}

.nav-copy small,
.identity-card small {
  color: rgba(71, 85, 105, 0.88);
  line-height: 1.5;
}

.sidebar-footer {
  margin-top: auto;
  padding-top: 18px;
  border-top: 1px solid rgba(148, 163, 184, 0.18);
}

.identity-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.identity-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 46px;
  height: 46px;
  border-radius: 16px;
  background: color-mix(in srgb, var(--theme-primary) 14%, white);
  color: var(--theme-primary);
  font-family: var(--font-display);
  font-weight: 700;
}

.identity-card strong,
.topbar-profile strong {
  display: block;
  font-size: 15px;
}

.logout-link {
  width: 100%;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  color: var(--theme-primary);
  font-weight: 700;
  transition:
    transform 180ms ease,
    background-color 180ms ease;
}

.logout-link:hover {
  transform: translateY(-1px);
  background: color-mix(in srgb, var(--theme-primary) 10%, white);
}

.workspace-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.workspace-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 24px 28px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.82), rgba(255, 255, 255, 0.64)),
    radial-gradient(circle at top left, var(--theme-glow), transparent 35%);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(18px);
}

.topbar-copy {
  display: grid;
}

.nav-toggle {
  display: none;
  width: fit-content;
  margin-bottom: 12px;
  padding: 10px 14px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--theme-primary) 10%, white);
  color: var(--theme-primary);
  font-weight: 700;
}

.topbar-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: fit-content;
  padding: 12px 14px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
}

.presence-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, #22c55e, #10b981);
  box-shadow: 0 0 0 6px rgba(34, 197, 94, 0.12);
}

.workspace-content {
  min-width: 0;
}

@media (max-width: 1080px) {
  .workspace-shell {
    grid-template-columns: 1fr;
    padding: 18px;
  }

  .workspace-sidebar {
    position: fixed;
    inset: 12px auto 12px 12px;
    width: min(86vw, 320px);
    min-height: auto;
    transform: translateX(-115%);
    transition: transform 220ms ease;
  }

  .workspace-sidebar.is-open {
    transform: translateX(0);
  }

  .sidebar-close,
  .nav-toggle {
    display: inline-flex;
  }
}

@media (max-width: 720px) {
  .workspace-shell {
    gap: 16px;
    padding: 14px;
  }

  .workspace-topbar {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
  }

  .topbar-profile {
    width: 100%;
  }
}
</style>
