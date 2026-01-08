<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'

export type Slide = {
  id: number
  tag: string
  title: string
  subtitle: string
  coverUrl?: string
  accent?: string
}

const props = defineProps<{
  slides: Slide[]
}>()

const active = ref(0)
const timer = ref<number | null>(null)

const current = computed(() => props.slides[active.value])

const emit = defineEmits<{
  (e: 'open', id: number): void
}>()

function next() {
  if (props.slides.length === 0) return
  active.value = (active.value + 1) % props.slides.length
}

function start() {
  stop()
  if (props.slides.length <= 1) return
  timer.value = window.setInterval(next, 5200)
}

function stop() {
  if (timer.value !== null) {
    window.clearInterval(timer.value)
    timer.value = null
  }
}

onMounted(() => start())
onUnmounted(() => stop())
</script>

<template>
  <section class="carousel" @mouseenter="stop" @mouseleave="start">
    <div class="frame">
      <button
        class="paper"
        type="button"
        :style="{ '--accent': current?.accent || 'rgba(184, 138, 44, 1)' }"
        :disabled="!current"
        @click="current && emit('open', current.id)"
      >
        <div v-if="current?.coverUrl" class="cover" :style="{ backgroundImage: `url(${current.coverUrl})` }" aria-hidden="true" />
        <div class="tag">{{ current?.tag }}</div>
        <div class="title">{{ current?.title }}</div>
        <div class="subtitle">{{ current?.subtitle }}</div>
        <div class="ornament" aria-hidden="true">
          <span />
          <span />
          <span />
        </div>
      </button>
    </div>

    <div class="dots">
      <button
        v-for="(s, idx) in slides"
        :key="s.title"
        class="dot"
        :class="{ active: idx === active }"
        type="button"
        @click="active = idx"
      >
        <span class="sr-only">切换到第{{ idx + 1 }}张</span>
      </button>
    </div>
  </section>
</template>

<style scoped>
.carousel {
  display: grid;
  gap: 14px;
}

.frame {
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  border: 1px solid rgba(18, 59, 121, 0.12);
}

.paper {
  position: relative;
  min-height: 240px;
  padding: 22px 22px 18px;
  width: 100%;
  text-align: left;
  overflow: hidden;
  background: radial-gradient(800px 260px at 10% 20%, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7) 46%, rgba(255, 255, 255, 0.55) 100%),
    radial-gradient(900px 380px at 85% 0%, rgba(18, 59, 121, 0.22), transparent 55%),
    linear-gradient(135deg, rgba(11, 43, 91, 1), rgba(18, 59, 121, 1));
  color: rgba(255, 255, 255, 0.92);
  border: none;
  cursor: pointer;
  transition: transform 180ms ease, filter 180ms ease;
}

.cover {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  opacity: 0.36;
  filter: saturate(1.05) contrast(1.05);
  transform: scale(1.03);
  pointer-events: none;
  z-index: 0;
}

.paper:disabled {
  cursor: default;
  opacity: 0.9;
}

.paper:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: saturate(1.03);
}

.paper::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.08) 1px, transparent 1px);
  background-size: 26px 26px;
  opacity: 0.34;
  pointer-events: none;
  z-index: 1;
}

.paper > :not(.cover) {
  position: relative;
  z-index: 2;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  letter-spacing: 0.6px;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.1);
  width: fit-content;
}

.title {
  margin-top: 14px;
  font-family: var(--font-serif);
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 22px;
  line-height: 1.24;
}

.subtitle {
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.86);
  font-size: 14px;
  line-height: 1.55;
}

.ornament {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  opacity: 0.9;
}

.ornament span {
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.8), rgba(255, 255, 255, 0.1));
}

.ornament span:nth-child(1) {
  width: 40%;
}

.ornament span:nth-child(2) {
  width: 24%;
  background: linear-gradient(90deg, var(--accent), rgba(255, 255, 255, 0.12));
}

.ornament span:nth-child(3) {
  width: 14%;
}

.dots {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  border: 1px solid rgba(11, 43, 91, 0.28);
  background: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: transform 160ms ease, background 160ms ease;
}

.dot.active {
  background: rgba(11, 43, 91, 0.86);
  transform: scale(1.15);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
</style>
