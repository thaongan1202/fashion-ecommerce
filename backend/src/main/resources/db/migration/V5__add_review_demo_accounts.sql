INSERT INTO users (full_name, email, password_hash, role, status)
VALUES
    ('Demo Buyer Two', 'demo.buyer2@fashion.local', 'demo-session-only', 'CUSTOMER', 'ACTIVE'),
    ('Demo Buyer Three', 'demo.buyer3@fashion.local', 'demo-session-only', 'CUSTOMER', 'ACTIVE'),
    ('Demo Buyer Four', 'demo.buyer4@fashion.local', 'demo-session-only', 'CUSTOMER', 'ACTIVE'),
    ('Demo Buyer Five', 'demo.buyer5@fashion.local', 'demo-session-only', 'CUSTOMER', 'ACTIVE'),
    ('Demo Visitor Two', 'demo.visitor2@fashion.local', 'demo-session-only', 'CUSTOMER', 'ACTIVE'),
    ('Demo Visitor Three', 'demo.visitor3@fashion.local', 'demo-session-only', 'CUSTOMER', 'ACTIVE')
ON CONFLICT (lower(email)) DO NOTHING;

INSERT INTO orders (
    order_code, user_id, total_amount, status, payment_method,
    shipping_recipient_name, shipping_phone, shipping_address_line
)
SELECT sample.order_code, u.id, 199000, 'DELIVERED', 'COD',
       u.full_name, '0900000000', 'Demo address for review testing'
FROM (VALUES
    ('DEMO-DELIVERED-002', 'demo.buyer2@fashion.local'),
    ('DEMO-DELIVERED-003', 'demo.buyer3@fashion.local'),
    ('DEMO-DELIVERED-004', 'demo.buyer4@fashion.local'),
    ('DEMO-DELIVERED-005', 'demo.buyer5@fashion.local')
) AS sample(order_code, email)
JOIN users u ON u.email = sample.email
WHERE NOT EXISTS (
    SELECT 1 FROM orders o WHERE o.order_code = sample.order_code
);

INSERT INTO order_items (
    order_id, variant_id, product_name_snapshot, size, color, quantity, price_at_purchase
)
SELECT o.id, v.id, p.name, v.size, v.color, 1, v.price
FROM orders o
JOIN users u ON u.id = o.user_id
JOIN products p ON p.name = 'Ão thun cotton Essential'
JOIN product_variants v ON v.product_id = p.id
WHERE o.order_code IN ('DEMO-DELIVERED-002', 'DEMO-DELIVERED-003', 'DEMO-DELIVERED-004', 'DEMO-DELIVERED-005')
  AND v.sku = 'DEMO-TSHIRT-WHT-M'
  AND NOT EXISTS (
      SELECT 1 FROM order_items oi WHERE oi.order_id = o.id AND oi.variant_id = v.id
  );
