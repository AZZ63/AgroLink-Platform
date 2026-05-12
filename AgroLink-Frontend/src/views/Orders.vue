<template>
  <div class="orders-page">
    <NavBar />

    <div class="orders-container">
      <div class="orders-card">
        <div class="orders-header">
          <h1 class="orders-title">我的订单</h1>
          <div class="orders-tabs">
            <button :class="['tab-btn', { active: activeTab === 'all' }]" @click="activeTab = 'all'">全部</button>
            <button :class="['tab-btn', { active: activeTab === 'buy' }]" @click="activeTab = 'buy'">买入</button>
            <button :class="['tab-btn', { active: activeTab === 'sell' }]" @click="activeTab = 'sell'">卖出</button>
          </div>
        </div>

        <div class="order-table">
          <div class="table-header">
            <span class="th" style="flex:2">产品</span>
            <span class="th" style="flex:1">数量</span>
            <span class="th" style="flex:1">金额</span>
            <span class="th" style="flex:1">对方</span>
            <span class="th" style="flex:1.5">状态</span>
            <span class="th" style="flex:2">操作</span>
          </div>

          <EmptyState v-if="!orders.length && !loading" icon="📄" title="暂无订单" description="去市场挑选心仪的产品吧" />

          <div v-for="row in orders" :key="row.id" class="table-row">
            <span class="td" style="flex:2; font-weight:600; cursor:pointer" @click="$router.push('/products/' + row.productId)">{{ row.productName }}</span>
            <span class="td" style="flex:1">{{ row.quantity }}</span>
            <span class="td" style="flex:1; color:#00ff88; font-weight:600">¥{{ row.totalPrice }}</span>
            <span class="td" style="flex:1; font-size:13px; color:#8892a4">
              {{ userStore.user?.userId === row.buyerId ? '卖家' + row.sellerId : '买家' + row.buyerId }}
            </span>
            <span class="td" style="flex:1.5">
              <div class="status-stack">
                <span class="tech-status" :class="'tech-status-' + row.status.toLowerCase()"><span class="status-dot" :class="'dot-' + row.status.toLowerCase()"></span>{{ statusLabel(row.status) }}</span>
                <span v-if="row.payStatus && row.payStatus !== 'UNPAID'" class="pay-badge" :class="'pay-' + row.payStatus.toLowerCase()">
                  {{ payLabel(row.payStatus) }}
                </span>
              </div>
            </span>
            <span class="td" style="flex:2; display:flex; gap:6px; flex-wrap:wrap">
              <!-- Payment -->
              <button v-if="row.status === 'UNPAID' && row.buyerId === userStore.user?.userId" class="t-btn t-btn-primary" @click="handlePay(row)">去支付</button>

              <!-- Seller: confirm -->
              <button v-if="row.status === 'PAID' && row.sellerId === userStore.user?.userId" class="t-btn t-btn-primary" @click="handleStatus(row, 'CONFIRMED')">确认</button>

              <!-- Complete: seller or buyer -->
              <button v-if="row.status === 'CONFIRMED' && (row.sellerId === userStore.user?.userId || row.buyerId === userStore.user?.userId)" class="t-btn t-btn-success" @click="handleStatus(row, 'COMPLETED')">完成</button>

              <!-- Cancel: buyer only, before payment -->
              <button v-if="row.status === 'UNPAID' && row.buyerId === userStore.user?.userId" class="t-btn t-btn-warn" @click="handleStatus(row, 'CANCELLED')">取消</button>

              <!-- Refund: buyer, after payment but before completion -->
              <button v-if="(row.status === 'PAID' || row.status === 'CONFIRMED') && row.buyerId === userStore.user?.userId" class="t-btn t-btn-warn" @click="handleRefund(row)">申请退款</button>

              <!-- View detail -->
              <button class="t-btn" @click="$router.push('/orders/' + row.id)">详情</button>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Payment Dialog -->
    <div v-if="showPayDialog" class="modal-overlay" @click.self="showPayDialog = false">
      <div class="modal-card">
        <h2 class="modal-title">模拟支付</h2>
        <div class="modal-body">
          <div class="pay-info">
            <span>订单号：{{ payingOrder?.id }}</span>
            <span class="pay-amount">¥{{ payingOrder?.totalPrice }}</span>
          </div>
          <p class="pay-desc">当前为模拟支付模式，点击确认模拟支付成功回调</p>
        </div>
        <div class="modal-actions">
          <button class="tech-btn tech-btn-primary" @click="confirmPay" :disabled="payLoading">{{ payLoading ? '处理中...' : '确认支付' }}</button>
          <button class="tech-btn-outline" @click="showPayDialog = false">取消</button>
        </div>
      </div>
    </div>

    <!-- Refund Dialog -->
    <div v-if="showRefundDialog" class="modal-overlay" @click.self="showRefundDialog = false">
      <div class="modal-card">
        <h2 class="modal-title">申请退款</h2>
        <div class="modal-body">
          <div class="form-group">
            <label>退款原因</label>
            <select v-model="refundType" class="f-input">
              <option value="QUALITY">品质问题</option>
              <option value="QUANTITY">数量不符</option>
              <option value="WRONG">发错货</option>
              <option value="OTHER">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>详细说明</label>
            <textarea v-model="refundReason" class="f-input f-textarea" placeholder="请描述具体情况" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>凭证图片</label>
            <div class="upload-area" @click="$refs.refundFileInput.click()">
              <input ref="refundFileInput" type="file" accept="image/*" style="display:none" @change="uploadRefundImage" />
              <div v-if="refundUploading" class="upload-status">上传中...</div>
              <div v-else-if="refundImages.length" class="upload-previews">
                <img v-for="(img, i) in refundImages" :key="i" :src="img" class="upload-thumb" @click.stop />
                <span class="upload-add" @click.stop="$refs.refundFileInput.click()">+</span>
              </div>
              <div v-else>
                <span class="upload-placeholder">点击上传凭证图片（可选）</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="tech-btn t-btn-warn-btn" @click="confirmRefund" :disabled="refundLoading">{{ refundLoading ? '提交中...' : '提交退款申请' }}</button>
          <button class="tech-btn-outline" @click="showRefundDialog = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import http, { orderApi, paymentApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import EmptyState from '../components/EmptyState.vue'
