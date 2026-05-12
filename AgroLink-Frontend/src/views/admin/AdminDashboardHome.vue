<template>
  <div class="admin-dash">
    <div class="page-heading">
      <h1 class="page-title">数据总览</h1>
      <p class="page-subtitle">平台运营核心指标</p>
    </div>

    <!-- 核心指标 -->
    <section class="stats-grid">
      <div class="stat-card"><div class="stat-header"><span class="stat-label">注册用户</span><div class="stat-icon si-user"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg></div></div><div class="stat-value">{{ stats.users }}</div><div class="stat-footer"><span class="stat-trend up">↑ {{ stats.userGrowth }}%</span> <span class="stat-compare">较上月</span></div></div>
      <div class="stat-card"><div class="stat-header"><span class="stat-label">在线商品</span><div class="stat-icon si-product"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg></div></div><div class="stat-value">{{ stats.products }}</div><div class="stat-footer"><span class="stat-trend up">↑ {{ stats.productGrowth }}%</span> <span class="stat-compare">较上月</span></div></div>
      <div class="stat-card"><div class="stat-header"><span class="stat-label">总订单</span><div class="stat-icon si-order"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div></div><div class="stat-value">{{ stats.orders }}</div><div class="stat-footer"><span class="stat-trend up">↑ {{ stats.orderGrowth }}%</span> <span class="stat-compare">较上月</span></div></div>
      <div class="stat-card"><div class="stat-header"><span class="stat-label">交易总额</span><div class="stat-icon si-revenue"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div></div><div class="stat-value">¥{{ stats.revenue }}</div><div class="stat-footer"><span class="stat-trend up">↑ {{ stats.revenueGrowth }}%</span> <span class="stat-compare">较上月</span></div></div>
    </section>

    <!-- 图表区域 -->
    <section class="charts-grid">
      <div class="chart-card"><div class="card-header"><h3>交易趋势</h3><div class="period-switch"><button :class="['per-btn',{active:p===chartP}]" v-for="p in [7,30]" :key="p" @click="chartP=p;initChart()">{{ p }}日</button></div></div><div class="chart-wrap"><canvas ref="chartRef"></canvas></div></div>
      <div class="chart-card"><div class="card-header"><h3>用户分布</h3></div><div class="chart-wrap"><canvas ref="pieRef"></canvas></div></div>
    </section>

    <!-- 底部：最近动态 + 地区统计 -->
    <section class="bottom-grid">
      <div class="info-card">
        <div class="card-header"><h3>最近动态</h3></div>
        <div class="list">
          <div v-for="a in activities" :key="a.t" class="list-row">
            <span :class="['act-dot', a.type]"></span>
            <span class="act-text">{{ a.text }}</span>
            <span class="act-time">{{ a.t }}</span>
          </div>
        </div>
      </div>
      <div class="info-card">
        <div class="card-header"><h3>地区分布 TOP5</h3></div>
        <div class="list">
          <div v-for="(v,k) in regionStats" :key="k" class="list-row">
            <span class="region-name">{{ k }}</span>
            <div class="region-bar-bg"><div class="region-bar" :style="{width: v.pct+'%'}"></div></div>
            <span class="region-val">{{ v.count }}</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

const chartRef = ref(null)
const pieRef = ref(null)
let lineChart = null
let pieChart = null
const chartP = ref(7)

const stats = ref({ users: 128, userGrowth: 12, products: 356, productGrowth: 8, orders: 892, orderGrowth: 23, revenue: '458,600', revenueGrowth: 18 })

const activities = ref([
  { text: '新用户 farmer_888 注册', type: 'user', t: '5分钟前' },
  { text: '订单 #239 已完成', type: 'order', t: '12分钟前' },
  { text: '商品 "有机大米" 审核通过', type: 'audit', t: '25分钟前' },
  { text: '用户 merchant_55 发起退款', type: 'refund', t: '1小时前' },
  { text: '系统自动下架 3 件过期商品', type: 'system', t: '2小时前' },
])

const regionStats = ref({ 云南: { count: 86, pct: 100 }, 山东: { count: 72, pct: 84 }, 河南: { count: 58, pct: 67 }, 四川: { count: 45, pct: 52 }, 江苏: { count: 38, pct: 44 } })

