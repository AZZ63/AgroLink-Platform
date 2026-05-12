<template>
  <div class="farmer-dash">
    <!-- 页面标题 -->
    <div class="page-heading">
      <div>
        <h1 class="page-title">农户工作台</h1>
        <p class="page-subtitle">{{ greeting }}，{{ userStore.user?.username }} — 今日有 {{ stats.todayOrders }} 笔新订单</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary btn-sm" @click="$router.push('/publish?type=SUPPLY')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          发布新货源
        </button>
      </div>
    </div>

    <!-- 数据统计 -->
    <section class="stats-grid">
      <div class="stat-card">
        <div class="stat-header"><span class="stat-label">在线货源</span><div class="stat-icon stat-icon-product"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg></div></div>
        <div class="stat-value">{{ stats.activeProducts }}</div>
        <div class="stat-footer"><span class="stat-trend up">上架率 {{ stats.onlineRate }}%</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-header"><span class="stat-label">待处理订单</span><div class="stat-icon stat-icon-order"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div></div>
        <div class="stat-value">{{ stats.pendingOrders }}</div>
        <div class="stat-footer"><span class="stat-trend" :class="stats.pendingOrders > 0 ? 'warning' : 'up'">{{ stats.pendingOrders > 0 ? '待确认' : '全部处理' }}</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-header"><span class="stat-label">本月收入</span><div class="stat-icon stat-icon-revenue"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div></div>
        <div class="stat-value">¥{{ stats.monthlyRevenue }}</div>
        <div class="stat-footer"><span class="stat-trend up">较上月 +{{ stats.revenueGrowth }}%</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-header"><span class="stat-label">总成交量</span><div class="stat-icon stat-icon-trade"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg></div></div>
        <div class="stat-value">{{ stats.completedOrders }}</div>
        <div class="stat-footer"><span class="stat-trend up">完成率 {{ stats.completionRate }}%</span></div>
      </div>
    </section>

    <!-- 内容网格 -->
    <div class="content-grid">
      <!-- 左侧：我的货源 -->
      <div class="content-card">
        <div class="card-header">
          <h3 class="card-title">我的货源</h3>
          <button class="card-link" @click="$router.push('/products')">查看全部 →</button>
        </div>
        <div class="card-body">
          <div v-for="p in products" :key="p.id" class="list-item" @click="$router.push(`/products/${p.id}`)">
            <div class="li-left">
              <div class="li-thumb">{{ p.name?.charAt(0) }}</div>
              <div class="li-info">
                <span class="li-name">{{ p.name }}</span>
                <span class="li-meta">{{ p.quantity }}{{ p.unit }} · ¥{{ p.price }}/{{ p.unit }}</span>
              </div>
            </div>
            <div class="li-right">
              <span :class="['badge', p.status?.toLowerCase()]">{{ statusLabel(p.status) }}</span>
              <button v-if="p.status === 'LISTED'" class="btn btn-ghost btn-xs" @click.stop="toggleStatus(p)">下架</button>
              <button v-else-if="p.status === 'UNLISTED'" class="btn btn-ghost btn-xs" @click.stop="toggleStatus(p)">上架</button>
            </div>
          </div>
          <div v-if="products.length === 0" class="empty-cell">暂无货源，点击右上角发布</div>
        </div>
      </div>

      <!-- 右侧：订单动态 -->
      <div class="content-card">
        <div class="card-header">
          <h3 class="card-title">订单动态</h3>
          <button class="card-link" @click="$router.push('/orders')">查看全部 →</button>
        </div>
        <div class="card-body">
          <div v-for="o in orders" :key="o.id" class="timeline-item" @click="$router.push(`/orders/${o.id}`)">
            <div :class="['tl-dot', o.status?.toLowerCase()]"></div>
            <div class="tl-content">
              <div class="tl-header">
                <span class="tl-name">{{ o.productName }}</span>
                <span :class="['badge badge-sm', o.status?.toLowerCase()]">{{ orderStatusLabel(o.status) }}</span>
              </div>
              <div class="tl-meta">x{{ o.quantity }} · ¥{{ o.totalPrice }} · {{ formatTime(o.createdAt) }}</div>
            </div>
          </div>
          <div v-if="orders.length === 0" class="empty-cell">暂无订单</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { productApi, orderApi } from '../../api/http'
import { statusLabel } from '../../utils/constants'

const userStore = useUserStore()
const products = ref([])
const orders = ref([])

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'; if (h < 12) return '早上好'; if (h < 14) return '中午好'; if (h < 18) return '下午好'; return '晚上好'
})

const stats = ref({
  activeProducts: 0, onlineRate: 0, pendingOrders: 0,
  monthlyRevenue: '0', revenueGrowth: 0, completedOrders: 0, completionRate: 0, todayOrders: 0
})

