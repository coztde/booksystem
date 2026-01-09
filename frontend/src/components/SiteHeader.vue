<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const navItems = [
  { label: '首页', to: '/' },
  { label: '馆藏资源', to: '/resources' },
]

const isHome = computed(() => route.path === '/')

function goLoginOrMe() {
  if (auth.isLoggedIn) {
    router.push('/me')
    return
  }
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <header class="site-header">
    <div class="topbar">
      <div class="container topbar-inner">
        <div class="topbar-left">
          <span class="badge">WZUT</span>
          <span class="muted">高校图书馆中文门户</span>
        </div>
        <div class="topbar-right">
          <RouterLink class="top-link" to="/resources">资源导航</RouterLink>
          <span class="dot">•</span>
          <RouterLink class="top-link" :to="{ path: '/posts', query: { type: '2' } }">新闻动态</RouterLink>
          <span class="dot">•</span>
          <RouterLink class="top-link" :to="{ path: '/posts', query: { type: '3' } }">通知公告</RouterLink>
          <span class="dot">•</span>
          <button class="top-btn" type="button" @click="goLoginOrMe">
            <span v-if="auth.isLoggedIn">用户中心</span>
            <span v-else>登录</span>
          </button>
          <button v-if="auth.isLoggedIn" class="top-btn subtle" type="button" @click="logout">退出</button>
        </div>
      </div>
    </div>

    <div class="masthead">
      <div class="container masthead-inner">
        <div class="brand" @click="router.push('/')">
          <div class="mark" aria-hidden="true">
            <span class="mark-line" />
            <span class="mark-line" />
            <span class="mark-line" />
          </div>
          <div class="brand-text">
            <div class="brand-title">高校图书馆</div>
            <div class="brand-sub">University Library · Chinese Portal</div>
          </div>
        </div>

        <nav class="nav">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="nav-link"
            :class="{ active: route.path === item.to }"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="right-slot">
          <button class="btn btn-primary nav-cta" type="button" @click="router.push(isHome ? '/resources' : '/')">
            {{ isHome ? '进入馆藏' : '返回首页' }}
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 50;
  backdrop-filter: saturate(120%) blur(10px);
}

.topbar {
  background: linear-gradient(180deg, rgba(11, 43, 91, 1) 0%, rgba(9, 34, 72, 1) 100%);
  color: rgba(255, 255, 255, 0.86);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  font-size: 13px;
}

.topbar .muted {
  color: rgba(255, 255, 255, 0.78);
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 0.6px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.top-link {
  color: rgba(255, 255, 255, 0.82);
  transition: color 160ms ease;
}

.top-link:hover {
  color: rgba(255, 255, 255, 1);
}

.dot {
  opacity: 0.4;
}

.top-btn {
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
  border-radius: 999px;
  padding: 4px 10px;
  cursor: pointer;
  transition: background 160ms ease, border-color 160ms ease;
}

.top-btn:hover {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(255, 255, 255, 0.3);
}

.top-btn.subtle {
  background: transparent;
}

.masthead {
  background: rgba(255, 255, 255, 0.72);
  border-bottom: 1px solid var(--line);
}

.masthead-inner {
  display: grid;
  grid-template-columns: 1.1fr 1fr auto;
  align-items: center;
  gap: 16px;
  padding: 14px 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  user-select: none;
  cursor: pointer;
}

.mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: radial-gradient(120% 120% at 30% 20%, rgba(18, 59, 121, 0.92) 0%, rgba(11, 43, 91, 1) 62%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(0, 0, 0, 0));
  box-shadow: 0 18px 30px rgba(11, 43, 91, 0.22);
  display: grid;
  place-items: center;
  gap: 5px;
  padding: 10px;
}

.mark-line {
  display: block;
  width: 100%;
  height: 2px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  opacity: 0.9;
}

.brand-title {
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.6px;
  color: var(--ink);
  font-size: 18px;
  line-height: 1.15;
}

.brand-sub {
  font-size: 12px;
  letter-spacing: 0.6px;
  color: var(--muted-2);
}

.nav {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
}

.nav-link {
  position: relative;
  padding: 10px 12px;
  border-radius: 12px;
  color: rgba(11, 43, 91, 0.84);
  transition: background 160ms ease, color 160ms ease;
}

.nav-link:hover {
  background: rgba(11, 43, 91, 0.06);
  color: rgba(11, 43, 91, 1);
}

.nav-link.active {
  background: rgba(11, 43, 91, 0.08);
  color: rgba(11, 43, 91, 1);
}

.right-slot {
  display: flex;
  justify-content: flex-end;
}

.nav-cta {
  padding: 10px 14px;
  border-radius: 12px;
}

@media (max-width: 880px) {
  .masthead-inner {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  .nav {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  .right-slot {
    justify-content: flex-start;
  }
}
</style>
