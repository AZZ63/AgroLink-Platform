import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/auth', name: 'Auth', component: () => import('../views/Auth.vue') },
  { path: '/login', redirect: '/auth' },
  { path: '/register', redirect: '/auth?tab=register' },
  { path: '/products', name: 'ProductList', meta: { requiresAuth: true }, component: () => import('../views/ProductList.vue') },
  { path: '/products/:id', name: 'ProductDetail', meta: { requiresAuth: true }, component: () => import('../views/ProductDetail.vue') },
  { path: '/favorites', name: 'Favorites', meta: { requiresAuth: true }, component: () => import('../views/Favorites.vue') },
  { path: '/addresses', name: 'Addresses', meta: { requiresAuth: true }, component: () => import('../views/Addresses.vue') },
  { path: '/publish', name: 'Publish', meta: { requiresAuth: true }, component: () => import('../views/Publish.vue') },
  { path: '/publish/:id', name: 'PublishEdit', meta: { requiresAuth: true }, component: () => import('../views/Publish.vue') },
  { path: '/orders', name: 'Orders', meta: { requiresAuth: true }, component: () => import('../views/Orders.vue') },
  { path: '/orders/:id', name: 'OrderDetail', meta: { requiresAuth: true }, component: () => import('../views/OrderDetail.vue') },
  { path: '/cart', name: 'Cart', meta: { requiresAuth: true }, component: () => import('../views/Cart.vue') },
  { path: '/profile', name: 'Profile', meta: { requiresAuth: true }, component: () => import('../views/Profile.vue') },
  { path: '/farmer/dashboard', name: 'FarmerDashboard', meta: { requiresAuth: true, role: 'FARMER' }, component: () => import('../views/FarmerDashboard.vue') },
  { path: '/merchant/dashboard', name: 'MerchantDashboard', meta: { requiresAuth: true, role: 'MERCHANT' }, component: () => import('../views/MerchantDashboard.vue') },
  { path: '/admin/dashboard', name: 'AdminDashboard', meta: { requiresAuth: true, role: 'ADMIN' }, component: () => import('../views/AdminDashboard.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Role-based home resolver
export function getRoleHome(role) {
  if (role === 'MERCHANT') return '/merchant/dashboard'
  if (role === 'ADMIN') return '/admin/dashboard'
  return '/farmer/dashboard'
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  const userStr = localStorage.getItem('user') || sessionStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  // Root path: login wall
  if (to.path === '/') {
    if (!user) return next({ path: '/auth', query: { redirect: to.fullPath } })
    return next(getRoleHome(user.role))
  }

  // Auth page: always accessible
  if (to.name === 'Auth') {
    return next()
  }

  if (to.meta.requiresAuth && !token) {
    return next({ path: '/auth', query: { redirect: to.fullPath } })
  }

  // Role check
  if (to.meta.role && user) {
    if (user.role !== to.meta.role) {
      return next(getRoleHome(user.role))
    }
  }

  next()
})

export default router
