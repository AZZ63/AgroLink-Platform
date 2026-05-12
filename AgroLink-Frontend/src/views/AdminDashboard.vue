<template>
  <div class="admin-page">
    <NavBar />
    <div class="admin-container">
      <h1 class="page-title">管理后台</h1>

      <!-- Tab Navigation -->
      <div class="admin-tabs">
        <button :class="['tab', { active: tab === 'users' }]" @click="tab = 'users'">用户管理</button>
        <button :class="['tab', { active: tab === 'products' }]" @click="tab = 'products'; loadProducts()">商品审核</button>
      </div>

      <!-- Users Tab -->
      <div v-if="tab === 'users'" class="admin-section">
        <div class="section-header">
          <h2>用户列表</h2>
          <span class="count">共 {{ users.length }} 人</span>
        </div>
        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>角色</th>
                <th>手机</th>
                <th>注册时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.id">
                <td>{{ u.id }}</td>
                <td>{{ u.username }}</td>
                <td>
                  <select v-model="u.role" class="role-select" @change="handleUpdateRole(u)">
                    <option value="FARMER">农户</option>
                    <option value="MERCHANT">商户</option>
                    <option value="ADMIN">管理员</option>
                  </select>
                </td>
                <td>{{ u.phone || '-' }}</td>
                <td>{{ formatTime(u.createdAt) }}</td>
                <td>
                  <button class="t-btn t-btn-warn" @click="handleDeleteUser(u)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Products Tab -->
      <div v-if="tab === 'products'" class="admin-section">
        <div class="section-header">
          <h2>商品列表</h2>
          <span class="count">共 {{ products.length }} 条</span>
        </div>
        <div class="admin-table-wrap">
          <table class="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>名称</th>
                <th>类型</th>
                <th>用户</th>
                <th>价格</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in products" :key="p.id">
                <td>{{ p.id }}</td>
                <td>{{ p.name }}</td>
                <td>{{ p.infoType === 'SUPPLY' ? '供应' : '需求' }}</td>
                <td>{{ p.userId }}</td>
                <td>¥{{ p.price }}</td>
                <td>
                  <select v-model="p.status" class="role-select" @change="handleUpdateProductStatus(p)">
                    <option value="LISTED">上架</option>
                    <option value="UNLISTED">下架</option>
                    <option value="SOLD">已售</option>
                    <option value="PENDING">待匹配</option>
                    <option value="COMPLETED">已成交</option>
                    <option value="CLOSED">关闭</option>
                  </select>
                </td>
                <td>
                  <button class="t-btn t-btn-warn" @click="handleDeleteProduct(p)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="products.length === 0 && !loadingProducts" class="empty-state">暂无商品</div>
        <div v-if="loadingProducts" class="loading">加载中...</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
import NavBar from '../components/NavBar.vue'

const tab = ref('users')
const users = ref([])
const products = ref([])
const loadingProducts = ref(false)

function formatTime(t) { return t ? t.substring(0, 16).replace('T', ' ') : '' }

// Admin API helpers
const adminApi = {
  getUsers() { return http.get('/admin/users') },
  updateRole(id, role) { return http.put(`/admin/users/${id}/role`, role, { headers: { 'Content-Type': 'text/plain' } }) },
  deleteUser(id) { return http.delete(`/admin/users/${id}`) },
  getProducts() { return http.get('/admin/product/list?page=1&size=200') },
  updateProductStatus(id, status) { return http.put(`/admin/product/${id}/status`, { status }) }
}

async function loadUsers() {
  try { users.value = await adminApi.getUsers() }
  catch (e) { ElMessage.error('加载用户失败') }
}

async function loadProducts() {
  loadingProducts.value = true
  try {
    const res = await adminApi.getProducts()
    products.value = res.records || []
  } catch (e) { ElMessage.error('加载商品失败') }
  finally { loadingProducts.value = false }
}

async function handleUpdateRole(u) {
  try {
    await adminApi.updateRole(u.id, u.role)
    ElMessage.success('角色已更新')
  } catch (e) { ElMessage.error(e.message || '更新失败') }
}

async function handleDeleteUser(u) {
  try {
    await adminApi.deleteUser(u.id)
    ElMessage.success('用户已删除')
    users.value = users.value.filter(x => x.id !== u.id)
  } catch (e) { ElMessage.error(e.message || '删除失败') }
}

async function handleUpdateProductStatus(p) {
  try {
    await adminApi.updateProductStatus(p.id, p.status)
    ElMessage.success('状态已更新')
  } catch (e) { ElMessage.error(e.message || '更新失败') }
}

async function handleDeleteProduct(p) {
  try { /* not implemented on admin side, skip for now */ }
  catch (e) { /* ignore */ }
}

onMounted(() => { loadUsers() })
</script>

<style scoped>
.admin-container { max-width: 1100px; margin: 0 auto; padding: 32px; }
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 36px; margin-bottom: 24px; }

.admin-tabs { display: flex; gap: 0; margin-bottom: 24px; border-bottom: 1px solid #242837; }
.tab {
  padding: 12px 24px; background: transparent; border: none; border-bottom: 2px solid transparent;
  color: #8892a4; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; transition: all 0.2s;
}
.tab:hover { color: #e8edf5; }
.tab.active { color: #00ff88; border-bottom-color: #00ff88; }

.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-header h2 { font-size: 18px; }
.count { font-size: 13px; color: #555d6e; font-family: 'JetBrains Mono', monospace; }

.admin-table-wrap { overflow-x: auto; }
.admin-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.admin-table th {
  text-align: left; padding: 12px 16px; background: #1e2230;
  color: #555d6e; font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px;
  border-bottom: 1px solid #242837;
}
.admin-table td { padding: 12px 16px; border-bottom: 1px solid #1e2230; color: #e8edf5; }
.admin-table tr:hover td { background: rgba(0,255,136,0.02); }

.role-select {
  padding: 4px 8px; background: #1e2230; border: 1px solid #242837;
  border-radius: 4px; color: #e8edf5; font-size: 12px; font-family: 'Outfit', sans-serif; outline: none;
}
.role-select:focus { border-color: #00ff88; }

.t-btn { padding: 4px 12px; border: 1px solid #242837; border-radius: 4px; font-size: 12px; font-family: 'Outfit', sans-serif; cursor: pointer; transition: all 0.15s; background: transparent; color: #8892a4; }
.t-btn:hover { border-color: #00ff88; color: #00ff88; }
.t-btn-warn { border-color: #ff4466; color: #ff4466; }
.t-btn-warn:hover { background: rgba(255,68,102,0.1); }

.loading { text-align: center; padding: 40px; color: #555d6e; }
.empty-state { text-align: center; padding: 40px; color: #555d6e; }
</style>
