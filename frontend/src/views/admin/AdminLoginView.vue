<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuth'

const route = useRoute()
const router = useRouter()
const adminAuth = useAdminAuthStore()

const username = ref('admin')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

const redirect = computed(() => (route.query.redirect as string) || '/admin')

async function submit() {
  loading.value = true
  errorMsg.value = ''
  try {
    await adminAuth.login(username.value.trim(), password.value)
    router.replace(redirect.value)
  } catch (e: any) {
    errorMsg.value = e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="section">
    <div class="container">
      <div class="wrap">
        <div class="card">
          <div class="head">
            <div class="h1">管理员登录</div>
            <div class="muted sub">内容/图书/借阅/用户管理</div>
          </div>

          <form class="form" @submit.prevent="submit">
            <div class="field">
              <label class="label" for="username">用户名</label>
              <input id="username" v-model="username" class="input" placeholder="admin" autocomplete="username" />
            </div>
            <div class="field">
              <label class="label" for="password">密码</label>
              <input id="password" v-model="password" class="input" type="password" placeholder="示例：admin 密码 111111aA" autocomplete="current-password" />
            </div>

            <div v-if="errorMsg" class="error">{{ errorMsg }}</div>

            <button class="btn btn-primary submit" type="submit" :disabled="loading">
              {{ loading ? '登录中..' : '登录' }}
            </button>

            <button class="btn" type="button" @click="router.push('/')">返回门户</button>
          </form>
        </div>

        <div class="side">
          <div class="panel">
            <div class="h2">默认管理员</div>
            <ul class="tips">
              <li>用户名：admin</li>
              <li>密码：111111aA（SHA-256 存储）</li>
              <li>需先执行 `backend/sql/book_seed.sql` 初始化数据</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  display: grid;
  grid-template-columns: 1fr 0.9fr;
  gap: 16px;
  align-items: start;
}

.card {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: var(--shadow);
  overflow: hidden;
}

.head {
  padding: 18px;
  background: radial-gradient(900px 260px at 10% 0%, rgba(18, 59, 121, 0.22), transparent 56%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.78), rgba(245, 247, 251, 0.76));
  border-bottom: 1px solid var(--line);
}

.sub {
  margin-top: 8px;
}

.form {
  padding: 18px;
  display: grid;
  gap: 12px;
}

.field {
  display: grid;
  gap: 6px;
}

.label {
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.6px;
}

.error {
  border: 1px dashed rgba(184, 138, 44, 0.38);
  background: rgba(184, 138, 44, 0.06);
  color: rgba(11, 43, 91, 0.86);
  padding: 10px 12px;
  border-radius: 14px;
}

.submit {
  width: 100%;
  justify-content: center;
  padding: 12px 14px;
}

.panel {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  padding: 14px;
}

.tips {
  margin-top: 10px;
  padding-left: 18px;
  color: rgba(11, 43, 91, 0.84);
}

.tips li {
  margin: 8px 0;
}

@media (max-width: 960px) {
  .wrap {
    grid-template-columns: 1fr;
  }
}
</style>

