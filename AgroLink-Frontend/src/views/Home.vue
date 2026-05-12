<template>
  <div class="home">
    <NavBar />

    <!-- Announcement Bar -->
    <div class="announcement-bar">
      <div class="announcement-track">
        <span v-for="(msg, i) in announcements" :key="i" class="announcement-item">{{ msg }}</span>
        <!-- Duplicate for seamless loop -->
        <span v-for="(msg, i) in announcements" :key="'dup-'+i" class="announcement-item">{{ msg }}</span>
      </div>
    </div>

    <!-- Hero -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-grid"></div>
        <div class="hero-grid-overlay"></div>
        <div class="hero-glow"></div>
        <div class="hero-glow-2"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">连接农户与采购商</h1>
        <p class="hero-desc">打造透明农产品交易平台，让每一份农产品的价值都被看见</p>
        <div class="hero-actions">
          <button class="tech-btn tech-btn-primary hero-btn" @click="$router.push('/products')">浏览市场</button>
          <button class="tech-btn-outline hero-btn" @click="$router.push('/publish')">发布信息</button>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="hero-stat-val" ref="heroStat0">0</span>
            <span class="hero-stat-lbl">成交笔数</span>
          </div>
          <span class="hero-stat-divider">·</span>
          <div class="hero-stat">
            <span class="hero-stat-val" ref="heroStat1">0</span>
            <span class="hero-stat-lbl">入驻农户</span>
          </div>
          <span class="hero-stat-divider">·</span>
          <div class="hero-stat">
            <span class="hero-stat-val" ref="heroStat2">0</span>
            <span class="hero-stat-lbl">注册商户</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Category Quick Links -->
    <section class="category-section section-container">
      <h2 class="tech-section-title">分类浏览</h2>
      <div class="category-scroll">
        <div
          v-for="cat in categories"
          :key="cat.name"
          class="category-card"
          @click="$router.push('/products?type=' + encodeURIComponent(cat.name))"
        >
          <span class="category-icon">{{ cat.icon }}</span>
          <span class="category-name">{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="features-section">
      <div class="section-container">
        <h2 class="tech-section-title">核心功能</h2>
        <div class="features-grid">
          <div class="feature-card" @click="$router.push('/products')">
            <div class="feature-icon">🔍</div>
            <h3>信息查询</h3>
            <p>按类型、价格、地区精准筛选供需信息</p>
          </div>
          <div class="feature-card" @click="$router.push('/publish')">
            <div class="feature-icon">📦</div>
            <h3>信息发布</h3>
            <p>农户发布供应，商户发布需求</p>
          </div>
          <div class="feature-card" @click="$router.push('/orders')">
            <div class="feature-icon">🤝</div>
            <h3>在线交易</h3>
            <p>下单、确认、成交，全流程线上化</p>
          </div>
          <div class="feature-card" @click="$router.push('/profile')">
            <div class="feature-icon">📊</div>
            <h3>数据看板</h3>
            <p>发布记录、成交数据一目了然</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Data Showcase -->
    <section class="stats-section section-container">
      <h2 class="tech-section-title">平台数据</h2>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">💰</div>
          <div class="stat-number">
            <span class="stat-value" ref="statEls0">{{ displayStats.turnover }}</span>
          </div>
          <div class="stat-label">平台累计成交</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📦</div>
          <div class="stat-number">
            <span class="stat-value" ref="statEls1">{{ displayStats.todayProducts }}</span>
          </div>
          <div class="stat-label">今日新增产品</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">👨‍🌾</div>
          <div class="stat-number">
            <span class="stat-value" ref="statEls2">{{ displayStats.farmers }}</span>
          </div>
          <div class="stat-label">入驻农户</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🏪</div>
          <div class="stat-number">
            <span class="stat-value" ref="statEls3">{{ displayStats.merchants }}</span>
          </div>
          <div class="stat-label">在线商户</div>
        </div>
      </div>
    </section>

    <!-- Platform Advantages -->
    <section class="advantages-section">
      <div class="section-container">
        <h2 class="tech-section-title">平台优势</h2>
        <div class="advantages-grid">
          <div class="advantage-card">
            <div class="advantage-icon-wrap">
              <span class="advantage-icon">🌱</span>
            </div>
            <h3>农户直连</h3>
            <p>跳过中间商，农户与商户直接交易</p>
          </div>
          <div class="advantage-card">
            <div class="advantage-icon-wrap">
              <span class="advantage-icon">📋</span>
            </div>
            <h3>信息透明</h3>
            <p>价格、产地、品质信息完全公开</p>
          </div>
          <div class="advantage-card">
            <div class="advantage-icon-wrap">
              <span class="advantage-icon">⚡</span>
            </div>
            <h3>快速撮合</h3>
            <p>智能匹配供需，快速达成交易</p>
          </div>
          <div class="advantage-card">
            <div class="advantage-icon-wrap">
              <span class="advantage-icon">🛡️</span>
            </div>
            <h3>交易保障</h3>
            <p>全流程线上化，资金安全有保障</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Latest products -->
    <section class="products-section">
      <div class="section-container">
        <h2 class="tech-section-title">最新发布</h2>
        <div v-if="loading" class="skeleton-grid">
          <div v-for="i in 4" :key="i" class="skeleton-card"></div>
        </div>
        <div v-else-if="hotProducts.length" class="products-grid enhanced-product-grid">
          <div v-for="p in hotProducts" :key="p.id" class="tech-card epc-card" @click="$router.push('/products/' + p.id)" style="cursor:pointer">
            <div class="epc-image">
              <span class="epc-emoji">{{ productEmoji(p.type) }}</span>
            </div>
            <div class="epc-body">
              <div class="epc-tags">
                <span class="tech-badge" :class="p.infoType === 'SUPPLY' ? 'tech-badge-supply' : 'tech-badge-demand'">
                  {{ p.infoType === 'SUPPLY' ? '供应' : '需求' }}
                </span>
                <span v-if="isHotProduct(p)" class="epc-hot-badge">🔥 热度</span>
              </div>
              <h3 class="epc-title">{{ p.name }}</h3>
              <div class="epc-type">{{ p.type }}</div>
              <div class="epc-price">¥{{ p.price }} <span class="epc-unit">/ {{ p.unit }}</span></div>
              <div class="epc-footer">
                <span v-if="p.province || p.city" class="epc-region">📍 {{ p.province }}{{ p.city }}</span>
                <span class="epc-views">👁️ {{ p.viewCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <footer class="footer">
      <p>© 2024 AGROLINK · 农产品供需交易平台</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { productApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import { statusLabel } from '../utils/constants'

const hotProducts = ref([])
const loading = ref(true)
const heroStat0 = ref(null)
const heroStat1 = ref(null)
const heroStat2 = ref(null)

// --- Announcements ---
const announcements = [
  '📢 新用户首单优惠进行中',
  '📢 春季农产品交易会即将开幕',
  '📢 平台已累计服务超过 5000 家农户',
  '📢 新增蔬菜分类，欢迎发布信息'
]

// --- Categories ---
const categories = [
  { icon: '🌾', name: '粮食' },
  { icon: '🥬', name: '蔬菜' },
  { icon: '🍎', name: '水果' },
  { icon: '🥩', name: '肉禽' },
  { icon: '🫘', name: '粮油' },
  { icon: '🧂', name: '调味品' }
]

// --- Stats ---
const displayStats = ref({
  turnover: 'N/A',
  todayProducts: 'N/A',
  farmers: '500+',
  merchants: '200+'
})

const statTargets = ref([0, 0, 500, 200])
const animated = ref(false)

function animateCounter(el, target, suffix = '') {
  if (!el) return
  const duration = 1500
  const start = performance.now()
  const isFloat = target % 1 !== 0
  function tick(now) {
    const t = Math.min((now - start) / duration, 1)
    // ease-out cubic
    const progress = 1 - Math.pow(1 - t, 3)
    let val
    if (isFloat) {
      val = (progress * target).toFixed(1)
    } else {
      val = Math.round(progress * target)
    }
    el.textContent = val + suffix
    if (t < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

function productEmoji(type) {
  const map = { '粮食': '🌾', '蔬菜': '🥬', '水果': '🍎', '肉禽': '🥩', '粮油': '🫘', '调味品': '🧂', '水产': '🐟', '茶叶': '🍵', '干货': '🥜', '药材': '🌿', '饲料': '🌽', '种子': '🌱' }
  return map[type] || '📦'
}

function isHotProduct(p) {
  return p.viewCount >= 10
}

onMounted(async () => {
  try {
    hotProducts.value = await productApi.getHot()
  } catch (e) { /* ignore */ }
  finally { loading.value = false }

  // Fetch stats
  try {
    const stats = await productApi.getStats()
    if (stats) {
      const turnover = stats.totalTurnover || stats.turnover || 0
      const today = stats.todayProducts || stats.todayCount || 0
      displayStats.value.turnover = turnover ? ('¥' + Number(turnover).toLocaleString() + '+') : 'N/A'
      displayStats.value.todayProducts = today ? String(today) : 'N/A'
      statTargets.value = [
        turnover || 0,
        today || 0,
        500,
        200
      ]
    }
  } catch (e) { /* use demo data */ }

  // Start counters after DOM update
  await nextTick()
  const els = [
    document.querySelector('.stats-grid .stat-card:nth-child(1) .stat-value'),
    document.querySelector('.stats-grid .stat-card:nth-child(2) .stat-value'),
    document.querySelector('.stats-grid .stat-card:nth-child(3) .stat-value'),
    document.querySelector('.stats-grid .stat-card:nth-child(4) .stat-value')
  ]
  // Animate turnover with ¥ prefix
  const targets = statTargets.value
  if (els[0] && targets[0] > 0) animateCounter(els[0], targets[0], '+')
  if (els[1] && targets[1] > 0) animateCounter(els[1], targets[1])
  if (els[2] && targets[2] > 0) animateCounter(els[2], targets[2], '+')
  if (els[3] && targets[3] > 0) animateCounter(els[3], targets[3], '+')
  animated.value = true

  // Animate hero stats after a short delay
  setTimeout(() => {
    const heroTargets = [2847, 1023, 856]
    const heroEls = [heroStat0.value, heroStat1.value, heroStat2.value]
    heroEls.forEach((el, i) => {
      if (el) animateCounter(el, heroTargets[i])
    })
  }, 600)
})
</script>

<style scoped>
/* === Tech Header extends === */
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
.tech-btn-outline:hover {
  border-color: #00ff88;
  color: #00ff88;
}

.section-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 60px 32px;
}

/* === Announcement Bar === */
.announcement-bar {
  background: #161921;
  border-bottom: 1px solid #242837;
  overflow: hidden;
  height: 40px;
  display: flex;
  align-items: center;
  position: relative;
}
.announcement-bar::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 40px;
  background: linear-gradient(90deg, #161921 0%, transparent 100%);
  z-index: 2;
  pointer-events: none;
}
.announcement-bar::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 40px;
  background: linear-gradient(270deg, #161921 0%, transparent 100%);
  z-index: 2;
  pointer-events: none;
}
.announcement-track {
  display: flex;
  gap: 64px;
  white-space: nowrap;
  animation: marquee 30s linear infinite;
}
.announcement-item {
  font-size: 13px;
  color: #00ff88;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}
@keyframes marquee {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

/* Hero */
.hero {
  position: relative;
  min-height: 70vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.hero-bg {
  position: absolute;
  inset: 0;
}
.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,255,136,0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,255,136,0.07) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
}

.hero-grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,255,136,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,255,136,0.04) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0;
  animation: heroGridPulse 6s ease-in-out infinite;
}
.hero-glow {
  position: absolute;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  width: 800px;
  height: 500px;
  background: radial-gradient(circle, rgba(0,255,136,0.1) 0%, transparent 70%);
  pointer-events: none;
  animation: heroGlowPulse 5s ease-in-out infinite;
}

.hero-glow-2 {
  position: absolute;
  bottom: 5%;
  right: 10%;
  width: 400px;
  height: 300px;
  background: radial-gradient(circle, rgba(0,204,255,0.06) 0%, transparent 70%);
  pointer-events: none;
  animation: heroGlowPulse2 7s ease-in-out infinite;
}

@keyframes heroGlowPulse2 {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.15); }
}

@keyframes heroGlowPulse {
  0%, 100% { opacity: 0.5; transform: translateX(-50%) scale(1); }
  50% { opacity: 1; transform: translateX(-50%) scale(1.1); }
}

@keyframes heroGridPulse {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 0.8; }
}
.hero-content {
  position: relative;
  text-align: center;
  z-index: 1;
  padding: 0 32px;
}
.hero-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 72px;
  letter-spacing: 3px;
  color: #e8edf5;
  margin-bottom: 12px;
}
.hero-desc {
  color: #8892a4;
  font-size: 18px;
  margin-bottom: 32px;
}
.hero-actions { display: flex; gap: 16px; justify-content: center; margin-bottom: 48px; }
.hero-btn {
  width: auto;
  padding: 14px 36px;
  font-size: 16px;
}
.hero-stats {
  display: flex;
  align-items: center;
  gap: 0;
  justify-content: center;
}
.hero-stat {
  text-align: center;
  padding: 0 32px;
}
.hero-stat-divider {
  color: #00ff88;
  font-size: 24px;
  font-weight: 700;
  opacity: 0.4;
}
.hero-stat-val {
  display: block;
  font-family: 'JetBrains Mono', monospace;
  font-size: 28px;
  color: #00ff88;
}
.hero-stat-lbl {
  font-size: 13px;
  color: #555d6e;
  letter-spacing: 1px;
}

/* === Category Quick Links === */
.category-section {
  padding-top: 48px;
  padding-bottom: 48px;
}
.category-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 8px 4px 16px;
  scrollbar-width: thin;
  scrollbar-color: #242837 transparent;
  -webkit-overflow-scrolling: touch;
}
.category-scroll::-webkit-scrollbar {
  height: 4px;
}
.category-scroll::-webkit-scrollbar-track {
  background: transparent;
}
.category-scroll::-webkit-scrollbar-thumb {
  background: #242837;
  border-radius: 4px;
}
.category-card {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 36px;
  background: #161921;
  border: 1px solid #242837;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
  min-width: 110px;
}
.category-card:hover {
  border-color: rgba(0,255,136,0.3);
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.3), 0 0 20px rgba(0,255,136,0.05);
}
.category-icon {
  font-size: 36px;
  line-height: 1;
}
.category-name {
  font-size: 14px;
  font-weight: 500;
  color: #e8edf5;
  white-space: nowrap;
}

