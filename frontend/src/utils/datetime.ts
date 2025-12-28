function pad2(value: number) {
  return String(value).padStart(2, '0')
}

export function normalizeDateTime(value: unknown) {
  if (!value) return ''

  if (typeof value === 'string') return value

  if (Array.isArray(value)) {
    const [y, m, d, h, mi, s] = value as Array<unknown>
    if (typeof y === 'number' && typeof m === 'number' && typeof d === 'number') {
      const hh = typeof h === 'number' ? h : 0
      const mm = typeof mi === 'number' ? mi : 0
      const ss = typeof s === 'number' ? s : undefined
      return `${y}-${pad2(m)}-${pad2(d)} ${pad2(hh)}:${pad2(mm)}${ss != null ? `:${pad2(ss)}` : ''}`
    }
  }

  if (typeof value === 'object') {
    const v = value as Record<string, unknown>
    const y = v.year
    const m = (v.monthValue ?? v.month) as unknown
    const d = (v.dayOfMonth ?? v.day) as unknown
    const h = v.hour
    const mi = v.minute
    const s = v.second

    if (typeof y === 'number' && typeof m === 'number' && typeof d === 'number') {
      return `${y}-${pad2(m)}-${pad2(d)} ${pad2(typeof h === 'number' ? h : 0)}:${pad2(typeof mi === 'number' ? mi : 0)}${
        typeof s === 'number' ? `:${pad2(s)}` : ''
      }`
    }
  }

  return String(value)
}

export function formatToMinute(value: unknown) {
  const text = normalizeDateTime(value)
  if (!text) return ''
  return text.replace('T', ' ').slice(0, 16)
}

export function formatMMDD(value: unknown) {
  const text = formatToMinute(value)
  if (!text || text.length < 10) return '--'
  return text.slice(5, 10)
}

