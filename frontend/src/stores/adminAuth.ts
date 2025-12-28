import { defineStore } from 'pinia'
import { adminLogin, type AdminLoginResponse } from '@/api/admin'

export type AdminUser = Pick<AdminLoginResponse, 'adminId' | 'name' | 'username'>

const TOKEN_KEY = 'lib_admin_token'
const USER_KEY = 'lib_admin_user'

export const useAdminAuthStore = defineStore('adminAuth', {
  state: () => ({
    token: '' as string,
    user: null as AdminUser | null,
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
      this.user = userJson ? (JSON.parse(userJson) as AdminUser) : null
      this.hydrated = true
    },
    async login(username: string, password: string) {
      const data = await adminLogin(username, password)
      this.token = data.token
      this.user = { adminId: data.adminId, name: data.name, username: data.username }
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

