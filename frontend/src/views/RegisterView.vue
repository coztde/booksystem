<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listReaderTypes, type ReaderType } from '@/api/library'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const redirect = computed(() => (route.query.redirect as string) || '/me')

const name = ref('')
const code = ref('')
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const readerTypes = ref<ReaderType[]>([])
const readerTypeId = ref<number | undefined>(undefined)

const loading = ref(false)
const errorMsg = ref('')

function validate() {
  if (!name.value.trim() || !code.value.trim() || !phone.value.trim()) return '请填写姓名/学号/手机号'
  if (password.value.length < 6) return '密码长度至少6位'
  if (password.value !== confirmPassword.value) return '两次输入的密码不一致'
  return ''
}

async function submit() {
  const msg = validate()
  if (msg) {
    errorMsg.value = msg
    return
  }

  loading.value = true
  errorMsg.value = ''
  try {
    await auth.register({
      name: name.value.trim(),
      code: code.value.trim(),
      phone: phone.value.trim(),
      password: password.value,
      readerTypeId: readerTypeId.value,
    })
    router.replace(redirect.value)
  } catch (e: any) {
    errorMsg.value = e?.message || '注册失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    readerTypes.value = await listReaderTypes()
    readerTypeId.value = readerTypes.value[0]?.id
  } catch {
    readerTypes.value = []
  }
})
</script>

<template>
  <div class="section">
    <div class="container">
      <div class="wrap">
        <div class="card">
          <div class="head">
            <div class="h1">读者注册</div>
            <div class="muted sub">注册后自动登录 · 可直接借阅与管理个人借阅</div>
          </div>

          <form class="form" @submit.prevent="submit">
            <div class="field">
              <label class="label" for="name">姓名</label>
              <input id="name" v-model="name" class="input" placeholder="例如：张同学" autocomplete="name" />
            </div>
            <div class="field">
              <label class="label" for="code">学号/工号</label>
              <input id="code" v-model="code" class="input" placeholder="例如：20251234" autocomplete="username" />
            </div>
            <div class="field">
              <label class="label" for="phone">手机号</label>
              <input id="phone" v-model="phone" class="input" placeholder="例如：13800000000" autocomplete="tel" />
            </div>

            <div class="field">
              <label class="label" for="readerType">读者类型</label>
              <select id="readerType" v-model.number="readerTypeId" class="input">
                <option v-for="rt in readerTypes" :key="rt.id" :value="rt.id">{{ rt.name }}</option>
              </select>
              <div v-if="readerTypes.length === 0" class="muted tip">读者类型加载失败，可先重试或检查后端是否启动。</div>
            </div>

            <div class="field">
              <label class="label" for="password">密码</label>
              <input id="password" v-model="password" class="input" type="password" autocomplete="new-password" />
            </div>
            <div class="field">
              <label class="label" for="confirmPassword">确认密码</label>
              <input
                id="confirmPassword"
                v-model="confirmPassword"
                class="input"
                type="password"
                autocomplete="new-password"
              />
            </div>

            <div v-if="errorMsg" class="error">{{ errorMsg }}</div>

            <button class="btn btn-primary submit" type="submit" :disabled="loading">
              {{ loading ? '注册中...' : '注册并登录' }}
            </button>

            <button class="btn" type="button" @click="router.push({ path: '/login', query: { redirect: redirect } })">
              已有账号？去登录
            </button>
          </form>
        </div>

        <div class="side">
          <div class="panel">
            <div class="h2">说明</div>
            <ul class="tips">
              <li>注册信息将写入数据库 `user` 表（角色=读者）</li>
              <li>密码以 SHA-256 哈希存储（不在前端保存明文）</li>
              <li>注册成功后自动登录，token 保存在 localStorage</li>
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

.tip {
  font-size: 12px;
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

