<template>
  <div class="cart-page">
    <NavBar />
    <div class="cart-container">
      <!-- Loading -->
      <div v-if="loading" class="cart-loading">
        <div v-for="n in 3" :key="n" class="skeleton-row">
          <div class="skeleton-check"></div>
          <div class="skeleton-img"></div>
          <div class="skeleton-info">
            <div class="skeleton-text w60"></div>
            <div class="skeleton-text w40"></div>
          </div>
          <div class="skeleton-qty"></div>
          <div class="skeleton-text w30"></div>
        </div>
      </div>

      <!-- Empty -->
      <EmptyState v-else-if="!items.length" icon="🛒" title="购物车是空的" subtitle="快去挑选心仪的农产品吧" actionText="去逛逛" @action="$router.push('/products')" />

      <!-- Cart list -->
      <template v-else>
        <div class="cart-header">
          <h1 class="cart-title">购物车 ({{ items.length }})</h1>
        </div>

        <div class="cart-list">
          <div
            v-for="item in items"
            :key="item.id"
            class="cart-item"
            :class="{ 'item-off': item.productStatus === 'UNLISTED' }"
          >
            <!-- Checkbox -->
            <label class="cart-checkbox" @click.prevent="toggleCheck(item)">
              <span class="ck-box" :class="{ checked: item.checked }">
                <span v-if="item.checked" class="ck-icon">✓</span>
              </span>
            </label>

            <!-- Image -->
            <div class="cart-img" @click="goProduct(item.productId)">
              <div v-if="item.productImage" class="img-wrap">
                <img :src="item.productImage" :alt="item.productName" />
              </div>
              <div v-else class="img-placeholder">🌾</div>
            </div>

            <!-- Info -->
            <div class="cart-info" @click="goProduct(item.productId)">
              <div class="item-name">{{ item.productName }}</div>
              <div class="item-status" v-if="item.productStatus === 'UNLISTED'">已下架</div>
              <div class="item-price">¥{{ item.productPrice }} <span class="item-unit">/ {{ item.productUnit }}</span></div>
            </div>

            <!-- Quantity -->
            <div class="cart-qty">
              <button class="qty-btn" @click="decrease(item)" :disabled="item.quantity <= 1">−</button>
              <span class="qty-val">{{ item.quantity }}</span>
              <button class="qty-btn" @click="increase(item)">+</button>
            </div>

            <!-- Subtotal -->
            <div class="cart-subtotal">¥{{ (item.productPrice * item.quantity).toFixed(2) }}</div>

            <!-- Delete -->
            <button class="cart-del" @click="handleRemove(item)">✕</button>
          </div>
        </div>

        <!-- Bottom bar -->
        <div class="cart-bottom">
          <label class="cart-checkbox all-check" @click.prevent="toggleAll">
            <span class="ck-box" :class="{ checked: allChecked }">
              <span v-if="allChecked" class="ck-icon">✓</span>
            </span>
            <span class="all-label">全选</span>
          </label>

          <div class="bottom-right">
            <div class="cart-total">
              <span class="total-label">合计：</span>
              <span class="total-price">¥{{ totalPrice }}</span>
            </div>
            <button
              class="checkout-btn"
              :class="{ disabled: checkedCount === 0 }"
              :disabled="checkedCount === 0"
              @click="handleCheckout"
            >
              结算 ({{ checkedCount }})
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cartApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import EmptyState from '../components/EmptyState.vue'

const router = useRouter()
const items = ref([])
const loading = ref(true)

const allChecked = computed(() => items.value.length > 0 && items.value.every(i => i.checked))

const checkedItems = computed(() => items.value.filter(i => i.checked))

const checkedCount = computed(() => checkedItems.value.length)

const totalPrice = computed(() => {
  return checkedItems.value
    .reduce((sum, i) => sum + i.productPrice * i.quantity, 0)
    .toFixed(2)
})

function toggleCheck(item) {
  item.checked = !item.checked
}

function toggleAll() {
  const newVal = !allChecked.value
  items.value.forEach(i => { i.checked = newVal })
}

async function decrease(item) {
  if (item.quantity <= 1) return
  item.quantity--
  try {
    await cartApi.update(item.id, { quantity: item.quantity })
  } catch (e) {
    item.quantity++
  }
}

async function increase(item) {
  item.quantity++
  try {
    await cartApi.update(item.id, { quantity: item.quantity })
  } catch (e) {
    item.quantity--
  }
}

function handleRemove(item) {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    background: '#161921',
    roundButton: true
  }).then(async () => {
    try {
      await cartApi.remove(item.id)
      items.value = items.value.filter(i => i.id !== item.id)
      ElMessage.success('已删除')
    } catch (e) {
      ElMessage.error(e.message || '删除失败')
    }
  }).catch(() => {})
}

function handleCheckout() {
  if (!checkedCount.value) return
  // Navigate to order creation or next step
  ElMessage.info('结算功能开发中')
}

function goProduct(id) {
  router.push('/products/' + id)
}

