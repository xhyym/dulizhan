-- =============================================
-- 门户网站测试数据（家具桌凳）
-- 请先执行 schema.sql 建表，再执行本文件
-- =============================================

USE `indie_station`;

-- 清空旧测试数据（按外键依赖顺序）
DELETE FROM `t_inquiry_item`;
DELETE FROM `t_inquiry`;
DELETE FROM `t_cart`;
DELETE FROM `t_product_sku`;
DELETE FROM `t_product_image`;
DELETE FROM `t_product`;
DELETE FROM `t_category`;
DELETE FROM `t_user`;

-- 重置自增ID
ALTER TABLE `t_inquiry_item` AUTO_INCREMENT = 1;
ALTER TABLE `t_inquiry` AUTO_INCREMENT = 1;
ALTER TABLE `t_cart` AUTO_INCREMENT = 1;
ALTER TABLE `t_product_sku` AUTO_INCREMENT = 1;
ALTER TABLE `t_product_image` AUTO_INCREMENT = 1;
ALTER TABLE `t_product` AUTO_INCREMENT = 1;
ALTER TABLE `t_category` AUTO_INCREMENT = 1;
ALTER TABLE `t_user` AUTO_INCREMENT = 1;

-- =============================================
-- 1. 门户用户（客户）
-- =============================================
INSERT INTO `t_user` (`id`, `username`, `email`, `whatsapp`, `avatar`, `status`) VALUES
(1, 'John Smith', 'john.smith@example.com', '+14155551234', NULL, 1),
(2, 'Emily Johnson', 'emily.j@example.com', '+447911123456', NULL, 1),
(3, 'Michael Chen', 'michael.c@example.com', '+8613800138000', NULL, 1),
(4, 'Sarah Williams', 'sarah.w@example.com', '+4915112345678', NULL, 1),
(5, 'David Müller', 'david.m@example.com', '+491701234567', NULL, 1);

