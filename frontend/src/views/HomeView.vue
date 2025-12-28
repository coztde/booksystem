<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import HeroCarousel, { type Slide } from '@/components/HeroCarousel.vue'
import NewsBoard, { type NewsItem } from '@/components/NewsBoard.vue'
import { listCarousel, listPortalPosts } from '@/api/library'
import { formatMMDD } from '@/utils/datetime'

const router = useRouter()

const q = ref('')

const slides = ref<Slide[]>([])
const news = ref<NewsItem[]>([])
const notices = ref<NewsItem[]>([])
const loading = ref(false)
const errorMsg = ref('')

async function loadPortal() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [carousel, newsList, noticeList] = await Promise.all([
      listCarousel(),
      listPortalPosts(2, 5),
      listPortalPosts(3, 5),
    ])

    slides.value = carousel.map((it) => ({
      id: it.id,
      tag: it.tag || '',
      title: it.title,
      subtitle: it.subtitle || '',
      accent: it.accent,
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

function quickSearch(keyword: string) {
  q.value = keyword
  submitSearch()
}

function openPost(id: number) {
  router.push(`/posts/${id}`)
}

onMounted(() => {
  loadPortal()
})
</script>

<template>
  <div class="home">
    <section class="hero">
      <div class="container hero-inner">
        <div class="hero-left">
          <div class="h1">面向课程设计的高校图书馆中文门户</div>
          <p class="lead">
            简洁、学术风格的白色+深蓝主色调布局，支持页面跳转、馆藏检索与登录态（localStorage），并与后端数据库接口对接。
          </p>

          <form class="search" @submit.prevent="submitSearch">
            <label class="label" for="search-input">馆藏检索</label>
            <div class="search-row">
              <input id="search-input" v-model="q" class="input" placeholder="书名 / 作者 / 出版社 / ISBN" />
              <button class="btn btn-primary" type="submit">搜索</button>
            </div>
            <div class="tags">
              <button class="tag" type="button" @click="quickSearch('算法')">算法</button>
              <button class="tag" type="button" @click="quickSearch('计算机网络')">计算机网络</button>
              <button class="tag" type="button" @click="quickSearch('文学')">文学</button>
              <button class="tag" type="button" @click="quickSearch('经济学原理')">经济学原理</button>
            </div>
          </form>

          <div class="meta-strip">
            <div class="meta-item">
              <div class="k">入口</div>
              <div class="v">首页 / 馆藏资源 / 登录 / 用户中心</div>
            </div>
            <div class="meta-item">
              <div class="k">提示</div>
              <div class="v">示例数据可用学号登录（密码统一 111111aA），也可直接注册新账号</div>
            </div>
          </div>
        </div>

        <div class="hero-right">
          <HeroCarousel :slides="slides" @open="openPost" />
        </div>
      </div>
    </section>

    <section id="news" class="section">
      <div class="container news-grid">
        <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>
        <NewsBoard
          title="新闻动态"
          :items="news"
          @open="openPost"
          @more="router.push({ path: '/posts', query: { type: '2' } })"
        />
        <NewsBoard
          title="通知公告"
          :items="notices"
          @open="openPost"
          @more="router.push({ path: '/posts', query: { type: '3' } })"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero {
  padding: 34px 0 18px;
}

.hero-inner {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 22px;
  align-items: start;
}

.hero-left {
  padding: 6px 0;
}

.lead {
  margin-top: 12px;
  color: var(--muted);
  max-width: 56ch;
}

.search {
  margin-top: 18px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  padding: 16px;
}

.label {
  font-size: 12px;
  letter-spacing: 0.6px;
  color: var(--muted-2);
}

.search-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  margin-top: 10px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tag {
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(11, 43, 91, 0.04);
  color: rgba(11, 43, 91, 0.78);
  padding: 6px 10px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 13px;
  transition: background 160ms ease, border-color 160ms ease;
}

.tag:hover {
  background: rgba(11, 43, 91, 0.06);
  border-color: rgba(11, 43, 91, 0.22);
}

.meta-strip {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}

.meta-item {
  display: grid;
  grid-template-columns: 70px 1fr;
  gap: 10px;
  align-items: baseline;
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.62);
}

.k {
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.5px;
}

.v {
  color: rgba(11, 43, 91, 0.86);
}

.news-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.alert {
  grid-column: 1 / -1;
  border: 1px dashed rgba(184, 138, 44, 0.38);
  background: rgba(184, 138, 44, 0.06);
  padding: 10px 12px;
  border-radius: 14px;
  color: rgba(11, 43, 91, 0.86);
}

@media (max-width: 980px) {
  .hero-inner {
    grid-template-columns: 1fr;
  }
  .news-grid {
    grid-template-columns: 1fr;
  }
}
</style>
