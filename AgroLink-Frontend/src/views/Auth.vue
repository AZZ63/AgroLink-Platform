<template>
  <div class="auth-page">
    <div class="auth-grid">
      <div class="auth-brand">
        <div class="brand-content">
          <div class="brand-graphic">
            <div class="graphic-circle"></div>
            <div class="graphic-lines">
              <span></span><span></span><span></span><span></span>
            </div>
          </div>
          <h1 class="brand-title">AGROLINK</h1>
          <p class="brand-sub">农产品供需交易平台</p>
          <div class="brand-metrics">
            <div class="metric">
              <span class="metric-value">2,847</span>
              <span class="metric-label">今日供需</span>
            </div>
            <div class="metric">
              <span class="metric-value">1,023</span>
              <span class="metric-label">在线农户</span>
            </div>
            <div class="metric">
              <span class="metric-value">856</span>
              <span class="metric-label">注册商户</span>
            </div>
          </div>
        </div>
      </div>
      <div class="auth-form-panel">
        <div class="auth-form-container">
          <div class="auth-tabs">
            <button :class="['auth-tab', { active: activeTab === 'login' }]" @click="activeTab = 'login'">登录</button>
            <button :class="['auth-tab', { active: activeTab === 'register' }]" @click="activeTab = 'register'">注册</button>
          </div>

          <!-- Login Form -->
          <div v-if="activeTab === 'login'" class="auth-form">
            <div class="form-group">
              <label>用户名</label>
              <input v-model="loginForm.username" placeholder="输入用户名" class="tech-input" @keyup.enter="handleLogin" />
            </div>
            <div class="form-group">
              <label>密码</label>
              <input v-model="loginForm.password" type="password" placeholder="输入密码" class="tech-input" @keyup.enter="handleLogin" />
            </div>
            <label class="remember-me">
              <input type="checkbox" v-model="remember" />
              <span>记住密码</span>
            </label>
            <button class="tech-btn tech-btn-primary" @click="handleLogin" :disabled="loginLoading">
              {{ loginLoading ? '登录中...' : '登录' }}
            </button>
          </div>

          <!-- Register Form -->
          <div v-else class="auth-form">
            <div class="form-group">
              <label>用户名</label>
              <input v-model="regForm.username" placeholder="输入用户名" class="tech-input" @keyup.enter="handleRegister" />
            </div>
            <div class="form-group">
              <label>密码</label>
              <input v-model="regForm.password" type="password" placeholder="密码至少6位" class="tech-input" @keyup.enter="handleRegister" />
            </div>
            <div class="form-group">
              <label>手机号</label>
              <input v-model="regForm.phone" placeholder="输入手机号" class="tech-input" @keyup.enter="handleRegister" />
            </div>
            <div class="form-group">
              <label>角色</label>
              <div class="role-selector">
                <button :class="['role-option', { active: regForm.role === 'FARMER' }]" @click="regForm.role = 'FARMER'">
                  <span>农户</span>
                </button>
                <button :class="['role-option', { active: regForm.role === 'MERCHANT' }]" @click="regForm.role = 'MERCHANT'">
                  <span>商户</span>
                </button>
              </div>
            </div>
            <button class="tech-btn tech-btn-primary" @click="handleRegister" :disabled="regLoading">
              {{ regLoading ? '注册中...' : '注册' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getRoleHome } from '../router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  if (userStore.isLoggedIn) {
    router.replace(route.query.redirect || getRoleHome(userStore.role))
  }
})

const activeTab = ref(route.query.tab || 'login')

const loginForm = reactive({ username: '', password: '' })
const loginLoading = ref(false)
const remember = ref(false)

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loginLoading.value = true
  try {
    await userStore.login(loginForm, remember.value)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || getRoleHome(userStore.role))
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

const regForm = reactive({ username: '', password: '', phone: '', role: 'FARMER' })
const regLoading = ref(false)

async function handleRegister() {
  if (!regForm.username || !regForm.password || !regForm.phone) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (regForm.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  regLoading.value = true
  try {
    await userStore.register({ ...regForm })
    ElMessage.success('注册成功')
    router.push(getRoleHome(userStore.role))
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    regLoading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
}
.auth-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  width: 100%;
}

/* Brand Panel */
.auth-brand {
  background: linear-gradient(135deg, #0d1117 0%, #0a0c10 50%, #111318 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  border-right: 1px solid #242837;
}
.auth-brand::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 50% 50%, rgba(0,255,136,0.03) 0%, transparent 50%);
  animation: pulse 8s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}
