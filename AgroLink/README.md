# AgroLink 农产品供需平台

农产品供需信息平台 + 交易平台，支持农户发布供应、商户发布需求、在线下单、支付退款、评价收藏等完整业务流程。

---

## 技术栈

| 层面 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.4 |
| ORM | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 (Spring Data Redis) |
| 消息队列 | RabbitMQ 3 (Spring AMQP) |
| 网关 | Spring Cloud Gateway 2023.0.1 |
| 认证 | JWT (jjwt 0.12.5) |
| 前端 | Vue 3 + Vite 5 + Element Plus + Pinia |
| 构建 | Maven + JDK 17 |

---

## 项目结构

```
AgroLink/
├── common/                  # 公共模块（DTO、工具类、全局异常处理）
│   └── src/main/java/com/agrolink/common/
│       ├── constant/        # 常量（AuthConstant, UserRole）
│       ├── dto/             # 数据传输对象
│       ├── exception/       # BusinessException, GlobalExceptionHandler
│       ├── result/          # 统一响应 Result
│       └── util/            # JwtUtil
├── user-service/            # 用户服务 (8081)
│   └── ...
│       ├── controller/      # UserController, AddressController, AdminController
│       ├── service/         # UserService, AddressService
│       ├── entity/          # User, Role, UserRole, Address
│       └── repository/      # MyBatis-Plus Mapper
├── product-service/         # 商品服务 (8082)
│   └── ...
│       ├── controller/      # ProductController, FavoriteController, ReviewController, AdminProductController, FileController
│       ├── service/         # ProductService, FavoriteService, ReviewService, ViewCounterService
│       ├── entity/          # Product, Favorite, Review
│       ├── repository/      # ProductRepository + MyBatis-Plus Mapper
│       ├── config/          # RabbitConfig (MQ inventory consumer)
│       └── mq/              # OrderCancelConsumer (库存恢复)
├── order-service/           # 订单服务 (8083)
│   └── ...
│       ├── controller/      # OrderController, PaymentController
│       ├── service/         # OrderService, PaymentService
│       ├── entity/          # Order, Payment, Refund
│       ├── repository/      # OrderRepository, PaymentRepository, RefundRepository
│       ├── config/          # RabbitConfig (DLX+TTL 延迟队列)
│       └── mq/              # OrderEventPublisher, OrderTimeoutConsumer, OrderNotifyConsumer
├── notify-service/          # 通知服务 (8084)
│   └── ...
│       ├── controller/      # NotificationController
│       ├── service/         # NotificationService
│       ├── entity/          # Notification
│       └── repository/      # NotificationRepository
├── gateway/                 # API 网关 (8080)
│   └── ...
│       └── config/          # AuthGlobalFilter, CorsConfig, GlobalErrorHandler
├── sql/
│   ├── init.sql             # 初始化建表
│   └── migration.sql        # 增量迁移（含 RBAC 等）
├── docker-compose.yml       # MySQL + Redis + RabbitMQ
├── pom.xml                  # 父 POM（模块管理）
└── README.md                # 本文件

AgroLink-Frontend/
└── src/
    ├── components/          # NavBar.vue
    ├── views/               # 页面组件
    ├── stores/              # Pinia 状态管理（user.js）
    ├── router/              # 路由配置
    └── api/                 # HTTP 客户端
```

---

## 启动指南

### 前置条件

- JDK 17
- MySQL 8.0（本地端口 3306，密码 123456）
- Redis（Windows 服务或 Docker）
- Docker Desktop（运行 RabbitMQ）
- Node.js 18+

### 1. 初始化数据库

```bash
mysql -uroot -p123456 < sql/init.sql
mysql -uroot -p123456 < sql/migration.sql
```

### 2. 启动基础设施

```bash
# MySQL
net start MySQL80

# Redis
net start Redis

# RabbitMQ
docker run -d --name agrolink-rabbitmq -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=agrolink -e RABBITMQ_DEFAULT_PASS=agrolink123 \
  rabbitmq:3-management
```

验证：
```bash
redis-cli ping          # → PONG
curl -I localhost:15672 # → 200 OK
```

### 3. 构建 & 启动后端

```bash
# 编译打包
mvn package -DskipTests -q

# 按顺序启动（等前一个 Ready 再启动下一个）
java -jar user-service/target/user-service-1.0.0.jar         # 8081
java -jar product-service/target/product-service-1.0.0.jar   # 8082
java -jar order-service/target/order-service-1.0.0.jar       # 8083
java -jar notify-service/target/notify-service-1.0.0.jar     # 8084
java -jar gateway/target/gateway-1.0.0.jar                   # 8080
```

### 4. 启动前端

```bash
cd ../AgroLink-Frontend
npm install
npm run dev
# → http://localhost:5173
```

---

