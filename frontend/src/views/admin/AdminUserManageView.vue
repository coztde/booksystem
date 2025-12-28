<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { adminPageReaders, adminResetReaderPassword, adminUpdateReaderStatus, type AdminReader } from '@/api/admin'
import { formatToMinute } from '@/utils/datetime'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const keyword = ref('')
const status = ref<number | ''>('')
const page = ref(1)
const pageSize = ref(10)

const total = ref(0)
const records = ref<AdminReader[]>([])
const loading = ref(false)
const errorMsg = ref('')
const toast = useToast()
const { confirm } = useConfirm()

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

function statusLabel(v: number) {
  return v === 1 ? '启用' : '禁用'
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await adminPageReaders({
      keyword: keyword.value.trim() || undefined,
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

async function toggleStatus(it: AdminReader) {
  const next = it.status === 1 ? 0 : 1
  const ok = await confirm({
    title: next === 0 ? '确认禁用' : '确认启用',
    message: `确认将该用户设置为“${statusLabel(next)}”？`,
    confirmText: statusLabel(next),
    cancelText: '取消',
    variant: next === 0 ? 'danger' : 'default',
  })
  if (!ok) return
  try {
    await adminUpdateReaderStatus(it.id, next)
    await load()
    toast.success('已更新')
  } catch (e: any) {
    toast.error(e?.message || '更新失败')
  }
}

async function resetPassword(it: AdminReader) {
  const ok = await confirm({
    title: '确认重置密码',
    message: '确认重置该读者密码为：111111aA ？',
    confirmText: '重置',
    cancelText: '取消',
    variant: 'danger',
  })
  if (!ok) return
  try {
    await adminResetReaderPassword(it.id)
    toast.success('已重置（新密码：111111aA）')
  } catch (e: any) {
    toast.error(e?.message || '重置失败')
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
        <div class="h1">用户管理</div>
        <div class="muted sub">读者列表 · 启用/禁用 · 重置密码</div>
      </div>
    </div>

    <div class="filters">
      <input v-model="keyword" class="input" placeholder="关键词：姓名/学号/手机号" @keydown.enter="search" />
      <select v-model="status" class="input select">
        <option value="">全部状态</option>
        <option :value="1">启用</option>
        <option :value="0">禁用</option>
      </select>
      <button class="btn" type="button" @click="search">查询</button>
    </div>

    <div v-if="errorMsg" class="alert">{{ errorMsg }}</div>

    <div class="table">
      <div class="tr th">
        <div class="td w-meta">ID</div>
        <div class="td w-title">读者</div>
        <div class="td w-meta">读者类型</div>
        <div class="td w-meta">手机号</div>
        <div class="td w-meta">注册时间</div>
        <div class="td w-meta">状态</div>
        <div class="td w-op">操作</div>
      </div>

      <div v-if="records.length === 0 && !loading" class="empty">
        <div class="h2">暂无用户</div>
        <div class="muted">读者可在门户“注册”页面创建账号</div>
      </div>

      <div v-for="it in records" :key="it.id" class="tr">
        <div class="td w-meta">{{ it.id }}</div>
        <div class="td w-title">
          <div class="t">{{ it.name }}</div>
          <div class="s muted">{{ it.code }}</div>
        </div>
        <div class="td w-meta">{{ it.readerTypeName || '-' }}</div>
        <div class="td w-meta">{{ it.phone }}</div>
        <div class="td w-meta">{{ formatToMinute(it.createTime) }}</div>
        <div class="td w-meta">
          <span class="pill" :class="{ off: it.status !== 1 }">{{ statusLabel(it.status) }}</span>
        </div>
        <div class="td w-op">
          <button class="btn sm" type="button" @click="toggleStatus(it)">{{ it.status === 1 ? '禁用' : '启用' }}</button>
          <button class="btn sm danger" type="button" @click="resetPassword(it)">重置密码</button>
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
  grid-template-columns: 80px 1.2fr 160px 160px 170px 110px 220px;
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
  gap: 10px;
  flex-wrap: wrap;
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

