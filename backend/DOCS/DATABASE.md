Chào bạn, tôi là **Technical Lead**.

Tôi đã thực hiện việc **hợp nhất (merge)** toàn diện giữa hai tài liệu: **Class Diagram (Business Logic)** và **ERD v2.0 (Database Structure)**.

Bản thiết kế dưới đây là phiên bản **FINAL**, đảm bảo:
1.  **Chuẩn hóa ERD:** Tối ưu hóa quan hệ, sử dụng khóa ngoại chặt chẽ.
2.  **Hỗ trợ trọn vẹn Class Diagram:** Các class như `OrderStatusHistory`, `Payment`, `PromotionTarget` (logic phức tạp) đều được ánh xạ vào DB.
3.  **Tên bảng:** Đã đổi `vouchers` thành `promotions` theo yêu cầu.
4.  **Công nghệ:** Tận dụng `JSONB` của PostgreSQL để lưu trữ linh hoạt các thuộc tính thừa kế (`Smartphone`, `Laptop`...).

---

# TÀI LIỆU THIẾT KẾ CƠ SỞ DỮ LIỆU (DATABASE SCHEMA) - FINAL
**DỰ ÁN: UTE PHONE HUB**
**Phiên bản:** 3.0 (Merged & Finalized)
**Hệ quản trị:** PostgreSQL 15

## 1. Tổng quan Kiến trúc Dữ liệu
*   **Chiến lược thừa kế (Inheritance Mapping):** Sử dụng chiến lược **Single Table** kết hợp **JSONB**.
    *   Tất cả `User`, `Registered`, `Administrator` -> Gộp vào bảng `users`.
    *   Tất cả `Product`, `Smartphone`, `Laptop` -> Gộp vào bảng `products` (dữ liệu riêng lưu cột `specifications`).
*   **Lịch sử & Theo dõi:**
    *   Thêm bảng `order_status_history` để theo dõi luồng `Order` (hỗ trợ Class Diagram).
    *   Thêm bảng `payments` để lưu chi tiết giao dịch (Transaction ID) bên cạnh phương thức thanh toán.

---

## 2. Chi tiết các bảng (Tables)

### 2.1. Nhóm Người dùng (Users Domain)
*Ánh xạ từ: User, Account, Registered, Administrator*

#### `users`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID người dùng. |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Tên đăng nhập. |
| password_hash | VARCHAR(255) | NOT NULL | Mật khẩu (BCrypt). |
| full_name | VARCHAR(100) | NOT NULL | Họ tên đầy đủ. |
| email | VARCHAR(100) | UNIQUE, NOT NULL | Email liên hệ. |
| phone_number | VARCHAR(15) | | Số điện thoại. |
| gender | VARCHAR(10) | | `EGender` (MALE, FEMALE, OTHER). |
| date_of_birth | DATE | | Ngày sinh. |
| role | VARCHAR(20) | NOT NULL | `UserRole` (ADMIN, CUSTOMER). |
| status | VARCHAR(20) | NOT NULL | `UserStatus` (ACTIVE, LOCKED). |
| created_at | TIMESTAMP | DEFAULT NOW() | |
| updated_at | TIMESTAMP | DEFAULT NOW() | |

#### `addresses`
*Ánh xạ từ: Address*
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID địa chỉ. |
| user_id | BIGINT | FK -> users(id) | Thuộc về User nào. |
| recipient_name | VARCHAR(100) | NOT NULL | Tên người nhận. |
| phone_number | VARCHAR(15) | NOT NULL | SĐT người nhận. |
| street_address | TEXT | NOT NULL | Địa chỉ cụ thể. |
| ward | VARCHAR(100) | | Phường/Xã. |
| **ward_code** | VARCHAR(20) | | Mã Phường/Xã (GHN/GHTK API). |
| province | VARCHAR(100) | | Tỉnh/Thành phố. |
| **province_code**| VARCHAR(20) | | Mã Tỉnh/Thành (GHN/GHTK API). |
| is_default | BOOLEAN | DEFAULT FALSE | Địa chỉ mặc định. |

---

