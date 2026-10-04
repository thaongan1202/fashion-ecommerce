-- Khuyến mãi mẫu cho một số sản phẩm thời trang
INSERT INTO promotions (id, effective_date, expiration_date, title, description, percent_discount, status, template_id) VALUES
('promo-flash-sale-2026', NOW(), NOW() + INTERVAL '30 days', 'Giảm giá cuối tuần', 'Giảm 15% cho một số mẫu thời trang', 15.0, 'ACTIVE', 'template-001')
ON CONFLICT (id) DO NOTHING;

INSERT INTO promotion_targets (applicable_object_id, type, promotion_id) VALUES
(1, 'PRODUCT', 'promo-flash-sale-2026'),
(5, 'PRODUCT', 'promo-flash-sale-2026'),
(9, 'PRODUCT', 'promo-flash-sale-2026'),
(11, 'PRODUCT', 'promo-flash-sale-2026');