function formatTime(t) { return t ? t.substring(0, 10) : '' }
function orderStatusLabel(s) { return { PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s }

async function toggleStatus(p) {
  const ns = p.status === 'LISTED' ? 'UNLISTED' : 'LISTED'
  try { await productApi.updateStatus(p.id, ns); p.status = ns; ElMessage.success(ns === 'LISTED' ? '已上架' : '已下架') }
  catch (e) { ElMessage.error(e.message) }
}

async function loadData() {
  try {
    const my = await productApi.getMy()
    products.value = my.slice(0, 5)
    const listed = my.filter(p => p.status === 'LISTED').length
    stats.value.activeProducts = listed
    stats.value.onlineRate = my.length ? Math.round(listed / my.length * 100) : 0
  } catch {}
  try {
    const sell = await orderApi.getSell()
    orders.value = sell.slice(0, 5)
    stats.value.pendingOrders = sell.filter(o => o.status === 'PENDING').length
    stats.value.completedOrders = sell.filter(o => o.status === 'COMPLETED').length
    stats.value.completionRate = sell.length ? Math.round(stats.value.completedOrders / sell.length * 100) : 0
    stats.value.todayOrders = sell.filter(o => o.createdAt?.substring(0, 10) === new Date().toISOString().substring(0, 10)).length
  } catch {}
  stats.value.monthlyRevenue = '12,800'
  stats.value.revenueGrowth = 15
}

onMounted(loadData)
</script>

<style scoped>
.farmer-dash { max-width: 1200px; }
.page-heading { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 28px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

/* 统计卡片 */
.stats-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; padding: 20px; transition: box-shadow 0.2s; }
.stat-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.stat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.stat-label { font-size: 13px; font-weight: 500; color: #6b7280; }
.stat-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.stat-icon-product { background: #e8f5e4; color: #7CCF5F; }
.stat-icon-order { background: #fef3c7; color: #f59e0b; }
.stat-icon-revenue { background: #dbeafe; color: #3b82f6; }
.stat-icon-trade { background: #f3e8ff; color: #8b5cf6; }
.stat-value { font-size: 28px; font-weight: 700; color: #1e1e1e; font-family: 'JetBrains Mono', monospace; margin-bottom: 8px; }
.stat-footer { }
.stat-trend { font-size: 12px; font-weight: 600; padding: 2px 8px; border-radius: 6px; }
.stat-trend.up { background: #e8f5e4; color: #7CCF5F; }
.stat-trend.warning { background: #fef3c7; color: #f59e0b; }

/* 内容网格 */
.content-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.content-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 20px 0; }
.card-title { font-size: 15px; font-weight: 600; color: #1e1e1e; margin: 0; }
.card-link { background: none; border: none; color: #7CCF5F; font-size: 13px; cursor: pointer; font-family: 'Inter', sans-serif; }
.card-body { padding: 16px 20px 20px; }

.list-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 14px; border-radius: 10px; cursor: pointer; transition: background 0.15s; margin-bottom: 4px; }
.list-item:hover { background: #f9faf8; }
.li-left { display: flex; align-items: center; gap: 12px; }
.li-thumb { width: 36px; height: 36px; background: #e8f5e4; color: #7CCF5F; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; flex-shrink: 0; }
.li-name { display: block; font-size: 14px; font-weight: 600; color: #1e1e1e; }
.li-meta { display: block; font-size: 12px; color: #6b7280; margin-top: 2px; }
.li-right { display: flex; align-items: center; gap: 8px; }

.badge { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.badge.listed { background: #e8f5e4; color: #7CCF5F; }
.badge.unlisted { background: #fee2e2; color: #ef4444; }
.badge.pending { background: #fef3c7; color: #f59e0b; }
.badge.completed { background: #e8f5e4; color: #7CCF5F; }
.badge.confirmed { background: #dbeafe; color: #3b82f6; }
.badge.cancelled { background: #f0f0eb; color: #9ca3af; }

/* 时间轴 */
.timeline-item { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px solid #f0f0eb; cursor: pointer; }
.timeline-item:last-child { border-bottom: none; }
.tl-dot { width: 10px; height: 10px; border-radius: 50%; margin-top: 5px; flex-shrink: 0; border: 2px solid #d1d5db; }
.tl-dot.pending { border-color: #f59e0b; background: #fef3c7; }
.tl-dot.confirmed { border-color: #3b82f6; background: #dbeafe; }
.tl-dot.completed { border-color: #7CCF5F; background: #e8f5e4; }
.tl-dot.cancelled { border-color: #9ca3af; background: #f0f0eb; }
.tl-content { flex: 1; }
.tl-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
.tl-name { font-size: 13px; font-weight: 600; color: #1e1e1e; }
.tl-meta { font-size: 12px; color: #9ca3af; }

.empty-cell { text-align: center; padding: 40px; color: #9ca3af; font-size: 13px; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 8px; transition: all 0.15s; font-weight: 500; display: inline-flex; align-items: center; gap: 6px; }
.btn-sm { padding: 8px 16px; font-size: 13px; }
.btn-xs { padding: 4px 10px; font-size: 11px; border: 1px solid #e8ebe4; background: transparent; color: #6b7280; }
.btn-xs:hover { border-color: #7CCF5F; color: #7CCF5F; }
.btn-primary { background: #7CCF5F; color: #fff; border: none; }
.btn-primary:hover { background: #6bc04e; }

@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2,1fr); } .content-grid { grid-template-columns: 1fr; } }
</style>
