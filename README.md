# AgroLink · 农产品供需交易平台

连接农户与采购商，打造透明农产品交易平台。

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## 项目简介

AgroLink 是一个面向农产品供需双方的 B2B 交易平台。农户可以发布供应信息，商户可以浏览、采购、下单，平台提供完整的交易闭环：商品管理 → 购物车 → 订单 → 支付 → 退款 → 评价。

## 功能特性

### 核心交易
- **商品管理** — 农户发布供应 / 商户发布需求，支持多图上传、分类筛选
- **购物车** — 加入、修改数量、删除、勾选结算
- **订单系统** — 下单 → 支付 → 确认 → 完成 → 退款全流程
- **支付模拟** — 模拟支付回调，完整交易状态流转

### 平台能力
- **实时通知** — SSE 推送订单状态变更、未读消息红点
- **数据看板** — Chart.js 图表展示销量趋势、产品分布、浏览量
- **收藏夹** — 商品收藏与取消收藏
- **评价系统** — 订单完成后评分与评价
- **搜索筛选** — 多维度条件查询 + 搜索历史 + 热门搜索

### 运营功能
- **平台公告** — 首页滚动公告栏
- **热门排行** — 热门产品展示
- **分类入口** — 快捷分类导航
- **商户信息卡** — 卖家信息展示

### 安全体系
- **JWT 认证** — Token 鉴权 + 自动刷新
- **RBAC** — 农户 / 商户 / 管理员 三级权限
- **网关鉴权** — 统一认证过滤，防止越权访问
- **数据隔离** — 用户只能操作自己的数据

## 技术栈

### 后端
| 组件 | 技术 |
|------|------|
| 框架 | Spring Boot 3.2 |
| 架构 | Spring Cloud Gateway 微服务 |
| ORM | MyBatis-Plus 3.5 |
| 缓存 | Redis |
| 消息 | RabbitMQ (延迟队列) |
| 数据库 | MySQL 8.0 |
| 鉴权 | JWT (jjwt) |

### 前端
| 组件 | 技术 |
|------|------|
| 框架 | Vue 3 (Composition API) |
| UI | Element Plus |
| 状态管理 | Pinia |
| 路由 | Vue Router |
| 图表 | Chart.js |
| 构建 | Vite |

### 微服务架构
```
gateway (8080)
├── user-service (8081) — 用户、地址、RBAC
├── product-service (8082) — 商品、收藏、评价、文件
├── order-service (8083) — 订单、支付、退款、购物车
└── notify-service (8084) — 通知、SSE 实时推送
```

## 快速启动

### 前置条件
- Docker Engine 24+ / Docker Desktop 4.x

### 1. 一键启动全部服务
```bash
cd AgroLink
docker compose up -d
```

### 2. 初始化数据库（首次）
```bash
docker exec -i agrolink-mysql mysql -uroot -proot agrolink < sql/init.sql
docker exec -i agrolink-mysql mysql -uroot -proot agrolink < sql/migration.sql
```

### 3. 访问
- 前端：http://localhost
- API 网关：http://localhost:8080

代码变更后重建单个服务：
```bash
docker compose up -d --build order-service
```

### 测试账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 农户 | farmer2 | test123 |
| 商户 | merchant2 | test123 |

## 项目结构

```
AgroLink/
├── common/              # 公共模块（DTO、工具类、常量）
├── gateway/             # API 网关（路由、鉴权）
├── user-service/        # 用户服务
├── product-service/     # 商品服务
├── order-service/       # 订单服务
├── notify-service/      # 通知服务
├── sql/                 # 数据库脚本
└── docker-compose.yml   # 容器编排

AgroLink-Frontend/
├── src/
│   ├── api/             # HTTP 请求封装
│   ├── components/      # 公共组件
│   ├── router/          # 路由配置
│   ├── stores/          # Pinia 状态管理
│   ├── utils/           # 工具函数
│   └── views/           # 页面组件
```

## 交易流程

```
农户发布商品 → 商户浏览 → 加入购物车 → 下单
    ↓                                              ↓
商户确认收货 ← 农户确认订单 ← 模拟支付 ← 商户结算
    ↓
评价完成
```

## API 概览

详细 API 文档参见 [docs/api.md](docs/api.md)。

## 部署

详细部署指南参见 [docs/deployment.md](docs/deployment.md)。

## 架构

系统架构图参见 [docs/architecture.md](docs/architecture.md)。
