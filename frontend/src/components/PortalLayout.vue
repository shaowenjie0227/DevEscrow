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
          <span class="nav-copy">
            <strong>{{ item.label }}</strong>
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
  filter: blur(108px);
  opacity: 0.24;
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
  gap: 18px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.16);
  border-radius: 26px;
  padding: 26px 22px 22px;
  background:
    linear-gradient(180deg, rgba(12, 18, 28, 0.985) 0%, rgba(15, 22, 33, 0.97) 52%, rgba(18, 24, 36, 0.985) 100%);
  box-shadow: 0 22px 52px rgba(8, 15, 31, 0.18);
  color: rgba(241, 245, 249, 0.95);
}

.workspace-sidebar::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.035), transparent 14%),
    linear-gradient(90deg, color-mix(in srgb, var(--theme-primary) 12%, rgba(255, 255, 255, 0.05)), transparent 64%);
  opacity: 0.34;
  pointer-events: none;
}

.workspace-sidebar::after {
  content: "";
  position: absolute;
  top: 22px;
  bottom: 22px;
  left: 12px;
  width: 1px;
  background: linear-gradient(
    180deg,
    rgba(255, 255, 255, 0.04),
    color-mix(in srgb, var(--theme-primary) 58%, rgba(255, 255, 255, 0.18)),
    rgba(255, 255, 255, 0.04)
  );
  opacity: 0.72;
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
  gap: 14px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
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

.workspace-sidebar .brand-eyebrow,
.workspace-sidebar .brand-status,
.workspace-sidebar .workspace-nav-head span,
.workspace-sidebar .workspace-nav-head strong,
.workspace-sidebar .identity-chip {
  min-height: auto;
  padding: 0;
  border-radius: 0;
  background: transparent;
  font-size: 11px;
  letter-spacing: 0.16em;
  box-shadow: none;
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

.workspace-sidebar .brand-eyebrow,
.workspace-sidebar .workspace-nav-head span {
  color: rgba(148, 163, 184, 0.72);
}

.workspace-sidebar .brand-status,
.workspace-sidebar .identity-chip {
  color: color-mix(in srgb, var(--theme-primary) 74%, white);
}

.workspace-sidebar .workspace-nav-head strong {
  color: rgba(241, 245, 249, 0.92);
}

.brand-panel__body {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  height: 56px;
  border: 1px solid color-mix(in srgb, var(--theme-primary) 28%, rgba(148, 163, 184, 0.18));
  border-radius: 16px;
  background: color-mix(in srgb, var(--theme-primary) 14%, rgba(255, 255, 255, 0.03));
  color: color-mix(in srgb, var(--theme-primary) 68%, white);
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.12em;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
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
  gap: 6px;
}

.nav-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  overflow: hidden;
  padding: 15px 16px 15px 18px;
  border: 1px solid rgba(148, 163, 184, 0.06);
  border-radius: 16px;
  color: rgba(241, 245, 249, 0.94);
  background: rgba(255, 255, 255, 0.015);
  transition:
    background-color 180ms ease,
    border-color 180ms ease,
    color 180ms ease;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.035);
  border-color: rgba(148, 163, 184, 0.14);
}

.nav-link.router-link-active {
  background:
    linear-gradient(90deg, color-mix(in srgb, var(--theme-primary) 12%, transparent), rgba(255, 255, 255, 0.025));
  border-color: color-mix(in srgb, var(--theme-primary) 26%, rgba(148, 163, 184, 0.18));
  color: #ffffff;
}

.nav-link__line {
  position: absolute;
  inset: 12px auto 12px 0;
  width: 2px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  transform: scaleY(0.32);
  transform-origin: center;
  transition: background-color 180ms ease, transform 180ms ease;
}

.nav-link:hover .nav-link__line {
  background: rgba(255, 255, 255, 0.18);
  transform: scaleY(0.56);
}

.nav-link.router-link-active .nav-link__line {
  background: color-mix(in srgb, var(--theme-primary) 78%, white);
  transform: scaleY(1);
}

.nav-chip {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 40px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.025);
  color: color-mix(in srgb, var(--theme-primary) 66%, white);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.nav-link.router-link-active .nav-chip {
  background: color-mix(in srgb, var(--theme-primary) 12%, rgba(255, 255, 255, 0.04));
  border-color: color-mix(in srgb, var(--theme-primary) 28%, rgba(148, 163, 184, 0.18));
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
  margin-top: 8px;
  display: grid;
  gap: 12px;
  padding-top: 18px;
  border-top: 1px solid rgba(148, 163, 184, 0.12);
}

