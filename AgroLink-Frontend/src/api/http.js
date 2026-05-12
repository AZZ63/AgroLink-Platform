import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 用于刷新 token 的锁，防止并发重复刷新
let isRefreshing = false
let refreshSubscribers = []

function onRefreshed(token) {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

function addRefreshSubscriber(cb) {
  refreshSubscribers.push(cb)
}

function getToken() {
  return localStorage.getItem('token') || sessionStorage.getItem('token') || ''
}

function getRefreshToken() {
  return localStorage.getItem('refreshToken') || sessionStorage.getItem('refreshToken') || ''
}

http.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 401) {
      // 交给 401 错误处理
      return Promise.reject({ isAuthError: true, message: res.message, config: response.config })
    }
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  async error => {
    const originalRequest = error.config
    // 只处理 401 且尚未重试过的请求
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const refreshToken = getRefreshToken()
      if (!refreshToken) {
        clearAuth()
        return Promise.reject(error)
      }

      if (isRefreshing) {
        // 已有刷新在进行中，排队等待新 token
        return new Promise(resolve => {
          addRefreshSubscriber(newToken => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            resolve(http(originalRequest))
          })
        })
      }

      isRefreshing = true
      try {
        const res = await axios.post('/api/user/refresh', { refreshToken })
        const newToken = res.data.data.token
        // 更新存储
        const token = localStorage.getItem('token')
        if (token) {
          localStorage.setItem('token', newToken)
        } else {
          sessionStorage.setItem('token', newToken)
        }
        onRefreshed(newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return http(originalRequest)
      } catch {
        clearAuth()
        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    }

    if (error.response?.status === 401) {
      clearAuth()
    }
    return Promise.reject(error)
  }
)

function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  localStorage.removeItem('rememberMe')
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('refreshToken')
  sessionStorage.removeItem('user')
  window.location.href = '/login'
}

export default http

export const authApi = {
  login(data) { return http.post('/user/login', data) },
  register(data) { return http.post('/user/register', data) },
  refresh(refreshToken) { return http.post('/user/refresh', { refreshToken }) },
  logout() { return http.post('/user/logout') },
  getUserInfo() { return http.get('/user/info') }
}

export const productApi = {
  create(data) { return http.post('/product/create', data) },
  query(data) { return http.post('/product/query', data) },
  getHot() { return http.get('/product/hot') },
  getTypes() { return http.get('/product/types') },
  getMy() { return http.get('/product/my') },
  delete(id) { return http.delete(`/product/${id}`) },
  updateStatus(id, status) { return http.put(`/product/${id}/status?status=${status}`) },
  getById(id) { return http.get(`/product/${id}`) },
  update(data) { return http.put('/product/update', data) },
  getStats() { return http.get('/product/stats') }
}

export const orderApi = {
  create(data) { return http.post('/order/create', data) },
  getMy() { return http.get('/order/my') },
  getBuy() { return http.get('/order/buy') },
  getSell() { return http.get('/order/sell') },
  updateStatus(id, status) { return http.put(`/order/${id}/status?status=${status}`) },
  getById(id) { return http.get(`/order/${id}`) }
}

export const paymentApi = {
  create(orderId) { return http.post('/payment/create', { orderId }) },
  callback(tradeNo) { return http.post('/payment/callback', { tradeNo }) },
  refundRequest(orderId, reasonType, reasonDetail, images) { return http.post('/payment/refund/request', { orderId, reasonType, reasonDetail, images }) },
  refundReview(refundId, approved, rejectReason) { return http.post('/payment/refund/review', { refundId, approved, rejectReason }) },
  getRefunds(orderId) { return http.get(`/payment/refund/${orderId}`) },
  getPendingRefunds() { return http.get('/payment/refund/pending') },
  getPayment(orderId) { return http.get(`/payment/${orderId}`) }
}

export const notifyApi = {
  list() { return http.get('/notify/list') },
  unread() { return http.get('/notify/unread') },
  markRead(id) { return http.put(`/notify/${id}/read`) },
  markAllRead() { return http.put('/notify/read-all') }
}

export const addressApi = {
  list() { return http.get('/address/list') },
  get(id) { return http.get(`/address/${id}`) },
  create(data) { return http.post('/address/create', data) },
  update(data) { return http.put('/address/update', data) },
  delete(id) { return http.delete(`/address/${id}`) },
  setDefault(id) { return http.put(`/address/${id}/default`) }
}

export const reviewApi = {
  create(data) { return http.post('/product/review/create', data) },
  getProductReviews(productId) { return http.get(`/product/review/product/${productId}`) },
  getProductRating(productId) { return http.get(`/product/review/product/${productId}/rating`) },
  getOrderReview(orderId) { return http.get(`/product/review/order/${orderId}`) },
  getUserReviews(userId) { return http.get(`/product/review/user/${userId}`) },
  delete(reviewId) { return http.delete(`/product/review/${reviewId}`) }
}

export const viewApi = {
  viewProduct(id) { return http.post(`/product/${id}/view`) }
}

export const favoriteApi = {
  toggle(productId) { return http.post(`/product/favorite/${productId}`) },
  list() { return http.get('/product/favorite/list') },
  ids() { return http.get('/product/favorite/ids') },
  count(productId) { return http.get(`/product/favorite/count/${productId}`) },
  check(productId) { return http.get(`/product/favorite/check/${productId}`) }
}

export const cartApi = {
  add(data) { return http.post('/cart/add', data) },
  list() { return http.get('/cart/list') },
  getChecked() { return http.get('/cart/checked') },
  getCheckedCount() { return http.get('/cart/checked/count') },
  update(id, data) { return http.put(`/cart/${id}`, data) },
  toggleAll(checked) { return http.put('/cart/toggle-all', null, { params: { checked } }) },
  remove(id) { return http.delete(`/cart/${id}`) },
  removeBatch(ids) { return http.delete('/cart/batch', { data: ids }) },
  clear() { return http.delete('/cart/clear') }
}
