<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import HeroCarousel, { type Slide } from '@/components/HeroCarousel.vue'
import type { NewsItem } from '@/components/NewsBoard.vue'
import { listCarousel, listPortalPosts } from '@/api/library'
import { useAuthStore } from '@/stores/auth'
import { formatMMDD } from '@/utils/datetime'
import heroReadingHallUrl from '@/assets/library/reading-hall.jpg'
import heroBookshelfUrl from '@/assets/library/bookshelf.jpg'

const router = useRouter()
const auth = useAuthStore()

const q = ref('')

const slides = ref<Slide[]>([])
const news = ref<NewsItem[]>([])
const notices = ref<NewsItem[]>([])
const loading = ref(false)
const errorMsg = ref('')
const infoTab = ref<'notices' | 'news'>('notices')

const heroBgUrl = heroReadingHallUrl
const portalBgUrl = heroBookshelfUrl

const userEntryText = computed(() => (auth.isLoggedIn ? '我的图书馆' : '读者登录'))
const currentInfoList = computed(() => (infoTab.value === 'notices' ? notices.value : news.value))
const featuredSlide = computed(() => slides.value[0] || null)

const MAX_RECENT_CATEGORIES = 8
const recentCategories = ref<string[]>([])
const recentCategoryStoreKey = computed(() => `lib_home_recent_categories:${auth.user?.userId ?? 'guest'}`)

function loadRecentCategories() {
  try {
    const raw = localStorage.getItem(recentCategoryStoreKey.value)
    if (!raw) {
      recentCategories.value = []
      return
    }
    const parsed = JSON.parse(raw) as unknown
    if (!Array.isArray(parsed)) {
      recentCategories.value = []
      return
    }
    recentCategories.value = parsed
      .filter((it): it is string => typeof it === 'string' && it.trim().length > 0)
      .map((it) => it.trim())
      .slice(0, MAX_RECENT_CATEGORIES)
  } catch {
    recentCategories.value = []
  }
}

function persistRecentCategories() {
  try {
    localStorage.setItem(recentCategoryStoreKey.value, JSON.stringify(recentCategories.value))
  } catch {
    // ignore
  }
}

function trackRecentCategory(category: string) {
  const cat = category.trim()
  if (!cat) return
  const next = [cat, ...recentCategories.value.filter((it) => it !== cat)].slice(0, MAX_RECENT_CATEGORIES)
  recentCategories.value = next
  persistRecentCategories()
}

function clearRecentCategories() {
  recentCategories.value = []
  try {
    localStorage.removeItem(recentCategoryStoreKey.value)
  } catch {
    // ignore
  }
}

const commonDatabases = [
  { title: 'CNKI 知网', href: 'https://www.cnki.net' },
  { title: 'Web of Science', href: 'https://www.webofscience.com' },
  { title: 'IEEE Xplore', href: 'https://ieeexplore.ieee.org' },
  { title: 'ScienceDirect', href: 'https://www.sciencedirect.com' },
  { title: 'EBSCO', href: 'https://www.ebsco.com' },
]

async function loadPortal() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [carousel, newsList, noticeList] = await Promise.all([
      listCarousel(),
      listPortalPosts(2, 6),
      listPortalPosts(3, 6),
    ])

    slides.value = carousel.map((it) => ({
      id: it.id,
      tag: it.tag || '',
      title: it.title,
      subtitle: it.subtitle || '',
      accent: it.accent,
      coverUrl: it.coverUrl,
    }))
    news.value = newsList.map((it) => ({ id: it.id, title: it.title, date: formatMMDD(it.publishTime) }))
    notices.value = noticeList.map((it) => ({ id: it.id, title: it.title, date: formatMMDD(it.publishTime) }))
  } catch (e: any) {
    errorMsg.value = e?.message || '门户内容加载失败'
    slides.value = []
    news.value = []
    notices.value = []
  } finally {
    loading.value = false
  }
}

function submitSearch() {
  const keyword = q.value.trim()
  router.push({ path: '/resources', query: keyword ? { q: keyword } : {} })
}

function quickCategory(category: string) {
  trackRecentCategory(category)
  router.push({ path: '/resources', query: { category } })
}

function goMyLibrary() {
  if (auth.isLoggedIn) {
    router.push('/me')
    return
  }
  router.push({ path: '/login', query: { redirect: '/' } })
}

