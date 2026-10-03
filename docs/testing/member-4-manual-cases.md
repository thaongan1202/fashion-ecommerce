# Kiểm thử thủ công — Cart, Checkout và Order

Chạy các ca sau trên môi trường tích hợp đã bật PostgreSQL, đăng nhập Customer/Admin và có dữ liệu sản phẩm/địa chỉ. Ghi kết quả Pass/Fail và mã đơn tạo ra để đối chiếu lịch sử.

| ID | Thao tác | Kết quả mong đợi |
|---|---|---|
| M4-CART-01 | Thêm một variant với số lượng hợp lệ; thêm lại cùng variant | Có một dòng trong giỏ, số lượng được cộng dồn |
| M4-CART-02 | Sửa số lượng và xóa dòng khỏi giỏ | Giỏ trả số lượng/tổng mới; xóa xong không còn dòng |
| M4-CART-03 | Thêm hoặc sửa số lượng lớn hơn tồn kho | API từ chối; giỏ và kho không đổi |
| M4-CART-04 | Mở trang chi tiết sản phẩm, chọn variant còn hàng và số lượng hợp lệ | POST `/api/cart/items` gửi đúng `variantId`, `quantity`; giao diện báo thêm thành công |
| M4-CART-05 | Chọn variant hết hàng hoặc nhập số lượng vượt kho | Nút bị khóa hoặc giao diện hiện lỗi tồn kho; database không đổi |
| M4-ORDER-01 | Checkout khi giỏ trống | Trả lỗi nghiệp vụ; không tạo đơn |
| M4-ORDER-02 | Checkout dùng addressId của người dùng khác | Trả lỗi địa chỉ; không tạo đơn |
| BR-PROD-03 | Thay tồn kho để giỏ có số lượng lớn hơn kho, rồi checkout | Từ chối đặt hàng; không có Order/OrderItem; kho không âm |
| M4-ORDER-03 | Checkout COD hợp lệ | Tạo Order PENDING, snapshot OrderItems/địa chỉ, ghi history, trừ kho và xóa giỏ |
| M4-ORDER-04 | Cho Voucher service báo lỗi trong checkout | Transaction rollback: không có Order, kho/giỏ/lượt dùng giữ nguyên |
| M4-ORDER-05 | Customer mở đơn của người dùng khác | Trả 404; không lộ dữ liệu |
| BR-ORDER-01 | Customer hủy đơn PENDING | Chuyển CANCELLED, ghi history và hoàn kho đúng một lần |
| BR-ORDER-02 | Customer hủy đơn PROCESSING/SHIPPING/DELIVERED/CANCELLED | Trả 409; trạng thái và kho không đổi |
| M4-ORDER-06 | Admin chuyển PENDING → PROCESSING → SHIPPING → DELIVERED | Mỗi lần đổi được lưu vào lịch sử trạng thái |
| M4-ORDER-07 | Admin thử chuyển DELIVERED → PROCESSING hoặc trạng thái lạ | Trả 409; đơn giữ nguyên trạng thái |
| NFR-AUTH-04 | Mở Cart/Checkout/Orders khi chưa đăng nhập | Frontend không gửi request tới API cần xác thực; yêu cầu đăng nhập |
| NFR-AUTH-05 | Gọi API Cart/Order trực tiếp không có Bearer token | Backend trả 401 |
| NFR-AUTH-06 | Gọi API Admin Order bằng JWT CUSTOMER | Backend trả 403; Admin JWT có thể xem/cập nhật theo quyền |
| M4-E2E-01 | Đăng nhập → chọn sản phẩm/variant → thêm giỏ → sửa số lượng/xóa dòng → checkout → mở lịch sử/chi tiết đơn → Admin duyệt trạng thái | Payload, tổng tiền, trạng thái và history đúng; quyền theo role; trừ kho một lần |

**Lưu ý kiểm thử:** endpoint Cart/Order dùng Bearer JWT với `sub` là userId; endpoint Admin kiểm tra role `ADMIN`. Voucher checkout cần bean thật của Member 5; không nhập voucher trong E2E nếu bean đó chưa được tích hợp.
