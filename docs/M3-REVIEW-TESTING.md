# Kiểm thử đăng nhập demo và đánh giá

Database local dùng PostgreSQL trong `docker-compose.yml`. Flyway chạy các migration V1–V4 khi backend khởi động; V2 tạo sản phẩm mẫu, V3 tạo hai tài khoản Customer demo và đơn hàng đã giao cho một trong hai tài khoản, V4 bổ sung thông tin fit cho review.

## Chạy local

Từ thư mục gốc:

```powershell
docker compose up -d database
```

Terminal backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Terminal frontend:

```powershell
cd frontend
npm install
npm run dev
```

Mở `http://localhost:5173`, chọn sản phẩm **Áo thun cotton Essential**, rồi chọn **Đăng nhập demo**.

| Tài khoản demo | Điều kiện |
|---|---|
| `demo.customer@fashion.local` | Có đơn `DELIVERED` chứa sản phẩm áo thun mẫu; có thể tạo và sửa review. |
| `demo.noorder@fashion.local` | Không có đơn mua; API từ chối tạo/sửa review với HTTP 403. |

Đăng nhập demo không cần mật khẩu và chỉ chấp nhận hai địa chỉ được seed ở migration V3. Session được lưu phía server. Đây là luồng giả lập để kiểm thử module, không thay thế luồng đăng nhập/JWT của hệ thống thật.

## Hành vi review
- All valid reviews are automatically approved and published after purchase and input validation.

- Chưa đăng nhập: tạo/sửa review bị từ chối với HTTP 401.
- Đã đăng nhập nhưng chưa có đơn giao thành công chứa sản phẩm: bị từ chối với HTTP 403.
- Một tài khoản chỉ có một review cho mỗi sản phẩm; ràng buộc unique hiện có trong V1 và API trả HTTP 409 nếu gửi lần hai.
- Chỉ người đã đăng nhập và có đơn `DELIVERED` chứa sản phẩm mới thấy form gửi/sửa review. Danh sách review đã duyệt vẫn hiển thị công khai.
- Form yêu cầu chọn variant thuộc đơn đã giao; kích cỡ và màu được lấy từ variant đó. Chiều cao (100–250 cm) và cân nặng (20–300 kg) là tùy chọn. Thông tin này hiện trên review công khai đã được duyệt.
- Điểm trung bình và số review sản phẩm chỉ tính review `APPROVED`; màn chi tiết tải lại tổng hợp sau khi gửi/sửa để sao được cập nhật ngay.
- Tài khoản khác không thể sửa review đó. Review công khai chỉ gồm trạng thái `APPROVED`.
- Review photo upload: JPEG, PNG, or WebP up to 5 MB; files are stored locally and the review stores their URL.

