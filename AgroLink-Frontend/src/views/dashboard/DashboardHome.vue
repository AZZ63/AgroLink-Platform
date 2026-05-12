<template>
  <div class="dash-home">
    <!-- 页面标题 -->
    <div class="page-heading">
      <div>
        <h1 class="page-title">欢迎回来，{{ userStore.user?.username || '用户' }}</h1>
        <p class="page-subtitle">{{ greeting }}，这是{{ roleLabel }}的今日概览</p>
      </div>
      <div class="date-badge">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
        {{ todayStr }}
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <section class="stats-grid">
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">{{ isFarmer ? '今日订单' : '今日采购' }}</span>
          <div class="stat-icon stat-icon-order">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </div>
        </div>
        <div class="stat-value">{{ stats.todayOrders }}</div>
        <div class="stat-footer">
          <span class="stat-trend" :class="stats.orderTrend >= 0 ? 'up' : 'down'">
            {{ stats.orderTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(stats.orderTrend) }}%
          </span>
          <span class="stat-compare">较昨日</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">{{ isFarmer ? '本月收入' : '本月支出' }}</span>
          <div class="stat-icon stat-icon-revenue">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
          </div>
        </div>
        <div class="stat-value">¥{{ stats.monthlyRevenue }}</div>
        <div class="stat-footer">
          <span class="stat-trend" :class="stats.revenueTrend >= 0 ? 'up' : 'down'">
            {{ stats.revenueTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(stats.revenueTrend) }}%
          </span>
          <span class="stat-compare">较上月</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">{{ isFarmer ? '在线货源' : '在途订单' }}</span>
          <div class="stat-icon stat-icon-product">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
          </div>
        </div>
        <div class="stat-value">{{ stats.activeProducts }}</div>
        <div class="stat-footer">
          <span class="stat-trend up">● {{ stats.onlineRate }}%</span>
          <span class="stat-compare">上架率</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">{{ isFarmer ? '成交量' : '成交订单' }}</span>
          <div class="stat-icon stat-icon-trade">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          </div>
        </div>
        <div class="stat-value">{{ stats.completedOrders }}</div>
        <div class="stat-footer">
          <span class="stat-trend up">↑ {{ stats.completionRate }}%</span>
          <span class="stat-compare">完成率</span>
        </div>
      </div>
    </section>

    <!-- 中间区域：图表 + 信息卡片 -->
    <section class="middle-grid">
      <!-- 左侧：收入趋势图 -->
      <div class="card chart-card-large">
        <div class="card-header">
          <div>
            <h3 class="card-title">{{ isFarmer ? '收入趋势' : '采购趋势' }}</h3>
            <p class="card-subtitle">近7日数据</p>
          </div>
          <div class="period-switch">
            <button :class="['period-btn', { active: chartPeriod === 7 }]" @click="chartPeriod = 7; initCharts()">7日</button>
            <button :class="['period-btn', { active: chartPeriod === 30 }]" @click="chartPeriod = 30; initCharts()">30日</button>
          </div>
        </div>
        <div class="chart-container">
          <canvas ref="revenueChartRef"></canvas>
        </div>
      </div>

      <!-- 右侧：天气 + 行情 + 通知 -->
      <div class="right-panel">
        <!-- 天气卡片 -->
        <div class="card weather-card">
          <div class="weather-top">
            <div class="weather-icon">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7CCF5F" stroke-width="1.5"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
            </div>
            <div class="weather-info">
              <div class="weather-temp">23°C</div>
              <div class="weather-desc">晴</div>
            </div>
            <div class="weather-location">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              中国 · 云南
            </div>
          </div>
          <div class="weather-divider"></div>
          <div class="weather-footer">
            <div class="weather-stat">
              <span class="ws-label">湿度</span>
              <span class="ws-value">68%</span>
            </div>
            <div class="weather-stat">
              <span class="ws-label">降水</span>
              <span class="ws-value">5%</span>
            </div>
            <div class="weather-stat">
              <span class="ws-label">风速</span>
              <span class="ws-value">12km/h</span>
            </div>
          </div>
        </div>

        <!-- 市场行情 -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">农产品行情</h3>
            <button class="card-link" @click="$router.push('/products')">查看全部 →</button>
          </div>
          <div class="market-list">
            <div v-for="item in marketPrices" :key="item.name" class="market-item">
              <span class="market-name">{{ item.name }}</span>
              <div class="market-right">
                <span class="market-price">¥{{ item.price }}</span>
                <span :class="['market-change', item.change >= 0 ? 'up' : 'down']">
                  {{ item.change >= 0 ? '+' : '' }}{{ item.change }}%
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 快速通知 -->
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">快速通知</h3>
            <button class="card-link">查看全部 →</button>
          </div>
          <div class="notif-list">
            <div v-for="n in notifications" :key="n.id" class="notif-item">
              <div :class="['notif-dot-indicator', n.type]"></div>
              <div class="notif-content">
                <span class="notif-text">{{ n.text }}</span>
                <span class="notif-time">{{ n.time }}</span>
              </div>
            </div>
            <div v-if="notifications.length === 0" class="notif-empty">暂无新通知</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部区域：货源表格 + 订单时间轴 -->
    <section class="bottom-grid">
      <!-- 左侧：我的货源 -->
      <div class="card table-card">
        <div class="card-header">
          <div>
            <h3 class="card-title">{{ isFarmer ? '我的货源' : '我的需求' }}</h3>
            <p class="card-subtitle">最近 {{ products.length }} 条记录</p>
          </div>
          <button class="btn btn-outline btn-sm" @click="$router.push('/publish')">+ 发布</button>
        </div>

        <div class="table-wrap">
          <table class="dash-table">
            <thead>
              <tr>
                <th>名称</th>
                <th>{{ isFarmer ? '数量' : '需求' }}</th>
                <th>价格</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in products" :key="p.id">
                <td>
                  <div class="product-cell">
                    <div class="product-thumb">{{ p.name?.charAt(0) }}</div>
                    <span class="product-name">{{ p.name }}</span>
                  </div>
                </td>
                <td>{{ p.quantity }}{{ p.unit }}</td>
                <td>¥{{ p.price }}/{{ p.unit }}</td>
                <td><span :class="['status-tag', p.status?.toLowerCase()]">{{ statusLabel(p.status) }}</span></td>
                <td>
                  <button class="table-btn" @click="$router.push(`/products/${p.id}`)">详情</button>
                </td>
              </tr>
              <tr v-if="products.length === 0">
                <td colspan="5" class="empty-td">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 右侧：最近订单 -->
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">{{ isFarmer ? '卖出的订单' : '我的订单' }}</h3>
            <p class="card-subtitle">最近 {{ orders.length }} 条</p>
          </div>
          <button class="btn btn-outline btn-sm" @click="$router.push('/orders')">查看全部</button>
        </div>

        <div class="order-timeline">
          <div v-for="o in orders" :key="o.id" class="timeline-item">
            <div class="tl-dot" :class="o.status?.toLowerCase()"></div>
            <div class="tl-content">
              <div class="tl-header">
                <span class="tl-title">{{ o.productName || '订单#' + o.id }}</span>
                <span :class="['order-status-tag', o.status?.toLowerCase()]">{{ orderStatusLabel(o.status) }}</span>
              </div>
              <div class="tl-meta">
                <span>x{{ o.quantity }} · ¥{{ o.totalPrice }}</span>
                <span class="tl-time">{{ formatTime(o.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div v-if="orders.length === 0" class="empty-td" style="padding: 40px; text-align: center;">暂无订单</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { productApi, orderApi } from '../../api/http'
import { statusLabel } from '../../utils/constants'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const userStore = useUserStore()
const isFarmer = computed(() => userStore.isFarmer)
const roleLabel = computed(() => isFarmer.value ? '农户' : '商户')

const todayStr = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const chartPeriod = ref(7)
const revenueChartRef = ref(null)
let revenueChart = null

// 统计卡片数据
const stats = ref({
  todayOrders: 0,
  orderTrend: 12,
  monthlyRevenue: 0,
  revenueTrend: 8,
  activeProducts: 0,
  onlineRate: 85,
  completedOrders: 0,
  completionRate: 92
})

// 市场行情
const marketPrices = ref([
  { name: '大米', price: '3.80', change: 2.1 },
  { name: '小麦', price: '2.95', change: -0.5 },
  { name: '玉米', price: '2.40', change: 1.8 },
  { name: '大豆', price: '4.50', change: -1.2 },
  { name: '西红柿', price: '5.20', change: 3.5 },
])

// 通知
const notifications = ref([
  { id: 1, type: 'order', text: '您有新的订单待确认', time: '2分钟前' },
  { id: 2, type: 'system', text: '系统维护通知：今晚2:00-4:00', time: '1小时前' },
  { id: 3, type: 'price', text: '大米价格今日上涨2.1%', time: '3小时前' },
])

// 产品列表
const products = ref([])
const orders = ref([])

function formatTime(t) { return t ? t.substring(0, 10) : '' }

function orderStatusLabel(s) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消', PAID: '已付款' }
  return map[s] || s || ''
}

function initCharts() {
  if (revenueChart) { revenueChart.destroy(); revenueChart = null }
  if (!revenueChartRef.value) return

  const days = chartPeriod.value === 7
    ? ['5/6', '5/7', '5/8', '5/9', '5/10', '5/11', '5/12']
    : ['4/13', '4/20', '4/27', '5/4', '5/11']

  const data = chartPeriod.value === 7
    ? [2800, 4200, 3800, 5100, 4600, 6200, 5800]
    : [32000, 38000, 35000, 42000, 48000]

  revenueChart = new Chart(revenueChartRef.value, {
    type: 'line',
    data: {
      labels: days,
      datasets: [{
        label: isFarmer.value ? '收入 (¥)' : '采购额 (¥)',
        data: data,
        borderColor: '#7CCF5F',
        backgroundColor: (ctx) => {
          const g = ctx.chart.ctx.createLinearGradient(0, 0, 0, 300)
          g.addColorStop(0, 'rgba(124, 207, 95, 0.2)')
          g.addColorStop(1, 'rgba(124, 207, 95, 0)')
          return g
        },
        fill: true,
        tension: 0.4,
        pointBackgroundColor: '#7CCF5F',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6,
        borderWidth: 2.5,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: '#9ca3af', font: { size: 12, family: 'Inter' } }
        },
        y: {
          grid: { color: '#f0f0eb' },
          ticks: { color: '#9ca3af', font: { size: 12, family: 'Inter' }, callback: v => '¥' + v.toLocaleString() }
        }
      },
      interaction: { intersect: false, mode: 'index' }
    }
  })
}

// 加载数据
async function loadData() {
  try {
    const myProducts = await productApi.getMy()
    products.value = myProducts.slice(0, 5)
    // 统计
    const listed = myProducts.filter(p => p.status === 'LISTED').length
    stats.value.activeProducts = listed
    stats.value.onlineRate = myProducts.length ? Math.round(listed / myProducts.length * 100) : 0
  } catch {}

  try {
    const allOrders = isFarmer.value ? await orderApi.getSell() : await orderApi.getBuy()
    orders.value = allOrders.slice(0, 5)
    stats.value.todayOrders = allOrders.filter(o => {
      if (!o.createdAt) return false
      const today = new Date().toISOString().substring(0, 10)
      return o.createdAt.substring(0, 10) === today
    }).length
    stats.value.completedOrders = allOrders.filter(o => o.status === 'COMPLETED').length
    stats.value.completionRate = allOrders.length ? Math.round(stats.value.completedOrders / allOrders.length * 100) : 0
  } catch {}

  // 模拟收入数据
  stats.value.monthlyRevenue = isFarmer.value ? '12,800' : '8,600'
}

onMounted(() => {
  loadData()
  setTimeout(initCharts, 150)
})

onUnmounted(() => {
  if (revenueChart) revenueChart.destroy()
})
</script>

<style scoped>
.dash-home {
  max-width: 1200px;
}

/* ========== 页面标题 ========== */
.page-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e1e1e;
  margin: 0 0 6px;
  letter-spacing: -0.3px;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.date-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #ffffff;
  border: 1px solid #e8ebe4;
  border-radius: 10px;
  font-size: 13px;
  color: #6b7280;
  white-space: nowrap;
}

