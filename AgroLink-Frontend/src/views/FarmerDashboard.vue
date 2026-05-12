<template>
  <div class="farmer-dashboard">
    <NavBar />

    <div class="dash-container">
      <!-- Stats -->
      <section class="stats-row">
        <div class="stat-card rich-card" style="--card-accent: #00ff88">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap" style="background: rgba(0,255,136,0.12)">
              <span class="stat-icon">🌾</span>
            </div>
            <div class="stat-body">
              <span class="stat-label">我的供应</span>
              <div class="stat-main">
                <span class="stat-value">{{ myProducts.length }}</span>
                <span class="stat-trend trend-up">较昨日 +1</span>
              </div>
            </div>
          </div>
        </div>
        <div class="stat-card rich-card" style="--card-accent: #4fc3f7">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap" style="background: rgba(79,195,247,0.12)">
              <span class="stat-icon">📊</span>
            </div>
            <div class="stat-body">
              <span class="stat-label">浏览量</span>
              <div class="stat-main">
                <span class="stat-value">{{ pageViews }}</span>
                <span class="stat-trend trend-up">+18%</span>
              </div>
            </div>
          </div>
        </div>
        <div class="stat-card rich-card" style="--card-accent: #ffb347">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap" style="background: rgba(255,179,71,0.12)">
              <span class="stat-icon">⭐</span>
            </div>
            <div class="stat-body">
              <span class="stat-label">好评率</span>
              <div class="stat-main">
                <span class="stat-value" style="color: var(--card-accent, #ffb347)">96%</span>
                <span class="stat-subtitle">优质供应商</span>
              </div>
            </div>
          </div>
        </div>
        <div class="stat-card rich-card action-card" style="--card-accent: #a78bfa">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap action-icon-wrap" style="background: rgba(167,139,250,0.12)">
              <span class="stat-icon">💰</span>
            </div>
            <div class="stat-body">
              <span class="stat-label">本月收入</span>
              <div class="stat-main">
                <span class="stat-value" style="color: var(--card-accent, #a78bfa)">¥12,800</span>
                <span class="stat-trend trend-up">较上月 +22%</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div class="dash-grid">
        <!-- My Products -->
        <section class="dash-section">
          <div class="section-head">
            <h2 class="tech-section-title" style="margin-bottom:0">我的货源</h2>
            <button class="tech-link" @click="$router.push('/products?infoType=SUPPLY')">查看全部 →</button>
          </div>
          <div v-if="loading" class="skeleton-list">
            <div v-for="i in 3" :key="i" class="skeleton-row"></div>
          </div>
          <div v-else-if="myProducts.length === 0" class="empty-state">
            <p>还没有发布供应信息</p>
            <button class="tech-btn tech-btn-primary" @click="$router.push('/publish?type=SUPPLY')">立即发布</button>
          </div>
          <div v-else class="product-list">
            <div v-for="p in myProducts" :key="p.id" class="list-item">
              <div class="item-info">
                <div class="item-top">
                  <span class="tech-badge tech-badge-supply">供应</span>
                  <span class="tech-status" :class="'tech-status-' + (p.status || '').toLowerCase()">{{ statusLabel(p.status) }}</span>
                </div>
                <strong>{{ p.name }}</strong>
                <span class="item-meta">{{ p.quantity }}{{ p.unit }} · ¥{{ p.price }}/{{ p.unit }}</span>
              </div>
              <div class="item-actions">
                <button v-if="p.status === 'LISTED'" class="tech-btn-sm" @click="toggleStatus(p)">下架</button>
                <button v-if="p.status === 'UNLISTED'" class="tech-btn-sm" @click="toggleStatus(p)">上架</button>
              </div>
            </div>
          </div>
        </section>

        <!-- Incoming Orders -->
        <section class="dash-section">
          <div class="section-head">
            <h2 class="tech-section-title" style="margin-bottom:0">订单处理</h2>
            <button class="tech-link" @click="$router.push('/orders')">查看全部 →</button>
          </div>
          <div v-if="loadingOrders" class="skeleton-list">
            <div v-for="i in 3" :key="i" class="skeleton-row"></div>
          </div>
          <div v-else-if="sellOrders.length === 0" class="empty-state">
            <p>暂无订单</p>
          </div>
          <div v-else class="product-list">
            <div v-for="o in sellOrders" :key="o.id" class="list-item">
              <div class="item-info">
                <div class="item-top">
                  <span class="tech-status" :class="'tech-status-' + (o.status || '').toLowerCase()">{{ orderStatusLabel(o.status) }}</span>
                </div>
                <strong>{{ o.productName }}</strong>
                <span class="item-meta">x{{ o.quantity }} · ¥{{ o.totalPrice }} · {{ o.buyerName || '买家' + o.buyerId }}</span>
              </div>
              <div class="item-actions">
                <button v-if="o.status === 'PENDING'" class="tech-btn-sm tech-btn-sm-primary" @click="confirmOrder(o)">确认</button>
                <button v-if="o.status === 'PENDING'" class="tech-btn-sm tech-btn-sm-danger" @click="cancelOrder(o)">取消</button>
                <button v-if="o.status === 'CONFIRMED'" class="tech-btn-sm tech-btn-sm-primary" @click="completeOrder(o)">完成</button>
              </div>
            </div>
          </div>
        </section>
      </div>

      <section class="chart-section">
        <div class="chart-section-header">
          <h2 class="tech-section-title" style="margin-bottom:0">数据趋势</h2>
          <div class="period-tabs">
            <button :class="['period-tab', { active: chartPeriod === 7 }]" @click="chartPeriod = 7">近7日</button>
            <button :class="['period-tab', { active: chartPeriod === 30 }]" @click="chartPeriod = 30">近30日</button>
          </div>
        </div>
        <div class="charts-grid">
          <div class="chart-card">
            <div class="chart-card-header">
              <h3 class="chart-title">近7日供应量趋势</h3>
              <span class="chart-badge trend-up">+15%</span>
            </div>
            <div class="chart-wrap"><canvas ref="salesChartRef"></canvas></div>
          </div>
          <div class="chart-card">
            <div class="chart-card-header">
              <h3 class="chart-title">产品分布</h3>
            </div>
            <div class="chart-wrap"><canvas ref="categoryChartRef"></canvas></div>
          </div>
          <div class="chart-card">
            <div class="chart-card-header">
              <h3 class="chart-title">近7日订单量</h3>
              <span class="chart-badge trend-up">+10%</span>
            </div>
            <div class="chart-wrap"><canvas ref="viewsChartRef"></canvas></div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { productApi, orderApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import { Chart, registerables } from 'chart.js'
import { statusLabel } from '../utils/constants'
Chart.register(...registerables)

const userStore = useUserStore()
const myProducts = ref([])
const loading = ref(true)

const sellOrders = ref([])
const loadingOrders = ref(true)

const completedCount = computed(() => sellOrders.value.filter(o => o.status === 'COMPLETED').length)
const activeOrders = computed(() => sellOrders.value.filter(o => o.status === 'PENDING' || o.status === 'CONFIRMED'))
const chartPeriod = ref(7)
const pageViews = ref(1280)

function orderStatusLabel(s) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[s] || s || ''
}

