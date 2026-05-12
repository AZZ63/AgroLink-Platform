<template>
  <div class="address-page">
    <div class="page-heading">
      <h1 class="page-title">收货地址</h1>
      <p class="page-subtitle">管理你的收货地址</p>
      <button class="btn btn-outline btn-sm" @click="showForm = true; editingId = null; form = { contactName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0 }">+ 新增地址</button>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>

    <div v-else-if="addresses.length === 0" class="empty-state">
      <p>还没有收货地址</p>
    </div>

    <div v-else class="address-list">
      <div v-for="addr in addresses" :key="addr.id" class="address-card">
        <div class="ac-top">
          <div class="ac-info">
            <span class="ac-name">{{ addr.contactName }}</span>
            <span class="ac-phone">{{ addr.phone }}</span>
            <span v-if="addr.isDefault" class="ac-default">默认</span>
          </div>
          <div class="ac-actions">
            <button class="ac-btn" @click="editAddress(addr)">编辑</button>
            <button class="ac-btn ac-btn-danger" @click="handleDelete(addr)">删除</button>
          </div>
        </div>
        <div class="ac-address">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}</div>
        <div v-if="!addr.isDefault" class="ac-set-default">
          <button class="ac-btn" @click="handleSetDefault(addr)">设为默认</button>
        </div>
      </div>
    </div>

    <!-- 地址表单对话框 -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-card">
        <h3 class="modal-title">{{ editingId ? '编辑地址' : '新增地址' }}</h3>
        <div class="modal-body">
          <div class="form-group"><label>联系人</label><input v-model="form.contactName" class="form-input" /></div>
          <div class="form-group"><label>电话</label><input v-model="form.phone" class="form-input" /></div>
          <div class="form-row">
            <select v-model="form.province" class="form-select"><option value="">省</option><option v-for="p in provinces" :key="p" :value="p">{{ p }}</option></select>
            <select v-model="form.city" class="form-select"><option value="">市</option></select>
            <select v-model="form.district" class="form-select"><option value="">区</option></select>
          </div>
          <div class="form-group"><label>详细地址</label><input v-model="form.detail" class="form-input" /></div>
          <label class="form-checkbox"><input type="checkbox" v-model="form.isDefault" :true-value="1" :false-value="0" /> 设为默认地址</label>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" @click="showForm = false">取消</button>
          <button class="btn btn-primary" @click="handleSave" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addressApi } from '../../api/http'

const addresses = ref([])
const loading = ref(true)
const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const form = ref({ contactName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: 0 })

const provinces = ['北京','天津','上海','重庆','河北','山西','辽宁','吉林','黑龙江','江苏','浙江','安徽','福建','江西','山东','河南','湖北','湖南','广东','广西','海南','四川','贵州','云南','西藏','陕西','甘肃','青海','宁夏','新疆','内蒙古']

async function loadAddresses() {
  try { addresses.value = await addressApi.list() }
  catch { ElMessage.error('加载地址失败') }
  finally { loading.value = false }
}

function editAddress(addr) {
  editingId.value = addr.id
  form.value = { ...addr }
  showForm.value = true
}

async function handleSave() {
  if (!form.value.contactName || !form.value.phone || !form.value.detail) {
    return ElMessage.warning('请填写完整信息')
  }
  saving.value = true
  try {
    if (editingId.value) { await addressApi.update({ ...form.value, id: editingId.value }); ElMessage.success('已更新') }
    else { await addressApi.create(form.value); ElMessage.success('已创建') }
    showForm.value = false; loadAddresses()
  } catch (e) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

async function handleDelete(addr) {
  try { await addressApi.delete(addr.id); ElMessage.success('已删除'); loadAddresses() }
  catch (e) { ElMessage.error(e.message) }
}

async function handleSetDefault(addr) {
  try { await addressApi.setDefault(addr.id); ElMessage.success('已设为默认'); loadAddresses() }
  catch (e) { ElMessage.error(e.message) }
}

onMounted(loadAddresses)
</script>

<style scoped>
.address-page { max-width: 800px; }
.page-heading { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.loading-state, .empty-state { text-align: center; padding: 60px; color: #6b7280; }

.address-list { display: flex; flex-direction: column; gap: 12px; }
.address-card { background: #ffffff; border: 1px solid #e8ebe4; border-radius: 16px; padding: 20px; transition: box-shadow 0.2s; }
.address-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }

.ac-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; flex-wrap: wrap; gap: 8px; }
.ac-info { display: flex; align-items: center; gap: 10px; }
.ac-name { font-size: 15px; font-weight: 600; color: #1e1e1e; }
.ac-phone { font-size: 14px; color: #6b7280; }
.ac-default { padding: 2px 8px; background: #e8f5e4; color: #7CCF5F; border-radius: 4px; font-size: 10px; font-weight: 600; }
.ac-actions { display: flex; gap: 6px; }
.ac-btn { padding: 4px 12px; border: 1px solid #e8ebe4; border-radius: 6px; background: transparent; color: #6b7280; font-size: 12px; cursor: pointer; font-family: 'Inter', sans-serif; }
.ac-btn:hover { border-color: #7CCF5F; color: #7CCF5F; }
.ac-btn-danger:hover { border-color: #ef4444; color: #ef4444; }
.ac-address { font-size: 13px; color: #6b7280; line-height: 1.5; margin-bottom: 8px; }
.ac-set-default { margin-top: 4px; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { background: #ffffff; border-radius: 20px; width: 480px; max-width: 90vw; padding: 28px; }
.modal-title { font-size: 18px; font-weight: 600; color: #1e1e1e; margin: 0 0 20px; }
.modal-body { display: flex; flex-direction: column; gap: 14px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }

.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label { font-size: 13px; font-weight: 500; color: #374151; }
.form-input, .form-select { padding: 10px 14px; border: 1px solid #e8ebe4; border-radius: 10px; font-size: 14px; color: #1e1e1e; background: #f9faf8; outline: none; font-family: 'Inter', sans-serif; }
.form-input:focus, .form-select:focus { border-color: #7CCF5F; }
.form-row { display: flex; gap: 8px; }
.form-row .form-select { flex: 1; }
.form-checkbox { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #374151; cursor: pointer; }
.form-checkbox input { width: 16px; height: 16px; accent-color: #7CCF5F; }

.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 10px; transition: all 0.15s; }
.btn-primary { padding: 10px 24px; background: #7CCF5F; color: white; border: none; font-size: 14px; font-weight: 500; }
.btn-primary:hover { background: #6bc04e; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-ghost { padding: 10px 24px; background: transparent; color: #6b7280; border: 1px solid #e8ebe4; font-size: 14px; }
.btn-ghost:hover { background: #f5f7f2; }
.btn-outline { padding: 8px 16px; border: 1px solid #e8ebe4; background: transparent; color: #374151; font-size: 13px; }
.btn-outline:hover { border-color: #7CCF5F; color: #7CCF5F; }
.btn-sm { font-size: 12px; }
</style>