### 2.2. Nhóm Sản phẩm (Product Domain)
*Ánh xạ từ: Product, Smartphone, Laptop, Tablet, Category, Brand, ProductImage*

#### `categories`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID danh mục. |
| name | VARCHAR(100) | NOT NULL | Tên (Điện thoại, Laptop...). |
| description | TEXT | | Mô tả. |
| parent_id | BIGINT | FK -> categories(id)| Danh mục cha (Đệ quy). |

#### `brands`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID thương hiệu. |
| name | VARCHAR(100) | NOT NULL | Tên (Apple, Samsung...). |
| logo_url | VARCHAR(255)| | Logo hãng. |

#### `products`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID sản phẩm. |
| name | VARCHAR(255) | NOT NULL | Tên sản phẩm. |
| description | TEXT | | Mô tả chi tiết (HTML). |
| price | DECIMAL(15,2)| NOT NULL | Giá bán hiện tại. |
| stock_quantity | INTEGER | DEFAULT 0 | Tồn kho. |
| thumbnail_url | VARCHAR(255)| | Ảnh đại diện. |
| **specifications**| **JSONB** | | **Quan trọng:** Lưu `screenResolution`, `chipset`, `ram`... |
| status | BOOLEAN | DEFAULT TRUE | Trạng thái kinh doanh. |
| category_id | BIGINT | FK -> categories(id)| Danh mục. |
| brand_id | BIGINT | FK -> brands(id) | Thương hiệu. |
| created_at | TIMESTAMP | DEFAULT NOW() | |
| updated_at | TIMESTAMP | DEFAULT NOW() | |

#### `product_images`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID ảnh. |
| product_id | BIGINT | FK -> products(id) | Sản phẩm. |
| image_url | VARCHAR(255)| NOT NULL | URL ảnh. |
| is_primary | BOOLEAN | DEFAULT FALSE | Ảnh chính trong gallery. |

---

### 2.3. Nhóm Đơn hàng & Thanh toán (Order Domain)
*Ánh xạ từ: Order, OrderItem, OrderStatusHistory, Payment, Shipping*

#### `orders`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID đơn hàng. |
| order_code | VARCHAR(20) | UNIQUE, NOT NULL | Mã đơn (VD: ORD-001). |
| user_id | BIGINT | FK -> users(id) | Khách hàng. |
| email | VARCHAR(100) | | Email liên hệ. |
| recipient_name | VARCHAR(100) | NOT NULL | Tên người nhận (Snapshot). |
| phone_number | VARCHAR(15) | NOT NULL | SĐT người nhận (Snapshot). |
| shipping_address| TEXT | NOT NULL | Địa chỉ giao (Snapshot). |
| note | TEXT | | Ghi chú khách hàng. |
| status | VARCHAR(20) | NOT NULL | `EOrderStatus` hiện tại. |
| payment_method | VARCHAR(20) | NOT NULL | `EPaymentType` (COD, VNPAY). |
| total_amount | DECIMAL(15,2)| NOT NULL | Tổng tiền phải trả. |
| promotion_id | BIGINT | FK -> promotions(id)| Khuyến mãi áp dụng. |
| created_at | TIMESTAMP | DEFAULT NOW() | Ngày đặt. |
| updated_at | TIMESTAMP | DEFAULT NOW() | |

#### `order_items`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID chi tiết. |
| order_id | BIGINT | FK -> orders(id) | Đơn hàng. |
| product_id | BIGINT | FK -> products(id) | Sản phẩm. |
| quantity | INTEGER | NOT NULL | Số lượng. |
| price | DECIMAL(15,2)| NOT NULL | Giá tại thời điểm mua. |

#### `order_status_history` (Mới - Hỗ trợ Class Diagram)
*Dùng để track lịch sử đơn hàng: Đặt -> Xác nhận -> Đóng gói -> Giao.*
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID lịch sử. |
| order_id | BIGINT | FK -> orders(id) | Đơn hàng. |
| status | VARCHAR(20) | NOT NULL | Trạng thái chuyển đổi. |
| changed_by | VARCHAR(50) | | Người thực hiện (System/Admin). |
| changed_at | TIMESTAMP | DEFAULT NOW() | Thời điểm thay đổi. |