/* ========== 统计卡片 ========== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #e8ebe4;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.2s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  transform: translateY(-1px);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stat-label {
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon-order { background: #e8f5e4; color: #7CCF5F; }
.stat-icon-revenue { background: #e0f0ff; color: #3b82f6; }
.stat-icon-product { background: #fef3c7; color: #f59e0b; }
.stat-icon-trade { background: #f3e8ff; color: #8b5cf6; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e1e1e;
  font-family: 'JetBrains Mono', monospace;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.stat-footer {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-trend {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 6px;
}

.stat-trend.up { background: #e8f5e4; color: #7CCF5F; }
.stat-trend.down { background: #fee2e2; color: #ef4444; }

.stat-compare {
  font-size: 12px;
  color: #9ca3af;
}

/* ========== 中间区域 ========== */
.middle-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 20px;
  margin-bottom: 24px;
}

.card {
  background: #ffffff;
  border: 1px solid #e8ebe4;
  border-radius: 16px;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e1e1e;
  margin: 0;
}

.card-subtitle {
  font-size: 12px;
  color: #9ca3af;
  margin: 2px 0 0;
}

.card-link {
  background: none;
  border: none;
  color: #7CCF5F;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  font-family: 'Inter', sans-serif;
  white-space: nowrap;
}

.card-link:hover {
  color: #58b33a;
}