.identity-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 14px 14px 12px;
  border: 1px solid rgba(148, 163, 184, 0.1);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.03), rgba(255, 255, 255, 0.015));
}

.identity-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 42px;
  height: 42px;
  border: 1px solid color-mix(in srgb, var(--theme-primary) 24%, rgba(148, 163, 184, 0.12));
  border-radius: 12px;
  background: color-mix(in srgb, var(--theme-primary) 16%, rgba(255, 255, 255, 0.06));
  color: rgba(255, 255, 255, 0.96);
  font-family: var(--font-display);
  font-weight: 700;
  box-shadow: none;
}

.identity-card strong,
.topbar-profile strong {
  display: block;
  font-size: 15px;
}

.logout-link {
  min-height: 44px;
  padding: 0 18px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.025);
  color: rgba(241, 245, 249, 0.9);
  font-weight: 600;
  transition:
    background-color 180ms ease,
    border-color 180ms ease,
    color 180ms ease;
}

.logout-link:hover {
  background: rgba(255, 255, 255, 0.055);
  border-color: rgba(148, 163, 184, 0.22);
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

.workspace-shell.theme-client .workspace-glow,
.workspace-shell.theme-developer .workspace-glow {
  display: none;
}

.workspace-shell.theme-client .workspace-sidebar,
.workspace-shell.theme-developer .workspace-sidebar {
  gap: 14px;
  border-color: color-mix(in srgb, var(--theme-primary) 16%, rgba(71, 85, 105, 0.4));
  border-radius: 18px;
  padding: 20px 16px 18px;
  background: linear-gradient(180deg, #18202d 0%, #161d29 100%);
  box-shadow: none;
}

.workspace-shell.theme-client .workspace-sidebar::before,
.workspace-shell.theme-client .workspace-sidebar::after,
.workspace-shell.theme-developer .workspace-sidebar::before,
.workspace-shell.theme-developer .workspace-sidebar::after {
  opacity: 0;
}

.workspace-shell.theme-client .brand-panel,
.workspace-shell.theme-developer .brand-panel {
  gap: 10px;
  padding: 0 6px 12px;
  border-bottom-color: rgba(71, 85, 105, 0.34);
}

.workspace-shell.theme-client .brand-panel__meta,
.workspace-shell.theme-client .workspace-nav-head,
.workspace-shell.theme-developer .brand-panel__meta,
.workspace-shell.theme-developer .workspace-nav-head {
  display: none;
}

.workspace-shell.theme-client .brand-panel__body,
.workspace-shell.theme-developer .brand-panel__body {
  gap: 10px;
}

.workspace-shell.theme-client .brand-mark,
.workspace-shell.theme-developer .brand-mark {
  min-width: 40px;
  height: 40px;
  border-radius: 10px;
  border-color: color-mix(in srgb, var(--theme-primary) 28%, rgba(71, 85, 105, 0.26));
  background: color-mix(in srgb, var(--theme-primary) 12%, rgba(255, 255, 255, 0.03));
  color: color-mix(in srgb, var(--theme-primary) 74%, white);
  font-size: 14px;
  letter-spacing: 0.08em;
}

.workspace-shell.theme-client .brand-copy h1,
.workspace-shell.theme-developer .brand-copy h1 {
  font-size: 20px;
  line-height: 1.2;
}

.workspace-shell.theme-client .brand-copy p,
.workspace-shell.theme-developer .brand-copy p {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.45;
  color: rgba(203, 213, 225, 0.56);
}

.workspace-shell.theme-client .workspace-nav,
.workspace-shell.theme-developer .workspace-nav {
  gap: 2px;
}

.workspace-shell.theme-client .nav-link,
.workspace-shell.theme-developer .nav-link {
  gap: 12px;
  padding: 12px 12px 12px 16px;
  border-color: transparent;
  border-radius: 12px;
  background: transparent;
}

.workspace-shell.theme-client .nav-link:hover,
.workspace-shell.theme-developer .nav-link:hover {
  background: rgba(255, 255, 255, 0.03);
  border-color: transparent;
}

.workspace-shell.theme-client .nav-link.router-link-active,
.workspace-shell.theme-developer .nav-link.router-link-active {
  background: rgba(255, 255, 255, 0.045);
  border-color: transparent;
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--theme-primary) 18%, rgba(255, 255, 255, 0.08));
}

