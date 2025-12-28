import { defineStore } from 'pinia'

export type ConfirmVariant = 'default' | 'danger'

export type ConfirmOptions = {
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  variant?: ConfirmVariant
}

type ConfirmState = {
  open: boolean
  title: string
  message: string
  confirmText: string
  cancelText: string
  variant: ConfirmVariant
}

let resolver: ((value: boolean) => void) | null = null

export const useConfirmStore = defineStore('confirm', {
  state: (): ConfirmState => ({
    open: false,
    title: '请确认',
    message: '',
    confirmText: '确认',
    cancelText: '取消',
    variant: 'default',
  }),
  actions: {
    async confirm(opts: ConfirmOptions): Promise<boolean> {
      if (this.open) {
        this.close(false)
      }

      this.open = true
      this.title = (opts.title || '请确认').trim()
      this.message = (opts.message || '').trim()
      this.confirmText = (opts.confirmText || '确认').trim()
      this.cancelText = (opts.cancelText || '取消').trim()
      this.variant = opts.variant || 'default'

      return new Promise<boolean>((resolve) => {
        resolver = resolve
      })
    },
    close(result: boolean) {
      this.open = false
      const r = resolver
      resolver = null
      if (r) r(result)
    },
    cancel() {
      this.close(false)
    },
    ok() {
      this.close(true)
    },
  },
})