function initChart() {
  if (lineChart) lineChart.destroy()
  if (!chartRef.value) return
  const days = chartP.value === 7 ? ['5/6','5/7','5/8','5/9','5/10','5/11','5/12'] : ['4/13','4/20','4/27','5/4','5/11']
  const data = chartP.value === 7 ? [12,19,15,22,18,25,20] : [68,72,55,80,75]
  lineChart = new Chart(chartRef.value, {
    type: 'line', data: {
      labels: days,
      datasets: [{
        label: '交易额(¥K)', data, borderColor: '#7CCF5F', backgroundColor: c => { const g = c.chart.ctx.createLinearGradient(0,0,0,200); g.addColorStop(0,'rgba(124,207,95,0.2)'); g.addColorStop(1,'rgba(124,207,95,0)'); return g },
        fill: true, tension: 0.4, pointBackgroundColor: '#7CCF5F', pointBorderColor: '#fff', pointBorderWidth: 2, pointRadius: 4, borderWidth: 2.5,
      }]
    }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { x: { grid: { display: false }, ticks: { color: '#9ca3af', font: { size: 11 } } }, y: { grid: { color: '#f0f0eb' }, ticks: { color: '#9ca3af', font: { size: 11 } } } }, interaction: { intersect: false, mode: 'index' } }
  })

  if (pieRef.value) {
    if (pieChart) pieChart.destroy()
    pieChart = new Chart(pieRef.value, {
      type: 'doughnut', data: {
        labels: ['农户','商户','管理员'],
        datasets: [{ data: [68, 28, 4], backgroundColor: ['#7CCF5F','#3b82f6','#f59e0b'], borderColor: '#fff', borderWidth: 3 }]
      }, options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { color: '#6b7280', padding: 12, boxWidth: 12, font: { size: 11 } } } }, cutout: '65%' }
    })
  }
}

onMounted(() => setTimeout(initChart, 100))
onUnmounted(() => { if (lineChart) lineChart.destroy(); if (pieChart) pieChart.destroy() })
</script>

<style scoped>
.admin-dash { max-width: 1200px; }
.page-heading { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 4px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.stats-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; padding: 20px; }
.stat-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.stat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.stat-label { font-size: 13px; font-weight: 500; color: #6b7280; }
.stat-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.si-user { background: #e8f5e4; color: #7CCF5F; }
.si-product { background: #dbeafe; color: #3b82f6; }
.si-order { background: #fef3c7; color: #f59e0b; }
.si-revenue { background: #f3e8ff; color: #8b5cf6; }
.stat-value { font-size: 28px; font-weight: 700; color: #1e1e1e; font-family: 'JetBrains Mono', monospace; margin-bottom: 8px; }
.stat-trend { font-size: 12px; font-weight: 600; padding: 2px 8px; border-radius: 6px; }
.stat-trend.up { background: #e8f5e4; color: #7CCF5F; }
.stat-compare { font-size: 11px; color: #9ca3af; margin-left: 6px; }

.charts-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-bottom: 24px; }
.chart-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.card-header h3 { font-size: 15px; font-weight: 600; color: #1e1e1e; margin: 0; }
.chart-wrap { height: 260px; position: relative; }

.period-switch { display: flex; gap: 4px; background: #f5f7f2; border-radius: 8px; padding: 3px; }
.per-btn { padding: 5px 12px; border: none; border-radius: 6px; background: transparent; color: #6b7280; font-size: 12px; cursor: pointer; font-family: 'Inter', sans-serif; }
.per-btn.active { background: #fff; color: #1e1e1e; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

.bottom-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.info-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; padding: 20px; }
.list { }
.list-row { display: flex; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid #f0f0eb; font-size: 13px; }
.list-row:last-child { border-bottom: none; }
.act-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.act-dot.user { background: #7CCF5F; }
.act-dot.order { background: #3b82f6; }
.act-dot.audit { background: #f59e0b; }
.act-dot.refund { background: #ef4444; }
.act-dot.system { background: #6b7280; }
.act-text { flex: 1; color: #374151; }
.act-time { color: #9ca3af; font-size: 12px; white-space: nowrap; }

.region-name { width: 40px; font-weight: 500; color: #374151; }
.region-bar-bg { flex: 1; height: 8px; background: #f0f0eb; border-radius: 4px; overflow: hidden; }
.region-bar { height: 100%; background: #7CCF5F; border-radius: 4px; }
.region-val { width: 30px; text-align: right; color: #6b7280; font-family: 'JetBrains Mono', monospace; font-size: 12px; }

@media (max-width: 900px) { .stats-grid { grid-template-columns: repeat(2,1fr); } .charts-grid, .bottom-grid { grid-template-columns: 1fr; } }
</style>
