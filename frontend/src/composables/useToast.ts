import { useToastStore } from '@/stores/toast'

export function useToast() {
  const store = useToastStore()
  return {
    success: store.success,
    error: store.error,
    info: store.info,
    show: store.show,
    dismiss: store.dismiss,
    clear: store.clear,
  }
}