function scrollTo(id: string) {
  const el = document.getElementById(id)
  if (!el) return
  el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function openPost(id: number) {
  router.push(`/posts/${id}`)
}

onMounted(() => {
  loadPortal()
})

watch(recentCategoryStoreKey, () => loadRecentCategories(), { immediate: true })
</script>

<template>
  <div class="home">
    <section class="hero">
      <div class="hero-bg" :style="{ backgroundImage: `url(${heroBgUrl})` }" aria-hidden="true" />
      <div class="hero-veil" aria-hidden="true" />

      <div class="container hero-inner">
        <div class="hero-main">
          <button class="hero-brand" type="button" @click="router.push('/')">
            <div class="brand-mark" aria-hidden="true">
              <span class="mark-line" />
              <span class="mark-line" />
              <span class="mark-line" />
            </div>
            <div class="brand-text">
              <div class="brand-title">高校图书馆</div>
              <div class="brand-sub">University Library · Chinese Portal</div>
            </div>
          </button>

          <div class="hero-heading">
            <div class="hero-kicker">
              <span class="k-dot" aria-hidden="true" />
              <span class="k-text">雾幕学术 · LIBRARY PORTAL</span>
              <span class="k-sep" aria-hidden="true">·</span>
              <span class="k-code">CAT. 2026</span>
            </div>
            <div class="h1 hero-title">馆藏检索与资源服务入口</div>
            <div class="muted hero-desc">以检索为中心，聚合资源导航、资讯动态与常用服务。</div>
          </div>

          <form class="portal-bar" @submit.prevent="submitSearch">
            <div class="portal-search">
              <span class="portal-mode">搜索</span>
              <label class="sr-only" for="portal-q">馆藏检索关键词</label>
              <input
                id="portal-q"
                v-model="q"
                class="portal-input"
                type="search"
                inputmode="search"
                autocomplete="off"
                enterkeyhint="search"
                placeholder="书名 / 作者 / 出版社 / ISBN"
                aria-label="馆藏检索"
              />
              <button class="portal-submit" type="submit">检索</button>
            </div>
            <div class="portal-quick" aria-label="快捷分类">
              <span class="quick-label">快捷</span>
              <template v-if="recentCategories.length">
                <button
                  v-for="cat in recentCategories"
                  :key="`cat:${cat}`"
                  class="quick-chip recent"
                  type="button"
                  :title="cat"
                  @click="quickCategory(cat)"
                >
                  {{ cat }}
                </button>
                <button class="quick-clear" type="button" aria-label="清空快捷分类" @click="clearRecentCategories">清空</button>
              </template>
              <span v-else class="quick-empty">暂无 · 去“馆藏资源”点分类即可生成</span>
            </div>
            <nav class="portal-nav" aria-label="主导航">
              <button class="nav-item" type="button" @click="router.push('/resources')">资源</button>
              <button class="nav-item" type="button" @click="scrollTo('services')">服务</button>
              <button class="nav-item" type="button" @click="scrollTo('portal')">资讯</button>
              <button class="nav-item" type="button" @click="goMyLibrary">{{ userEntryText }}</button>
            </nav>
          </form>

          <div class="dock" aria-label="快捷服务">
            <button class="dock-item" type="button" @click="router.push('/resources')">
              <span class="dock-icon" aria-hidden="true">
                <svg class="dock-svg" viewBox="0 0 24 24">
                  <circle cx="11" cy="11" r="6" />
                  <path d="M20 20l-3.4-3.4" />
                </svg>
              </span>
              <span class="dock-text">馆藏检索</span>
            </button>
            <button class="dock-item" type="button" @click="goMyLibrary">
              <span class="dock-icon" aria-hidden="true">
                <svg class="dock-svg" viewBox="0 0 24 24">
                  <rect x="4" y="7" width="12" height="10" rx="2" />
                  <path d="M7 10h6" />
                  <path d="M7 13h4" />
                  <path d="M14 12h6" />
                  <path d="M17 9l3 3-3 3" />
                </svg>
              </span>
              <span class="dock-text">入馆与借阅</span>
            </button>
            <button class="dock-item" type="button" @click="router.push({ path: '/posts', query: { type: '2' } })">
              <span class="dock-icon" aria-hidden="true">
                <svg class="dock-svg" viewBox="0 0 24 24">
                  <rect x="6" y="4" width="12" height="16" rx="2" />
                  <path d="M12 9h4" />
                  <path d="M14 7v4" />
                  <path d="M9 18h6" />
                </svg>
              </span>
              <span class="dock-text">新书通报</span>
            </button>
            <button class="dock-item" type="button" @click="router.push({ path: '/posts', query: { type: '3' } })">
              <span class="dock-icon" aria-hidden="true">
                <svg class="dock-svg" viewBox="0 0 24 24">
                  <path d="M6 16v-5a6 6 0 0 1 12 0v5" />
                  <path d="M5 16h14" />
                  <path d="M10 18a2 2 0 0 0 4 0" />
                  <path d="M12 5v-1" />
                </svg>
              </span>
              <span class="dock-text">通知公告</span>
            </button>
            <button class="dock-item" type="button" @click="scrollTo('portal')">
              <span class="dock-icon" aria-hidden="true">
                <svg class="dock-svg" viewBox="0 0 24 24">
                  <rect x="5" y="4" width="14" height="16" rx="2" />
                  <path d="M8 8h8" />
                  <path d="M8 11h8" />
                  <path d="M8 14h6" />
                  <path d="M8 17h5" />
                </svg>
              </span>
              <span class="dock-text">资讯速览</span>
            </button>
            <button class="dock-item" type="button" @click="router.push('/resources')">
              <span class="dock-icon" aria-hidden="true">
                <svg class="dock-svg" viewBox="0 0 24 24">
                  <rect x="4" y="4" width="7" height="7" rx="1.5" />
                  <rect x="13" y="4" width="7" height="7" rx="1.5" />
                  <rect x="4" y="13" width="7" height="7" rx="1.5" />
                  <rect x="13" y="13" width="7" height="7" rx="1.5" />
                </svg>
              </span>
              <span class="dock-text">分类导航</span>
            </button>
            <button class="dock-item" type="button" @click="router.push('/admin')">
              <span class="dock-icon" aria-hidden="true">
                <svg class="dock-svg" viewBox="0 0 24 24">
                  <circle cx="12" cy="12" r="3.5" />
                  <path d="M12 2v3" />
                  <path d="M12 19v3" />
                  <path d="M2 12h3" />
                  <path d="M19 12h3" />
                  <path d="M4.22 4.22l2.12 2.12" />
                  <path d="M17.66 17.66l2.12 2.12" />
                  <path d="M4.22 19.78l2.12-2.12" />
                  <path d="M17.66 6.34l2.12-2.12" />
                </svg>
              </span>
              <span class="dock-text">后台管理</span>
            </button>
            <button class="dock-item" type="button" @click="scrollTo('services')">
              <span class="dock-icon" aria-hidden="true">
                <svg class="dock-svg" viewBox="0 0 24 24">
                  <path d="M9 7V6a3 3 0 0 1 6 0v1" />
                  <rect x="4" y="7" width="16" height="13" rx="2" />
                  <path d="M4 13h16" />
                  <path d="M10 13v2" />
                  <path d="M14 13v2" />
                </svg>
              </span>
              <span class="dock-text">常用服务</span>
            </button>
          </div>

          <button class="scroll-cue" type="button" @click="scrollTo('portal')">
            <span class="chev" aria-hidden="true" />
            <span class="cue-text">下滑查看更多</span>
          </button>
        </div>

        <aside class="hero-side">
          <HeroCarousel v-if="slides.length" class="hero-carousel" :slides="slides" @open="openPost" />
          <div v-else class="carousel-placeholder" aria-label="轮播推荐">
            <div class="ph-title">轮播推荐</div>
            <div class="ph-sub muted">暂无轮播内容（可在后台内容管理中添加）</div>
          </div>

          <div class="db-panel">
            <div class="db-head">
              <div class="db-title">常用数据库</div>
            </div>
            <ul class="db-list">
              <li v-for="db in commonDatabases" :key="db.title" class="db-item">
                <a class="db-link" :href="db.href" target="_blank" rel="noopener noreferrer">{{ db.title }}</a>
              </li>
            </ul>
          </div>
        </aside>
      </div>
    </section>

    <section id="portal" class="portal">
      <div class="portal-band" :style="{ backgroundImage: `url(${portalBgUrl})` }" aria-hidden="true" />

      <div class="container portal-inner">
        <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>

        <div class="cards">
          <article class="card lecture">
            <div class="card-head">
              <div class="card-title">讲座与展览</div>
              <button class="card-more" type="button" @click="router.push({ path: '/posts', query: { type: '2' } })">MORE+</button>
            </div>

            <div class="card-body lecture-body">
              <button class="feature" type="button" :disabled="!featuredSlide" @click="featuredSlide && openPost(featuredSlide.id)">
                <div class="feature-media" :style="{ backgroundImage: `url(${featuredSlide?.coverUrl || heroBgUrl})` }">
                  <div class="feature-shade" aria-hidden="true" />
                </div>
                <div class="feature-text">
                  <div class="feature-tag">{{ featuredSlide?.tag || '专题' }}</div>
                  <div class="feature-title">{{ featuredSlide?.title || '暂无轮播内容' }}</div>
                  <div class="feature-sub">{{ featuredSlide?.subtitle || '请在后台内容管理添加轮播数据' }}</div>
                </div>
              </button>

              <div class="mini-list">
                <button v-for="it in news.slice(0, 4)" :key="it.id" class="mini-item" type="button" @click="openPost(it.id)">
                  <span class="mini-date">{{ it.date }}</span>
                  <span class="mini-title">{{ it.title }}</span>
                </button>
                <div v-if="!loading && news.length === 0" class="mini-empty muted">暂无动态</div>
              </div>
            </div>
          </article>

          <article class="card info">
            <div class="card-head tabs">
              <div class="tab-group" role="tablist" aria-label="资讯切换">
                <button class="tab" type="button" :class="{ active: infoTab === 'notices' }" @click="infoTab = 'notices'">通知公告</button>
                <button class="tab" type="button" :class="{ active: infoTab === 'news' }" @click="infoTab = 'news'">新闻动态</button>
              </div>
              <button
                class="card-more"
                type="button"
                @click="router.push({ path: '/posts', query: { type: infoTab === 'notices' ? '3' : '2' } })"
              >
                MORE+
              </button>
            </div>

            <div class="card-body info-body">
              <div v-if="loading" class="muted loading-tip">加载中...</div>
              <div v-else-if="currentInfoList.length === 0" class="muted loading-tip">暂无内容</div>
              <ul v-else class="info-list">
                <li v-for="it in currentInfoList.slice(0, 8)" :key="it.id" class="info-item">
                  <button class="info-btn" type="button" @click="openPost(it.id)">
                    <span class="info-title">{{ it.title }}</span>
                    <span class="info-date">{{ it.date }}</span>
                  </button>
                </li>
              </ul>
            </div>
          </article>
        </div>

        <div id="services" class="services">
          <div class="services-head">
            <div class="h2">常用服务</div>
            <div class="muted">可点击跳转</div>
          </div>

          <div class="service-grid">
            <button class="service-card" type="button" @click="router.push('/resources')">
              <div class="service-bg" :style="{ backgroundImage: `url(${portalBgUrl})`, backgroundPosition: '20% 35%' }" aria-hidden="true" />
              <div class="service-mask" aria-hidden="true" />
              <div class="service-text">
                <div class="st">馆藏检索</div>
                <div class="ss">按分类/关键词浏览</div>
              </div>
            </button>
            <button class="service-card" type="button" @click="router.push({ path: '/posts', query: { type: '2' } })">
              <div class="service-bg" :style="{ backgroundImage: `url(${heroBgUrl})`, backgroundPosition: '62% 28%' }" aria-hidden="true" />
              <div class="service-mask" aria-hidden="true" />
              <div class="service-text">
                <div class="st">新闻动态</div>
                <div class="ss">图书馆资讯更新</div>
              </div>
            </button>
            <button class="service-card" type="button" @click="router.push({ path: '/posts', query: { type: '3' } })">
              <div class="service-bg" :style="{ backgroundImage: `url(${portalBgUrl})`, backgroundPosition: '78% 62%' }" aria-hidden="true" />
              <div class="service-mask" aria-hidden="true" />
              <div class="service-text">
                <div class="st">通知公告</div>
                <div class="ss">重要通知与安排</div>
              </div>
            </button>
            <button class="service-card" type="button" @click="goMyLibrary">
              <div class="service-bg" :style="{ backgroundImage: `url(${heroBgUrl})`, backgroundPosition: '36% 75%' }" aria-hidden="true" />
              <div class="service-mask" aria-hidden="true" />
              <div class="service-text">
                <div class="st">我的图书馆</div>
                <div class="ss">借阅/续借/归还</div>
              </div>
            </button>
            <button class="service-card" type="button" @click="router.push('/admin')">
              <div class="service-bg" :style="{ backgroundImage: `url(${portalBgUrl})`, backgroundPosition: '12% 72%' }" aria-hidden="true" />
              <div class="service-mask" aria-hidden="true" />
              <div class="service-text">
                <div class="st">后台管理</div>
                <div class="ss">内容/图书/用户</div>
              </div>
            </button>
            <button class="service-card" type="button" @click="scrollTo('portal')">
              <div class="service-bg" :style="{ backgroundImage: `url(${heroBgUrl})`, backgroundPosition: '50% 15%' }" aria-hidden="true" />
              <div class="service-mask" aria-hidden="true" />
              <div class="service-text">
                <div class="st">讲座与展览</div>
                <div class="ss">专题与活动入口</div>
              </div>
            </button>
            <button class="service-card" type="button" @click="router.push('/resources')">
              <div class="service-bg" :style="{ backgroundImage: `url(${portalBgUrl})`, backgroundPosition: '88% 38%' }" aria-hidden="true" />
              <div class="service-mask" aria-hidden="true" />
              <div class="service-text">
                <div class="st">分类导航</div>
                <div class="ss">按学科快速筛选</div>
              </div>
            </button>
            <button class="service-card" type="button" @click="router.push('/')">
              <div class="service-bg" :style="{ backgroundImage: `url(${heroBgUrl})`, backgroundPosition: '40% 48%' }" aria-hidden="true" />
              <div class="service-mask" aria-hidden="true" />
              <div class="service-text">
                <div class="st">开放时间</div>
                <div class="ss">示例入口（可扩展）</div>
              </div>
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home {
  position: relative;
}

.hero {
  position: relative;
  overflow: hidden;
  padding: 34px 0 24px;
  min-height: 64vh;
  display: flex;
  align-items: stretch;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  transform: scale(1.08);
  filter: saturate(0.92) contrast(1.06) brightness(1.02);
  opacity: 0.88;
}

.hero-veil {
  position: absolute;
  inset: 0;
  background: radial-gradient(920px 560px at 18% 6%, rgba(11, 43, 91, 0.62), transparent 60%),
    radial-gradient(760px 480px at 88% 0%, rgba(184, 138, 44, 0.22), transparent 58%),
    linear-gradient(180deg, rgba(245, 247, 251, 0.12) 0%, rgba(245, 247, 251, 0.88) 42%, rgba(245, 247, 251, 0.98) 100%);
}

.hero-veil::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(11, 43, 91, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(11, 43, 91, 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
  opacity: 0.5;
  pointer-events: none;
}

.hero-inner {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 22px;
  align-items: start;
  border: 1px solid rgba(11, 43, 91, 0.16);
  border-radius: calc(var(--radius) + 8px);
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: saturate(135%) blur(14px);
  box-shadow: 0 30px 90px rgba(5, 20, 45, 0.16);
  padding: 18px;
  overflow: hidden;
  animation: rise 620ms cubic-bezier(0.2, 0.9, 0.2, 1) both;
}

.hero-inner::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(11, 43, 91, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(11, 43, 91, 0.04) 1px, transparent 1px);
  background-size: 48px 48px;
  opacity: 0.45;
  pointer-events: none;
}

.hero-inner::after {
  content: '';
  position: absolute;
  top: 16px;
  bottom: 16px;
  left: 14px;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(184, 138, 44, 0.95), rgba(184, 138, 44, 0));
  opacity: 0.75;
  pointer-events: none;
}

