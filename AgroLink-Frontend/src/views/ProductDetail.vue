<template>
  <div class="detail-page">
    <NavBar />
    <div class="detail-container">
      <button class="back-btn" @click="$router.back()">← 返回</button>

      <div v-if="loading" class="skeleton-detail">
        <div class="skeleton-img"></div>
        <div class="skeleton-text"></div>
        <div class="skeleton-text short"></div>
      </div>

      <template v-else-if="product">
        <div class="detail-layout">
          <div class="detail-image">
            <div v-if="parsedImages.length" class="carousel-wrap">
              <div class="carousel-main" @click="openLightbox">
                <img :src="parsedImages[currentImageIndex]" :alt="product.name" />
                <button class="carousel-btn carousel-prev" @click.stop="prevImage">‹</button>
                <button class="carousel-btn carousel-next" @click.stop="nextImage">›</button>
                <div class="carousel-counter">{{ currentImageIndex + 1 }} / {{ parsedImages.length }}</div>
              </div>
              <div class="carousel-thumbs" v-if="parsedImages.length > 1">
                <div
                  v-for="(img, idx) in parsedImages"
                  :key="idx"
                  class="thumb-item"
                  :class="{ active: idx === currentImageIndex }"
                  @click="selectImage(idx)"
                >
                  <img :src="img" :alt="'缩略图 ' + (idx + 1)" />
                </div>
              </div>
            </div>
            <div v-else class="img-placeholder">
              <span>{{ product.infoType === 'SUPPLY' ? '🌾' : '🛒' }}</span>
            </div>

            <!-- Lightbox -->
            <div v-if="showLightbox" class="lightbox-overlay" @click="closeLightbox">
              <div class="lightbox-content" @click.stop>
                <img :src="parsedImages[currentImageIndex]" :alt="product.name" />
                <button class="lightbox-close" @click="closeLightbox">✕</button>
                <button class="lightbox-btn lightbox-prev" @click.stop="prevImage">‹</button>
                <button class="lightbox-btn lightbox-next" @click.stop="nextImage">›</button>
              </div>
            </div>
          </div>

          <div class="detail-right">
            <div class="detail-info">
              <div class="detail-badges">
                <span class="tech-badge" :class="product.infoType === 'SUPPLY' ? 'tech-badge-supply' : 'tech-badge-demand'">
                  {{ product.infoType === 'SUPPLY' ? '供应' : '需求' }}
                </span>
                <span class="tech-status" :class="'tech-status-' + (product.status || '').toLowerCase()">
                  {{ statusLabel(product.status) }}
                </span>
              </div>

              <h1 class="detail-title">{{ product.name }}</h1>
              <p class="detail-type">{{ product.type }}</p>

              <div class="product-tags" v-if="product.infoType === 'SUPPLY'">
                <span class="tag">绿色食品</span>
                <span class="tag">产地直供</span>
                <span class="tag">有机认证</span>
              </div>

              <div class="detail-price">¥{{ product.price }} <span class="detail-unit">/ {{ product.unit }}</span></div>

              <div class="detail-meta">
                <div class="meta-row"><span class="meta-label">数量</span><span>{{ product.quantity }}{{ product.unit }}</span></div>
                <div class="meta-row" v-if="product.province || product.city"><span class="meta-label">地区</span><span>{{ product.province }}{{ product.city }}</span></div>
                <div class="meta-row"><span class="meta-label">浏览量</span><span>{{ product.viewCount || 0 }}</span></div>
                <div class="meta-row"><span class="meta-label">发布时间</span><span>{{ formatTime(product.createdAt) }}</span></div>
              </div>

              <div class="detail-desc" v-if="product.description">
                <h3>描述</h3>
                <p>{{ product.description }}</p>
              </div>

              <!-- Reviews -->
              <div class="reviews-section">
                <h3 class="reviews-title">评价 <span v-if="productRating.count > 0">({{ productRating.count }}) · {{ productRating.average }} 分</span></h3>
                <div v-if="reviews.length" class="reviews-list">
                  <div v-for="r in reviews" :key="r.id" class="review-item">
                    <div class="review-header">
                      <span class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
                      <span class="review-user">用户 {{ r.fromUserId }}</span>
                      <span class="review-time">{{ formatTime(r.createdAt) }}</span>
                    </div>
                    <p class="review-content" v-if="r.content">{{ r.content }}</p>
                  </div>
                </div>
                <div v-else class="reviews-empty">暂无评价</div>
              </div>

              <div class="detail-actions">
                <button v-if="userStore.isMerchant && product.infoType === 'SUPPLY' && product.status === 'LISTED'" class="tech-btn tech-btn-primary" @click="handleOrder">立即下单</button>
                <button v-if="userStore.user?.userId === product.userId" class="tech-btn-outline" @click="$router.push('/publish/' + product.id)">编辑</button>
                <button class="fav-btn" :class="{ favorited: isFavorited }" @click="toggleFavorite">
                  {{ isFavorited ? '♥' : '♡' }} <span v-if="favoriteCount > 0">{{ favoriteCount }}</span>
                </button>
              </div>
            </div>

            <!-- Merchant Info Card -->
            <div class="merchant-card">
              <h3 class="merchant-card-title">卖家信息</h3>
              <div class="merchant-info-row">
                <span class="merchant-label">商户名称</span>
                <span class="merchant-value">{{ product.username ? '用户' + product.username : '用户' + product.userId }}</span>
              </div>
              <div class="merchant-info-row">
                <span class="merchant-label">发布时间</span>
                <span class="merchant-value">{{ formatTime(product.createdAt) }}</span>
              </div>
              <div class="merchant-info-row">
                <span class="merchant-label">浏览量</span>
                <span class="merchant-value">{{ product.viewCount || 0 }}</span>
              </div>
              <button class="merchant-contact-btn" @click="handleContact">联系卖家</button>
            </div>
          </div>
        </div>

        <!-- Recommended Products -->
        <div class="recommended-section" v-if="recommendedProducts.length">
          <h2 class="recommended-title">猜你喜欢</h2>
          <div class="recommended-grid">
            <div
              v-for="item in recommendedProducts"
              :key="item.id"
              class="recommended-card"
              @click="$router.push('/product/' + item.id)"
            >
              <div class="recommended-img">
                <img v-if="item.image" :src="item.image" :alt="item.name" />
                <div v-else class="recommended-img-placeholder">
                  <span>{{ item.infoType === 'SUPPLY' ? '🌾' : '🛒' }}</span>
                </div>
              </div>
              <div class="recommended-info">
                <h4 class="recommended-name">{{ item.name }}</h4>
                <span class="recommended-price">¥{{ item.price }}<span class="recommended-unit">/{{ item.unit }}</span></span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="empty-state">
        <p>产品不存在</p>
        <button class="tech-btn-outline" @click="$router.push('/products')">去市场看看</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { productApi, orderApi, favoriteApi, reviewApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import { statusLabel, formatTime } from '../utils/constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref(null)
const loading = ref(true)
const isFavorited = ref(false)
const favoriteCount = ref(0)
const reviews = ref([])
const productRating = ref({ average: 0, count: 0 })

// Carousel state
const currentImageIndex = ref(0)
const showLightbox = ref(false)

// Recommended products
const recommendedProducts = ref([])

const parsedImages = computed(() => {
  if (!product.value) return []
  if (product.value.images) {
    try {
      const imgs = JSON.parse(product.value.images)
      if (Array.isArray(imgs) && imgs.length > 0) return imgs
    } catch (e) { /* not JSON array, treat as single image */ }
  }
  if (product.value.image) return [product.value.image]
  return []
})

function prevImage() {
  const len = parsedImages.value.length
  currentImageIndex.value = currentImageIndex.value > 0 ? currentImageIndex.value - 1 : len - 1
}

function nextImage() {
  const len = parsedImages.value.length
  currentImageIndex.value = currentImageIndex.value < len - 1 ? currentImageIndex.value + 1 : 0
}

function selectImage(idx) {
  currentImageIndex.value = idx
}

function openLightbox() {
  if (parsedImages.value.length) showLightbox.value = true
}

function closeLightbox() {
  showLightbox.value = false
}

function handleContact() {
  ElMessage.info('联系卖家功能即将上线')
}

async function handleOrder() {
  try {
    await orderApi.create({ productId: product.value.id, quantity: 1, infoType: product.value.infoType })
    ElMessage.success('下单成功，等待卖家确认')
  } catch (e) { ElMessage.error(e.message || '下单失败') }
}

async function toggleFavorite() {
  try {
    const res = await favoriteApi.toggle(product.value.id)
    isFavorited.value = res.favorited
    favoriteCount.value = res.count
  } catch (e) { /* ignore */ }
}

onMounted(async () => {
  try {
    product.value = await productApi.getById(route.params.id)
    if (product.value) {
      const [fav, cnt, rv, rt] = await Promise.all([
        favoriteApi.check(product.value.id).catch(() => false),
        favoriteApi.count(product.value.id).catch(() => 0),
        reviewApi.getProductReviews(product.value.id).catch(() => []),
        reviewApi.getProductRating(product.value.id).catch(() => ({}))
      ])
      isFavorited.value = fav
      favoriteCount.value = cnt
      reviews.value = rv
      productRating.value = { average: rt.average || 0, count: rt.count || 0 }

      // Fetch recommended products of the same type
      if (product.value.type) {
        productApi.query({ type: product.value.type, page: 1, size: 4 }).then(res => {
          const list = res.rows || res.data || res || []
          recommendedProducts.value = list.filter(item => item.id !== product.value.id).slice(0, 4)
        }).catch(() => {})
      }
    }
  } catch (e) { product.value = null }
  finally { loading.value = false }
})
</script>

<style scoped>
.detail-container { max-width: 960px; margin: 0 auto; padding: 24px 32px; }
.back-btn { background: none; border: none; color: #8892a4; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; margin-bottom: 24px; display: block; }
.back-btn:hover { color: #00ff88; }

.detail-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; }
.detail-image { position: sticky; top: 88px; align-self: start; }

/* ---- Carousel ---- */
.carousel-wrap { border-radius: 12px; overflow: hidden; border: 1px solid #242837; }
.carousel-main { position: relative; cursor: pointer; }
.carousel-main img { width: 100%; display: block; }
.carousel-btn {
  position: absolute; top: 50%; transform: translateY(-50%);
  background: rgba(0,0,0,0.5); color: #fff; border: none;
  width: 36px; height: 36px; border-radius: 50%;
  font-size: 22px; cursor: pointer; display: flex;
  align-items: center; justify-content: center;
  transition: background 0.2s; z-index: 2; line-height: 1;
}
.carousel-btn:hover { background: rgba(0,0,0,0.8); }
.carousel-prev { left: 12px; }
.carousel-next { right: 12px; }
.carousel-counter {
  position: absolute; bottom: 12px; right: 12px;
  background: rgba(0,0,0,0.6); color: #fff;
  font-size: 12px; padding: 2px 10px; border-radius: 10px;
}
.carousel-thumbs { display: flex; gap: 8px; padding: 10px; background: #111318; overflow-x: auto; }
.thumb-item {
  flex-shrink: 0; width: 64px; height: 64px; border-radius: 6px;
  overflow: hidden; border: 2px solid transparent; cursor: pointer;
  transition: border-color 0.2s; opacity: 0.6;
}
.thumb-item:hover { opacity: 0.9; }
.thumb-item.active { border-color: #00ff88; opacity: 1; }
.thumb-item img { width: 100%; height: 100%; object-fit: cover; display: block; }

/* ---- Lightbox ---- */
.lightbox-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.9); display: flex;
  align-items: center; justify-content: center;
}
.lightbox-content { position: relative; max-width: 90vw; max-height: 90vh; }
.lightbox-content img { max-width: 90vw; max-height: 90vh; object-fit: contain; display: block; }
.lightbox-close {
  position: absolute; top: -40px; right: 0;
  background: none; border: none; color: #fff;
  font-size: 24px; cursor: pointer;
}
.lightbox-btn {
  position: absolute; top: 50%; transform: translateY(-50%);
  background: rgba(255,255,255,0.15); color: #fff; border: none;
  width: 48px; height: 48px; border-radius: 50%;
  font-size: 30px; cursor: pointer; display: flex;
  align-items: center; justify-content: center;
  transition: background 0.2s; line-height: 1;
}
.lightbox-btn:hover { background: rgba(255,255,255,0.3); }
.lightbox-prev { left: -60px; }
.lightbox-next { right: -60px; }

.img-placeholder {
  aspect-ratio: 4/3; display: flex; align-items: center; justify-content: center;
  background: #111318; border: 1px solid #242837; border-radius: 12px; font-size: 64px;
}

.detail-right { display: flex; flex-direction: column; gap: 24px; }

.detail-badges { display: flex; gap: 8px; margin-bottom: 12px; }
.detail-title { font-family: 'Bebas Neue', sans-serif; font-size: 40px; letter-spacing: 1px; margin-bottom: 4px; }
.detail-type { color: #555d6e; font-size: 14px; margin-bottom: 16px; }

/* ---- Tags ---- */
.product-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.tag {
  display: inline-block; padding: 4px 12px; border-radius: 100px;
  font-size: 12px; background: #2a3a2a; color: #00ff88;
  border: 1px solid rgba(0,255,136,0.2);
}

.detail-price { font-size: 32px; font-weight: 600; color: #00ff88; margin-bottom: 24px; }
.detail-unit { font-size: 16px; color: #555d6e; font-weight: 400; }

.detail-meta { border-top: 1px solid #242837; padding-top: 16px; margin-bottom: 20px; }
.meta-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; }
.meta-label { color: #555d6e; }

.detail-desc { border-top: 1px solid #242837; padding-top: 16px; margin-bottom: 24px; }
.detail-desc h3 { font-size: 13px; color: #8892a4; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; }
.detail-desc p { font-size: 14px; line-height: 1.7; color: #8892a4; }

.detail-actions { display: flex; gap: 12px; }
.tech-btn-primary { padding: 14px 32px; background: #00ff88; color: #000; border: none; border-radius: 6px; font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 15px; cursor: pointer; transition: all 0.2s; }
.tech-btn-primary:hover { background: #00cc6a; box-shadow: 0 0 20px rgba(0,255,136,0.3); }
.tech-btn-outline { padding: 14px 32px; background: transparent; border: 1px solid #242837; border-radius: 6px; color: #8892a4; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; transition: all 0.2s; }
.tech-btn-outline:hover { border-color: #00ff88; color: #00ff88; }

.reviews-section { border-top: 1px solid #242837; padding-top: 20px; margin-top: 24px; }
.reviews-title { font-size: 13px; color: #8892a4; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 12px; }
.reviews-list { display: flex; flex-direction: column; gap: 12px; }
.review-item { background: #1e2230; border-radius: 8px; padding: 14px 16px; }
.review-header { display: flex; gap: 12px; align-items: center; margin-bottom: 4px; font-size: 13px; }
.review-stars { color: #ff8800; }
.review-user { color: #555d6e; }
.review-time { color: #3a4050; margin-left: auto; font-size: 11px; }
.review-content { font-size: 13px; color: #8892a4; line-height: 1.5; }
.reviews-empty { color: #555d6e; font-size: 13px; padding: 12px 0; }

.fav-btn {
  padding: 14px 20px;
  background: transparent;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #8892a4;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
}
.fav-btn:hover { border-color: #ff4466; color: #ff4466; }
.fav-btn.favorited { border-color: #ff4466; color: #ff4466; }

/* ---- Merchant Card ---- */
.merchant-card {
  background: #161921;
  border: 1px solid #242837;
  border-radius: 12px;
  padding: 20px;
}
.merchant-card-title {
  font-size: 13px; color: #8892a4;
  text-transform: uppercase; letter-spacing: 1px;
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 1px solid #242837;
}
.merchant-info-row {
  display: flex; justify-content: space-between;
  padding: 6px 0; font-size: 14px;
}
.merchant-label { color: #555d6e; }
.merchant-value { color: #e0e4ec; }
.merchant-contact-btn {
  width: 100%; margin-top: 16px; padding: 12px;
  background: transparent; border: 1px solid #00ff88;
  border-radius: 6px; color: #00ff88;
  font-family: 'Outfit', sans-serif; font-size: 14px;
  cursor: pointer; transition: all 0.2s;
}
.merchant-contact-btn:hover {
  background: #00ff88; color: #000;
}

/* ---- Recommended Products ---- */
.recommended-section { margin-top: 48px; padding-top: 32px; border-top: 1px solid #242837; }
.recommended-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 28px; letter-spacing: 1px;
  margin-bottom: 20px;
}
.recommended-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.recommended-card {
  background: #161921; border: 1px solid #242837;
  border-radius: 10px; overflow: hidden; cursor: pointer;
  transition: all 0.2s;
}
.recommended-card:hover { border-color: #00ff88; transform: translateY(-2px); }
.recommended-img { aspect-ratio: 4/3; overflow: hidden; }
.recommended-img img { width: 100%; height: 100%; object-fit: cover; display: block; }
.recommended-img-placeholder {
  width: 100%; height: 100%; display: flex;
  align-items: center; justify-content: center;
  background: #111318; font-size: 36px;
}
.recommended-info { padding: 12px; }
.recommended-name { font-size: 14px; margin-bottom: 6px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.recommended-price { font-size: 16px; font-weight: 600; color: #00ff88; }
.recommended-unit { font-size: 12px; color: #555d6e; font-weight: 400; }

.empty-state { text-align: center; padding: 80px 20px; color: #555d6e; }
.empty-state p { margin-bottom: 16px; }

.skeleton-detail { display: flex; flex-direction: column; gap: 16px; }
.skeleton-img { height: 300px; background: #161921; border-radius: 12px; animation: shimmer 1.5s infinite; }
.skeleton-text { height: 24px; background: #161921; border-radius: 6px; animation: shimmer 1.5s infinite; }
.skeleton-text.short { width: 60%; }
@keyframes shimmer { 0% { opacity: 0.5; } 50% { opacity: 1; } 100% { opacity: 0.5; } }

@media (max-width: 768px) {
  .detail-layout { grid-template-columns: 1fr; }
  .detail-image { position: static; }
  .recommended-grid { grid-template-columns: repeat(2, 1fr); }
  .lightbox-prev { left: 12px; }
  .lightbox-next { right: 12px; }
}
</style>
