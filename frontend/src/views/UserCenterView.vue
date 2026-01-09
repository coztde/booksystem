<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listCurrentBorrowed, me, renewBook, returnBook, type BorrowedBook, type UserProfile } from '@/api/library'
import AuthRequiredPanel from '@/components/AuthRequiredPanel.vue'
import { useAuthStore } from '@/stores/auth'
import { formatToMinute, normalizeDateTime } from '@/utils/datetime'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const { confirm } = useConfirm()

const profile = ref<UserProfile | null>(null)
const borrowedList = ref<BorrowedBook[]>([])
const loading = ref(false)
const errorMsg = ref('')

const name = computed(() => auth.user?.name || profile.value?.name || '—')
const code = computed(() => auth.user?.code || profile.value?.code || '—')

function logout() {
  auth.logout()
  router.push('/')
}

function formatDate(value: string) {
  const text = formatToMinute(value)
  return text || '—'
}

function statusText(status: number) {
  if (status === 2) return '逾期'
  if (status === 0) return '借阅中'
  return '已还'
}

function parseToDate(value: unknown) {
  const text = normalizeDateTime(value)
  if (!text) return null
  let iso = text.replace(' ', 'T')
  if (iso.length === 16) iso += ':00'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return null
  return d
}

function remainText(dueAt: unknown) {
  const due = parseToDate(dueAt)
  if (!due) return '—'

  const now = new Date()
  const diff = due.getTime() - now.getTime()
  const abs = Math.abs(diff)
  const totalMinutes = Math.floor(abs / 60000)
  const days = Math.floor(totalMinutes / (60 * 24))
  const hours = Math.floor((totalMinutes - days * 60 * 24) / 60)
  const minutes = totalMinutes - days * 60 * 24 - hours * 60

  const parts: string[] = []
  if (days > 0) parts.push(`${days}天`)
  if (hours > 0 && parts.length < 2) parts.push(`${hours}小时`)
  if (days === 0 && hours === 0) parts.push(`${Math.max(1, minutes)}分钟`)

  const text = parts.join('')
  return diff >= 0 ? `剩余${text}` : `逾期${text}`
}

function remainLevel(dueAt: unknown) {
  const due = parseToDate(dueAt)
  if (!due) return 'normal'
  const diff = due.getTime() - Date.now()
  if (diff < 0) return 'danger'
  if (diff < 24 * 60 * 60 * 1000) return 'warn'
  return 'normal'
}

async function refresh() {
  loading.value = true
  errorMsg.value = ''
  try {
    profile.value = await me()
    borrowedList.value = await listCurrentBorrowed()
  } catch (e: any) {
    errorMsg.value = e?.message || '加载失败'
    borrowedList.value = []
  } finally {
    loading.value = false
  }
}

async function doRenew(recordId: number) {
  try {
    await renewBook(recordId)
    await refresh()
    toast.success('续借成功')
  } catch (e: any) {
    toast.error(e?.message || '续借失败')
  }
}

async function doReturn(recordId: number) {
  const ok = await confirm({
    title: '确认归还',
    message: '确认归还该图书？',
    confirmText: '归还',
    cancelText: '取消',
  })
  if (!ok) return
  try {
    await returnBook(recordId)
    await refresh()
    toast.success('归还成功')
  } catch (e: any) {
    toast.error(e?.message || '归还失败')
  }
}

onMounted(async () => {
  await refresh()
})
</script>

<template>
  <div class="section">
    <div class="container">
      <template v-if="!auth.isLoggedIn">
        <div class="head">
          <div>
            <div class="h1">用户中心</div>
            <div class="muted sub">当前页面需要登录后访问。</div>
          </div>
          <div class="actions">
            <button class="btn" type="button" @click="router.push('/')">返回首页</button>
          </div>
        </div>

        <div class="auth-wrap">
          <AuthRequiredPanel />
        </div>
      </template>

      <template v-else>
        <div class="head">
          <div>
            <div class="h1">用户中心</div>
            <div class="muted sub">展示用户名与当前借阅列表。</div>
          </div>
          <div class="actions">
            <button class="btn" type="button" @click="router.push('/resources')">去检索馆藏</button>
            <button class="btn btn-primary" type="button" @click="logout">退出登录</button>
          </div>
        </div>

        <div class="grid">
          <section class="card profile">
            <div class="h2">读者信息</div>
            <div class="profile-rows">
              <div class="row"><span class="k">姓名</span><span class="v">{{ name }}</span></div>
              <div class="row"><span class="k">学号</span><span class="v">{{ code }}</span></div>
              <div class="row">
                <span class="k">状态</span>
                <span class="v pill">已登录</span>
              </div>
            </div>
            <div class="muted hint">续借可延期30天，请按时归还。</div>
          </section>

          <section class="card borrowed">
            <div class="head2">
              <div class="h2">当前借阅</div>
              <div class="muted">{{ loading ? '加载中...' : `共 ${borrowedList.length} 本` }}</div>
            </div>

            <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>

            <div v-if="!loading && borrowedList.length === 0" class="empty">
              <div class="h2">暂无借阅记录</div>
              <div class="muted">你可以在馆藏资源页选择图书并点击“借阅”。</div>
            </div>

            <ul v-else class="list">
              <li v-for="it in borrowedList" :key="it.recordId" class="item">
                <div class="row2">
                  <div class="thumb" @click="router.push(`/books/${it.bookId}`)">
                    <img v-if="it.coverUrl" :src="it.coverUrl" alt="" loading="lazy" />
                    <div v-else class="thumb-fallback" aria-hidden="true">{{ (it.title || '图').slice(0, 1) }}</div>
                  </div>
                  <div class="main">
                    <div class="title">
                      <button class="link" type="button" @click="router.push(`/books/${it.bookId}`)">{{ it.title }}</button>
                      <span class="status" :class="{ overdue: it.status === 2 }">{{ statusText(it.status) }}</span>
                    </div>
                    <div class="meta">
                      <div class="meta-left">
                        <div class="m">借出：{{ formatDate(it.borrowAt) }}</div>
                        <div class="remain" :class="remainLevel(it.dueAt)">{{ remainText(it.dueAt) }}</div>
                      </div>
                      <div class="meta-right">
                        <div class="m">应还：{{ formatDate(it.dueAt) }}</div>
                        <div class="m">续借：{{ it.renewCount }} 次</div>
                      </div>
                    </div>
                  </div>
                  <div class="ops right">
                    <button class="btn" type="button" @click="doRenew(it.recordId)">续借</button>
                    <button class="btn btn-primary" type="button" @click="doReturn(it.recordId)">归还</button>
                  </div>
                </div>
              </li>
            </ul>
          </section>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-end;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.sub {
  margin-top: 8px;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.auth-wrap {
  margin-top: 18px;
}

.grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  align-items: start;
}

