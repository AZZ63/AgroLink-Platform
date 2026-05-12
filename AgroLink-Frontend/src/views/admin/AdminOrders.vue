<template>
  <div class="admin-page">
    <div class="page-heading">
      <h1 class="page-title">订单监管</h1>
      <p class="page-subtitle">全平台订单监控与管理</p>
      <div class="header-tabs">
        <button :class="['tab',{active:tab==='all'}]" @click="tab='all'">全部</button>
        <button v-for="st in statusFilters" :key="st.v" :class="['tab',{active:tab===st.v}]" @click="tab=st.v">{{ st.l }}</button>
      </div>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <div class="search-box"><input v-model="keyword" placeholder="订单ID / 商品名..." @keyup.enter="loadAll()" /></div>
      </div>
      <div class="table-wrap">
        <table class="dash-table">
          <thead><tr><th>订单ID</th><th>商品</th><th>类型</th><th>买家</th><th>卖家</th><th>数量</th><th>总价</th><th>状态</th><th>时间</th></tr></thead>
          <tbody>
            <tr v-for="o in filteredOrders" :key="o.id">
              <td class="mono">#{{ o.id }}</td>
              <td><span class="product-name">{{ o.productName }}</span></td>
              <td><span :class="['badge', o.infoType==='SUPPLY'?'supply':'demand']">{{ o.infoType==='SUPPLY'?'供应':'需求' }}</span></td>
              <td>{{ o.buyerName || o.buyerId }}</td>
              <td>{{ o.sellerName || o.sellerId }}</td>
              <td>x{{ o.quantity }}</td>
              <td class="mono">¥{{ o.totalPrice }}</td>
              <td><span :class="['badge', o.status?.toLowerCase()]">{{ orderStatusLabel(o.status) }}</span></td>
              <td class="mono time">{{ formatTime(o.createdAt) }}</td>
            </tr>
            <tr v-if="filteredOrders.length===0"><td colspan="9" class="empty-cell">暂无订单</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '../../api/http'

const keyword = ref('')
const tab = ref('all')
const orders = ref([])
const statusFilters = ref([{ v: 'PENDING', l: '待确认' }, { v: 'CONFIRMED', l: '已确认' }, { v: 'COMPLETED', l: '已完成' }, { v: 'CANCELLED', l: '已取消' }])

function orderStatusLabel(s) { return { PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消', PAID: '已付款' }[s] || s }
function formatTime(t) { return t ? t.substring(0, 16).replace('T', ' ') : '' }

const filteredOrders = computed(() => {
  let list = orders.value
  if (tab.value !== 'all') list = list.filter(o => o.status === tab.value)
  if (keyword.value) { const kw = keyword.value.toLowerCase(); list = list.filter(o => String(o.id).includes(kw) || o.productName?.toLowerCase().includes(kw)) }
  return list
})

async function loadAll() {
  try {
    const [sell, buy] = await Promise.all([orderApi.getSell().catch(() => []), orderApi.getBuy().catch(() => [])])
    const map = new Map()
    for (const o of [...sell, ...buy]) map.set(o.id, o)
    orders.value = [...map.values()].sort((a, b) => (b.id || 0) - (a.id || 0))
  } catch { ElMessage.error('加载失败') }
}

onMounted(loadAll)
</script>

<style scoped>
.admin-page { max-width: 1200px; }
.page-heading { margin-bottom: 20px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0 0 12px; }
.header-tabs { display: flex; gap: 4px; background: #f5f7f2; border-radius: 10px; padding: 3px; width: fit-content; }
.tab { padding: 7px 16px; border: none; border-radius: 8px; font-size: 12px; font-weight: 500; color: #6b7280; cursor: pointer; font-family: 'Inter', sans-serif; background: transparent; }
.tab.active { background: #fff; color: #1e1e1e; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

.table-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; overflow: hidden; }
.table-toolbar { padding: 16px 20px; border-bottom: 1px solid #f0f0eb; }
.search-box input { width: 280px; max-width: 100%; padding: 8px 14px; border: 1px solid #e8ebe4; border-radius: 8px; font-size: 13px; outline: none; font-family: 'Inter', sans-serif; background: #f9faf8; }
.search-box input:focus { border-color: #7CCF5F; }

.table-wrap { overflow-x: auto; }
.dash-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.dash-table th { text-align: left; padding: 12px 20px; background: #f9faf8; color: #6b7280; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e8ebe4; }
.dash-table td { padding: 12px 20px; border-bottom: 1px solid #f0f0eb; color: #374151; }
.dash-table tr:hover td { background: #f9faf8; }
.mono { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #6b7280; }
.time { font-size: 11px; }
.product-name { font-weight: 500; color: #1e1e1e; }

.badge { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.badge.pending { background: #fef3c7; color: #f59e0b; }
.badge.confirmed { background: #dbeafe; color: #3b82f6; }
.badge.completed { background: #e8f5e4; color: #7CCF5F; }
.badge.cancelled { background: #f0f0eb; color: #9ca3af; }
.badge.supply { background: #e8f5e4; color: #7CCF5F; }
.badge.demand { background: #fef3c7; color: #f59e0b; }

.empty-cell { text-align: center; color: #9ca3af; padding: 40px !important; }
</style>
