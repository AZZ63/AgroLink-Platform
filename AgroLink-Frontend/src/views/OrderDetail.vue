<template>
  <div class="detail-page">
    <NavBar />
    <div class="detail-container">
      <button class="back-btn" @click="$router.push('/orders')">← 我的订单</button>

      <div v-if="loading" class="skeleton-box"></div>

      <div v-else-if="order" class="detail-layout">
        <div class="detail-main">
          <div class="detail-card">
            <div class="detail-top">
              <h1 class="detail-title">{{ order.productName }}</h1>
              <div class="detail-badges">
                <span class="tech-status" :class="'tech-status-' + order.status.toLowerCase()">{{ statusLabel(order.status) }}</span>
                <span v-if="order.payStatus && order.payStatus !== 'UNPAID'" class="pay-badge" :class="'pay-' + order.payStatus.toLowerCase()">{{ payLabel(order.payStatus) }}</span>
              </div>
            </div>

            <div class="detail-info-grid">
              <div class="info-item"><span class="info-label">订单编号</span><span class="info-value">#{{ order.id }}</span></div>
              <div class="info-item"><span class="info-label">产品</span><span class="info-value">{{ order.productName }}</span></div>
              <div class="info-item"><span class="info-label">数量</span><span class="info-value">{{ order.quantity }}</span></div>
              <div class="info-item"><span class="info-label">单价</span><span class="info-value">¥{{ order.price }}</span></div>
              <div class="info-item"><span class="info-label">总价</span><span class="info-value total">¥{{ order.totalPrice }}</span></div>
              <div class="info-item"><span class="info-label">买家</span><span class="info-value">{{ order.buyerName || '用户' + order.buyerId }}</span></div>
              <div class="info-item"><span class="info-label">卖家</span><span class="info-value">{{ order.sellerName || '用户' + order.sellerId }}</span></div>
              <div class="info-item"><span class="info-label">创建时间</span><span class="info-value">{{ formatTime(order.createdAt) }}</span></div>
              <div class="info-item" v-if="order.payTime"><span class="info-label">支付时间</span><span class="info-value">{{ formatTime(order.payTime) }}</span></div>
              <div class="info-item" v-if="order.refundTime"><span class="info-label">退款时间</span><span class="info-value">{{ formatTime(order.refundTime) }}</span></div>
            </div>

            <!-- Refund History -->
            <div v-if="refunds.length" class="refund-section">
              <h3 class="section-title">退款记录</h3>
              <div v-for="rf in refunds" :key="rf.id" class="refund-row">
                <div class="refund-header">
                  <span class="tech-status" :class="'tech-status-' + rf.status.toLowerCase()">{{ refundStatusLabel(rf.status) }}</span>
                  <span class="refund-no">{{ rf.refundNo }}</span>
                </div>
                <div class="refund-detail">
                  <span>原因：{{ reasonTypeLabel(rf.reasonType) }}{{ rf.reasonDetail ? ' - ' + rf.reasonDetail : '' }}</span>
                  <span>金额：¥{{ rf.amount }}</span>
                  <span v-if="rf.rejectReason">拒绝原因：{{ rf.rejectReason }}</span>
                </div>
                <!-- Seller review actions -->
                <div v-if="rf.status === 'PENDING' && order.sellerId === userStore.user?.userId" class="refund-actions">
                  <button class="t-btn t-btn-success" @click="handleRefundReview(rf.id, true)">同意退款</button>
                  <button class="t-btn t-btn-warn" @click="showRejectDialog(rf.id)">拒绝退款</button>
                </div>
              </div>
            </div>

            <!-- Review Section -->
            <div v-if="order.status === 'COMPLETED'" class="review-section">
              <h3 class="section-title">评价</h3>
              <div v-if="myReview" class="review-display">
                <div class="review-stars">评分：{{ '★'.repeat(myReview.rating) }}{{ '☆'.repeat(5 - myReview.rating) }}</div>
                <p class="review-text">{{ myReview.content || '用户未填写评价内容' }}</p>
              </div>
              <div v-else class="review-form">
                <div class="star-select">
                  <span v-for="s in 5" :key="s" class="star" :class="{ active: s <= reviewForm.rating }" @click="reviewForm.rating = s">{{ s <= reviewForm.rating ? '★' : '☆' }}</span>
                </div>
                <textarea v-model="reviewForm.content" class="f-input f-textarea" placeholder="写下你的评价..." rows="2"></textarea>
                <button class="tech-btn tech-btn-primary" @click="submitReview" :disabled="reviewSubmitting">{{ reviewSubmitting ? '提交中...' : '提交评价' }}</button>
              </div>
            </div>

            <div class="detail-actions">
              <button v-if="order.status === 'UNPAID' && order.buyerId === userStore.user?.userId" class="tech-btn tech-btn-primary" @click="handlePay">去支付</button>
              <button v-if="order.status === 'PAID' && order.sellerId === userStore.user?.userId" class="tech-btn tech-btn-primary" @click="handleStatus('CONFIRMED')">确认订单</button>
              <button v-if="order.status === 'CONFIRMED' && (order.sellerId === userStore.user?.userId || order.buyerId === userStore.user?.userId)" class="tech-btn tech-btn-success" @click="handleStatus('COMPLETED')">完成交易</button>
              <button v-if="order.status === 'UNPAID' && order.buyerId === userStore.user?.userId" class="tech-btn-outline warn" @click="handleStatus('CANCELLED')">取消订单</button>
              <button v-if="isRefundable && order.buyerId === userStore.user?.userId" class="tech-btn-outline warn" @click="showRefund = true">申请退款</button>
            </div>
          </div>
        </div>

        <aside class="detail-sidebar">
          <!-- Status Timeline -->
          <div class="timeline-card">
            <h3 class="timeline-title">订单状态</h3>
            <div class="timeline">
              <div class="timeline-item" :class="{ active: step >= 1, done: step > 1 }">
                <div class="tl-dot"></div>
                <div class="tl-content"><div class="tl-step-line"><span class="tl-step-icon">🛒</span><strong>下单</strong></div><span>{{ formatTime(order.createdAt) }}</span></div>
              </div>
              <div class="timeline-item" :class="{ active: step >= 2, done: step > 2 }">
                <div class="tl-dot"></div>
                <div class="tl-content"><div class="tl-step-line"><span class="tl-step-icon">💳</span><strong>支付</strong></div><span v-if="order.payTime">{{ formatTime(order.payTime) }}</span><span v-else>待支付</span></div>
              </div>
              <div class="timeline-item" :class="{ active: step >= 3, done: step > 3 }">
                <div class="tl-dot"></div>
                <div class="tl-content"><div class="tl-step-line"><span class="tl-step-icon">✅</span><strong>卖家确认</strong></div><span v-if="step >= 3">{{ formatTime(order.updatedAt) }}</span><span v-else>待确认</span></div>
              </div>
              <div class="timeline-item" :class="{ active: step >= 4 }">
                <div class="tl-dot"></div>
                <div class="tl-content"><div class="tl-step-line"><span class="tl-step-icon">🎉</span><strong>完成交易</strong></div><span v-if="step >= 4">{{ formatTime(order.updatedAt) }}</span><span v-else>进行中</span></div>
              </div>
              <div v-if="step === -1" class="timeline-item cancelled">
                <div class="tl-dot tl-dot-cancel"></div>
                <div class="tl-content"><div class="tl-step-line"><span class="tl-step-icon">🚫</span><strong>已取消</strong></div></div>
              </div>
              <div v-if="order.status === 'REFUNDING' || order.status === 'REFUNDED'" class="timeline-item active">
                <div class="tl-dot tl-dot-warn"></div>
                <div class="tl-content"><div class="tl-step-line"><span class="tl-step-icon">↩️</span><strong>{{ order.status === 'REFUNDED' ? '已退款' : '退款中' }}</strong></div><span v-if="order.refundTime">{{ formatTime(order.refundTime) }}</span></div>
              </div>
            </div>
          </div>

          <!-- Refund Application Form -->
          <div v-if="showRefund" class="refund-card">
            <h3 class="timeline-title">申请退款</h3>
            <div class="form-group">
              <label>退款原因</label>
              <select v-model="refundForm.type" class="f-input">
                <option value="QUALITY">品质问题</option>
                <option value="QUANTITY">数量不符</option>
                <option value="WRONG">发错货</option>
                <option value="OTHER">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>详细说明</label>
              <textarea v-model="refundForm.detail" class="f-input f-textarea" placeholder="请描述具体情况" rows="3"></textarea>
            </div>
            <div class="form-group">
              <label>凭证图片</label>
              <div class="upload-area" @click="$refs.detailFileInput.click()">
                <input ref="detailFileInput" type="file" accept="image/*" style="display:none" @change="uploadDetailImage" />
                <div v-if="refundUploading" class="upload-status">上传中...</div>
                <div v-else-if="refundImages.length" class="upload-previews">
                  <img v-for="(img, i) in refundImages" :key="i" :src="img" class="upload-thumb" @click.stop />
                  <span class="upload-add" @click.stop="$refs.detailFileInput.click()">+</span>
                </div>
                <div v-else><span class="upload-placeholder">点击上传凭证图片（可选）</span></div>
              </div>
            </div>
            <div class="refund-actions">
              <button class="warn-btn" @click="confirmRefund" :disabled="refundLoading">{{ refundLoading ? '提交中...' : '提交退款申请' }}</button>
              <button class="tech-btn-outline" @click="showRefund = false">取消</button>
            </div>
          </div>

          <!-- Reject Refund Dialog -->
          <div v-if="showReject" class="refund-card">
            <h3 class="timeline-title">拒绝退款</h3>
            <div class="form-group">
              <label>拒绝原因</label>
              <textarea v-model="rejectReason" class="f-input f-textarea" placeholder="请输入拒绝原因" rows="3"></textarea>
            </div>
            <div class="refund-actions">
              <button class="warn-btn" @click="confirmReject">确认拒绝</button>
              <button class="tech-btn-outline" @click="showReject = false">取消</button>
            </div>
          </div>
        </aside>
      </div>

      <div v-else class="empty-state">
        <p>订单不存在</p>
        <button class="tech-btn-outline" @click="$router.push('/orders')">返回订单列表</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import http, { orderApi, paymentApi, reviewApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import { statusLabel, payLabel, refundStatusLabel, reasonTypeLabel, formatTime } from '../utils/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const order = ref(null)
const loading = ref(true)
const refunds = ref([])
const showReject = ref(false)
const rejectingId = ref(null)
const rejectReason = ref('')

const statusOrder = { UNPAID: 1, PAID: 2, CONFIRMED: 3, COMPLETED: 4, CANCELLED: -1, REFUNDING: -2, REFUNDED: -3 }
const step = computed(() => statusOrder[order.value?.status] || 0)
const isRefundable = computed(() => order.value && (order.value.status === 'PAID' || order.value.status === 'CONFIRMED'))

const showRefund = ref(false)
const refundLoading = ref(false)
const refundUploading = ref(false)
const refundImages = ref([])
const refundForm = ref({ type: 'QUALITY', detail: '' })

const myReview = ref(null)
const reviewSubmitting = ref(false)
const reviewForm = ref({ rating: 5, content: '' })


	async function handleStatus(status) {
  try {
    await orderApi.updateStatus(order.value.id, status)
    ElMessage.success('操作成功')
    order.value = await orderApi.getById(route.params.id)
    loadRefunds()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

async function handlePay() {
  try {
    const res = await paymentApi.create(order.value.id)
    await paymentApi.callback(res.tradeNo)
    ElMessage.success('支付成功')
    order.value = await orderApi.getById(route.params.id)
  } catch (e) { ElMessage.error(e.message || '支付失败') }
}

async function uploadDetailImage(e) {
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

async function confirmRefund() {
  refundLoading.value = true
  try {
    await paymentApi.refundRequest(order.value.id, refundForm.value.type, refundForm.value.detail, refundImages.value.join(','))
    ElMessage.success('退款申请已提交，等待卖家审核')
    showRefund.value = false
    order.value = await orderApi.getById(route.params.id)
    loadRefunds()
  } catch (e) { ElMessage.error(e.message || '退款申请失败') }
  finally { refundLoading.value = false }
}

function showRejectDialog(refundId) {
  rejectingId.value = refundId
  rejectReason.value = ''
  showReject.value = true
}

async function handleRefundReview(refundId, approved) {
  try {
    await paymentApi.refundReview(refundId, approved, '')
    ElMessage.success(approved ? '已同意退款' : '已拒绝退款')
    order.value = await orderApi.getById(route.params.id)
    loadRefunds()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  try {
    await paymentApi.refundReview(rejectingId.value, false, rejectReason.value)
    ElMessage.success('已拒绝退款')
    showReject.value = false
    order.value = await orderApi.getById(route.params.id)
    loadRefunds()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

async function submitReview() {
  if (!reviewForm.value.rating) { ElMessage.warning('请选择评分'); return }
  reviewSubmitting.value = true
  try {
    const toUserId = order.value.buyerId === userStore.user?.userId ? order.value.sellerId : order.value.buyerId
    myReview.value = await reviewApi.create({
      orderId: order.value.id,
      productId: order.value.productId,
      toUserId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('评价成功')
  } catch (e) { ElMessage.error(e.message || '评价失败') }
  finally { reviewSubmitting.value = false }
}

async function loadReview() {
  try { myReview.value = await reviewApi.getOrderReview(route.params.id) }
  catch { myReview.value = null }
}

async function loadRefunds() {
  try { refunds.value = await paymentApi.getRefunds(route.params.id) } catch { refunds.value = [] }
}

onMounted(async () => {
  try {
    order.value = await orderApi.getById(route.params.id)
    loadRefunds()
    loadReview()
  } catch { order.value = null }
  finally { loading.value = false }
})
</script>

<style scoped>
.detail-container { max-width: 960px; margin: 0 auto; padding: 24px 32px; }
.back-btn { background: none; border: none; color: #8892a4; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; margin-bottom: 20px; display: block; }
.back-btn:hover { color: #00ff88; }
.detail-layout { display: grid; grid-template-columns: 1fr 300px; gap: 24px; align-items: start; }

.detail-card { background: #161921; border: 1px solid #242837; border-radius: 12px; padding: 28px; }
.detail-top { display: flex; justify-content: space-between; align-items: start; margin-bottom: 24px; gap: 12px; }
.detail-title { font-family: 'Bebas Neue', sans-serif; font-size: 32px; letter-spacing: 1px; }
.detail-badges { display: flex; gap: 8px; flex-wrap: wrap; }
.pay-badge { font-size: 11px; padding: 2px 8px; border-radius: 3px; display: inline-flex; align-items: center; }
.pay-paid { background: rgba(0,255,136,0.1); color: #00ff88; border: 1px solid rgba(0,255,136,0.15); }
.pay-refunding { background: rgba(0,204,255,0.1); color: #00ccff; border: 1px solid rgba(0,204,255,0.15); }
.pay-refunded { background: rgba(136,146,164,0.1); color: #8892a4; border: 1px solid rgba(136,146,164,0.15); }

.detail-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 24px; }
.info-item { display: flex; flex-direction: column; gap: 2px; padding: 8px 0; border-bottom: 1px solid #1e2230; }
.info-label { font-size: 11px; color: #555d6e; text-transform: uppercase; letter-spacing: 0.5px; }
.info-value { font-size: 14px; color: #e8edf5; }
.info-value.total { color: #00ff88; font-weight: 600; font-family: 'JetBrains Mono', monospace; }

.detail-actions { display: flex; gap: 12px; flex-wrap: wrap; }

/* Refund section */
.refund-section { border-top: 1px solid #242837; padding-top: 20px; margin-bottom: 20px; }
.section-title { font-size: 13px; color: #8892a4; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 12px; }
.refund-row { background: #1e2230; border-radius: 8px; padding: 14px 16px; margin-bottom: 8px; }
.refund-header { display: flex; gap: 8px; align-items: center; margin-bottom: 6px; }
.refund-no { font-size: 11px; color: #555d6e; font-family: 'JetBrains Mono', monospace; }
.refund-detail { display: flex; flex-direction: column; gap: 2px; font-size: 13px; color: #8892a4; }
.refund-actions { display: flex; gap: 8px; margin-top: 8px; }

/* Buttons */
.tech-btn-primary { padding: 12px 28px; background: #00ff88; color: #000; border: none; border-radius: 6px; font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 14px; cursor: pointer; transition: all 0.2s; }
.tech-btn-primary:hover { background: #00cc6a; }
.tech-btn-success { padding: 12px 28px; background: transparent; border: 1px solid #00ff88; color: #00ff88; border-radius: 6px; font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 14px; cursor: pointer; }
.tech-btn-success:hover { background: rgba(0,255,136,0.05); }
.tech-btn-outline { padding: 12px 28px; background: transparent; border: 1px solid #242837; border-radius: 6px; color: #8892a4; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; }
.tech-btn-outline:hover { border-color: #00ff88; color: #00ff88; }
.tech-btn-outline.warn { border-color: #ff8800; color: #ff8800; }
.tech-btn-outline.warn:hover { background: rgba(255,136,0,0.05); }
.t-btn { padding: 4px 12px; border: 1px solid #242837; border-radius: 4px; font-size: 12px; font-family: 'Outfit', sans-serif; cursor: pointer; transition: all 0.15s; background: transparent; }
.t-btn-success { border-color: #00ff88; color: #00ff88; }
.t-btn-success:hover { background: rgba(0,255,136,0.1); }
.t-btn-warn { border-color: #ff8800; color: #ff8800; }
.t-btn-warn:hover { background: rgba(255,136,0,0.1); }
.warn-btn { padding: 12px 24px; background: transparent; border: 1px solid #ff8800; border-radius: 6px; color: #ff8800; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; flex: 1; }
.warn-btn:hover { background: rgba(255,136,0,0.05); }

/* Timeline */
.timeline-card, .refund-card { background: #161921; border: 1px solid #242837; border-radius: 12px; padding: 24px; margin-bottom: 16px; }
.timeline-title { font-size: 13px; color: #8892a4; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 20px; }
.timeline { display: flex; flex-direction: column; gap: 0; }
.timeline-item { display: flex; gap: 12px; position: relative; padding-bottom: 24px; }
.timeline-item:last-child { padding-bottom: 0; }
.tl-dot { width: 12px; height: 12px; border-radius: 50%; flex-shrink: 0; background: #1e2230; border: 2px solid #242837; margin-top: 4px; position: relative; z-index: 1; transition: all 0.3s ease; }
.timeline-item.active .tl-dot { border-color: #00ff88; background: rgba(0,255,136,0.2); box-shadow: 0 0 16px rgba(0,255,136,0.4); animation: tl-pulse 2s ease-in-out infinite; }
.timeline-item.done .tl-dot { background: #00ff88; border-color: #00ff88; box-shadow: 0 0 8px rgba(0,255,136,0.3); }
.tl-dot-warn { border-color: #ff8800 !important; background: rgba(255,136,0,0.2) !important; box-shadow: 0 0 16px rgba(255,136,0,0.4) !important; }
.tl-dot-cancel { border-color: #ff4757 !important; background: rgba(255,71,87,0.2) !important; box-shadow: 0 0 16px rgba(255,71,87,0.4) !important; }
.timeline-item::before { content: ''; position: absolute; left: 5px; top: 18px; bottom: 0; width: 2px; background: #242837; transition: background 0.4s ease; }
.timeline-item:last-child::before { display: none; }
.timeline-item.done::before { background: linear-gradient(to bottom, #00ff88, #00cc6a); }
.timeline-item.active:not(.done)::before { background: linear-gradient(to bottom, #00ff88 40%, #242837 100%); }
.timeline-item.cancelled::before { background: #ff4757; }
.tl-content { display: flex; flex-direction: column; gap: 2px; }
.tl-step-line { display: flex; align-items: center; gap: 6px; }
.tl-step-icon { font-size: 15px; }
.tl-content strong { font-size: 14px; color: #e8edf5; }
.tl-content span { font-size: 12px; color: #555d6e; }
.timeline-item.cancelled .tl-content strong { color: #ff4757; }
@keyframes tl-pulse {
  0% { box-shadow: 0 0 0 0 rgba(0,255,136,0.5); }
  50% { box-shadow: 0 0 0 10px rgba(0,255,136,0); }
  100% { box-shadow: 0 0 0 0 rgba(0,255,136,0); }
}

/* Form */
.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 11px; color: #555d6e; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px; }
.f-input { width: 100%; padding: 10px 14px; background: #1e2230; border: 1px solid #242837; border-radius: 6px; color: #e8edf5; font-family: 'Outfit', sans-serif; font-size: 14px; outline: none; }
.f-input:focus { border-color: #00ff88; }
.f-textarea { resize: vertical; min-height: 80px; }

.upload-area { border: 1px dashed #242837; border-radius: 8px; padding: 16px; text-align: center; cursor: pointer; transition: all 0.2s; }
.upload-area:hover { border-color: #00ff88; }
.upload-previews { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.upload-thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #242837; }
.upload-add { width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; border: 1px dashed #242837; border-radius: 4px; color: #555d6e; font-size: 20px; cursor: pointer; }
.upload-add:hover { border-color: #00ff88; color: #00ff88; }
.upload-placeholder { color: #555d6e; font-size: 13px; }
.upload-status { color: #8892a4; font-size: 13px; }
.refund-actions { display: flex; gap: 8px; margin-top: 8px; }

/* Review */
.review-section { border-top: 1px solid #242837; padding-top: 20px; margin-bottom: 20px; }
.review-display { background: #1e2230; border-radius: 8px; padding: 14px 16px; }
.review-stars { color: #ff8800; font-size: 16px; margin-bottom: 4px; }
.review-text { font-size: 13px; color: #8892a4; }
.review-form { display: flex; flex-direction: column; gap: 8px; }
.star-select { display: flex; gap: 4px; }
.star { font-size: 24px; color: #242837; cursor: pointer; transition: color 0.15s; }
.star.active { color: #ff8800; }

.skeleton-box { height: 400px; background: #161921; border-radius: 12px; animation: shimmer 1.5s infinite; }
.empty-state { text-align: center; padding: 80px 20px; color: #555d6e; }
.empty-state p { margin-bottom: 16px; }
@keyframes shimmer { 0% { opacity: 0.5; } 50% { opacity: 1; } 100% { opacity: 0.5; } }

@media (max-width: 768px) {
  .detail-layout { grid-template-columns: 1fr; }
  .detail-info-grid { grid-template-columns: 1fr; }
}
</style>
