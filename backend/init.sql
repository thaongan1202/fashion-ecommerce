-- UTE FASHION HUB - DATABASE INITIALIZATION SCRIPT
-- PostgreSQL 15
-- Auto-run by Docker Compose on first startup
-- Updated to match all Entity classes

-- Enable extensions for text search helpers
CREATE EXTENSION IF NOT EXISTS unaccent;

-- ============================================
-- 1. USERS DOMAIN
-- ============================================

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    gender VARCHAR(10),
    date_of_birth DATE,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Sample data: users
INSERT INTO users (username, password_hash, full_name, email, phone_number, gender, date_of_birth, role, status, created_at, updated_at) VALUES
('admin', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Nguyễn Văn Admin', 'admin@utefashionhub.com', '0901234567', 'MALE', '1990-01-01', 'ADMIN', 'ACTIVE', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
('user001', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Trần Thị Hương', 'huong.tran@gmail.com', '0912345678', 'FEMALE', '1995-05-15', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
('user002', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Lê Văn Nam', 'nam.le@gmail.com', '0923456789', 'MALE', '1998-08-20', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
('user003', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Phạm Thị Mai', 'mai.pham@gmail.com', '0934567890', 'FEMALE', '2000-03-10', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
('user004', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Hoàng Văn Đức', 'duc.hoang@gmail.com', '0945678901', 'MALE', '1997-11-25', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
('user005', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Nguyễn Thị Lan', 'lan.nguyen@gmail.com', '0956789012', 'FEMALE', '1999-07-12', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
('user006', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Võ Văn Hùng', 'hung.vo@gmail.com', '0967890123', 'MALE', '1996-04-18', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
('user007', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Bùi Thị Hoa', 'hoa.bui@gmail.com', '0978901234', 'FEMALE', '2001-09-22', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
('user008', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Đặng Văn Tài', 'tai.dang@gmail.com', '0989012345', 'MALE', '1994-12-05', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
('user009', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Lý Thị Kim', 'kim.ly@gmail.com', '0990123456', 'FEMALE', '2002-06-30', 'CUSTOMER', 'ACTIVE', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
('user010', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Trương Văn Long', 'long.truong@gmail.com', '0901234568', 'MALE', '1993-03-15', 'CUSTOMER', 'LOCKED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
('user011', '$2a$10$N.zmdr9VKQf5VGcbq8BbXeuEYXCEL2k0E3W6AJCvHLE3p91NEVGie', 'Phan Thị Nga', 'nga.phan@gmail.com', '0912345679', 'FEMALE', '1996-08-20', 'CUSTOMER', 'ACTIVE', NOW(), NOW());

-- Table: addresses
CREATE TABLE IF NOT EXISTS addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    street_address TEXT NOT NULL,
    ward VARCHAR(100),
    ward_code VARCHAR(20),
    province VARCHAR(100),
    province_code VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Sample data: addresses
INSERT INTO addresses (user_id, recipient_name, phone_number, street_address, ward, ward_code, province, province_code, is_default) VALUES
(2, 'Trần Thị Hương', '0912345678', '123 Lê Lợi', 'Phường Bến Thành', '26734', 'TP. Hồ Chí Minh', '79', TRUE),
(2, 'Trần Thị Hương', '0912345678', '456 Nguyễn Huệ', 'Phường Bến Nghé', '26743', 'TP. Hồ Chí Minh', '79', FALSE),
(3, 'Lê Văn Nam', '0923456789', '789 Trần Hưng Đạo', 'Phường 1', '26755', 'TP. Hồ Chí Minh', '79', TRUE),
(4, 'Phạm Thị Mai', '0934567890', '321 Võ Văn Tần', 'Phường 5', '26767', 'TP. Hồ Chí Minh', '79', TRUE),
(5, 'Hoàng Văn Đức', '0945678901', '654 Đinh Tiên Hoàng', 'Phường Đa Kao', '26770', 'TP. Hồ Chí Minh', '79', TRUE);

-- ============================================
-- 2. PRODUCT DOMAIN
-- ============================================

-- Table: categories
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Sample data: categories
INSERT INTO categories (name, description, parent_id) VALUES
('Quần áo', 'Áo, quần, chân váy và đầm', NULL),
('Giày dép', 'Sneaker, sandal và giày casual', NULL),
('Túi xách', 'Túi tote, túi đeo chéo và balo', NULL),
('Phụ kiện', 'Mũ, thắt lưng, ví và kính', NULL);

-- Table: brands
CREATE TABLE IF NOT EXISTS brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    logo_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Sample data: brands
INSERT INTO brands (name, logo_url) VALUES
('UTE Basic', NULL),
('Denim Lab', NULL),
('Linen House', NULL),
('StepOne', NULL),
('Canvas Co', NULL),
('Leather Line', NULL);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_url VARCHAR(255),
    status BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    brand_id BIGINT REFERENCES brands(id) ON DELETE SET NULL,
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    updated_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    deleted_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_products_status_deleted ON products(status, is_deleted);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_products_brand_id ON products(brand_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- Table: product_templates
CREATE TABLE IF NOT EXISTS product_templates (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(100) UNIQUE NOT NULL,
    color VARCHAR(50),
    size VARCHAR(20),
    price DECIMAL(15,2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    stock_status VARCHAR(20) NOT NULL,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    updated_by BIGINT REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_product_template_sku ON product_templates (sku);
CREATE INDEX IF NOT EXISTS idx_product_template_product_id ON product_templates (product_id);
CREATE INDEX IF NOT EXISTS idx_product_template_stock_status ON product_templates (stock_status);

-- Table: product_metadata
CREATE TABLE IF NOT EXISTS product_metadata (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE REFERENCES products(id) ON DELETE CASCADE,
    import_price DECIMAL(15,2),
    sale_price DECIMAL(15,2),
    material VARCHAR(100),
    style VARCHAR(100),
    target_audience VARCHAR(50),
    season VARCHAR(50),
    pattern VARCHAR(100),
    fit VARCHAR(100),
    origin VARCHAR(100),
    care_instructions TEXT,
    additional_specs TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_metadata_product_id ON product_metadata (product_id);

-- Table: product_images
CREATE TABLE IF NOT EXISTS product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(255) NOT NULL,
    alt_text VARCHAR(255),
    image_order INTEGER NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- 3. PROMOTION DOMAIN
-- ============================================

-- Table: promotion_templates
CREATE TABLE IF NOT EXISTS promotion_templates (
    id VARCHAR(255) PRIMARY KEY,
    code VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL
);

INSERT INTO promotion_templates (id, code, created_at, type) VALUES
('template-001', 'DISCOUNT_TEMPLATE', NOW() - INTERVAL '30 days', 'DISCOUNT'),
('template-002', 'FREESHIP_TEMPLATE', NOW() - INTERVAL '30 days', 'FREESHIP'),
('template-003', 'VOUCHER_TEMPLATE', NOW() - INTERVAL '30 days', 'VOUCHER');

-- Table: promotions
CREATE TABLE IF NOT EXISTS promotions (
    id VARCHAR(255) PRIMARY KEY,
    effective_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    percent_discount DOUBLE PRECISION,
    fixed_amount DOUBLE PRECISION,
    max_discount DOUBLE PRECISION,
    min_value_to_be_applied DOUBLE PRECISION,
    status VARCHAR(20) NOT NULL,
    template_id VARCHAR(255) NOT NULL REFERENCES promotion_templates(id) ON DELETE RESTRICT
);

-- Table: promotion_targets
CREATE TABLE IF NOT EXISTS promotion_targets (
    id BIGSERIAL PRIMARY KEY,
    applicable_object_id BIGINT,
    type VARCHAR(20) NOT NULL,
    promotion_id VARCHAR(255) REFERENCES promotions(id) ON DELETE CASCADE
);

-- ============================================
-- 4. ORDER DOMAIN
-- ============================================

-- Table: orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_code VARCHAR(20) UNIQUE NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    email VARCHAR(100) NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    shipping_address TEXT NOT NULL,
    shipping_fee DECIMAL(15,2),
    shipping_unit VARCHAR(50),
    note TEXT,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    promotion_id VARCHAR(255) REFERENCES promotions(id) ON DELETE SET NULL,
    freeshipping_promotion_id VARCHAR(255) REFERENCES promotions(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table: order_items
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE SET NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table: order_status_history
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    changed_by VARCHAR(50),
    changed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table: payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    provider VARCHAR(20),
    transaction_id VARCHAR(100),
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    note TEXT,
    reconciled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table: payment_callback_logs
CREATE TABLE IF NOT EXISTS payment_callback_logs (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    request_data TEXT,
    response_code VARCHAR(20),
    transaction_id VARCHAR(100),
    signature VARCHAR(255),
    signature_valid BOOLEAN,
    error_message TEXT,
    received_at TIMESTAMP NOT NULL
);

-- ============================================
-- 5. CART DOMAIN
-- ============================================

-- Table: carts
CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0 NOT NULL
);

-- Table: cart_items
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0 NOT NULL
);

-- ============================================
-- 6. INTERACTION DOMAIN
-- ============================================

-- Table: reviews
CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table: review_likes
CREATE TABLE IF NOT EXISTS review_likes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    review_id BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, review_id)
);

