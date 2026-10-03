INSERT INTO users (full_name, email, password_hash, role, status)
VALUES
    ('Demo Customer', 'demo.customer@fashion.local', 'demo-session-only', 'CUSTOMER', 'ACTIVE'),
    ('Demo Customer Without Purchase', 'demo.noorder@fashion.local', 'demo-session-only', 'CUSTOMER', 'ACTIVE')
ON CONFLICT (lower(email)) DO NOTHING;

INSERT INTO orders (
    order_code, user_id, total_amount, status, payment_method,
    shipping_recipient_name, shipping_phone, shipping_address_line
)
SELECT 'DEMO-DELIVERED-001', u.id, 199000, 'DELIVERED', 'COD',
       u.full_name, '0900000000', 'Địa chỉ mẫu dùng để kiểm thử review'
FROM users u
WHERE u.email = 'demo.customer@fashion.local'
  AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_code = 'DEMO-DELIVERED-001');

INSERT INTO order_items (
    order_id, variant_id, product_name_snapshot, size, color, quantity, price_at_purchase
)
SELECT o.id, v.id, p.name, v.size, v.color, 1, v.price
FROM orders o
JOIN users u ON u.id = o.user_id
JOIN products p ON p.name = 'Áo thun cotton Essential'
JOIN product_variants v ON v.product_id = p.id
WHERE o.order_code = 'DEMO-DELIVERED-001'
  AND v.sku = 'DEMO-TSHIRT-WHT-M'
  AND NOT EXISTS (
      SELECT 1 FROM order_items oi WHERE oi.order_id = o.id AND oi.variant_id = v.id
  );
