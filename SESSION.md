# AgroLink 开发进度

## 最后会话时间
2026-05-09

## 已完成功能

### Batch 1：搜索推荐增强 ✅
- 产品加 province/city 地区字段
- 排序（价格/时间）、地区筛选
- Redis 热点缓存（JSON + 5分钟 TTL）
- 前端骨架屏、路由守卫、空状态引导

### Batch 2：订单服务 ✅
- order-service 模块 (8083)
- 下单→确认→完成→取消，状态流转锁死
- 冗余产品字段防数据丢失
- 权限校验（仅商户下单、仅卖家确认）

### Batch 3：消息通知 ✅
- notify-service 模块 (8084)
- 站内信：订单创建/确认/完成/取消自动推送

### Batch 4：权限精细化 ✅
- 农户→只能发 SUPPLY，商户→只能发 DEMAND
- 后端全接口角色校验

### Batch 5：数据统计 ✅
- 浏览量 Redis 异步计数，每分钟同步 MySQL

## 启动方式

```bash
# Docker (MySQL:3307, Redis:6379)
cd D:\OPENCLAW\AgroLink && docker compose up -d mysql redis

# 后端
cd D:\OPENCLAW\AgroLink
java -jar user-service/target/user-service-1.0.0.jar
java -jar product-service/target/product-service-1.0.0.jar
java -jar order-service/target/order-service-1.0.0.jar
java -jar notify-service/target/notify-service-1.0.0.jar
java -jar gateway/target/gateway-1.0.0.jar

# 前端
cd D:\OPENCLAW\AgroLink-Frontend && npm run dev
```

## 测试账号
- farmer2 / test123（农户）
- merchant2 / test123（商户）