.hero-main {
  min-height: 520px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: relative;
  z-index: 1;
  padding-left: 6px;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  width: fit-content;
  user-select: none;
  border-radius: 18px;
  padding: 6px 10px;
  transition: background 160ms ease;
  border: none;
  text-align: left;
  cursor: pointer;
  background: transparent;
}

.hero-brand:hover {
  background: rgba(11, 43, 91, 0.06);
}

.brand-mark {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: radial-gradient(120% 120% at 30% 20%, rgba(18, 59, 121, 0.92) 0%, rgba(11, 43, 91, 1) 62%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.1), rgba(0, 0, 0, 0));
  box-shadow: 0 18px 32px rgba(11, 43, 91, 0.24);
  display: grid;
  place-items: center;
  gap: 5px;
  padding: 12px;
}

.mark-line {
  display: block;
  width: 100%;
  height: 2px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  opacity: 0.9;
}

.brand-title {
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.6px;
  color: rgba(11, 43, 91, 0.96);
  font-size: 22px;
  line-height: 1.15;
}

.brand-sub {
  font-size: 12px;
  letter-spacing: 0.6px;
  color: var(--muted-2);
}

.hero-heading {
  padding: 0 2px;
  display: grid;
  gap: 6px;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 14px 26px rgba(5, 20, 45, 0.08);
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 0.8px;
  color: rgba(11, 43, 91, 0.76);
}

