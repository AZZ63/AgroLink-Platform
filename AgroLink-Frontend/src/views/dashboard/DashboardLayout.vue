<template>
  <div class="dashboard-root">
    <!-- 侧边栏（按角色） -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo" @click="goHome">
          <span class="logo-icon">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <rect width="28" height="28" rx="8" fill="#7CCF5F"/>
              <path d="M14 6C12 6 8 8 8 14C8 18 10 22 14 22C18 22 20 18 20 14C20 8 16 6 14 6Z" fill="white" opacity="0.9"/>
              <path d="M14 10L16 14L14 18L12 14L14 10Z" fill="#7CCF5F"/>
            </svg>
          </span>
          <span class="logo-text">Agro<span>Link</span></span>
        </div>
        <div class="sidebar-role-badge">{{ roleLabel }}</div>
      </div>

      <!-- 农户侧边栏 -->
      <template v-if="userStore.isFarmer">
        <nav class="sidebar-nav">
          <div class="nav-label">工作台</div>
          <a v-for="item in farmerNav" :key="item.path" :href="item.path"
            class="nav-item" :class="{ active: currentPath === item.path }"
            @click.prevent="navigate(item.path)">
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text">{{ item.label }}</span>
          </a>
        </nav>
        <div class="sidebar-quick">
          <div class="sq-title">快捷操作</div>
          <button class="sq-btn" @click="$router.push('/publish?type=SUPPLY')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            发布供应
          </button>
        </div>
      </template>

      <!-- 采购商侧边栏 -->
      <template v-if="userStore.isMerchant">
        <nav class="sidebar-nav">
          <div class="nav-label">工作台</div>
          <a v-for="item in merchantNav" :key="item.path" :href="item.path"
            class="nav-item" :class="{ active: currentPath === item.path }"
            @click.prevent="navigate(item.path)">
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text">{{ item.label }}</span>
          </a>
        </nav>
        <div class="sidebar-quick">
          <div class="sq-title">快捷操作</div>
          <button class="sq-btn" @click="$router.push('/publish?type=DEMAND')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            发布需求
          </button>
          <button class="sq-btn" @click="$router.push('/cart')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
            购物车
          </button>
        </div>
      </template>

      <div class="sidebar-footer">
        <div class="sidebar-user">
          <div class="user-avatar">{{ userStore.user?.username?.charAt(0).toUpperCase() || 'U' }}</div>
          <div class="user-info">
            <div class="user-name">{{ userStore.user?.username || '用户' }}</div>
            <div class="user-role">{{ roleLabel }}</div>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="main-area">
      <header class="top-header">
        <div class="header-left">
          <div class="search-box">
            <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input type="text" :placeholder="searchPlaceholder" v-model="searchQuery" @keyup.enter="handleSearch" />
          </div>
        </div>
        <div class="header-right">
          <button class="icon-btn" @click="$router.push('/orders')" title="订单">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </button>
          <button class="icon-btn" @click="goNotifications" title="通知">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
            <span v-if="unreadCount > 0" class="notif-dot">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </button>
          <div class="avatar-circle" @click="$router.push('/profile')">
            {{ userStore.user?.username?.charAt(0).toUpperCase() || 'U' }}
          </div>
        </div>
      </header>

      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { notifyApi } from '../../api/http'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchQuery = ref('')
const unreadCount = ref(0)

const currentPath = computed(() => route.path)

const roleLabel = computed(() => {
  if (userStore.isAdmin) return '管理员'
  if (userStore.isFarmer) return '农户'
  if (userStore.isMerchant) return '商户'
  return ''
})

const searchPlaceholder = computed(() => {
  if (userStore.isFarmer) return '搜索我的货源、订单...'
  return '搜索农产品、供应商...'
})

