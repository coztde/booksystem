<script setup lang="ts">
import type { Book } from '@/api/library'

const props = defineProps<{
  book: Book
}>()

const emit = defineEmits<{
  (e: 'view', id: number): void
  (e: 'borrow', id: number): void
}>()

const available = () => Number(props.book.availableQty ?? 0)
</script>

<template>
  <article class="card">
    <div class="top">
      <div class="title">{{ book.title }}</div>
      <div v-if="book.category" class="pill">{{ book.category }}</div>
    </div>

    <div class="meta">
      <div class="row"><span class="k">作者</span><span class="v">{{ book.author || '—' }}</span></div>
      <div class="row"><span class="k">出版社</span><span class="v">{{ book.publisher || '—' }}</span></div>
      <div class="row"><span class="k">馆藏位置</span><span class="v">{{ book.location || '—' }}</span></div>
      <div class="row">
        <span class="k">可借</span>
        <span class="v strong">{{ book.availableQty ?? 0 }}</span>
        <span class="k split">总量</span>
        <span class="v">{{ book.totalQty ?? 0 }}</span>
      </div>
    </div>

    <div class="actions">
      <button class="btn" type="button" @click="emit('view', book.id)">查看详情</button>
      <button class="btn btn-primary" type="button" :disabled="available() <= 0" @click="emit('borrow', book.id)">
        借阅
      </button>
    </div>
  </article>
</template>

<style scoped>
.card {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  padding: 16px;
  display: grid;
  gap: 12px;
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 220ms ease;
}

.card:hover {
  transform: translateY(-2px);
  border-color: rgba(18, 59, 121, 0.22);
  box-shadow: 0 22px 50px rgba(5, 20, 45, 0.12);
}

.top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.title {
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.3px;
  color: rgba(11, 43, 91, 0.96);
  line-height: 1.25;
}

.pill {
  flex: none;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(18, 59, 121, 0.18);
  background: rgba(18, 59, 121, 0.06);
  color: rgba(11, 43, 91, 0.8);
}

.meta {
  display: grid;
  gap: 6px;
  padding-top: 2px;
}

.row {
  display: flex;
  gap: 10px;
  align-items: baseline;
}

.k {
  width: 64px;
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.4px;
}

.k.split {
  width: auto;
  margin-left: 10px;
}

.v {
  color: rgba(11, 43, 91, 0.86);
}

.v.strong {
  color: rgba(11, 43, 91, 1);
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.actions .btn[disabled] {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
</style>