/* 图表 */
.chart-card-large {
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  min-height: 280px;
  position: relative;
}

.period-switch {
  display: flex;
  gap: 4px;
  background: #f5f7f2;
  border-radius: 8px;
  padding: 3px;
}

.period-btn {
  padding: 5px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #6b7280;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
  transition: all 0.15s;
}

.period-btn.active {
  background: #ffffff;
  color: #1e1e1e;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

/* 右侧面板 */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 天气卡片 */
.weather-card {
  background: linear-gradient(135deg, #f0f9ee 0%, #e8f5e4 100%);
  border-color: #d4edd0;
}

.weather-top {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.weather-icon {
  width: 56px;
  height: 56px;
  background: rgba(255,255,255,0.7);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.weather-info {
  flex: 1;
}

.weather-temp {
  font-size: 28px;
  font-weight: 700;
  color: #1e1e1e;
  line-height: 1.2;
}

.weather-desc {
  font-size: 14px;
  color: #6b7280;
}

.weather-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.weather-divider {
  height: 1px;
  background: rgba(0,0,0,0.06);
  margin: 14px 0;
}

.weather-footer {
  display: flex;
  justify-content: space-around;
}

.weather-stat {
  text-align: center;
}

.ws-label {
  display: block;
  font-size: 11px;
  color: #9ca3af;
  margin-bottom: 2px;
}

.ws-value {
  font-size: 14px;
  font-weight: 600;
  color: #1e1e1e;
}

/* 市场行情 */
.market-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.market-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0eb;
}

.market-item:last-child { border-bottom: none; }

.market-name {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

.market-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.market-price {
  font-size: 14px;
  font-weight: 600;
  color: #1e1e1e;
  font-family: 'JetBrains Mono', monospace;
}

.market-change {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  min-width: 48px;
  text-align: center;
}

.market-change.up { background: #e8f5e4; color: #7CCF5F; }
.market-change.down { background: #fee2e2; color: #ef4444; }

/* 通知 */
.notif-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notif-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0eb;
}

.notif-item:last-child { border-bottom: none; }

.notif-dot-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.notif-dot-indicator.order { background: #3b82f6; }
.notif-dot-indicator.system { background: #f59e0b; }
.notif-dot-indicator.price { background: #7CCF5F; }

.notif-content {
  flex: 1;
  min-width: 0;
}

.notif-text {
  display: block;
  font-size: 13px;
  color: #374151;
  line-height: 1.4;
}

.notif-time {
  font-size: 11px;
  color: #9ca3af;
}

.notif-empty {
  text-align: center;
  padding: 20px;
  color: #9ca3af;
  font-size: 13px;
}

/* ========== 底部区域 ========== */
.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.table-card {
  display: flex;
  flex-direction: column;
}

.table-wrap {
  overflow-x: auto;
  margin: 0 -20px -20px;
}

.dash-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.dash-table th {
  text-align: left;
  padding: 12px 20px;
  background: #f9faf8;
  color: #6b7280;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #e8ebe4;
}

.dash-table td {
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0eb;
  color: #374151;
}

.dash-table tr:last-child td { border-bottom: none; }
.dash-table tr:hover td { background: #f9faf8; }

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-thumb {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #e8f5e4;
  color: #7CCF5F;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.product-name {
  font-weight: 500;
  color: #1e1e1e;
}

.status-tag {
  display: inline-flex;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
}

.status-tag.listed { background: #e8f5e4; color: #7CCF5F; }
.status-tag.sold { background: #f0f0eb; color: #6b7280; }
.status-tag.unlisted { background: #fee2e2; color: #ef4444; }
.status-tag.pending { background: #fef3c7; color: #f59e0b; }
.status-tag.completed { background: #e8f5e4; color: #7CCF5F; }
.status-tag.closed { background: #f0f0eb; color: #9ca3af; }

.table-btn {
  padding: 4px 12px;
  border: 1px solid #e8ebe4;
  border-radius: 6px;
  background: transparent;
  color: #6b7280;
  font-size: 12px;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
  transition: all 0.15s;
}

.table-btn:hover {
  border-color: #7CCF5F;
  color: #7CCF5F;
}

.empty-td {
  text-align: center;
  color: #9ca3af;
  padding: 32px 20px !important;
}

/* ========== 订单时间轴 ========== */
.order-timeline {
  display: flex;
  flex-direction: column;
}

.timeline-item {
  display: flex;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0eb;
}

.timeline-item:last-child { border-bottom: none; }

.tl-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 5px;
  flex-shrink: 0;
  border: 2px solid #d1d5db;
}

.tl-dot.pending { border-color: #f59e0b; background: #fef3c7; }
.tl-dot.confirmed { border-color: #3b82f6; background: #dbeafe; }
.tl-dot.completed { border-color: #7CCF5F; background: #e8f5e4; }
.tl-dot.cancelled { border-color: #9ca3af; background: #f0f0eb; }

.tl-content {
  flex: 1;
  min-width: 0;
}

.tl-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.tl-title {
  font-size: 13px;
  font-weight: 600;
  color: #1e1e1e;
}

.order-status-tag {
  display: inline-flex;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
}

.order-status-tag.pending { background: #fef3c7; color: #f59e0b; }
.order-status-tag.confirmed { background: #dbeafe; color: #3b82f6; }
.order-status-tag.completed { background: #e8f5e4; color: #7CCF5F; }
.order-status-tag.cancelled { background: #f0f0eb; color: #9ca3af; }
.order-status-tag.paid { background: #dbeafe; color: #3b82f6; }

.tl-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #9ca3af;
}

/* ========== 通用按钮 ========== */
.btn {
  font-family: 'Inter', sans-serif;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-outline {
  padding: 8px 16px;
  border: 1px solid #e8ebe4;
  border-radius: 10px;
  background: transparent;
  color: #374151;
  font-size: 13px;
  font-weight: 500;
}

.btn-outline:hover {
  border-color: #7CCF5F;
  color: #7CCF5F;
}

.btn-sm {
  padding: 6px 14px;
  font-size: 12px;
}

/* ========== 响应式 ========== */
@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .middle-grid { grid-template-columns: 1fr; }
  .bottom-grid { grid-template-columns: 1fr; }
  .right-panel { display: grid; grid-template-columns: 1fr 1fr; }
}

@media (max-width: 768px) {
  .stats-grid { grid-template-columns: 1fr 1fr; }
  .right-panel { grid-template-columns: 1fr; }
  .page-heading { flex-direction: column; gap: 12px; }
}
</style>
