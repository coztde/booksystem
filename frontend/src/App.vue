<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import SiteHeader from './components/SiteHeader.vue'
import SiteFooter from './components/SiteFooter.vue'
import { useAuthStore } from './stores/auth'
import { useAdminAuthStore } from './stores/adminAuth'

const authStore = useAuthStore()
const adminAuthStore = useAdminAuthStore()
onMounted(() => {
  authStore.hydrate()
  adminAuthStore.hydrate()
})
</script>

<template>
  <div class="app-shell">
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
