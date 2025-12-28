import { defineStore } from 'pinia'

export type ToastType = 'success' | 'error' | 'info'

export type ToastItem = {
  id: string
  type: ToastType
  message: string
  duration: number
  createdAt: number
}

const timers = new Map<string, number>()
let seed = 0

function nextId() {
  seed += 1
  return `${Date.now()}-${seed}`
}

export const useToastStore = defineStore('toast', {
  state: () => ({
    items: [] as ToastItem[],
  }),
  actions: {
    show(type: ToastType, message: string, opts: { duration?: number } = {}) {
      const text = (message || '').trim()
      if (!text) return

      const id = nextId()
      const duration = Math.max(1200, Math.min(opts.duration ?? 2500, 8000))
      const item: ToastItem = { id, type, message: text, duration, createdAt: Date.now() }

      this.items.unshift(item)
      if (this.items.length > 3) {
        const dropped = this.items.pop()
        if (dropped) this.dismiss(dropped.id)
      }

      const timer = window.setTimeout(() => {
        this.dismiss(id)
      }, duration)
      timers.set(id, timer)
    },
    success(message: string, opts?: { duration?: number }) {
      this.show('success', message, opts)
    },
    error(message: string, opts?: { duration?: number }) {
      this.show('error', message, opts)
    },
    info(message: string, opts?: { duration?: number }) {
      this.show('info', message, opts)
    },
    dismiss(id: string) {
      const timer = timers.get(id)
      if (timer) {
        window.clearTimeout(timer)
        timers.delete(id)
      }
      this.items = this.items.filter((it) => it.id !== id)
    },
    clear() {
      for (const timer of timers.values()) {
        window.clearTimeout(timer)
      }
      timers.clear()
      this.items = []
    },
  },
})

