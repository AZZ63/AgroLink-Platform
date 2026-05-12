<template>
  <div class="merchant-dashboard">
    <NavBar />

    <div class="dash-container">
      <!-- Stats -->
      <section class="stats-row">
        <div class="stat-card rich-card" style="--card-accent: #00ff88">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap" style="background: rgba(0,255,136,0.12)">
              <span class="stat-icon">📦</span>
            </div>
            <div class="stat-body">
              <span class="stat-label">我的需求</span>
              <div class="stat-main">
                <span class="stat-value">{{ myDemands.length }}</span>
                <span class="stat-trend trend-up">较昨日 +3</span>
              </div>
            </div>
          </div>
        </div>
        <div class="stat-card rich-card" style="--card-accent: #4fc3f7">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap" style="background: rgba(79,195,247,0.12)">
              <span class="stat-icon">📋</span>
            </div>
            <div class="stat-body">
              <span class="stat-label">采购中的订单</span>
              <div class="stat-main">
                <span class="stat-value">{{ activeOrders.length }}</span>
                <span class="stat-subtitle">待处理 {{ pendingCount }} 单</span>
              </div>
            </div>
          </div>
        </div>
        <div class="stat-card rich-card" style="--card-accent: #a78bfa">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap" style="background: rgba(167,139,250,0.12)">
              <span class="stat-icon">✅</span>
            </div>
            <div class="stat-body">
              <span class="stat-label">已完成采购</span>
              <div class="stat-main">
                <span class="stat-value">{{ completedCount }}</span>
                <span class="stat-subtitle">完成率 {{ completionRate }}%</span>
              </div>
            </div>
          </div>
        </div>
        <div class="stat-card rich-card action-card" style="--card-accent: #ffb347">
          <div class="stat-card-inner">
            <div class="stat-icon-wrap action-icon-wrap" style="background: rgba(255,179,71,0.12)">
              <span class="stat-icon">🚀</span>
            </div>
            <div class="stat-body">
              <button class="tech-btn tech-btn-primary quick-action" @click="$router.push('/publish?type=DEMAND')">+ 发布需求</button>
            </div>
          </div>
        </div>
      </section>

      <div class="dash-grid">
        <!-- Procurement Market (Latest Supply) -->
        <section class="dash-section">
          <div class="section-head">
            <h2 class="tech-section-title" style="margin-bottom:0">采购市场</h2>
            <button class="tech-link" @click="$router.push('/products?infoType=SUPPLY')">查看全部 →</button>
          </div>
          <div v-if="loadingMarket" class="skeleton-list">
            <div v-for="i in 3" :key="i" class="skeleton-row"></div>
          </div>
          <div v-else-if="marketProducts.length === 0" class="empty-state">
            <p>暂无供应信息</p>
          </div>
          <div v-else class="product-list">
            <div v-for="p in marketProducts" :key="p.id" class="list-item">
              <div class="item-info">
                <div class="item-top">
                  <span class="tech-badge tech-badge-supply">供应</span>
                  <span class="item-region" v-if="p.province || p.city">{{ p.province }}{{ p.city }}</span>
                </div>
                <strong>{{ p.name }}</strong>
                <span class="item-meta">{{ p.quantity }}{{ p.unit }} · ¥{{ p.price }}/{{ p.unit }}</span>
              </div>
              <div class="item-actions">
                <button class="tech-btn-sm tech-btn-sm-primary" @click="quickOrder(p)">下单</button>
              </div>
            </div>
          </div>
        </section>

        <!-- My Purchase Orders -->
        <section class="dash-section">
          <div class="section-head">
            <h2 class="tech-section-title" style="margin-bottom:0">我的采购</h2>
            <button class="tech-link" @click="$router.push('/orders')">查看全部 →</button>
          </div>
          <div v-if="loadingOrders" class="skeleton-list">
            <div v-for="i in 3" :key="i" class="skeleton-row"></div>
          </div>
          <div v-else-if="buyOrders.length === 0" class="empty-state">
            <p>还没有采购订单</p>
            <button class="tech-btn tech-btn-primary" @click="$router.push('/products?infoType=SUPPLY')">去市场采购</button>
          </div>
          <div v-else class="product-list">
            <div v-for="o in buyOrders" :key="o.id" class="list-item">
              <div class="item-info">
                <div class="item-top">
                  <span class="tech-status" :class="'tech-status-' + (o.status || '').toLowerCase()">{{ orderStatusLabel(o.status) }}</span>
                </div>
                <strong>{{ o.productName }}</strong>
                <span class="item-meta">x{{ o.quantity }} · ¥{{ o.totalPrice }} · {{ o.sellerName || '卖家' + o.sellerId }}</span>
              </div>
              <div class="item-actions">
                <button v-if="o.status === 'CONFIRMED'" class="tech-btn-sm tech-btn-sm-primary" @click="confirmComplete(o)">确认收货</button>
                <button v-if="o.status === 'PENDING'" class="tech-btn-sm tech-btn-sm-danger" @click="cancelOrder(o)">取消</button>
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
              <h3 class="chart-title">近7日销量趋势</h3>
              <span class="chart-badge trend-up">+12%</span>
            </div>
            <div class="chart-wrap"><canvas ref="salesChartRef"></canvas></div>
          </div>
          <div class="chart-card">
            <div class="chart-card-header">
              <h3 class="chart-title">热门产品分类</h3>
            </div>
            <div class="chart-wrap"><canvas ref="categoryChartRef"></canvas></div>
          </div>
          <div class="chart-card">
            <div class="chart-card-header">
              <h3 class="chart-title">浏览趋势</h3>
              <span class="chart-badge trend-up">+8%</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { productApi, orderApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const userStore = useUserStore()
const marketProducts = ref([])
const loadingMarket = ref(true)
const buyOrders = ref([])
const loadingOrders = ref(true)
const myDemands = ref([])
const stats = ref(null)
const salesChartRef = ref(null)
const categoryChartRef = ref(null)
const viewsChartRef = ref(null)
let salesChart = null
let categoryChart = null
let viewsChart = null

function createCharts() {
  // Line: 近7日销量趋势
  if (salesChartRef.value) {
    salesChart = new Chart(salesChartRef.value, {
      type: 'line',
      data: {
        labels: ['5/6', '5/7', '5/8', '5/9', '5/10', '5/11', '5/12'],
        datasets: [{
          label: '销量',
          data: [12, 19, 8, 15, 22, 18, 25],
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
        responsive: true,
        maintainAspectRatio: false,
        color: '#8892a4',
        plugins: { legend: { display: false } },
        scales: {
          x: { ticks: { color: '#555d6e' }, grid: { color: 'rgba(255,255,255,0.04)' } },
          y: { ticks: { color: '#555d6e' }, grid: { color: 'rgba(255,255,255,0.04)' }, beginAtZero: true }
        }
      }
    })
  }

  // Doughnut: 热门产品分类
  if (categoryChartRef.value) {
    categoryChart = new Chart(categoryChartRef.value, {
      type: 'doughnut',
      data: {
        labels: ['水果', '蔬菜', '粮油', '其他'],
        datasets: [{
          data: [35, 28, 20, 17],
          backgroundColor: ['#00ff88', '#ffb347', '#4fc3f7', '#888fa4'],
          borderColor: '#161921',
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
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

  // Bar: 浏览趋势
  if (viewsChartRef.value) {
    viewsChart = new Chart(viewsChartRef.value, {
      type: 'bar',
      data: {
        labels: ['5/6', '5/7', '5/8', '5/9', '5/10', '5/11', '5/12'],
        datasets: [{
          label: '浏览量',
          data: [45, 52, 38, 60, 55, 70, 65],
          backgroundColor: 'rgba(0,255,136,0.3)',
          borderColor: '#00ff88',
          borderWidth: 1,
          borderRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        color: '#8892a4',
        plugins: { legend: { display: false } },
        scales: {
          x: { ticks: { color: '#555d6e' }, grid: { display: false } },
          y: { ticks: { color: '#555d6e' }, grid: { color: 'rgba(255,255,255,0.04)' }, beginAtZero: true }
        }
      }
    })
  }
}

const chartPeriod = ref(7)
const pendingCount = computed(() => buyOrders.value.filter(o => o.status === 'PENDING').length)
const completedCount = computed(() => buyOrders.value.filter(o => o.status === 'COMPLETED').length)
const activeOrders = computed(() => buyOrders.value.filter(o => o.status === 'PENDING' || o.status === 'CONFIRMED'))
const completionRate = computed(() => {
  const total = buyOrders.value.length
  return total > 0 ? Math.round((completedCount.value / total) * 100) : 0
})

function orderStatusLabel(s) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[s] || s || ''
}

async function quickOrder(p) {
  try {
    const { value: qty } = await ElMessageBox.prompt('下单数量', '采购 ' + p.name, {
      inputValue: 1,
      inputPlaceholder: '数量',
      inputType: 'number',
      confirmButtonText: '下单',
      cancelButtonText: '取消'
    })
    if (!qty || qty < 1) return
    await orderApi.create({
      productId: p.id,
      quantity: parseInt(qty),
      infoType: p.infoType
    })
    ElMessage.success('下单成功')
    // refresh orders
    const orders = await orderApi.getBuy()
    buyOrders.value = orders.slice(0, 10)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '下单失败')
  }
}

async function confirmComplete(o) {
  try { await orderApi.updateStatus(o.id, 'COMPLETED'); o.status = 'COMPLETED'; ElMessage.success('已确认收货') }
  catch (e) { ElMessage.error(e.message || '操作失败') }
}
async function cancelOrder(o) {
  try { await orderApi.updateStatus(o.id, 'CANCELLED'); o.status = 'CANCELLED'; ElMessage.success('已取消订单') }
  catch (e) { ElMessage.error(e.message || '操作失败') }
}

onMounted(async () => {
  try {
    const result = await productApi.query({ infoType: 'SUPPLY', status: 'LISTED', pageSize: 5 })
    marketProducts.value = result.records || result || []
  } catch {}
  finally { loadingMarket.value = false }

  try {
    const orders = await orderApi.getBuy()
    buyOrders.value = orders.slice(0, 10)
  } catch {}
  finally { loadingOrders.value = false }

  try { myDemands.value = await productApi.getMy() } catch {}
  try { stats.value = await productApi.getStats() } catch {}
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
.product-list { display: flex; flex-direction: column; gap: 8px; }
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: var(--bg-elevated, #1e2230);
  border-radius: 6px;
  gap: 12px;
}
.item-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.item-info strong { font-size: 15px; }
.item-top { display: flex; gap: 6px; align-items: center; }
.item-meta { font-size: 12px; color: var(--text-muted, #555d6e); }
.item-region { font-size: 11px; color: var(--text-muted, #555d6e); }
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
.empty-state { text-align: center; padding: 40px 20px; color: var(--text-muted, #555d6e); }
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

/* Charts */
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
