<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { adminBorrowOut, adminPageBorrows, adminReturnBorrow, type AdminBorrowRecord } from '@/api/admin'
import { formatToMinute } from '@/utils/datetime'
import { useToast } from '@/composables/useToast'

const keyword = ref('')
const status = ref<number | ''>('')
const page = ref(1)
const pageSize = ref(10)

const total = ref(0)
const records = ref<AdminBorrowRecord[]>([])
const loading = ref(false)
const errorMsg = ref('')

const borrowUserCode = ref('')
const borrowBookId = ref<number | null>(null)
const toast = useToast()

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

function statusLabel(v: number) {
  if (v === 2) return '逾期'
  if (v === 1) return '已还'
  return '借阅中'
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await adminPageBorrows({
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

function search() {
  page.value = 1
  load()
}

async function borrowOut() {
  const code = borrowUserCode.value.trim()
  const bookId = Number(borrowBookId.value)
  if (!code) {
    toast.info('请输入读者学号/工号')
    return
  }
  if (!bookId) {
    toast.info('请输入图书ID')
    return
  }
  try {
    await adminBorrowOut(code, bookId)
    borrowUserCode.value = ''
    borrowBookId.value = null
    await load()
    toast.success('借出成功')
  } catch (e: any) {
    toast.error(e?.message || '借出失败')
  }
}

async function returnBook(recordId: number) {
  const fineText = window.prompt('输入罚款金额（可选，留空为 0）', '')
  const fine = fineText && fineText.trim() ? Number(fineText.trim()) : undefined
  try {
    await adminReturnBorrow(recordId, fine)
    await load()
    toast.success('已归还')
  } catch (e: any) {
    toast.error(e?.message || '归还失败')
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
        <div class="h1">借阅管理</div>
        <div class="muted sub">查询借阅记录 · 管理员借出/归还</div>
      </div>
    </div>

    <div class="borrow-box">
      <div class="h2">借出</div>
      <div class="borrow-form">
        <input v-model="borrowUserCode" class="input" placeholder="读者学号/工号" />
        <input v-model.number="borrowBookId" class="input" type="number" min="1" placeholder="图书ID" />
        <button class="btn btn-primary" type="button" @click="borrowOut">借出</button>
      </div>
      <div class="muted hint">提示：图书ID可在“图书管理”列表中查看；借出会扣减可借库存并生成借阅记录。</div>
    </div>

    <div class="filters">
      <input v-model="keyword" class="input" placeholder="关键词：读者姓名/学号/书名" @keydown.enter="search" />
      <select v-model="status" class="input select">
        <option value="">全部状态</option>
        <option :value="0">借阅中</option>
        <option :value="2">逾期</option>
        <option :value="1">已还</option>
      </select>
      <button class="btn" type="button" @click="search">查询</button>
    </div>

    <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>

    <div class="table">
      <div class="tr th">
        <div class="td w-meta">记录ID</div>
        <div class="td w-title">读者</div>
        <div class="td w-title">图书</div>
        <div class="td w-meta">借出</div>
        <div class="td w-meta">应还</div>
        <div class="td w-meta">归还</div>
        <div class="td w-meta">状态</div>
        <div class="td w-meta">罚款</div>
        <div class="td w-op">操作</div>
      </div>

      <div v-if="records.length === 0 && !loading" class="empty">
        <div class="h2">暂无记录</div>
        <div class="muted">可在上方“借出”创建记录，或调整筛选条件</div>
      </div>

      <div v-for="it in records" :key="it.recordId" class="tr">
        <div class="td w-meta">{{ it.recordId }}</div>
        <div class="td w-title">
          <div class="t">{{ it.userName }}</div>
          <div class="s muted">{{ it.userCode }}</div>
        </div>
        <div class="td w-title">
          <div class="t">{{ it.bookTitle }}</div>
          <div class="s muted">ID: {{ it.bookId }}</div>
        </div>
        <div class="td w-meta">{{ formatToMinute(it.borrowAt) }}</div>
        <div class="td w-meta">{{ formatToMinute(it.dueAt) }}</div>
        <div class="td w-meta">{{ it.returnAt ? formatToMinute(it.returnAt) : '-' }}</div>
        <div class="td w-meta">
          <span class="pill" :class="{ warn: it.status === 2, ok: it.status === 1 }">{{ statusLabel(it.status) }}</span>
        </div>
        <div class="td w-meta">{{ it.fineAmount ?? 0 }}</div>
        <div class="td w-op">
          <button v-if="!it.returnAt" class="btn sm btn-primary" type="button" @click="returnBook(it.recordId)">归还</button>
          <span v-else class="muted">已归还{{ it.handledByName ? `（${it.handledByName}）` : '' }}</span>
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

.borrow-box {
  margin-top: 12px;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 12px;
  background: rgba(245, 247, 251, 0.5);
}

.borrow-form {
  margin-top: 10px;
  display: grid;
  grid-template-columns: 1fr 160px auto;
  gap: 10px;
  align-items: center;
}

.hint {
  margin-top: 8px;
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
  grid-template-columns: 90px 170px 1.2fr 170px 170px 170px 110px 90px 180px;
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

.pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(120, 120, 120, 0.22);
  background: rgba(120, 120, 120, 0.06);
  font-size: 12px;
}

.pill.warn {
  border-color: rgba(184, 138, 44, 0.38);
  background: rgba(184, 138, 44, 0.08);
}

.pill.ok {
  border-color: rgba(44, 184, 94, 0.28);
  background: rgba(44, 184, 94, 0.08);
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

@media (max-width: 1200px) {
  .borrow-form {
    grid-template-columns: 1fr;
  }
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

