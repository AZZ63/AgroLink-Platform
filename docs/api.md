# API 文档

基础地址：`http://localhost:8080`（通过网关访问，所有请求需带 `Authorization: Bearer <token>` header）

## 目录

- [用户服务](#用户服务)
- [商品服务](#商品服务)
- [订单服务](#订单服务)
- [通知服务](#通知服务)
- [购物车](#购物车)

---

## 用户服务

### 登录

```
POST /api/user/login
```

**请求：**
```json
{ "username": "farmer2", "password": "test123" }
```

**响应：**
```json
{
  "token": "eyJ...",
  "refreshToken": "eyJ...",
  "username": "farmer2",
  "role": "FARMER",
  "userId": 1
}
```

### 注册

```
POST /api/user/register
```

**请求：**
```json
{ "username": "newuser", "password": "pass123", "role": "FARMER", "phone": "13800138000" }
```

### 刷新 Token

```
POST /api/user/refresh
```

**请求：**
```json
{ "refreshToken": "eyJ..." }
```

### 获取用户信息

```
GET /api/user/info
```

### 地址管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/address/list | 地址列表 |
| GET | /api/address/{id} | 地址详情 |
| POST | /api/address/create | 新增地址 |
| PUT | /api/address/update | 更新地址 |
| DELETE | /api/address/{id} | 删除地址 |
| PUT | /api/address/{id}/default | 设为默认 |

---

## 商品服务

### 创建商品

```
POST /api/product/create
```

**请求：**
```json
{
  "name": "有机番茄",
  "type": "蔬菜",
  "quantity": 500,
  "price": 8.00,
  "unit": "斤",
  "description": "新鲜有机番茄",
  "province": "广东",
  "city": "深圳",
  "infoType": "SUPPLY",
  "image": "/uploads/xxx.jpg",
  "images": "[\"/uploads/1.jpg\",\"/uploads/2.jpg\"]"
}
```

**说明：** `infoType` 取值 `SUPPLY`(供应) 或 `DEMAND`(需求)。农户角色只能发布 SUPPLY，商户只能发布 DEMAND。

### 商品查询

```
POST /api/product/query
```

**请求参数：**

| 字段 | 类型 | 说明 |
|------|------|------|
| keyword | String | 关键词模糊搜索 |
| type | String | 产品类型精确匹配 |
| infoType | String | SUPPLY / DEMAND |
| status | String | LISTED / SOLD / PENDING |
| province | String | 省份 |
| city | String | 城市 |
| minPrice | BigDecimal | 最低价 |
| maxPrice | BigDecimal | 最高价 |
| sortBy | String | 排序字段 (createdAt / price) |
| sortOrder | String | asc / desc |
| page | int | 页码，默认 1 |
| size | int | 每页条数，默认 20 |

**响应：**
```json
{
  "records": [{ "id": 1, "name": "...", "price": 3.00 }],
  "total": 42,
  "page": 1,
  "size": 20,
  "pages": 3
}
```

### 其他商品接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/product/hot | 热门产品（最新 10 条） |
| GET | /api/product/{id} | 产品详情 |
| GET | /api/product/types | 产品类型列表 |
| GET | /api/product/my | 我的产品 |
| GET | /api/product/stats | 用户统计（每日浏览量） |
| PUT | /api/product/update | 更新产品 |
| PUT | /api/product/{id}/status?status=xxx | 更新状态 |
| DELETE | /api/product/{id} | 删除产品 |
| POST | /api/product/{id}/view | 增加浏览量 |

### 收藏

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/product/favorite/{productId} | 切换收藏（收藏/取消） |
| GET | /api/product/favorite/list | 我的收藏 |
| GET | /api/product/favorite/ids | 我收藏的产品 ID 列表 |
| GET | /api/product/favorite/count/{productId} | 收藏数 |
| GET | /api/product/favorite/check/{productId} | 是否已收藏 |

### 评价

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/product/review/create | 创建评价 |
| GET | /api/product/review/product/{productId} | 产品评价列表 |
| GET | /api/product/review/product/{productId}/rating | 产品评分 |
| GET | /api/product/review/order/{orderId} | 订单评价 |
| GET | /api/product/review/user/{userId} | 用户评价 |
| DELETE | /api/product/review/{id} | 删除评价 |

### 文件上传

```
POST /api/file/upload
```

- Content-Type: `multipart/form-data`
- 参数：`file`（图片文件）
- 响应：上传后的 URL 路径 `/uploads/uuid.jpg`

---

## 订单服务

### 创建订单

```
POST /api/order/create
```

**请求：**
```json
{ "productId": 1, "quantity": 10 }
```

### 订单状态流转

```
UNPAID → PAID → CONFIRMED → COMPLETED
  ↓        ↓        ↓
CANCELLED  REFUNDING → REFUNDED
```

### 更新订单状态

```
PUT /api/order/{id}/status?status=CONFIRMED
```

**权限：**
| 目标状态 | 操作者 |
|----------|--------|
| CANCELLED | 买家 |
| CONFIRMED | 卖家 |
| COMPLETED | 买家或卖家 |

### 订单查询

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/order/my | 我的订单（买入+卖出） |
| GET | /api/order/buy | 买入订单 |
| GET | /api/order/sell | 卖出订单 |
| GET | /api/order/{id} | 订单详情 |

### 支付

```
POST /api/payment/create
{ "orderId": 1 }
→ { "tradeNo": "MOCK..." }

POST /api/payment/callback
{ "tradeNo": "MOCK..." }
→ 支付成功，订单状态 → PAID
```

### 退款流程（三阶段）

**阶段 1：申请退款**
```
POST /api/payment/refund/request
{
  "orderId": 1,
  "reasonType": "QUALITY",
  "reasonDetail": "品质与描述不符",
  "images": "/uploads/1.jpg"
}
```

**阶段 2：卖家审核**
```
POST /api/payment/refund/review
{ "refundId": 1, "approved": true, "rejectReason": null }
```

**阶段 3：退款完成** — 审核通过后自动执行

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/payment/refund/{orderId} | 退款记录 |
| GET | /api/payment/refund/pending | 待审核退款 |
| GET | /api/payment/{orderId} | 支付记录 |

---

## 购物车

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/cart/add | 加入购物车 |
| GET | /api/cart/list | 购物车列表 |
| GET | /api/cart/checked | 选中项（结算用） |
| GET | /api/cart/checked/count | 选中数量 |
| PUT | /api/cart/{id} | 更新数量/选中状态 |
| PUT | /api/cart/toggle-all?checked=true | 全选/取消全选 |
| DELETE | /api/cart/{id} | 删除 |
| DELETE | /api/cart/batch | 批量删除 |
| DELETE | /api/cart/clear | 清空 |

---

## 通知服务

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/notify/list | 通知列表 |
| GET | /api/notify/unread | 未读数 |
| PUT | /api/notify/{id}/read | 标记已读 |
| PUT | /api/notify/read-all | 全部标记已读 |
| POST | /api/notify/internal/send | 内部创建通知 |
| GET | /api/notify/stream?token=xxx | SSE 实时推送 |

---

## 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "success": true
}
```

### 错误码

| Code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 / Token 过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
