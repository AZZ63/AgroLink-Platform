<template>
  <div class="profile-page">
    <NavBar />

    <div class="profile-container">
      <div class="profile-grid">
        <!-- User card -->
        <aside class="profile-sidebar">
          <div class="user-card">
            <div class="user-avatar">{{ userStore.user?.username?.[0] }}</div>
            <h2 class="user-name">{{ userStore.user?.username }}</h2>
            <span class="user-role">{{ userStore.isFarmer ? '农户' : '商户' }}</span>
          </div>
        </aside>

        <!-- Main -->
        <main class="profile-main">
          <div class="profile-toolbar">
            <h2 class="toolbar-title">我的发布</h2>
            <button class="tech-btn tech-btn-primary" @click="$router.push('/publish?type=' + (userStore.isFarmer ? 'SUPPLY' : 'DEMAND'))">+ 发布新信息</button>
          </div>

          <div class="products-table">
            <div class="table-header">
              <span class="th" style="flex:2">产品</span>
              <span class="th" style="flex:1">类型</span>
              <span class="th" style="flex:1">供需</span>
              <span class="th" style="flex:1">状态</span>
              <span class="th" style="flex:1">价格</span>
              <span class="th" style="flex:2">发布时间</span>
              <span class="th" style="flex:2">操作</span>
            </div>
            <div v-for="row in myProducts" :key="row.id" class="table-row">
              <span class="td" style="flex:2; font-weight:600">{{ row.name }}</span>
              <span class="td" style="flex:1">{{ row.type }}</span>
              <span class="td" style="flex:1">
                <span class="tech-badge" :class="row.infoType === 'SUPPLY' ? 'tech-badge-supply' : 'tech-badge-demand'">
                  {{ row.infoType === 'SUPPLY' ? '供应' : '需求' }}
                </span>
              </span>
              <span class="td" style="flex:1">
                <span class="tech-status" :class="'tech-status-' + (row.status || '').toLowerCase()">
                  {{ statusLabel(row.status) }}
                </span>
              </span>
              <span class="td" style="flex:1">¥{{ row.price }}/{{ row.unit }}</span>
              <span class="td" style="flex:2; font-size:12px; color:#555d6e">{{ formatTime(row.createdAt) }}</span>
              <span class="td" style="flex:2; display:flex; gap:8px; flex-wrap:wrap">
                <!-- Supply actions -->
                <button v-if="row.infoType === 'SUPPLY' && row.status === 'LISTED'" class="t-btn t-btn-success" @click="handleStatusChange(row, 'SOLD')">已售</button>
                <button v-if="row.infoType === 'SUPPLY' && row.status === 'LISTED'" class="t-btn t-btn-warn" @click="handleStatusChange(row, 'UNLISTED')">下架</button>
                <button v-if="row.infoType === 'SUPPLY' && row.status === 'UNLISTED'" class="t-btn t-btn-primary" @click="handleStatusChange(row, 'LISTED')">上架</button>
                <!-- Demand actions -->
                <button v-if="row.infoType === 'DEMAND' && row.status === 'PENDING'" class="t-btn t-btn-success" @click="handleStatusChange(row, 'COMPLETED')">已成交</button>
                <button v-if="row.infoType === 'DEMAND' && row.status === 'PENDING'" class="t-btn t-btn-warn" @click="handleStatusChange(row, 'CLOSED')">关闭</button>
                <button v-if="row.infoType === 'DEMAND' && row.status === 'CLOSED'" class="t-btn t-btn-primary" @click="handleStatusChange(row, 'PENDING')">重新打开</button>
                <button class="t-btn t-btn-danger" @click="handleDelete(row)">删除</button>
              </span>
            </div>
            <div v-if="!myProducts.length" class="table-empty">暂无发布记录</div>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { productApi } from '../api/http'
import NavBar from '../components/NavBar.vue'
import { statusLabel } from '../utils/constants'

const userStore = useUserStore()
const myProducts = ref([])
const loading = ref(false)

onMounted(() => {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  loadMyProducts()
})

async function loadMyProducts() {
  loading.value = true
  try { myProducts.value = await productApi.getMy() }
  catch (e) { /* ignore */ }
  finally { loading.value = false }
}

async function handleStatusChange(row, newStatus) {
  try {
    await productApi.updateStatus(row.id, newStatus)
    ElMessage.success('状态已更新')
    loadMyProducts()
  } catch (e) { ElMessage.error(e.message || '状态更新失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该产品信息？', '确认')
    await productApi.delete(row.id)
    ElMessage.success('删除成功')
    loadMyProducts()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '删除失败') }
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 16).replace('T', ' ')
}
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
.tech-btn {
  padding: 10px 24px;
  font-family: 'Bebas Neue', sans-serif;
  font-size: 15px;
  letter-spacing: 1.5px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.tech-btn-primary { background: #00ff88; color: #000; }
.tech-btn-primary:hover { background: #00cc6a; box-shadow: 0 0 20px rgba(0,255,136,0.2); }

.profile-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px;
}
.profile-grid { display: flex; gap: 32px; align-items: start; }

/* Sidebar */
.profile-sidebar { width: 240px; flex-shrink: 0; }
.user-card {
  background: #161921;
  border: 1px solid #242837;
  border-radius: 12px;
  padding: 32px;
  text-align: center;
}
.user-avatar {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  background: rgba(0,255,136,0.1);
  border: 2px solid rgba(0,255,136,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Bebas Neue', sans-serif;
  font-size: 32px;
  color: #00ff88;
}
.user-name { font-size: 20px; font-weight: 600; margin-bottom: 4px; }
.user-role {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(0,255,136,0.1);
  border: 1px solid rgba(0,255,136,0.15);
  border-radius: 4px;
  font-size: 12px;
  color: #00ff88;
}

/* Main */
.profile-main { flex: 1; min-width: 0; }
.profile-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.toolbar-title { font-size: 20px; font-weight: 600; }

/* Table */
.products-table {
  background: #161921;
  border: 1px solid #242837;
  border-radius: 8px;
  overflow: hidden;
}
.table-header {
  display: flex;
  padding: 12px 16px;
  background: #1e2230;
  border-bottom: 1px solid #242837;
}
.th {
  font-size: 11px;
  font-weight: 600;
  color: #555d6e;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.table-row {
  display: flex;
  padding: 14px 16px;
  align-items: center;
  border-bottom: 1px solid #1e2230;
  transition: background 0.15s;
}
.table-row:hover { background: rgba(255,255,255,0.02); }
.table-row:last-child { border-bottom: none; }
.td { font-size: 14px; color: #e8edf5; }

/* Action buttons */
.t-btn {
  padding: 4px 12px;
  border: 1px solid #242837;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'Outfit', sans-serif;
  cursor: pointer;
  transition: all 0.15s;
  background: transparent;
}
.t-btn-primary { border-color: #00ff88; color: #00ff88; }
.t-btn-primary:hover { background: rgba(0,255,136,0.1); }
.t-btn-success { border-color: #00ff88; color: #00ff88; }
.t-btn-success:hover { background: rgba(0,255,136,0.1); }
.t-btn-warn { border-color: #ff8800; color: #ff8800; }
.t-btn-warn:hover { background: rgba(255,136,0,0.1); }
.t-btn-danger { border-color: #ff3355; color: #ff3355; }
.t-btn-danger:hover { background: rgba(255,51,85,0.1); }

.table-empty { text-align: center; padding: 40px; color: #555d6e; font-size: 14px; }
</style>
