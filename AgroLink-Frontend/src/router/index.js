import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // 公开页面（无布局）
  { path: '/auth', name: 'Auth', component: () => import('../views/Auth.vue') },
  { path: '/login', redirect: '/auth' },
  { path: '/register', redirect: '/auth?tab=register' },

  // 根路径：路由守卫中按角色跳转
  { path: '/', redirect: '/farmer/dashboard' },

  // ===== 主应用：DashboardLayout 包裹所有页面 =====
  {
    path: '',
    component: () => import('../views/dashboard/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      // 角色工作台
      { path: 'farmer/dashboard', name: 'FarmerDashboard', meta: { role: 'FARMER' }, component: () => import('../views/dashboard/FarmerDashboard.vue') },
      { path: 'merchant/dashboard', name: 'MerchantDashboard', meta: { role: 'MERCHANT' }, component: () => import('../views/dashboard/MerchantDashboard.vue') },

      // 业务页面
      { path: 'products', name: 'ProductList', component: () => import('../views/products/ProductList.vue') },
      { path: 'products/:id', name: 'ProductDetail', component: () => import('../views/products/ProductDetail.vue') },
      { path: 'publish', name: 'Publish', component: () => import('../views/products/Publish.vue') },
      { path: 'publish/:id', name: 'PublishEdit', component: () => import('../views/products/Publish.vue') },
      { path: 'orders', name: 'Orders', component: () => import('../views/orders/Orders.vue') },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('../views/orders/OrderDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('../views/transactions/Cart.vue') },
      { path: 'favorites', name: 'Favorites', component: () => import('../views/transactions/Favorites.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/user/Profile.vue') },
      { path: 'addresses', name: 'Addresses', component: () => import('../views/user/Addresses.vue') },
    ]
  },

  // ===== 管理后台：AdminLayout =====
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/AdminDashboardHome.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/UserManagement.vue') },
      { path: 'roles', name: 'AdminRoles', component: () => import('../views/admin/RoleManagement.vue') },
      { path: 'menus', name: 'AdminMenus', component: () => import('../views/admin/MenuManagement.vue') },
      { path: 'products', name: 'AdminProducts', component: () => import('../views/admin/AdminProducts.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/AdminOrders.vue') },
      { path: 'content', name: 'AdminContent', component: () => import('../views/admin/AdminContent.vue') },
      { path: 'logs', name: 'AdminLogs', component: () => import('../views/admin/AdminLogs.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/AdminCategories.vue') },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 角色首页跳转
export function getRoleHome(role) {
  if (role === 'MERCHANT') return '/merchant/dashboard'
  if (role === 'ADMIN') return '/admin/users'
  return '/farmer/dashboard'
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  const userStr = localStorage.getItem('user') || sessionStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  // 根路径：按角色跳转
  if (to.path === '/') {
    if (!user) return next({ path: '/auth', query: { redirect: to.fullPath } })
    return next(getRoleHome(user.role))
  }

  // 登录页：永远可访问
  if (to.name === 'Auth') return next()

  // 需要登录
  if (to.meta.requiresAuth && !token) {
    return next({ path: '/auth', query: { redirect: to.fullPath } })
  }

  // 角色校验
  if (to.meta.role && user && user.role !== to.meta.role) {
    return next(getRoleHome(user.role))
  }

  next()
})

export default router
