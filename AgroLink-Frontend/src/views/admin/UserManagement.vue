<template>
  <div class="admin-page">
    <div class="page-heading">
      <h1 class="page-title">用户管理</h1>
      <p class="page-subtitle">管理平台用户账号</p>
    </div>

    <div class="data-card">
      <div class="toolbar">
        <div class="search-box"><input v-model="keyword" placeholder="搜索用户名 / 手机号..." @keyup.enter="handleSearch" />
          <button class="btn btn-sm btn-primary" @click="handleSearch">搜索</button>
          <button v-if="keyword" class="btn btn-sm btn-ghost" @click="keyword=''; handleSearch()">清空</button>
        </div>
      </div>
      <div class="table-wrap">
        <table class="dash-table">
          <thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>手机号</th><th>状态</th><th>注册时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="u in users" :key="u.id">
              <td class="mono">{{ u.id }}</td>
              <td><span class="user-name">{{ u.username }}</span></td>
              <td>
                <select v-model="u.role" class="form-select form-select-sm" @change="handleUpdateRole(u)">
                  <option value="FARMER">农户</option>
                  <option value="MERCHANT">商户</option>
                  <option value="ADMIN">管理员</option>
                </select>
              </td>
              <td>{{ u.phone || '-' }}</td>
              <td>
                <span v-if="u.status === 1 || u.status === undefined" class="badge success">启用</span>
                <span v-else class="badge danger">禁用</span>
              </td>
              <td class="mono time">{{ formatTime(u.createdAt) }}</td>
              <td class="actions">
                <button :class="['btn btn-xs', u.status === 1 || u.status === undefined ? 'btn-warn' : 'btn-primary']" @click="handleToggleStatus(u)">
                  {{ u.status === 1 || u.status === undefined ? '禁用' : '启用' }}
                </button>
                <button class="btn btn-xs btn-outline btn-danger-text" @click="handleDeleteUser(u)">删除</button>
              </td>
            </tr>
            <tr v-if="users.length === 0"><td colspan="7" class="empty-cell">暂无数据</td></tr>
          </tbody>
        </table>
      </div>
      <div v-if="total > 0" class="pagination">
        <span class="page-info">共 {{ total }} 条，第 {{ currentPage }}/{{ totalPages }} 页</span>
        <div class="page-btns">
          <button :disabled="currentPage <= 1" @click="goPage(currentPage - 1)" class="page-btn">上一页</button>
          <span class="page-num">{{ currentPage }}</span>
          <button :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)" class="page-btn">下一页</button>
        </div>
        <select v-model="pageSize" class="form-select form-select-sm" @change="loadUsers">
          <option :value="10">10条/页</option>
          <option :value="20">20条/页</option>
          <option :value="50">50条/页</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api/http'

const users = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

function formatTime(t) { return t ? t.substring(0, 16).replace('T', ' ') : '' }

async function loadUsers() {
  try {
    const res = await adminApi.getUsers(currentPage.value, pageSize.value, keyword.value)
    users.value = res.records || []
    total.value = res.total || 0
  } catch { ElMessage.error('加载用户失败') }
}

function handleSearch() { currentPage.value = 1; loadUsers() }
function goPage(p) { if (p < 1 || p > totalPages.value) return; currentPage.value = p; loadUsers() }

async function handleUpdateRole(u) {
  try { await adminApi.updateRole(u.id, u.role); ElMessage.success('角色已更新') }
  catch (e) { ElMessage.error(e.message || '更新失败') }
}

async function handleToggleStatus(u) {
  const ns = (u.status === 1 || u.status === undefined) ? 0 : 1
  try { await adminApi.updateStatus(u.id, ns); u.status = ns; ElMessage.success(ns === 1 ? '已启用' : '已禁用') }
  catch (e) { ElMessage.error(e.message || '操作失败') }
}

async function handleDeleteUser(u) {
  try {
    await ElMessageBox.confirm(`确定要删除「${u.username}」吗？`, '确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await adminApi.deleteUser(u.id); ElMessage.success('已删除'); loadUsers()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message || '删除失败') }
}

onMounted(loadUsers)
</script>

<style scoped>
.admin-page { max-width: 1100px; }
.page-heading { margin-bottom: 20px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 4px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.data-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; overflow: hidden; }
.toolbar { padding: 16px 20px; border-bottom: 1px solid #f0f0eb; }
.search-box { display: flex; gap: 8px; align-items: center; }
.search-box input { flex: 1; max-width: 280px; padding: 8px 14px; border: 1px solid #e8ebe4; border-radius: 8px; font-size: 13px; outline: none; font-family: 'Inter', sans-serif; background: #f9faf8; }
.search-box input:focus { border-color: #7CCF5F; }

.table-wrap { overflow-x: auto; }
.dash-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.dash-table th { text-align: left; padding: 12px 20px; background: #f9faf8; color: #6b7280; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e8ebe4; }
.dash-table td { padding: 12px 20px; border-bottom: 1px solid #f0f0eb; color: #374151; }
.dash-table tr:hover td { background: #f9faf8; }
.mono { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #6b7280; }
.time { font-size: 11px; }
.user-name { font-weight: 500; color: #1e1e1e; }

.badge { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.badge.success { background: #e8f5e4; color: #7CCF5F; }
.badge.danger { background: #fee2e2; color: #ef4444; }
.badge.default { background: #f0f0eb; color: #9ca3af; }

.form-select { padding: 7px 12px; border: 1px solid #e8ebe4; border-radius: 8px; font-size: 13px; background: #f9faf8; outline: none; font-family: 'Inter', sans-serif; color: #374151; }
.form-select-sm { padding: 4px 10px; font-size: 12px; }
.form-select:focus { border-color: #7CCF5F; }

.actions { white-space: nowrap; display: flex; gap: 4px; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 6px; transition: all 0.15s; display: inline-flex; align-items: center; justify-content: center; }
.btn-sm { padding: 7px 16px; font-size: 12px; }
.btn-xs { padding: 4px 10px; font-size: 11px; }
.btn-primary { background: #7CCF5F; color: #fff; border: none; }
.btn-primary:hover { background: #6bc04e; }
.btn-warn { background: #ef4444; color: #fff; border: none; }
.btn-warn:hover { background: #dc2626; }
.btn-outline { border: 1px solid #e8ebe4; background: transparent; color: #6b7280; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }
.btn-danger-text:hover { border-color: #ef4444 !important; color: #ef4444 !important; }
.btn-ghost { border: 1px solid #e8ebe4; background: transparent; color: #6b7280; }
.btn-ghost:hover { background: #f5f7f2; }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; flex-wrap: wrap; gap: 12px; }
.page-info { font-size: 12px; color: #6b7280; }
.page-btns { display: flex; align-items: center; gap: 8px; }
.page-btn { padding: 6px 14px; border: 1px solid #e8ebe4; border-radius: 6px; background: #fff; color: #374151; font-size: 12px; cursor: pointer; font-family: 'Inter', sans-serif; }
.page-btn:hover:not(:disabled) { border-color: #7CCF5F; color: #7CCF5F; }
.page-btn:disabled { opacity: 0.4; cursor: default; }
.page-num { font-size: 13px; font-weight: 600; color: #1e1e1e; font-family: 'JetBrains Mono', monospace; padding: 0 8px; }

.empty-cell { text-align: center; color: #9ca3af; padding: 40px !important; }
</style>
