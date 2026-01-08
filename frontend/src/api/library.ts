import { requestJson } from './http'

export type Book = {
  id: number
  coverUrl?: string
  title: string
  author?: string
  publisher?: string
  isbn?: string
  category?: string
  location?: string
  description?: string
  totalQty?: number
  availableQty?: number
}

export type LoginResponse = {
  token: string
  userId: number
  name: string
  code: string
}

export type BorrowedBook = {
  recordId: number
  bookId: number
  coverUrl?: string
  title: string
  author?: string
  publisher?: string
  location?: string
  borrowAt: string
  dueAt: string
  renewCount: number
  status: number
}

export type UserProfile = {
  userId: number
  name: string
  code: string
}

export type ReaderType = {
  id: number
  name: string
  maxBorrow: number
  borrowDays: number
  maxRenew: number
  description?: string
}

export type CarouselItem = {
  id: number
  tag?: string
  title: string
  subtitle?: string
  coverUrl?: string
  accent?: string
}

export type PortalPostListItem = {
  id: number
  type: number
  title: string
  subtitle?: string
  publishTime: string
}

export type PortalPostDetail = {
  id: number
  type: number
  title: string
  content?: string
  coverUrl?: string
  publishTime: string
}

export async function login(code: string, password: string) {
  return requestJson<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ code, password }),
  })
}

export async function register(payload: { name: string; code: string; phone: string; password: string; readerTypeId?: number }) {
  return requestJson<LoginResponse>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function listBooks(q?: string, category?: string) {
  const params = new URLSearchParams()
  if (q) params.set('q', q)
  if (category) params.set('category', category)
  const query = params.toString()
  return requestJson<Book[]>(`/api/books${query ? `?${query}` : ''}`)
}

export async function listCategories() {
  return requestJson<string[]>('/api/books/categories')
}

export async function getBook(id: number) {
  return requestJson<Book>(`/api/books/${id}`)
}

export async function me() {
  return requestJson<UserProfile>('/api/user/me')
}

export async function listCurrentBorrowed() {
  return requestJson<BorrowedBook[]>('/api/borrow/current')
}

export async function borrowBook(bookId: number) {
  return requestJson<void>('/api/borrow/borrow', {
    method: 'POST',
    body: JSON.stringify({ bookId }),
  })
}

export async function returnBook(recordId: number) {
  return requestJson<void>('/api/borrow/return', {
    method: 'POST',
    body: JSON.stringify({ recordId }),
  })
}

export async function renewBook(recordId: number) {
  return requestJson<void>('/api/borrow/renew', {
    method: 'POST',
    body: JSON.stringify({ recordId }),
  })
}

export async function listReaderTypes() {
  return requestJson<ReaderType[]>('/api/reader-types')
}

export async function listCarousel() {
  return requestJson<CarouselItem[]>('/api/portal/carousel')
}

export async function listPortalPosts(type: number, limit = 10) {
  const params = new URLSearchParams()
  params.set('type', String(type))
  params.set('limit', String(limit))
  return requestJson<PortalPostListItem[]>(`/api/portal/posts?${params.toString()}`)
}

export async function getPortalPost(id: number) {
  return requestJson<PortalPostDetail>(`/api/portal/posts/${id}`)
}