import { statusLabel, payLabel } from '../utils/constants'

const userStore = useUserStore()
const orders = ref([])
const loading = ref(false)
const activeTab = ref('all')

// Payment
const showPayDialog = ref(false)
const payingOrder = ref(null)
const payLoading = ref(false)

// Refund
const showRefundDialog = ref(false)
const refundOrder = ref(null)
const refundType = ref('QUALITY')
const refundReason = ref('')
const refundImages = ref([])
const refundLoading = ref(false)
const refundUploading = ref(false)

async function loadOrders() {
  loading.value = true
  try {
    if (activeTab.value === 'all') orders.value = await orderApi.getMy()
    else if (activeTab.value === 'buy') orders.value = await orderApi.getBuy()
    else orders.value = await orderApi.getSell()
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

function handleStatus(row, status) {
  orderApi.updateStatus(row.id, status).then(() => {
    ElMessage.success('操作成功')
    loadOrders()
  }).catch(e => ElMessage.error(e.message || '操作失败'))
}

function handlePay(row) {
  payingOrder.value = row
  showPayDialog.value = true
}

function handleRefund(row) {
  refundOrder.value = row
  refundType.value = 'QUALITY'
  refundReason.value = ''
  refundImages.value = []
  showRefundDialog.value = true
}

async function uploadRefundImage(e) {
  const file = e.target.files[0]
  if (!file) return
  refundUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const url = await http.post('/file/upload', fd)
    refundImages.value.push(url)
  } catch (e) { ElMessage.error('上传失败') }
  finally { refundUploading.value = false }
}

async function confirmPay() {
  if (!payingOrder.value) return
  payLoading.value = true
  try {
    const res = await paymentApi.create(payingOrder.value.id)
    // Mock callback
    await paymentApi.callback(res.tradeNo)
    ElMessage.success('支付成功')
    showPayDialog.value = false
    loadOrders()
  } catch (e) {
    ElMessage.error(e.message || '支付失败')
  } finally {
    payLoading.value = false
  }
}

async function confirmRefund() {
  if (!refundOrder.value) return
  refundLoading.value = true
  try {
    await paymentApi.refundRequest(refundOrder.value.id, refundType.value, refundReason.value, refundImages.value.join(','))
    ElMessage.success('退款申请已提交，等待卖家审核')
    showRefundDialog.value = false
    loadOrders()
  } catch (e) {
    ElMessage.error(e.message || '退款申请失败')
  } finally {
    refundLoading.value = false
  }
}

watch(activeTab, loadOrders)
onMounted(loadOrders)
</script>

<style scoped>
.orders-container { max-width: 1080px; margin: 40px auto; padding: 0 32px; }
.orders-card { background: #161921; border: 1px solid #242837; border-radius: 12px; padding: 32px; }
.orders-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.orders-title { font-family: 'Bebas Neue', sans-serif; font-size: 32px; letter-spacing: 1px; }
.orders-tabs { display: flex; gap: 4px; }
.tab-btn { padding: 8px 20px; background: transparent; border: 1px solid #242837; border-radius: 6px; color: #8892a4; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; transition: all 0.2s; }
.tab-btn.active { background: rgba(0,255,136,0.1); border-color: #00ff88; color: #00ff88; }
.tab-btn:hover:not(.active) { border-color: #555d6e; }

.order-table { border: 1px solid #242837; border-radius: 8px; overflow: hidden; }
.table-header { display: flex; padding: 12px 16px; background: #1e2230; border-bottom: 1px solid #242837; }
.th { font-size: 11px; font-weight: 600; color: #555d6e; text-transform: uppercase; letter-spacing: 1px; }
.table-row { display: flex; padding: 14px 16px; align-items: center; border-bottom: 1px solid #1e2230; transition: background 0.15s; }
.table-row:hover { background: rgba(255,255,255,0.02); }
.table-row:last-child { border-bottom: none; }
.td { font-size: 14px; color: #e8edf5; }

.status-stack { display: flex; flex-direction: column; gap: 4px; }
.pay-badge { font-size: 10px; padding: 1px 6px; border-radius: 3px; display: inline-block; width: fit-content; }
.pay-paid { background: rgba(0,255,136,0.1); color: #00ff88; }
.pay-refunding { background: rgba(0,204,255,0.1); color: #00ccff; }
.pay-refunded { background: rgba(136,146,164,0.1); color: #8892a4; }

.t-btn { padding: 4px 12px; border: 1px solid #242837; border-radius: 4px; font-size: 12px; font-family: 'Outfit', sans-serif; cursor: pointer; transition: all 0.15s; background: transparent; color: #8892a4; }
.t-btn-primary { border-color: #00ff88; color: #00ff88; }
.t-btn-primary:hover { background: rgba(0,255,136,0.1); }
.t-btn-success { border-color: #00ff88; color: #00ff88; }
.t-btn-success:hover { background: rgba(0,255,136,0.1); }
.t-btn-warn { border-color: #ff8800; color: #ff8800; }
.t-btn-warn:hover { background: rgba(255,136,0,0.1); }
.t-btn-warn-btn { padding: 12px 24px; border: 1px solid #ff8800; border-radius: 6px; background: transparent; color: #ff8800; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; }
.t-btn-warn-btn:hover { background: rgba(255,136,0,0.1); }

.table-empty { text-align: center; padding: 40px; color: #555d6e; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.6); z-index: 1000;
  display: flex; align-items: center; justify-content: center;
}
.modal-card {
  background: #161921; border: 1px solid #242837; border-radius: 12px;
  padding: 32px; width: 100%; max-width: 420px;
}
.modal-title { font-family: 'Bebas Neue', sans-serif; font-size: 24px; letter-spacing: 1px; margin-bottom: 20px; }
.modal-body { margin-bottom: 24px; }
.pay-info { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.pay-amount { font-family: 'JetBrains Mono', monospace; font-size: 28px; color: #00ff88; font-weight: 600; }
.pay-desc { font-size: 13px; color: #555d6e; }
.modal-actions { display: flex; gap: 12px; }
.tech-btn-primary {
  padding: 12px 24px; background: #00ff88; color: #000; border: none; border-radius: 6px;
  font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 14px; cursor: pointer; transition: all 0.2s; flex: 1;
}
.tech-btn-primary:hover { background: #00cc6a; }
.tech-btn-primary:disabled { opacity: 0.5; }
.tech-btn-outline {
  padding: 12px 24px; background: transparent; border: 1px solid #242837; border-radius: 6px;
  color: #8892a4; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; flex: 1;
}
.tech-btn-outline:hover { border-color: #00ff88; color: #00ff88; }

.f-input { width: 100%; padding: 10px 14px; background: #1e2230; border: 1px solid #242837; border-radius: 6px; color: #e8edf5; font-family: 'Outfit', sans-serif; font-size: 14px; outline: none; }
.f-input:focus { border-color: #00ff88; }
.f-textarea { resize: vertical; min-height: 80px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 11px; color: #555d6e; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px; }
.upload-area { border: 1px dashed #242837; border-radius: 8px; padding: 16px; text-align: center; cursor: pointer; transition: all 0.2s; }
.upload-area:hover { border-color: #00ff88; }
.upload-previews { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.upload-thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #242837; }
.upload-add { width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; border: 1px dashed #242837; border-radius: 4px; color: #555d6e; font-size: 20px; cursor: pointer; }
.upload-add:hover { border-color: #00ff88; color: #00ff88; }
.upload-placeholder { color: #555d6e; font-size: 13px; }
.upload-status { color: #8892a4; font-size: 13px; }
.f-textarea { resize: vertical; min-height: 80px; }

@media (max-width: 768px) {
  .orders-container { padding: 0 16px; }
  .orders-header { flex-direction: column; gap: 12px; align-items: start; }
  .table-header { display: none; }
  .table-row { flex-wrap: wrap; gap: 8px; }
  .table-row .td { flex: 1 1 45% !important; }
}
</style>
