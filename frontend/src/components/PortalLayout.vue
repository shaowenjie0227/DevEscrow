<template>
  <div class="workspace-shell" :class="`theme-${theme}`">
    <span class="workspace-glow workspace-glow--primary" />
    <span class="workspace-glow workspace-glow--secondary" />

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

      <section class="brand-panel">
        <div class="brand-panel__meta">
          <span class="brand-eyebrow">{{ eyebrow }}</span>
          <span class="brand-status">{{ themeLabel }}</span>
        </div>

        <div class="brand-panel__body">
          <div class="brand-mark">{{ badge }}</div>
          <div class="brand-copy">
            <h1>{{ title }}</h1>
            <p>{{ subtitle }}</p>
          </div>
        </div>
      </section>

      <div class="workspace-nav-head">
        <span>导航</span>
        <strong>{{ paddedNavCount }}</strong>
      </div>

      <nav class="workspace-nav">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          @click="closeMobileMenu"
        >
          <span class="nav-link__line" />
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

        <div class="identity-actions">
          <span class="identity-chip">{{ currentSection?.label || title }}</span>
          <button type="button" class="logout-link" @click="emit('logout')">退出登录</button>
        </div>
      </div>
    </aside>

    <div class="workspace-main">
      <header class="workspace-topbar">
        <div class="topbar-copy">
          <button type="button" class="nav-toggle" aria-label="打开导航菜单" @click="openMobileMenu">
            导航
          </button>

          <div class="topbar-meta">
            <span class="topbar-kicker">{{ currentSection?.caption || subtitle }}</span>
            <span class="topbar-index">{{ currentSection?.short || '00' }}</span>
          </div>

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
        <div class="workspace-content__inner">
          <router-view />
        </div>
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

const paddedNavCount = computed(() => String(props.navItems.length).padStart(2, '0'))

const themeLabel = computed(() => {
  if (props.theme === 'developer') return '交付中'
  if (props.theme === 'admin') return '运营中'
  return '协作中'
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
  isolation: isolate;
  min-height: 100vh;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 24px;
  padding: 24px;
}

.workspace-shell.theme-client {
  --theme-primary: #2f6df6;
  --theme-primary-soft: rgba(47, 109, 246, 0.18);
  --theme-glow: rgba(96, 165, 250, 0.34);
}

.workspace-shell.theme-developer {
  --theme-primary: #0f9b8e;
  --theme-primary-soft: rgba(15, 155, 142, 0.18);
  --theme-glow: rgba(45, 212, 191, 0.28);
}

.workspace-shell.theme-admin {
  --theme-primary: #d78d19;
  --theme-primary-soft: rgba(215, 141, 25, 0.2);
  --theme-glow: rgba(251, 191, 36, 0.3);
}

.workspace-glow {
  position: fixed;
  z-index: -1;
  border-radius: 999px;
  filter: blur(84px);
  opacity: 0.48;
  pointer-events: none;
}

.workspace-glow--primary {
  top: 36px;
  left: 18px;
  width: 260px;
  height: 260px;
  background: var(--theme-glow);
}

.workspace-glow--secondary {
  right: -50px;
  bottom: 80px;
  width: 320px;
  height: 320px;
  background: color-mix(in srgb, var(--theme-primary) 24%, transparent);
}

.workspace-mask {
  position: fixed;
  inset: 0;
  z-index: 20;
  border: 0;
  background: rgba(8, 15, 31, 0.54);
  backdrop-filter: blur(10px);
}

.workspace-sidebar {
  position: sticky;
  top: 24px;
  z-index: 30;
  display: flex;
  min-height: calc(100vh - 48px);
  flex-direction: column;
  gap: 22px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 34px;
  padding: 24px;
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--theme-primary) 28%, transparent), transparent 34%),
    linear-gradient(180deg, rgba(11, 17, 31, 0.96) 0%, rgba(17, 25, 40, 0.94) 48%, rgba(22, 30, 46, 0.94) 100%);
  box-shadow:
    0 34px 80px rgba(8, 15, 31, 0.24),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  color: rgba(248, 250, 252, 0.96);
  backdrop-filter: blur(22px);
}

.workspace-sidebar::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.02), transparent 18%),
    repeating-linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.028) 0,
      rgba(255, 255, 255, 0.028) 1px,
      transparent 1px,
      transparent 22px
    );
  opacity: 0.7;
  pointer-events: none;
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
  color: rgba(226, 232, 240, 0.82);
  background: transparent;
}

.brand-panel,
.workspace-nav,
.sidebar-footer {
  position: relative;
  z-index: 1;
}

.brand-panel {
  display: grid;
  gap: 18px;
  padding-bottom: 6px;
}