.workspace-shell.theme-client .nav-link__line,
.workspace-shell.theme-developer .nav-link__line {
  inset: 9px auto 9px 0;
  width: 3px;
  background: transparent;
  transform: scaleY(0.28);
}

.workspace-shell.theme-client .nav-link:hover .nav-link__line,
.workspace-shell.theme-developer .nav-link:hover .nav-link__line {
  background: rgba(148, 163, 184, 0.22);
  transform: scaleY(0.52);
}

.workspace-shell.theme-client .nav-link.router-link-active .nav-link__line,
.workspace-shell.theme-developer .nav-link.router-link-active .nav-link__line {
  background: color-mix(in srgb, var(--theme-primary) 92%, white);
  transform: scaleY(1);
}

.workspace-shell.theme-client .nav-chip,
.workspace-shell.theme-developer .nav-chip {
  min-width: 38px;
  height: 38px;
  border-color: color-mix(in srgb, var(--theme-primary) 12%, rgba(71, 85, 105, 0.46));
  border-radius: 10px;
  background: color-mix(in srgb, var(--theme-primary) 8%, rgba(30, 41, 59, 0.72));
  color: color-mix(in srgb, var(--theme-primary) 78%, white);
  font-size: 10px;
  letter-spacing: 0.12em;
}

.workspace-shell.theme-client .nav-link.router-link-active .nav-chip,
.workspace-shell.theme-developer .nav-link.router-link-active .nav-chip {
  background: color-mix(in srgb, var(--theme-primary) 12%, rgba(255, 255, 255, 0.03));
  border-color: color-mix(in srgb, var(--theme-primary) 26%, rgba(71, 85, 105, 0.26));
}

.workspace-shell.theme-client .nav-copy,
.workspace-shell.theme-developer .nav-copy {
  gap: 3px;
}

.workspace-shell.theme-client .nav-copy strong,
.workspace-shell.theme-developer .nav-copy strong {
  font-size: 15px;
  font-weight: 600;
}

.workspace-shell.theme-client .nav-copy small,
.workspace-shell.theme-developer .nav-copy small {
  font-size: 12px;
  line-height: 1.45;
  color: rgba(203, 213, 225, 0.62);
}

.workspace-shell.theme-client .sidebar-footer,
.workspace-shell.theme-developer .sidebar-footer {
  margin-top: 10px;
  gap: 10px;
  padding: 12px 6px 0;
  border-top-color: rgba(71, 85, 105, 0.34);
}

.workspace-shell.theme-client .identity-card,
.workspace-shell.theme-developer .identity-card {
  padding: 10px 12px;
  border-color: rgba(71, 85, 105, 0.34);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.02);
}

.workspace-shell.theme-client .identity-avatar,
.workspace-shell.theme-developer .identity-avatar {
  min-width: 36px;
  height: 36px;
  border-color: color-mix(in srgb, var(--theme-primary) 22%, rgba(71, 85, 105, 0.24));
  border-radius: 10px;
  background: color-mix(in srgb, var(--theme-primary) 12%, rgba(255, 255, 255, 0.03));
  color: color-mix(in srgb, var(--theme-primary) 76%, white);
}

.workspace-shell.theme-client .identity-card strong,
.workspace-shell.theme-developer .identity-card strong {
  font-size: 14px;
}

.workspace-shell.theme-client .identity-card small,
.workspace-shell.theme-developer .identity-card small {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.72);
}

.workspace-shell.theme-client .identity-actions,
.workspace-shell.theme-developer .identity-actions {
  justify-content: flex-end;
}

.workspace-shell.theme-client .identity-chip,
.workspace-shell.theme-developer .identity-chip {
  display: none;
}

.workspace-shell.theme-client .logout-link,
.workspace-shell.theme-developer .logout-link {
  min-height: 38px;
  padding: 0 14px;
  border-color: rgba(71, 85, 105, 0.42);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  color: rgba(226, 232, 240, 0.88);
}

.workspace-shell.theme-client .logout-link:hover,
.workspace-shell.theme-developer .logout-link:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(100, 116, 139, 0.48);
}

.workspace-shell.theme-admin .workspace-glow {
  display: none;
}

