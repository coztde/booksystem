<script setup lang="ts">
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
        <div class="content">{{ post.content || '' }}</div>
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
  white-space: pre-line;
  color: rgba(11, 43, 91, 0.88);
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
