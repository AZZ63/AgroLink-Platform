<template>
  <div class="publish-page">
    <div class="page-heading">
      <h1 class="page-title">{{ isEdit ? '编辑' : '发布' }}供需信息</h1>
      <p class="page-subtitle">{{ isEdit ? '修改已发布的' : '发布新的' }}农产品信息</p>
    </div>

    <div class="form-card">
      <div class="form-section">
        <h3 class="form-section-title">基本信息</h3>
        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">信息类型</label>
            <div class="type-selector">
              <button :class="['type-btn', { active: form.infoType === 'SUPPLY' }]" @click="form.infoType = 'SUPPLY'" :disabled="isEdit">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
                供应
              </button>
              <button :class="['type-btn', { active: form.infoType === 'DEMAND' }]" @click="form.infoType = 'DEMAND'" :disabled="isEdit">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
                需求
              </button>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">产品名称</label>
            <input v-model="form.name" class="form-input" placeholder="如：有机大米" />
          </div>

          <div class="form-group">
            <label class="form-label">产品分类</label>
            <select v-model="form.type" class="form-select">
              <option value="">选择分类</option>
              <option v-for="t in types" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">产地</label>
            <div class="form-row">
              <select v-model="form.province" class="form-select">
                <option value="">省</option>
                <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
              </select>
              <select v-model="form.city" class="form-select">
                <option value="">市</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div class="form-divider"></div>

      <div class="form-section">
        <h3 class="form-section-title">价格与库存</h3>
        <div class="form-grid form-grid-3">
          <div class="form-group">
            <label class="form-label">价格</label>
            <input v-model.number="form.price" class="form-input" type="number" min="0" step="0.01" placeholder="0.00" />
          </div>
          <div class="form-group">
            <label class="form-label">数量</label>
            <input v-model.number="form.quantity" class="form-input" type="number" min="0" placeholder="0" />
          </div>
          <div class="form-group">
            <label class="form-label">单位</label>
            <select v-model="form.unit" class="form-select">
              <option value="斤">斤</option>
              <option value="公斤">公斤</option>
              <option value="吨">吨</option>
              <option value="箱">箱</option>
              <option value="个">个</option>
            </select>
          </div>
        </div>
      </div>

      <div class="form-divider"></div>

      <div class="form-section">
        <h3 class="form-section-title">详细描述</h3>
        <textarea v-model="form.description" class="form-textarea" rows="4" placeholder="描述产品特点、品质、包装等信息..."></textarea>
      </div>

      <div class="form-actions">
        <button class="btn btn-ghost" @click="$router.back()">取消</button>
        <button class="btn btn-primary btn-lg" @click="handleSubmit" :disabled="submitting">
          {{ submitting ? '发布中...' : (isEdit ? '保存修改' : '发布信息') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi } from '../../api/http'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const types = ref([])
const submitting = ref(false)

const provinces = ['北京','天津','上海','重庆','河北','山西','辽宁','吉林','黑龙江','江苏','浙江','安徽','福建','江西','山东','河南','湖北','湖南','广东','广西','海南','四川','贵州','云南','西藏','陕西','甘肃','青海','宁夏','新疆','内蒙古']

const form = reactive({
  infoType: 'SUPPLY', name: '', type: '', province: '', city: '',
  price: '', quantity: '', unit: '斤', description: ''
})

async function handleSubmit() {
  if (!form.name || !form.type || !form.price || !form.quantity) {
    return ElMessage.warning('请填写完整信息')
  }
  submitting.value = true
  try {
    const payload = { ...form }
    if (isEdit.value) {
      await productApi.update({ ...payload, id: route.params.id })
      ElMessage.success('已更新')
    } else {
      await productApi.create(payload)
      ElMessage.success('发布成功')
    }
    router.push('/products')
  } catch (e) { ElMessage.error(e.message || '操作失败') }
  finally { submitting.value = false }
}

onMounted(async () => {
  try { types.value = await productApi.getTypes() } catch {}
  if (isEdit.value) {
    try {
      const p = await productApi.getById(route.params.id)
      Object.assign(form, {
        infoType: p.infoType, name: p.name, type: p.type,
        province: p.province || '', city: p.city || '',
        price: p.price, quantity: p.quantity, unit: p.unit || '斤', description: p.description || ''
      })
    } catch { ElMessage.error('加载失败'); router.push('/products') }
  }
})
</script>

<style scoped>
.publish-page { max-width: 720px; }
.page-heading { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; color: #1e1e1e; margin: 0 0 6px; }
.page-subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.form-card { background: #ffffff; border: 1px solid #e8ebe4; border-radius: 20px; padding: 32px; }
.form-section { }
.form-section-title { font-size: 15px; font-weight: 600; color: #1e1e1e; margin: 0 0 20px; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.form-grid-3 { grid-template-columns: 1fr 1fr 1fr; }

.form-group { }
.form-label { display: block; font-size: 13px; font-weight: 500; color: #374151; margin-bottom: 6px; }

.form-input, .form-select, .form-textarea {
  width: 100%; padding: 10px 14px; border: 1px solid #e8ebe4; border-radius: 10px;
  font-size: 14px; color: #1e1e1e; background: #f9faf8; outline: none; font-family: 'Inter', sans-serif;
  transition: border-color 0.15s; box-sizing: border-box;
}
.form-input:focus, .form-select:focus, .form-textarea:focus { border-color: #7CCF5F; }
.form-textarea { resize: vertical; min-height: 100px; line-height: 1.6; }

.form-row { display: flex; gap: 8px; }
.form-row .form-select { flex: 1; }

.type-selector { display: flex; gap: 10px; }
.type-btn {
  display: flex; align-items: center; gap: 8px; flex: 1; justify-content: center;
  padding: 12px 20px; border: 2px solid #e8ebe4; border-radius: 12px;
  background: transparent; color: #6b7280; font-size: 14px; font-weight: 500;
  cursor: pointer; font-family: 'Inter', sans-serif; transition: all 0.2s;
}
.type-btn.active { border-color: #7CCF5F; color: #7CCF5F; background: #e8f5e4; }
.type-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.type-btn:hover:not(:disabled):not(.active) { border-color: #d1d5db; }

.form-divider { height: 1px; background: #f0f0eb; margin: 28px 0; }

.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 28px; }
.btn { font-family: 'Inter', sans-serif; cursor: pointer; border-radius: 10px; transition: all 0.15s; font-weight: 500; }
.btn-lg { padding: 14px 32px; font-size: 15px; }
.btn-primary { background: #7CCF5F; color: white; border: none; }
.btn-primary:hover { background: #6bc04e; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e8ebe4; padding: 10px 24px; font-size: 14px; }
.btn-ghost:hover { background: #f5f7f2; }

@media (max-width: 600px) { .form-grid, .form-grid-3 { grid-template-columns: 1fr; } }
</style>
