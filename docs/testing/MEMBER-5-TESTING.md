# Checklist kiểm thử — Member 5

## Unit tests

- Phần trăm giảm được làm tròn hai chữ số.
- Discount cố định hoặc phần trăm không làm tổng tiền âm.
- Voucher hết hạn, hết lượt, bị tắt hoặc đơn chưa đạt tối thiểu bị từ chối.

## Cần chạy với PostgreSQL sau khi các nhánh tích hợp

1. `POST /api/vouchers/apply` trả discount từ subtotal server tính, không đổi `used_count`.
2. Checkout dùng mã hợp lệ: discount khớp, `used_count` tăng đúng một, order có `voucher_id`.
3. Hai checkout đồng thời dùng lượt cuối: chỉ một đơn được áp dụng lượt cuối.
4. Lỗi stock/order sau khi Voucher reserve lượt: toàn transaction rollback, `used_count` không đổi.
5. Voucher hết hạn/hết lượt/không đủ tối thiểu/bị tắt/trùng code: API trả lỗi phù hợp.
6. Customer hoặc user LOCKED gọi API Admin bị từ chối; Admin khóa/mở CUSTOMER được.
7. Không cho Admin tự khóa; không cho khóa Admin ACTIVE cuối cùng.
8. Review PENDING xuất hiện trong danh sách; đổi APPROVED thì public API Member 3 được phép hiển thị; đổi HIDDEN thì không hiển thị.
9. Dashboard trả số liệu đúng khi chưa có đơn, có đơn PENDING, DELIVERED, CANCELLED; doanh thu chỉ tính DELIVERED.

Ghi lại lệnh/test, dữ liệu dùng và kết quả pass/fail khi chạy tích hợp. Unit tests không thay cho các ca PostgreSQL/transaction ở trên.
