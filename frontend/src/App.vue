<script setup lang="ts">
import { onBeforeUnmount } from 'vue'
import { RouterView } from 'vue-router'
import SiteHeader from './components/SiteHeader.vue'
import SiteFooter from './components/SiteFooter.vue'
import ToastHost from './components/ToastHost.vue'
import ConfirmHost from './components/ConfirmHost.vue'
import { useAuthStore } from './stores/auth'
import { useAdminAuthStore } from './stores/adminAuth'
import { AUTH_EXPIRED_EVENT, type AuthScope } from './api/http'

const authStore = useAuthStore()
const adminAuthStore = useAdminAuthStore()
authStore.hydrate()
adminAuthStore.hydrate()

function onAuthExpired(event: Event) {
  const scope = (event as CustomEvent<{ scope: AuthScope }>).detail?.scope
  if (scope === 'admin') {
    adminAuthStore.logout()
    return
  }
  authStore.logout()
}

window.addEventListener(AUTH_EXPIRED_EVENT, onAuthExpired)

onBeforeUnmount(() => {
  window.removeEventListener(AUTH_EXPIRED_EVENT, onAuthExpired)
})
</script>

<template>
  <div class="app-shell">
    <ToastHost />
    <ConfirmHost />
    <SiteHeader />
    <main class="app-main">
      <RouterView />
    </main>
    <SiteFooter />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
}
</style>