-- =============================================
-- 2. 商品分类（家具桌凳）
-- =============================================
INSERT INTO `t_category` (`id`, `name`, `image`, `parent_id`, `sort`, `status`) VALUES
(1, 'Tables', 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=600&q=80', 0, 1, 1),
(2, 'Chairs', 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=600&q=80', 0, 2, 1),
(3, 'Stools', 'https://images.unsplash.com/photo-1503602642458-232111445657?w=600&q=80', 0, 3, 1),
(4, 'Storage', 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600&q=80', 0, 4, 1),
(5, 'Dining Tables', 'https://images.unsplash.com/photo-1600585152220-90363fe7e115?w=600&q=80', 1, 1, 1),
(6, 'Coffee Tables', 'https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=600&q=80', 1, 2, 1),
(7, 'Side Tables', 'https://images.unsplash.com/photo-1499933374294-4584851497cc?w=600&q=80', 1, 3, 1),
(8, 'Dining Chairs', 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=600&q=80', 2, 1, 1),
(9, 'Lounge Chairs', 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600&q=80', 2, 2, 1),
(10, 'Bar Stools', 'https://images.unsplash.com/photo-1541123603104-512919d6a96c?w=600&q=80', 3, 1, 1),
(11, 'Counter Stools', 'https://images.unsplash.com/photo-1503602642458-232111445657?w=600&q=80', 3, 2, 1);

-- =============================================
-- 3. 商品数据
-- =============================================
INSERT INTO `t_product` (`id`, `name`, `description`, `category_id`, `price`, `discount_price`, `sku_code`, `main_image`, `poster_image`, `detail_image`, `status`, `sort`) VALUES
(1, 'Nordic Oak Dining Table',
 'A beautifully crafted dining table made from solid European oak. Clean lines and warm tones bring Scandinavian simplicity to your dining space. Seats 6-8 comfortably.',
 5, 1299.00, 1099.00, 'TBL-NO-001',
 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=800&q=80',
 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=1920&q=80',
 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=1200&q=80',
 1, 1),

(2, 'Minimalist Walnut Coffee Table',
 'Hand-finished walnut coffee table with tapered legs. Natural grain patterns make each piece unique. A perfect centerpiece for modern living rooms.',
 6, 699.00, NULL, 'TBL-MW-001',
 'https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=800&q=80',
 'https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=1920&q=80',
 NULL,
 1, 2),

(3, 'Round Marble Side Table',
 'Elegant side table with a genuine marble top and powder-coated steel base. Compact enough for any corner, bold enough to stand alone.',
 7, 459.00, 399.00, 'TBL-RM-001',
 'https://images.unsplash.com/photo-1499933374294-4584851497cc?w=800&q=80',
 'https://images.unsplash.com/photo-1499933374294-4584851497cc?w=1920&q=80',
 NULL,
 1, 3),

(4, 'Bentwood Dining Chair',
 'Classic bentwood design reimagined with modern ergonomics. Solid beechwood frame with a comfortable curved backrest. Stackable for easy storage.',
 8, 349.00, NULL, 'CHR-BW-001',
 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=800&q=80',
 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=1920&q=80',
 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=1200&q=80',
 1, 4),

(5, 'Linen Lounge Chair',
 'Wide-seat lounge chair upholstered in natural linen. Solid ash wood frame with armrests designed for ultimate relaxation.',
 9, 899.00, 799.00, 'CHR-LL-001',
 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&q=80',
 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=1920&q=80',
 NULL,
 1, 5),

(6, 'Leather Accent Chair',
 'Premium top-grain leather paired with a solid oak frame. Mid-century modern design that ages beautifully over time.',
 9, 1199.00, NULL, 'CHR-LA-001',
 'https://images.unsplash.com/photo-1506439773649-6e0eb8cfb237?w=800&q=80',
 'https://images.unsplash.com/photo-1506439773649-6e0eb8cfb237?w=1920&q=80',
 NULL,
 1, 6),

(7, 'Wooden Bar Stool',
 'Solid oak bar stool with a comfortable footrest ring. Natural wood finish complements any kitchen island or home bar.',
 10, 279.00, NULL, 'STL-WB-001',
 'https://images.unsplash.com/photo-1503602642458-232111445657?w=800&q=80',
 'https://images.unsplash.com/photo-1503602642458-232111445657?w=1920&q=80',
 NULL,
 1, 7),

(8, 'Metal Frame Counter Stool',
 'Industrial-inspired counter stool with a solid elm seat and matte black steel frame. Perfect for modern kitchens.',
 11, 229.00, 199.00, 'STL-MC-001',
 'https://images.unsplash.com/photo-1541123603104-512919d6a96c?w=800&q=80',
 'https://images.unsplash.com/photo-1541123603104-512919d6a96c?w=1920&q=80',
 NULL,
 1, 8),

(9, 'Extendable Dining Table',
 'Smart engineering meets timeless design. Oak dining table extends from 140cm to 200cm with a simple pull mechanism. Seats 4-8.',
 5, 1599.00, 1399.00, 'TBL-ED-001',
 'https://images.unsplash.com/photo-1600585152220-90363fe7e115?w=800&q=80',
 'https://images.unsplash.com/photo-1600585152220-90363fe7e115?w=1920&q=80',
 'https://images.unsplash.com/photo-1600585152220-90363fe7e115?w=1200&q=80',
 1, 9),

(10, 'Rattan Storage Cabinet',
 'Hand-woven rattan doors with a solid mango wood frame. Two adjustable shelves inside. A natural addition to any room.',
 4, 849.00, NULL, 'STR-RS-001',
 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&q=80',
 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=1920&q=80',
 NULL,
 1, 10),

(11, 'Floating Wall Shelf Set',
 'Set of 3 solid pine wall shelves in different lengths. Hidden mounting hardware creates a clean floating effect.',
 4, 189.00, 159.00, 'STR-FW-001',
 'https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=800&q=80',
 'https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=1920&q=80',
 NULL,
 1, 11),

(12, 'Glass Top Coffee Table',
 'Tempered glass top with a solid walnut base. Transparent surface creates an airy feel, perfect for smaller living spaces.',
 6, 549.00, NULL, 'TBL-GC-001',
 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=800&q=80',
 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=1920&q=80',
 NULL,
 1, 12);

-- =============================================
-- 4. 商品副图
-- =============================================
INSERT INTO `t_product_image` (`product_id`, `image_url`, `sort`) VALUES
(1, 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=600&q=80', 1),
(1, 'https://images.unsplash.com/photo-1600585152220-90363fe7e115?w=600&q=80', 2),
(1, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600&q=80', 3),
(4, 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=600&q=80', 1),
(4, 'https://images.unsplash.com/photo-1506439773649-6e0eb8cfb237?w=600&q=80', 2),
(5, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600&q=80', 1),
(5, 'https://images.unsplash.com/photo-1506439773649-6e0eb8cfb237?w=600&q=80', 2),
(9, 'https://images.unsplash.com/photo-1600585152220-90363fe7e115?w=600&q=80', 1),
(9, 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=600&q=80', 2),
(9, 'https://images.unsplash.com/photo-1532372320572-cda25653a26d?w=600&q=80', 3);

-- =============================================
-- 5. 商品SKU
-- =============================================
INSERT INTO `t_product_sku` (`product_id`, `sku_code`, `spec_name`, `spec_value`, `price`, `stock`, `status`) VALUES
-- Nordic Oak Dining Table: 尺寸
(1, 'TBL-NO-001-S', 'Size', '140cm x 80cm', 1099.00, 15, 1),
(1, 'TBL-NO-001-M', 'Size', '180cm x 90cm', 1299.00, 10, 1),
(1, 'TBL-NO-001-L', 'Size', '220cm x 100cm', 1499.00, 5, 1),

-- Walnut Coffee Table: 颜色
(2, 'TBL-MW-001-N', 'Color', 'Natural Walnut', 699.00, 20, 1),
(2, 'TBL-MW-001-D', 'Color', 'Dark Walnut', 699.00, 12, 1),

-- Bentwood Dining Chair: 颜色
(4, 'CHR-BW-001-N', 'Color', 'Natural Beech', 349.00, 30, 1),
(4, 'CHR-BW-001-B', 'Color', 'Black', 349.00, 25, 1),
(4, 'CHR-BW-001-W', 'Color', 'White', 349.00, 18, 1),

-- Linen Lounge Chair: 面料
(5, 'CHR-LL-001-G', 'Upholstery', 'Grey Linen', 799.00, 8, 1),
(5, 'CHR-LL-001-B', 'Upholstery', 'Beige Linen', 799.00, 10, 1),

-- Wooden Bar Stool: 高度
(7, 'STL-WB-001-65', 'Height', '65cm', 279.00, 20, 1),
(7, 'STL-WB-001-75', 'Height', '75cm', 279.00, 15, 1),

-- Metal Counter Stool: 颜色
(8, 'STL-MC-001-B', 'Color', 'Black Frame', 199.00, 25, 1),
(8, 'STL-MC-001-W', 'Color', 'White Frame', 199.00, 18, 1),

-- Extendable Dining Table: 尺寸
(9, 'TBL-ED-001-S', 'Size', '140cm - 180cm', 1399.00, 8, 1),
(9, 'TBL-ED-001-L', 'Size', '180cm - 240cm', 1599.00, 5, 1);

-- =============================================
-- 6. 询盘订单
-- =============================================
INSERT INTO `t_inquiry` (`id`, `inquiry_no`, `user_id`, `user_name`, `user_email`, `user_whatsapp`, `total_amount`, `remark`, `status`, `admin_remark`) VALUES
(1, 'INQ202606200001', 1, 'John Smith', 'john.smith@example.com', '+14155551234', 2198.00, 'Interested in bulk order for restaurant project. Need 10+ tables.', 1, '已联系客户，等待回复'),
(2, 'INQ202606210001', 2, 'Emily Johnson', 'emily.j@example.com', '+447911123456', 799.00, 'Do you ship to UK? Interested in the lounge chair.', 0, NULL),
(3, 'INQ202606210002', 3, 'Michael Chen', 'michael.c@example.com', '+8613800138000', 1747.00, 'Need samples for showroom display.', 2, '样品已寄出，单号: SF1234567890'),
(4, 'INQ202606220001', 4, 'Sarah Williams', 'sarah.w@example.com', '+4915112345678', 1497.00, 'Looking for dining set for new apartment.', 0, NULL),
(5, 'INQ202606220002', 5, 'David Müller', 'david.m@example.com', '+491701234567', 2797.00, 'Wholesale order for German market. Need CE certification.', 1, '已发送认证文件'),
(6, 'INQ202606230001', 1, 'John Smith', 'john.smith@example.com', '+14155551234', 399.00, 'Second order. Love the marble side table.', 2, '老客户复购，已发货'),
(7, 'INQ202606230002', 3, 'Michael Chen', 'michael.c@example.com', '+8613800138000', 878.00, '', 3, '客户取消，改选其他产品');

-- =============================================
-- 7. 询盘商品明细
-- =============================================
INSERT INTO `t_inquiry_item` (`inquiry_id`, `product_id`, `product_name`, `product_image`, `sku_id`, `sku_spec`, `price`, `quantity`) VALUES
-- 询盘1: 2张餐桌
(1, 1, 'Nordic Oak Dining Table', 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=800&q=80', 2, 'Size: 180cm x 90cm', 1299.00, 2),
-- 询盘2: 1张休闲椅
(2, 5, 'Linen Lounge Chair', 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&q=80', 9, 'Upholstery: Grey Linen', 799.00, 1),
-- 询盘3: 1张餐桌 + 4把餐椅
(3, 1, 'Nordic Oak Dining Table', 'https://images.unsplash.com/photo-1617806118233-18e1de247200?w=800&q=80', 1, 'Size: 140cm x 80cm', 1099.00, 1),
(3, 4, 'Bentwood Dining Chair', 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=800&q=80', 6, 'Color: Natural Beech', 349.00, 4),
-- 询盘4: 3把餐椅 + 1个边几
(4, 4, 'Bentwood Dining Chair', 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=800&q=80', 6, 'Color: Black', 349.00, 3),
(4, 3, 'Round Marble Side Table', 'https://images.unsplash.com/photo-1499933374294-4584851497cc?w=800&q=80', NULL, NULL, 399.00, 1),
-- 询盘5: 2张伸缩餐桌
(5, 9, 'Extendable Dining Table', 'https://images.unsplash.com/photo-1600585152220-90363fe7e115?w=800&q=80', 16, 'Size: 180cm - 240cm', 1399.00, 2),
-- 询盘6: 1个边几（复购）
(6, 3, 'Round Marble Side Table', 'https://images.unsplash.com/photo-1499933374294-4584851497cc?w=800&q=80', NULL, NULL, 399.00, 1),
-- 询盘7: 2把吧凳（已取消）
(7, 7, 'Wooden Bar Stool', 'https://images.unsplash.com/photo-1503602642458-232111445657?w=800&q=80', 11, 'Height: 75cm', 279.00, 2),
(7, 8, 'Metal Frame Counter Stool', 'https://images.unsplash.com/photo-1541123603104-512919d6a96c?w=800&q=80', 15, 'Color: Black Frame', 199.00, 2);

-- =============================================
-- 8. 系统配置（更新为家具风格）
-- =============================================
UPDATE `t_site_config` SET `config_value` = '"Indie Station"' WHERE `config_key` = 'site_title';
UPDATE `t_site_config` SET `config_value` = '""' WHERE `config_key` = 'site_logo';
UPDATE `t_site_config` SET `config_value` = '[
  "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=1920&q=80",
  "https://images.unsplash.com/photo-1617806118233-18e1de247200?w=1920&q=80",
  "https://images.unsplash.com/photo-1592078615290-033ee584e267?w=1920&q=80"
]' WHERE `config_key` = 'banner_images';
UPDATE `t_site_config` SET `config_value` = '"contact@indiestation.com"' WHERE `config_key` = 'contact_email';

-- 首页文案配置
INSERT INTO `t_site_config` (`config_key`, `config_value`, `description`) VALUES
('hero_tagline', '"Handcrafted Furniture"', '首页标语'),
('hero_title', '"Timeless Design"', '首页主标题'),
('hero_subtitle', '"For Modern Living"', '首页副标题')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);
UPDATE `t_site_config` SET `config_value` = '"+14155550000"' WHERE `config_key` = 'contact_whatsapp';
UPDATE `t_site_config` SET `config_value` = '{
  "facebook": "https://facebook.com/indiestation",
  "instagram": "https://instagram.com/indiestation",
  "pinterest": "https://pinterest.com/indiestation"
}' WHERE `config_key` = 'social_links';
UPDATE `t_site_config` SET `config_value` = '{
  "copyright": "© 2026 Indie Station. All rights reserved.",
  "links": [
    {"title": "Privacy Policy", "url": "/privacy"},
    {"title": "Terms of Service", "url": "/terms"},
    {"title": "Shipping Info", "url": "/shipping"}
  ]
}' WHERE `config_key` = 'footer_info';
UPDATE `t_site_config` SET `config_value` = '{
  "bannerImage": "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?w=1920&q=80",
  "storyImage": "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?w=800&q=80",
  "storyTitle": "OUR STORY",
  "storyContent": "Founded in 2020, Indie Station was born from a simple belief: furniture should be simple, functional, and beautiful. Each piece in our collection is thoughtfully designed and crafted with care by skilled artisans. We work with solid wood, natural materials, and time-honored techniques to create pieces that last a lifetime.",
  "philosophy": [
    {"icon": "quality", "title": "QUALITY", "desc": "Premium materials built to last"},
    {"icon": "simple", "title": "SIMPLE", "desc": "Clean lines and minimal design that fits any space"},
    {"icon": "sustainable", "title": "SUSTAINABLE", "desc": "Eco-friendly materials and ethical practices"}
  ],
  "craftImage": "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?w=1200&q=80",
  "craftTitle": "HANDCRAFTED WITH CARE",
  "craftContent": "Every joint, every curve, every finish is executed with precision and patience. We work with solid wood, natural materials, and time-honored techniques to create pieces that last a lifetime.",
  "stats": [
    {"number": "500+", "label": "Products Sold"},
    {"number": "15", "label": "Years Craft"},
    {"number": "30+", "label": "Countries"}
  ],
  "ctaText": "READY TO FURNISH YOUR SPACE?",
  "ctaButtonText": "EXPLORE OUR COLLECTION"
}' WHERE `config_key` = 'about_us';
