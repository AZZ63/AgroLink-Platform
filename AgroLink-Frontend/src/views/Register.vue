<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="role">
          <el-radio-group v-model="form.role">
            <el-radio value="FARMER">农户</el-radio>
            <el-radio value="MERCHANT">商户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width:100%">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-link">
        已有账号？<router-link to="/login">立即登录</router-link>
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

const form = reactive({ username: '', password: '', phone: '', role: 'FARMER' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function handleRegister() {
  if (loading.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.register({ ...form })
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
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
  background: linear-gradient(135deg, #67c23a20 0%, #409eff10 100%);
}
.auth-card { width: 400px; }
.auth-card h2 { text-align: center; margin-bottom: 24px; color: #303133; }
.auth-link { text-align: center; font-size: 14px; color: #909399; }
.auth-link a { color: #409eff; text-decoration: none; }
</style>