.k-dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: rgba(184, 138, 44, 0.92);
  box-shadow: 0 0 0 4px rgba(184, 138, 44, 0.12);
}

.k-text {
  color: rgba(11, 43, 91, 0.84);
}

.k-code {
  color: rgba(11, 43, 91, 0.6);
}

.hero-title {
  margin-top: 2px;
}

.hero-desc {
  max-width: 52ch;
}

.portal-bar {
  border-radius: calc(var(--radius) + 6px);
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 44px rgba(5, 20, 45, 0.08);
  padding: 12px;
  display: grid;
  gap: 12px;
}

.portal-search {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(245, 247, 251, 0.74);
}

.portal-mode {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(11, 43, 91, 0.16);
  background: rgba(255, 255, 255, 0.74);
  font-size: 12px;
  letter-spacing: 0.6px;
  color: rgba(11, 43, 91, 0.78);
}

.portal-input {
  border: none;
  outline: none;
  background: transparent;
  padding: 6px 2px;
  min-width: 0;
  color: rgba(11, 43, 91, 0.92);
}

.portal-input::placeholder {
  color: rgba(11, 43, 91, 0.46);
}

.portal-submit {
  border: none;
  border-radius: 14px;
  padding: 10px 14px;
  cursor: pointer;
  background: linear-gradient(180deg, rgba(18, 59, 121, 1) 0%, rgba(11, 43, 91, 1) 100%);
  color: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 30px rgba(11, 43, 91, 0.18);
  transition: transform 160ms ease, box-shadow 160ms ease, filter 160ms ease;
}

