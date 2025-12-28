type ApiResult<T> = {
  code: number
  msg?: string
  data?: T
}

export async function requestJson<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')

  if (!headers.has('token')) {
    const token = localStorage.getItem('lib_token')
    if (token) {
      headers.set('token', token)
    }
  }

  const response = await fetch(path, { ...init, headers })
  const text = await response.text()
  let json: ApiResult<T> | null = null
  try {
    json = text ? (JSON.parse(text) as ApiResult<T>) : null
  } catch {
    json = null
  }

  if (!response.ok) {
    throw new Error(json?.msg || `请求失败(${response.status})`)
  }
  if (!json) {
    throw new Error('响应解析失败')
  }
  if (json.code !== 1) {
    throw new Error(json.msg || '请求失败')
  }
  return json.data as T
}
