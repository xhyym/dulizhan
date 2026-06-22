-- =============================================
-- 独立站数据库建表脚本
-- 数据库: indie_station
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `indie_station` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `indie_station`;

-- =============================================
-- 1. 管理员表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role` VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色: SUPER-超级管理员, ADMIN-管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- =============================================
-- 2. 门户用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `whatsapp` VARCHAR(20) DEFAULT NULL COMMENT 'WhatsApp号码',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门户用户表';

-- =============================================
-- 3. 角色表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =============================================
-- 4. 菜单表 (支持动态菜单)
-- =============================================
CREATE TABLE IF NOT EXISTS `t_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID (0为顶级)',
    `path` VARCHAR(200) NOT NULL COMMENT '路由路径',
    `name` VARCHAR(100) NOT NULL COMMENT '路由名称',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `title` VARCHAR(100) NOT NULL COMMENT '菜单标题(国际化key)',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `redirect` VARCHAR(200) DEFAULT NULL COMMENT '重定向路径',
    `is_hide` TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏: 0-显示, 1-隐藏',
    `is_hide_tab` TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏标签: 0-显示, 1-隐藏',
    `keep_alive` TINYINT NOT NULL DEFAULT 1 COMMENT '是否缓存: 0-不缓存, 1-缓存',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `type` CHAR(1) NOT NULL DEFAULT 'M' COMMENT '菜单类型: M-目录, C-菜单, B-按钮',
    `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- =============================================
-- 5. 角色-菜单关联表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    KEY `idx_role_code` (`role_code`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-菜单关联表';

-- =============================================
-- 6. 商品分类表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID (0为顶级)',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- =============================================
-- 7. 商品表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `description` TEXT DEFAULT NULL COMMENT '商品简介',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '原价',
    `discount_price` DECIMAL(10,2) DEFAULT NULL COMMENT '折后价',
    `sku_code` VARCHAR(50) DEFAULT NULL COMMENT '默认SKU编码',
    `main_image` VARCHAR(500) DEFAULT NULL COMMENT '主图URL',
    `poster_image` VARCHAR(500) DEFAULT NULL COMMENT '海报图URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- =============================================
-- 8. 商品副图表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_product_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品副图表';

-- =============================================
-- 9. 商品SKU表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_product_sku` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `sku_code` VARCHAR(50) DEFAULT NULL COMMENT 'SKU编码',
    `spec_name` VARCHAR(100) DEFAULT NULL COMMENT '规格名称',
    `spec_value` VARCHAR(200) DEFAULT NULL COMMENT '规格值',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'SKU价格',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- =============================================
-- 10. 询盘订单表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_inquiry` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '询盘ID',
    `inquiry_no` VARCHAR(30) NOT NULL COMMENT '询盘编号',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户名(冗余)',
    `user_email` VARCHAR(100) DEFAULT NULL COMMENT '用户邮箱(冗余)',
    `user_whatsapp` VARCHAR(20) DEFAULT NULL COMMENT 'WhatsApp(冗余)',
    `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    `remark` TEXT DEFAULT NULL COMMENT '用户备注',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-已联系, 2-已完成, 3-已取消',
    `admin_remark` TEXT DEFAULT NULL COMMENT '管理员备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_inquiry_no` (`inquiry_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='询盘订单表';

-- =============================================
-- 11. 询盘商品明细表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_inquiry_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `inquiry_id` BIGINT NOT NULL COMMENT '询盘ID',
    `product_id` BIGINT DEFAULT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称(冗余)',
    `product_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片(冗余)',
    `sku_id` BIGINT DEFAULT NULL COMMENT 'SKU ID',
    `sku_spec` VARCHAR(200) DEFAULT NULL COMMENT 'SKU规格(冗余)',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    PRIMARY KEY (`id`),
    KEY `idx_inquiry_id` (`inquiry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='询盘商品明细表';

-- =============================================
-- 12. 系统配置表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_site_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
    `config_value` TEXT DEFAULT NULL COMMENT '配置值(JSON)',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';


-- =============================================
-- 初始数据
-- =============================================

-- 管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO `t_admin` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('superadmin', '$2a$12$DtE2ORborht8F9FunpyVCeTTI9opLVnf22F4bds2oWtHZqM9.WRia', '超级管理员', 'SUPER', 1),
('admin', '$2a$12$DtE2ORborht8F9FunpyVCeTTI9opLVnf22F4bds2oWtHZqM9.WRia', '管理员', 'ADMIN', 1);

-- 角色数据
INSERT INTO `t_role` (`role_code`, `role_name`, `sort`, `remark`) VALUES
('R_SUPER', '超级管理员', 1, '拥有所有权限'),
('R_ADMIN', '管理员', 2, '拥有部分管理权限');

-- 菜单数据 (仪表盘)
INSERT INTO `t_menu` (`id`, `parent_id`, `path`, `name`, `component`, `title`, `icon`, `sort`, `type`, `keep_alive`) VALUES
(1, 0, '/dashboard', 'Dashboard', '/index/index', 'menus.dashboard.title', 'ri:pie-chart-line', 1, 'M', 1),
(2, 1, 'console', 'Console', '/dashboard/console', 'menus.dashboard.console', NULL, 1, 'C', 1);

