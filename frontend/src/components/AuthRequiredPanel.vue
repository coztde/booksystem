<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { AuthScope } from '@/api/http'

const props = withDefaults(
  defineProps<{
    scope?: AuthScope
    message?: string
    showHome?: boolean
  }>(),
  {
    scope: 'user',
    message: '',
    showHome: true,
  },
)

const router = useRouter()

const title = computed(() => (props.scope === 'admin' ? '管理员登录已过期' : '登录已过期'))
const desc = computed(() => props.message || '为保证操作安全，请重新登录后继续。')
const homeText = computed(() => (props.scope === 'admin' ? '返回门户' : '返回首页'))

function goLogin() {
  const redirect = router.currentRoute.value.fullPath
  router.replace({
    path: props.scope === 'admin' ? '/admin/login' : '/login',
    query: { redirect },
  })
}

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="panel" role="status">
    <div class="icon" aria-hidden="true">
      <span class="dot" />
    </div>
    <div class="content">
      <div class="h2">{{ title }}</div>
      <div class="muted sub">{{ desc }}</div>
      <div class="actions">
        <button class="btn btn-primary" type="button" @click="goLogin">去登录</button>
        <button v-if="showHome" class="btn" type="button" @click="goHome">{{ homeText }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.panel {
  border: 1px dashed rgba(18, 59, 121, 0.3);
  background: radial-gradient(900px 280px at 12% 0%, rgba(18, 59, 121, 0.16), transparent 60%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(245, 247, 251, 0.72));
  border-radius: var(--radius);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  padding: 16px;
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 14px;
  align-items: start;
}

.icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  border: 1px solid rgba(18, 59, 121, 0.18);
  background: rgba(18, 59, 121, 0.06);
  display: grid;
  place-items: center;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: rgba(18, 59, 121, 0.9);
  box-shadow: 0 0 0 6px rgba(18, 59, 121, 0.14);
}

.sub {
  margin-top: 8px;
  font-size: 13px;
}

.actions {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>

