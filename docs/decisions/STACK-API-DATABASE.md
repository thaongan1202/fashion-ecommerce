# Stack, database và API contract — bản chốt khởi đầu

Phạm vi: dựng skeleton và đóng băng các hợp đồng để năm thành viên làm song song. Đây là quyết định triển khai theo blueprint; các quy tắc chưa được blueprint định nghĩa được đánh dấu rõ bên dưới để nhóm xác nhận.

## Stack

- Backend: Java 17, Spring Boot 3.5.x, Maven Wrapper.
- Frontend: React + TypeScript + Vite, Node.js 22, React Router, CSS thường.
- Database: PostgreSQL 17; Flyway quản lý migration.
- IDE khuyến nghị: VS Code với Extension Pack for Java và Spring Tools. STS/Eclipse cũng dùng được; IDE không ảnh hưởng source.
- Chạy database bằng Compose file của repo; chỉ container hóa database, không container hóa app.

## Quy ước API chung

- Prefix `/api`; JSON UTF-8.
- Trang: query `page` bắt đầu từ 0, `size` mặc định 12, tối đa 100; `sort` dạng `field,asc|desc`.
- Thành công: trả resource hoặc object kết quả có cấu trúc ổn định. Lỗi: `{ "status": 400, "code": "VALIDATION_ERROR", "message": "...", "fieldErrors": { "email": "..." } }`; fieldErrors có thể vắng mặt.
- ID dùng số nguyên dương; tiền VND lưu `NUMERIC(12,2)`, API trả number. Backend là nguồn tính giá, stock, discount và total; không tin tổng do browser gửi.
- Email được trim/lowercase trước khi lưu; database có unique index không phân biệt hoa thường.
- Thời gian lưu UTC; ngày hết hạn Voucher theo ngày địa phương Việt Nam do nhóm cần xác nhận trước khi code nghiệp vụ.

## Endpoint và owner

| Nhóm | Endpoint tối thiểu | Auth | Owner / phối hợp |
|---|---|---|---|
| Health | `GET /api/health` | Public | Setup owner |
| Auth | `POST /api/auth/register` (request email OTP), `POST /api/auth/register/verify-otp`, `POST /api/auth/login` | Public | Member 1 |
| Profile | `GET, PUT /api/users/me` | Customer | Member 1 |
| Address | `GET, POST /api/users/me/addresses`; `PUT, DELETE /api/users/me/addresses/{addressId}` | Customer | Member 1 |
| Catalog read | `GET /api/categories`, `/api/brands` | Public | Member 2 |
| Catalog admin | CRUD `/api/admin/categories`, `/api/admin/brands` | Admin | Member 2 |
| Product read | `GET /api/products`, `/api/products/{productId}`, `/api/products/{productId}/related` | Public | Member 3; Member 2 cung cấp model/filter |
| Product admin | CRUD `/api/admin/products`, `/api/admin/products/{productId}/variants`; ảnh qua `/api/admin/products/{productId}/images` | Admin | Member 2 |
| Review | `GET, POST /api/products/{productId}/reviews` | GET public, POST Customer | Member 3 |
| Review moderation | `GET /api/admin/reviews?status=PENDING`; `PUT /api/admin/reviews/{reviewId}/status` | Admin | Member 5; Member 3 cung cấp model |
| Cart | `GET /api/cart`; `POST /api/cart/items`; `PUT, DELETE /api/cart/items/{itemId}` | Customer | Member 4 |
| Voucher | `POST /api/vouchers/apply` với `{ "code": "..." }`; CRUD `/api/admin/vouchers` | Customer / Admin | Member 5; tích hợp Member 4 |
| Customer order | `POST, GET /api/orders`; `GET /api/orders/{orderId}`; `PUT /api/orders/{orderId}/cancel` | Customer, chủ đơn | Member 4 |
| Admin order | `GET /api/admin/orders`; `PUT /api/admin/orders/{orderId}/status` | Admin | Member 4 |
| Admin user | `GET /api/admin/users`; `PUT /api/admin/users/{userId}/status` | Admin | Member 5; Member 1 sở hữu User/auth |
| Dashboard | `GET /api/admin/dashboard` | Admin | Member 5; dữ liệu Member 2/4 |

