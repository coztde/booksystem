import { useConfirmStore, type ConfirmOptions } from '@/stores/confirm'

export function useConfirm() {
  const store = useConfirmStore()
  return {
    confirm: (opts: ConfirmOptions) => store.confirm(opts),
  }
}

