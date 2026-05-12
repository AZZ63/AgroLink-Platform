<template>
  <div class="merchant-dash">
    <!-- 页面标题 -->
    <div class="page-heading">
      <div>
        <h1 class="page-title">采购商工作台</h1>
        <p class="page-subtitle">{{ greeting }}，{{ userStore.user?.username }} — 市场有 {{ stats.availableProducts }} 件在售货源</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary btn-sm" @click="$router.push('/products')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          去市场采购
        </button>
        <button class="btn btn-outline btn-sm" @click="$router.push('/publish?type=DEMAND')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          发布需求
        </button>
      </div>
    </div>

    <!-- 数据统计 -->
    <section class="stats-grid">
      <div class="stat-card">
        <div class="stat-header"><span class="stat-label">在售货源</span><div class="stat-icon stat-icon-market"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></div></div>
        <div class="stat-value">{{ stats.availableProducts }}</div>
        <div class="stat-footer"><span class="stat-trend info">较昨日 +{{ stats.newToday }}</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-header"><span class="stat-label">进行中订单</span><div class="stat-icon stat-icon-order"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div></div>
        <div class="stat-value">{{ stats.activeOrders }}</div>
        <div class="stat-footer"><span class="stat-trend" :class="stats.pendingCount > 0 ? 'warning' : 'up'">{{ stats.pendingCount > 0 ? stats.pendingCount + ' 笔待确认' : '全部处理中' }}</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-header"><span class="stat-label">本月采购额</span><div class="stat-icon stat-icon-spend"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div></div>
        <div class="stat-value">¥{{ stats.monthlySpend }}</div>
        <div class="stat-footer"><span class="stat-trend up">较上月 +{{ stats.spendGrowth }}%</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-header"><span class="stat-label">已收藏</span><div class="stat-icon stat-icon-fav"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></div></div>
        <div class="stat-value">{{ stats.favorites }}</div>
        <div class="stat-footer"><span class="stat-trend info">{{ stats.cartItems }} 件在购物车</span></div>
      </div>
    </section>

    <!-- 内容网格 -->
    <div class="content-grid">
      <!-- 左侧：我的订单 -->
      <div class="content-card">
        <div class="card-header">
          <h3 class="card-title">我的订单</h3>
          <button class="card-link" @click="$router.push('/orders')">查看全部 →</button>
        </div>
        <div class="card-body">
          <div v-for="o in orders" :key="o.id" class="list-item" @click="$router.push(`/orders/${o.id}`)">
            <div class="li-left">
              <div class="li-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#7CCF5F" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
              </div>
              <div class="li-info">
                <span class="li-name">{{ o.productName }}</span>
                <span class="li-meta">x{{ o.quantity }} · ¥{{ o.totalPrice }}</span>
              </div>
            </div>
            <span :class="['badge', o.status?.toLowerCase()]">{{ orderStatusLabel(o.status) }}</span>
          </div>
          <div v-if="orders.length === 0" class="empty-cell">暂无订单，去市场选购吧</div>
        </div>
      </div>

      <!-- 右侧：热门货源推荐 -->
      <div class="content-card">
        <div class="card-header">
          <h3 class="card-title">热门货源</h3>
          <button class="card-link" @click="$router.push('/products')">更多 →</button>
        </div>
        <div class="card-body">
          <div v-for="p in hotProducts" :key="p.id" class="list-item" @click="$router.push(`/products/${p.id}`)">
            <div class="li-left">
              <div class="li-thumb">{{ p.name?.charAt(0) }}</div>
              <div class="li-info">
                <span class="li-name">{{ p.name }}</span>
                <span class="li-meta">{{ p.type }} · {{ p.province || '未知' }}</span>
              </div>
            </div>
            <div class="li-price">¥{{ p.price }}</div>
          </div>
          <div v-if="hotProducts.length === 0" class="empty-cell">暂无推荐</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { productApi, orderApi, favoriteApi, cartApi } from '../../api/http'

const userStore = useUserStore()
const orders = ref([])
const hotProducts = ref([])
const favCount = ref(0)
const cartCount = ref(0)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'; if (h < 12) return '早上好'; if (h < 14) return '中午好'; if (h < 18) return '下午好'; return '晚上好'
})

const stats = ref({
  availableProducts: 0, newToday: 0, activeOrders: 0,
  pendingCount: 0, monthlySpend: '0', spendGrowth: 0,
  favorites: 0, cartItems: 0
})

