<template>
  <div class="publish-page">
    <NavBar />

    <div class="publish-container">
      <div class="publish-card">
        <h1 class="publish-title">{{ isEdit ? '编辑' : '' }}{{ userStore.isFarmer ? '发布供应' : '发布需求' }}</h1>
        <p class="publish-sub">{{ isEdit ? '修改以下信息' : (userStore.isFarmer ? '填写以下信息发布您的农产品供应' : '填写以下信息发布您的采购需求') }}</p>

        <div class="form-grid">
          <div class="form-section">
            <h2 class="form-section-title">基本信息</h2>

            <div class="form-group">
              <label>信息类型</label>
              <div class="type-selector">
                <button v-if="userStore.isFarmer" :class="['type-option', { active: form.infoType === 'SUPPLY' }]">
                  <span class="type-icon">🌾</span>
                  <div><strong>供应</strong><p>我要卖</p></div>
                </button>
                <button v-if="userStore.isMerchant" :class="['type-option', { active: form.infoType === 'DEMAND' }]">
                  <span class="type-icon">🛒</span>
                  <div><strong>需求</strong><p>我要买</p></div>
                </button>
              </div>
            </div>

            <div class="form-group">
              <label>产品名称</label>
              <input v-model="form.name" placeholder="如：有机大白菜" class="f-input" />
            </div>

            <div class="form-group">
              <label>产品类型</label>
              <input v-model="form.type" placeholder="如：蔬菜、水果、粮食" class="f-input" />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>数量</label>
                <input v-model.number="form.quantity" type="number" min="1" class="f-input" />
              </div>
              <div class="form-group">
                <label>单位</label>
                <select v-model="form.unit" class="f-input">
                  <option value="斤">斤</option>
                  <option value="公斤">公斤</option>
                  <option value="吨">吨</option>
                  <option value="个">个</option>
                  <option value="箱">箱</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label>价格</label>
              <div class="price-input">
                <span class="price-prefix">¥</span>
                <input v-model.number="form.price" type="number" step="0.01" placeholder="0.00" class="f-input" />
                <span class="price-suffix">/ {{ form.unit || '斤' }}</span>
              </div>
            </div>
          </div>

          <div class="form-section">
            <h2 class="form-section-title">详细信息</h2>

            <div class="form-row">
              <div class="form-group">
                <label>省份</label>
                <select v-model="form.province" class="f-input">
                  <option value="">选择省份</option>
                  <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>城市</label>
                <input v-model="form.city" placeholder="输入城市" class="f-input" />
              </div>
            </div>

            <div class="form-group">
              <label>描述</label>
              <textarea v-model="form.description" placeholder="产品描述、产地信息、品质说明等" class="f-input f-textarea" rows="5"></textarea>
            </div>

            <div class="form-group">
              <label>图片</label>
              <div class="upload-area" @click="$refs.fileInput.click()">
                <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleUpload" />
                <div v-if="uploading" class="upload-progress">上传中...</div>
                <template v-else-if="form.image">
                  <img :src="form.image" class="upload-preview" />
                  <button class="upload-remove" @click.stop="form.image = ''">删除</button>
                </template>
                <template v-else>
                  <span class="upload-icon">+</span>
                  <span class="upload-text">点击上传图片</span>
                </template>
              </div>
            </div>

            <div class="form-actions">
              <button class="tech-btn tech-btn-primary pub-btn" @click="handleSubmit" :disabled="loading">
                {{ loading ? '提交中...' : (isEdit ? '保存修改' : '立即发布') }}
              </button>
              <button class="tech-btn-outline pub-btn" @click="$router.back()">取消</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { productApi } from '../api/http'
import http from '../api/http'
import NavBar from '../components/NavBar.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const uploading = ref(false)
const fileInput = ref(null)

const isEdit = !!route.params.id
const provinces = ['北京','天津','上海','重庆','河北','山西','辽宁','吉林','黑龙江','江苏','浙江','安徽','福建','江西','山东','河南','湖北','湖南','广东','广西','海南','四川','贵州','云南','西藏','陕西','甘肃','青海','宁夏','新疆','内蒙古','香港','澳门','台湾']

const form = reactive({
  infoType: route.query.type === 'DEMAND' || userStore.isMerchant ? 'DEMAND' : 'SUPPLY',
  name: '', type: '', quantity: 100,
  unit: '斤', price: null, description: '', province: '', city: '', image: ''
})

