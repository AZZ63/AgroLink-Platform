-- ==================== RBAC 权限系统迁移 ====================
-- 应用方式: docker exec -i agrolink-mysql mysql -uroot -proot agrolink < sql/rbac-migration.sql

-- 1. 用户表增加状态字段（启用/禁用）
-- 首次执行若报错列已存在可忽略，或先检查再执行：
-- SELECT COUNT(*) INTO @exists FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='agrolink' AND TABLE_NAME='users' AND COLUMN_NAME='status';
-- SET @sql = IF(@exists = 0, 'ALTER TABLE users ADD COLUMN status TINYINT DEFAULT 1 COMMENT ''用户状态 1=启用 0=禁用'' AFTER role', 'SELECT 1');
-- PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE users ADD COLUMN status TINYINT DEFAULT 1 COMMENT '用户状态 1=启用 0=禁用' AFTER role;

-- 2. 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 3. 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 4. 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    permission_code VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见 1=显示 0=隐藏',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ==================== 种子数据 ====================

-- 权限数据
INSERT IGNORE INTO sys_permission (id, permission_name, permission_code) VALUES
-- 用户管理
(1, '用户查询', 'user:list'),
(2, '用户修改', 'user:update'),
(3, '用户删除', 'user:delete'),
-- 角色管理
(4, '角色查询', 'role:list'),
(5, '角色新增', 'role:create'),
(6, '角色编辑', 'role:update'),
(7, '角色删除', 'role:delete'),
(8, '角色分配权限', 'role:assign'),
-- 菜单管理
(9, '菜单查询', 'menu:list'),
(10, '菜单新增', 'menu:create'),
(11, '菜单编辑', 'menu:update'),
(12, '菜单删除', 'menu:delete'),
-- 商品管理（管理员）
(13, '商品审核', 'product:audit'),
(14, '商品删除', 'product:delete');

-- ADMIN 角色（id=3）拥有全部权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 3, id FROM sys_permission;

-- 菜单数据（管理后台侧边栏）
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, path, component, icon, permission_code, sort_order, visible) VALUES
(1, 0, '系统管理', '', '', 'Setting', '', 1, 1),
(2, 1, '用户管理', '/admin/users', 'admin/UserManagement', 'User', 'user:list', 1, 1),
(3, 1, '角色管理', '/admin/roles', 'admin/RoleManagement', 'UserFilled', 'role:list', 2, 1),
(4, 1, '菜单管理', '/admin/menus', 'admin/MenuManagement', 'Menu', 'menu:list', 3, 1);
