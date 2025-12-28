import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, type LoginResponse } from '@/api/library'

export type AuthUser = Pick<LoginResponse, 'userId' | 'name' | 'code'>

const TOKEN_KEY = 'lib_token'
const USER_KEY = 'lib_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '' as string,
    user: null as AuthUser | null,
    hydrated: false as boolean,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token && state.user),
  },
  actions: {
    hydrate() {
      if (this.hydrated) return
      this.token = localStorage.getItem(TOKEN_KEY) || ''
      const userJson = localStorage.getItem(USER_KEY)
      this.user = userJson ? (JSON.parse(userJson) as AuthUser) : null
      this.hydrated = true
    },
    async login(code: string, password: string) {
      const data = await loginApi(code, password)
      this.token = data.token
      this.user = { userId: data.userId, name: data.name, code: data.code }
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    async register(payload: { name: string; code: string; phone: string; password: string; readerTypeId?: number }) {
      const data = await registerApi(payload)
      this.token = data.token
      this.user = { userId: data.userId, name: data.name, code: data.code }
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
  },
})
