<template>
  <div class="admin-page">
    <div class="page-heading">
      <h1 class="page-title">商品审核</h1>
      <div class="header-tabs">
        <button :class="['tab',{active:tab==='all'}]" @click="tab='all'">全部 ({{ count.all }})</button>
        <button :class="['tab',{active:tab==='LISTED'}]" @click="tab='LISTED'">已上架 ({{ count.listed }})</button>
        <button :class="['tab',{active:tab==='PENDING'}]" @click="tab='PENDING'">待审核 ({{ count.pending }})</button>
        <button :class="['tab',{active:tab==='UNLISTED'}]" @click="tab='UNLISTED'">已下架 ({{ count.unlisted }})</button>
      </div>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <div class="search-box"><input v-model="keyword" placeholder="搜索商品名称..." @keyup.enter="load" /></div>
        <select v-model="filterType" class="form-select"><option value="">全部分类</option><option v-for="t in types" :key="t" :value="t">{{ t }}</option></select>
      </div>
      <div class="table-wrap">
        <table class="dash-table">
          <thead><tr><th>ID</th><th>名称</th><th>类型</th><th>分类</th><th>发布者</th><th>价格</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="p in filtered" :key="p.id">
              <td class="mono">{{ p.id }}</td>
              <td><span class="product-name">{{ p.name }}</span></td>
              <td><span :class="['badge', p.infoType==='SUPPLY'?'supply':'demand']">{{ p.infoType==='SUPPLY'?'供应':'需求' }}</span></td>
              <td>{{ p.type }}</td>
              <td>{{ p.username || p.userId }}</td>
              <td class="mono">¥{{ p.price }}</td>
              <td><span :class="['badge',p.status?.toLowerCase()]">{{ statusLabel(p.status) }}</span></td>
              <td class="actions">
                <button class="btn btn-xs btn-outline" @click="$router.push(`/products/${p.id}`)">查看</button>
                <button v-if="p.status==='LISTED'" class="btn btn-xs btn-warn" @click="toggleStatus(p,'UNLISTED')">下架</button>
                <button v-if="p.status==='UNLISTED'" class="btn btn-xs btn-primary" @click="toggleStatus(p,'LISTED')">上架</button>
              </td>
            </tr>
            <tr v-if="filtered.length===0"><td colspan="8" class="empty-cell">暂无数据</td></tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="page-info">共 {{ total }} 条</span>
        <div class="page-btns">
          <button :disabled="page<=1" @click="page--;load()" class="page-btn">上一页</button>
          <span class="page-num">{{ page }}/{{ maxPage }}</span>
          <button :disabled="page>=maxPage" @click="page++;load()" class="page-btn">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { productApi } from '../../api/http'
import { statusLabel } from '../../utils/constants'

const keyword = ref('')
const filterType = ref('')
const tab = ref('all')
const page = ref(1)
const total = ref(0)
const maxPage = ref(1)
const products = ref([])
const types = ref([])

const count = ref({ all: 0, listed: 0, pending: 0, unlisted: 0 })

const filtered = computed(() => products.value.filter(p => {
  if (tab.value !== 'all' && p.status !== tab.value) return false
  if (keyword.value && !p.name?.includes(keyword.value)) return false
  if (filterType.value && p.type !== filterType.value) return false
  return true
}))

async function load() {
  try {
    const res = await productApi.query({ page: page.value, size: 20 })
    const list = res.records || res || []
    products.value = list
    total.value = res.total || list.length
    maxPage.value = Math.max(1, Math.ceil(total.value / 20))
    count.value = { all: total.value, listed: list.filter(x => x.status === 'LISTED').length, pending: list.filter(x => x.status === 'PENDING' || x.status === 'UNLISTED').length, unlisted: list.filter(x => x.status === 'UNLISTED').length }
  } catch { ElMessage.error('加载失败') }
}

async function toggleStatus(p, s) {
  try { await productApi.updateStatus(p.id, s); p.status = s; ElMessage.success(s === 'LISTED' ? '已上架' : '已下架') }
  catch (e) { ElMessage.error(e.message) }
}

onMounted(async () => { load(); try { types.value = await productApi.getTypes() } catch {} })
</script>

<style scoped>
.admin-page { max-width: 1200px; }
.page-heading { margin-bottom: 20px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 12px; }
.header-tabs { display: flex; gap: 4px; background: #f5f7f2; border-radius: 10px; padding: 3px; width: fit-content; }
.tab { padding: 7px 16px; border: none; border-radius: 8px; font-size: 12px; font-weight: 500; color: #6b7280; cursor: pointer; font-family: 'Inter', sans-serif; background: transparent; }
.tab.active { background: #fff; color: #1e1e1e; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }

.table-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; overflow: hidden; }
.table-toolbar { display: flex; gap: 12px; padding: 16px 20px; border-bottom: 1px solid #f0f0eb; flex-wrap: wrap; }
.search-box { flex: 1; min-width: 200px; }
.search-box input { width: 100%; padding: 8px 14px; border: 1px solid #e8ebe4; border-radius: 8px; font-size: 13px; outline: none; font-family: 'Inter', sans-serif; background: #f9faf8; }
.search-box input:focus { border-color: #7CCF5F; }
.form-select { padding: 8px 12px; border: 1px solid #e8ebe4; border-radius: 8px; font-size: 13px; background: #f9faf8; outline: none; font-family: 'Inter', sans-serif; }

.table-wrap { overflow-x: auto; }
.dash-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.dash-table th { text-align: left; padding: 12px 20px; background: #f9faf8; color: #6b7280; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e8ebe4; }
.dash-table td { padding: 14px 20px; border-bottom: 1px solid #f0f0eb; color: #374151; }
.dash-table tr:hover td { background: #f9faf8; }
.mono { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #6b7280; }
.product-name { font-weight: 500; color: #1e1e1e; }

.badge { display: inline-flex; padding: 3px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.badge.listed { background: #e8f5e4; color: #7CCF5F; }
.badge.unlisted { background: #fee2e2; color: #ef4444; }
.badge.pending { background: #fef3c7; color: #f59e0b; }
.badge.supply { background: #e8f5e4; color: #7CCF5F; }
.badge.demand { background: #fef3c7; color: #f59e0b; }
.badge.sold { background: #f0f0eb; color: #9ca3af; }
.badge.completed { background: #e8f5e4; color: #7CCF5F; }

.actions { white-space: nowrap; }
.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 6px; transition: all 0.15s; display: inline-flex; align-items: center; gap: 4px; margin-right: 4px; }
.btn-xs { padding: 4px 10px; font-size: 11px; }
.btn-outline { border: 1px solid #e8ebe4; background: transparent; color: #6b7280; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }
.btn-primary { border: none; background: #7CCF5F; color: #fff; }
.btn-primary:hover { background: #6bc04e; }
.btn-warn { border: none; background: #ef4444; color: #fff; }
.btn-warn:hover { background: #dc2626; }
.empty-cell { text-align: center; color: #9ca3af; padding: 40px !important; }

.pagination { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; }
.page-info { font-size: 12px; color: #6b7280; }
.page-btns { display: flex; align-items: center; gap: 8px; }
.page-btn { padding: 6px 14px; border: 1px solid #e8ebe4; border-radius: 6px; background: #fff; color: #374151; font-size: 12px; cursor: pointer; }
.page-btn:hover:not(:disabled) { border-color: #7CCF5F; color: #7CCF5F; }
.page-btn:disabled { opacity: 0.4; cursor: default; }
.page-num { font-size: 12px; color: #6b7280; font-family: 'JetBrains Mono', monospace; }
</style>
