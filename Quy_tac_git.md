# QUY TẮC GIT CHO NHÓM (5 THÀNH VIÊN)

Tài liệu này để trong repo (đặt tên `CONTRIBUTING.md` hoặc dán vào cuối `README.md` chính đều được). Mục tiêu: mỗi người code trên nhánh riêng, tránh đụng code nhau, dễ review.

---

## 1. Cấu trúc nhánh

| Nhánh | Vai trò | Ai được push trực tiếp? |
| --- | --- | --- |
| `main` | Bản ổn định, dùng để demo/nộp bài | **Không ai push trực tiếp** — chỉ merge từ `dev` vào cuối mỗi Milestone |
| `dev` | Nhánh tích hợp chung | **Không push trực tiếp** — chỉ nhận code qua Pull Request đã được duyệt |
| `feature/<module>-<mô-tả>` | Nhánh làm việc cá nhân, mỗi người tạo cho từng chức năng đang làm | Chính chủ nhánh đó |
| `fix/<mô-tả>` | Nhánh sửa lỗi phát sinh (bug sau khi đã merge) | Người sửa lỗi |

**Nguyên tắc quan trọng nhất:** không ai code trực tiếp trên `main` hoặc `dev`. Mọi thay đổi đều đi qua 1 nhánh `feature/*` hoặc `fix/*` của riêng mình, rồi mới mở Pull Request (PR) để gộp vào `dev`.

---

## 2. Tên nhánh theo từng thành viên

Đặt tên nhánh theo module mình phụ trách để cả nhóm nhìn tên là biết ai đang làm gì:

| Thành viên | Module phụ trách | Prefix nhánh gợi ý |
| --- | --- | --- |
| Member 1 | Auth, Hồ sơ, Địa chỉ | `feature/auth-...` |
| Member 2 | Category, Brand, Product, Variant, Ảnh | `feature/catalog-...` |
| Member 3 | Danh sách/tìm kiếm/chi tiết sản phẩm, Review (viết + kiểm duyệt) | `feature/product-...`, `feature/review-...` |
| Member 4 | Cart, Checkout, Order | `feature/cart-...`, `feature/order-...` |
| Member 5 | Voucher, Dashboard, Quản lý người dùng | `feature/voucher-...`, `feature/dashboard-...`, `feature/user-admin-...` |

Ví dụ tên nhánh cụ thể:
```
feature/auth-register
feature/auth-jwt-login
feature/catalog-product-variant-crud
feature/product-search-filter
feature/review-report-abuse
feature/cart-add-item
feature/order-checkout-cod
feature/voucher-apply-checkout
feature/dashboard-revenue-chart
fix/order-cancel-wrong-status
```

Quy tắc: chữ thường, cách nhau bằng dấu `-`, không dấu tiếng Việt, không khoảng trắng.

---

## 3. Setup lần đầu (mỗi người làm 1 lần)

```bash
git clone <link-repo>
cd <ten-repo>
git checkout dev
git pull origin dev
```

---

## 4. Quy trình làm việc hằng ngày (để tránh xung đột)

### Bước 1 — Trước khi bắt đầu code mỗi ngày: cập nhật `dev`
```bash
git checkout dev
git pull origin dev
```
Làm bước này **mỗi lần ngồi vào code**, kể cả khi đang làm dở nhánh cũ — để biết dev đã có gì mới từ người khác chưa.

### Bước 2 — Tạo nhánh mới cho tính năng đang làm (chỉ tạo 1 lần)
```bash
git checkout -b feature/auth-register
```
Nếu nhánh đã tồn tại từ hôm trước, chỉ cần chuyển qua lại:
```bash
git checkout feature/auth-register
```

### Bước 3 — Code và commit thường xuyên
Đừng dồn cả buổi/cả ngày thành 1 commit. Commit theo từng phần việc nhỏ xong là commit luôn:
```bash
git add .
git commit -m "feat(auth): thêm API đăng ký"
```

### Bước 4 — Trước khi push, đồng bộ lại với `dev`
Đây là bước quan trọng nhất để tránh conflict lớn cuối kỳ — merge `dev` mới nhất vào nhánh của mình **trước khi** đẩy code lên:
```bash
git checkout dev
git pull origin dev
git checkout feature/auth-register
git merge dev
```
Nếu có conflict, xử lý ngay ở bước này (xem mục 8) — lúc này conflict còn nhỏ, dễ sửa hơn nhiều so với để dồn đến cuối.

### Bước 5 — Push nhánh của mình lên GitHub
```bash
git push origin feature/auth-register
```
(Lần đầu push nhánh mới thì dùng `git push -u origin feature/auth-register`, các lần sau chỉ cần `git push`.)

