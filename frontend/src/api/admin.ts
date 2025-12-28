import { requestJson } from './http'

export type PageResult<T> = {
  total: number
  records: T[]
}

export type AdminLoginResponse = {
  token: string
  adminId: number
  name: string
  username: string
}

export type AdminPortalPostListItem = {
  id: number
  type: number
  tag?: string
  title: string
  subtitle?: string
  coverUrl?: string
  accent?: string
  sort?: number
  publishTime: string
  status: number
}

export type AdminPortalPostDetail = {
  id: number
  type: number
  tag?: string
  title: string
  subtitle?: string
  content?: string
  coverUrl?: string
  accent?: string
  sort?: number
  publishTime?: string
  status: number
}

export type AdminBook = {
  id: number
  title: string
  author?: string
  publisher?: string
  isbn?: string
  category?: string
  location?: string
  description?: string
  totalQty: number
  availableQty: number
  status: number
}

export type AdminBorrowRecord = {
  recordId: number
  userId: number
  userName: string
  userCode: string
  bookId: number
  bookTitle: string
  borrowAt: string
  dueAt: string
  returnAt?: string
  renewCount: number
  status: number
  fineAmount?: number
  handledBy?: number
  handledByName?: string
}

export type AdminReader = {
  id: number
  name: string
  code: string
  phone: string
  readerTypeId?: number
  readerTypeName?: string
  status: number
  createTime: string
}

function withAdminToken(init: RequestInit = {}): RequestInit {
  const token = localStorage.getItem('lib_admin_token') || ''
  const headers = new Headers(init.headers)
  headers.set('token', token)
  return { ...init, headers }
}

export async function adminLogin(username: string, password: string) {
  return requestJson<AdminLoginResponse>(
    '/admin/auth/login',
    withAdminToken({
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),
  )
}

export async function adminPagePosts(params: { type?: number; status?: number; keyword?: string; page?: number; pageSize?: number }) {
  const search = new URLSearchParams()
  if (params.type != null) search.set('type', String(params.type))
  if (params.status != null) search.set('status', String(params.status))
  if (params.keyword) search.set('keyword', params.keyword)
  if (params.page != null) search.set('page', String(params.page))
  if (params.pageSize != null) search.set('pageSize', String(params.pageSize))
  return requestJson<PageResult<AdminPortalPostListItem>>(`/admin/portal/posts?${search.toString()}`, withAdminToken())
}

export async function adminGetPost(id: number) {
  return requestJson<AdminPortalPostDetail>(`/admin/portal/posts/${id}`, withAdminToken())
}

export async function adminCreatePost(payload: Partial<AdminPortalPostDetail>) {
  return requestJson<void>(
    '/admin/portal/posts',
    withAdminToken({
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  )
}

export async function adminUpdatePost(id: number, payload: Partial<AdminPortalPostDetail>) {
  return requestJson<void>(
    `/admin/portal/posts/${id}`,
    withAdminToken({
      method: 'PUT',
      body: JSON.stringify(payload),
    }),
  )
}

export async function adminDeletePost(id: number) {
  return requestJson<void>(`/admin/portal/posts/${id}`, withAdminToken({ method: 'DELETE' }))
}

export async function adminPageBooks(params: { keyword?: string; category?: string; status?: number; page?: number; pageSize?: number }) {
  const search = new URLSearchParams()
  if (params.keyword) search.set('keyword', params.keyword)
  if (params.category) search.set('category', params.category)
  if (params.status != null) search.set('status', String(params.status))
  if (params.page != null) search.set('page', String(params.page))
  if (params.pageSize != null) search.set('pageSize', String(params.pageSize))
  return requestJson<PageResult<AdminBook>>(`/admin/books?${search.toString()}`, withAdminToken())
}

export async function adminGetBook(id: number) {
  return requestJson<AdminBook>(`/admin/books/${id}`, withAdminToken())
}

export async function adminCreateBook(payload: Partial<AdminBook>) {
  return requestJson<void>(
    '/admin/books',
    withAdminToken({
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  )
}

export async function adminUpdateBook(id: number, payload: Partial<AdminBook>) {
  return requestJson<void>(
    `/admin/books/${id}`,
    withAdminToken({
      method: 'PUT',
      body: JSON.stringify(payload),
    }),
  )
}

export async function adminDeleteBook(id: number) {
  return requestJson<void>(`/admin/books/${id}`, withAdminToken({ method: 'DELETE' }))
}

export async function adminPageBorrows(params: { status?: number; keyword?: string; page?: number; pageSize?: number }) {
  const search = new URLSearchParams()
  if (params.status != null) search.set('status', String(params.status))
  if (params.keyword) search.set('keyword', params.keyword)
  if (params.page != null) search.set('page', String(params.page))
  if (params.pageSize != null) search.set('pageSize', String(params.pageSize))
  return requestJson<PageResult<AdminBorrowRecord>>(`/admin/borrows?${search.toString()}`, withAdminToken())
}

export async function adminBorrowOut(userCode: string, bookId: number) {
  return requestJson<void>(
    '/admin/borrows/borrow',
    withAdminToken({
      method: 'POST',
      body: JSON.stringify({ userCode, bookId }),
    }),
  )
}

export async function adminReturnBorrow(recordId: number, fineAmount?: number) {
  return requestJson<void>(
    '/admin/borrows/return',
    withAdminToken({
      method: 'POST',
      body: JSON.stringify({ recordId, fineAmount }),
    }),
  )
}

export async function adminPageReaders(params: { keyword?: string; status?: number; page?: number; pageSize?: number }) {
  const search = new URLSearchParams()
  if (params.keyword) search.set('keyword', params.keyword)
  if (params.status != null) search.set('status', String(params.status))
  if (params.page != null) search.set('page', String(params.page))
  if (params.pageSize != null) search.set('pageSize', String(params.pageSize))
  return requestJson<PageResult<AdminReader>>(`/admin/users?${search.toString()}`, withAdminToken())
}

export async function adminResetReaderPassword(id: number) {
  return requestJson<void>(`/admin/users/${id}/reset-password`, withAdminToken({ method: 'POST' }))
}

export async function adminUpdateReaderStatus(id: number, status: number) {
  return requestJson<void>(
    `/admin/users/${id}/status`,
    withAdminToken({
      method: 'POST',
      body: JSON.stringify({ status }),
    }),
  )
}

