<template>
  <div class="admin-page">
    <div class="page-heading">
      <h1 class="page-title">分类与地区管理</h1>
      <p class="page-subtitle">管理农产品分类和产地信息</p>
    </div>

    <div class="split-layout">
      <!-- 左侧：分类管理 -->
      <div class="table-card">
        <div class="table-toolbar">
          <span class="tb-title">农产品分类</span>
          <button class="btn btn-primary btn-sm" @click="showCatModal=true;catForm={name:''}">+ 新增</button>
        </div>
        <div class="table-wrap">
          <table class="dash-table">
            <thead><tr><th>分类名称</th><th>商品数</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="c in categories" :key="c.id">
                <td><span class="cat-tag">{{ c.name }}</span></td>
                <td class="mono">{{ c.count }}</td>
                <td>
                  <button class="btn btn-xs btn-ghost" @click="catForm={name:c.name};editCatId=c.id;showCatModal=true">编辑</button>
                  <button class="btn btn-xs btn-ghost btn-danger" @click="handleDeleteCat(c)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 右侧：地区管理 -->
      <div class="table-card">
        <div class="table-toolbar">
          <span class="tb-title">产地地区</span>
          <button class="btn btn-primary btn-sm" @click="showRegionModal=true;regionForm={name:''}">+ 新增</button>
        </div>
        <div class="table-wrap">
          <table class="dash-table">
            <thead><tr><th>地区</th><th>商品数</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="r in regions" :key="r.id">
                <td><span class="region-tag">{{ r.name }}</span></td>
                <td class="mono">{{ r.count }}</td>
                <td>
                  <button class="btn btn-xs btn-ghost" @click="regionForm={name:r.name};editRegionId=r.id;showRegionModal=true">编辑</button>
                  <button class="btn btn-xs btn-ghost btn-danger" @click="handleDeleteRegion(r)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Category Modal -->
    <div v-if="showCatModal" class="modal-overlay" @click.self="showCatModal=false">
      <div class="modal-card">
        <h3 class="modal-title">{{ editCatId ? '编辑' : '新增' }}分类</h3>
        <div class="modal-body">
          <div class="form-group"><label>分类名称</label><input v-model="catForm.name" class="form-input" placeholder="如：叶菜类" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="showCatModal=false">取消</button>
          <button class="btn btn-primary" @click="handleSaveCat">{{ editCatId ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>

    <!-- Region Modal -->
    <div v-if="showRegionModal" class="modal-overlay" @click.self="showRegionModal=false">
      <div class="modal-card">
        <h3 class="modal-title">{{ editRegionId ? '编辑' : '新增' }}地区</h3>
        <div class="modal-body">
          <div class="form-group"><label>地区名称</label><input v-model="regionForm.name" class="form-input" placeholder="如：云南" /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="showRegionModal=false">取消</button>
          <button class="btn btn-primary" @click="handleSaveRegion">{{ editRegionId ? '保存' : '创建' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const categories = ref([{ id: 1, name: '叶菜类', count: 45 }, { id: 2, name: '根茎类', count: 38 }, { id: 3, name: '水果类', count: 62 }, { id: 4, name: '粮食类', count: 28 }, { id: 5, name: '畜禽类', count: 19 }])
const regions = ref([{ id: 1, name: '云南', count: 86 }, { id: 2, name: '山东', count: 72 }, { id: 3, name: '河南', count: 58 }, { id: 4, name: '四川', count: 45 }])

const showCatModal = ref(false); const editCatId = ref(null); const catForm = ref({ name: '' })
const showRegionModal = ref(false); const editRegionId = ref(null); const regionForm = ref({ name: '' })

function handleSaveCat() { if (!catForm.value.name) return; ElMessage.success(editCatId.value ? '已更新' : '已创建'); showCatModal.value = false }
function handleDeleteCat(c) { ElMessage.success(`已删除「${c.name}」`) }
function handleSaveRegion() { if (!regionForm.value.name) return; ElMessage.success(editRegionId.value ? '已更新' : '已创建'); showRegionModal.value = false }
function handleDeleteRegion(r) { ElMessage.success(`已删除「${r.name}」`) }
</script>

<style scoped>
.admin-page { max-width: 1000px; }
.page-heading { margin-bottom: 20px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 4px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.split-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

.table-card { background: #fff; border: 1px solid #e8ebe4; border-radius: 16px; overflow: hidden; }
.table-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #f0f0eb; }
.tb-title { font-size: 14px; font-weight: 600; color: #1e1e1e; }
.table-wrap { overflow-x: auto; }
.dash-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.dash-table th { text-align: left; padding: 12px 20px; background: #f9faf8; color: #6b7280; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e8ebe4; }
.dash-table td { padding: 12px 20px; border-bottom: 1px solid #f0f0eb; color: #374151; }
.mono { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: #6b7280; }
.cat-tag { display: inline-flex; padding: 3px 10px; background: #e8f5e4; color: #7CCF5F; border-radius: 6px; font-size: 12px; font-weight: 500; }
.region-tag { display: inline-flex; padding: 3px 10px; background: #dbeafe; color: #3b82f6; border-radius: 6px; font-size: 12px; font-weight: 500; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 8px; transition: all 0.15s; }
.btn-sm { padding: 8px 16px; font-size: 12px; }
.btn-xs { padding: 4px 10px; font-size: 11px; border: none; background: transparent; color: #6b7280; }
.btn-xs:hover { color: #7CCF5F; }
.btn-danger:hover { color: #ef4444 !important; }
.btn-primary { background: #7CCF5F; color: #fff; border: none; }
.btn-primary:hover { background: #6bc04e; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { background: #fff; border-radius: 20px; width: 420px; max-width: 90vw; padding: 28px; }
.modal-title { font-size: 18px; font-weight: 600; color: #1e1e1e; margin: 0 0 20px; }
.modal-body {}
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 13px; font-weight: 500; color: #374151; margin-bottom: 6px; }
.form-input { width: 100%; padding: 10px 14px; border: 1px solid #e8ebe4; border-radius: 10px; font-size: 14px; color: #1e1e1e; background: #f9faf8; outline: none; font-family: 'Inter', sans-serif; box-sizing: border-box; }
.form-input:focus { border-color: #7CCF5F; }
.btn-ghost { padding: 10px 24px; background: transparent; color: #6b7280; border: 1px solid #e8ebe4; font-size: 14px; }
.btn-ghost:hover { background: #f5f7f2; }

@media (max-width: 768px) { .split-layout { grid-template-columns: 1fr; } }
</style>
