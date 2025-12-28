<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { adminCreateBook, adminDeleteBook, adminGetBook, adminPageBooks, adminUpdateBook, type AdminBook } from '@/api/admin'

const keyword = ref('')
const category = ref('')
const status = ref<number | ''>('')
const page = ref(1)
const pageSize = ref(10)

const total = ref(0)
const records = ref<AdminBook[]>([])
const loading = ref(false)
const errorMsg = ref('')

const editorOpen = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const saving = ref(false)
const form = ref<Partial<AdminBook>>({
  id: 0,
  title: '',
  author: '',
  publisher: '',
  isbn: '',
  category: '',
  location: '',
  description: '',
  totalQty: 0,
  status: 1,
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

function borrowedQty(it: AdminBook) {
  const totalQty = it.totalQty ?? 0
  const availableQty = it.availableQty ?? 0
  return Math.max(0, totalQty - availableQty)
}

function statusLabel(v: number) {
  return v === 1 ? '可借' : '停用'
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await adminPageBooks({
      keyword: keyword.value.trim() || undefined,
      category: category.value.trim() || undefined,
      status: status.value === '' ? undefined : Number(status.value),
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

function search() {
  page.value = 1
  load()
}

function openCreate() {
  editorMode.value = 'create'
  editorOpen.value = true
  form.value = {
    title: '',
    author: '',
    publisher: '',
    isbn: '',
    category: '',
    location: '',
    description: '',
    totalQty: 1,
    status: 1,
  }
}

async function openEdit(id: number) {
  editorMode.value = 'edit'
  editorOpen.value = true
  try {
    const detail = await adminGetBook(id)
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
    title: form.value.title,
    author: form.value.author || undefined,
    publisher: form.value.publisher || undefined,
    isbn: form.value.isbn || undefined,
    category: form.value.category || undefined,
    location: form.value.location || undefined,
    description: form.value.description || undefined,
    totalQty: Number(form.value.totalQty ?? 0),
    status: Number(form.value.status ?? 1),
  }

  saving.value = true
  try {
    if (editorMode.value === 'create') {
      await adminCreateBook(payload)
      window.alert('已新增')
    } else {
      await adminUpdateBook(Number(form.value.id), payload)
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
  const ok = window.confirm('确认删除该图书？（存在未归还借阅时将失败）')
  if (!ok) return
  try {
    await adminDeleteBook(id)
    await load()
    window.alert('已删除')
  } catch (e: any) {
    window.alert(e?.message || '删除失败')
  }
}

function prev() {
  page.value = Math.max(1, page.value - 1)
}

function next() {
  page.value = Math.min(totalPages.value, page.value + 1)
}

watch([page, pageSize], () => load())
onMounted(() => load())
</script>

<template>
  <div class="pane">
    <div class="head">
      <div>
        <div class="h1">图书管理</div>
        <div class="muted sub">新增/编辑/删除 · 调整库存与状态</div>
      </div>
      <button class="btn btn-primary" type="button" @click="openCreate">新增图书</button>
    </div>

    <div class="filters">
      <input v-model="keyword" class="input" placeholder="关键词：书名/作者/ISBN" @keydown.enter="search" />
      <input v-model="category" class="input" placeholder="分类（可选）" @keydown.enter="search" />
      <select v-model="status" class="input select">
        <option value="">全部状态</option>
        <option :value="1">可借</option>
        <option :value="0">停用</option>
      </select>
      <button class="btn" type="button" @click="search">查询</button>
    </div>

    <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>

    <div class="table">
      <div class="tr th">
        <div class="td w-title">书名</div>
        <div class="td w-meta">分类</div>
        <div class="td w-meta">库存</div>
        <div class="td w-meta">可借</div>
        <div class="td w-meta">已借</div>
        <div class="td w-meta">状态</div>
        <div class="td w-op">操作</div>
      </div>

      <div v-if="records.length === 0 && !loading" class="empty">
        <div class="h2">暂无数据</div>
        <div class="muted">可点击右上角“新增图书”</div>
      </div>

      <div v-for="it in records" :key="it.id" class="tr">
        <div class="td w-title">
          <div class="t">{{ it.title }}</div>
          <div class="s muted">
            <span v-if="it.author">{{ it.author }}</span>
            <span v-if="it.publisher"> · {{ it.publisher }}</span>
            <span v-if="it.isbn"> · {{ it.isbn }}</span>
          </div>
        </div>
        <div class="td w-meta">{{ it.category || '-' }}</div>
        <div class="td w-meta">{{ it.totalQty }}</div>
        <div class="td w-meta">{{ it.availableQty }}</div>
        <div class="td w-meta">{{ borrowedQty(it) }}</div>
        <div class="td w-meta">
          <span class="pill" :class="{ off: it.status !== 1 }">{{ statusLabel(it.status) }}</span>
        </div>
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
        <div class="h2">{{ editorMode === 'create' ? '新增图书' : '编辑图书' }}</div>
        <button class="btn" type="button" @click="closeEditor">关闭</button>
      </div>

      <div class="editor-body">
        <div class="grid">
          <div class="field">
            <div class="label">状态</div>
            <select v-model.number="form.status" class="input select">
              <option :value="1">可借</option>
              <option :value="0">停用</option>
            </select>
          </div>
          <div class="field">
            <div class="label">总库存</div>
            <input v-model.number="form.totalQty" class="input" type="number" min="0" />
          </div>
          <div class="field">
            <div class="label">分类</div>
            <input v-model="form.category" class="input" placeholder="如：计算机/文学" />
          </div>
          <div class="field">
            <div class="label">馆藏位置</div>
            <input v-model="form.location" class="input" placeholder="如：主馆A区-TP3" />
          </div>
        </div>

        <div class="grid">
          <div class="field">
            <div class="label">书名</div>
            <input v-model="form.title" class="input" placeholder="请输入书名" />
          </div>
          <div class="field">
            <div class="label">作者</div>
            <input v-model="form.author" class="input" placeholder="可选" />
          </div>
        </div>
        <div class="grid">
          <div class="field">
            <div class="label">出版社</div>
            <input v-model="form.publisher" class="input" placeholder="可选" />
          </div>
          <div class="field">
            <div class="label">ISBN</div>
            <input v-model="form.isbn" class="input" placeholder="可选" />
          </div>
        </div>
        <div class="field">
          <div class="label">简介</div>
          <textarea v-model="form.description" class="input textarea" rows="6" placeholder="可选" />
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

.filters {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr 220px 160px auto;
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
  grid-template-columns: 1.6fr 140px 90px 90px 90px 110px 170px;
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
  grid-template-columns: repeat(2, minmax(0, 1fr));
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
    grid-template-columns: 1fr;
  }
  .th {
    display: none;
  }
  .td {
    border-top: 1px dashed rgba(0, 0, 0, 0.06);
  }
  .w-op {
    justify-content: flex-start;
  }
}
</style>