/* Features */
.features-section { background: #111318; border-top: 1px solid #242837; border-bottom: 1px solid #242837; }
.features-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.feature-card {
  padding: 32px 24px;
  background: #161921;
  border: 1px solid #242837;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s;
}
.feature-card:hover {
  border-color: rgba(0,255,136,0.2);
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.3);
}
.feature-icon { font-size: 36px; margin-bottom: 16px; }
.feature-card h3 { font-size: 18px; font-weight: 600; margin-bottom: 8px; color: #e8edf5; }
.feature-card p { font-size: 14px; color: #8892a4; line-height: 1.5; }

/* === Data Showcase === */
.stats-section {
  padding-top: 48px;
  padding-bottom: 48px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.stat-card {
  background: #161921;
  border: 1px solid #242837;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  transition: all 0.25s ease;
}
.stat-card:hover {
  border-color: rgba(0,255,136,0.2);
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.3);
}
.stat-icon {
  font-size: 36px;
  margin-bottom: 16px;
}
.stat-number {
  margin-bottom: 8px;
}
.stat-value {
  font-family: 'JetBrains Mono', monospace;
  font-size: 32px;
  font-weight: 600;
  color: #00ff88;
}
.stat-label {
  font-size: 14px;
  color: #8892a4;
}

/* === Platform Advantages === */
.advantages-section {
  background: #111318;
  border-top: 1px solid #242837;
  border-bottom: 1px solid #242837;
}
.advantages-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.advantage-card {
  background: #161921;
  border: 1px solid #242837;
  border-radius: 12px;
  padding: 36px 24px;
  text-align: center;
  transition: all 0.25s ease;
}
.advantage-card:hover {
  border-color: rgba(0,255,136,0.2);
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.3);
}
.advantage-icon-wrap {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,255,136,0.08);
  border: 1px solid rgba(0,255,136,0.15);
  border-radius: 16px;
  transition: all 0.25s ease;
}
.advantage-card:hover .advantage-icon-wrap {
  background: rgba(0,255,136,0.15);
  border-color: rgba(0,255,136,0.3);
  box-shadow: 0 0 24px rgba(0,255,136,0.1);
}
.advantage-icon {
  font-size: 28px;
  line-height: 1;
}
.advantage-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #e8edf5;
  margin-bottom: 10px;
}
.advantage-card p {
  font-size: 14px;
  color: #8892a4;
  line-height: 1.6;
}

