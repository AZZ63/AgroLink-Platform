<template>
  <div class="addr-page">
    <NavBar />
    <div class="addr-container">
      <div class="page-header">
        <h1 class="page-title">收货地址</h1>
        <button class="tech-btn tech-btn-primary" @click="showForm = true; editing = null; form = { contactName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false }">+ 新增地址</button>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="addresses.length" class="addr-list">
        <div v-for="addr in addresses" :key="addr.id" class="addr-card">
          <div class="addr-info">
            <div class="addr-name">{{ addr.contactName }} <span class="addr-phone">{{ addr.phone }}</span></div>
            <div class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district || '' }}{{ addr.detail }}</div>
          </div>
          <div class="addr-actions">
            <label class="default-toggle">
              <input type="checkbox" :checked="addr.isDefault" @change="handleSetDefault(addr)" />
              <span>默认地址</span>
            </label>
            <button class="t-btn" @click="editAddress(addr)">编辑</button>
            <button class="t-btn t-btn-warn" @click="handleDelete(addr.id)">删除</button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>暂无收货地址</p>
        <button class="tech-btn-outline" @click="showForm = true; editing = null; form = { contactName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false }">添加地址</button>
      </div>

      <!-- Address Form Dialog -->
      <div v-if="showForm" class="dialog-overlay" @click.self="showForm = false">
        <div class="dialog-card">
          <h2 class="dialog-title">{{ editing ? '编辑地址' : '新增地址' }}</h2>
          <div class="form-grid">
            <div class="form-group">
              <label>联系人</label>
              <input v-model="form.contactName" class="f-input" placeholder="收货人姓名" />
            </div>
            <div class="form-group">
              <label>联系电话</label>
              <input v-model="form.phone" class="f-input" placeholder="手机号码" />
            </div>
            <div class="form-group">
              <label>省份</label>
              <select v-model="form.province" class="f-input">
                <option value="">选择省份</option>
                <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>城市</label>
              <input v-model="form.city" class="f-input" placeholder="城市" />
            </div>
            <div class="form-group">
              <label>区/县</label>
              <input v-model="form.district" class="f-input" placeholder="区/县（选填）" />
            </div>
            <div class="form-group full-width">
              <label>详细地址</label>
              <input v-model="form.detail" class="f-input" placeholder="街道、门牌号等" />
            </div>
          </div>
          <div class="dialog-actions">
            <button class="tech-btn tech-btn-primary" @click="handleSave" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
            <button class="tech-btn-outline" @click="showForm = false">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addressApi } from '../api/http'
import NavBar from '../components/NavBar.vue'

const addresses = ref([])
const loading = ref(true)
const showForm = ref(false)
const editing = ref(null)
const saving = ref(false)
const form = ref({ contactName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })

const provinces = ['北京','天津','上海','重庆','河北','山西','辽宁','吉林','黑龙江','江苏','浙江','安徽','福建','江西','山东','河南','湖北','湖南','广东','广西','海南','四川','贵州','云南','西藏','陕西','甘肃','青海','宁夏','新疆','内蒙古','香港','澳门','台湾']

async function loadAddresses() {
  try { addresses.value = await addressApi.list() }
  catch { addresses.value = [] }
  finally { loading.value = false }
}

function editAddress(addr) {
  editing.value = addr.id
  form.value = { ...addr }
  showForm.value = true
}

async function handleSave() {
  if (!form.value.contactName || !form.value.phone || !form.value.province || !form.value.city || !form.value.detail) {
    ElMessage.warning('请填写完整的地址信息')
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await addressApi.update({ id: editing.value, ...form.value })
      ElMessage.success('地址已更新')
    } else {
      await addressApi.create(form.value)
      ElMessage.success('地址已添加')
    }
    showForm.value = false
    await loadAddresses()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await addressApi.delete(id)
    ElMessage.success('地址已删除')
    await loadAddresses()
  } catch (e) { ElMessage.error(e.message || '删除失败') }
}

async function handleSetDefault(addr) {
  if (addr.isDefault) return
  try {
    await addressApi.setDefault(addr.id)
    ElMessage.success('已设为默认地址')
    await loadAddresses()
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

onMounted(() => loadAddresses())
</script>

<style scoped>
.addr-container { max-width: 720px; margin: 0 auto; padding: 32px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 36px; }

.addr-card {
  background: #161921; border: 1px solid #242837; border-radius: 10px;
  padding: 20px; margin-bottom: 12px;
  display: flex; justify-content: space-between; align-items: center;
}
.addr-card:hover { border-color: #00ff88; }
.addr-name { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.addr-phone { font-size: 13px; color: #555d6e; font-weight: 400; margin-left: 8px; }
.addr-detail { font-size: 13px; color: #8892a4; }
.addr-actions { display: flex; gap: 12px; align-items: center; flex-shrink: 0; }
.default-toggle { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #555d6e; cursor: pointer; }
.default-toggle input { accent-color: #00ff88; }
.t-btn { padding: 4px 12px; border: 1px solid #242837; border-radius: 4px; font-size: 12px; font-family: 'Outfit', sans-serif; cursor: pointer; transition: all 0.15s; background: transparent; color: #8892a4; }
.t-btn:hover { border-color: #00ff88; color: #00ff88; }
.t-btn-warn { border-color: #ff4466; color: #ff4466; }
.t-btn-warn:hover { background: rgba(255,68,102,0.1); }

/* Form Dialog */
.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.dialog-card {
  background: #161921; border: 1px solid #242837; border-radius: 12px;
  padding: 32px; width: 520px; max-width: 90vw;
}
.dialog-title { font-family: 'Bebas Neue', sans-serif; font-size: 28px; margin-bottom: 20px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.form-group { margin-bottom: 8px; }
.form-group.full-width { grid-column: 1 / -1; }
.form-group label { display: block; font-size: 11px; color: #555d6e; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px; }
.f-input {
  width: 100%; padding: 10px 14px; background: #1e2230; border: 1px solid #242837;
  border-radius: 6px; color: #e8edf5; font-family: 'Outfit', sans-serif; font-size: 14px; outline: none;
}
.f-input:focus { border-color: #00ff88; }
.dialog-actions { display: flex; gap: 12px; margin-top: 20px; }
.tech-btn-primary { padding: 12px 28px; background: #00ff88; color: #000; border: none; border-radius: 6px; font-family: 'Outfit', sans-serif; font-weight: 600; font-size: 14px; cursor: pointer; }
.tech-btn-primary:hover { background: #00cc6a; }
.tech-btn-outline { padding: 12px 28px; background: transparent; border: 1px solid #242837; border-radius: 6px; color: #8892a4; font-family: 'Outfit', sans-serif; font-size: 14px; cursor: pointer; }
.tech-btn-outline:hover { border-color: #00ff88; color: #00ff88; }

.loading { text-align: center; padding: 60px; color: #555d6e; }
.empty-state { text-align: center; padding: 80px 20px; color: #555d6e; }
.empty-state p { margin-bottom: 16px; }
</style>
