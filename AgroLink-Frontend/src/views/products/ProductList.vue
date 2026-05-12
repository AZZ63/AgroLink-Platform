<template>
  <div class="product-list-page">
    <!-- 页面头部 -->
    <div class="page-heading">
      <div>
        <h1 class="page-title">农产品市场</h1>
        <p class="page-subtitle">发现优质农产品，连接农户与商户</p>
      </div>
      <button class="btn btn-outline" @click="$router.push('/publish')">+ 发布供需</button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-main">
        <div class="search-box">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          <input v-model="query.keyword" placeholder="搜索农产品名称..." @keyup.enter="handleSearch" @focus="showHistory = true" @blur="hideHistoryDelayed" />
          <button class="btn btn-primary btn-sm" @click="handleSearch">搜索</button>
        </div>
        <!-- 搜索历史下拉 -->
        <div v-if="showHistory && searchHistory.length" class="search-history-dropdown">
          <div class="sh-header"><span>搜索历史</span><button class="sh-clear" @click="clearHistory">清空</button></div>
          <div class="sh-tags">
            <span v-for="kw in searchHistory" :key="kw" class="sh-tag" @mousedown.prevent="selectHistory(kw)">{{ kw }}</span>
          </div>
        </div>
      </div>
      <div class="supply-demand-tabs">
        <button :class="['tab-btn', { active: !query.infoType }]" @click="query.infoType = ''; query.page = 1; loadProducts()">全部</button>
        <button :class="['tab-btn', { active: query.infoType === 'SUPPLY' }]" @click="query.infoType = 'SUPPLY'; query.page = 1; loadProducts()">供应</button>
        <button :class="['tab-btn', { active: query.infoType === 'DEMAND' }]" @click="query.infoType = 'DEMAND'; query.page = 1; loadProducts()">需求</button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-group">
        <label>分类</label>
        <select v-model="query.type" @change="query.page = 1; loadProducts()">
          <option value="">全部分类</option>
          <option v-for="t in types" :key="t" :value="t">{{ t }}</option>
        </select>
      </div>
      <div class="filter-group">
        <label>产地</label>
        <select v-model="query.province" @change="query.page = 1; loadProducts()">
          <option value="">全部地区</option>
          <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
        </select>
      </div>
      <div class="filter-group">
        <label>价格</label>
        <div class="price-range">
          <input v-model="query.minPrice" placeholder="最低" type="number" />
          <span class="sep">—</span>
          <input v-model="query.maxPrice" placeholder="最高" type="number" />
        </div>
      </div>
      <div class="filter-group">
        <label>排序</label>
        <select v-model="query.sortBy" @change="query.page = 1; loadProducts()">
          <option value="">默认排序</option>
          <option value="price_asc">价格从低到高</option>
          <option value="price_desc">价格从高到低</option>
          <option value="newest">最新发布</option>
        </select>
      </div>
    </div>

    <!-- 商品网格 -->
    <div v-if="loading" class="grid-loading">
      <div v-for="i in 8" :key="i" class="skeleton-card"></div>
    </div>

    <div v-else-if="products.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <p>没有找到匹配的农产品</p>
      <button class="btn btn-outline btn-sm" @click="resetFilters">清除筛选</button>
    </div>

    <div v-else class="product-grid">
      <div v-for="p in products" :key="p.id" class="product-card" @click="$router.push(`/products/${p.id}`)">
        <div class="card-image">
          <div class="img-placeholder">
            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
          </div>
          <span :class="['card-badge', p.infoType === 'SUPPLY' ? 'supply' : 'demand']">
            {{ p.infoType === 'SUPPLY' ? '供应' : '需求' }}
          </span>
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ p.name }}</h3>
          <div class="card-tags">
            <span class="card-tag">{{ p.type }}</span>
            <span v-if="p.province" class="card-tag">
              <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              {{ p.province }}
            </span>
          </div>
          <div class="card-price-row">
            <span class="card-price">¥{{ p.price }}/{{ p.unit }}</span>
            <span class="card-stock">{{ p.quantity }}{{ p.unit }}</span>
          </div>
          <div class="card-footer">
            <span :class="['status-badge', p.status?.toLowerCase()]">{{ statusLabel(p.status) }}</span>
            <span class="card-user">{{ p.username || '用户' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > query.size" class="pagination">
      <span class="page-info">共 {{ total }} 条</span>
      <div class="page-btns">
        <button :disabled="query.page <= 1" @click="query.page--; loadProducts()" class="page-btn">上一页</button>
        <span class="page-current">{{ query.page }}/{{ Math.ceil(total / query.size) }}</span>
        <button :disabled="query.page >= Math.ceil(total / query.size)" @click="query.page++; loadProducts()" class="page-btn">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi } from '../../api/http'
import { statusLabel } from '../../utils/constants'

const route = useRoute()
const products = ref([])
const types = ref([])
const total = ref(0)
const loading = ref(false)
const showHistory = ref(false)

const provinces = ['北京','天津','上海','重庆','河北','山西','辽宁','吉林','黑龙江','江苏','浙江','安徽','福建','江西','山东','河南','湖北','湖南','广东','广西','海南','四川','贵州','云南','西藏','陕西','甘肃','青海','宁夏','新疆','内蒙古']

const query = reactive({
  keyword: route.query.search || '',
  type: route.query.type || '',
  infoType: route.query.infoType || '',
  province: '',
  minPrice: '', maxPrice: '',
  sortBy: '',
  page: 1, size: 20
})

const searchHistory = ref(JSON.parse(localStorage.getItem('searchHistory') || '[]'))

function saveSearch(kw) {
  if (!kw) return
  const h = [kw, ...searchHistory.value.filter(i => i !== kw)].slice(0, 10)
  localStorage.setItem('searchHistory', JSON.stringify(h))
  searchHistory.value = h
}

function clearHistory() {
  localStorage.removeItem('searchHistory')
  searchHistory.value = []
}

function selectHistory(kw) { query.keyword = kw; handleSearch() }
function hideHistoryDelayed() { setTimeout(() => { showHistory.value = false }, 200) }

async function loadProducts() {
  loading.value = true
  try {
    const params = { ...query }
    Object.keys(params).forEach(k => { if (!params[k]) delete params[k] })
    const res = await productApi.query(params)
    products.value = res.records || res || []
    total.value = res.total || products.value.length
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

async function loadTypes() {
  try { types.value = await productApi.getTypes() } catch {}
}

function handleSearch() { query.page = 1; saveSearch(query.keyword); loadProducts() }

function resetFilters() {
  query.keyword = ''; query.type = ''; query.province = '';
  query.minPrice = ''; query.maxPrice = ''; query.sortBy = '';
  query.page = 1; loadProducts()
}

onMounted(() => { loadProducts(); loadTypes() })
</script>

<style scoped>
.product-list-page {
  max-width: 1200px;
}

/* 头部 */
.page-heading { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

/* 搜索 */
.search-section { margin-bottom: 20px; }
.search-main { position: relative; display: flex; gap: 16px; align-items: center; flex-wrap: wrap; }
.search-box {
  display: flex; align-items: center; gap: 8px; flex: 1; min-width: 280px;
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 12px; padding: 10px 16px;
  transition: all 0.2s;
}
.search-box:focus-within { border-color: #7CCF5F; box-shadow: 0 0 0 3px rgba(124,207,95,0.1); }
.search-icon { color: #9ca3af; flex-shrink: 0; }
.search-box input { flex: 1; border: none; outline: none; font-size: 14px; color: #1e1e1e; font-family: 'Inter', sans-serif; background: transparent; }
.search-box input::placeholder { color: #9ca3af; }

/* 搜索历史 */
.search-history-dropdown {
  position: absolute; top: 100%; left: 0; right: 0; margin-top: 4px;
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 12px; padding: 14px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08); z-index: 50; max-width: 400px;
}
.sh-header { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 12px; color: #6b7280; }
.sh-clear { background: none; border: none; color: #7CCF5F; cursor: pointer; font-size: 12px; }
.sh-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.sh-tag {
  padding: 4px 10px; background: #f5f7f2; border: 1px solid #e8ebe4; border-radius: 6px;
  font-size: 12px; color: #374151; cursor: pointer; transition: all 0.15s;
}
.sh-tag:hover { border-color: #7CCF5F; color: #7CCF5F; }

.supply-demand-tabs { display: flex; gap: 4px; margin-top: 12px; background: #f5f7f2; border-radius: 10px; padding: 3px; width: fit-content; }
.tab-btn {
  padding: 7px 18px; border: none; border-radius: 8px; font-size: 13px; font-weight: 500;
  color: #6b7280; cursor: pointer; font-family: 'Inter', sans-serif; transition: all 0.15s; background: transparent;
}
.tab-btn.active { background: #ffffff; color: #1e1e1e; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.tab-btn:hover:not(.active) { color: #374151; }

/* 筛选栏 */
.filter-bar {
  display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 24px;
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 14px; padding: 16px 20px;
}
.filter-group { display: flex; align-items: center; gap: 8px; }
.filter-group label { font-size: 12px; font-weight: 600; color: #6b7280; white-space: nowrap; }
.filter-group select, .filter-group input {
  padding: 7px 12px; border: 1px solid #e8ebe4; border-radius: 8px; font-size: 13px;
  color: #374151; background: #f9faf8; outline: none; font-family: 'Inter', sans-serif;
}
.filter-group select:focus, .filter-group input:focus { border-color: #7CCF5F; }
.price-range { display: flex; align-items: center; gap: 4px; }
.price-range input { width: 80px; }
.price-range .sep { color: #d1d5db; font-size: 12px; }

/* 商品网格 */
.product-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 32px;
}

.product-card {
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 16px;
  overflow: hidden; cursor: pointer; transition: all 0.2s ease;
}
.product-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,0.08); transform: translateY(-2px); }

.card-image {
  position: relative; height: 180px; background: #f5f7f2; display: flex; align-items: center; justify-content: center;
}
.img-placeholder { opacity: 0.4; }
.card-badge {
  position: absolute; top: 12px; left: 12px; padding: 4px 10px; border-radius: 6px;
  font-size: 11px; font-weight: 600;
}
.card-badge.supply { background: #e8f5e4; color: #7CCF5F; }
.card-badge.demand { background: #fef3c7; color: #f59e0b; }

.card-body { padding: 16px; }
.card-title { font-size: 15px; font-weight: 600; color: #1e1e1e; margin: 0 0 8px; }

.card-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 10px; }
.card-tag {
  display: inline-flex; align-items: center; gap: 3px;
  padding: 2px 8px; background: #f5f7f2; border-radius: 5px; font-size: 11px; color: #6b7280;
}

.card-price-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.card-price { font-size: 18px; font-weight: 700; color: #7CCF5F; font-family: 'JetBrains Mono', monospace; }
.card-stock { font-size: 12px; color: #9ca3af; }

.card-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 10px; border-top: 1px solid #f0f0eb; }

.status-badge {
  display: inline-flex; padding: 2px 8px; border-radius: 5px; font-size: 10px; font-weight: 600;
}
.status-badge.listed { background: #e8f5e4; color: #7CCF5F; }
.status-badge.sold { background: #f0f0eb; color: #9ca3af; }
.status-badge.unlisted { background: #fee2e2; color: #ef4444; }
.status-badge.pending { background: #fef3c7; color: #f59e0b; }
.status-badge.completed { background: #e8f5e4; color: #7CCF5F; }
.status-badge.closed { background: #f0f0eb; color: #9ca3af; }

.card-user { font-size: 12px; color: #9ca3af; }

/* 加载骨架 */
.grid-loading { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 32px; }
.skeleton-card { height: 320px; background: #ffffff; border: 1px solid #e8ebe4; border-radius: 16px; animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }

.empty-state { text-align: center; padding: 60px 20px; color: #6b7280; }
.empty-state p { margin: 16px 0; }

/* 分页 */
.pagination { display: flex; justify-content: space-between; align-items: center; padding: 16px 0; }
.page-info { font-size: 13px; color: #6b7280; }
.page-btns { display: flex; align-items: center; gap: 8px; }
.page-btn {
  padding: 8px 16px; border: 1px solid #e8ebe4; border-radius: 8px; background: #ffffff;
  color: #374151; font-size: 13px; cursor: pointer; font-family: 'Inter', sans-serif; transition: all 0.15s;
}
.page-btn:hover:not(:disabled) { border-color: #7CCF5F; color: #7CCF5F; }
.page-btn:disabled { opacity: 0.4; cursor: default; }
.page-current { font-size: 13px; color: #6b7280; font-family: 'JetBrains Mono', monospace; }

/* 响应式 */
@media (max-width: 1200px) { .product-grid, .grid-loading { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px) { .product-grid, .grid-loading { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .product-grid, .grid-loading { grid-template-columns: 1fr; } }

/* 全局按钮复用 */
.btn { font-family: 'Inter', sans-serif; cursor: pointer; transition: all 0.15s; border-radius: 10px; }
.btn-primary { padding: 10px 20px; background: #7CCF5F; color: white; border: none; font-size: 13px; font-weight: 500; }
.btn-primary:hover { background: #6bc04e; }
.btn-outline { padding: 10px 20px; border: 1px solid #e8ebe4; background: transparent; color: #374151; font-size: 13px; font-weight: 500; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }
.btn-sm { padding: 8px 16px; font-size: 12px; }
</style>