.brand-content { text-align: center; position: relative; z-index: 1; padding: 40px; }

.brand-graphic {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 32px;
}
.graphic-circle {
  width: 120px;
  height: 120px;
  border: 2px solid rgba(0,255,136,0.3);
  border-radius: 50%;
  position: absolute;
  animation: spin 20s linear infinite;
}
.graphic-circle::before {
  content: '';
  position: absolute;
  top: -4px; left: 50%;
  width: 8px; height: 8px;
  background: #00ff88;
  border-radius: 50%;
  box-shadow: 0 0 10px #00ff88, 0 0 20px rgba(0,255,136,0.3);
}
@keyframes spin { to { transform: rotate(360deg); } }

.graphic-lines {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  gap: 8px;
}
.graphic-lines span {
  width: 4px; height: 30px;
  background: #00ff88;
  border-radius: 2px;
  opacity: 0.4;
  animation: bar 1.5s ease-in-out infinite;
}
.graphic-lines span:nth-child(2) { animation-delay: 0.2s; height: 45px; }
.graphic-lines span:nth-child(3) { animation-delay: 0.4s; height: 25px; }
.graphic-lines span:nth-child(4) { animation-delay: 0.6s; height: 40px; }
@keyframes bar {
  0%, 100% { opacity: 0.3; transform: scaleY(0.8); }
  50% { opacity: 1; transform: scaleY(1.2); }
}

.brand-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 52px;
  letter-spacing: 4px;
  color: #00ff88;
  text-shadow: 0 0 30px rgba(0,255,136,0.2);
  margin-bottom: 8px;
}
.brand-sub { color: #8892a4; font-size: 16px; margin-bottom: 40px; }

.brand-metrics { display: flex; gap: 32px; justify-content: center; }
.metric { text-align: center; }
.metric-value {
  display: block;
  font-family: 'JetBrains Mono', monospace;
  font-size: 24px;
  font-weight: 500;
  color: #e8edf5;
}
.metric-label {
  font-size: 12px;
  color: #555d6e;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* Form Panel */
.auth-form-panel {
  background: #0a0c10;
  display: flex;
  align-items: center;
  justify-content: center;
}
.auth-form-container {
  width: 100%;
  max-width: 380px;
  padding: 40px;
}

.auth-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 32px;
  border-bottom: 1px solid #242837;
}
.auth-tab {
  flex: 1;
  background: none;
  border: none;
  padding: 12px 0;
  font-family: 'Bebas Neue', sans-serif;
  font-size: 20px;
  letter-spacing: 2px;
  color: #555d6e;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}
.auth-tab.active {
  color: #00ff88;
  border-bottom-color: #00ff88;
}

.auth-form { display: flex; flex-direction: column; gap: 20px; }

.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label {
  font-size: 12px;
  font-weight: 500;
  color: #8892a4;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.tech-input {
  width: 100%;
  padding: 12px 16px;
  background: #1e2230;
  border: 1px solid #242837;
  border-radius: 6px;
  color: #e8edf5;
  font-family: 'Outfit', sans-serif;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
}
.tech-input:focus {
  border-color: #00ff88;
  box-shadow: 0 0 0 3px rgba(0,255,136,0.08);
}
.tech-input::placeholder { color: #555d6e; }

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8892a4;
  font-size: 14px;
  cursor: pointer;
}
.remember-me input { accent-color: #00ff88; }

.tech-btn {
  width: 100%;
  padding: 14px;
  font-family: 'Bebas Neue', sans-serif;
  font-size: 18px;
  letter-spacing: 2px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.tech-btn-primary {
  background: #00ff88;
  color: #000;
}
.tech-btn-primary:hover {
  background: #00cc6a;
  box-shadow: 0 0 30px rgba(0,255,136,0.3);
}
.tech-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.role-selector { display: flex; gap: 12px; }
.role-option {
  flex: 1;
  padding: 14px;
  background: #1e2230;
  border: 1px solid #242837;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Outfit', sans-serif;
  font-size: 15px;
  font-weight: 500;
  text-align: center;
  color: #8892a4;
}
.role-option.active {
  border-color: #00ff88;
  color: #e8edf5;
  background: rgba(0,255,136,0.05);
}

@media (max-width: 640px) {
  .auth-grid { grid-template-columns: 1fr; }
  .auth-form-container { padding: 20px 16px; }
  .auth-form-panel { padding: 24px 20px; }
  .brand-title { font-size: 28px; }
}
</style>