.portal-submit:hover {
  transform: translateY(-1px);
  box-shadow: 0 20px 34px rgba(11, 43, 91, 0.22);
  filter: saturate(1.03);
}

.portal-quick {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding: 0 2px;
}

.quick-label {
  font-size: 12px;
  letter-spacing: 0.6px;
  color: rgba(11, 43, 91, 0.56);
  font-family: var(--font-mono);
}

.quick-chip {
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.72);
  color: rgba(11, 43, 91, 0.82);
  padding: 6px 10px;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 160ms ease, background 160ms ease, border-color 160ms ease;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-chip:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.86);
  border-color: rgba(11, 43, 91, 0.22);
}

.quick-chip.recent {
  border-color: rgba(184, 138, 44, 0.3);
  background: radial-gradient(120% 120% at 30% 20%, rgba(184, 138, 44, 0.18), rgba(255, 255, 255, 0.88) 58%),
    rgba(255, 255, 255, 0.74);
  box-shadow: 0 10px 18px rgba(11, 43, 91, 0.06);
}

.quick-empty {
  font-size: 12px;
  color: rgba(11, 43, 91, 0.52);
  letter-spacing: 0.4px;
  padding-left: 6px;
}

.quick-clear {
  border: 1px dashed rgba(11, 43, 91, 0.18);
  background: rgba(11, 43, 91, 0.02);
  color: rgba(11, 43, 91, 0.72);
  padding: 6px 10px;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 160ms ease, background 160ms ease, border-color 160ms ease;
}

