-- DATA MẪU CHO MÁY DEV – KHÔNG phải Flyway migration, chạy tay bằng psql/DBeaver sau khi V1 đã migrate.
-- Dùng đúng tên cột V1 (stock_qty, image_url). Nếu V1 có thêm cột NOT NULL (vd slug) hãy bổ sung vào INSERT.
INSERT INTO categories(name) VALUES ('Áo'), ('Quần'), ('Giày') ON CONFLICT DO NOTHING;
INSERT INTO brands(name)     VALUES ('Nike'), ('Adidas'), ('Uniqlo') ON CONFLICT DO NOTHING;

INSERT INTO products(name, description, status, category_id, brand_id) VALUES
 ('Áo thun Dri-FIT', 'Áo thun thể thao thoáng khí', 'ACTIVE',   (SELECT id FROM categories WHERE name='Áo'),   (SELECT id FROM brands WHERE name='Nike')),
 ('Quần jean Slim',  'Quần jean ống ôm',            'ACTIVE',   (SELECT id FROM categories WHERE name='Quần'), (SELECT id FROM brands WHERE name='Uniqlo')),
 ('Giày Ultraboost', 'Giày chạy bộ',                'ACTIVE',   (SELECT id FROM categories WHERE name='Giày'), (SELECT id FROM brands WHERE name='Adidas')),
 ('Túi tote (đã ẩn)','Dùng test trạng thái INACTIVE','INACTIVE', (SELECT id FROM categories WHERE name='Áo'),   (SELECT id FROM brands WHERE name='Uniqlo'));

INSERT INTO product_variants(product_id, sku, size, color, price, stock_qty) VALUES
 ((SELECT id FROM products WHERE name='Áo thun Dri-FIT'),  'NIKE-DF-M-BLK', 'M',  'Đen',   450000, 20),
 ((SELECT id FROM products WHERE name='Áo thun Dri-FIT'),  'NIKE-DF-L-BLK', 'L',  'Đen',   450000, 15),
 ((SELECT id FROM products WHERE name='Áo thun Dri-FIT'),  'NIKE-DF-L-WHT', 'L',  'Trắng', 450000, 0),   -- hết hàng
 ((SELECT id FROM products WHERE name='Quần jean Slim'),   'UNI-JN-30',     '30', NULL,    790000, 10),  -- color null
 ((SELECT id FROM products WHERE name='Quần jean Slim'),   'UNI-JN-32',     '32', NULL,    790000, 8),
 ((SELECT id FROM products WHERE name='Giày Ultraboost'),  'ADI-UB-42',     '42', 'Xám',  3200000, 5),
 ((SELECT id FROM products WHERE name='Giày Ultraboost'),  'ADI-UB-43',     '43', 'Xám',  3200000, 3),
 ((SELECT id FROM products WHERE name='Túi tote (đã ẩn)'), 'UNI-TOTE',      NULL, NULL,    150000, 50);   -- size+color null

INSERT INTO product_images(product_id, image_url) VALUES
 ((SELECT id FROM products WHERE name='Áo thun Dri-FIT'), '/uploads/sample-ao.jpg'),
 ((SELECT id FROM products WHERE name='Quần jean Slim'),  '/uploads/sample-quan.jpg'),
 ((SELECT id FROM products WHERE name='Giày Ultraboost'), '/uploads/sample-giay.jpg');
