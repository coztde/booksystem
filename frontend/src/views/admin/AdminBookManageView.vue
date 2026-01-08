<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  adminCreateBook,
  adminDeleteBook,
  adminGetBook,
  adminPageBooks,
  adminUpdateBook,
  adminUploadBookCover,
  type AdminBook,
} from '@/api/admin'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const keyword = ref('')
const category = ref('')
const status = ref<number | ''>('')
const page = ref(1)
const pageSize = ref(10)

const total = ref(0)
const records = ref<AdminBook[]>([])
const loading = ref(false)
const errorMsg = ref('')
const toast = useToast()
const { confirm } = useConfirm()

const editorOpen = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const activeRowId = ref<number | null>(null)
const detailLoading = ref(false)
const saving = ref(false)
const uploadingCover = ref(false)
const fileEl = ref<HTMLInputElement | null>(null)
const form = ref<Partial<AdminBook>>({
  id: 0,
  coverUrl: '',
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
    if (editorOpen.value && editorMode.value === 'edit' && activeRowId.value != null) {
      const stillExists = records.value.some((it) => it.id === activeRowId.value)
      if (!stillExists) {
        closeEditor()
      }
    }
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
  if (editorOpen.value && editorMode.value === 'create') {
    closeEditor()
    return
  }
  editorMode.value = 'create'
  editorOpen.value = true
  activeRowId.value = null
  detailLoading.value = false
  form.value = {
    coverUrl: '',
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
  if (editorOpen.value && editorMode.value === 'edit' && activeRowId.value === id) {
    closeEditor()
    return
  }
  editorMode.value = 'edit'
  editorOpen.value = true
  activeRowId.value = id
  detailLoading.value = true
  form.value = {
    id,
    coverUrl: '',
    title: '',
    author: '',
    publisher: '',
    isbn: '',
    category: '',
    location: '',
    description: '',
    totalQty: 0,
    status: 1,
  }
  try {
    const detail = await adminGetBook(id)
    form.value = { ...detail }
  } catch (e: any) {
    editorOpen.value = false
    activeRowId.value = null
    toast.error(e?.message || '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

function closeEditor() {
  editorOpen.value = false
  activeRowId.value = null
  detailLoading.value = false
}

async function save() {
  const payload = {
    coverUrl: form.value.coverUrl || undefined,
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
      toast.success('已新增')
    } else {
      await adminUpdateBook(Number(form.value.id), payload)
      toast.success('已保存')
    }
    editorOpen.value = false
    activeRowId.value = null
    await load()
  } catch (e: any) {
    toast.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(id: number) {
  const ok = await confirm({
    title: '确认删除',
    message: '确认删除该图书？（存在未归还借阅时将失败）',
    confirmText: '删除',
    cancelText: '取消',
    variant: 'danger',
  })
  if (!ok) return
  try {
    await adminDeleteBook(id)
    if (activeRowId.value === id) {
      closeEditor()
    }
    await load()
    toast.success('已删除')
  } catch (e: any) {
    toast.error(e?.message || '删除失败')
  }
}

function pickCover() {
  if (uploadingCover.value) return
  fileEl.value?.click()
}

function clearCover() {
  form.value.coverUrl = ''
  if (fileEl.value) fileEl.value.value = ''
}

async function onCoverChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    toast.error('请选择图片文件')
    input.value = ''
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    toast.error('图片过大，请选择 5MB 以内的文件')
    input.value = ''
    return
  }

  uploadingCover.value = true
  try {
    const url = await adminUploadBookCover(file)
    form.value.coverUrl = url
    toast.success('封面已上传')
  } catch (err: any) {
    toast.error(err?.message || '上传失败')
    input.value = ''
  } finally {
    uploadingCover.value = false
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

    <div v-if="editorOpen && editorMode === 'create'" class="create-host">
      <div class="editor create">
        <div class="editor-head">
          <div class="h2">{{ editorMode === 'create' ? '新增图书' : '编辑图书' }}</div>
          <button class="btn" type="button" @click="closeEditor">关闭</button>
        </div>

        <div class="editor-body">
          <div class="cover-edit">
            <div class="label">封面</div>
            <div class="cover-edit-body">
              <div class="cover large">
                <img v-if="form.coverUrl" :src="form.coverUrl" alt="" />
                <div v-else class="cover-fallback large" aria-hidden="true">{{ (form.title || '图').slice(0, 1) }}</div>
              </div>
              <div class="cover-actions">
                <input ref="fileEl" class="file" type="file" accept="image/*" @change="onCoverChange" />
                <div class="row">
                  <button class="btn" type="button" :disabled="uploadingCover" @click="pickCover">
                    {{ uploadingCover ? '上传中…' : '选择图片' }}
                  </button>
                  <button class="btn" type="button" @click="clearCover">清除</button>
                </div>
                <input v-model="form.coverUrl" class="input" placeholder="或粘贴图片 URL（可选）" />
                <div class="muted tip">建议上传 5MB 内图片（JPG/PNG/WebP），将存入阿里云 OSS</div>
              </div>
            </div>
          </div>

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
          <div class="title-row">
            <div class="cover">
              <img v-if="it.coverUrl" :src="it.coverUrl" alt="" loading="lazy" />
              <div v-else class="cover-fallback" aria-hidden="true">{{ (it.title || '图').slice(0, 1) }}</div>
            </div>
            <div class="title-main">
              <div class="t">{{ it.title }}</div>
              <div class="s muted">
                <span v-if="it.author">{{ it.author }}</span>
                <span v-if="it.publisher"> · {{ it.publisher }}</span>
                <span v-if="it.isbn"> · {{ it.isbn }}</span>
              </div>
            </div>
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

        <div v-if="editorOpen && editorMode === 'edit' && activeRowId === it.id" class="editor-cell">
          <div class="editor inline">
            <div class="editor-head">
              <div class="h2">编辑图书</div>
              <button class="btn" type="button" @click="closeEditor">关闭</button>
            </div>

            <div class="editor-body">
              <div v-if="detailLoading" class="muted loading-tip">加载中...</div>

              <div class="cover-edit">
                <div class="label">封面</div>
                <div class="cover-edit-body">
                  <div class="cover large">
                    <img v-if="form.coverUrl" :src="form.coverUrl" alt="" />
                    <div v-else class="cover-fallback large" aria-hidden="true">{{ (form.title || '图').slice(0, 1) }}</div>
                  </div>
                  <div class="cover-actions">
                    <input ref="fileEl" class="file" type="file" accept="image/*" @change="onCoverChange" />
                    <div class="row">
                      <button class="btn" type="button" :disabled="detailLoading || uploadingCover" @click="pickCover">
                        {{ uploadingCover ? '上传中…' : '选择图片' }}
                      </button>
                      <button class="btn" type="button" :disabled="detailLoading" @click="clearCover">清除</button>
                    </div>
                    <input v-model="form.coverUrl" class="input" placeholder="或粘贴图片 URL（可选）" />
                    <div class="muted tip">建议上传 5MB 内图片（JPG/PNG/WebP），将存入阿里云 OSS</div>
                  </div>
                </div>
              </div>

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
                <button class="btn btn-primary" type="button" :disabled="saving || detailLoading" @click="save">
                  {{ saving ? '保存中..' : '保存' }}
                </button>
                <button class="btn" type="button" @click="closeEditor">取消</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="pager">
      <button class="btn" type="button" :disabled="page <= 1" @click="prev">上一页</button>
      <div class="muted">第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 条</div>
      <button class="btn" type="button" :disabled="page >= totalPages" @click="next">下一页</button>
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

.create-host {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.editor.create {
  width: min(980px, 100%);
  margin-top: 0;
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

.editor-cell {
  grid-column: 1 / -1;
  padding: 0;
}

.editor.inline {
  margin: 12px;
}

.loading-tip {
  padding: 8px 0;
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

.title-row {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 10px;
  align-items: center;
  width: 100%;
}

.title-main {
  min-width: 0;
}

.t {
  color: rgba(11, 43, 91, 0.92);
  font-weight: 650;
}

.s {
  font-size: 12px;
}

.cover {
  width: 44px;
  height: 56px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(11, 43, 91, 0.14);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 12px 26px rgba(5, 20, 45, 0.1);
  display: grid;
  place-items: center;
}

.cover.large {
  width: 84px;
  height: 108px;
  border-radius: 16px;
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
  color: rgba(11, 43, 91, 0.82);
  background: radial-gradient(120% 120% at 30% 20%, rgba(18, 59, 121, 0.18) 0%, rgba(18, 59, 121, 0.06) 55%, rgba(255, 255, 255, 0) 100%);
}

.cover-fallback.large {
  font-size: 26px;
}

.cover-edit {
  display: grid;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.78);
}

.cover-edit-body {
  display: grid;
  grid-template-columns: 92px 1fr;
  gap: 12px;
  align-items: start;
}

.cover-actions {
  display: grid;
  gap: 10px;
}

.cover-actions .row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.file {
  display: none;
}

.tip {
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
