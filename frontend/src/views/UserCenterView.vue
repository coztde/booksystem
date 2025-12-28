<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listCurrentBorrowed, me, renewBook, returnBook, type BorrowedBook, type UserProfile } from '@/api/library'
import { useAuthStore } from '@/stores/auth'
import { formatToMinute } from '@/utils/datetime'
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
      <div class="head">
        <div>
          <div class="h1">用户中心</div>
          <div class="muted sub">展示用户名与当前借阅列表（数据来自数据库）。</div>
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
          <div class="muted hint">登录态保存在 localStorage；后端接口通过 JWT 校验 `/api/user/**`。</div>
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
              <div class="title">
                <button class="link" type="button" @click="router.push(`/books/${it.bookId}`)">{{ it.title }}</button>
                <span class="status" :class="{ overdue: it.status === 2 }">{{ statusText(it.status) }}</span>
              </div>
              <div class="meta">
                <span class="m">借出：{{ formatDate(it.borrowAt) }}</span>
                <span class="m">应还：{{ formatDate(it.dueAt) }}</span>
                <span class="m">续借：{{ it.renewCount }} 次</span>
              </div>
              <div class="ops">
                <button class="btn" type="button" @click="doRenew(it.recordId)">续借</button>
                <button class="btn btn-primary" type="button" @click="doReturn(it.recordId)">归还</button>
              </div>
            </li>
          </ul>
        </section>
      </div>
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

.title {
  font-family: var(--font-serif);
  font-weight: 700;
  color: rgba(11, 43, 91, 0.96);
  letter-spacing: 0.2px;
  display: flex;
  align-items: center;
  gap: 8px;
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

.meta {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--muted);
  font-size: 12px;
}

.ops {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 920px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
