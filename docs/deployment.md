# 部署指南

## 目录

- [Docker 部署（推荐）](#docker-部署推荐)
- [本地开发部署](#本地开发部署)
- [环境变量](#环境变量)
- [CI/CD](#cicd)
- [性能优化](#性能优化)
- [运维](#运维)

---

## Docker 部署（推荐）

生产环境使用 Docker Compose 一键部署全部服务。

### 前置条件

- Docker Engine 24+ / Docker Desktop 4.x+
- Docker Compose v2+

### 1. 克隆项目

```bash
git clone <repository-url>
cd OPENCLAW/AgroLink
```

### 2. 一键启动全部服务

```bash
docker compose up -d
```

这会按依赖顺序启动以下容器：

| 容器 | 端口 | 依赖 |
|------|------|------|
| mysql | 3307 | — |
| redis | 6379 | — |
| rabbitmq | 5672, 15672 | — |
| user-service | 8081 | mysql, redis |
| product-service | 8082 | mysql, redis |
| order-service | 8083 | mysql, redis, rabbitmq |
| notify-service | 8084 | mysql |
| gateway | 8080 | 全部后端服务 |
| frontend (nginx) | 80 | gateway |

### 3. 初始化数据库

首次启动后需导入数据：

```bash
docker exec -i agrolink-mysql mysql -uroot -proot agrolink < sql/init.sql
docker exec -i agrolink-mysql mysql -uroot -proot agrolink < sql/migration.sql
```

### 4. 访问

- 前端：http://localhost
- API 网关：http://localhost:8080
- RabbitMQ 管理台：http://localhost:15672 (agrolink / agrolink123)

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 农户 | farmer2 | test123 |
| 商户 | merchant2 | test123 |

### 常用 Docker 命令

```bash
# 查看所有容器状态
docker compose ps

# 查看日志（所有服务）
docker compose logs -f

# 查看单个服务日志
docker compose logs -f order-service

# 重启单个服务
docker compose restart order-service

# 重新构建并启动（代码变更后）
docker compose up -d --build order-service

# 停止所有服务
docker compose down

# 停止并删除数据卷（清理数据库）
docker compose down -v
```

---

## 本地开发部署

### 数据库

```bash
# 使用 Docker MySQL
docker compose up -d mysql redis rabbitmq

# 或使用本地 MySQL
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS agrolink DEFAULT CHARACTER SET utf8mb4"
mysql -u root -p agrolink < sql/init.sql
mysql -u root -p agrolink < sql/migration.sql
```

### 后端

```bash
cd AgroLink

# 编译
mvn clean package -DskipTests

# 按顺序启动
java -jar user-service/target/user-service-1.0.0.jar &
sleep 15
java -jar product-service/target/product-service-1.0.0.jar &
sleep 12
java -jar order-service/target/order-service-1.0.0.jar &
sleep 12
java -jar notify-service/target/notify-service-1.0.0.jar &
sleep 10
java -jar gateway/target/gateway-1.0.0.jar &
```

### 前端

```bash
cd AgroLink-Frontend
npm install
npm run dev
```

访问 http://localhost:5173

---

## 环境变量

### 数据库（MySQL）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agrolink
    username: root
    password: 123456
```

### Redis

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### RabbitMQ

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: agrolink
    password: agrolink123
```

### Docker 环境

各服务的 `application-docker.yml` 会自动覆盖为容器内连接地址。Docker 部署时由 `SPRING_PROFILES_ACTIVE=docker` 环境变量激活。

---

## CI/CD

项目提供 GitHub Actions 工作流，支持自动构建和推送 Docker 镜像。

### 工作流文件

```yaml
# .github/workflows/deploy.yml
name: Build and Deploy

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build backend
        run: |
          cd AgroLink
          mvn clean package -DskipTests

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 20

      - name: Build frontend
        run: |
          cd AgroLink-Frontend
          npm ci
          npm run build

      - name: Build and push Docker images
        run: |
          docker compose -f AgroLink/docker-compose.yml build
```

---

## 性能优化

### 后端

| 优化项 | 实现方式 |
|--------|----------|
| 缓存 | Redis 缓存产品详情、热门产品、查询结果 |
| 缓存穿透防护 | 空值标记 + 短 TTL |
| 缓存雪崩防护 | 互斥锁 + 随机过期时间 |
| 数据库索引 | 用户、类型、状态、创建时间均有索引 |
| 浏览量异步 | Redis 原子计数，定时批量写入 DB |

### 前端

| 优化项 | 实现方式 |
|--------|----------|
| 路由懒加载 | 所有页面使用动态 import |
| 骨架屏 | 列表页加载时显示 skeleton |
| API 请求去重 | Token 刷新期间的请求排队 |
| 响应式图片 | 图片懒加载 |

---

## 运维

### 查看日志

```bash
# Docker 环境
docker compose logs -f

# 单个服务
docker compose logs -f order-service

# 本地部署 - 日志在 logs/ 目录
tail -f logs/user-service.log
tail -f logs/product-service.log
tail -f logs/order-service.log
tail -f logs/notify-service.log
tail -f logs/gateway.log
tail -f logs/frontend.log
```

### 健康检查

```bash
# 查看所有容器状态
docker compose ps

# 测试 API
curl http://localhost:8080/api/user/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"farmer2","password":"test123"}'

# 检查数据库
docker exec agrolink-mysql mysqladmin ping -uroot -proot

# 检查 Redis
docker exec agrolink-redis redis-cli ping

# 检查 RabbitMQ
curl http://localhost:15672/api/health/checks/alarms
```

### 数据库迁移

```bash
docker exec -i agrolink-mysql mysql -uroot -proot agrolink < sql/migration.sql
```

### 更新服务

```bash
# 拉取最新代码
git pull

# 重新构建并启动变更的服务
docker compose up -d --build

# 或只重建单个服务
docker compose up -d --build order-service
```

### 常见问题

| 问题 | 解决方案 |
|------|----------|
| 端口被占用 | 修改 docker-compose.yml 中的映射端口 |
| MySQL 连接拒绝 | `docker compose ps` 确认 mysql 容器健康 |
| 前端 API 报 401 | 重新登录获取 token |
| 订单状态不更新 | 检查 rabbitmq 容器是否运行 |
| 缓存数据陈旧 | `docker exec agrolink-redis redis-cli FLUSHDB` |
| 前端页面空白 | 检查 frontend 容器日志 `docker compose logs frontend` |