### Payload liên module quan trọng

- `POST /api/cart/items`: `{ "variantId": 123, "quantity": 2 }`.
- `POST /api/vouchers/apply`: `{ "code": "WELCOME" }`; backend tự lấy cart đang đăng nhập và trả `{ "valid": true, "discountAmount": 50000, "totalAfterDiscount": 450000 }` hoặc lỗi nghiệp vụ.
- `POST /api/orders`: `{ "addressId": 44, "voucherCode": "WELCOME" }`; Member 4 sở hữu toàn transaction và gọi service Voucher của Member 5 bên trong transaction đó. Chỉ tăng `used_count` sau khi Order/Items tạo thành công; rollback hết khi lỗi.
- Admin status update gửi `{ "status": "PROCESSING", "note": "..." }`; service kiểm tra transition hợp lệ.
- Review status update gửi `{ "status": "APPROVED" }` hoặc `HIDDEN`.

### Customer registration verification

- `POST /api/auth/register` accepts `fullName`, `phone`, `addressLine`, `email`, and `password`; it sends a six-digit OTP and returns `202 Accepted`. It does not create a user yet.
- Phone is exactly 10 digits beginning with `0`. Email must be a Gmail address. Password length is 8–32 characters and requires uppercase, lowercase, digit, and special character.
- `POST /api/auth/register/verify-otp` accepts `{ "email": "customer@gmail.com", "otp": "123456" }`. A valid OTP expires after 60 seconds; after expiry, requesting `/register` again sends a replacement OTP. A verified request creates the CUSTOMER account and its default address in one database transaction.
- OTPs and pending passwords are stored as hashes. The application sends mail from the sender configured by `MAIL_USERNAME`; keep sender credentials in environment variables, never in Git.

## Quyết định database đã áp dụng trong migration đầu

- 14 bảng theo blueprint; tên bảng snake_case số nhiều.
- Product status: `ACTIVE`, `INACTIVE`, `DELETED`; Variant size/color nullable; mọi Product cần ít nhất một Variant (kiểm tra ở service/admin flow).
- Order status: `PENDING`, `PROCESSING`, `SHIPPING`, `DELIVERED`, `CANCELLED`; COD là payment method hiện thực duy nhất.
- Voucher thêm `is_active`, vì yêu cầu Admin phải tắt mã nhưng bảng blueprint chưa có field trạng thái. Mã voucher unique. Admin tắt thay vì xóa cứng voucher đã được dùng.
- Review status: `PENDING`, `APPROVED`, `HIDDEN`; review mới chờ duyệt. Đây là lựa chọn cần nhóm xác nhận vì blueprint để moderation là SHOULD và chưa chốt visibility mặc định.
- Ràng buộc stock/rating/quantity, SKU unique, review unique user-product, một Cart/user, tối đa một default address/user, FK restrict/cascade và index truy vấn phổ biến được khai báo trong V1 migration.
- User bị khóa không thể đăng nhập; ảnh upload lưu local trong giai đoạn demo và database lưu URL. Chốt kích thước/loại file với Member 2.

## Handoff cần từng owner xác nhận

- Member 1: `User.role/status`, JWT claims, login response và xử lý user bị khóa.
- Member 2: Product/Variant field, ProductImage upload, status và filter fields.
- Member 3: Review status/visibility và điều kiện đơn Delivered.
- Member 4: gọi Voucher service trong transaction Order; state transition và hoàn kho khi hủy.
- Member 5: Voucher active/expiry/usage rules; Admin list/status cho User và Review.

Nếu có thay đổi schema sau migration V1, tạo migration Flyway mới; không sửa V1 sau khi thành viên khác đã chạy migration.
