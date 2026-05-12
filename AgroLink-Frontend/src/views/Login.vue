<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="remember">记住密码</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const remember = ref(false)

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form, remember.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #409eff20 0%, #337ecc10 100%);
}
.auth-card { width: 400px; }
.auth-card h2 { text-align: center; margin-bottom: 24px; color: #303133; }
.auth-link { text-align: center; font-size: 14px; color: #909399; }
.auth-link a { color: #409eff; text-decoration: none; }
</style>
