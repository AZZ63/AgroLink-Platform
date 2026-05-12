/* 订单状态映射 */
export const ORDER_STATUS = {
  UNPAID: '待支付',
  PAID: '已付款',
  CONFIRMED: '已确认',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REFUNDING: '退款中',
  REFUNDED: '已退款'
}

/* 支付状态映射 */
export const PAY_STATUS = {
  UNPAID: '',
  PAID: '已支付',
  REFUNDING: '退款中',
  REFUNDED: '已退款'
}

/* 退款状态映射 */
export const REFUND_STATUS = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  SUCCESS: '已退款',
  FAILED: '失败'
}

/* 退款原因映射 */
export const REASON_TYPES = {
  QUALITY: '品质问题',
  QUANTITY: '数量不符',
  WRONG: '发错货',
  OTHER: '其他'
}

/* 产品状态映射 */
export const PRODUCT_STATUS = {
  LISTED: '已上架',
  SOLD: '已售',
  UNLISTED: '已下架',
  PENDING: '待匹配',
  COMPLETED: '已成交',
  CLOSED: '已关闭'
}

/* 产品类型映射 */
export const INFO_TYPES = {
  SUPPLY: '供应',
  DEMAND: '需求'
}

export function statusLabel(s) {
  return ORDER_STATUS[s] || PRODUCT_STATUS[s] || s || ''
}

export function payLabel(s) {
  return PAY_STATUS[s] || s || ''
}

export function refundStatusLabel(s) {
  return REFUND_STATUS[s] || s || ''
}

export function reasonTypeLabel(s) {
  return REASON_TYPES[s] || s || ''
}

export function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 16).replace('T', ' ')
}
