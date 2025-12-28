<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { borrowBook, getBook, type Book } from '@/api/library'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToast()

const id = computed(() => Number(route.params.id))

const book = ref<Book | null>(null)
const loading = ref(false)
const errorMsg = ref('')

function available() {
  return Number(book.value?.availableQty ?? 0)
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    book.value = await getBook(id.value)
  } catch (e: any) {
    errorMsg.value = e?.message || '图书加载失败'
    book.value = null
  } finally {
    loading.value = false
  }
}

async function doBorrow() {
  if (!auth.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!book.value) return
  try {
    await borrowBook(book.value.id)
    await load()
    toast.success('借阅成功')
  } catch (e: any) {
    toast.error(e?.message || '借阅失败')
  }
}

onMounted(() => load())
</script>

<template>
  <div class="section">
    <div class="container">
      <div class="head">
        <button class="btn" type="button" @click="router.back()">返回</button>
        <div class="muted">馆藏资源 / 图书详情</div>
      </div>

      <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>

      <div v-if="book" class="card">
        <div class="title-row">
          <div class="h1">{{ book.title }}</div>
          <div v-if="book.category" class="pill">{{ book.category }}</div>
        </div>

        <div class="grid">
          <div class="kv"><span class="k">作者</span><span class="v">{{ book.author || '—' }}</span></div>
          <div class="kv"><span class="k">出版社</span><span class="v">{{ book.publisher || '—' }}</span></div>
          <div class="kv"><span class="k">ISBN</span><span class="v">{{ book.isbn || '—' }}</span></div>
          <div class="kv"><span class="k">馆藏位置</span><span class="v">{{ book.location || '—' }}</span></div>
          <div class="kv">
            <span class="k">库存</span>
            <span class="v">
              可借 <span class="strong">{{ book.availableQty ?? 0 }}</span> / 总量 {{ book.totalQty ?? 0 }}
            </span>
          </div>
        </div>

        <div v-if="book.description" class="desc">
          <div class="h2">简介</div>
          <div class="muted">{{ book.description }}</div>
        </div>

        <div class="ops">
          <button class="btn btn-primary" type="button" :disabled="available() <= 0" @click="doBorrow">
            {{ available() <= 0 ? '暂无可借' : '借阅' }}
          </button>
          <button class="btn" type="button" @click="router.push('/resources')">返回馆藏</button>
        </div>
      </div>

      <div v-else-if="!loading && !errorMsg" class="empty">
        <div class="h2">未找到图书</div>
        <div class="muted">请返回馆藏资源页重新选择。</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.alert {
  margin-top: 16px;
  border: 1px dashed rgba(184, 138, 44, 0.38);
  background: rgba(184, 138, 44, 0.06);
  padding: 10px 12px;
  border-radius: 14px;
  color: rgba(11, 43, 91, 0.86);
}

.card {
  margin-top: 16px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: var(--shadow);
  padding: 18px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.pill {
  flex: none;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(18, 59, 121, 0.18);
  background: rgba(18, 59, 121, 0.06);
  color: rgba(11, 43, 91, 0.8);
}

.grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 16px;
}

.kv {
  display: grid;
  grid-template-columns: 70px 1fr;
  gap: 10px;
  align-items: baseline;
}

.k {
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.4px;
}

.v {
  color: rgba(11, 43, 91, 0.88);
}

.strong {
  color: rgba(11, 43, 91, 1);
  font-weight: 800;
}

.desc {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
}

.ops {
  margin-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.empty {
  margin-top: 16px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.74);
  padding: 18px;
  text-align: center;
}

@media (max-width: 860px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>

