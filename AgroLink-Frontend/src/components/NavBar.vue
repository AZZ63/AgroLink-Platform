<template>
  <header class="tech-header">
    <div class="tech-header-inner">
      <span class="tech-logo" @click="goHome">AGRO<span>LINK</span></span>
      <nav class="tech-nav">
        <button class="tech-nav-btn" @click="$router.push('/')">首页</button>
        <button v-if="userStore.isLoggedIn" class="tech-nav-btn" @click="$router.push('/products')">市场</button>
        <button v-if="userStore.isFarmer" class="tech-nav-btn" @click="$router.push('/publish?type=SUPPLY')">发布供应</button>
        <button v-if="userStore.isMerchant" class="tech-nav-btn" @click="$router.push('/publish?type=DEMAND')">发布需求</button>
        <button v-if="userStore.isLoggedIn" class="tech-nav-btn" @click="goOrders">订单</button>
        <button v-if="userStore.isLoggedIn" class="tech-nav-btn" @click="$router.push('/favorites')">收藏</button>
        <button v-if="userStore.isLoggedIn" class="tech-nav-btn cart-btn" @click="$router.push('/cart')">🛒</button>
        <button v-if="userStore.isLoggedIn" class="tech-nav-btn" @click="$router.push('/addresses')">地址</button>
        <button v-if="userStore.isAdmin" class="tech-nav-btn admin-btn" @click="$router.push('/admin/users')">管理后台</button>
      </nav>
      <template v-if="userStore.isLoggedIn">
        <button class="notif-btn" @click="goNotifications" aria-label="通知">
          🔔
          <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </button>
        <button class="tech-nav-btn" @click="$router.push('/profile')">{{ userStore.user?.username }}</button>
        <button class="tech-btn-outline" @click="handleLogout">退出</button>
      </template>
      <template v-else>
        <button class="tech-btn-outline" @click="$router.push('/auth')">登录</button>
      </template>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { notifyApi } from '../api/http'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
let eventSource = null

function connectSSE() {
  if (eventSource) eventSource.close()
  const token = userStore.token
  if (!token) return
  eventSource = new EventSource(`/api/notify/stream?token=${token}`)
  eventSource.addEventListener('connected', () => {
    // SSE 连接建立后拉一次最新未读数
    fetchUnread()
  })
  eventSource.addEventListener('unread', (e) => {
    try {
      const data = JSON.parse(e.data)
      unreadCount.value = data.count || 0
    } catch {}
  })
  eventSource.addEventListener('error', () => {
    eventSource.close()
    eventSource = null
    // 断开后 10s 重连
    setTimeout(connectSSE, 10000)
  })
}

async function fetchUnread() {
  try {
    const res = await notifyApi.unread()
    unreadCount.value = res.count || 0
  } catch (e) { unreadCount.value = 0 }
}

watch(() => userStore.token, (newToken) => {
  if (newToken) connectSSE()
  else if (eventSource) { eventSource.close(); eventSource = null }
})

onMounted(() => {
  if (userStore.isLoggedIn) connectSSE()
})

onUnmounted(() => {
  if (eventSource) { eventSource.close(); eventSource = null }
})

function goHome() {
  if (!userStore.isLoggedIn) return router.push('/')
  if (userStore.isAdmin) return router.push('/admin/users')
  const path = userStore.isFarmer ? '/farmer/dashboard' : '/merchant/dashboard'
  router.push(path)
}

function goNotifications() {
  if (userStore.isAdmin) return router.push('/admin/users')
  const path = userStore.isFarmer ? '/farmer/dashboard' : '/merchant/dashboard'
  router.push(path)
}

function goOrders() {
  router.push('/orders')
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.tech-btn-outline {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #8892a4;
  font-family: 'Outfit', sans-serif;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.tech-btn-outline:hover {
  border-color: #00ff88;
  color: #00ff88;
}

.admin-btn {
  color: #ff6b35 !important;
}

.notif-btn {
  position: relative;
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 4px 8px;
  line-height: 1;
  transition: transform 0.2s;
  vertical-align: middle;
}
.notif-btn:hover {
  transform: scale(1.15);
}
.notif-badge {
  position: absolute;
  top: -2px;
  right: 0;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #ff4757;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  box-shadow: 0 0 6px rgba(255,71,87,0.4);
}
</style>