## 服务端口一览

| 服务 | 端口 | 说明 |
|------|------|------|
| Gateway | 8080 | API 网关（统一入口） |
| user-service | 8081 | 用户认证、地址、管理 |
| product-service | 8082 | 商品 CRUD、收藏、评价 |
| order-service | 8083 | 订单、支付、退款 |
| notify-service | 8084 | 站内通知 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 / 15672 | 消息队列 / 管理台 |

### 访问地址

| 页面 | URL |
|------|-----|
| 前端首页 | http://localhost:5173 |
| API 网关 | http://localhost:8080 |
| RabbitMQ 管理 | http://localhost:15672 (agrolink/agrolink123) |

---

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| farmer2 | test123 | FARMER | 农户，可发布供应信息 |
| merchant2 | test123 | MERCHANT | 商户，可发布需求、下单 |
| admin | admin123 | ADMIN | 管理员，可管理用户/商品 |

---

## API 概览

所有请求通过网关 `http://localhost:8080` 访问，需在 Header 携带 `Authorization: Bearer <token>`。

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/login` | 登录 `{username, password}` |
| POST | `/api/user/register` | 注册 `{username, password, role}` |
| POST | `/api/user/refresh` | 刷新 token `{refreshToken}` |
| POST | `/api/user/logout` | 登出 |

### 商品

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/product/create` | 发布商品/需求 |
| POST | `/api/product/query` | 分页查询（Redis 缓存） |
| GET | `/api/product/hot` | 热门商品（Redis 缓存 5min） |
| GET | `/api/product/{id}` | 商品详情（Redis 缓存 30min） |
| PUT | `/api/product/update` | 更新商品 |
| PUT | `/api/product/{id}/status` | 更新状态 |
| DELETE | `/api/product/{id}` | 删除商品 |
| POST | `/api/product/{id}/view` | 增加浏览量 |
| GET | `/api/product/types` | 商品分类列表 |
| GET | `/api/product/my` | 我的发布 |
| POST | `/api/product/{id}/favorite` | 收藏/取消收藏 |
| GET | `/api/product/favorites` | 收藏列表 |
| POST | `/api/product/{id}/review` | 评价商品 |
| GET | `/api/product/{id}/reviews` | 商品评价列表 |

### 订单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/order/create` | 创建订单（商户 → 供应）→ 发延迟取消消息 |
| PUT | `/api/order/{id}/status` | 更新订单状态 |
| GET | `/api/order/{id}` | 订单详情 |
| GET | `/api/order/my` | 我的订单 |
| GET | `/api/order/buy` | 采购订单 |
| GET | `/api/order/sell` | 销售订单 |
| POST | `/api/payment/create` | 创建支付 |
| POST | `/api/payment/success` | 支付成功回调 |
| POST | `/api/payment/refund/request` | 申请退款 |
| POST | `/api/payment/refund/review` | 审核退款 |

### 通知

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/notify/list` | 通知列表 |
| PUT | `/api/notify/{id}/read` | 标记已读 |
| GET | `/api/notify/unread-count` | 未读数量 |

### 地址

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/address/create` | 新增地址 |
| GET | `/api/address/list` | 地址列表 |
| PUT | `/api/address/update` | 更新地址 |
| DELETE | `/api/address/{id}` | 删除地址 |

### 管理后台

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 用户列表（ADMIN） |
| PUT | `/api/admin/user/{id}/role` | 修改用户角色 |
| DELETE | `/api/admin/user/{id}` | 删除用户 |
| GET | `/api/admin/products` | 商品列表（含未审核） |
| PUT | `/api/admin/product/{id}/status` | 强制修改商品状态 |

---

## 特性清单

### Batch 1-5（基础功能）

- [x] 用户注册/登录（JWT + Token 刷新）
- [x] 商品发布（供应/需求两种类型）
- [x] 商品搜索 + 分页 + 筛选 + 排序
- [x] 商品状态机（上架/下架/已售 / 待匹配/已完成/关闭）
- [x] 订单创建 + 状态流转（UNPAID→PAID→CONFIRMED→COMPLETED）
- [x] 站内通知系统
- [x] 权限精细化（角色路由守卫 + API 权限校验）

### Batch 6（增强功能）

- [x] 商品收藏（toggle + 收藏列表）
- [x] 评价系统（订单完成后双方互评）
- [x] 收货地址 CRUD + 默认地址
- [x] 管理后台（用户管理 + 商品审核）
- [x] "记住密码"（localStorage/sessionStorage 切换）

### Redis 缓存体系