onMounted(async () => {
  try {
    const data = await cartApi.list()
    items.value = (data || []).map(i => ({ ...i, checked: true }))
  } catch (e) {
    ElMessage.error(e.message || '加载购物车失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.cart-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px;
}

/* Header */
.cart-header {
  margin-bottom: 20px;
}
.cart-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 36px;
  letter-spacing: 1px;
}

/* Loading skeleton */
.cart-loading {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.skeleton-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #161921;
  border: 1px solid #242837;
  border-radius: 10px;
}
.skeleton-check {
  width: 20px;
  height: 20px;
  background: #1e2230;
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
}
.skeleton-img {
  width: 80px;
  height: 80px;
  background: #1e2230;
  border-radius: 8px;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
}
.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.skeleton-text {
  height: 16px;
  background: #1e2230;
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
}
.skeleton-text.w60 { width: 60%; }
.skeleton-text.w40 { width: 40%; }
.skeleton-text.w30 { width: 30%; }
.skeleton-qty {
  width: 100px;
  height: 32px;
  background: #1e2230;
  border-radius: 6px;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

/* Empty state */
.cart-empty {
  text-align: center;
  padding: 80px 20px;
  color: #555d6e;
}
.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
}
.cart-empty p {
  margin-bottom: 16px;
  font-size: 15px;
}
.tech-btn-outline {
  padding: 10px 24px;
  background: transparent;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #8892a4;
  font-family: 'Outfit', sans-serif;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.tech-btn-outline:hover {
  border-color: #00ff88;
  color: #00ff88;
}

/* Cart list */
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #161921;
  border: 1px solid #242837;
  border-radius: 10px;
  transition: border-color 0.2s;
}
.cart-item:hover {
  border-color: rgba(0,255,136,0.15);
}
.cart-item.item-off {
  opacity: 0.5;
}

/* Checkbox */
.cart-checkbox {
  display: flex;
  align-items: center;
  cursor: pointer;
  flex-shrink: 0;
}
.ck-box {
  width: 20px;
  height: 20px;
  border: 2px solid #3a4050;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}
.ck-box.checked {
  background: #00ff88;
  border-color: #00ff88;
}
.ck-icon {
  color: #000;
  font-size: 12px;
  font-weight: 700;
}

/* Image */
.cart-img {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  cursor: pointer;
}
.cart-img .img-wrap {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #242837;
}
.cart-img .img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.cart-img .img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #111318;
  border: 1px solid #242837;
  border-radius: 8px;
  font-size: 28px;
}

/* Info */
.cart-info {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}
.item-name {
  font-size: 15px;
  font-weight: 600;
  color: #e8edf5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}
.item-status {
  font-size: 12px;
  color: #ff3355;
  margin-bottom: 4px;
}
.item-price {
  font-size: 14px;
  color: #00ff88;
  font-weight: 600;
  font-family: 'JetBrains Mono', monospace;
}
.item-unit {
  font-size: 12px;
  color: #555d6e;
  font-weight: 400;
  font-family: 'Outfit', sans-serif;
}

/* Quantity stepper */
.cart-qty {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid #242837;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}
.qty-btn {
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  color: #8892a4;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
  font-family: 'Outfit', sans-serif;
  font-weight: 500;
}
.qty-btn:hover {
  background: rgba(0,255,136,0.08);
  color: #00ff88;
}
.qty-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
.qty-val {
  width: 36px;
  text-align: center;
  font-size: 14px;
  font-family: 'JetBrains Mono', monospace;
  color: #e8edf5;
  border-left: 1px solid #242837;
  border-right: 1px solid #242837;
  padding: 0 4px;
  height: 32px;
  line-height: 32px;
}

/* Subtotal */
.cart-subtotal {
  font-size: 16px;
  font-weight: 600;
  color: #00ff88;
  font-family: 'JetBrains Mono', monospace;
  min-width: 80px;
  text-align: right;
  flex-shrink: 0;
}

/* Delete */
.cart-del {
  background: none;
  border: none;
  color: #555d6e;
  font-size: 14px;
  cursor: pointer;
  padding: 6px;
  border-radius: 4px;
  transition: all 0.2s;
  flex-shrink: 0;
}
.cart-del:hover {
  color: #ff3355;
  background: rgba(255,51,85,0.1);
}

/* Bottom bar */
.cart-bottom {
  position: sticky;
  bottom: 0;
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #161921;
  border: 1px solid #242837;
  border-radius: 10px;
  gap: 16px;
}
.all-check {
  display: flex;
  align-items: center;
  gap: 10px;
}
.all-label {
  font-size: 14px;
  color: #e8edf5;
  user-select: none;
}
.bottom-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.cart-total {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.total-label {
  font-size: 14px;
  color: #8892a4;
}
.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #00ff88;
  font-family: 'JetBrains Mono', monospace;
}
.checkout-btn {
  padding: 12px 32px;
  background: #00ff88;
  color: #000;
  border: none;
  border-radius: 6px;
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.checkout-btn:hover {
  background: #00cc6a;
  box-shadow: 0 0 20px rgba(0,255,136,0.3);
}
.checkout-btn.disabled {
  background: #242837;
  color: #555d6e;
  cursor: not-allowed;
  box-shadow: none;
}
.checkout-btn.disabled:hover {
  background: #242837;
  box-shadow: none;
}

@media (max-width: 768px) {
  .cart-container { padding: 16px; }
  .cart-item { flex-wrap: wrap; gap: 12px; padding: 14px 16px; }
  .cart-info { flex: 1 1 100%; order: -1; }
  .cart-img { width: 64px; height: 64px; }
  .cart-subtotal { min-width: auto; }
  .cart-bottom { flex-direction: column; }
  .bottom-right { width: 100%; justify-content: space-between; }
}
</style>