.quick-clear:hover {
  transform: translateY(-1px);
  background: rgba(11, 43, 91, 0.04);
  border-color: rgba(11, 43, 91, 0.26);
}

.portal-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 2px;
}

.nav-item {
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(11, 43, 91, 0.04);
  color: rgba(11, 43, 91, 0.86);
  padding: 8px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 160ms ease, border-color 160ms ease, transform 160ms ease;
}

.nav-item:hover {
  background: rgba(11, 43, 91, 0.06);
  border-color: rgba(11, 43, 91, 0.22);
  transform: translateY(-1px);
}

.dock {
  margin-top: 2px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.dock-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  text-align: left;
  border-radius: 16px;
  border: 1px solid rgba(11, 43, 91, 0.12);
  background: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  transition: transform 160ms ease, background 160ms ease, border-color 160ms ease, box-shadow 200ms ease;
}

.dock-item:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.86);
  border-color: rgba(11, 43, 91, 0.2);
  box-shadow: 0 16px 32px rgba(5, 20, 45, 0.08);
}

.dock-icon {
  width: 32px;
  height: 32px;
  border-radius: 14px;
  border: 1px solid rgba(11, 43, 91, 0.16);
  background: radial-gradient(120% 120% at 30% 20%, rgba(184, 138, 44, 0.24), rgba(255, 255, 255, 0.9) 55%),
    linear-gradient(180deg, rgba(18, 59, 121, 0.24), rgba(11, 43, 91, 0.08));
  box-shadow: 0 10px 20px rgba(11, 43, 91, 0.1);
  display: grid;
  place-items: center;
  color: rgba(11, 43, 91, 0.86);
}

.dock-svg {
  width: 18px;
  height: 18px;
  stroke: currentColor;
  fill: none;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.dock-text {
  font-size: 13px;
  color: rgba(11, 43, 91, 0.86);
}

.scroll-cue {
  margin-top: auto;
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 999px;
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: transform 160ms ease, border-color 160ms ease, background 160ms ease;
}

.scroll-cue:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.84);
  border-color: rgba(11, 43, 91, 0.22);
}

.chev {
  width: 10px;
  height: 10px;
  border-right: 2px solid rgba(11, 43, 91, 0.84);
  border-bottom: 2px solid rgba(11, 43, 91, 0.84);
  transform: rotate(45deg);
  animation: cue 1.25s ease-in-out infinite;
}