async function toggleStatus(p) {
  const newStatus = p.status === 'LISTED' ? 'UNLISTED' : 'LISTED'
  try {
    await productApi.updateStatus(p.id, newStatus)
    p.status = newStatus
    ElMessage.success(newStatus === 'LISTED' ? '已上架' : '已下架')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function confirmOrder(o) {
  try { await orderApi.updateStatus(o.id, 'CONFIRMED'); o.status = 'CONFIRMED'; ElMessage.success('已确认订单') }
  catch (e) { ElMessage.error(e.message || '操作失败') }
}
async function cancelOrder(o) {
  try { await orderApi.updateStatus(o.id, 'CANCELLED'); o.status = 'CANCELLED'; ElMessage.success('已取消订单') }
  catch (e) { ElMessage.error(e.message || '操作失败') }
}
async function completeOrder(o) {
  try { await orderApi.updateStatus(o.id, 'COMPLETED'); o.status = 'COMPLETED'; ElMessage.success('已完成订单') }
  catch (e) { ElMessage.error(e.message || '操作失败') }
}

const salesChartRef = ref(null)
const categoryChartRef = ref(null)
const viewsChartRef = ref(null)
let salesChart = null
let categoryChart = null
let viewsChart = null

function createCharts() {
  // Line: 近7日供应量趋势
  if (salesChartRef.value) {
    salesChart = new Chart(salesChartRef.value, {
      type: 'line',
      data: {
        labels: ['5/6', '5/7', '5/8', '5/9', '5/10', '5/11', '5/12'],
        datasets: [{
          label: '供应量',
          data: [8, 15, 12, 20, 18, 25, 22],
          borderColor: '#00ff88',
          backgroundColor: 'rgba(0,255,136,0.08)',
          fill: true,
          tension: 0.4,
          pointBackgroundColor: '#00ff88',
          pointBorderColor: '#00ff88',
          pointRadius: 3
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        color: '#8892a4', plugins: { legend: { display: false } },
        scales: {
          x: { ticks: { color: '#555d6e' }, grid: { color: 'rgba(255,255,255,0.04)' } },
          y: { ticks: { color: '#555d6e' }, grid: { color: 'rgba(255,255,255,0.04)' }, beginAtZero: true }
        }
      }
    })
  }

  // Doughnut: 产品分布
  if (categoryChartRef.value) {
    categoryChart = new Chart(categoryChartRef.value, {
      type: 'doughnut',
      data: {
        labels: ['叶菜', '根茎', '水果', '粮食', '其他'],
        datasets: [{
          data: [30, 25, 20, 15, 10],
          backgroundColor: ['#00ff88', '#ffb347', '#4fc3f7', '#a78bfa', '#888fa4'],
          borderColor: '#161921',
          borderWidth: 2
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        color: '#8892a4',
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#8892a4', padding: 12, boxWidth: 12, font: { size: 12 } }
          }
        }
      }
    })
  }

  // Bar: 近7日订单量
  if (viewsChartRef.value) {
    viewsChart = new Chart(viewsChartRef.value, {
      type: 'bar',
      data: {
        labels: ['5/6', '5/7', '5/8', '5/9', '5/10', '5/11', '5/12'],
        datasets: [{
          label: '订单量',
          data: [5, 8, 6, 12, 10, 15, 9],
          backgroundColor: 'rgba(0,255,136,0.3)',
          borderColor: '#00ff88',
          borderWidth: 1,
          borderRadius: 4
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        color: '#8892a4', plugins: { legend: { display: false } },
        scales: {
          x: { ticks: { color: '#555d6e' }, grid: { display: false } },
          y: { ticks: { color: '#555d6e' }, grid: { color: 'rgba(255,255,255,0.04)' }, beginAtZero: true }
        }
      }
    })
  }
}

onMounted(async () => {
  try { myProducts.value = await productApi.getMy() } catch {}
  finally { loading.value = false }

  try {
    const orders = await orderApi.getSell()
    sellOrders.value = orders.slice(0, 10)
  } catch {}
  finally { loadingOrders.value = false }

  setTimeout(createCharts, 100)
})

onUnmounted(() => {
  if (salesChart) salesChart.destroy()
  if (categoryChart) categoryChart.destroy()
  if (viewsChart) viewsChart.destroy()
})
</script>

<style scoped>
.dash-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px;
}

/* Stats */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}
/* Rich stat cards */
.rich-card {
  position: relative;
  overflow: hidden;
  background: var(--bg-card, #161921);
  border: 1px solid var(--border, #242837);
  border-radius: 10px;
  padding: 20px;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}
.rich-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--card-accent, #00ff88);
  border-radius: 10px 10px 0 0;
}
.rich-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 120% 120%, var(--card-accent, #00ff88) 0%, transparent 60%);
  opacity: 0.04;
  pointer-events: none;
}
.rich-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.3), 0 0 20px rgba(0,0,0,0.15);
  border-color: var(--card-accent, #00ff88);
}
.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1;
}
.stat-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}
.rich-card:hover .stat-icon-wrap {
  transform: scale(1.08);
}
.stat-icon {
  font-size: 22px;
  line-height: 1;
}
.stat-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}
.stat-label {
  font-size: 12px;
  color: var(--text-muted, #555d6e);
  font-weight: 500;
  letter-spacing: 0.5px;
}
.stat-main {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}
.stat-value {
  font-family: 'JetBrains Mono', monospace;
  font-size: 30px;
  font-weight: 600;
  color: var(--accent-green, #00ff88);
  line-height: 1.2;
}
.stat-trend {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
}
.stat-trend.trend-up {
  color: #00ff88;
  background: rgba(0,255,136,0.1);
}
.stat-subtitle {
  font-size: 12px;
  color: var(--text-muted, #555d6e);
  white-space: nowrap;
}
.action-card .quick-action {
  width: 100%;
  margin-top: 2px;
  font-size: 14px;
  padding: 10px 16px;
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* Grid */
.dash-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.dash-section {
  background: var(--bg-card, #161921);
  border: 1px solid var(--border, #242837);
  border-radius: 8px;
  padding: 24px;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.tech-link {
  background: none;
  border: none;
  color: var(--accent-green, #00ff88);
  font-family: 'Outfit', sans-serif;
  font-size: 13px;
  cursor: pointer;
}

/* Product list */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: var(--bg-elevated, #1e2230);
  border-radius: 6px;
  gap: 12px;
}
.item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.item-info strong { font-size: 15px; }
.item-top { display: flex; gap: 6px; align-items: center; }
.item-meta { font-size: 12px; color: var(--text-muted, #555d6e); }
.item-actions { display: flex; gap: 6px; flex-shrink: 0; }

.tech-btn-sm {
  padding: 6px 14px;
  border-radius: 4px;
  border: 1px solid var(--border, #242837);
  background: transparent;
  color: var(--text-secondary, #8892a4);
  font-family: 'Outfit', sans-serif;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.tech-btn-sm:hover { border-color: var(--accent-green, #00ff88); color: var(--accent-green, #00ff88); }
.tech-btn-sm-primary { border-color: var(--accent-green, #00ff88); color: var(--accent-green, #00ff88); }
.tech-btn-sm-primary:hover { background: rgba(0,255,136,0.1); }
.tech-btn-sm-danger { border-color: var(--accent-red, #ff3355); color: var(--accent-red, #ff3355); }
.tech-btn-sm-danger:hover { background: rgba(255,51,85,0.1); }

.tech-btn-primary {
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--accent-green, #00ff88);
  color: #000;
  padding: 14px 24px;
}
.tech-btn-primary:hover { background: #00cc6a; box-shadow: 0 0 20px rgba(0,255,136,0.3); }

/* Empty & loading */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-muted, #555d6e);
}
.empty-state p { margin-bottom: 16px; }
.skeleton-list { display: flex; flex-direction: column; gap: 8px; }
.skeleton-row {
  height: 56px;
  background: var(--bg-elevated, #1e2230);
  border-radius: 6px;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

.chart-section { margin-top: 28px; }
.chart-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.period-tabs {
  display: flex;
  gap: 4px;
  background: var(--bg-elevated, #1e2230);
  border-radius: 8px;
  padding: 3px;
}
.period-tab {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-muted, #555d6e);
  font-family: 'Outfit', sans-serif;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.period-tab:hover {
  color: var(--text-secondary, #8892a4);
}
.period-tab.active {
  background: var(--bg-card, #161921);
  color: var(--accent-green, #00ff88);
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.charts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.chart-card {
  background: var(--bg-card, #161921);
  border: 1px solid var(--border, #242837);
  border-radius: 10px;
  padding: 20px;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}
.chart-card:hover {
  border-color: rgba(0,255,136,0.25);
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}
.chart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.chart-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary, #8892a4);
  margin: 0;
}
.chart-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(0,255,136,0.1);
  color: #00ff88;
}
.chart-badge.trend-up {
  background: rgba(0,255,136,0.1);
  color: #00ff88;
}
.chart-card .chart-wrap {
  height: 220px;
  position: relative;
}
@media (max-width: 1100px) {
  .charts-grid { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 700px) {
  .charts-grid { grid-template-columns: 1fr; }
}

@media (max-width: 900px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .dash-grid { grid-template-columns: 1fr; }
}
</style>
