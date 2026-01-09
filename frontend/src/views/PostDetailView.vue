<script setup lang="ts">
import DOMPurify from 'dompurify'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPortalPost, type PortalPostDetail } from '@/api/library'
import { formatToMinute } from '@/utils/datetime'

const route = useRoute()
const router = useRouter()

const id = computed(() => Number(route.params.id))
const post = ref<PortalPostDetail | null>(null)
const loading = ref(false)
const errorMsg = ref('')

function escapeHtml(input: string) {
  return input
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function looksLikeHtml(input: string) {
  return /<\/?[a-z][\s\S]*>/i.test(input)
}

const contentHtml = computed(() => {
  const raw = post.value?.content || ''
  if (!raw) return ''

  const html = looksLikeHtml(raw) ? raw : escapeHtml(raw).replace(/\n/g, '<br/>')

  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: [
      'a',
      'b',
      'blockquote',
      'br',
      'code',
      'div',
      'em',
      'h1',
      'h2',
      'h3',
      'h4',
      'h5',
      'h6',
      'hr',
      'i',
      'img',
      'li',
      'ol',
      'p',
      'pre',
      's',
      'span',
      'strong',
      'u',
      'ul',
    ],
    ALLOWED_ATTR: ['href', 'title', 'target', 'rel', 'src', 'alt', 'width', 'height'],
  })
})

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    post.value = await getPortalPost(id.value)
  } catch (e: any) {
    errorMsg.value = e?.message || '内容加载失败'
    post.value = null
  } finally {
    loading.value = false
  }
}

function backToList() {
  const type = post.value?.type || Number(route.query.type || 2)
  router.push({ path: '/posts', query: { type: String(type) } })
}

onMounted(() => load())
</script>

<template>
  <div class="section">
    <div class="container">
      <div class="head">
        <button class="btn" type="button" @click="backToList">返回列表</button>
        <div class="muted">门户内容 / 详情</div>
      </div>

      <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>

      <article v-if="post" class="card">
        <div class="h1">{{ post.title }}</div>
        <div class="meta">
          <span class="m">发布时间：{{ formatToMinute(post.publishTime) }}</span>
          <span class="m">类型：{{ post.type === 3 ? '公告' : post.type === 2 ? '新闻' : '轮播' }}</span>
        </div>
        <div class="content" v-html="contentHtml"></div>
      </article>

      <div v-else-if="!loading && !errorMsg" class="empty">
        <div class="h2">内容不存在</div>
        <div class="muted">请返回列表重新选择。</div>
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

.meta {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: var(--muted-2);
}

.content {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--line);
  color: rgba(11, 43, 91, 0.88);
  line-height: 1.85;
}

.content :deep(p) {
  margin: 0 0 0.9em;
}

.content :deep(ul),
.content :deep(ol) {
  padding-left: 1.3em;
  margin: 0 0 0.9em;
}

.content :deep(li) {
  margin: 0.25em 0;
}

.content :deep(a) {
  color: rgba(18, 59, 121, 0.92);
  text-decoration: underline;
  text-underline-offset: 2px;
}

.content :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  border-radius: 14px;
  box-shadow: 0 14px 34px rgba(5, 20, 45, 0.14);
  margin: 12px 0;
}

.content :deep(pre) {
  background: rgba(5, 20, 45, 0.06);
  border: 1px solid rgba(18, 59, 121, 0.14);
  border-radius: 14px;
  padding: 12px 12px;
  overflow: auto;
  margin: 12px 0;
}

.content :deep(code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 0.95em;
}

.empty {
  margin-top: 16px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.74);
  padding: 18px;
  text-align: center;
}
</style>
