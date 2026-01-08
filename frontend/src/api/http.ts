type ApiResult<T> = {
  code: number
  msg?: string
  data?: T
}

export type AuthScope = 'user' | 'admin'

export const AUTH_EXPIRED_EVENT = 'lib-auth-expired'

export class AuthExpiredError extends Error {
  scope: AuthScope

  constructor(scope: AuthScope, message = '登录已过期，请重新登录') {
    super(message)
    this.name = 'AuthExpiredError'
    this.scope = scope
  }
}

export function isAuthExpiredError(error: unknown): error is AuthExpiredError {
  return error instanceof AuthExpiredError || (typeof error === 'object' && error != null && (error as any).name === 'AuthExpiredError')
}

function withToken(headers: Headers) {
  if (!headers.has('token')) {
    const token = localStorage.getItem('lib_token')
    if (token) {
      headers.set('token', token)
    }
  }
  return headers
}

function handleUnauthorized(scope: AuthScope) {
  if (scope === 'admin') {
    localStorage.removeItem('lib_admin_token')
    localStorage.removeItem('lib_admin_user')
  } else {
    localStorage.removeItem('lib_token')
    localStorage.removeItem('lib_user')
  }

  window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT, { detail: { scope } }))
}

export async function requestJson<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')
  withToken(headers)

  const response = await fetch(path, { ...init, headers })
  const text = await response.text()
  let json: ApiResult<T> | null = null
  try {
    json = text ? (JSON.parse(text) as ApiResult<T>) : null
  } catch {
    json = null
  }

  if (response.status === 401) {
    const scope: AuthScope = path.startsWith('/admin/') ? 'admin' : 'user'
    handleUnauthorized(scope)
    throw new AuthExpiredError(scope)
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

export async function requestForm<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  withToken(headers)

  const response = await fetch(path, { ...init, headers })
  const text = await response.text()
  let json: ApiResult<T> | null = null
  try {
    json = text ? (JSON.parse(text) as ApiResult<T>) : null
  } catch {
    json = null
  }

  if (response.status === 401) {
    const scope: AuthScope = path.startsWith('/admin/') ? 'admin' : 'user'
    handleUnauthorized(scope)
    throw new AuthExpiredError(scope)
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
