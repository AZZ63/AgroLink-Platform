import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/http'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const rememberMe = ref(localStorage.getItem('rememberMe') === 'true')

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || '')
  const isFarmer = computed(() => role.value === 'FARMER')
  const isMerchant = computed(() => role.value === 'MERCHANT')
  const isAdmin = computed(() => role.value === 'ADMIN')

  function saveToStorage() {
    // 先清除所有旧 token，避免残留导致权限错误
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('refreshToken')
    sessionStorage.removeItem('user')
    if (rememberMe.value) {
      localStorage.setItem('token', token.value)
      localStorage.setItem('refreshToken', refreshToken.value)
      localStorage.setItem('user', JSON.stringify(user.value))
      localStorage.setItem('rememberMe', 'true')
    } else {
      sessionStorage.setItem('token', token.value)
      sessionStorage.setItem('refreshToken', refreshToken.value)
      sessionStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  function restoreFromStorage() {
    if (!token.value) {
      // 尝试从 sessionStorage 恢复（未勾选记住密码时的临时会话）
      const sToken = sessionStorage.getItem('token')
      if (sToken) {
        token.value = sToken
        refreshToken.value = sessionStorage.getItem('refreshToken') || ''
        const u = sessionStorage.getItem('user')
        user.value = u ? JSON.parse(u) : null
      }
    }
  }

  async function login(credentials, remember = false) {
    rememberMe.value = remember
    const res = await authApi.login(credentials)
    setTokens(res)
    return res
  }

  async function register(data) {
    const res = await authApi.register(data)
    rememberMe.value = true
    setTokens(res)
    return res
  }

  function setTokens(res) {
    token.value = res.token
    refreshToken.value = res.refreshToken
    user.value = { userId: res.userId, username: res.username, role: res.role }
    saveToStorage()
  }

  async function refreshAccessToken() {
    if (!refreshToken.value) return false
    try {
      const res = await authApi.refresh(refreshToken.value)
      token.value = res.token
      // 服务端返回相同的 refreshToken，不需要更新
      user.value = { userId: res.userId, username: res.username, role: res.role }
      saveToStorage()
      return true
    } catch {
      // refresh 失败，彻底登出
      forceLogout()
      return false
    }
  }

  function logout() {
    // 尝试调用后端登出使 token 失效，不阻塞
    if (token.value) {
      authApi.logout().catch(() => {})
    }
    forceLogout()
  }

  function forceLogout() {
    token.value = ''
    refreshToken.value = ''
    user.value = null
    rememberMe.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    localStorage.removeItem('rememberMe')
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('refreshToken')
    sessionStorage.removeItem('user')
  }

  return {
    user, token, refreshToken, isLoggedIn, role,
    isFarmer, isMerchant, isAdmin, rememberMe,
    login, register, logout, forceLogout,
    refreshAccessToken, restoreFromStorage
  }
})