async function handleUpload(e) {
  const file = e.target.files[0]
  if (!file) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await http.post('/file/upload', fd)
    form.image = res
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleSubmit() {
  if (!form.name || !form.type || !form.price) {
    ElMessage.warning('请填写完整信息')
    return
  }
  loading.value = true
  try {
    const payload = { ...form, price: Number(form.price) }
    if (isEdit) {
      payload.id = parseInt(route.params.id)
      await productApi.update(payload)
      ElMessage.success('保存成功')
    } else {
      await productApi.create(payload)
      ElMessage.success('发布成功')
    }
    router.push('/products')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (isEdit) {
    try {
      const p = await productApi.getById(route.params.id)
      Object.assign(form, {
        name: p.name, type: p.type, quantity: p.quantity,
        unit: p.unit, price: p.price, description: p.description || '',
        province: p.province || '', city: p.city || '',
        infoType: p.infoType, image: p.image || ''
      })
    } catch (e) {
      ElMessage.error('加载产品信息失败')
      router.push('/products')
    }
  }
})
</script>

<style scoped>
.tech-btn-outline {
  padding: 12px 32px;
  background: transparent;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #8892a4;
  font-family: 'Outfit', sans-serif;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.tech-btn-outline:hover { border-color: #00ff88; color: #00ff88; }

.publish-container {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 32px;
}
.publish-card {
  background: #161921;
  border: 1px solid #242837;
  border-radius: 12px;
  padding: 40px;
}
.publish-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 36px;
  letter-spacing: 1px;
  margin-bottom: 4px;
}
.publish-sub { color: #555d6e; font-size: 14px; margin-bottom: 32px; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; }
.form-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #8892a4;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 1px solid #242837;
}

.form-group { margin-bottom: 16px; }
.form-group label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: #8892a4;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  margin-bottom: 6px;
}
.form-row { display: flex; gap: 16px; }
.form-row .form-group { flex: 1; }

.f-input {
  width: 100%;
  padding: 10px 14px;
  background: #1e2230;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #e8edf5;
  font-family: 'Outfit', sans-serif;
  font-size: 14px;
  outline: none;
  transition: border 0.2s;
}
.f-input:focus { border-color: #00ff88; }
.f-input::placeholder { color: #555d6e; }
.f-textarea { resize: vertical; min-height: 120px; }

.type-selector { display: flex; gap: 12px; }
.type-option {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #1e2230;
  border: 1px solid #242837;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.type-option.active { border-color: #00ff88; background: rgba(0,255,136,0.05); }
.type-option strong { display: block; font-size: 15px; color: #e8edf5; }
.type-option p { font-size: 12px; color: #555d6e; margin: 0; }
.type-icon { font-size: 28px; }

.price-input { display: flex; align-items: center; gap: 0; }
.price-prefix, .price-suffix { padding: 0 12px; color: #555d6e; font-size: 14px; }

.form-actions { display: flex; gap: 12px; margin-top: 24px; }
.tech-btn {
  padding: 12px 32px;
  font-family: 'Bebas Neue', sans-serif;
  font-size: 16px;
  letter-spacing: 1.5px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.tech-btn-primary { background: #00ff88; color: #000; }
.tech-btn-primary:hover { background: #00cc6a; box-shadow: 0 0 25px rgba(0,255,136,0.25); }
.tech-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.pub-btn { flex: 1; max-width: 200px; }

.upload-area {
  border: 1px dashed #242837; border-radius: 8px; padding: 24px;
  text-align: center; cursor: pointer; transition: all 0.2s;
  display: flex; flex-direction: column; align-items: center; gap: 8px;
}
.upload-area:hover { border-color: #00ff88; background: rgba(0,255,136,0.02); }
.upload-icon { font-size: 28px; color: #555d6e; }
.upload-text { font-size: 13px; color: #555d6e; }
.upload-preview { max-height: 160px; max-width: 100%; border-radius: 4px; }
.upload-remove {
  padding: 4px 12px; background: rgba(255,51,85,0.1); border: 1px solid rgba(255,51,85,0.2);
  border-radius: 4px; color: #ff3355; font-family: 'Outfit', sans-serif; font-size: 12px; cursor: pointer;
}
.upload-progress { color: #8892a4; font-size: 14px; }
</style>
