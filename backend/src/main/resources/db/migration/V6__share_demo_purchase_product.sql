UPDATE order_items oi
SET variant_id = v.id,
    product_name_snapshot = p.name,
    size = v.size,
    color = v.color,
    price_at_purchase = v.price
FROM orders o
JOIN (VALUES
    ('DEMO-DELIVERED-002', 'DEMO-TSHIRT-WHT-M'),
    ('DEMO-DELIVERED-003', 'DEMO-TSHIRT-WHT-M'),
    ('DEMO-DELIVERED-004', 'DEMO-SHIRT-BGE-M'),
    ('DEMO-DELIVERED-005', 'DEMO-JEANS-30')
) AS sample(order_code, sku) ON sample.order_code = o.order_code
JOIN product_variants v ON v.sku = sample.sku
JOIN products p ON p.id = v.product_id
WHERE oi.order_id = o.id;

INSERT INTO order_items (
    order_id, variant_id, product_name_snapshot, size, color, quantity, price_at_purchase
)
SELECT o.id, v.id, p.name, v.size, v.color, 1, v.price
FROM orders o
JOIN (VALUES
    ('DEMO-DELIVERED-002', 'DEMO-TSHIRT-WHT-M'),
    ('DEMO-DELIVERED-003', 'DEMO-TSHIRT-WHT-M'),
    ('DEMO-DELIVERED-004', 'DEMO-SHIRT-BGE-M'),
    ('DEMO-DELIVERED-005', 'DEMO-JEANS-30')
) AS sample(order_code, sku) ON sample.order_code = o.order_code
JOIN product_variants v ON v.sku = sample.sku
JOIN products p ON p.id = v.product_id
WHERE NOT EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id = o.id);
