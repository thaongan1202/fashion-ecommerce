# Kiểm thử thủ công — Cart, Checkout và Order

Chạy các ca sau trên môi trường tích hợp đã bật PostgreSQL, đăng nhập Customer/Admin và có dữ liệu sản phẩm/địa chỉ. Ghi kết quả Pass/Fail và mã đơn tạo ra để đối chiếu lịch sử.

| ID | Thao tác | Kết quả mong đợi |
|---|---|---|
| M4-CART-01 | Thêm một variant với số lượng hợp lệ; thêm lại cùng variant | Có một dòng trong giỏ, số lượng được cộng dồn |
| M4-CART-02 | Sửa số lượng và xóa dòng khỏi giỏ | Giỏ trả số lượng/tổng mới; xóa xong không còn dòng |
| M4-CART-03 | Thêm hoặc sửa số lượng lớn hơn tồn kho | API từ chối; giỏ và kho không đổi |
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
| NFR-AUTH-04 | Gọi API Cart/Order khi chưa đăng nhập hoặc gọi Admin API bằng Customer | Trả 401/403 sau khi Member 1 tích hợp security rules |

**Lưu ý tích hợp:** cấu hình hiện tại từ chối mọi endpoint trừ health; các ca API cần Member 1 thêm rule `CUSTOMER` cho `/api/cart/**`, `/api/orders/**` và `ADMIN` cho `/api/admin/orders/**`. Voucher adapter cần được Member 5 thay bằng bean thật để chạy các ca Voucher.