.card {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  padding: 14px;
}

.profile-rows {
  margin-top: 12px;
  display: grid;
  gap: 8px;
}

.row {
  display: grid;
  grid-template-columns: 60px 1fr;
  gap: 10px;
  align-items: baseline;
}

.k {
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.5px;
}

.v {
  color: rgba(11, 43, 91, 0.9);
}

.pill {
  display: inline-flex;
  width: fit-content;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(18, 59, 121, 0.18);
  background: rgba(18, 59, 121, 0.06);
}

.hint {
  margin-top: 12px;
  font-size: 12px;
}

.head2 {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--line);
}

.alert {
  margin-top: 12px;
  border: 1px dashed rgba(184, 138, 44, 0.38);
  background: rgba(184, 138, 44, 0.06);
  padding: 10px 12px;
  border-radius: 14px;
  color: rgba(11, 43, 91, 0.86);
}

.empty {
  margin-top: 12px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(245, 247, 251, 0.86);
  padding: 16px;
  text-align: center;
}

.list {
  list-style: none;
  padding: 12px 0 0;
  display: grid;
  gap: 10px;
}

.item {
  border: 1px solid rgba(11, 43, 91, 0.1);
  background: rgba(255, 255, 255, 0.72);
  border-radius: 14px;
  padding: 12px;
}

.row2 {
  display: grid;
  grid-template-columns: 84px 1fr auto;
  gap: 12px;
  align-items: start;
}

.thumb {
  width: 84px;
  height: 112px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 12px 26px rgba(5, 20, 45, 0.1);
  cursor: pointer;
  display: grid;
  place-items: center;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumb-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-family: var(--font-serif);
  color: rgba(11, 43, 91, 0.82);
  background: radial-gradient(120% 120% at 30% 20%, rgba(18, 59, 121, 0.18) 0%, rgba(18, 59, 121, 0.06) 55%, rgba(255, 255, 255, 0) 100%);
}

.main {
  min-width: 0;
}

.title {
  font-family: var(--font-serif);
  font-weight: 700;
  color: rgba(11, 43, 91, 0.96);
  letter-spacing: 0.2px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.link {
  border: none;
  background: transparent;
  padding: 0;
  text-align: left;
  cursor: pointer;
  color: inherit;
}

.link:hover {
  text-decoration: underline;
}

.status {
  font-family: var(--font-sans);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.4px;
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid rgba(18, 59, 121, 0.22);
  background: rgba(18, 59, 121, 0.06);
  color: rgba(11, 43, 91, 0.86);
}

.status.overdue {
  border-color: rgba(184, 138, 44, 0.38);
  background: rgba(184, 138, 44, 0.08);
}

.remain {
  width: fit-content;
  font-family: var(--font-sans);
  font-weight: 650;
  font-size: 12px;
  letter-spacing: 0.3px;
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid rgba(11, 43, 91, 0.18);
  background: rgba(11, 43, 91, 0.05);
  color: rgba(11, 43, 91, 0.86);
}

.remain.warn {
  border-color: rgba(184, 138, 44, 0.4);
  background: rgba(184, 138, 44, 0.08);
}

.remain.danger {
  border-color: rgba(184, 44, 44, 0.28);
  background: rgba(184, 44, 44, 0.08);
  color: rgba(130, 18, 18, 0.92);
}

.meta {
  margin-top: 8px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 16px;
  color: var(--muted);
  font-size: 12px;
}

.meta-left,
.meta-right {
  display: grid;
  gap: 6px;
}

.ops {
  margin-top: 0;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.ops.right {
  align-items: flex-end;
  justify-content: flex-end;
  flex-direction: column;
  gap: 8px;
  padding-top: 2px;
}

.ops.right .btn {
  padding: 8px 12px;
  border-radius: 12px;
}

@media (max-width: 920px) {
  .grid {
    grid-template-columns: 1fr;
  }
  .row2 {
    grid-template-columns: 72px 1fr;
  }
  .thumb {
    width: 72px;
    height: 96px;
  }
  .meta {
    grid-template-columns: 1fr;
  }
  .ops.right {
    grid-column: 1 / -1;
    flex-direction: row;
    justify-content: flex-end;
    padding-top: 10px;
  }
}
</style>
