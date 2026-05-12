-- Migration: Add region fields, create orders table
-- Apply after Docker restart: docker exec -i agrolink-mysql mysql -uroot -proot agrolink < sql/migration.sql

-- 1. Add province/city to products (Batch 1)
ALTER TABLE products ADD COLUMN province VARCHAR(20) DEFAULT NULL COMMENT '省' AFTER description;
ALTER TABLE products ADD COLUMN city VARCHAR(20) DEFAULT NULL COMMENT '市' AFTER province;

-- 2. Create orders table (Batch 2)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称（冗余）',
    price DECIMAL(10,2) NOT NULL COMMENT '单价（冗余）',
    info_type VARCHAR(20) NOT NULL COMMENT 'SUPPLY or DEMAND（冗余）',
    quantity INT NOT NULL DEFAULT 0 COMMENT '下单数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '总价',
    buyer_id BIGINT NOT NULL COMMENT '买家ID（商户）',
    seller_id BIGINT NOT NULL COMMENT '卖家ID（农户）',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING(待确认)/CONFIRMED(已确认)/COMPLETED(已完成)/CANCELLED(已取消)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_product_id (product_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Create favorites table (Batch 6)
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Create reviews table (Batch 6)
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    from_user_id BIGINT NOT NULL COMMENT '评价者ID',
    to_user_id BIGINT NOT NULL COMMENT '被评价者ID',
    rating INT NOT NULL DEFAULT 5 COMMENT '评分 1-5',
    content TEXT COMMENT '评价内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_to_user_id (to_user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Create addresses table (Batch 6)
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(20) NOT NULL COMMENT '省',
    city VARCHAR(20) NOT NULL COMMENT '市',
    district VARCHAR(20) DEFAULT NULL COMMENT '区',
    detail VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Create notifications table (Batch 3)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    type VARCHAR(30) NOT NULL COMMENT 'ORDER_CREATED/ORDER_CONFIRMED/ORDER_CANCELLED/ORDER_COMPLETED',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    related_id BIGINT DEFAULT NULL COMMENT '关联订单ID',
    is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== Batch 7: RBAC ====================

-- 6. Create role table
CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(20) NOT NULL UNIQUE COMMENT '角色编码 FARMER/MERCHANT/ADMIN',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称 农户/商户/管理员'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Create user_role mapping table
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Seed roles
INSERT IGNORE INTO role (id, role_code, role_name) VALUES
(1, 'FARMER', '农户'),
(2, 'MERCHANT', '商户'),
(3, 'ADMIN', '管理员');

-- ==================== 购物车 & 多图 ====================

-- 9. Create carts table
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称（冗余）',
    price DECIMAL(10,2) NOT NULL COMMENT '单价（冗余）',
    unit VARCHAR(20) DEFAULT '斤' COMMENT '单位（冗余）',
    image VARCHAR(500) DEFAULT NULL COMMENT '产品图片（冗余）',
    info_type VARCHAR(20) NOT NULL COMMENT 'SUPPLY or DEMAND（冗余）',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    checked TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否选中',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_user_checked (user_id, checked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Add images field to products (多图轮播)
ALTER TABLE products ADD COLUMN images TEXT DEFAULT NULL COMMENT '多图JSON数组（冗余，用于轮播）' AFTER image;