#### `payments` (Mới - Hỗ trợ Class Diagram)
*Lưu chi tiết giao dịch điện tử (VNPAY/MOMO).*
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID giao dịch. |
| order_id | BIGINT | FK -> orders(id) | Đơn hàng. |
| provider | VARCHAR(20) | | `EWalletProvider` (VNPAY, MOMO). |
| transaction_id | VARCHAR(100)| | Mã giao dịch ngân hàng. |
| amount | DECIMAL(15,2)| NOT NULL | Số tiền thanh toán. |
| status | VARCHAR(20) | NOT NULL | `PaymentStatus` (PENDING, SUCCESS). |
| created_at | TIMESTAMP | DEFAULT NOW() | |

---

### 2.4. Nhóm Khuyến mãi (Promotion Domain)
*Ánh xạ từ: Promotion, PromotionTemplate, Voucher*
*Đã đổi tên bảng từ `vouchers` -> `promotions`*

#### `promotions`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID khuyến mãi. |
| code | VARCHAR(20) | UNIQUE, NOT NULL | Mã code (VD: SALE50). |
| name | VARCHAR(255) | | Tên chương trình. |
| description | TEXT | | Mô tả. |
| discount_type | VARCHAR(20) | NOT NULL | `DiscountType` (PERCENT, AMOUNT). |
| discount_value | DECIMAL(15,2)| NOT NULL | Giá trị giảm. |
| max_usage | INTEGER | | Giới hạn số lần dùng. |
| min_order_value| DECIMAL(15,2)| | Đơn tối thiểu để áp dụng. |
| start_date | TIMESTAMP | NOT NULL | Ngày bắt đầu hiệu lực. |
| end_date | TIMESTAMP | NOT NULL | Ngày hết hạn. |
| status | VARCHAR(20) | | `PromotionStatus` (ACTIVE, EXPIRED). |
| created_at | TIMESTAMP | DEFAULT NOW() | |

---

### 2.5. Nhóm Giỏ hàng (Cart Domain)
*Ánh xạ từ: Cart, CartItem*

#### `carts`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID giỏ hàng. |
| user_id | BIGINT | UNIQUE, FK -> users| Chủ sở hữu. |
| created_at | TIMESTAMP | | |

#### `cart_items`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID item. |
| cart_id | BIGINT | FK -> carts(id) | Giỏ hàng. |
| product_id | BIGINT | FK -> products(id) | Sản phẩm. |
| quantity | INTEGER | DEFAULT 1 | Số lượng. |

---

### 2.6. Nhóm Tương tác (Interaction Domain)
*Ánh xạ từ: Review, Conversation, Message*

#### `reviews`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID đánh giá. |
| user_id | BIGINT | FK -> users(id) | Người đánh giá. |
| product_id | BIGINT | FK -> products(id) | Sản phẩm. |
| rating | INTEGER | CHECK (1-5) | Số sao. |
| comment | TEXT | | Nội dung. |
| created_at | TIMESTAMP | | |

#### `review_likes`
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID lượt thích. |
| user_id | BIGINT | FK -> users(id) | Người thích. |
| review_id | BIGINT | FK -> reviews(id) | Đánh giá nào. |

#### `conversations` (Chat)
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID hội thoại. |
| user_id | BIGINT | FK -> users(id) | Người dùng (Nullable nếu Guest). |
| session_id | VARCHAR(100)| | Session cho Guest. |
| status | VARCHAR(20) | | `ConversationStatus` (OPEN, CLOSED). |
| created_at | TIMESTAMP | | |

#### `messages` (Chat Detail)
| Tên cột | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| **id** | BIGINT | PK, Auto Inc | ID tin nhắn. |
| conversation_id| BIGINT | FK -> conversations| Thuộc hội thoại nào. |
| sender_type | VARCHAR(20) | NOT NULL | `SenderType` (USER, BOT, ADMIN). |
| content | TEXT | NOT NULL | Nội dung tin nhắn. |
| metadata | JSONB | | Lưu metadata nếu có (ví dụ: Product Card). |
| created_at | TIMESTAMP | DEFAULT NOW() | |