<template>
  <div class="fav-page">
    <NavBar />
    <div class="fav-container">
      <h1 class="page-title">我的收藏</h1>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="products.length" class="fav-grid">
        <div v-for="p in products" :key="p.id" class="tech-card fav-card" @click="$router.push('/products/' + p.id)">
          <div class="card-top">
            <span class="tech-badge" :class="p.infoType === 'SUPPLY' ? 'tech-badge-supply' : 'tech-badge-demand'">
              {{ p.infoType === 'SUPPLY' ? '供应' : '需求' }}
            </span>
            <span class="tech-status">{{ statusLabel(p.status) }}</span>
          </div>
          <h3 class="card-title">{{ p.name }}</h3>
          <div class="card-meta">{{ p.type }} · {{ p.province || '未知' }}{{ p.city || '' }}</div>
          <div class="card-price">¥{{ p.price }} <span class="card-unit">/ {{ p.unit }}</span></div>
        </div>
      </div>
      <EmptyState v-else icon="♡" title="还没有收藏任何产品" description="去市场逛逛，发现感兴趣的产品吧" actionText="去市场看看" actionLink="/products" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productApi, favoriteApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import EmptyState from '../components/EmptyState.vue'
import { statusLabel } from '../utils/constants'

const products = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const favs = await favoriteApi.list()
    const ids = favs.map(f => f.productId)
    if (ids.length) {
      const res = await productApi.query({ ids: ids.join(','), page: 1, size: 100 })
      products.value = res.records
    }
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.fav-container { max-width: 960px; margin: 0 auto; padding: 32px; }
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 36px; margin-bottom: 24px; }
.fav-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.fav-card { padding: 20px; cursor: pointer; transition: border-color 0.2s; }
.fav-card:hover { border-color: #00ff88; }
.card-top { display: flex; gap: 8px; margin-bottom: 12px; }
.card-title { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.card-meta { font-size: 12px; color: #555d6e; margin-bottom: 8px; }
.card-price { font-size: 20px; font-weight: 600; color: #00ff88; }
.card-unit { font-size: 13px; color: #555d6e; font-weight: 400; }
.empty-state { text-align: center; padding: 80px 20px; color: #555d6e; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-state p { margin-bottom: 16px; }
.loading { text-align: center; padding: 60px; color: #555d6e; }
.tech-btn-outline { padding: 8px 20px; background: transparent; border: 1px solid #242837; border-radius: 6px; color: #8892a4; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; transition: all 0.2s; }
.tech-btn-outline:hover { border-color: #00ff88; color: #00ff88; }
</style>
