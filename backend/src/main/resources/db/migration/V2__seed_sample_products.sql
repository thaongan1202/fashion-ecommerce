INSERT INTO categories (name, description)
SELECT sample.name, sample.description
FROM (VALUES
    ('Áo', 'Áo thun, áo sơ mi và áo khoác'),
    ('Quần', 'Quần dài và quần short'),
    ('Váy', 'Váy mặc hằng ngày')
) AS sample(name, description)
WHERE NOT EXISTS (SELECT 1 FROM categories c WHERE c.name = sample.name);

INSERT INTO brands (name, description)
SELECT sample.name, sample.description
FROM (VALUES
    ('Mây Studio', 'Thiết kế ứng dụng cho phong cách hằng ngày'),
    ('Urban Basic', 'Trang phục cơ bản, dễ phối'),
    ('Linen Lab', 'Thiết kế với chất liệu linen')
) AS sample(name, description)
WHERE NOT EXISTS (SELECT 1 FROM brands b WHERE b.name = sample.name);

INSERT INTO products (name, description, category_id, brand_id, material)
SELECT sample.name, sample.description, c.id, b.id, sample.material
FROM (VALUES
    ('Áo thun cotton Essential', 'Áo thun dáng cơ bản, phù hợp phối đồ hằng ngày.', 'Áo', 'Urban Basic', 'Cotton'),
    ('Áo sơ mi linen Breeze', 'Áo sơ mi nhẹ thoáng với phom dáng rộng vừa.', 'Áo', 'Linen Lab', 'Linen'),
    ('Áo khoác denim Everyday', 'Áo khoác denim dễ phối cho những ngày se mát.', 'Áo', 'Mây Studio', 'Denim'),
    ('Quần jeans Straight Fit', 'Quần jeans ống đứng với phom dáng thoải mái.', 'Quần', 'Urban Basic', 'Denim'),
    ('Váy midi hoa nhí', 'Váy midi họa tiết hoa nhí, phù hợp đi chơi cuối tuần.', 'Váy', 'Linen Lab', 'Cotton'),
    ('Quần short linen Summer', 'Quần short linen nhẹ mát cho ngày hè.', 'Quần', 'Mây Studio', 'Linen')
) AS sample(name, description, category_name, brand_name, material)
JOIN categories c ON c.name = sample.category_name
JOIN brands b ON b.name = sample.brand_name;

INSERT INTO product_variants (product_id, size, color, sku, price, stock_qty)
SELECT p.id, sample.size, sample.color, sample.sku, sample.price, sample.stock_qty
FROM (VALUES
    ('Áo thun cotton Essential', 'S', 'Trắng', 'DEMO-TSHIRT-WHT-S', 199000.00, 12),
    ('Áo thun cotton Essential', 'M', 'Trắng', 'DEMO-TSHIRT-WHT-M', 199000.00, 18),
    ('Áo thun cotton Essential', 'L', 'Đen', 'DEMO-TSHIRT-BLK-L', 199000.00, 9),
    ('Áo sơ mi linen Breeze', 'M', 'Be', 'DEMO-SHIRT-BGE-M', 459000.00, 8),
    ('Áo sơ mi linen Breeze', 'L', 'Be', 'DEMO-SHIRT-BGE-L', 459000.00, 7),
    ('Áo khoác denim Everyday', 'M', 'Xanh denim', 'DEMO-DENIM-JKT-M', 699000.00, 5),
    ('Áo khoác denim Everyday', 'L', 'Xanh denim', 'DEMO-DENIM-JKT-L', 699000.00, 6),
    ('Quần jeans Straight Fit', '30', 'Xanh đậm', 'DEMO-JEANS-30', 599000.00, 10),
    ('Quần jeans Straight Fit', '32', 'Xanh đậm', 'DEMO-JEANS-32', 599000.00, 8),
    ('Váy midi hoa nhí', 'S', 'Kem', 'DEMO-MIDI-DRESS-S', 529000.00, 6),
    ('Váy midi hoa nhí', 'M', 'Kem', 'DEMO-MIDI-DRESS-M', 529000.00, 8),
    ('Quần short linen Summer', 'M', 'Trắng', 'DEMO-LINEN-SHORT-M', 329000.00, 10),
    ('Quần short linen Summer', 'L', 'Trắng', 'DEMO-LINEN-SHORT-L', 329000.00, 7)
) AS sample(product_name, size, color, sku, price, stock_qty)
JOIN products p ON p.name = sample.product_name;

INSERT INTO product_images (product_id, image_url, is_primary)
SELECT p.id, sample.image_url, TRUE
FROM (VALUES
    ('Áo thun cotton Essential', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=80'),
    ('Áo sơ mi linen Breeze', 'https://images.unsplash.com/photo-1598033129183-c4f50c736f10?auto=format&fit=crop&w=900&q=80'),
    ('Áo khoác denim Everyday', 'https://images.unsplash.com/photo-1543076447-215ad9ba6923?auto=format&fit=crop&w=900&q=80'),
    ('Quần jeans Straight Fit', 'https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=900&q=80'),
    ('Váy midi hoa nhí', 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?auto=format&fit=crop&w=900&q=80'),
    ('Quần short linen Summer', 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?auto=format&fit=crop&w=900&q=80')
) AS sample(product_name, image_url)
JOIN products p ON p.name = sample.product_name;