.cue-text {
  letter-spacing: 0.6px;
  font-size: 12px;
  color: rgba(11, 43, 91, 0.76);
}

@keyframes cue {
  0%,
  100% {
    transform: translateY(0) rotate(45deg);
  }
  50% {
    transform: translateY(3px) rotate(45deg);
  }
}

.hero-side {
  display: grid;
  gap: 14px;
  position: relative;
  z-index: 1;
  animation: rise 720ms cubic-bezier(0.2, 0.9, 0.2, 1) both;
  animation-delay: 80ms;
}

.carousel-placeholder {
  border-radius: var(--radius);
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.72);
  box-shadow: var(--shadow-sm);
  padding: 18px;
  min-height: 240px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.ph-title {
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 18px;
  color: rgba(11, 43, 91, 0.94);
}

.ph-sub {
  margin-top: 8px;
  color: var(--muted);
}

.db-panel {
  border-radius: var(--radius);
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.74);
  box-shadow: var(--shadow-sm);
  padding: 14px;
}

.db-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.db-title {
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.4px;
}

.db-more {
  border: none;
  background: transparent;
  padding: 6px 8px;
  border-radius: 12px;
  cursor: pointer;
  color: rgba(18, 59, 121, 0.9);
  letter-spacing: 0.6px;
  font-size: 12px;
  transition: background 160ms ease;
}

.db-more:hover {
  background: rgba(11, 43, 91, 0.06);
}

.db-list {
  list-style: none;
  margin-top: 10px;
  display: grid;
  gap: 8px;
  padding: 0;
}

.db-link {
  display: block;
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid rgba(11, 43, 91, 0.12);
  background: rgba(11, 43, 91, 0.04);
  transition: background 160ms ease, border-color 160ms ease, transform 160ms ease;
}

.db-link:hover {
  transform: translateY(-1px);
  background: rgba(11, 43, 91, 0.06);
  border-color: rgba(11, 43, 91, 0.2);
}

.portal {
  position: relative;
  padding: 18px 0 64px;
  scroll-margin-top: 110px;
}

.portal-band {
  position: absolute;
  inset: -110px 0 auto 0;
  height: 360px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  opacity: 0.14;
  filter: saturate(0.92) contrast(1.06);
  transform: scale(1.05);
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 1) 10%, rgba(0, 0, 0, 0) 100%);
  pointer-events: none;
}

.portal-inner {
  position: relative;
  z-index: 1;
}

.alert {
  border: 1px dashed rgba(184, 138, 44, 0.46);
  background: rgba(184, 138, 44, 0.08);
  padding: 10px 12px;
  border-radius: 16px;
  color: rgba(11, 43, 91, 0.9);
  margin-bottom: 14px;
}

.cards {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 18px;
  align-items: start;
}

.card {
  border: 1px solid rgba(11, 43, 91, 0.14);
  border-radius: calc(var(--radius) + 8px);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 22px 60px rgba(5, 20, 45, 0.08);
  overflow: hidden;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid rgba(11, 43, 91, 0.12);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(245, 247, 251, 0.86) 100%);
}

.card-title {
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 18px;
  color: rgba(11, 43, 91, 0.94);
}

.card-more {
  border: none;
  background: transparent;
  padding: 6px 8px;
  border-radius: 12px;
  cursor: pointer;
  color: rgba(18, 59, 121, 0.92);
  letter-spacing: 0.6px;
  font-size: 12px;
  transition: background 160ms ease;
}

.card-more:hover {
  background: rgba(11, 43, 91, 0.06);
}

.card-body {
  padding: 16px;
}

.lecture-body {
  display: grid;
  gap: 12px;
}

.feature {
  border: 1px solid rgba(11, 43, 91, 0.12);
  background: rgba(245, 247, 251, 0.76);
  border-radius: 18px;
  overflow: hidden;
  text-align: left;
  padding: 0;
  cursor: pointer;
  transition: transform 160ms ease, border-color 160ms ease, box-shadow 200ms ease;
}

.feature:hover:not(:disabled) {
  transform: translateY(-1px);
  border-color: rgba(11, 43, 91, 0.2);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.08);
}

.feature:disabled {
  cursor: default;
  opacity: 0.9;
}

.feature-media {
  position: relative;
  height: 172px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.feature-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(11, 43, 91, 0.06) 0%, rgba(11, 43, 91, 0.62) 100%);
}

.feature-text {
  padding: 14px 14px 16px;
}

.feature-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(11, 43, 91, 0.16);
  background: rgba(255, 255, 255, 0.74);
  font-size: 12px;
  letter-spacing: 0.6px;
  color: rgba(11, 43, 91, 0.8);
}