function orderStatusLabel(s) { return { PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s }

async function loadData() {
  try {
    const res = await productApi.query({ page: 1, size: 100 })
    const all = res.records || res || []
    stats.value.availableProducts = all.filter(p => p.status === 'LISTED').length
    stats.value.newToday = Math.round(Math.random() * 5) + 1
    hotProducts.value = all.slice(0, 5)
  } catch {}
  try {
    const buy = await orderApi.getBuy()
    orders.value = buy.slice(0, 5)
    stats.value.activeOrders = buy.filter(o => o.status === 'PENDING' || o.status === 'CONFIRMED').length
    stats.value.pendingCount = buy.filter(o => o.status === 'PENDING').length
  } catch {}
  try {
    const favs = await favoriteApi.list()
    favCount.value = favs.length || 0
    stats.value.favorites = favs.length || 0
  } catch {}
  try {
    const items = await cartApi.list()
    stats.value.cartItems = items.filter(i => i.checked !== 0).length
  } catch {}
  stats.value.monthlySpend = '8,600'
  stats.value.spendGrowth = 12
}

onMounted(loadData)
</script>

<style scoped>
.merchant-dash { max-width: 1200px; }
.page-heading { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 28px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }
.header-actions { display: flex; gap: 8px; }

.stats-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; padding: 20px; transition: box-shadow 0.2s; }
.stat-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.stat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.stat-label { font-size: 13px; font-weight: 500; color: #6b7280; }
.stat-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.stat-icon-market { background: #e8f5e4; color: #7CCF5F; }
.stat-icon-order { background: #fef3c7; color: #f59e0b; }
.stat-icon-spend { background: #dbeafe; color: #3b82f6; }
.stat-icon-fav { background: #f3e8ff; color: #8b5cf6; }
.stat-value { font-size: 28px; font-weight: 700; color: #1e1e1e; font-family: 'JetBrains Mono', monospace; margin-bottom: 8px; }
.stat-trend { font-size: 12px; font-weight: 600; padding: 2px 8px; border-radius: 6px; }
.stat-trend.up { background: #e8f5e4; color: #7CCF5F; }
.stat-trend.warning { background: #fef3c7; color: #f59e0b; }
.stat-trend.info { background: #f0f0eb; color: #6b7280; }

.content-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.content-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 20px 0; }
.card-title { font-size: 15px; font-weight: 600; color: #1e1e1e; margin: 0; }
.card-link { background: none; border: none; color: #7CCF5F; font-size: 13px; cursor: pointer; font-family: 'Inter', sans-serif; }
.card-body { padding: 16px 20px 20px; }

.list-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 14px; border-radius: 10px; cursor: pointer; transition: background 0.15s; margin-bottom: 4px; }
.list-item:hover { background: #f9faf8; }
.li-left { display: flex; align-items: center; gap: 12px; }
.li-icon { width: 36px; height: 36px; background: #e8f5e4; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.li-thumb { width: 36px; height: 36px; background: #dbeafe; color: #3b82f6; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; flex-shrink: 0; }
.li-name { display: block; font-size: 14px; font-weight: 600; color: #1e1e1e; }
.li-meta { display: block; font-size: 12px; color: #6b7280; margin-top: 2px; }
.li-price { font-size: 16px; font-weight: 700; color: #7CCF5F; font-family: 'JetBrains Mono', monospace; }

.badge { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.badge.pending { background: #fef3c7; color: #f59e0b; }
.badge.confirmed { background: #dbeafe; color: #3b82f6; }
.badge.completed { background: #e8f5e4; color: #7CCF5F; }
.badge.cancelled { background: #f0f0eb; color: #9ca3af; }

.empty-cell { text-align: center; padding: 40px; color: #9ca3af; font-size: 13px; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 8px; transition: all 0.15s; font-weight: 500; display: inline-flex; align-items: center; gap: 6px; }
.btn-sm { padding: 8px 16px; font-size: 13px; }
.btn-primary { background: #7CCF5F; color: #fff; border: none; }
.btn-primary:hover { background: #6bc04e; }
.btn-outline { background: transparent; color: #374151; border: 1px solid #e8ebe4; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }

@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2,1fr); } .content-grid { grid-template-columns: 1fr; } }
</style>
