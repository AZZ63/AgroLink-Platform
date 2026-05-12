<template>
  <div class="market-page">
    <NavBar />

    <div class="market-layout">
      <!-- Sidebar Filters -->
      <aside class="sidebar">
        <div class="sidebar-section">
          <h3 class="sidebar-title">供需类型</h3>
          <div class="filter-chips">
            <button :class="['chip', { active: query.infoType === '' }]" @click="query.infoType = ''; handleSearch()">全部</button>
            <button :class="['chip', 'chip-supply', { active: query.infoType === 'SUPPLY' }]" @click="query.infoType = 'SUPPLY'; handleSearch()">🌾 供应</button>
            <button :class="['chip', 'chip-demand', { active: query.infoType === 'DEMAND' }]" @click="query.infoType = 'DEMAND'; handleSearch()">🛒 需求</button>
          </div>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">搜索</h3>
          <div class="search-wrapper">
            <input v-model="query.keyword" placeholder="搜索产品名称..." class="sidebar-input search-input" @keyup.enter="handleSearch" @focus="showHistory = true" @blur="hideHistoryDelayed" />
            <div v-if="showHistory && searchHistory.length" class="search-history-dropdown">
              <div class="sh-header">
                <span>搜索历史</span>
                <button class="sh-clear" @mousedown.prevent="clearHistory">清除</button>
              </div>
              <div v-for="kw in searchHistory" :key="kw" class="sh-item" @mousedown.prevent="selectHistory(kw)">{{ kw }}</div>
            </div>
          </div>
          <div v-if="!query.keyword" class="hot-tags">
            <span class="hot-label">热门搜索:</span>
            <span v-for="tag in hotTags" :key="tag" class="hot-tag" @click="selectHot(tag)">{{ tag }}</span>
          </div>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">产品类型</h3>
          <select v-model="query.type" class="sidebar-select" @change="handleSearch">
            <option value="">全部类型</option>
            <option v-for="t in types" :key="t" :value="t">{{ t }}</option>
          </select>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">省份</h3>
          <select v-model="query.province" class="sidebar-select" @change="handleSearch">
            <option value="">全部地区</option>
            <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
          </select>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">价格区间</h3>
          <div class="price-range">
            <input v-model="query.minPrice" placeholder="最低" class="sidebar-input" />
            <span class="price-sep">—</span>
            <input v-model="query.maxPrice" placeholder="最高" class="sidebar-input" />
          </div>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">状态</h3>
          <select v-model="query.status" class="sidebar-select" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="LISTED">已上架</option>
            <option value="SOLD">已售</option>
            <option value="PENDING">待匹配</option>
          </select>
        </div>

        <div class="sidebar-actions">
          <button class="tech-btn tech-btn-primary sidebar-btn" @click="handleSearch">查询</button>
          <button class="tech-btn-outline sidebar-btn" @click="resetSearch">重置</button>
        </div>
      </aside>

      <!-- Main -->
      <main class="market-main">
        <div class="market-toolbar">
          <span class="result-count" v-if="total > 0">共 {{ total }} 条</span>
          <div class="toolbar-right">
            <select v-model="query.sortBy" class="sort-select" @change="handleSearch">
              <option value="">最新发布</option>
              <option value="price_asc">价格 ↑</option>
              <option value="price_desc">价格 ↓</option>
            </select>
          </div>
        </div>

        <!-- Skeleton -->
        <div v-if="loading && !products.length" class="products-grid">
          <div v-for="i in 8" :key="i" class="skeleton-card"></div>
        </div>

        <!-- Product Grid -->
        <div v-else-if="products.length" class="products-grid pl-grid">
          <div v-for="p in products" :key="p.id" class="tech-card pl-card" @click="$router.push('/products/' + p.id)" style="cursor:pointer">
            <div class="pl-card-image">
              <span class="pl-card-emoji">{{ productEmoji(p.type) }}</span>
            </div>
            <div class="pl-card-body">
              <div class="pl-card-header">
                <span class="tech-badge" :class="p.infoType === 'SUPPLY' ? 'tech-badge-supply' : 'tech-badge-demand'">
                  {{ p.infoType === 'SUPPLY' ? '供应' : '需求' }}
                </span>
                <span class="pl-card-views">👁️ {{ p.viewCount || 0 }}</span>
              </div>
              <h3 class="pl-card-name">{{ p.name }}</h3>
              <div class="pl-card-meta">{{ p.type }} · {{ p.province || '未知' }}{{ p.city || '' }}</div>
              <div class="pl-card-price">¥{{ p.price }} <span class="pl-card-unit">/ {{ p.unit }}</span></div>
              <div class="pl-card-qty">库存: {{ p.quantity }}{{ p.unit }}</div>
              <div class="pl-card-actions">
                <button v-if="userStore.isMerchant && p.infoType === 'SUPPLY' && p.status === 'LISTED'" class="pl-btn" @click.stop="handleOrder(p)">🛒 加入购物车</button>
                <button v-else-if="p.status === 'LISTED'" class="pl-btn pl-btn-outline" @click.stop="handleOrder(p)">📋 立即下单</button>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <EmptyState v-if="!loading && !products.length" icon="📭" title="暂无匹配的产品信息" description="试试调整筛选条件或搜索其他关键词" actionText="重置筛选" @action="resetSearch" />

        <!-- Pagination -->
        <div v-if="total > 0" class="pagination">
          <button :disabled="query.page <= 1" class="page-btn" @click="query.page--; loadProducts()">‹</button>
          <span class="page-info">{{ query.page }} / {{ Math.ceil(total / query.size) }}</span>
          <button :disabled="query.page >= Math.ceil(total / query.size)" class="page-btn" @click="query.page++; loadProducts()">›</button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { productApi, orderApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import EmptyState from '../components/EmptyState.vue'
import { statusLabel } from '../utils/constants'

const route = useRoute()
const userStore = useUserStore()
const products = ref([])
const types = ref([])
const total = ref(0)
const loading = ref(false)

const provinces = ['北京','天津','上海','重庆','河北','山西','辽宁','吉林','黑龙江','江苏','浙江','安徽','福建','江西','山东','河南','湖北','湖南','广东','广西','海南','四川','贵州','云南','西藏','陕西','甘肃','青海','宁夏','新疆','内蒙古','香港','澳门','台湾']

const query = reactive({
  keyword: '', type: '', infoType: route.query.infoType || '', status: '', province: '',
  minPrice: '', maxPrice: '', sortBy: '', page: 1, size: 20
})

const showHistory = ref(false)
const searchHistory = ref(JSON.parse(localStorage.getItem('searchHistory') || '[]'))

function saveSearch(keyword) {
  if (!keyword) return
  let h = JSON.parse(localStorage.getItem('searchHistory') || '[]')
  h = [keyword, ...h.filter(item => item !== keyword)].slice(0, 10)
  localStorage.setItem('searchHistory', JSON.stringify(h))
  searchHistory.value = h
}

function clearHistory() {
  localStorage.removeItem('searchHistory')
  searchHistory.value = []
}

function selectHistory(kw) {
  query.keyword = kw
  handleSearch()
}

function hideHistoryDelayed() {
  setTimeout(() => { showHistory.value = false }, 200)
}

const hotTags = ['水果', '蔬菜', '大米']

function selectHot(tag) {
  query.keyword = tag
  loadProducts()
}

async function loadProducts() {
  loading.value = true
  try {
    const params = { ...query }
    if (params.sortBy === 'price_asc') { params.sortBy = 'price'; params.sortOrder = 'asc' }
    else if (params.sortBy === 'price_desc') { params.sortBy = 'price'; params.sortOrder = 'desc' }
    else { delete params.sortBy }
    Object.keys(params).forEach(k => { if (!params[k] && k !== 'page' && k !== 'size') delete params[k] })
    const res = await productApi.query(params)
    products.value = res.records
    total.value = res.total
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

async function loadTypes() {
  try { types.value = await productApi.getTypes() }
  catch (e) { /* ignore */ }
}

function handleSearch() { query.page = 1; saveSearch(query.keyword); loadProducts() }

function resetSearch() {
  query.keyword = ''; query.type = ''; query.infoType = ''; query.status = '';
  query.province = ''; query.minPrice = ''; query.maxPrice = ''; query.sortBy = '';
  query.page = 1; loadProducts()
}

function productEmoji(type) {
  const map = { '粮食': '🌾', '蔬菜': '🥬', '水果': '🍎', '肉禽': '🥩', '粮油': '🫘', '调味品': '🧂', '水产': '🐟', '茶叶': '🍵', '干货': '🥜', '药材': '🌿', '饲料': '🌽', '种子': '🌱' }
  return map[type] || '📦'
}

async function handleOrder(product) {
  try {
    await orderApi.create({ productId: product.id, quantity: 1, infoType: product.infoType })
    ElMessage.success('下单成功，等待卖家确认')
    loadProducts()
  } catch (e) { ElMessage.error(e.message || '下单失败') }
}

onMounted(() => { loadProducts(); loadTypes() })
</script>

<style scoped>
.tech-btn-outline {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #8892a4;
  font-family: 'Outfit', sans-serif;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.tech-btn-outline:hover { border-color: #00ff88; color: #00ff88; }

/* Layout */
.market-layout { display: flex; max-width: 1280px; margin: 0 auto; padding: 24px 32px; gap: 32px; }

/* Sidebar */
.sidebar { width: 260px; flex-shrink: 0; }
.sidebar-section { margin-bottom: 24px; }
.sidebar-title {
  font-size: 11px;
  font-weight: 600;
  color: #555d6e;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  margin-bottom: 10px;
}
.sidebar-input, .sidebar-select {
  width: 100%;
  padding: 10px 12px;
  background: #1e2230;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #e8edf5;
  font-size: 13px;
  font-family: 'Outfit', sans-serif;
  outline: none;
}
.sidebar-input:focus, .sidebar-select:focus { border-color: #00ff88; }
.sidebar-select { appearance: none; background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23555d6e' stroke-width='1.5' fill='none'/%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 12px center; }

/* Search History */
.search-wrapper { position: relative; }
.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #1e2230;
  border: 1px solid #242837;
  border-top: none;
  border-radius: 0 0 6px 6px;
  z-index: 10;
  max-height: 240px;
  overflow-y: auto;
}
.sh-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; border-bottom: 1px solid #242837; font-size: 11px; color: #555d6e; text-transform: uppercase; letter-spacing: 0.5px; }
.sh-clear { background: none; border: none; color: #ff4757; font-size: 11px; cursor: pointer; padding: 0; font-family: 'Outfit', sans-serif; }
.sh-clear:hover { text-decoration: underline; }
.sh-item { padding: 8px 12px; font-size: 13px; color: #8892a4; cursor: pointer; transition: background 0.15s; }
.sh-item:hover { background: rgba(255,255,255,0.04); color: #e8edf5; }
.hot-tags { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; margin-top: 8px; }
.hot-label { font-size: 11px; color: #555d6e; white-space: nowrap; }
.hot-tag { padding: 2px 10px; background: #1e2230; border: 1px solid #242837; border-radius: 12px; font-size: 12px; color: #8892a4; cursor: pointer; transition: all 0.2s; }
.hot-tag:hover { border-color: #00ff88; color: #00ff88; }

.filter-chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  padding: 6px 14px;
  background: #1e2230;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #8892a4;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.chip.active { background: rgba(0,255,136,0.1); border-color: #00ff88; color: #00ff88; }
.chip-supply.active { background: rgba(0,255,136,0.1); border-color: #00ff88; color: #00ff88; }
.chip-demand.active { background: rgba(255,136,0,0.1); border-color: #ff8800; color: #ff8800; }

.price-range { display: flex; align-items: center; gap: 8px; }
.price-sep { color: #555d6e; font-size: 12px; }

.sidebar-actions { display: flex; gap: 8px; margin-top: 8px; }
.sidebar-btn { flex: 1; padding: 10px; font-size: 13px; width: auto; }

/* Main */
.market-main { flex: 1; min-width: 0; }
.market-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.result-count { font-size: 14px; color: #8892a4; font-family: 'JetBrains Mono', monospace; }
.sort-select {
  padding: 8px 12px;
  background: #1e2230;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #e8edf5;
  font-size: 13px;
  font-family: 'Outfit', sans-serif;
  outline: none;
}
.sort-select:focus { border-color: #00ff88; }

.pl-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }

/* Enhanced Product Card */
.pl-card {
  padding: 0;
  overflow: hidden;
  background: #161921;
  border: 1px solid #242837;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.pl-card:hover {
  transform: translateY(-4px);
  border-color: rgba(0,255,136,0.4);
  box-shadow: 0 12px 40px rgba(0,0,0,0.4), 0 0 30px rgba(0,255,136,0.08);
}

.pl-card-image {
  height: 110px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(0,255,136,0.04) 0%, rgba(0,204,255,0.04) 100%);
  border-bottom: 1px solid #242837;
  position: relative;
  overflow: hidden;
}
.pl-card-image::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 40%, rgba(22,25,33,0.9) 100%);
  pointer-events: none;
}
.pl-card-emoji {
  font-size: 48px;
  line-height: 1;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.3));
  z-index: 1;
}

.pl-card-body {
  padding: 14px 16px 16px;
}

.pl-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.pl-card-views {
  font-size: 11px;
  color: #555d6e;
  font-family: 'JetBrains Mono', monospace;
  letter-spacing: 0.3px;
}

.pl-card-name {
  font-size: 15px;
  font-weight: 600;
  color: #e8edf5;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pl-card-meta {
  font-size: 12px;
  color: #555d6e;
  margin-bottom: 8px;
}

.pl-card-price {
  font-size: 20px;
  font-weight: 700;
  color: #00ff88;
  font-family: 'JetBrains Mono', monospace;
  margin-bottom: 2px;
}
.pl-card-unit {
  font-size: 12px;
  color: #555d6e;
  font-weight: 400;
  font-family: 'Outfit', sans-serif;
}

.pl-card-qty {
  font-size: 12px;
  color: #8892a4;
  margin-bottom: 0;
}

.pl-card-actions {
  margin-top: 12px;
}

.pl-btn {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 6px;
  font-family: 'Outfit', sans-serif;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: #00ff88;
  color: #000;
}
.pl-btn:hover {
  background: #00cc6a;
  box-shadow: 0 0 20px rgba(0,255,136,0.2);
}

.pl-btn-outline {
  background: transparent;
  border: 1px solid #00ff88;
  color: #00ff88;
}
.pl-btn-outline:hover {
  background: rgba(0,255,136,0.1);
  box-shadow: 0 0 20px rgba(0,255,136,0.1);
}

.skeleton-card { height: 180px; background: #161921; border: 1px solid #242837; border-radius: 8px; animation: shimmer 1.5s infinite; }
@keyframes shimmer { 0% { opacity: 0.4; } 50% { opacity: 1; } 100% { opacity: 0.4; } }

.empty-state { text-align: center; padding: 80px 0; color: #555d6e; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-state p { margin-bottom: 16px; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 16px; margin-top: 32px; }
.page-btn {
  padding: 8px 16px;
  background: #1e2230;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #e8edf5;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
}
.page-btn:hover:not(:disabled) { border-color: #00ff88; }
.page-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.page-info { font-family: 'JetBrains Mono', monospace; font-size: 14px; color: #8892a4; }

/* Responsive */
@media (max-width: 1024px) {
  .pl-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .pl-grid { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .market-layout { flex-direction: column; padding: 16px; }
  .sidebar { width: 100%; }
}
</style>
