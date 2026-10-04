-- Migration Script: Add color/size variant to cart_items and order_items
-- Description: Cung 1 san pham nhung khac mau/size phai la cac dong rieng trong gio hang
--              (vd: 5 ao den + 5 ao trang khong duoc gop thanh 10).
-- Note: Neu dung spring.jpa.hibernate.ddl-auto=update thi Hibernate tu them cot; script nay dung cho moi truong chay migration thu cong.

ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS color VARCHAR(50);
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS size VARCHAR(50);

ALTER TABLE order_items ADD COLUMN IF NOT EXISTS color VARCHAR(50);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS size VARCHAR(50);

SELECT 'Migration completed successfully. color/size added to cart_items and order_items.' AS status;
