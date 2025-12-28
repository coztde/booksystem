<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  adminCreatePost,
  adminDeletePost,
  adminGetPost,
  adminPagePosts,
  adminUpdatePost,
  type AdminPortalPostDetail,
  type AdminPortalPostListItem,
} from '@/api/admin'
import { formatToMinute } from '@/utils/datetime'

const route = useRoute()
const router = useRouter()

const type = computed(() => Number(route.query.type || 2))

const keyword = ref('')
const status = ref<number | ''>('')
const page = ref(1)
const pageSize = ref(10)

const total = ref(0)
const records = ref<AdminPortalPostListItem[]>([])
const loading = ref(false)
const errorMsg = ref('')

const editorOpen = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const saving = ref(false)
const form = ref<AdminPortalPostDetail>({
  id: 0,
  type: type.value,
  title: '',
  status: 1,
  tag: '',
  subtitle: '',
  content: '',
  coverUrl: '',
  accent: '',
  sort: 0,
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

function typeLabel(t: number) {
  if (t === 1) return '轮播'
  if (t === 3) return '公告'
  return '新闻'
}

function statusLabel(v: number) {
  return v === 1 ? '上线' : '下线'
}

function switchType(next: number) {
  router.push({ path: '/admin/posts', query: { type: String(next) } })
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await adminPagePosts({
      type: type.value,
      status: status.value === '' ? undefined : Number(status.value),
      keyword: keyword.value.trim() || undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    total.value = data.total
    records.value = data.records
  } catch (e: any) {
    errorMsg.value = e?.message || '加载失败'
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editorMode.value = 'create'
  editorOpen.value = true
  form.value = {
    id: 0,
    type: type.value,
    title: '',
    status: 1,
    tag: '',
    subtitle: '',
    content: '',
    coverUrl: '',
    accent: '',
    sort: 0,
  }
}

async function openEdit(id: number) {
  editorMode.value = 'edit'
  editorOpen.value = true
  try {
    const detail = await adminGetPost(id)
    form.value = { ...detail }
  } catch (e: any) {
    editorOpen.value = false
    window.alert(e?.message || '加载详情失败')
  }
}

function closeEditor() {
  editorOpen.value = false
}

async function save() {
  const payload = {
    type: form.value.type,
    tag: form.value.tag || undefined,
    title: form.value.title,
    subtitle: form.value.subtitle || undefined,
    content: form.value.content || undefined,
    coverUrl: form.value.coverUrl || undefined,
    accent: form.value.accent || undefined,
    sort: form.value.sort || 0,
    status: form.value.status,
  }

  saving.value = true
  try {
    if (editorMode.value === 'create') {
      await adminCreatePost(payload)
      window.alert('已新增')
    } else {
      await adminUpdatePost(form.value.id, payload)
      window.alert('已保存')
    }
    editorOpen.value = false
    await load()
  } catch (e: any) {
    window.alert(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(id: number) {
  const ok = window.confirm('确认删除该内容？')
  if (!ok) return
  try {
    await adminDeletePost(id)
    await load()
    window.alert('已删除')
  } catch (e: any) {
    window.alert(e?.message || '删除失败')
  }
}

function search() {
  page.value = 1
  load()
}

function prev() {
  page.value = Math.max(1, page.value - 1)
}

function next() {
  page.value = Math.min(totalPages.value, page.value + 1)
}

watch(type, () => {
  page.value = 1
  status.value = ''
  keyword.value = ''
  load()
})
watch([page, pageSize], () => load())

onMounted(() => load())
</script>

<template>
  <div class="pane">
    <div class="head">
      <div>
        <div class="h1">内容管理</div>
        <div class="muted sub">轮播 / 新闻动态 / 通知公告</div>
      </div>
      <button class="btn btn-primary" type="button" @click="openCreate">新增内容</button>
    </div>

    <div class="tabs">
      <button class="btn" type="button" :class="{ active: type === 1 }" @click="switchType(1)">轮播</button>
      <button class="btn" type="button" :class="{ active: type === 2 }" @click="switchType(2)">新闻</button>
      <button class="btn" type="button" :class="{ active: type === 3 }" @click="switchType(3)">公告</button>
    </div>

    <div class="filters">
      <input v-model="keyword" class="input" placeholder="关键词：标题/摘要" @keydown.enter="search" />
      <select v-model="status" class="input select">
        <option value="">全部状态</option>
        <option :value="1">上线</option>
        <option :value="0">下线</option>
      </select>
      <button class="btn" type="button" @click="search">查询</button>
    </div>

    <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>

    <div class="table">
      <div class="tr th">
        <div class="td w-title">标题</div>
        <div class="td w-meta">类型</div>
        <div class="td w-meta">发布时间</div>
        <div class="td w-meta">状态</div>
        <div class="td w-meta">排序</div>
        <div class="td w-op">操作</div>
      </div>

      <div v-if="records.length === 0 && !loading" class="empty">
        <div class="h2">暂无数据</div>
        <div class="muted">可点击右上角“新增内容”</div>
      </div>

      <div v-for="it in records" :key="it.id" class="tr">
        <div class="td w-title">
          <div class="t">{{ it.title }}</div>
          <div v-if="it.subtitle" class="s muted">{{ it.subtitle }}</div>
        </div>
        <div class="td w-meta">{{ typeLabel(it.type) }}</div>
        <div class="td w-meta">{{ formatToMinute(it.publishTime) }}</div>
        <div class="td w-meta">
          <span class="pill" :class="{ off: it.status !== 1 }">{{ statusLabel(it.status) }}</span>
        </div>
        <div class="td w-meta">{{ it.sort ?? 0 }}</div>
        <div class="td w-op">
          <button class="btn sm" type="button" @click="openEdit(it.id)">编辑</button>
          <button class="btn sm danger" type="button" @click="remove(it.id)">删除</button>
        </div>
      </div>
    </div>

    <div class="pager">
      <button class="btn" type="button" :disabled="page <= 1" @click="prev">上一页</button>
      <div class="muted">第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 条</div>
      <button class="btn" type="button" :disabled="page >= totalPages" @click="next">下一页</button>
    </div>

    <div v-if="editorOpen" class="editor">
      <div class="editor-head">
        <div class="h2">{{ editorMode === 'create' ? '新增内容' : '编辑内容' }}</div>
        <button class="btn" type="button" @click="closeEditor">关闭</button>
      </div>

      <div class="editor-body">
        <div class="grid">
          <div class="field">
            <div class="label">类型</div>
            <select v-model.number="form.type" class="input select">
              <option :value="1">轮播</option>
              <option :value="2">新闻</option>
              <option :value="3">公告</option>
            </select>
          </div>
          <div class="field">
            <div class="label">状态</div>
            <select v-model.number="form.status" class="input select">
              <option :value="1">上线</option>
              <option :value="0">下线</option>
            </select>
          </div>
          <div class="field">
            <div class="label">排序（越小越靠前）</div>
            <input v-model.number="form.sort" class="input" type="number" min="0" />
          </div>
          <div class="field">
            <div class="label">标签（轮播可用）</div>
            <input v-model="form.tag" class="input" placeholder="如：活动/通知" />
          </div>
        </div>

        <div class="field">
          <div class="label">标题</div>
          <input v-model="form.title" class="input" placeholder="请输入标题" />
        </div>
        <div class="field">
          <div class="label">摘要/副标题</div>
          <input v-model="form.subtitle" class="input" placeholder="列表摘要/轮播说明（可选）" />
        </div>
        <div class="grid">
          <div class="field">
            <div class="label">封面图 URL（可选）</div>
            <input v-model="form.coverUrl" class="input" placeholder="https://..." />
          </div>
          <div class="field">
            <div class="label">强调色（可选）</div>
            <input v-model="form.accent" class="input" placeholder="#0B2B5B" />
          </div>
        </div>
        <div class="field">
          <div class="label">正文</div>
          <textarea v-model="form.content" class="input textarea" rows="8" placeholder="支持纯文本/HTML" />
        </div>

        <div class="editor-actions">
          <button class="btn btn-primary" type="button" :disabled="saving" @click="save">
            {{ saving ? '保存中..' : '保存' }}
          </button>
          <button class="btn" type="button" @click="closeEditor">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pane {
  padding: 16px;
}

.head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-end;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
}

.sub {
  margin-top: 8px;
}

.tabs {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn.active {
  border-color: rgba(18, 59, 121, 0.34);
  background: rgba(18, 59, 121, 0.06);
}

.filters {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr 180px auto;
  gap: 10px;
  align-items: center;
}

.select {
  appearance: none;
}

.alert {
  margin-top: 12px;
  border: 1px dashed rgba(184, 138, 44, 0.38);
  background: rgba(184, 138, 44, 0.06);
  padding: 10px 12px;
  border-radius: 14px;
  color: rgba(11, 43, 91, 0.86);
}

.table {
  margin-top: 12px;
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
}

.tr {
  display: grid;
  grid-template-columns: 1.5fr 120px 170px 120px 90px 170px;
  gap: 0;
  border-top: 1px solid var(--line);
}

.tr:first-child {
  border-top: none;
}

.th {
  background: rgba(245, 247, 251, 0.7);
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.4px;
}

.td {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.w-title {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.t {
  color: rgba(11, 43, 91, 0.92);
  font-weight: 650;
}

.s {
  font-size: 12px;
}

.w-op {
  justify-content: flex-end;
}

.btn.sm {
  padding: 8px 10px;
  border-radius: 10px;
}

.btn.danger {
  border-color: rgba(184, 44, 44, 0.22);
  background: rgba(184, 44, 44, 0.06);
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(18, 59, 121, 0.22);
  background: rgba(18, 59, 121, 0.06);
  font-size: 12px;
}

.pill.off {
  border-color: rgba(120, 120, 120, 0.22);
  background: rgba(120, 120, 120, 0.06);
}

.empty {
  padding: 16px;
}

.pager {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.editor {
  margin-top: 14px;
  border: 1px solid var(--line);
  border-radius: 14px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
}

.editor-head {
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid var(--line);
  background: rgba(245, 247, 251, 0.7);
}

.editor-body {
  padding: 14px;
  display: grid;
  gap: 12px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.field {
  display: grid;
  gap: 6px;
}

.label {
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.4px;
}

.textarea {
  resize: vertical;
}

.editor-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

@media (max-width: 1100px) {
  .filters {
    grid-template-columns: 1fr;
  }
  .tr {
    grid-template-columns: 1.6fr 110px 160px 110px 80px 160px;
  }
  .grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 720px) {
  .tr {
    grid-template-columns: 1fr;
  }
  .th {
    display: none;
  }
  .td {
    padding: 10px 12px;
    border-top: 1px dashed rgba(0, 0, 0, 0.06);
  }
  .w-op {
    justify-content: flex-start;
  }
}
</style>

