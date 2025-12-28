<script setup lang="ts">
export type NewsItem = { id: number; title: string; date: string }

defineProps<{
  title: string
  items: NewsItem[]
}>()

const emit = defineEmits<{
  (e: 'open', id: number): void
  (e: 'more'): void
}>()
</script>

<template>
  <section class="board">
    <div class="head">
      <div class="h2">{{ title }}</div>
      <button class="more" type="button" @click="emit('more')">更多</button>
    </div>
    <ul class="list">
      <li v-for="it in items" :key="it.id" class="item" @click="emit('open', it.id)">
        <span class="bullet" aria-hidden="true" />
        <span class="title">{{ it.title }}</span>
        <span class="date">{{ it.date }}</span>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.board {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 18px 40px rgba(5, 20, 45, 0.06);
  overflow: hidden;
}

.head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px 14px;
  border-bottom: 1px solid var(--line);
  background: linear-gradient(180deg, rgba(245, 247, 251, 0.9), rgba(255, 255, 255, 0.76));
}

.more {
  border: none;
  background: transparent;
  font-size: 12px;
  letter-spacing: 0.4px;
  color: rgba(11, 43, 91, 0.72);
  transition: color 160ms ease;
  cursor: pointer;
}

.more:hover {
  color: rgba(11, 43, 91, 1);
}

.list {
  list-style: none;
  padding: 8px 12px 12px;
  display: grid;
  gap: 8px;
}

.item {
  display: grid;
  grid-template-columns: 10px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 10px 10px;
  border-radius: 12px;
  transition: background 160ms ease;
  cursor: pointer;
}

.item:hover {
  background: rgba(11, 43, 91, 0.05);
}

.bullet {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: rgba(184, 138, 44, 0.92);
  box-shadow: 0 0 0 4px rgba(184, 138, 44, 0.12);
}

.title {
  color: rgba(11, 43, 91, 0.92);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.date {
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.3px;
}
</style>
