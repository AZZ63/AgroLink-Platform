# 系统架构

## 整体架构

```mermaid
graph TB
    Client[浏览器] --> GW[Gateway :8080]
    
    subgraph 网关层
        GW --> |路由| Auth[AuthGlobalFilter]
        Auth --> |JWT 校验| Router[路由分发]
    end
    
    subgraph 微服务层
        Router --> US[user-service :8081]
        Router --> PS[product-service :8082]
        Router --> OS[order-service :8083]
        Router --> NS[notify-service :8084]
    end
    
    subgraph 数据层
        US --> MySQL[(MySQL)]
        PS --> MySQL
        OS --> MySQL
        NS --> MySQL
        PS --> Redis[(Redis)]
    end
    
    subgraph 消息层
        OS --> RMQ[RabbitMQ]
        PS --> RMQ
        RMQ --> |延迟队列| OS
        RMQ --> |取消订单| PS
    end
    
    subgraph 实时推送
        NS --> SSE[SSE EventStream]
        SSE --> Client
    end
    
    subgraph 前端
        Client --> Vite[Vite Dev Server :5173]
        Vite --> |代理 /api| GW
    end
```

## 微服务职责

| 服务 | 端口 | 职责 |
|------|------|------|
| **gateway** | 8080 | 路由转发、JWT 鉴权、角色校验、CORS |
| **user-service** | 8081 | 用户注册/登录、地址管理、RBAC 权限 |
| **product-service** | 8082 | 商品 CRUD、多条件搜索、收藏、评价、文件上传 |
| **order-service** | 8083 | 订单流转、支付模拟、退款流程、购物车 |
| **notify-service** | 8084 | 通知创建/查询/已读、SSE 实时推送 |

## 数据流

### 交易流程
```mermaid
sequenceDiagram
    participant M as 商户
    participant F as 农户
    participant GW as 网关
    participant PS as 商品服务
    participant OS as 订单服务
    participant NS as 通知服务
    
    F->>PS: 发布商品
    M->>PS: 查询商品
    M->>OS: 创建订单
    OS->>NS: 通知卖家
    NS-->>F: SSE 实时推送
    M->>OS: 模拟支付
    OS->>NS: 通知已付款
    F->>OS: 确认订单
    M->>OS: 完成交易
    M->>PS: 评价
```

### 认证流程
```mermaid
sequenceDiagram
    participant C as 客户端
    participant GW as 网关
    participant US as 用户服务
    
    C->>GW: POST /api/user/login
    GW->>US: 转发登录请求
    US-->>GW: JWT Token
    GW-->>C: token + refreshToken
    
    Note over C,GW: 后续请求带 Authorization: Bearer token
    
    C->>GW: GET /api/product/hot
    GW->>GW: 校验 JWT 签名
    GW->>GW: 提取 userId/role
    GW->>GW: 角色权限校验
    GW->>PS: 透传 X-User-Id header
    PS-->>C: 响应数据
```

## 数据库 ER 图

```mermaid
erDiagram
    users ||--o{ products : "发布"
    users ||--o{ orders : "买家/卖家"
    users ||--o{ notifications : "接收"
    products ||--o{ orders : "包含"
    orders ||--o{ payments : "关联"
    orders ||--o{ refunds : "关联"
    orders ||--o{ reviews : "评价"
    users ||--o{ favorites : "收藏"
    products ||--o{ favorites : "被收藏"
    users ||--o{ addresses : "管理"
    users ||--o{ carts : "购物车"
    products ||--o{ carts : "加入"

    users {
        bigint id PK
        varchar username
        varchar password
        varchar role
        varchar phone
    }

    products {
        bigint id PK
        varchar name
        varchar type
        int quantity
        decimal price
        varchar unit
        varchar info_type "SUPPLY | DEMAND"
        varchar status
        bigint user_id FK
        varchar image
        text images
        int view_count
    }

    orders {
        bigint id PK
        bigint product_id FK
        varchar product_name
        int quantity
        decimal total_price
        bigint buyer_id FK
        bigint seller_id FK
        varchar status
        varchar pay_status
        decimal pay_amount
    }

    carts {
        bigint id PK
        bigint user_id FK
        bigint product_id FK
        varchar product_name
        decimal price
        int quantity
        tinyint checked
    }

    notifications {
        bigint id PK
        bigint user_id FK
        varchar type
        varchar title
        text content
        tinyint is_read
        bigint related_id
    }
```

## 缓存策略

| 数据 | 策略 | TTL |
|------|------|-----|
| 产品详情 | 三级缓存（缓存 → 空值标记 → 互斥锁） | 30min |
| 热门产品 | Redis List | 5min |
| 产品类型 | Redis Set | 1h |
| 查询结果 | Redis String | 120s |
| 浏览量 | Redis 原子计数，定时落库 | - |

## 消息队列

| 队列 | 用途 | 类型 |
|------|------|------|
| order.delay | 订单超时取消 | 延迟队列 (DLX) |
| order.cancel | 取消订单后恢复库存 | 普通队列 |
| order.notify | 订单状态变更通知 | 普通队列 |
