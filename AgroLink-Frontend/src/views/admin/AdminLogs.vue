<template>
  <div class="admin-page">
    <div class="page-heading">
      <h1 class="page-title">日志审计</h1>
      <p class="page-subtitle">平台操作日志与安全审计</p>
    </div>

    <div class="section-tabs">
      <button :class="['st',{active:section==='login'}]" @click="section='login'">登录日志</button>
      <button :class="['st',{active:section==='operate'}]" @click="section='operate'">操作日志</button>
      <button :class="['st',{active:section==='audit'}]" @click="section='audit'">审核日志</button>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <div class="search-box"><input v-model="keyword" placeholder="搜索用户名..." /></div>
        <div class="toolbar-right">
          <span class="log-count">共 {{ filtered.length }} 条</span>
          <button class="btn btn-outline btn-sm" @click="handleExport">导出</button>
        </div>
      </div>
      <div class="table-wrap">
        <table class="dash-table">
          <thead><tr><th>用户</th><th>操作</th><th>详情</th><th>IP</th><th>时间</th></tr></thead>
          <tbody>
            <tr v-for="(l,i) in filtered" :key="i">
              <td><span class="user-tag">{{ l.user }}</span></td>
              <td><span :class="['badge', l.type]">{{ l.action }}</span></td>
              <td class="detail-cell">{{ l.detail }}</td>
              <td class="mono">{{ l.ip }}</td>
              <td class="mono time">{{ l.time }}</td>
            </tr>
            <tr v-if="filtered.length===0"><td colspan="5" class="empty-cell">暂无日志</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const section = ref('login')
const keyword = ref('')

const logs = ref({
  login: [
    { user: 'admin', action: '登录成功', type: 'success', detail: '管理员后台登录', ip: '192.168.1.1', time: '2026-05-12 14:32' },
    { user: 'farmer2', action: '登录成功', type: 'success', detail: 'Web端登录', ip: '10.0.0.5', time: '2026-05-12 14:15' },
    { user: 'admin', action: '登录失败', type: 'danger', detail: '密码错误', ip: '192.168.1.2', time: '2026-05-12 13:50' },
    { user: 'merchant2', action: '登录成功', type: 'success', detail: 'Web端登录', ip: '10.0.0.8', time: '2026-05-12 11:20' },
    { user: 'farmer_88', action: '登录成功', type: 'success', detail: '首次登录', ip: '172.16.0.3', time: '2026-05-12 10:05' },
  ],
  operate: [
    { user: 'admin', action: '删除用户', type: 'warning', detail: '删除用户 farmer_old (ID:15)', ip: '192.168.1.1', time: '2026-05-12 14:30' },
    { user: 'admin', action: '修改角色', type: 'info', detail: '用户 merchant3 角色 MERCHANT→FARMER', ip: '192.168.1.1', time: '2026-05-12 14:00' },
    { user: 'farmer2', action: '下架商品', type: 'info', detail: '商品 "有机大米" (ID:23)', ip: '10.0.0.5', time: '2026-05-12 13:45' },
  ],
  audit: [
    { user: 'admin', action: '审核通过', type: 'success', detail: '商品 "红富士苹果" 审核通过', ip: '192.168.1.1', time: '2026-05-12 14:00' },
    { user: 'admin', action: '审核驳回', type: 'danger', detail: '商品 "散装大米" 信息不完整', ip: '192.168.1.1', time: '2026-05-12 13:30' },
  ]
})

const currentLogs = computed(() => logs.value[section.value] || [])
const filtered = computed(() => currentLogs.value.filter(l => !keyword.value || l.user.includes(keyword.value)))

function handleExport() { ElMessage.success('日志导出中...') }
</script>

<style scoped>
.admin-page { max-width: 1000px; }
.page-heading { margin-bottom: 16px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 4px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }
.section-tabs { display: flex; gap: 4px; margin-bottom: 20px; background: #f5f7f2; border-radius: 10px; padding: 3px; width: fit-content; }
.st { padding: 8px 18px; border: none; border-radius: 8px; font-size: 13px; font-weight: 500; color: #6b7280; cursor: pointer; font-family: 'Inter', sans-serif; background: transparent; }
.st.active { background: #fff; color: #1e1e1e; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

.table-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; overflow: hidden; }
.table-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #f0f0eb; flex-wrap: wrap; gap: 12px; }
.search-box input { width: 240px; max-width: 100%; padding: 8px 14px; border: 1px solid #e8ebe4; border-radius: 8px; font-size: 13px; outline: none; font-family: 'Inter', sans-serif; background: #f9faf8; }
.search-box input:focus { border-color: #7CCF5F; }
.toolbar-right { display: flex; align-items: center; gap: 12px; }
.log-count { font-size: 12px; color: #6b7280; }

.table-wrap { overflow-x: auto; }
.dash-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.dash-table th { text-align: left; padding: 12px 20px; background: #f9faf8; color: #6b7280; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e8ebe4; }
.dash-table td { padding: 12px 20px; border-bottom: 1px solid #f0f0eb; color: #374151; }
.user-tag { font-weight: 500; color: #1e1e1e; }
.detail-cell { color: #6b7280; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mono { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #6b7280; }
.time { font-size: 11px; }

.badge { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.badge.success { background: #e8f5e4; color: #7CCF5F; }
.badge.info { background: #dbeafe; color: #3b82f6; }
.badge.warning { background: #fef3c7; color: #f59e0b; }
.badge.danger { background: #fee2e2; color: #ef4444; }

.empty-cell { text-align: center; color: #9ca3af; padding: 40px !important; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 8px; transition: all 0.15s; }
.btn-sm { padding: 7px 14px; font-size: 12px; }
.btn-outline { border: 1px solid #e8ebe4; background: transparent; color: #374151; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }
</style>