.feature-title {
  margin-top: 10px;
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.2px;
  color: rgba(11, 43, 91, 0.96);
  font-size: 18px;
  line-height: 1.35;
}

.feature-sub {
  margin-top: 6px;
  color: rgba(11, 43, 91, 0.72);
  font-size: 13px;
  line-height: 1.55;
}

.mini-list {
  display: grid;
  gap: 8px;
}

.mini-item {
  border: 1px solid rgba(11, 43, 91, 0.12);
  background: rgba(255, 255, 255, 0.72);
  border-radius: 14px;
  padding: 10px 12px;
  cursor: pointer;
  text-align: left;
  display: grid;
  grid-template-columns: 62px 1fr;
  gap: 10px;
  align-items: center;
  transition: transform 160ms ease, background 160ms ease, border-color 160ms ease;
}

.mini-item:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.86);
  border-color: rgba(11, 43, 91, 0.2);
}

.mini-date {
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.6px;
}

.mini-title {
  color: rgba(11, 43, 91, 0.9);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-empty {
  padding: 10px 2px;
}

.card-head.tabs {
  align-items: center;
}

.tab-group {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px;
  border-radius: 999px;
  border: 1px solid rgba(11, 43, 91, 0.12);
  background: rgba(11, 43, 91, 0.04);
}

.tab {
  border: none;
  background: transparent;
  border-radius: 999px;
  padding: 7px 12px;
  cursor: pointer;
  color: rgba(11, 43, 91, 0.76);
  transition: background 160ms ease, color 160ms ease;
}

.tab.active {
  background: rgba(11, 43, 91, 0.9);
  color: rgba(255, 255, 255, 0.94);
}

.info-body {
  padding: 14px 16px 16px;
}

.loading-tip {
  padding: 10px 2px;
}

.info-list {
  list-style: none;
  display: grid;
  gap: 8px;
  padding: 0;
  margin: 0;
}

.info-btn {
  width: 100%;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid rgba(11, 43, 91, 0.12);
  background: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  text-align: left;
  transition: transform 160ms ease, background 160ms ease, border-color 160ms ease;
}

.info-btn:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.86);
  border-color: rgba(11, 43, 91, 0.2);
}

.info-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(11, 43, 91, 0.9);
}

.info-date {
  font-size: 12px;
  letter-spacing: 0.6px;
  color: var(--muted-2);
}

.services {
  margin-top: 28px;
  scroll-margin-top: 110px;
}

.services-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.service-card {
  position: relative;
  overflow: hidden;
  border-radius: calc(var(--radius) + 8px);
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.62);
  min-height: 128px;
  padding: 0;
  cursor: pointer;
  text-align: left;
  box-shadow: 0 18px 38px rgba(5, 20, 45, 0.08);
  transition: transform 160ms ease, border-color 160ms ease, box-shadow 200ms ease, filter 160ms ease;
}

.service-card:hover {
  transform: translateY(-1px);
  border-color: rgba(11, 43, 91, 0.22);
  box-shadow: 0 24px 56px rgba(5, 20, 45, 0.1);
  filter: saturate(1.03);
}

.service-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  transform: scale(1.07);
  opacity: 0.22;
  filter: saturate(0.92) contrast(1.08);
}

.service-mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(600px 220px at 10% 0%, rgba(11, 43, 91, 0.22), transparent 62%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.64) 0%, rgba(255, 255, 255, 0.92) 70%, rgba(255, 255, 255, 0.96) 100%);
}

.service-text {
  position: relative;
  z-index: 1;
  padding: 16px;
}

.st {
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.2px;
  font-size: 18px;
  color: rgba(11, 43, 91, 0.94);
}

.ss {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(11, 43, 91, 0.7);
}

@media (max-width: 1100px) {
  .hero-inner {
    grid-template-columns: 1fr;
  }
  .hero-main {
    min-height: unset;
  }
  .dock {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .cards {
    grid-template-columns: 1fr;
  }
  .service-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .portal-search {
    grid-template-columns: 1fr;
  }
  .portal-submit {
    width: 100%;
    justify-self: stretch;
  }
}

@media (max-width: 640px) {
  .hero {
    padding: 22px 0 18px;
  }
  .hero-inner {
    padding: 14px;
  }
  .brand-title {
    font-size: 19px;
  }
  .hero-main {
    padding-left: 0;
  }
  .hero-inner::after {
    display: none;
  }
}

@media (max-width: 520px) {
  .dock {
    grid-template-columns: 1fr;
  }
  .service-grid {
    grid-template-columns: 1fr;
  }
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero-inner,
  .hero-side {
    animation: none !important;
  }
}
</style>