- [x] **商品详情缓存** — `getProductById`，TTL 30min + 随机抖动
- [x] **热门商品缓存** — `getHotProducts`，TTL 5min
- [x] **分页查询缓存** — `queryProducts`，TTL 2min + 随机抖动，MD5 cache key
- [x] **防缓存穿透** — 不存在的 ID 缓存空值标记，TTL 5min
- [x] **防缓存击穿** — SETNX 互斥锁（3s TTL），等待线程重试
- [x] **缓存失效** — 更新/删除商品时主动清除详情 + 列表缓存
- [x] **浏览量计数** — Redis 自增 + 定时（60s）同步 MySQL

### RabbitMQ 消息队列

- [x] **订单超时取消** — TTL+DLX 延迟队列，下单 30min 自动取消
- [x] **异步解耦** — 订单取消事件通过 MQ 推送，替代直连 REST
- [x] **库存恢复** — MQ 消费者自动恢复商品状态为 LISTED
- [x] **消息可靠性** — Publisher Confirm + Manual ACK + 重试 3 次
- [x] **通知推送** — MQ 触发站内通知发送

### 支付与退款（企业级）

- [x] 支付记录（Payment）
- [x] 退款工作流（申请 → 审核 → 打款/拒绝）
- [x] 退款原因分类（品质/数量/发错/其他）
- [x] 退款图片凭证

---

## 订单状态机

```
UNPAID ──→ PAID ──→ CONFIRMED ──→ COMPLETED
   │         │          │
   │         │          └── REFUNDING ──→ REFUNDED
   │         │
   │         └── REFUNDING ──→ REFUNDED
   │
   └── CANCELLED
   ↑ (超时 30min 自动触发)
```

## 商品状态机

```
SUPPLY: LISTED ──→ SOLD
          │
          └── UNLISTED ──→ LISTED

DEMAND: PENDING ──→ COMPLETED
           │
           └── CLOSED ──→ PENDING
```

---

## Redis 缓存架构

```
┌─ 请求 ──────────────────────────────────────────┐
│                                                  │
│   getProductById(id)                             │
│     ├─ Cache hit → 返回 ProductDTO               │
│     ├─ Null marker hit → 直接 404（防穿透）       │
│     └─ Miss → SETNX 互斥锁（防击穿）              │
│                ├─ 获锁 → DB 查询 → 写缓存 → 返回   │
│                └─ 未获锁 → 等待 100ms → 重试       │
│                                                  │
│   queryProducts(filter)                          │
│     ├─ Cache hit → 返回 PageResult<ProductDTO>    │
│     └─ Miss → DB 查询 → 写缓存（key = MD5(参数)） │
│                                                  │
│   createProduct / updateProduct / deleteProduct   │
│     └─ 清除详情缓存 + 列表缓存                     │
└──────────────────────────────────────────────────┘
```

## RabbitMQ 消息架构

```
Order Created (UNPAID)
  └─ Publish → agrolink.order.topic ["order.created"]
                      │
              ┌───────┴────────┐
              │ 延迟队列        │  (TTL=30min, DLX=agrolink.order.dlx)
              │ (delay.queue)  │
              │               │
         30分钟后到期 ──────────┼─→ DLX → ["order.timeout"] → cancel.queue
              │               │
              ▼               ▼
          (订单已支付?      OrderTimeoutConsumer
           不处理)          ├─ UNPAID → 取消 → 发 order.cancelled
                            │
                            ▼
                   agrolink.order.topic ["order.cancelled"]
                      │              │
                      ▼              ▼
               notify.queue    inventory.queue
                      │              │
               OrderNotify    OrderCancelConsumer
               (发站内通知)    (product-service 恢复库存)
```

---

## 配置参考

### application.yml 关键配置

```yaml
# Redis（各服务通用）
spring.data.redis:
  host: localhost
  port: 6379

# RabbitMQ（order-service / product-service）
spring.rabbitmq:
  host: localhost
  port: 5672
  username: agrolink
  password: agrolink123
  publisher-confirm-type: correlated
  listener.simple:
    acknowledge-mode: manual
    retry:
      enabled: true
      max-attempts: 3
      initial-interval: 1000
```

### docker-compose.yml

```yaml
services:
  mysql:     # 本地 3307:3306（本地开发用 3306 直连）
  redis:     # 6379:6379
  rabbitmq:  # 5672:5672, 15672:15672（管理台）
```

实际开发中 MySQL 使用本地安装（端口 3306，密码 123456），Docker 中为 3307 端口。

---

## 开发说明

### 新增数据库表

1. 在 `sql/migration.sql` 追加 DDL
2. 在对应模块创建 Entity 类（MyBatis-Plus @TableName）
3. 创建 Repository（继承 BaseMapper）
4. 创建 Service 层
5. 创建 Controller + DTO

### 新增 MQ 消费者

1. 在 `RabbitConfig.java` 定义 Queue + Binding
2. 在 `mq/` 包下创建 `@RabbitListener` 类
3. 手动 ACK，异常时 basicNack