.workspace-shell.theme-admin .workspace-sidebar {
  gap: 14px;
  border-color: rgba(71, 85, 105, 0.4);
  border-radius: 18px;
  padding: 20px 16px 18px;
  background: linear-gradient(180deg, #18202d 0%, #161d29 100%);
  box-shadow: none;
}

.workspace-shell.theme-admin .workspace-sidebar::before,
.workspace-shell.theme-admin .workspace-sidebar::after {
  opacity: 0;
}

.workspace-shell.theme-admin .brand-panel {
  gap: 10px;
  padding: 0 6px 12px;
  border-bottom-color: rgba(71, 85, 105, 0.34);
}

.workspace-shell.theme-admin .brand-panel__meta,
.workspace-shell.theme-admin .workspace-nav-head {
  display: none;
}

.workspace-shell.theme-admin .brand-panel__body {
  gap: 10px;
}

.workspace-shell.theme-admin .brand-mark {
  min-width: 40px;
  height: 40px;
  border-radius: 10px;
  border-color: rgba(215, 141, 25, 0.24);
  background: rgba(215, 141, 25, 0.12);
  color: #f7c86c;
  font-size: 14px;
  letter-spacing: 0.08em;
}

.workspace-shell.theme-admin .brand-copy h1 {
  font-size: 20px;
  line-height: 1.2;
}

.workspace-shell.theme-admin .brand-copy p {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.45;
  color: rgba(203, 213, 225, 0.56);
}

.workspace-shell.theme-admin .workspace-nav {
  gap: 2px;
}

.workspace-shell.theme-admin .nav-link {
  gap: 12px;
  padding: 12px 12px 12px 16px;
  border-color: transparent;
  border-radius: 12px;
  background: transparent;
}

.workspace-shell.theme-admin .nav-link:hover {
  background: rgba(255, 255, 255, 0.03);
  border-color: transparent;
}

.workspace-shell.theme-admin .nav-link.router-link-active {
  background: rgba(255, 255, 255, 0.045);
  border-color: transparent;
  box-shadow: inset 0 0 0 1px rgba(215, 141, 25, 0.16);
}

.workspace-shell.theme-admin .nav-link__line {
  inset: 9px auto 9px 0;
  width: 3px;
  background: transparent;
  transform: scaleY(0.28);
}

.workspace-shell.theme-admin .nav-link:hover .nav-link__line {
  background: rgba(148, 163, 184, 0.22);
  transform: scaleY(0.52);
}

.workspace-shell.theme-admin .nav-link.router-link-active .nav-link__line {
  background: #d78d19;
  transform: scaleY(1);
}

.workspace-shell.theme-admin .nav-chip {
  min-width: 38px;
  height: 38px;
  border-color: rgba(71, 85, 105, 0.46);
  border-radius: 10px;
  background: rgba(30, 41, 59, 0.72);
  color: rgba(245, 158, 11, 0.92);
  font-size: 10px;
  letter-spacing: 0.12em;
}

.workspace-shell.theme-admin .nav-link.router-link-active .nav-chip {
  background: rgba(215, 141, 25, 0.12);
  border-color: rgba(215, 141, 25, 0.24);
  color: #f7c86c;
}

.workspace-shell.theme-admin .nav-copy {
  gap: 3px;
}

.workspace-shell.theme-admin .nav-copy strong {
  font-size: 15px;
  font-weight: 600;
}

.workspace-shell.theme-admin .nav-copy small {
  font-size: 12px;
  line-height: 1.45;
  color: rgba(203, 213, 225, 0.62);
}

.workspace-shell.theme-admin .sidebar-footer {
  margin-top: 10px;
  gap: 10px;
  padding: 12px 6px 0;
  border-top-color: rgba(71, 85, 105, 0.34);
}

.workspace-shell.theme-admin .identity-card {
  padding: 10px 12px;
  border-color: rgba(71, 85, 105, 0.34);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.02);
}

.workspace-shell.theme-admin .identity-avatar {
  min-width: 36px;
  height: 36px;
  border-color: rgba(215, 141, 25, 0.2);
  border-radius: 10px;
  background: rgba(215, 141, 25, 0.12);
  color: #f7d28a;
}

.workspace-shell.theme-admin .identity-card strong {
  font-size: 14px;
}

.workspace-shell.theme-admin .identity-card small {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.72);
}

.workspace-shell.theme-admin .identity-actions {
  justify-content: flex-end;
}

.workspace-shell.theme-admin .identity-chip {
  display: none;
}

.workspace-shell.theme-admin .logout-link {
  min-height: 38px;
  padding: 0 14px;
  border-color: rgba(71, 85, 105, 0.42);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  color: rgba(226, 232, 240, 0.88);
}

.workspace-shell.theme-admin .logout-link:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(100, 116, 139, 0.48);
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
    border-radius: 24px;
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