### Bước 6 — Mở Pull Request (PR)
Trên GitHub: mở PR từ `feature/auth-register` → `dev`. Ghi rõ trong mô tả PR: làm gì, test đã chạy chưa, có ảnh hưởng module nào khác không.

### Bước 7 — Nhờ review, không tự merge PR của chính mình
Tag 1 thành viên khác review. Chỉ merge khi có ít nhất 1 người duyệt.

### Bước 8 — Merge vào `dev`, xóa nhánh feature
Sau khi được duyệt, merge PR (nên dùng "Squash and merge" cho gọn lịch sử commit), rồi xóa nhánh `feature/...` đã merge xong — tránh repo có quá nhiều nhánh rác.

---

## 5. Quy tắc commit message

Theo Conventional Commits — dễ đọc, dễ biết ai làm gì:

| Prefix | Dùng khi nào |
| --- | --- |
| `feat:` | Thêm chức năng mới |
| `fix:` | Sửa lỗi |
| `docs:` | Sửa tài liệu (SRS, README...) |
| `refactor:` | Sửa lại code, không đổi chức năng |
| `test:` | Thêm/sửa test case |
| `chore:` | Việc lặt vặt (cấu hình, dọn dẹp, cập nhật thư viện...) |

Ví dụ:
```
feat(product): thêm CRUD variant size/màu
fix(order): sửa lỗi không hoàn kho khi hủy đơn
test(voucher): thêm test case voucher hết hạn
docs(srs): cập nhật chương Admin-side
```

---

## 6. Checklist trước khi mở Pull Request

- [ ] Code chạy được, không lỗi cú pháp
- [ ] Đã tự test qua tính năng mình vừa code (thủ công hoặc chạy test case)
- [ ] Đặt tên biến/hàm đúng convention chung của nhóm
- [ ] Không hardcode dữ liệu (link, mật khẩu, số cố định...)
- [ ] Có xử lý lỗi cơ bản (không để hệ thống crash khi input sai)
- [ ] **Không commit file `.env`, file chứa mật khẩu/API key**
- [ ] Đã merge `dev` mới nhất vào nhánh trước khi push (mục 4, bước 4)

---

## 7. Cách tránh xung đột (mở rộng)

- **Chỉ code trong phạm vi module của mình** theo đúng bảng phân công — hạn chế 2 người cùng sửa chung 1 file.
- Nếu bắt buộc phải sửa 1 file dùng chung (ví dụ file cấu hình chung, file khai báo route tổng)  → **báo nhóm trước** qua group chat, tránh 2 người sửa cùng lúc.
- Pull `dev` ít nhất 1 lần/ngày trước khi bắt đầu code (mục 4, bước 1).
- Commit nhỏ và thường xuyên, không để 1 commit gộp cả tuần code.
- Đặt tên nhánh đúng theo module (mục 2) để cả nhóm biết ai đang đụng vào phần nào, tránh làm trùng việc.

---

## 8. Khi bị conflict — xử lý thế nào

1. Chạy `git status` để xem file nào đang bị conflict.
2. Mở file đó, sẽ thấy đoạn được đánh dấu:
   ```
   <<<<<<< HEAD
   (code của mình)
   =======
   (code từ dev/người khác)
   >>>>>>> dev
   ```
3. Đọc kỹ, quyết định giữ đoạn nào (hoặc giữ cả hai nếu không loại trừ nhau), xóa hết các dòng `<<<<<<<`, `=======`, `>>>>>>>`.
4. Sau khi sửa xong:
   ```bash
   git add <tên-file-vừa-sửa>
   git commit
   git push
   ```
5. Nếu không chắc nên giữ đoạn nào (vì không hiểu code của người kia) — **hỏi trực tiếp người đó**, đừng tự đoán rồi xóa nhầm.

---

## 9. Merge vào `main`

- Chỉ merge `dev` → `main` vào **cuối mỗi Milestone**, khi `dev` đã chạy ổn định.
- Chỉ 1 người trong nhóm (ví dụ trưởng nhóm) thực hiện việc này, tránh nhiều người merge `main` cùng lúc gây rối.

---

## 10. Cheat sheet lệnh Git hay dùng

```bash
git status                        # xem đang ở nhánh nào, có gì thay đổi chưa commit
git checkout <tên-nhánh>          # chuyển sang nhánh khác
git checkout -b <tên-nhánh>       # tạo nhánh mới rồi chuyển sang luôn
git pull origin dev               # lấy code mới nhất từ dev
git add .                         # gom toàn bộ thay đổi để chuẩn bị commit
git commit -m "..."               # lưu lại thay đổi
git push origin <tên-nhánh>       # đẩy nhánh lên GitHub
git log --oneline -10             # xem 10 commit gần nhất, dạng rút gọn
git branch                        # xem danh sách nhánh hiện có ở máy mình
```