/* Enhanced Product Cards */
.enhanced-product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }

.epc-card {
  overflow: hidden;
  padding: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}
.epc-card:hover {
  transform: translateY(-4px);
  border-color: rgba(0,255,136,0.4);
  box-shadow: 0 12px 40px rgba(0,0,0,0.4), 0 0 30px rgba(0,255,136,0.08);
}

.epc-image {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(0,255,136,0.04) 0%, rgba(0,204,255,0.04) 100%);
  border-bottom: 1px solid #242837;
  position: relative;
  overflow: hidden;
}
.epc-image::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 50%, rgba(22,25,33,0.9) 100%);
  pointer-events: none;
}
.epc-emoji {
  font-size: 52px;
  line-height: 1;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.3));
  z-index: 1;
}

.epc-body { padding: 16px 18px 18px; }

.epc-tags { display: flex; gap: 6px; margin-bottom: 10px; align-items: center; }

.epc-hot-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  background: rgba(255,51,85,0.15);
  color: #ff3355;
  border: 1px solid rgba(255,51,85,0.2);
  letter-spacing: 0.3px;
}

.epc-title {
  font-size: 16px;
  font-weight: 600;
  color: #e8edf5;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.epc-type {
  font-size: 12px;
  color: #555d6e;
  margin-bottom: 10px;
}

