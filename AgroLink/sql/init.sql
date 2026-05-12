CREATE DATABASE IF NOT EXISTS agrolink DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE agrolink;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'FARMER or MERCHANT',
    phone VARCHAR(20),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '产品名称',
    type VARCHAR(50) NOT NULL COMMENT '产品类型',
    quantity INT NOT NULL DEFAULT 0 COMMENT '数量',
    price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '价格',
    unit VARCHAR(20) DEFAULT '斤' COMMENT '单位',
    description TEXT COMMENT '描述',
    province VARCHAR(20) DEFAULT NULL COMMENT '省',
    city VARCHAR(20) DEFAULT NULL COMMENT '市',
    info_type VARCHAR(20) NOT NULL COMMENT 'SUPPLY(供应) or DEMAND(需求) — 供需类型区分',
    status VARCHAR(20) NOT NULL DEFAULT 'LISTED' COMMENT '供应: LISTED(上架)/SOLD(已售)/UNLISTED(下架); 需求: PENDING(待匹配)/COMPLETED(已成交)/CLOSED(关闭)',
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_info_type (info_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