.brand-panel__meta,
.workspace-nav-head,
.identity-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.brand-eyebrow,
.brand-status,
.workspace-nav-head span,
.workspace-nav-head strong,
.topbar-kicker,
.topbar-index,
.identity-chip {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.brand-eyebrow,
.workspace-nav-head span {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(226, 232, 240, 0.74);
}

.brand-status,
.topbar-index,
.identity-chip {
  background: color-mix(in srgb, var(--theme-primary) 20%, rgba(255, 255, 255, 0.06));
  color: color-mix(in srgb, var(--theme-primary) 62%, white);
}

.workspace-nav-head strong {
  background: rgba(255, 255, 255, 0.04);
  color: rgba(248, 250, 252, 0.84);
}

.brand-panel__body {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 68px;
  height: 68px;
  border-radius: 22px;
  background:
    linear-gradient(145deg, color-mix(in srgb, var(--theme-primary) 90%, white), color-mix(in srgb, var(--theme-primary) 48%, black));
  color: #fff;
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.08em;
  box-shadow:
    0 18px 36px color-mix(in srgb, var(--theme-primary) 28%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.24);
}

.brand-copy h1,
.topbar-copy h2 {
  margin: 0;
  font-size: clamp(24px, 2vw, 34px);
  line-height: 1.02;
  color: rgba(248, 250, 252, 0.98);
}

.topbar-copy h2 {
  color: var(--text-main);
}

.brand-copy p,
.topbar-profile p {
  margin: 10px 0 0;
  color: rgba(226, 232, 240, 0.74);
  line-height: 1.7;
}

.workspace-nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.nav-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  overflow: hidden;
  padding: 14px;
  border: 1px solid transparent;
  border-radius: 22px;
  color: rgba(241, 245, 249, 0.94);
  background: rgba(255, 255, 255, 0.035);
  transition:
    transform 180ms ease,
    background-color 180ms ease,
    border-color 180ms ease,
    box-shadow 180ms ease;
}

.nav-link:hover {
  transform: translateX(3px);
  background: rgba(255, 255, 255, 0.07);
  border-color: rgba(255, 255, 255, 0.08);
}

.nav-link.router-link-active {
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--theme-primary) 18%, transparent), rgba(255, 255, 255, 0.08));
  border-color: color-mix(in srgb, var(--theme-primary) 22%, rgba(255, 255, 255, 0.12));
  box-shadow: 0 18px 32px rgba(8, 15, 31, 0.16);
}

.nav-link__line {
  position: absolute;
  inset: 10px auto 10px 10px;
  width: 4px;
  border-radius: 999px;
  background: transparent;
  transition: background-color 180ms ease, transform 180ms ease;
}

.nav-link.router-link-active .nav-link__line {
  background: linear-gradient(180deg, color-mix(in srgb, var(--theme-primary) 88%, white), transparent);
}

.nav-chip {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 48px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
  color: color-mix(in srgb, var(--theme-primary) 70%, white);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.nav-link.router-link-active .nav-chip {
  background: color-mix(in srgb, var(--theme-primary) 16%, rgba(255, 255, 255, 0.08));
}

.nav-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.nav-copy strong {
  font-size: 15px;
  font-weight: 700;
}

.nav-copy small,
.identity-card small {
  color: rgba(226, 232, 240, 0.68);
  line-height: 1.55;
}

.sidebar-footer {
  margin-top: auto;
  display: grid;
  gap: 14px;
  padding-top: 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.identity-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.identity-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 48px;
  border-radius: 18px;
  background: color-mix(in srgb, var(--theme-primary) 20%, rgba(255, 255, 255, 0.12));
  color: #fff;
  font-family: var(--font-display);
  font-weight: 700;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.14);
}

.identity-card strong,
.topbar-profile strong {
  display: block;
  font-size: 15px;
}

.logout-link {
  min-height: 44px;
  padding: 0 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, color-mix(in srgb, var(--theme-primary) 82%, white), color-mix(in srgb, var(--theme-primary) 52%, black));
  color: #fff;
  font-weight: 700;
  box-shadow: 0 14px 26px color-mix(in srgb, var(--theme-primary) 24%, transparent);
  transition:
    transform 180ms ease,
    box-shadow 180ms ease,
    filter 180ms ease;
}

.logout-link:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 32px color-mix(in srgb, var(--theme-primary) 28%, transparent);
  filter: saturate(1.08);
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
  border: 1px solid rgba(255, 255, 255, 0.74);
  border-radius: 32px;
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--theme-primary) 20%, transparent), transparent 32%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.84), rgba(255, 255, 255, 0.62));
  box-shadow:
    0 24px 50px rgba(15, 23, 42, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
}

.topbar-copy {
  display: grid;
  gap: 10px;
}

.topbar-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.topbar-kicker {
  padding-inline: 14px;
  background: rgba(15, 23, 42, 0.04);
  color: color-mix(in srgb, var(--theme-primary) 76%, #334155);
  letter-spacing: 0.08em;
  text-transform: none;
}

.nav-toggle {
  display: none;
  width: fit-content;
  min-height: 40px;
  padding: 0 16px;
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
  padding: 14px 16px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.64);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.78);
}

.presence-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, #22c55e, #10b981);
  box-shadow: 0 0 0 7px rgba(34, 197, 94, 0.12);
}

.workspace-content {
  min-width: 0;
}

.workspace-content__inner {
  min-width: 0;
}

@media (max-width: 1180px) {
  .workspace-shell {
    grid-template-columns: 1fr;
    padding: 18px;
  }

  .workspace-sidebar {
    position: fixed;
    inset: 12px auto 12px 12px;
    width: min(88vw, 332px);
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

  .workspace-sidebar {
    border-radius: 28px;
    padding: 20px;
  }

  .brand-panel__body {
    gap: 14px;
  }

  .brand-mark {
    min-width: 58px;
    height: 58px;
    border-radius: 18px;
    font-size: 20px;
  }

  .workspace-topbar {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
  }

  .topbar-profile {
    width: 100%;
  }

  .identity-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
