-- Member 2: Catalog. Đổi số V2 nếu người điều phối Flyway đã dùng số này.
CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE brands (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','HIDDEN')),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    brand_id    BIGINT NOT NULL REFERENCES brands(id),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_brand ON products(brand_id);
CREATE INDEX idx_products_status ON products(status);

CREATE TABLE product_variants (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    sku        VARCHAR(64) NOT NULL UNIQUE,
    size       VARCHAR(50),            -- BR-PROD-04: cho phép null
    color      VARCHAR(50),            -- BR-PROD-04: cho phép null
    price      NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    stock      INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    active     BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_variants_product ON product_variants(product_id);

CREATE TABLE product_images (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_images_product ON product_images(product_id);

-- ===== Seed data cho Member 3 (review) và Member 4 (cart/stock) thử =====
INSERT INTO categories(name, description) VALUES
 ('Áo', 'Áo thun, áo sơ mi, áo khoác'),
 ('Quần', 'Quần jean, quần short'),
 ('Giày', 'Giày thể thao, giày da');

INSERT INTO brands(name, description) VALUES
 ('Nike', 'Thương hiệu thể thao'),
 ('Adidas', 'Thương hiệu thể thao'),
 ('Uniqlo', 'Thời trang cơ bản');

INSERT INTO products(name, description, status, category_id, brand_id) VALUES
 ('Áo thun Dri-FIT', 'Áo thun thể thao thoáng khí', 'ACTIVE',
   (SELECT id FROM categories WHERE name='Áo'), (SELECT id FROM brands WHERE name='Nike')),
 ('Quần jean Slim', 'Quần jean ống ôm', 'ACTIVE',
   (SELECT id FROM categories WHERE name='Quần'), (SELECT id FROM brands WHERE name='Uniqlo')),
 ('Giày Ultraboost', 'Giày chạy bộ', 'ACTIVE',
   (SELECT id FROM categories WHERE name='Giày'), (SELECT id FROM brands WHERE name='Adidas')),
 ('Túi tote (sản phẩm đã ẩn)', 'Dùng để test trạng thái HIDDEN', 'HIDDEN',
   (SELECT id FROM categories WHERE name='Áo'), (SELECT id FROM brands WHERE name='Uniqlo'));

INSERT INTO product_variants(product_id, sku, size, color, price, stock) VALUES
 ((SELECT id FROM products WHERE name='Áo thun Dri-FIT'), 'NIKE-DF-M-BLK', 'M', 'Đen', 450000, 20),
 ((SELECT id FROM products WHERE name='Áo thun Dri-FIT'), 'NIKE-DF-L-BLK', 'L', 'Đen', 450000, 15),
 ((SELECT id FROM products WHERE name='Áo thun Dri-FIT'), 'NIKE-DF-L-WHT', 'L', 'Trắng', 450000, 0),
 ((SELECT id FROM products WHERE name='Quần jean Slim'), 'UNI-JN-30', '30', NULL, 790000, 10),
 ((SELECT id FROM products WHERE name='Quần jean Slim'), 'UNI-JN-32', '32', NULL, 790000, 8),
 ((SELECT id FROM products WHERE name='Giày Ultraboost'), 'ADI-UB-42', '42', 'Xám', 3200000, 5),
 ((SELECT id FROM products WHERE name='Giày Ultraboost'), 'ADI-UB-43', '43', 'Xám', 3200000, 3),
 ((SELECT id FROM products WHERE name='Túi tote (sản phẩm đã ẩn)'), 'UNI-TOTE', NULL, NULL, 150000, 50);

-- URL ảnh mẫu: đặt file cùng tên vào thư mục uploads/ nếu muốn hiện ảnh thật
INSERT INTO product_images(product_id, url, sort_order) VALUES
 ((SELECT id FROM products WHERE name='Áo thun Dri-FIT'), '/uploads/sample-ao.jpg', 0),
 ((SELECT id FROM products WHERE name='Quần jean Slim'), '/uploads/sample-quan.jpg', 0),
 ((SELECT id FROM products WHERE name='Giày Ultraboost'), '/uploads/sample-giay.jpg', 0);
