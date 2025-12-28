<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listPortalPosts, type PortalPostListItem } from '@/api/library'
import { formatToMinute } from '@/utils/datetime'

const route = useRoute()
const router = useRouter()

const type = computed(() => Number(route.query.type || 2))
const title = computed(() => (type.value === 3 ? '通知公告' : '新闻动态'))

const items = ref<PortalPostListItem[]>([])
const loading = ref(false)
const errorMsg = ref('')

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    items.value = await listPortalPosts(type.value, 50)
  } catch (e: any) {
    errorMsg.value = e?.message || '内容加载失败'
    items.value = []
  } finally {
    loading.value = false
  }
}

function switchType(next: number) {
  router.push({ path: '/posts', query: { type: String(next) } })
}

onMounted(() => load())
watch(type, () => load())
</script>

<template>
  <div class="section">
    <div class="container">
      <div class="head">
        <div>
          <div class="h1">{{ title }}</div>
          <div class="muted sub">内容来自数据库 `portal_post`</div>
        </div>
        <div class="tabs">
          <button class="btn" type="button" :class="{ active: type === 2 }" @click="switchType(2)">新闻</button>
          <button class="btn" type="button" :class="{ active: type === 3 }" @click="switchType(3)">公告</button>
        </div>
      </div>

      <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>

      <div class="list">
        <button
          v-for="it in items"
          :key="it.id"
          class="item"
          type="button"
          @click="router.push(`/posts/${it.id}`)"
        >
          <div class="t">{{ it.title }}</div>
          <div class="meta">
            <span class="m">{{ formatToMinute(it.publishTime) }}</span>
            <span v-if="it.subtitle" class="m muted">· {{ it.subtitle }}</span>
          </div>
        </button>
      </div>

      <div v-if="!loading && items.length === 0 && !errorMsg" class="empty">
        <div class="h2">暂无内容</div>
        <div class="muted">请先执行 `backend/sql/book_seed.sql` 初始化门户内容。</div>
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

.tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn.active {
  border-color: rgba(18, 59, 121, 0.34);
  background: rgba(18, 59, 121, 0.06);
}

.alert {
  margin-top: 16px;
  border: 1px dashed rgba(184, 138, 44, 0.38);
  background: rgba(184, 138, 44, 0.06);
  padding: 10px 12px;
  border-radius: 14px;
  color: rgba(11, 43, 91, 0.86);
}

.list {
  margin-top: 16px;
  display: grid;
  gap: 10px;
}

.item {
  text-align: left;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  padding: 14px;
  cursor: pointer;
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 220ms ease;
}

.item:hover {
  transform: translateY(-1px);
  border-color: rgba(18, 59, 121, 0.22);
  box-shadow: 0 22px 50px rgba(5, 20, 45, 0.12);
}

.t {
  font-family: var(--font-serif);
  font-weight: 700;
  color: rgba(11, 43, 91, 0.96);
  letter-spacing: 0.2px;
}

.meta {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: var(--muted-2);
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
