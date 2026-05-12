<template>
  <div class="cart-page">
    <div class="page-heading">
      <h1 class="page-title">购物车</h1>
      <p class="page-subtitle">{{ items.length }} 件商品</p>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else-if="items.length === 0">
      <div class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
        <p>购物车是空的</p>
        <button class="btn btn-primary" @click="$router.push('/products')">去选购</button>
      </div>
    </template>

    <template v-else>
      <div class="cart-list">
        <div v-for="item in items" :key="item.id" class="cart-item">
          <div class="ci-check">
            <input type="checkbox" v-model="item.checked" @change="updateChecked(item)" />
          </div>
          <div class="ci-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#7CCF5F" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
          </div>
          <div class="ci-info" @click="$router.push(`/products/${item.productId}`)">
            <div class="ci-name">{{ item.productName }}</div>
            <div class="ci-meta">¥{{ item.price }}/{{ item.unit || '斤' }}</div>
          </div>
          <div class="ci-qty">
            <button class="qty-btn" @click="updateQty(item, -1)">−</button>
            <span class="qty-value">{{ item.quantity }}</span>
            <button class="qty-btn" @click="updateQty(item, 1)">+</button>
          </div>
          <div class="ci-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          <button class="ci-remove" @click="handleRemove(item)">×</button>
        </div>
      </div>

      <div class="cart-footer">
        <div class="cf-left">
          <button class="btn btn-ghost" @click="handleClear">清空</button>
        </div>
        <div class="cf-right">
          <span class="cf-label">合计：</span>
          <span class="cf-total">¥{{ totalPrice }}</span>
          <button class="btn btn-primary btn-lg" :disabled="checkedItems.length === 0" @click="handleCheckout">
            去结算 ({{ checkedItems.length }})
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { cartApi } from '../../api/http'

const items = ref([])
const loading = ref(true)

const totalPrice = computed(() => {
  return items.value.filter(i => i.checked)
    .reduce((sum, i) => sum + i.price * i.quantity, 0)
    .toFixed(2)
})
const checkedItems = computed(() => items.value.filter(i => i.checked))

async function loadCart() {
  try { items.value = await cartApi.list() }
  catch { ElMessage.error('加载购物车失败') }
  finally { loading.value = false }
}

async function updateChecked(item) { try { await cartApi.update(item.id, { checked: item.checked ? 1 : 0 }) } catch {} }
async function updateQty(item, delta) {
  const newQty = item.quantity + delta
  if (newQty < 1) return handleRemove(item)
  try { await cartApi.update(item.id, { quantity: newQty }); item.quantity = newQty }
  catch (e) { ElMessage.error(e.message) }
}
async function handleRemove(item) {
  try { await cartApi.remove(item.id); items.value = items.value.filter(i => i.id !== item.id); ElMessage.success('已移除') }
  catch (e) { ElMessage.error(e.message) }
}
async function handleClear() {
  try { await cartApi.clear(); items.value = []; ElMessage.success('已清空') }
  catch (e) { ElMessage.error(e.message) }
}
async function handleCheckout() {
  ElMessage.success('结算功能开发中...')
}

onMounted(loadCart)
</script>

<style scoped>
.cart-page { max-width: 900px; }
.page-heading { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.loading-state, .empty-state { text-align: center; padding: 60px; color: #6b7280; }
.empty-state p { margin: 16px 0; }

.cart-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }
.cart-item {
  display: flex; align-items: center; gap: 14px;
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 16px; padding: 16px 20px;
  transition: box-shadow 0.2s;
}
.cart-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }

.ci-check input { width: 18px; height: 18px; accent-color: #7CCF5F; cursor: pointer; }
.ci-icon { width: 40px; height: 40px; background: #e8f5e4; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.ci-info { flex: 1; min-width: 0; cursor: pointer; }
.ci-name { font-size: 15px; font-weight: 600; color: #1e1e1e; }
.ci-meta { font-size: 12px; color: #6b7280; margin-top: 2px; }

.ci-qty { display: flex; align-items: center; gap: 6px; }
.qty-btn {
  width: 30px; height: 30px; border: 1px solid #e8ebe4; border-radius: 8px;
  background: #f9faf8; color: #374151; font-size: 16px; cursor: pointer; display: flex; align-items: center; justify-content: center;
}
.qty-btn:hover { border-color: #7CCF5F; color: #7CCF5F; }
.qty-value { min-width: 24px; text-align: center; font-size: 14px; font-weight: 600; color: #1e1e1e; }

.ci-total { font-size: 16px; font-weight: 700; color: #7CCF5F; font-family: 'JetBrains Mono', monospace; min-width: 80px; text-align: right; }
.ci-remove { background: none; border: none; color: #d1d5db; font-size: 20px; cursor: pointer; padding: 4px; }
.ci-remove:hover { color: #ef4444; }

.cart-footer {
  display: flex; justify-content: space-between; align-items: center;
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 16px; padding: 20px 24px;
}
.cf-right { display: flex; align-items: center; gap: 16px; }
.cf-label { font-size: 14px; color: #6b7280; }
.cf-total { font-size: 24px; font-weight: 700; color: #1e1e1e; font-family: 'JetBrains Mono', monospace; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 10px; transition: all 0.15s; font-weight: 500; }
.btn-primary { background: #7CCF5F; color: white; border: none; padding: 10px 24px; font-size: 14px; }
.btn-primary:hover { background: #6bc04e; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-lg { padding: 14px 28px; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e8ebe4; padding: 8px 20px; font-size: 13px; }
.btn-ghost:hover { border-color: #ef4444; color: #ef4444; }

@media (max-width: 600px) { .cart-item { flex-wrap: wrap; } }
</style>
