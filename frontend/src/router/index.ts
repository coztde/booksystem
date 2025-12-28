import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/admin/login',
      name: 'adminLogin',
      component: () => import('../views/admin/AdminLoginView.vue'),
    },
    {
      path: '/admin',
      name: 'admin',
      meta: { requiresAdmin: true },
      component: () => import('../views/admin/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/admin/posts' },
        { path: 'posts', name: 'adminPosts', component: () => import('../views/admin/AdminPostManageView.vue') },
        { path: 'books', name: 'adminBooks', component: () => import('../views/admin/AdminBookManageView.vue') },
        { path: 'borrows', name: 'adminBorrows', component: () => import('../views/admin/AdminBorrowManageView.vue') },
        { path: 'users', name: 'adminUsers', component: () => import('../views/admin/AdminUserManageView.vue') },
      ],
    },
    {
      path: '/resources',
      name: 'resources',
      component: () => import('../views/ResourcesView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/me',
      name: 'me',
      meta: { requiresAuth: true },
      component: () => import('../views/UserCenterView.vue'),
    },
    {
      path: '/books/:id',
      name: 'bookDetail',
      component: () => import('../views/BookDetailView.vue'),
    },
    {
      path: '/posts',
      name: 'postList',
      component: () => import('../views/PostListView.vue'),
    },
    {
      path: '/posts/:id',
      name: 'postDetail',
      component: () => import('../views/PostDetailView.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('lib_token')
  const userJson = localStorage.getItem('lib_user')
  const isLoggedIn = Boolean(token && userJson)

  const adminToken = localStorage.getItem('lib_admin_token')
  const adminUserJson = localStorage.getItem('lib_admin_user')
  const isAdminLoggedIn = Boolean(adminToken && adminUserJson)

  if (to.meta?.requiresAdmin && !isAdminLoggedIn) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/admin/login' && isAdminLoggedIn) {
    return { path: '/admin' }
  }

  if (to.meta?.requiresAuth && !isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if ((to.path === '/login' || to.path === '/register') && isLoggedIn) {
    return { path: '/me' }
  }
})

export default router
