<template>
  <div class="order-detail-page">
    <div class="breadcrumb">
      <a @click.prevent="$router.push('/orders')" href="/orders">订单</a>
      <span class="sep">/</span>
      <span>订单 #{{ order?.id || '' }}</span>
    </div>

    <div v-if="loading" class="skeleton-box"></div>

    <template v-else-if="order">
      <div class="detail-card">
        <div class="dc-header">
          <div>
            <h1 class="dc-title">订单 #{{ order.id }}</h1>
            <span :class="['status-tag', order.status?.toLowerCase()]">{{ orderStatusLabel(order.status) }}</span>
          </div>
          <div class="dc-time">{{ formatTime(order.createdAt) }}</div>
        </div>

        <div class="dc-divider"></div>

        <div class="dc-body">
          <div class="dc-section">
            <h3 class="dc-section-title">产品信息</h3>
            <div class="dc-product">
              <div class="dcp-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#7CCF5F" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
              </div>
              <div>
                <div class="dcp-name">{{ order.productName }}</div>
                <div class="dcp-meta">x{{ order.quantity }} · ¥{{ order.price }}/{{ order.unit || '斤' }}</div>
              </div>
              <div class="dcp-total">¥{{ order.totalPrice }}</div>
            </div>
          </div>

          <div class="dc-section">
            <h3 class="dc-section-title">交易详情</h3>
            <div class="dc-meta-grid">
              <div class="dc-meta-item"><span class="dcm-label">买家</span><span class="dcm-value">{{ order.buyerName || '买家#' + order.buyerId }}</span></div>
              <div class="dc-meta-item"><span class="dcm-label">卖家</span><span class="dcm-value">{{ order.sellerName || '卖家#' + order.sellerId }}</span></div>
              <div class="dc-meta-item"><span class="dcm-label">类型</span><span class="dcm-value">{{ order.infoType === 'SUPPLY' ? '供应' : '需求' }}</span></div>
              <div class="dc-meta-item"><span class="dcm-label">创建时间</span><span class="dcm-value">{{ formatTime(order.createdAt) }}</span></div>
            </div>
          </div>
        </div>

        <div v-if="order.status === 'PENDING'" class="dc-actions">
          <button class="btn btn-primary" @click="handleConfirm">确认订单</button>
          <button class="btn btn-outline" @click="handleCancel">取消订单</button>
        </div>
        <div v-else-if="order.status === 'CONFIRMED'" class="dc-actions">
          <button class="btn btn-primary" @click="handleComplete">标记完成</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi } from '../../api/http'

const route = useRoute()
const order = ref(null)
const loading = ref(true)

function orderStatusLabel(s) { return { PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s }
function formatTime(t) { return t ? t.substring(0, 16).replace('T', ' ') : '' }

async function loadOrder() {
  try { order.value = await orderApi.getById(route.params.id) }
  catch { ElMessage.error('加载订单失败') }
  finally { loading.value = false }
}

async function handleConfirm() { try { await orderApi.updateStatus(order.value.id, 'CONFIRMED'); order.value.status = 'CONFIRMED'; ElMessage.success('已确认') } catch (e) { ElMessage.error(e.message) } }
async function handleCancel() { try { await orderApi.updateStatus(order.value.id, 'CANCELLED'); order.value.status = 'CANCELLED'; ElMessage.success('已取消') } catch (e) { ElMessage.error(e.message) } }
async function handleComplete() { try { await orderApi.updateStatus(order.value.id, 'COMPLETED'); order.value.status = 'COMPLETED'; ElMessage.success('已完成') } catch (e) { ElMessage.error(e.message) } }

onMounted(loadOrder)
</script>

<style scoped>
.order-detail-page { max-width: 800px; }

.breadcrumb { display: flex; align-items: center; gap: 8px; margin-bottom: 24px; font-size: 13px; color: #6b7280; }
.breadcrumb a { color: #7CCF5F; text-decoration: none; cursor: pointer; }
.breadcrumb .sep { color: #d1d5db; }

.skeleton-box { height: 400px; background: #ffffff; border: 1px solid #e8ebe4; border-radius: 16px; animation: pulse 1.5s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.5} }

.detail-card { background: #ffffff; border: 1px solid #e8ebe4; border-radius: 20px; padding: 28px; }
.dc-header { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px; }
.dc-title { font-size: 22px; font-weight: 700; color: #1e1e1e; margin: 0 0 8px; }

.status-tag { display: inline-flex; padding: 4px 12px; border-radius: 8px; font-size: 12px; font-weight: 600; }
.status-tag.pending { background: #fef3c7; color: #f59e0b; }
.status-tag.confirmed { background: #dbeafe; color: #3b82f6; }
.status-tag.completed { background: #e8f5e4; color: #7CCF5F; }
.status-tag.cancelled { background: #f0f0eb; color: #9ca3af; }

.dc-time { font-size: 13px; color: #9ca3af; }
.dc-divider { height: 1px; background: #f0f0eb; margin: 20px 0; }
.dc-body { display: flex; flex-direction: column; gap: 24px; }
.dc-section-title { font-size: 14px; font-weight: 600; color: #6b7280; margin: 0 0 12px; }

.dc-product { display: flex; align-items: center; gap: 14px; }
.dcp-icon { width: 44px; height: 44px; background: #e8f5e4; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.dcp-name { font-size: 16px; font-weight: 600; color: #1e1e1e; }
.dcp-meta { font-size: 13px; color: #6b7280; margin-top: 2px; }
.dcp-total { margin-left: auto; font-size: 20px; font-weight: 700; color: #1e1e1e; font-family: 'JetBrains Mono', monospace; }

.dc-meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.dc-meta-item { display: flex; flex-direction: column; gap: 4px; }
.dcm-label { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; }
.dcm-value { font-size: 14px; color: #374151; font-weight: 500; }

.dc-actions { margin-top: 24px; padding-top: 20px; border-top: 1px solid #f0f0eb; display: flex; gap: 12px; }
.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 10px; transition: all 0.15s; padding: 10px 24px; font-size: 14px; font-weight: 500; }
.btn-primary { background: #7CCF5F; color: white; border: none; }
.btn-primary:hover { background: #6bc04e; }
.btn-outline { background: transparent; color: #374151; border: 1px solid #e8ebe4; }
.btn-outline:hover { border-color: #ef4444; color: #ef4444; }
</style>
