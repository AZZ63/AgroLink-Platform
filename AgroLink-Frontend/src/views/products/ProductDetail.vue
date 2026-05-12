<template>
  <div class="detail-page">
    <!-- 面包屑 -->
    <div class="breadcrumb">
      <a @click.prevent="$router.push('/products')" href="/products">市场</a>
      <span class="sep">/</span>
      <span>{{ product?.name || '加载中...' }}</span>
    </div>

    <div v-if="loading" class="detail-loading">
      <div class="skeleton-main"></div>
    </div>

    <template v-else-if="product">
      <div class="detail-layout">
        <!-- 左：图片 -->
        <div class="detail-image-section">
          <div class="main-image">
            <div class="image-placeholder">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
            </div>
            <span :class="['detail-badge', product.infoType === 'SUPPLY' ? 'supply' : 'demand']">
              {{ product.infoType === 'SUPPLY' ? '供应' : '需求' }}
            </span>
          </div>
        </div>

        <!-- 右：信息 -->
        <div class="detail-info-section">
          <div class="info-header">
            <h1 class="info-title">{{ product.name }}</h1>
            <div class="info-tags">
              <span class="tag">{{ product.type }}</span>
              <span v-if="product.province" class="tag">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                {{ product.province }}{{ product.city ? '·' + product.city : '' }}
              </span>
            </div>
          </div>

          <div class="info-price-section">
            <div class="price-row">
              <span class="price">¥{{ product.price }}</span>
              <span class="price-unit">/{{ product.unit }}</span>
            </div>
            <div class="stock-row">
              <span class="stock-label">库存：<strong>{{ product.quantity }}{{ product.unit }}</strong></span>
              <span :class="['status-badge', product.status?.toLowerCase()]">{{ statusLabel(product.status) }}</span>
            </div>
          </div>

          <div class="info-meta">
            <div class="meta-item">
              <span class="meta-label">发布者</span>
              <span class="meta-value">{{ product.username || '匿名用户' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">发布时间</span>
              <span class="meta-value">{{ formatDate(product.createdAt) }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">浏览</span>
              <span class="meta-value">{{ viewCount }} 次</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">收藏</span>
              <span class="meta-value">{{ favCount }} 次</span>
            </div>
          </div>

          <div v-if="product.description" class="info-desc">
            <h4 class="desc-label">描述</h4>
            <p class="desc-text">{{ product.description }}</p>
          </div>

          <div class="info-actions">
            <button class="btn btn-primary" @click="handleOrder">
              {{ product.infoType === 'SUPPLY' ? '立即下单' : '联系发布' }}
            </button>
            <button class="btn btn-outline" @click="handleToggleFav">
              <svg width="16" height="16" :fill="isFav ? '#ef4444' : 'none'" stroke="#ef4444" stroke-width="2" viewBox="0 0 24 24"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
              {{ isFav ? '已收藏' : '收藏' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 评价区 -->
      <div v-if="reviews.length" class="reviews-section">
        <h2 class="section-title">评价 ({{ reviews.length }})</h2>
        <div class="reviews-list">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-avatar">{{ r.fromUsername?.charAt(0) || 'U' }}</div>
            <div class="review-body">
              <div class="review-header">
                <span class="review-user">{{ r.fromUsername || '用户' }}</span>
                <span class="review-stars">★ {{ r.rating }}</span>
                <span class="review-time">{{ formatDate(r.createdAt) }}</span>
              </div>
              <p class="review-text">{{ r.content }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 相关产品 -->
      <div v-if="related.length" class="related-section">
        <h2 class="section-title">相关产品</h2>
        <div class="related-grid">
          <div v-for="p in related" :key="p.id" class="related-card" @click="$router.push(`/products/${p.id}`)">
            <div class="rc-image">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
            </div>
            <div class="rc-body">
              <h4 class="rc-name">{{ p.name }}</h4>
              <span class="rc-price">¥{{ p.price }}/{{ p.unit }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi, reviewApi, favoriteApi, orderApi, viewApi } from '../../api/http'
import { statusLabel } from '../../utils/constants'

const route = useRoute()
const product = ref(null)
const loading = ref(true)
const isFav = ref(false)
const favCount = ref(0)
const viewCount = ref(0)
const reviews = ref([])
const related = ref([])

function formatDate(t) { return t ? t.substring(0, 10) : '' }

async function loadProduct() {
  try {
    product.value = await productApi.getById(route.params.id)
    const [fav, count, revs, rating] = await Promise.all([
      favoriteApi.check(product.value.id).catch(() => false),
      favoriteApi.count(product.value.id).catch(() => 0),
      reviewApi.getProductReviews(product.value.id).catch(() => []),
      reviewApi.getProductRating(product.value.id).catch(() => ({}))
    ])
    isFav.value = !!fav
    favCount.value = count
    reviews.value = revs
    viewCount.value = rating.viewCount || 0

    productApi.query({ type: product.value.type, page: 1, size: 4 }).then(res => {
      related.value = (res.records || res || []).filter(p => p.id !== product.value.id).slice(0, 4)
    })
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function handleOrder() {
  try {
    await orderApi.create({ productId: product.value.id, quantity: 1, infoType: product.value.infoType })
    ElMessage.success('订单已创建')
  } catch (e) { ElMessage.error(e.message || '下单失败') }
}

async function handleToggleFav() {
  try {
    const res = await favoriteApi.toggle(product.value.id)
    isFav.value = !isFav.value
    favCount.value += isFav.value ? 1 : -1
    ElMessage.success(isFav.value ? '已收藏' : '已取消收藏')
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

onMounted(() => {
  loadProduct()
  viewApi.viewProduct(route.params.id).catch(() => {})
})
</script>

<style scoped>
.detail-page { max-width: 1100px; }

.breadcrumb { display: flex; align-items: center; gap: 8px; margin-bottom: 24px; font-size: 13px; color: #6b7280; }
.breadcrumb a { color: #7CCF5F; text-decoration: none; cursor: pointer; }
.breadcrumb a:hover { text-decoration: underline; }
.breadcrumb .sep { color: #d1d5db; }

.detail-loading { padding: 60px 0; }
.skeleton-main { height: 400px; background: #ffffff; border: 1px solid #e8ebe4; border-radius: 16px; animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.5} }

/* 主布局 */
.detail-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 32px; margin-bottom: 40px; }

.detail-image-section { }
.main-image {
  position: relative; border-radius: 20px; overflow: hidden; background: #f5f7f2;
  aspect-ratio: 1; display: flex; align-items: center; justify-content: center; border: 1px solid #e8ebe4;
}
.image-placeholder { opacity: 0.3; }
.detail-badge {
  position: absolute; top: 16px; left: 16px; padding: 6px 14px; border-radius: 8px;
  font-size: 12px; font-weight: 600;
}
.detail-badge.supply { background: #e8f5e4; color: #7CCF5F; }
.detail-badge.demand { background: #fef3c7; color: #f59e0b; }

/* 信息区 */
.detail-info-section {}
.info-header { margin-bottom: 20px; }
.info-title { font-size: 26px; font-weight: 700; color: #1e1e1e; margin: 0 0 12px; }
.info-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.tag {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 12px; background: #f5f7f2; border-radius: 8px; font-size: 12px; color: #6b7280;
}

/* 价格 */
.info-price-section {
  background: #f9faf8; border-radius: 14px; padding: 20px; margin-bottom: 20px;
}
.price-row { display: flex; align-items: baseline; gap: 4px; margin-bottom: 8px; }
.price { font-size: 32px; font-weight: 700; color: #7CCF5F; font-family: 'JetBrains Mono', monospace; }
.price-unit { font-size: 16px; color: #9ca3af; }
.stock-row { display: flex; align-items: center; gap: 12px; }
.stock-label { font-size: 13px; color: #6b7280; }
.stock-label strong { color: #374151; }

.status-badge { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.status-badge.listed { background: #e8f5e4; color: #7CCF5F; }
.status-badge.sold { background: #f0f0eb; color: #9ca3af; }
.status-badge.pending { background: #fef3c7; color: #f59e0b; }
.status-badge.completed { background: #e8f5e4; color: #7CCF5F; }
.status-badge.closed { background: #f0f0eb; color: #9ca3af; }
.status-badge.unlisted { background: #fee2e2; color: #ef4444; }

/* 元信息 */
.info-meta { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 20px; }
.meta-item { display: flex; flex-direction: column; gap: 2px; }
.meta-label { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; }
.meta-value { font-size: 14px; color: #374151; font-weight: 500; }

/* 描述 */
.info-desc { margin-bottom: 24px; }
.desc-label { font-size: 14px; font-weight: 600; color: #1e1e1e; margin: 0 0 8px; }
.desc-text { font-size: 14px; color: #6b7280; line-height: 1.7; margin: 0; }

/* 操作按钮 */
.info-actions { display: flex; gap: 12px; }
.btn { font-family: 'Inter', sans-serif; cursor: pointer; transition: all 0.15s; border-radius: 12px; display: inline-flex; align-items: center; gap: 8px; padding: 14px 28px; font-size: 14px; font-weight: 600; }
.btn-primary { background: #7CCF5F; color: white; border: none; }
.btn-primary:hover { background: #6bc04e; }
.btn-outline { border: 1px solid #e8ebe4; background: transparent; color: #374151; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }

/* 评价 */
.reviews-section { margin-bottom: 40px; }
.section-title { font-size: 18px; font-weight: 600; color: #1e1e1e; margin: 0 0 16px; }
.reviews-list { display: flex; flex-direction: column; gap: 12px; }
.review-item { display: flex; gap: 14px; padding: 16px; background: #ffffff; border: 1px solid #e8ebe4; border-radius: 14px; }
.review-avatar {
  width: 40px; height: 40px; border-radius: 10px; background: #7CCF5F; color: white;
  display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 14px; flex-shrink: 0;
}
.review-body { flex: 1; }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.review-user { font-size: 13px; font-weight: 600; color: #1e1e1e; }
.review-stars { font-size: 12px; color: #f59e0b; }
.review-time { font-size: 11px; color: #9ca3af; margin-left: auto; }
.review-text { font-size: 13px; color: #6b7280; line-height: 1.6; margin: 0; }

/* 相关产品 */
.related-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.related-card {
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 14px; overflow: hidden;
  cursor: pointer; transition: all 0.2s;
}
.related-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); transform: translateY(-2px); }
.rc-image {
  height: 100px; background: #f5f7f2; display: flex; align-items: center; justify-content: center;
}
.rc-body { padding: 12px 14px; }
.rc-name { font-size: 13px; font-weight: 600; color: #1e1e1e; margin: 0 0 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rc-price { font-size: 14px; font-weight: 600; color: #7CCF5F; font-family: 'JetBrains Mono', monospace; }

@media (max-width: 768px) { .detail-layout { grid-template-columns: 1fr; } }
</style>
