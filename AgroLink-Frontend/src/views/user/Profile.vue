<template>
  <div class="profile-page">
    <div class="page-heading">
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">管理你的账户信息</p>
    </div>

    <div class="profile-layout">
      <!-- 左侧：用户信息卡 -->
      <div class="profile-sidebar-card">
        <div class="psc-avatar">{{ userStore.user?.username?.charAt(0).toUpperCase() || 'U' }}</div>
        <h2 class="psc-name">{{ userStore.user?.username || '用户' }}</h2>
        <span class="psc-role">{{ roleLabel }}</span>
        <div class="psc-divider"></div>
        <div class="psc-stats">
          <div class="psc-stat"><span class="psc-stat-val">{{ productCount }}</span><span class="psc-stat-lbl">发布</span></div>
          <div class="psc-stat"><span class="psc-stat-val">{{ orderCount }}</span><span class="psc-stat-lbl">订单</span></div>
          <div class="psc-stat"><span class="psc-stat-val">{{ favCount }}</span><span class="psc-stat-lbl">收藏</span></div>
        </div>
      </div>

      <!-- 右侧：信息编辑 -->
      <div class="profile-detail-card">
        <h3 class="pd-title">账户信息</h3>
        <div class="pd-grid">
          <div class="pd-item">
            <span class="pd-label">用户名</span>
            <span class="pd-value">{{ userStore.user?.username }}</span>
          </div>
          <div class="pd-item">
            <span class="pd-label">手机号</span>
            <span class="pd-value">{{ userInfo?.phone || '未设置' }}</span>
          </div>
          <div class="pd-item">
            <span class="pd-label">角色</span>
            <span class="pd-value">{{ roleLabel }}</span>
          </div>
          <div class="pd-item">
            <span class="pd-label">用户 ID</span>
            <span class="pd-value pd-mono">{{ userStore.user?.userId }}</span>
          </div>
        </div>

        <div class="pd-divider"></div>

        <div class="pd-links">
          <button class="pd-link" @click="$router.push('/addresses')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            收货地址管理
          </button>
          <button class="pd-link" @click="$router.push('/orders')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            我的订单
          </button>
          <button class="pd-link pd-logout" @click="handleLogout">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            退出登录
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { productApi, orderApi, favoriteApi } from '../../api/http'

const router = useRouter()
const userStore = useUserStore()
const userInfo = ref(null)
const productCount = ref(0)
const orderCount = ref(0)
const favCount = ref(0)

const roleLabel = computed(() => {
  if (userStore.isAdmin) return '管理员'
  if (userStore.isFarmer) return '农户'
  if (userStore.isMerchant) return '商户'
  return ''
})

function handleLogout() { userStore.logout(); router.push('/') }

onMounted(async () => {
  try { const p = await productApi.getMy(); productCount.value = p.length } catch {}
  try { const o = userStore.isFarmer ? await orderApi.getSell() : await orderApi.getBuy(); orderCount.value = o.length } catch {}
  try { const f = await favoriteApi.list(); favCount.value = f.length } catch {}
})
</script>

<style scoped>
.profile-page { max-width: 900px; }
.page-heading { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.profile-layout { display: grid; grid-template-columns: 280px 1fr; gap: 24px; }

/* 左侧卡片 */
.profile-sidebar-card {
  background: #ffffff; border: 1px solid #e8ebe4; border-radius: 20px;
  padding: 32px 24px; text-align: center;
}
.psc-avatar {
  width: 72px; height: 72px; border-radius: 20px; background: #7CCF5F; color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 700; margin: 0 auto 16px;
}
.psc-name { font-size: 18px; font-weight: 700; color: #1e1e1e; margin: 0 0 4px; }
.psc-role { display: inline-block; padding: 3px 12px; background: #e8f5e4; color: #7CCF5F; border-radius: 6px; font-size: 12px; font-weight: 600; }
.psc-divider { height: 1px; background: #f0f0eb; margin: 20px 0; }
.psc-stats { display: flex; justify-content: space-around; }
.psc-stat { text-align: center; }
.psc-stat-val { display: block; font-size: 20px; font-weight: 700; color: #1e1e1e; font-family: 'JetBrains Mono', monospace; }
.psc-stat-lbl { font-size: 11px; color: #9ca3af; }

/* 右侧卡片 */
.profile-detail-card { background: #ffffff; border: 1px solid #e8ebe4; border-radius: 20px; padding: 28px; }
.pd-title { font-size: 16px; font-weight: 600; color: #1e1e1e; margin: 0 0 20px; }
.pd-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.pd-item { display: flex; flex-direction: column; gap: 4px; }
.pd-label { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; }
.pd-value { font-size: 15px; color: #1e1e1e; font-weight: 500; }
.pd-mono { font-family: 'JetBrains Mono', monospace; font-size: 13px; color: #6b7280; }

.pd-divider { height: 1px; background: #f0f0eb; margin: 24px 0; }
.pd-links { display: flex; flex-direction: column; gap: 8px; }
.pd-link {
  display: flex; align-items: center; gap: 10px; width: 100%; text-align: left;
  padding: 12px 14px; border: 1px solid transparent; border-radius: 10px;
  background: transparent; color: #374151; font-size: 14px; cursor: pointer; font-family: 'Inter', sans-serif;
  transition: all 0.15s;
}
.pd-link:hover { background: #f5f7f2; border-color: #e8ebe4; }
.pd-logout { color: #ef4444; }
.pd-logout:hover { background: #fee2e2; border-color: #fecaca; }

@media (max-width: 768px) { .profile-layout { grid-template-columns: 1fr; } }
</style>
