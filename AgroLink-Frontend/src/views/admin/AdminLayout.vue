<template>
  <div class="admin-root">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <rect width="28" height="28" rx="8" fill="#7CCF5F"/>
              <path d="M14 6C12 6 8 8 8 14C8 18 10 22 14 22C18 22 20 18 20 14C20 8 16 6 14 6Z" fill="white" opacity="0.9"/>
              <path d="M14 10L16 14L14 18L12 14L14 10Z" fill="#7CCF5F"/>
            </svg>
          </span>
          <span class="logo-text">Agro<span>Link</span></span>
        </div>
        <div class="sidebar-role-badge">管理后台</div>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-label">运营管理</div>
        <a v-for="item in menuItems" :key="item.path" :href="item.path"
          class="nav-item" :class="{ active: currentPath === item.path }"
          @click.prevent="navigate(item.path)">
          <span class="nav-icon" v-html="item.icon"></span>
          <span class="nav-text">{{ item.name }}</span>
        </a>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-user">
          <div class="user-avatar">{{ userStore.user?.username?.charAt(0).toUpperCase() || 'A' }}</div>
          <div class="user-info">
            <div class="user-name">{{ userStore.user?.username || '管理员' }}</div>
            <div class="user-role">管理员</div>
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
            <input type="text" placeholder="搜索用户、订单、商品..." v-model="searchQuery" @keyup.enter="handleSearch" />
          </div>
        </div>
        <div class="header-right">
          <button class="icon-btn" @click="$router.push('/admin/users')" title="通知">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
          </button>
          <button class="btn btn-outline btn-xs" @click="$router.push('/')">返回前台</button>
        </div>
      </header>
      <main class="content-area">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const currentPath = computed(() => route.path)
const searchQuery = computed({ get: () => '', set: v => {} })

const menuItems = [
  { path: '/admin/dashboard', name: '数据总览', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
  { path: '/admin/users', name: '用户管理', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>' },
  { path: '/admin/roles', name: '角色管理', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>' },
  { path: '/admin/menus', name: '菜单管理', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="18" x2="21" y2="18"/></svg>' },
  { path: '/admin/products', name: '商品审核', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>' },
  { path: '/admin/orders', name: '订单监管', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>' },
  { path: '/admin/content', name: '内容管理', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>' },
  { path: '/admin/logs', name: '日志审计', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>' },
  { path: '/admin/categories', name: '分类管理', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="7" cy="7" r="2.5"/><circle cx="17" cy="7" r="2.5"/><circle cx="7" cy="17" r="2.5"/><circle cx="17" cy="17" r="2.5"/></svg>' },
]

function navigate(path) { router.push(path) }
function handleSearch() {}
function handleLogout() { userStore.logout(); router.push('/') }
</script>

<style scoped>
.admin-root { display: flex; min-height: 100vh; background: #f5f7f2; font-family: 'Inter', system-ui, -apple-system, sans-serif; }

/* ===== 侧边栏 ===== */
.sidebar { width: 230px; flex-shrink: 0; background: #1a1e2c; display: flex; flex-direction: column; position: fixed; top: 0; left: 0; bottom: 0; z-index: 50; }
.sidebar-header { padding: 24px 20px 12px; }
.logo { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.logo-icon { display: flex; align-items: center; }
.logo-text { font-size: 22px; font-weight: 700; color: #ffffff; letter-spacing: -0.5px; }
.logo-text span { color: #7CCF5F; }
.sidebar-role-badge { display: inline-flex; margin-top: 8px; padding: 3px 10px; background: rgba(124,207,95,0.15); color: #7CCF5F; border-radius: 6px; font-size: 11px; font-weight: 600; }

.sidebar-nav { flex: 1; padding: 8px 12px; overflow-y: auto; }
.nav-label { font-size: 11px; font-weight: 600; color: #4a5068; text-transform: uppercase; letter-spacing: 1px; padding: 8px 12px 12px; }
.nav-item { display: flex; align-items: center; gap: 10px; padding: 9px 12px; border-radius: 8px; color: #8b92a5; text-decoration: none; font-size: 13px; font-weight: 500; transition: all 0.15s; cursor: pointer; margin-bottom: 2px; }
.nav-item:hover { background: #242838; color: #e0e3eb; }
.nav-item.active { background: #2a2f42; color: #ffffff; }
.nav-icon { display: flex; align-items: center; width: 20px; justify-content: center; flex-shrink: 0; }

.sidebar-footer { padding: 16px; border-top: 1px solid #262b3a; position: relative; }
.sidebar-user { display: flex; align-items: center; gap: 10px; }
.user-avatar { width: 36px; height: 36px; border-radius: 8px; background: #7CCF5F; color: white; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 14px; flex-shrink: 0; }
.user-info { flex: 1; min-width: 0; }
.user-name { font-size: 13px; font-weight: 600; color: #e0e3eb; }
.user-role { font-size: 11px; color: #5a6180; }
.logout-btn { position: absolute; right: 12px; bottom: 20px; background: none; border: none; color: #5a6180; cursor: pointer; padding: 6px; border-radius: 6px; }
.logout-btn:hover { color: #ff5a5a; background: rgba(255,90,90,0.1); }

/* ===== 主区域 ===== */
.main-area { flex: 1; margin-left: 230px; display: flex; flex-direction: column; }
.top-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 28px; background: #fff; border-bottom: 1px solid #eef0eb; }
.header-left { flex: 1; max-width: 360px; }
.search-box { display: flex; align-items: center; gap: 8px; background: #f5f7f2; border: 1px solid #e8ebe4; border-radius: 10px; padding: 8px 14px; transition: all 0.2s; }
.search-box:focus-within { border-color: #7CCF5F; box-shadow: 0 0 0 3px rgba(124,207,95,0.1); }
.search-icon { color: #9ca3af; flex-shrink: 0; }
.search-box input { border: none; background: transparent; outline: none; font-size: 14px; color: #1e1e1e; width: 100%; font-family: 'Inter', sans-serif; }
.search-box input::placeholder { color: #9ca3af; }
.header-right { display: flex; align-items: center; gap: 10px; }
.icon-btn { width: 38px; height: 38px; border-radius: 10px; border: none; background: #f5f7f2; color: #5a6180; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.icon-btn:hover { background: #e8ebe4; color: #1e1e1e; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 8px; transition: all 0.15s; }
.btn-outline { padding: 6px 14px; border: 1px solid #e8ebe4; background: transparent; color: #374151; font-size: 12px; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }
.btn-xs { font-size: 11px; padding: 5px 12px; }

.content-area { flex: 1; padding: 24px 28px; max-width: 1280px; }
</style>
