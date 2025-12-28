<script setup lang="ts">
import { computed, onBeforeUnmount, watch } from 'vue'
import { useConfirmStore } from '@/stores/confirm'

const confirmStore = useConfirmStore()
const open = computed(() => confirmStore.open)

function onKeydown(e: KeyboardEvent) {
  if (!confirmStore.open) return
  if (e.key === 'Escape') confirmStore.cancel()
}

watch(
  () => confirmStore.open,
  (v) => {
    if (v) {
      window.addEventListener('keydown', onKeydown)
      return
    }
    window.removeEventListener('keydown', onKeydown)
  },
  { immediate: true },
)

onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Transition name="confirm-fade">
    <div v-if="open" class="overlay" role="presentation" @click.self="confirmStore.cancel">
      <Transition name="confirm-pop" appear>
        <div class="dialog" role="dialog" aria-modal="true" :aria-label="confirmStore.title">
          <div class="head">
            <div class="title">{{ confirmStore.title }}</div>
          </div>
          <div class="body">
            <div class="msg">{{ confirmStore.message }}</div>
          </div>
          <div class="actions">
            <button class="btn" type="button" @click="confirmStore.cancel">{{ confirmStore.cancelText }}</button>
            <button
              class="btn"
              :class="confirmStore.variant === 'danger' ? 'btn-danger' : 'btn-primary'"
              type="button"
              @click="confirmStore.ok"
              autofocus
            >
              {{ confirmStore.confirmText }}
            </button>
          </div>
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(3, 10, 22, 0.32);
  backdrop-filter: blur(6px);
}

.dialog {
  width: min(520px, calc(100vw - 28px));
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 30px 70px rgba(5, 20, 45, 0.26);
  overflow: hidden;
}

.head {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(11, 43, 91, 0.08);
  background: radial-gradient(900px 260px at 10% 0%, rgba(18, 59, 121, 0.12), transparent 56%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(245, 247, 251, 0.9));
}

.title {
  font-family: var(--font-serif);
  font-weight: 800;
  color: rgba(11, 43, 91, 0.92);
  letter-spacing: 0.3px;
}

.body {
  padding: 14px 16px;
}

.msg {
  color: rgba(11, 43, 91, 0.84);
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
}

.actions {
  padding: 12px 16px 16px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.btn-danger {
  border-color: rgba(239, 68, 68, 0.28);
  background: linear-gradient(180deg, rgba(239, 68, 68, 1) 0%, rgba(185, 28, 28, 1) 100%);
  color: #fff;
}

.btn-danger:hover {
  border-color: rgba(239, 68, 68, 0.44);
  box-shadow: 0 18px 36px rgba(185, 28, 28, 0.22);
}

.confirm-fade-enter-active,
.confirm-fade-leave-active {
  transition: opacity 180ms ease;
}
.confirm-fade-enter-from,
.confirm-fade-leave-to {
  opacity: 0;
}

.confirm-pop-enter-active,
.confirm-pop-leave-active {
  transition: transform 200ms ease, opacity 200ms ease;
}
.confirm-pop-enter-from,
.confirm-pop-leave-to {
  transform: translateY(-8px) scale(0.98);
  opacity: 0;
}
</style>
