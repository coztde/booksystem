<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BookCard from '@/components/BookCard.vue'
import { borrowBook, listBooks, listCategories, type Book } from '@/api/library'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const toast = useToast()

const keyword = ref<string>((route.query.q as string) || '')
const activeCategory = ref<string>((route.query.category as string) || '')
const loading = ref(false)
const errorMsg = ref('')

const categories = ref<string[]>([])
const books = ref<Book[]>([])

const shownCategories = computed(() => ['全部', ...categories.value])

const MAX_RECENT_CATEGORIES = 8
const recentCategoryStoreKey = computed(() => `lib_home_recent_categories:${auth.user?.userId ?? 'guest'}`)

async function loadCategories() {
  categories.value = await listCategories()
}

function trackRecentCategory(category: string) {
  const cat = category.trim()
  if (!cat) return

  let current: string[] = []
  try {
    const raw = localStorage.getItem(recentCategoryStoreKey.value)
    const parsed = raw ? (JSON.parse(raw) as unknown) : []
    if (Array.isArray(parsed)) {
      current = parsed.filter((it): it is string => typeof it === 'string' && it.trim().length > 0)
    }
  } catch {
    current = []
  }

  const next = [cat, ...current.filter((it) => it !== cat)].slice(0, MAX_RECENT_CATEGORIES)
  try {
    localStorage.setItem(recentCategoryStoreKey.value, JSON.stringify(next))
  } catch {
    // ignore
  }
}

async function loadBooks() {
  loading.value = true
  errorMsg.value = ''
  try {
    const q = keyword.value.trim() || undefined
    const cat = activeCategory.value || undefined
    books.value = await listBooks(q, cat)
  } catch (e: any) {
    errorMsg.value = e?.message || '馆藏数据加载失败'
    books.value = []
  } finally {
    loading.value = false
  }
}

function syncRoute() {
  const q = keyword.value.trim()
  const category = activeCategory.value
  const query: Record<string, string> = {}
  if (q) query.q = q
  if (category) query.category = category
  router.replace({ path: '/resources', query })
}

function pickCategory(cat: string) {
  activeCategory.value = cat === '全部' ? '' : cat
  if (activeCategory.value) {
    trackRecentCategory(activeCategory.value)
  }
  syncRoute()
}

function submitSearch() {
  syncRoute()
}

function viewDetail(id: number) {
  router.push(`/books/${id}`)
}

async function handleBorrow(id: number) {
  if (!auth.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await borrowBook(id)
    await loadBooks()
    toast.success('借阅成功')
  } catch (e: any) {
    toast.error(e?.message || '借阅失败')
  }
}

onMounted(async () => {
  try {
    await loadCategories()
  } catch (e: any) {
    errorMsg.value = e?.message || '分类加载失败'
  }
  await loadBooks()
})

watch(
  () => route.query,
  () => {
    keyword.value = (route.query.q as string) || ''
    activeCategory.value = (route.query.category as string) || ''
    loadBooks()
  },
)
</script>

<template>
  <div class="section">
    <div class="container">
      <div class="head">
        <div>
          <div class="h1">馆藏资源</div>
          <div class="muted sub">按分类浏览，或使用关键词进行馆藏检索。</div>
        </div>
        <form class="search" @submit.prevent="submitSearch">
          <input v-model="keyword" class="input" placeholder="输入关键词（书名/作者/出版社/ISBN）" />
          <button class="btn btn-primary" type="submit">搜索</button>
        </form>
      </div>

      <div class="layout">
        <aside class="side">
          <div class="side-sticky">
            <div class="panel">
              <div class="h2">图书分类</div>
              <div class="cats">
                <button
                  v-for="cat in shownCategories"
                  :key="cat"
                  class="cat"
                  type="button"
                  :class="{ active: (cat === '全部' && !activeCategory) || cat === activeCategory }"
                  @click="pickCategory(cat)"
                >
                  {{ cat }}
                </button>
              </div>
            </div>

            <div class="panel hint">
              <div class="h2">说明</div>
              <div class="muted">
                书本借阅期为30天
                拥有一次续借机会
                请爱护书籍，按时归还。
              </div>
            </div>
          </div>
        </aside>

        <section class="main">
          <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>
          <div class="grid" :class="{ loading }">
            <BookCard v-for="b in books" :key="b.id" :book="b" @view="viewDetail" @borrow="handleBorrow" />
          </div>
          <div v-if="!loading && books.length === 0" class="empty">
            <div class="h2">未找到结果</div>
            <div class="muted">请尝试更换关键词或分类。</div>
          </div>
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

.search {
  display: grid;
  grid-template-columns: 320px auto;
  gap: 10px;
  align-items: center;
}

.layout {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 290px 1fr;
  gap: 16px;
}

.panel {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  padding: 14px;
}

.panel.hint {
  margin-top: 12px;
}

.side-sticky {
  position: sticky;
  top: 140px;
  align-self: start;
  max-height: calc(100vh - 160px);
  overflow: auto;
  scrollbar-width: thin;
  padding-right: 4px;
}

.cats {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cat {
  padding: 8px 10px;
  border-radius: 999px;
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(11, 43, 91, 0.04);
  color: rgba(11, 43, 91, 0.8);
  cursor: pointer;
  transition: background 160ms ease, border-color 160ms ease, transform 160ms ease;
}

.cat:hover {
  transform: translateY(-1px);
  background: rgba(11, 43, 91, 0.06);
  border-color: rgba(11, 43, 91, 0.22);
}

.cat.active {
  background: rgba(11, 43, 91, 0.1);
  border-color: rgba(18, 59, 121, 0.28);
  color: rgba(11, 43, 91, 1);
}

.alert {
  margin-bottom: 12px;
  border: 1px dashed rgba(184, 138, 44, 0.38);
  background: rgba(184, 138, 44, 0.06);
  padding: 10px 12px;
  border-radius: 14px;
  color: rgba(11, 43, 91, 0.86);
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.grid.loading {
  opacity: 0.7;
  filter: saturate(0.9);
}

.empty {
  margin-top: 18px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.74);
  padding: 18px;
  text-align: center;
}

@media (max-width: 1100px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 860px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .search {
    grid-template-columns: 1fr auto;
  }
  .panel.hint {
    margin-top: 12px;
  }
  .side-sticky {
    position: static;
    max-height: none;
    overflow: visible;
    padding-right: 0;
  }
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
