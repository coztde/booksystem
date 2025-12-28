<script setup lang="ts">
import { computed } from 'vue'
import { useToastStore } from '@/stores/toast'

const toastStore = useToastStore()
const items = computed(() => toastStore.items)
</script>

<template>
  <div class="toast-layer" aria-live="polite" aria-relevant="additions removals">
    <TransitionGroup name="toast" tag="div" class="toast-stack">
      <div v-for="it in items" :key="it.id" class="toast" :class="it.type" role="status">
        <span class="icon" aria-hidden="true">
          <svg v-if="it.type === 'success'" viewBox="0 0 24 24" fill="none">
            <path
              d="M20 6L9 17l-5-5"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <svg v-else-if="it.type === 'error'" viewBox="0 0 24 24" fill="none">
            <path
              d="M18 6L6 18M6 6l12 12"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none">
            <path
              d="M12 8.25v.5M12 11v5"
              stroke="currentColor"
              stroke-width="2.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </span>
        <span class="msg">{{ it.message }}</span>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-layer {
  position: fixed;
  top: calc(env(safe-area-inset-top) + 14px);
  left: 50%;
  transform: translateX(-50%);
  z-index: 9997;
  pointer-events: none;
}

.toast-stack {
  display: grid;
  gap: 10px;
  justify-items: center;
}

.toast {
  pointer-events: none;
  --accent: rgba(11, 43, 91, 0.85);
  max-width: min(560px, calc(100vw - 28px));
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 999px;
  border: 1px solid rgba(11, 43, 91, 0.12);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 26px rgba(5, 20, 45, 0.14);
  backdrop-filter: saturate(115%) blur(10px);
  color: rgba(11, 43, 91, 0.9);
}

.toast::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: var(--accent);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.85);
}

.icon {
  width: 16px;
  height: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  color: var(--accent);
}

.icon svg {
  width: 16px;
  height: 16px;
  display: block;
}

.msg {
  font-size: 13px;
  letter-spacing: 0.2px;
  line-height: 1.2;
  word-break: break-word;
}

.toast.success {
  --accent: rgba(21, 128, 61, 0.95);
  border-color: rgba(34, 197, 94, 0.22);
}

.toast.error {
  --accent: rgba(185, 28, 28, 0.95);
  border-color: rgba(239, 68, 68, 0.22);
}

.toast.info {
  --accent: rgba(37, 99, 235, 0.92);
  border-color: rgba(59, 130, 246, 0.18);
}

.toast-enter-active,
.toast-leave-active {
  transition: transform 220ms ease, opacity 220ms ease;
}

.toast-enter-from,
.toast-leave-to {
  transform: translateY(-12px);
  opacity: 0;
}

.toast-move {
  transition: transform 220ms ease;
}
</style>
