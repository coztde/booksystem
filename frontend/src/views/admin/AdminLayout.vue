<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuth'

const route = useRoute()
const router = useRouter()
const adminAuth = useAdminAuthStore()
adminAuth.hydrate()

const adminName = computed(() => adminAuth.user?.name || adminAuth.user?.username || '管理员')

function isActive(path: string) {
  return route.path === path
}

function logout() {
  adminAuth.logout()
  router.replace('/admin/login')
}
</script>

<template>
  <div class="section">
    <div class="container shell">
      <aside class="side">
        <div class="brand">
          <div class="h2">后台管理</div>
          <div class="muted sub">{{ adminName }}</div>
        </div>

        <nav class="nav">
          <RouterLink class="nav-link" :class="{ active: isActive('/admin/posts') }" :to="{ path: '/admin/posts', query: { type: '2' } }">
            内容管理
          </RouterLink>
          <RouterLink class="nav-link" :class="{ active: isActive('/admin/books') }" to="/admin/books">图书管理</RouterLink>
          <RouterLink class="nav-link" :class="{ active: isActive('/admin/borrows') }" to="/admin/borrows">借阅管理</RouterLink>
          <RouterLink class="nav-link" :class="{ active: isActive('/admin/users') }" to="/admin/users">用户管理</RouterLink>
        </nav>

        <div class="actions">
          <button class="btn" type="button" @click="router.push('/')">返回门户</button>
          <button class="btn btn-primary" type="button" @click="logout">退出登录</button>
        </div>
      </aside>

      <main class="main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.shell {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  align-items: start;
}

.side {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: var(--shadow);
  padding: 14px;
  position: sticky;
  top: 92px;
}

.brand {
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
}

.sub {
  margin-top: 6px;
}

.nav {
  margin-top: 12px;
  display: grid;
  gap: 8px;
}

.nav-link {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 10px 12px;
  color: rgba(11, 43, 91, 0.86);
  background: rgba(255, 255, 255, 0.6);
  transition: border-color 160ms ease, background 160ms ease;
}

.nav-link:hover {
  border-color: var(--line-2);
}

.nav-link.active {
  border-color: rgba(18, 59, 121, 0.34);
  background: rgba(18, 59, 121, 0.06);
}

.actions {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}

.main {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: var(--shadow);
  overflow: hidden;
}

@media (max-width: 960px) {
  .shell {
    grid-template-columns: 1fr;
  }
  .side {
    position: static;
    top: auto;
  }
}
</style>

