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
    <div class="cover-wrap" role="button" tabindex="0" @click="emit('view', book.id)" @keydown.enter="emit('view', book.id)">
      <img v-if="book.coverUrl" :src="book.coverUrl" alt="" loading="lazy" />
      <div v-else class="cover-fallback" aria-hidden="true">{{ (book.title || '图').slice(0, 1) }}</div>
      <div v-if="book.category" class="badge">{{ book.category }}</div>
    </div>

    <div class="body">
      <div class="title">{{ book.title }}</div>
      <div class="sub muted">
        <span class="sub-item">{{ book.author || '—' }}</span>
        <span class="dot">·</span>
        <span class="sub-item">{{ book.publisher || '—' }}</span>
      </div>

      <div class="meta">
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
    </div>
  </article>
</template>

<style scoped>
.card {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.78);
  overflow: hidden;
  display: grid;
  gap: 0;
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 220ms ease;
}

.card:hover {
  transform: translateY(-2px);
  border-color: rgba(18, 59, 121, 0.22);
  box-shadow: 0 22px 50px rgba(5, 20, 45, 0.12);
}

.cover-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 4;
  background: rgba(245, 247, 251, 0.86);
  cursor: pointer;
  border-bottom: 1px solid rgba(11, 43, 91, 0.1);
}

.cover-wrap:hover {
  filter: saturate(1.02);
}

.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-wrap::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(5, 20, 45, 0) 58%, rgba(5, 20, 45, 0.42) 100%);
  pointer-events: none;
}

.cover-fallback {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  font-family: var(--font-serif);
  font-size: 56px;
  color: rgba(11, 43, 91, 0.82);
  background: radial-gradient(120% 120% at 30% 20%, rgba(18, 59, 121, 0.18) 0%, rgba(18, 59, 121, 0.06) 55%, rgba(255, 255, 255, 0) 100%);
}

.badge {
  position: absolute;
  left: 10px;
  top: 10px;
  padding: 5px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.16);
  color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px) saturate(120%);
  font-size: 12px;
  letter-spacing: 0.2px;
}

.body {
  padding: 14px 14px 12px;
  display: grid;
  gap: 10px;
}

.title {
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.3px;
  color: rgba(11, 43, 91, 0.96);
  line-height: 1.25;
}

.sub {
  font-size: 12px;
  letter-spacing: 0.2px;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.sub-item {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 20ch;
}

.dot {
  opacity: 0.6;
}

.meta {
  display: grid;
  gap: 6px;
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
