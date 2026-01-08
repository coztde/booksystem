<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BookCard from '@/components/BookCard.vue'
import { borrowBook, getBook, listBooks, type Book } from '@/api/library'
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
const recommendLoading = ref(false)
const recommendBooks = ref<Book[]>([])

function available() {
  return Number(book.value?.availableQty ?? 0)
}

async function load() {
  const currentId = id.value
  loading.value = true
  errorMsg.value = ''
  recommendBooks.value = []
  try {
    const data = await getBook(currentId)
    if (currentId !== id.value) return
    book.value = data
    await loadRecommend(data.category, currentId)
  } catch (e: any) {
    errorMsg.value = e?.message || '图书加载失败'
    book.value = null
    recommendBooks.value = []
  } finally {
    loading.value = false
  }
}

async function loadRecommend(category: string | undefined, currentBookId: number) {
  if (!category) {
    recommendBooks.value = []
    return
  }
  recommendLoading.value = true
  try {
    const list = await listBooks(undefined, category)
    recommendBooks.value = list.filter((it) => it.id !== currentBookId).slice(0, 8)
  } catch {
    recommendBooks.value = []
  } finally {
    recommendLoading.value = false
  }
}

function goCategory() {
  if (!book.value?.category) return
  router.push({ path: '/resources', query: { category: book.value.category } })
}

async function doBorrow(bookId?: number) {
  if (!auth.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const targetId = bookId ?? book.value?.id
  if (!targetId) return
  try {
    await borrowBook(targetId)
    toast.success('借阅成功')
    await load()
  } catch (e: any) {
    toast.error(e?.message || '借阅失败')
  }
}

async function scrollToTop() {
  await nextTick()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(
  id,
  async (nextId, prevId) => {
    if (prevId != null && nextId !== prevId) {
      await scrollToTop()
    }
    await load()
  },
  { immediate: true },
)
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
        <div class="layout">
          <div class="cover">
            <img v-if="book.coverUrl" :src="book.coverUrl" alt="" />
            <div v-else class="cover-fallback" aria-hidden="true">{{ (book.title || '图').slice(0, 1) }}</div>
          </div>

          <div class="content">
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
              <button class="btn btn-primary" type="button" :disabled="available() <= 0" @click="doBorrow()">
                {{ available() <= 0 ? '暂无可借' : '借阅' }}
              </button>
              <button class="btn" type="button" @click="router.push('/resources')">返回馆藏</button>
            </div>
          </div>
        </div>
      </div>

      <section v-if="book?.category" class="recommend">
        <div class="recommend-head">
          <div>
            <div class="h2">同类推荐</div>
            <div class="muted sub">更多“{{ book.category }}”类馆藏</div>
          </div>
          <button class="btn" type="button" @click="goCategory">查看更多</button>
        </div>

        <div v-if="recommendLoading" class="muted loading-tip">加载中...</div>
        <div v-else-if="recommendBooks.length === 0" class="empty small">
          <div class="h2">暂无推荐</div>
          <div class="muted">该分类下暂时没有更多图书。</div>
        </div>
        <div v-else class="recommend-grid">
          <BookCard v-for="b in recommendBooks" :key="b.id" :book="b" @view="(bid) => router.push(`/books/${bid}`)" @borrow="doBorrow" />
        </div>
      </section>

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

.layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 18px;
  align-items: start;
}

.cover {
  width: 220px;
  height: 300px;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.14);
  display: grid;
  place-items: center;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-family: var(--font-serif);
  font-size: 56px;
  color: rgba(11, 43, 91, 0.82);
  background: radial-gradient(120% 120% at 30% 20%, rgba(18, 59, 121, 0.18) 0%, rgba(18, 59, 121, 0.06) 55%, rgba(255, 255, 255, 0) 100%);
}

.content {
  min-width: 0;
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

.empty.small {
  margin-top: 12px;
  background: rgba(245, 247, 251, 0.86);
}

.recommend {
  margin-top: 16px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  padding: 16px;
}

.recommend-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
}

.recommend-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.loading-tip {
  padding: 12px 2px;
}

@media (max-width: 860px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .cover {
    width: 140px;
    height: 192px;
  }
  .grid {
    grid-template-columns: 1fr;
  }
  .recommend-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
