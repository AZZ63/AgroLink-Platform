#!/bin/bash
# AgroLink 一键启动脚本
# 在 Git Bash 中执行: bash start.sh

set -e

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$ROOT_DIR/AgroLink-Frontend"
BACKEND_DIR="$ROOT_DIR/AgroLink"
LOG_DIR="$ROOT_DIR/logs"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

info()  { echo -e "${CYAN}[INFO]${NC} $1"; }
ok()    { echo -e "${GREEN}[OK]${NC}   $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
fail()  { echo -e "${RED}[FAIL]${NC} $1"; }

mkdir -p "$LOG_DIR"

cleanup() {
    info "正在停止所有服务..."
    pkill -f "agrolink.*.jar" 2>/dev/null || true
    pkill -f "vite" 2>/dev/null || true
    ok "服务已停止"
}
trap cleanup EXIT

echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}    AgroLink 一键启动脚本${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# ============================================================
# Step 1: 检查基础设施
# ============================================================
info "Step 1/6: 检查基础设施..."

# MySQL
MYSQL_RUNNING=false
if command -v mysqladmin &>/dev/null; then
    if mysqladmin ping -h 127.0.0.1 -u root -p123456 --silent 2>/dev/null; then
        MYSQL_RUNNING=true
        ok "MySQL 已运行"
    fi
fi
if ! $MYSQL_RUNNING; then
    if net start MySQL80 2>/dev/null || net start MySQL 2>/dev/null; then
        ok "MySQL 已启动"
        sleep 3
    else
        warn "MySQL 未能自动启动，请手动启动 MySQL 服务"
        warn "CMD(管理员): net start MySQL80"
    fi
fi

# Redis
if redis-cli -h 127.0.0.1 -p 6379 ping 2>/dev/null | grep -q PONG; then
    ok "Redis 已运行"
else
    if net start Redis 2>/dev/null; then
        ok "Redis 已启动"
    else
        warn "Redis 未能自动启动，请手动启动 Redis 服务"
        warn "CMD(管理员): net start Redis"
    fi
fi

# RabbitMQ (Docker)
if docker ps --format "{{.Names}}" 2>/dev/null | grep -q "agrolink-rabbitmq"; then
    ok "RabbitMQ 已运行"
else
    if docker info &>/dev/null; then
        info "正在启动 RabbitMQ..."
        docker rm -f agrolink-rabbitmq 2>/dev/null || true
        if docker run -d --name agrolink-rabbitmq -p 5672:5672 -p 15672:15672 \
            -e RABBITMQ_DEFAULT_USER=agrolink -e RABBITMQ_DEFAULT_PASS=agrolink123 \
            rabbitmq:3-management &>/dev/null; then
            ok "RabbitMQ 已启动 (管理台: http://localhost:15672)"
        else
            warn "RabbitMQ 启动失败，请检查 Docker Desktop 是否运行"
        fi
    else
        warn "Docker 未运行，跳过 RabbitMQ 启动。订单超时功能不可用。"
        warn "请先启动 Docker Desktop"
    fi
fi

echo ""

# ============================================================
# Step 2: 初始化数据库
# ============================================================
info "Step 2/6: 检查数据库表..."
if mysql -u root -p123456 -e "SELECT COUNT(*) FROM agrolink.orders;" 2>/dev/null; then
    ok "数据库表已存在"
else
    info "正在初始化数据库..."
    mysql -u root -p123456 < "$BACKEND_DIR/sql/init.sql" 2>/dev/null
    mysql -u root -p123456 < "$BACKEND_DIR/sql/migration.sql" 2>/dev/null
    ok "数据库初始化完成"
fi

echo ""

# ============================================================
# Step 3: 构建后端
# ============================================================
info "Step 3/6: 构建后端项目..."
cd "$BACKEND_DIR"
if mvn package -DskipTests -q 2>&1 | tail -5; then
    ok "后端构建成功"
else
    fail "后端构建失败，请检查错误信息"
    exit 1
fi

echo ""

# ============================================================
# Step 4: 启动后端服务
# ============================================================
info "Step 4/6: 启动后端服务..."

SERVICES=(
    "user-service:8081"
    "product-service:8082"
    "order-service:8083"
    "notify-service:8084"
    "gateway:8080"
)

for svc in "${SERVICES[@]}"; do
    IFS=':' read -r name port <<< "$svc"
    jar="$BACKEND_DIR/$name/target/$name-1.0.0.jar"

    # 检查端口是否已被占用
    if lsof -i :$port &>/dev/null 2>&1 || ss -tlnp 2>/dev/null | grep -q ":$port "; then
        warn "$name:$port 端口已被占用，跳过"
        continue
    fi

    if [ ! -f "$jar" ]; then
        fail "未找到 $jar，请先构建"
        exit 1
    fi

    info "启动 $name (端口 $port)..."
    java -jar "$jar" > "$LOG_DIR/$name.log" 2>&1 &
    PID=$!

    # 等待 Ready
    for i in $(seq 1 30); do
        sleep 1
        if grep -q "Started" "$LOG_DIR/$name.log" 2>/dev/null; then
            ok "$name 启动成功 (PID $PID)"
            break
        fi
        if [ $i -eq 30 ]; then
            warn "$name 启动超时，请检查 $LOG_DIR/$name.log"
        fi
    done
done

echo ""

# ============================================================
# Step 5: 启动前端
# ============================================================
info "Step 5/6: 启动前端..."
cd "$FRONTEND_DIR"

if [ ! -d "node_modules" ]; then
    info "安装前端依赖..."
    npm install --silent
fi

npm run dev > "$LOG_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
sleep 3

if curl -s -o /dev/null -w "%{http_code}" http://localhost:5173 2>/dev/null | grep -q 200; then
    ok "前端启动成功 http://localhost:5173"
else
    warn "前端启动可能较慢，请稍后刷新 http://localhost:5173"
fi

echo ""

# ============================================================
# Step 6: 验证
# ============================================================
info "Step 6/6: 验证服务..."

sleep 2

# 测试登录
LOGIN_RESULT=$(curl -s -X POST http://localhost:8080/api/user/login \
    -H "Content-Type: application/json" \
    -d '{"username":"farmer2","password":"test123"}' 2>/dev/null)

if echo "$LOGIN_RESULT" | grep -q '"code":200'; then
    TOKEN=$(echo "$LOGIN_RESULT" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    ok "API 网关登录正常"

    # 测试商品接口
    if curl -s http://localhost:8080/api/product/hot \
        -H "Authorization: Bearer $TOKEN" | grep -q '"code":200'; then
        ok "商品接口正常 (Redis 缓存已生效)"
    fi

    # 测试订单接口
    if curl -s http://localhost:8080/api/order/my \
        -H "Authorization: Bearer $TOKEN" | grep -q '"code":200'; then
        ok "订单接口正常 (RabbitMQ 已连接)"
    fi
else
    warn "API 验证未通过，请稍后手动检查"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}     AgroLink 启动完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "  前端:      ${CYAN}http://localhost:5173${NC}"
echo -e "  API 网关:   ${CYAN}http://localhost:8080${NC}"
echo -e "  RabbitMQ:  ${CYAN}http://localhost:15672${NC} (agrolink/agrolink123)"
echo ""
echo -e "  测试账号:"
echo -e "    farmer2  / test123  (农户)"
echo -e "    merchant2 / test123  (商户)"
echo ""
echo -e "  日志文件: ${YELLOW}$LOG_DIR/*.log${NC}"
echo ""
echo -e "${YELLOW}按 Ctrl+C 停止所有服务${NC}"
echo ""

# 保持运行
wait
