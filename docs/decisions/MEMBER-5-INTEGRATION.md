# Member 5 — API và tích hợp

## API do Member 5 sở hữu

| API | Quyền | Mục đích |
|---|---|---|
| `POST /api/vouchers/apply` `{ "code": "WELCOME" }` | CUSTOMER | Xem mức giảm dựa trên giỏ hàng hiện tại; không tăng lượt dùng |
| `GET /api/admin/vouchers` | ADMIN | Danh sách Voucher |
| `GET /api/admin/vouchers/{id}` | ADMIN | Xem một Voucher |
| `POST /api/admin/vouchers` | ADMIN | Tạo Voucher |
| `PUT /api/admin/vouchers/{id}` | ADMIN | Sửa giới hạn và cấu hình Voucher |
| `DELETE /api/admin/vouchers/{id}` | ADMIN | Xóa nếu chưa được dùng; nếu đã gắn với đơn thì tắt để giữ lịch sử |
| `GET /api/admin/users` | ADMIN | Tìm/lọc tài khoản theo tên, email, role, status; có phân trang |
| `PUT /api/admin/users/{id}/status` `{ "status": "LOCKED" }` | ADMIN | Khóa/mở tài khoản; không cho tự khóa hoặc khóa Admin đang hoạt động cuối cùng |
| `GET /api/admin/reviews?status=PENDING` | ADMIN | Danh sách Review cần duyệt; hỗ trợ `APPROVED`, `HIDDEN`, `ALL` |
| `PUT /api/admin/reviews/{id}/status` `{ "status": "APPROVED" }` | ADMIN | Duyệt hoặc ẩn Review |
| `GET /api/admin/dashboard` | ADMIN | KPI và doanh thu 7 ngày |

Trang apply không nhận `subtotal` từ browser. Backend tự tính từ giỏ hàng và giá trong database.

## Voucher và Checkout (Member 4)

- `VoucherService.redeem(code, subtotal)` là nghiệp vụ dùng trong transaction Checkout. Nó khóa Voucher bằng `SELECT ... FOR UPDATE`, kiểm tra active/hạn/tối thiểu/lượt, tăng `used_count`, rồi trả discount. `@Transactional` dùng propagation mặc định `REQUIRED` để cùng transaction của `OrderService.checkout`.
- `preview(email, code)` chỉ kiểm tra và tính trước; tuyệt đối không tăng `used_count`.
- `CheckoutVoucherPort` và bean `VoucherCheckoutConfiguration` nằm trong nhánh Member 5; bean trỏ thẳng tới `VoucherService.redeem`. Nhánh Member 4 định nghĩa cùng interface để checkout biên dịch độc lập; khi PRs được tích hợp, giữ duy nhất bản interface này. Bean đó thay fallback `UnavailableVoucherAdapter`; không dùng `REQUIRES_NEW`.
- Đơn rollback thì lượt Voucher, Order, OrderItems, tồn kho và giỏ hàng phải rollback cùng nhau. Hủy đơn không hoàn lại lượt dùng theo quy ước hiện tại.

## User và Review (Member 1/3)

- Controller Admin nhận email từ `Authentication.getName()`. Member 1 cần đặt email đã chuẩn hóa trong JWT subject/principal và bảo vệ `/api/admin/**` bằng role ADMIN; `/api/vouchers/apply` yêu cầu CUSTOMER. Các service vẫn tự xác minh user ACTIVE/role từ bảng `users`.
- Schema Voucher/User/Review dùng V1 hiện có; Member 5 không tạo bảng hay sửa V1.
- V1 đặt status Review mặc định `PENDING`. Để màn hình moderation có việc cần làm, Member 3 cần lưu Review mới và Review sửa lại ở trạng thái `PENDING`. Nhánh `feature/client-review` hiện lưu `APPROVED` ngay và có migration chuyển PENDING sang APPROVED; cần thống nhất sửa luồng đó trước demo moderation.
- Controller Member 3 hiện xác thực qua `HttpSession`, trong khi cart/checkout và Member 5 dùng `Authentication`; nhóm cần thống nhất session/JWT với Member 1 trước khi end-to-end.

## Định nghĩa Dashboard

- Khách hoạt động: `users.role=CUSTOMER AND status=ACTIVE`.
- Sản phẩm đang bán: `products.status=ACTIVE`.
- Doanh thu chỉ tính Order `DELIVERED`; chuỗi theo ngày dựa vào thời điểm trạng thái DELIVERED trong `order_status_history`, múi giờ `Asia/Ho_Chi_Minh`.
- Biến thể sắp hết hàng: `stock_qty <= 5` thuộc sản phẩm ACTIVE. Ngưỡng 5 là quyết định triển khai ban đầu.

## Database migrations

M5 dùng bảng `vouchers`, `users`, `reviews`, `orders`, `products`, `product_variants`, `order_status_history` đã khai báo ở V1; module không thêm migration. Không sửa V1 sau khi thành viên đã chạy migration.