// 农户导航
const farmerNav = computed(() => [
  { path: '/farmer/dashboard', label: '工作台', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
  { path: '/products', label: '我的货源', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>' },
  { path: '/orders', label: '订单管理', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>' },
  { path: '/profile', label: '个人中心', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>' },
])

// 采购商导航
const merchantNav = computed(() => [
  { path: '/merchant/dashboard', label: '工作台', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
  { path: '/products', label: '市场采购', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>' },
  { path: '/favorites', label: '收藏', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>' },
  { path: '/orders', label: '订单管理', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>' },
  { path: '/profile', label: '个人中心', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>' },
])

function navigate(path) { router.push(path) }
function goHome() { router.push(userStore.isFarmer ? '/farmer/dashboard' : '/merchant/dashboard') }
function goNotifications() { router.push(userStore.isFarmer ? '/farmer/dashboard' : '/merchant/dashboard') }

function handleSearch() {
  if (searchQuery.value.trim()) {
    router.push(`/products?search=${encodeURIComponent(searchQuery.value.trim())}`)
  }
}

function handleLogout() { userStore.logout(); router.push('/') }

onMounted(async () => {
  try { const res = await notifyApi.unread(); unreadCount.value = res.count || 0 } catch {}
})
</script>

<style scoped>
.dashboard-root { display: flex; min-height: 100vh; background: #f5f7f2; font-family: 'Inter', system-ui, -apple-system, sans-serif; }

/* ===== 侧边栏 ===== */
.sidebar { width: 240px; flex-shrink: 0; background: #1a1e2c; display: flex; flex-direction: column; position: fixed; top: 0; left: 0; bottom: 0; z-index: 50; }
.sidebar-header { padding: 24px 20px 16px; border-bottom: 1px solid #262b3a; }
.logo { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.logo-icon { display: flex; align-items: center; }
.logo-text { font-size: 22px; font-weight: 700; color: #ffffff; letter-spacing: -0.5px; }
.logo-text span { color: #7CCF5F; }

.sidebar-role-badge { display: inline-flex; margin-top: 10px; padding: 3px 10px; background: rgba(124,207,95,0.15); color: #7CCF5F; border-radius: 6px; font-size: 11px; font-weight: 600; }

.sidebar-nav { flex: 1; padding: 12px 12px 8px; overflow-y: auto; }
.nav-label { font-size: 11px; font-weight: 600; color: #4a5068; text-transform: uppercase; letter-spacing: 1px; padding: 8px 12px 12px; }
.nav-item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 8px; color: #8b92a5; text-decoration: none; font-size: 14px; font-weight: 500; transition: all 0.15s ease; cursor: pointer; margin-bottom: 2px; }
.nav-item:hover { background: #242838; color: #e0e3eb; }
.nav-item.active { background: #2a2f42; color: #ffffff; }
.nav-icon { display: flex; align-items: center; width: 20px; justify-content: center; flex-shrink: 0; }

.sidebar-quick { padding: 8px 12px 12px; border-top: 1px solid #262b3a; }
.sq-title { font-size: 11px; color: #4a5068; margin-bottom: 8px; padding: 0 4px; }
.sq-btn { display: flex; align-items: center; gap: 8px; width: 100%; padding: 8px 12px; border: 1px solid #262b3a; border-radius: 8px; background: transparent; color: #8b92a5; font-size: 12px; cursor: pointer; font-family: 'Inter', sans-serif; margin-bottom: 4px; transition: all 0.15s; }
.sq-btn:hover { border-color: #7CCF5F; color: #7CCF5F; background: rgba(124,207,95,0.06); }

.sidebar-footer { padding: 16px; border-top: 1px solid #262b3a; position: relative; }
.sidebar-user { display: flex; align-items: center; gap: 10px; }
.user-avatar { width: 36px; height: 36px; border-radius: 8px; background: #7CCF5F; color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 14px; flex-shrink: 0; }
.user-info { flex: 1; min-width: 0; }
.user-name { font-size: 13px; font-weight: 600; color: #e0e3eb; }
.user-role { font-size: 11px; color: #5a6180; }
.logout-btn { position: absolute; right: 12px; bottom: 20px; background: none; border: none; color: #5a6180; cursor: pointer; padding: 6px; border-radius: 6px; transition: all 0.15s; }
.logout-btn:hover { color: #ff5a5a; background: rgba(255,90,90,0.1); }

/* ===== 主区域 ===== */
.main-area { flex: 1; margin-left: 240px; display: flex; flex-direction: column; min-height: 100vh; }
.top-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 32px; background: #ffffff; border-bottom: 1px solid #eef0eb; position: sticky; top: 0; z-index: 40; }
.header-left { flex: 1; max-width: 400px; }
.search-box { display: flex; align-items: center; gap: 8px; background: #f5f7f2; border: 1px solid #e8ebe4; border-radius: 10px; padding: 8px 14px; transition: all 0.2s; }
.search-box:focus-within { border-color: #7CCF5F; box-shadow: 0 0 0 3px rgba(124,207,95,0.1); }
.search-icon { color: #9ca3af; flex-shrink: 0; }
.search-box input { border: none; background: transparent; outline: none; font-size: 14px; color: #1e1e1e; width: 100%; font-family: 'Inter', sans-serif; }
.search-box input::placeholder { color: #9ca3af; }
.header-right { display: flex; align-items: center; gap: 12px; }
.icon-btn { position: relative; width: 40px; height: 40px; border-radius: 10px; border: none; background: #f5f7f2; color: #5a6180; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.15s; }
.icon-btn:hover { background: #e8ebe4; color: #1e1e1e; }
.notif-dot { position: absolute; top: 6px; right: 6px; min-width: 16px; height: 16px; background: #ff5a5a; color: white; font-size: 10px; font-weight: 700; border-radius: 8px; display: flex; align-items: center; justify-content: center; padding: 0 4px; }
.avatar-circle { width: 40px; height: 40px; border-radius: 10px; background: #7CCF5F; color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 15px; cursor: pointer; transition: opacity 0.15s; }
.avatar-circle:hover { opacity: 0.85; }
.content-area { flex: 1; padding: 28px 32px; max-width: 1280px; }
</style>
