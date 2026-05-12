<template>
  <div class="fav-page">
    <div class="page-heading">
      <h1 class="page-title">我的收藏</h1>
      <p class="page-subtitle">{{ products.length }} 件收藏商品</p>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>

    <div v-else-if="products.length === 0" class="empty-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
      <p>还没有收藏的商品</p>
      <button class="btn btn-outline btn-sm" @click="$router.push('/products')">去市场看看</button>
    </div>

    <div v-else class="fav-grid">
      <div v-for="p in products" :key="p.id" class="fav-card" @click="$router.push(`/products/${p.id}`)">
        <div class="fc-image">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
          <button class="fc-unfav" @click.stop="handleRemove(p)">×</button>
        </div>
        <div class="fc-body">
          <h3 class="fc-name">{{ p.name }}</h3>
          <div class="fc-tags">
            <span class="fc-tag">{{ p.type }}</span>
            <span v-if="p.province" class="fc-tag">{{ p.province }}</span>
          </div>
          <div class="fc-price">¥{{ p.price }}/{{ p.unit }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { favoriteApi } from '../../api/http'

const products = ref([])
const loading = ref(true)

async function loadFavs() {
  try { products.value = await favoriteApi.list() || [] }
  catch { ElMessage.error('加载收藏失败') }
  finally { loading.value = false }
}

async function handleRemove(p) {
  try { await favoriteApi.toggle(p.id); products.value = products.value.filter(x => x.id !== p.id); ElMessage.success('已取消收藏') }
  catch (e) { ElMessage.error(e.message) }
}

onMounted(loadFavs)
</script>

<style scoped>
.fav-page { max-width: 1200px; }
.page-heading { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.loading-state, .empty-state { text-align: center; padding: 60px; color: #6b7280; }
.empty-state p { margin: 16px 0; }

.fav-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.fav-card {
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 16px; overflow: hidden;
  cursor: pointer; transition: all 0.2s;
}
.fav-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,0.08); transform: translateY(-2px); }

.fc-image {
  height: 160px; background: #f5f7f2; display: flex; align-items: center; justify-content: center;
  position: relative;
}
.fc-unfav {
  position: absolute; top: 10px; right: 10px; width: 28px; height: 28px;
  border-radius: 8px; border: none; background: rgba(0,0,0,0.4); color: white;
  font-size: 16px; cursor: pointer; display: flex; align-items: center; justify-content: center;
}
.fc-unfav:hover { background: #ef4444; }

.fc-body { padding: 14px; }
.fc-name { font-size: 14px; font-weight: 600; color: #1e1e1e; margin: 0 0 8px; }
.fc-tags { display: flex; gap: 6px; margin-bottom: 8px; }
.fc-tag { padding: 2px 8px; background: #f5f7f2; border-radius: 5px; font-size: 11px; color: #6b7280; }
.fc-price { font-size: 16px; font-weight: 700; color: #7CCF5F; font-family: 'JetBrains Mono', monospace; }

@media (max-width: 900px) { .fav-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .fav-grid { grid-template-columns: 1fr; } }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 8px; transition: all 0.15s; }
.btn-outline { padding: 8px 16px; border: 1px solid #e8ebe4; background: transparent; color: #374151; font-size: 13px; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }
.btn-sm { font-size: 12px; }
</style>