-- 菜单数据 (系统管理)
INSERT INTO `t_menu` (`id`, `parent_id`, `path`, `name`, `component`, `title`, `icon`, `sort`, `type`, `keep_alive`) VALUES
(3, 0, '/system', 'System', '/index/index', 'menus.system.title', 'ri:settings-3-line', 99, 'M', 1),
(4, 3, 'user', 'User', '/system/user', 'menus.system.user', NULL, 1, 'C', 1),
(5, 3, 'role', 'Role', '/system/role', 'menus.system.role', NULL, 2, 'C', 1),
(6, 3, 'menu', 'Menus', '/system/menu', 'menus.system.menu', NULL, 3, 'C', 1),
(7, 3, 'user-center', 'UserCenter', '/system/user-center', 'menus.system.userCenter', NULL, 4, 'C', 1);

-- 菜单数据 (商品管理)
INSERT INTO `t_menu` (`id`, `parent_id`, `path`, `name`, `component`, `title`, `icon`, `sort`, `type`, `keep_alive`) VALUES
(10, 0, '/product', 'Product', '/index/index', 'menus.product.title', 'ri:shopping-bag-line', 2, 'M', 1),
(11, 10, 'category', 'Category', '/product/category', 'menus.product.category', NULL, 1, 'C', 1),
(12, 10, 'list', 'ProductList', '/product/list', 'menus.product.list', NULL, 2, 'C', 1),
(13, 10, 'detail', 'ProductDetail', '/product/detail', 'menus.product.detail', NULL, 3, 'C', 1);

-- 菜单数据 (询盘管理)
INSERT INTO `t_menu` (`id`, `parent_id`, `path`, `name`, `component`, `title`, `icon`, `sort`, `type`, `keep_alive`) VALUES
(20, 0, '/inquiry', 'Inquiry', '/index/index', 'menus.inquiry.title', 'ri:file-list-3-line', 3, 'M', 1),
(21, 20, 'list', 'InquiryList', '/inquiry/list', 'menus.inquiry.list', NULL, 1, 'C', 1),
(22, 20, 'detail', 'InquiryDetail', '/inquiry/detail', 'menus.inquiry.detail', NULL, 2, 'C', 1);

-- 菜单数据 (系统设置)
INSERT INTO `t_menu` (`id`, `parent_id`, `path`, `name`, `component`, `title`, `icon`, `sort`, `type`, `keep_alive`) VALUES
(30, 0, '/site-config', 'SiteConfig', '/index/index', 'menus.siteConfig.title', 'ri:global-line', 4, 'M', 1),
(31, 30, 'basic', 'SiteBasic', '/site-config/basic', 'menus.siteConfig.basic', NULL, 1, 'C', 1),
(32, 30, 'banner', 'SiteBanner', '/site-config/banner', 'menus.siteConfig.banner', NULL, 2, 'C', 1),
(33, 30, 'footer', 'SiteFooter', '/site-config/footer', 'menus.siteConfig.footer', NULL, 3, 'C', 1);

-- 角色-菜单关联 (超级管理员拥有所有菜单)
INSERT INTO `t_role_menu` (`role_code`, `menu_id`) VALUES
('R_SUPER', 1), ('R_SUPER', 2), ('R_SUPER', 3), ('R_SUPER', 4), ('R_SUPER', 5), ('R_SUPER', 6), ('R_SUPER', 7),
('R_SUPER', 10), ('R_SUPER', 11), ('R_SUPER', 12), ('R_SUPER', 13),
('R_SUPER', 20), ('R_SUPER', 21), ('R_SUPER', 22),
('R_SUPER', 30), ('R_SUPER', 31), ('R_SUPER', 32), ('R_SUPER', 33);

-- 角色-菜单关联 (普通管理员 - 不含系统管理和菜单管理)
INSERT INTO `t_role_menu` (`role_code`, `menu_id`) VALUES
('R_ADMIN', 1), ('R_ADMIN', 2),
('R_ADMIN', 10), ('R_ADMIN', 11), ('R_ADMIN', 12), ('R_ADMIN', 13),
('R_ADMIN', 20), ('R_ADMIN', 21), ('R_ADMIN', 22),
('R_ADMIN', 30), ('R_ADMIN', 31), ('R_ADMIN', 32), ('R_ADMIN', 33);

-- 系统配置初始数据
INSERT INTO `t_site_config` (`config_key`, `config_value`, `description`) VALUES
('site_title', '"Indie Station"', '网站标题'),
('site_logo', '""', '网站Logo'),
('banner_images', '[]', '首页轮播图(JSON数组)'),
('footer_info', '{"copyright":"","links":[]}', '底部信息'),
('contact_email', '""', '联系邮箱'),
('contact_whatsapp', '""', '联系WhatsApp'),
('social_links', '{}', '社交媒体链接');