.epc-price {
  font-size: 22px;
  font-weight: 700;
  color: #00ff88;
  font-family: 'JetBrains Mono', monospace;
  margin-bottom: 10px;
}
.epc-unit {
  font-size: 13px;
  color: #555d6e;
  font-weight: 400;
  font-family: 'Outfit', sans-serif;
}

.epc-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid rgba(36,40,55,0.5);
}
.epc-region {
  font-size: 12px;
  color: #8892a4;
}
.epc-views {
  font-size: 12px;
  color: #555d6e;
  font-family: 'JetBrains Mono', monospace;
}

.skeleton-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.skeleton-card {
  height: 200px;
  background: #161921;
  border: 1px solid #242837;
  border-radius: 8px;
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

.footer { text-align: center; padding: 24px; color: #555d6e; font-size: 13px; border-top: 1px solid #242837; }

/* Mobile responsive overrides */
@media (max-width: 1024px) {
  .enhanced-product-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 768px) {
  .stats-grid,
  .advantages-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .enhanced-product-grid { grid-template-columns: repeat(2, 1fr); }
  .category-card {
    padding: 16px 24px;
    min-width: 90px;
  }
  .category-icon {
    font-size: 28px;
  }
  .stat-value {
    font-size: 24px;
  }
  .hero-stat {
    padding: 0 12px;
  }
  .hero-stat-val {
    font-size: 20px;
  }
  .hero-stat-divider {
    font-size: 16px;
  }
  .hero-title {
    font-size: 40px;
  }
  .hero-desc {
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .enhanced-product-grid { grid-template-columns: 1fr; }
  .hero-stat { padding: 0 8px; }
}
</style>
