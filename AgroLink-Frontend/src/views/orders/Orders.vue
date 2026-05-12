<template>
  <div class="orders-page">
    <div class="page-heading">
      <h1 class="page-title">我的订单</h1>
      <p class="page-subtitle">管理所有采购和销售订单</p>
    </div>

    <!-- 标签切换 -->
    <div class="tabs-bar">
      <button :class="['tab', { active: tab === 'all' }]" @click="tab = 'all'">全部</button>
      <button :class="['tab', { active: tab === 'PENDING' }]" @click="tab = 'PENDING'">待确认</button>
      <button :class="['tab', { active: tab === 'CONFIRMED' }]" @click="tab = 'CONFIRMED'">已确认</button>
      <button :class="['tab', { active: tab === 'COMPLETED' }]" @click="tab = 'COMPLETED'">已完成</button>
      <button :class="['tab', { active: tab === 'CANCELLED' }]" @click="tab = 'CANCELLED'">已取消</button>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else>
      <div v-for="o in filteredOrders" :key="o.id" class="order-card" @click="$router.push(`/orders/${o.id}`)">
        <div class="order-header">
          <div class="order-id">#{{ o.id }}</div>
          <div class="order-status">
            <span :class="['status-tag', o.status?.toLowerCase()]">{{ orderStatusLabel(o.status) }}</span>
          </div>
        </div>
        <div class="order-body">
          <div class="order-product">
            <div class="op-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#7CCF5F" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
            </div>
            <div>
              <div class="op-name">{{ o.productName }}</div>
              <div class="op-meta">{{ o.infoType === 'SUPPLY' ? '供应' : '需求' }} · x{{ o.quantity }}</div>
            </div>
          </div>
          <div class="order-amount">
            <div class="oa-price">¥{{ o.totalPrice }}</div>
            <div class="oa-label">总价</div>
          </div>
        </div>
        <div class="order-footer">
          <span class="of-time">{{ formatTime(o.createdAt) }}</span>
          <div class="of-actions">
            <button v-if="o.status === 'PENDING'" class="btn btn-sm btn-primary" @click.stop="handleConfirm(o)">确认</button>
            <button v-if="o.status === 'PENDING'" class="btn btn-sm btn-ghost" @click.stop="handleCancel(o)">取消</button>
            <button v-if="o.status === 'CONFIRMED'" class="btn btn-sm btn-primary" @click.stop="handleComplete(o)">完成</button>
          </div>
        </div>
      </div>
      <div v-if="filteredOrders.length === 0" class="empty-state">暂无订单</div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { orderApi } from '../../api/http'

const userStore = useUserStore()
const orders = ref([])
const loading = ref(true)
const tab = ref('all')

const isFarmer = computed(() => userStore.isFarmer)
const filteredOrders = computed(() => {
  if (tab.value === 'all') return orders.value
  return orders.value.filter(o => o.status === tab.value)
})

function orderStatusLabel(s) { return { PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消', PAID: '已付款' }[s] || s }
function formatTime(t) { return t ? t.substring(0, 16).replace('T', ' ') : '' }

async function loadOrders() {
  loading.value = true
  try { orders.value = isFarmer.value ? await orderApi.getSell() : await orderApi.getBuy() }
  catch { ElMessage.error('加载订单失败') }
  finally { loading.value = false }
}

async function handleConfirm(o) { try { await orderApi.updateStatus(o.id, 'CONFIRMED'); o.status = 'CONFIRMED'; ElMessage.success('已确认') } catch (e) { ElMessage.error(e.message) } }
async function handleCancel(o) { try { await orderApi.updateStatus(o.id, 'CANCELLED'); o.status = 'CANCELLED'; ElMessage.success('已取消') } catch (e) { ElMessage.error(e.message) } }
async function handleComplete(o) { try { await orderApi.updateStatus(o.id, 'COMPLETED'); o.status = 'COMPLETED'; ElMessage.success('已完成') } catch (e) { ElMessage.error(e.message) } }

onMounted(loadOrders)
</script>

<style scoped>
.orders-page { max-width: 900px; }
.page-heading { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.tabs-bar { display: flex; gap: 4px; margin-bottom: 24px; background: #f5f7f2; border-radius: 10px; padding: 3px; width: fit-content; }
.tab { padding: 8px 18px; border: none; border-radius: 8px; font-size: 13px; font-weight: 500; color: #6b7280; cursor: pointer; font-family: 'Inter', sans-serif; background: transparent; }
.tab.active { background: #ffffff; color: #1e1e1e; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.tab:hover:not(.active) { color: #374151; }

.loading-state { text-align: center; padding: 60px; color: #6b7280; }
.empty-state { text-align: center; padding: 60px; color: #6b7280; }

.order-card {
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 16px; padding: 20px;
  margin-bottom: 12px; cursor: pointer; transition: all 0.2s;
}
.order-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }

.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.order-id { font-family: 'JetBrains Mono', monospace; font-size: 13px; color: #6b7280; }

.status-tag { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.status-tag.pending { background: #fef3c7; color: #f59e0b; }
.status-tag.confirmed { background: #dbeafe; color: #3b82f6; }
.status-tag.completed { background: #e8f5e4; color: #7CCF5F; }
.status-tag.cancelled { background: #f0f0eb; color: #9ca3af; }
.status-tag.paid { background: #dbeafe; color: #3b82f6; }

.order-body { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.order-product { display: flex; align-items: center; gap: 12px; }
.op-icon { width: 40px; height: 40px; background: #e8f5e4; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.op-name { font-size: 15px; font-weight: 600; color: #1e1e1e; }
.op-meta { font-size: 12px; color: #9ca3af; margin-top: 2px; }

.order-amount { text-align: right; }
.oa-price { font-size: 18px; font-weight: 700; color: #1e1e1e; font-family: 'JetBrains Mono', monospace; }
.oa-label { font-size: 11px; color: #9ca3af; }

.order-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid #f0f0eb; }
.of-time { font-size: 12px; color: #9ca3af; }
.of-actions { display: flex; gap: 8px; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 8px; transition: all 0.15s; font-weight: 500; }
.btn-sm { padding: 6px 14px; font-size: 12px; }
.btn-primary { background: #7CCF5F; color: white; border: none; }
.btn-primary:hover { background: #6bc04e; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e8ebe4; }
.btn-ghost:hover { border-color: #ef4444; color: #ef4444; }

@media (max-width: 600px) { .order-body { flex-direction: column; align-items: flex-start; gap: 8px; } }
</style>
